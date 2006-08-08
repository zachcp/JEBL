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

    private static Preferences PREFS = Preferences.userNodeForPackage(TreePaneController.class);

    private static final String FOREGROUND_COLOUR_KEY = "foregroundColour";
    private static final String BACKGROUND_COLOUR_KEY = "backgroundColour";
    private static final String SELECTION_COLOUR_KEY = "selectionColour";
    private static final String BRANCH_LINE_WIDTH_KEY = "branchLineWidth";

    private static final String TRANSFORM_KEY = "transform";
    private static final String TRANSFORM_TYPE_KEY = "transformType";
    private static final String ORDER_KEY = "order";
    private static final String ORDER_TYPE_KEY = "orderType";

    private static final String SHOW_ROOT_KEY = "showRoot";

    // The defaults if there is nothing in the preferences
    private static Color DEFAULT_FOREGROUND_COLOUR = Color.BLACK;
    private static Color DEFAULT_BACKGROUND_COLOUR = Color.WHITE;
    private static Color DEFAULT_SELECTION_COLOUR = new Color(180, 213, 254);
    private static float DEFAULT_BRANCH_LINE_WIDTH = 1.0f;

    public TreePaneController(final TreePane treePane) {
        this.treePane = treePane;

        int foregroundRGB = PREFS.getInt(FOREGROUND_COLOUR_KEY, DEFAULT_FOREGROUND_COLOUR.getRGB());
        int backgroundRGB = PREFS.getInt(BACKGROUND_COLOUR_KEY, DEFAULT_BACKGROUND_COLOUR.getRGB());
        int selectionRGB = PREFS.getInt(SELECTION_COLOUR_KEY, DEFAULT_SELECTION_COLOUR.getRGB());
        float branchLineWidth = PREFS.getFloat(BRANCH_LINE_WIDTH_KEY, DEFAULT_BRANCH_LINE_WIDTH);

        treePane.setForeground(new Color(foregroundRGB));
        treePane.setBackground(new Color(backgroundRGB));
        treePane.setSelectionPaint(new Color(selectionRGB));
        treePane.setBranchStroke(new BasicStroke(branchLineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        titleLabel = new JLabel("Tree Formatting");

        optionsPanel = new OptionsPanel();

        transformCheck = new JCheckBox("Transform branches");
        optionsPanel.addComponent(transformCheck);

        transformCheck.setSelected(treePane.isTransformBranchesOn());

        transformCombo = new JComboBox(TransformedRootedTree.Transform.values());
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
        optionsPanel.addComponent(orderCheck);

        orderCheck.setSelected(treePane.isOrderBranchesOn());

        orderCombo = new JComboBox(SortedRootedTree.BranchOrdering.values());
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

        showRootCheck = new JCheckBox("Show Root Branch");
        optionsPanel.addComponent(showRootCheck);

        showRootCheck.setSelected(treePane.isShowingRootBranch());
        showRootCheck.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                treePane.setShowingRootBranch(showRootCheck.isSelected());
            }
        });

        branchLineWidthSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.01, 48.0, 1.0));

        branchLineWidthSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                float lineWidth = ((Double) branchLineWidthSpinner.getValue()).floatValue();
                treePane.setBranchStroke(new BasicStroke(lineWidth));
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
        treePane.setForeground((Color)settings.get(FOREGROUND_COLOUR_KEY));
        treePane.setBackground((Color)settings.get(BACKGROUND_COLOUR_KEY));
        treePane.setSelectionPaint((Color)settings.get(SELECTION_COLOUR_KEY));

        transformCheck.setSelected((Boolean) settings.get(TRANSFORM_KEY));
        transformCombo.setSelectedItem(TransformedRootedTree.Transform.valueOf((String)settings.get(TRANSFORM_TYPE_KEY)));
        orderCheck.setSelected((Boolean) settings.get(ORDER_KEY));
        orderCombo.setSelectedItem(SortedRootedTree.BranchOrdering.valueOf((String)settings.get(ORDER_TYPE_KEY)));

        showRootCheck.setSelected((Boolean) settings.get(SHOW_ROOT_KEY));

        branchLineWidthSpinner.setValue((Integer)settings.get(BRANCH_LINE_WIDTH_KEY));
    }

    public void getSettings(Map<String, Object> settings) {
        // These settings don't have controls yet but they will!
        settings.put(FOREGROUND_COLOUR_KEY, treePane.getForeground());
        settings.put(BACKGROUND_COLOUR_KEY, treePane.getBackground());
        settings.put(SELECTION_COLOUR_KEY, treePane.getSelectionPaint());

        settings.put(TRANSFORM_KEY, transformCheck.isSelected());
        settings.put(TRANSFORM_TYPE_KEY, transformCombo.getSelectedItem().toString());
        settings.put(ORDER_KEY, orderCheck.isSelected());
        settings.put(ORDER_TYPE_KEY, orderCombo.getSelectedItem().toString());

        settings.put(SHOW_ROOT_KEY, showRootCheck.isSelected());

        settings.put(BRANCH_LINE_WIDTH_KEY, branchLineWidthSpinner.getValue());
    }


    private final JLabel titleLabel;
    private final OptionsPanel optionsPanel;

    private final JCheckBox transformCheck;
    private final JComboBox transformCombo;

    private final JCheckBox orderCheck;
    private final JComboBox orderCombo;

    private final JCheckBox showRootCheck;

    private final JSpinner branchLineWidthSpinner;

    private final TreePane treePane;
}
