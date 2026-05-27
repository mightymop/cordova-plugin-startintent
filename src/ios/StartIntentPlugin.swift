import Foundation
import Cordova

@objc(StartIntentPlugin)
class StartIntentPlugin: CDVPlugin {
  
    var onNewIntentCallbackId: String?
    
    // MARK: - App Start & Intent Handling
    
    override func pluginInitialize() {
        super.pluginInitialize()
    }

    // Fängt die URL ab, über die die App aufgerufen wurde (Erzeuger/Konsument)
    @objc override func handleOpenURL(_ notification: Notification) {
        guard let url = notification.object as? URL else { return }
        
        let urlString = url.absoluteString
        print("[Plugin] Boom! URL gefangen: \(urlString)")
        // Parameter der eingehenden Uri auflösen (wie in Spezifikation gefordert)
        let parsedData = parseUrlToIntentData(url: url)
                     
        // JavaScript Listener benachrichtigen, falls registriert
        if let callbackId = onNewIntentCallbackId {
            let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: parsedData)
      
            result.setKeepCallbackAs(true)
            self.commandDelegate.send(result, callbackId: callbackId)
        }
    }
    
    private func parseUrlToIntentData(url: URL) -> [String: Any] {
        var data: [String: Any] = [:]
        data["scheme"] = url.scheme
        
        // <Schema>://?Schlüssel1=Wert1&Schlüssel2=Wert2 in Dictionary wandeln
        if let components = URLComponents(url: url, resolvingAgainstBaseURL: false),
           let queryItems = components.queryItems {
            var extras: [String: String] = [:]
            for item in queryItems {
                if let value = item.value {
                    extras[item.name] = value
                }
            }
            data["extras"] = extras
        }
        return data
    }
    
    // MARK: - Cordova Interface Methoden (Senden / Prüfen)
    
    @objc(isPackageAvailable:)
        func isPackageAvailable(_ command: CDVInvokedUrlCommand) {
            // Android nutzt data.getJSONArray(0), wir erwarten also als erstes Argument ein String-Array
            guard let schemesToCheck = command.arguments.first as? [String] else {
                self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Invalid arguments. Expected an array of schemes."), callbackId: command.callbackId)
                return
            }
            
            var availableSchemes: [String] = []
            
            for scheme in schemesToCheck {
                if scheme.isEmpty { continue }
                
                // Entfernt ggf. "://" vom String, um ein sauberes URL Objekt zu erzeugen
                let cleanScheme = scheme.replacingOccurrences(of: "://", with: "")
                
                if let url = URL(string: "\(cleanScheme)://") {
                    // Prüft, ob die App auf dem iOS-Gerät installiert ist
                    if UIApplication.shared.canOpenURL(url) {
                        // Wenn ja, fügen wir das exakte Scheme (so wie es angefragt wurde) zum Ergebnis hinzu
                        availableSchemes.append(scheme)
                    }
                }
            }
            
            // Analog zu Android geben wir ein Array (hier als [String]) zurück
            let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: availableSchemes)
            self.commandDelegate.send(result, callbackId: command.callbackId)
        }
    
    @objc(startApplicationFromCordova:)
    func startApplicationFromCordova(_ command: CDVInvokedUrlCommand) {
        handleInterAppCall(command: command)
    }
    
    @objc(startActivityFromCordova:)
    func startActivityFromCordova(_ command: CDVInvokedUrlCommand) {
        handleInterAppCall(command: command)
    }
    
    private func handleInterAppCall(command: CDVInvokedUrlCommand) {
		guard let params = command.arguments.first as? [String: Any] else {
			self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Invalid parameters"), callbackId: command.callbackId)
			return
		}
		
		let scheme = params["package"] as? String ?? params["scheme"] as? String ?? params["action"] as? String ?? ""
		var urlString = "\(scheme)://?"
		
		var queryItems: [String] = []
		
		// Spezifikation: "Die Werte der Parameter müssen Url kodiert werden"
		var allowedChars = CharacterSet.urlQueryAllowed
		allowedChars.remove(charactersIn: "!*'();:@&=+$,/?%#[]")
		
		// Hilfsfunktion zum sauberen Hinzufügen der Parameter
		func addQueryItem(key: String, value: Any) {
			let stringValue = "\(value)"
			if let encodedValue = stringValue.addingPercentEncoding(withAllowedCharacters: allowedChars) {
				queryItems.append("\(key)=\(encodedValue)")
			}
		}
		
		// 1. "extras" Dictionary auflösen
		if let extras = params["extras"] as? [String: Any] {
			for (key, value) in extras {
				addQueryItem(key: key, value: value)
			}
		}
		
		// 2. Restliche Root-Parameter auflösen
		// Ignoriere Keys, die bereits verarbeitet wurden oder zur Steuerung dienen
		let ignoredKeys: Set<String> = ["flags", "extras", "package", "scheme", "action"]
		
		for (key, value) in params {
			if !ignoredKeys.contains(key) {
				addQueryItem(key: key, value: value)
			}
		}
		
		urlString += queryItems.joined(separator: "&") // Spezifikation: Parameter werden durch "&" getrennt
		
		openUrlString(urlString, command: command)
	}
    
    @objc(openurl:)
    func openurl(_ command: CDVInvokedUrlCommand) {
        guard let urlString = command.arguments.first as? String else {
            self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Missing URL"), callbackId: command.callbackId)
            return
        }
        openUrlString(urlString, command: command)
    }
    
    private func openUrlString(_ urlString: String, command: CDVInvokedUrlCommand) {
        guard let url = URL(string: urlString) else {
            self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Invalid URL string"), callbackId: command.callbackId)
            return
        }
        
        DispatchQueue.main.async {
            // Spezifikation: Der Aufruf beginnt mit UIApplication.shared.openURL (Hier modernisiert zu .open)
            UIApplication.shared.open(url, options: [:]) { success in
                if success {
                    self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus_OK, messageAs: true), callbackId: command.callbackId)
                } else {
                    self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Could not open App. Scheme registered in Info.plist?"), callbackId: command.callbackId)
                }
            }
        }
    }
    
    // MARK: - Cordova Interface Methoden (Empfangen)
    
    @objc(getCordovaIntent:)
    func getCordovaIntent(_ command: CDVInvokedUrlCommand) {

        let result: CDVPluginResult

        guard let url = StartIntentContext.url else {
            result = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: "{}"
            )

            self.commandDelegate.send(result, callbackId: command.callbackId)
            return
        }

        let parsedData = parseUrlToIntentData(url: url)

        result = CDVPluginResult(
            status: CDVCommandStatus_OK,
            messageAs: parsedData
        )

        self.commandDelegate.send(result, callbackId: command.callbackId)
    }
    
    @objc(setNewIntentHandler:)
    func setNewIntentHandler(_ command: CDVInvokedUrlCommand) {
        self.onNewIntentCallbackId = command.callbackId
        
        let result = CDVPluginResult(status: CDVCommandStatus_NO_RESULT)

        result.setKeepCallbackAs(true) // Hält den Callback offen für zukünftige App-Aufrufe
        
        self.commandDelegate.send(result, callbackId: command.callbackId)
    }
    
    // MARK: - App Beenden Methoden
    
    @objc(exitApp:)
    func exitApp(_ command: CDVInvokedUrlCommand) { terminateApp(command: command) }
    
    @objc(killApp:)
    func killApp(_ command: CDVInvokedUrlCommand) { terminateApp(command: command) }
    
    @objc(closeApp:)
    func closeApp(_ command: CDVInvokedUrlCommand) { terminateApp(command: command) }
    
    private func terminateApp(command: CDVInvokedUrlCommand) {
        self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus_OK), callbackId: command.callbackId)
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
            exit(0) // Harter Exit (von Apple eigentlich ungern gesehen, im Enterprise-Sektor oft genutzt)
        }
    }
    
    // MARK: - Android-spezifische Stubs (Unter iOS ohne Funktion)
    
    @objc(isActionAvailable:)
    func isActionAvailable(_ command: CDVInvokedUrlCommand) {
        isPackageAvailable(command) // Fallback auf URL Scheme Check
    }
    
    @objc(readDataFromContentUri:)
    func readDataFromContentUri(_ command: CDVInvokedUrlCommand) { sendNotSupportedError(command: command) }
    
    @objc(getRealPathFromContentUrl:)
    func getRealPathFromContentUrl(_ command: CDVInvokedUrlCommand) { sendNotSupportedError(command: command) }
    
    @objc(open:)
    func open(_ command: CDVInvokedUrlCommand) { sendNotSupportedError(command: command) }
    
    @objc(getUriForFile:)
    func getUriForFile(_ command: CDVInvokedUrlCommand) { sendNotSupportedError(command: command) }
    
    @objc(deleteUri:)
    func deleteUri(_ command: CDVInvokedUrlCommand) { sendNotSupportedError(command: command) }
    
    @objc(getIcons:)
    func getIcons(_ command: CDVInvokedUrlCommand) { sendNotSupportedError(command: command) }
    
    @objc(getAllIcons:)
    func getAllIcons(_ command: CDVInvokedUrlCommand) { sendNotSupportedError(command: command) }
    
    private func sendNotSupportedError(command: CDVInvokedUrlCommand) {
        self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Not implemented on iOS"), callbackId: command.callbackId)
    }
}
