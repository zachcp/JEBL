package jebl.gui.trees.treeviewer;

import jebl.evolution.io.NexusExporter;
import jebl.evolution.trees.Tree;
import org.virion.jam.controlpanels.*;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @version $Id$
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
    }

    private int labelSize = 6;

    public void setTrees(Collection<? extends Tree> trees) {
        setTrees(trees, labelSize);
    }

    public void setTrees(Collection<? extends Tree> trees, int defaultLabelSize) {
        this.trees = new ArrayList<Tree>(trees);
        labelSize = defaultLabelSize;
        super.setTree(this.trees.get(0), defaultLabelSize);
    }

    private void setCurrentTree(Tree tree) {
        super.setTree(tree, labelSize);
    }

    private ControlsProvider multipleTreeControlsProvider = new ControlsProvider() {

        public void setControlPalette(ControlPalette controlPalette) {
            // do nothing
        }

        public List<Controls> getControls(boolean detachPrimaryCheckbox) {

            List<Controls> controlsList = new ArrayList<Controls>();

            if (controls == null) {
                OptionsPanel optionsPanel = new OptionsPanel();

                boolean useNames = false;
                for( Tree t : trees ) {
                    Object name = t.getAttribute(NexusExporter.treeNameAttributeKey);
                    if( !NexusExporter.isGeneratedTreeName(name.toString()) ) {
                        useNames = true;
                        break;
                    }
                }
                if( useNames ) {
                    final List<String> names = new ArrayList<String>();
                    for( Tree t : trees ) {
                        Object name = t.getAttribute(NexusExporter.treeNameAttributeKey);
                        if( name == null ) {
                            name = trees.indexOf(t);
                        }
                        names.add(name.toString());
                    }
                    final JSpinner spinner1 = new JSpinner(new SpinnerListModel(names));

                    spinner1.addChangeListener(new ChangeListener() {
                        public void stateChanged(ChangeEvent changeEvent) {
                            setCurrentTree(trees.get( names.indexOf( spinner1.getValue()) ) );
                        }
                    });
                    optionsPanel.addComponentWithLabel("Tree:", spinner1);
                }  else {
                    final JSpinner spinner1 = new JSpinner(new SpinnerNumberModel(1, 1, trees.size(), 1));

                    spinner1.addChangeListener(new ChangeListener() {
                        public void stateChanged(ChangeEvent changeEvent) {
                            setCurrentTree(trees.get((Integer) spinner1.getValue() - 1));
                        }
                    });
                    optionsPanel.addComponentWithLabel("Tree:", spinner1);
                }

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

    private List<Tree> trees = null;
}
