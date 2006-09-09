package jebl.gui.trees.treeviewer_dev;

import jebl.evolution.trees.SortedRootedTree;
import jebl.evolution.trees.TransformedRootedTree;
import org.virion.jam.controlpalettes.AbstractController;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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

    private static final String TRANSFORM_KEY = "transform";
    private static final String TRANSFORM_TYPE_KEY = "transformType";
    private static final String ORDER_KEY = "order";
    private static final String ORDER_TYPE_KEY = "orderType";


    public TreePaneController(final TreePane treePane) {
        this.treePane = treePane;

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
    }

    public void getSettings(Map<String, Object> settings) {
        settings.put(CONTROLLER_KEY + "." + TRANSFORM_KEY, transformCheck.isSelected());
        settings.put(CONTROLLER_KEY + "." + TRANSFORM_TYPE_KEY, transformCombo.getSelectedItem().toString());
        settings.put(CONTROLLER_KEY + "." + ORDER_KEY, orderCheck.isSelected());
        settings.put(CONTROLLER_KEY + "." + ORDER_TYPE_KEY, orderCombo.getSelectedItem().toString());
    }


    private final JLabel titleLabel;
    private final OptionsPanel optionsPanel;

    private final JCheckBox transformCheck;
    private final JComboBox transformCombo;

    private final JCheckBox orderCheck;
    private final JComboBox orderCombo;

    private final TreePane treePane;
}
