import java.util.Arrays;
import org.apache.commons.math3.complex.Complex;

public class StretchSynth extends Synth {
    public double vol;
    public int myType;
    public StretchSynth(int type) {
        spatializer = new Spatializer(Math.PI * 2 / (20 + 20 * rand.nextDouble()), Math.PI * 2 * rand.nextDouble());
        loadSampleFreqs();
        loadVoiceSampleFreqs();
        vol = 0.25;
        mix = 0.25;
        myType = type;
    }

    public void childWriteNote(float[][] frames, double time, double freq, double startVol, double[] pan) {
        SampleFreq sf = null;
        if(myType == 1)
            sf = getClosestVoiceSampleFreq(freq);
        else if(myType == 0)
            sf = getClosestSampleFreq(freq);
        else if(myType == 2)
            sf = new SampleFreq(ReadSound.readSoundDoubles("31.wav"),2055);
        /*
        if(freq > 85 && freq < 480)
            sf = getClosestVoiceSampleFreq(freq);
        else
            sf = getClosestSampleFreq(freq);
             */

             double[] ps = pitchShift(sf.dry, sf.freq, freq);
       
        double[] sample = null;
        if(ps.length > WaveWriter.SAMPLE_RATE * 3 / 2){
            sample = Arrays.copyOfRange(ps, WaveWriter.SAMPLE_RATE,
                WaveWriter.SAMPLE_RATE * 3 / 2);
        }
        else{
            sample = Arrays.copyOfRange(ps, WaveWriter.SAMPLE_RATE/10,
                WaveWriter.SAMPLE_RATE / 10 + WaveWriter.SAMPLE_RATE / 4);
        }
        int sec = 6;
        if(startVol == 1)
             sec = 12;
        double[] sig = sustainedSignal(sample, sec);

        for (int i = 0; i < sec * WaveWriter.SAMPLE_RATE + 48000; i++) {
            
            double t = i / (double) WaveWriter.SAMPLE_RATE;

            if (startVol == 1)
                t /= 2.0;

            if (startVol == 0 || startVol == 2)
                t = 6 - t;

            double env = Math.pow(10, -t / 4.0);

            t = i / (double) WaveWriter.SAMPLE_RATE;
            if(startVol == 2 && t < 0.3){
                double e1 = Math.abs(t-0.15)/0.15;
                env = env * e1 + (1-e1) * 1;
            }
            
            sig[i] *= env;
        }
        for (int i = 0; i < 48000; i++) {
            if(startVol != 2)
                sig[i] *= i / 48000.0;
            sig[sig.length - 1 - i] *= i / 48000.0;
        }
        if(startVol == 2){//just to smooth out sig, not an audible fade
            for (int i = 0; i < 100; i++) {
                    sig[i] *= i / 100.0;
            }
        }
        //mix = 0.5;
        sig = addReverb(sig);
        int startFrame = (int) (time * WaveWriter.SAMPLE_RATE);
        for (int n = 0; n < pan.length; n++) {
            for (int i = 0; i < sig.length; i++) {
                frames[n][i + startFrame] += sig[i] * pan[n] * vol;
            }

        }
    }

    public static double[] sustainedSignal(double[] sample, int sec){
        for (int i = 0; i < sample.length / 2; i++) {
            sample[i] *= 2 * i / (double) sample.length;
            sample[sample.length - 1 - i] *= 2 * i / (double) sample.length;
        }
        Complex[] compFreqDom = FFT2.forwardTransformComplex(sample);
        
        int numOfFrames = (int) Math.pow(2, (int) (Math.log(sec * WaveWriter.SAMPLE_RATE) / Math.log(2)) + 1);
        double[] longerSig = new double[numOfFrames];
        for (int i = 0; i < longerSig.length; i++) {
            longerSig[i] = Math.random() * 2 - 1;
        }
        Complex[] longerFreqDom = FFT2.forwardTransformComplex(longerSig);

        Complex[] stretchedFreqDom = new Complex[numOfFrames];
        for (int i = 0; i < stretchedFreqDom.length; i++) {
            double x = i / (double) stretchedFreqDom.length;
            double scaledX = x * (compFreqDom.length - 1);
            int flooredX = (int) scaledX;
            double fract = scaledX - flooredX;
            double r1, i1;
            if (fract > 0) {
                r1 = compFreqDom[flooredX].getReal() * (1 - fract) + compFreqDom[flooredX + 1].getReal() * fract;
                i1 = compFreqDom[flooredX].getImaginary() * (1 - fract)
                        + compFreqDom[flooredX + 1].getImaginary() * fract;
            } else {
                r1 = compFreqDom[flooredX].getReal();
                i1 = compFreqDom[flooredX].getImaginary();
            }
            double r2 = longerFreqDom[i].getReal();
            double i2 = longerFreqDom[i].getImaginary();
            double rProd = r1 * r2 - i1 * i2;
            double iProd = r1 * i2 + r2 * i1;
            stretchedFreqDom[i] = new Complex(rProd, iProd);
        }
        Complex[] timeDomain = FFT2.inverseTransform(stretchedFreqDom);
        double[] sig = new double[sec * WaveWriter.SAMPLE_RATE + 48000];
        for (int i = 0; i < sec * WaveWriter.SAMPLE_RATE + 48000; i++) {
            sig[i] = timeDomain[i].getReal();
        }
        return sig;
    }

    public static void test() {
        Synth synth = null;
        Synth voiceSynth = new StretchSynth(1);
        Synth metalSynth = new StretchSynth(0);
        WaveWriter ww = new WaveWriter("stretchTest");
        int[][] octs = {{0,-1},{0,0},{0,-1,2},{0,-1,1},{0,1,2}};

        double time = 0;

        synth = voiceSynth;
        int[] chord = new int[] { 0 + 21 * 5, 3 + 21 * 5, 7 + 21 * 5, 12 + 21 * 5, 15 + 21 * 5 };
        for (int n = 0; n < chord.length; n++) {
            int note = chord[n];
            double[] pan = new double[5];
            pan[n] = 1;
            for(int o: octs[n])
                synth.writeNote(ww.df, time, Piece.c0Freq * Math.pow(2, note / 21.0) * Math.pow(2,o), 2, pan);
        }
        // 3?
        synth = metalSynth;
        chord = new int[] { 19 + 21 * 4, 1 + 21 * 5, 5 + 21 * 5, 10 + 21 * 5, 14 + 21 * 5 };
        for (int n = 0; n < chord.length; n++) {
            int note = chord[n];
            double[] pan = new double[5];
            pan[n] = 1;
            for(int o: octs[n])
                synth.writeNote(ww.df, time + 6, Piece.c0Freq * Math.pow(2, note / 21.0) * Math.pow(2,o), 1, pan);
        }

       
        ww.render(1);
    }

   
}
