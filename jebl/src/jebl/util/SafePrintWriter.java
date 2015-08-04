package jebl.util;

import java.io.*;
import java.util.Formatter;
import java.util.IllegalFormatException;
import java.util.Locale;

/**
 *
 * This class is a duplication of {@link PrintWriter}, but all print, append, format etc methods throw {@link IOException}.
 * That is because all these methods of {@link PrintWriter} do not throw any exceptions, instead they silently suppress the IOException and set a flag that is only available if you call PrintWriter.checkError().
 * This means anything that uses PrintWriter on file or network IO and doesn't call checkError() can have data loss without Geneious or the end user being aware of it.
 * Therefore this class is created as replacement of {@link PrintWriter} where we need to print something to file or network IO
 *
 * @author Frank Lee
 *         Created on 28/07/15 2:45 PM
 * @since This was introduced for Geneious 9.0.0
 */
public class SafePrintWriter extends Writer {

    /**
     * The underlying character-output stream of this
     * <code>SafePrintWriter</code>.
     *
     */
    protected Writer out;

    private boolean autoFlush = false;
    private Formatter formatter;
    private PrintStream psOut = null;

    /**
     * Line separator string.  This is the value of the line.separator
     * property at the moment that the stream was created.
     */
    private String lineSeparator;

    /**
     * Creates a new SafePrintWriter, without automatic line flushing.
     *
     * @param out A character-output stream
     */
    public SafePrintWriter(Writer out) {
        this(out, false);
    }

