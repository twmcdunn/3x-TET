
/**
 * Write a description of class BPF here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class BPF
{
    public static double[] BPF(double[] x, double sampleFreq, double centerFreq, double gain, double dur)
    {
        double[] y = new double[x.length];
        double A = Math.pow(10, gain / 40.0);
        double q = (dur * centerFreq);// / A;
        double w0 = 2 * Math.PI * centerFreq / sampleFreq;
        double alfa = Math.sin(w0) / (2 * q);//Math.sin(w0) * Math.sinh((Math.log(2) / 2.0) * bw * (w0 / (Math.sin(20))));
        double b0 = q * alfa;//1 + A * alfa;
        double b1 = 0;//-2 * Math.cos(w0);
        double b2 = - q * alfa;;//1 - A * alfa;
        double a0 = 1 + alfa;
        double a1 = -2 * Math.cos(w0);
        double a2 = 1 - alfa;
        for(int n = 2; n < y.length; n++){
            y[n] = (b0/a0) * x[n] + (b1/a0) * x[n - 1] + (b2/a0) * x[n - 2] 
            - (a1/a0) * y[n - 1] - (a2/a0) * y[n - 2];
        }
        return y;
    }

    public static double[] BPF(double[] x, double sampleFreq, Signal centerFreq, Signal gain, Signal dur)
    {
        double[] y = new double[x.length];

        for(int n = 2; n < y.length; n++){
            double A = Math.pow(10, gain.getValue(n / (double)(WaveWriter.SAMPLE_RATE)) / 40.0);
            double q = (dur.getValue(n / (double)(WaveWriter.SAMPLE_RATE)) * centerFreq.getValue(n / (double)(WaveWriter.SAMPLE_RATE)));// / A;
            double w0 = 2 * Math.PI * centerFreq.getValue(n / (double)(WaveWriter.SAMPLE_RATE)) / sampleFreq;
            double alfa = Math.sin(w0) / (2 * q);//Math.sin(w0) * Math.sinh((Math.log(2) / 2.0) * bw * (w0 / (Math.sin(20))));
            double b0 = q * alfa;//1 + A * alfa;
            double b1 = 0;//-2 * Math.cos(w0);
            double b2 = - q * alfa;;//1 - A * alfa;
            double a0 = 1 + alfa;
            double a1 = -2 * Math.cos(w0);
            double a2 = 1 - alfa;

            y[n] = (b0/a0) * x[n] + (b1/a0) * x[n - 1] + (b2/a0) * x[n - 2] 
            - (a1/a0) * y[n - 1] - (a2/a0) * y[n - 2];
        }
        double onset = WaveWriter.SAMPLE_RATE/40.0;
        for(int i = 0; i < onset; i++){
            y[y.length - 1 - i] *= i / onset;
            y[i] *= i / onset;
        }
        return y;
    }

    public static void test(){
        double[] sig = new double[WaveWriter.SAMPLE_RATE * 30];
        for(int i = 0; i < sig.length; i++) 
            sig[i] = 2 * Math.random() - 1;

        for(int i = 0; i < 3; i++){
            sig = BPF(sig, WaveWriter.SAMPLE_RATE, 110, 0, 5);
            sig = BPF(sig, WaveWriter.SAMPLE_RATE, 330, -6, 5);
            sig = BPF(sig, WaveWriter.SAMPLE_RATE, 550, -12, 1);
            double max = 0;
            for(int n = 0; n < sig.length; n++) 
                max = Math.max(Math.abs(sig[n]), max);
            for(int n = 0; n < sig.length; n++) 
                sig[n] /= max;
        }
        WaveWriter ww = new WaveWriter("TEST");

        for(int i = 0; i < sig.length; i++)
            ww.df[0][i] += sig[i];
        ww.render();

    }
}
