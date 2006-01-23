/**
 * MultiDocApplication.java
 */

package org.virion.jam.framework;

import org.virion.jam.mac.Utils;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.lang.reflect.Method;

public class MultiDocApplication extends Application {
	private DocumentFrameFactory documentFrameFactory = null;

    private AbstractFrame invisibleFrame = null;
    private DocumentFrame upperDocumentFrame = null;

    private ArrayList documents = new ArrayList();

    public MultiDocApplication(String nameString, String aboutString, Icon icon) {

        super(new MultiDocMenuBarFactory(), nameString, aboutString, icon);
    }

    public MultiDocApplication(String nameString, String aboutString, Icon icon,
    							String websiteURLString, String helpURLString) {

        super(new MultiDocMenuBarFactory(), nameString, aboutString, icon, websiteURLString, helpURLString);
    }

	public final void initialize() {
		setupFramelessMenuBar();
	}

	public void setDocumentFrameFactory(DocumentFrameFactory documentFrameFactory) {
	    this.documentFrameFactory = documentFrameFactory;
	}

    protected JFrame getDefaultFrame() {
        JFrame frame = getUpperDocumentFrame();
        if (frame == null) return invisibleFrame;
        return frame;
    }

    protected DocumentFrame createDocumentFrame() {
        return documentFrameFactory.createDocumentFrame(this, getMenuBarFactory());
    }

    public void doNew() {
        addDocumentFrame(createDocumentFrame());
    }

    public void doOpenFile(File file) {
        DocumentFrame documentFrame = createDocumentFrame();
        documentFrame.openFile(file);
        addDocumentFrame(documentFrame);
    }

    public void doQuit() {

        boolean ok = true;

        Iterator iter = documents.iterator();
        while (iter.hasNext()) {
            DocumentFrame documentFrame = (DocumentFrame) iter.next();
            if (!documentFrame.requestClose()) {
                ok = false;
                break;
            } else {
                documentFrame.setVisible(false);
                documentFrame.dispose();
            }
        }

        if (ok) {
            System.exit(0);
        }
    }

    public void doPreferences() {
        //       JFrame frame = new JPEGViewerPreferences(this);
        //       frame.pack();
        //       SwingTools.centerComponent(frame, null);
        //       if (macOS) frame.setJMenuBar(createMenuBar());
        //       frame.setVisible(true);
    }

    private void documentFrameActivated(DocumentFrame documentFrame) {
        upperDocumentFrame = documentFrame;
    }

    // Close the window when the close box is clicked
    private void documentFrameClosing(DocumentFrame documentFrame) {
        if (documentFrame.requestClose()) {
            documentFrame.setVisible(false);
            documentFrame.dispose();
            documents.remove(documentFrame);
        }
    }

    private void addDocumentFrame(DocumentFrame documentFrame) {
        documentFrame.initialize();
        documentFrame.setVisible(true);

        // event handling
        documentFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent e) {
                documentFrameActivated((DocumentFrame) e.getWindow());
            }

            public void windowClosing(java.awt.event.WindowEvent e) {
                documentFrameClosing((DocumentFrame) e.getWindow());
            }
        });

        documents.add(documentFrame);
        upperDocumentFrame = documentFrame;
    }

    private DocumentFrame getUpperDocumentFrame() {
        return upperDocumentFrame;
    }

	private void setupFramelessMenuBar() {
		if (Utils.isMacOSX() &&
		    System.getProperty("apple.laf.useScreenMenuBar").equalsIgnoreCase("true")) {
			if (invisibleFrame == null) {
				// We use reflection here because the setUndecorated() method
				// only exists in Java 1.4 and up
				invisibleFrame = new AbstractFrame() {

					protected void initializeComponents() {
						getSaveAction().setEnabled(false);
						getSaveAsAction().setEnabled(false);
						if (getImportAction() != null) getImportAction().setEnabled(false);
						if (getExportAction() != null) getExportAction().setEnabled(false);
						getPrintAction().setEnabled(false);

						getCutAction().setEnabled(false);
						getCopyAction().setEnabled(false);
						getPasteAction().setEnabled(false);
						getDeleteAction().setEnabled(false);
						getSelectAllAction().setEnabled(false);
						getFindAction().setEnabled(false);

						getZoomWindowAction().setEnabled(false);
						getMinimizeWindowAction().setEnabled(false);
						getCloseWindowAction().setEnabled(false);

					}

					public boolean requestClose() {
						return false;
					}

					public JComponent getExportableComponent() {
						return null;
					}
				};
				invisibleFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				try {
					Method mthd = invisibleFrame.getClass().getMethod("setUndecorated",
						new Class[] {Boolean.TYPE});
					mthd.invoke(invisibleFrame, new Object[] {Boolean.TRUE});
				} catch (Exception ex) {
					// Shouldn't happen
				}
				invisibleFrame.setSize(0, 0);
				invisibleFrame.pack();
			}
			invisibleFrame.initialize();

			if (!invisibleFrame.isVisible())
				invisibleFrame.setVisible(true);

			invisibleFrame.pack();
		}

	}

}