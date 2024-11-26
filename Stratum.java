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
       if(time > 5 * 60 + 30 && time < 7 * 60){
        double manheimCresc = (time - (5 * 60 + 30)) / (7*60.0 - ( 5 * 60 + 30));
        manheimCresc /= 2.0;//cresc halfway, then drop suddenly for foreground entrance
        vol = (1-manheimCresc) * vol + manheimCresc;
       }
       if(time > 7 * 60 + 24 && time < 7*60 + 45){
            double manheimCresc = (time - (7 * 60 + 24)) / (21.0);
            //cresc all the way
            vol = (1-manheimCresc) * vol + manheimCresc;
       }
       if(time > 330 && time < 335){
        double manheimCresc = (time - 330)/5.0;
        vol = (1-manheimCresc) * vol + manheimCresc;
       }
       if(time > 335 && time < 340){
        double manheimCresc = (time - 335)/5.0;
        vol = (1-manheimCresc) * vol + manheimCresc;
       }
        return vol;
    }

}
