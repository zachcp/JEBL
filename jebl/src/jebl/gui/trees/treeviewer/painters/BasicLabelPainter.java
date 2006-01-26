package jebl.gui.trees.treeviewer.painters;

import jebl.evolution.graphs.Node;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.Tree;
import org.virion.jam.controlpanels.ControlPalette;
import org.virion.jam.controlpanels.Controls;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class BasicLabelPainter extends AbstractPainter<Node> {

	public static final String TAXON_NAMES = "Taxon Names";
	public static final String NODE_HEIGHTS = "Node Heights";
	public static final String BRANCH_LENGTHS = "Branch Lengths";

	public BasicLabelPainter(String title, Tree tree) {
	    this(title, tree, false, 6);
	}

	public BasicLabelPainter(String title, Tree tree, boolean includeTaxonNames, int defaultSize) {
		this.title = title;

		this.defaultFontSize = defaultSize;
		taxonLabelFont = new Font("sansserif", Font.PLAIN, defaultFontSize);

		this.tree = tree;

		Set<String> names = new TreeSet<String>();
		for (Node node : tree.getNodes()) {
		    names.addAll(node.getAttributeNames());
		}

        this.attribute = null;

        List<String> sources = new ArrayList<String>();
        if ( includeTaxonNames ) {
            sources.add(TAXON_NAMES);
        }

        if( tree instanceof RootedTree && ((RootedTree)tree).hasHeights() ) {
            sources.add(NODE_HEIGHTS);
            this.attribute  = NODE_HEIGHTS;
        }

        if( tree instanceof RootedTree && ((RootedTree)tree).hasLengths() ) {
            sources.add(BRANCH_LENGTHS);
        }

        sources.addAll(names);

        if( this.attribute == null && sources.size() > 0 ) {
           this.attribute = sources.get(0);
        } else {
            this.attribute = "";
        }

        this.attributes = new String[sources.size()];
		sources.toArray(this.attributes);
	}

	protected String getLabel(Node node) {
		if (attribute.equalsIgnoreCase(TAXON_NAMES)) {
		    return tree.getTaxon(node).getName();
		} else if (attribute.equalsIgnoreCase(NODE_HEIGHTS) && tree instanceof RootedTree) {
	        return Double.toString(((RootedTree)tree).getHeight(node));
	    } else if (attribute.equalsIgnoreCase(BRANCH_LENGTHS) && tree instanceof RootedTree) {
	            return Double.toString(((RootedTree)tree).getLength(node));
	    } else {
	        Object value = node.getAttribute(attribute);
	        if (value != null) {
	            return value.toString();
	        }
	    }
	    return null;
	}

	private int defaultFontSize;

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        firePainterChanged();
    }

    public void calibrate(Graphics2D g2, Node item) {
        Font oldFont = g2.getFont();
        g2.setFont(taxonLabelFont);

        FontMetrics fm = g2.getFontMetrics();
        preferredHeight = fm.getHeight();
        preferredWidth = 0;

        String label = getLabel(item);
        if (label != null) {
            Rectangle2D rect = fm.getStringBounds(label, g2);
            preferredWidth = rect.getWidth();
        }

        yOffset = (float)(fm.getAscent());

        g2.setFont(oldFont);
    }

    public double getPreferredWidth() {
        return preferredWidth;
    }

    public double getPreferredHeight() {
        return preferredHeight;
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

    public void paint(Graphics2D g2, Node item, Justification justification, Rectangle2D bounds) {
        Font oldFont = g2.getFont();

        if (background != null) {
            g2.setPaint(background);
            g2.fill(bounds);
        }

        if (borderPaint != null && borderStroke != null) {
            g2.setPaint(borderPaint);
            g2.setStroke(borderStroke);
            g2.draw(bounds);
        }

        g2.setPaint(foreground);
        g2.setFont(taxonLabelFont);

        String label = getLabel(item);
        if (label != null) {

            Rectangle2D rect = g2.getFontMetrics().getStringBounds(label, g2);

            float xOffset;
            switch (justification) {
                case CENTER:
                    xOffset = (float)(bounds.getX() + (bounds.getWidth() - rect.getWidth()) / 2.0);
                    break;
                case FLUSH:
                case LEFT:
                    xOffset = (float)bounds.getX();
                    break;
                case RIGHT:
                    xOffset = (float)(bounds.getX() + bounds.getWidth() - rect.getWidth());
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized alignment enum option");
            }

            g2.drawString(label, xOffset, yOffset + (float)bounds.getY());
        }

        g2.setFont(oldFont);
    }

	public String[] getAttributes() {
		return attributes;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
		firePainterChanged();
	}

    public void setControlPanel(ControlPalette controlPalette) {
        // nothing to do
    }

    public List<Controls> getControls() {

        List<Controls> controlsList = new ArrayList<Controls>();

        if (controls == null) {
            OptionsPanel optionsPanel = new OptionsPanel();

            final JCheckBox checkBox1 = new JCheckBox("Show " + getTitle());
            optionsPanel.addComponent(checkBox1);

            checkBox1.setSelected(isVisible());

	        final JComboBox combo1 = new JComboBox(getAttributes());
	        combo1.addItemListener(new ItemListener() {
	            public void itemStateChanged(ItemEvent itemEvent) {
	                String attribute = (String)combo1.getSelectedItem();
		            setAttribute(attribute);
	            }
	        });

	        optionsPanel.addComponentWithLabel("Display:", combo1);
            final JSpinner spinner1 = new JSpinner(new SpinnerNumberModel(defaultFontSize, 0.01, 48, 1));

            final JLabel label1 = optionsPanel.addComponentWithLabel("Font Size:", spinner1);
            label1.setEnabled(checkBox1.isSelected());
            spinner1.setEnabled(checkBox1.isSelected());

            checkBox1.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    label1.setEnabled(checkBox1.isSelected());
                    spinner1.setEnabled(checkBox1.isSelected());
                    setVisible(checkBox1.isSelected());
                }
            });

            spinner1.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    setFontSize(((Double)spinner1.getValue()).floatValue());
                }
            });

	        controls = new Controls(getTitle(), optionsPanel, false);
        }

        controlsList.add(controls);

        return controlsList;
    }

    private Controls controls = null;

	public String getTitle() {
		return title;
	}

	private final String title;

    private Paint foreground = Color.BLACK;
    private Paint background = null;
    private Paint borderPaint = null;
    private Stroke borderStroke = null;

    private Font taxonLabelFont;
    private double preferredWidth;
    private double preferredHeight;
    private float yOffset;

    private boolean visible = true;

	private final Tree tree;
	protected String attribute;
	protected String[] attributes;
}
