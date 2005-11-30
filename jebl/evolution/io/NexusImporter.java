/*
 * NexusImporter.java
 *
 * (c) 2002-2005 JEBL development team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.io;

import jebl.evolution.alignments.Alignment;
import jebl.evolution.alignments.BasicAlignment;
import jebl.evolution.graphs.Node;
import jebl.evolution.sequences.*;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.SimpleRootedTree;
import jebl.evolution.trees.Tree;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.distances.DistanceMatrix;
import jebl.evolution.distances.BasicDistanceMatrix;

import java.io.*;
import java.util.*;

/**
 * Class for importing NEXUS file format
 *
 * @version $Id$
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 */
public class NexusImporter implements AlignmentImporter, SequenceImporter, TreeImporter, DistanceMatrixImporter {

    public enum NexusBlock {
        UNKNOWN,
        TAXA,
        CHARACTERS,
        DATA,
        UNALIGNED,
        DISTANCES,
        TREES
    }

	// NEXUS specific ImportException classes
	public static class MissingBlockException extends ImportException {
		public MissingBlockException() { super(); }
		public MissingBlockException(String message) { super(message); }
	}

	/**
	 * Constructor
	 */
	public NexusImporter(Reader reader) {
		helper = new ImportHelper(reader);

		// ! defines a comment to be written out to a log file
		// & defines a meta comment
		helper.setCommentDelimiters('[', ']', '\0', '!', '&');
	}

	/**
	 * This function returns an integer to specify what the
	 * next block in the file is. The internal variable nextBlock is also set to this
	 * value. This should be overridden to provide support for other blocks. Once
	 * the block is read in, nextBlock is automatically set to UNKNOWN_BLOCK by
	 * findEndBlock.
	 */
	public NexusBlock findNextBlock() throws IOException
	{
		findToken("BEGIN", true);
		String blockName = helper.readToken(";");
		return findBlockName(blockName);
	}

	/**
	 * This function returns an enum class to specify what the
	 * block given by blockName is.
	 */
	public NexusBlock findBlockName(String blockName)
	{
		nextBlock = NexusBlock.valueOf(blockName.toUpperCase());
		if (nextBlock == null) {
			nextBlock = NexusBlock.UNKNOWN;
		}
		return nextBlock;
	}

	/**
	 * Parses a 'TAXA' block.
	 */
	public List<Taxon> parseTaxaBlock() throws ImportException, IOException
	{
		return readTaxaBlock();
	}

	/**
	 * Parses a 'CHARACTERS' block.
	 */
	public List<Sequence> parseCharactersBlock(List<Taxon> taxonList) throws ImportException, IOException
	{
		return readCharactersBlock(taxonList);
	}

	/**
	 * Parses a 'DATA' block.
	 */
	public List<Sequence> parseDataBlock(List<Taxon> taxonList) throws ImportException, IOException
	{
		return readDataBlock(taxonList);
	}

	/**
	 * Parses a 'TREES' block.
	 */
	public List<Tree> parseTreesBlock(List<Taxon> taxonList) throws ImportException, IOException
	{
		return readTreesBlock(taxonList);
	}

    // **************************************************************
    // SequenceImporter IMPLEMENTATION
    // **************************************************************

	/**
	 * importAlignment.
	 */
	public List<Alignment> importAlignments() throws IOException, ImportException
	{
		boolean done = false;

		List<Taxon> taxonList = null;
		List<Alignment> alignments = new ArrayList<Alignment>();

		while (!done) {
			try {

				NexusBlock block = findNextBlock();

				if (block == NexusBlock.TAXA) {

					taxonList = readTaxaBlock();

				} else if (block == NexusBlock.CHARACTERS) {

					if (taxonList == null) {
						throw new MissingBlockException("TAXA block is missing");
					}

					List<Sequence> sequences = readCharactersBlock(taxonList);
					alignments.add(new BasicAlignment(sequences));

				} else if (block == NexusBlock.DATA) {

					// A data block doesn't need a taxon block before it
					// but if one exists then it will use it.
					List<Sequence> sequences = readDataBlock(taxonList);
					alignments.add(new BasicAlignment(sequences));

				} else {
					// Ignore the block..
				}

			} catch (EOFException ex) {
				done = true;
			}
		}

		if (alignments.size() == 0) {
			throw new MissingBlockException("DATA or CHARACTERS block is missing");
		}

		return alignments;
	}

