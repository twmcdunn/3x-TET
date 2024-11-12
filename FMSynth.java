
/**
 * Write a description of class FMSynth here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class FMSynth implements Synth
{
    public void writeNote(float[][] frames, double time, double freq, double vol, double[] pan){
        double carrier = freq;
        double modulator = freq;
        double dur = 3;
        int durFrames = (int)Math.rint(dur * WaveWriter.SAMPLE_RATE);
        int startFrame = (int)Math.rint(time * WaveWriter.SAMPLE_RATE);

        for(int i = 0; i < durFrames; i++){
            double t = i /(double)WaveWriter.SAMPLE_RATE; 
            double env = (durFrames - i) / (double)durFrames;
            double db = env;
            double amp = Math.pow(10, (60 * db - 60) / 20.0);
            amp *= vol;
            if(durFrames - i < 100){
                amp *= (durFrames - i) / 100.0;
            }
            double frame = amp * Math.sin(2 * Math.PI * carrier * t - Math.cos(2 * Math.PI * modulator * t));
            for(int chan = 0; chan < pan.length; chan++)
                frames[chan][i + startFrame] += pan[chan] * frame;
        }
    }
}
