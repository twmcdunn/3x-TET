import java.util.Arrays;

/**
 * Write a description of class SampleSynth here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class OmniRegisterSynth extends Synth {
    public double[] sig;
    public double f2, metalProb;
    public int type;
    public double[] wetSig;
    double mix = 0.7;
    double[] cathedral;
    public SampleFreq mySf;
    public boolean useGlass;

    /**
     * Constructor for objects of class SampleSynth
     */

     

    public OmniRegisterSynth() {
        spatializer = new Spatializer(Math.PI * 2 / (20 + 20 * rand.nextDouble()), Math.PI * 2 * rand.nextDouble());
        loadSampleFreqs();
        mySf = new SampleFreq(ReadSound.readSoundDoubles("31.wav"),2055);
        useGlass = true;
    }

   

    public void childWriteNote(float[][] frames, double time, double freq, double vol, double[] pan) {
        metalProb = 0;
        if(time > 9 * 60 + 45 && time < 10 * 60){
            metalProb = (time - (9 * 60 + 45)) / 15.0;
        }
        else if(time >= 10 * 60 && time < 10 * 60 + 15){
            metalProb = 1;
        }
        else if(time >= 10 * 60 + 15 && time < 10 * 60 + 30){
            metalProb = 1 - (time - (10 * 60 + 15)) / 15.0;
        }
        if(!useGlass)
             metalProb = 1;

        SampleFreq sf = mySf;
        if(!useGlass || Piece.rand.nextDouble() < metalProb)
            sf = getClosestSampleFreq(freq);
        sig = pitchShift(sf.dry, sf.freq, freq);

        sig = Arrays.copyOf(sig, WaveWriter.SAMPLE_RATE * 2);
        for(int i = 0; i < sig.length; i++){
           sig[sig.length - 1 - i] *= i / (double)sig.length;
        }

        try{
        double globalReverb = Piece.reverbEnv.getValue(time);
         mix = (1 - globalReverb) + globalReverb * vol;// max reverb is 50% mix
         mix = 0.5 + 0.5 * mix;
         sig = addReverb(sig);
        }
        catch(Exception e){}
        
        int startFrame = (int) Math.rint(time * WaveWriter.SAMPLE_RATE);

        for (int i = 0; i < sig.length && i < frames[0].length; i++) {
           
            double frame = sig[i];
            frame *= vol;
            for (int chan = 0; chan < pan.length; chan++)
                try {
                    frames[chan][i + startFrame] += pan[chan] * frame;
                } catch (Exception e) {
                    System.out.println(e);
                }
        }
    }

}
