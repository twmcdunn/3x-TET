
/**
 * Write a description of class SampleSynth here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class SampleSynth implements Synth
{
    public double[] sig;
    public double f2;

    /**
     * Constructor for objects of class SampleSynth
     */
    public SampleSynth(int sampleNumber)
    {
        switch(sampleNumber){
            case 0:
            f2 = 1760;
            sig = ReadSound.readSoundDoubles("8.wav");
            break;
            case 1:
            f2 = 1327;
            sig = ReadSound.readSoundDoubles("9.wav");
            break;
        }
    }

    public void writeNote(float[][] frames, double time, double freq, double vol, double[] pan){
        double f1 = freq;
        double[] processed = new double[(int)(sig.length * f2 / f1)];
        int startFrame = (int)Math.rint(time * WaveWriter.SAMPLE_RATE);
        for(int i = 0; i < processed.length; i++){
            double exInd = i * f1 / f2;
            int index = (int)exInd;
            double fract = exInd - index;
            double frame1 = sig[index];
            double frame2 = frame1;
            if(index + 1 < sig.length)
                frame2 = sig[index + 1];
            double frame = frame1 * (1-fract) + frame2 * fract;
            frame *= vol;
            for(int chan = 0; chan < pan.length; chan++)
                try{
                    frames[chan][i + startFrame] += pan[chan] * frame;
                }catch(Exception e){
                    System.out.println(e);
                }
        }
    }
}
