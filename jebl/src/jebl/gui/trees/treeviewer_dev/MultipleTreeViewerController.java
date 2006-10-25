package jebl.gui.trees.treeviewer_dev;

import org.virion.jam.controlpalettes.AbstractController;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Map;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class MultipleTreeViewerController extends AbstractController {

    public MultipleTreeViewerController(final MultipleTreeViewer treeViewer) {

        this.treeViewer = treeViewer;

        titleLabel = new JLabel("Current Tree");
        optionsPanel = new OptionsPanel();

        OptionsPanel optionsPanel = new OptionsPanel();

        currentTreeSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1, 1) {
            public Comparable getMaximum() {
                return treeViewer.getTrees().size();
            }
        });

        currentTreeSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                treeViewer.getTrees();
                treeViewer.setCurrentTree(treeViewer.getTrees().get((Integer)currentTreeSpinner.getValue() - 1));
            }
        });
        optionsPanel.addComponentWithLabel("Tree:", currentTreeSpinner);


    }

    public JComponent getTitleComponent() {
        return titleLabel;
    }

    public JPanel getPanel() {
        return optionsPanel;
    }

    public boolean isInitiallyVisible() {
        return true;
    }
    
    public void initialize() {
        // nothing to do
    }

    public void setSettings(Map<String,Object> settings) {
    }

    public void getSettings(Map<String, Object> settings) {
    }

    private final JSpinner currentTreeSpinner;

    private final JLabel titleLabel;
    private final OptionsPanel optionsPanel;

    private final MultipleTreeViewer treeViewer;

}
