package jebl.evolution.align;

import jebl.evolution.align.gui.FastaPanel;
import jebl.evolution.align.gui.TracebackPanel;
import jebl.evolution.align.gui.TracebackPlot;
import jebl.evolution.align.scores.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

/**
 * @author Alexei Drummond
 * @version $Id$
 */
public class AlignPanel extends JPanel {

    String[] penalties = new String[]{
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "25", "30", "35", "40", "45", "50"
    };

    Scores[] scores = new Scores[]{
            new NucleotideScores(10, -9),
            new NucleotideScores(5, -4),
            new Blosum45(),
            new Blosum50(),
            new Blosum60(),
            new Blosum62(),
            new Blosum70(),
            new Blosum80(),
            new Blosum90()
    };

    JTextArea output = new JTextArea();
    JScrollPane pane = new JScrollPane(output);

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

    JTabbedPane leftTabbedPane = new JTabbedPane();
    JTabbedPane rightTabbedPane = new JTabbedPane();

    JButton alignButton = new JButton("Align");
    JComboBox sequence1Combo;
    JComboBox sequence2Combo;
    JComboBox gapCreationPenalty = new JComboBox(penalties);

    JCheckBox shuffleCheckBox = new JCheckBox("shuffle?", false);

//    JComboBox gapExtensionPenalty = new JComboBox(penalties);
    JComboBox scoresComboBox = new JComboBox(scores);

    JButton importButton = new JButton("Import fasta");

    TracebackPlot tracebackPlot = new TracebackPlot();

    SequencePanel sequencePanel;

    JComboBox alignmentComboBox = new JComboBox(
            new String[]{
                    "Smith-Waterman",
//            "Overlap",
                    "Needleman-Wunsch",
                    "Maximal Segment Pair",
            }
    );

    String[] names = new String[]{"A", "B"};
    String[] sequences = new String[]{"ACGTAGCTACG", "GCTAGCTAGCTG"};
    URL[] urls;

    public AlignPanel(URL[] urls) {

        this.urls = urls;
        setLayout(new BorderLayout());
        initializeComponents();
    }

    public void setNames(String[] names) {
        this.names = names;
        sequence1Combo.setModel(new DefaultComboBoxModel(names));
        sequence2Combo.setModel(new DefaultComboBoxModel(names));
        sequencePanel.repaint();
    }

    public void setSequences(String[] sequences) {
        this.sequences = sequences;
    }

