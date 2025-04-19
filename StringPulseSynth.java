import java.util.Arrays;
import org.apache.commons.math3.complex.Complex;

public class StringPulseSynth extends Synth {
    public SampleFreq sf;
    public double vol, phaseLength = 24;
    public int myType;
    public static boolean ducking = false;
    
    public StringPulseSynth(int type) {
        myType = type;
        spatializer = new Spatializer(Math.PI * 2 / (20 + 20 * rand.nextDouble()), Math.PI * 2 * rand.nextDouble());

loadSampleFreqs(); 
        switch(type){
            case 0:
            sf = new SampleFreq(ReadSound.readSoundDoubles("28.wav"), 164);
            break;
            case 1:
            sf = new SampleFreq(ReadSound.readSoundDoubles("29.wav"), 885);
            break;
            case 2:
            sf = new SampleFreq(ReadSound.readSoundDoubles("30.wav"), 295);
            break;
            case 3:
            sf = sampleFreqs.get(0);
            break;
        }
        
        vol = 0.2;//.05 too soft
    }

    public void childWriteNote(float[][] frames, double time, double freq, double startVol, double[] pan) {
        
        double[] sample = Arrays.copyOfRange(pitchShift(sf.dry, sf.freq, freq), WaveWriter.SAMPLE_RATE,
            WaveWriter.SAMPLE_RATE * 3 / 2);
        
        for (int i = 0; i < sample.length / 2; i++) {
            sample[i] *= 2 * i / (double) sample.length;
            sample[sample.length - 1 - i] *= 2 * i / (double) sample.length;
        }
        Complex[] compFreqDom = FFT2.forwardTransformComplex(sample);
        double sec = 0.25;
        int numOfFrames = (int) Math.pow(2, (int) (Math.log(sec * WaveWriter.SAMPLE_RATE) / Math.log(2)) + 1);
        double[] longerSig = new double[numOfFrames];
        for (int i = 0; i < longerSig.length; i++) {
            longerSig[i] = Math.random() * 2 - 1;
        }
        Complex[] longerFreqDom = FFT2.forwardTransformComplex(longerSig);

        Complex[] stretchedFreqDom = new Complex[numOfFrames];
        for (int i = 0; i < stretchedFreqDom.length; i++) {
            double x = i / (double) stretchedFreqDom.length;
            double scaledX = x * (compFreqDom.length - 1);
            int flooredX = (int) scaledX;
            double fract = scaledX - flooredX;
            double r1, i1;
            if (fract > 0) {
                r1 = compFreqDom[flooredX].getReal() * (1 - fract) + compFreqDom[flooredX + 1].getReal() * fract;
                i1 = compFreqDom[flooredX].getImaginary() * (1 - fract)
                        + compFreqDom[flooredX + 1].getImaginary() * fract;
            } else {
                r1 = compFreqDom[flooredX].getReal();
                i1 = compFreqDom[flooredX].getImaginary();
            }
            double r2 = longerFreqDom[i].getReal();
            double i2 = longerFreqDom[i].getImaginary();
            double rProd = r1 * r2 - i1 * i2;
            double iProd = r1 * i2 + r2 * i1;
            stretchedFreqDom[i] = new Complex(rProd, iProd);
        }
        Complex[] timeDomain = FFT2.inverseTransform(stretchedFreqDom);
        double[] sig = new double[(int)Math.rint(sec * WaveWriter.SAMPLE_RATE)];


       

        for (int i = 0; i < sig.length; i++) {
            sig[i] = timeDomain[i].getReal();
            

            double env = 1;
            double attackLength = WaveWriter.SAMPLE_RATE / 20.0;
            if(i < attackLength)
                env = i / attackLength;
            if(i > sig.length - attackLength)
                env = (sig.length - i) / attackLength;

            sig[i] *= env;
        }


        double t = time + 12;//displaced phase
            int phaseNum = (int)(t / phaseLength);
            double phaseTime = t - (phaseLength * phaseNum);

        mix = 0.5 * (1 - Math.abs((0.5*phaseLength) - phaseTime) / (0.5*phaseLength)) + 0.5;
        sig = addReverb(sig);
        int startFrame = (int) (time * WaveWriter.SAMPLE_RATE);

        for(int i = 0; i < sig.length; i++){

            double env = 1;
      
             t = 12 + time + i / (double) WaveWriter.SAMPLE_RATE; //displaced phase
             phaseNum = (int)(t / phaseLength);
             phaseTime = t - (phaseLength * phaseNum);
             env *= Math.pow(10, -1 * (Math.abs(phaseTime - (0.5*phaseLength)) / (0.5*phaseLength)));
            

            sig[i] *= env;

            if(ducking)
                for (int n = 0; n < pan.length; n++) {
                    frames[n][i + startFrame] *= (1-env) * 0.2 + 0.8;
                }
        }
       
        for (int n = 0; n < pan.length; n++) {
            for (int i = 0; i < sig.length; i++) {
                frames[n][i + startFrame] += sig[i] * pan[n] * vol;
            }
        }
    }

    public static void test() {
        Synth synth = new StringPulseSynth(3);
        WaveWriter ww = new WaveWriter("stringpulsetest");

        int[] chord = new int[] { 0 + 21 * 4, 3 + 21 * 4, 7 + 21 * 4, 12 + 21 * 4, 15 + 21 * 4};
        double time = 0;
        for(time = 0; time < 24; time += 0.25)
            for (int n = 0; n < chord.length; n++) {
                int note = chord[n];
                //synth.writeNote(ww.df, time, Piece.c0Freq * Math.pow(2, note / 21.0), 0, new double[] { 1 });
                synth.writeNote(ww.df, time, 2 * Piece.c0Freq * Math.pow(2, note / 21.0), 0, new double[] { 1 });

            }
        ww.render(1);
    }

    public static void main(String[] args) {
        test();
    }
}
