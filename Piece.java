import java.util.Random;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Write a description of class Piece here.
 * turn on samplesynth and vibs to hear full piece
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
        ww = new WaveWriter("FORE GROUND TEST");
        foreground();
        ww.render(1);
          */

        parsimoniousTexture1();


        // testChordProgression();
        // onsetProbabilities(15);
    }

    public static void main(String[] args) {

        new Piece();
    }

    

    public void testChordProgression() {
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

        // different approach (second half is T5 of first) //switch last two chords in both progressions
        // {6,11,0,3} {1,5,10,13} T5 {6,10,0,3} {1,5,11,13}
        // {11,0,5,3} {6,10,1,13} {6,10,0,13} {11,1,5,8}
        Synth[] synths = new Synth[]{new SustainedSynth(2,0.35),new SustainedSynth(0,0.35),new SustainedSynth(8,0.35),new SustainedSynth(15,0.35)};
        int chord[] = new int[]{6 + 15 * 4,11+ 15 * 4,0+ 15 * 5,3+ 15 * 5};
        chord = new int[]{6 + 15 * 4,0 + 15 * 5,3 + 15 * 5,11+ 15 * 5};
        for(int n = 0; n < 4; n++){
            int note = chord[n];
            synths[n].writeNote(ww.df, 91, c0Freq * Math.pow(2,note / 15.0), 0, new double[]{1});
        }
        
      
        chord = new int[]{5 + 15 * 4,10 + 15 * 4,1 + 15 * 5,13+ 15 * 5};
        chord = new int[]{10 + 15 * 4,1 + 15 * 5,5 + 15 * 5,13+ 15 * 5};
        for(int n = 0; n < 4; n++){
            int note = chord[n];
            
            synths[n].writeNote(ww.df, 91 + 6, c0Freq * Math.pow(2,note / 15.0), 1, new double[]{1});
        }
        chord = new int[]{6 + 15 * 4,10 + 15 * 4,3 + 15 * 5,0+ 15 * 6};
        chord = new int[]{13 + 15 * 4,1 + 15 * 5,5 + 15 * 5,11+ 15 * 5};
        for(int n = 0; n < 4; n++){
            int note = chord[n];
            synths[n].writeNote(ww.df, 91 + 12, c0Freq * Math.pow(2,note / 15.0), 0, new double[]{1});
        }
        chord = new int[]{6 + 15 * 4,0 + 15 * 5,3+ 15 * 5,10 + 15 * 5};
        for(int n = 0; n < 4; n++){
            int note = chord[n];
            synths[n].writeNote(ww.df, 91 + 18, c0Freq * Math.pow(2,note / 15.0), 1, new double[]{1});
        }

        //
        Synth synth = new MultiConvolutionSynth(5,0.35);//new ReverseSynth(0.5);//SustainedSynth(-1,0.01);//16
        chord = new int[]{5 + 15 * 4,0 + 15 * 5,3 + 15 * 5,11+ 15 * 5};
        for(int n = 0; n < 4; n++){
            int note = chord[n];
            synth.writeNote(ww.df, 136, c0Freq * Math.pow(2,note / 15.0), 1, new double[]{1});
        }
        
    
        chord = new int[]{10 + 15 * 4,1 + 15 * 5,6 + 15 * 5,13+ 15 * 5};
        for(int n = 0; n < 4; n++){
            int note = chord[n];
            synth.writeNote(ww.df, 136 + 6, c0Freq * Math.pow(2,note / 15.0), 1, new double[]{1});
        }

        chord = new int[]{8 + 15 * 4,1 + 15 * 5,5 + 15 * 5,11+ 15 * 5};
        for(int n = 0; n < 4; n++){
            int note = chord[n];
            synth.writeNote(ww.df, 136 + 12, c0Freq * Math.pow(2,note / 15.0), 1, new double[]{1});
        }
        chord = new int[]{6 + 15 * 4,0 + 15 * 5,13+ 15 * 4,10 + 15 * 5};
        for(int n = 0; n < 4; n++){
            int note = chord[n];
            synth.writeNote(ww.df, 136 + 18, c0Freq * Math.pow(2,note / 15.0), 1, new double[]{1});
        }


       synths = new Synth[]{new SustainedSynth(2,0.3),new SustainedSynth(0,0.3),new SustainedSynth(8,0.3),new SustainedSynth(15,0.3),new SustainedSynth(15,0.3)};
                //{0, 3, 7, 12, 15} {14,19,1,5,10} t7 {7, 10, 14, 19, 1} {0,3,5,12,15}
                
                chord = new int[]{15 + 21 * 5,3 + 21 * 5,7+ 21 * 5,12 + 21 * 5, 0 + 21 * 5};
                chord = new int[]{15 + 21 * 4,3 + 21 * 5,7+ 21 * 5,12 + 21 * 5, 0 + 21 * 6};
                for(int n = 0; n < 5; n++){
                    int note = chord[n];
                    synths[n].writeNote(ww.df, 275, c0Freq * Math.pow(2,note / 21.0), 0, new double[]{1});
                }
                                                    //3?
                chord = new int[]{14 + 21 * 4,1 + 21 * 4,5+ 21 * 5,10 + 21 * 5, 19 + 21 * 5};
                for(int n = 0; n < 5; n++){
                    int note = chord[n];
                    synths[n].writeNote(ww.df, 275 + 6, c0Freq * Math.pow(2,note / 21.0), 1, new double[]{1});
                }
                chord = new int[]{19 + 21 * 4,1 + 21 * 3,7+ 21 * 5,10 + 21 * 5, 14 + 21 * 5};
                for(int n = 0; n < 5; n++){
                    int note = chord[n];
                    synths[n].writeNote(ww.df, 275 + 12, c0Freq * Math.pow(2,note / 21.0), 0, new double[]{1});
                }
                chord = new int[]{0 + 21 * 4,3 + 21 * 3,5+ 21 * 5,12 + 21 * 5, 15 + 21 * 5};
                for(int n = 0; n < 5; n++){
                    int note = chord[n];
                    synths[n].writeNote(ww.df, 275 + 18, c0Freq * Math.pow(2,note / 21.0), 1, new double[]{1});
                }

                //33 tet progression at 7 minutes
                //{25,0,5,11,19} {3,8,14,22,27} T11 {3, 11, 27, 22, 8} {14, 19, 25, 0, 5}
                //{25, 11, 3,22} {3, 22, 14,0}
                synths = new Synth[]{new SustainedSynth(2,0.3),new SustainedSynth(2,0.3),new SustainedSynth(0,0.3),new SustainedSynth(8,0.3),new SustainedSynth(15,0.3),new SustainedSynth(15,0.3)};

                chord = new int[]{25 + 21 * 4, 0 + 21 * 5, 5 + 21 * 5, 11 + 21 * 5, 19 + 21 * 5,25 + 21 * 5};
                for(int n = 0; n < 5; n++){
                    int note = chord[n];
                    synths[n].writeNote(ww.df, 7*60 + 0, c0Freq * Math.pow(2,note / 21.0), 0, new double[]{1});
                }
                                                   
                chord = new int[]{3 + 21 * 5,8 + 21 * 5,14+ 21 * 5,22 + 21 * 5, 27 + 21 * 5};
                for(int n = 0; n < 5; n++){
                    int note = chord[n];
                    synths[n].writeNote(ww.df, 7*60 + 6, c0Freq * Math.pow(2,note / 21.0), 1, new double[]{1});
                }

                chord = new int[]{11 + 21 * 4,8 + 21 * 5,22 + 21 * 5, 27 + 21 * 5,3+ 21 * 6};
                for(int n = 0; n < 5; n++){
                    int note = chord[n];
                    synths[n].writeNote(ww.df, 7*60 + 12, c0Freq * Math.pow(2,note / 21.0), 0, new double[]{1});
                }
                chord = new int[]{ 0 + 21 * 5, 5 + 21 * 5, 14 + 21 * 5, 19 + 21 * 5, 25 + 21 * 5};
                for(int n = 0; n < 5; n++){
                    int note = chord[n];
                    synths[n].writeNote(ww.df, 7*60 + 18, c0Freq * Math.pow(2,note / 21.0), 1, new double[]{1});
                }
    }

    public double probOfHomorhythm;
    public boolean changeTimeline;
