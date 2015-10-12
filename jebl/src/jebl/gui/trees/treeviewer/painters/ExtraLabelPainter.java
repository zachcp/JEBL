package jebl.gui.trees.treeviewer.painters;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * Creates a label at the position specified in the paint() method that handles mirroring.
 *
 * @author Jonas
 *         Created on 9/10/15 3:59 PM
 */

public class ExtraLabelPainter extends ExtraElementPainter {
    private Rectangle2D.Double rect;

    public ExtraLabelPainter(String label) {
        super(12);
        this.label = label;
    }

    public void paint(Graphics2D g2, Rectangle2D bounds, int posX, int posY) {
        if (!isVisible()) {
            return;
        }
        AffineTransform oldTransform = g2.getTransform();
        Font oldFont = g2.getFont();
        Paint oldPaint = g2.getPaint();
        boolean antiAliasingWasOn = g2.getRenderingHints().containsValue(RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if(paintAsMirrorImage) {
            double distanceFromRight = bounds.getWidth() - posX;
            g2.scale(-1,1);
            g2.translate(-2*bounds.getWidth()-bounds.getX()+distanceFromRight + 5,0);
        }

        if (background != null) {
            g2.setPaint(background);
            g2.fill(bounds);
        }

        if (borderPaint != null && borderStroke != null) {
            g2.setPaint(borderPaint);
            g2.setStroke(borderStroke);
            g2.draw(bounds);
        }

        g2.setFont(font);
        g2.setPaint(foreground);

        g2.drawString(label, posX, posY);
        Rectangle2D stringBounds = g2.getFontMetrics().getStringBounds(label, g2);
        rect = new Rectangle2D.Double((double) posX, (double) posY - stringBounds.getHeight(), stringBounds.getWidth(), stringBounds.getHeight());

        g2.setFont(oldFont);
        g2.setPaint(oldPaint);

        if(paintAsMirrorImage) {    // restore it again after painting the label
            g2.setTransform(oldTransform);
        }
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, (antiAliasingWasOn) ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    public boolean intersectsWith(Point point) {
        return rect != null && rect.contains(point);
    }
}
