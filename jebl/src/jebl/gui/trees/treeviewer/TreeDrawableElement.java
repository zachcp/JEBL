package jebl.gui.trees.treeviewer;

import jebl.evolution.graphs.Node;
import jebl.util.Bounds;
import jebl.util.QuadTree;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

/**
 * @author Joseph Heled
 */
public abstract class TreeDrawableElement implements QuadTree.Item {
    final protected Node node;
    protected Paint foreground;

    TreeDrawableElement(Node  node) {
        this.node = node;
        this.foreground = Color.black;
    }
    
    public Point2D.Double getCenter() {
       final Rectangle2D b = getBounds();
       return new Point2D.Double( b.getX() + b.getWidth() / 2,  b.getY() + b.getHeight()/2 );
    }

    public double getRadius2() {
        final Rectangle2D b = getBounds();
        final double width = b.getWidth();
        final double h = b.getHeight();
        return (width*width + h*h)/ 4.0;
    }

    public double getRadius() {
        return Math.sqrt( getRadius2() );
    }

    public static boolean intersects(TreeDrawableElement e1, TreeDrawableElement e2) {
        final Rectangle2D d = e1.getBounds();
        final Rectangle2D d1 = e2.getBounds();
        //System.out.println(e1.getDebugName() + " bounds " + e1.getBounds().toString());
        //System.out.println(e2.getDebugName() + " bounds " + e2.getBounds().toString());
        return d1.intersects(d) && e1.intersects(e2);
    }

    public Node getNode() {
        return node;
    }

    boolean visible = true;
    protected boolean isAutoContracted;

    public void setVisible(boolean visible) { this.visible = visible; }

    public boolean isVisible() {  return visible; }

    public void setIsAutoContracted(boolean isAutoContracted) {
        this.isAutoContracted = isAutoContracted;
    }

    public boolean isAutoContracted() {
        return isAutoContracted;
    }

    public void setForeground(Paint p){
        this.foreground = p;
    }

    public Paint getForeground(){
        return foreground;
    }

    public abstract boolean intersects(TreeDrawableElement e);

    public abstract Rectangle2D getBounds();

    public abstract void setSize(int size, Graphics2D g2);

    public abstract int getCurrentSize();

    public abstract String getDebugName();

    protected abstract void drawIt(Graphics2D g2);

    public final void draw(Graphics2D g2, Shape viewRect) {
        boolean doit = true;
        if( viewRect != null ) {
            final Rectangle2D d = getBounds();
//            final Point viewPosition = vp.getViewPosition();
//            final Rectangle rectangle = vp.getBounds();
//            rectangle.translate(viewPosition.x, viewPosition.y);
            doit = viewRect.intersects(d);
        }
        if( doit ) {
            drawIt(g2);
        }
    }

    abstract public int getMinSize();
    abstract public int getMaxSize();

    public abstract int getPriority();

    public abstract boolean hit(Graphics2D g2, Rectangle rect);

    // Size of variance for a set
    private static class VarianceIndication {
        double sum = 0.0;
        double sum2 = 0.0;
        long n = 0;

        void add(double x) {
            sum += x;
            sum2 += x*x;
            ++n;
        }

        double variance() {
            final double avg = sum / n;
            return (sum2 / n) - avg*avg;
        }
    }

    enum ElementSortMethod {SORTO, SORTX, SORTY}

    // debugging
    static boolean expensiveAssert = false;
    static boolean smallAsserts = false;
    static int prints = 0;

    @Override
    public Bounds getBoundsForQuadTree() {
        return new Bounds(getBounds());
    }

    static void newSetOverlappingVisiblitiy(Collection<TreeDrawableElement> elements, Graphics2D g2) {
//        if (true) return;
//        long start = System.currentTimeMillis();
        QuadTree<TreeDrawableElement> quadTree = new QuadTree<TreeDrawableElement>(elements);

        for (TreeDrawableElement element : elements) {
            final boolean intersectsAnything = quadTree.hasIntersection(element);
            element.setVisible(!intersectsAnything);
        }
//        System.out.println("SetOverlappingVisibility: " + (System.currentTimeMillis() - start));
    }



