import java.util.Arrays;
import java.util.Random;

public class VoiceProgs extends Synth {
    public VoiceProgs(){
        Synth.rand = new Random(123);
        Synth.spatialize = false;
        spatializer = new Spatializer(Math.PI * 2 / (20 + 20 * rand.nextDouble()), Math.PI * 2 * rand.nextDouble());
    }
    public void childWriteNote(float[][] frames, double time, double freq, double vol, double[] pan) {

    }

    public static void susProg() {
        Synth.spatialize = false;
        WaveWriter ww = new WaveWriter("susprog2_");
        int chords[][] = new int[][] { { 3 + 15 * 3, 6 + 15 * 3, 11 + 15 * 3, 0 + 15 * 4 },
                { 13 + 15 * 2, 5 + 15 * 3, 10 + 15 * 3, 1 + 15 * 4 } ,
                { 13 + 15 * 2, 5 + 15 * 3, 10 + 15 * 3, 1 + 15 * 4 },
                { 3 + 15 * 3, 6 + 15 * 3, 10 + 15 * 3, 0 + 15 * 4 }
            };


            double[] prog = new double[WaveWriter.SAMPLE_RATE * 30];
        for (int i = 1; i <= 4; i++) {
            double[] sig = ReadSound.readSoundDoubles("voiceProgs/prog2_-0" + i + ".wav");

            if (sig.length < 24 * WaveWriter.SAMPLE_RATE) {
                sig = Arrays.copyOf(sig, 24 * WaveWriter.SAMPLE_RATE);
            }
            double max = 0;
            for (int n = 0; n < sig.length; n++) {
                max = Math.max(Math.abs(sig[n]), max);
            }
            for (int n = 0; n < sig.length; n++) {
                sig[n] /= max;
            }
            for (int s = 0; s <= 3; s += 1) {
                int sec = s * 6;
                double[] sus = Arrays.copyOfRange(sig, (int) ((sec + 2) * WaveWriter.SAMPLE_RATE),
                        (int) ((sec + 2.1) * WaveWriter.SAMPLE_RATE));

                int susSec = 6;
                if(s == 3)
                    susSec = 12;
                sus = StretchSynth.sustainedSignal(sus, susSec);



                max = 0;
                for (int n = 0; n < sus.length; n++) {
                    max = Math.max(Math.abs(sus[n]), max);
                }
                for (int n = 0; n < sus.length; n++) {
                    sus[n] /= max;
                }
                double[] conv = FFT2.convAsImaginaryProduct(sus, sus);
                conv = Arrays.copyOf(conv, sus.length);
                max = 0;
                for (int n = 0; n < conv.length; n++) {
                    max = Math.max(Math.abs(conv[n]), max);
                }
                for (int n = 0; n < conv.length; n++) {
                    conv[n] /= max;
                }

                
                //sus = BPF.BPF(sus,WaveWriter.SAMPLE_RATE, Piece.c0Freq * Math.pow(2,chords[s][i-1] / 15.0),0.001);
                max = 0;
                for (int n = 0; n < sus.length; n++) {
                    max = Math.max(Math.abs(sus[n]), max);
                }
                for (int n = 0; n < sus.length; n++) {
                    sus[n] /= max;
                }
                     
                for (int n = 0; n < sus.length; n++) {
                    sus[n] = 0.2 * sus[n] + 0.8 * conv[n];
                }

                int startFrame = (int) (WaveWriter.SAMPLE_RATE * sec);
                for (int j = 0; j < susSec * WaveWriter.SAMPLE_RATE; j++) {
                    double t = j / (double) (6 * WaveWriter.SAMPLE_RATE);
                    if (s % 2 == 0)
                        t = 1 - t;
                    double env = Math.pow(10, -1 * t);

                    double mix = 1;
                    double mixFrames = (int) (WaveWriter.SAMPLE_RATE * 0.25);

                    int sigOffset = 0;
                    if (j < mixFrames) {
                        mix = j / mixFrames;
                        if (s % 2 == 0) {
                            double e1 = 1 - Math.abs(j - 0.5 * mixFrames) / (0.5 * mixFrames);
                            env = e1 + (1 - e1) * env;
                            // System.out.println(e1 + " " + env);
                        }
                    }
                    else if(s == 3)
                        sigOffset = -6 * WaveWriter.SAMPLE_RATE;
                    if(s % 2 != 0){
                        mixFrames *= 4;
                    }
                    
                    if (j > (susSec * WaveWriter.SAMPLE_RATE) - mixFrames) {
                       
                        mix = ((susSec * WaveWriter.SAMPLE_RATE) - j) / (mixFrames);
                    }

                    
                    prog[startFrame + j] += env * (sig[startFrame + j + sigOffset] * (1 - mix) + mix * sus[j]);
                }
                if(s == 3)
                    for (int n = startFrame + 12 * WaveWriter.SAMPLE_RATE; n < sig.length; n++) {
                        double env = 1;
                        if(n > sig.length - 100){
                            env = (sig.length - n) / 100.0;
                        }
                        prog[n] += env * sig[n - 6 * WaveWriter.SAMPLE_RATE];
                    }
                    for(int n = 0; n < sig.length - WaveWriter.SAMPLE_RATE; n ++){
                        double env = 1;
                        if(n > sig.length - 2 * WaveWriter.SAMPLE_RATE)
                            env = (sig.length - 1 * WaveWriter.SAMPLE_RATE - n) / (double)WaveWriter.SAMPLE_RATE;
                        prog[n] += env * 0.01 * sig[n];
                    }
            }

        }

        VoiceProgs vp = new VoiceProgs();
        vp.mix = 0.8;
        prog = vp.addReverb(prog);
        for(int i = 0; i < prog.length; i++){
            ww.df[0][i] += prog[i] * Math.pow(10, -1);
        }
        ww.render(1);
    }

