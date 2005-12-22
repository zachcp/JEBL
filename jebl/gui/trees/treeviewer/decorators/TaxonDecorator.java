package jebl.gui.trees.treeviewer.decorators;

import jebl.evolution.taxa.Taxon;

import java.awt.*;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id$
 */
public interface TaxonDecorator {
    Paint getTaxonPaint(Taxon taxon);
    Font getTaxonFont(Taxon taxon, Font font);
}
