package audio;

import java.util.Arrays;

public class Comparator {

	public static class MVector {
		public static MVector diff(final MVector v1, final MVector v2) {
			MVector v3 = new MVector(v1.size());
			if (v1.vector.length != v2.vector.length) {
				throw new RuntimeException("Wrong dimensoins");
			} else {
				for (int i = 0; i < v3.size(); i++) {
					v3.vector[i] = v1.vector[i]-v2.vector[i];
				}
				return v3;
			}
		}

		public static double similarity(final MVector v1, final MVector v2,
				double threshold) {
			threshold = threshold > 1 ? threshold : 1;
			MVector v3 = diff(v1, v2);
			System.out.println(v3);
			System.out.println(v3.length() + " " + threshold);
			if (v3.length() > threshold) {
				return -1;
			} else if (v3.length() >= 0) {
				return 1 - v3.length();
			} else
				throw new RuntimeException(
						"Somthing is really going wrong in similarity computing!!! RES = "
								+ v3.length() + ", thr = " + threshold);
		}

		private double[] vector;

		public MVector(double[] a) {
			vector = a;
		}

		public MVector(int size) {
			vector = new double[size];
		}

		public MVector(int[] a) {
			vector = new double[a.length];
			for (int i = 0; i < vector.length; i++) {
				vector[i] = a[i];
			}
		}

		public MVector add(final MVector v) {
			if (v.vector.length != this.vector.length) {
				throw new RuntimeException("Wrong dimensoins");
			} else {
				for (int i = 0; i < vector.length; i++) {
					vector[i] += v.vector[i];
				}
				return this;
			}
		}

		public double length() {
			double sum = 0;
			for (int i = 0; i < vector.length; i++) {
				sum += vector[i] * vector[i];
			}
			return Math.sqrt(sum);
		}

		public int size() {
			return vector.length;
		}

		@Override
		public String toString() {
			return Arrays.toString(vector);
		}
	}

	public static boolean isSimilar(double[] d1, double[] d2) {
		MVector v1 = new MVector(d1);
		MVector v2 = new MVector(d2);
		double len = (v1.length()+v2.length())/2;
		return MVector.similarity(v1,v2, len/6) > -1;
	}

	public static boolean isSimilar(int[] d1, int[] d2) {
		MVector v1 = new MVector(d1);
		MVector v2 = new MVector(d2);
		double len = (v1.length()+v2.length())/2;
		return MVector.similarity(v1,v2, len/6) > -1;
	}

	public static void main(String[] args) {
//		double[] d1 = { 1, 2, 3, 4, 5, };
//		double[] d2 = { 1, 2, 3, 3.1, 5 };
//		MVector v1 = new MVector(d1);
//		MVector v2 = new MVector(d2);
//		System.out.print(v1 + " ");
//		System.out.println(v1.length());
//		System.out.print(v2 + " ");
//		System.out.println(v2.length());
//		System.out.println(isSimilar(d1, d2));
		System.out.println(70<<8);
	}
}
