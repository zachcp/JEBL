package jebl.evolution.aligners.pairwise;

class TracebackSimple extends Traceback {

    public TracebackSimple(int i, int j) {
        this.i = i; this.j = j;
    }

    public final void setTraceback(int i, int j) {
        this.i = i;
        this.j = j;
    }
}
