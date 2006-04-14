package jebl.gui.trees.treeviewer_dev.painters;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.prefs.Preferences;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public abstract class LabelPainter<T> extends AbstractPainter<T> {

	private static Preferences PREFS = Preferences.userNodeForPackage(LabelPainter.class);

	private static final String DEFAULT_FONT_NAME_PREFS_KEY = "defaultFontName";
	private static final String DEFAULT_FONT_SIZE_PREFS_KEY = "defaultFontSize";
	private static final String DEFAULT_FONT_STYLE_PREFS_KEY = "defaultFontStyle";

	private static final String DEFAULT_NUMBER_FORMATTING_PREFS_KEY = "defaultNumberFormatting";

	// The defaults if there is nothing in the preferences
	private static String DEFAULT_FONT_NAME = "sansserif";
	private static int DEFAULT_FONT_SIZE = 6;
	private static int DEFAULT_FONT_STYLE = Font.PLAIN;

	private static String DEFAULT_NUMBER_FORMATTING = "#.####";

	protected LabelPainter() {
		final String defaultFontName = PREFS.get(DEFAULT_FONT_NAME_PREFS_KEY, DEFAULT_FONT_NAME);
		final int defaultFontStyle = PREFS.getInt(DEFAULT_FONT_STYLE_PREFS_KEY, DEFAULT_FONT_STYLE);
		final int defaultFontSize = PREFS.getInt(DEFAULT_FONT_SIZE_PREFS_KEY, DEFAULT_FONT_SIZE);

		setFont(new Font(defaultFontName, defaultFontStyle, defaultFontSize));

		final String defaultNumberFormatting = PREFS.get(DEFAULT_NUMBER_FORMATTING_PREFS_KEY, DEFAULT_NUMBER_FORMATTING);

		setNumberFormat(new DecimalFormat(defaultNumberFormatting));
	}

	// Abstract

	public abstract String[] getAttributes();

	public abstract void setDisplayAttribute(String displayAttribute);

	// Getters

	public Paint getForeground() {
		return foreground;
	}

	public Paint getBackground() {
		return background;
	}

	public Paint getBorderPaint() {
		return borderPaint;
	}

	public Stroke getBorderStroke() {
		return borderStroke;
	}

	public Font getFont() {
		return font;
	}

	public NumberFormat getNumberFormat() {
		return numberFormat;
	}

	public boolean isVisible() {
	    return visible;
	}

	// Setters

	public void setBackground(Paint background) {
	    this.background = background;
	    firePainterChanged();
	}

	public void setBorder(Paint borderPaint, Stroke borderStroke) {
	    this.borderPaint = borderPaint;
	    this.borderStroke = borderStroke;
	    firePainterChanged();
	}

	public void setFont(Font font) {
		this.font = font;
	    firePainterChanged();
	}

	public void setForeground(Paint foreground) {
	    this.foreground = foreground;
	    firePainterChanged();
	}

	protected void setNumberFormat(NumberFormat numberFormat) {
		this.numberFormat = numberFormat;
	    firePainterChanged();
	}

	public void setVisible(boolean visible) {
	    this.visible = visible;
	    firePainterChanged();
	}

	private Paint foreground = Color.BLACK;
	private Paint background = null;
	private Paint borderPaint = null;
	private Stroke borderStroke = null;

	private Font font;
	private boolean visible = true;

	private NumberFormat numberFormat = null;
}
