package magic;
import java.util.Random;
import java.util.Vector;

public class MatrixPic {
	
	private final class Coord {
		int x;
		int y;

		public Coord(int a, int b) {
			x = a;
			y = b;
		}

		@Override
		public boolean equals(Object o) {
			Coord diff = (Coord) o;
			return diff.x == this.x && diff.y == this.y;
		}

		@Override
		public String toString() {
			return "[".concat(Integer.toString(x)).concat("=")
					.concat(Integer.toString(y)).concat("]");
		}
	}

	private int N = 500;
	private int M = N;
	private int colorsLimit = 1;

	private Double[][] data = new Double[N][M];
	Vector<Coord> siblings = new Vector<Coord>();
	private boolean[][] visitedPixels = new boolean[N][M];

	public MatrixPic(int n, int m, int c){
		N = n;
		M = m;
		colorsLimit = c;
	}
	
	public MatrixPic(int n, int m, int c, double d){
		N = n;
		M = m;
		colorsLimit = c;
		fillWith(d);
	}
	
	public void setPredefinedPic() {
		Double[][] pic = { { 0.0, 0.0, 0.0, 0.0, 0.0 }, { 1.0, 0.0, 1.0, 1.0, 1.0 },
				{ 1.0, 1.0, 0.0, 0.0, 0.0}, { 0.0, 0.0, 0.0, 0.0, 1.0 }, { 1.0, 0.0, 0.0, 1.0, 1.0 } };
		data = pic;
	}

