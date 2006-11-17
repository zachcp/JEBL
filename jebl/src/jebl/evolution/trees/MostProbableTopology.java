package jebl.evolution.trees;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.util.FixedBitSet;

import java.util.*;

/**
 * Work in progress. may contain serious bugs
 * @author Joseph Heled
 * @version $Id$
 */
public class MostProbableTopology {
    final List<Tree> trees;
    final private List<Taxon> taxa;
    final String consAttributeName = GreedyUnrootedConsensusTreeBuilder.DEFAULT_SUPPORT_ATTRIBUTE_NAME;

    public MostProbableTopology(Collection<Tree> trees) {
       this.trees = new ArrayList<Tree>(trees);
       taxa = new ArrayList<Taxon>(trees.iterator().next().getTaxa());
    }

    private class TopInfo {
        int count;
        int representativeIndex;

        public TopInfo(int nTree) {
            representativeIndex = nTree;
            count = 1;
        }
    }


    private interface Callback {
        void visit(Node n, FixedBitSet tipSet);
    }

    private class TreeInfo {

        class NodeInfo {
            Node n;
            double hSum;
            int count;

            public NodeInfo(Node n) {
                this.n = n;
                hSum = 0.0;
                count = 0;
            }
        }
        final public SimpleRootedTree t;
        final public Map<FixedBitSet, NodeInfo> m;


        TreeInfo(SimpleRootedTree t) {
            this.t = t;
            m = new HashMap<FixedBitSet, NodeInfo>();
            traverse(new Callback() {
                public void visit(Node n, FixedBitSet tipSet) {
                     m.put(tipSet, new NodeInfo(n));
                }
            } );
        }

        public void finalizeHeights() {
            traverse(new Callback() {
                public void visit(Node n, FixedBitSet tipSet) {
                    final NodeInfo info = m.get(tipSet);
                    assert(info != null && info.count > 0);
                    double h = info.hSum / info.count;
                    for( Node c : t.getChildren(info.n) ) {
                        final double ch = t.getHeight(c);
                        if( ch > h ) {
                            h = ch;
                        }
                    }
                    t.setHeight(info.n, h);

                    info.n.setAttribute(consAttributeName,
                            (double)info.count / trees.size());
                }
            } );
        }

       private void traverse(Callback call) {
           traverse(call, t.getRootNode());
       }

        private FixedBitSet traverse(Callback call, Node n) {
           final FixedBitSet tipSet = new FixedBitSet(taxa.size());

            if( t.isExternal(n) ) {
                tipSet.set( taxa.indexOf( t.getTaxon(n) ) );

            } else {

                for( Node c : t.getChildren(n) ) {
                    final FixedBitSet cTips = traverse(call, c);
                    tipSet.union(cTips);
                }
            }
            call.visit(n, tipSet);
            return tipSet;
        }
    }

    public List<Tree> get(final int max, final double threshold) {
        final int nTrees = trees.size();
        Map<String, TopInfo> m = new HashMap<String, TopInfo>(nTrees);
        for(int nTree = 0; nTree < nTrees; ++ nTree) {
            final Tree t = trees.get(nTree);
            if( t instanceof RootedTree ) {
                final RootedTree r = (RootedTree) t;
                final String rep = standardTop(r, r.getRootNode());
                TopInfo e = m.get(rep);
                if( e == null ) {
                    m.put(rep, new TopInfo(nTree));
                } else {
                   e.count += 1;
                }
            }
        }
        // sorts support from largest to smallest
        final Comparator<Map.Entry<String, TopInfo>> comparator = new Comparator<Map.Entry<String, TopInfo>>() {
            public int compare(Map.Entry<String, TopInfo> o1, Map.Entry<String, TopInfo> o2) {
                return o2.getValue().count - o1.getValue().count;
            }
        };

        // add everything to queue
        PriorityQueue<Map.Entry<String, TopInfo>> queue =
                new PriorityQueue<Map.Entry<String, TopInfo>>(m.size(), comparator);

        for (Map.Entry<String, TopInfo> s : m.entrySet()) {
            queue.add(s);
        }

        List<TreeInfo> candidates = new ArrayList<TreeInfo>();

        int th = threshold > 0 ? (int)(threshold * nTrees) : 1;

        while (queue.peek() != null) {
            Map.Entry<String, TopInfo> e = queue.poll();
            final MostProbableTopology.TopInfo info = e.getValue();
            if( info.count >= th ) {
                // make a copy
                final SimpleRootedTree rt = new SimpleRootedTree((RootedTree) trees.get(info.representativeIndex));

                rt.setAttribute(consAttributeName, (double)info.count / nTrees);
                candidates.add(new TreeInfo(rt));
            }

            if( max > 0 && candidates.size() >= max ) {
                break;
            }
        }


        for(int nTree = 0; nTree < nTrees; ++ nTree) {
            final RootedTree t = (RootedTree)trees.get(nTree);
            add(t, t.getRootNode(), candidates);
        }

        List<Tree> results = new ArrayList<Tree>();
        for(int nTree = 0; nTree < candidates.size(); ++ nTree) {
            final TreeInfo info = candidates.get(nTree);
            info.finalizeHeights();

            results.add(info.t);
        }
        return results;

    }

    private FixedBitSet add(RootedTree t, Node n, List<TreeInfo> results) {
        final double height = t.getHeight(n);
        final FixedBitSet tipSet = new FixedBitSet(taxa.size());
        if( t.isExternal(n) ) {
            tipSet.set( taxa.indexOf( t.getTaxon(n) ) );
        }  else {
            for( Node c : t.getChildren(n) ) {
                final FixedBitSet cTips = add(t, c, results);
                tipSet.union(cTips);
            }
        }
        for( TreeInfo ti : results ) {
            final TreeInfo.NodeInfo ni = ti.m.get(tipSet);

            if( ni != null ) {
                ni.count ++;
                ni.hSum += height;
            }
        }
        return tipSet;
    }

    private String standardTop(RootedTree t, Node n) {
        if( t.isExternal(n) ) {
            return Integer.toString(taxa.indexOf( t.getTaxon(n) ));
        }
        final List<Node> dec = t.getChildren(n);
        final String[] strings = new String[dec.size()];
        for(int k = 0; k < dec.size(); ++k) {
          strings[k] = standardTop(t, dec.get(k));
        }
        final List<String> list = Arrays.asList(strings);
        Collections.sort(list);
        StringBuilder sb = new StringBuilder();

        for( String s : strings ) {           
            sb.append(sb.length() == 0  ? '(' : ",");
            sb.append(s);
        }
        sb.append(')');
        return sb.toString();
    }
}
