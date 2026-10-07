/**
 * FITNESS RBAC Tool — Set Admin Custom Claim
 * 
 * Usage:
 *   node scripts/set-admin-claim.js <USER_UID_OR_EMAIL> [true|false]
 * 
 * Requirements:
 *   - Place your Firebase Service Account private key file as `serviceAccountKey.json`
 *     in the project root OR in this `scripts/` folder OR set GOOGLE_APPLICATION_CREDENTIALS.
 *   - Ensure `npm install firebase-admin` has been run (e.g. in `functions/` or project root).
 * 
 * WARNING:
 *   NEVER place service account keys inside the Android or iOS client code.
 *   NEVER commit serviceAccountKey.json to Git.
 */

const fs = require('fs');
const path = require('path');

// Candidate paths for service account key
const possibleKeyPaths = [
  path.join(__dirname, '..', 'serviceAccountKey.json'),
  path.join(__dirname, 'serviceAccountKey.json'),
  path.join(__dirname, '..', 'functions', 'serviceAccountKey.json'),
  process.env.GOOGLE_APPLICATION_CREDENTIALS
].filter(Boolean);

let serviceAccountPath = null;
for (const p of possibleKeyPaths) {
  if (fs.existsSync(p)) {
    serviceAccountPath = p;
    break;
  }
}

let admin;
try {
  admin = require('firebase-admin');
} catch (e) {
  try {
    admin = require(path.join(__dirname, '..', 'functions', 'node_modules', 'firebase-admin'));
  } catch (err) {
    console.error('❌ Error: firebase-admin module not found.');
    console.error('Please run "npm install firebase-admin" in the functions directory or root.');
    process.exit(1);
  }
}

const firebaseToolsPath = path.join(
  process.env.USERPROFILE || process.env.HOME || '',
  '.config',
  'configstore',
  'firebase-tools.json'
);

if (serviceAccountPath) {
  const serviceAccount = require(path.resolve(serviceAccountPath));
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });
} else if (fs.existsSync(firebaseToolsPath)) {
  try {
    const fbTools = JSON.parse(fs.readFileSync(firebaseToolsPath, 'utf8'));
    const token = fbTools?.tokens?.access_token;
    if (!token) throw new Error('No access_token found in firebase-tools.json');
    admin.initializeApp({
      credential: {
        getAccessToken: () => Promise.resolve({
          access_token: token,
          expires_in: 3600
        })
      },
      projectId: 'fitness-5ee68'
    });
    console.log('ℹ️ Using active Firebase CLI authenticated credentials.');
  } catch (err) {
    console.error('❌ Error reading Firebase CLI credentials:', err.message);
    process.exit(1);
  }
} else {
  console.error('❌ Error: Firebase Service Account key file not found.');
  console.error('Please download a private key from Firebase Console -> Project Settings -> Service Accounts,');
  console.error('and place it as "serviceAccountKey.json" in the project root directory, or login with "firebase login".');
  process.exit(1);
}

async function main() {
  const target = process.argv[2];
  const grantArg = process.argv[3];
  const grantAdmin = grantArg === undefined || grantArg.toLowerCase() === 'true';

  if (!target) {
    console.log('Usage: node scripts/set-admin-claim.js <USER_UID_OR_EMAIL> [true|false]');
    process.exit(1);
  }

  try {
    let user;
    if (target.includes('@')) {
      user = await admin.auth().getUserByEmail(target);
    } else {
      user = await admin.auth().getUser(target);
    }

    console.log(`Found Firebase Auth User:`);
    console.log(`  UID:   ${user.uid}`);
    console.log(`  Email: ${user.email}`);
    console.log(`  Name:  ${user.displayName || '(none)'}`);
    console.log(`  Prior Custom Claims:`, user.customClaims || {});

    const newClaims = { ...(user.customClaims || {}) };
    if (grantAdmin) {
      newClaims.admin = true;
    } else {
      delete newClaims.admin;
    }

    await admin.auth().setCustomUserClaims(user.uid, newClaims);

    console.log(`\n✅ Successfully updated custom claims for UID: ${user.uid}`);
    console.log(`  New Custom Claims:`, newClaims);
    console.log(`\nNote: The user must refresh their ID token (e.g. sign out and sign in, or reload token) for claims to reflect in the client.`);
  } catch (error) {
    console.error('❌ Failed to update custom claims:', error.message);
    process.exit(1);
  }
}

main();
