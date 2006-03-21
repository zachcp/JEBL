package jebl.gui.trees.treeviewer.painters;

import jebl.evolution.trees.RootedTree;
import jebl.gui.trees.treeviewer.TreePane;
import org.virion.jam.controlpanels.*;
import org.virion.jam.panels.OptionsPanel;
import org.virion.jam.components.RealNumberField;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
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
        this(0.0, 12);
    }

    public ScaleBarPainter(double scaleRange) {
        this(scaleRange, 12);
    }

    public ScaleBarPainter(int defaultSize) {
        this(0.0, defaultSize);
    }

    public ScaleBarPainter(double scaleRange, int defaultSize) {
        this.scaleRange = scaleRange;
        this.defaultFontSize = defaultSize;
        scaleFont = new Font("sansserif", Font.PLAIN, defaultFontSize);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        firePainterChanged();
    }

    public void calibrate(Graphics2D g2, TreePane treePane) {
        Font oldFont = g2.getFont();
        g2.setFont(scaleFont);

        FontMetrics fm = g2.getFontMetrics();
        double labelHeight = fm.getHeight();

        if (scaleRange == 0.0) {
            RootedTree tree = treePane.getTree();
            scaleRange = tree.getHeight(tree.getRootNode()) / 10.0;
        }

        preferredWidth = treePane.getTreeScale() * scaleRange;
        preferredHeight = labelHeight + 4 + scaleBarStroke.getLineWidth();

        yOffset = (float)(fm.getAscent()) + 4 + scaleBarStroke.getLineWidth();

        g2.setFont(oldFont);
    }

    public void paint(Graphics2D g2, TreePane treePane, Justification justification, Rectangle2D bounds) {
        Font oldFont = g2.getFont();
        Paint oldPaint = g2.getPaint();
        Stroke oldStroke = g2.getStroke();

        if (background != null) {
            g2.setPaint(background);
            g2.fill(bounds);
        }

        if (borderPaint != null && borderStroke != null) {
            g2.setPaint(borderPaint);
            g2.setStroke(borderStroke);
            g2.draw(bounds);
        }

        g2.setFont(scaleFont);

        String label = Double.toString(scaleRange);

        Rectangle2D rect = g2.getFontMetrics().getStringBounds(label, g2);

        double x1, x2;
        float xOffset;
        switch (justification) {
            case CENTER:
                xOffset = (float)(bounds.getX() + (bounds.getWidth() - rect.getWidth()) / 2.0);
                x1 = (bounds.getX() + (bounds.getWidth() - preferredWidth) / 2.0);
                x2 = x1 + preferredWidth;
                break;
            case FLUSH:
            case LEFT:
                xOffset = (float)bounds.getX();
                x1 = bounds.getX();
                x2 = x1 + preferredWidth;
                break;
            case RIGHT:
                xOffset = (float)(bounds.getX() + bounds.getWidth() - rect.getWidth());
                x2 = bounds.getX() + bounds.getWidth();
                x1 = x2 - preferredWidth;
                break;
            default:
                throw new IllegalArgumentException("Unrecognized alignment enum option");
        }

        g2.setPaint(foreground);
        g2.setStroke(scaleBarStroke);

        g2.draw(new Line2D.Double(x1, bounds.getY(), x2, bounds.getY()));

        g2.drawString(label, xOffset, yOffset + (float)bounds.getY());

        g2.setFont(oldFont);
        g2.setPaint(oldPaint);
        g2.setStroke(oldStroke);
    }

    public double getPreferredWidth() {
        return preferredWidth;
    }

    public double getPreferredHeight() {
        return preferredHeight;
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

    public void setLineWeight(float weight) {
        this.scaleBarStroke = new BasicStroke(weight, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
        firePainterChanged();
    }

    public void setControlPalette(ControlPalette controlPalette) {
        // nothing to do
    }

    public List<Controls> getControls() {

        List<Controls> controlsList = new ArrayList<Controls>();

        if (controls == null) {
            OptionsPanel optionsPanel = new OptionsPanel();

            final JCheckBox checkBox1 = new JCheckBox("Show Scale Bar");
            optionsPanel.addComponent(checkBox1);

            checkBox1.setSelected(isVisible());

            final RealNumberField text1 = new RealNumberField(0.0, Double.MAX_VALUE);
            text1.setValue(scaleRange);

            text1.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    Double value = text1.getValue();
                    if (value != null) {
                        setScaleRange(value.doubleValue());
                    }
                }
            });
            final JLabel label1 = optionsPanel.addComponentWithLabel("Scale Range:", text1, true);

            final JSpinner spinner1 = new JSpinner(new SpinnerNumberModel(defaultFontSize, 0.01, 48.0, 1.0));

            spinner1.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    setFontSize(((Double)spinner1.getValue()).floatValue());
                }
            });
            final JLabel label2 = optionsPanel.addComponentWithLabel("Font Size:", spinner1);

            final JSpinner spinner2 = new JSpinner(new SpinnerNumberModel(1.0, 0.01, 48.0, 1.0));

            spinner2.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    setLineWeight(((Double)spinner2.getValue()).floatValue());
                }
            });
            final JLabel label3 = optionsPanel.addComponentWithLabel("Line Weight:", spinner2);

            label1.setEnabled(checkBox1.isSelected());
            text1.setEnabled(checkBox1.isSelected());
            label2.setEnabled(checkBox1.isSelected());
            spinner1.setEnabled(checkBox1.isSelected());
            label3.setEnabled(checkBox1.isSelected());
            spinner2.setEnabled(checkBox1.isSelected());

            checkBox1.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    label1.setEnabled(checkBox1.isSelected());
                    text1.setEnabled(checkBox1.isSelected());
                    label2.setEnabled(checkBox1.isSelected());
                    spinner1.setEnabled(checkBox1.isSelected());
                    label3.setEnabled(checkBox1.isSelected());
                    spinner2.setEnabled(checkBox1.isSelected());

                    setVisible(checkBox1.isSelected());
                }
            });

	        controls = new Controls("Scale Bar", optionsPanel, false);
        }

        controlsList.add(controls);

        return controlsList;
    }

	public void setSettings(ControlsSettings settings) {
	}

	public void getSettings(ControlsSettings settings) {
	}

    private Controls controls = null;

    private boolean visible = true;

    private Paint foreground = Color.BLACK;
    private Paint background = null;
    private Paint borderPaint = null;
    private Stroke borderStroke = null;
    private BasicStroke scaleBarStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

    private Font scaleFont;
    private double preferredHeight;
    private double preferredWidth;

    private float yOffset;
}