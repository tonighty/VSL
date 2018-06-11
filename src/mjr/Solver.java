package mjr;

public class Solver
{
    private double[][] matrix;
    private int n;

    Solver(double[][] matrix)
    {
        n = matrix.length;
        this.matrix = new double[n][n];
        double sum;

        for (int i = 0; i < n; i++)
        {
            for (int j = i; j < n; j++)
            {
                sum = 0;
                for (int k = 0; k < i; k++)
                    sum += this.matrix[i][k] * this.matrix[k][j];
                this.matrix[i][j] = matrix[i][j] - sum;
            }
            for (int j = i + 1; j < n; j++)
            {
                sum = 0;
                for (int k = 0; k < i; k++)
                    sum += this.matrix[j][k] * this.matrix[k][i];
                this.matrix[j][i] = (1 / this.matrix[i][i]) * (matrix[j][i] - sum);
            }
        }
    }
    public double[] getSolve(double[] F)
    {
        double sum;

        double[] y = new double[n];
        for (int i = 0; i < n; i++)
        {
            sum = 0;
            for (int k = 0; k < i; k++)
                sum += this.matrix[i][k] * y[k];
            y[i] = F[i] - sum;
        }

        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--)
        {
            sum = 0;
            for (int k = i + 1; k < n; k++)
                sum += this.matrix[i][k] * x[k];
            x[i] = (1 / this.matrix[i][i]) * (y[i] - sum);
        }

        return x;
    }
}
