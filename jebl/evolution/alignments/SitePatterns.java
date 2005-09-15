/*
 * SitePatterns.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package is distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.alignments;

import jebl.evolution.sequences.SequenceType;
import jebl.evolution.taxa.Taxon;

import java.util.List;

/**
 * An interface representing a set of site patterns.
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public interface SitePatterns {

	List<Pattern> getSitePatterns();

	int getPatternLength();

	/**
	 * @return the list of taxa that the state values correspond to.
	 */
	List<Taxon> getTaxa();

	/**
	 * @return the data type of the states in these site patterns.
	 */
	SequenceType getSequenceType();

}
