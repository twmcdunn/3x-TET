import java.util.Arrays;

public class Gong implements Synth {
    double f2;
    double[] sig, wetSig;

    public Gong() {
        f2 = 626;
        sig = ReadSound.readSoundDoubles("19.wav");// unpitched gong
        for(int i = 0; i < sig.length; i++){
            sig[i] *= Math.pow(10, -100 * (i / (double)sig.length));
        }
        double[] cathedral = ReadSound.readSoundDoubles("cathedral.wav");
        sig = Arrays.copyOf(sig, sig.length + cathedral.length);
        cathedral = Arrays.copyOf(cathedral, sig.length);
        wetSig = FFT2.convAsImaginaryProduct(sig, cathedral);
        wetSig = Arrays.copyOf(wetSig, sig.length);
        double sMax = 0;
        double wMax = 0;
        for (int i = 0; i < wetSig.length; i++) {
            sMax = Math.max(sMax, Math.abs(sig[i]));
            wMax = Math.max(wMax, Math.abs(wetSig[i]));
        }
        for (int i = 0; i < wetSig.length; i++) {
            sig[i] /= sMax;
            wetSig[i] /= wMax;
        }
    }

    public void writeNote(float[][] frames, double time, double freq, double vol, double[] pan) {
        // if(type != 17)
        // return;
double mix = vol * 0.5 + 0.5;
mix = 1 - vol;
        double[] reverb = new double[sig.length];
        for (int i = 0; i < reverb.length; i++) {
            reverb[i] = wetSig[i] * (1 - mix) + sig[i] * mix;
        }

        for (int i = 0; i < 100; i++)
            reverb[reverb.length - 1 - i] *= i / 100.0;

        double f1 = freq;
        double[] processed = new double[(int) (reverb.length * f2 / f1)];
        int startFrame = (int) Math.rint(time * WaveWriter.SAMPLE_RATE);

        for (int i = 0; i < processed.length && i < frames[0].length; i++) {
            double exInd = i * f1 / f2;
            int index = (int) exInd;
            double fract = exInd - index;
            double frame1 = reverb[index];
            double frame2 = frame1;
            if (index + 1 < reverb.length)
                frame2 = reverb[index + 1];
            double frame = frame1 * (1 - fract) + frame2 * fract;
            frame *= vol;
            for (int chan = 0; chan < pan.length; chan++)
                try {
                    processed[i] += pan[chan] * frame;
                } catch (Exception e) {
                    System.out.println(e);
                }
        }
        double dur = Math.max(0.0001,vol - 0.6);
        processed = BPF.BPF(processed, WaveWriter.SAMPLE_RATE, freq,dur * 1);
        for (int i = 0; i < processed.length; i++) {
            for (int n = 0; n < pan.length; n++) {
                double p = pan[n];
                frames[n][i + startFrame] += p * processed[i];
            }
        }
    }

    public static void test() {
        int[] notes = new int[] { 0, 5, 9, 12 };
        Gong gong = new Gong();
        WaveWriter ww = new WaveWriter("GONGTEST");
        for (int n = 0; n < 100; n++) {
            double t = Math.random();
            t = (Math.log(t) + 10)/10.0;//(Math.pow(10, t) - 1) / 9.0;
            t *= 10;
            int note = notes[(int) (Math.random() * notes.length)];
            double freq = 880 * Math.pow(2, note / 15.0);
            gong.writeNote(ww.df, t, freq, t / 10.0, new double[] { 1 });
            //gong.writeNote(ww.df, 20 - t, freq, t / 10.0, new double[] { 1 });
        }
        double max = 0;
        for (int i = 0; i < WaveWriter.SAMPLE_RATE * 20; i++) {
            max = Math.max(max, Math.abs(ww.df[0][i]));
        }
        for (int i = 0; i < WaveWriter.SAMPLE_RATE * 20; i++) {
            ww.df[0][i] /= max;
        }
        ww.render(1);

    }

    public static void main(String[] args) {
        test();
    }

}