public ArrayList<Stratum> unplayedStrata;
public ArrayList<Stratum> strata;
int timeMode;
static Random rand;
static double altChordProb, pulseLength;
Sequencer seq;
WaveWriter ww;
double time;
ArrayList<LoopSynth> loopSynths;
static Envelope reverbEnv;
    public void parsimoniousTexture1() {
        boolean testOctave = false;
        int octToTest = 4;
        seq = new Sequencer(0);

         timeMode = 1;
        // 0 - fast even rhythms 1 - probiblistic MER based on TET

        // used in rhythmic mode 1
        int pulses = 15;
        int progsPerTimeline = 1; // a subtle value. 2 makes harmonic rhythm faster (anything higher leads to
                                  // repeated notes)
        int onsetsPerTimeline = 5;
        onsetsPerTimeline = 1;
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

        seq.alternateChords = new int[][]{{1,6,10,0},{11,6,5,0}};

        strata = new ArrayList<Stratum>();
        ArrayList<Envelope> envs = GUI.open(new File("envs.txt"));
        /*
         * envs by index
         * 0 - oct 7 vol
         * 1 - oct 6 vol
         * 2 - oct 4 vol 
         * 3 - oct 5 vol
         * 4 - oct 5 vol
         * 5 - oct 7 vol
         * 6 - oct 6 vol
         * 7 - oct 6 vol
         * 8 - onsets per timeline (1 - 15)
         */
        ArrayList<Envelope> envs1 = GUI.open(new File("envs1.txt"));
        /*
         * envs by index
         * 0 - global reverb
         * 1 - altChordProb envelop (controls the probibility that seq.altChord is played, i.e. something to accommpany foreground)
         * 2 - altchordProbDist env (used for generating probibility distribution for which of the alt chords to choose)
         * 3 - used by sutstained synth (vol 1 = backward and vol 0 = fowarad)
         */
        reverbEnv = envs1.get(0);

        strata.add(new Stratum(c, oct, new SampleSynth(8), rand, envs.get(0)));

         time = 0;
     ww = new WaveWriter("parsi");
        Synth[] synths = { new SampleSynth(8) };// new SampleSynth(0), new SampleSynth(1) };

        double[] pan = new double[] { 1 };
        unplayedStrata = new ArrayList<Stratum>();
        unplayedStrata.add(new Stratum(null, 7, new SampleSynth(0), rand, envs.get(5)));//0
        unplayedStrata.add(new Stratum(null, 6, new SampleSynth(0), rand, envs.get(1)));
        unplayedStrata.add(new Stratum(null, 6, new SampleSynth(2), rand, envs.get(6)));
        unplayedStrata.add(new Stratum(null, 5, new SampleSynth(2), rand, envs.get(3)));
        unplayedStrata.add(new Stratum(null, 6, new SampleSynth(8), rand, envs.get(7)));

        Synth vibs = new Vibs();

        // strata.remove(0);
        unplayedStrata.add(new Stratum(null, 5, vibs, rand, envs.get(4)));
        unplayedStrata.add(new Stratum(null, 4, vibs, rand, envs.get(2)));

        unplayedStrata.add(strata.get(0));//for the second exposition (index)

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
        }.initialize(this, 2*60 + 5));

        cues.add(new Cue() {
            void run() {
                piece.probOfHomorhythm = 0;
                piece.changeTimeline = false;
                piece.realizeDrone(
                    piece.populateChords(piece.seq, piece.rand, 60, 0), new SampleSynth(17), piece.seq.TET, piece.ww, piece.time,0.5,new double[]{1});
            }
        }.initialize(this, 2*60 + 8));

        cues.add(new Cue() {
            void run() {
                piece.probOfHomorhythm = 1;
                piece.changeTimeline = true;
            }
        }.initialize(this, 2*60 + 13));

        cues.add(new Cue() {
            void run() {
                piece.probOfHomorhythm = 0;
                piece.changeTimeline = false;
                piece.realizeDrone(
                    piece.populateChords(piece.seq, piece.rand, 60, 0), new SampleSynth(17), piece.seq.TET, piece.ww, piece.time,0.5,new double[]{1});
            }
        }.initialize(this, 2*60 + 16));

        //another 15 tet progression here
        cues.add(new Cue() {
            void run() {
                seq.alternateChords = new int[][] { { 11, 1, 5, 10 }, { 1, 6, 10, 0 } };
            }
        }.initialize(this, 2 * 60 + 11));

        cues.add(new Cue() {
            void run() {
                piece.probOfHomorhythm = 1;
                piece.changeTimeline = true;
            }
        }.initialize(this, 2*60 + 40));

        cues.add(new Cue() {
            void run() {
                piece.probOfHomorhythm = 0;
                piece.changeTimeline = false;
                piece.realizeDrone(
                    piece.populateChords(piece.seq, piece.rand, 60, 0), new SampleSynth(17), piece.seq.TET, piece.ww, piece.time,0.5,new double[]{1});
                int[][] alt = piece.seq.alternateChords;
                    piece.seq = new Sequencer(1);
                    piece.seq.alternateChords = alt;
                
                }
        }.initialize(this, 2*60 + 50));

        cues.add(new Cue() {
            void run() {
               piece.strata = new ArrayList<Stratum>();
                    piece.addStratum(6); //env index 2
            }
        }.initialize(this, 2*60 + 52));

        cues.add(new Cue() {
            void run() {
                piece.addStratum(5); ////env index 4
            }
        }.initialize(this, 3*60 + 17));

        cues.add(new Cue() {
            void run() {
                piece.addStratum(0);////env 5
            }
        }.initialize(this, 3*60 + 32));


        cues.add(new Cue() {
            void run() {
                piece.addStratum(1); // //env index 1
            }
        }.initialize(this, 3*60 + 47));

        cues.add(new Cue() {
            void run() {
                piece.addStratum(2);//env index 6
            }
        }.initialize(this, 4*60 + 2));


        cues.add(new Cue() {
            void run() {
                piece.addStratum(3);////env index 3
            }
        }.initialize(this, 4*60 + 7));
        cues.add(new Cue() {
            void run() {
                piece.addStratum(7);//env index 0
            }
        }.initialize(this, 4*60 + 12));
        cues.add(new Cue() {
            void run() {
                piece.addStratum(4);////env index 7
            }
        }.initialize(this, 4*60 + 17));
        cues.add(new Cue() {
            void run() {
                piece.seq.alternateChords = new int[][] { {25, 11, 3,22}, {3, 22, 14,0} };//33tetALt
            }
        }.initialize(this, 4 * 60 + 35 - 5));

        cues.add(new Cue() {
            void run() {
                piece.probOfHomorhythm = 1;
                piece.changeTimeline = true;
            }
        }.initialize(this, 5*60 + 5));

        cues.add(new Cue() {
            void run() {
                piece.probOfHomorhythm = 0;
                piece.changeTimeline = false;
                piece.realizeDrone(
                    piece.populateChords(piece.seq, piece.rand, 60, 0), new SampleSynth(17), piece.seq.TET, piece.ww, piece.time,0.5,new double[]{1});
           piece.timeMode = 0;
                }
        }.initialize(this, 5*60 + 10));
        cues.add(new Cue() {
            void run() {
                piece.timeMode = 1;
            }
        }.initialize(this, 5*60 + 13));

        cues.add(new Cue() {
            void run() {
                piece.probOfHomorhythm = 1;
                piece.changeTimeline = true;
            }
        }.initialize(this, 5*60 + 20));

        cues.add(new Cue() {
            void run() {
                piece.probOfHomorhythm = 0;
                piece.changeTimeline = false;
                piece.realizeDrone(
                    piece.populateChords(piece.seq, piece.rand, 60, 0), new SampleSynth(17), piece.seq.TET, piece.ww, piece.time,0.5,new double[]{1});
           piece.timeMode = 0;
                }
        }.initialize(this, 5*60 + 25));
        cues.add(new Cue() {
            void run() {
                int[][] alt = piece.seq.alternateChords;
                piece.seq = new Sequencer(2);
                piece.seq.alternateChords = new int[][] { { 0, 12, 14, 5, 3, 2 }, { 7, 19, 7, 12, 10, 9 } };
                piece.strata.remove(5);
                piece.strata.remove(1);
                piece.strata.remove(0);
                piece.timeMode = 3;
            }
        }.initialize(this, 5 * 60 + 30));

        //@ 7 min add a 33 TET progression

        cues.add(new Cue() {
            void run() {
                Piece.pulseLength = 0.5;
                piece.timeMode = 4;
                loopSynths = new ArrayList<LoopSynth>();
                for(int i = 0; i < 10; i++){
                    loopSynths.add(new LoopSynth());
                }  
                piece.strata = new ArrayList<Stratum>();
                    piece.addStratum(1); //env index 1
            }
        }.initialize(this, 7*60 + 45));


        cues.add(new Cue() {
            void run() {
                for(int i = 0; i < 10; i++){
                    loopSynths.get(i).addPitch(-1, 9*60 + 20);
                    loopSynths.get(i).writeNote(ww.df, 0, 0, 0, new double[]{1});;
                }  
            }
        }.initialize(this, 9*60));
        

        Collections.sort(cues);

        int tl = 0;
        int tlSeed = -1;
        double pd = 0;
        while (time < 60 * 9 + 10) {
            while (cues.size() > 0 && cues.get(0).startTime <= time) {
                cues.get(0).run();
                cues.remove(0);
            }
            System.out.println("TIME: " + time);

            //update altchord values
            altChordProb = envs1.get(1).getValue(time);
            double altChordProbDistEnv = envs1.get(2).getValue(time) * (seq.alternateChords.length - 1);
            seq.probDistOfAltChords = new double[seq.alternateChords.length];
            double tot = 0;
            for(int i = 0; i < seq.probDistOfAltChords.length; i++){
                seq.probDistOfAltChords[i] = Math.abs(altChordProbDistEnv - i);
                tot += seq.probDistOfAltChords[i];
            }
            for(int i = 0; i < seq.probDistOfAltChords.length; i++)
                seq.probDistOfAltChords[i] /= tot;

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
                case 3://in case we want 33 tet w/o overlapping chords
                    time += 10 * 1 / 10.0;
                    break;
                case 4://for sustained texture
                    time += notes.length * notes[0].length * pulseLength;
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
                    if (false && onsetsPerTimeline < 10 && rand.nextDouble() < probOfNewOnset) {
                        onsetsPerTimeline++;
                    }
                    onsetsPerTimeline = (int)(envs.get(8).getValue(time) * 14) + 1;
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

                case 3:
                 pd += 1/(135.0 * 5);
                if ((time < 7 * 60 || time > 7 * 60 + 24) && rand.nextDouble() < pd) {
                    realizeDrone(strata.get(0).chords, new SampleSynth(17), seq.TET, ww, time, 0.5,
                            new double[] { 1 });
                }
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
        foreground();

        ww.render(1);//change this for 8 channels
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
                chords = advanceChord(chords, notes, tet, strat.getTarget(time) * tet);
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
                        chords = advanceChord(chords, notes, tet, strat.getTarget(time) * tet);
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
                chords = advanceChord(chords, notes, tet, strat.getTarget(time) * tet);
                break;
                case 3:
                for (int n = 0; n < chords.length; n++) {
                    for (int i = 0; i < chords[n].length; i++) {
                        synth.writeNote(ww.df, time, c0Freq * Math.pow(2, chords[n][i] / (double) tet),
                                chordVolScalar * strat.vol(time) + chordVolMin, pan);
                        time += 1 / 10.0;
                    }
                }
                chords = advanceChord(chords, notes, tet, strat.getTarget(time) * tet);
                break;
                case 4:
                chords = advanceChord(chords, notes, tet, strat.getTarget(time) * tet);
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

    public int[][] advanceChord(int[][] chords, int[][] notes, int tet, int target) {
       for(int i = 0; i < chords.length; i++)
           if (chords[i].length != notes[i].length) {
               chords[i] = Arrays.copyOf(chords[i], notes[i].length);
           }

        // progress to next chord (form chords to notes)
        
        ArrayList<Integer> chordReplaceIndexes = new ArrayList<Integer>();
        for (int n = 0; n < chords.length; n++) {
            boolean[] notesAreContained = new boolean[chords[n].length];
            
            for (int i = 0; i < chords[n].length; i++)
                notesAreContained[i] = false;
            for (int i = 0; i < chords[n].length; i++) {
                boolean chordNoteIsContained = false;
                for (int j = 0; j < notes[n].length; j++) {
                    if (notes[n][j] % tet == chords[n][i] % tet) {
                        chordNoteIsContained = true;
                        notesAreContained[j] = true;
                        break;
                    }
                }
                if (!chordNoteIsContained) {
                    // chords[n][i] = -1;
                    chordReplaceIndexes.add(i);
                    //break;
                }
            }
            /*
            if(chordReplaceIndexes.size() !=1)
                    System.out.println("MISTAKE");
                     */
            if (chordReplaceIndexes.size() == 0)
                continue;// why would the chord be identical?
                
            for (int j = 0; j < notesAreContained.length; j++) {
                if (!notesAreContained[j]) {
                    int chordReplaceIndex = chordReplaceIndexes.get(0);
                    chordReplaceIndexes.remove(0);
                    int note = closestOct(chords[n][chordReplaceIndex], notes[n][j], tet);
                    while (note - target > tet)
                        note -= tet;
                    while (target - note > tet)
                        note += tet;
                    chords[n][chordReplaceIndex] = note;

                    if (timeMode == 4)
                        loopSynths.get(n * chords[0].length + chordReplaceIndex).addPitch(
                                c0Freq * Math.pow(2, note / (double) tet),
                                time + pulseLength * (n * chords[0].length + chordReplaceIndex));
                    break;
                }
            }
        }
        System.out.println(target +" " + chords[0][1] + " " + chords[0][2] + " " + chords[0][3] + " " + chords[0][0]);
       //remove zero padding
        for(int i = 0; i < chords.length; i++){
            int n = 0;
            for(n = chords[i].length - 1; n >= 0 && chords[i][n] == 0; n--){

            }
            n++;
            chords[i] = Arrays.copyOf(chords[i], n);
        }

        return chords;
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
        System.out.println("DRONE NOTE: " + note);
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
