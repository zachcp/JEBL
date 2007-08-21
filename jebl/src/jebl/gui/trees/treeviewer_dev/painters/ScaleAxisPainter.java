package jebl.gui.trees.treeviewer_dev.painters;

import jebl.evolution.trees.Tree;
import jebl.evolution.trees.RootedTree;
import jebl.gui.trees.treeviewer_dev.TreePane;
import jebl.gui.trees.treeviewer_dev.TimeScale;
import org.virion.jam.controlpalettes.ControlPalette;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: ScaleBarPainter.java,v 1.7 2006/11/21 16:10:24 rambaut Exp $
 */
public class ScaleAxisPainter extends LabelPainter<TreePane> implements ScalePainter {

    public ScaleAxisPainter() {
        this(0.0, 1.0);
    }

	public ScaleAxisPainter(double userScaleMinimum, double userScaleMaximum) {
		this.userScaleMinimum = userScaleMinimum;
		this.userScaleMaximum = userScaleMaximum;
	}

	public void setTreePane(TreePane treePane) {
        this.treePane = treePane;
    }

    public Rectangle2D calibrate(Graphics2D g2, TreePane treePane) {
        Font oldFont = g2.getFont();
        g2.setFont(getFont());

        FontMetrics fm = g2.getFontMetrics();
        double labelHeight = fm.getHeight();

	    scaleFactor = treePane.getTreeScale();
		offset = 0.0;

        preferredWidth = treePane.getTreeBounds().getWidth();
        preferredHeight = labelHeight + topMargin + bottomMargin + scaleBarStroke.getLineWidth() + majorTickSize;

        yOffset = (float) (fm.getAscent() + topMargin + bottomMargin + majorTickSize) + scaleBarStroke.getLineWidth();

        g2.setFont(oldFont);

	    TimeScale timeScale = treePane.getTimeScale();
	    RootedTree tree = treePane.getTree();

	    axis.setManualRange(timeScale.getAge(tree.getHeight(tree.getRootNode()), tree), timeScale.getAge(0.0, tree));

	    return new Rectangle2D.Double(0.0, 0.0, preferredWidth, preferredHeight);
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

        g2.setPaint(getForeground());
        g2.setStroke(getScaleBarStroke());

	    paintAxis(g2, bounds);

        g2.setFont(oldFont);
        g2.setPaint(oldPaint);
        g2.setStroke(oldStroke);
    }
    /**
    *	Get the maximum width of the labels of an axis
    */
    protected double getMaxTickLabelWidth(Graphics2D g2)
    {
        String label;
        double width;
        double maxWidth = 0;

        if (axis.getLabelFirst()) { // Draw first minor tick as a major one (with a label)
            label = axis.getFormatter().format(axis.getMinorTickValue(0, -1));
            width = g2.getFontMetrics().stringWidth(label);
            if (maxWidth < width)
                maxWidth = width;
        }
        int n = axis.getMajorTickCount();
        for (int i = 0; i < n; i++) {
            label = axis.getFormatter().format(axis.getMajorTickValue(i));
            width = g2.getFontMetrics().stringWidth(label);
            if (maxWidth < width)
                maxWidth = width;
        }
        if (axis.getLabelLast()) { // Draw first minor tick as a major one (with a label)
            label = axis.getFormatter().format(axis.getMinorTickValue(0, n - 1));
            width = g2.getFontMetrics().stringWidth(label);
            if (maxWidth < width)
                maxWidth = width;
        }

        return maxWidth;
    }

