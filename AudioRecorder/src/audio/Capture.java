package audio;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.security.auth.Refreshable;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JButton;
import javax.swing.JFrame;

@SuppressWarnings({ "unused", "serial" })
public class Capture extends JFrame {

	/**//* Audio format settings */
	/**/public static int sampleRate = //32
			//64
			//128
			//256
			//512
			//1024
			//2048
			//4096
			//8192
			//11025
			16384
			//22050 //IS NOT POWER OF 2
			//32768
			//44100 //IS NOT POWER OF 2
			//65536
			;
	/**/public static int sampleSizeInBits = 16;
	/**/public static int channels = 1;
	/**/public static boolean signed = true;
	/**/public static boolean bigEndian = true;
	/**//* ======================= */

	public static final byte FOURIER = 0;
	public static final byte BUFFER = 1;
	public static final byte MFCC = 2;
	private static byte LIMIT_VIEW = 3;
	private static final byte DFT_STATE = BUFFER;
	
	public static boolean running;
	public static int state;
	public static boolean sync = false;
	public static byte buffer[];
	public static Object locker = new Object();

	public static ByteArrayOutputStream rec;

	public static byte audio[];

	public static void main(String args[]) {
		new Capture();
		new AudioCanvas();
	}

	public Capture() {
		super("Capture Sound");

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		Container content = getContentPane();
		setResizable(false);

		final JButton hannign = new JButton("Hanning");
		final JButton bufferView = new JButton("B");
		final JButton fourierView = new JButton("F");
		final JButton mfccView = new JButton("M");
		final JButton stop = new JButton("Stop");
		final JButton linesOrDots = new JButton("Lines/Dots");

		linesOrDots.setEnabled(true);
		hannign.setEnabled(true);
		bufferView.setEnabled(false);
		fourierView.setEnabled(true);
		mfccView.setEnabled(true);
		state = DFT_STATE;
		stop.setEnabled(true);
		running = true;
		captureAudio();
		
		ActionListener captureListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AudioPainter.useWindowFunction = !AudioPainter.useWindowFunction;
				AudioPainter.refreshColors();
			}
		};
		ActionListener switchActivityListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AudioPainter.drL = (AudioPainter.drL+1)%2;
				AudioPainter.refreshColors();			}
		};
		ActionListener switchViewListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				enableViewButton();
				JButton b = (JButton) e.getSource();
				if (b.getText().equals("B")) {
					state = BUFFER;
				} else if (b.getText().equals("F")) {
					state = FOURIER;
				} else if (b.getText().equals("M")) {
					state = MFCC;
				}
				b.setEnabled(false);
			}

			private void enableViewButton() {
				switch (state) {
				case FOURIER:
					fourierView.setEnabled(true);
					return;
				case BUFFER:
					bufferView.setEnabled(true);
					return;
				case MFCC:
					mfccView.setEnabled(true);
					return;
				default:
					System.exit(1);
				}
			}
		};
		ActionListener stopListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				hannign.setEnabled(true);
				stop.setEnabled(false);
				running = false;
			}
		};

		hannign.addActionListener(captureListener);

		linesOrDots.addActionListener(switchActivityListener);

		bufferView.addActionListener(switchViewListener);
		fourierView.addActionListener(switchViewListener);
		mfccView.addActionListener(switchViewListener);

		stop.addActionListener(stopListener);

		content.add(hannign, BorderLayout.NORTH);
		content.add(bufferView, BorderLayout.WEST);
		content.add(fourierView, BorderLayout.CENTER);
		content.add(mfccView, BorderLayout.EAST);
		content.add(linesOrDots, BorderLayout.SOUTH);

		pack();
		setVisible(true);

	}

	private void captureAudio() {
		try {
			final AudioFormat format = getFormat();
			DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
			final TargetDataLine line = ((TargetDataLine) AudioSystem
					.getLine(info));
			line.open(format);
			line.start();
			

			new Thread(new Runnable() {
				int bufferSize = (int) format.getSampleRate()
						* format.getFrameSize();

				@Override
				public void run() {
					buffer = new byte[bufferSize];
					rec = new ByteArrayOutputStream();
					running = true;
					try {
						while (running) {
							int count = 0;
							 synchronized (locker) {
							count = line.read(buffer, 0, buffer.length);
//							System.out.println(Arrays.toString(buffer));
							 }
							if (count > 0) {
//								rec.write(buffer, 0, count);
							}
						}
						rec.close();
					} catch (IOException e) {
						System.err.println("I/O problems: " + e);
						System.exit(-1);
					}
				}
			}).start();
		} catch (LineUnavailableException e) {
			System.err.println("Line unavailable: " + e);
			System.exit(-2);
		}
	}

	private AudioFormat getFormat() {
		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed,
				bigEndian);
	}

	private void lol() {
		for (double i = 0; i < audio.length; i++) {
			audio[(int) i] = (byte) ((Math.cos(i + Math.PI) * 50) + Math.cos(i) * 25);
		}
	}

	private void playAudio() {
		try {
			// audio = rec.toByteArray();
			audio = new byte[96000];
			lol();
			// InputStream inputFile = null;
			// try {
			// inputFile = new FileInputStream(new
			// File("D:/Torrents/Muzik/65daysofstatic/Albums/2004 - The Fall of Math/03. Retreat! Retreat!.mp3"));
			// } catch (FileNotFoundException e1) {
			// e1.printStackTrace();
			// }
			InputStream input = new ByteArrayInputStream(audio);
			final AudioFormat format = getFormat();
			final AudioInputStream ais = new AudioInputStream(input, format,
					audio.length / format.getFrameSize());
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
			final SourceDataLine line = (SourceDataLine) AudioSystem
					.getLine(info);
			line.open(format);
			line.start();
			Runnable runner = new Runnable() {
				int bufferSize = (int) format.getSampleRate()
						* format.getFrameSize();

				@Override
				public void run() {
					buffer = new byte[bufferSize];
					try {
						int count;
						while ((count = ais.read(buffer, 0, buffer.length)) != -1) {
							if (count > 0) {
								line.write(buffer, 0, count);
							}
						}
						line.drain();
						line.close();
					} catch (IOException e) {
						System.err.println("I/O problems: " + e);
						System.exit(-3);
					}
				}
			};
			Thread playThread = new Thread(runner);
			playThread.start();
		} catch (LineUnavailableException e) {
			System.err.println("Line unavailable: " + e);
			System.exit(-4);
		}
	}
}