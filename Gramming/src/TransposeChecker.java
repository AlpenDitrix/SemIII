import java.util.ArrayList;
import java.util.Arrays;

public class TransposeChecker {

	private enum ChckrSt {
		q0, q1, q2, q3, q4, q5, q6, q7, q8, q9, qt;
	}

	private enum D {
		left, right, stop
	}

	private static Character[] workLine;
	private static Character[] stackLine;
	private static int workIndex;
	private static int stackIndex;

	private static boolean stepSucceed;
	private static ChckrSt state;

	/**
	 * dummy class for breaking Turing Machine
	 * 
	 * @author Alpen Ditrix
	 */
	private static class TuringRuntimeException extends RuntimeException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2384999436162524672L;
	}

	private static boolean p(Character work, Character stack) {
		return getWork() == work && getStack() == stack;
	}

	private static String getStr(Character[] arr) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == null)
				sb.append('_');
			else
				sb.append(arr[i]);
		}
		return sb.toString();
	}

	public static void process(Character[] line) {
		initializeLines(line);
		state = ChckrSt.q0;

		while (state != ChckrSt.qt) {
			System.out.println(getStr(workLine));
			System.out.println(getRef(workIndex));
			System.out.println(getStr(stackLine));
			System.out.println(getRef(stackIndex) + "\n\n");
			stepSucceed = false;
			switch (state) {
			}

			if (!stepSucceed) {
				throw new TuringRuntimeException();
			}
		}

		// now state == qf_finish
		if (getWork() == null && getStack() == '*') {
			state = ChckrSt.qt;
		} else {
			throw new TuringRuntimeException();
		}
	}

	private static String getRef(int idx) {
		StringBuilder sb = new StringBuilder();
		int charsLeft = idx;
		while (charsLeft > 0) {
			sb.append(' ');
			charsLeft--;
		}
		return sb.append('^').toString();
	}

	private static void performStep(ChckrSt st, Character workCh, D workDest,
			Character stackCh, D stackDest) {
		state = st;
		write(workCh, stackCh);
		go(workDest, stackDest);
		stepSucceed = true;
	}

	private static void go(D work, D stack) {
		switch (work) {
		case left:
			workIndex--;
			break;
		case right:
			workIndex++;
			break;
		case stop: // do nothing;
		}
		switch (stack) {
		case left:
			stackIndex--;
			break;
		case right:
			stackIndex++;
			break;
		case stop: // do nothing;
		}
	}

	private static void write(Character work, Character stack) {
		if (work != null) {
			writeWork(work);
		}
		if (stack != null) {
			writeStack(stack);
		}
	}

	private static void writeWork(Character work) {
		workLine[workIndex] = work;
	}

	private static void writeStack(Character stack) {
		stackLine[stackIndex] = stack;
	}

	private static Character getWork() {
		return workLine[workIndex];
	}

	private static Character getStack() {
		return stackLine[stackIndex];
	}

	private static void initializeLines(Character[] line) {
		workLine = line.clone();
		stackLine = new Character[line.length];
	}

	public static void main(String[] args) {
		Character[] chars = { 'a', 'c', 'c', 'c', 'c', 'c', 'c', 'c', 'c', 'c',
				null };
		try {
			process(chars);
			System.out.println("succ");
		} catch (TuringRuntimeException e) {
			System.out.println("/\\A/\\KA!");
		}
	}

}
