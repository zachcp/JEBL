package jebl.gui.trees.treeviewer_dev.painters;

import org.virion.jam.controlpalettes.Controller;
import org.virion.jam.controlpalettes.ControllerSettings;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.NumberFormat;
import java.util.Set;
import java.util.TreeSet;
import java.util.Arrays;

import jebl.evolution.trees.Tree;
import jebl.evolution.graphs.Node;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class LabelPainterController implements Controller {

    public LabelPainterController(String title, final LabelPainter labelPainter) {
        this.title = title;
        this.labelPainter = labelPainter;

        optionsPanel = new OptionsPanel();

        titleCheckBox = new JCheckBox(getTitle());

        titleCheckBox.setSelected(labelPainter.isVisible());

        titleCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                final boolean selected = titleCheckBox.isSelected();
                //label1.setEnabled(selected);
                //fontSizeSpinner.setEnabled(selected);
                //label2.setEnabled(selected);
                //digitsSpinner.setEnabled(selected);
                labelPainter.setVisible(selected);
            }
        });

        String[] attributes = labelPainter.getAttributes();
        displayAttributeCombo = new JComboBox(attributes);
        displayAttributeCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                String attribute = (String)displayAttributeCombo.getSelectedItem();
                labelPainter.setDisplayAttribute(attribute);
            }
        });

        optionsPanel.addComponentWithLabel("Display:", displayAttributeCombo);

        Font font = labelPainter.getFont();
        final JSpinner fontSizeSpinner = new JSpinner(new SpinnerNumberModel(font.getSize(), 0.01, 48, 1));

        final JLabel label1 = optionsPanel.addComponentWithLabel("Font Size:", fontSizeSpinner);
        //final boolean xselected = showTextCHeckBox.isSelected();
        //label1.setEnabled(selected);
        //fontSizeSpinner.setEnabled(selected);

        fontSizeSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                final float size = ((Double) fontSizeSpinner.getValue()).floatValue();
                Font font = labelPainter.getFont().deriveFont(size);
                labelPainter.setFont(font);
            }
        });

        NumberFormat format = labelPainter.getNumberFormat();
        int digits = format.getMaximumFractionDigits();
        final JSpinner digitsSpinner = new JSpinner(new SpinnerNumberModel(digits, 2, 14, 1));

        final JLabel label2 = optionsPanel.addComponentWithLabel("Significant Digits:", digitsSpinner);
        // label2.setEnabled(selected);
        //  digitsSpinner.setEnabled(selected);

        digitsSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                final int digits = (Integer)digitsSpinner.getValue();
                NumberFormat format = labelPainter.getNumberFormat();
                format.setMaximumFractionDigits(digits);
                labelPainter.setNumberFormat(format);
            }
        });

        labelPainter.addPainterListener(new PainterListener() {
            public void painterChanged() {
                
            }

            public void painterSettingsChanged() {
                displayAttributeCombo.removeAllItems();
                for (String name : labelPainter.getAttributes()) {
                    displayAttributeCombo.addItem(name);
                }

                optionsPanel.repaint();
            }
        });
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

    public void getSettings(ControllerSettings settings) {
    }

    public void setSettings(ControllerSettings settings) {
    }

    private final JCheckBox titleCheckBox;
    private final OptionsPanel optionsPanel;

    private JComboBox displayAttributeCombo;

    public String getTitle() {
        return title;
    }

    private final String title;

    private final LabelPainter labelPainter;
}
