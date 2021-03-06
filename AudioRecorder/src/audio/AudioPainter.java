package audio;

import java.awt.Canvas;
import java.awt.Color;
import java.io.IOException;

public class AudioPainter extends Painter {

	private static final class Detector {
		private static int decision;
		private static int prevWeiBeg;
		private static int curWeiBeg;
		private static int slowCounter = 0;

		public static void process() {
			decision = 0x00;
			for (int i = 0; i < mfcc.length; i++) {
				curWeiBeg += mfcc[i];
			}
			curWeiBeg /= mfcc.length;

			isChanging();

			prevWeiBeg = curWeiBeg;
			voiceDecision = decision != 0x00 ? decision : voiceDecision;
		}

		private static int computeDerivation(int bound1, int bound2) {
			int res = 0;
			int b1 = bound1 < 0 ? 0 : bound1;
			int b2 = bound2 > mfcc.length ? mfcc.length : bound2;
			for (int i = b1 + 1; i < b2; i++) {
				res += mfcc[i] - mfcc[i - 1];
			}
			return res;
		}

		private static void isChanging() {
			int diff = curWeiBeg - prevWeiBeg;
			int der = computeDerivation(0, mfcc.length / 2);
			System.out.println(diff + " " + der);

			if (der < -5) {
				// ���� ��� �����
				if (diff > 5) {// ������� ���� �.�.
					// ����� �������� ������ ���
					if (voiceDecision == FAST_MATCH) {
						// ����������-������ ���� �_�
						decision = SPEECH;
						System.out.println("fast speech");
					} else {
						decision = FAST_MATCH;
						System.out.println("fast");
					}
				} else {
					// ���
					decision = SPEECH;
					System.out.println("speech");
				}
				slowCounter = 0;
			} else // ��� ����...
			if (diff < -5) {// ������� �������
				// � ���� ���!
				decision = SLOW_RELEASE;
				System.out.println("slow");
				if (voiceDecision == decision) {
					// �����������-������ ������� �_�
					slowCounter++;
				}
			} else {// ��� � ���� �����!
				if (voiceDecision == SLOW_RELEASE || voiceDecision == SPEECH) {
					slowCounter++;
					System.out
							.println("SLOOOW: " + (3 - slowCounter) + " left");
				}
				if (slowCounter > 3) {
					slowCounter = 0;
					decision = NO_SPEECH;
					System.out.println("no speech");
				}
			}
		}
	}

	public static int voiceDecision = 0x00;
	/**
	 * 1 => draw lines<br>
	 * 0 => draw dots
	 */
	static int drL = 1;
	static boolean useWindowFunction = false;

	private static final int SPEECH = 0x11;
	private static final int FAST_MATCH = 0x01;
	private static final int SLOW_RELEASE = 0x02;
	private static final int NO_SPEECH = 0x03;

	/* Temporary storages for data */
	public static byte[] buffer;

	public static double[] fourier;
	public static double[] mfcc;

	/* One step computing timer */
	private static long timer;

	/* Windows size */
	private static final int h = AudioCanvas.height;
	private static final int w = AudioCanvas.width;

	/* Oh Jesus, it's really scalers! */
	private static final int logFourierScaler = 10;
	private static final int fourierScaler = useWindowFunction ? 70 : 150;
	private static final int mfccScaler = 5;

	/* GUI colors */
	private static final Color[] colors = {
			/* 0 */Color.WHITE // bg DEPRECATED/
			/* 1 */,
			new Color(0, 0, 255, drL == 1 ? 20 : 128) // Buffer
			/* 2 */,
			new Color(0, 255, 0, useWindowFunction ? 128 : drL == 1 ? 64 : 128) // Fo�urier
			/* 3 */,
			new Color(255, 0, 0, useWindowFunction ? 128 : drL == 1 ? 64 : 128)// logFourier
			/* 4 */, new Color(255, 255, 255, 80) // grid};
			/* 5 */, new Color(0, 255, 0) // MFCC
	};

	static void refreshColors() {
		colors[1] = new Color(0, 0, 255, drL == 1 ? 20 : 128);
		colors[2] = new Color(0, 255, 0, useWindowFunction ? 128
				: drL == 1 ? 64 : 128);
		colors[3] = new Color(255, 0, 0, useWindowFunction ? 128
				: drL == 1 ? 64 : 128);
	}

