package jebl.gui.utils;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.IOException;


/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class IconUtils {
    /**
     * @return a named image from file or resource bundle.
     */
    public static Image getImage(String name) {
        return getImage(IconUtils.class, name);
    }

    /**
     * @return a named image from file or resource bundle.
     */
    public static Image getImage(Class resourceClass, String name) {
        java.net.URL url = resourceClass.getResource(name);
        if (url != null) {
            return Toolkit.getDefaultToolkit().createImage(url);
        } else {
            JOptionPane.showMessageDialog(null, "Image " + name + " could not be loaded.", "Warning", JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }

    /**
     * @return a named image from file or resource bundle.
     */
    public static BufferedImage getBufferedImage(String name) {
        return getBufferedImage(IconUtils.class, name);
    }

    /**
	 * @return a named image from file or resource bundle.
	 */
	public static BufferedImage getBufferedImage(Class resourceClass, String name) {

        java.net.URL url = resourceClass.getResource(name);
	    if (url != null) {
		    try {
	            return ImageIO.read(url);
		    } catch (IOException ioe) {
                JOptionPane.showMessageDialog(null, "Image " + name + " could not be loaded.", "Warning", JOptionPane.WARNING_MESSAGE);
                return null;
		    }
	    }

		return null;
	}

    /**
     * @return a named icon from file or resource bundle.
     */
    public static Icon getIcon(String name) {
        return getIcon(IconUtils.class, name);
    }

    /**
     * @return a named icon from file or resource bundle.
     */
    public static Icon getIcon(Class resourceClass, String name) {
        Image image = getImage(resourceClass, name);
        if (image != null) {
            return new ImageIcon(image);
        } else {
            return null;
        }
    }

    /**
     * Returns a slightly brighter version of the icon.
     */
    public static Icon brighten(Icon icon) {
        BufferedImage img = getBufferedImageFromIcon(icon);
        if(img == null)
            return icon;
        BufferedImageOp op = new RescaleOp(1.25f, 0, null);
        return new ImageIcon(op.filter(img, null));
    }

    /**
     * Returns a slightly darker version of the icon.
     */
    public static Icon darken(Icon icon) {
        BufferedImage img = getBufferedImageFromIcon(icon);
        if(img == null)
            return icon;
        BufferedImageOp op = new RescaleOp(0.75f, 0, null);
        return new ImageIcon(op.filter(img, null));
    }

    /**
     * Returns a grayed version of the icon.
     */
    public static Icon gray(Icon icon) {
        BufferedImage img = getBufferedImageFromIcon(icon);
        if(img == null)
            return icon;
        return new ImageIcon(GrayFilter.createDisabledImage(img));
    }

    /**
     * Resizes an icon.
     */
    public static Icon resize(Icon icon, int width, int height) {
        Image image = getImageFromIcon(icon);
        if(image == null)
            return icon;
        image = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    /**
     * Creates an image from an icon.
     */
    public static Image getImageFromIcon(Icon icon) {
        if(icon instanceof ImageIcon) {
            return ((ImageIcon)icon).getImage();
        } else {
            return getBufferedImageFromIcon(icon);
        }
    }

    /**
     * Creates a buffered image from an icon.
     */
    public static BufferedImage getBufferedImageFromIcon(Icon icon) {
        BufferedImage buffer = new BufferedImage(
                icon.getIconWidth(), icon.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics g = buffer.getGraphics();
        icon.paintIcon(new JLabel(), g, 0,0);
        g.dispose();
        return buffer;
    }
}