	/**
	 * importSequences.
	 */
	public List<Sequence> importSequences() throws IOException, ImportException {
		boolean done = false;

		List<Taxon> taxonList = null;
		List<Sequence> sequences = null;

		while (!done) {
			try {

				NexusBlock block = findNextBlock();

				if (block == NexusBlock.TAXA) {

					taxonList = readTaxaBlock();

				} else if (block == NexusBlock.CHARACTERS) {

					if (taxonList == null) {
						throw new MissingBlockException("TAXA block is missing");
					}

					sequences = readCharactersBlock(taxonList);

					done = true;

				} else if (block == NexusBlock.DATA) {

					// A data block doesn't need a taxon block before it
					// but if one exists then it will use it.
					sequences = readDataBlock(taxonList);

					done = true;

				} else {
					// Ignore the block..
				}

			} catch (EOFException ex) {
				done = true;
			}
		}

		if (sequences == null) {
			throw new MissingBlockException("DATA or CHARACTERS block is missing");
		}

		return sequences;
	}

    // **************************************************************
    // TreeImporter IMPLEMENTATION
    // **************************************************************

	private boolean isReadingTreesBlock = false;
	private List<Taxon> treeTaxonList = null;
	private Map<String, Taxon> translationList = Collections.emptyMap();
	private Tree nextTree = null;
	private String[] lastToken = new String[1];

	/**
	 * return whether another tree is available.
	 */
	public boolean hasTree() throws IOException, ImportException
	{
		if (!isReadingTreesBlock) {
			isReadingTreesBlock = startReadingTrees();
			translationList = readTranslationList(treeTaxonList, lastToken);
		}

		if (!isReadingTreesBlock) return false;

		if (nextTree == null) {
			nextTree = readNextTree(lastToken);
		}

		return (nextTree != null);
	}


	/**
	 * import the next tree.
	 * return the tree or null if no more trees are available
	 */
	public Tree importNextTree() throws IOException, ImportException
	{
		// call hasTree to do the hard work...
		if (!hasTree()) {
			isReadingTreesBlock = false;
			return null;
		}

		Tree tree = nextTree;
		nextTree = null;

		return tree;
	}

	public List<Tree> importTrees() throws IOException, ImportException {
		isReadingTreesBlock = false;
		if (!startReadingTrees()) {
			throw new MissingBlockException("TREES block is missing");
		}
		return readTreesBlock(treeTaxonList);
	}

	public boolean startReadingTrees() throws IOException, ImportException
	{
		boolean done = false;

		treeTaxonList = null;

		while (!done) {
			try {

				NexusBlock block = findNextBlock();

				if (block == NexusBlock.TAXA) {

					treeTaxonList = readTaxaBlock();

				} else if (block == NexusBlock.TREES) {

					return true;
				} else {
					// Ignore the block..
				}

			} catch (EOFException ex) {
				done = true;
			}
		}

		return false;
	}

    // **************************************************************
    // DistanceMatrixImporter IMPLEMENTATION
    // **************************************************************

    /**
     * importDistances.
     */
    public List<DistanceMatrix> importDistanceMatrices() throws IOException, ImportException {
        boolean done = false;

        List<Taxon> taxonList = null;
        List<DistanceMatrix> distanceMatrices = new ArrayList<DistanceMatrix>();

        while (!done) {
            try {

                NexusBlock block = findNextBlock();

                if (block == NexusBlock.TAXA) {

                    taxonList = readTaxaBlock();

                } else if (block == NexusBlock.DISTANCES) {

                    if (taxonList == null) {
                        throw new MissingBlockException("TAXA block is missing");
                    }

                    DistanceMatrix distanceMatrix = readDistancesBlock(taxonList);
                    distanceMatrices.add(distanceMatrix);

                } else {
                    // Ignore the block..
                }

            } catch (EOFException ex) {
                done = true;
            }
        }

        if (distanceMatrices == null) {
            throw new MissingBlockException("DISTANCES block is missing");
        }

        return distanceMatrices;
    }

    // **************************************************************
    // PRIVATE Methods
    // **************************************************************

	/**
	 * Finds the end of the current block.
	 */
	private void findToken(String query, boolean ignoreCase) throws IOException
	{
		String token;
		boolean found = false;

		do {

			token = helper.readToken();

			if ( (ignoreCase && token.equalsIgnoreCase(query)) || token.equals(query) ) {
				found = true;
			}
		} while (!found);
	}

