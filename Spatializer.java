/*
 * assumes speakers are arranged clockwise with channel 0 at 12:15,
 * channel 7 at 11:45.
 * 
 * Files will be renamed after render for easy identification. (the numbers
 * do not go in order)
 * See diagram in this folder titled "__8_channel_speaker_setup.pdf"
 * channel 0 = 2. Front Right
 * channel 1 = 4. Wide Right
 * channel 2 = 6. Side Right
 * channel 3 = 8. Rear Right
 * channel 4 = 7. Rear Left
 * channel 5 = 5. Side Left
 * channel 6 = 3. Wide Left
 * channel 7 = 1. Front Left
 * 
 * For convenience, the whole arrangement is shifted 3 Pi / 8 radians, so that in code,
 * channel 0 is at theta = 0. In practice, channel 0 is actually at an angle of 3 Pi / 8
 * with respect to the listener.
 * 
 * The unit circle is used, with polar coordinates. In other words, in code, channel 0
 * is located at theta = 0, magnitude = 1
 * 
 * (negative magnitudes are acceptable)
 * 
 * We assume amplitude is inversely proportional to distance. The closer
 * a sound is to a speaker, the louder its signal from that speaker.
 * All pan arrays are normalized to that their values add up to 1.
 * 
 * Yes, the physics here are very approximate. The normalization also won't quite lead to 
 * equal loudness perception, since some sort of logrithmic transformation would be necessary
 * for this.
 */

public class Spatializer {
    public double thetaSlope, thetaConst, magFreq, magPh, magAmp, magnitute;
    public boolean magnitudeAutomated;    

    //magnitude automated as a sinusoidal function of time
    public Spatializer(double thetaSpeed, double thetaNaught, double magSpeed, double magPhase, double magAmplitude){
        magnitudeAutomated = true;
        thetaSlope = thetaSpeed;
        thetaConst = thetaNaught;
        magFreq = magSpeed;
        magPh = magPhase;
        magAmp = magAmplitude;
        if(Synth.rand.nextDouble() > 0.5)//randomize direction of rotation
            thetaSpeed *= -1;
    }

    //set magnitude should be invoded manually before every call to pan
    public Spatializer(double thetaSpeed, double thetaNaught){
        magnitudeAutomated = false;
        thetaSlope = thetaSpeed;
        thetaConst = thetaNaught;
        if(Synth.rand.nextDouble() > 0.5)//randomize direction of rotation
            thetaSpeed *= -1;
    }

    //magnitude can be set as 1 - amp so that loud sounds
    //are more centered where the listener is located
    public void setMagnitude(double m){
        magnitute = m;
    }

    public double[] pan(double time){
        double theta = time * thetaSlope + thetaConst;
        if(magnitudeAutomated)
            magnitute = Math.sin(time * magFreq * Math.PI * 2 + magPh) * magAmp;

        double[] pan = new double[8];
        double tot = 0;
        for(int chan = 0; chan < 8; chan++){
            double speakerTheta = chan * Math.PI / 4.0;
            double deltThet = theta - speakerTheta;
            double dist = Math.pow(
                Math.pow(magnitute * Math.sin(deltThet),2) +
                Math.pow(1 - magnitute * Math.cos(deltThet),2)
            ,0.5);
            double amp = 1 / (1 + dist);//amp is 1 when sound is located at speaker
            pan[chan] = amp;
            tot += amp;
        }
        for(int chan = 0; chan < 8; chan++){
            pan[chan] /= tot;
        }
        return pan;
    }
}