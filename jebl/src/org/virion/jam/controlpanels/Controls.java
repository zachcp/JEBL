package org.virion.jam.controlpanels;

import javax.swing.*;
import java.awt.*;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class Controls {


    public Controls(String title, JPanel panel, boolean isVisible) {
        this(title, panel, isVisible, false, null);
    }

    /**
     * @param title
     * @param panel
     * @param isVisible
     * @param isPinned
     * @param primaryComponent A JCheckBox or a JComponent encapsulating a checkbox that serves as the "main" on/off toggle, or null.
     */
    public Controls(String title, JPanel panel, boolean isVisible, boolean isPinned, JComponent primaryComponent) {
        this.title = title;
        this.panel = panel;
        this.isVisible = isVisible;
        this.isPinned = isPinned;
        this.primaryCheckbox = getCheckBoxFromComponent(primaryComponent);
        this.primaryComponent = primaryComponent;
    }

    private JCheckBox getCheckBoxFromComponent(JComponent primaryComponent) {
        if (primaryComponent == null) {
            return null;
        }
        if (primaryComponent instanceof JCheckBox) {
            return (JCheckBox) primaryComponent;
        } else {
            for (Component component : primaryComponent.getComponents()) {
                if (component instanceof JCheckBox) {
                    return (JCheckBox) component;   // we shouldn't have more than one here!
                }
            }
        }
        return null;
    }

    public String getTitle() {
        return title;
    }

    public JPanel getPanel() {
        return panel;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

    public JCheckBox getPrimaryCheckbox() {
        return primaryCheckbox;
    }

    public JComponent getPrimaryComponent() {
        return primaryComponent;
    }

    private String title;
    private JPanel panel;
    private JCheckBox primaryCheckbox;
    private JComponent primaryComponent;
    private boolean isVisible;

    private boolean isPinned;
}
