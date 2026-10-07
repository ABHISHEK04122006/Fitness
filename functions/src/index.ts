import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";

// Initialize Firebase Admin SDK using application default credentials (secure environment runtime)
if (admin.apps.length === 0) {
  admin.initializeApp();
}

const db = admin.firestore();
const messaging = admin.messaging();

export interface SendFitnessNotificationPayload {
  uid?: string;
  targetUids?: string[];
  audience?: "SINGLE_USER" | "ALL_ATHLETES" | "GLOBAL_ANNOUNCEMENT" | "MULTI_USER";
  title: string;
  message: string;
  type?: string;
  actionType?: string | null;
  actionId?: string | null;
}

export interface SendFitnessNotificationResult {
  success: boolean;
  notificationId: string;
  recipientsCount: number;
  devicesFound: number;
  pushAccepted: number;
  pushFailed: number;
  messagesSent?: number; // legacy backward compatibility
  status: string;
  message?: string;
}

interface TargetTokenInfo {
  uid: string;
  sourceCollection: "fcmTokens" | "devices";
  docId: string;
  token: string;
}

/**
 * Helper function to chunk an array into sub-arrays of size at most chunkSize (max 500 for FCM multicast).
 */
export function chunkTokens<T>(items: T[], chunkSize: number = 500): T[][] {
  const chunks: T[][] = [];
  for (let i = 0; i < items.length; i += chunkSize) {
    chunks.push(items.slice(i, i + chunkSize));
  }
  return chunks;
}

/**
 * Callable Cloud Function: sendFitnessNotification
 *
 * Implements authoritative server-side push notification delivery for ATHGYM:
 * 1. Authenticates caller & verifies admin custom claim (admin: true).
 * 2. Validates notification payload (title, message, recipients/audience).
 * 3. Resolves recipient UIDs (single, selected multiple, or everyone).
 * 4. Ensures Firestore in-app notification records exist at users/{uid}/notifications/{notificationId}.
 * 5. Queries active registered device tokens from users/{uid}/fcmTokens and users/{uid}/devices.
 * 6. Dispatches FCM multicast with BOTH system notification and data navigation payloads.
 * 7. Safely prunes expired / unregistered tokens reported by FCM.
 * 8. Returns delivery metrics (pushAccepted, pushFailed, recipientsCount, devicesFound)
 * @deprecated Deprecated for production delivery. ATHGYM uses Cloudflare Worker (athgym-r2-api)
 * POST /v1/admin/notifications/push for FCM HTTP v1 delivery to avoid requiring Firebase Blaze billing plan.
 */