    /**
     * Creates a new SafePrintWriter.
     *
     * @param out       A character-output stream
     * @param autoFlush A boolean; if true, the <tt>println</tt>,
     *                  <tt>printf</tt>, or <tt>format</tt> methods will
     *                  flush the output buffer
     */
    public SafePrintWriter(Writer out,
                           boolean autoFlush) {
        super(out);
        this.out = out;
        this.autoFlush = autoFlush;
        lineSeparator = java.security.AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction("line.separator"));
    }

    /**
     * Creates a new SafePrintWriter, without automatic line flushing, from an
     * existing OutputStream.  This convenience constructor creates the
     * necessary intermediate OutputStreamWriter, which will convert characters
     * into bytes using the default character encoding.
     *
     * @param out An output stream
     * @see OutputStreamWriter#OutputStreamWriter(OutputStream)
     */
    public SafePrintWriter(OutputStream out) {
        this(out, false);
    }

    /**
     * Creates a new SafePrintWriter from an existing OutputStream.  This
     * convenience constructor creates the necessary intermediate
     * OutputStreamWriter, which will convert characters into bytes using the
     * default character encoding.
     *
     * @param out       An output stream
     * @param autoFlush A boolean; if true, the <tt>println</tt>,
     *                  <tt>printf</tt>, or <tt>format</tt> methods will
     *                  flush the output buffer
     * @see OutputStreamWriter#OutputStreamWriter(OutputStream)
     */
    public SafePrintWriter(OutputStream out, boolean autoFlush) {
        this(new BufferedWriter(new OutputStreamWriter(out)), autoFlush);

        // save print stream for error propagation
        if (out instanceof PrintStream) {
            psOut = (PrintStream) out;
        }
    }

    /**
     * Creates a new SafePrintWriter, without automatic line flushing, with the
     * specified file name.  This convenience constructor creates the necessary
     * intermediate {@link OutputStreamWriter OutputStreamWriter},
     * which will encode characters using the {@linkplain
     * java.nio.charset.Charset#defaultCharset() default charset} for this
     * instance of the Java virtual machine.
     *
     * @param fileName The name of the file to use as the destination of this writer.
     *                 If the file exists then it will be truncated to zero size;
     *                 otherwise, a new file will be created.  The output will be
     *                 written to the file and is buffered.
     * @throws FileNotFoundException If the given string does not denote an existing, writable
     *                               regular file and a new regular file of that name cannot be
     *                               created, or if some other error occurs while opening or
     *                               creating the file
     * @throws SecurityException     If a security manager is present and {@link
     *                               SecurityManager#checkWrite checkWrite(fileName)} denies write
     *                               access to the file
     */
    public SafePrintWriter(String fileName) throws FileNotFoundException {
        this(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName))),
                false);
    }

    /**
     * Creates a new SafePrintWriter, without automatic line flushing, with the
     * specified file name and charset.  This convenience constructor creates
     * the necessary intermediate {@link OutputStreamWriter
     * OutputStreamWriter}, which will encode characters using the provided
     * charset.
     *
     * @param fileName The name of the file to use as the destination of this writer.
     *                 If the file exists then it will be truncated to zero size;
     *                 otherwise, a new file will be created.  The output will be
     *                 written to the file and is buffered.
     * @param csn      The name of a supported {@linkplain java.nio.charset.Charset
     *                 charset}
     * @throws FileNotFoundException        If the given string does not denote an existing, writable
     *                                      regular file and a new regular file of that name cannot be
     *                                      created, or if some other error occurs while opening or
     *                                      creating the file
     * @throws SecurityException            If a security manager is present and {@link
     *                                      SecurityManager#checkWrite checkWrite(fileName)} denies write
     *                                      access to the file
     * @throws UnsupportedEncodingException If the named charset is not supported
     */
    public SafePrintWriter(String fileName, String csn)
            throws FileNotFoundException, UnsupportedEncodingException {
        this(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), csn)),
                false);
    }

    /**
     * Creates a new SafePrintWriter, without automatic line flushing, with the
     * specified file.  This convenience constructor creates the necessary
     * intermediate {@link OutputStreamWriter OutputStreamWriter},
     * which will encode characters using the {@linkplain
     * java.nio.charset.Charset#defaultCharset() default charset} for this
     * instance of the Java virtual machine.
     *
     * @param file The file to use as the destination of this writer.  If the file
     *             exists then it will be truncated to zero size; otherwise, a new
     *             file will be created.  The output will be written to the file
     *             and is buffered.
     * @throws FileNotFoundException If the given file object does not denote an existing, writable
     *                               regular file and a new regular file of that name cannot be
     *                               created, or if some other error occurs while opening or
     *                               creating the file
     * @throws SecurityException     If a security manager is present and {@link
     *                               SecurityManager#checkWrite checkWrite(file.getPath())}
     *                               denies write access to the file
     */
    public SafePrintWriter(File file) throws FileNotFoundException {
        this(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file))),
                false);
    }

    /**
     * Creates a new SafePrintWriter, without automatic line flushing, with the
     * specified file and charset.  This convenience constructor creates the
     * necessary intermediate {@link OutputStreamWriter
     * OutputStreamWriter}, which will encode characters using the provided
     * charset.
     *
     * @param file The file to use as the destination of this writer.  If the file
     *             exists then it will be truncated to zero size; otherwise, a new
     *             file will be created.  The output will be written to the file
     *             and is buffered.
     * @param csn  The name of a supported {@linkplain java.nio.charset.Charset
     *             charset}
     * @throws FileNotFoundException        If the given file object does not denote an existing, writable
     *                                      regular file and a new regular file of that name cannot be
     *                                      created, or if some other error occurs while opening or
     *                                      creating the file
     * @throws SecurityException            If a security manager is present and {@link
     *                                      SecurityManager#checkWrite checkWrite(file.getPath())}
     *                                      denies write access to the file
     * @throws UnsupportedEncodingException If the named charset is not supported
     */
    public SafePrintWriter(File file, String csn)
            throws FileNotFoundException, UnsupportedEncodingException {
        this(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), csn)),
                false);
    }

    /**
     * Checks to make sure that the stream has not been closed
     */
    private void ensureOpen() throws IOException {
        if (out == null)
            throw new IOException("Stream closed");
    }

    /**
     * Flushes the stream.
     */
    public void flush() throws IOException {
        synchronized (lock) {
            ensureOpen();
            out.flush();
        }
    }

    /**
     * Closes the stream and releases any system resources associated
     * with it. Closing a previously closed stream has no effect.
     */
    public void close() throws IOException {
        synchronized (lock) {
            if (out == null)
                return;
            out.close();
            out = null;
        }
    }


    /*
     * Exception-catching, synchronized output operations,
     * which also implement the write() methods of Writer
     */

    /**
     * Writes a single character.
     *
     * @param c int specifying a character to be written.
     */
    public void write(int c) throws IOException {
        try {
            synchronized (lock) {
                ensureOpen();
                out.write(c);
            }
        } catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Writes A Portion of an array of characters.
     *
     * @param buf Array of characters
     * @param off Offset from which to start writing characters
     * @param len Number of characters to write
     */
    public void write(char buf[], int off, int len) throws IOException {
        try {
            synchronized (lock) {
                ensureOpen();
                out.write(buf, off, len);
            }
        } catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Writes an array of characters.  This method cannot be inherited from the
     * Writer class because it must suppress I/O exceptions.
     *
     * @param buf Array of characters to be written
     */
    public void write(char buf[]) throws IOException {
        write(buf, 0, buf.length);
    }

    /**
     * Writes a portion of a string.
     *
     * @param s   A String
     * @param off Offset from which to start writing characters
     * @param len Number of characters to write
     */
    public void write(String s, int off, int len) throws IOException {
        try {
            synchronized (lock) {
                ensureOpen();
                out.write(s, off, len);
            }
        } catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Writes a string.  This method cannot be inherited from the Writer class
     * because it must suppress I/O exceptions.
     *
     * @param s String to be written
     */
    public void write(String s) throws IOException {
        write(s, 0, s.length());
    }

    private void newLine() throws IOException {
        try {
            synchronized (lock) {
                ensureOpen();
                out.write(lineSeparator);
                if (autoFlush)
                    out.flush();
            }
        } catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        }
    }

    /* Methods that do not terminate lines */

    /**
     * Prints a boolean value.  The string produced by <code>{@link
     * String#valueOf(boolean)}</code> is translated into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the <code>{@link
     * #write(int)}</code> method.
     *
     * @param b The <code>boolean</code> to be printed
     */
    public void print(boolean b) throws IOException {
        write(b ? "true" : "false");
    }

    /**
     * Prints a character.  The character is translated into one or more bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the <code>{@link
     * #write(int)}</code> method.
     *
     * @param c The <code>char</code> to be printed
     */
    public void print(char c) throws IOException {
        write(c);
    }

    /**
     * Prints an integer.  The string produced by <code>{@link
     * String#valueOf(int)}</code> is translated into bytes according
     * to the platform's default character encoding, and these bytes are
     * written in exactly the manner of the <code>{@link #write(int)}</code>
     * method.
     *
     * @param i The <code>int</code> to be printed
     * @see Integer#toString(int)
     */
    public void print(int i) throws IOException {
        write(String.valueOf(i));
    }

    /**
     * Prints a long integer.  The string produced by <code>{@link
     * String#valueOf(long)}</code> is translated into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the <code>{@link #write(int)}</code>
     * method.
     *
     * @param l The <code>long</code> to be printed
     * @see Long#toString(long)
     */
    public void print(long l) throws IOException {
        write(String.valueOf(l));
    }

    /**
     * Prints a floating-point number.  The string produced by <code>{@link
     * String#valueOf(float)}</code> is translated into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the <code>{@link #write(int)}</code>
     * method.
     *
     * @param f The <code>float</code> to be printed
     * @see Float#toString(float)
     */
    public void print(float f) throws IOException {
        write(String.valueOf(f));
    }

    /**
     * Prints a double-precision floating-point number.  The string produced by
     * <code>{@link String#valueOf(double)}</code> is translated into
     * bytes according to the platform's default character encoding, and these
     * bytes are written in exactly the manner of the <code>{@link
     * #write(int)}</code> method.
     *
     * @param d The <code>double</code> to be printed
     * @see Double#toString(double)
     */
    public void print(double d) throws IOException {
        write(String.valueOf(d));
    }

    /**
     * Prints an array of characters.  The characters are converted into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the <code>{@link #write(int)}</code>
     * method.
     *
     * @param s The array of chars to be printed
     * @throws NullPointerException If <code>s</code> is <code>null</code>
     */
    public void print(char s[]) throws IOException {
        write(s);
    }

    /**
     * Prints a string.  If the argument is <code>null</code> then the string
     * <code>"null"</code> is printed.  Otherwise, the string's characters are
     * converted into bytes according to the platform's default character
     * encoding, and these bytes are written in exactly the manner of the
     * <code>{@link #write(int)}</code> method.
     *
     * @param s The <code>String</code> to be printed
     */
    public void print(String s) throws IOException {
        if (s == null) {
            s = "null";
        }
        write(s);
    }

    /**
     * Prints an object.  The string produced by the <code>{@link
     * String#valueOf(Object)}</code> method is translated into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the <code>{@link #write(int)}</code>
     * method.
     *
     * @param obj The <code>Object</code> to be printed
     * @see Object#toString()
     */
    public void print(Object obj) throws IOException {
        write(String.valueOf(obj));
    }

    /* Methods that do terminate lines */

    /**
     * Terminates the current line by writing the line separator string.  The
     * line separator string is defined by the system property
     * <code>line.separator</code>, and is not necessarily a single newline
     * character (<code>'\n'</code>).
     */
    public void println() throws IOException {
        newLine();
    }

    /**
     * Prints a boolean value and then terminates the line.  This method behaves
     * as though it invokes <code>{@link #print(boolean)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param x the <code>boolean</code> value to be printed
     */
    public void println(boolean x) throws IOException {
        synchronized (lock) {
            print(x);
            println();
        }
    }

    /**
     * Prints a character and then terminates the line.  This method behaves as
     * though it invokes <code>{@link #print(char)}</code> and then <code>{@link
     * #println()}</code>.
     *
     * @param x the <code>char</code> value to be printed
     */
    public void println(char x) throws IOException {
        synchronized (lock) {
            print(x);
            println();
        }
    }

    /**
     * Prints an integer and then terminates the line.  This method behaves as
     * though it invokes <code>{@link #print(int)}</code> and then <code>{@link
     * #println()}</code>.
     *
     * @param x the <code>int</code> value to be printed
     */
    public void println(int x) throws IOException {
        synchronized (lock) {
            print(x);
            println();
        }
    }

    /**
     * Prints a long integer and then terminates the line.  This method behaves
     * as though it invokes <code>{@link #print(long)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param x the <code>long</code> value to be printed
     */
    public void println(long x) throws IOException {
        synchronized (lock) {
            print(x);
            println();
        }
    }

    /**
     * Prints a floating-point number and then terminates the line.  This method
     * behaves as though it invokes <code>{@link #print(float)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param x the <code>float</code> value to be printed
     */
    public void println(float x) throws IOException {
        synchronized (lock) {
            print(x);
            println();
        }
    }

    /**
     * Prints a double-precision floating-point number and then terminates the
     * line.  This method behaves as though it invokes <code>{@link
     * #print(double)}</code> and then <code>{@link #println()}</code>.
     *
     * @param x the <code>double</code> value to be printed
     */
    public void println(double x) throws IOException {
        synchronized (lock) {
            print(x);
            println();
        }
    }

    /**
     * Prints an array of characters and then terminates the line.  This method
     * behaves as though it invokes <code>{@link #print(char[])}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param x the array of <code>char</code> values to be printed
     */
    public void println(char x[]) throws IOException {
        synchronized (lock) {
            print(x);
            println();
        }
    }

    /**
     * Prints a String and then terminates the line.  This method behaves as
     * though it invokes <code>{@link #print(String)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param x the <code>String</code> value to be printed
     */
    public void println(String x) throws IOException {
        synchronized (lock) {
            print(x);
            println();
        }
    }

    /**
     * Prints an Object and then terminates the line.  This method calls
     * at first String.valueOf(x) to get the printed object's string value,
     * then behaves as
     * though it invokes <code>{@link #print(String)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param x The <code>Object</code> to be printed.
     */
    public void println(Object x) throws IOException {
        String s = String.valueOf(x);
        synchronized (lock) {
            print(s);
            println();
        }
    }

    /**
     * A convenience method to write a formatted string to this writer using
     * the specified format string and arguments.  If automatic flushing is
     * enabled, calls to this method will flush the output buffer.
     * <p/>
     * <p> An invocation of this method of the form <tt>out.printf(format,
     * args)</tt> behaves in exactly the same way as the invocation
     * <p/>
     * <pre>
     *     out.format(format, args) </pre>
     *
     * @param format A format string as described in <a
     *               href="../util/Formatter.html#syntax">Format string syntax</a>.
     * @param args   Arguments referenced by the format specifiers in the format
     *               string.  If there are more arguments than format specifiers, the
     *               extra arguments are ignored.  The number of arguments is
     *               variable and may be zero.  The maximum number of arguments is
     *               limited by the maximum dimension of a Java array as defined by
     *               the <a href="http://java.sun.com/docs/books/vmspec/">Java
     *               Virtual Machine Specification</a>.  The behaviour on a
     *               <tt>null</tt> argument depends on the <a
     *               href="../util/Formatter.html#syntax">conversion</a>.
     * @return This writer
     * @throws IllegalFormatException If a format string contains an illegal syntax, a format
     *                                specifier that is incompatible with the given arguments,
     *                                insufficient arguments given the format string, or other
     *                                illegal conditions.  For specification of all possible
     *                                formatting errors, see the <a
     *                                href="../util/Formatter.html#detail">Details</a> section of the
     *                                formatter class specification.
     * @throws NullPointerException   If the <tt>format</tt> is <tt>null</tt>
     */
    public SafePrintWriter printf(String format, Object... args) throws IOException {
        return format(format, args);
    }

    /**
     * A convenience method to write a formatted string to this writer using
     * the specified format string and arguments.  If automatic flushing is
     * enabled, calls to this method will flush the output buffer.
     * <p/>
     * <p> An invocation of this method of the form <tt>out.printf(l, format,
     * args)</tt> behaves in exactly the same way as the invocation
     * <p/>
     * <pre>
     *     out.format(l, format, args) </pre>
     *
     * @param l      The {@linkplain Locale locale} to apply during
     *               formatting.  If <tt>l</tt> is <tt>null</tt> then no localization
     *               is applied.
     * @param format A format string as described in <a
     *               href="../util/Formatter.html#syntax">Format string syntax</a>.
     * @param args   Arguments referenced by the format specifiers in the format
     *               string.  If there are more arguments than format specifiers, the
     *               extra arguments are ignored.  The number of arguments is
     *               variable and may be zero.  The maximum number of arguments is
     *               limited by the maximum dimension of a Java array as defined by
     *               the <a href="http://java.sun.com/docs/books/vmspec/">Java
     *               Virtual Machine Specification</a>.  The behaviour on a
     *               <tt>null</tt> argument depends on the <a
     *               href="../util/Formatter.html#syntax">conversion</a>.
     * @return This writer
     * @throws IllegalFormatException If a format string contains an illegal syntax, a format
     *                                specifier that is incompatible with the given arguments,
     *                                insufficient arguments given the format string, or other
     *                                illegal conditions.  For specification of all possible
     *                                formatting errors, see the <a
     *                                href="../util/Formatter.html#detail">Details</a> section of the
     *                                formatter class specification.
     * @throws NullPointerException   If the <tt>format</tt> is <tt>null</tt>
     */
    public SafePrintWriter printf(Locale l, String format, Object... args) throws IOException {
        return format(l, format, args);
    }

    /**
     * Writes a formatted string to this writer using the specified format
     * string and arguments.  If automatic flushing is enabled, calls to this
     * method will flush the output buffer.
     * <p/>
     * <p> The locale always used is the one returned by {@link
     * Locale#getDefault() Locale.getDefault()}, regardless of any
     * previous invocations of other formatting methods on this object.
     *
     * @param format A format string as described in <a
     *               href="../util/Formatter.html#syntax">Format string syntax</a>.
     * @param args   Arguments referenced by the format specifiers in the format
     *               string.  If there are more arguments than format specifiers, the
     *               extra arguments are ignored.  The number of arguments is
     *               variable and may be zero.  The maximum number of arguments is
     *               limited by the maximum dimension of a Java array as defined by
     *               the <a href="http://java.sun.com/docs/books/vmspec/">Java
     *               Virtual Machine Specification</a>.  The behaviour on a
     *               <tt>null</tt> argument depends on the <a
     *               href="../util/Formatter.html#syntax">conversion</a>.
     * @return This writer
     * @throws IllegalFormatException If a format string contains an illegal syntax, a format
     *                                specifier that is incompatible with the given arguments,
     *                                insufficient arguments given the format string, or other
     *                                illegal conditions.  For specification of all possible
     *                                formatting errors, see the <a
     *                                href="../util/Formatter.html#detail">Details</a> section of the
     *                                Formatter class specification.
     * @throws NullPointerException   If the <tt>format</tt> is <tt>null</tt>
     */
    public SafePrintWriter format(String format, Object... args) throws IOException {
        try {
            synchronized (lock) {
                ensureOpen();
                if ((formatter == null)
                        || (formatter.locale() != Locale.getDefault()))
                    formatter = new Formatter(this);
                formatter.format(Locale.getDefault(), format, args);
                if (autoFlush)
                    out.flush();
            }
        } catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        }
        return this;
    }

    /**
     * Writes a formatted string to this writer using the specified format
     * string and arguments.  If automatic flushing is enabled, calls to this
     * method will flush the output buffer.
     *
     * @param l      The {@linkplain Locale locale} to apply during
     *               formatting.  If <tt>l</tt> is <tt>null</tt> then no localization
     *               is applied.
     * @param format A format string as described in <a
     *               href="../util/Formatter.html#syntax">Format string syntax</a>.
     * @param args   Arguments referenced by the format specifiers in the format
     *               string.  If there are more arguments than format specifiers, the
     *               extra arguments are ignored.  The number of arguments is
     *               variable and may be zero.  The maximum number of arguments is
     *               limited by the maximum dimension of a Java array as defined by
     *               the <a href="http://java.sun.com/docs/books/vmspec/">Java
     *               Virtual Machine Specification</a>.  The behaviour on a
     *               <tt>null</tt> argument depends on the <a
     *               href="../util/Formatter.html#syntax">conversion</a>.
     * @return This writer
     * @throws IllegalFormatException If a format string contains an illegal syntax, a format
     *                                specifier that is incompatible with the given arguments,
     *                                insufficient arguments given the format string, or other
     *                                illegal conditions.  For specification of all possible
     *                                formatting errors, see the <a
     *                                href="../util/Formatter.html#detail">Details</a> section of the
     *                                formatter class specification.
     * @throws NullPointerException   If the <tt>format</tt> is <tt>null</tt>
     */
    public SafePrintWriter format(Locale l, String format, Object... args) throws IOException {
        try {
            synchronized (lock) {
                ensureOpen();
                if ((formatter == null) || (formatter.locale() != l))
                    formatter = new Formatter(this, l);
                formatter.format(l, format, args);
                if (autoFlush)
                    out.flush();
            }
        } catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        }
        return this;
    }

    /**
     * Appends the specified character sequence to this writer.
     * <p/>
     * <p> An invocation of this method of the form <tt>out.append(csq)</tt>
     * behaves in exactly the same way as the invocation
     * <p/>
     * <pre>
     *     out.write(csq.toString()) </pre>
     *
     * <p> Depending on the specification of <tt>toString</tt> for the
     * character sequence <tt>csq</tt>, the entire sequence may not be
     * appended. For instance, invoking the <tt>toString</tt> method of a
     * character buffer will return a subsequence whose content depends upon
     * the buffer's position and limit.
     *
     * @param csq The character sequence to append.  If <tt>csq</tt> is
     *            <tt>null</tt>, then the four characters <tt>"null"</tt> are
     *            appended to this writer.
     * @return This writer
     */
    public SafePrintWriter append(CharSequence csq) throws IOException {
        if (csq == null)
            write("null");
        else
            write(csq.toString());
        return this;
    }

    /**
     * Appends a subsequence of the specified character sequence to this writer.
     * <p/>
     * <p> An invocation of this method of the form <tt>out.append(csq, start,
     * end)</tt> when <tt>csq</tt> is not <tt>null</tt>, behaves in
     * exactly the same way as the invocation
     * <p/>
     * <pre>
     *     out.write(csq.subSequence(start, end).toString()) </pre>
     *
     * @param csq   The character sequence from which a subsequence will be
     *              appended.  If <tt>csq</tt> is <tt>null</tt>, then characters
     *              will be appended as if <tt>csq</tt> contained the four
     *              characters <tt>"null"</tt>.
     * @param start The index of the first character in the subsequence
     * @param end   The index of the character following the last character in the
     *              subsequence
     * @return This writer
     * @throws IndexOutOfBoundsException If <tt>start</tt> or <tt>end</tt> are negative, <tt>start</tt>
     *                                   is greater than <tt>end</tt>, or <tt>end</tt> is greater than
     *                                   <tt>csq.length()</tt>
     */
    public SafePrintWriter append(CharSequence csq, int start, int end) throws IOException {
        CharSequence cs = (csq == null ? "null" : csq);
        write(cs.subSequence(start, end).toString());
        return this;
    }

    /**
     * Appends the specified character to this writer.
     * <p/>
     * <p> An invocation of this method of the form <tt>out.append(c)</tt>
     * behaves in exactly the same way as the invocation
     * <p/>
     * <pre>
     *     out.write(c) </pre>
     *
     * @param c The 16-bit character to append
     * @return This writer
     */
    public SafePrintWriter append(char c) throws IOException {
        write(c);
        return this;
    }
}
