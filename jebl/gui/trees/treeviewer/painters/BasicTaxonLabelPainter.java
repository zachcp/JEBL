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

	protected double getMaxLabelWidth(Graphics2D g2) {
		double maxLabelWidth = 0.0;

		FontMetrics fm = g2.getFontMetrics();
		for (Taxon taxon : tree.getTaxa()) {
			Rectangle2D rect = fm.getStringBounds(taxon.getName(), g2);
		    if (rect.getWidth() > maxLabelWidth) {
		        maxLabelWidth = rect.getWidth();
		    }
		}

		return maxLabelWidth;
    }

	protected String getLabel(Taxon taxon) {
		return taxon.getName();
	}

	public String getTitle() {
		return "Taxon Labels";
	}

	private final Tree tree;
}