    protected void paintAxis(Graphics2D g2, Rectangle2D axisBounds)
    {
        int n1 = axis.getMajorTickCount();
        int n2, i, j;

        n2 = axis.getMinorTickCount(-1);
        if (axis.getLabelFirst()) { // Draw first minor tick as a major one (with a label)

            paintMajorTick(g2, axisBounds, axis.getMinorTickValue(0, -1));

            for (j = 1; j < n2; j++) {
                paintMinorTick(g2, axisBounds, axis.getMinorTickValue(j, -1));
            }
        } else {

            for (j = 0; j < n2; j++) {
                paintMinorTick(g2, axisBounds, axis.getMinorTickValue(j, -1));
            }
        }

        for (i = 0; i < n1; i++) {

            paintMajorTick(g2, axisBounds, axis.getMajorTickValue(i));
            n2 = axis.getMinorTickCount(i);

            if (i == (n1-1) && axis.getLabelLast()) { // Draw last minor tick as a major one

                paintMajorTick(g2, axisBounds, axis.getMinorTickValue(0, i));

                for (j = 1; j < n2; j++) {
                    paintMinorTick(g2, axisBounds, axis.getMinorTickValue(j, i));
                }
            } else {

                for (j = 0; j <  n2; j++) {
                    paintMinorTick(g2, axisBounds, axis.getMinorTickValue(j, i));
                }
            }
        }
    }

    protected void paintMajorTick(Graphics2D g2, Rectangle2D axisBounds, double value)
    {
        g2.setPaint(getForeground());
        g2.setStroke(getScaleBarStroke());

            String label = axis.getFormatter().format(value);
            double pos = transformX(value);

            Line2D line = new Line2D.Double(pos, axisBounds.getMaxY(), pos, axisBounds.getMaxY() + majorTickSize);
            g2.draw(line);

            g2.setPaint(getForeground());
            double width = g2.getFontMetrics().stringWidth(label);
            g2.drawString(label, (float)(pos - (width / 2)), (float)(axisBounds.getMaxY() + (majorTickSize * 1.25) + tickLabelOffset));
    }

    protected void paintMinorTick(Graphics2D g2, Rectangle2D axisBounds, double value)
    {

        g2.setPaint(getForeground());
        g2.setStroke(getScaleBarStroke());

            double pos = transformX(value);

            Line2D line = new Line2D.Double(pos, axisBounds.getMaxY(), pos, axisBounds.getMaxY() + minorTickSize);
            g2.draw(line);
    }

    /**
    *	Transform a chart co-ordinates into a drawing co-ordinates
    */
    protected double transformX(double value) {
        return ((axis.transform(value) - axis.transform(axis.getMinAxis())) * scaleFactor) + offset;
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

	public void setScaleMinimum(double scaleMinimum) {
	    this.userScaleMinimum = scaleMinimum;
	    axis.setRange(userScaleMinimum, userScaleMaximum);
	    firePainterChanged();
	}

    public void setScaleMaximum(double scaleMaximum) {
        this.userScaleMaximum = scaleMaximum;
	    axis.setRange(userScaleMinimum, userScaleMaximum);
        firePainterChanged();
    }

	public double getScaleMinimum() {
		return axis.getMinAxis();
	}

	public double getScaleMaximum() {
		return axis.getMaxAxis();
	}

	public void setAutomaticScale(boolean automaticScale) {
        this.automaticScale = automaticScale;
	    axis.setAutomatic();
        firePainterChanged();
    }

    public void setControlPalette(ControlPalette controlPalette) {
        // nothing to do
    }

    public String[] getAttributes() {
        return new String[0];
    }

    public void setupAttributes(Collection<? extends Tree> trees) {
        // nothing to do...
    }

    public void setDisplayAttribute(String displayAttribute) {
    }

    private BasicStroke scaleBarStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

    private ScaleBarAxis axis = new ScaleBarAxis(ScaleBarAxis.AT_DATA, ScaleBarAxis.AT_DATA);

    private double scaleFactor;
	private double offset;
    private double topMargin = 4.0;
    private double bottomMargin = 4.0;
	private double userScaleMinimum = 0.0;
    private double userScaleMaximum = 1.0;
    private boolean automaticScale = true;

	private double majorTickSize = 5.0;
	private double minorTickSize = 2.0;
	private double tickLabelOffset = 4.0;

    private double preferredHeight;
    private double preferredWidth;

    private float yOffset;

    protected TreePane treePane;
}
