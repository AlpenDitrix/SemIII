import java.util.Random;

public class MatrixMultiply {
	private static final int N = 3;

	private static final int[][] A = new int[N][N];
	private static final int[][] B = new int[N][N];
	private static final int[][] C = new int[N][N];


	public static void main(String[] args) {
		precompute();
		nonTransposedMultiply();
		printResult();
	}

	public static void printResult() {
		for (int i = 0; i < N; i++) {
			System.out.print("(");
			
			for (int j = 0; j < N; j++) {
				System.out.print(A[i][j]);
				if(j!=N-1) System.out.print(" ");
			}
			
			if (i != N / 2) {
				System.out.print(")   (");
			} else {
				System.out.print(") x (");
			}
			
			for (int j = 0; j < N; j++) {
				System.out.print(B[i][j]);
				if(j!=N-1) System.out.print(" ");
			}
			
			if(i!=N/2){
			System.out.print(")   (");}
			else{
				System.out.print(") = (");
			}
			
			for(int j = 0; j<N; j++){
				System.out.print(C[i][j]);
				if(j!=N-1) System.out.print(" ");
			}
			
			System.out.println(")");
		}
	}

	public static double[][] multiply(double[][] A, double[][] B) {
		if ((A.length == 0 || A.length != A[0].length)
				|| (B.length == 0 || B.length != B[0].length)
				|| (A.length != B.length))
			throw new IllegalArgumentException("Wrong dimensions");

		int n = A.length;

		double[][] result = new double[n][n];

		for (int i = 0; i < n; i++)
			for (int k = 0; k < n; k++)
				for (int j = 0; j < n; j++)
					result[i][j] += A[i][k] * B[k][j];

		return result;
	}

	private static void nonTransposedMultiply() {
		for (int i = 0; i < N; i++)
			for (int k = 0; k < N; k++)
				for (int j = 0; j < N; j++)
					C[i][j] += A[i][k] * B[k][j];
	}

	private static void precompute() {
		Random r = new Random();
		for (int i = 0; i < N; i++)
			for (int j = 0; j < N; j++)
				A[i][j] = B[i][j] = r.nextInt(10);
	}
}
