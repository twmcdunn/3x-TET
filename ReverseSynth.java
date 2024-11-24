import java.util.Arrays;

public class ReverseSynth implements Synth{
       double f2;
    double[] sig;
Envelope env;
double vol;
    public ReverseSynth(double volume){
        sig = ReadSound.readSoundDoubles("3.wav");
        f2 = 626;
                vol = volume;
    }
    
    public void writeNote(float[][] frames, double time, double freq, double startVol, double[] pan) {


        double freqRatio = freq / f2;//Math.pow(2, (exactMidi - midiNum) / 12.0);
        
        
        double[] processed = new double[(int)(sig.length / freqRatio)];
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

            processed[i] = frame;
        }
        processed = Arrays.copyOf(processed, WaveWriter.SAMPLE_RATE * 6);

        for(int i = 0; i < 100; i++)
        processed[processed.length - 1 - i] *= i / 100.0;
        

        for(int i = 0; i < processed.length; i++){

            for(int n = 0; n < pan.length; n++){
                if(startVol == 1)
                frames[n][i + startFrame] +=  pan[n] * vol * processed[i];
                if(startVol == 0)
                frames[n][i + startFrame] += pan[n] * vol * processed[processed.length - 1 - i];
            }
        }
    }
}
