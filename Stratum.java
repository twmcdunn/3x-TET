import java.util.Random;

/**
 * Write a description of class Stratum here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Stratum {
    public int[][] chords;
    public int target;
    public Synth synth;
    public Random rand;
    public double amFreq, amPhase;
    public Envelope env;

    public Stratum(int[][] chords, int target, Synth synth, Random rand, Envelope env) {
        this.chords = chords;
        this.target = target;
        this.synth = synth;
        this.rand = rand;
        amFreq = 1 / (rand.nextDouble() * 20 + 10);
        amPhase = Math.PI * 2 * rand.nextDouble();
        this.env = env;
    }

    public int getTarget(double time) {
        return this.target;
    }

    public double vol(double time) {
        // double vol = (Math.sin(Math.PI * 2 * time * amFreq + amPhase) + 1) / 2.0;
        // System.out.println(time + "," + vol);
        double vol = env.getValue(time);
        if (time > 5 * 60 + 30 && time < 7 * 60) {
            double manheimCresc = (time - (5 * 60 + 30)) / (7 * 60.0 - (5 * 60 + 30));
            manheimCresc /= 2.0;// cresc halfway, then drop suddenly for foreground entrance
            vol = (1 - manheimCresc) * vol + manheimCresc;
        }
        if (time > 7 * 60 + 24 && time < 7 * 60 + 45) {
            double manheimCresc = (time - (7 * 60 + 24)) / (21.0);
            // cresc all the way
            vol = (1 - manheimCresc) * vol + manheimCresc;
        }
        if (time > 330 && time < 335) {
            double manheimCresc = (time - 330) / 5.0;
            vol = (1 - manheimCresc) * vol + manheimCresc;
        }
        if (time > 335 && time < 340) {
            double manheimCresc = (time - 335) / 5.0;
            vol = (1 - manheimCresc) * vol + manheimCresc;
        }
        if (time > 3 * 60 + 55 && time < 4 * 60) {
            double t = time - (3 * 60 + 55);
            double resid = 0.25 * (t) / 5.0;
            if (false && 5 - t < 0.05) {
                resid *= (5 - t) / 0.05;
            }
            Piece.volScalarExternal = 0.1 + resid;
        } else if (time > 5 * 60 + 50 && time < 5 * 60 + 53.25) {
            double t = time - (5 * 60 + 50);

            double manheimCresc = (t) / 3.25;
            vol = (1 - manheimCresc) * vol + manheimCresc;

            double resid = 0.15 * (t) / 3.25;
            if (false && 3.25 - t < 0.05) {
                resid *= (3.25 - t) / 0.05;
            }
            Piece.volScalarExternal = 0.1 + resid;
        } else if (time > 2 * 60 + 45 && time < 2 * 60 + 52) {
            double t = time - (2 * 60 + 45);
            double resid = 0.25 * (t) / 7.0;
            if (false && 7 - t < 0.05) {
                resid *= (5 - t) / 0.05;
            }
            Piece.volScalarExternal = 0.1 + resid;
        } else {
            Piece.volScalarExternal = 0.1;
        }
        return vol * Piece.volScalarExternal;
    }

}
