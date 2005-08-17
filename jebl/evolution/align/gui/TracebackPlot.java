package jebl.evolution.align.gui;

import jebl.evolution.align.TracebackPlotter;
import jebl.evolution.align.Traceback;

import javax.swing.*;
import java.util.List;
import java.util.ArrayList;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public class TracebackPlot extends JComponent implements TracebackPlotter {

    List tracebacks = new ArrayList();
    int m, n;
    int scale = 1;
    static final int TARGET_SIZE = 400;

    public TracebackPlot() {}

    public void newTraceBack(String sequence1, String sequence2) {

        m = sequence1.length();
        n = sequence2.length();

        while (Math.max(m,n)*scale < TARGET_SIZE) { scale += 1; }

        tracebacks.clear();
    }

    public void traceBack(Traceback t) {
        tracebacks.add(t);
    }

    public void finishedTraceBack() {
        repaint();
    }

    public void paintComponent(Graphics g) {

        Graphics2D g2d = (Graphics2D)g;

        int width = getWidth();
        int height = getHeight();

        double scale = Math.min((double)width/(double)m,(double)height/(double)n);

        float strokeWidth = 1.0f;
        if (scale > 2.0) {
            strokeWidth = 2.0f;
        } else if (scale < 0.5) {
            strokeWidth = 0.5f;
        }
        g2d.setStroke(new BasicStroke(strokeWidth));

        g2d.draw(new Rectangle2D.Double(0,0,(double)m*scale - strokeWidth,(double)n*scale - strokeWidth));

        for (int i = 1; i < getPointCount(); i++) {
            Point2D p = getPoint(i-1, scale);
            Point2D q = getPoint(i, scale);
            Line2D line = new Line2D.Double(p,q);
            g2d.draw(line);
        }
    }

    public Point2D getPoint(int i, double scale) {
        Traceback t = (Traceback)tracebacks.get(i);
        return new Point2D.Double((double)t.getX()*scale, (double)t.getY()*scale);
    }

    public int getPointCount() { return tracebacks.size(); }

    public Dimension getPreferredSize() {
        return new Dimension(m*scale,n*scale);
    }
}