	/**
	 * Finds the end of the current block.
	 */
	public void findEndBlock() throws IOException
	{
		try {
			String token;

			do {
				token = helper.readToken(";");
			} while ( !token.equalsIgnoreCase("END") && !token.equalsIgnoreCase("ENDBLOCK") );
		} catch (EOFException e) { } // Doesn't matter if the End is missing

		nextBlock = NexusBlock.UNKNOWN;
	}

	/**
	 * Reads the header information for a 'DATA', 'CHARACTERS' or 'TAXA' block.
	 */
	private void readDataBlockHeader(String tokenToLookFor, NexusBlock block) throws ImportException, IOException
	{

		boolean dim = false, ttl = false, fmt = false;
		String token;

		do {
			token = helper.readToken();

			if ( token.equalsIgnoreCase("TITLE") ) {
				if (ttl) {
					throw new ImportException.DuplicateFieldException("TITLE");
				}

				ttl = true;
			} else if ( token.equalsIgnoreCase("DIMENSIONS") ) {

				if (dim) {
					throw new ImportException.DuplicateFieldException("DIMENSIONS");
				}

				boolean nchar = (block == NexusBlock.TAXA);
				boolean ntax = (block == NexusBlock.CHARACTERS);

				do {
					String token2 = helper.readToken( "=;" );

					if (helper.getLastDelimiter() != '=') {
						throw new ImportException.BadFormatException("Unknown subcommand, '" + token2 + "', or missing '=' in DIMENSIONS command");
					}

					if ( token2.equalsIgnoreCase("NTAX") ) {

						if (block == NexusBlock.CHARACTERS) {
							throw new ImportException.BadFormatException("NTAX subcommand in CHARACTERS block");
						}

						taxonCount = helper.readInteger( ";" );
						ntax = true;

					} else if ( token2.equalsIgnoreCase("NCHAR") ) {

						if (block == NexusBlock.TAXA) {
							throw new ImportException.BadFormatException("NCHAR subcommand in TAXA block");
						}

						siteCount = helper.readInteger( ";" );
						nchar = true;

					} else {
						throw new ImportException.BadFormatException("Unknown subcommand, '" + token2 + "', in DIMENSIONS command");
					}

				} while (helper.getLastDelimiter() != ';');

				if (!ntax) {
					throw new ImportException.BadFormatException("NTAX subcommand missing from DIMENSIONS command");
				}
				if (!nchar) {
					throw new ImportException.BadFormatException("NCHAR subcommand missing from DIMENSIONS command");
				}
				dim = true;

			} else if ( token.equalsIgnoreCase("FORMAT") ) {

				if (fmt) {
					throw new ImportException.DuplicateFieldException("FORMAT");
				}

				sequenceType = null;

				do {
					String token2 = helper.readToken("=;");

					if (token2.equalsIgnoreCase("GAP")) {

						if (helper.getLastDelimiter() != '=') {
							throw new ImportException.BadFormatException("Expecting '=' after GAP subcommand in FORMAT command");
						}

						gapCharacters = helper.readToken(";");

					} else if (token2.equalsIgnoreCase("MISSING")) {

						if (helper.getLastDelimiter() != '=') {
							throw new ImportException.BadFormatException("Expecting '=' after MISSING subcommand in FORMAT command");
						}

						missingCharacters = helper.readToken(";");

					} else if (token2.equalsIgnoreCase("MATCHCHAR")) {

						if (helper.getLastDelimiter() != '=') {
							throw new ImportException.BadFormatException("Expecting '=' after MATCHCHAR subcommand in FORMAT command");
						}

						matchCharacters = helper.readToken(";");

					} else if (token2.equalsIgnoreCase("DATATYPE")) {

						if (helper.getLastDelimiter() != '=') {
							throw new ImportException.BadFormatException("Expecting '=' after DATATYPE subcommand in FORMAT command");
						}

						String token3 = helper.readToken(";");
						if (token3.equalsIgnoreCase("NUCLEOTIDE") ||
							token3.equalsIgnoreCase("DNA") ||
							token3.equalsIgnoreCase("RNA")) {

							sequenceType = SequenceType.NUCLEOTIDE;

						} else if (token3.equalsIgnoreCase("PROTEIN")) {

							sequenceType = SequenceType.AMINO_ACID;

						} else if (token3.equalsIgnoreCase("CONTINUOUS")) {

							throw new ImportException.UnparsableDataException("Continuous data cannot be parsed at present");

						}
					} else if (token2.equalsIgnoreCase("INTERLEAVE")) {
						isInterleaved = true;
					}

				} while (helper.getLastDelimiter() != ';');

				fmt = true;
			}
		} while ( !token.equalsIgnoreCase(tokenToLookFor) );

		if ( !dim ) {
			throw new ImportException.MissingFieldException("DIMENSIONS");
		}
		if ( block != NexusBlock.TAXA && sequenceType == null ) {
			throw new ImportException.MissingFieldException("DATATYPE");
		}
	}