    public static void addRevToLulliby(){
        WaveWriter ww = new WaveWriter("37");

        double[] sig = ReadSound.readSoundDoubles("37dry.wav");
        VoiceProgs vp = new VoiceProgs();
        vp.mix = 0.65;
        sig = vp.addReverb(sig);


        for(int i = 0; i < sig.length; i++)
            ww.df[0][i] += sig[i];
        ww.render(1);
    }

    public void prog1() {
        loadSampleFreqs();
        WaveWriter ww = new WaveWriter("voiceprog1");
        OmniRegisterSynth synth = new OmniRegisterSynth();
        synth.useGlass = false;
        for (int i = 1; i <= 4; i++) {
            double[] sig = ReadSound.readSoundDoubles("voiceProgs/prog1_-0" + i + ".wav");
            // sig = FFT2.convAsImaginaryProduct(sig, sig);

            for (int n = 0; n < sig.length - 2048; n += 2048) {
                System.out.println(n / (double) sig.length);
                double[] samp = Arrays.copyOfRange(sig, n, n + 4096);
                for (int j = 0; j < 2048; j++) {
                    samp[j] *= j / 2048.0;
                    samp[samp.length - j - 1] *= j / 2048.0;
                }
                double[][] freqDom = FFT2.forwardTransform(samp);
                double[] unNorm = Arrays.copyOf(freqDom[0], freqDom[0].length);
                double max = 0;
                for (int j = 0; j < freqDom[0].length; j++) {
                    max = Math.max(max, freqDom[0][j]);
                }
                for (int j = 0; j < freqDom[0].length; j++) {
                    freqDom[0][j] /= max;
                }

                for (int k = 0; k < 30; k++) {
                    double rand = Math.random();
                    double tot = 0;

                    for (int j = 0; j < freqDom[0].length; j++) {
                        tot += freqDom[0][j];
                        if (tot >= rand) {
                            double f = WaveWriter.SAMPLE_RATE * (j + 1) / (double) freqDom[0].length;
                            double t = (n + (2048 * Math.random())) / (double) WaveWriter.SAMPLE_RATE;
                            int startFrame = (int) (t * WaveWriter.SAMPLE_RATE);
                            SampleFreq sf = getClosestSampleFreq(f);
                            double[] sp = pitchShift(sf.dry, sf.freq, f);
                            int len = WaveWriter.SAMPLE_RATE / 10;
                            sp = Arrays.copyOf(sp, len);
                            for (int l = 0; l < (len * 0.5); l++) {
                                sp[l] *= l / (len * 0.5);
                                sp[sp.length - 1 - l] *= l / (len * 0.5);
                            }

                            for (int l = 0; l < (len); l++) {
                                ww.df[0][startFrame + l] += unNorm[j] * 0.001 * sp[l];
                            }
                            break;
                        }
                    }
                }
            }
        }
        ww.render(1);
    }

