package audio;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@SuppressWarnings("serial")
public class AudioCanvas extends Frame {

	public static int height = 512;
	public static int width = 1024;

	static java.awt.Canvas canvas = new Canvas() {
		{
			setPreferredSize(new Dimension(width, height));
		}
	};

	public AudioCanvas() {
		super("Canvas");

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				System.exit(0);
			}
		});
		setLocation(200, 0);
		setLayout(new BorderLayout());
		add(canvas, BorderLayout.CENTER);
		setResizable(true);
		(new Thread(new AudioPainter(canvas))).start();
		pack();
		setVisible(true);
	}
}