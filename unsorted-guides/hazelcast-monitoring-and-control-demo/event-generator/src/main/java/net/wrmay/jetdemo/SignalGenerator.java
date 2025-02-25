package net.wrmay.jetdemo;

import java.util.Random;

/**
 * Generates a signal according to
 *
 * B + tM + E(S) where B and M are constants and E(S) is a normally distributed, 0 centered noise term with
 * std. dev S
 *
 * The value will be calculated as a float and rounded to an integer.
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

    public int compute(int t){
        float noise = (float) rand.nextGaussian() * noiseSD;
        float result = bias + (float) t * slope + noise;
        return (int) result;
    }

}
