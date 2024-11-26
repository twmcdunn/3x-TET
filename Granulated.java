import java.util.Arrays;

public class Granulated extends SampleSynth {
    double f2;
    double[] sig, wetSig;
    int[] chord = new int[] { 0, 5, 9, 12 };

    public Granulated() {
        
        f2 = 626;
        sig = ReadSound.readSoundDoubles("3.wav");
    }

    public void setChord(int[] c){
        chord = c;
    }

    public void writeGrain(float[][] frames, double time, double freq, double vol, double[] pan){
        
        double[] shifted = pitchShift(sig, f2, freq);
        shifted = Arrays.copyOf(shifted, WaveWriter.SAMPLE_RATE / 10);
        for(int i = 0; i < shifted.length; i++){
            shifted[i] *= 1 - (i / (double)shifted.length);
        }

        mix = vol;
        shifted = addReverb(shifted);
        for(int i = 0; i < shifted.length; i++){
            shifted[i] *= vol;
        }


        int startFrame = (int) Math.rint(time * WaveWriter.SAMPLE_RATE);
        for (int i = 0; i < shifted.length; i++) {
            for (int n = 0; n < pan.length; n++) {
                double p = pan[n];
                frames[n][i + startFrame] += p * shifted[i];
            }
        }
    }

    public void writeNote(float[][] frames, double time, double target, double vol, double[] pan) {
        if(true)
            return;
        for (int n = 0; n < 500; n++) {
            double t = Math.random();
            t = 1 - (Math.log(t) + 10)/10.0;//(Math.pow(10, t) - 1) / 9.0;
            int note = chord[(int) (Math.random() * chord.length)];
int tet = 15;
            if(Piece.seq != null)
            tet = Piece.seq.TET;
            double freq = Piece.c0Freq * Math.pow(2,Piece.closestOct((int)target,note,tet) / (double)tet);
            writeGrain(frames, time- t * 0.5, freq, vol * (1-t), pan);
            writeGrain(frames, time + t * 10, freq, vol * (1-t), pan);
            //gong.writeNote(ww.df, 20 - t, freq, t / 10.0, new double[] { 1 });
        }
    }

   
    public static void main(String[] args) {
        Granulated g = new Granulated();
        WaveWriter ww = new WaveWriter("GRAN");
        g.writeNote(ww.df, 5, 15*5, 0.5, new double[]{1});
        ww.render(1);
    }


}
