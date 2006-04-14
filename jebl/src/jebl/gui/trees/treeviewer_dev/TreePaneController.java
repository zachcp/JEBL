package jebl.gui.trees.treeviewer_dev;

import jebl.evolution.trees.SortedRootedTree;
import jebl.evolution.trees.TransformedRootedTree;
import org.virion.jam.controlpalettes.Controller;
import org.virion.jam.controlpalettes.ControllerSettings;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class TreePaneController implements Controller {

	public TreePaneController(final TreePane treePane) {
		this.treePane = treePane;

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

	public void setSettings(ControllerSettings settings) {
		transformCheck.setSelected((Boolean) settings.getSetting("Transformed"));
	}

	public void getSettings(ControllerSettings settings) {
		settings.putSetting("Transformed", transformCheck.isSelected());
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
