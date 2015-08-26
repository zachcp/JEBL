package jebl.util;

/**
 * Data structure used to sort objects on a 2D plane and find colliding objects in expected nlogn time
 *
 * @author Sebastian Dunn
 *         Created on 25/08/15 11:20 AM
 */

import java.util.*;

public class QuadTree<T extends QuadTree.Item> {
    public interface Item {
        Bounds getBoundsForQuadTree();
    }
    /**
     * The bounds of this quad.
     */
    private Bounds mBounds;

    /**
     * The depth of this quad in the tree.
     */
    private final int mDepth;

    /**
     * Maximum number of elements to store in a quad before splitting.
     */
    private final static int MAX_ELEMENTS = 50;

    /**
     * The elements inside this quad, if any.
     */
    private List<T> mItems;

    /**
     * Maximum depth.
     */
    private final static int MAX_DEPTH = 40;

    /**
     * Child quads.
     */
    private List<QuadTree<T>> mChildren = null;

    /**
     * Creates a new quad tree with specified bounds.
     *
     * @param minX
     * @param maxX
     * @param minY
     * @param maxY
     */
    public QuadTree(double minX, double maxX, double minY, double maxY) {
        this(new Bounds(minX, maxX, minY, maxY));
    }

    public QuadTree(Collection<T> elements) {
        this(getTotalBounds(elements));
        this.addAll(elements);
    }


    public QuadTree(Bounds bounds) {
        this(bounds, 0);
    }

    private QuadTree(double minX, double maxX, double minY, double maxY, int depth) {
        this(new Bounds(minX, maxX, minY, maxY), depth);
    }

    private QuadTree(Bounds bounds, int depth) {
        mBounds = bounds;
        mDepth = depth;
    }

    /**
     * Insert an item.
     */
    public void add(T item) {
        if (this.mBounds.intersects(item.getBoundsForQuadTree())) {
            if (this.mChildren != null) {
                for (QuadTree<T> mChild : mChildren) {
                    mChild.add(item);
                }
            } else {
                if (mItems == null) {
                    mItems = new ArrayList<T>();
                }
                mItems.add(item);
                if (mItems.size() > MAX_ELEMENTS && mDepth < MAX_DEPTH) {
                    split();
                }
            }
        }
    }

    private void addAll(Collection<T> items) {
        List<T> intersectingItems = new ArrayList<T>();
        for (T item : items) {
            if (this.mBounds.intersects(item.getBoundsForQuadTree())) {
                intersectingItems.add(item);
            }
        }
        if (intersectingItems.size() == 0) return;
        int numberItems = intersectingItems.size();
        if (mItems != null) {
            numberItems += mItems.size();
        }
        this.mBounds = getTotalBounds(intersectingItems);
        if (numberItems > MAX_ELEMENTS && mDepth < MAX_DEPTH) {
            split();
        }
        if (this.mChildren != null) {
            for (QuadTree<T> mChild : mChildren) {
                mChild.addAll(intersectingItems);
            }
        } else {
            if (mItems == null) {
                mItems = new ArrayList<T>();
            }
            mItems.addAll(intersectingItems);
        }

    }

    /**
     * Split this quad.
     */
    private void split() {
        mChildren = new ArrayList<QuadTree<T>>(4);
        mChildren.add(new QuadTree<T>(mBounds.minX, mBounds.midX, mBounds.minY, mBounds.midY, mDepth + 1));
        mChildren.add(new QuadTree<T>(mBounds.midX, mBounds.maxX, mBounds.minY, mBounds.midY, mDepth + 1));
        mChildren.add(new QuadTree<T>(mBounds.minX, mBounds.midX, mBounds.midY, mBounds.maxY, mDepth + 1));
        mChildren.add(new QuadTree<T>(mBounds.midX, mBounds.maxX, mBounds.midY, mBounds.maxY, mDepth + 1));

        List<T> items = mItems;
        mItems = null;

        if (items != null) {
            for (T item : items) {
                // re-insert items into child quads.
                add(item);
            }
        }
    }

    /**
     * Remove the given item from the set.
     *
     * @return whether the item was removed.
     */
    public boolean remove(T item) {
        if (this.mBounds.intersects(item.getBoundsForQuadTree())) {
            if (this.mChildren != null) {
                boolean removed = false;
                for (QuadTree<T> mChild : mChildren) {
                    if (mChild.remove(item)) {
                        removed = true;
                    }
                }
                return removed;
            } else {
                if (mItems == null) {
                    return false;
                }
                return mItems.remove(item);
            }
        }
        return false;
    }

    /**
     * Removes all points from the quadTree
     */
    public void clear() {
        mChildren = null;
        if (mItems != null) {
            mItems.clear();
        }
    }

    /**
     * Search for all items within a given bounds.
     */
    public Collection<T> search(Bounds searchBounds) {
        final Set<T> results = new HashSet<T>();
        search(searchBounds, results);
        return results;
    }

    private void search(Bounds searchBounds, Collection<T> results) {
        if (!mBounds.intersects(searchBounds)) {
            return;
        }

        if (this.mChildren != null) {
            for (QuadTree<T> quad : mChildren) {
                quad.search(searchBounds, results);
            }
        } else if (mItems != null) {
            if (searchBounds.contains(mBounds)) {
                results.addAll(mItems);
            } else {
                for (T item : mItems) {
                    if (searchBounds.intersects(item.getBoundsForQuadTree())) {
                        results.add(item);
                    }
                }
            }
        }
    }

    public boolean hasIntersection(T item) {
        final Bounds bounds = item.getBoundsForQuadTree();
        if (!mBounds.intersects(bounds)) {
            return false;
        }

        if (this.mChildren != null) {
            for (QuadTree<T> quad : mChildren) {
                if (quad.hasIntersection(item)) {
                    return true;
                }
            }
        } else if (mItems != null) {
                for (T mItem : mItems) {
                    if (mItem != item && bounds.intersects(mItem.getBoundsForQuadTree())) {
                        return true;
                    }
                }
        }
        return false;
    }

    private static <T extends QuadTree.Item> Bounds getTotalBounds(Collection<T> elements) {
        double maxX = - Double.MAX_VALUE;
        double minX = Double.MAX_VALUE;
        double maxY = - Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;

        for (T element : elements) {
            final Bounds bounds = element.getBoundsForQuadTree();
            maxX = Math.max(bounds.maxX, maxX);
            minX = Math.min(bounds.minX, minX);
            maxY = Math.max(bounds.maxY, maxY);
            minY = Math.min(bounds.minY, minY);
        }

        return new Bounds(minX, maxX, minY, maxY);
    }
}
