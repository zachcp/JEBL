package jebl.gui.trees.treeviewer_dev.painters;

import org.virion.jam.components.RealNumberField;
import org.virion.jam.controlpalettes.Controller;
import org.virion.jam.controlpalettes.ControllerSettings;
import org.virion.jam.controlpalettes.AbstractController;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.text.NumberFormat;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class ScaleBarPainterController extends AbstractController {

	public ScaleBarPainterController(final ScaleBarPainter scaleBarPainter) {
		this.scaleBarPainter = scaleBarPainter;

		optionsPanel = new OptionsPanel();

		titleCheckBox = new JCheckBox(getTitle());

		titleCheckBox.setSelected(scaleBarPainter.isVisible());

		scaleRangeText = new RealNumberField(0.0, Double.MAX_VALUE);
		scaleRangeText.setValue(scaleBarPainter.getScaleRange());

		scaleRangeText.addChangeListener(new ChangeListener() {
		    public void stateChanged(ChangeEvent changeEvent) {
		        Double value = scaleRangeText.getValue();
		        if (value != null) {
		            scaleBarPainter.setScaleRange(value);
		        }
		    }
		});
		final JLabel label1 = optionsPanel.addComponentWithLabel("Scale Range:", scaleRangeText, true);

		Font font = scaleBarPainter.getFont();
		fontSizeSpinner = new JSpinner(new SpinnerNumberModel(font.getSize(), 0.01, 48, 1));

		final JLabel label2 = optionsPanel.addComponentWithLabel("Font Size:", fontSizeSpinner);

		fontSizeSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				final float size = ((Double) fontSizeSpinner.getValue()).floatValue();
				Font font = scaleBarPainter.getFont().deriveFont(size);
				scaleBarPainter.setFont(font);
			}
		});

		NumberFormat format = this.scaleBarPainter.getNumberFormat();
		int digits = format.getMaximumFractionDigits();
		digitsSpinner = new JSpinner(new SpinnerNumberModel(digits, 2, 14, 1));

		final JLabel label3 = optionsPanel.addComponentWithLabel("Significant Digits:", digitsSpinner);

		digitsSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				final int digits = (Integer)digitsSpinner.getValue();
				NumberFormat format = scaleBarPainter.getNumberFormat();
				format.setMaximumFractionDigits(digits);
				scaleBarPainter.setNumberFormat(format);
			}
		});

		lineWeightSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.01, 48.0, 1.0));

		lineWeightSpinner.addChangeListener(new ChangeListener() {
		    public void stateChanged(ChangeEvent changeEvent) {
			    float weight = ((Double) lineWeightSpinner.getValue()).floatValue();
		        scaleBarPainter.setScaleBarStroke(new BasicStroke(weight, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		    }
		});
		final JLabel label4 = optionsPanel.addComponentWithLabel("Line Weight:", lineWeightSpinner);

		final boolean isSelected = titleCheckBox.isSelected();
		label1.setEnabled(isSelected);
		scaleRangeText.setEnabled(isSelected);
		label2.setEnabled(isSelected);
		fontSizeSpinner.setEnabled(isSelected);
		label3.setEnabled(isSelected);
		digitsSpinner.setEnabled(isSelected);
		label4.setEnabled(isSelected);
		lineWeightSpinner.setEnabled(isSelected);

		titleCheckBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				final boolean isSelected = titleCheckBox.isSelected();

				label1.setEnabled(isSelected);
				scaleRangeText.setEnabled(isSelected);
				label2.setEnabled(isSelected);
				fontSizeSpinner.setEnabled(isSelected);
				label3.setEnabled(isSelected);
				digitsSpinner.setEnabled(isSelected);
				label4.setEnabled(isSelected);
				lineWeightSpinner.setEnabled(isSelected);

				scaleBarPainter.setVisible(isSelected);
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

	private final RealNumberField scaleRangeText;
	private final JSpinner fontSizeSpinner;
	private final JSpinner digitsSpinner;
	private final JSpinner lineWeightSpinner;

	public String getTitle() {
		return "Scale Bar";
	}

	private final ScaleBarPainter scaleBarPainter;
}
