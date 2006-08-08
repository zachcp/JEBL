package jebl.gui.trees.treeviewer_dev.painters;

import org.virion.jam.controlpalettes.AbstractController;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class LabelPainterController extends AbstractController {

    private static Preferences PREFS = Preferences.userNodeForPackage(LabelPainterController.class);

    private static final String FONT_NAME_KEY = "fontName";
    private static final String FONT_SIZE_KEY = "fontSize";
    private static final String FONT_STYLE_KEY = "fontStyle";

    private static final String NUMBER_FORMATTING_KEY = "numberFormatting";

    private static final String DISPLAY_ATTRIBUTE_KEY = "displayAttribute";
    private static final String SIGNIFICANT_DIGITS_KEY = "significantDigits";

    // The defaults if there is nothing in the preferences
    private static String DEFAULT_FONT_NAME = "sansserif";
    private static int DEFAULT_FONT_SIZE = 6;
    private static int DEFAULT_FONT_STYLE = Font.PLAIN;

    private static String DEFAULT_NUMBER_FORMATTING = "#.####";

    public LabelPainterController(String title, String key, final LabelPainter labelPainter) {

        this.title = title;
	    this.key = key;
        this.labelPainter = labelPainter;

        final String defaultFontName = PREFS.get(key + "." + FONT_NAME_KEY, DEFAULT_FONT_NAME);
        final int defaultFontStyle = PREFS.getInt(key + "." + FONT_SIZE_KEY, DEFAULT_FONT_STYLE);
        final int defaultFontSize = PREFS.getInt(key + "." + FONT_STYLE_KEY, DEFAULT_FONT_SIZE);
        final String defaultNumberFormatting = PREFS.get(key + "." + NUMBER_FORMATTING_KEY, DEFAULT_NUMBER_FORMATTING);

        labelPainter.setFont(new Font(defaultFontName, defaultFontStyle, defaultFontSize));
        labelPainter.setNumberFormat(new DecimalFormat(defaultNumberFormatting));

        optionsPanel = new OptionsPanel();

        titleCheckBox = new JCheckBox(getTitle());

        titleCheckBox.setSelected(labelPainter.isVisible());

        titleCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                final boolean selected = titleCheckBox.isSelected();
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
        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(font.getSize(), 0.01, 48, 1));

        optionsPanel.addComponentWithLabel("Font Size:", fontSizeSpinner);

        fontSizeSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                final float size = ((Double) fontSizeSpinner.getValue()).floatValue();
                Font font = labelPainter.getFont().deriveFont(size);
                labelPainter.setFont(font);
            }
        });

        NumberFormat format = labelPainter.getNumberFormat();
        int digits = format.getMaximumFractionDigits();
        digitsSpinner = new JSpinner(new SpinnerNumberModel(digits, 2, 14, 1));

        optionsPanel.addComponentWithLabel("Significant Digits:", digitsSpinner);

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

    public void setSettings(Map<String,Object> settings) {
        displayAttributeCombo.setSelectedItem(settings.get(key+"."+DISPLAY_ATTRIBUTE_KEY));
        fontSizeSpinner.setValue((Double)settings.get(key+"."+FONT_SIZE_KEY));
        digitsSpinner.setValue((Integer)settings.get(key+"."+SIGNIFICANT_DIGITS_KEY));
    }

    public void getSettings(Map<String, Object> settings) {
        settings.put(key+"."+DISPLAY_ATTRIBUTE_KEY, displayAttributeCombo.getSelectedItem().toString());
        settings.put(key+"."+FONT_SIZE_KEY, fontSizeSpinner.getValue());
        settings.put(key+"."+SIGNIFICANT_DIGITS_KEY, digitsSpinner.getValue());
    }

    public String getTitle() {
        return title;
    }

    private final JCheckBox titleCheckBox;
    private final OptionsPanel optionsPanel;

    private final JComboBox displayAttributeCombo;
    private final JSpinner fontSizeSpinner;
    private final JSpinner digitsSpinner;

    private final String title;
	private final String key;

    private final LabelPainter labelPainter;
}
