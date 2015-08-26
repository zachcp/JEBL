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
    public List<Controls> getControls(boolean detachPrimaryCheckbox) {
        List<Controls> controlsList = new ArrayList<Controls>();

        if (controls == null) {
            OptionsPanel optionsPanel = new OptionsPanel();

            final JSlider collapseSlider = new JSlider(SwingConstants.HORIZONTAL, 0, collapseSliderMax, 0);
            collapseSlider.setValue((int) (collapsedDistanceThreshold * 100));


            collapseSlider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
//                    JSlider source = (JSlider) changeEvent.getSource();
//                    if (source.getValueIsAdjusting()) return; //Don't change the tree while the JSlider is still adjusting
                    double value = collapseSlider.getValue() / (double) 100;
                    if (Math.abs(value - collapsedDistanceThreshold) > 0.0001) setCollapsedDistanceThreshold(value);
                    getPrefs().putInt(KEY_COLLAPSE_THRESHOLD, collapseSlider.getValue());
                }
            });

            optionsPanel.addComponentWithLabel("Subtree Distance:", collapseSlider, true);

            final JCheckBox collapseCheckbox = new JCheckBox("Automatically Contract Subtrees");
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

            resetButton = new JButton("Reset state of 0 nodes");
            resetButton.setToolTipText("Reset the state of nodes that have been manually expanded or contracted");

            resetButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    resetNodeCollapseStates();
                }
            });

            optionsPanel.addComponent(resetButton);

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
}