	private static int[] approximateBuffer;
	private static int[] approximateFourier;

	private static void approxB(int x, int y) {
		approximateBuffer = new int[y];
		for (int i = 0; i < approximateBuffer.length; i++) {
			approximateBuffer[i] = (int) ((double) i / (double) y * x);
		}
	}

	private static void approxF(int x, int y) {
		approximateFourier = new int[y];
		for (int i = 0; i < approximateFourier.length; i++) {
			approximateFourier[i] = (int) ((double) i / (double) y * x);
		}
	}

	private static void paintFourierGrid() {
		g.setColor(Color.lightGray);
		g.drawLine(-1, h / 2, w + 1, h / 2);
		for (int i = h - 1; i > h / 2; i -= h / 10) {
			g.setColor(colors[4]);
			g.drawLine(-1, i, w + 1, i);
			g.setColor(colors[2].darker());
			g.drawString(Integer.toString((i - h / 2) * fourierScaler), 5,
					i - 2);
		}

		int f10 = w * 20000 / Capture.sampleRate;// 10kHz
		int f1 = w * 2000 / Capture.sampleRate;// 1kHz
		g.drawLine(f10, -1, f10, h + 1);
		g.drawLine(f10 + 1, -1, f10 + 1, h + 1);
		g.drawLine(f1, -1, f1, h + 1);
		g.drawLine(f1 + 1, -1, f1 + 1, h + 1);
		g.setColor(colors[4]);

		for (int i = 0; i < 40; i++) {
			int freq = i * 500;
			int x = freq * w * 2 / Capture.sampleRate; // i*500Hz
			g.drawLine(x, -1, x, h + 1);
			g.drawString(Double.toString(((double) freq) / 1000) + "k", x, 15);
		}

		for (int i = h / 2; i > 0; i -= h / 30) {
			g.setColor(colors[4]);
			g.drawLine(-1, i, w + 1, i);
			g.setColor(colors[3].darker());
			double hh = (i - (double) h / 2) / logFourierScaler;
			g.drawString(Double.toString(hh), 5, i - 2);
		}
		g.setColor(colors[3].darker());
		g.drawString("log(FFT) : " + logFourierScaler, 35, h / 2 - 2);
		g.setColor(colors[2].darker());
		g.drawString("FFT : " + fourierScaler, 35, h / 2 + 15);
	}

	@SuppressWarnings("unused")
	private static void smooth(double[] d) {
		d[0] = (d[0] + d[1]) / 2;
		double prev = d[0];
		for (int i = 1; i < d.length - 1; i++) {
			double tmp = (prev + d[i] + d[i + 1]) / 3;
			prev = d[i];
			d[i] = tmp;
		}
		d[d.length - 1] = (prev + d[d.length - 1]) / 2;
	}

	public AudioPainter(Canvas c) {
		super(c);
	}

	@Override
	public void paint() {
		Color bg;
		if (Capture.state == Capture.MFCC) {
			switch (voiceDecision) {
			case FAST_MATCH:
				bg = Color.DARK_GRAY;
				break;
			case SLOW_RELEASE:
				bg = Color.LIGHT_GRAY;
				break;
			case SPEECH:
//				bg = Color.BLACK;
				bg = Color.WHITE;
				break;
			default:
				bg = Color.WHITE;
			}
		} else {
//			bg = Color.black;
			bg = Color.WHITE;
		}
		g.setColor(bg);
		g.fillRect(-1, -1, w + 1, h + 1);
		g.setColor(Color.white);
		g.drawString(Long.toString(System.currentTimeMillis() - timer), w - 15,
				20);

		switch (Capture.state) {
		case Capture.MFCC:
			paintMfccGrid();
			break;
		case Capture.FOURIER:
			paintFourierGrid();
			break;
		case Capture.BUFFER:
			paintBufferGrid();
			break;
		}
		if (!getAndPaintArray()) {
			return;
		}
		timer = System.currentTimeMillis();
	}

	private boolean getAndPaintArray() {
		byte[] a;
		if (Capture.sync) {
			synchronized (Capture.locker) {
				a = Capture.buffer;
			}
		} else {
			a = Capture.buffer;
		}
		if (a == null || a.length < 1) {
			return false;
		}

		g.setColor(colors[1]);
		switch (Capture.state) {
		case Capture.FOURIER:
			drawFourier(a);
			break;
		case Capture.BUFFER:
			drawBuffer(a);
			break;
		case Capture.MFCC:
			drawMFCC(a);
		}
		return true;
	}

