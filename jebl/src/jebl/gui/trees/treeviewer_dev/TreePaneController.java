package jebl.gui.trees.treeviewer_dev;

import jebl.evolution.trees.SortedRootedTree;
import jebl.evolution.trees.TransformedRootedTree;
import org.virion.jam.controlpalettes.AbstractController;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class TreePaneController extends AbstractController {

	private static final String CONTROLLER_TITLE = "Tree";

    private static Preferences PREFS = Preferences.userNodeForPackage(TreePaneController.class);

	private static final String CONTROLLER_KEY = "tree";

    private static final String FOREGROUND_COLOUR_KEY = "foregroundColour";
    private static final String BACKGROUND_COLOUR_KEY = "backgroundColour";
    private static final String SELECTION_COLOUR_KEY = "selectionColour";
    private static final String BRANCH_LINE_WIDTH_KEY = "branchLineWidth";

    private static final String TRANSFORM_KEY = "transform";
    private static final String TRANSFORM_TYPE_KEY = "transformType";
    private static final String ORDER_KEY = "order";
    private static final String ORDER_TYPE_KEY = "orderType";

    // The defaults if there is nothing in the preferences
    private static Color DEFAULT_FOREGROUND_COLOUR = Color.BLACK;
    private static Color DEFAULT_BACKGROUND_COLOUR = Color.WHITE;
    private static Color DEFAULT_SELECTION_COLOUR = new Color(180, 213, 254);
    private static float DEFAULT_BRANCH_LINE_WIDTH = 1.0f;

    public TreePaneController(final TreePane treePane) {
        this.treePane = treePane;

        int foregroundRGB = PREFS.getInt(CONTROLLER_KEY + "." + FOREGROUND_COLOUR_KEY, DEFAULT_FOREGROUND_COLOUR.getRGB());
        int backgroundRGB = PREFS.getInt(CONTROLLER_KEY + "." + BACKGROUND_COLOUR_KEY, DEFAULT_BACKGROUND_COLOUR.getRGB());
        int selectionRGB = PREFS.getInt(CONTROLLER_KEY + "." + SELECTION_COLOUR_KEY, DEFAULT_SELECTION_COLOUR.getRGB());
        float branchLineWidth = PREFS.getFloat(CONTROLLER_KEY + "." + BRANCH_LINE_WIDTH_KEY, DEFAULT_BRANCH_LINE_WIDTH);

        treePane.setForeground(new Color(foregroundRGB));
        treePane.setBackground(new Color(backgroundRGB));
        treePane.setSelectionPaint(new Color(selectionRGB));
        treePane.setBranchStroke(new BasicStroke(branchLineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        titleLabel = new JLabel(CONTROLLER_TITLE);

        optionsPanel = new OptionsPanel();

        transformCheck = new JCheckBox("Transform branches");
        transformCheck.setOpaque(false);
        optionsPanel.addComponent(transformCheck);

        transformCheck.setSelected(treePane.isTransformBranchesOn());

        transformCombo = new JComboBox(TransformedRootedTree.Transform.values());
        transformCombo.setOpaque(false);
        transformCombo.setSelectedItem(treePane.getBranchTransform());
        transformCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                treePane.setBranchTransform(
                        (TransformedRootedTree.Transform) transformCombo.getSelectedItem());

            }
        });
        final JLabel label1 = optionsPanel.addComponentWithLabel("Transform:", transformCombo);
        label1.setEnabled(transformCheck.isSelected());
        transformCombo.setEnabled(transformCheck.isSelected());

        transformCheck.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                final boolean selected = transformCheck.isSelected();
                label1.setEnabled(selected);
                transformCombo.setEnabled(selected);

                treePane.setTransformBranchesOn(selected);
            }
        });

        orderCheck = new JCheckBox("Order branches");
        orderCheck.setOpaque(false);
        optionsPanel.addComponent(orderCheck);

        orderCheck.setSelected(treePane.isOrderBranchesOn());

        orderCombo = new JComboBox(SortedRootedTree.BranchOrdering.values());
        orderCombo.setOpaque(false);
        orderCombo.setSelectedItem(treePane.getBranchOrdering());
        orderCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                treePane.setBranchOrdering(
                        (SortedRootedTree.BranchOrdering) orderCombo.getSelectedItem());
            }
        });

        final JLabel label2 = optionsPanel.addComponentWithLabel("Ordering:", orderCombo);
        label2.setEnabled(orderCheck.isSelected());
        orderCombo.setEnabled(orderCheck.isSelected());

        orderCheck.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                label2.setEnabled(orderCheck.isSelected());
                orderCombo.setEnabled(orderCheck.isSelected());

                treePane.setOrderBranchesOn(orderCheck.isSelected());
            }
        });

        branchLineWidthSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.01, 48.0, 1.0));

        branchLineWidthSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                float lineWidth = ((Double) branchLineWidthSpinner.getValue()).floatValue();
                treePane.setBranchStroke(new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
            }
        });
        optionsPanel.addComponentWithLabel("Line Weight:", branchLineWidthSpinner);

    }

    public JComponent getTitleComponent() {
        return titleLabel;
    }

    public JPanel getPanel() {
        return optionsPanel;
    }

    public boolean isInitiallyVisible() {
        return false;
    }

    public void setSettings(Map<String,Object> settings) {
        // These settings don't have controls yet but they will!
        treePane.setForeground((Color)settings.get(CONTROLLER_KEY + "." + FOREGROUND_COLOUR_KEY));
        treePane.setBackground((Color)settings.get(CONTROLLER_KEY + "." + BACKGROUND_COLOUR_KEY));
        treePane.setSelectionPaint((Color)settings.get(CONTROLLER_KEY + "." + SELECTION_COLOUR_KEY));

        transformCheck.setSelected((Boolean) settings.get(CONTROLLER_KEY + "." + TRANSFORM_KEY));
        String transformName = (String)settings.get(CONTROLLER_KEY + "." + TRANSFORM_TYPE_KEY);
        for (TransformedRootedTree.Transform transform : TransformedRootedTree.Transform.values()) {
            if (transform.toString().equalsIgnoreCase(transformName)) {
                transformCombo.setSelectedItem(transform);
            }
        }

        orderCheck.setSelected((Boolean) settings.get(CONTROLLER_KEY + "." + ORDER_KEY));
        String orderName = (String)settings.get(CONTROLLER_KEY + "." + ORDER_TYPE_KEY);
        for (SortedRootedTree.BranchOrdering order : SortedRootedTree.BranchOrdering.values()) {
            if (order.toString().equalsIgnoreCase(orderName)) {
                orderCombo.setSelectedItem(order);
            }
        }

        branchLineWidthSpinner.setValue((Double)settings.get(CONTROLLER_KEY + "." + BRANCH_LINE_WIDTH_KEY));
    }

    public void getSettings(Map<String, Object> settings) {
        // These settings don't have controls yet but they will!
        settings.put(CONTROLLER_KEY + "." + FOREGROUND_COLOUR_KEY, treePane.getForeground());
        settings.put(CONTROLLER_KEY + "." + BACKGROUND_COLOUR_KEY, treePane.getBackground());
        settings.put(CONTROLLER_KEY + "." + SELECTION_COLOUR_KEY, treePane.getSelectionPaint());

        settings.put(CONTROLLER_KEY + "." + TRANSFORM_KEY, transformCheck.isSelected());
        settings.put(CONTROLLER_KEY + "." + TRANSFORM_TYPE_KEY, transformCombo.getSelectedItem().toString());
        settings.put(CONTROLLER_KEY + "." + ORDER_KEY, orderCheck.isSelected());
        settings.put(CONTROLLER_KEY + "." + ORDER_TYPE_KEY, orderCombo.getSelectedItem().toString());

        settings.put(CONTROLLER_KEY + "." + BRANCH_LINE_WIDTH_KEY, branchLineWidthSpinner.getValue());
    }


    private final JLabel titleLabel;
    private final OptionsPanel optionsPanel;

    private final JCheckBox transformCheck;
    private final JComboBox transformCombo;

    private final JCheckBox orderCheck;
    private final JComboBox orderCombo;

    private final JSpinner branchLineWidthSpinner;

    private final TreePane treePane;
}
