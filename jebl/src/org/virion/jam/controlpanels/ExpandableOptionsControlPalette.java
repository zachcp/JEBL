package org.virion.jam.controlpanels;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: joseph
 * Date: 20/03/2006
 * Time: 10:33:22
 *
 * @author Joseph Heled
 * @version $Id$
 */
public class ExpandableOptionsControlPalette extends ExpandableOptions implements ControlPaletteInterface {
    public ExpandableOptionsControlPalette(boolean allowMultipleExpanded, Preferences preferences) {
        super(allowMultipleExpanded, preferences);
    }

    public JPanel getPanel() {
        return this;
    }

    private List<ControlsProvider> providers = new ArrayList<ControlsProvider>();

    public void addControlsProvider(ControlsProvider provider, boolean addAtStart) {
        provider.setControlPanel(this);
        providers.add(addAtStart ? 0 : providers.size(), provider);
    }

    private List<ControlPaletteListener> listeners = new ArrayList<ControlPaletteListener>();

    public void fireControlsChanged() {
        for (ControlPaletteListener listener : listeners) {
            listener.controlsChanged();
        }
    }

    public void addControlPanelListener(ControlPaletteListener listener) {
        listeners.add(listener);
    }

    public void removeControlPanelListener(ControlPaletteListener listener) {
        listeners.remove(listener);
    }

    public void setupControls() {
        removeAllOptions();

        boolean first = true;
        for (ControlsProvider controlsProvider : providers) {

            for (Controls controls : controlsProvider.getControls()) {
                addOption(controls.getTitle(), controls.getTitle(), controls.getPanel(), null, first);
                first = false;
            }
        }
    }
}
