package jebl.gui.trees.treeviewer.controlpanels;

import java.util.List;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public interface ControlsProvider {
    void setControlPanel(ControlPanel controlPanel);    
    List<Controls> getControls();
}
