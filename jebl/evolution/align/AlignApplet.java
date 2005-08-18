package jebl.evolution.align;

import jebl.evolution.align.scores.*;
import jebl.evolution.align.gui.TracebackPlot;
import jebl.evolution.align.gui.FastaPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * @author Alexei Drummond
 *
 * @version $Id$
 *
 */
public class AlignApplet extends JApplet {

    String[] penalties = new String[] {
        "1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20"
    };

    Scores[] scores = new Scores[] {
            new NucleotideScores(5,-4),
            new NucleotideScores(4,-5),
            new NucleotideScores(3,-6),
            new Blosum45(),
            new Blosum50(),
            new Blosum60(),
            new Blosum62(),
            new Blosum70(),
            new Blosum80(),
            new Blosum90()
    };

    JTextArea output = new JTextArea();

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

    JTabbedPane leftTabbedPane = new JTabbedPane();
    JTabbedPane rightTabbedPane = new JTabbedPane();

    JButton alignButton = new JButton("Align");
    JComboBox sequence1Combo;
    JComboBox sequence2Combo;
    JComboBox gapCreationPenalty = new JComboBox(penalties);


//    JComboBox gapExtensionPenalty = new JComboBox(penalties);
    JComboBox scoresComboBox = new JComboBox(scores);

    JButton importButton = new JButton("Import fasta");

    TracebackPlot tracebackPlot = new TracebackPlot();

    SequencePanel sequencePanel;

    JComboBox alignmentComboBox = new JComboBox(
        new String[] {
            "Smith-Waterman",
            "Needleman-Wunsch",
            "Maximal Segment Pair",
        }
    );

    String[] names;
    String[] sequences;

    public void setNames(String[] names) {
        this.names = names;
        sequence1Combo.setModel(new DefaultComboBoxModel(names));
        sequence2Combo.setModel(new DefaultComboBoxModel(names));
        sequencePanel.repaint();
    }

    public void setSequences(String[] sequences) {
        this.sequences = sequences;
    }


