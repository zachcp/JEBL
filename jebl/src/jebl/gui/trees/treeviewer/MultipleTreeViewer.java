package jebl.gui.trees.treeviewer;

import jebl.evolution.io.NexusExporter;
import jebl.evolution.trees.Tree;
import org.virion.jam.controlpanels.*;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

/**
 * @author Andrew Rambaut
 */
public class MultipleTreeViewer extends TreeViewer {

    private void init() {
        getControlPalette().addControlsProvider(multipleTreeControlsProvider, true);
    }

    public MultipleTreeViewer() {
        super();
        init();
    }

    public MultipleTreeViewer(int CONTROL_PALETTE_ALIGNMENT, BasicControlPalette.DisplayMode mode) {
        super(CONTROL_PALETTE_ALIGNMENT, mode);
        init();
    }

    public MultipleTreeViewer(ControlPalette controlPalette, int controlsLocation) {
        super(controlPalette, controlsLocation);
        init();
    }

    public void setTree(Tree tree) {
        this.trees = new ArrayList<Tree>();
        trees.add(tree);
        setCurrentTree(tree);
        treesChanged();
    }

    private int labelSize = 6;

    private int currentTree = 0;

    public void setTrees(Collection<? extends Tree> trees) {
        setTrees(trees, labelSize);
    }

    public void setTrees(Collection<? extends Tree> trees, int defaultLabelSize) {
        this.trees = new ArrayList<Tree>(trees);
        labelSize = defaultLabelSize;
        setCurrentTree(this.trees.get(0));
        treesChanged();
    }

    private boolean updatingCombo = false;

    private void treesChanged() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updatingCombo = true;
                currentTreeCombo.removeAllItems();
                for (String name : getTreeNames(trees)) {
                    currentTreeCombo.addItem(name);
                }
                currentTreeCombo.setSelectedIndex(currentTree);
                updatingCombo = false;
            }
        });
    }

    private void setCurrentTree(Tree tree) {
        currentTree = trees.indexOf(tree);
        super.setTree(tree, labelSize);
    }

    public List<Tree> getAllTrees(){
        trees.set(currentTree, treePane.getTree()); //make sure the list is up to date
        return new ArrayList<Tree>(trees);
    }

    public Tree getTree(){
        return trees.get(currentTree);
    }

    private ControlsProvider multipleTreeControlsProvider = new ControlsProvider() {

        public void setControlPalette(ControlPalette controlPalette) {
            // do nothing
        }

        public List<Controls> getControls(boolean detachPrimaryCheckbox) {

            List<Controls> controlsList = new ArrayList<Controls>();

            if (controls == null) {
                OptionsPanel optionsPanel = new OptionsPanel();

                currentTreeCombo = new JComboBox(getTreeNames(trees));
                currentTreeCombo.setMaximumRowCount(24);
                currentTreeCombo.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        if (updatingCombo) {
                            return;
                        }
                        trees.set(currentTree, treePane.getTree());
                        setCurrentTree(trees.get(currentTreeCombo.getSelectedIndex()));
                        //this is here becasue the selection clears when we change trees, and we
                        //want to refresh the toolbar
                        treePane.clearSelection();
                    }
                });
                optionsPanel.addComponentWithLabel("Tree:", currentTreeCombo);

                controls = new Controls("Current Tree", optionsPanel, true);
            }

            controlsList.add(controls);

            return controlsList;
        }

        public void setSettings(ControlsSettings settings) {
        }

        public void getSettings(ControlsSettings settings) {
        }

        private Controls controls = null;
    };

    private static Vector<String> getTreeNames(List<Tree> trees) {
        final Vector<String> names = new Vector<String>();
        for( int i = 0; i < trees.size(); i ++) {
            Tree tree = trees.get(i);
            final Object oname = tree.getAttribute(NexusExporter.treeNameAttributeKey);
            final String indexString = "" + (i + 1) + "/" + trees.size();
            final String name = oname == null ? indexString : oname.toString() + " (" + indexString + ")";

            names.add(name);
        }
        return names;
    }

    private List<Tree> trees = null;
    private JComboBox currentTreeCombo;
}
