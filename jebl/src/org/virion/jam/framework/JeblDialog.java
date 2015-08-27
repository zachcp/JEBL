package org.virion.jam.framework;

import javax.swing.*;
import java.awt.*;
import java.awt.peer.ComponentPeer;

/**
 * Same as JDialog but uses {@link MacOsJava8WindowMemoryLeakFix}
 *
 * @author Matthew Cheung
 *         Created on 27/08/15 4:59 PM
 */
public class JeblDialog extends JDialog {
    @Override
    public void dispose() {
        // Get a reference to the peer before calling super.dispose() because that will set it to null
        ComponentPeer peer = getPeer();
        super.dispose();
        MacOsJava8WindowMemoryLeakFix.applyFix(peer);
    }

    public JeblDialog() {
    }

    public JeblDialog(Frame owner) {
        super(owner);
    }

    public JeblDialog(Frame owner, boolean modal) {
        super(owner, modal);
    }

    public JeblDialog(Frame owner, String title) {
        super(owner, title);
    }

    public JeblDialog(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    public JeblDialog(Frame owner, String title, boolean modal, GraphicsConfiguration gc) {
        super(owner, title, modal, gc);
    }

    public JeblDialog(Dialog owner) {
        super(owner);
    }

    public JeblDialog(Dialog owner, boolean modal) {
        super(owner, modal);
    }

    public JeblDialog(Dialog owner, String title) {
        super(owner, title);
    }

    public JeblDialog(Dialog owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    public JeblDialog(Dialog owner, String title, boolean modal, GraphicsConfiguration gc) {
        super(owner, title, modal, gc);
    }

    public JeblDialog(Window owner) {
        super(owner);
    }

    public JeblDialog(Window owner, ModalityType modalityType) {
        super(owner, modalityType);
    }

    public JeblDialog(Window owner, String title) {
        super(owner, title);
    }

    public JeblDialog(Window owner, String title, ModalityType modalityType) {
        super(owner, title, modalityType);
    }

    public JeblDialog(Window owner, String title, ModalityType modalityType, GraphicsConfiguration gc) {
        super(owner, title, modalityType, gc);
    }
}