    public void init() {

        names = new String[] {"seq1","seq2"};
        sequences = new String[] {"ACAGCTAGCTGACT", "ACACGACATCATCGA"};

        gapCreationPenalty.setSelectedIndex(7);

        alignmentComboBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (alignmentComboBox.getSelectedIndex() == 2) {
                    gapCreationPenalty.setEnabled(false);
 //                   gapExtensionPenalty.setEnabled(false);
                } else {
                    gapCreationPenalty.setEnabled(true);
//                    gapExtensionPenalty.setEnabled(true);

                }
            }
        });


        final FastaPanel fastaPanel = new FastaPanel(importButton);

        importButton.setAction(new AbstractAction("Import fasta") {

            public void actionPerformed(ActionEvent ae) {
                try {
                    fastaPanel.importSequences();
                    setNames(fastaPanel.getNames());
                    setSequences(fastaPanel.getSequences());
                    fastaPanel.setText("Import successful.");
                }
                catch (Exception e) {
                    StringWriter writer = new StringWriter();
                    PrintWriter pw = new PrintWriter(writer);
                    e.printStackTrace(pw);
                    pw.flush();
                    fastaPanel.setText("Error importing:\n" + writer.toString());
                }

            }
        });

        sequencePanel = new SequencePanel();
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(fastaPanel,BorderLayout.NORTH);

        leftTabbedPane.add("Align", new AlignPanel(names));
        leftTabbedPane.add("Sequences", sequencePanel);
        leftTabbedPane.add("Import", panel);

        rightTabbedPane.add("Output", output);
        rightTabbedPane.add("Plot", tracebackPlot);

        splitPane.setTopComponent(leftTabbedPane);
        splitPane.setBottomComponent(rightTabbedPane);
        splitPane.setDividerLocation(300);

        getContentPane().add(splitPane, BorderLayout.CENTER);

        alignButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                int i = sequence1Combo.getSelectedIndex();
                int j = sequence2Combo.getSelectedIndex();

                String seq1 = sequences[i];
                String seq2 = sequences[j];

                int d = Integer.parseInt((String)gapCreationPenalty.getSelectedItem());
   //             int e = Integer.parseInt((String)gapExtensionPenalty.getSelectedItem());

                Scores scores = (Scores)scoresComboBox.getSelectedItem();

                Align align;
                int selected = alignmentComboBox.getSelectedIndex();
                switch (selected) {
                    case 0:
                        align = new SmithWatermanLinearSpace(scores,d);
                        break;
                    case 1:
                        align = new NeedlemanWunschLinearSpace(scores,d);
                        break;
                    default:
                        align = new MaximalSegmentPair(scores); break;
                }




                StringBuffer message = new StringBuffer();
                message.append("Aligning " + names[i] + " and " + names[j] + " [");
                message.append(alignmentComboBox.getSelectedItem());
                message.append(", ");
                message.append("S=").append(scoresComboBox.getSelectedItem());
                if (selected < 2) {
                    message.append(", ");
                    message.append("d=").append(d);
//                    message.append(", ");
//                    message.append("e=").append(e);
                }
                message.append("]");

                align.doAlignment(seq1, seq2);

                align.doMatch(
                    new Output() {
                        public void print(String s) { output.append(s); }
                        public void println(String s) { output.append(s+"\n"); }
                        public void println() { output.append("\n"); }
                   },
                   message.toString());

                align.traceback(tracebackPlot);
            }
        });
    }

    class SequencePanel extends JPanel {

        JTable jTable = new JTable();

        public SequencePanel() {

            assert names.length == sequences.length;

            jTable.setModel(new SequenceTableModel());

            JScrollPane scrollPane = new JScrollPane(jTable);

            setLayout(new BorderLayout());
            add(scrollPane,BorderLayout.CENTER);
        }

        class SequenceTableModel extends DefaultTableModel {
            public int getRowCount() { return sequences.length; }

            public int getColumnCount() { return 2; }

            public String getColumnName(int i) {
                if (i == 0) return "Names";
                else return "Sequences";
            }

            public Class getColumnClass(int i) { return String.class; }

            public boolean isCellEditable(int row, int column) { return false;  }

            public Object getValueAt(int row, int column) {
                if (column == 0) {
                    return names[row];
                }
                else {
                    return sequences[row];
                }
            }
        }
    }

    class AlignPanel extends JPanel {

        JPanel pane = new JPanel();

        public AlignPanel(String[] sequenceNames) {

            sequence1Combo = new JComboBox(sequenceNames);
            sequence2Combo = new JComboBox(sequenceNames);

            BoxLayout boxLayout = new BoxLayout(pane,BoxLayout.PAGE_AXIS);
            pane.setLayout(boxLayout);

            addComponent(new JLabel("Sequence 1", JLabel.LEFT));
            addComponent(sequence1Combo);
            addComponent(new JLabel("Sequence 2"));
            addComponent(sequence2Combo);
            pane.add(Box.createRigidArea(new Dimension(10,10)));
            addComponent(new JLabel("Scores"));
            addComponent(scoresComboBox);
            pane.add(Box.createRigidArea(new Dimension(10,10)));
            addComponent(new JLabel("Gap creation ",JLabel.LEFT));
            addComponent(gapCreationPenalty);
  //         addComponent(new JLabel("Gap extension ",JLabel.LEFT));
  //          addComponent(gapExtensionPenalty);
            pane.add(Box.createRigidArea(new Dimension(10,10)));
            addComponent(alignmentComboBox);
            pane.add(Box.createRigidArea(new Dimension(10,10)));
            addComponent(alignButton);
            pane.add(Box.createVerticalGlue());

            setLayout(new BorderLayout());
            add(pane,BorderLayout.NORTH);
        }

        private void addComponent(JComponent component) {
            component.setAlignmentX(0.0f);
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.add(component,BorderLayout.WEST);
            pane.add(panel);
        }
    }
}
