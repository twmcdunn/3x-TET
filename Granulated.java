import java.util.ArrayList;
import java.util.Arrays;

public class Granulated extends SampleSynth {
    double f2, minT, maxT;
    double[] sig, wetSig;
    int[] chord = new int[] { 5, 10, 14, 2 };//{ 5, 10, 14, 2 }, { 0, 4, 9, 12 }
    
    boolean randEnv;
    SampleFreq sf;
    public static boolean useSF = false;

    public Granulated(boolean randomizeEnv) {
        cathedral = ReadSound.readSoundDoubles("cathedral.wav");
        minT = Double.MAX_VALUE;
        maxT = Double.MIN_VALUE;
        loadSampleFreqs();

        sf = new SampleFreq(ReadSound.readSoundDoubles("34.wav"), 107);
        sf = new SampleFreq(ReadSound.readSoundDoubles("35.wav"), 181);
        randEnv = randomizeEnv;
    }



    public void setChord(int[] c){
        chord = c;
    }

    public void writeGrain(double[] frames, double time, double freq, double v){

        double[] shifted = pitchShift(sig, f2, freq);
       
        shifted = Arrays.copyOf(shifted, WaveWriter.SAMPLE_RATE / 10);
        for(int i = 0; i < shifted.length; i++){
            shifted[i] *= 1 - (i / (double)shifted.length);
        }

        mix = v;
        
        //the benefit here is that we can have reverb applied to a processed signal
        //it's already been sped or slowed and had a short envelope applied
        //shifted = addReverb(shifted);
    
/*
        for(int i = 0; i < shifted.length; i++){
            shifted[i] *= v;
        }
             */


        int startFrame = (int) Math.rint(time * WaveWriter.SAMPLE_RATE);
        for (int i = 0; i < shifted.length; i++) {
                frames[i + startFrame] += shifted[i];
            
        }
    }

  

    public void childWriteNote(float[][] frames, double time, double target, double vol, double[] pan) {
        if(false)
            return;

            SampleFreq closetsSampleFreq = getClosestSampleFreq(target);

            if(useSF && false)
                closetsSampleFreq = sf;

            sig = closetsSampleFreq.dry;
            f2 = closetsSampleFreq.freq;

            double r = Math.random();
            double attackLength = 0.5;
            if(useSF)
                attackLength = 16;
            double decayLength = 10;
            boolean stayWet = false;
            
            if(randEnv)
                if (r > 2 / 3.0) {
                    attackLength = 0.5;
                    decayLength = 0.5;
                    stayWet = true;
                } else if (r > 1 / 3.0) {
                    attackLength = 0.5;
                    decayLength = 0.5;
                }

            boolean invertReverb = false;
            boolean reverseSig = false;

            double dry[] = new double[(int)((attackLength + decayLength + 0.1) * WaveWriter.SAMPLE_RATE) + 1];
            int grains = 500;
        if(useSF)
            grains = 2000;
        for (int n = 0; n < grains; n++) {
            double t = Math.random();
            t = 1 - (Math.log(t) + 10)/10.0;//(Math.pow(10, t) - 1) / 9.0;
            t = Math.min(1,t);

            if(useSF){
                if(t > 0.5 && (t - 0.5) * 2 > Math.random())
                   chord = new int[] { 5, 10, 14, 2 };
                else
                   chord = new int[]{ 0, 4, 9, 12 };
                
            }

            int note = chord[(int) (Math.random() * chord.length)];
            int tet = 15;
            if(Piece.seq != null)
            tet = Piece.seq.TET;
            double freq = Piece.c0Freq * Math.pow(2,Piece.closestOct((int)target,note,tet) / (double)tet);
            minT = Math.min(t, minT);
            maxT = Math.max(t, maxT);
            
            writeGrain(dry, attackLength - t * attackLength, freq, vol * (1-t));
            if(!useSF)
                writeGrain(dry, attackLength + t * decayLength, freq, vol * (1-t));
            //gong.writeNote(ww.df, 20 - t, freq, t / 10.0, new double[] { 1 });
            
        }

        double[] wet = getReverb(dry);
        dry = Arrays.copyOf(dry, wet.length);
        double dMax = 0;
        for(int i = 0; i < dry.length; i++){
            dMax = Math.max(dMax, Math.abs(dry[i]));
        }
        for(int i = 0; i < dry.length; i++){
            dry[i] /= dMax;
        }
        for(int i = 0; i < dry.length; i++){
            double mix = 1 - i / (attackLength * WaveWriter.SAMPLE_RATE);

            if(i > attackLength* WaveWriter.SAMPLE_RATE && !useSF){
                if(!stayWet)
                    mix = Math.max(0,(i - attackLength * WaveWriter.SAMPLE_RATE) / (decayLength * WaveWriter.SAMPLE_RATE));
                else
                    mix = 0;
            }
            if (invertReverb || useSF)
                mix = 1 - mix;

            if(useSF){
                mix = Math.min(mix, 1);
                mix = 0.7 * mix;
            }
                //mix = 0;
            dry[i] = dry[i] * mix + wet[i] * (1-mix);
        }

        double[] sig = dry;
        if(reverseSig)
            for (int i = 0; i < sig.length; i++)
                sig[i] = dry[dry.length - 1 - i];
        spatializer.setMagnitude(1-vol);
        for(int i = 0; i < sig.length; i++){
            double t = (i + (int)((time - attackLength) * WaveWriter.SAMPLE_RATE))/(double)WaveWriter.SAMPLE_RATE;
            pan = spatializer.pan(time);
            for(int n = 0; n < pan.length; n++){
                frames[n][i + (int)((time - attackLength) * WaveWriter.SAMPLE_RATE)] += pan[n] * vol * sig[i];
            }
        }
    }

   
    public static void main(String[] args) {
        useSF = true;
        Granulated g = new Granulated(false);
        WaveWriter ww = new WaveWriter("GRAN");
        g.writeNote(ww.df, 32, 15*5, 1, new double[]{1});
        System.out.println("T RANGE: " + g.minT + ", " + g.maxT);
        ww.render(1);
    }


}
