import java.util.Arrays;

/**
 * Write a description of class SampleSynth here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class SampleSynth implements Synth {
    public double[] sig;
    public double f2;
    public int type;
    public double[] wetSig;

    /**
     * Constructor for objects of class SampleSynth
     */
    public SampleSynth(int sampleNumber) {
        type = sampleNumber;
        switch (sampleNumber) {
            case 0:
                f2 = 1762;
                sig = ReadSound.readSoundDoubles("1.wav"); // xylo oct 7 and 6
                break;
            case 1:
                f2 = 1327;
                sig = ReadSound.readSoundDoubles("2.wav"); // piano
                break;
            case 2:
                f2 = 626;
                sig = ReadSound.readSoundDoubles("3.wav");// unfiltered chime, octavve 5 and 6
                break;
            case 3:
                f2 = 626;
                sig = ReadSound.readSoundDoubles("4.wav"); // filtered chime oct 4 ok oc 5 good
                break;
            case 4:
                f2 = 626;
                sig = ReadSound.readSoundDoubles("5.wav");// nice percussive chime sound oct 5
                break;
            case 5:
                f2 = 172;
                sig = ReadSound.readSoundDoubles("6.wav");
                break;
            case 6:
                f2 = 172;
                sig = ReadSound.readSoundDoubles("7.wav");// nice version of church bell
                break;
            case 7:
                f2 = 112;
                sig = ReadSound.readSoundDoubles("8.wav");// low piano
                break;
            case 8:
                f2 = 2795;
                sig = ReadSound.readSoundDoubles("9.wav");// original chime sound oct 6 and 7
                break;
            case 9:
                f2 = 2795;
                sig = ReadSound.readSoundDoubles("10.wav");// processed chime sound oct 7 sound good
                break;
            case 10:
                f2 = 2795;
                sig = ReadSound.readSoundDoubles("11.wav");// variant w/ delay attack
                break;
            case 11:
                f2 = 1760;
                sig = ReadSound.readSoundDoubles("12.wav");// filtered xylo oct 6 ok
                break;
            case 12:
                f2 = 627;
                sig = ReadSound.readSoundDoubles("13.wav");// filtered xylo oct 6 ok
                break;
            case 13:
                f2 = 394;
                sig = ReadSound.readSoundDoubles("14.wav");// low xylo oct 4 and 3
                break;
            case 14:
                f2 = 264;
                sig = ReadSound.readSoundDoubles("16.wav");// low vibs oct 4
                break;
            case 15:
                f2 = 351;
                sig = ReadSound.readSoundDoubles("17.wav");// low vibs oct 4 or 3
                break;
            case 16:
                f2 = 176;
                sig = ReadSound.readSoundDoubles("vibs/vib53.wav");// low vibs oct 4 or 3
                break;
            case 17:
                f2 = 224;
                sig = ReadSound.readSoundDoubles("18.wav");// drone metallic swell A3
                break;
        }

        double[] cathedral = ReadSound.readSoundDoubles("cathedral.wav");
        sig = Arrays.copyOf(sig, sig.length + cathedral.length);
        cathedral = Arrays.copyOf(cathedral, sig.length);
        wetSig = FFT2.convAsImaginaryProduct(sig, cathedral);
        wetSig = Arrays.copyOf(wetSig, sig.length);
        double sMax = 0;
        double wMax = 0;
        for(int i = 0; i < wetSig.length; i++){
            sMax = Math.max(sMax, Math.abs(sig[i]));
            wMax = Math.max(wMax, Math.abs(wetSig[i]));
        }
        for(int i = 0; i < wetSig.length; i++){
            sig[i] /= sMax;
            wetSig[i] /= wMax;
        }
    }

    public void writeNote(float[][] frames, double time, double freq, double vol, double[] pan) {
        // if(type != 17)
        // return;
double globalReverb = Piece.reverbEnv.getValue(time);
        double mix = (1-globalReverb) + globalReverb * vol;//max reverb is 50% mix
        double[] reverb = new double[sig.length];
        for(int i = 0; i < reverb.length; i++){
            reverb[i] = wetSig[i] * (1-mix) + sig[i] * mix;
        }

        for(int i = 0; i < 100; i++)
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
                    frames[chan][i + startFrame] += pan[chan] * frame;
                } catch (Exception e) {
                    System.out.println(e);
                }
        }
    }

    public static void testSample() {
        Synth synth = new SampleSynth(16);
        WaveWriter ww = new WaveWriter("test");

        float[][] sound = new float[1][WaveWriter.SAMPLE_RATE * 60];
        synth.writeNote(sound, 0, 176, 0.1, new double[] { 1 });
        double max = Double.MIN_VALUE;

        for (int i = 0; i < sound[0].length; i++) {
            max = Math.max(max, Math.abs(sound[0][i]));

        }

        for (int i = 0; i < sound[0].length; i++) {
            ww.df[0][i] += sound[0][i] / max;
        }
        ww.render(1);
    }

    public static void testFilteredChime() {
        Synth synth = new SampleSynth(0);
        WaveWriter ww = new WaveWriter("filteredChime");

        float[][] sound = new float[1][WaveWriter.SAMPLE_RATE * 60];
        synth.writeNote(sound, 0, 1762, 0.1, new double[] { 1 });
        double[] unfiltered = new double[sound[0].length];
        for (int i = 0; i < sound[0].length; i++) {
            unfiltered[i] += sound[0][i];
        }

        double[] filtered = new double[sound[0].length];
        double[] freqs = new double[] { 1762, 4829, 6231 };
        double[] amps = new double[] { 1, 0.5, 0.25, .125, 0.0625, 0.03125 };
        for (int n = 0; n < 5; n++) {
            for (int j = 0; j < freqs.length; j++) {
                double f = freqs[j];
                double[] partial = BPF.BPF(unfiltered, WaveWriter.SAMPLE_RATE, f, 10);
                for (int i = 0; i < partial.length; i++) {
                    filtered[i] += amps[j] * 0.02 * partial[i] / (double) freqs.length;
                }
            }
            unfiltered = filtered;
        }

        // filtered = unfiltered;

        double max = Double.MIN_VALUE;

        for (int i = 0; i < filtered.length; i++) {
            max = Math.max(max, Math.abs(filtered[i]));

        }

        for (int i = 0; i < filtered.length; i++) {
            ww.df[0][i] += filtered[i] / max;
        }
        ww.render(1);
    }

    public static void main(String[] args) {
        // testFilteredChime();
        testSample();
    }
}
