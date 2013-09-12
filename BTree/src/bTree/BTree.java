package bTree;

import java.util.Vector;

/**
 * Этот класс описывает обычное Б-дерево и стандартные операции добаления,
 * чтения, удаления элементов, etc.
 * 
 * @author Alpen Ditrix
 * 
 * @param <Key>
 *            тип ключей
 * @param <Value>
 *            тип значений
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class BTree<Key extends Comparable<Key>, Value> {

	/**
	 * Это внутренний класс, описывающий узел дерева. <br>
	 * Каждый узел содержит 3 массива: ключи, значения и ссылки на следующие
	 * узлы, причем массив ссылок на один элемент длиннее двух других (мы
	 * сопоставляем каждому "Этому" элементу по 1 ссылке на узел, в котором все
	 * элементы меньше "Этого", а для последнего в Этом узле есть еще ссылка на
	 * узел, в котором все элементы больше него - это определение Б-Дерева)<br>
	 * Так же каждый узел знает, сколько в нем хранится значимых элементов и
	 * имеет ссылку на родительский узел, чтобы правильно оперировать элементами
	 * во время вызова каких-либо функций.
	 * 
	 * Так же класс имеет парочку статических параметров:<br>
	 * <li>ссылка на единственное дерево, для которого сейчас идет обработка
	 * узлов <li>
	 * некоторая магическая константа для магических операций
	 * 
	 * @author Alpen Ditrix
	 * 
	 * @param <Key>
	 *            тип ключа
	 * @param <Value>
	 *            тип значение
	 */
	private final static class Node<Key extends Comparable<Key>, Value> {

		/**
		 * Ссылка на дерево, для которого ведется обработка. Она нужна, чтобы
		 * можно было хранить параметры дерева, такие как, например, размеры
		 * узлов, в одном поле дерева, а не в каждом узле
		 */
		private static BTree caller;

		/**
		 * Запомним какое-то число, чтобы в процессе "ух-какого шифрования" и
		 * "ух-какого расшифрования" мы точно пользовались одинаковой константой
		 */
		private static final int magicNumber = 0x45;

		/**
		 * Библиотечкная функция бинарного поиска
		 * 
		 * @param a
		 *            упорядоченный массив, в котором ищетсяс элемент
		 * @param fromIndex
		 *            нижняя граница поиска
		 * @param toIndex
		 *            верхняя граница поиска
		 * @param key
		 *            искомый элемент
		 * @return индекс элемента в массиве или, если не нашелся, место, где он
		 *         должен быть
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
					return mid; // найден
			}
			return -(low + 1); // не найден.
		}

		/**
		 * Объединяет 2 соседных узла. Вызывается для двух узлов - соседей,
		 * когда размер одного - минимальный, а второго - меньше минимального на
		 * 1.
		 * 
		 * @param left
		 *            левый из соседей
		 * @param right
		 *            правый из соседей
		 * @param delimeterIndex
		 *            индекс элемента, раделяющего соседей в узле-родителе.
		 *            Ссылка по этому индексу из родителя ссылается на левого
		 *            соседа
		 */
		private static void merge(Node left, Node right, int delimeterIndex) {
			if (debug) {
				System.out.println("merge");
			}
			/* I. Создаем новые массивы */
			Object[] newKeys = new Object[caller.maxThreshold];
			Object[] newValues = new Object[caller.maxThreshold];
			Node[] newLinks = null;

			/*
			 * II. Заполняем их: сначала элементы из левого, потом 1
			 * "разделяющий" элемент (тот, что разделяет ссылки на правый и
			 * левый объединяемый узел)из родителя, потом элементы из правого
			 */
			System.arraycopy(left.keys, 0, newKeys, 0, left.count);
			newKeys[left.count] = left.parent.keys[delimeterIndex];
			System.arraycopy(right.keys, 0, newKeys, left.count + 1,
					right.count);

			System.arraycopy(left.values, 0, newValues, 0, left.count);
			newValues[left.count] = left.parent.values[delimeterIndex];
			System.arraycopy(right.values, 0, newValues, left.count + 1,
					right.count);

			/* III. Для нелистового узла нужно скопировать и ссылки */
			if (!left.isLeaf()) {
				newLinks = new Node[caller.maxThreshold + 1];
				System.arraycopy(left.links, 0, newLinks, 0, left.count + 1);
				System.arraycopy(right.links, 0, newLinks, left.count + 1,
						right.count + 1);
			}

			/*
			 * IV. Все дети объединяемых узлов должны узнать, что теперь они
			 * принадлежат новому родителю
			 */
			Node newNode = new Node(newKeys, newValues, newLinks, left.count
					+ 1 + right.count, left.parent);
			if (!newNode.isLeaf()) {
				for (int i = 0; i < newNode.count + 1; i++) {
					newNode.links[i].parent = newNode;
				}
			}

			/* V. Меняем ссылку на правый узел ссылкой на новый */
			left.parent.links[delimeterIndex + 1] = newNode;

			/*
			 * VI. Убираем "разделитель" из родителя вместе со ссылкой на левый
			 * узел
			 */
			left.parent.removeFromPos(delimeterIndex);
			left.parent.checkMinimalSizeAndProcess();
			left.parent.checkForEmptyRoot();
		}

		/**
		 * Ссылка на узел-родитель
		 */
		private Node<Key, Value> parent;
		/**
		 * Массив ключей
		 */
		private Object[] keys;
		/**
		 * Массив значений
		 */
		private Object[] values;

		/**
		 * Массив ссылок на узлы с ключами, попадающими в промежутки между
		 * текущими
		 */
		private Node<Key, Value>[] links;

		/**
		 * Количество элементов в узле
		 */
		private int count;

		/**
		 * Конструктор, создающий новый пустой узел. Применяется для создания
		 * корня нового дерева
		 * 
		 * @param ord
		 *            порядок узла
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
		 * Создает новый узел с заданными данными
		 * 
		 * @param k
		 *            массив ключей
		 * @param v
		 *            массив значений
		 * @param l
		 *            массив промежуточных ссылок
		 * @param c
		 *            количество элементов
		 * @param p
		 *            ссылка на родительский узел
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
		 * Замещает стандартный метод Object.toString();
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
		 * Добавляет элемент в узел в определенную позицию. Если
		 * {@code lazyOverfill} == true (такой вызов совершается при работе
		 * метода {@link #split()} и он сам проверит корректность добавления, то
		 * метод не будет проверять переполненность узла после вставки
		 * 
		 * @param pos
		 *            позиция, куда вставляем
		 * @param k
		 *            добавляемый ключ
		 * @param v
		 *            добавляемое значение
		 * @param lazyOverfill
		 *            определяет необходимость проверки переполнения узла
		 */
		private void addAt(int pos, Key k, Value v, boolean lazyOverfill) {
			/*
			 * II.1 Толкает часть массива вправо, чтобы создать "дырку" на
			 * нужной позиции
			 */
			System.arraycopy(keys, pos, keys, pos + 1, count - pos);
			System.arraycopy(values, pos, values, pos + 1, count - pos);
			if (links != null)
				System.arraycopy(links, pos, links, pos + 1, count - pos + 1);

			/* II.2 Вставляет элемент в дырку */
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
		 * Добавляет новый эелемент в конец узла
		 * 
		 * @param k
		 *            добавляемый ключ
		 * @param v
		 *            добавляемое значение
		 */
		private void addAtEnd(Key k, Value v) {
			addAt(count, k, v, false);
		}

		/**
		 * Добавляет на самую первую позицию в узел
		 * 
		 * @param k
		 *            добавляемый ключ
		 * @param v
		 *            добавляемое значение
		 */
		private void addAtZero(Key k, Value v) {
			addAt(0, k, v, false);
		}

		/**
		 * В процессе разбиения переполненного узла, его центральный элемент
		 * "поднимается" в родителя.<br>
		 * Так же важно, что когда элемент "поднят", его правая и левая ссылки
		 * будут установлены на 2 заранее подготовленных узла, оставшихся от
		 * переполненного. <br>
		 * <br>
		 * 
		 * Метод используется только внутри {@link #split()}
		 * 
		 * @param k
		 *            добавляемый ключ
		 * @param v
		 *            добавляемое значение
		 * @param nodeLeft
		 *            левая ссылка
		 * @param nodeRight
		 *            правая ссылка
		 * 
		 * @deprecated метод обозначен "устаревшим", чтобы случайно не
		 *             использовать его вне {@link #split()}, так как это
		 *             черевато переполнением узла без разбиения
		 */
		@Deprecated
		private void addWithLinking(Key k, Value v, Node nodeLeft,
				Node nodeRight) {
			int pos = getPredictedPos(k);
			addAt(pos, k, v, true);
			/* II.3 Смена ссылок */
			links[pos] = nodeLeft;
			links[pos + 1] = nodeRight;

			count++;
			if (count == keys.length) {
				split();
			}
		}

		/**
		 * Сравнивает ключи
		 * 
		 * @param A
		 *            ключ
		 * @param B
		 *            ключ
		 * @return true, если {@code A == B}. Иначе - false
		 */
		private boolean AequalsB(Key A, Key B) {
			if (A != null && B != null)
				return A.compareTo(B) == 0;
			else {
				return false;
			}
		}

		/**
		 * Сравнивает ключи
		 * 
		 * @param A
		 *            ключ
		 * @param B
		 *            ключ
		 * @return true, если A меньше или равен B. Иначе - false
		 */
		private boolean AlessOrEqThanB(Key A, Key B) {
			if (A != null && B != null)
				return A.compareTo(B) <= 0;
			else {
				return false;
			}
		}

		/**
		 * Проводит проверку минимального размера корня дерева. Если
		 * {@link #count} {@code == 0}, то ясно, что возможны 2 варианта:<br>
		 * <li>Корень - единственный узел в дереве. Тогда Это значит, что оно
		 * полностью пусто и тогда, на всякий случай, дерево очищается силой <li>
		 * Из корня есть ссылки. Причем, очевидно, что до текущая операция
		 * проверки проводится при слиянии двух детей корня (так как мы никого
		 * не подняли вверх при удалении), а значит, из этого корня идет ровно 1
		 * ссылка. С данного момента, узел по этой сслыке и будет корнем.
		 */
		private void checkForEmptyRoot() {
			if (caller.root.count == 0) {
				try {
					// корень пустой
					caller.root = caller.root.links[0];
					caller.root.parent = null;
					caller.height--;
				} catch (NullPointerException e) {
					// корень пустой. Вообще всё дерево пустое
					System.out.println("Tree is empty");
					caller.clear();
					// ну идеально пустое
				}
			}
		}

		/**
		 * Проверяет корректность размера этого узла. Если размер опустился ниже
		 * порогового значения, то метод пытается "перелить" элементы из
		 * соседних узлов. Если это не получается (оба соседа, если существуют,
		 * имеют минимально возможное число элементов)- сливает в один узел с
		 * соседом
		 */
		private void checkMinimalSizeAndProcess() {
			if (parent != null) {
				// если не корень
				if (count < caller.minThreshold) {
					int share = whoCanShareItems();
					if (share < 0) {
						// переливаем...
						if (share == -1) {
							// ...из правого
							// SIC! whoLinksMeFromParent(this)==0;
							// SIC! whoLinksMeFromParent(sibling)==1;
							flowFromRight(0);
						} else {
							// ...из левого
							share += magicNumber * caller.maxThreshold;
							flowFromLeft(share - 1);
						}
					} else {
						// сливаем в один...
						if (share == 0) {
							// ...с правым
							// SIC! whoLinksMeFromParent(this)==0;
							// SIC! whoLinksMeFromParent(sibling)==1;
							merge(this, parent.links[1], 0);
						} else {
							// ...с левым
							merge(parent.links[share - 1], parent.links[share],
									share - 1);
						}
					}
				}
			}
		}

		/**
		 * Полностью очищает узел. <br>
		 * Фактически же, всем: объектам ключей, объектам значений,
		 * промежуточным ссылкам, ссылке на родителя - присвается {@code null},
		 * а число элементов в узле обнуляется
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
		 * Переливает элемент из левого соседа в правого, чтобы восстановить
		 * допустимость размера последнего.
		 * 
		 * @param indexLeft
		 *            индекс ссылки из родителя, указывающей на левого из
		 *            соседей.
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
			 * добавляем элемент из родителя, для достижения необходимого
			 * размера
			 */
			addAtZero((Key) parent.keys[indexLeft],
					(Value) parent.values[indexLeft]);

			/* перекидываем ссылку для нового элемента */
			if (!isLeaf()) {
				links[0] = leftNode.links[leftNode.count];
				links[0].parent = this;
			}

			/* поднимаем элемент из соседа вверх в родителя */
			parent.set(indexLeft, (Key) leftNode.keys[leftNode.count - 1],
					(Value) leftNode.values[leftNode.count - 1]);

			/* убираем передаваемый элемент вместе с его ссылкой */
			int i = leftNode.count - 1;
			if (!isLeaf()) {
				System.arraycopy(leftNode.links, i + 2, leftNode.links, i + 1,
						1);
			}
			leftNode.count--;
		}

		/**
		 * Переливает элемент из правого соседа в левого, чтобы восстановить
		 * допустимость размера последнего.
		 * 
		 * @param indexLeft
		 *            индекс ссылки из родителя, указывающей на левого из
		 *            соседей.
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
			 * добавляем элемент из родителя, для достижения необходимого
			 * размера
			 */
			addAtEnd((Key) parent.keys[indexLeft],
					(Value) parent.values[indexLeft]);

			/* перекидываем ссылку для нового элемента */
			if (!isLeaf()) {
				links[count] = rightNode.links[0];
				links[count].parent = this;
			}

			/* поднимаем элемент из соседа вверх в родителя */
			parent.set(indexLeft, (Key) rightNode.keys[0],
					(Value) rightNode.values[0]);

			/* убираем передаваемый элемент вместе с его ссылкой */
			rightNode.removeFromPos(0);
		}

		/**
		 * Возвращает значение элемента, хранящегося в узле с ключем {@code key}
		 * 
		 * @param key
		 *            искомый ключ
		 * @return элемент, соответствующий ключу {@code k}, или {@code null},
		 *         если такого не обнаружилось
		 */
		private Value get(Key key) {
			int pos = getStrictPos(key);
			return pos == -1 ? null : (Value) values[pos];
		}

		/**
		 * Возвращает индекс ссылки на промежуточный узел, в котором хранятся
		 * ключи, меньшие данного. <br>
		 * Используется в поиске узла, из которого нужно
		 * {@link BTree#get(Object)} или узел, в который нужно
		 * {@link BTree#put(Object, Object)} элемент <br>
		 * В процессе поиска для всповки, алгоритм проходит все узлы до листа и
		 * возвращает индекс, куда можно было бы вставить этот ключ в узел.
		 * Важно, что этот же индекс имеет и промежуточная ссылка на узел, в
		 * который так же может быть вставлен данный ключ. Ну потому-то и
		 * работает же. <br>
		 * Для вставки же мы ищем первый узел, в котором ключ на определенный
		 * "предпологаемой позиции элемента" совпадает с искомым ключем
		 * 
		 * @param k
		 *            искомый ключ
		 * @param put
		 *            обозначает, ищется ли узел для вставки. Иначе - ищется для
		 *            получения.
		 * @return либо {@code null}, если этот узел "подходящий", либо ссылка
		 *         на следующий узел из "ветвления поиска"
		 */
		private Integer getLinkPosFrom(Key k, boolean put) {
			if (put) {
				// ищем для вставки
				if (isLeaf()) {
					// нету ссылок дальше. Пихаем сюда
					return null;
				} else {
					// есть куда ехать дальше
					return getPredictedPos(k);
				}
			} else {
				// ищем для чтения
				Integer pos = getPredictedPos(k);
				if (pos == count || !AequalsB(k, (Key) keys[pos])) {
					// не нашли
					if (isLeaf()) {
						// нету ссылок. Не нашли
						return null;
					} else {
						// Не нашли, но можно ехать дальше
						return pos;
					}
				} else {
					// нашли что-то
					// k == keys[pos];
					return -1;
				}
			}
		}

		/**
		 * @return массив промежуточных ссылок этого узла
		 */
		private Node<Key, Value>[] getLinks() {
			return links;
		}

		/**
		 * Возвращает узел по ссылке по заданному индексу
		 * 
		 * @param idx
		 *            просматриваемый индекс
		 * @return ссылка на промежуточный узел из массива по индексу
		 */
		private Node<Key, Value> getNextFrom(int idx) {
			if (idx < 0 || idx > count) {
				return null;
			} else {
				return links[idx];
			}
		}

		/**
		 * Возвращает "мягко" номер ключа куда может быть вставлен {@code k}
		 * 
		 * @param k
		 *            обрабатываемый ключ
		 * @return возвращает либо позицию ключа в узле, либо то, куда его
		 *         можно/нужно вставить
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
		 * Выдает "жестко" позицию элемента с ключом {@code k}
		 * 
		 * @param k
		 *            обрабатываемый ключ
		 * @return либо позицию ключа в узле, либо {@code -1}, если его там не
		 *         нашлось
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
		 * @return true, если этот узел - лист. Иначе - false
		 */
		private boolean isLeaf() {
			return links == null;
		}

		/**
		 * Ищет узел, в который можно было бы вставит заданный ключ. Поиск места
		 * производится как в этом узле, так и в его детях
		 * 
		 * @param k
		 *            искомый ключ
		 * @return найденный узел
		 */
		private Node predictLeafWhereToPut(Key k) {
			Node node = this;
			Integer pos = getLinkPosFrom(k, true);

			if (pos != null/*
							 * то есть, если root - не лист(см.
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
		 * Удаляет элемент из узла по его индексу и проводит проверки
		 * корректности
		 * 
		 * @param i
		 *            индекс удаляемого элемента
		 */
		private boolean remove(int i) {
			if (i == -1) {
				// такой ключ не найден в дереве во время ветвления поиска
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
		 * Удаляет элемент из узла, сохраняя ссылки на следующие узлы
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
		 * Удаляются все данный по передаваемому индексу: ключ, значение и
		 * ссылку.
		 * <p>
		 * Просто сдвигаем ту часть массива, что справа от удаляемого элемента,
		 * на одну позицию влево и накладываем её поверх остальных. <br>
		 * Было:<br>
		 * 12345<b>67890</b>. <br>
		 * Хотим удалить 5. Берем 67890 и кладем на 1 позицию влево. Получаем:<br>
		 * 1234<b>67890</b>0<br>
		 * Дальше уменьшаем <count> на 1, так что нам видно только то, что
		 * нужно: <br>
		 * 1234<b>67890</b>
		 * <p>
		 * Если метод вызывается во время слияния двух узлов, когда мы убираем
		 * из родителя разделитель, мы должны убрать и его ссылку
		 * 
		 * @param i
		 *            индекс удаляемых данных
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
		 * Удаляет последний значимый элемент в узле
		 */
		private void removeLast() {
			remove(count - 1);
		}

		/**
		 * Устанавливает значение заданного индексом элемента переданными
		 * значениями
		 * 
		 * @param i
		 *            индекс изменяемого элемента
		 * @param k
		 *            новый ключ
		 * @param v
		 *            новое значение
		 */
		private void set(int i, Key k, Value v) {
			keys[i] = k;
			values[i] = v;
		}

		/**
		 * Разбивает заполнившийся узел на 2 минимально заполненных, поднимая в
		 * родителя центральный элемент из этого.
		 */
		@SuppressWarnings("deprecation")
		private void split() {
			if (debug) {
				System.out.println("split");
			}
			/* 0. Полезные константы */
			final int l = keys.length;
			final int h = keys.length / 2;

			/* I. Подготовим новые массивы */
			Object[] keysLeft = new Object[l];
			Object[] keysRight = new Object[l];
			Object[] valuesLeft = new Object[l];
			Object[] valuesRight = new Object[l];
			Node[] linksLeft = isLeaf() ? null : new Node[l + 1];
			Node[] linksRight = isLeaf() ? null : new Node[l + 1];

			/*
			 * II. Заполним эти массивы, соответственно, 2мя половинками старого
			 * узла (делим относительно центрального эелемента)
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
			 * III. Определяем родителя. Если это мы делим не корень дерева -
			 * родитель останется тем же. Иначе мы создадим новый корень, в
			 * котором оставим единственный элемент - тот, что центральный в
			 * узле, который сейчас делим
			 */
			Node parentt = parent == null ? this : parent;

			/* IV. Создадим, наконец новые узлы */
			Node nodeLeft = new Node(keysLeft, valuesLeft, linksLeft,
					caller.minThreshold, parentt);
			Node nodeRight = new Node(keysRight, valuesRight, linksRight,
					caller.minThreshold, parentt);

			/* V. Если мы делили не лист, то нужно скопировать и ссылки */
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
			 * VI. Пихаем вверх цинтральный элемент, либо создаем новый корень с
			 * ним же
			 */
			if (parent == null) {
				/*
				 * Создавать новые "чистые" массивы, в принципе, излишне, потому
				 * что мы и так никогда не смотрим в элементы, идекс которых
				 * больше, чем <count>, но тестики на производительность не
				 * показывали ухудшений в производительности
				 * 
				 * Но дебажить проще, когда нету мусора
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
		 * Возвращает разные числа, в зависимости от разных возможностей этого
		 * узла для переливания или слияния:<br>
		 * <li>слияние с правым возможно => 0<br> <li>переливание из правого
		 * возможно => -1<br> <li>слияние с левым возможно => его индекс<br> <li>
		 * переливание из левого возможно => его идекс {@value #magicNumber}*
		 * {@link BTree#maxThreshold} (точно меньше нуля) <br>
		 * 
		 * @return определенный ключ, обозначающий текущую возможность для этого
		 *         узла, когда его размер опустился ниже минимального, по
		 *         описанному выше алгоритму
		 */
		private int whoCanShareItems() {
			if (caller.root == this) {
				throw new RuntimeException("Unable to share root node");
			}
			int l = whoLinksMeFromParent();
			if (l == 0) {
				// проверяем ПРАВЫЙ
				if (parent.links[1].count == caller.minThreshold) {
					// объеденить!
					return 0;
				} else {
					return -1;
				}
			} else {
				// ЛЕВЫЙ брат (тот, что parent.links[текущийИндесВРодителе-1])
				// существует
				if (parent.links[l - 1].count == caller.minThreshold) {
					return l;
				} else {
					// что-то точно меньшее нуля
					return l - magicNumber * caller.maxThreshold;
				}
			}
		}

		/**
		 * Возвращает индекс ссылки на текущий узел из его родителя.
		 * 
		 * @return индекс ссылки на этот узел из массива ссылок родительского
		 *         узла
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
	 * Параметр, определяющий использовать двоичный или линейный поиск во время
	 * поиска ключа в массиве.
	 */
	private boolean binarySearch;

	/**
	 * Ссылка на корневой узел дерева. Собственно, само дерево знает только о
	 * том, что у него есть один-единственный корень.<br>
	 * "Вассал моего вассала - не мой вассал" же.
	 */
	private Node<Key, Value> root;

	/**
	 * Минимальный допустимый размер узла.<br>
	 * <b> order - 1
	 */
	private int minThreshold;

	/**
	 * Максимальный допустимый размер узла<br>
	 * <b> 2 * order - 1;
	 */
	private int maxThreshold;

	/**
	 * Количество элементов в дереве. Меняется при добавлении или удалении
	 * элементов
	 */
	private int count = 0;

	/**
	 * Высота дерева. Меняется при разбиении корня(+) или когда корень пуст и он
	 * меняется на свою единственную ссылку(-). Она точно будет единственная. Я
	 * гаратирую это!
	 * */
	private int height;

	/**
	 * Создает новое пустое Б-дерево заданного порядка
	 * 
	 * @param ord
	 *            порядок
	 */
	public BTree(int ord) {
		if (ord < 2) {
			throw new IllegalArgumentException(
					"Tree degree must be more than or equal 2");
		}
		// по тестикам вроде выходит, что для вершин порядка 8 и более, более
		// производителен бинарный поиск, а для вершин меньшего порядка -
		// обычный
		// линейный
		binarySearch = (ord < 8);

		maxThreshold = 2 * ord - 1;
		minThreshold = ord - 1;
		Node.caller = this;
		root = new Node<Key, Value>(ord);
		Node.caller = null;
		height = 1;
	}

	/**
	 * Стирает информацию о дереве.
	 * <p>
	 * Фактически, просто очещает корень, а "сборщик мусора" из Java Virtual
	 * Machine сам освободит память
	 */
	public void clear() {
		root.clear();
	}

	/**
	 * Возвращает значение элемента дерева по ключу
	 * 
	 * @param key
	 *            искомый ключ
	 * @return первый попавшийся Value с искомым ключом или null, если такого не
	 *         обнаружилось
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
	 * @return высота дерева
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @return true, если в дереве нет элементов (т.е. ех количество равно
	 *         нулю). Иначе - false
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * Добавляет новый элемент в дерево
	 * 
	 * @param key
	 *            добавляемый ключ
	 * @param value
	 *            добавляемое значение
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
	 * Удаляет элемент по ключу из дерева
	 * 
	 * @param key
	 *            ключ удаляемого объекта
	 * @return был ли найден и удален ключ
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
				// нашли => убираем
				succ = node.remove(pos);
				count--;
				break;
			} else {
				// не нашли => копаем глубже, если еще можем
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
	 * @return общее число элементов в дереве (добавленные минус удаленные)
	 */
	public int size() {
		return count;
	}

	/**
	 * Замещает стандартный метод Object.toString();
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