	/**
	 * Reads sequences in a 'DATA' or 'CHARACTERS' block.
	 */
	private List<Sequence> readSequenceData(List<Taxon> taxonList) throws ImportException, IOException
	{
		int i;
		String firstSequence = null;

		List<Sequence> sequences = new ArrayList<Sequence>();

		if (isInterleaved) {

			throw new ImportException.UnparsableDataException("At present, interleaved data is not parsable");
//			boolean firstLoop = true;
//
//			int readCount = 0;
//			while (readCount < siteCount) {
//
//				n = -1;
//
//				for (i = 0; i < taxonCount; i++) {
//
//					String token = helper.readToken();
//
//					Sequence sequence;
//
//					if (firstLoop) {
//
//						Taxon taxon = Taxon.getTaxon(token);
//
//						if (taxonList != null && !taxonList.contains(taxon)) {
//							// taxon not found in taxon list...
//							// ...perhaps it is a numerical taxon reference?
//							throw new ImportException.UnknownTaxonException(token);
//						}
//
//						sequence.setTaxon(taxon);
//
//					} else {
//
//						sequence = sequences.getSequence(i);
//						Taxon taxon = sequence.getTaxon();
//						if (!taxon.getId().equals(token)) {
//							throw new ImportException.UnknownTaxonException("Unknown taxon label: expecting '" +
//								taxon.getId() + "', found '" + token + "'");
//						}
//					}
//
//					StringBuffer buffer = new StringBuffer();
//					helper.readSequenceLine(buffer, sequenceType, ";", gapCharacters, missingCharacters,
//										matchCharacters, firstSequence);
//					String seqString = buffer.toString();
//					sequence.appendSequenceString(seqString);
//					if (i == 0) {
//						firstSequence = seqString;
//					}
//
//					if (helper.getLastDelimiter() == ';') {
//						if (i < taxonCount - 1) {
//							throw new ImportException.TooFewTaxaException();
//						}
//						if (readCount + n < siteCount) {
//							throw new ImportException.ShortSequenceException(sequence.getTaxon().getId());
//						}
//					}
//
//					if (n == -1) {
//						n = seqString.length();
//					}
//
//					if (n != seqString.length()) {
//						throw new ImportException.ShortSequenceException(sequence.getTaxon().getId());
//					}
//				}
//
//				firstLoop = false;
//				readCount += n;
//
//			}
//			if (helper.getLastDelimiter() != ';') {
//				throw new ImportException.BadFormatException("Expecting ';' after sequences data");
//			}

		} else {

			for (i = 0; i < taxonCount; i++) {
				String token = helper.readToken();

				Taxon taxon = Taxon.getTaxon(token);

				if (taxonList != null && !taxonList.contains(taxon)) {
					// taxon not found in taxon list...
					// ...perhaps it is a numerical taxon reference?
					throw new ImportException.UnknownTaxonException(token);
				}

				StringBuffer buffer = new StringBuffer();
				helper.readSequence(buffer, sequenceType, ";", siteCount, gapCharacters,
								missingCharacters, matchCharacters, firstSequence);
				String seqString = buffer.toString();

				if (seqString.length() != siteCount) {
					throw new ImportException.ShortSequenceException(taxon.getName());
				}

				if (i == 0) {
					firstSequence = seqString;
				}

				if (helper.getLastDelimiter() == ';' && i < taxonCount - 1) {
					throw new ImportException.TooFewTaxaException();
				}

				Sequence sequence = new BasicSequence(sequenceType, taxon, seqString);
				sequences.add(sequence);
			}

			if (helper.getLastDelimiter() != ';') {
				throw new ImportException.BadFormatException("Expecting ';' after sequences data");
			}

		}

		return sequences;
	}


