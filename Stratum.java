import java.util.Random;
/**
 * Write a description of class Stratum here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Stratum
{   
    public int[][] chords;
    public int target;
    public Synth synth;

    public Stratum(int[][] chords, int target, Synth synth)
    {
        this.chords = chords;
        this.target = target;
        this.synth = synth;
    }

}
