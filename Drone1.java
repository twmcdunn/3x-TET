import java.util.Arrays;
import java.util.ArrayList;
import org.apache.commons.math3.complex.Complex;

public class Drone1 extends Synth {
    public double vol;

    public Drone1() {
        loadSampleFreqs();
        spatializer = new Spatializer(Math.PI * 2 / (20 + 20 * rand.nextDouble()), Math.PI * 2 * rand.nextDouble());
        vol = 0.1;
    }

    public void childWriteNote(float[][] frames, double time, double freq, double startVol, double[] pan) {

        ArrayList<double[]> parts = new ArrayList<double[]>();
        
        parts.add(new double[]{Math.pow(2, 5/33.0),0.99});
        parts.add(new double[]{Math.pow(2, 14/33.0),0.98});
        parts.add(new double[]{Math.pow(2, 19/33.0),0.98});
        parts.add(new double[]{Math.pow(2, 25/33.0),0.97});

        ArrayList<double[]> partsExpanded = new ArrayList<double[]>();
        partsExpanded.addAll(parts);

        for(int i = 0; i < 2; i++){
            ArrayList<double[]> partsToAdd = new ArrayList<double[]>();
            for (double[] p1 : parts)
                for (double[] p2 : partsExpanded)
                    partsToAdd.add(new double[] { p1[0] * p2[0], p1[1] * p2[1] });
            partsExpanded.addAll(partsToAdd);
        }
        parts = partsExpanded;
        double[] partials = new double[parts.size() + 1];
        double[] vols = new double[parts.size() + 1];
        for (int i = 1; i < partials.length; i++) {
            partials[i] = parts.get(i-1)[0];
            vols[i] = parts.get(i-1)[1];
        }
        partials[0] = 1;
        vols[0] = 1;

        // 19 25 0 5 14
        double[] sig = new double[45 * WaveWriter.SAMPLE_RATE];
        for (int i = 0; i < partials.length; i++) {
            double f = partials[i];
            double v = vols[i];
            double[] s = writePartial(freq * f, v);
            double max = 0;
            for (int n = 0; n < s.length; n++) {
                max = Math.max(Math.abs(s[n]), max);
            }
            for (int n = 0; n < s.length; n++) {
                s[n] /= max;
            }

            double amFreq = 1 / (Math.random() * 2 + 1);
            double amPh = Math.PI * 2 * Math.random();
            for (int n = 0; n < sig.length; n++) {
                double am = (Math.sin(amPh + amFreq * n * Math.PI * 2 / (double) WaveWriter.SAMPLE_RATE) + 1) / 2.0;
                if (i == 0)
                    am = 1;
                sig[n] += s[n] * am;
            }
        }
        double max = 0;
        for (int i = 0; i < sig.length; i++) {
            max = Math.max(Math.abs(sig[i]), max);
        }
        for (int i = 0; i < sig.length; i++) {
            sig[i] /= max;
            sig[i] *= vol;
        }

        mix = 0.5;
        sig = addReverb(sig);
        int startFrame = (int) (time * WaveWriter.SAMPLE_RATE);
        for (int n = 0; n < pan.length; n++) {
            for (int i = 0; i < sig.length; i++) {
                frames[n][i + startFrame] += sig[i] * pan[n] * vol;
            }

        }
    }

    public double[] writePartial(double freq, double v) {
        SampleFreq sf = getClosestSampleFreq(freq);
        double[] sample = Arrays.copyOfRange(pitchShift(sf.dry, sf.freq, freq), WaveWriter.SAMPLE_RATE,
                WaveWriter.SAMPLE_RATE * 3 / 2);
        for (int i = 0; i < sample.length / 2; i++) {
            sample[i] *= 2 * i / (double) sample.length;
            sample[sample.length - 1 - i] *= 2 * i / (double) sample.length;
        }
        Complex[] compFreqDom = FFT2.forwardTransformComplex(sample);
        int sec = 45;
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
        double[] sig = new double[sec * WaveWriter.SAMPLE_RATE];
        for (int i = 0; i < sec * WaveWriter.SAMPLE_RATE; i++) {
            double t = i / (double) sig.length;
            sig[i] = timeDomain[i].getReal();

            double env = Math.pow(10, -Math.abs(0.5 - t) * 2 * 4);

            sig[i] *= env * v;
        }
        for (int i = 0; i < 500; i++) {
            sig[i] *= i / 500.0;
            sig[sig.length - 1 - i] *= i / 500.0;
        }
        return sig;
    }

