//based on https://webaudio.github.io/Audio-EQ-Cookbook/audio-eq-cookbook.html
import java.util.Arrays;
/**
 * Write a description of class PeakingEQ here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class PeakingEQ
{

    public static double[] eqPeak(double[] x, double sampleFreq, double centerFreq, double gain, double bw)
    {
        double[] y = new double[x.length];
        double A = Math.pow(10, gain / 40.0);
        double w0 = 2 * Math.PI * centerFreq / sampleFreq;
        double alfa = Math.sin(w0) * Math.sinh((Math.log(2) / 2.0) * bw * (w0 / (Math.sin(20))));
        double b0 = 1 + A * alfa;
        double b1 = -2 * Math.cos(w0);
        double b2 = 1 - A * alfa;
        double a0 = 1 + A * alfa;
        double a1 = -2 * Math.cos(w0);
        double a2 = 1 - A * alfa;
        for(int n = 2; n < y.length; n++){
            y[n] = (b0/a0) * x[n] + (b1/a0) * x[n - 1] + (b2/a0) * x[n - 2] 
            - (a1/a0) * y[n - 1] - (a2/a0) * y[n - 2];
        }
        return y;
    }

    public static double[] eqPeak1(double[] x, double sampleFreq, double centerFreq, double gain, double q)
    {
        double[] y = new double[x.length];
        double A = Math.pow(10, gain / 40.0);
        double w0 = 2 * Math.PI * centerFreq / sampleFreq;
        double alfa = Math.sin(w0) / (2 * q);//Math.sin(w0) * Math.sinh((Math.log(2) / 2.0) * bw * (w0 / (Math.sin(20))));
        double b0 = 1 + A * alfa;
        double b1 = -2 * Math.cos(w0);
        double b2 = 1 - A * alfa;
        double a0 = 1 + A * alfa;
        double a1 = -2 * Math.cos(w0);
        double a2 = 1 - A * alfa;
        for(int n = 2; n < y.length; n++){
            y[n] = (b0/a0) * x[n] + (b1/a0) * x[n - 1] + (b2/a0) * x[n - 2] 
            - (a1/a0) * y[n - 1] - (a2/a0) * y[n - 2];
        }
        return y;
    }

    public static double[] eqPeak2(double[] x, double sampleFreq, double centerFreq, double gain, double dur)
    {
        double[] y = new double[x.length];
        double A = Math.pow(10, gain / 40.0);
        double q = (dur * centerFreq) / A;
        double w0 = 2 * Math.PI * centerFreq / sampleFreq;
        double alfa = Math.sin(w0) / (2 * q);//Math.sin(w0) * Math.sinh((Math.log(2) / 2.0) * bw * (w0 / (Math.sin(20))));
        double b0 = 1 + A * alfa;
        double b1 = -2 * Math.cos(w0);
        double b2 = 1 - A * alfa;
        double a0 = 1 + A * alfa;
        double a1 = -2 * Math.cos(w0);
        double a2 = 1 - A * alfa;
        for(int n = 2; n < y.length; n++){
            y[n] = (b0/a0) * x[n] + (b1/a0) * x[n - 1] + (b2/a0) * x[n - 2] 
            - (a1/a0) * y[n - 1] - (a2/a0) * y[n - 2];
        }
        return y;
    }

    public static double[] eqPeak(double[] x, double sampleFreq, Signal centerFreq, Signal gain, Signal dur)
    {
        double[] y = new double[x.length];

        for(int n = 2; n < y.length; n++){
            double g = gain.getValue(n / sampleFreq);
            double cf = centerFreq.getValue(n / sampleFreq);
            double d = dur.getValue(n / sampleFreq) / 30.0;
            double A = Math.pow(10, g / 40.0);
            double q = (d * cf); // A;
            double w0 = 2 * Math.PI * cf / sampleFreq;
            double alfa = Math.sin(w0) / (2 * q);//Math.sin(w0) * Math.sinh((Math.log(2) / 2.0) * bw * (w0 / (Math.sin(20))));
            double b0 = 1 + A * alfa;
            double b1 = -2 * Math.cos(w0);
            double b2 = 1 - A * alfa;
            double a0 = 1 + A * alfa;
            double a1 = -2 * Math.cos(w0);
            double a2 = 1 - A * alfa;
            y[n] = (b0/a0) * x[n] + (b1/a0) * x[n - 1] + (b2/a0) * x[n - 2] 
            - (a1/a0) * y[n - 1] - (a2/a0) * y[n - 2];
        }
        return y;
    }
    
    

    public static void test(){
        double[] sound = new double[WaveWriter.SAMPLE_RATE * 3];
        /*
        for(int i = 0; i < 20; i++)
        sound[i] = Math.random();
         */
        sound[0] = 1;
        sound = eqPeak2(Arrays.copyOf(sound, 60* WaveWriter.SAMPLE_RATE), WaveWriter.SAMPLE_RATE, 440, -12, 10);
        //sound = sampleMethod(sound, WaveWriter.SAMPLE_RATE, 440, 1, 0.1);
        //sound = sampleMethod(sound, WaveWriter.SAMPLE_RATE, 440, 1, 0.1);
        //sound = sampleMethod(sound, WaveWriter.SAMPLE_RATE, 550, 0.1, 0.1);
        // sound = sampleMethod(sound, WaveWriter.SAMPLE_RATE, 330, 0.1, 0.1);

        WaveWriter ww = new WaveWriter("test");

        for(int i = 0; i < sound.length; i++){
            ww.df[0][i] += sound[i];
            ww.df[1][i] += sound[i];
        }

        ww.render();
    }

    public static void test1(){
        for(int freq = 110; freq <=880; freq += 100){
            double[] sound = new double[WaveWriter.SAMPLE_RATE * 3];

            sound[0] = 1;
            sound = eqPeak2(Arrays.copyOf(sound, 60* WaveWriter.SAMPLE_RATE), WaveWriter.SAMPLE_RATE, 440, -12, 1.5);

            WaveWriter ww = new WaveWriter("test" + freq);

            for(int i = 0; i < sound.length; i++){
                ww.df[0][i] += sound[i];
                ww.df[1][i] += sound[i];
            }

            ww.render();
        }
    }

    public static void test2(){
        double[] sound = new double[WaveWriter.SAMPLE_RATE * 20];

        for(int i = 0; i < sound.length; i++)
            sound[i] = Math.random() * 2 - 1;

        sound = eqPeak1(Arrays.copyOf(sound, 60* WaveWriter.SAMPLE_RATE), WaveWriter.SAMPLE_RATE, 440, -12, 1200);
        //sound = sampleMethod(sound, WaveWriter.SAMPLE_RATE, 440, 1, 0.1);
        //sound = sampleMethod(sound, WaveWriter.SAMPLE_RATE, 440, 1, 0.1);
        //sound = sampleMethod(sound, WaveWriter.SAMPLE_RATE, 550, 0.1, 0.1);
        // sound = sampleMethod(sound, WaveWriter.SAMPLE_RATE, 330, 0.1, 0.1);

        WaveWriter ww = new WaveWriter("test");

        for(int i = 0; i < sound.length; i++){
            ww.df[0][i] += sound[i];
            ww.df[1][i] += sound[i];
        }

        ww.render();
    }
}
