package jebl.gui.trees.treeviewer_dev.decorators;

import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.Tree;
import jebl.evolution.graphs.Node;

import java.awt.*;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id$
 */
public class AttributeTaxonDecorator implements TaxonDecorator {
    
    public AttributeTaxonDecorator(String paintAttributeName, String fontAttributeName) {
        this.paintAttributeName = paintAttributeName;
        this.fontAttributeName = fontAttributeName;
    }

    public Paint getTaxonPaint(Taxon taxon) {
        Paint paint = getPaint(taxon.getAttribute(paintAttributeName));
        if (paint == null) return Color.BLACK;
        return paint;
    }

    public Font getTaxonFont(Taxon taxon, Font font) {
        String value = (String)taxon.getAttribute(fontAttributeName);
        if (value != null) {
            return Font.decode(value.toString());
        }

        return font;
    }

    protected Paint getPaint(Object value) {
        if (value != null) {
            return Color.decode(value.toString());
        }
        return null;
    }

    protected final String paintAttributeName;
    protected final String fontAttributeName;
}