    public static void main(String[] args) {
        // new VoiceProgs().prog1();
        // foregroundReferenceNotes();
        // convoluteVoiceWithSynth();


        //susProg();
         //lulliby();

         //iProgNearEnd();

         addRevToLulliby();

    }

    public static void iProgNearEnd(){
//14    12
//{ 2, 5, 10, 14}, { 0, 4, 9, 12 }
//  2, 5, 9, 14       0, 5, 9, 12          

WaveWriter ww = new WaveWriter("iProgRefernceNotes");

Synth synth = new Synth() {
    public void childWriteNote(float[][] frames, double time, double freq, double startVol, double[] pan) {
        int startFrame = (int) (time * WaveWriter.SAMPLE_RATE);
        for (int i = 0; i < 6 * WaveWriter.SAMPLE_RATE; i++) {
            double env = i / (double) (6 * WaveWriter.SAMPLE_RATE);
            if (startVol == 1)
                env = 1 - env;
            for (int n = 0; n < pan.length; n++)
                frames[n][i + startFrame] += pan[n] * env
                        * Math.sin(freq * Math.PI * 2 * i / (double) WaveWriter.SAMPLE_RATE);
        }
    }
};
// synth.mix = 0.5;
int chord[] = new int[] { 2 + 15 * 3, 5 + 15 * 3, 9 + 15 * 3, 14 + 15 * 3 };

int[][] octs = { { 0, -1, 2 }, { 0, 0, 2 }, { 0, -1, 2 }, { 0, -1, 1 }, { 0, 1, 2 } };
octs = new int[][] { { 0 }, { 0 }, { 0 }, { 0 }, { 0 } };
double time = 4;
for (int n = 0; n < chord.length; n++) {
    int note = chord[n];
    double[] pan = new double[5];
    pan[n] = 1;
    for (int o : octs[n])
        synth.writeNote(ww.df, time, Piece.c0Freq * Math.pow(2, note / 15.0) * Math.pow(2, o) * Math.pow(2, 7 / 12.0) / 2.0, 0, pan);
}
// 6,0,3,11, 5,1, 13, 10,at6
chord = new int[] { 0 + 15 * 3, 5 + 15 * 3, 9 + 15 * 3, 12 + 15 * 3 };
for (int n = 0; n < chord.length; n++) {
    int note = chord[n];
    double[] pan = new double[5];
    pan[n] = 1;
    for (int o : octs[n])
        synth.writeNote(ww.df, time + 6, Piece.c0Freq * Math.pow(2, note / 15.0) * Math.pow(2, o)* Math.pow(2, 7 / 12.0) / 2.0, 1, pan);
}

for (int s = 0; s < 16; s++) {

    int startFrame = (int) (s * WaveWriter.SAMPLE_RATE);
    for (int n = 0; n < WaveWriter.SAMPLE_RATE / 10; n++) {
        for (int i = 0; i < 5; i++)
            ww.df[i][n + startFrame] += (1 - n / (WaveWriter.SAMPLE_RATE * 0.1)) * (Math.random() * 2 - 1);
    }
    // click.writeNote(ww.df, s, 440, 1, new double[]{1});
}
ww.render(4);
    }

