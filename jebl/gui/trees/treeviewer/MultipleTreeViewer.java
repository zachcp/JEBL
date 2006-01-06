package jebl.gui.trees.treeviewer;

import jebl.evolution.trees.Tree;
import jebl.gui.trees.treeviewer.controlpanels.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.*;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class MultipleTreeViewer extends TreeViewer {
	public MultipleTreeViewer() {
		super();
		getControlPanel().addControlsProvider(multipleTreeControlsProvider);
	}

	public void setTree(Tree tree) {
		this.trees = new ArrayList<Tree>();
		trees.add(tree);
		setCurrentTree(tree);
	}

	public void setTrees(Collection<? extends Tree> trees) {
		setTrees(trees,  6);
	}

    public void setTrees(Collection<? extends Tree> trees, int defaultLabelSize) {
        this.trees = new ArrayList<Tree>(trees);
        super.setTree(this.trees.get(0), defaultLabelSize);
    }

    private void setCurrentTree(Tree tree) {
		super.setTree(tree);
	}

	private ControlsProvider multipleTreeControlsProvider = new ControlsProvider() {

	    public void setControlPanel(ControlPanel controlPanel) {
	        // do nothing
	    }

	    public List<Controls> getControls() {

	        List<Controls> controls = new ArrayList<Controls>();

	        if (optionsPanel == null) {
	            optionsPanel = new OptionsPanel();

		        final JSpinner spinner1 = new JSpinner(new SpinnerNumberModel(1, 1, trees.size(), 1));

		        spinner1.addChangeListener(new ChangeListener() {
		            public void stateChanged(ChangeEvent changeEvent) {
		                setCurrentTree(trees.get((Integer)spinner1.getValue() - 1));
		            }
		        });
		        optionsPanel.addComponentWithLabel("Tree:", spinner1);
	        }

	        controls.add(new Controls("Current Tree", optionsPanel));

	        return controls;
	    }

	    private OptionsPanel optionsPanel = null;
	};

	private List<Tree> trees = null;
}
