import java.util.Random;
import java.util.ArrayList;

/**
 * Write a description of class Piece here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Piece {
    public static int[] mode5c = { 0, 1, 3, 5, 6, 8, 10, 11, 13 };// 8 = sym
    public static int[] backgroundChord = { 0, 1, 6, 7, 9, 10 };
    public static double[][] matrix1;
    // generic steps (gs) = scale steps
    // specific steps (ss) = is like half steps

    public static double c0Freq = 440 * Math.pow(2, (3 - 12 * 5) / 12.0);

    // c4 = 75 ss

    public Piece() {
        /*
         * WaveWriter ww = new WaveWriter("15TET");
         * buildMatrices();
         * 
         * writeStratum(ww, new Random(1),mode5c,1,75,90,76,new
         * double[]{1},generateMatrix(new int[]{0,2,4,7}, 0.98, 0.02, 9, true));
         * writeStratum(ww, new Random(2),mode5c,6,75 + 15 * 1,90 + 15 * 1,76 + 15 *
         * 1,new double[]{0,1},generateMatrix(new int[]{0,2,4,7}, 0.98, 0.2, 9, true));
         * writeStratum(ww, new Random(3),mode5c,11,75 + 15 * 2,90 + 15 * 2,76 + 15 *
         * 2,new double[]{0,0,1},generateMatrix(new int[]{0,2,4,7}, 0.98, 0.02, 9,
         * true));
         * writeStratum(ww, new Random(4),mode5c,1,75 + 15 * 3,90 + 15 * 3,76 + 15 *
         * 3,new double[]{0,0,0,1},generateMatrix(new int[]{0,2,4,7}, 0.98, 0.02, 9,
         * true));
         * writeStratum(ww, new Random(5),mode5c,6,75 + 15 * -1,90 + 15 * -1,76 + 15 *
         * -1,new double[]{0,0,0,0,1},generateMatrix(new int[]{0,2,4,7}, 0.98, 0.02, 9,
         * true));
         * ww.render();
         */
        parsimoniousTexture1();
        // testChordProgression();
        // onsetProbabilities(15);
    }

    public static void main(String[] args) {
        new Piece();
    }

    /*
     * Modes of limited trans in 21 TET
     * 0 1 5 7 8 12 14 15 19 Contains [0,5,12] [7,14,19]
     * 0 1 3 7 8 10 14 15 17 Contains [3,8,15] [10,17,1]
     */
    /*
     * Modes of limited trans in 21 TET
     * a: 0 1 5 7 8 12 14 15 19 Contains [0,5,12] [7,14,19]
     * b: 0 1 3 7 8 10 14 15 17 Contains [3,8,15] [10,17,1]
     * 
     * 
     * a':0 1 2 5 7 8 9 12 14 15 16 19
     * 
     * b':0 1 3 4 7 8 10 11 14 15 17 18
     * b''0 1 2 3 7 8 9 10 14 15 16 17
     * 
     * 
     * a+b:0 1 3 5 7 8 10 12 14 15 17 19 (maximumly even)
     * contains maximumly even: [0,3,7,12,15] [14,19,1,5,10]
     * 0 2 4 7 9 0 3 5 7 10
     * [3,8,12,15,0] [10,14,17,1,5]
     * 0 3 5 7 10 0 2 4 7 9
     */

    public void testChordProgression() {
        int[][] chords = new int[][] {

                { 0, 5, 12, 8,// 2
                },
                { 7, 14, 19, 1,// 1
                },
                { 3, 8, 15, 0
                },
                { 10, 17, 1, 7// 14 and 7 are both ok
                }
        };

        // this is a good 21 TET progression
        // it's based on 'second order' maximal eveness
        // it may be that this princple appeals to me more
        // than limited transpositions
        chords = new int[][] { { 0, 3, 7, 12, 15 }, { 14, 19, 1, 5, 10 }, { 3, 8, 12, 15, 0 }, { 10, 14, 17, 1, 5 } };

        WaveWriter ww = new WaveWriter("chordProg");
        Synth synth = new SampleSynth(1);

        double time = 0;

        for (int n = 0; n < chords.length; n++) {
            for (int i = 0; i < chords[n].length; i++) {
                chords[n][i] = closestOct(106 - 21, chords[n][i], 21);
                synth.writeNote(ww.df, time, c0Freq * Math.pow(2, chords[n][i] / (double) 21), 0.1, new double[] { 1 });
                time += 0.1;
            }
            time += 3;
        }
        ww.render();

    }

    public void parsimoniousTexture1() {
        Sequencer seq = new Sequencer(1);

        int timeMode = 1;
        // 0 - fast even rhythms 1 - probiblistic MER based on TET

        int numOfChords = 2;// used only for rhythm in time mode 1

        Random rand = new Random(123);
        int[][] c = populateChords(seq, rand, 6 * seq.TET);
        ArrayList<int[][]> strata = new ArrayList<int[][]>();
        strata.add(c);

        double time = 0;
        WaveWriter ww = new WaveWriter("parsi");
        Synth[] synths = { new SampleSynth(0) };// new SampleSynth(0), new SampleSynth(1) };

        double[] pan = new double[] { 1 };
        ArrayList<Integer> octs = new ArrayList<Integer>();
        for (int i = 0; i < 6; i++) {
            if (i != 2)
                octs.add(new Integer(i));
        }
        while (time < 60) {
            int[][] notes = seq.getChords();
            int sNum = 0;
            for (int[][] chords : strata) {
                realizeChords(chords, notes, time, synths[sNum % synths.length], ww, rand, pan, seq.TET, timeMode);
                sNum++;
            }
            switch (timeMode) {
                case 0:
                    time += 8 * 1 / 10.0;
                    break;
                case 1:
                    time += numOfChords * seq.TET * 1 / 10.0;
                    break;
            }
            double probOfNewVoice = 0;
            switch (timeMode) {
                case 0:
                    probOfNewVoice = 0.1;
                    break;
                case 1:
                    probOfNewVoice = 0.0;
                    for (int i = 0; i < 3; i++)
                        strata.add(populateChords(seq, rand, 4 * seq.TET + (rand.nextInt(3) + 2) * seq.TET));
                    break;
            }
            if (octs.size() > 0 && rand.nextDouble() < probOfNewVoice) {
                int ind = (int) (rand.nextDouble() * octs.size());
                strata.add(populateChords(seq, rand, 4 * seq.TET + octs.get(ind) * seq.TET));
                octs.remove(ind);
            }
        }
        System.out.println(seq.myGame);

        ww.render();
    }

    public int[][] populateChords(Sequencer seq, Random rand, int target) {
        int[][] chords = new int[seq.myGame.getLastBoard().size()][seq.myGame.getLastBoard().get(0).notes().size()];
        for (int i = 0; i < chords.length; i++) {
            ArrayList<Integer> notes = seq.myGame.getLastBoard().get(i).notes();
            for (int n = 0; n < chords[i].length; n++) {
                int ind = (int) (rand.nextDouble() * notes.size());
                chords[i][n] = closestOct(target, notes.get(ind), seq.TET);
                notes.remove(ind);
            }
        }
        return chords;
    }

    public void realizeChords(int[][] chords, int[][] notes, double time, Synth synth, WaveWriter ww, Random rand,
            double[] pan, int tet, int timeMode) {

        switch (timeMode) {
            case 0:
                for (int n = 0; n < chords.length; n++) {
                    for (int i = 0; i < chords[n].length; i++) {
                        synth.writeNote(ww.df, time, c0Freq * Math.pow(2, chords[n][i] / (double) tet), 0.01, pan);
                        time += 1 / 10.0;
                    }
                }
                advanceChord(chords, notes, tet );
                break;
            case 1:
                int density = 10;
                double[] probDist = onsetProbabilities(tet);
                boolean[] onset = new boolean[probDist.length];
                for (int i = 0; i < density; i++) {
                    double rnd = rand.nextDouble();
                    double tot = 0;
                    int ind = 0;
                    for (ind = 0; ind < probDist.length && tot <= rnd; ind++) {
                        tot += probDist[i];
                    }
                    onset[ind] = true;
                }
                for(int ind = 0; ind < onset.length; ind++){
                    synth.writeNote(ww.df, time + (ind / 10.0),
                            c0Freq * Math.pow(2, chords[0][9] / (double) tet), 0.01, pan);
                }
                break;
        }

    }

    public void advanceChord(int[][] chords, int[][] notes, int tet) {
        // progress to next chord
        for (int n = 0; n < chords.length; n++) {
            boolean[] notesAreContained = new boolean[chords[n].length];
            int chordReplaceIndex = -1;
            for (int i = 0; i < chords[n].length; i++)
                notesAreContained[i] = false;
            for (int i = 0; i < chords[n].length; i++) {
                boolean chordNoteIsContained = false;
                for (int j = 0; j < notes[n].length; j++) {
                    if (notes[n][j] == chords[n][i] % tet) {
                        chordNoteIsContained = true;
                        notesAreContained[j] = true;
                        break;
                    }
                }
                if (!chordNoteIsContained) {
                    // chords[n][i] = -1;
                    chordReplaceIndex = i;
                    break;
                }
            }
            if (chordReplaceIndex == -1)
                continue;// why would the chord be identical?
            for (int j = 0; j < notesAreContained.length; j++) {
                if (!notesAreContained[j]) {
                    chords[n][chordReplaceIndex] = closestOct(chords[n][chordReplaceIndex], notes[n][j], tet);
                    break;
                }
            }
        }
    }

    public int closestOct(int target, int pc, int tet) {
        int oct = (int) Math.rint((target - pc) / (double) tet);

        return oct * tet + pc;
    }

    public double[] onsetProbabilities(int tet) {
        double divisor = (tet / 2) + 1;
        ArrayList<Integer> timeLine = new ArrayList<Integer>();
        double t = 0;
        while ((int) t < tet) {
            timeLine.add((int) t);
            t += tet / divisor;
        }

        divisor = timeLine.size() / 2;
        ArrayList<Integer> onsets = new ArrayList<Integer>();
        t = 0;
        while ((int) t < timeLine.size()) {// stores indexes of TIMELINE, not of beat
            onsets.add((int) t);
            t += timeLine.size() / divisor;
        }

        int timeLineOnlys = timeLine.size() - onsets.size();
        int numOfOnsets = onsets.size();
        int notTimeLines = tet - timeLine.size();
        double probTLOnly = 1 / (timeLineOnlys + 2 * numOfOnsets + notTimeLines / 2.0);
        double probOnSets = 2 * probTLOnly;
        double probNotTLs = probTLOnly / 2.0;

        double[] onsetProbs = new double[tet];
        double tot = 0;
        for (int i = 0; i < onsetProbs.length; i++) {
            boolean isTL = false;
            for (int n : timeLine)
                if (n == i) {
                    isTL = true;
                    break;
                }

            boolean isOS = false;
            for (int n : onsets)
                if (timeLine.get(n) == i) {// retreaves indexes of TIMELINE
                    isOS = true;
                    break;
                }
            if (isOS)
                onsetProbs[i] = probOnSets;
            else if (isTL)
                onsetProbs[i] = probTLOnly;
            else
                onsetProbs[i] = probNotTLs;
            tot += onsetProbs[i];
        }

        return onsetProbs;
    }
}
