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
import jebl.evolution.io.NexusExporter;
import jebl.evolution.io.NexusImporter;
import jebl.evolution.io.TreeImporter;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.TransformedRootedTree;
import jebl.evolution.trees.Tree;
import jebl.evolution.trees.Utils;
import jebl.gui.trees.treeviewer.decorators.BranchDecorator;
import jebl.gui.trees.treeviewer.painters.*;
import jebl.gui.trees.treeviewer.treelayouts.PolarTreeLayout;
import jebl.gui.trees.treeviewer.treelayouts.RadialTreeLayout;
import jebl.gui.trees.treeviewer.treelayouts.RectilinearTreeLayout;
import jebl.gui.trees.treeviewer.treelayouts.TreeLayout;
import jebl.util.NumberFormatter;
import org.virion.jam.controlpanels.*;
import org.virion.jam.panels.OptionsPanel;
import org.virion.jam.util.IconUtils;
import org.virion.jam.util.SimpleListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
@SuppressWarnings("unused")
public class TreeViewer extends JPanel implements Printable {
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

    public enum SearchType {
        CONTAINS("Contains"),
        STARTS_WITH("Starts with"),
        ENDS_WITH("Ends with"),
        MATCHES("Matches");

        SearchType(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }

        private final String name;
    }

    static final int defaultPaletteSize = 200;

    JToggleButton rectilinearToggle;
    JToggleButton polarToggle;
    JToggleButton radialToggle;

    /**
     * Creates new TreeViewer
     */
    public TreeViewer() {
        this(new BasicControlPalette(defaultPaletteSize, BasicControlPalette.DisplayMode.ONLY_ONE_OPEN, true), SwingConstants.LEFT);
    }

    public TreeViewer(int CONTROL_PALETTE_ALIGNMENT, BasicControlPalette.DisplayMode mode) {
        this(new BasicControlPalette(defaultPaletteSize, mode, true), CONTROL_PALETTE_ALIGNMENT);
    }

    public TreeViewer(int CONTROL_PALETTE_ALIGNMENT) {
        this(new BasicControlPalette(defaultPaletteSize, BasicControlPalette.DisplayMode.ONLY_ONE_OPEN, true), CONTROL_PALETTE_ALIGNMENT);
    }

    public Tree getTree(){
        return treePane.getTree();
    }

