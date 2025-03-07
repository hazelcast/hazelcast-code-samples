package hazelcast.platform.labs.machineshop;

import java.util.Random;

/**
 * Generates a signal according to
 *
 * B + tM + E(S) where B and M are constants and E(S) is a normally distributed, 0 centered noise term with
 * std. dev S
 *
 * The value will be calculated as a float and rounded to an integer.
 *
 * Any positive value of M (slope) will cause the value to rise over time and any negative value will cause
 * it to fall.
 *
 * Note that this class has configuration, but no dynamic state.  It can be re-used in multiple emulators.
 */
public class SignalGenerator {
    public SignalGenerator(float bias, float slope, float noiseSD) {
        this.bias = bias;
        this.slope = slope;
        this.noiseSD = noiseSD;
        this.rand = new Random();
    }

    private final float bias;
    private final float slope;
    private final float noiseSD;
    private final Random rand;

    public short compute(int t){
        float noise = (float) rand.nextGaussian() * noiseSD;
        float result = bias + (float) t * slope + noise;
        return (short) result;
    }

}
