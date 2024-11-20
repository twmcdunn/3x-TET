
public abstract class Cue implements Comparable<Cue>{
    Piece piece;
    double startTime;
    public Cue(Piece p, double startTime){
        piece = p;
        this.startTime = startTime;
    }
    public int compareTo(Cue c){
        return (int)Math.rint(10 * (c.startTime - startTime));
    }
    abstract void run();
}
