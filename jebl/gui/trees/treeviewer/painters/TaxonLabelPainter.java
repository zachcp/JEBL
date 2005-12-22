package jebl.gui.trees.treeviewer.painters;

import jebl.evolution.taxa.Taxon;

import javax.swing.*;
import java.awt.*;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id$
 */
public interface TaxonLabelPainter extends Painter {

    void calibrate(Graphics2D g2);

	void paintTaxonLabel(Graphics2D g2, Taxon taxon, LabelAlignment labelAlignment, Insets insets);

    JPanel getControlPanel();
}
