package jebl.gui.trees.treeviewer_dev;

import jebl.evolution.trees.Tree;
import jebl.gui.trees.treeviewer_dev.decorators.*;
import jebl.util.Attributable;
import org.virion.jam.controlpalettes.AbstractController;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class TreeAppearanceController extends AbstractController {

	private static final String CONTROLLER_TITLE = "Appearance";

	private static Preferences PREFS = Preferences.userNodeForPackage(TreeAppearanceController.class);

	private static final String CONTROLLER_KEY = "appearance";

	private static final String FOREGROUND_COLOUR_KEY = "foregroundColour";
	private static final String BACKGROUND_COLOUR_KEY = "backgroundColour";
	private static final String SELECTION_COLOUR_KEY = "selectionColour";
	private static final String BRANCH_COLOR_ATTRIBUTE_KEY = "branchColorAttribute";
	private static final String BRANCH_LINE_WIDTH_KEY = "branchLineWidth";

	// The defaults if there is nothing in the preferences
	private static Color DEFAULT_FOREGROUND_COLOUR = Color.BLACK;
	private static Color DEFAULT_BACKGROUND_COLOUR = Color.WHITE;
	private static Color DEFAULT_SELECTION_COLOUR = new Color(180, 213, 254);
	private static float DEFAULT_BRANCH_LINE_WIDTH = 1.0f;

	public TreeAppearanceController(final TreePane treePane) {
		this.treePane = treePane;

		final AttributableDecorator branchDecorator = new AttributableDecorator();
		branchDecorator.setPaintAttributeName("!color");
		branchDecorator.setStrokeAttributeName("!stroke");
		treePane.setBranchDecorator(branchDecorator);

		int foregroundRGB = TreeAppearanceController.PREFS.getInt(CONTROLLER_KEY + "." + FOREGROUND_COLOUR_KEY, DEFAULT_FOREGROUND_COLOUR.getRGB());
		int backgroundRGB = TreeAppearanceController.PREFS.getInt(CONTROLLER_KEY + "." + BACKGROUND_COLOUR_KEY, DEFAULT_BACKGROUND_COLOUR.getRGB());
		int selectionRGB = TreeAppearanceController.PREFS.getInt(CONTROLLER_KEY + "." + SELECTION_COLOUR_KEY, DEFAULT_SELECTION_COLOUR.getRGB());
		float branchLineWidth = TreeAppearanceController.PREFS.getFloat(CONTROLLER_KEY + "." + BRANCH_LINE_WIDTH_KEY, DEFAULT_BRANCH_LINE_WIDTH);

		treePane.setForeground(new Color(foregroundRGB));
		treePane.setBackground(new Color(backgroundRGB));
		treePane.setSelectionPaint(new Color(selectionRGB));
		treePane.setBranchStroke(new BasicStroke(branchLineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

		titleLabel = new JLabel(CONTROLLER_TITLE);

		optionsPanel = new OptionsPanel();

		branchLineWidthSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.01, 48.0, 1.0));

		branchLineWidthSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				float lineWidth = ((Double) branchLineWidthSpinner.getValue()).floatValue();
				treePane.setBranchStroke(new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
			}
		});
		optionsPanel.addComponentWithLabel("Line Weight:", branchLineWidthSpinner);

		branchColorAttributeCombo = new JComboBox();
		setupAttributes(treePane.getTree());
		branchColorAttributeCombo.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				if (branchColorAttributeCombo.getSelectedIndex() == 0) {
					treePane.setBranchDecorator(branchDecorator);
				} else {
					Tree tree = treePane.getTree();
					String attribute = (String)branchColorAttributeCombo.getSelectedItem();
					Decorator decorator = new ContinuousColorDecorator(
							attribute, tree.getNodes(),
							new Color(192, 16, 0), new Color(0, 16, 192));

					treePane.setBranchDecorator(decorator);
				}
			}
		});

		optionsPanel.addComponentWithLabel("Color by:", branchColorAttributeCombo);

		treePane.addTreePaneListener(new TreePaneListener() {
			public void treePaneSettingsChanged() {
				setupAttributes(treePane.getTree());
				optionsPanel.repaint();
			}
		});
	}

	private void setupAttributes(Tree tree) {
		Object selected = branchColorAttributeCombo.getSelectedItem();

		branchColorAttributeCombo.removeAllItems();
		branchColorAttributeCombo.addItem("User Selection");
		if (tree == null) {
			return;
		}
		for (String name : getAttributeNames(tree.getNodes())) {
			branchColorAttributeCombo.addItem(name);
		}

		branchColorAttributeCombo.setSelectedItem(selected);
	}

	private String[] getAttributeNames(Collection<? extends Attributable> items) {
		java.util.Set<String> attributeNames = new TreeSet<String>();

		for (Attributable item : items) {
			for (String name : item.getAttributeNames()) {
				if (!name.startsWith("!")) {
					Object attr = item.getAttribute(name);
					if (!(attr instanceof Object[])) {
						attributeNames.add(name);
					}
				}
			}
		}

		String[] attributeNameArray = new String[attributeNames.size()];
		attributeNames.toArray(attributeNameArray);

		return attributeNameArray;
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

		branchColorAttributeCombo.setSelectedItem(settings.get(CONTROLLER_KEY+"."+BRANCH_COLOR_ATTRIBUTE_KEY));
		branchLineWidthSpinner.setValue((Double)settings.get(CONTROLLER_KEY + "." + BRANCH_LINE_WIDTH_KEY));
	}

	public void getSettings(Map<String, Object> settings) {
		// These settings don't have controls yet but they will!
		settings.put(CONTROLLER_KEY + "." + FOREGROUND_COLOUR_KEY, treePane.getForeground());
		settings.put(CONTROLLER_KEY + "." + BACKGROUND_COLOUR_KEY, treePane.getBackground());
		settings.put(CONTROLLER_KEY + "." + SELECTION_COLOUR_KEY, treePane.getSelectionPaint());

		settings.put(CONTROLLER_KEY + "." + BRANCH_COLOR_ATTRIBUTE_KEY, branchColorAttributeCombo.getSelectedItem().toString());
		settings.put(CONTROLLER_KEY + "." + BRANCH_LINE_WIDTH_KEY, branchLineWidthSpinner.getValue());
	}


	private final JLabel titleLabel;
	private final OptionsPanel optionsPanel;

	private final JComboBox branchColorAttributeCombo;
	private final JSpinner branchLineWidthSpinner;

	private final TreePane treePane;
}