    public void initializeComponents() {

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

        final FastaPanel fastaPanel = new FastaPanel(urls);
        fastaPanel.setAction(
                new AbstractAction("Import fasta") {

                    public void actionPerformed(ActionEvent ae) {
                        try {
                            fastaPanel.importSequences();
                            setNames(fastaPanel.getNames());
                            setSequences(fastaPanel.getSequences());
                        }
                        catch (Exception e) {
                            StringWriter writer = new StringWriter();
                            PrintWriter pw = new PrintWriter(writer);
                            e.printStackTrace(pw);
                            pw.flush();
                        }

                    }
                });


        sequencePanel = new SequencePanel();
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(fastaPanel, BorderLayout.NORTH);

        leftTabbedPane.add("Align", new AlignOptionPanel(names));
        leftTabbedPane.add("Sequences", sequencePanel);
        leftTabbedPane.add("Import", panel);

        TracebackPanel panel2 = new TracebackPanel(tracebackPlot);

        rightTabbedPane.add("Output", pane);
        rightTabbedPane.add("Plot", panel2);

        splitPane.setTopComponent(leftTabbedPane);
        splitPane.setBottomComponent(rightTabbedPane);
        splitPane.setDividerLocation(300);

        add(splitPane, BorderLayout.CENTER);

        alignButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                final int i = sequence1Combo.getSelectedIndex();
                final int j = sequence2Combo.getSelectedIndex();

                String seq1 = sequences[i];
                String seq2 = sequences[j];

                int d = Integer.parseInt((String) gapCreationPenalty.getSelectedItem());
                //             int e = Integer.parseInt((String)gapExtensionPenalty.getSelectedItem());

                Scores scores = (Scores) scoresComboBox.getSelectedItem();

                final Align align;
                int selected = alignmentComboBox.getSelectedIndex();
                switch (selected) {
                    case 0:
                        align = new SmithWatermanLinearSpace(scores, d);
                        break;
                    case 1:
                        align = new NeedlemanWunschLinearSpace(scores, d);
                        break;
                    default:
                        align = new MaximalSegmentPair(scores);
                        break;
                }

                StringBuilder message = new StringBuilder();
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
                            public void print(String s) {
                                output.append(s);
                            }

                            public void println(String s) {
                                output.append(s + "\n");
                            }

                            public void println() {
                                output.append("\n");
                            }
                        },
                        message.toString());

                align.traceback(tracebackPlot);

                if (shuffleCheckBox.isSelected()) {
                    // shuffle

                    final int numShuffles = 200;

                    final ProgressMonitor monitor = new ProgressMonitor(AlignPanel.this, "Shuffling", "", 0, numShuffles);

                    Runnable shuffle = new Runnable() {
                        public void run() {
                            final SequenceShuffler shuffler = new SequenceShuffler();
                            shuffler.setProgressMonitor(monitor);
                            shuffler.shuffle(align, sequences[i], sequences[j], numShuffles);
                            Runnable swingUpdate = new Runnable() {
                                public void run() {

                                    double stdev = Math.round(shuffler.getStdev() * 10) / 10;

                                    output.append(numShuffles + " shuffles: mean score=" +
                                            shuffler.getMean() + " +/- " +
                                            stdev + " range=[" + shuffler.getMin() +
                                            "," + shuffler.getMax() + "]\n\n");
                                }
                            };
                            SwingUtilities.invokeLater(swingUpdate);
                        }
                    };
                    Thread thread = new Thread(shuffle);
                    thread.start();
                } else {
                    output.append("\n");
                }


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
            add(scrollPane, BorderLayout.CENTER);
        }

        class SequenceTableModel extends DefaultTableModel {
            public int getRowCount() {
                return sequences.length;
            }

            public int getColumnCount() {
                return 2;
            }

            public String getColumnName(int i) {
                if (i == 0) return "Names";
                else return "Sequences";
            }

            public Class getColumnClass(int i) {
                return String.class;
            }

            public boolean isCellEditable(int row, int column) {
                return false;
            }

            public Object getValueAt(int row, int column) {
                if (column == 0) {
                    return names[row];
                } else {
                    return sequences[row];
                }
            }
        }
    }

    class AlignOptionPanel extends JPanel {

        JPanel pane = new JPanel();

        public AlignOptionPanel(String[] sequenceNames) {

            sequence1Combo = new JComboBox(sequenceNames);
            sequence2Combo = new JComboBox(sequenceNames);

            BoxLayout boxLayout = new BoxLayout(pane, BoxLayout.PAGE_AXIS);
            pane.setLayout(boxLayout);

            addComponent(new JLabel("Sequence 1", JLabel.LEFT));
            addComponent(sequence1Combo);
            addComponent(new JLabel("Sequence 2"));
            addComponent(sequence2Combo);
            pane.add(Box.createRigidArea(new Dimension(10, 10)));
            addComponent(new JLabel("Scores"));
            addComponent(scoresComboBox);
            pane.add(Box.createRigidArea(new Dimension(10, 10)));
            addComponent(new JLabel("Gap creation ", JLabel.LEFT));
            addComponent(gapCreationPenalty);
            //         addComponent(new JLabel("Gap extension ",JLabel.LEFT));
            //          addComponent(gapExtensionPenalty);
            pane.add(Box.createRigidArea(new Dimension(10, 10)));
            addComponent(shuffleCheckBox);
            pane.add(Box.createRigidArea(new Dimension(5, 5)));
            addComponent(alignmentComboBox);
            pane.add(Box.createRigidArea(new Dimension(5, 5)));
            addComponent(alignButton);
            pane.add(Box.createGlue());

            setLayout(new BorderLayout());
            add(pane, BorderLayout.WEST);
        }

        private void addComponent(JComponent component) {
            component.setAlignmentX(0.0f);
            pane.add(component);
        }
    }
}
