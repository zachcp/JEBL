package jebl.gui.trees.treeviewer.painters;

import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.Tree;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * @author rambaut
 *         Date: Dec 6, 2005
 *         Time: 10:23:17 PM
 */
public class BasicTaxonLabelPainter extends AbstractPainter implements TaxonLabelPainter {

    public BasicTaxonLabelPainter(Tree tree) {
        taxonLabelFont = new Font("sansserif", Font.PLAIN, 6);
	    this.tree = tree;
    }

    public void calibrate(Graphics2D g2) {
        Font oldFont = g2.getFont();
        g2.setFont(taxonLabelFont);

        FontMetrics fm = g2.getFontMetrics();
        double labelHeight = fm.getHeight();
        double maxLabelWidth = 0.0;

        for (Taxon taxon : tree.getTaxa()) {
	        Rectangle2D rect = fm.getStringBounds(taxon.getName(), g2);
            if (rect.getWidth() > maxLabelWidth) {
                maxLabelWidth = rect.getWidth();
            }
        }

        width = maxLabelWidth;
        height = labelHeight;

        yOffset = (float)(fm.getAscent());

        g2.setFont(oldFont);
    }

    public double getPreferredWidth() {
        return width;
    }

    public double getPreferredHeight() {
        return height;
    }

    public void setFontSize(float size) {
        taxonLabelFont = taxonLabelFont.deriveFont(size);
        firePainterChanged();
    }

    public void setForeground(Paint foreground) {
        this.foreground = foreground;
        firePainterChanged();
    }

    public void setBackground(Paint background) {
        this.background = background;
        firePainterChanged();
    }

    public void setBorder(Paint borderPaint, Stroke borderStroke) {
        this.borderPaint = borderPaint;
        this.borderStroke = borderStroke;
        firePainterChanged();
    }

    public void paintTaxonLabel(Graphics2D g2, Taxon taxon, LabelAlignment labelAlignment, Insets insets) {
        Font oldFont = g2.getFont();

        if (background != null) {
            g2.setPaint(background);
            g2.fill(new Rectangle2D.Double(0.0, 0.0, width, height));
        }

        if (borderPaint != null && borderStroke != null) {
            g2.setPaint(borderPaint);
            g2.setStroke(borderStroke);
            g2.draw(new Rectangle2D.Double(0.0, 0.0, width, height));
        }

        g2.setPaint(foreground);
        g2.setFont(taxonLabelFont);

        String label = taxon.getName();
	    Rectangle2D rect = g2.getFontMetrics().getStringBounds(label, g2);

        float xOffset;
        switch (labelAlignment) {
            case CENTER:
	            xOffset = (float)(insets.left + (width - rect.getWidth()) / 2.0);
                break;
            case FLUSH:
            case LEFT:
                xOffset = (float)insets.left;
                break;
            case RIGHT:
                xOffset = (float)(insets.left + width - rect.getWidth());
                break;
            default:
	            throw new IllegalArgumentException("Unrecognized alignment enum option");
        }

        g2.drawString(label, xOffset, yOffset + insets.top);

        g2.setFont(oldFont);
    }

    public JPanel getControlPanel() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel1.add(new JLabel("Font Size:"));
        final JSpinner spinner1 = new JSpinner(new SpinnerNumberModel(6, 1, 48, 1));

        spinner1.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                setFontSize((Integer)spinner1.getValue());
            }
        });
        panel1.add(spinner1);
        panel.add(panel1);
        panel1.setAlignmentX(Component.LEFT_ALIGNMENT);


        JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel2.add(new JLabel("Colour taxa by:"));
        final JComboBox combo1 = new JComboBox();
        combo1.addItem("None");
//        for (AnnotationDefinition annotation : annotations) {
//            combo1.addItem(annotation);
//        }
//        combo1.addItemListener(new ItemListener() {
//            public void itemStateChanged(ItemEvent itemEvent) {
//                Object item = combo1.getSelectedItem();
//                if (item instanceof AnnotationDefinition) {
//                    setTaxonDecorator(new AnnotationTaxonDecorator((AnnotationDefinition)item));
//                } else {
//                    setBranchDecorator(null);
//                }
//            }
//        });
        panel2.add(combo1);
        panel.add(panel2);
        panel2.setAlignmentX(Component.LEFT_ALIGNMENT);

        return panel;
    }

	private final Tree tree;
	private Paint foreground = Color.BLACK;
    private Paint background = null;
    private Paint borderPaint = null;
    private Stroke borderStroke = null;

    private Font taxonLabelFont;
    private double width;
    private double height;
    private float yOffset;
}
