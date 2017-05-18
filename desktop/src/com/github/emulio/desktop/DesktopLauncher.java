package com.github.emulio.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.github.emulio.Emulio;

public class DesktopLauncher {
	public static void main (String[] arg) {
		final LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		//detect current resolution and stick with it


		config.fullscreen = true;
		new LwjglApplication(new Emulio(), config);
	}
}
