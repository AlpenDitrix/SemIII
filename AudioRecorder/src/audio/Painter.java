package audio;


abstract class Painter implements Runnable {

	protected static java.awt.Graphics g;
	private static java.awt.image.BufferStrategy bufferStrategy;
	protected long frames;

	static java.awt.Canvas canvas;

	private static boolean precompute() {
		try {
			bufferStrategy = canvas.getBufferStrategy();
			if (bufferStrategy == null) {
				canvas.createBufferStrategy(2);
				canvas.requestFocus();
				return true;
			}
			g = bufferStrategy.getDrawGraphics();
			return false;
		} catch (Exception e) {
			return true;
		}
	}

	protected static void show() {
		g.dispose();
		bufferStrategy.show();
	}
	public Painter(java.awt.Canvas c) {
		canvas = c;
	}

	public abstract void paint();

	public boolean render() {
		if (precompute()) {
			// some magic hiding here
			return false;
		}
		paint();
		show();
		return true;
	}

	@Override
	public void run() {
		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		// while (true) {
		// // try {
		// // Thread.sleep(100);
		// // } catch (InterruptedException e) {
		// // e.printStackTrace();
		// // }
		// System.err.println(Updater.steps + " : " + delay + " : " +
		// elapsed +
		// " : "
		// + sleepTime + " : " + Snake.aliveSnakesLeft);
		// }
		// }
		// }).start();

		// new Thread(new FramesCounter()).start();
		while (true) {
			// enterTime = System.currentTimeMillis();
			startPainting();
			// frames++;
			// elapsed = System.currentTimeMillis() - enterTime;
			// System.out.println(elapsed);
			// sleepTime = delay - elapsed;
			// try {
			// if (sleepTime > 0) {
			// Thread.sleep(sleepTime);
			// } else {
			// Thread.sleep(10);
			// }
			// } catch (Exception e) {
			// }
		}
	}

	private void startPainting() {
		if (render()) {
			show();
		} else {
			startPainting();
		}
	}

}
