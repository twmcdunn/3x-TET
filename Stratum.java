import java.util.Random;
/**
 * Write a description of class Stratum here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Stratum
{
    public int currentNote, lastNote,stratumNum;
    public double time;
    public Envelope myEnv;
    public static int range = 90, min = 60;
    public WaveWriter ww;
    public Synth synth;
    public ChordTracker myCt;

    public Stratum(Envelope env, WaveWriter ww, Synth s, ChordTracker ct, int stNum)
    {
        time = 0;
        myEnv = env;
        lastNote = 0;
        currentNote = (int)Math.rint(myEnv.getValue(0) * range) + min;
        stratumNum = stNum;
        synth = s;
        this.ww = ww;
        myCt = ct;
    }

    public int fixNoteViolations(int note, int high, int low,int[] mode, int trans){
        while(!noteIsInMode(mode,note,trans))
            note++;
        while(note > high){
            note--;
            while(!noteIsInMode(mode,note,trans))
                note--;
        }
        while(note < low){
            note++;
            while(!noteIsInMode(mode,note,trans))
                note++;
        }

        return note;
    }

    public boolean noteIsInMode(int[] mode, int note, int trans){
        return getScaleDegree(note,mode,trans) >= 0;
    }

    public double writeNote(double[] pan, int tansposition, int[] mode, Random random, double[][] matrix){
        int midNote = (int)Math.rint(myEnv.getValue(time) * range) + min;
        int lowestNote = midNote - 7;//75;//in ss
        int highestNote = midNote + 7;//90;//in ss
        currentNote = fixNoteViolations(currentNote, highestNote,lowestNote,mode,tansposition);
        double dirChangeProb = 0.05;
        double dur = 0.05;//9-tuplets at 120
        synth.writeNote(ww.df, time, Piece.c0Freq * Math.pow(2, currentNote / 15.0), 0.01, pan);
        boolean ascending = (currentNote > lastNote);
        int scaleDegree = getScaleDegree(currentNote, mode, tansposition);
        double[] probDist = null;
        int chordNum = -1;
        int note = -1;
        while(note > highestNote || note < lowestNote){
            boolean ascending1 = ascending;
            if(random.nextDouble() < dirChangeProb)
                ascending1 = !ascending;
            if(ascending1){
                probDist = matrix[scaleDegree * 2];
            }
            else{
                probDist = matrix[scaleDegree * 2 + 1];
            }
            double rnd = random.nextDouble();
            double[] chordProb = myCt.probDist(time);
            double sum = 0;
            chordNum = -1;

            for(int i = 0; i < chordProb.length; i++){
                sum += chordProb[i];
                if(sum > rnd){
                    chordNum = i + 1;
                    break;
                }
            }
            //chordNum = 1;
            //System.out.println(chordProb[0] + " " + chordProb[1]);
            //System.out.println(chordNum);


            for(int i = 0; i < probDist.length; i++){
                if(probDist[i] == chordNum){
                    note = matrixIndToNote(i, mode, scaleDegree, currentNote);
                    break;
                }
            }
        }
        lastNote = currentNote;
        currentNote = note;
        myCt.addNote(stratumNum, time, chordNum-1);
        time += dur;
        if(random.nextDouble() > 0.3333)
            time += dur;

        return time;
    }

    public int matrixIndToNote(int i, int[] mode, int scaleDegree, int currentNote){
        int sdIndex = mode.length / 2;

        int gi = i - sdIndex; //general interval (positive is ascending)
        boolean giIsAscending = (gi > 0);

        if(!giIsAscending)
            gi += mode.length;
        int firstPC = mode[scaleDegree];
        int secondPC = mode[(scaleDegree + gi) % mode.length];
        if(giIsAscending && secondPC < firstPC)
            secondPC += 15;
        else if(!giIsAscending && secondPC > firstPC)
            secondPC -= 15;
        int si = secondPC - firstPC;

        int note = currentNote + si;

        return note;
    }

    public static int getScaleDegree(int note, int[] mode, int trans){
        for(int i = 0; i < mode.length; i++){
            if(note % 15 == (mode[i] + trans) % 15)
                return i;
        }
        return - 1;
    }

}