    public static void test() {
        Synth synth = new Drone1();
        WaveWriter ww = new WaveWriter("drone1Test");

        synth.writeNote(ww.df, 0, 110, 1, new double[] { 1 });

        ww.render(1);
        if (true)
            return;

        int chord[] = new int[] { 6 + 15 * 4, 11 + 15 * 4, 0 + 15 * 5, 3 + 15 * 5 };
        chord = new int[] { 3 + 15 * 4, 6 + 15 * 4, 11 + 15 * 4, 0 + 15 * 5 };
        double time = 0;
        for (int n = 0; n < chord.length; n++) {
            int note = chord[n];
            synth.writeNote(ww.df, time, Piece.c0Freq * Math.pow(2, note / 15.0), 0, new double[] { 1 });
            if (n == 0 || n == 2 || n == 3)
                synth.writeNote(ww.df, time, Piece.c0Freq * Math.pow(2, note / 15.0) / 2, 0, new double[] { 1 });
            if (n == chord.length - 1) {
                synth.writeNote(ww.df, time, Piece.c0Freq * Math.pow(2, note / 15.0) * 2, 0, new double[] { 1 });
                synth.writeNote(ww.df, time, Piece.c0Freq * Math.pow(2, note / 15.0) * 4, 0, new double[] { 1 });
            }

            if (n == chord.length - 2) {
                synth.writeNote(ww.df, time, Piece.c0Freq * Math.pow(2, note / 15.0) * 2, 0, new double[] { 1 });
            }
            if (n == chord.length - 3) {
                synth.writeNote(ww.df, time, Piece.c0Freq * Math.pow(2, note / 15.0) * 4, 0, new double[] { 1 });
            }

        }
        // 6,0,3,11, 5,1, 13, 10,
        chord = new int[] { 13 + 15 * 3, 5 + 15 * 4, 10 + 15 * 4, 1 + 15 * 5 };

        for (int n = 0; n < chord.length; n++) {
            int note = chord[n];

            synth.writeNote(ww.df, time + 6, Piece.c0Freq * Math.pow(2, note / 15.0), 1, new double[] { 1 });
            if (n == 0 || n == 2 || n == 3)
                synth.writeNote(ww.df, time, Piece.c0Freq * Math.pow(2, note / 15.0) / 2, 1, new double[] { 1 });
            if (n == chord.length - 1) {
                synth.writeNote(ww.df, time + 6, Piece.c0Freq * Math.pow(2, note / 15.0) * 2, 1, new double[] { 1 });
                synth.writeNote(ww.df, time + 6, Piece.c0Freq * Math.pow(2, note / 15.0) * 4, 1, new double[] { 1 });
            }

            if (n == chord.length - 2) {
                synth.writeNote(ww.df, time + 6, Piece.c0Freq * Math.pow(2, note / 15.0) * 2, 1, new double[] { 1 });
            }
            if (n == chord.length - 3) {
                synth.writeNote(ww.df, time + 6, Piece.c0Freq * Math.pow(2, note / 15.0) * 4, 1, new double[] { 1 });
            }
            // synth.writeNote(ww.df, 0, Piece.c0Freq * Math.pow(2, note / 15.0) / 2, 0, new
            // double[] { 1 });

        }

        // 13, 1, 5, 11 3, 0, 6, 10

        chord = new int[] { 13 + 15 * 3, 1 + 15 * 5, 5 + 15 * 5, 11 + 15 * 5 };

        for (int n = 0; n < chord.length; n++) {
            int note = chord[n];
            synth.writeNote(ww.df, time + 12, Piece.c0Freq * Math.pow(2, note / 15.0), 0, new double[] { 1 });

            if (n == 0 || n == 2 || n == 3)
                synth.writeNote(ww.df, time + 12, Piece.c0Freq * Math.pow(2, note / 15.0) / 2, 0, new double[] { 1 });
            if (n == chord.length - 1) {
                synth.writeNote(ww.df, time + 12, Piece.c0Freq * Math.pow(2, note / 15.0) * 2, 0, new double[] { 1 });
                synth.writeNote(ww.df, time + 12, Piece.c0Freq * Math.pow(2, note / 15.0) * 4, 0, new double[] { 1 });
            }

            if (n == chord.length - 2) {
                synth.writeNote(ww.df, time + 12, Piece.c0Freq * Math.pow(2, note / 15.0) * 2, 0, new double[] { 1 });
            }
            if (n == chord.length - 3) {
                synth.writeNote(ww.df, time + 12, Piece.c0Freq * Math.pow(2, note / 15.0) * 4, 0, new double[] { 1 });
            }
            // synth.writeNote(ww.df, 0, Piece.c0Freq * Math.pow(2, note / 15.0) / 2, 0, new
            // double[] { 1 });

        }
        chord = new int[] { 3 + 15 * 4, 0 + 15 * 5, 6 + 15 * 5, 10 + 15 * 5 };
        time = 18;
        for (int n = 0; n < chord.length; n++) {
            int note = chord[n];
            synth.writeNote(ww.df, time + 18, Piece.c0Freq * Math.pow(2, note / 15.0), 1, new double[] { 1 });
            if (n == 0 || n == 2 || n == 3)
                synth.writeNote(ww.df, time + 18, Piece.c0Freq * Math.pow(2, note / 15.0) / 2, 1, new double[] { 1 });
            if (n == chord.length - 1) {
                synth.writeNote(ww.df, time + 18, Piece.c0Freq * Math.pow(2, note / 15.0) * 2, 1, new double[] { 1 });
                synth.writeNote(ww.df, time + 18, Piece.c0Freq * Math.pow(2, note / 15.0) * 4, 1, new double[] { 1 });
            }

            if (n == chord.length - 2) {
                synth.writeNote(ww.df, time + 18, Piece.c0Freq * Math.pow(2, note / 15.0) * 2, 1, new double[] { 1 });
            }
            if (n == chord.length - 3) {
                synth.writeNote(ww.df, time + 18, Piece.c0Freq * Math.pow(2, note / 15.0) * 4, 1, new double[] { 1 });
            }

            // synth.writeNote(ww.df, 0, Piece.c0Freq * Math.pow(2, note / 15.0) / 2, 0, new
            // double[] { 1 });

        }

        ww.render(1);
    }

    public static void main(String[] args) {
        test();
    }
}
