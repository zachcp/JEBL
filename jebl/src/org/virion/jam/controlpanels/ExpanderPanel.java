package org.virion.jam.controlpanels;

import org.virion.jam.util.IconUtils;
import org.virion.jam.util.SimpleListener;
import org.virion.jam.util.SimpleListenerManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


/**
 * @author Richard
 * @version $Id$
 */
public class ExpanderPanel extends JPanel {

    static private Image out = IconUtils.getImage(ExpanderPanel.class, "images/expander_out.png");
    static private Image in = IconUtils.getImage(ExpanderPanel.class, "images/expander_in.png");
    private boolean expanded;
    private boolean paintRollover = false;

    public ExpanderPanel(boolean expanded) {
        this.expanded = expanded;
        addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                listeners.fire();
                ExpanderPanel.this.expanded = !ExpanderPanel.this.expanded;
            }
        });
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                paintRollover = true;
                repaint();
            }

            public void mouseExited(MouseEvent e) {
                paintRollover = false;
                repaint();
            }
        });
    }

    SimpleListenerManager listeners = new SimpleListenerManager();

    public void addExpansionListener(SimpleListener listener) {
        listeners.add(listener);
    }

    public void removeExpansionListener(SimpleListener listener) {
        listeners.remove(listener);
    }

    public void paint(Graphics g) {
        super.paint(g);
        if (paintRollover) {
            g.setColor(new Color(255, 255, 255, 125));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        if (expanded) {
            g.drawImage(out, 1, getHeight() / 2 - 3, this);
        } else {
            g.drawImage(in, 0, getHeight() / 2 - 3, this);
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension(9, 100);
    }
}
