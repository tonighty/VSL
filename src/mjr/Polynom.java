package mjr;

public class Polynom {

    protected double[] coefficients;

    public double getValue(double x) {
        double value = 0;
        for (int i = 0; i < this.coefficients.length; i++) {
            value += Math.pow(x, i) * coefficients[i];
        }
        return value;
    }

    public double[] getCoefficients() {
        return this.coefficients;
    }
}
