package bTree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Worker {

	static BTree tree;
	static int keyType;
	static int valueType;

	static String currString;
	static String prevString;

	static BufferedReader br = new BufferedReader(new InputStreamReader(
			System.in));

	public static void main(String[] args) throws IOException {
		System.out.println("�������� ������� ������, ������? ������� \"��\", ���� �������-�");
		if (!readString().toLowerCase().equals("��")) {
			System.out.println("�� ��� �� ������-�...");
			System.exit(0);}
	
			// ������� ���
			System.out
					.println("\n�� ������ �� �������� ��� ������-� ��������� ������ �� ����� ������, ������?");
			System.out.println("0 Integer");
			System.out.println("1 Long");
			System.out.println("2 Double");
			System.out.println("3 String");

			int t1 = readInteger();
			System.out.println("\n� ��� �� ���� ���� �������� ���������-�?");

			int t2 = readInteger();

			System.out
					.println("\n�� �, �������, ������� ������� ������ (������� �� ������ 2, �����������!)");
			int t3 = readInteger();

			createTree(t1, t2, t3);

			System.out
					.println("\n������ ������, ��� �� �� �������, ������!\n\n\n");
			work();
	}

	private static void addElements() throws IOException {
		while (true) {
			System.out.println("������� ����");
			Object k = readKey();
			System.out.println("������� ��������");
			tree.put(k, readValue());
		}
	}

	private static Integer readInteger() throws IOException {
		Integer i = 0;
		boolean complete = false;
		while (!complete) {
			complete = true;
			try {
				i = Integer.parseInt(readString());
			} catch (NumberFormatException e) {
				System.out.println("        !!!!!�������� ������ �����: "
						+ e.getMessage().substring(18)
						+ "\n        !!!!!���������� ������\n");
				complete = false;
			}
		}
		return i;
	}
	
	private static Long readLong() throws IOException {
		Long l = 0l;
		boolean complete = false;
		while (!complete) {
			try {
				l = Long.parseLong(readString());
			} catch (NumberFormatException e) {
				System.out.println("        !!!!!�������� ������ �����: "
						+ e.getMessage().substring(18)
						+ "\n        !!!!!���������� ������\n");
			}
			complete = true;
		}
		return l;
	}
	private static Double readDouble() throws IOException {
		Double d = 0d;
		boolean complete = false;
		while (!complete) {
			try {
				d = Double.parseDouble(readString());
			} catch (NumberFormatException e) {
				System.out.println("        !!!!!�������� ������ �����: "
						+ e.getMessage().substring(18)
						+ "\n        !!!!!���������� ������\n");
			}
			complete = true;
		}
		return d;
	}

	private static Object readKey() throws IOException, NumberFormatException {
		switch (keyType) {
		case 0:
			return readInteger();
		case 1:
			return readLong();
		case 2:
			return readDouble();
		case 3:
			return readString();
		}
		return null;
	}

	private static String readString() throws IOException {
		String s = br.readLine();
		if (s.equals(""))
			throw new RuntimeException();
		else
			return s;
	}

	private static Object readValue() throws IOException {
		switch (valueType) {
		case 0:
			return readInteger();
		case 1:
			return readLong();
		case 2:
			return readDouble();
		case 3:
			return readString();
		}
		return null;
	}

	private static void removeElement() throws IOException {
		while (true) {
			System.out.println("������� ����");
			tree.remove(readKey());
		}
	}

	static void createTree(int t1, int t2, int t3) {
		keyType = t1;
		valueType = t2;
		switch (t1) {
		case 0:
			createWithInteger(t2, t3);
			break;
		case 1:
			createWithLong(t2, t3);
			break;
		case 2:
			createWithDouble(t2, t3);
			break;
		case 3:
			createWithString(t2, t3);
			break;
		}
	}

	static void createWithDouble(int t2, int t3) {
		switch (t2) {
		case 0:
			tree = new BTree<Double, Integer>(t3);
			break;
		case 1:
			tree = new BTree<Double, Long>(t3);
			break;
		case 2:
			tree = new BTree<Double, Double>(t3);
			break;
		case 3:
			tree = new BTree<Double, String>(t3);
			break;
		}
	}

	static void createWithInteger(int t2, int t3) {
		switch (t2) {
		case 0:
			tree = new BTree<Integer, Integer>(t3);
			break;
		case 1:
			tree = new BTree<Integer, Long>(t3);
			break;
		case 2:
			tree = new BTree<Integer, Double>(t3);
			break;
		case 3:
			tree = new BTree<Integer, String>(t3);
			break;
		}
	}

	static void createWithLong(int t2, int t3) {
		switch (t2) {
		case 0:
			tree = new BTree<Long, Integer>(t3);
			break;
		case 1:
			tree = new BTree<Long, Long>(t3);
			break;
		case 2:
			tree = new BTree<Long, Double>(t3);
			break;
		case 3:
			tree = new BTree<Long, String>(t3);
			break;
		}
	}

	static void createWithString(int t2, int t3) {
		switch (t2) {
		case 0:
			tree = new BTree<String, Integer>(t3);
			break;
		case 1:
			tree = new BTree<String, Long>(t3);
			break;
		case 2:
			tree = new BTree<String, Double>(t3);
			break;
		case 3:
			tree = new BTree<String, String>(t3);
			break;
		}
	}

	static void decideTask(int i) throws IOException {
		switch (i) {
		case 0:
			System.out.println(tree);
			remainTask = false;
			break;
		case 1:
			addElements();
			break;
		case 2:
			removeElement();
			break;
		case 3:
			getValue();
			break;
		case 9:
			System.exit(0);
		}
	}

	static void getValue() throws IOException {
		System.out.println("������� ����");
		System.out.println("�������� �� �����: " + tree.get(readKey()));
		System.out.println();
	}

	static void printTasks() {
		System.out.println("�������� �������: ");
		System.out.println("0 ���������� ������");
		System.out.println("1 �������� ��������");
		System.out.println("2 ������� ��������");
		System.out.println("3 �������� �������� �� �����");
		System.out.println("9 �����");
		System.out.println();
		System.out.println("����� �� ������� ������ ������� Enter");
		System.out.println();
	}

	static boolean remainTask = false;
	
	static void work() {
		int task = 0;
		
		while (true) {
			try {
				if (!remainTask) {
					printTasks();
					task = readInteger();
				}

				remainTask = true;
				decideTask(task);
				
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			} catch (RuntimeException e) {
				remainTask = false;
			}
		}
	}
}