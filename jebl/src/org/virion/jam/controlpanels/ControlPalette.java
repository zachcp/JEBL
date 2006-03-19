package org.virion.jam.controlpanels;

import org.virion.jam.disclosure.DisclosureListener;
import org.virion.jam.disclosure.DisclosurePanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class ControlPalette extends JPanel implements ControlPaletteInterface {

    public enum DisplayMode {
        DEFAULT_OPEN,
        INITIALLY_OPEN,
        INITIALLY_CLOSED,
        ONLY_ONE_OPEN
    }

    public ControlPalette(int preferredWidth) {
        this(preferredWidth, DisplayMode.DEFAULT_OPEN, false);
    }

    public ControlPalette(int preferredWidth, DisplayMode displayMode, boolean fastBlueStyle) {
        this.fastBlueStyle = fastBlueStyle;
        this.preferredWidth = preferredWidth;
        this.displayMode = displayMode;
        BoxLayout layout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
        setLayout(layout);
    }


    public Dimension getPreferredSize() {
        return new Dimension(preferredWidth, super.getPreferredSize().height);
    }

    public JPanel getPanel() {
        return this;
    }

    public void addControlsProvider(ControlsProvider provider, boolean addAtStart) {
        provider.setControlPanel(this);
        providers.add(addAtStart ? 0 : providers.size(), provider);
    }

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

    private final List<ControlPaletteListener> listeners = new ArrayList<ControlPaletteListener>();

    public void setupControls() {
        removeAll();
        disclosurePanels.clear();

        for (ControlsProvider provider : providers) {
            for (Controls controls : provider.getControls()) {
                add(Box.createVerticalStrut(1));
                addControls(controls);
            }
        }
        add(Box.createVerticalStrut(Integer.MAX_VALUE));
    }

    private void addControls(final Controls controls) {

        boolean open = true;

        switch (displayMode) {
            case DEFAULT_OPEN:
                open = controls.isVisible();
                break;
            case INITIALLY_CLOSED:
                open = false;
                break;
            case INITIALLY_OPEN:
                open = true;
                break;
            case ONLY_ONE_OPEN:
                open = (currentlyOpen == disclosurePanels.size());
                break;
            default:
                throw new IllegalArgumentException("Unknown DisplayMode enum item");
        }

        final DisclosurePanel panel = new DisclosurePanel(controls.getTitle(), controls.getPanel(), open, fastBlueStyle, null);

        if (displayMode == DisplayMode.ONLY_ONE_OPEN) {
            panel.addDisclosureListener(new DisclosureListener() {
                public void opening(Component component) {
                }

                public void opened(Component component) {
                    int newlyOpened = disclosurePanels.indexOf(component);
                    if (currentlyOpen >= 0) {
                        DisclosurePanel panel = disclosurePanels.get(currentlyOpen);
                        currentlyOpen = newlyOpened;
                        panel.setOpen(false);
                    } else {
                        currentlyOpen = newlyOpened;
                    }
                }

                public void closing(Component component) {
                }

                public void closed(Component component) {
                    int newlyClosed = disclosurePanels.indexOf(component);
                    if (newlyClosed == currentlyOpen) {
                        currentlyOpen = -1;
                    }
                }
            });
        }

        disclosurePanels.add(panel);

        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(panel);
    }

    private int preferredWidth;
    private DisplayMode displayMode;
    private boolean fastBlueStyle = false;
    private int currentlyOpen = 0;
    private List<ControlsProvider> providers = new ArrayList<ControlsProvider>();
    private List<DisclosurePanel> disclosurePanels = new ArrayList<DisclosurePanel>();

}
