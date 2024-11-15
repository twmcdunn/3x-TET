
/**
 * Write a description of class FMSynth here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class FMSynth implements Synth
{
    private double[] waveTable;
    public FMSynth(){
        waveTable = new double[WaveWriter.SAMPLE_RATE];
        for(int i = 0; i < WaveWriter.SAMPLE_RATE; i++){
            waveTable[i] = Math.sin(2 * Math.PI * i / (double) WaveWriter.SAMPLE_RATE);
        }
    }

    public double sin(double x){
        double scaledX = (WaveWriter.SAMPLE_RATE * x / (2 * Math.PI));
        int flooredX = (int)scaledX;
        double fract = scaledX - flooredX;
        int modularX = flooredX % WaveWriter.SAMPLE_RATE;
        double y1 = waveTable[modularX];
        double y2 = waveTable[(modularX + 1) % WaveWriter.SAMPLE_RATE];
        return y1 * (1-fract) + y2 * (fract);
    }
    
    public double cos(double x){
        return sin(x + Math.PI * 3 / 2.0);
    }

    public void writeNote(float[][] frames, double time, double freq, double vol, double[] pan){
        double carrier = freq;
        double modulator = freq;
        double dur = 6;
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
            if(i < 100)
                amp *= i / 100.0;
            double frame = amp * sin(2 * Math.PI * carrier * t - 3 * amp * cos(2 * Math.PI * modulator * t));
            for(int chan = 0; chan < pan.length; chan++)
                frames[chan][i + startFrame] += pan[chan] * frame;
        }
    }
}
