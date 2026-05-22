const fs = require('fs');
const path = require('path');

module.exports = function (context) {

    console.log('Startintent patch removal started');
    const iosPath = path.join(context.opts.projectRoot, 'platforms', 'ios');

    function findSceneDelegate(dir) {

        const files = fs.readdirSync(dir);

        for (const file of files) {

            const full = path.join(dir, file);
            const stat = fs.statSync(full);

            if (stat.isDirectory()) {
                const res = findSceneDelegate(full);
                if (res) return res;
            }

            if (file === 'SceneDelegate.swift') {
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

    const START = '// STARTINTENT SCENE PATCH START';
    const END = '// STARTINTENT SCENE PATCH END';

    console.log('Removing StartIntent patch...');

    const startIndex = content.indexOf(START);
    const endIndex = content.indexOf(END);

    if (startIndex === -1 || endIndex === -1) {
        console.log('No patch found');
        return;
    }

    // 👉 Ende inkl. Marker
    const removeEnd = endIndex + END.length;

    // 👉 vorher noch Zeilen sauber entfernen (wichtig!)
    let before = content.substring(0, startIndex);
    let after = content.substring(removeEnd);

    // 👉 optional: doppelte Leerzeilen bereinigen
    content = (before + after)
        .replace(/\n\s*\n\s*\n/g, '\n\n')
        .trim();

    fs.writeFileSync(scenePath, content, 'utf8');

    console.log('StartIntent patch removed successfully');
};