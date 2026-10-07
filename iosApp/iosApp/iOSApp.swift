import SwiftUI
import GoogleSignIn
import Shared

@main
struct iOSApp: App {
    private let googleAuthHandler = SwiftGoogleAuthHandler()
    private let appleAuthHandler = SwiftAppleAuthHandler()

    init() {
        IosGoogleAuthBridge.shared.handler = googleAuthHandler
        IosAppleAuthBridge.shared.handler = appleAuthHandler
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    GIDSignIn.sharedInstance.handle(url)
                }
        }
    }
}