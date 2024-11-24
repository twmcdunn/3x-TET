import java.io.File;
import java.util.Arrays;

public class LoopSynth implements Synth{
    double f2;
    double[] sig;
Envelope env;
double vol;
    public LoopSynth(double volume){
        sig = ReadSound.readSoundDoubles("22.wav");
        f2 = 932;
                env = GUI.open(new File("envs1.txt")).get(3);
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
        

        for(int i = 0; i < WaveWriter.SAMPLE_RATE * 6; i++){
            double envVal = 0;
            if(startVol == 1)
                envVal = env.getValue(i / (double) (WaveWriter.SAMPLE_RATE * 6));
            else if(startVol == 0)
                envVal = env.getValue(1 - i / (double) (WaveWriter.SAMPLE_RATE * 6));
            if(i < 100)
                envVal *= i / 100.0;
            if(i > WaveWriter.SAMPLE_RATE * 6 - 100)
            envVal *= (WaveWriter.SAMPLE_RATE * 6 - i) / 100.0;
            for(int n = 0; n < pan.length; n++){
                frames[n][i + startFrame] += envVal * pan[n] * vol * processed[i % processed.length];
            }
        }
    }
}
