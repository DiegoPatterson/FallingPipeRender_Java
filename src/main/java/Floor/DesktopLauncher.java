
//In order to run, use this: Set-Location "c:\Users\griss\OneDrive\Desktop\PipeFalling-Graphics\FallingPipeRender_Java"; .\gradlew.bat run --no-daemon

package com.pipefalling;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public final class DesktopLauncher {
    private DesktopLauncher() {
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Falling Pipe Render - Floor Preview");
        config.setWindowedMode(1280, 720);
        config.useVsync(true);
        config.setForegroundFPS(60);

        new Lwjgl3Application(new FloorRendererApp(), config);
    }
}
