package jebl.gui.trees.treeviewer;

import jebl.evolution.graphs.Node;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

/**
 * @author Joseph Heled
 * @version $Id$
 */
public abstract class TreeDrawableElement {
    final protected Node node;

    TreeDrawableElement(Node  node) {
        this.node = node;
    }
    
    public Point2D.Double getCenter() {
       final Rectangle2D b = getBounds();
       return new Point2D.Double( b.getX() + b.getWidth() / 2,  b.getY() + b.getHeight()/2 );
    }

    public double getRadious2() {
        final Rectangle2D b = getBounds();
        final double width = b.getWidth();
        final double h = b.getHeight();
        return (width*width + h*h)/ 4.0;
    }

    public double getRadious() {
        return Math.sqrt( getRadious2() );
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

    public void setVisible(boolean visible) { this.visible = visible; }

    public boolean isVisible() {  return visible; }

    public abstract boolean intersects(TreeDrawableElement e);

    public abstract Rectangle2D getBounds();

    public abstract void setSize(int size, Graphics2D g2);

    public abstract int getCurrentSize();

    public abstract String getDebugName();

    protected abstract void drawIt(Graphics2D g2);

    public final void draw(Graphics2D g2) {
        if( ! visible ) {
            return;
        }
        drawIt(g2);
    }

    abstract public int getMinSize();
    abstract public int getMaxSize();

    public abstract int getPriority();

    private static class Stats {
        double sum = 0.0;
        double sum2 = 0.0;
        long n = 0;

        void add(double x) {
            sum += x;
            sum2 += x*x;
            ++n;
        }

        double std2() {
            final double avg = sum / n;
            return (sum2 / n) - avg*avg;
        }
    }

    enum ElementSortMethod {SORTO, SORTX, SORTY}

    // debugging
    static boolean expensiveAssert = false;
    static boolean smallAsserts = false;
    static boolean prints = false;

    static void setClashingVisiblitiy(Collection<TreeDrawableElement> elements, Graphics2D g2) {
        final List<TreeDrawableElement> list = new ArrayList<TreeDrawableElement>(elements);
        double maxRadious2 = 0.0, maxDY = 0.0, maxDX = 0.0;
        Stats x = new Stats();
        Stats y = new Stats();
        Stats o = new Stats();

        final Map<TreeDrawableElement, double[]> d = new HashMap<TreeDrawableElement, double[]>(list.size());

        for( TreeDrawableElement e : elements ) {
            final Rectangle2D bounds = e.getBounds();
            final double x1 = bounds.getMinX();
            x.add(x1);
            final double y1 = bounds.getMinY();
            y.add(y1);
            final double o1 = e.getCenter().distance(0, 0);
            o.add(o1);

            d.put(e, new double[]{o1, x1, y1});

            if( bounds.getHeight() > maxDY ) {
                maxDY = bounds.getHeight();
            }
            if( bounds.getWidth() > maxDX ) {
                maxDX = bounds.getWidth();
            }
            final double r = e.getRadious2();
            if( r > maxRadious2 ) {
                maxRadious2 = r;
            }
        }
        final double maxRadious = Math.sqrt(maxRadious2);

        final double xstd2 = x.std2() / maxDX;
        final double ystd2 = y.std2() / maxDY;
        final double ostd2 = o.std2() / maxRadious;
        final ElementSortMethod s;

        if( ostd2 > Math.max(xstd2, ystd2) ) {
            s = ElementSortMethod.SORTO;
        } else {
            s = xstd2 > ystd2 ? ElementSortMethod.SORTX :  ElementSortMethod.SORTY;
        }

        final int which = s.ordinal();
        // order must match that of enum
        double[] limits = {2*maxRadious, maxDX, maxDY};
        double maxxRadious =  limits[which];

        Collections.sort(list, new Comparator<TreeDrawableElement>() {
            public int compare(TreeDrawableElement o1, TreeDrawableElement o2) {
                final double[] v1 = d.get(o1);
                final double[] v2 = d.get(o2);

                return (int)Math.signum(v1[which] - v2[which]);
            }
        });

        int nChecks = 0;

        // build list of clashes for each element at max size
        Map<TreeDrawableElement, List<TreeDrawableElement> >
                conflicts = new HashMap<TreeDrawableElement, List<TreeDrawableElement>>();

        int last = 0;
        for(int k = 0; k < list.size(); ++k) {
            final TreeDrawableElement ek = list.get(k);
            final double ekd = d.get(ek)[which];

            if( expensiveAssert ) {
                for( int j = 0; j < last; ++j) {
                    if( intersects(ek, list.get(j)) ) {
                        System.out.println(k + "/" + j);
                        assert false;
                    }
                }
            }

            for(int j = last; j < k; ++j) {
                final TreeDrawableElement ej = list.get(j);
                ++nChecks;
                if( intersects(ek, ej) ) {
                    if( ! conflicts.containsKey(ek) ) {
                        conflicts.put(ek, new ArrayList<TreeDrawableElement>());
                    }
                    conflicts.get(ek).add(ej);

                    if( ! conflicts.containsKey(ej) ) {
                        conflicts.put(ej, new ArrayList<TreeDrawableElement>());
                    }
                    conflicts.get(ej).add(ek);
                }

                if( ekd - d.get(ej)[which] > maxxRadious ) {
                    assert last == j;
                    ++last;
                }
            }
        }

        if (prints)
            System.out.println("using " + s.toString() + " did " + nChecks + " intersect checks for "
                    + list.size() +" elemens " + (nChecks*100.0) / (list.size()*(list.size()-1)/2));

        if( conflicts.size() == 0 ) {
            // lucky - all clear
            return;
        }

        // Resize all clashing elements to smallest size. Remove element so that no more clashes remain.
        // inspect elemnents in decending priority order to remove lower priority elements first.

        final Set<TreeDrawableElement> clashingElements = conflicts.keySet();

        if( prints ) {
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
                if( dp == 0 ) {
                    // enforce arbitrary order on node with equal priority for repeatability
                    return o2.getNode().hashCode() - o1.getNode().hashCode();
                }
                return dp;
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
                 // not clashed after removing, can bring back to normal size
                e.setSize(e.getMaxSize(), g2);
                clashingElements.remove(e);
            }
        }

        //ascheck(list);

        queue.clear();

        for (TreeDrawableElement e : clashingElements) {
            queue.add(e);
        }

        while (queue.peek() != null) {
            TreeDrawableElement e = queue.poll();
            final List<TreeDrawableElement> conflicting = conflicts.get(e);

            int size = e.getMaxSize();
            e.setSize(size, g2);

            if(prints) System.out.println("** Start for " + e.getDebugName());

            while( size >= e.getMinSize() ) {
                e.setSize(size, g2);
                int nc = 0;
                for(; nc < conflicting.size(); ++nc) {
                   if( intersects(e, conflicting.get(nc) ) ) {
                       break;
                   }
                    if(prints) System.out.println(e.getDebugName() + " is ok with " + conflicting.get(nc).getDebugName() +
                         " (" + conflicting.get(nc).getCurrentSize() + ")" );
                }
                if( nc == conflicting.size() ) {
                    break;
                }
                --size;
            }

           // ascheck(list);

            assert size >= e.getMinSize() : "for " + e.getDebugName();

            int priority = e.getPriority();

            for( TreeDrawableElement ec : conflicting ) {
                if( ec.getPriority() == priority ) {
                    final int ecs = ec.getCurrentSize();
                    // size should only go down to accomodate others of same priority
                    if( ecs >= size )  {
                        continue;
                    }
                    final int sizeMax = size;

                    int ecSize = ecs;
                    
                    if(prints) System.out.println("resolve conflict of " + e.getDebugName() + " with " + ec.getDebugName() + " - " + ecs);
                    if( smallAsserts  ) {
                        assert !intersects(e, ec);
                    }

                    while( ecSize < size ) {
                        // take ec up, exit loop with no intersection
                        while( ecSize < size && ecSize < ec.getMaxSize() ) {
                            ec.setSize(ecSize + 1, g2);
                            if( ! intersects(e, ec) ) {
                                ++ecSize;
                            } else {
                                ec.setSize(ecSize, g2);
                                break;
                            }
                        }
                        if( smallAsserts  ) assert ! intersects(e, ec);

//                        boolean intersects = false;
//                        while( ecSize < size && size > e.getMinSize() && ! intersects ) {
//                            --size;
//                            e.setSize(size, g2);
//                            intersects = intersects(e, ec);
//                        }

                        if( ecSize < size && size > e.getMinSize() ) {
                            --size;
                            e.setSize(size, g2);
                        }
                        if( smallAsserts  ) assert ! intersects(e, ec);
                    }

                    while( size+1 < sizeMax ) {
                        e.setSize(size+1, g2);
                        if( intersects(e, ec) ) {
                            e.setSize(size, g2);
                            break;
                        }
                        ++size;
                    }

                    if( smallAsserts  ) assert ! intersects(e, ec);
                    ec.setSize(ecs, g2);
                    if( smallAsserts  ) assert ! intersects(e, ec);
                }
            }
            if( smallAsserts  ) {
                for (TreeDrawableElement aConflicting : conflicting) {
                    if (intersects(e, aConflicting)) {
                        System.out.println(e.getDebugName() + " " + e.getCurrentSize() + " conflicts with " +
                                aConflicting.getDebugName() + " " + aConflicting.getCurrentSize());
                        assert false;
                    }
                }
            }
          //  ascheck(list);
        }
        ascheck(list);
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
