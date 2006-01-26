/*
 * AlignmentPanel.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */

package jebl.gui.trees.treeviewer;

import jebl.evolution.graphs.Node;
import jebl.evolution.io.NexusImporter;
import jebl.evolution.io.TreeImporter;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.Tree;
import jebl.evolution.trees.Utils;
import org.virion.jam.controlpanels.*;
import jebl.gui.trees.treeviewer.decorators.BranchDecorator;
import jebl.gui.trees.treeviewer.painters.BasicLabelPainter;
import jebl.gui.trees.treeviewer.painters.Painter;
import jebl.gui.trees.treeviewer.painters.ScaleBarPainter;
import org.virion.jam.util.IconUtils;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class TreeViewer extends JPanel {

    public enum SearchType {
        CONTAINS("Contains"),
        STARTS_WITH("Starts with"),
        ENDS_WITH("Ends with"),
        MATCHES("Matches");

        SearchType(String name) {
            this.name = name;
        }

        public String toString() { return name; }

        private final String name;
    }

    static final int  defaultPalleteSize = 200;

    /** Creates new TreeViewer */
    public TreeViewer() {
	    this(new ControlPalette(defaultPalleteSize, ControlPalette.DisplayMode.ONLY_ONE_OPEN, true), SwingConstants.LEFT);
    }

    public TreeViewer(int CONTROL_PALETTE_ALIGNMENT, ControlPalette.DisplayMode mode) {
        this(new ControlPalette(defaultPalleteSize, mode, true), CONTROL_PALETTE_ALIGNMENT);
    }

    public TreeViewer(int CONTROL_PALETTE_ALIGNMENT) {
        this(new ControlPalette(defaultPalleteSize, ControlPalette.DisplayMode.ONLY_ONE_OPEN, true), CONTROL_PALETTE_ALIGNMENT);
    }

    /** Creates new TreeViewer */
	public TreeViewer(ControlPalette controlPalette, int CONTROL_PALETTE_ALIGNMENT) {
        setOpaque(false);
        setLayout(new BorderLayout());

        treePane = new TreePane();
        treePane.setAutoscrolls(true); //enable synthetic drag events

        JScrollPane scrollPane = new JScrollPane(treePane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setMinimumSize(new Dimension(150,150));

        scrollPane.setBorder(null);
        viewport = scrollPane.getViewport();

        this.controlPalette = controlPalette;
	    controlPalette.setBorder(BorderFactory.createMatteBorder(0,0,0,1,Color.GRAY));

//        JPanel panel = new JPanel(new BorderLayout());
//        panel.add(controlPalette, BorderLayout.NORTH);

//        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, controlPalette);
//        splitPane.setContinuousLayout(true);
//        splitPane.setOneTouchExpandable(true);
//        splitPane.setResizeWeight(1.0);
//        splitPane.setDividerLocation(0.95);
//
//        add(splitPane, BorderLayout.CENTER);

        add(scrollPane, BorderLayout.CENTER);

		if (CONTROL_PALETTE_ALIGNMENT == SwingConstants.LEFT) {
			add(controlPalette, BorderLayout.WEST);
		} else {
			add(controlPalette, BorderLayout.EAST);
		}
        treePane.setTreeLayoutType(TreePane.TreeLayoutType.RECTILINEAR);

        // This overrides MouseListener and MouseMotionListener to allow selection in the TreePane -
        // It installs itself within the constructor.
        treePaneSelector = new TreePaneSelector(treePane);

        controlPalette.addControlsProvider(controlsProvider);
        controlPalette.addControlsProvider(treePane);

        controlPalette.addControlPanelListener(new ControlPaletteListener() {
            public void controlsChanged() {
                TreeViewer.this.controlPalette.setupControls();
                validate();
                repaint();
            }
        });

    }

    public void setTree(Tree inTree, int defaultLabelSize) {
        if( tree instanceof RootedTree ) {
            tree = (RootedTree)inTree;
        } else {
            tree = Utils.rootTheTree(inTree);
        }

        //System.out.println(Utils.toNewick(tree));
        treePane.setTree(tree);

        BasicLabelPainter taxonLabelPainter = new BasicLabelPainter("Tip Labels", tree, true, defaultLabelSize);
        taxonLabelPainter.setAttribute(BasicLabelPainter.TAXON_NAMES);
        treePane.setTaxonLabelPainter(taxonLabelPainter);

        BasicLabelPainter nodeLabelPainter = new BasicLabelPainter("Node Labels", tree);
        nodeLabelPainter.setVisible(false);
        treePane.setNodeLabelPainter(nodeLabelPainter);

        treePane.setScaleBarPainter(new ScaleBarPainter());
    }

    public void setTree(Tree tree) {
        setTree(tree, 6);
    }

    public ControlPalette getControlPanel() {
        return controlPalette;
    }

	private ControlsProvider controlsProvider = new ControlsProvider() {

	    public void setControlPanel(ControlPalette controlPalette) {
	        // do nothing
	    }

	    public java.util.List<Controls> getControls() {

	        List<Controls> controlsList = new ArrayList<Controls>();

	        if (controls == null) {
	            OptionsPanel optionsPanel = new OptionsPanel();

	            JPanel panel1 = new JPanel();
		        panel1.setLayout(new BoxLayout(panel1, BoxLayout.LINE_AXIS));
	            Icon rectangularTreeIcon = IconUtils.getIcon(this.getClass(), "/jebl/gui/trees/treeviewer/images/rectangularTree.png");
	            Icon polarTreeIcon = IconUtils.getIcon(this.getClass(), "/jebl/gui/trees/treeviewer/images/polarTree.png");
	            Icon radialTreeIcon = IconUtils.getIcon(this.getClass(), "/jebl/gui/trees/treeviewer/images/radialTree.png");
	            final JToggleButton toggle1 = new JToggleButton(rectangularTreeIcon);
	            final JToggleButton toggle2 = new JToggleButton(polarTreeIcon);
	            final JToggleButton toggle3 = new JToggleButton(radialTreeIcon);
	            toggle1.putClientProperty( "Quaqua.Button.style", "toggleWest");
	            toggle2.putClientProperty( "Quaqua.Button.style", "toggleCenter");
	            toggle3.putClientProperty( "Quaqua.Button.style", "toggleEast");
	            ButtonGroup buttonGroup = new ButtonGroup();
	            buttonGroup.add(toggle1);
	            buttonGroup.add(toggle2);
	            buttonGroup.add(toggle3);
	            toggle1.setSelected(true);
		        panel1.add(Box.createHorizontalStrut(0));
	            panel1.add(toggle1);
	            panel1.add(toggle2);
	            panel1.add(toggle3);
		        panel1.add(Box.createHorizontalStrut(0));

	            optionsPanel.addSpanningComponent(panel1);

	            final JSlider zoomSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 0);
	            zoomSlider.setAlignmentX(Component.LEFT_ALIGNMENT);

	            zoomSlider.setPaintTicks(true);
	            zoomSlider.setPaintLabels(true);

	            zoomSlider.addChangeListener(new ChangeListener() {
	                public void stateChanged(ChangeEvent changeEvent) {
	                    setZoom(((double)zoomSlider.getValue()) / 100.0);
	                }
	            });

	            optionsPanel.addComponentWithLabel("Zoom:", zoomSlider, true);

	            final JSlider verticalExpansionSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 1000, 0);
	            verticalExpansionSlider.setPaintTicks(true);
	            verticalExpansionSlider.setPaintLabels(true);

	            verticalExpansionSlider.addChangeListener(new ChangeListener() {
	                public void stateChanged(ChangeEvent changeEvent) {
	                    setVerticalExpansion(((double)verticalExpansionSlider.getValue()) / 100.0);
	                }
	            });

	            final JLabel label = new JLabel("Expansion:");
		        optionsPanel.addComponents(label, false, verticalExpansionSlider, true);
	            label.setEnabled(!treePane.maintainAspectRatio());
	            verticalExpansionSlider.setEnabled(!treePane.maintainAspectRatio());

		        toggle1.addChangeListener(new ChangeListener() {
		            public void stateChanged(ChangeEvent changeEvent) {
		                if (toggle1.isSelected())
		                    treePane.setTreeLayoutType(TreePane.TreeLayoutType.RECTILINEAR);
			            label.setEnabled(!treePane.maintainAspectRatio());
			            verticalExpansionSlider.setEnabled(!treePane.maintainAspectRatio());
		            }
		        });
		        toggle2.addChangeListener(new ChangeListener() {
		            public void stateChanged(ChangeEvent changeEvent) {
		                if (toggle2.isSelected())
		                    treePane.setTreeLayoutType(TreePane.TreeLayoutType.POLAR);
			            label.setEnabled(!treePane.maintainAspectRatio());
			            verticalExpansionSlider.setEnabled(!treePane.maintainAspectRatio());
		            }
		        });
		        toggle3.addChangeListener(new ChangeListener() {
		            public void stateChanged(ChangeEvent changeEvent) {
		                if (toggle3.isSelected())
		                    treePane.setTreeLayoutType(TreePane.TreeLayoutType.RADIAL);
			            label.setEnabled(!treePane.maintainAspectRatio());
			            verticalExpansionSlider.setEnabled(!treePane.maintainAspectRatio());
		            }
		        });

		        controls = new Controls("General", optionsPanel, true);
	        }

	        controlsList.add(controls);

	        return controlsList;
	    }
	    private Controls controls = null;
	};

    public void setControlPanelVisible(boolean visible) {
        controlPalette.setVisible(visible);
    }

    public void setBranchDecorator(BranchDecorator branchDecorator) {
        treePane.setBranchDecorator(branchDecorator);
    }

    public void setNodeLabelPainter(Painter<Node> nodeLabelPainter) {
        treePane.setNodeLabelPainter(nodeLabelPainter);
    }

    public void setZoom(double zoom) {
        setZoom(zoom, zoom);
    }

    public void setVerticalExpansion(double verticalExpansion) {
        setZoom(0.0, verticalExpansion);
    }

    public void setZoom(double xZoom, double yZoom) {
        Dimension viewportSize = viewport.getViewSize();
        Point position = viewport.getViewPosition();

        Dimension extentSize = viewport.getExtentSize();
        double w = extentSize.getWidth() * (1.0 + (10.0  * xZoom));
        double h = extentSize.getHeight() * (1.0 + (10.0  * yZoom));

        Dimension newSize = new Dimension((int)w, (int)h);
        treePane.setPreferredSize(newSize);

        double cx = position.getX() + (0.5 * extentSize.getWidth());
        double cy = position.getY() + (0.5 * extentSize.getHeight());

        double rx = ((double)newSize.getWidth()) / viewportSize.getWidth();
        double ry = ((double)newSize.getHeight()) / viewportSize.getHeight();

        double px = (cx * rx) - (extentSize.getWidth() / 2.0);
        double py = (cy * ry) - (extentSize.getHeight() / 2.0);

        Point newPosition = new Point((int)px, (int)py);
        viewport.setViewPosition(newPosition);
        treePane.revalidate();
    }

    public void selectTaxa(SearchType searchType, String searchString, boolean caseSensitive) {
        treePane.clearSelection();

        if (searchType == SearchType.MATCHES && !caseSensitive) {
            throw new IllegalArgumentException("Regular expression matching cannot be case-insensitive");
        }

        String query = (caseSensitive ? searchString : searchString.toUpperCase());

        for (Taxon taxon : tree.getTaxa()) {
            String target = (caseSensitive ?
                    taxon.getName() : taxon.getName().toUpperCase());
            switch (searchType) {
                case CONTAINS:
                    if (target.contains(query)) {
                        treePane.addSelectedTaxon(taxon);
                    }
                    break;
                case STARTS_WITH:
                    if (target.startsWith(query)) {
                        treePane.addSelectedTaxon(taxon);
                    }
                    break;
                case ENDS_WITH:
                    if (target.endsWith(query)) {
                        treePane.addSelectedTaxon(taxon);
                    }
                    break;
                case MATCHES:
                    if (target.matches(query)) {
                        treePane.addSelectedTaxon(taxon);
                    }
                    break;
            }
        }
    }

    public void selectNodes(String attribute, SearchType searchType, String searchString, boolean caseSensitive) {
        treePane.clearSelection();

        if (searchType == SearchType.MATCHES && !caseSensitive) {
            throw new IllegalArgumentException("Regular expression matching cannot be case-insensitive");
        }

        String query = (caseSensitive ? searchString : searchString.toUpperCase());

        for (Node node : tree.getNodes()) {
            Object value = node.getAttribute(attribute);

            if (value != null) {
                String target = (caseSensitive ?
                        value.toString() : value.toString().toUpperCase());
                switch (searchType) {
                    case CONTAINS:
                        if (target.contains(query)) {
                            treePane.addSelectedNode(node);
                        }
                        break;
                    case STARTS_WITH:
                        if (target.startsWith(query)) {
                            treePane.addSelectedNode(node);
                        }
                        break;
                    case ENDS_WITH:
                        if (target.endsWith(query)) {
                            treePane.addSelectedNode(node);
                        }
                        break;
                    case MATCHES:
                        if (target.matches(query)) {
                            treePane.addSelectedNode(node);
                        }
                        break;
                }
            }
        }
    }

    public void clearSelectedTaxa() {
        treePane.clearSelection();
    }

    public void setSelectionMode(TreePaneSelector.SelectionMode selectionMode) {
        treePaneSelector.setSelectionMode(selectionMode);
    }

    public void setDragMode(TreePaneSelector.DragMode dragMode) {
        treePaneSelector.setDragMode(dragMode);
    }

    public JComponent getExportableComponent() {
        return treePane;
    }

    protected RootedTree tree = null;

    protected TreePane treePane;
    protected TreePaneSelector treePaneSelector;

    protected JViewport viewport;
    protected JSplitPane splitPane;
    private ControlPalette controlPalette;

    static public void main(String[] args) {

        JFrame frame = new JFrame("TreeViewer Test");
        TreeViewer treeViewer = new TreeViewer();

        try {
            File inputFile = null;

            if (args.length > 0) {
                inputFile = new File(args[0]);
            }

            if (inputFile == null) {
                // No input file name was given so throw up a dialog box...
                java.awt.FileDialog chooser = new java.awt.FileDialog(frame, "Select NEXUS Tree File",
                                                                    java.awt.FileDialog.LOAD);
                chooser.setVisible(true);
                inputFile = new java.io.File(chooser.getDirectory(), chooser.getFile());
                chooser.dispose();
            }

            if (inputFile ==  null) {
                throw new RuntimeException("No file specified");
            }

//        TreeImporter importer = new NewickImporter(new FileReader(inputFile));
            TreeImporter importer = new NexusImporter(new FileReader(inputFile));
            Tree tree = importer.importNextTree();

            treeViewer.setTree(tree);
        } catch (Exception ie) {
            ie.printStackTrace();
            System.exit(1);
        }

        frame.getContentPane().add(treeViewer, BorderLayout.CENTER);
        frame.setVisible(true);
    }
}