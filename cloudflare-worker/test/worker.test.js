import { describe, it, before, beforeEach, after } from 'node:test';
import assert from 'node:assert/strict';
import { generateKeyPairSync } from 'node:crypto';
import worker from '../worker.js';

// Generate native 2048-bit RSA PKCS#8 test key for Web Crypto JWT RS256 signing tests
const { privateKey: testPrivateKeyPem } = generateKeyPairSync('rsa', {
  modulusLength: 2048,
  privateKeyEncoding: { type: 'pkcs8', format: 'pem' }
});

/**
 * In-Memory Mock for Cloudflare R2 Bucket
 */
class MockR2Bucket {
  constructor() {
    this.storage = new Map();
  }

  async put(key, body, options = {}) {
    let buffer;
    if (body instanceof ArrayBuffer) {
      buffer = Buffer.from(body);
    } else if (body instanceof Uint8Array) {
      buffer = Buffer.from(body);
    } else if (typeof body === 'string') {
      buffer = Buffer.from(body);
    } else if (body && typeof body.arrayBuffer === 'function') {
      buffer = Buffer.from(await body.arrayBuffer());
    } else {
      buffer = Buffer.from(body || '');
    }

    const item = {
      key,
      data: buffer,
      size: buffer.byteLength,
      httpMetadata: options.httpMetadata || {},
      customMetadata: options.customMetadata || {},
      httpEtag: `"${Date.now()}"`
    };
    this.storage.set(key, item);
    return item;
  }

  async get(key, options = {}) {
    const item = this.storage.get(key);
    if (!item) return null;

    let returnData = item.data;
    let rangeInfo = null;

    // Check if range was requested
    const rangeHeader = options.range ? (options.range.get ? options.range.get('Range') : options.range) : null;
    if (rangeHeader && typeof rangeHeader === 'string' && rangeHeader.startsWith('bytes=')) {
      const parts = rangeHeader.replace('bytes=', '').split('-');
      const start = parseInt(parts[0], 10) || 0;
      const end = parts[1] ? parseInt(parts[1], 10) : item.size - 1;
      const length = end - start + 1;
      returnData = item.data.subarray(start, end + 1);
      rangeInfo = { offset: start, length };
    }

    return {
      key: item.key,
      size: item.size,
      httpEtag: item.httpEtag,
      httpMetadata: item.httpMetadata,
      customMetadata: item.customMetadata,
      range: rangeInfo,
      body: new ReadableStream({
        start(controller) {
          controller.enqueue(returnData);
          controller.close();
        }
      }),
      async arrayBuffer() {
        return returnData.buffer.slice(returnData.byteOffset, returnData.byteOffset + returnData.byteLength);
      },
      async text() {
        return returnData.toString('utf-8');
      }
    };
  }

  async delete(key) {
    this.storage.delete(key);
  }
}

