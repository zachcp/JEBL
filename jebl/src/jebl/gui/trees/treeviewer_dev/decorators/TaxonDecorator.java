package jebl.gui.trees.treeviewer_dev.decorators;

import jebl.evolution.taxa.Taxon;

import java.awt.*;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public interface TaxonDecorator {
    Paint getTaxonPaint(Taxon taxon);
    Font getTaxonFont(Taxon taxon, Font font);
}
