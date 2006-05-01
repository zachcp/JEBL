package jebl.gui.trees.treeviewer_dev.painters;

import jebl.gui.trees.treeviewer_dev.TreePane;
import jebl.evolution.trees.Tree;
import org.virion.jam.controlpalettes.ControlPalette;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.prefs.Preferences;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id$
 */
public class ScaleBarPainter extends LabelPainter<TreePane> {

	private static Preferences PREFS = Preferences.userNodeForPackage(ScaleBarPainter.class);

	private static final String DEFAULT_SCALE_BAR_LINE_WIDTH_PREFS = "defaultScaleBarLineWidth";

	// The defaults if there is nothing in the preferences
	private static float DEFAULT_SCALE_BAR_LINE_WIDTH = 1.0f;


    public ScaleBarPainter() {
        this(0.0);
    }

    public ScaleBarPainter(double scaleRange) {
        this.scaleRange = scaleRange;

	    float lineWidth = PREFS.getFloat(DEFAULT_SCALE_BAR_LINE_WIDTH_PREFS, DEFAULT_SCALE_BAR_LINE_WIDTH);
	    setScaleBarStroke(new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    }

	public void setTreePane(TreePane treePane) {
		this.treePane = treePane;
	}

    public void calibrate(Graphics2D g2, TreePane treePane) {
        Font oldFont = g2.getFont();
        g2.setFont(getFont());

        FontMetrics fm = g2.getFontMetrics();
        double labelHeight = fm.getHeight();

        if( userScaleRange != 0.0 ) {
            scaleRange = userScaleRange;
        } else {
            final double treeScale = treePane.getTreeScale();
            if( treeScale == 0.0 ) {
                scaleRange = 0.0;
            } else {
                int w10 = treePane.getWidth() / 10;

                double low = w10 /treeScale;
                double b = -(Math.ceil(Math.log10(low)) - 1);
                for(int n = 0; n < 3; ++n) {
                    double factor = Math.pow(10, b);
                    double x = ((int)(low * factor) + 1)/factor;
                    if( n == 2 || x < w10 * 2 ) {
                        scaleRange = x;
                        break;
                    }
                    ++b;
                }
            }
        }

        preferredWidth = treePane.getTreeScale() * scaleRange;
        preferredHeight = labelHeight + 4 + scaleBarStroke.getLineWidth();

        yOffset = (float) (fm.getAscent()) + 4 + scaleBarStroke.getLineWidth();

        g2.setFont(oldFont);
    }

	public void paint(Graphics2D g2, TreePane treePane, Justification justification, Rectangle2D bounds) {
	    Font oldFont = g2.getFont();
	    Paint oldPaint = g2.getPaint();
	    Stroke oldStroke = g2.getStroke();

	    if (getBackground() != null) {
	        g2.setPaint(getBackground());
	        g2.fill(bounds);
	    }

	    if (getBorderPaint() != null && getBorderStroke() != null) {
	        g2.setPaint(getBorderPaint());
	        g2.setStroke(getBorderStroke());
	        g2.draw(bounds);
	    }

	    g2.setFont(getFont());

	    // we don't need accuracy but a nice short number
	    final String label = Double.toString(scaleRange);

	    Rectangle2D rect = g2.getFontMetrics().getStringBounds(label, g2);

	    double x1, x2;
	    float xOffset;
	    switch (justification) {
	        case CENTER:
	            xOffset = (float) (bounds.getX() + (bounds.getWidth() - rect.getWidth()) / 2.0);
	            x1 = (bounds.getX() + (bounds.getWidth() - preferredWidth) / 2.0);
	            x2 = x1 + preferredWidth;
	            break;
	        case FLUSH:
	        case LEFT:
	            xOffset = (float) bounds.getX();
	            x1 = bounds.getX();
	            x2 = x1 + preferredWidth;
	            break;
	        case RIGHT:
	            xOffset = (float) (bounds.getX() + bounds.getWidth() - rect.getWidth());
	            x2 = bounds.getX() + bounds.getWidth();
	            x1 = x2 - preferredWidth;
	            break;
	        default:
	            throw new IllegalArgumentException("Unrecognized alignment enum option");
	    }

	    g2.setPaint(getForeground());
	    g2.setStroke(getScaleBarStroke());

	    g2.draw(new Line2D.Double(x1, bounds.getY(), x2, bounds.getY()));

	    g2.drawString(label, xOffset, yOffset + (float) bounds.getY());

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

    public double getHeightBound() {
        return preferredHeight + yOffset;
    }

	public BasicStroke getScaleBarStroke() {
		return scaleBarStroke;
	}

	public void setScaleBarStroke(BasicStroke scaleBarStroke) {
		this.scaleBarStroke = scaleBarStroke;
		firePainterChanged();
	}

	public double getScaleRange() {
		return userScaleRange;
	}

    public void setScaleRange(double scaleRange) {
        this.userScaleRange = scaleRange;
        firePainterChanged();
    }

    public void setControlPalette(ControlPalette controlPalette) {
        // nothing to do
    }


	public String[] getAttributes() {
		return new String[0];
	}

    public void setupAttributes(Tree tree) {
        // nothing to do...
    }

    public void setDisplayAttribute(String displayAttribute) {
    }

	private BasicStroke scaleBarStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

	private double scaleRange;
	private double userScaleRange = 0.0;

    private double preferredHeight;
    private double preferredWidth;

    private float yOffset;

	protected TreePane treePane;
}