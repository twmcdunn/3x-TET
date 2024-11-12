import java.util.ArrayList;
import java.util.Random;
/**
 *keeps track of which chord each stratum is
 *drawing from
 */
public class ChordTracker
{
    public ArrayList<ArrayList<double[]>> data;
    public int numOfChords;
    public static double randomness = 0.1;
    public Random rand;
    public ChordTracker(int strata, int chords)
    {
        data = new ArrayList<ArrayList<double[]>>();
        for(int i = 0; i < strata; i++)
            data.add(new ArrayList<double[]>());
        numOfChords = chords;
        rand = new Random(1);
    }

    public void addNote(int stratum, double time, int chord){
        data.get(stratum).add(new double[]{time, chord});
    }

    public double[] probDist(double time){
        double[] probDist = new double[numOfChords];
        boolean empty = true;
        for(ArrayList<double[]> d: data)
            if(d.size() > 0){
                empty = false;
                break;
            }
        if(empty){
            for(int i = 0; i < numOfChords; i++)
                probDist[i] = 1 / (double)numOfChords;
            return probDist;
        }
        int[] chords = new int[data.size()];
        for(int strat = 0; strat < data.size(); strat++){
            if(data.get(strat).size() == 0){
                chords[strat] = -1;
                continue;
            }
            int c = -1;
            int n  = 0;
            double t = -1;
            do{
                c = (int)data.get(strat).get(n)[1];
                t = data.get(strat).get(n)[0];
                n++;
            }while(t < time && n < data.get(strat).size());
            chords[strat] = c;
        }

        double tot = 0;
        for(int num: chords)
            if(num != -1){
                probDist[num]++;
                tot++;
            }
        probDist[rand.nextInt(probDist.length)]+= randomness;
        tot+= randomness;
        for(int i = 0; i < probDist.length; i++)
            probDist[i] /= tot;
        return probDist;
    }
}
