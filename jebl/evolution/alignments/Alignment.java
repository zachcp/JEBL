/*
 * Alignment.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.alignments;

import jebl.evolution.sequences.Sequences;

import java.util.List;

/**
 * @author rambaut
 *         Date: Apr 6, 2005
 *         Time: 5:17:38 PM
 */
public interface Alignment extends Sequences, SitePatterns {

    List getSitePatternList();
}