    public static void lulliby() {

        WaveWriter ww = new WaveWriter("lulliby");
        int[] mode = { 0, 1, 3, 5, 6, 8, 10, 11, 13 };
        int[] melody = { 6, 7, 14, 6, 7, 16, 7, 11, 18, 16, 12, 16, 14};
        double[] rhyth = { 1, 1, 2, 1, 1, 2, 1, 1, 2, 2, 1, 1, 4 };

       

        int[] trans = { 0, 3, 5, 7};

        for (int voice = 0; voice < trans.length; voice++) {
            double time = 4;
            for (int i = 0; i < melody.length; i++) {
                int tet12Note = melody[i];
                //int mod12 = (tet12Note + 5) % 12 + 24;
                double tet12Octs = 2 + (tet12Note + 5) / 12.0;
                double resid = 15;
                double origResid = 15;
                int closest = 0;
                for (int n = 0; n < mode.length; n++) {
                    int tet15Note = (mode[n] + 14) % 15;
                    double tet15Octs = tet15Note / 15.0;
                    while (Math.abs(tet12Octs - (tet15Octs + 1)) < Math.abs(tet12Octs - tet15Octs)) {
                        tet15Octs += 1;
                    }
                    if (Math.abs(tet15Octs - tet12Octs) < Math.abs(origResid)) {
                        double orig15TetOct = tet15Octs;
                        origResid = orig15TetOct - tet12Octs;
                        tet15Note = (mode[(n + trans[voice]) % mode.length] + 14) % 15;
                        tet15Octs = tet15Note / 15.0;
                        while (tet15Octs < orig15TetOct) {
                            tet15Octs += 1;
                        }
                        resid = tet15Octs - tet12Octs;
                        closest = tet15Note;
                    }
                }
                System.out.print(closest + " ");
                tet12Octs += resid;
                tet12Octs += 1;

                double freq = Piece.c0Freq * Math.pow(2, tet12Octs) * Math.pow(2, 7 / 12.0) / 2.0;

                int startFrame = (int) (WaveWriter.SAMPLE_RATE * time);
                int durFrames = (int) (WaveWriter.SAMPLE_RATE * rhyth[i]);
                for (int n = 0; n < durFrames; n++) {
                    double env = 1;
                    if (n < 100) {
                        env = n / 100.0;
                    }
                    if (n > durFrames - 100) {
                        env = (durFrames - n) / 100.0;
                    }
                    ww.df[voice][startFrame + n] += env
                            * Math.sin(Math.PI * 2 * freq * n / (double) WaveWriter.SAMPLE_RATE);

                            if(voice == 0)
                            ww.df[voice][startFrame + n] += env
                            * Math.sin(2 * Math.PI * 2 * freq * n / (double) WaveWriter.SAMPLE_RATE);
                }
                time += rhyth[i];
            }
            System.out.println();
           
        }

        for (int s = 0; s < 36; s++) {

            int startFrame = (int) (s * WaveWriter.SAMPLE_RATE);
            for (int n = 0; n < WaveWriter.SAMPLE_RATE / 10; n++) {
                for (int i = 0; i < 5; i++)
                    ww.df[i][n + startFrame] += (1 - n / (WaveWriter.SAMPLE_RATE * 0.1)) * (Math.random() * 2 - 1);
            }
            // click.writeNote(ww.df, s, 440, 1, new double[]{1});
        }
        ww.render(4);

    }

    public static void convoluteVoiceWithSynth() {
        WaveWriter ww = new WaveWriter("voiceConv");
        Synth synth = new StretchSynth(0);

        int chord[] = new int[] { 3 + 15 * 3, 6 + 15 * 3, 11 + 15 * 3, 0 + 15 * 4 };

        double time = 0;
        float[][] synthSig = new float[4][60 * WaveWriter.SAMPLE_RATE];
        double[][] voiceSig = new double[4][];
        for (int n = 0; n < 4; n++) {
            voiceSig[n] = ReadSound.readSoundDoubles("voiceProgs/prog1_-0" + (n + 1) + ".wav");
        }
        for (int n = 0; n < chord.length; n++) {
            int note = chord[n];
            double[] pan = new double[4];
            pan[n] = 1;
            synth.writeNote(synthSig, time, Piece.c0Freq * Math.pow(2, note / 15.0), 0, pan);

        }
        // 6,0,3,11, 5,1, 13, 10,at6
        chord = new int[] { 13 + 15 * 2, 5 + 15 * 3, 10 + 15 * 3, 1 + 15 * 4 };
        for (int n = 0; n < chord.length; n++) {
            int note = chord[n];
            double[] pan = new double[4];
            pan[n] = 1;
            synth.writeNote(synthSig, time + 6, Piece.c0Freq * Math.pow(2, note / 15.0), 1, pan);
        }
        for (int n = 0; n < 4; n++) {
            double[] synthSigDoubles = new double[synthSig[n].length];
            for (int i = 0; i < synthSigDoubles.length; i++) {
                synthSigDoubles[i] += synthSig[n][i];
            }
            voiceSig[n] = FFT2.convAsImaginaryProduct(voiceSig[n], synthSigDoubles);
            for (int i = 0; i < voiceSig[n].length; i++) {
                ww.df[0][i] += voiceSig[n][i];
            }
        }
        ww.render(1);
    }

