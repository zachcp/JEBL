package jebl.gui.trees.treeviewer.painters;

import jebl.gui.trees.treeviewer.TreePane;
import jebl.gui.trees.treeviewer.TreeViewerUtilities;
import org.virion.jam.controlpanels.ControlPalette;
import org.virion.jam.controlpanels.Controls;
import org.virion.jam.controlpanels.ControlsSettings;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Collections;

/**
 * Base class for any extra elements that need to be painted, such as scale bar or "Too many labels" label
 *
 * @author Jonas
 *         Created on 9/10/15 3:46 PM
 */
public class ExtraElementPainter extends AbstractPainter<TreePane> {
    private boolean visible = true;
    Font font;
    double preferredHeight;
    private float yOffset;
    public String label;

    public ExtraElementPainter() {
        this(12);
    }

    public ExtraElementPainter(int defaultSize) {
        font = new Font("sansserif", Font.PLAIN, defaultSize);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        firePainterChanged();
    }

    public void calibrate(Graphics2D g2) {
        Font oldFont = g2.getFont();
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        preferredHeight = fm.getHeight() + 4;
        yOffset =  (float) (fm.getAscent()) + 4;
        g2.setFont(oldFont);
    }

    public void paint(Graphics2D g2, TreePane treePane, Justification justification, Rectangle2D bounds) {
    }

    public double getWidth(Graphics2D g2) {
        Font oldFont = g2.getFont();
        g2.setFont(font);
        double textWidth = TreeViewerUtilities.getTextWidth(label, font, g2);
        g2.setFont(oldFont);
        return textWidth;
    }

    public double getWidth(Graphics2D g2, TreePane treePane) {
        Font oldFont = g2.getFont();
        g2.setFont(font);
        double textWidth = TreeViewerUtilities.getTextWidth(label, font, g2);
        g2.setFont(oldFont);
        return textWidth;
    }

    public double getPreferredHeight(Graphics2D g, TreePane item) {
        return preferredHeight;
    }

    public double getHeightBound(Graphics2D g, TreePane item) {
        return preferredHeight + yOffset;
    }

    public void setFontSize(float size) {
        font = font.deriveFont(size);
        firePainterChanged();
    }

    public void setControlPalette(ControlPalette controlPalette) {
        // nothing to do
    }

    public java.util.List<Controls> getControls(boolean detachPrimaryCheckbox) {
        return Collections.emptyList();
    }

    public void setSettings(ControlsSettings settings) {
    }

    public void getSettings(ControlsSettings settings) {
    }
}