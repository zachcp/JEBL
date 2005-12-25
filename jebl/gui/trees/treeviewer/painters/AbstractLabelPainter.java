package jebl.gui.trees.treeviewer.painters;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.geom.Rectangle2D;

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

    public void calibrate(Graphics2D g2) {
        Font oldFont = g2.getFont();
        g2.setFont(taxonLabelFont);

        FontMetrics fm = g2.getFontMetrics();
        double labelHeight = fm.getHeight();

        width = getMaxLabelWidth(g2);
        height = labelHeight;

        yOffset = (float)(fm.getAscent());

        g2.setFont(oldFont);
    }

	protected abstract double getMaxLabelWidth(Graphics2D g2);

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

    public void paint(Graphics2D g2, T item, LabelAlignment labelAlignment, Insets insets) {
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
		    switch (labelAlignment) {
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

    public JPanel getControlPanel() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel1.add(new JLabel("Font Size:"));
        final JSpinner spinner1 = new JSpinner(new SpinnerNumberModel(defaultFontSize, 1, 48, 1));

        spinner1.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                setFontSize((Integer)spinner1.getValue());
            }
        });
        panel1.add(spinner1);
        panel.add(panel1);
        panel1.setAlignmentX(Component.LEFT_ALIGNMENT);

        return panel;
    }

	private Paint foreground = Color.BLACK;
    private Paint background = null;
    private Paint borderPaint = null;
    private Stroke borderStroke = null;

    private Font taxonLabelFont;
    private double width;
    private double height;
    private float yOffset;
}
