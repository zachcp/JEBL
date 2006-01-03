package jebl.gui.trees.treeviewer.painters;

import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.Tree;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class BasicTaxonLabelPainter extends AbstractLabelPainter<Taxon> {

    public BasicTaxonLabelPainter(Tree tree, int defaultSize) {
	    super(defaultSize);
	    this.tree = tree;
    }

    public BasicTaxonLabelPainter(Tree tree) {
        super();
	    this.tree = tree;
    }

	protected String getLabel(Taxon taxon) {
		return taxon.getName();
	}

	public String getTitle() {
		return "Taxon Labels";
	}

	private final Tree tree;
}
