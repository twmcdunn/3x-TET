import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Write a description of interface Synth here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public abstract class Synth {
    public double mix;
    public double[] cathedral;
    public static ArrayList<SampleFreq> sampleFreqs,voiceSampleFreqs;
    public static double maxVol = 0, minVol = 100;
    public Spatializer spatializer;
    public static Random rand = new Random(456);
    public static boolean spatialize = true;

    /**
     * An example of a method header - replace this comment with your own
     *
     * @param y a sample parameter for a method
     * @return the result produced by sampleMethod
     */

    //general processes that apply to all synthesized sounds
    //n.b. the pan parameter is depricated. within this general method, pan
    //is determined fromm a Spatializer object, stored in an instancc variable
    public void writeNote(float[][] frames, double time, double freq, double vol, double[] pan) {
        maxVol = Math.max(maxVol,vol);
        minVol = Math.min(minVol, vol);
        
        if(time >= 10 * 60 + 45){
            freq *= Math.pow(2, 7 / 12.0) / 2.0;//trans down 12TET 4th
        }
        if (spatialize) {
            spatializer.setMagnitude(Math.max(1 - vol, 0));// loud sounds are more central
            pan = spatializer.pan(time);// over write input
        }/*
        System.out.print("PAN: ");
        for(double p: pan)
        System.out.print(p + ", ");

        System.out.println();
        */

        childWriteNote(frames, time, freq, vol, pan);
    }

    //implemented in children to produce the specific sound
    public abstract void childWriteNote(float[][] frames, double time, double freq, double vol, double[] pan);

    public void loadVoiceSampleFreqs() {
        if (voiceSampleFreqs != null)
            return;
        voiceSampleFreqs = new ArrayList<SampleFreq>();
        voiceSampleFreqs.add(new SampleFreq(ReadSound.readSoundDoubles("voice/voice1.wav"), 92));
        voiceSampleFreqs.add(new SampleFreq(ReadSound.readSoundDoubles("voice/voice2.wav"), 162));
        voiceSampleFreqs.add(new SampleFreq(ReadSound.readSoundDoubles("voice/voice3.wav"), 159));
        voiceSampleFreqs.add(new SampleFreq(ReadSound.readSoundDoubles("voice/voice4.wav"), 162));
        voiceSampleFreqs.add(new SampleFreq(ReadSound.readSoundDoubles("voice/voice5.wav"), 168));
        voiceSampleFreqs.add(new SampleFreq(ReadSound.readSoundDoubles("voice/voice6.wav"), 176));
        voiceSampleFreqs.add(new SampleFreq(ReadSound.readSoundDoubles("voice/voice7.wav"), 261));
        voiceSampleFreqs.add(new SampleFreq(ReadSound.readSoundDoubles("voice/voice8.wav"), 267));
        voiceSampleFreqs.add(new SampleFreq(ReadSound.readSoundDoubles("voice/voice9.wav"), 274));
        voiceSampleFreqs.add(new SampleFreq(ReadSound.readSoundDoubles("voice/voice10.wav"), 413));
        voiceSampleFreqs.add(new SampleFreq(ReadSound.readSoundDoubles("voice/voice11.wav"), 428));
        voiceSampleFreqs.add(new SampleFreq(ReadSound.readSoundDoubles("voice/voice12.wav"), 440));
        voiceSampleFreqs.add(new SampleFreq(ReadSound.readSoundDoubles("voice/voice13.wav"), 462));
        voiceSampleFreqs.add(new SampleFreq(ReadSound.readSoundDoubles("voice/voice14.wav"), 469));

        for (SampleFreq sf : voiceSampleFreqs) {
            sf.freq *= WaveWriter.SAMPLE_RATE / 44100.0;
        }
    }

    public void loadSampleFreqs() {
        if (sampleFreqs != null)
            return;
        sampleFreqs = new ArrayList<SampleFreq>();
        sampleFreqs.add(new SampleFreq(ReadSound.readSoundDoubles("3.wav"), 626));
        sampleFreqs.add(new SampleFreq(ReadSound.readSoundDoubles("1.wav"), 1762));
        sampleFreqs.add(new SampleFreq(ReadSound.readSoundDoubles("9.wav"), 2795));
        sampleFreqs.addAll(getVibSamples());
    }

    public ArrayList<SampleFreq> getVibSamples() {
        ArrayList<SampleFreq> sampleFreqs = new ArrayList<SampleFreq>();

        ArrayList<double[]> samples = new ArrayList<double[]>();
        for (int i = 53; i <= 89; i++) {
            samples.add(Arrays.copyOf(ReadSound.readSoundDoubles("vibs/vib" + i + ".wav"), WaveWriter.SAMPLE_RATE * 3));
        }

        for (int n = 0; n < samples.size(); n++) {
            double[] sig = samples.get(n);

            // add decay envelope
            for (int i = 0; i < sig.length; i++) {
                double env = i / (double) ((int) (sig.length));
                env = Math.pow(10, -env * 3);
                if ((int) (sig.length) - i < 100)
                    env *= ((int) (sig.length) - i) / 100.0;
                sig[i] *= env;
            }

            int midi = n + 53;
            double f = 440 * Math.pow(2, (midi - 60) / 12.0);

            sampleFreqs.add(new SampleFreq(sig, f));

        }

        return sampleFreqs;
    }

    public SampleFreq getClosestVoiceSampleFreq(double freq) {
        ArrayList<SampleFreq> sfs = sampleFreqs;
        sampleFreqs = voiceSampleFreqs;
        SampleFreq sf = getClosestSampleFreq(freq);
        sampleFreqs = sfs;
        return sf;
    }

    public SampleFreq getClosestSampleFreq(double freq) {

        double lowestHs = Double.MAX_VALUE;
        SampleFreq closetsSampleFreq = null;
        for (SampleFreq sf : sampleFreqs) {
            double hs = Math.abs(12 * Math.log(sf.freq / freq) / Math.log(2));
            if (hs < lowestHs) {
                lowestHs = hs;
                closetsSampleFreq = sf;
            }
        }
        return closetsSampleFreq;
    }

    public double[] addReverb(double[] processed) {
        if (cathedral == null) {
            cathedral = ReadSound.readSoundDoubles("cathedral.wav");
        }
        processed = Arrays.copyOf(processed, processed.length + cathedral.length);
        double[] cathedralCopy = Arrays.copyOf(cathedral, processed.length);
        double[] wetSig = FFT2.convAsImaginaryProduct(processed, cathedralCopy);
        wetSig = Arrays.copyOf(wetSig, processed.length);
        double sMax = 0;
        double wMax = 0;
        for (int i = 0; i < wetSig.length; i++) {
            sMax = Math.max(sMax, Math.abs(processed[i]));
            wMax = Math.max(wMax, Math.abs(wetSig[i]));
            if(sMax == Double.NaN){
                System.out.println("TOO LOUD: " + i);
            }
        }

        for (int i = 0; i < wetSig.length; i++) {
            processed[i] /= sMax;
            wetSig[i] /= wMax;
            processed[i] = mix * processed[i] + (1 - mix) * wetSig[i];
        }
        return processed;
    }

    public double[] getReverb(double[] processed) {
        if (cathedral == null) {
            cathedral = ReadSound.readSoundDoubles("cathedral.wav");
        }
        processed = Arrays.copyOf(processed, processed.length + cathedral.length);
        double[] cathedralCopy = Arrays.copyOf(cathedral, processed.length);
        double[] wetSig = FFT2.convAsImaginaryProduct(processed, cathedralCopy);
        // wetSig = Arrays.copyOf(wetSig, processed.length);
        double wMax = 0;
        for (int i = 0; i < wetSig.length; i++) {
            wMax = Math.max(wMax, Math.abs(wetSig[i]));
        }
        for (int i = 0; i < wetSig.length; i++) {
            wetSig[i] /= wMax;
        }
        return wetSig;
    }

    public double[] pitchShift(double[] sig, double f2, double freq) {

        double freqRatio = freq / f2;// Math.pow(2, (exactMidi - midiNum) / 12.0);

        double[] processed = new double[(int) (sig.length / freqRatio)];

        for (int i = 0; i < (int) (sig.length / freqRatio); i++) {
            double exInd = i * freqRatio;
            int index = (int) exInd;
            double fract = exInd - index;
            double frame1 = sig[index];
            double frame2 = frame1;
            if (index + 1 < sig.length)
                frame2 = sig[index + 1];
            double frame = frame1 * (1 - fract) + frame2 * fract;

            processed[i] = frame;
        }
        return processed;
    }
}
