import java.util.Arrays;
import java.util.ArrayList;
/**
 * Write a description of class Resonator here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Resonance extends Synth
{

    public Resonance(){
        spatializer = new Spatializer(Math.PI * 2 / (20 + 20 * rand.nextDouble()), Math.PI * 2 * rand.nextDouble());
    }

    
    public static double[] Resonator4(double[] input, double freq, Signal qDur, double dur)
    {

        Signal f = new Signal() {
            public double getValue(double t)
            {return freq;}
        };
        return BPF.BPF(Arrays.copyOf(input, (int)((dur) * WaveWriter.SAMPLE_RATE)), WaveWriter.SAMPLE_RATE, f, qDur);
    }

    static double maxR = Double.MIN_VALUE;

    public static double[] ResBank2(double[] input, double[] freqs, double[] vols, double[] durs, double dur){
        Signal[] d = new Signal[durs.length];
        for(int i = 0; i < durs.length;i++){
            final double aDur = durs[i];
            d[i] = new Signal(){
                public double getValue(double time) {
                    return aDur;
                }
            };
        }
        return ResBank2(input,freqs,null,d,dur);
    }
    
    public static double[] ResBank2(double[] input, double[] freqs, Signal[] vols, Signal[] durs, double dur){
        
        double[] composite = new double[(int)(dur * WaveWriter.SAMPLE_RATE)];
        for(int i = 0; i < freqs.length; i++){
            double[] res = Resonator4(input, freqs[i], durs[i], dur);

            for(int n = 0; n < res.length && n < composite.length; n++){
                if(res[n] > maxR){
                    maxR = res[n];
                    //System.out.println("MAX: " + maxR);
                }

                double v = 1;
                if(vols != null)
                    v = vols[i].getValue(n / (double) WaveWriter.SAMPLE_RATE);
                composite[n] += v * res[n] / (double)freqs.length;;
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

   public void writeNote1(float[][] frames, double time, double freq, double vol, double[] pan){
    double[] mode = {1,1.5,2,3,3.5,5};// 0, 1, 3, 5, 6, 8, 10, 11, 13 };
    //freq /= 4.0;
    int tet = 15;
       double[] freqs = new double[mode.length];
    
       double[] vols = new double[mode.length];
       double[] durs = new double[mode.length];
       double dur = 3;

       for (int p = 0; p < freqs.length && p * freq < WaveWriter.SAMPLE_RATE / 2; p++) {
           freqs[p] = freq * mode[p];
           vols[p] = Math.pow(10, -p);
           durs[p] = dur * Math.pow(1.1, -p);// * (0.5 + 0.5 * Math.random());
       }

       double[] sound = new double[1];
       sound[0] = 1;
       sound = ResBank2(sound, freqs, vols, durs, dur);
       sound[0] = 0;
       sound = Arrays.copyOf(sound, (int) (WaveWriter.SAMPLE_RATE * dur));
       double max = Double.MIN_VALUE;

       for (int i = 0; i < sound.length; i++) {
           max = Math.max(max, Math.abs(sound[i]));
           if (i >= sound.length - 100)
               sound[i] *= (sound.length - i) / 100.0;
       }
       int startFrame = (int) Math.rint(time * WaveWriter.SAMPLE_RATE);
       for (int i = 0; i < sound.length; i++) {
           for (int chan = 0; chan < pan.length; chan++)
               frames[chan][i + startFrame] += vol * pan[chan] * sound[i] / max;
       }
   }

    public static void main(String[] args){
        //stochasticSpectrum(440);
        //resTest();
        resTest1();
    }

    
/* 
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
    */
    

    public static void stochasticSpectrum(double freq){
        freq /= 4.0;
        double[] freqs = new double[30];
        double[] vols = new double[30];
        double[] durs = new double[30];
        double dur = 10;

        for(int p = 0; p < 30 && p * freq < WaveWriter.SAMPLE_RATE / 2; p++){
            freqs[p] = freq * (p+1);
            vols[p] = Math.pow(2, -p) * Math.random(); 
            durs[p] = dur * Math.pow(10, -p) * (0.5 + 0.5 * Math.random()); 
        }

        double[] sound = new double[1];
        sound[0] = 1;
        sound = ResBank2(sound, freqs, vols, durs, dur);
        sound[0] = 0;
        sound = Arrays.copyOf(sound, (int)(WaveWriter.SAMPLE_RATE * dur));
        System.out.println(sound.length / (double) WaveWriter.SAMPLE_RATE);

        WaveWriter ww = new WaveWriter("cb2");
        double max = Double.MIN_VALUE;

        for(int i = 0; i < sound.length; i++){
            max = Math.max(max,Math.abs(sound[i]));
            if(i >= sound.length - 100)
            sound[i] *= (sound.length - i) / 100.0;
        }
        for(int i = 0; i < sound.length; i++){
            ww.df[0][i] += sound[i] / max;
        }
        //System.out.println(min + " " + max);

        ww.render();
    }

    public void childWriteNote(float[][] frames, double time, double freq, double vol, double[] pan){
        
        //freq /= 4.0;
        int tet = 15;
           double[] freqs = new double[]{freq, freq * 3, freq * 5};
        
           Signal[] vols = new Signal[freqs.length];
           Signal[] durs = new Signal[freqs.length];
           double dur = 3;
    
           for (int p = 0; p < freqs.length; p++) {
            final double level = Math.pow(2,-p);
            Signal gainSig = new Signal() {
                public double getValue(double t){
                    return level;// * Math.pow(1.6,-t);
                }
            };
               vols[p] = gainSig;
               Signal durSig = new Signal() {
                public double getValue(double t){
                    return 0.01 + Math.min(0.1 * t / 1.0,0.1);
                }
            };
               durs[p] =  durSig;
           }
    
           double[] sound = new double[(int)(48000 * dur)];
           for (int i = 0; i < (int)(48000 * dur); i++){
               sound[i] = (Math.random() * 2 - 1);
               if(i < 100)
                   sound[i] *=  i / 100.0;
               sound[i] *= Math.pow(2, -i / 4800.0);
               // sound[i] *=(48000 * 3 - i) / (48000 * 3);
           }
           sound = ResBank2(sound, freqs, vols, durs, dur);
           sound[0] = 0;
           sound = Arrays.copyOf(sound, (int) (WaveWriter.SAMPLE_RATE * dur));
           double max = Double.MIN_VALUE;
    
           for (int i = 0; i < sound.length; i++) {
               max = Math.max(max, Math.abs(sound[i]));
               if (i >= sound.length - 100)
                   sound[i] *= (sound.length - i) / 100.0;
           }
           int startFrame = (int) Math.rint(time * WaveWriter.SAMPLE_RATE);
           for (int i = 0; i < sound.length; i++) {
               for (int chan = 0; chan < pan.length; chan++)
                   frames[chan][i + startFrame] += vol * pan[chan] * sound[i] / max;
           }
       }

       public static void resTest() {
           double f = 880;
           double[] sound = new double[48000 * 10];
           for (int i = 0; i < 48000 * 10; i++) {
               sound[i] = (Math.random() * 2 - 1);
               if (i < 100)
                   sound[i] *= i / 100.0;
               // sound[i] *= Math.pow(2, - i / 4800.0);
               // sound[i] *=(48000 * 3 - i) / (48000 * 3);
           }

           int[] mode = { 0, 1, 3, 5, 6, 8, 10, 11, 13 };// 0, 5, 9, 12};//

           Signal durSig = new Signal() {
               public double getValue(double t) {
                   return 0.01 + Math.min(0.1 * t / 10.0, 0.1);
               }
           };

           Signal[] gains = new Signal[mode.length];
           Signal[] durs = new Signal[mode.length];
           double[] freqs = new double[mode.length];
           for (int i = 0; i < mode.length; i++) {
               final double level = Math.pow(1.1, -i);
               Signal gainSig = new Signal() {
                   public double getValue(double t) {
                       return level * Math.pow(1.2, -t);
                   }
               };
               gains[i] = gainSig;
               freqs[i] = f * Math.pow(2, mode[i] / 15.0);
               durs[i] = durSig;
           }

           for (int i = 0; i < 5; i++)
               sound = ResBank2(sound, freqs, gains,
                       durs, 10);
           WaveWriter ww = new WaveWriter("rt");
           double max = Double.MIN_VALUE;

           for (int i = 0; i < sound.length; i++) {
               max = Math.max(max, Math.abs(sound[i]));
               if (i >= sound.length - 100)
                   sound[i] *= (sound.length - i) / 100.0;
           }
           for (int i = 0; i < sound.length; i++) {
               ww.df[0][i] += sound[i] / max;
           }
           // System.out.println(min + " " + max);

           ww.render();
    }

    public static void resTest1() {
        double f = 440;
        double[] sound = new double[48000 * 10];
        for (int i = 0; i < 48000 * 10; i++) {
            sound[i] = (Math.random() * 2 - 1);
            if (i < 100)
                sound[i] *= i / 100.0;
            // sound[i] *= Math.pow(2, - i / 4800.0);
            // sound[i] *=(48000 * 3 - i) / (48000 * 3);
        }

        int[] mode = { 0, 1, 3, 5, 6, 8, 10, 11, 13 };// 0, 5, 9, 12};//

        Signal durSig = new Signal() {
            public double getValue(double t) {
                return 1 * t / 10.0;
            }
        };

        double[] freqs = {86,170,209,263,350,522,716,833,994,1183,1421,1668,1932,2284,4180,6083};//new double[mode.length];
        Signal[] gains = new Signal[freqs.length];
        Signal[] durs = new Signal[freqs.length];
        for (int i = 0; i < freqs.length; i++) {
            final double level = Math.pow(1.3, -i);
            final int part = i;
            Signal gainSig = new Signal() {
                public double getValue(double t) {
                    double env = (10-t) / 10.0;
                    env = Math.pow((1 + part / (double)freqs.length),-t);
                    return level * (0.9 * env + 0.1);
                }
            };
            gains[i] = gainSig;
            freqs[i] =   freqs[i];//Math.pow(5/4.0,mode[i]);//(1+mode[i]);//
            durs[i] = durSig;
        }

        for (int i = 0; i < 5; i++)
            sound = ResBank2(sound, freqs, gains,
                    durs, 10);
        WaveWriter ww = new WaveWriter("rt");
        double max = Double.MIN_VALUE;

        for (int i = 0; i < sound.length; i++) {
            max = Math.max(max, Math.abs(sound[i]));
            if (i >= sound.length - 100)
                sound[i] *= (sound.length - i) / 100.0;
        }
        for (int i = 0; i < sound.length; i++) {
            ww.df[0][i] += sound[i] / max;
        }
        // System.out.println(min + " " + max);

        ww.render(1);
 }

    
}
