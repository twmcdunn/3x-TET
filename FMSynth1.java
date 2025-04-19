import java.util.Arrays;

/**
 * Write a description of class FMSynth here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class FMSynth1 extends LoopDecaySynth
{
    private double[] waveTable, loop;
    public double vol;
    public FMSynth1(){
        super(0.35,0);
        
        waveTable = new double[WaveWriter.SAMPLE_RATE];
        for(int i = 0; i < WaveWriter.SAMPLE_RATE; i++){
            waveTable[i] = Math.sin(2 * Math.PI * i / (double) WaveWriter.SAMPLE_RATE);
        }
        vol = 0.35;
    }

    public double sin(double x){
        
        double scaledX = WaveWriter.SAMPLE_RATE * x;
        while(scaledX < 0)
            scaledX += loop.length;
        int flooredX = (int)scaledX;
        double fract = scaledX - flooredX;
        int modularX = flooredX % loop.length;
        double y1 = loop[modularX];
        double y2 = loop[(modularX + 1) % loop.length];
        return y1 * (1-fract) + y2 * (fract);
         


    }
    
    public double cos(double x){
        return sin(x + Math.PI * 3 / 2.0);
    }

    public void childWriteNote(float[][] frames, double time, double freq, double startVol, double[] pan){
        double carrier = freq;
        loop = generateLoop(carrier);
        double modulator = freq;
        double dur = 6;
        if(startVol == 1)
            dur = 12;
        int durFrames = (int)Math.rint(dur * WaveWriter.SAMPLE_RATE);
        int startFrame = (int)Math.rint(time * WaveWriter.SAMPLE_RATE);
        double[] noteSig = new double[durFrames];
        double amFreq = 1/(Math.random() * 2 + 1);
        double amPh = Math.random() * Math.PI * 2;
        for(int i = 0; i < durFrames; i++){
            double t = i /(double)WaveWriter.SAMPLE_RATE; 
            double env = (durFrames - i) / (double)durFrames;
            double db = env;
            double amp = Math.pow(10, (60 * db - 60) / 20.0);



            if (startVol == 1)
                t /= 2.0;

            if (startVol == 0)
                t = 6 - t;

             env = Math.pow(10, -t / 2.0);
            
            amp = vol * env;
            if(durFrames - i < 100){
                amp *= (durFrames - i) / 100.0;
            }
            if(i < 100)
                amp *= i / 100.0;
            if(i > durFrames - 100)
                amp *= (durFrames-i) / 100.0;
            t = i /(double)WaveWriter.SAMPLE_RATE; 
            double am = 0.5 + 0.5 * (sin(Math.PI * 2 * amFreq * t + amPh) + 1) / 2.0;
            am = 1;
            double frame = amp * sin(t - 0.004 * amp * Math.cos(2 * Math.PI * modulator * t));
            noteSig[i] += frame;
        }
        mix = 0.5;
       noteSig = addReverb(noteSig);
        for(int i = 0; i < noteSig.length; i++)
            for(int chan = 0; chan < pan.length; chan++)
                    frames[chan][i + startFrame] += pan[chan] * noteSig[i];
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
        sample = FFT2.convAsImaginaryProduct(sample, sample);
        for(int i = 0; i < 3; i++)
            sample = BPF.BPF(sample, WaveWriter.SAMPLE_RATE, freq, 0.01);
        sample = Arrays.copyOf(sample,WaveWriter.SAMPLE_RATE * 10);
        for(int i = 0; i < WaveWriter.SAMPLE_RATE; i++){
            double mix = i / (double)WaveWriter.SAMPLE_RATE;
            sample[i] = mix * sample[i] + (1-mix) * sample[i+WaveWriter.SAMPLE_RATE*9];
        }
        return sample;
    }

    public static void test() {
        Synth synth = new FMSynth1();
        WaveWriter ww = new WaveWriter("testFM1");
        int[] chord = new int[]{6 + 15 * 4,0 + 15 * 5,3 + 15 * 5,11+ 15 * 5};
        for(int n = 0; n < 4; n++){
            int note = chord[n];
            synth.writeNote(ww.df, 0, Piece.c0Freq * Math.pow(2,note / 15.0), 0, new double[]{1});
        }
        
      
        chord = new int[]{10 + 15 * 4,1 + 15 * 5,5 + 15 * 5,13+ 15 * 5};
        for(int n = 0; n < 4; n++){
            int note = chord[n];
            
            synth.writeNote(ww.df, 6, Piece.c0Freq * Math.pow(2,note / 15.0), 1, new double[]{1});
        }

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
