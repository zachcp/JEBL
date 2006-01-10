package jebl.evolution.align;

import jebl.evolution.align.scores.Scores;
import jebl.evolution.align.scores.ScoresFactory;
import jebl.evolution.sequences.SequenceTester;

// Global alignment using the Needleman-Wunsch algorithm (affine gap costs)
// uses linear space.

public class NeedlemanWunschLinearSpaceAffine extends AlignLinearSpaceAffine {
    private float finalScore;
    static final int RECURSION_THRESHOLD = 6;

    public NeedlemanWunschLinearSpaceAffine(Scores sub, float d, float e) {
        super(sub, d, e);
        quadraticAlign = new NeedlemanWunschAffine(sub, d, e);
    }

    private NeedlemanWunschAffine quadraticAlign;//we use the quadratic
    //  algorithm to calculate the alignment as the base recursion case.
    String[] matchResult;
    private static final int TYPE_ANY = 0;
    private static final int TYPE_X = 1;
    private static final int TYPE_Y= 2;
    private int C[][][];
    private int Ctype[][][];
    private int previousm= 0, previousn= 0;

    private AlignmentProgressListener progress;
    private long totalProgress;
    private long currentProgress;
    private boolean cancelled;
    

    private boolean addProgress(long value) {
        currentProgress += value;
        if(progress !=null) {
            double fraction =((double) currentProgress)/totalProgress;
            cancelled =progress.setProgress(fraction);
        }
        return cancelled;
    }


    public void doAlignment(String sq1, String sq2) {
        doAlignment(sq1,sq2, null);
    }


    public void doAlignment(String sq1, String sq2, AlignmentProgressListener progress) {
        this.progress =progress;


        sq1 = strip(sq1);
        sq2 = strip(sq2);

        prepareAlignment (sq1,sq2);
        //we initialise the following arrays here rather than in prepareAlignment
        //so that we do not have to create them again during recursion.
        if (n> previousn||m> previousm) {
            F = new float[3][2][m + 1];
            C = new int [3] [ 2] [m+1];
            Ctype = new int [3] [3] [m+1];
            previousn=n;
            previousm=m;
        }
        totalProgress =n*m*2;
        currentProgress= 0;
        cancelled = false;
        matchResult =doAlignment (sq1,sq2, TYPE_ANY, TYPE_ANY);
    }

    public String[] getMatch() {
        return matchResult;
    }

    public void prepareAlignment(String sq1, String sq2) {
        this.seq1 = sq1;
        this.seq2 = sq2;

        this.n = sq1.length();
        this.m = sq2.length();

    }

