package org.virion.jam.toolbar;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.*;
import java.awt.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public class Toolbar extends JToolBar {

    public Toolbar() {
		this(new ToolbarOptions(ToolbarOptions.ICON_AND_TEXT, false));
    }

	public Toolbar(ToolbarOptions options) {

		this.options = options;

	    setLayout(new GridBagLayout());

	    setBorder(BorderFactory.createCompoundBorder(
	        BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray),
	        BorderFactory.createEmptyBorder(2, 12, 2, 12)));

		final JPopupMenu menu = new JPopupMenu();

		menu.setLightWeightPopupEnabled(false);

		ChangeListener listener = new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				toolbarOptionsChanged();
			}
		};

		ButtonGroup group = new ButtonGroup();
		iconTextMenuItem = new JRadioButtonMenuItem("Icon & Text");
		iconTextMenuItem.addChangeListener(listener);
		group.add(iconTextMenuItem);
		menu.add(iconTextMenuItem);

		iconOnlyMenuItem = new JRadioButtonMenuItem("Icon Only");
		iconOnlyMenuItem.addChangeListener(listener);
		group.add(iconOnlyMenuItem);
		menu.add(iconOnlyMenuItem);

		textOnlyMenuItem = new JRadioButtonMenuItem("Text Only");
		textOnlyMenuItem.addChangeListener(listener);
		group.add(textOnlyMenuItem);
		menu.add(textOnlyMenuItem);

		menu.add(new JSeparator());

		smallSizeMenuItem = new JCheckBoxMenuItem("Small Size");
		smallSizeMenuItem.addChangeListener(listener);
		menu.add(smallSizeMenuItem);

		menu.add(new JSeparator());

		JMenuItem item = new JMenuItem("Customize Toolbar...");
		item.setEnabled(false);
		menu.add(item);

		// Set the component to show the popup menu
		addMouseListener(new MouseAdapter() {
		    public void mousePressed(MouseEvent evt) {
		        if (evt.isPopupTrigger()) {
		            menu.show(evt.getComponent(), evt.getX(), evt.getY());
		        }
		    }
		    public void mouseReleased(MouseEvent evt) {
		        if (evt.isPopupTrigger()) {
		            menu.show(evt.getComponent(), evt.getX(), evt.getY());
		        }
		    }
		});

		iconTextMenuItem.setSelected(options.getDisplay() == ToolbarOptions.ICON_AND_TEXT);
		iconOnlyMenuItem.setSelected(options.getDisplay() == ToolbarOptions.ICON_ONLY);
		textOnlyMenuItem.setSelected(options.getDisplay() == ToolbarOptions.TEXT_ONLY);
		smallSizeMenuItem.setSelected(options.getSmallSize());
	}

    public void addComponent(Component component) {
	    if (component instanceof ToolbarItem) {
		    ToolbarItem item = (ToolbarItem)component;
		    toolbarItems.add(item);
		    item.setToolbarOptions(options);
	    }
        addItem(component);
    }

    public void addSeperator() {
        addItem(new Separator());
    }

    public void addSpace() {
        addItem(new Separator());
    }

    private void addItem(Component component) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = 0;
        c.weightx = 0;
        add(component, c);
    }

    public void addFlexibleSpace() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = 0;
        c.weightx = 1;
        add(new Separator(), c);
    }

	private void toolbarOptionsChanged() {
		int display = (iconTextMenuItem.isSelected() ? ToolbarOptions.ICON_AND_TEXT :
						(iconOnlyMenuItem.isSelected() ? ToolbarOptions.ICON_ONLY :
								ToolbarOptions.TEXT_ONLY));
		boolean smallSize = smallSizeMenuItem.isSelected();

		setToolbarOptions(new ToolbarOptions(display, smallSize));
	}

	private void setToolbarOptions(ToolbarOptions toolbarOptions) {
		this.options = toolbarOptions;

		Iterator iter = toolbarItems.iterator();
		while (iter.hasNext()) {
			ToolbarItem item = (ToolbarItem)iter.next();
			item.setToolbarOptions(options);
		}

		validate();
		repaint();
	}

	private ToolbarOptions options;

	private final JRadioButtonMenuItem iconTextMenuItem;
	private final JRadioButtonMenuItem iconOnlyMenuItem;
	private final JRadioButtonMenuItem textOnlyMenuItem;
	private final JCheckBoxMenuItem smallSizeMenuItem;

	private List toolbarItems = new ArrayList();
}