	/**
	 * Reads a 'TAXA' block.
	 */
	private List<Taxon> readTaxaBlock() throws ImportException, IOException
	{

		taxonCount = 0;

		readDataBlockHeader("TAXLABELS", NexusBlock.TAXA);

		if (taxonCount == 0) {
			throw new ImportException.MissingFieldException("NTAXA");
		}

		List<Taxon> taxa = new ArrayList<Taxon>();

		do {
			String name = helper.readToken(";");

			Taxon taxon = Taxon.getTaxon(name);
			taxa.add(taxon);
		} while (helper.getLastDelimiter() != ';');

		if (taxa.size() != taxonCount) {
			throw new ImportException.BadFormatException("Number of taxa doesn't match NTAXA field");
		}

		findEndBlock();

		return taxa;
	}

	/**
	 * Reads a 'CHARACTERS' block.
	 */
	private List<Sequence> readCharactersBlock(List<Taxon> taxonList) throws ImportException, IOException
	{

		siteCount = 0;
		sequenceType = null;

		readDataBlockHeader("MATRIX", NexusBlock.CHARACTERS);

		List<Sequence> sequences = readSequenceData(taxonList);

		findEndBlock();

		return sequences;
	}

	/**
	 * Reads a 'DATA' block.
	 */
	private List<Sequence> readDataBlock(List<Taxon> taxonList) throws ImportException, IOException
	{

		taxonCount = 0;
		siteCount = 0;
		sequenceType = null;

		readDataBlockHeader("MATRIX", NexusBlock.DATA);

		List<Sequence> sequences = readSequenceData(taxonList);

		findEndBlock();

		return sequences;
	}

    /**
     * Reads a 'DISTANCES' block.
     */
    private DistanceMatrix readDistancesBlock(List<Taxon> taxonList) throws ImportException, IOException
    {
        if (taxonList == null) {
            throw new ImportException.BadFormatException("Missing Taxa for reading distances");
        }

        Triangle triangle = Triangle.LOWER;
        boolean diagonal = false;
        boolean labels = false;

        boolean ttl = false, fmt = false;

        String token = helper.readToken();
        while ( !token.equalsIgnoreCase("MATRIX") ) {

            if ( token.equalsIgnoreCase("TITLE") ) {
                if (ttl) {
                    throw new ImportException.DuplicateFieldException("TITLE");
                }

                ttl = true;
            } else if ( token.equalsIgnoreCase("FORMAT") ) {

                if (fmt) {
                    throw new ImportException.DuplicateFieldException("FORMAT");
                }

                sequenceType = null;

                do {
                    String token2 = helper.readToken("=;");

                    if (token2.equalsIgnoreCase("TRIANGLE")) {

                        if (helper.getLastDelimiter() != '=') {
                            throw new ImportException.BadFormatException("Expecting '=' after TRIANGLE subcommand in FORMAT command");
                        }

                        String token3 = helper.readToken(";");
                        if (token3.equalsIgnoreCase("LOWER")) {
                            triangle = Triangle.LOWER;
                        } else if (token3.equalsIgnoreCase("UPPER")) {
                            triangle = Triangle.UPPER;
                        } else if (token3.equalsIgnoreCase("BOTH")) {
                            triangle = Triangle.BOTH;
                        }
                    } else if (token2.equalsIgnoreCase("DIAGONAL")) {
                        diagonal = true;
                    } else if (token2.equalsIgnoreCase("LABELS")) {
                        labels = true;
                    }

                } while (helper.getLastDelimiter() != ';');

                fmt = true;
            }
        }

        double[][] distances = new double[taxonList.size()][taxonList.size()];

        for (int i = 0; i < taxonList.size(); i++) {
            token = helper.readToken();

            Taxon taxon = Taxon.getTaxon(token);

            int index = taxonList.indexOf(taxon);

            if (index < 0) {
                // taxon not found in taxon list...
                // ...perhaps it is a numerical taxon reference?
                throw new ImportException.UnknownTaxonException(token);
            }

            if (index != i) {
                throw new ImportException.BadFormatException("The taxon labels are in a different order to those in the TAXA block");
            }

            if (triangle == Triangle.LOWER) {
                for (int j = 0; j < i + 1; j++) {
                    if (i != j) {
                        distances[i][j] = helper.readDouble();
                        distances[j][i] = distances[i][j];
                    } else {
                        if (diagonal) {
                            distances[i][j] = helper.readDouble();
                        }
                    }
                }
            } else if (triangle == Triangle.LOWER) {
                for (int j = i; j < taxonList.size(); j++) {
                    if (i != j) {
                        distances[i][j] = helper.readDouble();
                        distances[j][i] = distances[i][j];
                    } else {
                        if (diagonal) {
                            distances[i][j] = helper.readDouble();
                        }
                    }
                }
            } else {
                for (int j = 0; j < taxonList.size(); j++) {
                    if (i != j || diagonal) {
                        distances[i][j] = helper.readDouble();
                    } else {
                        distances[i][j] = 0.0;
                    }
                }
            }


            if (helper.getLastDelimiter() == ';' && i < taxonList.size() - 1) {
                throw new ImportException.TooFewTaxaException();
            }
        }

        if (helper.getLastDelimiter() != ';') {
            throw new ImportException.BadFormatException("Expecting ';' after sequences data");
        }


        findEndBlock();

        return new BasicDistanceMatrix(taxonList, distances);
    }


