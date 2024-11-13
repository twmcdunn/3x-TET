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
        //parsimoniousTexture1();
        testChordProgression();
    }

    public static void main(String[] args) {
        new Piece();
    }

    /*
     * Modes of limited trans in 21 TET
     * 0 1 5 7 8 12 14 15 19 Contains [0,5,12] [7,14,19]
     * 0 1 3 7 8 10 14 15 17 Contains [3,8,15] [10,17,1]
     */

    public void testChordProgression() {
        int[][] chords = new int[][] { 
            { 0, 5, 12, 8,//15
        }, { 7, 14, 19, 1,//1
        }, { 3, 8, 15, 0 
        }, { 10, 17, 1, 7//14 and 7 are both ok
        } };

        WaveWriter ww = new WaveWriter("chordProg");
        Synth synth = new SampleSynth(0);

        double time = 0;

        for (int n = 0; n < chords.length; n++) {
            for (int i = 0; i < chords[n].length; i++) {
                chords[n][i] = closestOct(106,chords[n][i]);
                synth.writeNote(ww.df, time, c0Freq * Math.pow(2, chords[n][i] /(double) Sequencer.TET), 0.01, new double[]{1});
            }
            time += 3;
        }
        ww.render();

    }

    public void parsimoniousTexture() {
        Sequencer seq = new Sequencer();
        int[][] chords = new int[2][4];
        for (int i = 0; i < 2; i++) {
            ArrayList<Integer> notes = seq.myGame.getLastBoard().get(i).notes();
            for (int n = 0; n < notes.size(); n++)
                chords[i][n] = closestOct(90, notes.get(n));
        }
        double time = 0;
        WaveWriter ww = new WaveWriter("parsi");
        Synth synth = new SampleSynth(0);
        double[] pan = new double[] { 1 };
        while (time < 30) {
            for (int n = 0; n < chords.length; n++)
                for (int i = 0; i < chords[n].length; i++) {
                    synth.writeNote(ww.df, time, c0Freq * Math.pow(2, chords[n][i] / 15.0), 0.01, pan);
                    time += 1 / 10.0;
                }
            int[][] notes = seq.getChords();
            for (int n = 0; n < chords.length; n++) {
                boolean[] notesAreContained = new boolean[4];
                int chordReplaceIndex = -1;
                for (int i = 0; i < 4; i++)
                    notesAreContained[i] = false;
                for (int i = 0; i < chords[n].length; i++) {
                    boolean chordNoteIsContained = false;
                    for (int j = 0; j < notes[n].length; j++) {
                        if (notes[n][j] == chords[n][i] % 15) {
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
                        chords[n][chordReplaceIndex] = closestOct(chords[n][chordReplaceIndex], notes[n][j]);
                        break;
                    }
                }
            }
        }
        System.out.println(seq.myGame);

        ww.render();
    }

    public void parsimoniousTexture1() {
        Sequencer seq = new Sequencer();

        Random rand = new Random(123);
        int[][] c = populateChords(seq, rand, 90);
        ArrayList<int[][]> strata = new ArrayList<int[][]>();
        strata.add(c);

        double time = 0;
        WaveWriter ww = new WaveWriter("parsi");
        Synth[] synths = { new SampleSynth(0), new SampleSynth(1) };

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
                realizeChords(chords, notes, time, synths[sNum % 2], ww, rand, pan);
                sNum++;
            }
            time += 8 * 1 / 10.0;
            if (octs.size() > 0 && rand.nextDouble() > 0.9) {
                int ind = (int) (rand.nextDouble() * octs.size());
                strata.add(populateChords(seq, rand, 60 + octs.get(ind) * 15));
                octs.remove(ind);
            }
        }
        System.out.println(seq.myGame);

        ww.render();
    }

    public int[][] populateChords(Sequencer seq, Random rand, int target) {
        int[][] chords = new int[2][4];
        for (int i = 0; i < 2; i++) {
            ArrayList<Integer> notes = seq.myGame.getLastBoard().get(i).notes();
            for (int n = 0; n < chords[i].length; n++) {
                int ind = (int) (rand.nextDouble() * notes.size());
                chords[i][n] = closestOct(target, notes.get(ind));
                notes.remove(ind);
            }
        }
        return chords;
    }

    public void realizeChords(int[][] chords, int[][] notes, double time, Synth synth, WaveWriter ww, Random rand,
            double[] pan) {
        for (int n = 0; n < chords.length; n++)
            for (int i = 0; i < chords[n].length; i++) {
                synth.writeNote(ww.df, time, c0Freq * Math.pow(2, chords[n][i] / 15.0), 0.01, pan);
                time += 1 / 10.0;
            }

        for (int n = 0; n < chords.length; n++) {
            boolean[] notesAreContained = new boolean[4];
            int chordReplaceIndex = -1;
            for (int i = 0; i < 4; i++)
                notesAreContained[i] = false;
            for (int i = 0; i < chords[n].length; i++) {
                boolean chordNoteIsContained = false;
                for (int j = 0; j < notes[n].length; j++) {
                    if (notes[n][j] == chords[n][i] % 15) {
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
                    chords[n][chordReplaceIndex] = closestOct(chords[n][chordReplaceIndex], notes[n][j]);
                    break;
                }
            }
        }
    }

    public int closestOct(int target, int pc) {
        int oct = (int) Math.rint((target - pc) / (double) Sequencer.TET);

        return oct * Sequencer.TET + pc;
    }

    public void piece2(ArrayList<Envelope> envs) {
        WaveWriter ww = new WaveWriter("15TET");
        double[][] m = generateAbsMatrix(new int[][] { { 0, 2, 4, 7 }, { 1, 3, 5, 6, 8 } }, 9, true);
        m = generateAbsMatrix(new int[][] { { 0, 1, 2, 3, 4, 5 } }, 6, true);

        ChordTracker ct = new ChordTracker(6, 2);
        ct = new ChordTracker(6, 1);

        Stratum[] strata = new Stratum[6];
        Synth synth = new SampleSynth(0);
        for (int i = 0; i < 6; i++) {
            strata[i] = new Stratum(envs.get(i), ww, synth, ct, i);
        }
        double t = 0;
        Random random = new Random(1);
        double[][] pan = new double[][] { { 1, 0, 0, 0, 0, 0 }, { 0, 1, 0, 0, 0, 0 }, { 0, 0, 1, 0, 0, 0 },
                { 0, 0, 0, 1, 0, 0 }, { 0, 0, 0, 0, 1, 0 }, { 0, 0, 0, 0, 0, 1 } };
        while (t < 30) {
            double min = Double.MAX_VALUE;
            Stratum oldest = null;
            int sInd = -1;
            for (int i = 0; i < 6; i++) {
                Stratum s = strata[i];
                if (s.time < min) {
                    min = s.time;
                    oldest = s;
                    sInd = i;
                }
            }
            t = min;
            // oldest.writeNote(pan[sInd], 1, mode5c, random, m);
            oldest.writeNote(pan[sInd], 1, backgroundChord, random, m);
        }
        ww.render();
    }

    public static void test() {
        WaveWriter ww = new WaveWriter("test");
        new FMSynth().writeNote(ww.df, 0, 440, 0.01, new double[] { 1 });
        ww.render();
    }

    public static void buildMatrices() {

        // middleNum in each matrix is the current note (+0 steps)
        // notes above and below are plus or minus a number of
        // scale steps.
        // rows are as long as the mode they apply to.
        // One row is for ascending, one row is for decending

        // matrix 1 is a 2 chord progression in mode 5c (T1)
        // chord in 5c T 1
        // ss: 1,4,7,12 2,6,9,11,14 gs: 0,2,4,7 1,3,5,6,8
        // 0, 1, 2, 5, 6, 8, 10, 11, 13
        /*
         * matrix1 = new double[][]{
         * //gs 5 6 7 8 0 1 2 3 4
         * { 0, 0, .10, .002, 0, .015, .883, 0, 0},//gs 0 ascending
         * { 0, 0, .883, .015, 0, .002, .10, 0, 0},//gs 0 descending
         * //gs 6 7 8 0 1 2 3 4 5
         * { 0, 0, .10, .002, 0, .015, .883, 0, 0},//gs 1 ascending
         * { 0, 0, .883, .015, 0, .002, .10, 0, 0},//gs 1 descending
         * //gs 7 8 0 1 2 3 4 5 6
         * { 0, 0, .10, .002, 0, .015, .883, 0, 0},//gs 2 ascending
         * { 0, 0, .883, .015, 0, .002, .10, 0, 0},//gs 2 descending
         * //gs 8 0 1 2 3 4 5 6 7
         * { 0, 0, .10, .002, 0, .015, .883, 0, 0},//gs 3 ascending
         * { 0, 0, .883, .015, 0, .002, .10, 0, 0},//gs 3 descending
         * //gs 0 1 2 3 4 5 6 7 8
         * { 0, 0, .10, .002, 0, .008, .007, .883, 0},//gs 4 ascending
         * { 0, 0, .883, .015, 0, .001, .001, .1, 0},//gs 4 descending
         * //gs 1 2 3 4 5 6 7 8 0
         * { 0, 0, .10, .002, 0, .883, .015, 0, 0},//gs 5 ascending
         * { 0, 0, .883, .015, 0, .1, .002, 0, 0},//gs 5 descending
         * //gs 2 3 4 5 6 7 8 0 1
         * { 0, 0, .002, .1, 0, .015, .883, 0, 0},//gs 6 ascending
         * { 0, 0, .015, .883, 0, .002, .1, 0, 0},//gs 6 descending
         * //gs 3 4 5 6 7 8 0 1 2
         * { 0, .1, .001, .001, 0, .015, .883, 0, 0},//gs 7 ascending
         * { 0, .883, .007, .008, 0, .002, .1, 0, 0},//gs 7 descending
         * //gs 4 5 6 7 8 0 1 2 3
         * { 0, 0, .1, .002, 0, .015, .883, 0, 0},//gs 8 ascending
         * { 0, 0, .883, .015, 0, .002, .1, 0, 0},//gs 8 descending
         * };
         */

        double ct = 0.98;
        double nct = 0.02;

        matrix1 = new double[][] {
                // gs 5 6 7 8 0 1 2 3 4
                { 0, 0, 0, 0, 0, nct, ct, 0, 0 }, // gs 0 ascending
                { 0, 0, ct, nct, 0, 0, 0, 0, 0 }, // gs 0 descending
                // gs 6 7 8 0 1 2 3 4 5
                { 0, 0, 0, 0, 0, nct, ct, 0, 0 }, // gs 1 ascending
                { 0, 0, ct, nct, 0, 0, 0, 0, 0 }, // gs 1 descending
                // gs 7 8 0 1 2 3 4 5 6
                { 0, 0, 0, 0, 0, nct, ct, 0, 0 }, // gs 2 ascending
                { 0, 0, ct, nct, 0, 0, 0, 0, 0 }, // gs 2 descending
                // gs 8 0 1 2 3 4 5 6 7
                { 0, 0, 0, 0, 0, nct, ct, 0, 0 }, // gs 3 ascending
                { 0, 0, ct, nct, 0, 0, 0, 0, 0 }, // gs 3 descending
                // gs 0 1 2 3 4 5 6 7 8
                { 0, 0, 0, 0, 0, nct / 2.0, nct / 2.0, ct, 0 }, // gs 4 ascending
                { 0, 0, ct, nct, 0, 0, 0, 0, 0 }, // gs 4 descending
                // gs 1 2 3 4 5 6 7 8 0
                { 0, 0, 0, 0, 0, ct, nct, 0, 0 }, // gs 5 ascending
                { 0, 0, ct, nct, 0, 0, 0, 0, 0 }, // gs 5 descending
                // gs 2 3 4 5 6 7 8 0 1
                { 0, 0, 0, 0, 0, nct, ct, 0, 0 }, // gs 6 ascending
                { 0, 0, nct, ct, 0, 0, 0, 0, 0 }, // gs 6 descending
                // gs 3 4 5 6 7 8 0 1 2
                { 0, 0, 0, 0, 0, nct, ct, 0, 0 }, // gs 7 ascending
                { 0, ct, nct / 2.0, nct / 2.0, 0, 0, 0, 0, 0 }, // gs 7 descending
                // gs 4 5 6 7 8 0 1 2 3
                { 0, 0, 0, 0, 0, nct, ct, 0, 0 }, // gs 8 ascending
                { 0, 0, ct, nct, 0, 0, 0, 0, 0 },// gs 8 descending
        };

        for (double[] row : matrix1) {
            double sum = 0;
            for (double p : row)
                sum += p;
            if (sum != 1)
                System.out.println("error");
        }
        System.out.println("done");
    }

    // matrix that indicates which chord each note belongs to
    // not whether it belongs to the same chord aas the row note
    public double[][] generateAbsMatrix(int[][] chordMembers, int modeLength, boolean polarize) {
        double[][] matrix = new double[modeLength * 2][modeLength];
        int sdIndex = modeLength / 2;
        for (int row = 0; row < modeLength; row++) {

            for (int n = 0; n < chordMembers.length; n++)
                for (int i = sdIndex + 1; i < modeLength; i++) {
                    int sd = (int) (row + Math.rint(modeLength / 2.0) + i) % modeLength;
                    boolean sdInChord = false;
                    for (int cm : chordMembers[n]) {
                        if (sd == cm)
                            sdInChord = true;
                    }
                    if (sdInChord) {
                        matrix[row * 2][i] = n + 1;
                        break;
                    }
                }

            for (int n = 0; n < chordMembers.length; n++)
                for (int i = sdIndex - 1; i >= 0; i--) {
                    int sd = (int) (row + Math.rint(modeLength / 2.0) + i) % modeLength;
                    boolean sdInChord = false;
                    for (int cm : chordMembers[n]) {
                        if (sd == cm)
                            sdInChord = true;
                    }
                    if (sdInChord) {
                        matrix[row * 2 + 1][i] = n + 1;
                        break;
                    }
                }
        }

        if (!polarize) {
            for (int row = 0; row < modeLength; row++) {
                for (int i = 0; i < modeLength; i++) {
                    matrix[row * 2][i] = (matrix[row * 2][i] + matrix[row * 2 + 1][i]) / 2.0;
                    matrix[row * 2 + 1][i] = matrix[row * 2][i];
                }
            }
        }

        return matrix;
    }

    public double[][] generateMatrix(int[] chordMembers, double ct, double nct, int modeLength, boolean polarize) {
        double[][] matrix = new double[modeLength * 2][modeLength];
        int sdIndex = modeLength / 2;
        for (int row = 0; row < modeLength; row++) {

            boolean nctFound = false;
            boolean ctFound = false;
            for (int i = sdIndex + 1; i < modeLength; i++) {
                int sd = (int) (row + Math.rint(modeLength / 2.0) + i) % modeLength;
                boolean sdInChord = false;
                boolean rowNoteInChord = false;
                for (int cm : chordMembers) {
                    if (sd == cm)
                        sdInChord = true;
                    if (row == cm)
                        rowNoteInChord = true;
                }
                if (sdInChord == rowNoteInChord) {
                    ctFound = true;
                    matrix[row * 2][i] = ct;
                    if (nctFound)
                        break;
                } else if (!nctFound) {
                    nctFound = true;
                    matrix[row * 2][i] = nct;
                    if (ctFound)
                        break;
                }
            }

            nctFound = false;
            ctFound = false;
            for (int i = sdIndex - 1; i >= 0; i--) {
                int sd = (int) (row + Math.rint(modeLength / 2.0) + i) % modeLength;
                boolean sdInChord = false;
                boolean rowNoteInChord = false;
                for (int cm : chordMembers) {
                    if (sd == cm)
                        sdInChord = true;
                    if (row == cm)
                        rowNoteInChord = true;
                }
                if (sdInChord == rowNoteInChord) {
                    ctFound = true;
                    matrix[row * 2 + 1][i] = ct;
                    if (nctFound)
                        break;
                } else if (!nctFound) {
                    nctFound = true;
                    matrix[row * 2 + 1][i] = nct;
                    if (ctFound)
                        break;
                }
            }
        }

        if (!polarize) {
            for (int row = 0; row < modeLength; row++) {
                for (int i = 0; i < modeLength; i++) {
                    matrix[row * 2][i] = (matrix[row * 2][i] + matrix[row * 2 + 1][i]) / 2.0;
                    matrix[row * 2 + 1][i] = matrix[row * 2][i];
                }
            }
        }

        return matrix;
    }

    public void writeStratum(WaveWriter ww, Random random, int[] mode, int trans, int low, int high, int startingNote,
            double[] pan, double[][] matrix) {
        Synth synth = new SampleSynth(0);
        int lowestNote = low;// 75;//in ss
        int highestNote = high;// 90;//in ss

        int currentNote = startingNote;// %15 = 1
        int lastNote = 0;
        double t = 0;
        double dirChangeProb = 0.1;

        // the index of THIS scale degree within the matrix row
        // based on the length of the mode. E.g. mode w/ 8 notes, index is 3
        // there are three notes below and four notes above in each row

        int tansposition = trans;
        while (t < 30) {
            dirChangeProb = 0.1 + 0.8 * t / 30.0;
            double dur = 0.05;// 9-tuplets at 120
            synth.writeNote(ww.df, t, c0Freq * Math.pow(2, currentNote / 15.0), 0.01, pan);
            boolean ascending = (currentNote > lastNote);
            int scaleDegree = getScaleDegree(currentNote, mode, tansposition);
            double[] probDist = null;

            int note = -1;
            while (note > highestNote || note < lowestNote) {
                boolean ascending1 = ascending;
                if (random.nextDouble() < dirChangeProb)
                    ascending1 = !ascending;
                if (ascending1) {
                    probDist = matrix[scaleDegree * 2];
                } else {
                    probDist = matrix[scaleDegree * 2 + 1];
                }
                double rnd = random.nextDouble();

                double sum = 0;
                for (int i = 0; i < probDist.length; i++) {
                    sum += probDist[i];
                    if (sum > rnd) {
                        note = matrixIndToNote(i, mode, scaleDegree, currentNote);
                        break;
                    }
                }
            }
            lastNote = currentNote;
            currentNote = note;
            t += dur;
            if (random.nextDouble() > 0.3333)
                t += dur;
        }
    }

    public void writeStrata() {
        ChordTracker ct = new ChordTracker(6, 2);
        int[][] startaNotes = new int[2][6];

    }

    public int matrixIndToNote(int i, int[] mode, int scaleDegree, int currentNote) {
        int sdIndex = mode.length / 2;

        int gi = i - sdIndex; // general interval (positive is ascending)
        boolean giIsAscending = (gi > 0);

        if (!giIsAscending)
            gi += mode.length;
        int firstPC = mode[scaleDegree];
        int secondPC = mode[(scaleDegree + gi) % mode.length];
        if (giIsAscending && secondPC < firstPC)
            secondPC += 15;
        else if (!giIsAscending && secondPC > firstPC)
            secondPC -= 15;
        int si = secondPC - firstPC;

        int note = currentNote + si;

        return note;
    }

    // octave-specific note in ints
    // mode, and transposition of mode, (what is the first note of the mode 0 - 14)
    public static int getScaleDegree(int note, int[] mode, int trans) {
        for (int i = 0; i < mode.length; i++) {
            if (note % 15 == (mode[i] + trans) % 15)
                return i;
        }
        return -1;
    }
}
