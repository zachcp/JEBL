package jebl.gui.trees.treeviewer.painters;

import org.virion.jam.controlpanels.ControlPalette;
import org.virion.jam.controlpanels.Controls;
import org.virion.jam.controlpanels.ControlsSettings;
import org.virion.jam.panels.OptionsPanel;
import org.virion.jam.util.IconUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import jebl.evolution.trees.RootedTree;
import jebl.evolution.graphs.Node;
import jebl.gui.trees.treeviewer.TreeViewerUtilities;
import jebl.gui.trees.treeviewer.TreeViewer;

/**
 * @author Steven Stones-Havas
 * @version $Id$
 *          <p/>
 *          Created on 24/03/2009 11:35:41 AM
 */
public class TaxonLabelPainter extends AbstractPainter<Node>{

    private static final Preferences PREFS = Preferences.userNodeForPackage(TaxonLabelPainter.class);

    private RootedTree tree;
    private Set<String> attributes;
    private Controls controls;
    private boolean visible;
    private String attribute = TAXON_NAMES;


    public TaxonLabelPainter(RootedTree tree) {

        this.tree = tree;

        //populate the attributes list
        attributes = new LinkedHashSet();
        attributes.add(TAXON_NAMES);
        attributes.add(NODE_HEIGHTS);
        for(Node n : tree.getNodes()) {
            if(tree.isExternal(n)) { //only get attributes from tip nodes
                attributes.addAll(n.getAttributeNames());
            }
        }

        visible = PREFS.getBoolean("Tip Labels_isopoen", true);
    }


    public boolean isVisible() {
        return visible;
    }

    public void calibrate(Graphics2D g2) {

    }

    private String getLabel(Node node){
        String prefix = " ";
        String suffix = " ";
        if (attribute.equalsIgnoreCase(TAXON_NAMES)) {
            return prefix+tree.getTaxon(node).getName()+suffix;
        }

        if (attribute.equalsIgnoreCase(NODE_HEIGHTS) ) {
            return prefix+getFormattedValue(tree.getHeight(node))+suffix;
        } else if (attribute.equalsIgnoreCase(BRANCH_LENGTHS) ) {
            return prefix+getFormattedValue(tree.getLength(node))+suffix;
        }

        final Object value = node.getAttribute(attribute);
        if (value != null) {
            if (value instanceof Double) {
                return prefix+formatter.getFormattedValue((Double) value)+suffix;
            }
            if(value instanceof Date){
                DateFormat format = new SimpleDateFormat("dd MMM yyyy h:mm a");
                return  prefix+format.format((Date)value)+suffix;
            }
            String s = value.toString();
            //limit node labels to 15 chars (plus ...)
            //if(s.length() > 15)
            //    return s.substring(0,15)+"...";
            return prefix+s+suffix;
        }
        return null;    
    }

    public void paint(Graphics2D g2, Node item, Justification justification, Rectangle2D bounds) {
        final Font oldFont = g2.getFont();

        if(paintAsMirrorImage) {
            g2.scale(-1,1);
            g2.translate(-bounds.getWidth()-2*bounds.getX(),0);
        }

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
        Object fontString = item.getAttribute("labelFont");
        Font taxonLabelFont = TreeViewerUtilities.DEFAULT_FONT;
        if(fontString != null) {
            try {
                taxonLabelFont = TreeViewerUtilities.fontFromString(fontString.toString());
            }
            catch(IllegalArgumentException ex) {
                System.err.println("Invalid font string: "+fontString);
            }
        }
        Font taxonExponentFont = taxonLabelFont.deriveFont(taxonLabelFont.getSize2D()*0.66f);
        g2.setFont(taxonLabelFont);


        final String label = getLabel(item);
        if (label != null) {
            String prefix = label;
            String suffix = "";

            int exponentIndex = label.indexOf("E");
            if(exponentIndex >= 0){
                try{
                    Double.parseDouble(label.substring(0, exponentIndex).trim());
                    Integer.parseInt(label.substring(exponentIndex+1, label.length()).trim());
                    prefix = label.substring(0, exponentIndex)+"x10";
                    suffix = label.substring(exponentIndex+1, label.length());
                }
                catch(NumberFormatException ex){} //skip out
                catch(IndexOutOfBoundsException ex){} //skip out
            }

            Rectangle2D rect = g2.getFontMetrics().getStringBounds(prefix, g2);

            float xOffset = 0;
            float yOffset = g2.getFontMetrics().getAscent();
            float y = yOffset + (float) bounds.getY();
            switch (justification) {
                case CENTER:
                    break;
                case FLUSH:
                case LEFT:
                    xOffset = (float) bounds.getX();
                    break;
                case RIGHT:
                    xOffset = (float) (bounds.getX() + bounds.getWidth() - rect.getWidth());
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized alignment enum option");
            }

            g2.drawString(prefix, xOffset, y);
            if(suffix.length() > 0){
                g2.setFont(taxonExponentFont);
                g2.drawString(suffix, xOffset+(float)rect.getWidth(), y-(float)rect.getHeight()/2);
            }
        }

        g2.setFont(oldFont);

        if(paintAsMirrorImage) {
            g2.translate(bounds.getWidth()+2*bounds.getX(),0);
            g2.scale(-1,1);
        }
    }

