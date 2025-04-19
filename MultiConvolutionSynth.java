import java.util.Arrays;

public class MultiConvolutionSynth extends SustainedSynth {
    public MultiConvolutionSynth(int sampleType, double volume){
        super(sampleType, volume);
    }

    public void childWriteNote(float[][] frames, double time, double freq, double startVol, double[] pan) {

        //setSigAndF2(2);
        double[] processed = susSound(sig, f2, freq, startVol);
        
        int actualDur = processed.length;
        
        for(int i = 0; i < 2; i++)
         processed = FFT2.convAsImaginaryProduct(processed, processed);

        processed = Arrays.copyOf(processed, actualDur);//+5000

        for (int i = 0; i < 5000; i++) {

            processed[processed.length - 1 - i] *= i / 5000.0;
            // processed[(processed.length - 1) - i] *= (i / 1000.0);
            // System.out.println(processed[i]);

            if (startVol == 0)
                processed[i] *= i / 5000.0;

        }
        
        //setSigAndF2(0);
        //setSigAndF2(2);

        //processed = BPF.BPF(processed, WaveWriter.SAMPLE_RATE, freq, 0.0005);
 
         processed = addReverb(processed);
 
         //processed = BPF.BPF(processed, WaveWriter.SAMPLE_RATE, freq, 0.1);
 
         
         int startFrame = (int) Math.rint(time * WaveWriter.SAMPLE_RATE);
 
         for(int i = 0; i < processed.length; i++){
             for(int n = 0; n < pan.length; n++){
                 frames[n][i + startFrame] += pan[n] * vol * processed[i];
             }
         }
     }

     public static void test() {
        Synth synth = new MultiConvolutionSynth(19,0.01);
        WaveWriter ww = new WaveWriter("multiConvTest");

        //float[][] sound = new float[1][WaveWriter.SAMPLE_RATE * 60];
        synth.writeNote(ww.df, 0, 440, -1, new double[] { 1 });
        double max = Double.MIN_VALUE;

        for (int i = 0; i < ww.df[0].length; i++) {
            max = Math.max(max, Math.abs(ww.df[0][i]));

        }

        for (int i = 0; i < ww.df[0].length; i++) {
            ww.df[0][i] /= max;
        }
        ww.render(1);
    }

    public static void main(String[] args) {
        test();
    }
}
