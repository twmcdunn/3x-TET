public class Additive extends Synth{
    public Additive(){
        spatializer = new Spatializer(Math.PI * 2 / (20 + 20 * rand.nextDouble()), Math.PI * 2 * rand.nextDouble());
    }

    public void childWriteNote(float[][] frames, double time, double freq, double vol, double[] pan){
        double dur = 30;
        double[] freqs = {86,170,209,263,350,522,716,833,994,1183,1421,1668,1932,2284,4180,6083};
        for(int i = 0; i < freqs.length; i++){
            int startFrame = (int)((0.5 * dur * i / (double)freqs.length) * WaveWriter.SAMPLE_RATE);
            int endframe = (int)((dur - (0.5 * dur * i / (double)freqs.length)) * WaveWriter.SAMPLE_RATE);
            for(int f = startFrame; f < endframe; f++){
                double t = f / (double)WaveWriter.SAMPLE_RATE;
                t = Math.abs(dur*0.5 - t) / ((endframe - startFrame) * 0.5 / (double)WaveWriter.SAMPLE_RATE);
                double env = Math.pow(10,-t * 4);
                //System.out.println(env);
                double frame = 0.1 * env * Math.sin(freqs[i] * 2 * Math.PI * f / (double)WaveWriter.SAMPLE_RATE);
                if(f - startFrame < 100)
                    frame *= (f - startFrame) / 100.0;
                if(endframe - f < 100)
                    frame *= (endframe - f) / 100.0;
                for(int n = 0; n < pan.length; n++)
                    frames[n][f] += pan[n] * frame;
            }
        }
    }

    public void test(){
        WaveWriter ww = new WaveWriter("additivve");
        writeNote(ww.df, 0, 0, 0, new double[]{1});

        ww.render(1);
    }


    public static void main(String[] args){

        new Additive().test();
    }
}