	public void setRandomPic() {
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < M; j++) {
				data[i][j] = nextRandomPixel();
			}
		}
	}
	
	public void clear(){
		fillWith(0);
	}

	public Vector<Coord> setSelectedPixelsList() {
		return siblings;
	}
	
	public void setZeroPic(){
		fillWith(0);
	}

	public  void selectAreaAroundPixel(int x, int y) {
		// check availability
		if (x < 0 || x > N - 1 || y < 0 || y > M - 1) {
			throw new IllegalArgumentException("Pixel outside picture");
		}

		// forgot previous visits
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < M; j++) {
				visitedPixels[i][j] = false;
			}
		}

		// compute
		search(x, y);
	}

	public  void weightedBlur() {
		weightedBlur(1);
	}

	private  Double nextRandomPixel() {
		Random r = new Random();
		return (double)r.nextInt(colorsLimit); // 0<=random<=colorsLimit
	}

	private  void search(int x, int y) {
		visitedPixels[x][y] = true;
		siblings.add(new Coord(x, y));

		double myColor = data[x][y];
//		Thread t1 = new Thread(new ParallelComputer(x, y, 2));
//		Thread t2 = new Thread(new ParallelComputer(x, y, 0));
//		Thread t3 = new Thread(new ParallelComputer(x, y, 3));
//		Thread t4 = new Thread(new ParallelComputer(x, y, 1));

		if (x - 1 >= 0 && x - 1 < N)
			if (!visitedPixels[x - 1][y] && data[x - 1][y] == myColor) {
				search(x-1,y);
			}
		if (x + 1 >= 0 && x + 1 < N)
			if (!visitedPixels[x + 1][y] && data[x + 1][y] == myColor) {
				search(x+1,y);
			}
		if (y - 1 >= 0 && y - 1 < N)
			if (!visitedPixels[x][y - 1] && data[x][y - 1] == myColor) {
				search(x,y-1);
			}
		if (y + 1 >= 0 && y + 1 < N)
			if (!visitedPixels[x][y + 1] && data[x][y + 1] == myColor) {
				search(x,y+1);
			}
		//
		//
		// if (x - 1 >= 0 && x - 1 < N)
		// if (!visitedPixels[x - 1][y] && matrixPic[x - 1][y] == myColor) {
		// search(x - 1, y);
		// }
		// if (x + 1 >= 0 && x + 1 < N)
		// if (!visitedPixels[x + 1][y] && matrixPic[x + 1][y] == myColor) {
		// search(x + 1, y);
		// }
		// if (y - 1 >= 0 && y - 1 < N)
		// if (!visitedPixels[x][y - 1] && matrixPic[x][y - 1] == myColor) {
		// search(x, y - 1);
		// }
		// if (y + 1 >= 0 && y + 1 < N)
		// if (!visitedPixels[x][y + 1] && matrixPic[x][y + 1] == myColor) {
		// search(x, y + 1);
		// }
	}

	private  void weightedBlur(int nextTo) {
		double S = 0;
		double weight = 0;
		Double[][] newPic = new Double[N][M];
		for (int x = 0; x < N; x++) {
			for (int y = 0; y < M; y++) {
				S += data[x][y];
				weight += 1;
				for (int i = 1; i <= nextTo; i++) {
					if (x + i < N) {
						S += (double) data[x + i][y] / (double) (i + 2);
						weight += 1 / (double) (i + 2);
					}
					if (x - i >= 0) {
						S += (double) data[x - i][y] / (double) (i + 2);
						weight += 1 / (double) (i + 2);
					}
					if (y + i < M) {
						S += (double) data[x][y + i] / (double) (i + 2);
						weight += 1 / (double) (i + 2);
					}
					if (y - i >= 0) {
						S += (double) data[x][y - i] / (double) (i + 2);
						weight += 1 / (double) (i + 2);
					}
				}
				newPic[x][y] = (S / weight);
				S = 0;
				weight = 0;
			}
		}
		data = newPic;
	}

	 void printCompare() {
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < M; j++) {
				System.out.print(data[i][j]);
			}
			System.out.print("     ");
			for (int j = 0; j < M; j++) {
				if (siblings.get(0).equals(new Coord(i, j))) {
					System.out.print("•");
				} else if (siblings.contains(new Coord(i, j))) {
					System.out.print("*");
				} else
					System.out.print(data[i][j]);
			}
			System.out.println();
		}
	}

	 void printCompareInt() {
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < M; j++) {
				System.out.print(data[i][j].intValue());
			}
			System.out.print("     ");
			for (int j = 0; j < M; j++) {
				if (siblings.get(0).equals(new Coord(i, j))) {
					System.out.print("•");
				} else if (siblings.contains(new Coord(i, j))) {
					System.out.print("*");
				} else
					System.out.print(data[i][j].intValue());
			}
			System.out.println();
		}
	}

	 void printMatrixPic(double[][] m) {
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < M; j++) {
				System.out.print(m[i][j]);
				System.out.print(" ");
			}
			System.out.println();
		}
	}

	 void printMatrixPicBoth(double[][] m) {
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < M; j++) {
				System.out.print((int) m[i][j]);
				System.out.print(" ");
			}
			System.out.print("      ");
			for (int j = 0; j < M; j++) {
				System.out.print(m[i][j]);
				System.out.print(" ");
			}
			System.out.println();
		}
	}

	 void printMatrixPicInt(double[][] m) {
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < M; j++) {
				System.out.print((int) m[i][j]);
				System.out.print(" ");
			}
			System.out.println();
		}
	}
	 
	 public void fillWith(double d){
		 for(int i = 0; i<N; i++){
				for(int j = 0; j<M; j++){
					data[i][j]=d;
				}
			}
	 }

	 void printSelectedPixelsList() {
		System.out.print("(");
		boolean first = true;
		for (Coord c : siblings) {
			if (!first) {
				System.out.print(", ");
			}
			first = false;
			System.out.print(c);

		}
		System.out.println(")");
	}
	 
	 @Override
	 public String toString(){
		 StringBuilder sb = new StringBuilder();
		 for (int i = 0; i < N; i++) {
				for (int j = 0; j < M; j++) {
					sb.append(data[i][j].intValue());
					sb.append(" ");
				}
				sb.append("\n");
			}		 
		 return sb.toString();
	 }
}