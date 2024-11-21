import java.util.Random;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

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
        // System.out.println(closestOct(6,0,12));
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
        Synth synth = new SampleSynth(7);

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

    public void foreground() {
        // { 0, 5, 9, 12 }, { 0, 4, 9, 12 }

        // {6,11,0,3} {1,5,10,13} {3,7,12,0} {8,13,2,5} <CHORD PROGRESSION
        // {1,6,10,0} {3,8,12,2} <BACKGROUNDS (2 and 2)

        // {6,10,0,3} {1,5,11,13} {3,7,13,0} {8,12,2,5} <CHORD PROGRESSION (T5)

        // {11,0,5,3?} {1,6,10,13} {3,8,12,0} {13,2,7,5?} <CHORD PROGRESSION (T-5)
        // should be 8 and 10, but modified for horizontal relations

        // different approach (second half is T5 of first)
        // {6,11,0,3} {1,5,10,13} T5 {6,10,0,3} {1,5,11,13}
        // {11,0,5,3} {6,10,1,13} {6,10,0,13} {11,1,5,8}

    }

    public double probOfHomorhythm;
    public boolean changeTimeline;
public ArrayList<Stratum> unplayedStrata;
public ArrayList<Stratum> strata;
int timeMode;
Random rand;
Sequencer seq;
WaveWriter ww;
double time;
    public void parsimoniousTexture1() {
        boolean testOctave = false;
        int octToTest = 4;
         seq = new Sequencer(1);

         timeMode = 1;
        // 0 - fast even rhythms 1 - probiblistic MER based on TET

        // used in rhythmic mode 1
        int pulses = 15;
        int progsPerTimeline = 1; // a subtle value. 2 makes harmonic rhythm faster (anything higher leads to
                                  // repeated notes)
        int onsetsPerTimeline = 5;
        int timelineChangeFreq = 1; // how often the timeline changes

        probOfHomorhythm = 0;// probability that strata will use the same timeline
        changeTimeline = false;// use to force timeline to change

        // pulses = 2;
        // onsetsPerTimeline = 2;

        rand = new Random(123);
        int oct = 7;
        if (testOctave)
            oct = octToTest;
        int[][] c = populateChords(seq, rand, oct * seq.TET, timeMode);// 6

        strata = new ArrayList<Stratum>();
        ArrayList<Envelope> envs = GUI.open(new File("envs.txt"));
        strata.add(new Stratum(c, oct, new SampleSynth(8), rand, envs.get(0)));

         time = 0;
     ww = new WaveWriter("parsi");
        Synth[] synths = { new SampleSynth(8) };// new SampleSynth(0), new SampleSynth(1) };

        double[] pan = new double[] { 1 };
        unplayedStrata = new ArrayList<Stratum>();
        unplayedStrata.add(new Stratum(null, 7, new SampleSynth(0), rand, envs.get(5)));
        unplayedStrata.add(new Stratum(null, 6, new SampleSynth(0), rand, envs.get(1)));
        unplayedStrata.add(new Stratum(null, 6, new SampleSynth(2), rand, envs.get(6)));
        unplayedStrata.add(new Stratum(null, 5, new SampleSynth(2), rand, envs.get(3)));
        unplayedStrata.add(new Stratum(null, 6, new SampleSynth(8), rand, envs.get(7)));

        Synth vibs = new Vibs();

        // strata.remove(0);
        unplayedStrata.add(new Stratum(null, 5, vibs, rand, envs.get(4)));
        unplayedStrata.add(new Stratum(null, 4, vibs, rand, envs.get(2)));

        ArrayList<Cue> cues = new ArrayList<Cue>();


        cues.add(new Cue() {
            void run() {
                piece.addStratum(1);
            }
        }.initialize(this, 25));
        cues.add(new Cue() {
            void run() {
                piece.addStratum(6);
            }
        }.initialize(this, 40));
        cues.add(new Cue() {
            void run() {
                piece.addStratum(3);
            }
        }.initialize(this, 55));

        cues.add(new Cue() {
            void run() {
                piece.addStratum(5);
            }
        }.initialize(this, 70));
        cues.add(new Cue() {
            void run() {
                piece.addStratum(0);
            }
        }.initialize(this, 75));
        cues.add(new Cue() {
            void run() {
                piece.addStratum(2);
            }
        }.initialize(this, 80));
        cues.add(new Cue() {
            void run() {
                piece.addStratum(4);
            }
        }.initialize(this, 85));

        cues.add(new Cue() {
            void run() {
                piece.probOfHomorhythm = 1;
                piece.changeTimeline = true;
            }
        }.initialize(this, 3 * 60));
        cues.add(new Cue() {
            void run() {
                piece.probOfHomorhythm = 0;
                piece.changeTimeline = false;
            }
        }.initialize(this, 3*60 + 3));

        cues.add(new Cue() {
            void run() {
                piece.probOfHomorhythm = 1;
                piece.changeTimeline = true;
            }
        }.initialize(this, 3 * 60 + 13));
        cues.add(new Cue() {
            void run() {
                piece.probOfHomorhythm = 0;
                piece.changeTimeline = false;
                piece.realizeDrone(
                    piece.populateChords(piece.seq, piece.rand, 60, 0), new SampleSynth(17), piece.seq.TET, piece.ww, piece.time,0.5,new double[]{1});
            }
        }.initialize(this, 3*60 + 16));

        Collections.sort(cues);

        int tl = 0;
        int tlSeed = -1;
        while (time < 60 * 4) {
            while (cues.size() > 0 && cues.get(0).startTime <= time) {
                cues.get(0).run();
                cues.remove(0);
            }
            int[][] notes = seq.getChords();
            int sNum = 0;
            if (tl % timelineChangeFreq == 0 || changeTimeline)
                tlSeed = rand.nextInt();
            tl++;
            for (int stratum = 0; stratum < strata.size(); stratum++) {
                int seed = tlSeed + stratum;
                if (rand.nextDouble() < probOfHomorhythm) {
                    seed = tlSeed;
                }
                int[][] chords = strata.get(stratum).chords;
                realizeChords(chords, notes, time, strata.get(stratum).synth, ww, rand, pan, seq.TET, timeMode,
                        strata.get(stratum), onsetsPerTimeline, progsPerTimeline, seed, pulses);// rand.nextInt()
                sNum++;
            }
            switch (timeMode) {
                case 0:
                    time += 8 * 1 / 10.0;
                    break;
                case 1:
                    time += pulses * 1 / 10.0;
                    break;
                case 2:
                    time += pulses * 2 / 10.0;
                    break;
            }
            double probOfNewVoice = 0;
            switch (timeMode) {
                case 0:
                    probOfNewVoice = 0.1;
                    break;
                case 1:
                    probOfNewVoice = 8 / 160.0;// over the course of 4 minutes all voices enter
                    double probOfNewOnset = 5 / 160.0;// 5 out of 160 progressions (over 4 minutes)
                    if (onsetsPerTimeline < 10 && rand.nextDouble() < probOfNewOnset) {
                        onsetsPerTimeline++;
                    }
                    double probOfNewRep = 5 / 160.0;
                    if (timelineChangeFreq < 5 && rand.nextDouble() < probOfNewRep) {
                        timelineChangeFreq++;
                    }
                    double probOfDrone = 5 / 80.0;
                    if (time > 120 && rand.nextDouble() < probOfDrone) {
                        realizeDrone(strata.get(0).chords, new SampleSynth(17), seq.TET, ww, time, 0.5,
                                new double[] { 1 });
                    }
                    break;
                case 2:
                    probOfNewVoice = 0.05;
                    break;

            }
            if (testOctave)
                probOfNewVoice = 0;

                //deprecated (now using deterministic entrances)
            if (false && unplayedStrata.size() > 0 && rand.nextDouble() < probOfNewVoice) {
                int ind = (int) (rand.nextDouble() * unplayedStrata.size());
                unplayedStrata.get(ind).chords = populateChords(seq, rand, unplayedStrata.get(ind).target * seq.TET,
                        timeMode);
                strata.add(unplayedStrata.get(ind));
                unplayedStrata.remove(ind);
            }
        }
        System.out.println(seq.myGame);

        ww.render();
    }

    public void addStratum(int ind){
        unplayedStrata.get(ind).chords = populateChords(seq, rand, unplayedStrata.get(ind).target * seq.TET,
        timeMode);
strata.add(unplayedStrata.get(ind));
    }

    public int[][] populateChords(Sequencer seq, Random rand, int target, int timeMode) {
        int[][] chords = new int[seq.myGame.getLastBoard().size()][seq.myGame.getLastBoard().get(0).notes().size()];
        for (int i = 0; i < chords.length; i++) {
            ArrayList<Integer> notes = seq.myGame.getLastBoard().get(i).notes();
            ArrayList<Integer> chrd = new ArrayList<Integer>();
            for (int n = 0; n < chords[i].length; n++) {
                int ind = (int) (rand.nextDouble() * notes.size());
                if (timeMode == 2) {
                    chrd.add(closestOct(target, notes.get(ind), seq.TET));
                } else
                    chords[i][n] = closestOct(target, notes.get(ind), seq.TET);
                notes.remove(ind);
            }
            if (timeMode == 2) {
                Collections.sort(chrd);
                for (int n = 0; n < chrd.size(); n++) {
                    chords[i][n] = chrd.get(n) - seq.TET * (n % 2);
                }
            }
        }
        return chords;
    }

    /*
     * Parameters specific to mode 1:
     * int numOfOnsets, int numOfProgressions, int seed
     */
    public static double chordVolScalar = 0.1;
    public static double chordVolMin = 0.001;

    public void realizeChords(int[][] chords, int[][] notes, double time, Synth synth, WaveWriter ww, Random rand,
            double[] pan, int tet, int timeMode, Stratum strat, int numOfOnsets, int numOfProgressions, int seed,
            int pulses) {

        switch (timeMode) {
            case 0:
                for (int n = 0; n < chords.length; n++) {
                    for (int i = 0; i < chords[n].length; i++) {
                        synth.writeNote(ww.df, time, c0Freq * Math.pow(2, chords[n][i] / (double) tet),
                                chordVolScalar * strat.vol(time) + chordVolMin, pan);
                        time += 1 / 10.0;
                    }
                }
                advanceChord(chords, notes, tet, strat.getTarget(time) * tet);
                break;
            case 1:
                // inputs

                ArrayList<Integer> onsets = generateOnsets(pulses, seed, numOfOnsets);
                int numOfChords = numOfProgressions * chords.length;
                double exactNotesPerChord = numOfOnsets / (double) numOfChords;
                ArrayList<Integer> notesPerChord = new ArrayList<Integer>();
                double tot = exactNotesPerChord;
                double lastTot = 0;
                while (tot <= numOfOnsets) {
                    notesPerChord.add(((int) tot) - ((int) lastTot));
                    lastTot = tot;
                    tot += exactNotesPerChord;
                }

                int onsetIndex = 0;

                int chordIndex = 0;
                int boardIndex = 0;// not which board, but where in the board
                while (onsetIndex < onsets.size()) {// 1 pass = 1 chord

                    for (int chordMemberIndex = 0; chordMemberIndex < notesPerChord
                            .get(chordIndex); chordMemberIndex++) {
                        int note = chords[boardIndex][chordMemberIndex % chords[boardIndex].length];
                        synth.writeNote(ww.df, time + (onsets.get(onsetIndex) / 10.0),
                                c0Freq * Math.pow(2, note / (double) tet),
                                chordVolScalar * strat.vol(time) + chordVolMin, pan);
                        onsetIndex++;
                    }

                    boardIndex++;
                    chordIndex++;
                    if (boardIndex == chords.length) {
                        advanceChord(chords, notes, tet, strat.getTarget(time) * tet);
                        boardIndex = 0;
                    }
                }
                break;
            case 2:
                chords[1] = chordComplement(chords[0], chords[1], tet);
                for (int i = 0; i < chords.length; i++) {
                    int[] comp = chordComplement(chords[i], tet);
                    for (int member = 0; member < chords[i].length; member++) {
                        onsets = generateOnsets(pulses, seed + member + chords[i].length * i, numOfOnsets);
                        for (int osInd = 0; osInd < onsets.size(); osInd++) {
                            int note = chords[i][member];
                            if (osInd % 2 == 1)
                                note = comp[member];
                            synth.writeNote(ww.df, time + (onsets.get(osInd) / 10.0) + i * pulses / 10.0,
                                    c0Freq * Math.pow(2, note / (double) tet),
                                    chordVolScalar * strat.vol(time) + chordVolMin, pan);
                        }
                    }
                }
                advanceChord(chords, notes, tet, strat.getTarget(time) * tet);
                break;
        }

    }

    public ArrayList<Integer> generateOnsets(int pulses, int seed, int numOfOnsets) {
        double[] probDist = onsetProbabilities(pulses);
        boolean[] onset = new boolean[probDist.length];// ensures onset indexes are unique (could be done with
                                                       // .contains)
        ArrayList<Integer> onsets = new ArrayList<Integer>();
        Random localRand = new Random(seed);
        for (int i = 0; i < numOfOnsets; i++) {
            int ind = 0;
            do {
                double rnd = localRand.nextDouble();
                double tot = 0;
                ind = 0;
                for (ind = 0; tot <= rnd; ind++) {
                    tot += probDist[ind];
                }
                ind--;
            } while (onset[ind]);// must be a unique onset
            onset[ind] = true;
            onsets.add(ind);
        }
        Collections.sort(onsets);
        return onsets;
    }

    public void advanceChord(int[][] chords, int[][] notes, int tet, int target) {
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
                    int note = closestOct(chords[n][chordReplaceIndex], notes[n][j], tet);
                    while (note - target > tet)
                        note -= tet;
                    while (target - note > tet)
                        note += tet;
                    chords[n][chordReplaceIndex] = note;
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

    public int[] chordComplement(int[] chord, int tet) {
        ArrayList<ArrayList<Integer>> vls = allVLs(chord, new ArrayList<Integer>(), tet);
        int leastSteps = Integer.MAX_VALUE;
        ArrayList<Integer> bestVL = null;
        for (ArrayList<Integer> vl : vls) {
            int tot = 0;
            for (int i = 0; i < chord.length; i++)
                tot += Math.abs(vl.get(i) - chord[i]);
            if (tot < leastSteps) {
                bestVL = vl;
                leastSteps = tot;
            }
        }
        int[] vl = new int[chord.length];
        for (int i = 0; i < chord.length; i++)
            vl[i] = bestVL.get(i);
        return vl;
    }

    public void realizeDrone(int[][] chords, Synth synth, int tet, WaveWriter ww, double time, double vol,
            double[] pan) {
        int[] notePopularity = new int[tet];
        for (int[] c : chords)
            for (int n : c)
                notePopularity[n % tet]++;
        int maxPopularity = 0;
        int popularNote = 0;
        for (int n = 0; n < notePopularity.length; n++) {
            if (notePopularity[n] > maxPopularity) {
                maxPopularity = notePopularity[n];
                popularNote = n % tet;
            }
        }
        // play popular note as close to A3 as possible
        int note = closestOct(tet * 3 + (int) Math.rint(tet * 9 / 12.0), popularNote, tet);
        synth.writeNote(ww.df, time, Math.pow(2, note / (double) tet) * c0Freq, vol, pan);

    }

    public int[] chordComplement(int[] chord, int[] secondChord, int tet) {
        ArrayList<ArrayList<Integer>> vls = allVLs(chord, secondChord, new ArrayList<Integer>(), tet);
        int leastSteps = Integer.MAX_VALUE;
        ArrayList<Integer> bestVL = null;
        for (ArrayList<Integer> vl : vls) {
            int tot = 0;
            for (int i = 0; i < chord.length; i++)
                tot += Math.abs(vl.get(i) - chord[i]);
            if (tot < leastSteps) {
                bestVL = vl;
                leastSteps = tot;
            }
        }
        int[] vl = new int[chord.length];
        for (int i = 0; i < chord.length; i++)
            vl[i] = bestVL.get(i);
        return vl;
    }

    public ArrayList<ArrayList<Integer>> allVLs(int[] chord, ArrayList<Integer> vl, int tet) {

        ArrayList<ArrayList<Integer>> completeVLs = new ArrayList<ArrayList<Integer>>();
        if (vl.size() == chord.length) {
            completeVLs.add(vl);
            return completeVLs;
        }
        int target = chord[vl.size()];
        for (int i = 0; i < chord.length; i++)
            if (i != vl.size()) {
                ArrayList<Integer> vlCopy = new ArrayList<Integer>();
                vlCopy.addAll(vl);
                vlCopy.add(closestOct(target, chord[i], tet));
                completeVLs.addAll(allVLs(chord, vlCopy, tet));
            }

        return completeVLs;
    }

    public ArrayList<ArrayList<Integer>> allVLs(int[] chord, int[] secondChord, ArrayList<Integer> vl, int tet) {

        ArrayList<ArrayList<Integer>> completeVLs = new ArrayList<ArrayList<Integer>>();
        if (vl.size() == chord.length) {
            completeVLs.add(vl);
            return completeVLs;
        }
        int target = secondChord[vl.size()];
        for (int i = 0; i < chord.length; i++) {
            ArrayList<Integer> vlCopy = new ArrayList<Integer>();
            vlCopy.addAll(vl);
            vlCopy.add(closestOct(target, chord[i], tet));
            completeVLs.addAll(allVLs(chord, secondChord, vlCopy, tet));
        }

        return completeVLs;
    }
}
