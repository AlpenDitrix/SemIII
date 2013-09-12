package bTree;

import java.util.Vector;

/**
 * ���� ����� ��������� ������� �-������ � ����������� �������� ���������,
 * ������, �������� ���������, etc.
 * 
 * @author Alpen Ditrix
 * 
 * @param <Key>
 *            ��� ������
 * @param <Value>
 *            ��� ��������
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class BTree<Key extends Comparable<Key>, Value> {

	/**
	 * ��� ���������� �����, ����������� ���� ������. <br>
	 * ������ ���� �������� 3 �������: �����, �������� � ������ �� ���������
	 * ����, ������ ������ ������ �� ���� ������� ������� ���� ������ (��
	 * ������������ ������� "�����" �������� �� 1 ������ �� ����, � ������� ���
	 * �������� ������ "�����", � ��� ���������� � ���� ���� ���� ��� ������ ��
	 * ����, � ������� ��� �������� ������ ���� - ��� ����������� �-������)<br>
	 * ��� �� ������ ���� �����, ������� � ��� �������� �������� ��������� �
	 * ����� ������ �� ������������ ����, ����� ��������� ����������� ����������
	 * �� ����� ������ �����-���� �������.
	 * 
	 * ��� �� ����� ����� ������� ����������� ����������:<br>
	 * <li>������ �� ������������ ������, ��� �������� ������ ���� ���������
	 * ����� <li>
	 * ��������� ���������� ��������� ��� ���������� ��������
	 * 
	 * @author Alpen Ditrix
	 * 
	 * @param <Key>
	 *            ��� �����
	 * @param <Value>
	 *            ��� ��������
	 */
	private final static class Node<Key extends Comparable<Key>, Value> {

		/**
		 * ������ �� ������, ��� �������� ������� ���������. ��� �����, �����
		 * ����� ���� ������� ��������� ������, ����� ���, ��������, �������
		 * �����, � ����� ���� ������, � �� � ������ ����
		 */
		private static BTree caller;

		/**
		 * �������� �����-�� �����, ����� � �������� "��-������ ����������" �
		 * "��-������ �������������" �� ����� ������������ ���������� ����������
		 */
		private static final int magicNumber = 0x45;

		/**
		 * ������������� ������� ��������� ������
		 * 
		 * @param a
		 *            ������������� ������, � ������� ������� �������
		 * @param fromIndex
		 *            ������ ������� ������
		 * @param toIndex
		 *            ������� ������� ������
		 * @param key
		 *            ������� �������
		 * @return ������ �������� � ������� ���, ���� �� �������, �����, ��� ��
		 *         ������ ����
		 */
		private static int binarySearch0(Object[] a, int fromIndex,
				int toIndex, Object key) {
			int low = fromIndex;
			int high = toIndex - 1;

			while (low <= high) {
				int mid = (low + high) >>> 1;
				Comparable midVal = (Comparable) a[mid];
				int cmp = midVal.compareTo(key);

				if (cmp < 0)
					low = mid + 1;
				else if (cmp > 0)
					high = mid - 1;
				else
					return mid; // ������
			}
			return -(low + 1); // �� ������.
		}

		/**
		 * ���������� 2 �������� ����. ���������� ��� ���� ����� - �������,
		 * ����� ������ ������ - �����������, � ������� - ������ ������������ ��
		 * 1.
		 * 
		 * @param left
		 *            ����� �� �������
		 * @param right
		 *            ������ �� �������
		 * @param delimeterIndex
		 *            ������ ��������, ����������� ������� � ����-��������.
		 *            ������ �� ����� ������� �� �������� ��������� �� ������
		 *            ������
		 */
		private static void merge(Node left, Node right, int delimeterIndex) {
			if (debug) {
				System.out.println("merge");
			}
			/* I. ������� ����� ������� */
			Object[] newKeys = new Object[caller.maxThreshold];
			Object[] newValues = new Object[caller.maxThreshold];
			Node[] newLinks = null;

			/*
			 * II. ��������� ��: ������� �������� �� ������, ����� 1
			 * "�����������" ������� (���, ��� ��������� ������ �� ������ �
			 * ����� ������������ ����)�� ��������, ����� �������� �� �������
			 */
			System.arraycopy(left.keys, 0, newKeys, 0, left.count);
			newKeys[left.count] = left.parent.keys[delimeterIndex];
			System.arraycopy(right.keys, 0, newKeys, left.count + 1,
					right.count);

			System.arraycopy(left.values, 0, newValues, 0, left.count);
			newValues[left.count] = left.parent.values[delimeterIndex];
			System.arraycopy(right.values, 0, newValues, left.count + 1,
					right.count);

			/* III. ��� ����������� ���� ����� ����������� � ������ */
			if (!left.isLeaf()) {
				newLinks = new Node[caller.maxThreshold + 1];
				System.arraycopy(left.links, 0, newLinks, 0, left.count + 1);
				System.arraycopy(right.links, 0, newLinks, left.count + 1,
						right.count + 1);
			}

			/*
			 * IV. ��� ���� ������������ ����� ������ ������, ��� ������ ���
			 * ����������� ������ ��������
			 */
			Node newNode = new Node(newKeys, newValues, newLinks, left.count
					+ 1 + right.count, left.parent);
			if (!newNode.isLeaf()) {
				for (int i = 0; i < newNode.count + 1; i++) {
					newNode.links[i].parent = newNode;
				}
			}

			/* V. ������ ������ �� ������ ���� ������� �� ����� */
			left.parent.links[delimeterIndex + 1] = newNode;

			/*
			 * VI. ������� "�����������" �� �������� ������ �� ������� �� �����
			 * ����
			 */
			left.parent.removeFromPos(delimeterIndex);
			left.parent.checkMinimalSizeAndProcess();
			left.parent.checkForEmptyRoot();
		}

		/**
		 * ������ �� ����-��������
		 */
		private Node<Key, Value> parent;
		/**
		 * ������ ������
		 */
		private Object[] keys;
		/**
		 * ������ ��������
		 */
		private Object[] values;

		/**
		 * ������ ������ �� ���� � �������, ����������� � ���������� �����
		 * ��������
		 */
		private Node<Key, Value>[] links;

		/**
		 * ���������� ��������� � ����
		 */
		private int count;

		/**
		 * �����������, ��������� ����� ������ ����. ����������� ��� ��������
		 * ����� ������ ������
		 * 
		 * @param ord
		 *            ������� ����
		 */
		private Node(int ord) {
			if (ord < 2) {
				throw new IllegalArgumentException(
						"Tree degree must be more than or equal 2");
			}
			keys = new Object[caller.maxThreshold];
			values = new Object[caller.maxThreshold];
			links = null;
			count = 0;
			parent = null;
		}

		/**
		 * ������� ����� ���� � ��������� �������
		 * 
		 * @param k
		 *            ������ ������
		 * @param v
		 *            ������ ��������
		 * @param l
		 *            ������ ������������� ������
		 * @param c
		 *            ���������� ���������
		 * @param p
		 *            ������ �� ������������ ����
		 */
		private Node(Object[] k, Object[] v, Node<Key, Value>[] l, int c,
				Node<Key, Value> p) {
			keys = k;
			values = v;
			links = l;
			parent = p;
			count = c;
		}

		/**
		 * �������� ����������� ����� Object.toString();
		 */
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append('[');
			if (debug) {
				sb.append(count);
				sb.append("> ");
			}
			int i = 0;
			for (; i < count; i++) {
				sb.append('(');
				sb.append(keys[i]);
				sb.append(':');
				sb.append(values[i]);
				sb.append("), ");
			}
			if (i > 0) {
				sb.delete(sb.length() - 2, sb.length());
			}
			return sb.append(']').toString();
		}

		/**
		 * ��������� ������� � ���� � ������������ �������. ����
		 * {@code lazyOverfill} == true (����� ����� ����������� ��� ������
		 * ������ {@link #split()} � �� ��� �������� ������������ ����������, ��
		 * ����� �� ����� ��������� ��������������� ���� ����� �������
		 * 
		 * @param pos
		 *            �������, ���� ���������
		 * @param k
		 *            ����������� ����
		 * @param v
		 *            ����������� ��������
		 * @param lazyOverfill
		 *            ���������� ������������� �������� ������������ ����
		 */
		private void addAt(int pos, Key k, Value v, boolean lazyOverfill) {
			/*
			 * II.1 ������� ����� ������� ������, ����� ������� "�����" ��
			 * ������ �������
			 */
			System.arraycopy(keys, pos, keys, pos + 1, count - pos);
			System.arraycopy(values, pos, values, pos + 1, count - pos);
			if (links != null)
				System.arraycopy(links, pos, links, pos + 1, count - pos + 1);

			/* II.2 ��������� ������� � ����� */
			keys[pos] = k;
			values[pos] = v;
			if (!lazyOverfill) {
				count++;
				if (count == keys.length) {
					split();
				}
			}
		}

		/**
		 * ��������� ����� �������� � ����� ����
		 * 
		 * @param k
		 *            ����������� ����
		 * @param v
		 *            ����������� ��������
		 */
		private void addAtEnd(Key k, Value v) {
			addAt(count, k, v, false);
		}

		/**
		 * ��������� �� ����� ������ ������� � ����
		 * 
		 * @param k
		 *            ����������� ����
		 * @param v
		 *            ����������� ��������
		 */
		private void addAtZero(Key k, Value v) {
			addAt(0, k, v, false);
		}

		/**
		 * � �������� ��������� �������������� ����, ��� ����������� �������
		 * "�����������" � ��������.<br>
		 * ��� �� �����, ��� ����� ������� "������", ��� ������ � ����� ������
		 * ����� ����������� �� 2 ������� �������������� ����, ���������� ��
		 * ��������������. <br>
		 * <br>
		 * 
		 * ����� ������������ ������ ������ {@link #split()}
		 * 
		 * @param k
		 *            ����������� ����
		 * @param v
		 *            ����������� ��������
		 * @param nodeLeft
		 *            ����� ������
		 * @param nodeRight
		 *            ������ ������
		 * 
		 * @deprecated ����� ��������� "����������", ����� �������� ��
		 *             ������������ ��� ��� {@link #split()}, ��� ��� ���
		 *             �������� ������������� ���� ��� ���������
		 */
		@Deprecated
		private void addWithLinking(Key k, Value v, Node nodeLeft,
				Node nodeRight) {
			int pos = getPredictedPos(k);
			addAt(pos, k, v, true);
			/* II.3 ����� ������ */
			links[pos] = nodeLeft;
			links[pos + 1] = nodeRight;

			count++;
			if (count == keys.length) {
				split();
			}
		}

		/**
		 * ���������� �����
		 * 
		 * @param A
		 *            ����
		 * @param B
		 *            ����
		 * @return true, ���� {@code A == B}. ����� - false
		 */
		private boolean AequalsB(Key A, Key B) {
			if (A != null && B != null)
				return A.compareTo(B) == 0;
			else {
				return false;
			}
		}

		/**
		 * ���������� �����
		 * 
		 * @param A
		 *            ����
		 * @param B
		 *            ����
		 * @return true, ���� A ������ ��� ����� B. ����� - false
		 */
		private boolean AlessOrEqThanB(Key A, Key B) {
			if (A != null && B != null)
				return A.compareTo(B) <= 0;
			else {
				return false;
			}
		}

		/**
		 * �������� �������� ������������ ������� ����� ������. ����
		 * {@link #count} {@code == 0}, �� ����, ��� �������� 2 ��������:<br>
		 * <li>������ - ������������ ���� � ������. ����� ��� ������, ��� ���
		 * ��������� ����� � �����, �� ������ ������, ������ ��������� ����� <li>
		 * �� ����� ���� ������. ������, ��������, ��� �� ������� ��������
		 * �������� ���������� ��� ������� ���� ����� ����� (��� ��� �� ������
		 * �� ������� ����� ��� ��������), � ������, �� ����� ����� ���� ����� 1
		 * ������. � ������� �������, ���� �� ���� ������ � ����� ������.
		 */
		private void checkForEmptyRoot() {
			if (caller.root.count == 0) {
				try {
					// ������ ������
					caller.root = caller.root.links[0];
					caller.root.parent = null;
					caller.height--;
				} catch (NullPointerException e) {
					// ������ ������. ������ �� ������ ������
					System.out.println("Tree is empty");
					caller.clear();
					// �� �������� ������
				}
			}
		}

		/**
		 * ��������� ������������ ������� ����� ����. ���� ������ ��������� ����
		 * ���������� ��������, �� ����� �������� "��������" �������� ��
		 * �������� �����. ���� ��� �� ���������� (��� ������, ���� ����������,
		 * ����� ���������� ��������� ����� ���������)- ������� � ���� ���� �
		 * �������
		 */
		private void checkMinimalSizeAndProcess() {
			if (parent != null) {
				// ���� �� ������
				if (count < caller.minThreshold) {
					int share = whoCanShareItems();
					if (share < 0) {
						// ����������...
						if (share == -1) {
							// ...�� �������
							// SIC! whoLinksMeFromParent(this)==0;
							// SIC! whoLinksMeFromParent(sibling)==1;
							flowFromRight(0);
						} else {
							// ...�� ������
							share += magicNumber * caller.maxThreshold;
							flowFromLeft(share - 1);
						}
					} else {
						// ������� � ����...
						if (share == 0) {
							// ...� ������
							// SIC! whoLinksMeFromParent(this)==0;
							// SIC! whoLinksMeFromParent(sibling)==1;
							merge(this, parent.links[1], 0);
						} else {
							// ...� �����
							merge(parent.links[share - 1], parent.links[share],
									share - 1);
						}
					}
				}
			}
		}

		/**
		 * ��������� ������� ����. <br>
		 * ���������� ��, ����: �������� ������, �������� ��������,
		 * ������������� �������, ������ �� �������� - ���������� {@code null},
		 * � ����� ��������� � ���� ����������
		 */
		private void clear() {
			keys = new Object[keys.length];
			values = new Object[values.length];
			if (links != null) {
				links = new Node[links.length];
			}
			count = 0;
			parent = null;
		}

		/**
		 * ���������� ������� �� ������ ������ � �������, ����� ������������
		 * ������������ ������� ����������.
		 * 
		 * @param indexLeft
		 *            ������ ������ �� ��������, ����������� �� ������ ��
		 *            �������.
		 */
		private void flowFromLeft(int indexLeft) {
			if (debug) {
				System.out.println("flowFromLeft");
			}
			if (parent.links[indexLeft + 1] != this) {
				throw new RuntimeException("Selected nodes are not neighbors");
			}
			Node<Key, Value> leftNode = parent.links[indexLeft];
			/*
			 * ��������� ������� �� ��������, ��� ���������� ������������
			 * �������
			 */
			addAtZero((Key) parent.keys[indexLeft],
					(Value) parent.values[indexLeft]);

			/* ������������ ������ ��� ������ �������� */
			if (!isLeaf()) {
				links[0] = leftNode.links[leftNode.count];
				links[0].parent = this;
			}

			/* ��������� ������� �� ������ ����� � �������� */
			parent.set(indexLeft, (Key) leftNode.keys[leftNode.count - 1],
					(Value) leftNode.values[leftNode.count - 1]);

			/* ������� ������������ ������� ������ � ��� ������� */
			int i = leftNode.count - 1;
			if (!isLeaf()) {
				System.arraycopy(leftNode.links, i + 2, leftNode.links, i + 1,
						1);
			}
			leftNode.count--;
		}

		/**
		 * ���������� ������� �� ������� ������ � ������, ����� ������������
		 * ������������ ������� ����������.
		 * 
		 * @param indexLeft
		 *            ������ ������ �� ��������, ����������� �� ������ ��
		 *            �������.
		 */
		private void flowFromRight(int indexLeft) {
			if (debug) {
				System.out.println("flowFromRight");
			}
			if (parent.links[indexLeft] != this) {
				throw new RuntimeException("Selected nodes are not neighbors");
			}
			Node<Key, Value> rightNode = parent.links[indexLeft + 1];
			/*
			 * ��������� ������� �� ��������, ��� ���������� ������������
			 * �������
			 */
			addAtEnd((Key) parent.keys[indexLeft],
					(Value) parent.values[indexLeft]);

			/* ������������ ������ ��� ������ �������� */
			if (!isLeaf()) {
				links[count] = rightNode.links[0];
				links[count].parent = this;
			}

			/* ��������� ������� �� ������ ����� � �������� */
			parent.set(indexLeft, (Key) rightNode.keys[0],
					(Value) rightNode.values[0]);

			/* ������� ������������ ������� ������ � ��� ������� */
			rightNode.removeFromPos(0);
		}

		/**
		 * ���������� �������� ��������, ����������� � ���� � ������ {@code key}
		 * 
		 * @param key
		 *            ������� ����
		 * @return �������, ��������������� ����� {@code k}, ��� {@code null},
		 *         ���� ������ �� ������������
		 */
		private Value get(Key key) {
			int pos = getStrictPos(key);
			return pos == -1 ? null : (Value) values[pos];
		}

		/**
		 * ���������� ������ ������ �� ������������� ����, � ������� ��������
		 * �����, ������� �������. <br>
		 * ������������ � ������ ����, �� �������� �����
		 * {@link BTree#get(Object)} ��� ����, � ������� �����
		 * {@link BTree#put(Object, Object)} ������� <br>
		 * � �������� ������ ��� �������, �������� �������� ��� ���� �� ����� �
		 * ���������� ������, ���� ����� ���� �� �������� ���� ���� � ����.
		 * �����, ��� ���� �� ������ ����� � ������������� ������ �� ����, �
		 * ������� ��� �� ����� ���� �������� ������ ����. �� ������-�� �
		 * �������� ��. <br>
		 * ��� ������� �� �� ���� ������ ����, � ������� ���� �� ������������
		 * "�������������� ������� ��������" ��������� � ������� ������
		 * 
		 * @param k
		 *            ������� ����
		 * @param put
		 *            ����������, ������ �� ���� ��� �������. ����� - ������ ���
		 *            ���������.
		 * @return ���� {@code null}, ���� ���� ���� "����������", ���� ������
		 *         �� ��������� ���� �� "��������� ������"
		 */
		private Integer getLinkPosFrom(Key k, boolean put) {
			if (put) {
				// ���� ��� �������
				if (isLeaf()) {
					// ���� ������ ������. ������ ����
					return null;
				} else {
					// ���� ���� ����� ������
					return getPredictedPos(k);
				}
			} else {
				// ���� ��� ������
				Integer pos = getPredictedPos(k);
				if (pos == count || !AequalsB(k, (Key) keys[pos])) {
					// �� �����
					if (isLeaf()) {
						// ���� ������. �� �����
						return null;
					} else {
						// �� �����, �� ����� ����� ������
						return pos;
					}
				} else {
					// ����� ���-��
					// k == keys[pos];
					return -1;
				}
			}
		}

		/**
		 * @return ������ ������������� ������ ����� ����
		 */
		private Node<Key, Value>[] getLinks() {
			return links;
		}

		/**
		 * ���������� ���� �� ������ �� ��������� �������
		 * 
		 * @param idx
		 *            ��������������� ������
		 * @return ������ �� ������������� ���� �� ������� �� �������
		 */
		private Node<Key, Value> getNextFrom(int idx) {
			if (idx < 0 || idx > count) {
				return null;
			} else {
				return links[idx];
			}
		}

		/**
		 * ���������� "�����" ����� ����� ���� ����� ���� �������� {@code k}
		 * 
		 * @param k
		 *            �������������� ����
		 * @return ���������� ���� ������� ����� � ����, ���� ��, ���� ���
		 *         �����/����� ��������
		 */
		private int getPredictedPos(Key k) {
			if (caller.binarySearch) {
				int i = binarySearch0(keys, 0, count, k);
				if (i < 0) {
					i = -(i + 1);
				}
				return i;
			} else {
				int i = 0;
				for (; i < count; i++) {
					if (AlessOrEqThanB(k, (Key) keys[i])) {
						return i;
					}
				}
				return i;
			}
		}

		/**
		 * ������ "������" ������� �������� � ������ {@code k}
		 * 
		 * @param k
		 *            �������������� ����
		 * @return ���� ������� ����� � ����, ���� {@code -1}, ���� ��� ��� ��
		 *         �������
		 */
		private int getStrictPos(Key k) {
			if (caller.binarySearch) {
				int i = binarySearch0(keys, 0, count, k);
				if (i < 0) {
					i = -1;
				}
				return i;
			} else {
				for (int i = 0; i < count; i++) {
					if (AequalsB(k, (Key) keys[i])) {
						return i;
					}
				}
				return -1;
			}
		}

		/**
		 * @return true, ���� ���� ���� - ����. ����� - false
		 */
		private boolean isLeaf() {
			return links == null;
		}

		/**
		 * ���� ����, � ������� ����� ���� �� ������� �������� ����. ����� �����
		 * ������������ ��� � ���� ����, ��� � � ��� �����
		 * 
		 * @param k
		 *            ������� ����
		 * @return ��������� ����
		 */
		private Node predictLeafWhereToPut(Key k) {
			Node node = this;
			Integer pos = getLinkPosFrom(k, true);

			if (pos != null/*
							 * �� ����, ���� root - �� ����(��.
							 * #node.getLinkFrom()#
							 */) {
				do {
					node = node.getNextFrom(pos);
					pos = node.getLinkPosFrom(k, true);
				} while (pos != null);
			}

			return node;
		}

		/**
		 * ������� ������� �� ���� �� ��� ������� � �������� ��������
		 * ������������
		 * 
		 * @param i
		 *            ������ ���������� ��������
		 */
		private boolean remove(int i) {
			if (i == -1) {
				// ����� ���� �� ������ � ������ �� ����� ��������� ������
				return false;
			}

			if (isLeaf()) {
				removeFromPos(i);
				checkMinimalSizeAndProcess();
				checkForEmptyRoot();
			} else {
				removeFromNode(i);
			}
			return true;
		}

		/**
		 * ������� ������� �� ����, �������� ������ �� ��������� ����
		 * 
		 * @param i
		 */
		private void removeFromNode(int i) {
			// pop up and remove last
			Node node = links[i];
			while (!node.isLeaf()) {
				node = node.links[node.count];
			}

			keys[i] = node.keys[node.count - 1];
			values[i] = node.values[node.count - 1];
			node.removeLast();
		}

		/**
		 * ��������� ��� ������ �� ������������� �������: ����, �������� �
		 * ������.
		 * <p>
		 * ������ �������� �� ����� �������, ��� ������ �� ���������� ��������,
		 * �� ���� ������� ����� � ����������� � ������ ���������. <br>
		 * ����:<br>
		 * 12345<b>67890</b>. <br>
		 * ����� ������� 5. ����� 67890 � ������ �� 1 ������� �����. ��������:<br>
		 * 1234<b>67890</b>0<br>
		 * ������ ��������� <count> �� 1, ��� ��� ��� ����� ������ ��, ���
		 * �����: <br>
		 * 1234<b>67890</b>
		 * <p>
		 * ���� ����� ���������� �� ����� ������� ���� �����, ����� �� �������
		 * �� �������� �����������, �� ������ ������ � ��� ������
		 * 
		 * @param i
		 *            ������ ��������� ������
		 */
		private void removeFromPos(int i) {

			System.arraycopy(keys, i + 1, keys, i, count - i - 1);
			System.arraycopy(values, i + 1, values, i, count - i - 1);
			if (links != null) {
				System.arraycopy(links, i + 1, links, i, count - i);
			}
			count--;
		}

		/**
		 * ������� ��������� �������� ������� � ����
		 */
		private void removeLast() {
			remove(count - 1);
		}

		/**
		 * ������������� �������� ��������� �������� �������� �����������
		 * ����������
		 * 
		 * @param i
		 *            ������ ����������� ��������
		 * @param k
		 *            ����� ����
		 * @param v
		 *            ����� ��������
		 */
		private void set(int i, Key k, Value v) {
			keys[i] = k;
			values[i] = v;
		}

		/**
		 * ��������� ������������� ���� �� 2 ���������� �����������, �������� �
		 * �������� ����������� ������� �� �����.
		 */
		@SuppressWarnings("deprecation")
		private void split() {
			if (debug) {
				System.out.println("split");
			}
			/* 0. �������� ��������� */
			final int l = keys.length;
			final int h = keys.length / 2;

			/* I. ���������� ����� ������� */
			Object[] keysLeft = new Object[l];
			Object[] keysRight = new Object[l];
			Object[] valuesLeft = new Object[l];
			Object[] valuesRight = new Object[l];
			Node[] linksLeft = isLeaf() ? null : new Node[l + 1];
			Node[] linksRight = isLeaf() ? null : new Node[l + 1];

			/*
			 * II. �������� ��� �������, ��������������, 2�� ����������� �������
			 * ���� (����� ������������ ������������ ���������)
			 */
			System.arraycopy(keys, 0, keysLeft, 0, h);
			System.arraycopy(keys, h + 1, keysRight, 0, h);
			System.arraycopy(values, 0, valuesLeft, 0, h);
			System.arraycopy(values, h + 1, valuesRight, 0, h);
			if (!isLeaf()) {
				System.arraycopy(links, 0, linksLeft, 0, h + 1);
				System.arraycopy(links, h + 1, linksRight, 0, h + 1);
				// if it is leaf it must remain "link" arrays equal to null
			}

			/*
			 * III. ���������� ��������. ���� ��� �� ����� �� ������ ������ -
			 * �������� ��������� ��� ��. ����� �� �������� ����� ������, �
			 * ������� ������� ������������ ������� - ���, ��� ����������� �
			 * ����, ������� ������ �����
			 */
			Node parentt = parent == null ? this : parent;

			/* IV. ��������, ������� ����� ���� */
			Node nodeLeft = new Node(keysLeft, valuesLeft, linksLeft,
					caller.minThreshold, parentt);
			Node nodeRight = new Node(keysRight, valuesRight, linksRight,
					caller.minThreshold, parentt);

			/* V. ���� �� ������ �� ����, �� ����� ����������� � ������ */
			if (!nodeLeft.isLeaf()) {
				for (Node node : nodeLeft.links) {
					if (node != null) {
						node.parent = nodeLeft;
					}
				}
			}
			if (!nodeRight.isLeaf()) {
				for (Node node : nodeRight.links) {
					if (node != null) {
						node.parent = nodeRight;
					}
				}
			}

			/*
			 * VI. ������ ����� ����������� �������, ���� ������� ����� ������ �
			 * ��� ��
			 */
			if (parent == null) {
				/*
				 * ��������� ����� "������" �������, � ��������, �������, ������
				 * ��� �� � ��� ������� �� ������� � ��������, ����� �������
				 * ������, ��� <count>, �� ������� �� ������������������ ��
				 * ���������� ��������� � ������������������
				 * 
				 * �� �������� �����, ����� ���� ������
				 */
				// Object kk = keys[h];
				// Object vv = values[h];
				// keys = new Object[caller.maxThreshold];
				// values = new Object[caller.maxThreshold];
				// keys[0] = kk;
				// values[0] = vv;
				/**/

				keys[0] = keys[h];
				values[0] = values[h];
				count = 1;
				links = new Node[caller.maxThreshold + 1];
				links[0] = nodeLeft;
				links[1] = nodeRight;

				caller.height++;
			} else {
				parent.addWithLinking((Key) keys[h], (Value) values[h],
						nodeLeft, nodeRight);
			}
		}

		/**
		 * ���������� ������ �����, � ����������� �� ������ ������������ �����
		 * ���� ��� ����������� ��� �������:<br>
		 * <li>������� � ������ �������� => 0<br> <li>����������� �� �������
		 * �������� => -1<br> <li>������� � ����� �������� => ��� ������<br> <li>
		 * ����������� �� ������ �������� => ��� ����� {@value #magicNumber}*
		 * {@link BTree#maxThreshold} (����� ������ ����) <br>
		 * 
		 * @return ������������ ����, ������������ ������� ����������� ��� �����
		 *         ����, ����� ��� ������ ��������� ���� ������������, ��
		 *         ���������� ���� ���������
		 */
		private int whoCanShareItems() {
			if (caller.root == this) {
				throw new RuntimeException("Unable to share root node");
			}
			int l = whoLinksMeFromParent();
			if (l == 0) {
				// ��������� ������
				if (parent.links[1].count == caller.minThreshold) {
					// ����������!
					return 0;
				} else {
					return -1;
				}
			} else {
				// ����� ���� (���, ��� parent.links[���������������������-1])
				// ����������
				if (parent.links[l - 1].count == caller.minThreshold) {
					return l;
				} else {
					// ���-�� ����� ������� ����
					return l - magicNumber * caller.maxThreshold;
				}
			}
		}

		/**
		 * ���������� ������ ������ �� ������� ���� �� ��� ��������.
		 * 
		 * @return ������ ������ �� ���� ���� �� ������� ������ �������������
		 *         ����
		 */
		private int whoLinksMeFromParent() {
			for (int i = 0; i < parent.count + 1; i++) {
				if (parent.links[i] == this) {
					return i;
				}
			}
			throw new RuntimeException("Not found link from parent to \n"
					+ parent + "\n" + this);
		}
	}

	/**
	 * debug
	 */
	public static final boolean debug = false;

	/**
	 * ��������, ������������ ������������ �������� ��� �������� ����� �� �����
	 * ������ ����� � �������.
	 */
	private boolean binarySearch;

	/**
	 * ������ �� �������� ���� ������. ����������, ���� ������ ����� ������ �
	 * ���, ��� � ���� ���� ����-������������ ������.<br>
	 * "������ ����� ������� - �� ��� ������" ��.
	 */
	private Node<Key, Value> root;

	/**
	 * ����������� ���������� ������ ����.<br>
	 * <b> order - 1
	 */
	private int minThreshold;

	/**
	 * ������������ ���������� ������ ����<br>
	 * <b> 2 * order - 1;
	 */
	private int maxThreshold;

	/**
	 * ���������� ��������� � ������. �������� ��� ���������� ��� ��������
	 * ���������
	 */
	private int count = 0;

	/**
	 * ������ ������. �������� ��� ��������� �����(+) ��� ����� ������ ���� � ��
	 * �������� �� ���� ������������ ������(-). ��� ����� ����� ������������. �
	 * ��������� ���!
	 * */
	private int height;

	/**
	 * ������� ����� ������ �-������ ��������� �������
	 * 
	 * @param ord
	 *            �������
	 */
	public BTree(int ord) {
		if (ord < 2) {
			throw new IllegalArgumentException(
					"Tree degree must be more than or equal 2");
		}
		// �� �������� ����� �������, ��� ��� ������ ������� 8 � �����, �����
		// �������������� �������� �����, � ��� ������ �������� ������� -
		// �������
		// ��������
		binarySearch = (ord < 8);

		maxThreshold = 2 * ord - 1;
		minThreshold = ord - 1;
		Node.caller = this;
		root = new Node<Key, Value>(ord);
		Node.caller = null;
		height = 1;
	}

	/**
	 * ������� ���������� � ������.
	 * <p>
	 * ����������, ������ ������� ������, � "������� ������" �� Java Virtual
	 * Machine ��� ��������� ������
	 */
	public void clear() {
		root.clear();
	}

	/**
	 * ���������� �������� �������� ������ �� �����
	 * 
	 * @param key
	 *            ������� ����
	 * @return ������ ���������� Value � ������� ������ ��� null, ���� ������ ��
	 *         ������������
	 */
	public Value get(Object key) {
		while (Node.caller != null) {
			System.out.println("get");
		}
		Node.caller = this;
		Node<Key, Value> node = root;
		Value out = null;
		Integer pos = node.getLinkPosFrom((Key) key, false);
		while (pos != null) {
			if (pos != -1) {
				node = node.getNextFrom(pos);
				pos = node.getLinkPosFrom((Key) key, false);
			} else {
				out = node.get((Key) key);
				break;
			}
		}
		Node.caller = null;
		return out;
	}

	/**
	 * @return ������ ������
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @return true, ���� � ������ ��� ��������� (�.�. �� ���������� �����
	 *         ����). ����� - false
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * ��������� ����� ������� � ������
	 * 
	 * @param key
	 *            ����������� ����
	 * @param value
	 *            ����������� ��������
	 */
	public void put(Object key, Object value) {
		while (Node.caller != null) {
			System.out.println("put");
		}
		Node.caller = this;
		Node<Key, Value> node = root.predictLeafWhereToPut((Key) key);
		node.addAt(node.getPredictedPos((Key) key), (Key) key, (Value) value,
				false);
		count++;
		Node.caller = null;
	}

	/**
	 * ������� ������� �� ����� �� ������
	 * 
	 * @param key
	 *            ���� ���������� �������
	 * @return ��� �� ������ � ������ ����
	 */
	public boolean remove(Object key) {
		while (Node.caller != null) {
			System.out.println("remove");
		}
		Node.caller = this;
		Node<Key, Value> node = root;
		int pos = node.getPredictedPos((Key) key);
		boolean succ = true;
		while (true) {
			if (pos < node.count
					&& node.AequalsB((Key) node.keys[pos], (Key) key)) {
				// ����� => �������
				succ = node.remove(pos);
				count--;
				break;
			} else {
				// �� ����� => ������ ������, ���� ��� �����
				if (node.isLeaf()) {
					break;
				}
				node = node.links[pos];
				pos = node.getPredictedPos((Key) key);
			}
		}
		Node.caller = null;
		return succ;
	}

	/**
	 * @return ����� ����� ��������� � ������ (����������� ����� ���������)
	 */
	public int size() {
		return count;
	}

	/**
	 * �������� ����������� ����� Object.toString();
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (debug) {
			sb.append(height + "\n");
		}
		Vector<Node<Key, Value>> thisLevelLinks = new Vector<Node<Key, Value>>();
		thisLevelLinks.add(root);
		while (thisLevelLinks.size() > 0) {
			Vector<Node<Key, Value>> th = new Vector<Node<Key, Value>>();
			if (debug) {
				sb.append("[[" + thisLevelLinks.size() + ">>  ");
			}
			for (Node<Key, Value> n : thisLevelLinks) {
				sb.append(n);
				sb.append(' ');
				try {
					Node links[] = n.getLinks();
					for (int i = 0; i < n.count + 1; i++) {
						th.add(links[i]);
					}
				} catch (NullPointerException e) {
				}
			}
			sb.append('\n');
			thisLevelLinks = th;
		}

		return sb.toString();
	}

//	public boolean containsKey(Object key) {
//		return false;
//	}
//
//	public boolean containsValue(Object value) {
//		// TODO Auto-generated method stub
//		return false;
//	}
}