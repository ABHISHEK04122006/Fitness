# FITNESS — Admin Custom Claim Setup Guide (Spark Plan Compatible)

This guide documents the secure procedure for granting the authoritative `admin: true` custom claim to an authenticated Firebase user for local development and testing.

---

## 1. Security Architecture

- **Authoritative Source**: Firebase Authentication Custom Claims (`{ "admin": true }`).
- **Authorization Enforcement**: 
  - **Server-side**: `firestore.rules` enforces `request.auth.token.admin == true`.
  - **Client-side**: Kotlin Multiplatform `RoleResolver` reads the ID token claims to determine UI navigation and screen-level guards (`AdminDashboardScreen`).
- **No Client Elevation**: Normal mobile clients can **never** promote themselves.
- **No Hardcoded Emails**: Roles are never assigned by matching email strings (`email == "admin@..."`).
- **Spark Plan Safety**: No Cloud Functions or Blaze billing are required. Claim assignment happens from your developer environment using the Firebase Admin SDK.

---

## 2. Generating a Service Account Key (One-Time Setup)

1. Open the [Firebase Console](https://console.firebase.google.com/).
2. Select your FITNESS project.
3. Click the **Gear icon (Project Settings)** in the top-left sidebar -> **Service accounts** tab.
4. Click **Generate new private key** and confirm by clicking **Generate key**.
5. Save the downloaded JSON file as `serviceAccountKey.json` inside the root of your project directory (`c:\Users\acer\OneDrive\Desktop\Fitness\Fitness\serviceAccountKey.json`).

> [!CAUTION]
> **CRITICAL SECURITY RULES**:
> 1. **NEVER** include `serviceAccountKey.json` inside the Android (`androidApp/`) or iOS (`iosApp/`) source folders.
> 2. **NEVER** commit `serviceAccountKey.json` to Git. (It is strictly ignored in `.gitignore`).
> 3. Keep this file only on trusted development workstations.

---

## 3. Assigning the Admin Custom Claim

Run the provided Node.js script from your terminal:

```bash
# Using email:
node scripts/set-admin-claim.js your-admin-email@example.com true

# Or using Firebase Auth UID:
node scripts/set-admin-claim.js ABC123XYZ true
```

### Revoking Admin Rights:
```bash
node scripts/set-admin-claim.js your-admin-email@example.com false
```

---

## 4. How the App Reflects the Role

1. The target user signs in to FITNESS (via Email/Password, Google Sign-In, or Apple Sign-In).
2. The app invokes `UserSession.updateRoleFromUser(currentUser)`, which calls `currentUser.getIdTokenResult(forceRefresh)`.
3. `RoleResolver.resolveRoleFromClaims` detects `{ "admin": true }` in the token.
4. `UserSession.currentUserRole` is set to `UserRole.ADMIN`.
5. Post-auth routing directs the administrator straight to `Screen.AdminDashboard`.
6. If a standard user (`UserRole.USER`) attempts to navigate to `Screen.AdminDashboard`, the screen guard denies access and redirects them to `Screen.Home`.
