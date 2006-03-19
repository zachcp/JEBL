package org.virion.jam.controlpanels;

import java.util.List;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public interface ControlsProvider {
    void setControlPanel(ControlPaletteInterface controlPalette);

    List<Controls> getControls();
}
