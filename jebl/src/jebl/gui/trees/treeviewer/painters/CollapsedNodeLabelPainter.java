package jebl.gui.trees.treeviewer.painters;

import jebl.evolution.graphs.Node;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.Utils;
import jebl.gui.trees.treeviewer.TreePane;
import jebl.util.NumberFormatter;
import org.virion.jam.controlpanels.Controls;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
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
    private boolean areLabelsVisible;
    private static final String KEY_SHOW_COLLAPSE_LABELS = "showCollapsedLabels";
    private static String KEY_COLLAPSE_THRESHOLD = "collapsedThreshold";
    private static String KEY_IS_COLLAPSED = "autoCollapseNodes";
    private double maxDistanceForCurrentTree;
    private boolean resetCollapseState = false;
    private BasicLabelPainter taxonLabelPainterHook;
    private Map<Node, Integer> externalNodesUnderNode = new HashMap<Node, Integer>();

    private OptionsPanel optionsPanel;
    private JButton resetButton;
    private JSlider collapseSlider;
    private JLabel sliderLabel;
    private JCheckBox showLabelsCheckBox;

    /**
     * Constructor takes the instance of TaxonLabelPainter so we can use its getLabel method to get
     * collapsed node labels with the same attributes as the taxon labels.
     */
    public CollapsedNodeLabelPainter(RootedTree tree, BasicLabelPainter taxonLabelPainterHook) {
        super("Collapsed Node Labels", tree, PainterIntent.COLLAPSED, 12);
        maxDistanceForCurrentTree = getMaxDistanceForTree(tree);
        collapsedDistanceThreshold = getPrefs().getDouble(KEY_COLLAPSE_THRESHOLD, 0.0) * maxDistanceForCurrentTree / 100;
        isCollapsedDefault = getPrefs().getBoolean(KEY_IS_COLLAPSED, (tree.getNodes().size() > 1000));
        isCollapsed = isCollapsedDefault;
        areLabelsVisible = getPrefs().getBoolean(KEY_SHOW_COLLAPSE_LABELS, true);
        this.taxonLabelPainterHook = taxonLabelPainterHook;
    }

    public CollapsedNodeLabelPainter(RootedTree tree) {
        this(tree, null);
    }

    private static final String ELIPSES = "...";
    private static final int MAX_CHARS_TAXON = 20;

    @Override
    public String getLabel(Node node) {
        Node first = node;
        while( ! tree.isExternal(first) ) {
            final List<Node> children = tree.getChildren(first);
            first = children.get(0);
        }
        Object distanceObject = node.getAttribute(TreePane.KEY_MAX_DISTANCE_TO_DESCENDANT);
        Double distance;
        if (distanceObject != null && distanceObject instanceof Double) {
            distance = (Double) distanceObject;
        } else {
            throw new IllegalStateException("Collapsed Node encountered with no distance to lowest ancestor");
        }

        final int extraNodes = Utils.getExternalNodeCount(tree, node, externalNodesUnderNode) - 1;

        String taxon = getTaxonLabel(first);
        return taxon + " and " + extraNodes + " other" + (extraNodes == 1 ? "" : "s") + " (" + String.format("%.3f", distance) + ")";
    }

    private String getTaxonLabel(Node first) {
        return (taxonLabelPainterHook != null) ? limitString(taxonLabelPainterHook.getLabel(first))
                : limitString(tree.getTaxon(first).getName());
    }

    private String limitString(String name) {
        return (name.length() > MAX_CHARS_TAXON) ? name.substring(0, MAX_CHARS_TAXON-ELIPSES.length()) + ELIPSES : name;
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
                        recursiveEnable(optionsPanel, collapseCheckbox.isSelected());
                        setCollapseSubtrees(collapseCheckbox.isSelected());
                        getPrefs().putBoolean(KEY_IS_COLLAPSED, collapseCheckbox.isSelected());
                    }
                }
            });

            // Set the preferred size of the label so it doesn't change.  The label will be one of:
            // N.NNE(+/-)M
            // 0.NNNN
            // N.NN
            // The largest being the scientific notation.  So that's the one we size the label for.
            // We add an extra space just to be safe :)
            char[] forSizing = new char[formatter.getSignificantFigures()];
            Arrays.fill(forSizing, '0');
            sliderLabel = new JLabel(" .E-4" + new String(forSizing));
            sliderLabel.setPreferredSize(sliderLabel.getPreferredSize());
            createCollapseSlider();
            createShowLabelsCheckbox();
            createCheckBoxWarningLabel(true);
            createResetButton();

            Box sliderBox = Box.createHorizontalBox();
            sliderBox.add(collapseSlider);
            sliderBox.add(sliderLabel);
            sliderBox.add(Box.createHorizontalStrut(5));
            sliderBox.add(HelpButtonGetter.get(helpTitle, helpText));

            Box showLabelsBox = Box.createHorizontalBox();
            showLabelsBox.add(showLabelsCheckBox);
            showLabelsBox.add(checkBoxWarningLabel);

            optionsPanel.addComponentWithLabel("Subtree Distance:", sliderBox, true);
            optionsPanel.addComponent(showLabelsBox, true);
            optionsPanel.addComponent(resetButton, false);

            controls = new Controls("Subtree Collapse", optionsPanel, true, false, collapseCheckbox);
        }

        controlsList.add(controls);

        return controlsList;
    }

    private static void recursiveEnable(JComponent component, boolean enable) {
        component.setEnabled(enable);
        for (Component child : component.getComponents()) {
            if(child instanceof JComponent) {
                recursiveEnable((JComponent)child, enable);
            }
        }
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
        if (resetButton != null) {
            resetButton.setText("Reset state of " + numberManualNodes + " nodes");
        }
    }

    /**
     * Notifies the {@link CollapsedNodeLabelPainter} that the tree it is painting has changed.  This will update any
     * dependent controls previously obtained from calling {@link #getControls(boolean)}
     * @param tree
     */
    public void treeChanged(RootedTree tree) {
        this.tree = tree;
        // If the tree could have changed then the max dist under the root could have too.  Re-calculate and update control.
        maxDistanceForCurrentTree = getMaxDistanceForTree(this.tree);
        updateSliderFromCollapseDistanceValue();
    }

    private static double getMaxDistanceForTree(RootedTree tree) {
        //Get the newly set distance from the root to a tip and make it the max on the control panel slider
        Object distanceObject = tree.getRootNode().getAttribute(TreePane.KEY_MAX_DISTANCE_TO_DESCENDANT);
        double distance = 1.0;
        if (distanceObject != null && distanceObject instanceof Double) {
            distance = (Double) distanceObject;
        }
        return distance;
    }

    public void calibrate(Graphics2D g2) {
        final Font oldFont = g2.getFont();
        g2.setFont(taxonLabelFont);

        final FontMetrics fm = g2.getFontMetrics();
        preferredHeight = fm.getHeight()-5;

        yOffset = (float)fm.getAscent();
        yOffset -= yOffset/4;

        g2.setFont(oldFont);
    }


    public boolean areLabelsVisible() {
        return areLabelsVisible;
    }

    @Override
    public boolean isVisible() {
        return areLabelsVisible;
    }

    private void createShowLabelsCheckbox() {
        showLabelsCheckBox = new JCheckBox("Show Collapsed Node Labels", areLabelsVisible);
        showLabelsCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (areLabelsVisible != showLabelsCheckBox.isSelected()) {
                    areLabelsVisible = showLabelsCheckBox.isSelected();
                    getPrefs().putBoolean(KEY_SHOW_COLLAPSE_LABELS, areLabelsVisible);
                    firePainterChanged();
                }
            }
        });
    }

    private static final int NUM_SLIDER_INCREMENTS = 100;

    private void createCollapseSlider() {
        collapseSlider = new JSlider(SwingConstants.HORIZONTAL, 0, getSliderMax(), 0);
        updateSliderFromCollapseDistanceValue();
        collapseSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                double value = getCollapseDistance();
                setCollapsedDistanceThreshold(value);
                sliderLabel.setText(getCollapseSliderLabelText());
                getPrefs().putDouble(KEY_COLLAPSE_THRESHOLD, collapseSlider.getValue());
            }
        });
    }

    /**
     * Updates the collapse slider component and label based on the collapse distance that has been set on the painter.
     * i.e. Updates the view based on the model.
     */
    private void updateSliderFromCollapseDistanceValue() {
        collapseSlider.setValue((int) (collapsedDistanceThreshold / maxDistanceForCurrentTree * NUM_SLIDER_INCREMENTS));
        sliderLabel.setText(getCollapseSliderLabelText());
    }

    private NumberFormatter formatter = new NumberFormatter(3);
    private String getCollapseSliderLabelText() {
        int sliderValue = collapseSlider.getValue();
        if(sliderValue == 0) {
            return "0";
        } else if(sliderValue == getSliderMax()) {
            return "Max";
        } else {
            return formatter.getFormattedValue(getCollapsedDistanceThreshold());
        }
    }

    private int getSliderMax() {
        // Number of increments +1 to make sure the last position on the slider will collapse to the root node
        // 0 will indicate no nodes collapsed
        // Max dist + 1 will indicate fully collapsed
        return NUM_SLIDER_INCREMENTS+1;
    }

    private double getCollapseDistance() {
        return collapseSlider.getValue() * maxDistanceForCurrentTree/NUM_SLIDER_INCREMENTS;
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
            + "Collapsed nodes are labeled with the name of one of the tips, a count of how many tips the subtree contains, and the maximum distance between the top of the subtree and any of the tips within it."
            + "Double-clicking a node in a tree will force it to expand or contract. <b>Automatically Collapse Subtrees</b> will not override this state. "
            + "To reset the state of double-clicked nodes in the tree, click <b>Reset state of X nodes</b>. <b>X</b> is the number of nodes with a manually expanded or collapsed state.";


    static abstract class HelpButtonGetter {
        static abstract class Factory {
            protected abstract JButton get(String title, String message);
        }

        static void setImplementation(HelpButtonGetter.Factory impl) {
            implementation = impl;
        }

        static JButton get(String title, String message) {
            return implementation.get(title, message);
        }

        private static HelpButtonGetter.Factory implementation = new Factory() {
            @Override
            protected JButton get(final String title, final String message) {
                JButton button = new JButton("?");
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JEditorPane textPane = new JEditorPane("text/html", message);
                        textPane.setOpaque(false);
                        textPane.setEditable(false);

                        JScrollPane scrollPane = new JScrollPane(textPane);
                        scrollPane.setPreferredSize(new Dimension(400, 300));
                        JOptionPane.showMessageDialog(
                                null, scrollPane, title, JOptionPane.INFORMATION_MESSAGE);
                    }
                });
                return button;
            }
        };
    }
}
