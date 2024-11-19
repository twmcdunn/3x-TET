
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
            sig = ReadSound.readSoundDoubles("8.wav"); // xylo
            break;
            case 1:
            f2 = 1327;
            sig = ReadSound.readSoundDoubles("9.wav"); // piano
            break;
            case 2:
            f2 = 626;
            sig = ReadSound.readSoundDoubles("10.wav");//26.wav unfilt but shorter //unfiltered chime, octavve 5
            break;
            case 3:
            f2 = 626;
            sig = ReadSound.readSoundDoubles("29.wav"); //filtered chime oct 4 ok oc 5 good
            break;
            case 4:
            f2 = 626;
            sig = ReadSound.readSoundDoubles("16.wav");//nice percussive chime sound oct 5
            break;
            case 5:
            f2 = 172;
            sig = ReadSound.readSoundDoubles("bell.wav");
            break;
            case 6:
            f2 = 172;
            sig = ReadSound.readSoundDoubles("18.wav");//nice version of church bell
            break;
            case 7:
            f2 = 112;
            sig = ReadSound.readSoundDoubles("19.wav");//low piano
            break;
            case 8:
            f2 = 2795;
            sig = ReadSound.readSoundDoubles("20.wav");//original chime sound
            break;
            case 9:
            f2 = 2795;
            sig = ReadSound.readSoundDoubles("21.wav");//processed chime sound oct 7 sound good
            break;
            case 10:
            f2 = 2795;
            sig = ReadSound.readSoundDoubles("23.wav");//variant w/ delay attack
            break;
            case 11:
            f2 = 1760;
            sig = ReadSound.readSoundDoubles("30.wav");//filtered xylo oct 6 ok
            break;
            case 12:
            f2 = 627;
            sig = ReadSound.readSoundDoubles("32.wav");//filtered xylo oct 6 ok
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

    public static void testFilteredChime() {
        Synth synth = new SampleSynth(12);
        WaveWriter ww = new WaveWriter("filteredChime");

        float[][] sound = new float[1][WaveWriter.SAMPLE_RATE * 60];
        synth.writeNote(sound, 0, 1761, 0.1, new double[] { 1 });
        double[] unfiltered = new double[sound[0].length];
        for (int i = 0; i < sound[0].length; i++) {
            unfiltered[i] += sound[0][i];
        }

        double[] filtered = new double[sound[0].length];
        double[] freqs = new double[] { 627,1677,3142,3543,5782,7052};
        double[] amps = new double[] { 1,0.5,0.25,.125,0.0625,0.03125};
        for (int n = 0; n < 5; n++) {
            for (int j = 0; j < freqs.length; j++) {
                double f = freqs[j];
                double[] partial = BPF.BPF(unfiltered, WaveWriter.SAMPLE_RATE, f, 10);
                for (int i = 0; i < partial.length; i++) {
                    filtered[i] += amps[j] * 0.02 * partial[i] / (double) freqs.length;
                }
            }
            unfiltered = filtered;
        }

        //filtered = unfiltered;

        double max = Double.MIN_VALUE;

        for (int i = 0; i < filtered.length; i++) {
            max = Math.max(max, Math.abs(filtered[i]));
            
        }

        for (int i = 0; i < filtered.length; i++) {
            ww.df[0][i] += filtered[i] / max;
        }
        ww.render(1);
    }

    public static void main(String[] args) {
        testFilteredChime();
    }
}
