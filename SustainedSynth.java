import java.io.File;
import java.util.Arrays;
public class SustainedSynth extends SampleSynth {//SampleSynth
    Envelope env;
    double vol;
    public SustainedSynth(int sampleType, double volume){
        super(sampleType);
        if(sampleType == -1){
            f2 = 1576;
            sig = ReadSound.readSoundDoubles("20.wav"); 
            sig = Arrays.copyOf(sig, (int)(WaveWriter.SAMPLE_RATE * 0.05));
            for(int i = 0; i < 10; i++){
                sig[i] *= i /10.0;
                sig[sig.length - 1 - i] *= i /10.0;
            }
        }
        env = GUI.open(new File("envs1.txt")).get(3);
        vol = volume;
        double max = 0;
        for(int i = 0; i < sig.length; i++){
            max = Math.max(Math.abs(sig[i]), max);
        }
        for(int i = 0; i < sig.length; i++)
            sig[i] /= max;
    }

    //volume is used to indicate whether it grows or decays
    public void writeNote(float[][] frames, double time, double freq, double startVol, double[] pan) {


       double[] processed = susSound(sig, f2, freq, startVol);
       processed = BPF.BPF(processed, WaveWriter.SAMPLE_RATE, freq, 0.0005);

        processed = addReverb(processed);

        //processed = BPF.BPF(processed, WaveWriter.SAMPLE_RATE, freq, 0.1);

        
        int startFrame = (int) Math.rint(time * WaveWriter.SAMPLE_RATE);

        for(int i = 0; i < processed.length; i++){
            for(int n = 0; n < pan.length; n++){
                frames[n][i + startFrame] += pan[n] * vol * processed[i];
            }
        }
    }

    public double[] addReverb(double[] processed){
        double[] cathedral = ReadSound.readSoundDoubles("cathedral.wav");
        processed = Arrays.copyOf(processed, processed.length + cathedral.length);
        cathedral = Arrays.copyOf(cathedral, processed.length);
        double[] wetSig = FFT2.convAsImaginaryProduct(processed, cathedral);
        wetSig = Arrays.copyOf(wetSig, processed.length);
        double sMax = 0;
        double wMax = 0;
        for(int i = 0; i < wetSig.length; i++){
            sMax = Math.max(sMax, Math.abs(processed[i]));
            wMax = Math.max(wMax, Math.abs(wetSig[i]));
        }
        double mix = 0.7;
        for(int i = 0; i < wetSig.length; i++){
            processed[i] /= sMax;
            wetSig[i] /= wMax;
            processed[i] = mix * processed[i] + (1-mix) * wetSig[i];
        }
        return processed;
    }

    public double[] susSound(double[] sig, double f2, double freq, double startVol) {
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
            // frame *= vol;
            /*
             * double env = i / (double) ((int) (sig.length / freqRatio));
             * env = Math.pow(10, -env * 3);
             * if ((int) (sig.length / freqRatio) - i < 100)
             * env *= ((int) (sig.length / freqRatio) - i) / 100.0;
             * 
             * frame *= env;
             */

            processed[i] = frame;
        }

        // System.out.println(env.getValue(1));
        // env.display();

        double[] noise = new double[WaveWriter.SAMPLE_RATE * 6];
        for (int i = 0; i < noise.length; i++) {
            noise[i] = Math.random() * 2 - 1;

            double envVal = 0;
            if (startVol == 1)
                envVal = env.getValue(i / (double) noise.length);
            else if (startVol == 0)
                envVal = env.getValue(1 - i / (double) noise.length);
            noise[i] *= envVal;
        }

        int actualDur = noise.length;
        noise = Arrays.copyOf(noise, noise.length + WaveWriter.SAMPLE_RATE);

        processed = FFT2.convAsImaginaryProduct(Arrays.copyOf(processed, noise.length), noise);

        processed = Arrays.copyOf(processed, actualDur + 5000);

        for (int i = 0; i < 5000; i++) {

            processed[processed.length - 1 - i] *= i / 5000.0;
            // processed[(processed.length - 1) - i] *= (i / 1000.0);
            // System.out.println(processed[i]);

            if (startVol == 0)
                processed[i] *= i / 5000.0;

        }

        double max = 0;
        for (int i = 0; i < processed.length; i++) {
            max = Math.max(Math.abs(processed[i]), max);
        }
        for (int i = 0; i < processed.length; i++)
            processed[i] /= max;

        return processed;
    }

    public static void test() {
        Synth synth = new SustainedSynth(2,0.01);
        WaveWriter ww = new WaveWriter("test");

        //float[][] sound = new float[1][WaveWriter.SAMPLE_RATE * 60];
        synth.writeNote(ww.df, 0, 440, 1, new double[] { 1 });
        double max = Double.MIN_VALUE;

        for (int i = 0; i < ww.df[0].length; i++) {
            max = Math.max(max, Math.abs(ww.df[0][i]));

        }

        for (int i = 0; i < ww.df[0].length; i++) {
            ww.df[0][i] /= max;
        }
        ww.render(1);
    }

    public static void main(String[] args) {
        test();
    }
}
