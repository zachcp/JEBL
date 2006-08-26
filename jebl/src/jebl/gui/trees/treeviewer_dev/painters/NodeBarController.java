package jebl.gui.trees.treeviewer_dev.painters;

import org.virion.jam.controlpalettes.AbstractController;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.*;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class NodeBarController extends AbstractController {

    private static Preferences PREFS = Preferences.userNodeForPackage(NodeBarController.class);

    private static final String NODE_BARS_KEY = "nodeBars";

    private static final String BAR_WIDTH_KEY = "barWidth";

    private static float DEFAULT_BAR_WIDTH = 4.0f;

    public NodeBarController(String title, final NodeBarPainter nodeBarPainter) {
        this.title = title;
        this.nodeBarPainter = nodeBarPainter;

        final float defaultBarWidth = PREFS.getFloat(BAR_WIDTH_KEY, DEFAULT_BAR_WIDTH);

        optionsPanel = new OptionsPanel();

        titleCheckBox = new JCheckBox(getTitle());

        titleCheckBox.setSelected(this.nodeBarPainter.isVisible());

        titleCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                final boolean selected = titleCheckBox.isSelected();
                nodeBarPainter.setVisible(selected);
            }
        });

        String[] attributeNames = this.nodeBarPainter.getAttributeNames();

        displayAttributeCombo = new JComboBox(attributeNames);
        displayAttributeCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                String attribute = (String)displayAttributeCombo.getSelectedItem();
                nodeBarPainter.setDisplayAttribute(attribute);
            }
        });

        optionsPanel.addComponentWithLabel("Display:", displayAttributeCombo);

        this.nodeBarPainter.addPainterListener(new PainterListener() {
            public void painterChanged() {

            }

            public void painterSettingsChanged() {
                displayAttributeCombo.removeAllItems();
                for (String name : nodeBarPainter.getAttributeNames()) {
                    displayAttributeCombo.addItem(name);
                }

                optionsPanel.repaint();
            }
        });

        barWidthSpinner = new JSpinner(new SpinnerNumberModel(defaultBarWidth, 0.01, 48.0, 1.0));

        barWidthSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                float lineWidth = ((Double) barWidthSpinner.getValue()).floatValue();
                nodeBarPainter.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            }
        });
        optionsPanel.addComponentWithLabel("Bar Width:", barWidthSpinner);

        nodeBarPainter.setStroke(new BasicStroke(defaultBarWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));

    }

    public JComponent getTitleComponent() {
        return titleCheckBox;
    }

    public JPanel getPanel() {
        return optionsPanel;
    }

    public boolean isInitiallyVisible() {
        return false;
    }

    public void setSettings(Map<String,Object> settings) {
        barWidthSpinner.setValue((Double)settings.get(NODE_BARS_KEY + "." + BAR_WIDTH_KEY));
    }

    public void getSettings(Map<String, Object> settings) {
        settings.put(NODE_BARS_KEY + "." + BAR_WIDTH_KEY, barWidthSpinner.getValue());
    }


    private final JCheckBox titleCheckBox;
    private final OptionsPanel optionsPanel;

    private JComboBox displayAttributeCombo;

    public String getTitle() {
        return title;
    }

    private final String title;

    private final NodeBarPainter nodeBarPainter;

    private final JSpinner barWidthSpinner;
}
