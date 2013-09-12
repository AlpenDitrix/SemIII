import java.util.ArrayList;
import java.util.Arrays;

public class Checker {

	private enum ChckrSt {
		q0_start, q1_a_reading, q2_a_skipping, q3_b_checking, q4_c_reading, qf_finish, qt_terminate;
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
		state = ChckrSt.q0_start;

		while (state != ChckrSt.qf_finish) {
			System.out.println(getStr(workLine));
			System.out.println(getRef(workIndex));
			System.out.println(getStr(stackLine));
			System.out.println(getRef(stackIndex) + "\n\n");
			stepSucceed = false;
			switch (state) {
			case q0_start:
				if (p('a', null)) {
					performStep(ChckrSt.q1_a_reading, null, D.stop, '*', D.right);
				} else if (p('c', null)) {
					performStep(ChckrSt.q4_c_reading, null, D.stop, '*', D.stop);
				} else if (p(null, null)) {
					performStep(ChckrSt.qf_finish, null, D.stop, '*', D.stop);
				}
				break;
			case q1_a_reading:
				if (p('a', null)) {
					performStep(ChckrSt.q2_a_skipping, null, D.right, '1',
							D.stop);
				}
				if (p('b', null)) {
					performStep(ChckrSt.q3_b_checking, null, D.stop, null,
							D.left);
				}
				break;
			case q2_a_skipping:
				if (p('a', '1')) {
					performStep(ChckrSt.q1_a_reading, null, D.right, null,
							D.right);
				} else if (p(null, '1')) {
					performStep(ChckrSt.qf_finish, null, D.stop, null, D.left);
				} else if (p('c', '1')) {
					performStep(ChckrSt.q4_c_reading, null, D.stop, null,
							D.left);
				} else if (p('b', '1')) {
					performStep(ChckrSt.q3_b_checking, null, D.stop, null,
							D.left);
				}
				break;
			case q3_b_checking:
				if (p('b', '1')) {
					performStep(ChckrSt.q3_b_checking, null, D.right, null,
							D.left);
				} else if (p('c', '*')) {
					performStep(ChckrSt.q4_c_reading, null, D.stop, null,
							D.stop);
				} else if (p(null, '*')) {
					performStep(ChckrSt.qf_finish, null, D.stop, null, D.stop);
				}
				break;
			case q4_c_reading:
				if (p('c', '*')) {
					performStep(ChckrSt.q4_c_reading, null, D.right, null,
							D.stop);
				} else if (p(null, '*')) {
					performStep(ChckrSt.qf_finish, null, D.stop, null, D.stop);
				}
				break;
			}

			if (!stepSucceed) {
				throw new TuringRuntimeException();
			}
		}

		// now state == qf_finish
		if (getWork() == null && getStack() == '*') {
			state = ChckrSt.qt_terminate;
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
		Character[] chars = initialize();
		
		try {
			process(chars);
			System.out.println("succ");
		} catch (TuringRuntimeException e) {
			System.out.println("FUUUUUUUUUUUUUUUUUUU /\\OX!");
		}
	}

	private static Character[] initialize() {
		String s = "aaaaaaaaabbbbccccccccccccccccccccccccccccccccccc";
		Character[] tmp = toCharArray(s);
		Character[] chars = new Character[tmp.length+1];
		System.arraycopy(tmp, 0, chars, 0, tmp.length);
		return chars;
	}

	private static Character[] toCharArray(String s) {
		Character[] tmp = new Character[s.length()];
		for(int i=0; i<tmp.length;i++){
			tmp[i] = s.charAt(i);
		}
		return tmp;
	}

}
