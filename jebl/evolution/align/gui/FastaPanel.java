package jebl.evolution.align.gui;

import javax.swing.*;
import java.util.*;
import java.util.List;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.awt.*;

/**
 *
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public class FastaPanel extends JPanel {

    private JComboBox urlComboBox;
    private URL[] urls;
    private JButton button;
    List names, sequences;

    public FastaPanel(URL[] urls) {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));

        button = new JButton();
        button.setAlignmentX(0.0f);

        String[] names = new String[urls.length];
        for (int i = 0; i < urls.length; i++) {
            names[i] = urls[i].getFile();
            int pos = names[i].lastIndexOf(File.separatorChar);
            names[i] = names[i].substring(pos+1);
        }

        urlComboBox = new JComboBox(names);
        urlComboBox.setAlignmentX(0.0f);

        JLabel label = new JLabel("Get fasta format:");
        label.setAlignmentX(0.0f);

        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(10,10)));
        panel.add(urlComboBox);
        panel.add(Box.createRigidArea(new Dimension(10,10)));
        panel.add(button);
        panel.add(Box.createGlue());

        this.urls = urls;

        setLayout(new BorderLayout());
        add(panel,BorderLayout.WEST);
    }

    public void importSequences() {

        names = new ArrayList();
        sequences = new ArrayList();

        try {

            InputStream is = urls[urlComboBox.getSelectedIndex()].openStream();

            ImportHelper helper = new ImportHelper(new InputStreamReader(is));

            int ch = helper.read();
            while (ch != '>') {
                ch = helper.read();
            }

            do {
                String line = helper.readLine();

                StringTokenizer tokenizer = new StringTokenizer(line, " \t");
                String name = tokenizer.nextToken();

                StringBuffer seq = new StringBuffer();
                helper.readSequence(seq, ">", Integer.MAX_VALUE, "-", "?", "", null);
                ch = helper.getLastDelimiter();

                names.add(name);
                sequences.add(seq.toString());

            } while(ch == '>');

        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        catch (EOFException e) {
        }
        catch (IOException e) {
            // Show dialog box explaining the problem
        }
    }

    public String[] getNames() {

        String[] nameArray = new String[names.size()];
        for (int i = 0; i < nameArray.length; i++) {
            nameArray[i] = (String)names.get(i);
        }
        return nameArray;
    }

    public String[] getSequences() {
        return (String[])sequences.toArray(new String[]{});
    }

    public void setAction(Action action) {
        button.setAction(action);
    }

    class ImportHelper {

        /**
         * Constructor
         */
        public ImportHelper(Reader reader) {
            this.reader = new LineNumberReader(reader);
            this.commentWriter = null;
        }

        public ImportHelper(Reader reader, Writer commentWriter) {
            this.reader = new LineNumberReader(reader);
            this.commentWriter = new BufferedWriter(commentWriter);
        }

        public void setCommentDelimiters(char line) {
            hasComments = true;
            this.lineComment = line;
        }

        public void setCommentDelimiters(char start, char stop) {
            hasComments = true;
            this.startComment = start;
            this.stopComment = stop;
        }

        public void setCommentDelimiters(char start, char stop, char line) {
            hasComments = true;
            this.startComment = start;
            this.stopComment = stop;
            this.lineComment = line;
        }

        public void setCommentWriter(Writer commentWriter) {
            this.commentWriter = new BufferedWriter(commentWriter);
        }

        public int getLineNumber() {
            return reader.getLineNumber();
        }

        public int getLastDelimiter() {
            return lastDelimiter;
        }

        public char nextCharacter() throws IOException {
            if (lastChar == '\0') {
                lastChar = readCharacter();
            }
            return (char)lastChar;
        }

        public char readCharacter() throws IOException {

            skipSpace();

            char ch = read();

            while (hasComments && (ch == startComment || ch == lineComment)) {
                skipComments(ch);
                skipSpace();
                ch = read();
            }

            return ch;
        }

        public void unreadCharacter(char ch) {
            lastChar = ch;
        }

        public char next() throws IOException {
            if (lastChar == '\0') {
                lastChar = read();
            }
            return (char)lastChar;
        }

        public char read() throws IOException {
            int ch;

            if (lastChar == '\0') {
                ch = reader.read();
                if (ch == -1) {
                    throw new EOFException();
                }
            } else {
                ch = lastChar;
                lastChar = '\0';
            }

            return (char)ch;
        }

        /**
         * Reads a line, skipping over any comments.
         */
        public String readLine() throws IOException {

            StringBuffer line = new StringBuffer();

            char ch = read();

            try {

                while (ch != '\n' && ch != '\r') {

                    if (hasComments) {
                        if (ch == lineComment) {
                            skipComments(ch);
                            break;
                        }
                        if (ch == startComment) {
                            skipComments(ch);
                            ch = read();
                        }
                    }

                    line.append(ch);
                    ch = read();
                }

                // accommodate DOS line endings..
                if (ch == '\r') {
                    if (next() == '\n') read();
                }

                lastDelimiter = ch;

            } catch (EOFException e) {
                // We catch an EOF and return the line we have so far
            }

            return line.toString();
        }

        /**
         *
         * Reads sequence, skipping over any comments and filtering using sequenceType.
         * @param sequence a StringBuffer into which the sequence is put
         * @param delimiters list of characters that will stop the reading
         * @param gapCharacters list of characters that will be read as gaps
         * @param missingCharacters list of characters that will be read as missing
         * @param matchCharacters list of characters that will be read as matching the matchSequence
         * @param matchSequence the sequence string to match match characters to
         * @param maxSites maximum number of sites to read
         */
        public void readSequence(StringBuffer sequence,
                                 String delimiters, int maxSites,
                                 String gapCharacters, String missingCharacters,
                                 String matchCharacters, String matchSequence) throws IOException {

            char ch = read();

            try {
                int n = 0;

                while (n < maxSites && delimiters.indexOf(ch) == -1) {

                    if (hasComments && (ch == startComment || ch == lineComment)) {
                        skipComments(ch);
                        ch = read();
                    }

                    if (!Character.isWhitespace(ch)) {

                        if (gapCharacters.indexOf(ch) != -1) {
                            sequence.append("-");
                        } else if (missingCharacters.indexOf(ch) != -1) {
                            sequence.append("-");
                        } else if (matchCharacters.indexOf(ch) != -1) {
                            if (matchSequence == null) {
                                throw new RuntimeException("Match character in first sequences");
                            }
                            if (n >= matchSequence.length()) {
                                throw new RuntimeException("Match sequences too short");
                            }

                            sequence.append(matchSequence.charAt(n));
                        } else {
                            sequence.append(ch);
                        }
                        n++;
                    }

                    ch = read();
                }

                lastDelimiter = ch;

                if (Character.isWhitespace((char)lastDelimiter)) {
                    ch = nextCharacter();
                    if (delimiters.indexOf(ch) != -1) {
                        lastDelimiter = readCharacter();
                    }
                }

            } catch (EOFException e) {
                // We catch an EOF and return the sequences we have so far
            }
        }

        /**
         * Reads a line of sequence, skipping over any comments and filtering using sequenceType.
         * @param sequence a StringBuffer into which the sequence is put
         * @param delimiters list of characters that will stop the reading
         * @param gapCharacters list of characters that will be read as gaps
         * @param missingCharacters list of characters that will be read as missing
         * @param matchCharacters list of characters that will be read as matching the matchSequence
         * @param matchSequence the sequence string to match match characters to
         * @throws IOException
         */
        public void readSequenceLine(StringBuffer sequence,
                                     String delimiters,
                                     String gapCharacters, String missingCharacters,
                                     String matchCharacters, String matchSequence) throws IOException {

            char ch = read();

            try {
                int n = 0;

                while (ch != '\r' && ch != '\n' && delimiters.indexOf(ch) == -1) {

                    if (hasComments) {
                        if (ch == lineComment) {
                            skipComments(ch);
                            break;
                        }
                        if (ch == startComment) {
                            skipComments(ch);
                            ch = read();
                        }
                    }

                    if (ch != ' ' && ch != '\t') {
                        if (gapCharacters.indexOf(ch) != -1) {
                            sequence.append("-");
                        } else if (missingCharacters.indexOf(ch) != -1) {
                            sequence.append("-");
                        } else if (matchCharacters.indexOf(ch) != -1) {
                            if (matchSequence == null) {
                                throw new RuntimeException("Match character in first sequences");
                            }
                            if (n >= matchSequence.length()) {
                                throw new RuntimeException("Match sequences too short");
                            }

                            sequence.append(matchSequence.charAt(n));
                        } else {
                            sequence.append(ch);
                        }

                        n++;
                    }

                    ch = read();
                }

                if (ch == '\r') {
                    if (next() == '\n') read();
                }

                lastDelimiter = ch;

                if (Character.isWhitespace((char)lastDelimiter)) {
                    ch = nextCharacter();
                    if (delimiters.indexOf(ch) != -1) {
                        lastDelimiter = readCharacter();
                    }
                }

            } catch (EOFException e) {
                // We catch an EOF and return the sequences we have so far
            }
        }

        /**
         * Reads a token stopping when any whitespace or a comment is found.
         * If the token begins with a quote char then all characters will be
         * included in token until a matching quote is found (including whitespace or comments).
         */
        public String readToken() throws IOException {
            return readToken("");
        }

        /**
         * Reads a token stopping when any whitespace, a comment or when any character
         * in delimiters is found. If the token begins with a quote char
         * then all characters will be included in token until a matching
         * quote is found (including whitespace or comments).
         */
        public String readToken(String delimiters) throws IOException {
            int space = 0;
            char ch, ch2, quoteChar = '\0';
            boolean done = false, first = true, quoted = false, isSpace;

            nextCharacter();

            StringBuffer token = new StringBuffer();

            while (!done) {
                ch = read();

                try {
                    isSpace = Character.isWhitespace(ch);

                    if (quoted && ch == quoteChar) { // Found the closing quote
                        ch2 = read();

                        if (ch == ch2) {
                            // A repeated quote character so add this to the token
                            token.append(ch);
                        } else {
                            // otherwise it terminates the token

                            lastDelimiter = ' ';
                            unreadCharacter(ch2);
                            done = true;
                            quoted = false;
                        }
                    } else if (first && (ch == '\'' || ch == '"')) {
                        // if the opening character is a quote
                        // read everything up to the closing quote
                        quoted = true;
                        quoteChar = ch;
                        first = false;
                        space = 0;
                    } else if ( ch == startComment || ch == lineComment ) {
                        skipComments(ch);
                        lastDelimiter = ' ';
                        done = true;
                    } else {
                        if (quoted) {
                            // compress multiple spaces into one
                            if (isSpace) {
                                space++;
                                ch = ' ';
                            } else {
                                space = 0;
                            }

                            if (space < 2) {
                                token.append(ch);
                            }
                        } else if (isSpace) {
                            lastDelimiter = ' ';
                            done = true;
                        } else if (delimiters.indexOf(ch) != -1) {
                            done = true;
                            lastDelimiter = ch;
                        } else {
                            token.append(ch);
                            first = false;
                        }
                    }
                } catch (EOFException e) {
                    // We catch an EOF and return the token we have so far
                    done = true;
                }
            }

            if (Character.isWhitespace((char)lastDelimiter)) {
                ch = nextCharacter();
                while (Character.isWhitespace(ch)) {
                    read();
                    ch = nextCharacter();
                }

                if (delimiters.indexOf(ch) != -1) {
                    lastDelimiter = readCharacter();
                }
            }

            return token.toString();
        }

        /**
         * Skips over any comments. The opening comment delimiter is passed.
         */
        protected void skipComments(char delimiter) throws IOException {

            char ch;
            int n=1;
            boolean write = false;

            if (nextCharacter() == writeComment) {
                read();
                write = true;
            }

            if (delimiter == lineComment) {
                String line = readLine();
                if (write && commentWriter != null) {
                    commentWriter.write(line, 0, line.length());
                    commentWriter.newLine();
                }
            } else {
                do {
                    ch = read();
                    if (ch == startComment) {
                        n++;
                    } else if (ch == stopComment) {
                        if (write && commentWriter != null) {
                            commentWriter.newLine();
                        }
                        n--;
                    } else if (write && commentWriter != null) {
                        commentWriter.write(ch);
                    }
                } while (n > 0);
            }
        }

        /**
         * Skips to the end of the line. If a comment is found then this is read.
         */
        public void skipToEndOfLine() throws IOException {

            char ch;

            do {
                ch = read();
                if (hasComments) {
                    if (ch == lineComment) {
                        skipComments(ch);
                        break;
                    }
                    if (ch == startComment) {
                        skipComments(ch);
                        ch = read();
                    }
                }

            } while (ch != '\n' && ch != '\r');

            if (ch == '\r') {
                if (nextCharacter() == '\n') read();
            }
        }

        /**
         * Skips char any contiguous characters in skip. Will also skip
         * comments.
         */
        public void skipWhile(String skip) throws IOException {

            char ch;

            do {
                ch = read();
            } while ( skip.indexOf(ch) > -1 );

            unreadCharacter(ch);
        }

        /**
         * Skips over any space (plus tabs and returns) in the file. Will also skip
         * comments.
         */
        public void skipSpace() throws IOException {
            skipWhile(" \t\r\n");
        }

        /**
         * Skips over any contiguous characters in skip. Will also skip
         * comments and space.
         */
        public void skipCharacters(String skip) throws IOException {
            skipWhile(skip + " \t\r\n");
        }

        /**
         * Skips over the file until a character from delimiters is found. Returns
         * the delimiter found. Will skip comments and will ignore delimiters within
         * comments.
         */
        public char skipUntil(String skip) throws IOException {
            char ch;

            do {
                ch = readCharacter();
            } while ( skip.indexOf(ch) == -1 );

            return ch;
        }

        // Private stuff

        private LineNumberReader reader;
        private BufferedWriter commentWriter = null;

        private int lastChar = '\0';
        private int lastDelimiter = '\0';

        private boolean hasComments = false;
        private char startComment = (char)-1;
        private char stopComment = (char)-1;
        private char lineComment = (char)-1;
        private char writeComment = (char)-1;

    }
}
