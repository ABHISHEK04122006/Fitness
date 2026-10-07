import Foundation
import UIKit
import GoogleSignIn
import Shared

class SwiftGoogleAuthHandler: NSObject, IosGoogleAuthHandler {
    func signIn(
        onSuccess: @escaping (String, String?) -> Void,
        onCancel: @escaping () -> Void,
        onError: @escaping (String) -> Void
    ) {
        DispatchQueue.main.async {
            guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
                  let rootViewController = windowScene.windows.first(where: { $0.isKeyWindow })?.rootViewController
                    ?? windowScene.windows.first?.rootViewController else {
                onError("Unable to find presenting UIViewController for Google Sign-In.")
                return
            }

            GIDSignIn.sharedInstance.signIn(withPresenting: rootViewController) { result, error in
                if let error = error {
                    let nsError = error as NSError
                    if nsError.code == GIDSignInError.canceled.rawValue {
                        onCancel()
                    } else {
                        onError(error.localizedDescription)
                    }
                    return
                }

                guard let user = result?.user,
                      let idToken = user.idToken?.tokenString else {
                    onError("Missing Google ID token.")
                    return
                }

                let accessToken = user.accessToken.tokenString
                onSuccess(idToken, accessToken)
            }
        }
    }
}
