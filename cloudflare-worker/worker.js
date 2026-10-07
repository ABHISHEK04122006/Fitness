/**
 * ATHGYM Cloudflare Worker API
 * 
 * Endpoints:
 * - Profile Photo (authenticated user):
 *   GET    /v1/profile/photo
 *   PUT    /v1/profile/photo
 *   DELETE /v1/profile/photo
 * 
 * - Exercise Media:
 *   GET    /v1/exercises/{exerciseId}/media/{mediaType}        (public streaming via private R2)
 *   PUT    /v1/admin/exercises/{exerciseId}/media/{mediaType}  (admin only)
 *   DELETE /v1/admin/exercises/{exerciseId}/media/{mediaType}  (admin only)
 */

// Supported media types and allowed MIME types
const SUPPORTED_MEDIA_TYPES = {
  thumbnail: {
    maxBytes: 5 * 1024 * 1024, // 5 MB
    allowedContentTypes: [
      'image/jpeg',
      'image/png',
      'image/webp'
    ]
  },
  animation: {
    maxBytes: 10 * 1024 * 1024, // 10 MB
    allowedContentTypes: [
      'image/gif',
      'image/webp',
      'application/json',
      'application/zip',
      'video/webm',
      'video/mp4'
    ]
  },
  video: {
    maxBytes: 100 * 1024 * 1024, // 100 MB
    allowedContentTypes: [
      'video/mp4',
      'video/webm',
      'video/quicktime'
    ]
  }
};

const MAX_PROFILE_PHOTO_SIZE = 5 * 1024 * 1024; // 5 MB

// Cache policy for instructional exercise media:
// Efficient repeated playback/downloads without creating an indefinite immutable cache
const EXERCISE_MEDIA_CACHE_CONTROL = 'public, max-age=86400, s-maxage=604800, stale-while-revalidate=86400';

/**
 * Standard CORS headers
 */
function getCorsHeaders(extra = {}) {
  return {
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Methods': 'GET, HEAD, PUT, DELETE, OPTIONS',
    'Access-Control-Allow-Headers': 'Authorization, Content-Type, Range',
    'Access-Control-Expose-Headers': 'Content-Length, Content-Range, Accept-Ranges, ETag',
    'Access-Control-Max-Age': '86400',
    ...extra
  };
}

/**
 * Helper to return a JSON response with CORS headers
 */
function jsonResponse(data, status = 200, extraHeaders = {}) {
  return new Response(JSON.stringify(data), {
    status,
    headers: {
      'Content-Type': 'application/json',
      ...getCorsHeaders(extraHeaders)
    }
  });
}

/**
 * Helper to return a plain text error response with CORS headers
 */
function errorResponse(message, status = 400) {
  return new Response(message, {
    status,
    headers: {
      'Content-Type': 'text/plain;charset=UTF-8',
      ...getCorsHeaders()
    }
  });
}

/**
 * Safe validation for exerciseId to prevent path traversal and arbitrary keys
 */
function validateExerciseId(exerciseId) {
  if (!exerciseId || typeof exerciseId !== 'string') {
    return false;
  }
  // Disallow path traversal, directory separators, and arbitrary characters
  // Only alphanumeric, hyphens, and underscores allowed, length 1-128
  const validPattern = /^[a-zA-Z0-9_-]{1,128}$/;
  return validPattern.test(exerciseId);
}

/**
 * Verifies Firebase ID Token using Google Identity Toolkit accounts:lookup API
 * and extracts custom admin claim
 */
