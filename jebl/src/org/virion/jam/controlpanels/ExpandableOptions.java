package org.virion.jam.controlpanels;

import org.virion.jam.disclosure.DisclosureListener;
import org.virion.jam.disclosure.DisclosurePanel;
import org.virion.jam.util.SimpleListener;
import sun.awt.VerticalBagLayout;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.prefs.Preferences;

/**
 * @author Matt Kearse
 * @version $Id$
 *          <p/>
 *          A set of expandable panels that remember their state between sessions.
 *          Currently used by the sequence viewer.
 */
public class ExpandableOptions extends JPanel {

    JPanel mainPanel;
    boolean allowMultipleExpanded;
    Preferences preferences;
    private int maxPanelWidth = 0;
    JScrollPane scroll;
    private static final String EXPANDED_KEY = "expanded";

    public ExpandableOptions(boolean allowMultipleExpanded, final Preferences preferences) {
        this.allowMultipleExpanded = allowMultipleExpanded;
        this.preferences = preferences;
        mainPanel = new JPanel(new VerticalBagLayout());
        setLayout(new BorderLayout());
        scroll = new JScrollPane(mainPanel, JScrollPane. VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        scroll.getHorizontalScrollBar().setUnitIncrement(10);
        scroll.getVerticalScrollBar().setUnitIncrement(10);

        scroll.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        // use simple scrolling, since adding components to residue frequencies at run time causes problems

        add(scroll, BorderLayout.CENTER);
        boolean expanded = preferences.getBoolean(EXPANDED_KEY, true);
        ExpanderPanel expander = new ExpanderPanel(expanded);
        scroll.setVisible(expanded);
        expander.addExpansionListener(new SimpleListener() {
            public void objectChanged() {
                scroll.setVisible(!scroll.isVisible());
                revalidate();
                preferences.putBoolean(EXPANDED_KEY, scroll.isVisible());
            }
        });
        add(expander, BorderLayout.WEST);
    }

    /**
     * @param preferenceSuffix a unique suffix to store the preferences under
     * @param text             the text to show at top level
     * @param contents         the panel of contents
     * @param component        another component?
     * @param defaultOpen      true if these options should be expanded by default
     */
    public void addOption(String preferenceSuffix, String text, JPanel contents, JComponent component, boolean defaultOpen) {
        final String preferenceName = "expanded_" + preferenceSuffix;
        boolean open = preferences.getBoolean(preferenceName, defaultOpen);
        DisclosurePanel disclosure = new DisclosurePanel(text, contents, open, true, component);
        disclosure.addDisclosureListener(new DisclosureListener() {
            public void opening(Component component) {
            }

            public void opened(Component component) {
                preferences.putBoolean(preferenceName, true);
            }

            public void closing(Component component) {
            }

            public void closed(Component component) {
                preferences.putBoolean(preferenceName, false);
            }
        });

        disclosure.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));
        mainPanel.add(disclosure);
        Dimension d = contents.getPreferredSize();
        int width = (int) d.getWidth();
        width += disclosure.getBorder().getBorderInsets(disclosure).left + disclosure.getBorder().getBorderInsets(disclosure).right;
        maxPanelWidth = Math.max(width, maxPanelWidth);
        recalculateScrollPaneSize();
    }

    private void recalculateScrollPaneSize() {
        //next few lines of code ensure that the
        // options panel is wide enough to accommodate its vertical scroll bar.
        //if this is not done, then for some strange reason, when the mouse is moved over
        //the name of this viewer in the tabbed panel, then the scroll panel will automatically resize
        //to allow for the size of the scroll bar, even when the scroll bar is not visible.
        scroll.getPreferredSize();
        Dimension verticalScrollBarSize = scroll.getVerticalScrollBar().getUI().getPreferredSize(scroll);
        int extra = verticalScrollBarSize.width;
        // Logs.fine("extra =" + extra);
        scroll.setPreferredSize(new Dimension(maxPanelWidth + extra, 100));
    }

    void removeAllOptions() {
        mainPanel.removeAll();
        maxPanelWidth = 0;
    }
}
