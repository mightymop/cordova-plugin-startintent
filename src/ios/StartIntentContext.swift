import Foundation
import UIKit

@objc(StartIntentContext)
class StartIntentContext: NSObject {

    static var launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    static var url: URL?
}