async function authenticateFirebaseToken(request, env) {
  const authHeader = request.headers.get('Authorization');
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return {
      authenticated: false,
      response: errorResponse('Missing Firebase ID token', 401)
    };
  }

  const token = authHeader.substring(7).trim();
  if (!token) {
    return {
      authenticated: false,
      response: errorResponse('Missing Firebase ID token', 401)
    };
  }

  const apiKey = env.FIREBASE_API_KEY || 'AIzaSyD-l8AK9B-itxvmdh08L557i3Myfb-N8xE';
  if (!apiKey) {
    console.error('FIREBASE_API_KEY environment variable is not configured');
    return {
      authenticated: false,
      response: errorResponse('Server configuration error', 500)
    };
  }

  try {
    const lookupUrl = `https://identitytoolkit.googleapis.com/v1/accounts:lookup?key=${encodeURIComponent(apiKey)}`;
    const res = await fetch(lookupUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ idToken: token })
    });

    if (!res.ok) {
      return {
        authenticated: false,
        response: errorResponse('Invalid or expired Firebase ID token', 401)
      };
    }

    const data = await res.json();
    if (!data.users || data.users.length === 0) {
      return {
        authenticated: false,
        response: errorResponse('Invalid or expired Firebase ID token', 401)
      };
    }

    const user = data.users[0];
    const uid = user.localId;

    // Verify custom claim: admin == true
    let isAdmin = false;
    if (user.customAttributes) {
      try {
        const claims = JSON.parse(user.customAttributes);
        if (claims.admin === true) {
          isAdmin = true;
        }
      } catch (err) {
        console.warn('Failed to parse user.customAttributes:', err);
      }
    }

    return {
      authenticated: true,
      uid,
      isAdmin,
      user
    };
  } catch (err) {
    console.error('Firebase token verification error:', err);
    return {
      authenticated: false,
      response: errorResponse('Authentication verification failed', 500)
    };
  }
}

/**
 * In-memory idempotency cache for duplicate dispatch protection
 */
export const recentDispatches = new Map();

/**
 * Helper to safely format truncated token for debug logging
 */
export function safeTokenSnippet(token) {
  if (!token || typeof token !== 'string') return '***';
  return token.length > 10 ? `${token.slice(0, 6)}...${token.slice(-4)}` : '***';
}

/**
 * Base64URL encoder
 */
export function base64UrlEncode(strOrBuffer) {
  let base64;
  if (typeof strOrBuffer === 'string') {
    base64 = btoa(unescape(encodeURIComponent(strOrBuffer)));
  } else {
    const bytes = new Uint8Array(strOrBuffer);
    let binary = '';
    for (let i = 0; i < bytes.byteLength; i++) {
      binary += String.fromCharCode(bytes[i]);
    }
    base64 = btoa(binary);
  }
  return base64.replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

/**
 * Converts a PEM private key into a CryptoKey using Web Crypto
 */
export async function importPrivateKey(pemKey) {
  const cleanPem = pemKey
    .replace(/-----BEGIN[ A-Z_-]*-----/g, '')
    .replace(/-----END[ A-Z_-]*-----/g, '')
    .replace(/\s+/g, '');

  const binaryString = atob(cleanPem);
  const bytes = new Uint8Array(binaryString.length);
  for (let i = 0; i < binaryString.length; i++) {
    bytes[i] = binaryString.charCodeAt(i);
  }

  return await crypto.subtle.importKey(
    'pkcs8',
    bytes.buffer,
    {
      name: 'RSASSA-PKCS1-v1_5',
      hash: { name: 'SHA-256' }
    },
    false,
    ['sign']
  );
}

// In-memory cache for OAuth2 access token
export let cachedOAuthToken = null; // { token, expiresAt }
export function resetOAuthTokenCache() {
  cachedOAuthToken = null;
}

/**
 * Generates or returns a cached OAuth 2.0 access token for Google APIs
 */
export async function getGoogleOAuthAccessToken(env) {
  const nowSec = Math.floor(Date.now() / 1000);
  if (cachedOAuthToken && cachedOAuthToken.expiresAt > nowSec + 300) {
    return cachedOAuthToken.token;
  }

  let clientEmail = env.FCM_CLIENT_EMAIL;
  let privateKey = env.FCM_PRIVATE_KEY;
  let projectId = env.FCM_PROJECT_ID || env.FIREBASE_PROJECT_ID || 'fitness-5ee68';

  if (!clientEmail || !privateKey) {
    if (env.FCM_SERVICE_ACCOUNT_KEY) {
      try {
        const sa = JSON.parse(env.FCM_SERVICE_ACCOUNT_KEY);
        clientEmail = clientEmail || sa.client_email;
        privateKey = privateKey || sa.private_key;
        projectId = projectId || sa.project_id;
      } catch (e) {
        console.warn('Failed to parse FCM_SERVICE_ACCOUNT_KEY JSON:', e);
      }
    }
  }

  if (!clientEmail || !privateKey) {
    throw new Error('FCM credentials missing. FCM_CLIENT_EMAIL and FCM_PRIVATE_KEY secrets required.');
  }

  const formattedPrivateKey = privateKey.replace(/\\n/g, '\n');

  const header = { alg: 'RS256', typ: 'JWT' };
  const payload = {
    iss: clientEmail,
    scope: 'https://www.googleapis.com/auth/firebase.messaging https://www.googleapis.com/auth/datastore',
    aud: 'https://oauth2.googleapis.com/token',
    exp: nowSec + 3600,
    iat: nowSec
  };

  const encodedHeader = base64UrlEncode(JSON.stringify(header));
  const encodedPayload = base64UrlEncode(JSON.stringify(payload));
  const signInput = `${encodedHeader}.${encodedPayload}`;

  const cryptoKey = await importPrivateKey(formattedPrivateKey);
  const signatureBuffer = await crypto.subtle.sign(
    'RSASSA-PKCS1-v1_5',
    cryptoKey,
    new TextEncoder().encode(signInput)
  );
  const encodedSignature = base64UrlEncode(signatureBuffer);
  const jwt = `${signInput}.${encodedSignature}`;

  const tokenRes = await fetch('https://oauth2.googleapis.com/token', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    body: new URLSearchParams({
      grant_type: 'urn:ietf:params:oauth:grant-type:jwt-bearer',
      assertion: jwt
    })
  });

  if (!tokenRes.ok) {
    const errText = await tokenRes.text();
    throw new Error(`Google OAuth token exchange failed (${tokenRes.status}): ${errText}`);
  }

  const tokenData = await tokenRes.json();
  const accessToken = tokenData.access_token;
  const expiresIn = tokenData.expires_in || 3600;

  cachedOAuthToken = {
    token: accessToken,
    expiresAt: nowSec + expiresIn
  };

  return accessToken;
}

