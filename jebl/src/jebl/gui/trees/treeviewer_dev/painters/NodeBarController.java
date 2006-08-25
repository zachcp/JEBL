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

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class NodeBarController extends AbstractController {

    public NodeBarController(String title, final NodeBarPainter nodeBarPainter) {
        this.title = title;
        this.nodeBarPainter = nodeBarPainter;

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

        barWidthSpinner = new JSpinner(new SpinnerNumberModel(4.0, 0.01, 48.0, 1.0));

        barWidthSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                float lineWidth = ((Double) barWidthSpinner.getValue()).floatValue();
                nodeBarPainter.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            }
        });
        optionsPanel.addComponentWithLabel("Bar Width:", barWidthSpinner);

        nodeBarPainter.setStroke(new BasicStroke(4.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));

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

    public void getSettings(Map<String, Object> settings) {
    }

    public void setSettings(Map<String,Object> settings) {
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
