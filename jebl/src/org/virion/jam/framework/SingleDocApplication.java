/**
 * SingleDocApplication.java
 */

package org.virion.jam.framework;

import javax.swing.*;
import java.io.File;

public class SingleDocApplication extends Application {

    private DocumentFrame documentFrame = null;

    public SingleDocApplication(String nameString, String aboutString, Icon icon) {

        super(new SingleDocMenuBarFactory(), nameString, aboutString, icon);
    }

    public SingleDocApplication(String nameString, String aboutString, Icon icon,
    							String websiteURLString, String helpURLString) {

        super(new SingleDocMenuBarFactory(), nameString, aboutString, icon, websiteURLString, helpURLString);
    }

    public SingleDocApplication(MenuBarFactory menuBarFactory, String nameString, String aboutString, Icon icon) {

        super(menuBarFactory, nameString, aboutString, icon);
    }

    public SingleDocApplication(MenuBarFactory menuBarFactory, String nameString, String aboutString, Icon icon,
    							String websiteURLString, String helpURLString) {

        super(menuBarFactory, nameString, aboutString, icon, websiteURLString, helpURLString);
    }

	public final void initialize() {
		// nothing to do...
	}

    public void setDocumentFrame(DocumentFrame documentFrame) {

        this.documentFrame = documentFrame;

        documentFrame.initialize();
        documentFrame.setVisible(true);

        // event handling
        documentFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                thisWindowClosing(e);
            }
        });
    }

	protected JFrame getDefaultFrame() { return documentFrame; }

	protected String getDocumentExtension() { return ""; }

    public void doNew() {
        throw new RuntimeException("A SingleDocApplication cannot do a New command");
    }

    public void doOpenFile(File file) {
        documentFrame.openFile(file);
    }

    public void doCloseWindow() {
        doQuit();
    }

    public void doQuit() {
        if (documentFrame.requestClose()) {

            documentFrame.setVisible(false);
            documentFrame.dispose();
            System.exit(0);
        }
    }

    public void doPreferences() {
    }

    // Close the window when the close box is clicked
    private void thisWindowClosing(java.awt.event.WindowEvent e) {
        doQuit();
    }
}