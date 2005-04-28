/*
 * (c) 2002-2005 JEBL Development Core Team
 *
 * This package is distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.alignments;

import jebl.evolution.sequences.Sequences;

import java.util.List;

/**
 * @author rambaut
 *
 * @version $Id$
 */
public interface Alignment extends Sequences, SitePatterns {

    List getSitePatternList();
}
