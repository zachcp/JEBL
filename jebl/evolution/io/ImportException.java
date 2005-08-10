package jebl.evolution.io;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public class ImportException extends Exception {
	public ImportException() { super(); }
	public ImportException(String message) { super(message); }

	public static class DuplicateFieldException extends ImportException {
		public DuplicateFieldException() { super(); }
		public DuplicateFieldException(String message) { super(message); }
	}

	public static class BadFormatException extends ImportException {
		public BadFormatException() { super(); }
		public BadFormatException(String message) { super(message); }
	}

	public static class UnparsableDataException extends ImportException {
		public UnparsableDataException() { super(); }
		public UnparsableDataException(String message) { super(message); }
	}

	public static class MissingFieldException extends ImportException {
		public MissingFieldException() { super(); }
		public MissingFieldException(String message) { super(message); }
	}

	public static class ShortSequenceException extends ImportException {
		public ShortSequenceException() { super(); }
		public ShortSequenceException(String message) { super(message); }
	}

	public static class TooFewTaxaException extends ImportException {
		public TooFewTaxaException() { super(); }
		public TooFewTaxaException(String message) { super(message); }
	}

	public static class UnknownTaxonException extends ImportException {
		public UnknownTaxonException() { super(); }
		public UnknownTaxonException(String message) { super(message); }
	}

}
