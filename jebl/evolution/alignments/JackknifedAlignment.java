package jebl.evolution.alignments;

import jebl.math.Random;

/**
 * Created by IntelliJ IDEA.
 * User: joseph
 * Date: 17/01/2006
 * Time: 08:18:32
 *
 * @author Joseph Heled
 * @version $Id$
 *
 */
public class JackknifedAlignment extends ResampledAlignment {
     public JackknifedAlignment(Alignment srcAlignment, double percent) {
        final int nSites = srcAlignment.getSiteCount();
        final int nNewSites = (int)Math.ceil(nSites * percent);
        int[] sites = new int[nSites];

        for(int n = 0; n < nSites; ++n) {
            sites[n] = n;
        }

        Random.shuffle(sites);

        int[] newSites = new int[nNewSites];
        System.arraycopy(sites, 0, newSites, 0, nNewSites);
        init(srcAlignment, newSites);
    }
}