    private String[] doAlignment(String sq1, String sq2, int startType, int endType) {
        prepareAlignment(sq1, sq2);

        char[] s1 = sq1.toCharArray();
        char[] s2 = sq2.toCharArray();

        int n = this.n, m = this.m;
        float[][] score = sub.score;
        float[][] M = F[0], Ix = F[1], Iy = F[2];
        int[][] cm= C [0], cx= C [1],cy= C [2];
        int[][] cmtype= Ctype [0], cxtype= Ctype [1],cytype= Ctype [2];
        float val;
        float s, a, b, c;
        int u=n/2;

        if (n< RECURSION_THRESHOLD || m<RECURSION_THRESHOLD) {
//            NeedlemanWunschAffine align = new NeedlemanWunschAffine (sub,d,e);
            quadraticAlign.setScores(sub);
            quadraticAlign.setGapOpen(d);
            quadraticAlign.setGapExtend(e);
//            align.doAlignment(sq1,sq2);
            quadraticAlign.doAlignment(sq1,sq2, startType, endType);
            if(addProgress(n*m)) return null;
            setScore (quadraticAlign.getScore());
            return quadraticAlign.getMatch();
        }

        //all that the remainder of this function does is to calculate the midpoint
        //(u,v) that the optimal alignment passes through, along with the
        //type of extension applied at the midpoint, in case it is the gap extension,
        //in which case the recursion functions need to know that.

        for (int j = 1; j <= m; j++) {
            float base = d;
            if (startType ==TYPE_Y)
                base =e;//if startType IS TYPE_Y then we were already in a
            // gap, so we can use gap extension penalty rather than gap starting penalty
            Iy[0] [j] = - base -e*(j- 1);
            Ix [0] [j] = M [0] [j] = Float.NEGATIVE_INFINITY;
        }
        Ix[0][0] = Iy[0][0] = M[0][0]= 0;
        swap01(Ix);swap01(Iy); swap01 (M);
        swap01(cm); swap01 (cx); swap01 (cy);
        swap01(cmtype); swap01 (cxtype); swap01 (cytype);

        for (int i=1; i<=n; i++) {
            swap01(Ix);swap01(Iy); swap01 (M);
            swap01(cm); swap01 (cx); swap01 (cy);
            swap01(cmtype); swap01 (cxtype); swap01 (cytype);

            M[1][0] = Float.NEGATIVE_INFINITY;
            Iy[1][0] = Float.NEGATIVE_INFINITY;
            float base =d;
            if (startType == TYPE_X) base =e;
            Ix[1][0] = - base -e*(i- 1);
            for (int j=1; j<=m; j++) {
                if(cancelled) return null;
                char c1= s1[i - 1];
                char c2= s2[j - 1];
                
                s = score[c1][c2];
                a = M[0][j-1]+s;
                b = Ix[0][j-1]+s;
                c = Iy[0][j-1]+s;

                val = M[1][j] = max(a, b, c);
                if (i == u) {
                    cm[1][j]=j;
                    cmtype[1][j]= TYPE_ANY;
                }
                else if (i > u) {
                    if (val == a) {
                        cm[1][j]= cm[0][j-1];
                        cmtype[1][j]= cmtype[0][j-1];
                    }
                    else if (val == b) {
                        cm[1][j]= cx[0][j-1];
                        cmtype[1][j]= cxtype[0][j-1];
                    }
                    else if (val == c) {
                        cm[1][j]= cy[0][j-1];
                        cmtype[1][j]=cytype[0][j-1];
                    } else {
                        throw new Error("NWAffine 1");
                    }
                }

                a = M[0][j]-d;
                b = Ix[0][j]-e;
                c = Iy[0][j]-d;
                val = Ix[1][j] = max(a, b, c);
                if (i == u) {
                    cx[1][j] = j;
                    cxtype[1][j] = TYPE_X;
                } else if (i > u) {
                    if (val == a) {
                        cx[1][j] = cm[0][j];
                        cxtype[1][j] = cmtype[0][j];
                    } else if (val == b) {
                        cx[1][j] = cx[0][j];
                        cxtype[1][j] = cxtype[0][j];
                    } else if (val == c) {
                        cx[1][j] = cy[0][j];
                        cxtype[1][j] = cytype[0][j];
                    } else {
                        throw new Error("NWAffine 2");
                    }
                }


                a = M[1][j-1]-d;
                b = Iy[1][j-1]-e;
                c = Ix[1][j-1]-d;
                val = Iy[1][j] = max(a, b, c);
                if (i == u) {
                    cy[1][j] = j;
                    cytype[1][j] = TYPE_ANY;
                } else if (i > u) {
                    if (val == a) {
                        cy[1][j] = cm[1][j - 1];
                        cytype[1][j] = cmtype[1][j - 1];
                    } else if (val == b) {
                        cy[1][j] = cy[1][j - 1];
                        cytype[1][j] = cytype[1][j - 1];
                    } else if (val == c) {
                        cy[1][j] = cx[1][j - 1];
                        cytype[1][j] = cxtype[1][j - 1];
                    } else {
                        throw new Error("NWAffine 3");
                    }
                }

            }
            if(addProgress(m)) return null;
        }
        // Find maximal score
        int bestk = 0;
        for (int k = 1; k < 3; k++) {
            if (F[k][1][m]> F [ bestk][1][m])
                bestk=k;
        }

        //if the alignment must end with a particular type, force that type to be selected:
        if (endType == TYPE_X)
            bestk= 1;
        if (endType == TYPE_Y)
            bestk= 2;

        int v= C [ bestk][1][m];
        int vtype = Ctype [ bestk][1][m];
        float finalScore = F [ bestk][1][m];


        String sequence1a = sq1.substring(0, u);
        String sequence2a = sq2.substring(0, v);
        String[] match1= doAlignment(sequence1a,sequence2a,startType,vtype );
        if(cancelled) return null;
        float match1Score= getScore();
        String sequence1b = sq1.substring(u);
        String sequence2b = sq2.substring(v);
        String[] match2= doAlignment(sequence1b,sequence2b,vtype, endType );
        if (cancelled) return null;
        float match2Score = getScore();
        float combineScore = match1Score + match2Score;

        //I thought the following would be a good idea to test how well it is working,
        //  but in practice
        // the floatingpoint error builds up to exceed small amounts
        // even on my test caseof only a few hundred characters
        /*
        if (Math.abs(combineScore - finalScore)> 0.0001f) {
            System.out.println (sequence1a+ "+" + sequence1b);
            System.out.println (sequence2a+ "+" + sequence2b);

            String message = "final score doesn't match (" + match1Score + "+" + match2Score + "=" + (match2Score + match1Score)+ "!=" + finalScore + ")";
            System.out.println (message);
            System.out.println (match1[0]);
            System.out.println (match1[1]);
            System.out.println (match2[0]);
            System.out.println (match2[1]);
            NeedlemanWunschAffine align = new NeedlemanWunschAffine(sub, d, e);
            align.doAlignment(sq1, sq2, startType, endType);
            System.out.println ("score from quadratic algorithm =" +align.getScore());
            String[] match3=align.getMatch();
            System.out.println(match3[0]);
            System.out.println(match1[0] + match2[0]);
            System.out.println(match3[1]);
            System.out.println(match1[1]+match2[1]);


            throw new Error (message);
        }
        */
        setScore (finalScore);

//        setScore (combineScore);
        return new String[] {match1[0]+ match2[0], match1[1]+ match2[1]};
    }

