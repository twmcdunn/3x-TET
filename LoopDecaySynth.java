import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class LoopDecaySynth extends LoopSynth {
    public double vol;
    public static double[] attackOrig;

    public LoopDecaySynth(double volume, int type) {
        super(0);     
        if(attackOrig == null){
            attackOrig = ReadSound.readSoundDoubles("26.wav");
        }       
        switch (type) {
            case 0:
                sig = ReadSound.readSoundDoubles("25.wav");
                f2 = 440;
                break;
            case 1:
                sig = ReadSound.readSoundDoubles("23.wav");
                f2 = 173;
                break;
        }
        vol = volume;
        loadSampleFreqs();
    }

    public void addPitch(double freq, double time) {
        pitches.add(new double[] { freq, time });
    }

    public void childWriteNote(float[][] frames, double time, double freq, double startVol, double[] pan) {
        double[] spectMultiples = new double[]{1,2,3,4,5};
        double[] spectVols = new double[]{1,0.5,0.25,0.125,0.0625};
        spectMultiples = new double[10];
        spectVols = new double[10];
        for(int i =0; i < spectMultiples.length; i++){
            spectMultiples[i] = i + 1;
            spectVols[i] = Math.pow(2,-i);
        }
        time += Math.random() * 0.1;
        //spectMultiples = new double[]{1};
        double instanceVol = vol;
        for(int i = 0; i < spectMultiples.length; i++){
            vol = instanceVol * spectVols[i];
            double amFreq = 0;
            if(i!= 0 && false)
                amFreq = 1 / (1 + 2 * Math.random());
            writePartial(frames,time,freq * spectMultiples[i], startVol, pan, amFreq);
        }
        vol = instanceVol;
    }

    public void writePartial(float[][] frames, double time, double freq, double startVol, double[] pan, double amFreq){
        

       // double freqRatio = freq / f2;// Math.pow(2, (exactMidi - midiNum) / 12.0);

        double[] processed = generateLoop(freq);


        
        /* double[] processed = new double[(int) (sig.length / freqRatio)];

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
             */
          



        double max = 0;
        for (int i = 0; i < processed.length; i++) {
            max = Math.max(Math.abs(processed[i]), max);
        }
        for (int i = 0; i < processed.length; i++) {
            processed[i] /= max;
        }

        // int endFrame = (int)((time + 6) * WaveWriter.SAMPLE_RATE)+
        // WaveWriter.SAMPLE_RATE;
        int seconds = 6;
        if (startVol == 1)
            seconds *= 2;
        double[] noteSig = new double[WaveWriter.SAMPLE_RATE * seconds + 5000];

        for (int i = 0; i < noteSig.length; i++) {

            noteSig[i] += processed[i % processed.length];

        }

        int nLen = noteSig.length;

        noteSig = FFT2.convAsImaginaryProduct(noteSig, noteSig);
        noteSig = BPF.BPF(noteSig, WaveWriter.SAMPLE_RATE, freq, 0.001);
        noteSig = Arrays.copyOf(noteSig, nLen);
        
        double amPh = Math.PI * 2 * Math.random();
        if(amFreq == 0)
            amPh = Math.PI / 2.0;
        for (int i = 0; i < noteSig.length; i++) {
            double t = i / (double) WaveWriter.SAMPLE_RATE;
            double am = Math.sin(t * amFreq * Math.PI * 2 + amPh);
            am = (am + 1) / 2.0;

            if (startVol == 1)
                t /= 2.0;

            if (startVol == 0)
                t = 6 - t;

            double env = Math.pow(10, -t / 2.0);
            
            noteSig[i] *= env * am;

        }

        for (int i = 0; i < 5000; i++) {
            if (startVol == 0)
                noteSig[i] *= i / 5000.0;
            noteSig[noteSig.length - 1 - i] *= i / 5000.0;
        }
        max = 0;
        for (int i = 0; i < noteSig.length; i++) {
            max = Math.max(Math.abs(noteSig[i]), max);
        }
        for (int i = 0; i < noteSig.length; i++) {
            noteSig[i] /= max;
        }

        if (startVol == 1) {
            // f2 = 265;
            double[] attack = pitchShift(attackOrig, 265, freq);
            attack = Arrays.copyOf(attack, WaveWriter.SAMPLE_RATE / 20);
            attack = BPF.BPF(attack, WaveWriter.SAMPLE_RATE, freq, 0.1);//01 001
            max = 0;
            for (int i = 0; i < attack.length; i++) {
                max = Math.max(Math.abs(attack[i]), max);
            }
            for (int i = 0; i < attack.length; i++) {
                attack[i] /= max;
            }
            for (int i = 0; i < attack.length; i++) {
                double env = i / (double) attack.length;
                noteSig[i] = env * noteSig[i] + (1 - env) * attack[i];
            }
        }
        // noteSig = Arrays.copyOf(noteSig, noteSig.length + WaveWriter.SAMPLE_RATE *
        // 3);//3 sec for reverb tail

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
        spatializer.setMagnitude(1-vol);
        for (int i = 0; i < noteSig.length; i++) {
            pan = spatializer.pan((i+startFrame)/(double)WaveWriter.SAMPLE_RATE);
            for (int n = 0; n < pan.length; n++) {
                frames[n][i + startFrame] +=  vol * noteSig[i] * pan[n];
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

    public static void test() {
        LoopDecaySynth synth = new LoopDecaySynth(0.35,0);
        WaveWriter ww = new WaveWriter("test");
        int chord[] = new int[]{6 + 15 * 4,11+ 15 * 4,0+ 15 * 5,3+ 15 * 5};
        chord = new int[]{6 + 15 * 4,0 + 15 * 5,3 + 15 * 5,11+ 15 * 5};
        for(int n = 0; n < 4; n++){
            int note = chord[n];
            synth.writeNote(ww.df, 0, Piece.c0Freq * Math.pow(2,note / 15.0), 0, new double[]{1});
        }
        
      
        chord = new int[]{5 + 15 * 4,10 + 15 * 4,1 + 15 * 5,13+ 15 * 5};
        chord = new int[]{10 + 15 * 4,1 + 15 * 5,5 + 15 * 5,13+ 15 * 5};
        for(int n = 0; n < 4; n++){
            int note = chord[n];
            
            synth.writeNote(ww.df, 6, Piece.c0Freq * Math.pow(2,note / 15.0), 1, new double[]{1});
        }
        chord = new int[]{6 + 15 * 4,10 + 15 * 4,3 + 15 * 5,0+ 15 * 6};
        chord = new int[]{13 + 15 * 4,1 + 15 * 5,5 + 15 * 5,11+ 15 * 5};
        for(int n = 0; n < 4; n++){
            int note = chord[n];
            synth.writeNote(ww.df, 12, Piece.c0Freq * Math.pow(2,note / 15.0), 0, new double[]{1});
        }
        chord = new int[]{6 + 15 * 4,0 + 15 * 5,3+ 15 * 5,10 + 15 * 5};
        for(int n = 0; n < 4; n++){
            int note = chord[n];
            synth.writeNote(ww.df,  18, Piece.c0Freq * Math.pow(2,note / 15.0), 1, new double[]{1});
        }
        ww.render(1);
    }

    public static void main(String[] args) {
        test();
    }
}
