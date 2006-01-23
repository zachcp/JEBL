package org.virion.jam.console;

import org.virion.jam.framework.Application;
import org.virion.jam.framework.MenuBarFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class ConsoleApplication extends Application {

	private ConsoleFrame consoleFrame = null;

    public ConsoleApplication(String nameString, String aboutString, Icon icon) throws IOException {
        this(new ConsoleMenuBarFactory(), nameString, aboutString, icon);
    }

    public ConsoleApplication(MenuBarFactory menuBarFactory, String nameString, String aboutString, Icon icon) throws IOException {

		super(menuBarFactory, nameString, aboutString, icon);

		consoleFrame = new ConsoleFrame();
		consoleFrame.initialize();
		consoleFrame.setVisible(true);

		// event handling
		consoleFrame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				thisWindowClosing(e);
			}
		});
	}

    public void initialize() {
        // nothing to do...
    }

    protected JFrame getDefaultFrame() { return consoleFrame; }

	public void doNew() {
		throw new RuntimeException("A ConsoleApplication cannot do a New command");
	}

	public void doOpenFile(File file) {
		throw new RuntimeException("A ConsoleApplication cannot do an Open command");
	}

	public void doCloseWindow() {
		doQuit();
	}

	public void doQuit() {
		if (consoleFrame.requestClose()) {

			consoleFrame.setVisible(false);
			consoleFrame.dispose();
			System.exit(0);
		}
	}

    public void doPreferences() {
    }

    public void doStop() {
		doQuit();
    }

	// Close the window when the close box is clicked
	private void thisWindowClosing(java.awt.event.WindowEvent e) {
		doQuit();
	}

}