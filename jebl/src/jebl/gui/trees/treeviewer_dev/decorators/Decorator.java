package jebl.gui.trees.treeviewer_dev.decorators;

import jebl.evolution.taxa.Taxon;

import java.awt.*;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public interface Decorator {

    void setItem(Object item);

    Paint getPaint(Paint paint);
    Stroke getStroke(Stroke stroke);
    Font getFont(Font font);
}
