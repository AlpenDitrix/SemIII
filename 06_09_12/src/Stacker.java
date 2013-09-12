import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Stack;

public class Stacker {

	public static void main(String[] args) throws Exception {
		solve();
	}

	static void solve() throws Exception {
		Stack<Character> chars = new Stack<Character>();
		Stack<Integer> numbers = new Stack<Integer>();
		Stack<Character> other = new Stack<Character>();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String outputTail;

		char[] line = br.readLine().toCharArray();

		for (char c : line) {
			chars.push(c);
		}

		// write letters
		outputTail = "";
		while (!chars.empty()) {
			char c = chars.peek();
			if (Character.isLetter(c)) {
				outputTail = Character.toString(c).concat(outputTail);
				chars.pop();
			} else if (isNumber(c)) {
				numbers.push(Integer.parseInt(Character.toString(chars.pop())));
			} else {
				other.push(chars.pop());
			}
		}
		System.out.println(outputTail);

		// write numbers
		while (!numbers.empty()) {
			System.out.print(numbers.pop());
		}
		
		System.out.println();
		
		//write other characters
		while (!other.empty()){
			System.out.print(other.pop());
		}
	}

	static boolean isNumber(char c) {
		return c == '0' || c == '1' || c == '2' || c == '3' || c == '4'
			|| c == '5' || c == '6' || c == '7' || c == '8' || c == '9';
	}
}