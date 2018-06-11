package mjr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class Approximator extends Polynom {
    private int n;
    private int degree;
    private int maxDegree;
    private double[] x;
    private double[] F;
    private double[][] matrix;
    private double[] vector;
    private Solver slv;

    Approximator() {
    }

    Approximator(double[] x, double[] F, int m) {
        this.n = x.length;
        this.maxDegree = n - 1;
        this.degree = m;
        this.x = x;
        this.F = F;
    }

    public void readFromFile(File file) throws FileNotFoundException {
        FileReader reader = new FileReader(file);
        Scanner scanner = new Scanner(reader);

        this.n = Integer.parseInt(scanner.nextLine());
        this.maxDegree = this.n - 1;
        this.x = new double[this.n];
        this.F = new double[this.n];

        for (int i = 0; i < this.n; i++) {
            this.x[i] = Double.parseDouble(scanner.next());
        }

        for (int i = 0; i < this.n; i++) {
            this.F[i] = Double.parseDouble(scanner.next());
        }

        scanner.close();
    }

    public void createPolynom(int m) {
        this.degree = m;
        this.matrix = new double[m][m];
        this.vector = new double[m];

        for (int i = 0; i < m; i++) {
            for (int k = 0; k < this.n; k++) {
                for (int j = 0; j < m; j++) {
                    matrix[i][j] += Math.pow(x[k], i + j);
                }
                vector[i] += Math.pow(x[k], i) * F[k];
            }
        }

        this.slv = new Solver(matrix);
        this.coefficients = this.slv.getSolve(vector);
    }

    public void setDegree(int degree) {
        this.degree = degree;
    }

    public int getDegree() {
        return this.degree;
    }

    public int getMaxDegree() {
        return this.maxDegree;
    }

    public double[] getX() {
        return this.x;
    }

    public double[] getY() {
        return this.F;
    }

    public void setYByIndex(int index, double value) {
        this.F[index] = value;
        this.createPolynom(this.degree);
    }
}
