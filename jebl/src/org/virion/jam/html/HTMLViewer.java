package org.virion.jam.html;

import org.virion.jam.framework.JeblFrame;

import javax.swing.*;
import java.awt.*;

/**
 * General-purpose class to display HTML in a standalone frame.
 */
public class HTMLViewer extends JeblFrame {

    private JEditorPane editorPane;

    public HTMLViewer(String title, String html) {
        super(title);
        getContentPane().setLayout(new BorderLayout());
        editorPane = new JEditorPane("text/html", html);
        editorPane.setEditable(false);
        getContentPane().add(new JScrollPane(editorPane), BorderLayout.CENTER);
    }
}