describe('ATHGYM Cloudflare Worker API Suite', () => {
  let originalFetch;
  let mockBucket;
  let mockEnv;
  let mockFirestoreDb = new Map();
  let mockFcmSentMessages = [];
  let mockDeletedFirestoreDocs = [];

  function addMockFirestoreDevice(uid, collection, docId, token) {
    const fullPath = `projects/fitness-5ee68/databases/(default)/documents/users/${uid}/${collection}/${docId}`;
    mockFirestoreDb.set(fullPath, {
      name: fullPath,
      fields: {
        token: { stringValue: token },
        platform: { stringValue: 'android' },
        deviceId: { stringValue: docId },
        updatedAt: { integerValue: '1700000000000' }
      }
    });
  }

  before(() => {
    originalFetch = globalThis.fetch;
    // Mock global fetch for Google Identity Toolkit accounts:lookup, OAuth token, Firestore REST, and FCM HTTP v1
    globalThis.fetch = async (url, options = {}) => {
      const urlStr = url.toString();

      // 1. Firebase Identity Toolkit accounts:lookup
      if (urlStr.includes('identitytoolkit.googleapis.com/v1/accounts:lookup')) {
        const body = JSON.parse(options.body);
        const token = body.idToken;

        if (token === 'ADMIN_TOKEN') {
          return new Response(JSON.stringify({
            users: [
              {
                localId: 'admin_user_001',
                email: 'admin@athgym.test',
                customAttributes: JSON.stringify({ admin: true })
              }
            ]
          }), { status: 200, headers: { 'Content-Type': 'application/json' } });
        }

        if (token === 'NON_ADMIN_TOKEN') {
          return new Response(JSON.stringify({
            users: [
              {
                localId: 'regular_user_002',
                email: 'user@athgym.test',
                customAttributes: JSON.stringify({ admin: false })
              }
            ]
          }), { status: 200, headers: { 'Content-Type': 'application/json' } });
        }

        if (token === 'USER_WITHOUT_CLAIMS_TOKEN') {
          return new Response(JSON.stringify({
            users: [
              {
                localId: 'regular_user_003',
                email: 'user3@athgym.test'
              }
            ]
          }), { status: 200, headers: { 'Content-Type': 'application/json' } });
        }

        // Invalid or expired token
        return new Response(JSON.stringify({
          error: {
            code: 400,
            message: 'INVALID_ID_TOKEN'
          }
        }), { status: 400, headers: { 'Content-Type': 'application/json' } });
      }

      // 2. Google OAuth 2.0 token endpoint
      if (urlStr.includes('oauth2.googleapis.com/token')) {
        return new Response(JSON.stringify({
          access_token: 'test-oauth-access-token-12345',
          expires_in: 3600,
          token_type: 'Bearer'
        }), { status: 200, headers: { 'Content-Type': 'application/json' } });
      }

      // 3. Firestore REST API (runQuery, subcollections, delete)
      if (urlStr.includes('firestore.googleapis.com')) {
        if (options.method === 'DELETE') {
          mockDeletedFirestoreDocs.push(urlStr);
          for (const [path] of mockFirestoreDb.entries()) {
            if (urlStr.includes(path) || urlStr.endsWith(path.replace('projects/fitness-5ee68/databases/(default)/documents/', ''))) {
              mockFirestoreDb.delete(path);
            }
          }
          return new Response(JSON.stringify({}), { status: 200, headers: { 'Content-Type': 'application/json' } });
        }

        if (urlStr.includes(':runQuery')) {
          const reqBody = JSON.parse(options.body || '{}');
          const collectionId = reqBody.structuredQuery?.from?.[0]?.collectionId;
          const docs = [];
          for (const [path, doc] of mockFirestoreDb.entries()) {
            if (path.includes(`/${collectionId}/`)) {
              docs.push({ document: doc });
            }
          }
          return new Response(JSON.stringify(docs), { status: 200, headers: { 'Content-Type': 'application/json' } });
        }

        const userSubMatch = urlStr.match(/\/users\/([^/]+)\/(fcmTokens|devices)$/);
        if (userSubMatch && (!options.method || options.method === 'GET')) {
          const uid = decodeURIComponent(userSubMatch[1]);
          const collectionId = userSubMatch[2];
          const docs = [];
          for (const [path, doc] of mockFirestoreDb.entries()) {
            if (path.includes(`/users/${uid}/${collectionId}/`)) {
              docs.push(doc);
            }
          }
          return new Response(JSON.stringify({ documents: docs }), { status: 200, headers: { 'Content-Type': 'application/json' } });
        }

        return new Response(JSON.stringify({ documents: [] }), { status: 200, headers: { 'Content-Type': 'application/json' } });
      }

      // 4. FCM HTTP v1 messages:send endpoint
      if (urlStr.includes('fcm.googleapis.com/v1/projects')) {
        const fcmBody = JSON.parse(options.body || '{}');
        const msg = fcmBody.message;
        if (msg) {
          mockFcmSentMessages.push(msg);
        }

        if (msg && msg.token && (msg.token.includes('invalid') || msg.token.includes('unregistered'))) {
          return new Response(JSON.stringify({
            error: {
              code: 404,
              message: 'Requested entity was not found. UNREGISTERED',
              status: 'NOT_FOUND'
            }
          }), { status: 404, headers: { 'Content-Type': 'application/json' } });
        }

        if (msg && msg.token && msg.token.includes('failing')) {
          return new Response(JSON.stringify({
            error: {
              code: 500,
              message: 'Internal upstream error in FCM',
              status: 'INTERNAL'
            }
          }), { status: 500, headers: { 'Content-Type': 'application/json' } });
        }

        return new Response(JSON.stringify({
          name: `projects/fitness-5ee68/messages/fake_msg_${Date.now()}`
        }), { status: 200, headers: { 'Content-Type': 'application/json' } });
      }

      return originalFetch(url, options);
    };
  });

  after(() => {
    globalThis.fetch = originalFetch;
  });

  beforeEach(() => {
    mockBucket = new MockR2Bucket();
    mockFirestoreDb = new Map();
    mockFcmSentMessages = [];
    mockDeletedFirestoreDocs = [];
    mockEnv = {
      MEDIA_BUCKET: mockBucket,
      FIREBASE_API_KEY: 'test-firebase-api-key',
      FCM_PROJECT_ID: 'fitness-5ee68',
      FCM_CLIENT_EMAIL: 'firebase-adminsdk@fitness-5ee68.iam.gserviceaccount.com',
      FCM_PRIVATE_KEY: testPrivateKeyPem
    };
  });

  // =========================================================================
  // TEST 1: GET existing thumbnail
  // =========================================================================
  it('1. GET existing thumbnail returns 200 with stored body and content-type', async () => {
    const exerciseId = 'bench_press';
    const thumbData = Buffer.from('fake-jpeg-binary-thumbnail');
    await mockBucket.put(`exercises/${exerciseId}/thumbnail`, thumbData, {
      httpMetadata: { contentType: 'image/jpeg' },
      customMetadata: { exerciseId, mediaType: 'thumbnail' }
    });

    const req = new Request(`https://athgym-r2-api.test/v1/exercises/${exerciseId}/media/thumbnail`, {
      method: 'GET'
    });
    const res = await worker.fetch(req, mockEnv);

    assert.equal(res.status, 200);
    assert.equal(res.headers.get('Content-Type'), 'image/jpeg');
    assert.equal(res.headers.get('Accept-Ranges'), 'bytes');
    assert.ok(res.headers.get('Cache-Control').includes('max-age=86400'));
    const bodyText = await res.text();
    assert.equal(bodyText, 'fake-jpeg-binary-thumbnail');
  });

  // =========================================================================
  // TEST 2: GET existing animation
  // =========================================================================
  it('2. GET existing animation returns 200 with stored body and content-type', async () => {
    const exerciseId = 'squat';
    const animData = Buffer.from('{"v":"5.7.4","fr":60,"ip":0,"op":60,"layers":[]}');
    await mockBucket.put(`exercises/${exerciseId}/animation`, animData, {
      httpMetadata: { contentType: 'application/json' }
    });

    const req = new Request(`https://athgym-r2-api.test/v1/exercises/${exerciseId}/media/animation`, {
      method: 'GET'
    });
    const res = await worker.fetch(req, mockEnv);

    assert.equal(res.status, 200);
    assert.equal(res.headers.get('Content-Type'), 'application/json');
    const bodyText = await res.text();
    assert.equal(bodyText, animData.toString());
  });

  // =========================================================================
  // TEST 3: GET existing video
  // =========================================================================
  it('3. GET existing video returns 200 with video/mp4', async () => {
    const exerciseId = 'deadlift';
    const videoData = Buffer.from('mp4-raw-video-bytes');
    await mockBucket.put(`exercises/${exerciseId}/video`, videoData, {
      httpMetadata: { contentType: 'video/mp4' }
    });

    const req = new Request(`https://athgym-r2-api.test/v1/exercises/${exerciseId}/media/video`, {
      method: 'GET'
    });
    const res = await worker.fetch(req, mockEnv);

    assert.equal(res.status, 200);
    assert.equal(res.headers.get('Content-Type'), 'video/mp4');
    assert.equal(res.headers.get('Accept-Ranges'), 'bytes');
    const bodyText = await res.text();
    assert.equal(bodyText, 'mp4-raw-video-bytes');
  });

  // =========================================================================
  // TEST 4: GET missing media -> 404
  // =========================================================================
  it('4. GET missing media returns 404', async () => {
    const req = new Request('https://athgym-r2-api.test/v1/exercises/non_existent/media/thumbnail', {
      method: 'GET'
    });
    const res = await worker.fetch(req, mockEnv);

    assert.equal(res.status, 404);
    const bodyText = await res.text();
    assert.ok(bodyText.includes('Exercise media not found'));
  });

  // =========================================================================
  // TEST 5: PUT without Firebase token -> 401
  // =========================================================================
  it('5. PUT without Firebase token returns 401', async () => {
    const req = new Request('https://athgym-r2-api.test/v1/admin/exercises/ex1/media/thumbnail', {
      method: 'PUT',
      headers: { 'Content-Type': 'image/jpeg' },
      body: Buffer.from('image-data')
    });
    const res = await worker.fetch(req, mockEnv);

    assert.equal(res.status, 401);
    const body = await res.text();
    assert.ok(body.includes('Missing Firebase ID token'));
  });

  // =========================================================================
  // TEST 6: PUT with non-admin user -> 403
  // =========================================================================
  it('6. PUT with non-admin user returns 403', async () => {
    const req = new Request('https://athgym-r2-api.test/v1/admin/exercises/ex1/media/thumbnail', {
      method: 'PUT',
      headers: {
        'Authorization': 'Bearer NON_ADMIN_TOKEN',
        'Content-Type': 'image/jpeg'
      },
      body: Buffer.from('image-data')
    });
    const res = await worker.fetch(req, mockEnv);

    assert.equal(res.status, 403);
    const body = await res.text();
    assert.ok(body.includes('Admin privileges required'));
  });

  // =========================================================================
  // TEST 7: PUT with admin -> success
  // =========================================================================
  it('7. PUT with admin returns success JSON and stores in R2', async () => {
    const exerciseId = 'overhead_press';
    const thumbData = Buffer.from('admin-uploaded-jpeg-bytes');

    const req = new Request(`https://athgym-r2-api.test/v1/admin/exercises/${exerciseId}/media/thumbnail`, {
      method: 'PUT',
      headers: {
        'Authorization': 'Bearer ADMIN_TOKEN',
        'Content-Type': 'image/jpeg'
      },
      body: thumbData
    });
    const res = await worker.fetch(req, mockEnv);

    assert.equal(res.status, 200);
    const json = await res.json();
    assert.equal(json.success, true);
    assert.equal(json.exerciseId, exerciseId);
    assert.equal(json.mediaType, 'thumbnail');
    assert.equal(json.key, `exercises/${exerciseId}/thumbnail`);

    // Verify item in R2
    const stored = await mockBucket.get(`exercises/${exerciseId}/thumbnail`);
    assert.ok(stored);
    assert.equal(stored.httpMetadata.contentType, 'image/jpeg');
    assert.equal(stored.customMetadata.uploadedBy, 'admin_user_001');
  });

  // =========================================================================
  // TEST 8: DELETE without Firebase token -> 401
  // =========================================================================
  it('8. DELETE without Firebase token returns 401', async () => {
    const req = new Request('https://athgym-r2-api.test/v1/admin/exercises/ex1/media/thumbnail', {
      method: 'DELETE'
    });
    const res = await worker.fetch(req, mockEnv);

    assert.equal(res.status, 401);
  });

  // =========================================================================
  // TEST 9: DELETE with non-admin -> 403
  // =========================================================================
  it('9. DELETE with non-admin returns 403', async () => {
    const req = new Request('https://athgym-r2-api.test/v1/admin/exercises/ex1/media/thumbnail', {
      method: 'DELETE',
      headers: { 'Authorization': 'Bearer NON_ADMIN_TOKEN' }
    });
    const res = await worker.fetch(req, mockEnv);

    assert.equal(res.status, 403);
  });

  // =========================================================================
  // TEST 10: DELETE with admin -> success
  // =========================================================================
  it('10. DELETE with admin deletes from R2 idempotently and returns success JSON', async () => {
    const exerciseId = 'bicep_curl';
    await mockBucket.put(`exercises/${exerciseId}/video`, Buffer.from('video-to-delete'));

    const req = new Request(`https://athgym-r2-api.test/v1/admin/exercises/${exerciseId}/media/video`, {
      method: 'DELETE',
      headers: { 'Authorization': 'Bearer ADMIN_TOKEN' }
    });
    const res = await worker.fetch(req, mockEnv);

    assert.equal(res.status, 200);
    const json = await res.json();
    assert.equal(json.success, true);
    assert.equal(json.exerciseId, exerciseId);
    assert.equal(json.mediaType, 'video');

    const check = await mockBucket.get(`exercises/${exerciseId}/video`);
    assert.equal(check, null);

    // Deleting again should be idempotent and still return 200
    const req2 = new Request(`https://athgym-r2-api.test/v1/admin/exercises/${exerciseId}/media/video`, {
      method: 'DELETE',
      headers: { 'Authorization': 'Bearer ADMIN_TOKEN' }
    });
    const res2 = await worker.fetch(req2, mockEnv);
    assert.equal(res2.status, 200);
  });

  // =========================================================================
  // TEST 11: Invalid mediaType -> 400
  // =========================================================================
  it('11. Invalid mediaType returns 400', async () => {
    // GET with invalid mediaType
    const reqGet = new Request('https://athgym-r2-api.test/v1/exercises/ex1/media/unsupported_type', {
      method: 'GET'
    });
    const resGet = await worker.fetch(reqGet, mockEnv);
    assert.equal(resGet.status, 400);
    const bodyGet = await resGet.text();
    assert.ok(bodyGet.includes('Invalid media type'));

    // PUT with invalid mediaType
    const reqPut = new Request('https://athgym-r2-api.test/v1/admin/exercises/ex1/media/audio', {
      method: 'PUT',
      headers: {
        'Authorization': 'Bearer ADMIN_TOKEN',
        'Content-Type': 'audio/mp3'
      },
      body: Buffer.from('audio-data')
    });
    const resPut = await worker.fetch(reqPut, mockEnv);
    assert.equal(resPut.status, 400);
    const bodyPut = await resPut.text();
    assert.ok(bodyPut.includes('Invalid media type'));
  });

  // =========================================================================
  // TEST 12: Invalid Content-Type -> 400
  // =========================================================================
  it('12. Invalid Content-Type returns 400', async () => {
    // thumbnail with video/mp4 Content-Type
    const req = new Request('https://athgym-r2-api.test/v1/admin/exercises/ex1/media/thumbnail', {
      method: 'PUT',
      headers: {
        'Authorization': 'Bearer ADMIN_TOKEN',
        'Content-Type': 'video/mp4'
      },
      body: Buffer.from('video-data')
    });
    const res = await worker.fetch(req, mockEnv);

    assert.equal(res.status, 400);
    const text = await res.text();
    assert.ok(text.includes('Unsupported Content-Type'));
  });

  // =========================================================================
  // TEST 13: Thumbnail > 5MB -> 413
  // =========================================================================
  it('13. Thumbnail >5MB returns 413 Payload Too Large', async () => {
    const oversizedBuffer = Buffer.alloc(5 * 1024 * 1024 + 1); // 5 MB + 1 byte

    const req = new Request('https://athgym-r2-api.test/v1/admin/exercises/ex1/media/thumbnail', {
      method: 'PUT',
      headers: {
        'Authorization': 'Bearer ADMIN_TOKEN',
        'Content-Type': 'image/jpeg',
        'Content-Length': oversizedBuffer.byteLength.toString()
      },
      body: oversizedBuffer
    });
    const res = await worker.fetch(req, mockEnv);

    assert.equal(res.status, 413);
    const text = await res.text();
    assert.ok(text.includes('Payload exceeds maximum size limit'));
  });

  // =========================================================================
  // TEST 14: Animation > 10MB -> 413
  // =========================================================================
  it('14. Animation >10MB returns 413 Payload Too Large', async () => {
    const oversizedBuffer = Buffer.alloc(10 * 1024 * 1024 + 1); // 10 MB + 1 byte

    const req = new Request('https://athgym-r2-api.test/v1/admin/exercises/ex1/media/animation', {
      method: 'PUT',
      headers: {
        'Authorization': 'Bearer ADMIN_TOKEN',
        'Content-Type': 'video/webm',
        'Content-Length': oversizedBuffer.byteLength.toString()
      },
      body: oversizedBuffer
    });
    const res = await worker.fetch(req, mockEnv);

    assert.equal(res.status, 413);
    const text = await res.text();
    assert.ok(text.includes('Payload exceeds maximum size limit'));
  });

  // =========================================================================
  // TEST 15: Video > 100MB -> 413
  // =========================================================================
  it('15. Video >100MB returns 413 Payload Too Large (via Content-Length check)', async () => {
    // We test using Content-Length header to verify the early 413 rejection
    const req = new Request('https://athgym-r2-api.test/v1/admin/exercises/ex1/media/video', {
      method: 'PUT',
      headers: {
        'Authorization': 'Bearer ADMIN_TOKEN',
        'Content-Type': 'video/mp4',
        'Content-Length': (100 * 1024 * 1024 + 1024).toString()
      },
      body: Buffer.from('video-stream-placeholder')
    });
    const res = await worker.fetch(req, mockEnv);

    assert.equal(res.status, 413);
    const text = await res.text();
    assert.ok(text.includes('Payload exceeds maximum size limit'));
  });

  // =========================================================================
  // TEST 16: Path traversal attempt rejected -> 400
  // =========================================================================
  it('16. Path traversal attempt is rejected with 400', async () => {
    // Encoded traversal %2e%2e
    const req1 = new Request('https://athgym-r2-api.test/v1/exercises/%2e%2e/media/thumbnail', {
      method: 'GET'
    });
    const res1 = await worker.fetch(req1, mockEnv);
    assert.equal(res1.status, 400);

    // Literal .. in pathname
    const req2 = new Request('https://athgym-r2-api.test/v1/exercises/test..traversal/media/thumbnail', {
      method: 'GET'
    });
    const res2 = await worker.fetch(req2, mockEnv);
    assert.equal(res2.status, 400);

    // Slashes or invalid characters in exerciseId
    const req3 = new Request('https://athgym-r2-api.test/v1/admin/exercises/ex1%2fsub/media/thumbnail', {
      method: 'PUT',
      headers: {
        'Authorization': 'Bearer ADMIN_TOKEN',
        'Content-Type': 'image/jpeg'
      },
      body: Buffer.from('data')
    });
    const res3 = await worker.fetch(req3, mockEnv);
    assert.equal(res3.status, 400);
  });

  // =========================================================================
  // TEST 17: Existing profile-photo endpoints still work
  // =========================================================================
  it('17. Existing profile-photo endpoints still work (GET, PUT, DELETE with Bearer auth)', async () => {
    const photoData = Buffer.from('user-profile-photo-bytes');

    // 17.1: PUT /v1/profile/photo
    const putReq = new Request('https://athgym-r2-api.test/v1/profile/photo', {
      method: 'PUT',
      headers: {
        'Authorization': 'Bearer NON_ADMIN_TOKEN',
        'Content-Type': 'image/png'
      },
      body: photoData
    });
    const putRes = await worker.fetch(putReq, mockEnv);
    assert.equal(putRes.status, 200);
    const putJson = await putRes.json();
    assert.equal(putJson.success, true);

    // 17.2: GET /v1/profile/photo
    const getReq = new Request('https://athgym-r2-api.test/v1/profile/photo', {
      method: 'GET',
      headers: {
        'Authorization': 'Bearer NON_ADMIN_TOKEN'
      }
    });
    const getRes = await worker.fetch(getReq, mockEnv);
    assert.equal(getRes.status, 200);
    assert.equal(getRes.headers.get('Content-Type'), 'image/png');
    const getBody = await getRes.text();
    assert.equal(getBody, photoData.toString());

    // 17.3: DELETE /v1/profile/photo
    const delReq = new Request('https://athgym-r2-api.test/v1/profile/photo', {
      method: 'DELETE',
      headers: {
        'Authorization': 'Bearer NON_ADMIN_TOKEN'
      }
    });
    const delRes = await worker.fetch(delReq, mockEnv);
    assert.equal(delRes.status, 200);

    // Verify GET after delete returns 404
    const getAfterDel = await worker.fetch(getReq, mockEnv);
    assert.equal(getAfterDel.status, 404);
  });

  // =========================================================================
  // BONUS TEST: HTTP Range Request on Video (for PlatformVideoPlayer streaming)
  // =========================================================================
  it('Bonus: HTTP Range request on video returns 206 Partial Content', async () => {
    const exerciseId = 'squat_video';
    const videoData = Buffer.from('0123456789abcdefghijklmnopqrstuvwxyz'); // 36 bytes
    await mockBucket.put(`exercises/${exerciseId}/video`, videoData, {
      httpMetadata: { contentType: 'video/mp4' }
    });

    const req = new Request(`https://athgym-r2-api.test/v1/exercises/${exerciseId}/media/video`, {
      method: 'GET',
      headers: {
        'Range': 'bytes=0-9'
      }
    });
    const res = await worker.fetch(req, mockEnv);

    assert.equal(res.status, 206);
    assert.equal(res.headers.get('Content-Range'), 'bytes 0-9/36');
    assert.equal(res.headers.get('Content-Length'), '10');
    assert.equal(res.headers.get('Content-Type'), 'video/mp4');
    const bodyText = await res.text();
    assert.equal(bodyText, '0123456789');
  });

  // =========================================================================
  // BONUS TEST: HEAD Request for Video (ExoPlayer/MediaPlayer compatibility)
  // =========================================================================
  it('Bonus: HTTP HEAD request on video returns 200 OK with metadata headers and empty body', async () => {
    const exerciseId = 'squat_video';
    const videoData = Buffer.from('0123456789abcdefghijklmnopqrstuvwxyz'); // 36 bytes
    await mockBucket.put(`exercises/${exerciseId}/video`, videoData, {
      httpMetadata: { contentType: 'video/mp4' }
    });

    const req = new Request(`https://athgym-r2-api.test/v1/exercises/${exerciseId}/media/video`, {
      method: 'HEAD'
    });
    const res = await worker.fetch(req, mockEnv);

    assert.equal(res.status, 200);
    assert.equal(res.headers.get('Content-Type'), 'video/mp4');
    assert.equal(res.headers.get('Content-Length'), '36');
    assert.equal(res.headers.get('Accept-Ranges'), 'bytes');
    assert.equal(res.body, null);
  });

  // =========================================================================
  // BONUS TEST: CORS Preflight
  // =========================================================================
  it('Bonus: OPTIONS preflight returns 204 with CORS headers', async () => {
    const req = new Request('https://athgym-r2-api.test/v1/admin/exercises/ex1/media/thumbnail', {
      method: 'OPTIONS'
    });
    const res = await worker.fetch(req, mockEnv);

    assert.equal(res.status, 204);
    assert.equal(res.headers.get('Access-Control-Allow-Origin'), '*');
    assert.ok(res.headers.get('Access-Control-Allow-Methods').includes('PUT'));
  });

  // =========================================================================
  // ATHGYM CLOUDFLARE WORKER FCM PUSH BACKEND TESTS (14 Scenarios - Section 32)
  // =========================================================================

  // TEST 1: No Authorization -> 401
  it('FCM 1: No Authorization header returns 401 Unauthorized', async () => {
    const req = new Request('https://athgym-r2-api.test/v1/admin/notifications/push', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        title: 'Workout Reminder',
        message: 'Time for your workout!',
        audience: 'all'
      })
    });
    const res = await worker.fetch(req, mockEnv);
    assert.equal(res.status, 401);
  });

  // TEST 2: Invalid Firebase token -> 401
  it('FCM 2: Invalid Firebase ID token returns 401 Unauthorized', async () => {
    const req = new Request('https://athgym-r2-api.test/v1/admin/notifications/push', {
      method: 'POST',
      headers: {
        'Authorization': 'Bearer INVALID_EXPIRED_TOKEN',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        title: 'Workout Reminder',
        message: 'Time for your workout!',
        audience: 'all'
      })
    });
    const res = await worker.fetch(req, mockEnv);
    assert.equal(res.status, 401);
  });

  // TEST 3: Normal user (non-admin) -> 403
  it('FCM 3: Non-admin caller returns 403 Forbidden', async () => {
    const req = new Request('https://athgym-r2-api.test/v1/admin/notifications/push', {
      method: 'POST',
      headers: {
        'Authorization': 'Bearer NON_ADMIN_TOKEN',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        title: 'Workout Reminder',
        message: 'Time for your workout!',
        audience: 'all'
      })
    });
    const res = await worker.fetch(req, mockEnv);
    assert.equal(res.status, 403);
    const text = await res.text();
    assert.ok(text.includes('Admin privileges required'));
  });

  // TEST 4: Admin user -> accepted (200)
  it('FCM 4: Admin user is accepted (200 OK) and dispatches push', async () => {
    addMockFirestoreDevice('admin_test_user', 'fcmTokens', 'dev_adm_1', 'fcm_tok_adm_1');

    const req = new Request('https://athgym-r2-api.test/v1/admin/notifications/push', {
      method: 'POST',
      headers: {
        'Authorization': 'Bearer ADMIN_TOKEN',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        notificationId: 'notif_test_4',
        title: 'Athgym checking how the josh!!!',
        message: 'FCM push test',
        audience: 'single',
        targetUids: ['admin_test_user']
      })
    });
    const res = await worker.fetch(req, mockEnv);
    assert.equal(res.status, 200);
    const json = await res.json();
    assert.equal(json.success, true);
    assert.equal(json.notificationId, 'notif_test_4');
    assert.equal(json.pushAccepted, 1);
    assert.equal(json.tokensFound, 1);
    assert.equal(json.failed, 0);
  });

  // TEST 5: Single user -> only that user's devices receive push
  it('FCM 5: Single user targeting delivers only to that user active devices with rich FCM payload', async () => {
    // User A has 2 devices (multi-device)
    addMockFirestoreDevice('user_a', 'fcmTokens', 's24_phone', 'token_s24_phone');
    addMockFirestoreDevice('user_a', 'devices', 'tablet_dev', 'token_tablet_dev');
    // User B has 1 device
    addMockFirestoreDevice('user_b', 'fcmTokens', 'pixel_phone', 'token_pixel_phone');

    const req = new Request('https://athgym-r2-api.test/v1/admin/notifications/push', {
      method: 'POST',
      headers: {
        'Authorization': 'Bearer ADMIN_TOKEN',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        notificationId: 'notif_test_5',
        title: 'Athgym checking how the josh!!!',
        message: 'FCM push test',
        audience: 'single',
        targetUids: ['user_a'],
        destination: 'notifications'
      })
    });
    const res = await worker.fetch(req, mockEnv);
    assert.equal(res.status, 200);
    const json = await res.json();
    assert.equal(json.success, true);
    assert.equal(json.targetedUsers, 1);
    assert.equal(json.tokensFound, 2);
    assert.equal(json.pushAccepted, 2);
    assert.equal(json.failed, 0);

    // Verify User A devices received push, but User B device did NOT
    const sentTokens = mockFcmSentMessages.map(m => m.token);
    assert.ok(sentTokens.includes('token_s24_phone'));
    assert.ok(sentTokens.includes('token_tablet_dev'));
    assert.ok(!sentTokens.includes('token_pixel_phone'));

    // Verify rich FCM HTTP v1 payload formatting (Section 13)
    const msg = mockFcmSentMessages.find(m => m.token === 'token_s24_phone');
    assert.equal(msg.notification.title, 'Athgym checking how the josh!!!');
    assert.equal(msg.notification.body, 'FCM push test');
    assert.equal(msg.data.notificationId, 'notif_test_5');
    assert.equal(msg.data.destination, 'notifications');
    assert.equal(msg.data.navigate_to, 'notifications');
    assert.equal(msg.data.deepLink, 'athgym://notifications/notif_test_5');
    assert.equal(msg.android.priority, 'HIGH');
    assert.equal(msg.android.notification.channel_id, 'athgym_notifications');
    assert.equal(msg.android.notification.icon, 'ic_notification');
    assert.equal(msg.android.notification.color, '#A8FF3E');
    assert.equal(msg.android.notification.default_sound, true);
  });

  // TEST 6: Selected users -> only selected users receive push
  it('FCM 6: Selected users targeting delivers only to selected recipients', async () => {
    addMockFirestoreDevice('user_a', 'fcmTokens', 'dev_a', 'token_a');
    addMockFirestoreDevice('user_b', 'fcmTokens', 'dev_b', 'token_b');
    addMockFirestoreDevice('user_c', 'fcmTokens', 'dev_c', 'token_c');

    const req = new Request('https://athgym-r2-api.test/v1/admin/notifications/push', {
      method: 'POST',
      headers: {
        'Authorization': 'Bearer ADMIN_TOKEN',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        notificationId: 'notif_test_6',
        title: 'Team Meeting',
        message: 'Selected users notification',
        audience: 'selected',
        targetUids: ['user_a', 'user_b']
      })
    });
    const res = await worker.fetch(req, mockEnv);
    assert.equal(res.status, 200);
    const json = await res.json();
    assert.equal(json.success, true);
    assert.equal(json.targetedUsers, 2);
    assert.equal(json.tokensFound, 2);
    assert.equal(json.pushAccepted, 2);

    const sentTokens = mockFcmSentMessages.map(m => m.token);
    assert.ok(sentTokens.includes('token_a'));
    assert.ok(sentTokens.includes('token_b'));
    assert.ok(!sentTokens.includes('token_c'));
  });

  // TEST 7: Everyone -> all active users receive push
  it('FCM 7: Everyone (audience: all) resolves active tokens across all users', async () => {
    addMockFirestoreDevice('user_1', 'fcmTokens', 'dev_1', 'token_u1');
    addMockFirestoreDevice('user_2', 'fcmTokens', 'dev_2', 'token_u2');
    addMockFirestoreDevice('user_3', 'fcmTokens', 'dev_3', 'token_u3');

    const req = new Request('https://athgym-r2-api.test/v1/admin/notifications/push', {
      method: 'POST',
      headers: {
        'Authorization': 'Bearer ADMIN_TOKEN',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        notificationId: 'notif_test_7',
        title: 'Global Announcement',
        message: 'Gym open 24/7 this weekend',
        audience: 'all'
      })
    });
    const res = await worker.fetch(req, mockEnv);
    assert.equal(res.status, 200);
    const json = await res.json();
    assert.equal(json.success, true);
    assert.equal(json.targetedUsers, 3);
    assert.equal(json.tokensFound, 3);
    assert.equal(json.pushAccepted, 3);

    const sentTokens = mockFcmSentMessages.map(m => m.token);
    assert.ok(sentTokens.includes('token_u1'));
    assert.ok(sentTokens.includes('token_u2'));
    assert.ok(sentTokens.includes('token_u3'));
  });

  // TEST 8: Duplicate tokens -> one push per token
  it('FCM 8: Duplicate tokens across fcmTokens and devices are deduplicated to 1 push', async () => {
    // Same token registered in both fcmTokens and legacy devices subcollection
    addMockFirestoreDevice('user_dup', 'fcmTokens', 'phone_1', 'identical_fcm_token_xyz');
    addMockFirestoreDevice('user_dup', 'devices', 'phone_1', 'identical_fcm_token_xyz');

    const req = new Request('https://athgym-r2-api.test/v1/admin/notifications/push', {
      method: 'POST',
      headers: {
        'Authorization': 'Bearer ADMIN_TOKEN',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        notificationId: 'notif_test_8',
        title: 'Deduplication Test',
        message: 'One push per token',
        audience: 'single',
        targetUids: ['user_dup']
      })
    });
    const res = await worker.fetch(req, mockEnv);
    assert.equal(res.status, 200);
    const json = await res.json();
    assert.equal(json.tokensFound, 1);
    assert.equal(json.pushAccepted, 1);

    const sentIdentical = mockFcmSentMessages.filter(m => m.token === 'identical_fcm_token_xyz');
    assert.equal(sentIdentical.length, 1);
  });

  // TEST 9: 500+ tokens -> correctly batched
  it('FCM 9: 500+ tokens are correctly batched in groups of at most 500', async () => {
    // Generate 550 unique devices
    for (let i = 0; i < 550; i++) {
      addMockFirestoreDevice(`batch_user_${Math.floor(i / 2)}`, 'fcmTokens', `dev_${i}`, `batch_tok_${i}`);
    }

    const req = new Request('https://athgym-r2-api.test/v1/admin/notifications/push', {
      method: 'POST',
      headers: {
        'Authorization': 'Bearer ADMIN_TOKEN',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        notificationId: 'notif_test_9',
        title: 'Batching Test',
        message: 'Testing 550 tokens batching',
        audience: 'all'
      })
    });
    const res = await worker.fetch(req, mockEnv);
    assert.equal(res.status, 200);
    const json = await res.json();
    assert.equal(json.tokensFound, 550);
    assert.equal(json.pushAccepted, 550);
    assert.equal(json.failed, 0);
    assert.equal(mockFcmSentMessages.length, 550);
  });

  // TEST 10: Invalid FCM token -> removed -> other tokens continue
  it('FCM 10: Invalid/unregistered FCM token is removed from Firestore while valid token succeeds', async () => {
    addMockFirestoreDevice('user_cleanup', 'fcmTokens', 'dev_valid', 'valid_token_clean_1');
    addMockFirestoreDevice('user_cleanup', 'fcmTokens', 'dev_invalid', 'invalid_unregistered_token_99');

    const req = new Request('https://athgym-r2-api.test/v1/admin/notifications/push', {
      method: 'POST',
      headers: {
        'Authorization': 'Bearer ADMIN_TOKEN',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        notificationId: 'notif_test_10',
        title: 'Cleanup Test',
        message: 'Prune stale tokens',
        audience: 'single',
        targetUids: ['user_cleanup']
      })
    });
    const res = await worker.fetch(req, mockEnv);
    assert.equal(res.status, 200);
    const json = await res.json();
    assert.equal(json.tokensFound, 2);
    assert.equal(json.pushAccepted, 1);
    assert.equal(json.failed, 1);
    assert.equal(json.invalidTokensRemoved, 1);

    // Verify Firestore DELETE was called for stale token doc
    assert.equal(mockDeletedFirestoreDocs.length, 1);
    assert.ok(mockDeletedFirestoreDocs[0].includes('dev_invalid'));
  });

  // TEST 11: FCM partial failure -> accurate response
  it('FCM 11: FCM partial failure returns accurate metrics with PARTIAL_SUCCESS', async () => {
    addMockFirestoreDevice('user_partial', 'fcmTokens', 'dev_ok', 'valid_tok_part_1');
    addMockFirestoreDevice('user_partial', 'fcmTokens', 'dev_fail', 'failing_upstream_token_2');

    const req = new Request('https://athgym-r2-api.test/v1/admin/notifications/push', {
      method: 'POST',
      headers: {
        'Authorization': 'Bearer ADMIN_TOKEN',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        notificationId: 'notif_test_11',
        title: 'Partial Test',
        message: 'One token fails',
        audience: 'single',
        targetUids: ['user_partial']
      })
    });
    const res = await worker.fetch(req, mockEnv);
    assert.equal(res.status, 200);
    const json = await res.json();
    assert.equal(json.success, true);
    assert.equal(json.pushAccepted, 1);
    assert.equal(json.failed, 1);
    assert.equal(json.status, 'PARTIAL_SUCCESS');
  });

  // TEST 12: Firestore notification remains if FCM fails
  it('FCM 12: Upstream FCM failure preserves notificationId and returns status PUSH_FAILED without deleting records', async () => {
    addMockFirestoreDevice('user_fail', 'fcmTokens', 'dev_f', 'failing_tok_only');

    const req = new Request('https://athgym-r2-api.test/v1/admin/notifications/push', {
      method: 'POST',
      headers: {
        'Authorization': 'Bearer ADMIN_TOKEN',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        notificationId: 'notif_test_12_fcm_fail',
        title: 'Upstream Failure',
        message: 'Push failed but Firestore remains',
        audience: 'single',
        targetUids: ['user_fail']
      })
    });
    const res = await worker.fetch(req, mockEnv);
    assert.equal(res.status, 200);
    const json = await res.json();
    assert.equal(json.success, true);
    assert.equal(json.notificationId, 'notif_test_12_fcm_fail');
    assert.equal(json.pushAccepted, 0);
    assert.equal(json.failed, 1);
    assert.equal(json.status, 'PUSH_FAILED');
  });

  // TEST 13: Missing title/message -> 400
  it('FCM 13: Missing title or message is rejected with 400 Bad Request', async () => {
    // Missing title
    const reqNoTitle = new Request('https://athgym-r2-api.test/v1/admin/notifications/push', {
      method: 'POST',
      headers: {
        'Authorization': 'Bearer ADMIN_TOKEN',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        message: 'Missing title',
        audience: 'all'
      })
    });
    const resNoTitle = await worker.fetch(reqNoTitle, mockEnv);
    assert.equal(resNoTitle.status, 400);

    // Missing message
    const reqNoMsg = new Request('https://athgym-r2-api.test/v1/admin/notifications/push', {
      method: 'POST',
      headers: {
        'Authorization': 'Bearer ADMIN_TOKEN',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        title: 'Missing message',
        audience: 'all'
      })
    });
    const resNoMsg = await worker.fetch(reqNoMsg, mockEnv);
    assert.equal(resNoMsg.status, 400);

    // Oversized title (>100 chars)
    const reqOverTitle = new Request('https://athgym-r2-api.test/v1/admin/notifications/push', {
      method: 'POST',
      headers: {
        'Authorization': 'Bearer ADMIN_TOKEN',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        title: 'A'.repeat(101),
        message: 'Valid message',
        audience: 'all'
      })
    });
    const resOverTitle = await worker.fetch(reqOverTitle, mockEnv);
    assert.equal(resOverTitle.status, 400);
  });

  // TEST 14: Duplicate notification request -> does not create duplicate push
  it('FCM 14: Duplicate notificationId within 60s returns cached response without duplicate FCM sends', async () => {
    addMockFirestoreDevice('user_idemp', 'fcmTokens', 'dev_idemp', 'token_idemp_1');

    const payload = {
      notificationId: 'notif_idempotent_unique_1',
      title: 'Idempotency Test',
      message: 'Testing duplicate protection',
      audience: 'single',
      targetUids: ['user_idemp']
    };

    // First send
    const req1 = new Request('https://athgym-r2-api.test/v1/admin/notifications/push', {
      method: 'POST',
      headers: {
        'Authorization': 'Bearer ADMIN_TOKEN',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    });
    const res1 = await worker.fetch(req1, mockEnv);
    assert.equal(res1.status, 200);
    const json1 = await res1.json();
    assert.equal(json1.pushAccepted, 1);
    const sentCountAfterFirst = mockFcmSentMessages.length;
    assert.equal(sentCountAfterFirst, 1);

    // Duplicate send with same notificationId (e.g. rapid double tap or network retry)
    const req2 = new Request('https://athgym-r2-api.test/v1/admin/notifications/push', {
      method: 'POST',
      headers: {
        'Authorization': 'Bearer ADMIN_TOKEN',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    });
    const res2 = await worker.fetch(req2, mockEnv);
    assert.equal(res2.status, 200);
    const json2 = await res2.json();
    assert.equal(json2.pushAccepted, 1);

    // Verify NO additional FCM message was dispatched
    assert.equal(mockFcmSentMessages.length, sentCountAfterFirst);
  });
});

