package org.virion.jam.framework;

import javax.swing.*;
import java.awt.*;
import java.awt.peer.ComponentPeer;
import java.lang.reflect.Field;

/**
 * Same as JFrame but uses {@link MacOsJava8WindowMemoryLeakFix}
 *
 * @author Matthew Cheung
 *         Created on 27/08/15 2:08 PM
 */
public class JeblFrame extends JFrame {

    @Override
    public void dispose() {
        // Get a reference to the peer before calling super.dispose() because that will set it to null
        ComponentPeer peer = getPeer();
        super.dispose();
        MacOsJava8WindowMemoryLeakFix.applyFix(peer);
    }

    public JeblFrame() throws HeadlessException {
    }

    public JeblFrame(GraphicsConfiguration gc) {
        super(gc);
    }

    public JeblFrame(String title) throws HeadlessException {
        super(title);
    }

    public JeblFrame(String title, GraphicsConfiguration gc) {
        super(title, gc);
    }
}