    public double getWidth(Graphics2D g2, Node item) {
        FontMetrics fm = getCurrentFontMetricsForGraphicsAndNode(g2, item);
        String label = getLabel(item);
        return label == null ? 0 : fm.getStringBounds(label, g2).getWidth();
    }

    public double getPreferredHeight(Graphics2D g2, Node item) {
        FontMetrics fm = getCurrentFontMetricsForGraphicsAndNode(g2, item);
        return fm.getHeight();
    }

    public double getHeightBound(Graphics2D g2, Node item) {
        FontMetrics fm = getCurrentFontMetricsForGraphicsAndNode(g2, item);
        return fm.getHeight()+fm.getAscent();
    }

    private FontMetrics getCurrentFontMetricsForGraphicsAndNode(Graphics2D g2, Node item) {
        Font oldFont = g2.getFont();
        Object fontValue = item.getAttribute("labelFont");
        g2.setFont(TreeViewerUtilities.DEFAULT_FONT);
        if(fontValue != null) {
            try {
                g2.setFont(TreeViewerUtilities.fontFromString(fontValue.toString()));
            }
            catch(IllegalArgumentException ex){
                System.err.println("Invalid font string: "+fontValue);
            }
        }
        FontMetrics fm = g2.getFontMetrics();
        g2.setFont(oldFont);
        return fm;
    }

    public void setControlPalette(ControlPalette controlPalette) {}

    public List<Controls> getControls(boolean detachPrimaryCheckbox) {
        List<Controls> controlsList = new ArrayList<Controls>();

        if(controls == null) {
            OptionsPanel panel = new OptionsPanel();

            final JCheckBox visibleCheckBox = new JCheckBox("Show Tip Labels", visible);
            visibleCheckBox.addChangeListener(new ChangeListener(){
                public void stateChanged(ChangeEvent e) {
                    visible = visibleCheckBox.isSelected();
                    PREFS.putBoolean("Tip Labels_isopoen", visible);
                    firePainterChanged();
                }
            });

            final JComboBox attributeBox = new JComboBox(attributes.toArray(new String[attributes.size()]));
            String prefsValue = PREFS.get("Tip Labels_whatToDisplay", TAXON_NAMES);
            if(attribute.contains(prefsValue)) {
                attributeBox.setSelectedItem(prefsValue);
            }
            attributeBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent itemEvent) {
                    String attr = (String) attributeBox.getSelectedItem();
                    attribute = attr;
                    PREFS.put("Tip Labels_whatToDisplay", attribute);
                    firePainterChanged();
                }
            });


            panel.addComponentWithLabel("Display:", attributeBox);

            final JSpinner significantDigitSpinner = new JSpinner(new SpinnerNumberModel(PREFS.getInt("Tip Labels_sigDigits", formatter.getSignificantFigures()), 2, 14, 1));
            panel.addComponentWithLabel("Significant Digits:", significantDigitSpinner);
            significantDigitSpinner.addChangeListener(new ChangeListener(){
                public void stateChanged(ChangeEvent e) {
                    Integer value = (Integer) significantDigitSpinner.getValue();
                    formatter.setSignificantFigures(value);
                    PREFS.putInt("Tip Labels_sigDigits", value);
                    firePainterChanged();
                }
            });

            Icon infoIcon = IconUtils.getIcon(TreeViewer.class, "/jebl/gui/trees/treeviewer/images/info16.png");
            JLabel infoLabel = new JLabel("Set font sizes in the toolbar above", infoIcon, JLabel.CENTER);
            infoLabel.setBorder(new EmptyBorder(5,0,5,0));
            panel.addSpanningComponent(infoLabel);

            controls = new Controls("Tip Labels", panel, true, true, visibleCheckBox);
            
        }
        controlsList.add(controls);


        return controlsList;
    }

    public void setSettings(ControlsSettings settings) {}

    public void getSettings(ControlsSettings settings) {}
}
