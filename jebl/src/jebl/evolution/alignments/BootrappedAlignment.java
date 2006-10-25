package jebl.evolution.alignments;

import jebl.math.Random;

/**
 * Date: 15/01/2006
 * Time: 10:13:50
 *
 * @author Joseph Heled
 * @version $Id$
 *
 */
public class BootrappedAlignment extends ResampledAlignment {

    public BootrappedAlignment(Alignment srcAlignment) {
        final int nSites = srcAlignment.getSiteCount();
        int[] sites = new int[nSites];

        for(int n = 0; n < nSites; ++n) {
            sites[n] = Random.nextInt(nSites);
        }

        init(srcAlignment, sites);
    }
}