    /**
     * Creates new TreeViewer
     */
    public TreeViewer(ControlPalette controlPalette, int CONTROL_PALETTE_ALIGNMENT) {
        setOpaque(false);
        setLayout(new BorderLayout());

        treePane = new TreePane();
        treePane.setAutoscrolls(true); //enable synthetic drag events
        listeners = new HashSet<SimpleListener>();

        //make sure that the change listeners are fired when the treeSelectionListener fires
        treePane.addTreeSelectionListener(new TreeSelectionListener(){
            public void selectionChanged() {
                fireChangeListeners();
            }
        });

        JScrollPane scrollPane = new JScrollPane(treePane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(15);
        scrollPane.setMinimumSize(new Dimension(150, 150));
        treePane.setViewPort(scrollPane.getViewport());

        scrollPane.setBorder(null);
        viewport = scrollPane.getViewport();
        viewport.addComponentListener(new ComponentAdapter(){
            public void componentResized(ComponentEvent e) {
                refreshZoom();
            }
        });

        this.controlPalette = controlPalette;
        controlPalette.getPanel().setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));

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
            add(controlPalette.getPanel(), BorderLayout.WEST);
        } else {
            add(controlPalette.getPanel(), BorderLayout.EAST);
        }
        setTreeLayoutType(TreeLayoutType.RECTILINEAR);

        // This overrides MouseListener and MouseMotionListener to allow selection in the TreePane -
        // It installs itself within the constructor.
        treePaneSelector = new TreePaneSelector(treePane);

        controlPalette.addControlsProvider(controlsProvider, false);
        controlPalette.addControlsProvider(treePane, false);

        controlPalette.addControlPanelListener(new ControlPaletteListener() {
            public void controlsChanged() {
                TreeViewer.this.controlPalette.setupControls();
                validate();
                repaint();
            }
        });
    }

    private String currentTreeLayoutPrefKey() {
        return (tree.conceptuallyUnrooted() ? "treelayout_unrooted" : "treelayout_rooted");
    }

    protected TreeLayoutType getDefaultTreeLayoutType() {
        TreeLayoutType defaultLayout = TreeLayoutType.RECTILINEAR;
        String layoutPrefKey = currentTreeLayoutPrefKey();
        return TreeLayoutType.values()[getPrefs().getInt(layoutPrefKey, defaultLayout.ordinal())];
    }

    protected void setDefaultTreeLayoutType(TreeLayoutType treeLayoutType) {
        String layoutPrefKey = currentTreeLayoutPrefKey();
        getPrefs().putInt(layoutPrefKey, treeLayoutType.ordinal());
    }

    private void fireChangeListeners(){
        for(SimpleListener listener : listeners){
            listener.objectChanged();
        }
    }

    /**
     *
     * @param listener
     * @return true if the supplied listener is not already attached
     */
    public boolean addChangeListener(SimpleListener listener){
        return listeners.add(listener);
    }

    /**
     *
     * @param listener
     * @return true if the supplied listener was attached
     */
    public boolean removeChangeListener(SimpleListener listener){
        return listeners.remove(listener);
    }

    public void setTree(Tree inTree, int defaultLabelSize) {
        if (inTree instanceof TransformedRootedTree) inTree = ((TransformedRootedTree) inTree).getSource();
        final boolean isRooted = (inTree instanceof RootedTree);
        if (isRooted) {
            tree = (RootedTree) inTree;
        } else {
            tree = Utils.rootTheTree(inTree);
        }
//        infoArea.setText("");
        infoIsVisible = false;
        // make this settable?
        infoText = "";
        NumberFormatter formatter = new NumberFormatter(4);
        for( String an : inTree.getAttributeNames() ) {
            if( ! an.startsWith("&") && !an.equals(NexusExporter.treeNameAttributeKey) ) {
                Object o = inTree.getAttribute(an);
                String v;
                if( o instanceof Double ) {
                    v = formatter.getFormattedValue((Double) o);
                } else {
                    v = o.toString();
                }
//                infoArea.append(an + ": " + v + "\n");
                infoText += an + ": " + v + "\n";
                infoIsVisible = true;
            }
        }

        treePane.setTree(tree, null);

//        BasicLabelPainter taxonLabelPainter =
//                new BasicLabelPainter("Tip Labels", tree, BasicLabelPainter.PainterIntent.TIP, defaultLabelSize);
//        taxonLabelPainter.setAttribute(BasicLabelPainter.TAXON_NAMES);
        TaxonLabelPainter taxonLabelPainter = new TaxonLabelPainter(tree);
        treePane.setTaxonLabelPainter(taxonLabelPainter);

        BasicLabelPainter nodeLabelPainter =
                new BasicLabelPainter("Node Labels", tree, BasicLabelPainter.PainterIntent.NODE, defaultLabelSize);

        // don't show controls when there is nothing to choose from
        treePane.setNodeLabelPainter(nodeLabelPainter.getAttributes().length > 0 ? nodeLabelPainter : null);

        BasicLabelPainter branchLabelPainter =
                new BasicLabelPainter("Branch Labels", tree, BasicLabelPainter.PainterIntent.BRANCH, defaultLabelSize);

        treePane.setBranchLabelPainter(branchLabelPainter.getAttributes().length > 0 ? branchLabelPainter : null);

        CollapsedNodeLabelPainter collapsedNodeLabelPainter = new CollapsedNodeLabelPainter(tree);
        treePane.setCollapsedNodeLabelPainter(collapsedNodeLabelPainter);

        treePane.setScaleBarPainter(new ScaleBarPainter());

        // load appropriate tree layout from preferences and set it
        setTreeLayoutType(getDefaultTreeLayoutType());
    }

    public void setTree(Tree tree) {
        setTree(tree, 6);
    }

    public TreePane getTreePane(){
        return treePane;
    }

    public ControlPalette getControlPalette() {
        return controlPalette;
    }

    private static Preferences getPrefs() {
        return Preferences.userNodeForPackage(TreeViewer.class);
    }

