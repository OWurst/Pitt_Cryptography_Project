const { app, session, BrowserWindow, screen } = require("electron");
const path = require("path");
const os = require("node:os");
const isDev = process.env.NODE_ENV === "dev";

if (isDev) {
    require("electron-reload")(__dirname, {
        electron: path.join(__dirname, "..", "node_modules", ".bin", "electron")
    });
}

const reduxDevToolsPath = path.join(
    os.homedir(),
    "Library/Application Support/Google/Chrome/Default/Extensions/lmhkpmbekcpmknklioeibfkpmmfibljd/3.1.3_0"
);

let mainWindow;

const createWindow = async () => {
    // Create the browser window.
    const { width, height } = screen.getPrimaryDisplay().workAreaSize;
    mainWindow = new BrowserWindow({
        width,
        height,
        webPreferences: { nodeIntegration: true, contextIsolation: false, devTools: isDev }
    });
    // and load the index.html of the app.
    if (isDev) {
        mainWindow.loadURL("http://localhost:3000");
    }
    else {
        mainWindow.loadFile(path.join(__dirname, "../build/index.html"));
    }
    session.defaultSession.clearStorageData({
        // without set origin options
        storages: ["localstorage", "caches", "indexdb"]
    }, () => {
        return;
    });
};

if (isDev) {
    app.whenReady().then(async () => {
        await session.defaultSession.loadExtension(reduxDevToolsPath);
    });
}

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.on("ready", createWindow);
