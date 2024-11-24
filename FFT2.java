
import java.util.Arrays;
import org.apache.commons.math3.transform.*;
import org.apache.commons.math3.complex.Complex;

/**
 * 
 * The math3 dependency can be found at
 * https://repo.maven.apache.org/maven2/org/apache/commons/commons-math3/3.6.1/
 * 
 * Basic explainations of convolution can be found here:
 * https://cmtext.indiana.edu/synthesis/chapter4_convolution.php
 * 
 * Write a description of class FFT2 here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class FFT2 {
    // returns [amp, phase]
    // freq is implied by index
    // freq = sample_rate * (index + 1) / length
    public static double[][] forwardTransform(double[] data) {
        int po2 = 0;
        while (Math.pow(2, po2) < data.length) {
            po2++;
        }
        data = Arrays.copyOf(data, (int) Math.pow(2, po2));
        double[][] tempConversion = new double[2][data.length];
        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        try {
            Complex[] complx = transformer.transform(data, TransformType.FORWARD);

            for (int i = 0; i < complx.length; i++) {
                double rr = (complx[i].getReal());
                double ri = (complx[i].getImaginary());

                tempConversion[0][i] = Math.sqrt((rr * rr) + (ri * ri));// amp
                tempConversion[1][i] = Math.atan2(ri, rr);// phase
            }

        } catch (IllegalArgumentException e) {
            System.out.println(e);
        }
        return tempConversion;
    }

    public static Complex[] forwardTransformComplex(double[] data) {
        int po2 = 0;
        while (Math.pow(2, po2) < data.length) {
            po2++;
        }
        data = Arrays.copyOf(data, (int) Math.pow(2, po2));
        double[][] tempConversion = new double[2][data.length];
        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] complx = transformer.transform(data, TransformType.FORWARD);

        return complx;
    }

    public static Complex[] inverseTransform(Complex[] data) {
        int po2 = 0;
        while (Math.pow(2, po2) < data.length) {
            po2++;
        }
        data = Arrays.copyOf(data, (int) Math.pow(2, po2));
        double[][] tempConversion = new double[2][data.length];
        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] complx = transformer.transform(data, TransformType.INVERSE);
        /*
         * try {
         * 
         * for (int i = 0; i < complx.length; i++) {
         * double rr = (complx[i].getReal());
         * double ri = (complx[i].getImaginary());
         * 
         * tempConversion[0][i] = Math.sqrt((rr * rr) + (ri * ri));//amp
         * tempConversion[1][i] = Math.atan2(ri, rr);//phase
         * }
         * 
         * } catch (IllegalArgumentException e) {
         * System.out.println(e);
         * }
         */
        return complx;
    }

    public static double getPitch(double[] sig, int sampleRate) {
        double f = 0;
        double[][] realFreqDom = forwardTransform(sig);
        double maxAmp = 0;
        for (int i = 0; i < realFreqDom[0].length; i++) {
            if (realFreqDom[0][i] > maxAmp) {
                maxAmp = realFreqDom[0][i];
                f = (i + 1) / (double) (realFreqDom[0].length);
            }
        }
        return f * sampleRate;
    }

    // matches the second to the first
    public static double[] matchPitch(double[] refSig, double[] sig) {
        double f1 = 0;
        double f2 = 0;
        double[][] realFreqDom1 = forwardTransform(refSig);
        double[][] realFreqDom2 = forwardTransform(sig);
        double maxAmp = 0;
        for (int i = 0; i < realFreqDom1[0].length; i++) {
            if (realFreqDom1[0][i] > maxAmp) {
                maxAmp = realFreqDom1[0][i];
                f1 = (i + 1) / (double) (realFreqDom1[0].length);
            }
        }

        maxAmp = 0;
        for (int i = 0; i < realFreqDom2[0].length; i++) {
            if (realFreqDom2[0][i] > maxAmp) {
                maxAmp = realFreqDom2[0][i];
                f2 = (i + 1) / (double) (realFreqDom2[0].length);
            }
        }

        double[] processed = new double[(int) (sig.length * f2 / f1)];

        for (int i = 0; i < processed.length; i++) {
            double exInd = i * f1 / f2;
            int index = (int) exInd;
            double fract = exInd - index;
            double frame1 = sig[index];
            double frame2 = frame1;
            if (index + 1 < sig.length)
                frame2 = sig[index + 1];
            processed[i] = frame1 * (1 - fract) + frame2 * fract;
        }

        return processed;
    }

    public static double[] convAsImaginaryProduct(double[] sig1, double[] sig2) {
        Complex[] freqDom1 = forwardTransformComplex(sig1);
        Complex[] freqDom2 = forwardTransformComplex(sig2);
        Complex[] freqProd = new Complex[freqDom1.length];
        for (int i = 0; i < freqDom1.length; i++) {
            double r1 = freqDom1[i].getReal();
            double r2 = freqDom2[i].getReal();
            double i1 = freqDom1[i].getImaginary();
            double i2 = freqDom2[i].getImaginary();
            double rProd = r1 * r2 - i1 * i2;
            double iProd = r1 * i2 + r2 * i1;
            freqProd[i] = new Complex(rProd, iProd);
        }
        Complex[] timeDomain = inverseTransform(freqProd);
        double[] convSig = new double[timeDomain.length];
        for (int i = 0; i < sig1.length; i++) {
            convSig[i] = timeDomain[i].getReal();
        }
        return convSig;
    }

    public static void convTest() {
        double[] sig1 = ReadSound.readSoundDoubles("21.wav");// "cathedral.wav");
        //sig1 = Arrays.copyOf(sig1, 48000 * 10);
        /*
         * float[][] frames = new float[1][WaveWriter.SAMPLE_RATE * 30];
         * Synth synth = new SampleSynth(5);
         * synth.writeNote(frames, 0, 220, 0.1, new double[] { 1 });
         * sig1 = new double[WaveWriter.SAMPLE_RATE * 20];
         * for (int i = 0; i < sig1.length; i++)
         * sig1[i] += frames[0][i];
         */
/* 
        double[] sig2 = new double[WaveWriter.SAMPLE_RATE * 30];
        for (int i = 0; i < sig2.length; i++) {
            double env = 2 * Math.abs(0.5 - i / (double) sig2.length);
            env = Math.pow(10, -env * 5);
            sig2[i] = env * (Math.random() * 2 - 1);
        }
        */

        double[] sig2 = ReadSound.readSoundDoubles("20.wav");// "test.wav");

        boolean pitchMatch = true;
        if (pitchMatch)
            sig2 = matchPitch(sig1, sig2);
        if (sig1.length < sig2.length) {
            double[] sig3 = sig1;
            sig1 = sig2;
            sig2 = sig3;
        }
        sig2 = Arrays.copyOf(sig2, sig1.length);

        double[] convSig = convAsImaginaryProduct(sig1, sig2);// convolutedTDSig
        /*
         * for(int i = 0; i < convSig.length; i++){
         * if(convSig[i] != 0)
         * System.out.println("NONZEO:" + convSig[i]);
         * }
         */

        WaveWriter ww = new WaveWriter("convolution");
double max = 0;
        for(int i = 0; i < convSig.length; i++)
            max = Math.max(Math.abs(convSig[i]),max);

        for (int i = 0; i < convSig.length; i++) {
            ww.df[0][i] += convSig[i] / max;
        }
        ww.render(1);
    }

    public static void main(String[] args) {
        convTest();
    }

    public static void dynamicConvTest() {
        double[] sig1 = ReadSound.readSoundDoubles("15.wav");

        double[] sig2 = ReadSound.readSoundDoubles("cathedral.wav");
        boolean pitchMatch = false;
        if (pitchMatch)
            sig2 = matchPitch(sig1, sig2);
        if (sig1.length > sig2.length) {
            double[] sig3 = sig1;
            sig1 = sig2;
            sig2 = sig3;
        }
        sig2 = Arrays.copyOf(sig2, sig1.length);

        int windSize = (int) Math.pow(2, 11);
        double[] tone = new double[sig1.length];
        for (int i = 0; i < sig1.length - windSize; i += windSize / 2) {
            double[] samp1 = Arrays.copyOfRange(sig1, i, i + windSize);
            double[] samp2 = Arrays.copyOfRange(sig2, i, i + windSize);
            // double[] conv = convolutedTDSig(samp1, samp2);
            for (int n = 0; n < windSize; n++) {
                double windowFunction = (2 - (Math.cos(Math.PI * 2 * n / (double) (windSize - 1)) + 1)) / 2.0;

                windowFunction = Math.pow(Math.cos(Math.PI * (n - windSize / 2) / (double) windSize), 2)
                        / (double) windSize;

                samp1[n] *= windowFunction;
                samp2[n] *= windowFunction;

                /*
                 * if(n < samp1.length/2){
                 * samp1[n] *= n / (windSize/2);
                 * samp2[n] *= n / (windSize/2);
                 * }
                 * else{
                 * samp1[n] *= (windSize - n) / (windSize/2);//nb windsize is even
                 * samp2[n] *= (windSize - n) / (windSize/2);
                 * }
                 */
            }
            double[] conv = null;// convolutedTDSig(samp1, samp2);
            // if(false)
            for (int n = 0; n < windSize; n++) {
                double windowFunction = Math.pow(Math.cos(Math.PI * (n - windSize / 2) / (double) windSize), 2)
                        / (double) windSize;

                // conv[n] *= windowFunction;

                if (n < WaveWriter.SAMPLE_RATE / 20)
                    conv[n] *= n / (double) (WaveWriter.SAMPLE_RATE / 20);
                if (n > windSize - WaveWriter.SAMPLE_RATE / 20)
                    conv[n] *= (windSize - n) / (double) (WaveWriter.SAMPLE_RATE / 20);

            }
            // System.out.println("START: " + i + " END: " + (i+conv.length-1));
            for (int n = 0; n < conv.length; n++) {
                tone[i + n] += conv[n];
            }
            // break;
        }
        WaveWriter ww = new WaveWriter("convolution");

        for (int i = 0; i < tone.length; i++) {
            ww.df[0][i] += tone[i];
            ww.df[1][i] += tone[i];
        }
        ww.render();

    }

    // Proof the Inverse FFT works!
    public static void test() {
        double[] sig = new double[WaveWriter.SAMPLE_RATE];

        for (int i = 0; i < sig.length; i++) {
            sig[i] = Math.sin(Math.PI * 2 * 440 * i / (double) (WaveWriter.SAMPLE_RATE));
        }
        // Complex[] freqDomain = forwardTransformComplex(sig);
        double[][] realFreqDomain = forwardTransform(sig);

        Complex[] freqDomain = new Complex[realFreqDomain[0].length];

        for (int i = 0; i < realFreqDomain[0].length; i++) {
            double theta = realFreqDomain[1][i];
            double hypot = realFreqDomain[0][i];
            freqDomain[i] = new Complex(hypot * Math.cos(theta), hypot * Math.sin(theta));
        }

        Complex[] timeDomain = inverseTransform(freqDomain);

        double[] sig1 = new double[timeDomain.length];
        for (int i = 0; i < sig1.length; i++) {
            sig1[i] = timeDomain[i].getReal();
        }

        WaveWriter ww = new WaveWriter("fft2");

        for (int i = 0; i < sig.length; i++) {
            ww.df[0][i] += sig[i];
            ww.df[1][i] += sig[i];
        }
        ww.render();
    }

    /*
     * public static void randomizeSpect(){
     * double[] sig = ReadSound.readSoundDoubles("bell.wav");
     * double[][] fft = forwardTransform(sig);
     * for(int i = 0; i < fft[0].length; i++){
     * fft[0][i] = fft[0][i] * 0.5 + Math.random();
     * }
     * 
     * double[] out = inverseTransformm;
     * 
     * WaveWriter ww = new WaveWriter("rand");
     * 
     * for(int i = 0; i < out.length; i++){
     * ww.df[0][i] += out[i];
     * ww.df[1][i] += out[i];
     * }
     * ww.render();
     * }
     */

}
