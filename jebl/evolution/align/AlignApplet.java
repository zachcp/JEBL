package jebl.evolution.align;

import javax.swing.*;
import java.awt.*;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.io.InputStream;
import java.net.URL;

/**
 * @author Alexei Drummond
 *
 * @version $Id$
 *
 */
public class AlignApplet extends JApplet {

    String[] urls;

    private String[] parse(String s) {

        if (s != null) {
            StringTokenizer tokens = new StringTokenizer(s,",");
            ArrayList tokensArray = new ArrayList();
            while (tokens.hasMoreElements()) {
                tokensArray.add(tokens.nextElement());
            }
            return (String[])tokensArray.toArray(new String[]{});
        }
        return null;
    }

    public void init() {


        getContentPane().add(new JLabel("Aligner 0.1"));
    }

    public void start() {
        String urlString = getParameter("urls");
        String[] urlStrings = parse(urlString);

        URL[] urls = new URL[urlStrings.length];
        for (int i = 0; i < urls.length; i++) {
            urls[i] = getClass().getResource(urlStrings[i]);
            System.out.println(urls[i]);
        }

        AlignPanel panel = new AlignPanel(urls);

        JFrame frame = new JFrame("Align");
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.setSize(800,600);
        frame.setVisible(true);
    }

}