	/**
	 * Reads a 'TREES' block.
	 */
	private List<Tree> readTreesBlock(List<Taxon> taxonList) throws ImportException, IOException
	{
		List<Tree> trees = new ArrayList<Tree>();

		String[] lastToken = new String[1];
		translationList = readTranslationList(taxonList, lastToken);

		boolean done = false;
		do {

			Tree tree = readNextTree(lastToken);

			if (tree != null) {
				trees.add(tree);
			} else {
				done = true;
			}
		} while ( !done);

		if (trees.size() == 0) {
			throw new ImportException.BadFormatException("No trees defined in TREES block");
		}

		nextBlock = NexusBlock.UNKNOWN;

		return trees;
	}

	private Map<String, Taxon> readTranslationList(List<Taxon> taxonList, String[] lastToken) throws ImportException, IOException
	{
		Map<String, Taxon> translationList = new HashMap<String, Taxon>();

		String token = helper.readToken(";");

		if ( token.equalsIgnoreCase("TRANSLATE") ) {

			do {
				String token2 = helper.readToken(",;");

				if (helper.getLastDelimiter() == ',' || helper.getLastDelimiter() == ';') {
					throw new ImportException.BadFormatException("Missing taxon label in TRANSLATE command of TREES block");
				}

				String token3 = helper.readToken(",;");

				if (helper.getLastDelimiter() != ',' && helper.getLastDelimiter() != ';') {
					throw new ImportException.BadFormatException("Expecting ',' or ';' after taxon label in TRANSLATE command of TREES block");
				}

				Taxon taxon = Taxon.getTaxon(token3);

				if (taxonList != null && !taxonList.contains(taxon)) {
					// taxon not found in taxon list...
					// ...perhaps it is a numerical taxon reference?
					throw new ImportException.UnknownTaxonException(token3);
				}
				translationList.put(token2, taxon);

			} while (helper.getLastDelimiter() != ';');

			token = helper.readToken(";");

		} else if (taxonList != null) {
			for (Taxon taxon : taxonList) {
				translationList.put(taxon.getName(), taxon);
			}
		}

		lastToken[0] = token;

		return translationList;
	}

	private RootedTree readNextTree(String[] lastToken) throws ImportException, IOException
    {
        try {
            SimpleRootedTree tree = null;
            String token = lastToken[0];

            if ( token.equalsIgnoreCase("UTREE") || token.equalsIgnoreCase("TREE")) {

                if (helper.nextCharacter() == '*') {
                    // Star is used to specify a default tree - ignore it
                    helper.readCharacter();
                }

                String token2 = helper.readToken( "=;" );

                if (helper.getLastDelimiter() != '=') {
                    throw new ImportException.BadFormatException("Missing label for tree'" + token2 + "' or missing '=' in TREE command of TREES block");
                }

                try {

                    if (helper.nextCharacter() != '(') {
                        throw new ImportException.BadFormatException("Missing tree definition in TREE command of TREES block");
                    }

                    helper.clearLastMetaComment();

	                tree = new SimpleRootedTree();
	                readInternalNode(tree);

// At present, trees don't have ids:
//                    tree.setId(token2);

                    if (helper.getLastDelimiter() != ';') {
                        throw new ImportException.BadFormatException("Expecting ';' after tree, '" + token2 + "', TREE command of TREES block");
                    }

                } catch (EOFException e) {
                    // If we reach EOF we may as well return what we have?
                    return tree;
                }

                token = helper.readToken(";");
            } else if ( token.equalsIgnoreCase("ENDBLOCK") || token.equalsIgnoreCase("END") ) {
                return null;
            } else {
                throw new ImportException.BadFormatException("Unknown command '" + token + "' in TREES block");
            }

            //added this to escape readNextTree loop correctly -- AJD
            lastToken[0] = token;

            return tree;

        } catch (EOFException e) {
            return null;
        }
	}

