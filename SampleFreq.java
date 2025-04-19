public class SampleFreq implements Comparable<SampleFreq>{
    double[] dry, wet;
    double freq;
    public SampleFreq(double[] sample, double freq){
        
        dry = sample;
        this.freq = freq;
        

        /*
        //create wet
        dry = Arrays.copyOf(dry, dry.length + cathedral.length);
        cathedral = Arrays.copyOf(cathedral, dry.length);
        double[] wetSig = FFT2.convAsImaginaryProduct(dry, cathedral);
        wetSig = Arrays.copyOf(wetSig, dry.length);
*/
        // normalize both wet and dry
        double sMax = 0;
        for (int i = 0; i < dry.length; i++) {
            sMax = Math.max(sMax, Math.abs(dry[i]));
        }
        for (int i = 0; i < dry.length; i++) {
            dry[i] /= sMax;
        }
    }

    public int compareTo(SampleFreq sf) {
        return (int) (10 * (sf.freq - freq));
    } 
}