    static void setOverlappingVisiblitiy(Collection<TreeDrawableElement> elements, Graphics2D g2) {
        if(elements.size() > TreePane.BIG_TREE_TAXA_THRESHOLD) {
            return; // this method has terrible performance on large data sets
        }
//        long start = System.currentTimeMillis();

        final List<TreeDrawableElement> list = new ArrayList<TreeDrawableElement>(elements);

        // get variance of each "axis of projection" (x y and distance-from origin) and element maximum along each
        // axis
        double maxRadious2 = 0.0, maxDY = 0.0, maxDX = 0.0;
        VarianceIndication x = new VarianceIndication();
        VarianceIndication y = new VarianceIndication();
        VarianceIndication o = new VarianceIndication();

        // establish a map in the process which saves the 3 values for each element. Used in sorting later
        final Map<TreeDrawableElement, double[]> elementAxisValues =
                new HashMap<TreeDrawableElement, double[]>(list.size());

        for( TreeDrawableElement e : elements ) {
            final Rectangle2D bounds = e.getBounds();
            final double x1 = bounds.getMinX();
            x.add(x1);

            final double y1 = bounds.getMinY();
            y.add(y1);

            final double o1 = e.getCenter().distance(0, 0);
            o.add(o1);

            elementAxisValues.put(e, new double[]{o1, x1, y1});

            if( bounds.getHeight() > maxDY ) {
                maxDY = bounds.getHeight();
            }

            if( bounds.getWidth() > maxDX ) {
                maxDX = bounds.getWidth();
            }

            final double r = e.getRadius2();
            if( r > maxRadious2 ) {
                maxRadious2 = r;
            }
        }

        // use the projection which has the largest ratio of variance to max, which is the one likely to require
        // the least number of pairwise element comparisons

        final double maxRadious = Math.sqrt(maxRadious2);

        final double xstd2 = x.variance() / maxDX;
        final double ystd2 = y.variance() / maxDY;
        final double ostd2 = o.variance() / maxRadious;
        final ElementSortMethod s;

        if( ostd2 > Math.max(xstd2, ystd2) ) {
            s = ElementSortMethod.SORTO;
        } else {
            s = xstd2 > ystd2 ? ElementSortMethod.SORTX :  ElementSortMethod.SORTY;
        }

        // which projection is used
        final int which = s.ordinal();

        // order must match that of enum
        double[] limits = {2*maxRadious, maxDX, maxDY};

        // distance along projection which insures two elements further apart than that can't overlap
        double nonOverlapRadius =  limits[which];

        // order elements according to projection
        Collections.sort(list, new Comparator<TreeDrawableElement>() {
            public int compare(TreeDrawableElement o1, TreeDrawableElement o2) {
                final double[] v1 = elementAxisValues.get(o1);
                final double[] v2 = elementAxisValues.get(o2);

                return (int)Math.signum(v1[which] - v2[which]);
            }
        });

        // debug prints only
        int nChecks = 0;

        // build list of clashes for each element at max size
        Map<TreeDrawableElement, List<TreeDrawableElement> >
                conflicts = new HashMap<TreeDrawableElement, List<TreeDrawableElement>>();

        // first element that need to be compared with current one. all the ones prior to it
        // are known to be non clashing
        int last = 0;
        for(int k = 0; k < list.size(); ++k) {
            final TreeDrawableElement ek = list.get(k);
            final double ekd = elementAxisValues.get(ek)[which];

            if( expensiveAssert ) {
                for( int j = 0; j < last; ++j) {
                    if( intersects(ek, list.get(j)) ) {
                        System.out.println(k + "/" + j);
                        assert false;
                    }
                }
            }
            if(prints>0) System.out.println("checking " + k + " " + ek.getDebugName() + "(" + ek.getCurrentSize() + ")");

            for(int j = last; j < k; ++j) {
                final TreeDrawableElement ej = list.get(j);

                if( ekd - elementAxisValues.get(ej)[which] > nonOverlapRadius ) {
                    if( smallAsserts ) assert ! intersects(ek, ej);
                    assert last == j;
                    ++last;
                    continue;
                }

                ++nChecks;
                if(prints>0) System.out.print("  against " + j +  ej.getDebugName() + "(" + ej.getCurrentSize() + ")");

                if( intersects(ek, ej) ) {
                    // add ej on ek conflict list and vice versa
                    if( ! conflicts.containsKey(ek) ) {
                        conflicts.put(ek, new ArrayList<TreeDrawableElement>());
                    }
                    conflicts.get(ek).add(ej);

                    if( ! conflicts.containsKey(ej) ) {
                        conflicts.put(ej, new ArrayList<TreeDrawableElement>());
                    }
                    conflicts.get(ej).add(ek);

                    if(prints>0) System.out.println(" - overlapps " );
                }  else {
                    if(prints>0) System.out.println(" - non overlapp");
                }
            }
        }

        if (prints>0)
            System.out.println("using " + s.toString() + " did " + nChecks + " intersect checks for "
                    + list.size() +" elemens " + (nChecks*100.0) / (list.size()*(list.size()-1)/2));

        if( conflicts.size() == 0 ) {
            // lucky - all clear
            return;
        }

        // Resize all clashing elements to smallest size. Remove elements as needed so that no clashes remain.
        // inspect elemnents in decending priority order to remove lower priority elements first.

        final Set<TreeDrawableElement> clashingElements = conflicts.keySet();

        if( prints>0 ) {
             for (TreeDrawableElement e : clashingElements) {
                 System.out.print(e.getDebugName() + " clashes:");
                 for( TreeDrawableElement c : conflicts.get(e) ) {
                    System.out.print(c.getDebugName() + ",");
                 }
                 System.out.println();
             }
        }

        // add everything to queue
        Comparator<? super TreeDrawableElement> comparator = new Comparator<TreeDrawableElement>() {
            public int compare(TreeDrawableElement o1, TreeDrawableElement o2) {
                final int dp = o2.getPriority() - o1.getPriority();
                if( dp != 0 ) {
                    return dp;
                }
                // enforce arbitrary order on element with equal priority for repeatability

                //return o1.hashCode() - o2.hashCode();
                int dn = o1.getNode().hashCode() - o2.getNode().hashCode();

                return dn;
            }
        };
        
        PriorityQueue<TreeDrawableElement> queue =
                new PriorityQueue<TreeDrawableElement>(clashingElements.size(), comparator);

        // resize to smaller size
        for (TreeDrawableElement e : clashingElements) {
            e.setSize(e.getMinSize(), g2);
            queue.add(e);
        }

        while (queue.peek() != null) {

            TreeDrawableElement e = queue.poll();
            if( ! e.isVisible() ) {
                // clash detected earlier and element not visible, nothing more to do
                assert !clashingElements.contains(e);
                continue;
            }

            final List<TreeDrawableElement> conflicting = conflicts.get(e);
            for (int nc = 0; nc < conflicting.size(); ++nc) {
                final TreeDrawableElement ec = conflicting.get(nc);
                if( intersects(e, ec) ) {
                    // intersects at smallest size, have to remove
                    ec.setVisible(false);
                    clashingElements.remove(ec);
                    conflicting.remove(nc);
                    --nc;
                }
            }

            if (conflicting.size() == 0) {
                // no clashed after removing, can simply restore normal size
                e.setSize(e.getMaxSize(), g2);
                clashingElements.remove(e);
            }
        }

        // second pass. Node try to enlarge remaining elements
        queue.clear();

        for (TreeDrawableElement e : clashingElements) {
            queue.add(e);
        }

        while (queue.peek() != null) {
            TreeDrawableElement label = queue.poll();
            final List<TreeDrawableElement> overlapping = conflicts.get(label);

            // start with maximal size for element
            int maxSize = label.getMaxSize();
            label.setSize(maxSize, g2);

            if(prints>0) System.out.println("** Start for " + label.getDebugName() + " (" + label.getCurrentSize() + ")");

            // loop until finding a size where no overlaps. this must be the case
            // for the min size
            while( maxSize >= label.getMinSize() ) {
                label.setSize(maxSize, g2);
                int nc = 0;
                for(; nc < overlapping.size(); ++nc) {
                   if( intersects(label, overlapping.get(nc) ) ) {
                        if(prints>0) System.out.println(label.getDebugName() + " (" + label.getCurrentSize() + ")"
                         + " overlaps " + overlapping.get(nc).getDebugName() +
                         " (" + overlapping.get(nc).getCurrentSize() + ")" );
                       break;
                   }
                   if(prints>0) System.out.println(label.getDebugName() + " (" + label.getCurrentSize() + ")"
                         + " is ok with " + overlapping.get(nc).getDebugName() +
                         " (" + overlapping.get(nc).getCurrentSize() + ")" );
                }
                if( nc == overlapping.size() ) {
                    // no overlaps - use this size
                    break;
                }
                --maxSize;
            }

            if(smallAsserts) assert maxSize >= label.getMinSize() : "for " + label.getDebugName() + " (" + maxSize + " >= " + label.getMinSize();

            // try to get elements with same priority to the same size if possible
            int priority = label.getPriority();

            for( TreeDrawableElement clashingLabel : overlapping ) {
                if( clashingLabel.getPriority() == priority ) {
                    final int ecs = clashingLabel.getCurrentSize();
                    // size should only go down to accomodate others of same priority
                    if( ecs >= maxSize )  {
                        // overlapping element is already larger
                        continue;
                    }

                    // save size as it is not allowed to go up
                    final int sizeMax = maxSize;

                    // size for overlapping element
                    int overlappingSize = ecs;
                    
                    if(prints>0) System.out.println("resolve conflict of " + label.getDebugName() + " with " + clashingLabel.getDebugName() + " - " + ecs);
                    if( smallAsserts  ) {
                        assert !intersects(label, clashingLabel);
                    }

                    boolean keepGoing = true;
                    while( overlappingSize < maxSize && keepGoing) {
                        // take ec up, exit loop with no intersection
                        while( overlappingSize < maxSize && overlappingSize < clashingLabel.getMaxSize() ) {
                            clashingLabel.setSize(overlappingSize + 1, g2);
                            if( ! intersects(label, clashingLabel) ) {
                                ++overlappingSize;
                            } else {
                                clashingLabel.setSize(overlappingSize, g2);
                                keepGoing = false;
                                break;
                            }
                        }
                        if( smallAsserts  ) assert ! intersects(label, clashingLabel);

                        // if overlapping element has still smaller size, take element size one down
                        if( overlappingSize < maxSize && maxSize > label.getMinSize() ) {
                            --maxSize;
                            label.setSize(maxSize, g2);
                        }
                        if( smallAsserts  ) assert ! intersects(label, clashingLabel);
                    }

                    // now set size of element to largest possible given known overlapper size
                    while( maxSize+1 < sizeMax ) {
                        label.setSize(maxSize+1, g2);
                        if( intersects(label, clashingLabel) ) {
                            label.setSize(maxSize, g2);
                            break;
                        }
                        ++maxSize;
                    }

                    if( smallAsserts  ) assert ! intersects(label, clashingLabel);

                    // restore overlapper to original size. it's true size will be known only when checked
                    // against all it's overlappers
                    clashingLabel.setSize(ecs, g2);
                    if( smallAsserts  ) assert ! intersects(label, clashingLabel);
                }
            }

            if( smallAsserts  ) {
                for (TreeDrawableElement aConflicting : overlapping) {
                    if (intersects(label, aConflicting)) {
                        System.out.println(label.getDebugName() + " " + label.getCurrentSize() + " conflicts with " +
                                aConflicting.getDebugName() + " " + aConflicting.getCurrentSize());
                        assert false;
                    }
                }
            }
        }
        ascheck(list);
//        System.out.println("SetOverlappingVisibility: " + (System.currentTimeMillis() - start));

    }

    private static void ascheck(List<TreeDrawableElement> list) {
        if( expensiveAssert ) {
            for(int k = 0; k < list.size(); ++k) {
                TreeDrawableElement ek = list.get(k);
                if( ek.isVisible() ) {
                    for(int j = 0; j < k; ++j) {
                        TreeDrawableElement ej = list.get(j);
                        if( ej.isVisible() ) {
                            boolean b = intersects(ek, ej);
                            if( b ) {
                                //b = intersects(ek, ej);
                               // b = intersects(ej, ek);
                                System.out.println(ek.getDebugName() + " (" + ek.getCurrentSize() + ") & "
                                        + ej.getDebugName() + " (" + ej.getCurrentSize() + ")");
                            }
                            assert !b;
                        }
                    }
                }
            }
        }
    }
}
