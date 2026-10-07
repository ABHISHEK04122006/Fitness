import Foundation
import UIKit
import AuthenticationServices
import CryptoKit
import Shared

class SwiftAppleAuthHandler: NSObject, IosAppleAuthHandler, ASAuthorizationControllerDelegate, ASAuthorizationControllerPresentationContextProviding {

    // Strong self-retention during active authorization request to ensure delegate stays alive
    private var retainCycle: SwiftAppleAuthHandler?
    private var currentRawNonce: String?
    private var currentController: ASAuthorizationController?
    private var onSuccessCallback: ((String, String) -> Void)?
    private var onCancelCallback: (() -> Void)?
    private var onErrorCallback: ((String) -> Void)?

    func signIn(
        onSuccess: @escaping (String, String) -> Void,
        onCancel: @escaping () -> Void,
        onError: @escaping (String) -> Void
    ) {
        DispatchQueue.main.async { [weak self] in
            guard let self = self else { return }

            self.retainCycle = self
            self.onSuccessCallback = onSuccess
            self.onCancelCallback = onCancel
            self.onErrorCallback = onError

            let rawNonce = self.randomNonceString()
            self.currentRawNonce = rawNonce
            let hashedNonce = self.sha256(rawNonce)

            let appleIDProvider = ASAuthorizationAppleIDProvider()
            let request = appleIDProvider.createRequest()
            request.requestedScopes = [.fullName, .email]
            request.nonce = hashedNonce

            let authorizationController = ASAuthorizationController(authorizationRequests: [request])
            authorizationController.delegate = self
            authorizationController.presentationContextProvider = self
            self.currentController = authorizationController

            print("[DEBUG] APPLE_AUTH_REQUEST_STARTED")
            authorizationController.performRequests()
        }
    }

    // MARK: - ASAuthorizationControllerPresentationContextProviding

    func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let window = windowScene.windows.first(where: { $0.isKeyWindow }) ?? windowScene.windows.first else {
            return UIWindow()
        }
        return window
    }

    // MARK: - ASAuthorizationControllerDelegate

    func authorizationController(controller: ASAuthorizationController, didCompleteWithAuthorization authorization: ASAuthorization) {
        print("[DEBUG] APPLE_AUTHORIZATION_CALLBACK")

        guard let appleIDCredential = authorization.credential as? ASAuthorizationAppleIDCredential else {
            let err = self.onErrorCallback
            self.cleanup()
            err?("Invalid authorization credential received.")
            return
        }

        guard let nonce = self.currentRawNonce else {
            let err = self.onErrorCallback
            self.cleanup()
            err?("Missing raw nonce for Apple Sign-In.")
            return
        }

        guard let appleIDToken = appleIDCredential.identityToken,
              let idTokenString = String(data: appleIDToken, encoding: .utf8) else {
            let err = self.onErrorCallback
            self.cleanup()
            err?("Unable to parse Apple identity token.")
            return
        }

        print("[DEBUG] APPLE_IDENTITY_TOKEN_RECEIVED")
        let success = self.onSuccessCallback
        self.cleanup()
        success?(idTokenString, nonce)
    }

    func authorizationController(controller: ASAuthorizationController, didCompleteWithError error: Error) {
        let nsError = error as NSError
        if nsError.domain == ASAuthorizationError.errorDomain,
           nsError.code == ASAuthorizationError.canceled.rawValue {
            print("[DEBUG] APPLE_AUTHORIZATION_CANCELLED")
            let cancel = self.onCancelCallback
            self.cleanup()
            cancel?()
        } else {
            print("[DEBUG] APPLE_FIREBASE_AUTH_FAILED: \(error.localizedDescription)")
            let err = self.onErrorCallback
            self.cleanup()
            err?(error.localizedDescription)
        }
    }

    private func cleanup() {
        self.currentRawNonce = nil
        self.onSuccessCallback = nil
        self.onCancelCallback = nil
        self.onErrorCallback = nil
        self.currentController = nil
        self.retainCycle = nil
    }

    // MARK: - Nonce Utilities

    private func randomNonceString(length: Int = 32) -> String {
        precondition(length > 0)
        var randomBytes = [UInt8](repeating: 0, count: length)
        let errorCode = SecRandomCopyBytes(kSecRandomDefault, randomBytes.count, &randomBytes)
        if errorCode != errSecSuccess {
            fatalError("Unable to generate nonce. SecRandomCopyBytes failed with OSStatus \(errorCode)")
        }

        let charset: [Character] =
            Array("0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._")

        let nonce = randomBytes.map { byte in
            charset[Int(byte) % charset.count]
        }

        return String(nonce)
    }

    private func sha256(_ input: String) -> String {
        let inputData = Data(input.utf8)
        let hashedData = SHA256.hash(data: inputData)
        let hashString = hashedData.compactMap {
            String(format: "%02x", $0)
        }.joined()

        return hashString
    }
}
