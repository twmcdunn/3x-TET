import java.util.ArrayList;
import java.util.Arrays;

public class Vibs implements Synth {
    ArrayList<double[]> samples;

    public Vibs() { // oct 4 and 5
        samples = new ArrayList<double[]>();
        for (int i = 53; i <= 89; i++) {
            samples.add(Arrays.copyOf(ReadSound.readSoundDoubles("vibs/vib" + i + ".wav"), WaveWriter.SAMPLE_RATE * 3));
        }
    }

    public void writeNote(float[][] frames, double time, double freq, double vol, double[] pan) {
        freq = freq * 44100 / (double) WaveWriter.SAMPLE_RATE;
        double exactMidi = 69 + 12 * Math.log(freq / 440) / Math.log(2);
        int midiNum = (int) Math.rint(exactMidi);

        double freqRatio = Math.pow(2, (exactMidi - midiNum) / 12.0);

        midiNum -= 53;
        midiNum = Math.max(0, midiNum);
        midiNum = Math.min(midiNum, samples.size() - 1);

        double[] sig = samples.get(midiNum);

        // double[] processed = new double[(int)(sig.length / freqRatio)];
        int startFrame = (int) Math.rint(time * WaveWriter.SAMPLE_RATE);
        for (int i = 0; i < (int) (sig.length / freqRatio) && i < frames[0].length; i++) {
            double exInd = i * freqRatio;
            int index = (int) exInd;
            double fract = exInd - index;
            double frame1 = sig[index];
            double frame2 = frame1;
            if (index + 1 < sig.length)
                frame2 = sig[index + 1];
            double frame = frame1 * (1 - fract) + frame2 * fract;
            frame *= vol;

            double env = i / (double) ((int) (sig.length / freqRatio));
            env = Math.pow(10, -env * 3);
            if ((int) (sig.length / freqRatio) - i < 100)
                env *= ((int) (sig.length / freqRatio) - i) / 100.0;

            frame *= env;
            for (int chan = 0; chan < pan.length; chan++)
                try {
                    frames[chan][i + startFrame] += pan[chan] * frame;
                } catch (Exception e) {
                    System.out.println(e);
                }
        }

    }

    public static void testSample(){
        Synth synth = new Vibs();
        WaveWriter ww = new WaveWriter("test");

        float[][] sound = new float[1][WaveWriter.SAMPLE_RATE * 60];
        synth.writeNote(sound, 0, 440, 0.1, new double[] { 1 });
        double max = Double.MIN_VALUE;

        for (int i = 0; i < sound[0].length; i++) {
            max = Math.max(max, Math.abs(sound[0][i]));
            
        }

        for (int i = 0; i < sound[0].length; i++) {
            ww.df[0][i] += sound[0][i] / max;
        }
        ww.render(1);
    }

    public static void main(String[] args){
        testSample();
    }
}