//    private JTextArea infoArea = null;
    private boolean infoIsVisible = false;
    private String infoText = "";

    @SuppressWarnings("FieldCanBeLocal")
    private ControlsProvider controlsProvider = new ControlsProvider() {

        public void setControlPalette(ControlPalette controlPalette) {
            // do nothing
        }

        private void setExpansion() {
            final boolean enabled = !treePane.maintainAspectRatio();
            verticalExpansionLabel.setEnabled(enabled);
            verticalExpansionSlider.setEnabled(enabled);
        }

        public java.util.List<Controls> getControls(boolean detachPrimaryCheckbox) {

            List<Controls> controlsList = new ArrayList<Controls>();

            if (controls == null) {
                OptionsPanel optionsPanel = new OptionsPanel();

                JPanel treeViewPanel = new JPanel();
                treeViewPanel.setLayout(new BoxLayout(treeViewPanel, BoxLayout.LINE_AXIS));
                final String imagePath = "/jebl/gui/trees/treeviewer/images/";
                Icon rectangularTreeIcon = IconUtils.getIcon(this.getClass(), imagePath + "rectangularTree.png");
                Icon polarTreeIcon = IconUtils.getIcon(this.getClass(), imagePath + "polarTree.png");
                Icon radialTreeIcon = IconUtils.getIcon(this.getClass(), imagePath + "radialTree.png");
                rectilinearToggle = new JToggleButton(rectangularTreeIcon);
                polarToggle = new JToggleButton(polarTreeIcon);
                radialToggle = new JToggleButton(radialTreeIcon);
                rectilinearToggle.setToolTipText("Rooted tree layout");
                polarToggle.setToolTipText("Circular tree layout");
                radialToggle.setToolTipText("Unrooted tree layout");
                rectilinearToggle.putClientProperty("Quaqua.Button.style", "toggleWest");
                polarToggle.putClientProperty("Quaqua.Button.style", "toggleCenter");
                radialToggle.putClientProperty("Quaqua.Button.style", "toggleEast");
                ButtonGroup buttonGroup = new ButtonGroup();
                buttonGroup.add(rectilinearToggle);
                buttonGroup.add(polarToggle);
                buttonGroup.add(radialToggle);

                final JLabel unrootedLabel = new JLabel("Showing unrooted tree as rooted");
                unrootedLabel.setFont(unrootedLabel.getFont().deriveFont(11f));

                switch (getDefaultTreeLayoutType()) {
                    case RECTILINEAR:
                        rectilinearToggle.setSelected(true);
                        unrootedLabel.setVisible(tree.conceptuallyUnrooted());
                        break;
                    case POLAR:
                        unrootedLabel.setVisible(tree.conceptuallyUnrooted());
                        polarToggle.setSelected(true);
                        break;
                    case RADIAL:
                        unrootedLabel.setVisible(false);
                        radialToggle.setSelected(true);
                        break;
                }

                treeViewPanel.add(Box.createHorizontalStrut(0));

                treeViewPanel.add(rectilinearToggle);
                treeViewPanel.add(polarToggle);
                treeViewPanel.add(radialToggle);
                treeViewPanel.add(Box.createHorizontalGlue());
                treeViewPanel.add(unrootedLabel);
                treeViewPanel.add(Box.createHorizontalGlue());
                optionsPanel.addSpanningComponent(treeViewPanel);

                Preferences prefs = getPrefs();

                zoomSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 0);
                zoomSlider.setAlignmentX(Component.LEFT_ALIGNMENT);

                zoomSlider.setPaintTicks(true);
                zoomSlider.setPaintLabels(true);

                final String zoomValuePrefKey = "zoomvalue";
                final int zoomValue = prefs.getInt(zoomValuePrefKey, 0);
                zoomSlider.setValue(zoomValue);
                zoom = ((double) zoomValue) / 100.0;
                zoomPending = true;

                zoomSlider.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent changeEvent) {
                        final int value = zoomSlider.getValue();
                        setZoom(((double) value) / 100.0);
                        getPrefs().putInt(zoomValuePrefKey, value);
                        fireChangeListeners();
                    }
                });

                optionsPanel.addComponentWithLabel("Zoom:", zoomSlider, true);

                verticalExpansionSlider = new JSlider(SwingConstants.HORIZONTAL, 0, calculateExpansionSliderMax(tree), 0);
                verticalExpansionSlider.setPaintTicks(true);
                verticalExpansionSlider.setPaintLabels(true);

                final String expansionValuePrefKey = "vzoomvalue";
                final int expansionValue = prefs.getInt(expansionValuePrefKey, 0);
                verticalExpansionSlider.setValue(expansionValue);
                verticalExpansion = ((double)expansionValue) / 100.0;

                verticalExpansionSlider.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent changeEvent) {
                        final int value = verticalExpansionSlider.getValue();
                        setVerticalExpansion(((double) value) / 100.0);
                        getPrefs().putInt(expansionValuePrefKey, value);
                        fireChangeListeners();
                    }
                });
                verticalExpansionSlider.addPropertyChangeListener(new PropertyChangeListener(){
                    public void propertyChange(PropertyChangeEvent evt) {
                        if(evt.getPropertyName().equals("enabled") && evt.getNewValue() instanceof Boolean) {
                            final int value = verticalExpansionSlider.getValue();
                            setVerticalExpansion(((Boolean)evt.getNewValue()) ? ((double) value) / 100.0 : 0.0);
                        }
                    }
                });

                verticalExpansionLabel = new JLabel("Expansion:");
                optionsPanel.addComponents(verticalExpansionLabel, false, verticalExpansionSlider, true);
                setExpansion();

                rectilinearToggle.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent changeEvent) {
                        if (rectilinearToggle.isSelected()) {
                            unrootedLabel.setVisible(tree.conceptuallyUnrooted());
                            setAndStoreTreeLayoutType(TreeLayoutType.RECTILINEAR);
                        }
                        setExpansion();
                        fireChangeListeners();
                    }
                });
                polarToggle.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent changeEvent) {
                        if (polarToggle.isSelected()) {
                            unrootedLabel.setVisible(tree.conceptuallyUnrooted());
                            setAndStoreTreeLayoutType(TreeLayoutType.POLAR);
                        }
                        setExpansion();
                        fireChangeListeners();
                    }
                });
                radialToggle.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent changeEvent) {
                        if (radialToggle.isSelected()) {
                            unrootedLabel.setVisible(false);
                            setAndStoreTreeLayoutType(TreeLayoutType.RADIAL);
                        }
                        setExpansion();
                        fireChangeListeners();
                    }
                });

                controls = new Controls("General", optionsPanel, true);
            }

            controlsList.add(controls);

            if (infoIsVisible) {
                JPanel jPanel = new JPanel(new BorderLayout());
                jPanel.setBorder(new EmptyBorder(5,5,5,5));
                JTextArea infoArea = new JTextArea(infoText);
                infoArea.setOpaque(false);
                infoArea.setFont(new JLabel().getFont());
                infoArea.setWrapStyleWord(true);
                infoArea.setLineWrap(true);
                infoArea.setEditable(false);
                jPanel.add(infoArea, BorderLayout.CENTER);
                Controls infoControls = new Controls("Info", jPanel, true);
                infoControls.setVisible(infoIsVisible);
                controlsList.add(infoControls);
            }

            //infoArea.setText("info");

            return controlsList;
        }

        public void setSettings(ControlsSettings settings) {
            zoomSlider.setValue((Integer) settings.getSetting("Zoom"));
            verticalExpansionSlider.setValue((Integer) settings.getSetting("Expansion"));
        }

        public void getSettings(ControlsSettings settings) {
            settings.putSetting("Zoom", zoomSlider.getValue());
            settings.putSetting("Expansion", verticalExpansionSlider.getValue());
        }

        private JSlider zoomSlider;
        private JSlider verticalExpansionSlider;
        private JLabel verticalExpansionLabel;

        private Controls controls = null;

    };

    public TreeLayoutType getTreeLayoutType(){
        TreeLayout layout = treePane.getTreeLayout();
        if(layout instanceof RectilinearTreeLayout)
            return TreeLayoutType.RECTILINEAR;
        if(layout instanceof PolarTreeLayout)
            return TreeLayoutType.POLAR;
        if(layout instanceof RadialTreeLayout)
            return TreeLayoutType.RADIAL;
        //this should never happen
        throw new RuntimeException("Unknown TreeLayoutType: " + layout);
    }

    public void setTreeLayoutType(TreeLayoutType treeLayoutType) {
        TreeLayout treeLayout;
        switch (treeLayoutType) {
            case RECTILINEAR:
                treeLayout = new RectilinearTreeLayout();
                if(rectilinearToggle != null) {
                    rectilinearToggle.setSelected(true);
                }
                break;
            case POLAR:
                treeLayout = new PolarTreeLayout();
                if(polarToggle != null) {
                    polarToggle.setSelected(true);
                }
                break;
            case RADIAL:
                treeLayout = new RadialTreeLayout();
                if(radialToggle != null) {
                    radialToggle.setSelected(true);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown TreeLayoutType: " + treeLayoutType);
        }
        treePane.setTreeLayout(treeLayout);
    }

    protected void setAndStoreTreeLayoutType(TreeLayoutType treeLayoutType) {
        setTreeLayoutType(treeLayoutType);
        setDefaultTreeLayoutType(treeLayoutType);
    }

    public void setControlPanelVisible(boolean visible) {
        controlPalette.getPanel().setVisible(visible);
    }

    public void setBranchDecorator(BranchDecorator branchDecorator) {
        treePane.setBranchDecorator(branchDecorator);
    }

    public void setNodeLabelPainter(Painter<Node> nodeLabelPainter) {
        treePane.setNodeLabelPainter(nodeLabelPainter);
    }

    private boolean zoomPending = false;
    private double zoom = 0.0, verticalExpansion = 0.0;

    public void setZoom(double zoom) {
        this.zoom = zoom;
        refreshZoom();
    }

    public void setVerticalExpansion(double verticalExpansion) {
        this.verticalExpansion = verticalExpansion;
        refreshZoom();
    }

    private void refreshZoom() {
       setZoom(zoom, zoom + verticalExpansion);
    }

    public void setZoom(double xZoom, double yZoom) {

        Dimension viewportSize = viewport.getViewSize();
        Point position = viewport.getViewPosition();

        Dimension extentSize = viewport.getExtentSize();
        double w = extentSize.getWidth() * (1.0 + (10.0 * xZoom));
        double h = extentSize.getHeight() * (1.0 + (10.0 * yZoom));

        Dimension newSize = new Dimension((int) w, (int) h);
        treePane.setPreferredSize(newSize);

        double cx = position.getX() + (0.5 * extentSize.getWidth());
        double cy = position.getY() + (0.5 * extentSize.getHeight());

        double rx = newSize.getWidth() / viewportSize.getWidth();
        double ry = newSize.getHeight() / viewportSize.getHeight();

        double px = (cx * rx) - (extentSize.getWidth() / 2.0);
        double py = (cy * ry) - (extentSize.getHeight() / 2.0);

        Point newPosition = new Point((int) px, (int) py);
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

    public void paint(Graphics g) {
        if( zoomPending  ) {
           refreshZoom();
           zoomPending = false;
        }
        super.paint(g);
    }

    protected RootedTree tree = null;

    protected TreePane treePane;
    protected TreePaneSelector treePaneSelector;

    protected JViewport viewport;
    protected JSplitPane splitPane;
    private ControlPalette controlPalette;
    private Set<SimpleListener> listeners;

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

//        TreeImporter importer = new NewickImporter(new FileReader(inputFile));
            Reader reader = new BufferedReader(new FileReader(inputFile));
            TreeImporter importer = new NexusImporter(reader);
            Tree tree = importer.importNextTree();
            reader.close();
            treeViewer.setTree(tree);
        } catch (Exception ie) {
            ie.printStackTrace();
            System.exit(1);
        }

        frame.getContentPane().add(treeViewer, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        return treePane.print(graphics, pageFormat, pageIndex);
    }

    /**
     * Calculates the maximum value of the expansion slider based off the formula {@code 1.12759 x},
     * where {@code x} is the number of tips in the tree.<br>
     * This is being used on a 'for now' basis to make the expansion slider take more reasonable values without
     * investing too much time.
     * <p>
     * The formula was determined by taking trees of varying number of tips, from 50 to 263691, with their font size
     * set to 36pt and using the expansion slider until each label had a 'comfortable' level of padding between them
     * (roughly the size of the label). Once the right level of expansion was found the expansion slider's value
     * was recorded and plotted. The trend line of the graph expansion slide value vs. number of tips then gave the
     * formula used.
     *
     * @param tree tree who is being considered
     * @return the maximum value the expansion slider should take
     */
    private int calculateExpansionSliderMax(Tree tree) {
        int numberOfTips = tree.getTaxa().size();
        double coefficient = 1.12759;
        return (int)(coefficient * numberOfTips);
    }
}
