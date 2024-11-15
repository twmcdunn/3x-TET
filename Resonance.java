import java.util.Arrays;
import java.util.ArrayList;
/**
 * Write a description of class Resonator here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Resonance
{

    public static double[] Resonator(double[] input, double freq, double vol, double dur)
    {

        //beccause dur also varies w/ freq we find out the hypothetical real duration as a function of
        //freq if dur were held constant at 3. Then we change dur proportionately
        double durAt3 = 1.017608 + (12350.92-1.017608)/(1 + Math.pow((freq/5.619966),2.003796));
        dur *= durAt3/3.0;

        //this formula was determined through emperical testtts of the peqkingeq filter
        double bw = 1/(dur*23.25814 - 23.72093);

        System.out.println(bw + " " + dur);
        return PeakingEQ.eqPeak(Arrays.copyOf(input, (int)((60) * WaveWriter.SAMPLE_RATE)), WaveWriter.SAMPLE_RATE, freq, vol, bw);
    }

    public static double[] Resonator2(double[] input, double freq, double vol, double dur)
    {

        return PeakingEQ.eqPeak2(Arrays.copyOf(input, (int)((60) * WaveWriter.SAMPLE_RATE)), WaveWriter.SAMPLE_RATE, freq, vol, dur / 50.0);
    }


    public static double[] Resonator1(double[] input, double freq, double vol, double q)
    {
        return PeakingEQ.eqPeak(Arrays.copyOf(input, (int)((60) * WaveWriter.SAMPLE_RATE)), WaveWriter.SAMPLE_RATE, freq, vol, q);
    }
    
        public static double[] Resonator3(double[] input, double freq, double vol, double dur)
    {

        return BPF.BPF(Arrays.copyOf(input, (int)((60) * WaveWriter.SAMPLE_RATE)), WaveWriter.SAMPLE_RATE, freq, vol, dur);
    }

    static double maxR = Double.MIN_VALUE;
    public static double[] ResBank(double[] input, double[] freqs, double[] vols, double[] durs){
        double maxDur = Double.MIN_VALUE;
        for(int i = 0; i < durs.length; i++){
            maxDur = Math.max(maxDur, durs[i]);
        }
        double[] composite = new double[(int)((maxDur*20 + 3) * WaveWriter.SAMPLE_RATE)];
        for(int i = 0; i < freqs.length; i++){
            double[] res = Resonator2(input, freqs[i], vols[i], durs[i]);

            for(int n = 0; n < res.length; n++){
                if(res[n] > maxR){
                    maxR = res[n];
                    //System.out.println("MAX: " + maxR);
                }
                composite[n] += res[n] / (double)freqs.length;;
            }
        }
        /*
        for(int n = 0; n < composite.length; n++){
        composite[n] /= (double)freqs.length;
        }

         */
        return composite;
    }
    
    public static double[] ResBank1(double[] input, double[] freqs, double[] vols, double[] durs){
        double maxDur = Double.MIN_VALUE;
        for(int i = 0; i < durs.length; i++){
            maxDur = Math.max(maxDur, durs[i]);
        }
        double[] composite = new double[(int)((maxDur*20 + 3) * WaveWriter.SAMPLE_RATE)];
        for(int i = 0; i < freqs.length; i++){
            double[] res = Resonator3(input, freqs[i], vols[i], durs[i]);

            for(int n = 0; n < res.length; n++){
                if(res[n] > maxR){
                    maxR = res[n];
                    //System.out.println("MAX: " + maxR);
                }
                composite[n] += res[n] / (double)freqs.length;;
            }
        }
        /*
        for(int n = 0; n < composite.length; n++){
        composite[n] /= (double)freqs.length;
        }

         */
        return composite;
    }

    public static double dbAmp(double db){
        double amp = Math.pow(10,db/1.0);
        //amp = 1;
        System.out.println(amp);
        return db;
    }

    public static void test1(){
        for(int q = 1; q < 5; q++)
            for(int oct = 0; oct < 4; oct++){
                double[] sound = new double[1];
                sound[0] = 1;

                sound = Resonator1(sound, 110 * Math.pow(2,oct), 1, q * 0.1);
                sound[0] = 0;
                WaveWriter ww = new WaveWriter("oct" + oct + "_q" + q);

                for(int i = 0; i < sound.length; i++){
                    ww.df[0][i] += sound[i];
                    ww.df[1][i] += sound[i];
                }

                ww.render();
            }

    }

    public static void analyze(){
        ArrayList<double[]> data = new ArrayList<double[]>();

        for(int oct = 0; oct < 4; oct++)
            for(int q = 1; q < 5; q++){
                float[] sound = ReadSound.readSound("oct" + oct + "_q" + q + ".wav");
                double[] sig = new double[sound.length];
                for(int i = 0; i < sound.length; i++)
                    sig[i] += sound[i];
                double ref = rms(sig, 0, WaveWriter.SAMPLE_RATE / 10);
                double t = 0;
                for(int i = 0; i < sig.length - WaveWriter.SAMPLE_RATE / 10; i++)
                    if(20 * Math.log(rms(sig,i, i + WaveWriter.SAMPLE_RATE / 10) / ref) <= -24){
                        t = i / (double) WaveWriter.SAMPLE_RATE;
                        data.add(new double[]{110 * Math.pow(2,oct), q / 10.0, t});
                        break;
                    }
            }
        System.out.println();
        for(int i = 0; i < 3; i++){
            for(double[] point: data)
                System.out.println(point[i]);
            System.out.println();
        }
    }

    public static double rms(double[] sig, int start, int limit){
        double rms = 0;
        for(int i = start; i < limit; i++){
            rms += Math.pow(sig[i], 2) / (double)(limit - start);
        }
        rms = Math.sqrt(rms);
        return rms;
    }

    public static void test(){
        for(int n = 0; n < 6; n++){
            double[] sound = new double[1];
            sound[0] = 1;
            //sound = ResBank(sound, new double[]{440,550,330}, new double[]{1,0.7,0.5}, new double[]{3,2,7});
            //sound = ResBank(sound, new double[]{6083}, new double[]{1}, new double[]{17.4});

            sound = Resonator(sound, 110 * Math.pow(2,n), 1, 3);
            sound[0] = 0;
            WaveWriter ww = new WaveWriter("test");

            for(int i = 0; i < sound.length; i++){
                ww.df[0][i] += sound[i];
                ww.df[1][i] += sound[i];
            }

            ww.render();
        }
    }

    public static void churchBellTest(){
        double[] sound = new double[1];
        sound[0] = 1;
        sound = ResBank(sound, new double[]{86,170,209,263,350,522,716,833,994,1183,1421,1668,1932,2284,4180,6083}, 
            new double[]{-27.0,-10.6,-14.5,-33.3,-23.6,-12.4,-32.5,-34.3,
                    -28.4,-24.8,-15.4,-32.9,-21.9,-29.8,-46.7,-56.7}, 
            new double[]{10.8,17.4,14.6,8.0,17.2,18.1,10.2,8.9,7.9,6.7,5.0,3.0,2.6,1.6,0.5,0.1});
        sound[0] = 0;

        WaveWriter ww = new WaveWriter("cb");
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for(int i = 0; i < sound.length; i++){
            min = Math.min(min,sound[i]);
            max = Math.max(max,sound[i]);
            ww.df[0][i] += sound[i];
            ww.df[1][i] += sound[i];
        }
        //System.out.println(min + " " + max);

        ww.render();
    }
    
        public static void churchBellTest1(){
        double[] sound = new double[1];
        sound[0] = 1;
        sound = ResBank(sound, new double[]{86,170,209,263,350,522,716,833,994,1183,1421,1668,1932,2284,4180,6083}, 
            new double[]{-27.0,-10.6,-14.5,-33.3,-23.6,-12.4,-32.5,-34.3,
                    -28.4,-24.8,-15.4,-32.9,-21.9,-29.8,-46.7,-56.7}, 
            new double[]{10.8,17.4,14.6,8.0,17.2,18.1,10.2,8.9,7.9,6.7,5.0,3.0,2.6,1.6,0.5,0.1});
        sound[0] = 0;

        WaveWriter ww = new WaveWriter("cb1");
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for(int i = 0; i < sound.length; i++){
            min = Math.min(min,sound[i]);
            max = Math.max(max,sound[i]);
            ww.df[0][i] += sound[i];
            ww.df[1][i] += sound[i];
        }
        //System.out.println(min + " " + max);

        ww.render();
    }
    
     public static void churchBellTest2(){
        double[] sound = new double[1];
        sound[0] = 1;
        sound = ResBank1(sound, new double[]{86,170,209,263,350,522,716,833,994,1183,1421,1668,1932,2284,4180,6083}, 
            new double[]{-27.0,-10.6,-14.5,-33.3,-23.6,-12.4,-32.5,-34.3,
                    -28.4,-24.8,-15.4,-32.9,-21.9,-29.8,-46.7,-56.7}, 
            new double[]{10.8,17.4,14.6,8.0,17.2,18.1,10.2,8.9,7.9,6.7,5.0,3.0,2.6,1.6,0.5,0.1});
        sound[0] = 0;

        WaveWriter ww = new WaveWriter("cb2");
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for(int i = 0; i < sound.length; i++){
            min = Math.min(min,sound[i]);
            max = Math.max(max,sound[i]);
            ww.df[0][i] += sound[i];
            ww.df[1][i] += sound[i];
        }
        //System.out.println(min + " " + max);

        ww.render();
    }
    
     public static void celloPluck(){
        double[] sound = new double[1];
        sound[0] = 1;
        sound = ResBank(sound, new double[]{
            95,191,285,380,475,572,667,
		766,864,953,1047,1144,1333}, 
            new double[]{-11.8,-11.7,-29.2,-35.2,-44.9,-41.8,-40.3,
		-49.9,-50.0,-49.3,-61.6,-60.4,-67.9}, 
            new double[]{6.0,4.3,4.5,2.6,1.2,1.2,0.8,0.6,0.5,0.7,0.3,0.3,0.3});
        sound[0] = 0;

        WaveWriter ww = new WaveWriter("celloPluck");
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for(int i = 0; i < sound.length; i++){
            min = Math.min(min,sound[i]);
            max = Math.max(max,sound[i]);
            ww.df[0][i] += sound[i];
            ww.df[1][i] += sound[i];
        }
        //System.out.println(min + " " + max);

        ww.render();
    }
}