export const sendFitnessNotification = onCall<SendFitnessNotificationPayload, Promise<SendFitnessNotificationResult>>(
  {
    cors: true,
    maxInstances: 10
  },
  async (request) => {
    // 1. Authentication check
    if (!request.auth) {
      throw new HttpsError(
        "unauthenticated",
        "Authentication required: callers must be authenticated with Firebase to send notifications."
      );
    }

    const { uid, targetUids, audience, title, message, type, actionType, actionId } = request.data || {};

    // 2. Input validation
    if (!title || typeof title !== "string" || title.trim().length === 0) {
      throw new HttpsError(
        "invalid-argument",
        "Invalid parameter 'title': notification title must be a non-empty string."
      );
    }
    if (!message || typeof message !== "string" || message.trim().length === 0) {
      throw new HttpsError(
        "invalid-argument",
        "Invalid parameter 'message': notification message body must be a non-empty string."
      );
    }

    const callerUid = request.auth.uid;
    const isAdmin = request.auth.token?.admin === true;

    // 3. Authorization check:
    // Only users with the admin custom claim (admin: true) can trigger push notification broadcast.
    // Self-notification is permitted for test harnesses if callerUid matches single targetUid.
    const isSelfTest = uid && typeof uid === "string" && uid.trim() === callerUid;
    if (!isAdmin && !isSelfTest) {
      throw new HttpsError(
        "permission-denied",
        "Access denied. Only administrators with the 'admin' custom claim can dispatch push notifications."
      );
    }

    // 4. Resolve recipient UIDs
    const resolvedUids = new Set<string>();

    if (audience === "ALL_ATHLETES" || audience === "GLOBAL_ANNOUNCEMENT") {
      const usersSnap = await db.collection("users").get();
      usersSnap.forEach((doc) => {
        if (doc.id && doc.id.trim().length > 0) {
          resolvedUids.add(doc.id.trim());
        }
      });
    } else if (Array.isArray(targetUids) && targetUids.length > 0) {
      targetUids.forEach((id) => {
        if (typeof id === "string" && id.trim().length > 0) {
          resolvedUids.add(id.trim());
        }
      });
    } else if (uid && typeof uid === "string" && uid.trim().length > 0) {
      resolvedUids.add(uid.trim());
    }

    if (resolvedUids.size === 0) {
      throw new HttpsError(
        "invalid-argument",
        "No recipient Firebase user UIDs could be resolved for notification delivery."
      );
    }

    const recipientList = Array.from(resolvedUids);
    const now = Date.now();
    const broadcastId = `notif_fcm_${now}_${Math.floor(1000 + Math.random() * 9000)}`;
    const notificationCategory = typeof type === "string" && type.trim().length > 0 ? type.trim() : "GENERAL";
    const cleanTitle = title.trim();
    const cleanMessage = message.trim();

    // 5. Ensure Firestore notification document exists for every target user
    const firestoreWrites: Promise<any>[] = [];
    for (const targetUid of recipientList) {
      const userNotificationId = recipientList.length === 1 ? broadcastId : `${broadcastId}_${targetUid.substring(0, 6)}`;
      const docRef = db.collection("users").doc(targetUid).collection("notifications").doc(userNotificationId);
      firestoreWrites.push(
        docRef.set(
          {
            id: userNotificationId,
            userId: targetUid,
            title: cleanTitle,
            message: cleanMessage,
            type: notificationCategory,
            read: false,
            createdAt: now,
            actionType: actionType && typeof actionType === "string" ? actionType.trim() : null,
            actionId: actionId && typeof actionId === "string" ? actionId.trim() : null
          },
          { merge: true }
        )
      );
    }
    await Promise.allSettled(firestoreWrites);

    // 6. Query registered device tokens across both users/{uid}/fcmTokens and users/{uid}/devices
    const allTokensInfo: TargetTokenInfo[] = [];
    const seenTokenStrings = new Set<string>();

    for (const targetUid of recipientList) {
      // Query fcmTokens collection
      try {
        const fcmTokensSnap = await db.collection("users").doc(targetUid).collection("fcmTokens").get();
        fcmTokensSnap.forEach((doc) => {
          const data = doc.data();
          const tokenStr = typeof data?.token === "string" ? data.token.trim() : "";
          if (tokenStr.length > 0 && !seenTokenStrings.has(tokenStr)) {
            seenTokenStrings.add(tokenStr);
            allTokensInfo.push({
              uid: targetUid,
              sourceCollection: "fcmTokens",
              docId: doc.id,
              token: tokenStr
            });
          }
        });
      } catch (err: any) {
        console.warn(`[WARN] Failed to query fcmTokens for user ${targetUid}: ${err?.message}`);
      }

      // Query devices collection (legacy & multi-device compatibility)
      try {
        const devicesSnap = await db.collection("users").doc(targetUid).collection("devices").get();
        devicesSnap.forEach((doc) => {
          const data = doc.data();
          const tokenStr = typeof data?.token === "string" ? data.token.trim() : "";
          if (tokenStr.length > 0 && !seenTokenStrings.has(tokenStr)) {
            seenTokenStrings.add(tokenStr);
            allTokensInfo.push({
              uid: targetUid,
              sourceCollection: "devices",
              docId: doc.id,
              token: tokenStr
            });
          }
        });
      } catch (err: any) {
        console.warn(`[WARN] Failed to query devices for user ${targetUid}: ${err?.message}`);
      }
    }

    // If no active device tokens exist, return safely with Firestore documents preserved
    if (allTokensInfo.length === 0) {
      return {
        success: true,
        notificationId: broadcastId,
        recipientsCount: recipientList.length,
        devicesFound: 0,
        pushAccepted: 0,
        pushFailed: 0,
        messagesSent: 0,
        status: "RECORD_CREATED_NO_DEVICES",
        message: `Notification saved for ${recipientList.length} recipient(s), but no active FCM device tokens were registered.`
      };
    }

    // 7. Dispatch FCM multicast in batches of up to 500 tokens
    const tokenChunks = chunkTokens(allTokensInfo, 500);
    let totalSuccessCount = 0;
    let totalFailureCount = 0;
    const staleTokenDeletions: Promise<any>[] = [];

    console.log(`[FCM] Push send request started: recipients=${recipientList.length}, devices=${allTokensInfo.length}`);

    for (const chunk of tokenChunks) {
      const tokens = chunk.map((item) => item.token);
      const multicastMessage: admin.messaging.MulticastMessage = {
        tokens: tokens,
        notification: {
          title: cleanTitle,
          body: cleanMessage
        },
        data: {
          notificationId: String(broadcastId),
          notification_id: String(broadcastId),
          title: String(cleanTitle),
          message: String(cleanMessage),
          body: String(cleanMessage),
          type: String(notificationCategory),
          actionType: String(actionType || ""),
          actionId: String(actionId || ""),
          destination: "notifications",
          navigate_to: "notifications",
          deepLink: `athgym://notifications/${broadcastId}`
        },
        android: {
          priority: "high",
          notification: {
            channelId: "athgym_notifications",
            icon: "ic_notification",
            color: "#A8FF3E",
            defaultSound: true
          }
        }
      };

      try {
        const batchResponse = await messaging.sendEachForMulticast(multicastMessage);
        totalSuccessCount += batchResponse.successCount;
        totalFailureCount += batchResponse.failureCount;

        // Inspect errors for invalid or unregistered tokens to prune
        batchResponse.responses.forEach((resp, idx) => {
          if (!resp.success && resp.error) {
            const errorCode = resp.error.code;
            if (
              errorCode === "messaging/invalid-registration-token" ||
              errorCode === "messaging/registration-token-not-registered"
            ) {
              const staleItem = chunk[idx];
              console.log(`[FCM] Pruning stale registration token: user=${staleItem.uid}, docId=${staleItem.docId}`);
              staleTokenDeletions.push(
                db
                  .collection("users")
                  .doc(staleItem.uid)
                  .collection(staleItem.sourceCollection)
                  .doc(staleItem.docId)
                  .delete()
              );
            }
          }
        });
      } catch (fcmError: any) {
        console.error("[FCM] Batch multicast send encountered an error:", fcmError?.message);
        totalFailureCount += chunk.length;
      }
    }

    if (staleTokenDeletions.length > 0) {
      await Promise.allSettled(staleTokenDeletions);
    }

    if (totalFailureCount > 0 && totalSuccessCount > 0) {
      console.log(`[FCM] Push partial failure: accepted=${totalSuccessCount}, failed=${totalFailureCount}`);
    } else if (totalSuccessCount > 0) {
      console.log(`[FCM] Push send accepted: count=${totalSuccessCount}`);
    }

    const status =
      totalFailureCount === 0
        ? "SEND_ACCEPTED"
        : totalSuccessCount > 0
        ? "PARTIAL_SUCCESS"
        : "PUSH_FAILED";

    const summaryMessage =
      totalFailureCount === 0
        ? `Notification sent. Recipients: ${recipientList.length} user(s). Push accepted: ${totalSuccessCount}. Failed: 0.`
        : `Notification saved. Push accepted: ${totalSuccessCount}. Failed: ${totalFailureCount}.`;

    return {
      success: true,
      notificationId: broadcastId,
      recipientsCount: recipientList.length,
      devicesFound: allTokensInfo.length,
      pushAccepted: totalSuccessCount,
      pushFailed: totalFailureCount,
      messagesSent: totalSuccessCount,
      status: status,
      message: summaryMessage
    };
  }
);
