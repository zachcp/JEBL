package jebl.gui.trees.treeviewer.painters;

import jebl.gui.trees.treeviewer.controlpanels.Controls;
import jebl.gui.trees.treeviewer.controlpanels.OptionsPanel;
import jebl.gui.trees.treeviewer.controlpanels.ControlPanel;
import jebl.evolution.graphs.Node;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.geom.Rectangle2D;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public abstract class AbstractLabelPainter<T> extends AbstractPainter<T> {
    private int defaultFontSize;

    public AbstractLabelPainter(int defaultSize) {
        this.defaultFontSize = defaultSize;
        taxonLabelFont = new Font("sansserif", Font.PLAIN, defaultFontSize);
    }

    public AbstractLabelPainter() {
        this(6);
    }

    public void calibrate(Graphics2D g2, Collection<T> items) {
        Font oldFont = g2.getFont();
        g2.setFont(taxonLabelFont);

        FontMetrics fm = g2.getFontMetrics();
        double labelHeight = fm.getHeight();

        width = getMaxLabelWidth(g2, items);
        height = labelHeight;

        yOffset = (float)(fm.getAscent());

        g2.setFont(oldFont);
    }

    private double getMaxLabelWidth(Graphics2D g2, Collection<T> items) {
        double maxLabelWidth = 0.0;

        FontMetrics fm = g2.getFontMetrics();
        for (T item : items) {
            String label = getLabel(item);
            if (label != null) {
                Rectangle2D rect = fm.getStringBounds(label, g2);
                if (rect.getWidth() > maxLabelWidth) {
                    maxLabelWidth = rect.getWidth();
                }
            }
        }

        return maxLabelWidth;
    }


    public double getPreferredWidth() {
        return width;
    }

    public double getPreferredHeight() {
        return height;
    }

    public void setFontSize(float size) {
        taxonLabelFont = taxonLabelFont.deriveFont(size);
        firePainterChanged();
    }

    public void setForeground(Paint foreground) {
        this.foreground = foreground;
        firePainterChanged();
    }

    public void setBackground(Paint background) {
        this.background = background;
        firePainterChanged();
    }

    public void setBorder(Paint borderPaint, Stroke borderStroke) {
        this.borderPaint = borderPaint;
        this.borderStroke = borderStroke;
        firePainterChanged();
    }

    public void paint(Graphics2D g2, T item, Justification justification, Insets insets) {
        Font oldFont = g2.getFont();

        if (background != null) {
            g2.setPaint(background);
            g2.fill(new Rectangle2D.Double(0.0, 0.0, width, height));
        }

        if (borderPaint != null && borderStroke != null) {
            g2.setPaint(borderPaint);
            g2.setStroke(borderStroke);
            g2.draw(new Rectangle2D.Double(0.0, 0.0, width, height));
        }

        g2.setPaint(foreground);
        g2.setFont(taxonLabelFont);

        String label = getLabel(item);
        if (label != null) {

            Rectangle2D rect = g2.getFontMetrics().getStringBounds(label, g2);

            float xOffset;
            switch (justification) {
                case CENTER:
                    xOffset = (float)(insets.left + (width - rect.getWidth()) / 2.0);
                    break;
                case FLUSH:
                case LEFT:
                    xOffset = (float)insets.left;
                    break;
                case RIGHT:
                    xOffset = (float)(insets.left + width - rect.getWidth());
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized alignment enum option");
            }

            g2.drawString(label, xOffset, yOffset + insets.top);
        }

        g2.setFont(oldFont);
    }

    protected abstract String getLabel(T item);

    public void setControlPanel(ControlPanel controlPanel) {
        // nothing to do
    }

    public List<Controls> getControls() {

        List<Controls> controls = new ArrayList<Controls>();

        if (optionsPanel == null) {
            optionsPanel = new OptionsPanel();

            final JSpinner spinner1 = new JSpinner(new SpinnerNumberModel(defaultFontSize, 1, 48, 1));

            spinner1.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    setFontSize((Integer)spinner1.getValue());
                }
            });
            optionsPanel.addComponentWithLabel("Font Size:", spinner1);
        }

        controls.add(new Controls(getTitle(), optionsPanel));

        return controls;
    }
    private OptionsPanel optionsPanel = null;

    public abstract String getTitle();

    private Paint foreground = Color.BLACK;
    private Paint background = null;
    private Paint borderPaint = null;
    private Stroke borderStroke = null;

    private Font taxonLabelFont;
    private double width;
    private double height;
    private float yOffset;
}
