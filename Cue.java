
public abstract class Cue implements Comparable<Cue>{
    Piece piece;
    double startTime;

    public Cue initialize(Piece p, double startTime){
        piece = p;
        this.startTime = startTime;
        return this;
    }
    public int compareTo(Cue c){
        return (int)Math.rint(10 * (startTime - c.startTime));
    }
    abstract void run();
}
