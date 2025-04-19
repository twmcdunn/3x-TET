import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class LoopSynth extends Synth{
    double f2;
    double[] sig;
    ArrayList<double[]> pitches;
    double amFreq, amPhase;
    int chordNum;
    public static double globalAmFreq = 1 / 30.0;
    public LoopSynth(int chordNum){
        spatializer = new Spatializer(Math.PI * 2 / (20 + 20 * rand.nextDouble()), Math.PI * 2 * rand.nextDouble());
        sig = ReadSound.readSoundDoubles("23.wav");
        f2 = 173;
        pitches = new ArrayList<double[]>();   
        amFreq = 1 / (Math.random() * 40 + 20);   
        amPhase = Math.random() * Math.PI * 2;
        this.chordNum = chordNum;
        loadSampleFreqs();
    }

    public void addPitch(double freq, double time){
        pitches.add(new double[]{freq,time});
    }

    public double[] generateLoop(double freq){
        SampleFreq sf = getClosestSampleFreq(freq);
        double[] sample = Arrays.copyOf(pitchShift(sf.dry, sf.freq, freq), WaveWriter.SAMPLE_RATE/10);
        for(int i = 0; i < sample.length; i++){
            sample[i] *= 1 - i/(double)sample.length;
        }
        double[] noise = new double[WaveWriter.SAMPLE_RATE * 10];
        for(int i = 0; i < noise.length; i++){
            noise[i] = Math.random() * 2 - 1;
        }
        sample = Arrays.copyOf(sample, noise.length);
        sample = FFT2.convAsImaginaryProduct(sample,noise);
        sample = Arrays.copyOf(sample,WaveWriter.SAMPLE_RATE * 10);
        for(int i = 0; i < WaveWriter.SAMPLE_RATE; i++){
            double mix = i / (double)WaveWriter.SAMPLE_RATE;
            sample[i] = mix * sample[i] + (1-mix) * sample[i+WaveWriter.SAMPLE_RATE*9];
        }
        return sample;
    }
    
    public void childWriteNote(float[][] frames, double time, double freq, double vol, double[] pan) {
        ArrayList<double[]> samples = new  ArrayList<double[]>();
        Collections.sort(pitches, new Comparator<double[]>(){
            public int compare(double[] p1,double[] p2){
                return (int)(100 * (p1[1] - p2[1]));
            }
        });
        ArrayList<double[]> pitchesCopy = new ArrayList<double[]>();
        
        for(int i = 0; i < pitches.size(); i++){
            pitchesCopy.add(pitches.get(i));
            if(pitches.get(i)[0] == -1){
                break;
            }
        }

        ArrayList<double[]> pitchesToAdd= new ArrayList<double[]>();//split long notes in half
        for(int i = 0; i < pitches.size() - 1; i++){
            double dur = pitches.get(i+1)[1] - pitches.get(i)[1];
            if(dur > 3){
                pitchesToAdd.add(new double[] {pitches.get(i)[0], pitches.get(i)[1] + dur / 2.0});
            }
        }

        pitches.addAll(pitchesToAdd);

        Collections.sort(pitches, new Comparator<double[]>(){
            public int compare(double[] p1,double[] p2){
                return (int)(100 * (p1[1] - p2[1]));
            }
        });


        for (double[] pitch : pitches) {
            freq = pitch[0];
            if(freq == -1)
                break;
                /* 
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
            */
            //samples.add(processed);
            samples.add(generateLoop(freq));
        }
        for (int p = 0; p < pitches.size() - 1; p++) {
            double[] processed = samples.get(p);
            int startFrame = (int)(pitches.get(p)[1] * WaveWriter.SAMPLE_RATE);
            int endFrame= (int)(pitches.get(p+1)[1] * WaveWriter.SAMPLE_RATE)+ WaveWriter.SAMPLE_RATE;
            
            double[] noteSig = new double[endFrame - startFrame];

            for (int i = 0; i < endFrame - startFrame; i++) {
            
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
                noteSig[i] += processed[i % processed.length];//env

              

            }
            int nLen = endFrame - startFrame;
            noteSig = FFT2.convAsImaginaryProduct(noteSig, noteSig);
            noteSig = BPF.BPF(noteSig, WaveWriter.SAMPLE_RATE, pitches.get(p)[0], 0.001);
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

                double t = (i + startFrame) / (double)WaveWriter.SAMPLE_RATE;
                double env = 1;
                if (t > 10 * 60 + 15)
                    env = 1 - (t - 10 * 60 + 15) / 135.0;
                    if(t > 10 * 60 + 7.5 && t < 10 * 60 + 22.5){
                        double e1 = 0;
                        double t1 = t - (10 * 60 + 7.5);
                        if(t1 < 1)
                            e1 = 1 - t1;
                        if(t1 > 14)
                            e1 = (15 - t1);
                        env *= e1;
                    }
                double am = (Math.sin(Math.PI * 2 * t * amFreq + amPhase) + 1) / 2.0;
                am *= Math.pow(Math.sin((Math.PI * 2 * t / 20.0) + (Math.PI * 3 / 2.0) * chordNum),2);
                am *= 0.5 + 0.5 * (Math.cos(Math.PI * 2 * t * globalAmFreq) + 1)/2.0;
                spatializer.setMagnitude(1-am*env);
                pan = spatializer.pan((i+startFrame)/(double)WaveWriter.SAMPLE_RATE);
                for(int n = 0; n < pan.length; n++){
                    frames[n][i+startFrame] += am * 0.2 * noteSig[i] * pan[n] * env;
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
        LoopSynth synth = new LoopSynth(0);
        WaveWriter ww = new WaveWriter("test");
        synth.addPitch(440,0);
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
    public static void main(String[] args){
        test();
    }
}
