package jebl.gui.trees.treeviewer.painters;

import jebl.evolution.graphs.Node;
import jebl.evolution.trees.RootedTree;
import jebl.gui.trees.treeviewer.TreePane;
import org.virion.jam.controlpanels.Controls;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * Draws labels for nodes whose subtree have been collapsed. Also provides controls for auto-collapsing subtrees
 *
 * @author Sebastian Dunn
 *         Created on 10/08/15 3:05 PM
 */
public class CollapsedNodeLabelPainter extends BasicLabelPainter {
    private Controls controls;
    private double collapsedDistanceThreshold;
    private final boolean isCollapsedDefault;
    private boolean isCollapsed;
    private static String KEY_COLLAPSE_THRESHOLD = "collapsedThreshold";
    private static String KEY_IS_COLLAPSED = "autoCollapseNodes";
    private int collapseSliderMax;
    private boolean resetCollapseState = false;
    private int numberManualNodes;
    private JButton resetButton;
    private Map<Node, Integer> externalNodesUnderNode = new HashMap<Node, Integer>();
    private JButton helpButton;
    private OptionsPanel optionsPanel;
    private JSlider collapseSlider;

    public CollapsedNodeLabelPainter(RootedTree tree) {
        super("Collapsed Node Labels", tree, PainterIntent.COLLAPSED, 12);
        collapsedDistanceThreshold = getPrefs().getDouble(KEY_COLLAPSE_THRESHOLD, 0.0);
        isCollapsedDefault = getPrefs().getBoolean(KEY_IS_COLLAPSED, (tree.getNodes().size() > 1000));
        isCollapsed = isCollapsedDefault;
        Object distanceObject = tree.getRootNode().getAttribute(TreePane.KEY_MAX_DISTANCE_TO_ANCESTOR);
        double distance;
        if (distanceObject != null && distanceObject instanceof Double) {
            distance = (Double) distanceObject;
        } else {
            distance = 1.0;
        }
        collapseSliderMax = (int) (distance * 100) + 1; //+1 to make sure the last position on the slider will collapse to the root node
        numberManualNodes = 0;
    }

    @Override
    public String getLabel(Node node) {
        Node first = node;
        while( ! tree.isExternal(first) ) {
            final List<Node> children = tree.getChildren(first);
            first = children.get(0);
        }
        Object distanceObject = node.getAttribute(TreePane.KEY_MAX_DISTANCE_TO_ANCESTOR);
        Double distance;
        if (distanceObject != null && distanceObject instanceof Double) {
            distance = (Double) distanceObject;
        } else {
            throw new IllegalStateException("Collapsed Node encountered with no distance to lowest ancestor");
        }
        return tree.getTaxon(first).getName() + " and " + countExternalNodesUnderNode(node) + " others (" + String.format("%.3f", distance) + ")";
    }

    private int countExternalNodesUnderNode(Node node) {
        if (tree.isExternal(node)) {
            return 0;
        }
        Integer cachedValue = externalNodesUnderNode.get(node);
        if (cachedValue != null) return cachedValue;

        Stack<Node> stack = new Stack<Node>();
        int count  = 0;
        stack.push(node);
        while (!stack.empty()) {
            Node currentNode = stack.pop();
            if (tree.isExternal(currentNode)) {
                count++;
            } else {
                for (Node child : tree.getChildren(currentNode)) {
                    stack.push(child);
                }
            }
        }
        externalNodesUnderNode.put(node, count);
        return count;
    }

    private static Preferences getPrefs() {
        return Preferences.userNodeForPackage(CollapsedNodeLabelPainter.class);
    }

