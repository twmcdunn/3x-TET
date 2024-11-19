(
~b0 = Buffer.read(s, "/Users/peanutbutter/Documents/Programs/Music and sound projects/Fixed Media/15 TET/Git Clone/3x-tet/20.wav");
~freqs = [12/15.0,1,48/45.0,9/8.0,6/5.0,3/2.0];
SynthDef.new(\holdReverb, {
	arg inBus = 4, feedback = 1, decayTime = 1, gate = 1;
	var sig, local, env = 1, envs;
	sig =  PlayBuf.ar(1,~b0,BufRateScale.kr(~b0));
	//sig = sig * 0.1;

	local = (LocalIn.ar(1) + sig);
	20.do({local = AllpassN.ar(local, 0.06,
		Rand(0.001, 0.06), decayTime)});

	LocalOut.ar(local * feedback);

	sig = Mix.ar([sig,local]);// * envs[i];//.ar(1);

	//sig = BLowPass4.ar(sig,20000,12);

	Out.ar(0,  0.3 * sig.dup);


}).add;
)

a = Synth.new(\holdReverb);