	/**/
	/**/private void paintBufferGrid() {
		g.setColor(colors[4]);
		int offX = 5;
		int offY = h / 2;
		int[] lines = { 128, 96, 64, 32, 0, -32, -64, -96, -128, };
		for (int i : lines) {
			g.drawLine(-1, offY - i, w + 1, offY - i);
		}
		g.setColor(colors[4].brighter());
		for (int i : lines) {
			g.drawString(Integer.toString(i), offX, offY - i - 1);
		}
	}

	private void paintMfccGrid() {
		for (int i = h - 1; i > 0; i -= h / 10) {
			g.setColor(colors[4]);
			g.drawLine(-1, i, w + 1, i);
			g.setColor(colors[5].darker());
			g.drawString(Integer.toString((i - h / 2) / mfccScaler), 5, i - 2);
		}
	}

	void drawBuffer(byte[] a) {
		buffer = a;
		if (approximateBuffer == null
				|| approximateBuffer.length != buffer.length)
			approxB(w, buffer.length);
		g.setColor(Color.black);
		g.fillRect(-1, -1, w + 1, h / 2 - 127);
		g.fillRect(-1, h / 2 + 128, w + 1, h / 2);
		int lim = buffer.length - 1;
		if (buffer.length >= w) {
			g.setColor(colors[1]);
			for (int i = 0; i < lim; i++) {
				g.drawLine(approximateBuffer[i], buffer[i] + h / 2,
						approximateBuffer[i + drL], buffer[i + drL] + h / 2);
			}
		} else {
			for (int i = 0; i < lim; i++) {
				g.drawLine(approximateBuffer[i], 10 * buffer[i] + h / 2,
						approximateBuffer[i + drL], buffer[i + drL] + h / 2);
			}
		}
	}

	void drawFourier(byte[] b) {
		double[] c = Maths.DCT(Maths.byteToDoubleArray(b));
		// double[] c = Maths.invDCT_log_DCT(b);
		fourier = new double[c.length / 4];
		System.arraycopy(c, 0, fourier, 0, c.length / 4);
//		double[] log = fourier.clone();
//		for (int i = 0; i < log.length; i++) {
//			log[i] = -Math.log(log[i]) * logFourierScaler;
//		}
		for(int i = 0; i<fourier.length; i++){
			fourier[i] = -Math.abs(fourier[i]);
		}

		// ���� ������ void initialize();
		if (approximateFourier == null
				|| approximateFourier.length != fourier.length)
			approxF(w, fourier.length);

		double ff = 0;
		double ll = 0;

		for (int i = 0; i < fourier.length - 1; i++) {
			g.setColor(colors[2]);
			g.drawLine(approximateFourier[i], (int) fourier[i] / fourierScaler
					+ h / 2, approximateFourier[i + drL],
					(int) fourier[i + drL] / fourierScaler + h / 2);
			ff += fourier[i];
//			g.setColor(colors[3]);
//			g.drawLine(approximateFourier[i], (int) log[i] + h / 2,
//					approximateFourier[i + drL], (int) log[i + drL] + h / 2);
//			ll += log[i];
		}
		ff /= fourier.length;
//		ll /= log.length;
		g.setColor(Color.white);
		int he = (int) ll + h / 2;
		g.drawLine(-1, he, w + 1, he);
		he = (int) ff / fourierScaler + h / 2;
		g.drawLine(-1, he, w + 1, he);
	}

	void drawMFCC(byte[] a) {
		try {
			mfcc = Maths.MFCC(Maths.byteToDoubleArray(a))[0];
			Detector.process();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		int i = 0;
		// for (double[] dd : mfcc) {
		for (int j = 0; j < mfcc.length / 3; j++) {
			g.setColor(colors[4]);
			g.drawLine(i, -1, i, h + 1);
			g.setColor(Color.red);
			g.drawLine(i, (int) -mfcc[j] * mfccScaler + h / 2, i + 10 * drL,
					(int) -mfcc[j + drL] * mfccScaler + h / 2);
			i += 10;
		}
		g.setColor(Color.black);
		g.drawLine(i, -1, i, h + 1);
		// }
	}
}
