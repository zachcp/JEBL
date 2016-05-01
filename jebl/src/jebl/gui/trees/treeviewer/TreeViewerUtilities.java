package jebl.gui.trees.treeviewer;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Steven Stones-Havas
 *          <p/>
 *          Created on 20/03/2009 8:19:25 PM
 */
public class TreeViewerUtilities {

    public static final Font DEFAULT_FONT = new Font("sansserif", Font.PLAIN, 12);
    public static String KEY_NODE_COLOR = "nodeColor";
    public static String KEY_LABEL_FONT = "labelFont";

    private static final Map<String, Font> cache = new HashMap<String, Font>();
    public static final Map<TextWidthCacheKey,Double> textWidthCache = new HashMap<TextWidthCacheKey, Double>();

    public static Font fontFromString(String fontRepresentation) {
        if (cache.containsKey(fontRepresentation)) {
            return cache.get(fontRepresentation);
        }
        String[] attributes = fontRepresentation.split(":");
        if(attributes.length != 4) {
            throw new IllegalArgumentException("The font descriptor \""+fontRepresentation+"\" is invalid or corrupt");
        }
        String family = attributes[0];
        int size = 0;
        try {
            size = Integer.parseInt(attributes[1]);
        }
        catch(NumberFormatException e) {
            throw new IllegalArgumentException("The size marker in the font descriptor \""+fontRepresentation+"\" is invalid or corrupt");
        }

        boolean bold = Boolean.parseBoolean(attributes[2]);
        boolean italic = Boolean.parseBoolean(attributes[3]);

        int style = 0;
        if(bold) {
            style += Font.BOLD;
        }
        if(italic) {
            style += Font.ITALIC;
        }

        final Font font = new Font(family, style, size);
        cache.put(fontRepresentation, font);
        return font;
    }


    public static String fontToString(Font font) {
        return font.getFamily() + ":" + font.getSize() + ":" + font.isBold() + ":" + font.isItalic();
    }

    /**
     * Gets the text width some text would be rendered with.
     * @param text the text
     * @param font the font
     * @param g This is optional, but if provided the results will be more accurate.
     * @return the width the text is rendered with
     */
    public static double getTextWidth(String text, Font font, Graphics2D g) {
        if (text.length() == 0) {
            return 0;
        }
        FontRenderContext frc = g==null?new FontRenderContext(new AffineTransform(), true, false) : g.getFontRenderContext();
        TextWidthCacheKey key=new TextWidthCacheKey(frc, font, text);
        Double value;
        synchronized (textWidthCache) {
            value = textWidthCache.get(key);
        }
        if (value != null) {
            return value;
        }
        final Rectangle2D bounds = new TextLayout(text, font, frc).getBounds();
        value = bounds.getWidth() + bounds.getX();
        synchronized (textWidthCache) {
            textWidthCache.put(key,value);
        }
        return value;
    }

    private static class TextWidthCacheKey {
        FontRenderContext fontRenderContext;
        Font font;
        String text;

        private TextWidthCacheKey(FontRenderContext fontRenderContext, Font font, String text) {
            this.fontRenderContext = fontRenderContext;
            this.font = font;
            this.text = text;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TextWidthCacheKey that = (TextWidthCacheKey) o;

            if (!font.equals(that.font)) return false;
            if (!fontRenderContext.equals(that.fontRenderContext)) return false;
            if (!text.equals(that.text)) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = fontRenderContext.hashCode();
            result = 31 * result + font.hashCode();
            result = 31 * result + text.hashCode();
            return result;
        }
    }
}