    public static void foregroundReferenceNotes() {
        // { 0, 5, 9, 12 }, { 0, 4, 9, 12 }

        // {6,11,0,3} {1,5,10,13} {3,7,12,0} {8,13,2,5} <CHORD PROGRESSION
        // {1,6,10,0} {3,8,12,2} <BACKGROUNDS (2 and 2)

        // {6,10,0,3} {1,5,11,13} {3,7,13,0} {8,12,2,5} <CHORD PROGRESSION (T5)

        // {11,0,5,3?} {1,6,10,13} {3,8,12,0} {13,2,7,5?} <CHORD PROGRESSION (T-5)
        // should be 8 and 10, but modified for horizontal relations

        // different approach (second half is T5 of first) //switch last two chords in
        // both progressions
        // {6,11,0,3} {1,5,10,13} T5 {6,10,0,3} {1,5,11,13}
        // {11,0,5,3} {6,10,1,13} {6,10,0,13} {11,1,5,8}
        WaveWriter ww = new WaveWriter("refernceNotes");

        Synth synth = new Synth() {
            public void childWriteNote(float[][] frames, double time, double freq, double startVol, double[] pan) {
                int startFrame = (int) (time * WaveWriter.SAMPLE_RATE);
                for (int i = 0; i < 6 * WaveWriter.SAMPLE_RATE; i++) {
                    double env = i / (double) (6 * WaveWriter.SAMPLE_RATE);
                    if (startVol == 1)
                        env = 1 - env;
                    for (int n = 0; n < pan.length; n++)
                        frames[n][i + startFrame] += pan[n] * env
                                * Math.sin(freq * Math.PI * 2 * i / (double) WaveWriter.SAMPLE_RATE);
                }
            }
        };
        // synth.mix = 0.5;
        int chord[] = new int[] { 3 + 15 * 3, 6 + 15 * 3, 11 + 15 * 3, 0 + 15 * 4 };

        int[][] octs = { { 0, -1, 2 }, { 0, 0, 2 }, { 0, -1, 2 }, { 0, -1, 1 }, { 0, 1, 2 } };
        octs = new int[][] { { 0 }, { 0 }, { 0 }, { 0 }, { 0 } };
        double time = 91;
        for (int n = 0; n < chord.length; n++) {
            int note = chord[n];
            double[] pan = new double[5];
            pan[n] = 1;
            for (int o : octs[n])
                synth.writeNote(ww.df, time, Piece.c0Freq * Math.pow(2, note / 15.0) * Math.pow(2, o), 0, pan);
        }
        // 6,0,3,11, 5,1, 13, 10,at6
        chord = new int[] { 13 + 15 * 2, 5 + 15 * 3, 10 + 15 * 3, 1 + 15 * 4 };
        for (int n = 0; n < chord.length; n++) {
            int note = chord[n];
            double[] pan = new double[5];
            pan[n] = 1;
            for (int o : octs[n])
                synth.writeNote(ww.df, time + 6, Piece.c0Freq * Math.pow(2, note / 15.0) * Math.pow(2, o), 1, pan);
        }

        // 13, 1, 5, 11 3, 0, 6, 10
        // synth.mix = 0.1;
        chord = new int[] { 13 + 15 * 2, 5 + 15 * 3, 11 + 15 * 3, 1 + 15 * 4 };

        for (int n = 0; n < chord.length; n++) {
            int note = chord[n];
            double[] pan = new double[5];
            pan[n] = 1;
            for (int o : octs[n])
                synth.writeNote(ww.df, time + 12, Piece.c0Freq * Math.pow(2, note / 15.0) * Math.pow(2, o), 0, pan);
        }

        chord = new int[] { 3 + 15 * 3, 6 + 15 * 3, 10 + 15 * 3, 0 + 15 * 4 };

        for (int n = 0; n < chord.length; n++) {
            int note = chord[n];
            double[] pan = new double[5];
            pan[n] = 1;
            for (int o : octs[n])
                synth.writeNote(ww.df, time + 18, Piece.c0Freq * Math.pow(2, note / 15.0) * Math.pow(2, o), 1, pan);
        }

        // synth.mix = 0.5;

        // second 15 TET
        chord = new int[] { 3 + 15 * 3, 5 + 15 * 3, 11 + 15 * 3, 0 + 15 * 4 };
        time = 136;
        for (int n = 0; n < chord.length; n++) {
            int note = chord[n];
            double[] pan = new double[5];
            pan[n] = 1;
            for (int o : octs[n])
                synth.writeNote(ww.df, time, 2 * Piece.c0Freq * Math.pow(2, note / 15.0) * Math.pow(2, o), 0, pan);
        }

        chord = new int[] { 13 + 15 * 2, 6 + 15 * 3, 10 + 15 * 3, 1 + 15 * 4 };
        for (int n = 0; n < chord.length; n++) {
            int note = chord[n];
            double[] pan = new double[5];
            pan[n] = 1;
            for (int o : octs[n])
                synth.writeNote(ww.df, time + 6, 2 * Piece.c0Freq * Math.pow(2, note / 15.0) * Math.pow(2, o), 1, pan);
        }

        chord = new int[] { 8 + 15 * 2, 5 + 15 * 3, 11 + 15 * 3, 1 + 15 * 4 };
        for (int n = 0; n < chord.length; n++) {
            int note = chord[n];
            double[] pan = new double[5];
            pan[n] = 1;
            for (int o : octs[n]) {
                synth.writeNote(ww.df, time + 14, 2 * Piece.c0Freq * Math.pow(2, note / 15.0) * Math.pow(2, o), 0, pan);
                synth.writeNote(ww.df, time + 14, Piece.c0Freq * Math.pow(2, note / 15.0) * Math.pow(2, o), 0, pan);
            }
        }
        chord = new int[] { 6 + 15 * 3, 10 + 15 * 3, 13 + 15 * 3, 0 + 15 * 4 };
        for (int n = 0; n < chord.length; n++) {
            int note = chord[n];
            double[] pan = new double[5];
            pan[n] = 1;
            for (int o : octs[n]) {
                synth.writeNote(ww.df, time + 20, 2 * Piece.c0Freq * Math.pow(2, note / 15.0) * Math.pow(2, o), 1, pan);
                synth.writeNote(ww.df, time + 20, Piece.c0Freq * Math.pow(2, note / 15.0) * Math.pow(2, o), 1, pan);
            }
        }

        // 1st 21 TET
        octs = new int[][] { { 0 }, { 0 }, { 0 }, { 0 }, { 0 } };
        // 15, 2, 7, 12, 0 14,1,5,10,19
        chord = new int[] { 0 + 21 * 3, 3 + 21 * 3, 7 + 21 * 3, 12 + 21 * 3, 15 + 21 * 3 };
        time = 275;

        for (int n = 0; n < chord.length; n++) {
            int note = chord[n];
            double[] pan = new double[5];
            pan[n] = 1;
            for (int o : octs[n]) {
                synth.writeNote(ww.df, time, Piece.c0Freq * Math.pow(2, note / 21.0) * Math.pow(2, o), 0, pan);
            }
        }

        // 3?
        chord = new int[] { 19 + 21 * 2, 1 + 21 * 3, 5 + 21 * 3, 10 + 21 * 3, 14 + 21 * 3 };
        for (int n = 0; n < chord.length; n++) {
            int note = chord[n];
            double[] pan = new double[5];
            pan[n] = 1;
            for (int o : octs[n]) {
                synth.writeNote(ww.df, time + 6, Piece.c0Freq * Math.pow(2, note / 21.0) * Math.pow(2, o), 0, pan);
            }
        }
        for (int n = 0; n < chord.length; n++) {// fade back out
            int note = chord[n];
            double[] pan = new double[5];
            pan[n] = 1;
            for (int o : octs[n]) {
                synth.writeNote(ww.df, time + 12, Piece.c0Freq * Math.pow(2, note / 21.0) * Math.pow(2, o), 1, pan);
            }
        }

        // octs = new int[][]{{0,-1},{0,-1},{0,-1,-2},{0,-1,-2},{0,1,-1}};
        // 10 14 19 1 7 12 15 3 0 5

        chord = new int[] { 19 + 21 * 2, 7 + 21 * 3, 10 + 21 * 3, 14 + 21 * 3, 1 + 21 * 3 };
        for (int n = 0; n < chord.length; n++) {
            int note = chord[n];
            double[] pan = new double[5];
            pan[n] = 1;
            for (int o : octs[n]) {
                synth.writeNote(ww.df, time + 16, Piece.c0Freq * Math.pow(2, note / 21.0) * Math.pow(2, o), 0, pan);
            }
        }
        chord = new int[] { 15 + 21 * 2, 0 + 21 * 3, 3 + 21 * 3, 5 + 21 * 3, 12 + 21 * 3 };
        // synth = metalSynth;
        for (int n = 0; n < chord.length; n++) {
            int note = chord[n];
            double[] pan = new double[5];
            pan[n] = 1;
            for (int o : octs[n]) {

                synth.writeNote(ww.df, time + 22, Piece.c0Freq * Math.pow(2, note / 21.0) * Math.pow(2, o), 1, pan);
            }
        }

        // octs = new int[][]{{0,-1},{0},{0,-1,2},{0,-1,1},{0,1,2}};

        // miscileneous chords
        time = 241;
        chord = new int[] { 0 + 21 * 2, 3 + 21 * 3, 7 + 21 * 3, 12 + 21 * 3, 15 + 21 * 3 };
        for (int n = 0; n < chord.length; n++) {
            int note = chord[n];
            double[] pan = new double[5];
            pan[n] = 1;
            for (int o : octs[n])
                synth.writeNote(ww.df, time, Piece.c0Freq * Math.pow(2, note / 21.0) * Math.pow(2, o), 2, pan);
        }
        // 3?
        chord = new int[] { 19 + 21 * 2, 1 + 21 * 3, 5 + 21 * 3, 10 + 21 * 3, 14 + 21 * 3 };
        for (int n = 0; n < chord.length; n++) {
            int note = chord[n];
            double[] pan = new double[5];
            pan[n] = 1;
            for (int o : octs[n])
                synth.writeNote(ww.df, time + 6, Piece.c0Freq * Math.pow(2, note / 21.0) * Math.pow(2, o), 1, pan);
        }

        // 33 tet progression at 7 minutes
        // {25,0,5,11,19} {3,8,14,22,27} T11 {3, 11, 27, 22, 8} {14, 19, 25, 0, 5}
        // {25, 11, 3,22} {3, 22, 14,0}

        // 33 TET

        // 25 0 5 11 19 27 3 8 14 22
        chord = new int[] { 0 + 33 * 3, 3 + 33 * 3, 8 + 33 * 3, 22 + 33 * 3, 27 + 33 * 3 };
        // {25 + 21 * 4, 0 + 21 * 5, 5 + 21 * 5, 11 + 21 * 5, 19 + 21 * 5,25 + 21 * 5};
        time = 6 * 60 + 30;
        for (int n = 0; n < chord.length; n++) {
            int note = chord[n];
            double[] pan = new double[5];
            pan[n] = 1;
            for (int o : octs[n]) {
                synth.writeNote(ww.df, time, Piece.c0Freq * Math.pow(2, note / 33.0) * Math.pow(2, o), 0, pan);
            }
        }

        chord = new int[] { 27 + 33 * 2, 0 + 33 * 3, 14 + 33 * 3, 19 + 33 * 3, 25 + 33 * 3 };
        // {3 + 21 * 5,8 + 21 * 5,14+ 21 * 5,22 + 21 * 5, 27 + 21 * 5};
        for (int n = 0; n < chord.length; n++) {
            int note = chord[n];
            double[] pan = new double[5];
            pan[n] = 1;
            for (int o : octs[n]) {
                synth.writeNote(ww.df, time + 6, Piece.c0Freq * Math.pow(2, note / 33.0) * Math.pow(2, o), 1, pan);
            }
        }

        // 22 27 3 8 11 19 25 0 5 14
        chord = new int[] { 22 + 33 * 2, 25 + 33 * 2, 0 + 33 * 3, 5 + 33 * 3, 19 + 33 * 3 };
        // {11 + 21 * 4,8 + 21 * 5,22 + 21 * 5, 27 + 21 * 5,3+ 21 * 6};

        for (int n = 0; n < chord.length; n++) {
            int note = chord[n];
            double[] pan = new double[5];
            pan[n] = 1;
            for (int o : octs[n]) {
                synth.writeNote(ww.df, time + 12, Piece.c0Freq * Math.pow(2, note / 33.0) * Math.pow(2, o), 0, pan);
            }
        }
        chord = new int[] { 25 + 33 * 2, 27 + 33 * 2, 3 + 33 * 3, 8 + 33 * 3, 22 + 33 * 3 };
        // { 0 + 21 * 5, 5 + 21 * 5, 14 + 21 * 5, 19 + 21 * 5, 25 + 21 * 5};
        for (int n = 0; n < chord.length; n++) {
            int note = chord[n];
            double[] pan = new double[5];
            pan[n] = 1;
            for (int o : octs[n]) {

                synth.writeNote(ww.df, time + 18, Piece.c0Freq * Math.pow(2, note / 33.0) * Math.pow(2, o), 1, pan);
            }
        }

        // 2nd 33 tet

        // 25 0 5 11 19 27 3 8 14 22
        chord = new int[] { 19 + 33 * 3, 25 + 33 * 3, 0 + 33 * 3, 5 + 33 * 3, 11 + 33 * 4 };
        // {25 + 21 * 4, 0 + 21 * 5, 5 + 21 * 5, 11 + 21 * 5, 19 + 21 * 5,25 + 21 * 5};
        time = 7 * 60;
        for (int n = 0; n < chord.length; n++) {
            int note = chord[n];
            double[] pan = new double[5];
            pan[n] = 1;
            for (int o : octs[n]) {
                synth.writeNote(ww.df, time, Piece.c0Freq * Math.pow(2, note / 33.0) * Math.pow(2, o), 0, pan);
            }
        }

        chord = new int[] { 22 + 33 * 3, 27 + 33 * 3, 3 + 33 * 4, 8 + 33 * 4, 14 + 33 * 4 };
        // {3 + 21 * 5,8 + 21 * 5,14+ 21 * 5,22 + 21 * 5, 27 + 21 * 5};
        for (int n = 0; n < chord.length; n++) {
            int note = chord[n];
            double[] pan = new double[5];
            pan[n] = 1;
            for (int o : octs[n]) {
                synth.writeNote(ww.df, time + 6, Piece.c0Freq * Math.pow(2, note / 33.0) * Math.pow(2, o), 1, pan);
            }
        }

        // 22 27 3 8 11 19 25 0 5 14
        chord = new int[] { 11 + 33 * 3, 22 + 33 * 3, 27 + 33 * 3, 3 + 33 * 3, 8 + 33 * 4 };
        // {11 + 21 * 4,8 + 21 * 5,22 + 21 * 5, 27 + 21 * 5,3+ 21 * 6};
        for (int n = 0; n < chord.length; n++) {
            int note = chord[n];
            double[] pan = new double[5];
            pan[n] = 1;
            for (int o : octs[n]) {
                synth.writeNote(ww.df, time + 12, Piece.c0Freq * Math.pow(2, note / 33.0) * Math.pow(2, o), 0, pan);
                // voiceSynth.writeNote(ww.df, time + 12, Piece.c0Freq * Math.pow(2, note /
                // 33.0) * Math.pow(2,o), 0, pan);
            }
        }
        chord = new int[] { 14 + 33 * 3, 19 + 33 * 3, 25 + 33 * 3, 0 + 33 * 4, 5 + 33 * 4 };
        octs = new int[][] { { 0 }, { 0 }, { 0 }, { 0 }, { 0 } };

        // { 0 + 21 * 5, 5 + 21 * 5, 14 + 21 * 5, 19 + 21 * 5, 25 + 21 * 5};
        for (int n = 0; n < chord.length; n++) {
            int note = chord[n];
            double[] pan = new double[5];
            pan[n] = 1;
            for (int o : octs[n]) {
                synth.writeNote(ww.df, time + 18, Piece.c0Freq * Math.pow(2, note / 33.0) * Math.pow(2, o), 1, pan);
                // glassSynth.writeNote(ww.df, time+18, Piece.c0Freq * Math.pow(2, note / 33.0)
                // * Math.pow(2,o), 1, pan);
            }
        }

        // Synth click = new SampleSynth(0);
        for (int s = 0; s < 60 * 12; s++) {

            int startFrame = (int) (s * WaveWriter.SAMPLE_RATE);
            for (int n = 0; n < WaveWriter.SAMPLE_RATE / 10; n++) {
                for (int i = 0; i < 5; i++)
                    ww.df[i][n + startFrame] += (1 - n / (WaveWriter.SAMPLE_RATE * 0.1)) * (Math.random() * 2 - 1);
            }
            // click.writeNote(ww.df, s, 440, 1, new double[]{1});
        }

        ww.render(5);

    }
}
