package jebl.evolution.align.gui;

import jebl.evolution.align.Traceback;
import jebl.evolution.align.TracebackPlotter;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public class TracebackPlot extends JComponent implements TracebackPlotter {

    List tracebacks = new ArrayList();
    int m, n;

    public TracebackPlot() {}

    public void newTraceBack(String sequence1, String sequence2) {

        m = sequence1.length();
        n = sequence2.length();

        tracebacks.add(new ArrayList());
    }

    public void traceBack(Traceback t) {
        ((List)tracebacks.get(tracebacks.size()-1)).add(t);
    }

    public void finishedTraceBack() {
        repaint();
    }

    public void clear() {
        tracebacks.clear();
        repaint();
    }

    public void paintComponent(Graphics g) {

        Graphics2D g2d = (Graphics2D)g;

        int width = getWidth();
        int height = getHeight();

        float tx = 40;
        float ty = 20;

        double scale = Math.min((width-tx)/(double)m,(height-ty)/(double)n);

        float strokeWidth = 1.0f;
        if (scale > 2.0) {
            strokeWidth = 2.0f;
        } else if (scale < 0.5) {
            strokeWidth = 0.5f;
        }

        String label = "0,0";
        float w = g2d.getFontMetrics().stringWidth(label);
        g2d.drawString(label,tx-w/2.0f, ty-2);

        label = m+"";
        w = g2d.getFontMetrics().stringWidth(label);
        g2d.drawString(m+"",(float)(m*scale+tx-w), ty-2);

        label = n+"";
        w = g2d.getFontMetrics().stringWidth(label);
        g2d.drawString(label,Math.max(2,tx-w-2), (float)(n*scale+ty-2));

        g2d.setStroke(new BasicStroke(strokeWidth));

        g2d.draw(new Rectangle2D.Double(tx,ty,(double)m*scale - strokeWidth,(double)n*scale - strokeWidth));

        for (int i = 0; i < tracebacks.size(); i++) {

            g2d.setColor(colors[i%colors.length]);
            for (int j = 1; j < getPointCount(i); j++) {
                Point2D p = getPoint(i, j-1, scale, tx, ty);
                Point2D q = getPoint(i, j, scale, tx, ty);
                Line2D line = new Line2D.Double(p,q);
                g2d.draw(line);
            }
        }
    }

    public Point2D getPoint(int i, int j, double scale, double tx, double ty) {
        Traceback t = (Traceback)((List)tracebacks.get(i)).get(j);
        return new Point2D.Double((double)t.getX()*scale+tx, (double)t.getY()*scale+ty);
    }

    public int getPointCount(int i) { return ((List)tracebacks.get(i)).size(); }

    private Color[] colors = new Color[] {
            Color.black,
            Color.red,
            Color.blue,
            Color.green,
            Color.yellow,
            Color.orange,
            Color.cyan,
            Color.magenta,
            Color.darkGray,
            Color.lightGray};
}
