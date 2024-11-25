import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class LoopSynth implements Synth{
    double f2;
    double[] sig;
    ArrayList<double[]> pitches;
    double amFreq, amPhase;
    public LoopSynth(){
        sig = ReadSound.readSoundDoubles("23.wav");
        f2 = 173;
        pitches = new ArrayList<double[]>();   
        amFreq = 1 / (Math.random() * 20 + 20);   
        amPhase = Math.random() * Math.PI * 2;
    }

    public void addPitch(double freq, double time){
        pitches.add(new double[]{freq,time});
    }
    
    public void writeNote(float[][] frames, double time, double freq, double vol, double[] pan) {
        ArrayList<double[]> samples = new  ArrayList<double[]>();
        Collections.sort(pitches, new Comparator<double[]>(){
            public int compare(double[] p1,double[] p2){
                return (int)(100 * (p1[1] - p2[1]));
            }
        });


        for (double[] pitch : pitches) {
            freq = pitch[0];
            if(freq == -1)
                break;
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
                //frame *= 0.01;

                processed[i] = frame;
            }
            samples.add(processed);
        }
        for (int p = 0; p < pitches.size() - 1; p++) {
            double[] processed = samples.get(p);
            int startFrame = (int)(pitches.get(p)[1] * WaveWriter.SAMPLE_RATE);
            int endFrame= (int)(pitches.get(p+1)[1] * WaveWriter.SAMPLE_RATE)+ WaveWriter.SAMPLE_RATE;
            
            double[] noteSig = new double[endFrame - startFrame];

            for (int i = 0; i < endFrame - startFrame; i++) {
                double t = (i + startFrame) / (double)WaveWriter.SAMPLE_RATE;
                double am = (Math.sin(Math.PI * 2 * t * amFreq + amPhase) + 1) / 2.0;
            
                /* double env = 1;
                if(i <  WaveWriter.SAMPLE_RATE)
                    env *= i /(double) WaveWriter.SAMPLE_RATE;
                if(endFrame - startFrame - i < WaveWriter.SAMPLE_RATE)
                    env *= (endFrame - startFrame - i) / (double) WaveWriter.SAMPLE_RATE;
                */
                /*  for (int n = 0; n < pan.length; n++) {
                    //frames[n][i + startFrame] += am * env * pan[n] * processed[i % processed.length];
                }
                 */  
                //drySig[i + startFrame - firstStartFrame] += am * env * processed[i % processed.length];
                noteSig[i] += am * processed[i % processed.length];//env

              

            }
            int nLen = endFrame - startFrame;
            noteSig = FFT2.convAsImaginaryProduct(noteSig, noteSig);
            noteSig = Arrays.copyOf(noteSig, nLen);

            double nMax = 0;
        for(int i = 0; i < noteSig.length; i++){
            nMax = Math.max(nMax, Math.abs(noteSig[i]));
        }
        for(int i = 0; i < noteSig.length; i++){
            noteSig[i] /= nMax;
        }
        for(int i = 0; i < WaveWriter.SAMPLE_RATE; i++){
            noteSig[i] *= i / (double)WaveWriter.SAMPLE_RATE;
            noteSig[noteSig.length - 1 - i] *= i / (double)WaveWriter.SAMPLE_RATE;
        }
            for(int i = 0; i < noteSig.length; i++){
                for(int n = 0; n < pan.length; n++){
                    frames[n][i+startFrame] += 0.3 * noteSig[i] * pan[n];
                }
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
        double mix = 0.1;
        for(int i = 0; i < wetSig.length; i++){
            processed[i] /= sMax;
            wetSig[i] /= wMax;
            processed[i] = mix * processed[i] + (1-mix) * wetSig[i];
        }
        return processed;
    }

    public static void test() {
        LoopSynth synth = new LoopSynth();
        WaveWriter ww = new WaveWriter("test");
        synth.addPitch(440,0);
        synth.addPitch(330, 10);
        synth.addPitch(-1, 20);

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
    public static void main(String[] args){
        test();
    }
}
