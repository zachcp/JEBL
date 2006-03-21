package jebl.gui.trees.treeviewer;

import jebl.evolution.trees.Tree;
import org.virion.jam.controlpanels.*;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.prefs.Preferences;

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

    public MultipleTreeViewer(Preferences control_palette_alignment, int right) {
        super(control_palette_alignment, right);
        init();
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

	    public void setControlPalette(ControlPalette controlPalette) {
	        // do nothing
	    }

		public List<Controls> getControls() {

		    List<Controls> controlsList = new ArrayList<Controls>();

		    if (controls == null) {
		        OptionsPanel optionsPanel = new OptionsPanel();

			    final JSpinner spinner1 = new JSpinner(new SpinnerNumberModel(1, 1, trees.size(), 1));

			    spinner1.addChangeListener(new ChangeListener() {
			        public void stateChanged(ChangeEvent changeEvent) {
			            setCurrentTree(trees.get((Integer)spinner1.getValue() - 1));
			        }
			    });
			    optionsPanel.addComponentWithLabel("Tree:", spinner1);

			    controls = new Controls("Current Tree", optionsPanel, true);
		    }

		    controlsList.add(controls);

		    return controlsList;
		}

		public void setSettings(Controls controls, ControlsSettings settings) {
		}

		public ControlsSettings getSettings(Controls controls) {
			return null;
		}

		private Controls controls = null;
	};

	private List<Tree> trees = null;
}
