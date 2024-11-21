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
    public Random rand;
    public double amFreq, amPhase;
    public Envelope env;

    public Stratum(int[][] chords, int target, Synth synth, Random rand, Envelope env)
    {
        this.chords = chords;
        this.target = target;
        this.synth = synth;
        this.rand = rand;
        amFreq = 1 / (rand.nextDouble() * 20 + 10);
        amPhase = Math.PI * 2 * rand.nextDouble();
        this.env = env;
    }

    public int getTarget(double time){
        return this.target;
    }

    public double vol(double time){
        //double vol = (Math.sin(Math.PI * 2 * time * amFreq + amPhase) + 1) / 2.0;
       // System.out.println(time + "," + vol);
       double vol = env.getValue(time);
        return vol;
    }

}