    @Override
    /**
     * This controls method only sets up the default checkbox and leaves a reference
     * to the options panel for later. When setHelpButton() is called the rest of the
     * components are made and added.
     */
    public List<Controls> getControls(boolean detachPrimaryCheckbox) {
        List<Controls> controlsList = new ArrayList<Controls>();

        if (controls == null) {
            optionsPanel = new OptionsPanel();

            final JCheckBox collapseCheckbox = new JCheckBox(helpTitle);
            collapseCheckbox.setSelected(isCollapsedDefault);
            collapseCheckbox.setToolTipText("Automatically collapse subtrees when there is not enough space on-screen");

            collapseCheckbox.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    if (collapseCheckbox.isSelected() != isCollapsed) { //If the state has changed. Good to check because this triggers repaint
                        setCollapseSubtrees(collapseCheckbox.isSelected());
                        getPrefs().putBoolean(KEY_IS_COLLAPSED, collapseCheckbox.isSelected());
                    }
                }
            });

            controls = new Controls("Subtree Collapse", optionsPanel, true, false, collapseCheckbox);
        }

        controlsList.add(controls);

        return controlsList;
    }

    private void resetNodeCollapseStates() {
        resetCollapseState = true;
        firePainterChanged();
    }

    public boolean isResetCollapseState() {
        if (resetCollapseState) {
            resetCollapseState = false;
            return true;
        }
        return false;
    }

    private void setCollapseSubtrees(boolean selected) {
        isCollapsed = selected;
        firePainterChanged();
    }

    private void setCollapsedDistanceThreshold(double value) {
        collapsedDistanceThreshold = value;
        firePainterChanged();
    }

    public double getCollapsedDistanceThreshold() {
        return collapsedDistanceThreshold;
    }

    public boolean isCollapsed() {
        return isCollapsed;
    }

    public void setNumberManualNodes(int numberManualNodes) {
        this.numberManualNodes = numberManualNodes;
        if (resetButton != null) {
            resetButton.setText("Reset state of " + numberManualNodes + " nodes");
        }
    }

    public void setHelpButton(JButton help) {
        helpButton = help;
        finishSettingUpControlPanel();
    }

    /**
     * Controls are created and given to the treePane above in getControls(), but
     * can't be fully populated until we have the help button. To fit with the theme,
     * the help button has to be created in TreeDocumentViewer with access to the PublicAPI,
     * then passed down to here to finish the panel.
     */
    private void finishSettingUpControlPanel() {
        createCollapseSlider();
        createResetButton();

        Box sliderBox = Box.createHorizontalBox();
        sliderBox.add(collapseSlider);
        sliderBox.add(Box.createHorizontalStrut(5));
        sliderBox.add(helpButton);

        optionsPanel.addComponentWithLabel("Subtree Distance:", sliderBox, true);
        optionsPanel.addComponent(resetButton, false);
    }

    private void createCollapseSlider() {
        collapseSlider = new JSlider(SwingConstants.HORIZONTAL, 0, collapseSliderMax, 0);
        collapseSlider.setValue((int) (collapsedDistanceThreshold * 100));
        collapseSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
//                    JSlider source = (JSlider) changeEvent.getSource();
//                    if (source.getValueIsAdjusting()) return; //Don't change the tree while the JSlider is still adjusting
                double value = collapseSlider.getValue() / (double) 100;
                if (Math.abs(value - collapsedDistanceThreshold) > 0.0001) setCollapsedDistanceThreshold(value);
                getPrefs().putDouble(KEY_COLLAPSE_THRESHOLD, value);
            }
        });
    }

    private void createResetButton() {
        resetButton = new JButton("Reset state of 0 nodes");
        resetButton.setToolTipText("Reset the state of nodes that have been manually expanded or contracted");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetNodeCollapseStates();
            }
        });
    }

    public static String helpTitle = "Automatically Collapse Subtrees";
    public static String helpText = "When <b>Automatically Collapse Subtrees</b> is enabled, groups of similar nodes will be collapsed into a single node that represents that subtree. "
            + "The maximum distance within the subtrees is determined by the <b>Subtree Distance</b> slider. Use this option to help navigate trees with many nodes and tips.\n\n"
            + "Double-clicking a node in a tree will force it to expand or contract. <b>Automatically Collapse Subtrees</b> will not override this state. "
            + "To reset the state of double-clicked nodes in the tree, click <b>Reset state of X nodes</b>. <b>X</b> is the number of nodes with a manually expanded or collapsed state.";
}
