import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class LoopDecaySynth extends LoopSynth {
    public double vol;

    public LoopDecaySynth(double volume) {
        sig = ReadSound.readSoundDoubles("25.wav");
        f2 = 440;
        vol = volume;
    }

    public void addPitch(double freq, double time) {
        pitches.add(new double[] { freq, time });
    }

    public void writeNote(float[][] frames, double time, double freq, double startVol, double[] pan) {
        time += Math.random() * 0.05;

        double freqRatio = freq / f2;// Math.pow(2, (exactMidi - midiNum) / 12.0);

        double[] processed = new double[(int) (sig.length / freqRatio)];

        for (int i = 0; i < (int) (sig.length / freqRatio) && i < frames[0].length; i++) {
            double exInd = i * freqRatio;
            int index = (int) exInd;
            double fract = exInd - index;
            double frame1 = sig[index];
            double frame2 = frame1;
            if (index + 1 < sig.length)
                frame2 = sig[index + 1];
            double frame = frame1 * (1 - fract) + frame2 * fract;
            // frame *= 0.01;

            processed[i] = frame;
        }
        double max = 0;
        for(int i = 0; i < processed.length; i++){
            max = Math.max(Math.abs(processed[i]), max);
        }
        for(int i = 0; i < processed.length; i++){
            processed[i] /= max;
        }

        // int endFrame = (int)((time + 6) * WaveWriter.SAMPLE_RATE)+
        // WaveWriter.SAMPLE_RATE;
        int seconds = 6;
        if(startVol == 1)
            seconds *= 2;
        double[] noteSig = new double[WaveWriter.SAMPLE_RATE * seconds + 5000];
        
        

        for (int i = 0; i < noteSig.length; i++) {
            double t = i / (double) WaveWriter.SAMPLE_RATE;

            if(startVol == 1)
                t /= 2.0;
                        
            if (startVol == 0)
                t = 6 - t;

            double env = Math.pow(10, -t / 2.0);

            noteSig[i] += env * processed[i % processed.length];

        }

        for (int i = 0; i < 5000; i++) {
            if(startVol == 0)
                noteSig[i] *= i / 5000.0;
            noteSig[noteSig.length - 1 - i] *= i / 5000.0;
        }
        if(startVol == 1){
            //f2 = 265;
            double[] attack = pitchShift(ReadSound.readSoundDoubles("26.wav"), 265, freq);
            attack = Arrays.copyOf(attack, WaveWriter.SAMPLE_RATE / 20);
            attack = BPF.BPF(attack, WaveWriter.SAMPLE_RATE, freq, 0.01);//001
        max = 0;
            for(int i = 0; i < attack.length; i++){
                max = Math.max(Math.abs(attack[i]), max);
            }
            for(int i = 0; i < attack.length; i++){
                attack[i] /= max;
            }
            for(int i = 0; i < attack.length; i++){
                double env = i / (double) attack.length;
                noteSig[i] = env * noteSig[i] + (1-env) * attack[i];
            }
        }
        //noteSig = Arrays.copyOf(noteSig, noteSig.length + WaveWriter.SAMPLE_RATE * 3);//3 sec for reverb tail

        // int nLen = endFrame - startFrame;
        noteSig = addReverb(noteSig);
        // noteSig = Arrays.copyOf(noteSig, nLen);

        double nMax = 0;
        for (int i = 0; i < noteSig.length; i++) {
            nMax = Math.max(nMax, Math.abs(noteSig[i]));
        }
        for (int i = 0; i < noteSig.length; i++) {
            noteSig[i] /= nMax;
        }

        int startFrame = (int) (time * WaveWriter.SAMPLE_RATE);
        for (int i = 0; i < noteSig.length; i++) {

            for (int n = 0; n < pan.length; n++) {
                frames[n][i + startFrame] += vol * noteSig[i] * pan[n];
            }
        }
    }

    public double[] addReverb(double[] processed) {
        double[] cathedral = ReadSound.readSoundDoubles("cathedral.wav");
        processed = Arrays.copyOf(processed, processed.length + cathedral.length);
        cathedral = Arrays.copyOf(cathedral, processed.length);
        double[] wetSig = FFT2.convAsImaginaryProduct(processed, cathedral);
        wetSig = Arrays.copyOf(wetSig, processed.length);
        double sMax = 0;
        double wMax = 0;
        for (int i = 0; i < wetSig.length; i++) {
            sMax = Math.max(sMax, Math.abs(processed[i]));
            wMax = Math.max(wMax, Math.abs(wetSig[i]));
        }
        double mix = 0.15;
        for (int i = 0; i < wetSig.length; i++) {
            processed[i] /= sMax;
            wetSig[i] /= wMax;
            processed[i] = mix * processed[i] + (1 - mix) * wetSig[i];
        }
        return processed;
    }
    public double[] pitchShift(double[] sig, double f2, double freq){
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

    public static void test() {
        LoopSynth synth = new LoopSynth();
        WaveWriter ww = new WaveWriter("test");
        synth.addPitch(440, 0);
        synth.addPitch(330, 10);
        synth.addPitch(-1, 50);

        float[][] sound = new float[1][WaveWriter.SAMPLE_RATE * 60];
        synth.writeNote(ww.df, 0, 176, 0.1, new double[] { 1 });
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
