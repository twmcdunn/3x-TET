import java.util.Arrays;

public class Arpegg {
    public int note, myTet;
    public double min, max, amFreq, myTime, myDur;//measured doctaves
    public boolean ascending;
    public  Synth synth;

    public Arpegg(double startTime, double dur){
        note = 15 * 4;
        ascending = true;
        myTet = 15;
        min = 3;
        max = 8;
        amFreq = 1 / (Piece.rand.nextDouble() * 10 + 30);
        //if(synth == null)
        synth = new OmniRegisterSynth();
        myTime = startTime;
        myDur = dur;
    }

    public double vol(){
        return Piece.envs1.get(4).getValue(myTime) * (0.5 + 0.5 * (Math.sin(Math.PI * 2 * myTime * amFreq) + 1) / 2.0) * Piece.volScalarExternal;
    }

    public int getNextNote(int[] chord, int tet){//time is incase I decide to automate min and max
        double range = Piece.envs1.get(5).getValue(myTime);
        min = 5.5 - 2.5 * range;
        max = 5.5 + 2.5 * range;

        min = Math.max(min, 3 + 5 * (myTime - 10 * 60) / 165.0);

        if(myTime >= 10 * 60 + 45){
            min = 2 + 6 * Piece.envs1.get(7).getValue(myTime);
            max = 2 + 6 * Piece.envs1.get(6).getValue(myTime);
            min += 7 / 12.0;//allowed range moves opposite direct of synthesized transposition
            max += 7 / 12.0;
        }

        myTime += myDur * 1 / (1 + 9 * (Piece.envs1.get(9).getValue(myTime)));
        int[] sortedChord = new int[chord.length];
        for(int i = 0; i < chord.length; i++){
            sortedChord[i] = chord[i] % tet;
        }
        Arrays.sort(sortedChord);
        if(note / (double)myTet <= min){
            ascending = true;
        }
        if(note / (double)myTet >= max){
            ascending = false;
        }

        if(ascending){
            for(int oct = 0; oct < 10; oct++){
                for(int chordInd = 0; chordInd < sortedChord.length; chordInd++){
                    int propNote = oct * tet + sortedChord[chordInd];
                    if(propNote / (double) tet > note / (double) myTet){
                        myTet = tet;
                        note = propNote;
                        
                        return note;
                    }
                }
            }
        }
        else{
            for(int oct = 10; oct >= 0; oct--){
                for(int chordInd = sortedChord.length - 1; chordInd >= 0; chordInd--){
                    int propNote = oct * tet + sortedChord[chordInd];
                    if(propNote / (double) tet < note / (double) myTet){
                        myTet = tet;
                        note = propNote;
                        
                        return note;
                    }
                }
            }
        }
        return - 1;
    }
}
