const fs = require('fs');
const path = require('path');

module.exports = function (context) {

    const iosPath = path.join(context.opts.projectRoot, 'platforms', 'ios');

    function findSceneDelegate(dir) {
        const files = fs.readdirSync(dir);

        for (const file of files) {
            const full = path.join(dir, file);

            if (fs.statSync(full).isDirectory()) {
                const res = findSceneDelegate(full);
                if (res) return res;
            } else if (file === 'SceneDelegate.swift') {
                return full;
            }
        }
        return null;
    }

    const scenePath = findSceneDelegate(iosPath);
    if (!scenePath) {
        console.log('SceneDelegate not found');
        return;
    }

    let content = fs.readFileSync(scenePath, 'utf8');

    const START = "// STARTINTENT SCENE PATCH START";
    const END   = "// STARTINTENT SCENE PATCH END";

    if (content.includes(START) && content.includes(END)) {
        console.log('SceneDelegate already patched');
        return;
    }

    const injection = `
${START}
override func scene(_ scene: UIScene,
                    willConnectTo session: UISceneSession,
                    options connectionOptions: UIScene.ConnectionOptions) {

    super.scene(scene, willConnectTo: session, options: connectionOptions)

    StartIntentContext.launchOptions =
        connectionOptions.notificationResponse?.notification.request.content.userInfo
        as? [UIApplication.LaunchOptionsKey: Any]

    if let url = connectionOptions.urlContexts.first?.url {
        StartIntentContext.url = url
    }

    print("🚀 StartIntent: Scene captured launch data")
}
${END}
`;

    content = content.replace(
        /class SceneDelegate: CDVSceneDelegate\s*\{/,
        match => match + injection
    );

    fs.writeFileSync(scenePath, content);
    console.log('SceneDelegate patched successfully');
};