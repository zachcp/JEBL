package jebl.gui.trees.treeviewer.painters;

import jebl.gui.trees.treeviewer.controlpanels.Controls;
import jebl.gui.trees.treeviewer.controlpanels.OptionsPanel;
import jebl.gui.trees.treeviewer.controlpanels.ControlPanel;
import jebl.gui.trees.treeviewer.TreePane;
import jebl.gui.utils.RealNumberField;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;
import java.util.*;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id$
 */
public class ScaleBarPainter extends AbstractPainter<TreePane> {
    private int defaultFontSize;
    private double scaleRange;

    public ScaleBarPainter() {
        this(0.1, 12);
    }

    public ScaleBarPainter(double scaleRange) {
        this(scaleRange, 12);
    }

    public ScaleBarPainter(int defaultSize) {
        this(0.1, defaultSize);
    }

    public ScaleBarPainter(double scaleRange, int defaultSize) {
        this.scaleRange = scaleRange;
        this.defaultFontSize = defaultSize;
        scaleFont = new Font("sansserif", Font.PLAIN, defaultFontSize);
    }

    public void calibrate(Graphics2D g2, Collection<TreePane> treePanes) {
        TreePane treePane = treePanes.toArray(new TreePane[1])[0];

        Font oldFont = g2.getFont();
        g2.setFont(scaleFont);

        FontMetrics fm = g2.getFontMetrics();
        double labelHeight = fm.getHeight();

        width = treePane.getTreeBounds().getWidth();
        height = labelHeight + 10;

        yOffset = (float)(fm.getAscent()) + 10;

        g2.setFont(oldFont);
    }

    public void paint(Graphics2D g2, TreePane treePane, Justification justification, Insets insets) {
        Font oldFont = g2.getFont();
        Paint oldPaint = g2.getPaint();
        Stroke oldStroke = g2.getStroke();

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
        g2.setStroke(scaleBarStroke);

        g2.draw(new Line2D.Double(0.0, 0.0, width, 0.0));

        g2.setFont(scaleFont);

        String label = Double.toString(scaleRange);
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
        g2.setPaint(oldPaint);
        g2.setStroke(oldStroke);
    }

    public double getPreferredWidth() {
        return width;
    }

    public double getPreferredHeight() {
        return height;
    }

    public void setScaleRange(double scaleRange) {
        this.scaleRange = scaleRange;
        firePainterChanged();
    }

    public void setFontSize(float size) {
        scaleFont = scaleFont.deriveFont(size);
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

    public void setScaleBarStroke(Stroke scaleBarStroke) {
        this.scaleBarStroke = scaleBarStroke;
        firePainterChanged();
    }

    public void setLineWeight(float weight) {
        this.scaleBarStroke = new BasicStroke(weight);
        firePainterChanged();
    }

    public void setControlPanel(ControlPanel controlPanel) {
        // nothing to do
    }

    public List<Controls> getControls() {

        List<Controls> controls = new ArrayList<Controls>();

        if (optionsPanel == null) {
            optionsPanel = new OptionsPanel();

            final RealNumberField text1 = new RealNumberField(0.0, Double.MAX_VALUE);

            text1.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    setScaleRange(text1.getValue().doubleValue());
                }
            });
            optionsPanel.addComponentWithLabel("Scale Range:", text1, true);

            final JSpinner spinner2 = new JSpinner(new SpinnerNumberModel(defaultFontSize, 1, 48, 1));

            spinner2.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    setFontSize((Integer)spinner2.getValue());
                }
            });
            optionsPanel.addComponentWithLabel("Font Size:", spinner2);

            final JSpinner spinner3 = new JSpinner(new SpinnerNumberModel(1.0, 0.01, 48.0, 1.0));

            spinner3.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    setLineWeight(((Double)spinner3.getValue()).floatValue());
                }
            });
            optionsPanel.addComponentWithLabel("Line Weight:", spinner3);
        }

        controls.add(new Controls("Scale Bar", optionsPanel));

        return controls;
    }
    private OptionsPanel optionsPanel = null;

    private Paint foreground = Color.BLACK;
    private Paint background = null;
    private Paint borderPaint = null;
    private Stroke borderStroke = null;
    private Stroke scaleBarStroke = new BasicStroke(1.0f);

    private Font scaleFont;
    private double width;
    private double height;
    private float yOffset;
}
