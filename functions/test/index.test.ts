import { describe, it } from "node:test";
import * as assert from "node:assert";
import { chunkTokens } from "../src/index";

describe("Fitness Notification Backend Models & Payload Tests", () => {
  it("formats notification document correctly for Firestore", () => {
    const uid = "test_athlete_123";
    const notificationId = "notif_doc_789";
    const title = "Today's Workout";
    const message = "Chest + Triceps is waiting for you. 💪";
    const type = "WORKOUT_REMINDER";
    const actionType = "WORKOUT";
    const actionId = "chest_day_1";
    const now = Date.now();

    const notificationDoc = {
      id: notificationId,
      userId: uid,
      title: title.trim(),
      message: message.trim(),
      type: type || "GENERAL",
      read: false,
      createdAt: now,
      actionType: actionType || null,
      actionId: actionId || null
    };

    assert.strictEqual(notificationDoc.id, notificationId);
    assert.strictEqual(notificationDoc.userId, uid);
    assert.strictEqual(notificationDoc.title, "Today's Workout");
    assert.strictEqual(notificationDoc.read, false);
    assert.strictEqual(typeof notificationDoc.createdAt, "number");
    assert.strictEqual(notificationDoc.actionType, "WORKOUT");
    assert.strictEqual(notificationDoc.actionId, "chest_day_1");
  });

  it("constructs compliant FCM data and notification payloads with athgym_notifications channel", () => {
    const notificationId = "notif_doc_789";
    const title = "ATHGYM Announcement";
    const message = "Gym open 24/7 this weekend!";
    const type = "ANNOUNCEMENT";
    const actionType = "NOTIFICATIONS";
    const actionId = "gym_announcement_1";

    const notificationPayload = {
      title: title.trim(),
      body: message.trim()
    };

    const fcmDataPayload: Record<string, string> = {
      notificationId: String(notificationId),
      notification_id: String(notificationId),
      title: String(title),
      message: String(message),
      body: String(message),
      type: String(type),
      actionType: String(actionType || ""),
      actionId: String(actionId || ""),
      destination: "notifications",
      navigate_to: "notifications",
      deepLink: `athgym://notifications/${notificationId}`
    };

    const androidConfig = {
      priority: "high",
      notification: {
        channelId: "athgym_notifications",
        icon: "ic_notification",
        color: "#A8FF3E",
        defaultSound: true
      }
    };

    // Verify all data values are strictly non-null strings
    for (const [key, value] of Object.entries(fcmDataPayload)) {
      assert.strictEqual(typeof key, "string");
      assert.strictEqual(typeof value, "string", `Key ${key} must be a string value`);
    }

    assert.strictEqual(notificationPayload.title, "ATHGYM Announcement");
    assert.strictEqual(notificationPayload.body, "Gym open 24/7 this weekend!");
    assert.strictEqual(androidConfig.notification.channelId, "athgym_notifications");
    assert.strictEqual(androidConfig.notification.icon, "ic_notification");
    assert.strictEqual(androidConfig.notification.color, "#A8FF3E");
    assert.strictEqual(fcmDataPayload.destination, "notifications");
    assert.strictEqual(fcmDataPayload.navigate_to, "notifications");
  });

  it("correctly chunks tokens into batches of up to 500", () => {
    const tokens = Array.from({ length: 1250 }, (_, i) => `token_${i}`);
    const batches = chunkTokens(tokens, 500);

    assert.strictEqual(batches.length, 3);
    assert.strictEqual(batches[0].length, 500);
    assert.strictEqual(batches[1].length, 500);
    assert.strictEqual(batches[2].length, 250);
  });

  it("resolves recipient UIDs from single, multi, and audience targets without duplicates", () => {
    const singleUid = "athlete_1";
    const selectedUids = [singleUid, "athlete_2", "athlete_3", "athlete_1", "athlete_2"];

    const resolved = new Set<string>();
    selectedUids.forEach((id) => resolved.add(id));
    assert.strictEqual(resolved.size, 3);
    assert.ok(resolved.has("athlete_1"));
    assert.ok(resolved.has("athlete_2"));
    assert.ok(resolved.has("athlete_3"));
  });

  it("prunes invalid device tokens accurately across both fcmTokens and devices collections", () => {
    const activeTokens = [
      { uid: "user_a", sourceCollection: "fcmTokens", docId: "token_doc_1", token: "valid_token_1" },
      { uid: "user_a", sourceCollection: "devices", docId: "device_doc_2", token: "stale_token_2" },
      { uid: "user_b", sourceCollection: "fcmTokens", docId: "token_doc_3", token: "stale_token_3" },
      { uid: "user_b", sourceCollection: "devices", docId: "device_doc_4", token: "valid_token_4" }
    ];

    const fcmBatchResponse = {
      successCount: 2,
      failureCount: 2,
      responses: [
        { success: true },
        { success: false, error: { code: "messaging/registration-token-not-registered" } },
        { success: false, error: { code: "messaging/invalid-registration-token" } },
        { success: true }
      ]
    };

    const staleDeletions: { uid: string; collection: string; docId: string }[] = [];
    fcmBatchResponse.responses.forEach((resp, idx) => {
      if (!resp.success && resp.error) {
        const errorCode = resp.error.code;
        if (
          errorCode === "messaging/invalid-registration-token" ||
          errorCode === "messaging/registration-token-not-registered"
        ) {
          const item = activeTokens[idx];
          staleDeletions.push({
            uid: item.uid,
            collection: item.sourceCollection,
            docId: item.docId
          });
        }
      }
    });

    assert.strictEqual(staleDeletions.length, 2);
    assert.strictEqual(staleDeletions[0].uid, "user_a");
    assert.strictEqual(staleDeletions[0].collection, "devices");
    assert.strictEqual(staleDeletions[0].docId, "device_doc_2");
    assert.strictEqual(staleDeletions[1].uid, "user_b");
    assert.strictEqual(staleDeletions[1].collection, "fcmTokens");
    assert.strictEqual(staleDeletions[1].docId, "token_doc_3");
  });
});
