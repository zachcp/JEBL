package jebl.gui.trees.treeviewer_dev;

import jebl.gui.trees.treeviewer_dev.treelayouts.*;
import org.virion.jam.controlpalettes.Controller;
import org.virion.jam.controlpalettes.ControllerSettings;
import org.virion.jam.panels.OptionsPanel;
import org.virion.jam.util.IconUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class TreeViewerController implements Controller {

	public enum TreeLayoutType {
		RECTILINEAR("Rectangle"),
		POLAR("Polar"),
		RADIAL("Radial");

		TreeLayoutType(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}

		private final String name;
	}

	private final static int MAX_ZOOM_SLIDER = 10000;
	private final static int DELTA_ZOOM_SLIDER = 200;

	public TreeViewerController(final TreeViewer treeViewer) {

		this.treeViewer = treeViewer;

		titleLabel = new JLabel("Layout");
		optionsPanel = new OptionsPanel();

		rectilinearTreeLayout = new RectilinearTreeLayout();
		rectilinearTreeLayoutController = new RectilinearTreeLayoutController(rectilinearTreeLayout);

		polarTreeLayout = new PolarTreeLayout();
		polarTreeLayoutController = new PolarTreeLayoutController(polarTreeLayout);

		radialTreeLayout = new RadialTreeLayout();
		radialTreeLayoutController = new RadialTreeLayoutController(radialTreeLayout);

		treeViewer.setTreeLayout(rectilinearTreeLayout);

		JPanel panel1 = new JPanel();
		panel1.setLayout(new BoxLayout(panel1, BoxLayout.LINE_AXIS));
		Icon rectangularTreeIcon = IconUtils.getIcon(this.getClass(), "/jebl/gui/trees/treeviewer_dev/images/rectangularTree.png");
		Icon polarTreeIcon = IconUtils.getIcon(this.getClass(), "/jebl/gui/trees/treeviewer_dev/images/polarTree.png");
		Icon radialTreeIcon = IconUtils.getIcon(this.getClass(), "/jebl/gui/trees/treeviewer_dev/images/radialTree.png");
		rectangularTreeToggle = new JToggleButton(rectangularTreeIcon);
		polarTreeToggle = new JToggleButton(polarTreeIcon);
		radialTreeToggle = new JToggleButton(radialTreeIcon);
		rectangularTreeToggle.putClientProperty("Quaqua.Button.style", "toggleWest");
		polarTreeToggle.putClientProperty("Quaqua.Button.style", "toggleCenter");
		radialTreeToggle.putClientProperty("Quaqua.Button.style", "toggleEast");
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(rectangularTreeToggle);
		buttonGroup.add(polarTreeToggle);
		buttonGroup.add(radialTreeToggle);
		rectangularTreeToggle.setSelected(true);
		panel1.add(Box.createHorizontalStrut(0));
		panel1.add(rectangularTreeToggle);
		panel1.add(polarTreeToggle);
		panel1.add(radialTreeToggle);
		panel1.add(Box.createHorizontalStrut(0));

		optionsPanel.addSpanningComponent(panel1);

		zoomSlider = new JSlider(SwingConstants.HORIZONTAL, 0, MAX_ZOOM_SLIDER, 0);
		zoomSlider.setAlignmentX(Component.LEFT_ALIGNMENT);

		zoomSlider.setPaintTicks(true);
		zoomSlider.setPaintLabels(true);

		zoomSlider.setValue(0);

		zoomSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				final int value = zoomSlider.getValue();
				treeViewer.setZoom(((double) value) / MAX_ZOOM_SLIDER);
			}
		});

		optionsPanel.addComponentWithLabel("Zoom:", zoomSlider, true);

		verticalExpansionSlider = new JSlider(SwingConstants.HORIZONTAL, 0, MAX_ZOOM_SLIDER, 0);
		verticalExpansionSlider.setPaintTicks(true);
		verticalExpansionSlider.setPaintLabels(true);

		verticalExpansionSlider.setValue(0);

		verticalExpansionSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				final int value = verticalExpansionSlider.getValue();
				treeViewer.setVerticalExpansion(((double) value) / MAX_ZOOM_SLIDER);
			}
		});

		verticalExpansionLabel = new JLabel("Expansion:");
		optionsPanel.addComponents(verticalExpansionLabel, false, verticalExpansionSlider, true);
		setExpansion();

		optionsPanel.addSeparator();

		final JPanel layoutPanel = new JPanel(new BorderLayout());
		layoutPanel.add(rectilinearTreeLayoutController.getPanel(), BorderLayout.CENTER);
		optionsPanel.addSpanningComponent(layoutPanel);

		rectangularTreeToggle.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				if (rectangularTreeToggle.isSelected())
					treeViewer.setTreeLayout(rectilinearTreeLayout);
				setExpansion();
				layoutPanel.removeAll();
				layoutPanel.add(rectilinearTreeLayoutController.getPanel(), BorderLayout.CENTER);
				optionsPanel.invalidate();

			}
		});
		polarTreeToggle.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				if (polarTreeToggle.isSelected())
					treeViewer.setTreeLayout(polarTreeLayout);
				setExpansion();
				layoutPanel.removeAll();
				layoutPanel.add(polarTreeLayoutController.getPanel(), BorderLayout.CENTER);
				optionsPanel.invalidate();
			}
		});
		radialTreeToggle.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				if (radialTreeToggle.isSelected())
					treeViewer.setTreeLayout(radialTreeLayout);
				setExpansion();
				layoutPanel.removeAll();
				layoutPanel.add(radialTreeLayoutController.getPanel(), BorderLayout.CENTER);
				optionsPanel.invalidate();
			}
		});

		// Set some InputMaps and ActionMaps for key strokes. The ActionMaps are set in setExpansion()
		// because they differ by whether vertical expansion is allowed for the current layout.
		// The key strokes could be obtained from preferences and set in a preference dialog box
		optionsPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke("meta 0"), "resetZoom");
		optionsPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke("meta EQUALS"), "increasePrimaryZoom");
		optionsPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke("meta MINUS"), "decreasePrimaryZoom");
		optionsPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke("meta alt EQUALS"), "increaseSecondaryZoom");
		optionsPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke("meta alt MINUS"), "decreaseSecondaryZoom");

		optionsPanel.getActionMap().put("resetZoom", resetZoomAction);

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
	public void setSettings(ControllerSettings settings) {
		TreeLayoutType layout = (TreeLayoutType)settings.getSetting("Layout");
		switch (layout) {
			case RECTILINEAR:
				rectangularTreeToggle.setSelected(true);
				break;
			case POLAR:
				polarTreeToggle.setSelected(true);
				break;
			case RADIAL:
				radialTreeToggle.setSelected(true);
				break;
		}
		zoomSlider.setValue((Integer) settings.getSetting("Zoom"));
		verticalExpansionSlider.setValue((Integer) settings.getSetting("Expansion"));
	}

	public void getSettings(ControllerSettings settings) {
		if (rectangularTreeToggle.isSelected()) {
			settings.putSetting("Layout", TreeLayoutType.RECTILINEAR);
		} else if (polarTreeToggle.isSelected()) {
			settings.putSetting("Layout", TreeLayoutType.POLAR);
		} else if (radialTreeToggle.isSelected()) {
			settings.putSetting("Layout", TreeLayoutType.RADIAL);
		}
		settings.putSetting("Zoom", zoomSlider.getValue());
		settings.putSetting("Expansion", verticalExpansionSlider.getValue());
	}

	private void setExpansion() {
		if (treeViewer.verticalExpansionAllowed()) {
			verticalExpansionLabel.setEnabled(true);
			verticalExpansionSlider.setEnabled(true);
			optionsPanel.getActionMap().put("increasePrimaryZoom", increaseVerticalExpansionAction);
			optionsPanel.getActionMap().put("decreasePrimaryZoom", decreaseVerticalExpansionAction);
			optionsPanel.getActionMap().put("increaseSecondaryZoom", increaseZoomAction);
			optionsPanel.getActionMap().put("decreaseSecondaryZoom", decreaseZoomAction);
		} else {
			verticalExpansionLabel.setEnabled(false);
			verticalExpansionSlider.setEnabled(false);
			optionsPanel.getActionMap().put("increasePrimaryZoom", increaseZoomAction);
			optionsPanel.getActionMap().put("decreasePrimaryZoom", decreaseZoomAction);
			optionsPanel.getActionMap().put("increaseSecondaryZoom", increaseZoomAction);
			optionsPanel.getActionMap().put("decreaseSecondaryZoom", decreaseZoomAction);
		}
	}

	private Action resetZoomAction = new AbstractAction("Reset Zoom") {
		public void actionPerformed(ActionEvent actionEvent) {
			zoomSlider.setValue(0);
			verticalExpansionSlider.setValue(0);
		}
	};

	private Action increaseZoomAction = new AbstractAction("Zoom In") {
		public void actionPerformed(ActionEvent actionEvent) {
			zoomSlider.setValue(zoomSlider.getValue() + DELTA_ZOOM_SLIDER);
		}
	};

	private Action decreaseZoomAction = new AbstractAction("Zoom In") {
		public void actionPerformed(ActionEvent actionEvent) {
			zoomSlider.setValue(zoomSlider.getValue() - DELTA_ZOOM_SLIDER);
		}
	};

	private Action increaseVerticalExpansionAction = new AbstractAction("Expand Vertically") {
		public void actionPerformed(ActionEvent actionEvent) {
			verticalExpansionSlider.setValue(verticalExpansionSlider.getValue() + DELTA_ZOOM_SLIDER);
		}
	};

	private Action decreaseVerticalExpansionAction = new AbstractAction("Unexpand Vertically") {
		public void actionPerformed(ActionEvent actionEvent) {
			int value = verticalExpansionSlider.getValue();
			if (value > 0) {
				verticalExpansionSlider.setValue(value - DELTA_ZOOM_SLIDER);
			} else {
				// If the vertical expansion was zero then assume the user is trying to un-zoom
				zoomSlider.setValue(zoomSlider.getValue() - DELTA_ZOOM_SLIDER);
			}
		}
	};


	private JToggleButton rectangularTreeToggle;
	private JToggleButton polarTreeToggle;
	private JToggleButton radialTreeToggle;
	private JSlider zoomSlider;
	private JSlider verticalExpansionSlider;
	private JLabel verticalExpansionLabel;

	private final JLabel titleLabel;
	private final OptionsPanel optionsPanel;

	private final RectilinearTreeLayout rectilinearTreeLayout;
	private final PolarTreeLayout polarTreeLayout;
	private final RadialTreeLayout radialTreeLayout;

	private final RectilinearTreeLayoutController rectilinearTreeLayoutController;
	private final PolarTreeLayoutController polarTreeLayoutController;
	private final RadialTreeLayoutController radialTreeLayoutController;

	private final TreeViewer treeViewer;

}