	/**
	 * Reads a branch in. This could be a node or a tip (calls readNode or readTip
	 * accordingly). It then reads the branch length and SimpleNode that will
	 * point at the new node or tip.
	 */
	private Node readBranch(SimpleRootedTree tree) throws IOException, ImportException
	{
	    double length = 0.0;
	    Node branch;

        helper.clearLastMetaComment();
	    if (helper.nextCharacter() == '(') {
	        // is an internal node
	        branch = readInternalNode(tree);

	    } else {
	        // is an external node
	        branch = readExternalNode(tree);
	    }

        if (helper.getLastDelimiter() == ':') {
            length = helper.readDouble(",():;");
        }

		if (helper.getLastMetaComment() != null) {
			// There was a meta-comment which should be in the form:
			// \[&label=value[,label=value>[,/..]]\]
			String[] pairs = helper.getLastMetaComment().split("[,\\s]+");
			for (String pair : pairs) {
				String[] parts = pair.split("[=\\s]+");
				if (parts.length != 2 || parts[0].length() == 0 || parts[1].length() == 0) {
					throw new ImportException.BadFormatException("Badly formatted attribute pair: '"+pair+"'");
				}
				// Attempt to format the value as a number
				Number number = null;
				try {
					number = Integer.parseInt(parts[1]);
				} catch (NumberFormatException nfe1) {
					try {
						number = Double.parseDouble(parts[1]);
					} catch (NumberFormatException nfe2) {

					}
				}
				if (number != null) {
					branch.setAttribute(parts[0], number);
				} else {
					branch.setAttribute(parts[0], parts[1]);
				}
			}
            helper.clearLastMetaComment();
        }

	    tree.setLength(branch, length);

	    return branch;
	}

	/**
	 * Reads a node in. This could be a polytomy. Calls readBranch on each branch
	 * in the node.
	 */
	private Node readInternalNode(SimpleRootedTree tree) throws IOException, ImportException
	{
	    Set<Node> children = new HashSet<Node>();

	    // read the opening '('
	    helper.readCharacter();

	    // read the first child
	    children.add( readBranch(tree) );

	    // an internal node must have at least 2 children
	    if (helper.getLastDelimiter() != ',') {
	        throw new ImportException.BadFormatException("Missing ',' in tree");
	    }

	    // read subsequent children
	    do {
	        children.add( readBranch(tree) );

	    } while (helper.getLastDelimiter() == ',');

	    // should have had a closing ')'
	    if (helper.getLastDelimiter() != ')') {
	        throw new ImportException.BadFormatException("Missing closing ')' in tree");
	    }

	    // find the next delimiter
	    helper.readToken(":(),;");

	    return tree.createInternalNode(children);
	}

	/**
	 * Reads an external node in.
	 */
	private Node readExternalNode(SimpleRootedTree tree) throws ImportException, IOException
	{
	    String label = helper.readToken(":(),;");
		Taxon taxon = Taxon.getTaxon(label);

		if (translationList.size() > 0) {
			taxon = translationList.get(label);

			if (taxon == null) {
				// taxon not found in taxon list...
				throw new ImportException.UnknownTaxonException("Taxon in tree, '" + label + "' is unknown");
			}
		}

        return tree.createExternalNode(taxon);
	}


	// private stuff

	private NexusBlock nextBlock = null;

	private int taxonCount = 0, siteCount = 0;
	private SequenceType sequenceType = null;
	private String gapCharacters = "-";
	private String matchCharacters = ".";
	private String missingCharacters = "?";
	private boolean isInterleaved = false;

	private final ImportHelper helper;
}
