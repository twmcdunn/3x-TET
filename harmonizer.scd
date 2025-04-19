(
//Buffer.alloc(s, 48000 * 5, bufnum: 1);

SynthDef.new(\harmonizer,{
	arg inBus = 5;
	var sig;
	sig = In.ar(inBus);
	sig = Mix.ar([PitchShift.ar(sig, pitchRatio: 2.pow(0 / 15.0)),
		PitchShift.ar(sig, pitchRatio: 2.pow((5 / 15.0) - 0)),
		PitchShift.ar(sig, pitchRatio: 2.pow((9 / 15.0) - 0)),
		PitchShift.ar(sig, pitchRatio: 2.pow((11 / 15.0) - 0))]
	);
	Out.ar(0,sig);
}).add;

SynthDef(\patch, {
	var bus = 5;
	Out.ar(0, In.ar(bus));
}).add;

SynthDef.new(\harmonizer1,{
	arg inBus = 5;
	var sig, chain;
	sig = In.ar(inBus);
	chain = FFT.new(LocalBuf(2048), sig);

	sig = IFFT.ar(chain * 3/4.0);//PitchShift.ar(sig, pitchRatio: 5 / 4.0) + PitchShift.ar(sig, pitchRatio: 3 / 2.0);
	Out.ar(0,sig);
}).add;
)

p = Synth.new(\patch);
h = Synth.new(\harmonizer);

(
m = {
	~b0 = Buffer.read(s, "/Users/maestro/Documents/Programs/Fixed Media/3x-tet/bestill.wav");
	PlayBuf.ar(1, ~b0, BufRateScale.kr(~b0));//193
}.play(s, outbus: 5);
)

LocalBuf(2048)

(
{
    var in, chain;
    in = WhiteNoise.ar(0.1);
    chain = FFT(LocalBuf(2048), in);
    IFFT(chain) // inverse FFT
}.play;
)



