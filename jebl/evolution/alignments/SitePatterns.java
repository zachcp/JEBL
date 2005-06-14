/*
 * SitePatterns.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package is distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.alignments;

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

}