    @Override public float getScore() {
        return finalScore;
    }

    private void setScore(float finalScore) {
        this.finalScore=finalScore;
    }

    public static void main(String[] arguments) {
        Scores scores = ScoresFactory.generateScores("Blosum45");
        String sequence1 = SequenceTester.getTestSequence1(arguments);
        String sequence2 = SequenceTester.getTestSequence2(arguments);
        float e = 0.1f;
        float d = 1.0f;

        System.out.println ("aligning sequence of length " + sequence1.length()+ " with sequence of length " + sequence2.length());



        long start;
        long end;
        final int repeat = 2;

        start = System.currentTimeMillis();
        String[] results2= null, results= null, results3= null;
        NeedlemanWunschAffine align2= null;
        NeedlemanWunschLinearSpaceAffine align= null;
        OldNeedlemanWunschAffine align3= null;

        for(int i= 0; i<repeat;i++) {
            align2 = new NeedlemanWunschAffine(scores, d, e);
            align2.doAlignment(sequence1, sequence2);

            results2 = align2.getMatch() ;
        }
        end = System.currentTimeMillis();
        System.out.println("quadratic space took " + (end - start) + " milliseconds");

        start = System.currentTimeMillis();
        for (int i = 0; i < repeat; i++) {
            align = new NeedlemanWunschLinearSpaceAffine(scores, d, e);
            align.doAlignment(sequence1, sequence2);
            results = align.getMatch();
        }
        end = System.currentTimeMillis();
        System.out.println("linear space took " + (end - start) + " milliseconds");

        start = System.currentTimeMillis();
        for (int i = 0; i < repeat; i++) {
            align3 = new OldNeedlemanWunschAffine(scores, d, e);
            align3.doAlignment(sequence1, sequence2);

            results3 = align3.getMatch() ;
        }
        end = System.currentTimeMillis();
        System.out.println("old quadratic space took " + (end - start) + " milliseconds");
        System.out.println(results[0]);
        System.out.println(results3[0]);
//        System.out.println(results3[0]);
        System.out.println(results[1]);
        System.out.println (results3[1]);
//        System.out.println (results3[1]);
        float score = align.getScore();
        float score3= align3.getScore() ;
        if (results[0].equals (results3[0])&& results[1].equals ( results3[1]))
            System.out.println ("results are the same");
        else
            System.out.println ("results are different");
        System.out.println ("score 1 =" + score);
        System.out.println ("score 2 =" + score3);

        SmithWatermanLinearSpaceAffine align4= null;
        start = System.currentTimeMillis();
        for (int i = 0; i < repeat; i++) {
            align4 = new SmithWatermanLinearSpaceAffine(scores, d, e);
            align4.doAlignment(sequence1, sequence2);
            align4.getMatch();
        }
        end = System.currentTimeMillis();
        System.out.println("SmithWaterman linear space affine space took " + (end - start) + " milliseconds");
    }
}


