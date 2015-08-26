package jebl.util;

/**
 * Object representing the bounding box of a QuadTree or an item added into a QuadTree
 *
 * @author Sebastian Dunn
 *         Created on 25/08/15 11:22 AM
 */

import java.awt.geom.Rectangle2D;

/**
 * Represents an area in the cartesian plane.
 */
public class Bounds {
    public final double minX;
    public final double minY;

    public final double maxX;
    public final double maxY;

    public final double midX;
    public final double midY;

    public Bounds(double minX, double maxX, double minY, double maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;

        midX = (minX + maxX) / 2;
        midY = (minY + maxY) / 2;
    }

    public Bounds(Rectangle2D bounds) {
        this(bounds.getMinX(), bounds.getMaxX(), bounds.getMinY(), bounds.getMaxY());
    }

    public boolean contains(double x, double y) {
        return minX <= x && x <= maxX && minY <= y && y <= maxY;
    }

    public boolean intersects(double minX, double maxX, double minY, double maxY) {
        return minX < this.maxX && this.minX < maxX && minY < this.maxY && this.minY < maxY;
    }

    public boolean intersects(Bounds bounds) {
        return intersects(bounds.minX, bounds.maxX, bounds.minY, bounds.maxY);
    }

    public boolean contains(Bounds bounds) {
        return bounds.minX >= minX && bounds.maxX <= maxX && bounds.minY >= minY && bounds.maxY <= maxY;
    }
}
