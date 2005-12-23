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
import jebl.evolution.trees.*;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.io.TreeImporter;
import jebl.evolution.io.NexusImporter;
import jebl.evolution.io.ImportException;
import jebl.gui.trees.treeviewer.painters.BasicTaxonLabelPainter;
import jebl.gui.trees.treeviewer.treelayouts.*;
import jebl.gui.trees.treeviewer.decorators.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import java.io.FileReader;
import java.io.File;

/**
 * A panel that displays correlation plots of 2 traces
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id$
 */
public class TreeViewer extends JPanel {
    public enum TreeLayoutType {
        RECTILINEAR,
        POLAR,
        RADIAL
    };

    final private String[] layoutNames = {"Rectangle", "Polar", "Radial"};

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
    };

    /** Creates new AlignmentPanel */
    public TreeViewer() {

        setOpaque(false);
        setLayout(new BorderLayout());

        treePane = new TreePane();
        treePane.setAutoscrolls(true); //enable synthetic drag events

        JScrollPane scrollPane = new JScrollPane(treePane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setMinimumSize(new Dimension(150,150));

        scrollPane.setBorder(null);
        viewport = scrollPane.getViewport();

        controlPanel = new JToolBar();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.PAGE_AXIS));
        controlPanel.setPreferredSize(new Dimension(150,150));
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(controlPanel, BorderLayout.NORTH);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, panel);
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(1.0);
        splitPane.setDividerLocation(0.95);

        add(splitPane, BorderLayout.CENTER);

        setTreeLayoutType(TreeLayoutType.RECTILINEAR);

        // This overrides MouseListener and MouseMotionListener to allow selection in the TreePane -
        // It installs itself within the constructor.
        treePaneSelector = new TreePaneSelector(treePane);
    }

    public void setTree(Tree tree, int defaultLabelSize) {
        this.tree = (RootedTree)tree;
//        treePane.setTree(new SortedRootedTree((RootedTree)tree, Utils.createNodeDensityComparator((RootedTree)tree)));
        treePane.setTree(new SortedRootedTree((RootedTree)tree));
        treePane.setTaxonLabelPainter(new BasicTaxonLabelPainter(tree, defaultLabelSize));

        setupControlPanel();
    }

    public void setTree(Tree tree) {
        setTree(tree, 6);
    }

    private TreeLayoutType currentLayout = null;

    public void setTreeLayoutType(TreeLayoutType treeLayoutType) {
        currentLayout = treeLayoutType;
        switch (treeLayoutType) {
            case RECTILINEAR: treeLayout = new RectilinearTreeLayout(); break;
            case POLAR: treeLayout = new PolarTreeLayout(); break;
            case RADIAL: treeLayout = new RadialTreeLayout(); break;
            default: throw new IllegalArgumentException("Unknown TreeLayoutType: " + treeLayoutType);
        }

        treePane.setTreeLayout(treeLayout);

        setupControlPanel();
    }

    public void setupControlPanel() {
        controlPanel.removeAll();

        JPanel cp1 = new JPanel();
        cp1.setLayout(new BoxLayout(cp1, BoxLayout.PAGE_AXIS));

        cp1.add(new JLabel("Zoom:"));
        final JSlider zoomSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 0);
        zoomSlider.setPaintTicks(true);
        zoomSlider.setPaintLabels(true);

        zoomSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                setZoom(((double)zoomSlider.getValue()) / 100.0);
            }
        });
        cp1.add(zoomSlider);

        JLabel label = new JLabel("Vertical Expansion:");
        cp1.add(label);
        final JSlider verticalExpansionSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 1000, 0);
        verticalExpansionSlider.setPaintTicks(true);
        verticalExpansionSlider.setPaintLabels(true);

        verticalExpansionSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                setVerticalExpansion(((double)verticalExpansionSlider.getValue()) / 100.0);
            }
        });

        label.setEnabled(!treeLayout.maintainAspectRatio());
        verticalExpansionSlider.setEnabled(!treeLayout.maintainAspectRatio());

        cp1.add(verticalExpansionSlider);
        controlPanel.add(cp1);

        controlPanel.add(new JSeparator());

        JPanel cp2 = treeLayout.getControlPanel();
        controlPanel.add(cp2);
        controlPanel.add(new JSeparator());

        JPanel cp3 = new JPanel();
        cp3.setLayout(new BoxLayout(cp3, BoxLayout.PAGE_AXIS));

        final JCheckBox checkBox1 = new JCheckBox("Show Taxon Labels");
        cp3.add(checkBox1);
        checkBox1.setAlignmentX(Component.LEFT_ALIGNMENT);

        checkBox1.setSelected(treePane.isShowingTaxonLabels());
        checkBox1.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                treePane.setShowingTaxonLabels(checkBox1.isSelected());
            }
        });

        if (treePane.getTaxonLabelPainter() != null) {
            cp3.add(treePane.getTaxonLabelPainter().getControlPanel());
        }

        final JCheckBox checkBox2 = new JCheckBox("Show Root Branch");
        cp3.add(checkBox2);
        checkBox2.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkBox2.setSelected(treePane.isShowingRootBranch());
        checkBox2.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                treePane.setShowingRootBranch(checkBox2.isSelected());

            }
        });

        JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel2.add(new JLabel("Line Weight:"));
        final JSpinner spinner2 = new JSpinner(new SpinnerNumberModel(1.0, 0.01, 48.0, 1.0));

        spinner2.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                treePane.setBranchLineWeight(((Double)spinner2.getValue()).floatValue());

            }
        });
        panel2.add(spinner2);
        cp3.add(panel2);
        panel2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel panel3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel3.add(new JLabel("Layout:"));

        final JComboBox layoutsBox = new JComboBox();
        for( TreeLayoutType i : TreeLayoutType.values() ) {
            layoutsBox.addItem(i);
        }
        if( currentLayout != null ) {
           layoutsBox.setSelectedItem(currentLayout);
        }
        layoutsBox.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setTreeLayoutType((TreeLayoutType)layoutsBox.getSelectedItem());
            }
        } );

        panel3.add(layoutsBox);
        cp3.add(panel3);
        panel3.setAlignmentX(Component.LEFT_ALIGNMENT);

        controlPanel.add(cp3);
        cp3.setAlignmentX(Component.LEFT_ALIGNMENT);

        validate();
        repaint();
    }

    public void setControlPanelVisible(boolean visible) {
        if (visible) {
            splitPane.setDividerLocation(splitPane.getLastDividerLocation());
        } else {
            splitPane.setDividerLocation(Integer.MAX_VALUE);
        }
    }

    public void setBranchDecorator(BranchDecorator branchDecorator) {
        treePane.setBranchDecorator(branchDecorator);
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

    protected TreeLayout treeLayout = null;

    protected RootedTree tree = null;

    protected TreePane treePane;
    protected TreePaneSelector treePaneSelector;

    protected JViewport viewport;
    protected JSplitPane splitPane;
    protected JToolBar controlPanel;

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