/**
 * Extracts normalized target device metadata from a Firestore document
 */
export function parseFirestoreDeviceDoc(doc) {
  if (!doc || !doc.name || !doc.fields) return null;
  const match = doc.name.match(/users\/([^/]+)\/(fcmTokens|devices)\/([^/]+)$/);
  if (!match) return null;
  const uid = match[1];
  const collection = match[2];
  const docId = match[3];
  const token = doc.fields.token?.stringValue?.trim();
  if (!token) return null;
  return { uid, collection, docId, token };
}

/**
 * Main handler for POST /v1/admin/notifications/push
 */
export async function handleAdminPushNotification(request, env) {
  // 1. Authenticate caller & verify admin custom claim
  const auth = await authenticateFirebaseToken(request, env);
  if (!auth.authenticated) {
    return auth.response;
  }
  if (!auth.isAdmin) {
    return errorResponse('Forbidden: Admin privileges required', 403);
  }

  // 2. Parse request JSON body
  let body;
  try {
    body = await request.json();
  } catch (e) {
    return errorResponse('Invalid JSON payload in request body', 400);
  }
  if (!body || typeof body !== 'object') {
    return errorResponse('Request body must be a valid JSON object', 400);
  }

  const {
    notificationId,
    targetUids,
    audience,
    title,
    message,
    type,
    actionType,
    actionId,
    destination
  } = body;

  // 3. Validate title and message
  if (!title || typeof title !== 'string' || !title.trim()) {
    return errorResponse('Notification title is required', 400);
  }
  if (!message || typeof message !== 'string' || !message.trim()) {
    return errorResponse('Notification message body is required', 400);
  }
  if (title.trim().length > 100) {
    return errorResponse('Notification title cannot exceed 100 characters', 400);
  }
  if (message.trim().length > 500) {
    return errorResponse('Notification message body cannot exceed 500 characters', 400);
  }

  const cleanNotificationId = (notificationId && typeof notificationId === 'string' && notificationId.trim())
    ? notificationId.trim()
    : `notif_admin_${Date.now()}_${Math.floor(1000 + Math.random() * 9000)}`;

  // 4. Rate & Duplicate Protection (Section 23)
  const existingDispatch = recentDispatches.get(cleanNotificationId);
  if (existingDispatch && (Date.now() - existingDispatch.timestamp < 60000)) {
    console.log(`[FCM] Duplicate request detected for ${cleanNotificationId}, returning cached result`);
    return jsonResponse(existingDispatch.result, 200);
  }

  // 5. Normalize Audience & Target UIDs
  const normAudience = String(audience || '').toLowerCase().trim();
  let audienceType;
  let resolvedUids = [];

  if (normAudience === 'all' || normAudience === 'all_athletes' || normAudience === 'global_announcement') {
    audienceType = 'all';
  } else if (normAudience === 'single' || normAudience === 'single_user') {
    audienceType = 'single';
    if (Array.isArray(targetUids) && targetUids.length > 0) {
      resolvedUids = [String(targetUids[0]).trim()].filter(Boolean);
    } else if (body.uid && typeof body.uid === 'string') {
      resolvedUids = [body.uid.trim()].filter(Boolean);
    }
    if (resolvedUids.length === 0) {
      return errorResponse('Target user UID is required for single recipient audience', 400);
    }
  } else {
    audienceType = 'selected';
    if (Array.isArray(targetUids)) {
      resolvedUids = targetUids.map(u => String(u).trim()).filter(Boolean);
    } else if (body.uid && typeof body.uid === 'string') {
      resolvedUids = [body.uid.trim()].filter(Boolean);
    }
    if (resolvedUids.length === 0) {
      return errorResponse('Please select at least one recipient user', 400);
    }
  }

  console.log(`[FCM] Push request received: audience=${audienceType}, targetCount=${audienceType === 'all' ? 'ALL' : resolvedUids.length}`);
  console.log(`[FCM] Admin verified: uid=${auth.uid}`);

  const projectId = env.FCM_PROJECT_ID || env.FIREBASE_PROJECT_ID || 'fitness-5ee68';

  // 6. Obtain Google OAuth2 Access Token
  let oauthAccessToken;
  try {
    oauthAccessToken = await getGoogleOAuthAccessToken(env);
  } catch (err) {
    console.error('Failed to obtain Google OAuth access token for FCM:', err.message);
    return jsonResponse({
      success: false,
      notificationId: cleanNotificationId,
      status: 'CONFIG_ERROR',
      message: 'Failed to authenticate with FCM backend: ' + err.message
    }, 500);
  }

  // 7. Resolve FCM registration tokens from Firestore
  const tokenMap = new Map(); // tokenString -> { uid, collection, docId, token }
  const firestoreBaseUrl = `https://firestore.googleapis.com/v1/projects/${projectId}/databases/(default)/documents`;

  const queryUserSubcollection = async (uid, collectionId) => {
    try {
      const url = `${firestoreBaseUrl}/users/${encodeURIComponent(uid)}/${collectionId}`;
      const res = await fetch(url, {
        headers: {
          'Authorization': `Bearer ${oauthAccessToken}`
        }
      });
      if (res.ok) {
        const data = await res.json();
        if (Array.isArray(data.documents)) {
          for (const doc of data.documents) {
            const dev = parseFirestoreDeviceDoc(doc);
            if (dev && !tokenMap.has(dev.token)) {
              tokenMap.set(dev.token, dev);
            }
          }
        }
      }
    } catch (e) {
      console.warn(`[WARN] Error fetching ${collectionId} for ${uid}:`, e.message);
    }
  };

  if (audienceType === 'all') {
    // Collection group queries for fcmTokens and devices across all users
    const runCollectionGroupQuery = async (collectionId) => {
      try {
        const queryRes = await fetch(`${firestoreBaseUrl}:runQuery`, {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${oauthAccessToken}`,
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            structuredQuery: {
              from: [{ collectionId, allDescendants: true }]
            }
          })
        });
        if (queryRes.ok) {
          const list = await queryRes.json();
          if (Array.isArray(list)) {
            for (const item of list) {
              const dev = parseFirestoreDeviceDoc(item.document);
              if (dev && !tokenMap.has(dev.token)) {
                tokenMap.set(dev.token, dev);
              }
            }
          }
        } else {
          console.warn(`[WARN] Firestore runQuery failed for ${collectionId}: ${queryRes.status}`);
        }
      } catch (e) {
        console.warn(`[WARN] Error querying ${collectionId} collection group:`, e.message);
      }
    };

    await Promise.allSettled([
      runCollectionGroupQuery('fcmTokens'),
      runCollectionGroupQuery('devices')
    ]);

    // Fallback: If collection group query yielded 0 tokens, and targetUids was supplied
    if (tokenMap.size === 0 && Array.isArray(targetUids) && targetUids.length > 0) {
      console.log(`[FCM] Collection group yielded 0 tokens, querying ${targetUids.length} targetUids directly...`);
      const userPromises = [];
      for (const uid of targetUids) {
        const cleanUid = String(uid).trim();
        if (cleanUid) {
          userPromises.push(queryUserSubcollection(cleanUid, 'fcmTokens'));
          userPromises.push(queryUserSubcollection(cleanUid, 'devices'));
        }
      }
      await Promise.allSettled(userPromises);
    }
  } else {
    // Specific targeted users (single or selected)
    const userPromises = [];
    for (const uid of resolvedUids) {
      userPromises.push(queryUserSubcollection(uid, 'fcmTokens'));
      userPromises.push(queryUserSubcollection(uid, 'devices'));
    }
    await Promise.allSettled(userPromises);
  }

  const allTokens = Array.from(tokenMap.values());
  console.log(`[FCM] Token resolution complete: found=${allTokens.length}`);
  console.log(`[FCM] Tokens found: ${allTokens.length}`);

  if (allTokens.length === 0) {
    const emptyResult = {
      success: true,
      notificationId: cleanNotificationId,
      targetedUsers: audienceType === 'all' ? 0 : resolvedUids.length,
      tokensFound: 0,
      pushAccepted: 0,
      failed: 0,
      invalidTokensRemoved: 0,
      status: 'RECORD_CREATED_NO_DEVICES',
      message: 'Notification record created in Firestore, but no active device tokens were found.'
    };
    recentDispatches.set(cleanNotificationId, { timestamp: Date.now(), result: emptyResult });
    return jsonResponse(emptyResult, 200);
  }

  // 8. Dispatch FCM HTTP v1 messages in batches
  const fcmSendUrl = `https://fcm.googleapis.com/v1/projects/${projectId}/messages:send`;
  const cleanTitle = title.trim();
  const cleanMessage = message.trim();
  const cleanType = String(type || 'GENERAL');
  const cleanDestination = String(destination || 'notifications');

  let pushAccepted = 0;
  let failed = 0;
  let invalidTokensRemoved = 0;
  const staleTokenDeletions = [];

  // Batch tokens (Section 12: at most 500 per batch)
  const BATCH_SIZE = 500;
  for (let b = 0; b < allTokens.length; b += BATCH_SIZE) {
    const batch = allTokens.slice(b, b + BATCH_SIZE);
    const batchIndex = Math.floor(b / BATCH_SIZE) + 1;

    // Concurrency controlled subrequests
    const CONCURRENCY = 25;
    for (let i = 0; i < batch.length; i += CONCURRENCY) {
      const slice = batch.slice(i, i + CONCURRENCY);
      const slicePromises = slice.map(async (item) => {
        const payload = {
          message: {
            token: item.token,
            notification: {
              title: cleanTitle,
              body: cleanMessage
            },
            data: {
              notificationId: String(cleanNotificationId),
              notification_id: String(cleanNotificationId),
              title: String(cleanTitle),
              message: String(cleanMessage),
              body: String(cleanMessage),
              type: cleanType,
              actionType: String(actionType || ''),
              actionId: String(actionId || ''),
              destination: cleanDestination,
              navigate_to: cleanDestination,
              deepLink: `athgym://notifications/${cleanNotificationId}`
            },
            android: {
              priority: 'HIGH',
              notification: {
                channel_id: 'athgym_notifications',
                icon: 'ic_notification',
                color: '#A8FF3E',
                default_sound: true,
                default_vibrate_timings: true,
                notification_priority: 'PRIORITY_MAX'
              }
            }
          }
        };

        try {
          const res = await fetch(fcmSendUrl, {
            method: 'POST',
            headers: {
              'Authorization': `Bearer ${oauthAccessToken}`,
              'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
          });

          if (res.ok) {
            pushAccepted++;
          } else {
            failed++;
            const errText = await res.text().catch(() => '');
            // Check if token is invalid or unregistered (Section 17)
            if (
              res.status === 404 ||
              errText.includes('UNREGISTERED') ||
              errText.includes('NOT_FOUND') ||
              errText.includes('INVALID_ARGUMENT')
            ) {
              invalidTokensRemoved++;
              console.log(`[FCM] Invalid token removed: user=${item.uid}, docId=${item.docId}`);
              const deleteUrl = `${firestoreBaseUrl}/users/${encodeURIComponent(item.uid)}/${item.collection}/${encodeURIComponent(item.docId)}`;
              staleTokenDeletions.push(
                fetch(deleteUrl, {
                  method: 'DELETE',
                  headers: { 'Authorization': `Bearer ${oauthAccessToken}` }
                }).catch(() => {})
              );
            }
          }
        } catch (fetchErr) {
          failed++;
          console.warn('[FCM] Send error for token:', safeTokenSnippet(item.token), fetchErr.message);
        }
      });

      await Promise.allSettled(slicePromises);
    }

    console.log(`[FCM] Batch ${batchIndex} accepted`);
  }

  if (staleTokenDeletions.length > 0) {
    await Promise.allSettled(staleTokenDeletions);
  }

  const correlationId = cleanNotificationId;
  const attemptedCount = allTokens.length;
  const tempFailures = Math.max(0, failed - invalidTokensRemoved);

  console.log(`[FCM-DIAG] correlationId=${correlationId} targetMode=${audienceType} requestedUsers=${audienceType === 'all' ? 'ALL' : resolvedUids.length} tokensFound=${allTokens.length} uniqueTokens=${allTokens.length} attempted=${attemptedCount} accepted=${pushAccepted} permanentErrors=${invalidTokensRemoved} temporaryFailures=${tempFailures} status=${failed === 0 ? 200 : 207}`);
  console.log(`[FCM] Push dispatch complete: accepted=${pushAccepted}, failed=${failed}`);

  const status = (failed === 0)
    ? 'SEND_ACCEPTED'
    : (pushAccepted > 0 ? 'PARTIAL_SUCCESS' : 'PUSH_FAILED');

  const targetedUsersCount = audienceType === 'all'
    ? (Array.isArray(targetUids) && targetUids.length > 0 ? targetUids.length : new Set(allTokens.map(t => t.uid)).size)
    : resolvedUids.length;

  const finalResult = {
    success: true,
    notificationId: cleanNotificationId,
    targetedUsers: targetedUsersCount,
    tokensFound: allTokens.length,
    pushAccepted,
    failed,
    invalidTokensRemoved,
    status,
    message: failed === 0
      ? `Push accepted for ${pushAccepted} device(s).`
      : `Push accepted for ${pushAccepted} device(s), ${failed} failed.`
  };

  recentDispatches.set(cleanNotificationId, { timestamp: Date.now(), result: finalResult });
  if (recentDispatches.size > 200) {
    const cutoff = Date.now() - 60000;
    for (const [k, v] of recentDispatches.entries()) {
      if (v.timestamp < cutoff) recentDispatches.delete(k);
    }
  }

  return jsonResponse(finalResult, 200);
}

/**
 * Main Worker request handler
 */
export default {
  async fetch(request, env, ctx) {
    // Handle CORS preflight
    if (request.method === 'OPTIONS') {
      return new Response(null, {
        status: 204,
        headers: getCorsHeaders()
      });
    }

    const url = new URL(request.url);
    const path = url.pathname;
    const method = request.method;

    // Reject path traversal attempts early (direct .., encoded %2e%2e, or collapsed traversal paths)
    if (
      url.pathname.includes('..') ||
      request.url.toLowerCase().includes('%2e%2e') ||
      (path.includes('/media/') && !path.includes('/exercises/'))
    ) {
      return errorResponse('Path traversal attempt rejected', 400);
    }

    // =========================================================================
    // 0. ADMIN PUSH NOTIFICATION DISPATCH (Cloudflare Worker FCM HTTP v1 Backend)
    //    POST /v1/admin/notifications/push
    // =========================================================================
    if (path === '/v1/admin/notifications/push') {
      if (method !== 'POST') {
        return errorResponse('Method Not Allowed', 405);
      }
      return await handleAdminPushNotification(request, env);
    }

    // Check R2 binding
    if (!env.MEDIA_BUCKET) {
      console.error('MEDIA_BUCKET R2 binding is not available');
      return errorResponse('R2 storage is not configured', 500);
    }

    // =========================================================================
    // 1. PROFILE PHOTO ENDPOINTS (Existing Verified Functionality)
    // =========================================================================
    if (path === '/v1/profile/photo') {
      // All profile photo endpoints require authentication
      const auth = await authenticateFirebaseToken(request, env);
      if (!auth.authenticated) {
        return auth.response;
      }
      const uid = auth.uid;
      const objectKey = `profiles/${uid}/photo`;

      if (method === 'GET') {
        const object = await env.MEDIA_BUCKET.get(objectKey);
        if (!object) {
          return errorResponse('Profile photo not found', 404);
        }

        const headers = getCorsHeaders({
          'Content-Type': object.httpMetadata?.contentType || 'image/jpeg',
          'Cache-Control': 'private, no-cache',
          'ETag': object.httpEtag
        });
        if (object.size != null) {
          headers['Content-Length'] = object.size.toString();
        }

        return new Response(object.body, {
          status: 200,
          headers
        });
      }

      if (method === 'PUT') {
        const rawContentType = request.headers.get('Content-Type') || '';
        const mimeType = rawContentType.split(';')[0].trim().toLowerCase();

        if (!mimeType.startsWith('image/')) {
          return errorResponse('Invalid image format. Content-Type must be an image.', 400);
        }

        // Check Content-Length if present
        const contentLength = parseInt(request.headers.get('Content-Length') || '0', 10);
        if (contentLength > MAX_PROFILE_PHOTO_SIZE) {
          return errorResponse('Selected image exceeds maximum allowed size of 5 MB', 413);
        }

        const body = await request.arrayBuffer();
        if (body.byteLength === 0) {
          return errorResponse('Request body is empty', 400);
        }
        if (body.byteLength > MAX_PROFILE_PHOTO_SIZE) {
          return errorResponse('Selected image exceeds maximum allowed size of 5 MB', 413);
        }

        await env.MEDIA_BUCKET.put(objectKey, body, {
          httpMetadata: {
            contentType: rawContentType || mimeType
          },
          customMetadata: {
            uid,
            uploadedAt: new Date().toISOString()
          }
        });

        return jsonResponse({
          success: true,
          message: 'Profile photo uploaded successfully'
        }, 200);
      }

      if (method === 'DELETE') {
        await env.MEDIA_BUCKET.delete(objectKey);
        return jsonResponse({
          success: true,
          message: 'Profile photo deleted successfully'
        }, 200);
      }

      return errorResponse('Method Not Allowed', 405);
    }

    // =========================================================================
    // 2. EXERCISE MEDIA GET / HEAD: /v1/exercises/{exerciseId}/media/{mediaType}
    // =========================================================================
    const getMediaMatch = path.match(/^\/v1\/exercises\/([^/]+)\/media\/([^/]+)$/);
    if (getMediaMatch) {
      if (method !== 'GET' && method !== 'HEAD') {
        return errorResponse('Method Not Allowed', 405);
      }

      let rawExerciseId = getMediaMatch[1];
      let rawMediaType = getMediaMatch[2];

      try {
        rawExerciseId = decodeURIComponent(rawExerciseId);
        rawMediaType = decodeURIComponent(rawMediaType);
      } catch (e) {
        return errorResponse('Invalid URL encoding', 400);
      }

      // Check for path traversal or invalid exerciseId
      if (!validateExerciseId(rawExerciseId)) {
        return errorResponse('Invalid exerciseId format', 400);
      }

      // Check media type
      if (!SUPPORTED_MEDIA_TYPES[rawMediaType]) {
        return errorResponse(`Invalid media type: ${rawMediaType}. Supported types: thumbnail, animation, video`, 400);
      }

      const objectKey = `exercises/${rawExerciseId}/${rawMediaType}`;

      // Support HTTP Range requests for video playback / streaming
      const rangeHeader = request.headers.get('Range');
      const getOptions = {};
      if (rangeHeader) {
        getOptions.range = request.headers;
      }

      const object = await env.MEDIA_BUCKET.get(objectKey, getOptions);
      if (!object) {
        return errorResponse('Exercise media not found', 404);
      }

      const contentType = object.httpMetadata?.contentType || 'application/octet-stream';
      const headers = getCorsHeaders({
        'Content-Type': contentType,
        'Accept-Ranges': 'bytes',
        'Cache-Control': EXERCISE_MEDIA_CACHE_CONTROL,
        'ETag': object.httpEtag
      });

      // Handle HEAD request (inspect content length and type without streaming body)
      if (method === 'HEAD') {
        if (object.size != null) {
          headers['Content-Length'] = object.size.toString();
        }
        return new Response(null, {
          status: 200,
          headers
        });
      }

      // Handle Range response (206 Partial Content)
      if (object.range) {
        const offset = object.range.offset ?? 0;
        const length = object.range.length ?? (object.size - offset);
        const end = offset + length - 1;

        headers['Content-Range'] = `bytes ${offset}-${end}/${object.size}`;
        headers['Content-Length'] = length.toString();

        return new Response(object.body, {
          status: 206,
          headers
        });
      }

      // Full content response (200 OK)
      if (object.size != null) {
        headers['Content-Length'] = object.size.toString();
      }

      return new Response(object.body, {
        status: 200,
        headers
      });
    }

    // =========================================================================
    // 3. ADMIN EXERCISE MEDIA ENDPOINTS:
    //    PUT    /v1/admin/exercises/{exerciseId}/media/{mediaType}
    //    DELETE /v1/admin/exercises/{exerciseId}/media/{mediaType}
    // =========================================================================
    const adminMediaMatch = path.match(/^\/v1\/admin\/exercises\/([^/]+)\/media\/([^/]+)$/);
    if (adminMediaMatch) {
      if (method !== 'PUT' && method !== 'DELETE') {
        return errorResponse('Method Not Allowed', 405);
      }

      // Require Firebase ID Token
      const auth = await authenticateFirebaseToken(request, env);
      if (!auth.authenticated) {
        return auth.response;
      }

      // Verify custom admin claim: admin == true
      if (!auth.isAdmin) {
        return errorResponse('Forbidden: Admin privileges required', 403);
      }

      let rawExerciseId = adminMediaMatch[1];
      let rawMediaType = adminMediaMatch[2];

      try {
        rawExerciseId = decodeURIComponent(rawExerciseId);
        rawMediaType = decodeURIComponent(rawMediaType);
      } catch (e) {
        return errorResponse('Invalid URL encoding', 400);
      }

      // Validate exerciseId (prevent path traversal / invalid chars)
      if (!validateExerciseId(rawExerciseId)) {
        return errorResponse('Invalid exerciseId format', 400);
      }

      // Validate mediaType
      const mediaConfig = SUPPORTED_MEDIA_TYPES[rawMediaType];
      if (!mediaConfig) {
        return errorResponse(`Invalid media type: ${rawMediaType}. Supported types: thumbnail, animation, video`, 400);
      }

      const objectKey = `exercises/${rawExerciseId}/${rawMediaType}`;

      // PUT: Upload media
      if (method === 'PUT') {
        const rawContentType = request.headers.get('Content-Type') || '';
        const mimeType = rawContentType.split(';')[0].trim().toLowerCase();

        if (!mimeType || !mediaConfig.allowedContentTypes.includes(mimeType)) {
          return errorResponse(
            `Unsupported Content-Type: ${rawContentType || 'empty'} for ${rawMediaType}. Allowed: ${mediaConfig.allowedContentTypes.join(', ')}`,
            400
          );
        }

        // Check Content-Length header early if available
        const contentLength = parseInt(request.headers.get('Content-Length') || '0', 10);
        if (contentLength > mediaConfig.maxBytes) {
          return errorResponse(`Payload exceeds maximum size limit for ${rawMediaType} (${mediaConfig.maxBytes / (1024 * 1024)} MB)`, 413);
        }

        const body = await request.arrayBuffer();
        if (body.byteLength === 0) {
          return errorResponse('Request body is empty', 400);
        }
        if (body.byteLength > mediaConfig.maxBytes) {
          return errorResponse(`Payload exceeds maximum size limit for ${rawMediaType} (${mediaConfig.maxBytes / (1024 * 1024)} MB)`, 413);
        }

        await env.MEDIA_BUCKET.put(objectKey, body, {
          httpMetadata: {
            contentType: rawContentType
          },
          customMetadata: {
            exerciseId: rawExerciseId,
            mediaType: rawMediaType,
            uploadedBy: auth.uid
          }
        });

        return jsonResponse({
          success: true,
          exerciseId: rawExerciseId,
          mediaType: rawMediaType,
          key: objectKey
        }, 200);
      }

      // DELETE: Delete media (idempotent)
      if (method === 'DELETE') {
        await env.MEDIA_BUCKET.delete(objectKey);

        return jsonResponse({
          success: true,
          exerciseId: rawExerciseId,
          mediaType: rawMediaType
        }, 200);
      }
    }

    return errorResponse('Not Found', 404);
  }
};
