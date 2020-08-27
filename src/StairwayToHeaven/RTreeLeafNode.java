package StairwayToHeaven;

import java.io.Serializable;
import java.util.Vector;
import java.util.TreeSet;

public class RTreeLeafNode<T extends Comparable<T>> extends RTreeNode<T> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Ref[] records;
	private RTreeLeafNode<T> next;
	private Vector<Ref>[] overFlowPucket;
	private Vector<TreeSet<point>>[] points;

	public Ref[] getRecords() {
		return records;
	}

	public Vector[] getPuckets() {
		return overFlowPucket;
	}
	public Vector[] getPoints() {
		return points;
	}
	@SuppressWarnings("unchecked")
	public RTreeLeafNode(int n) {
		super(n);
		keys = new Comparable[n];
		records = new Ref[n];
		overFlowPucket = new Vector[n];
		points = new Vector[n];
	}

	/**
	 * @return the next leaf node
	 */
	public RTreeLeafNode<T> getNext() {
		return this.next;
	}

	/**
	 * sets the next leaf node
	 * 
	 * @param node the next leaf node
	 */
	public void setNext(RTreeLeafNode<T> node) {
		this.next = node;
	}

	/**
	 * @param index the index to find its record
	 * @return the reference of the queried index
	 */
	public Ref getRecord(int index) {
		return records[index];
	}

	/**
	 * sets the record at the given index with the passed reference
	 * 
	 * @param index           the index to set the value at
	 * @param recordReference the reference to the record
	 */
	public void setRecord(int index, Ref recordReference) {
		records[index] = recordReference;
	}

	/**
	 * @return the reference of the last record
	 */
	public Ref getFirstRecord() {
		return records[0];
	}

	/**
	 * @return the reference of the last record
	 */
	public Ref getLastRecord() {
		return records[numberOfKeys - 1];
	}

	public Ref getMaxRecord(int id) {
		Ref cur = new Ref(0, 0);
		if (overFlowPucket[id].size() == 0 || keys[id] == null)
			return null;
		for (int i = 0; i < overFlowPucket[id].size(); i++) {
			if (overFlowPucket[id].get(i).getPage() > cur.getPage()) {
				cur = new Ref(overFlowPucket[id].get(i).getPage(), overFlowPucket[id].get(i).getIndexInPage());
			} else if (overFlowPucket[id].get(i).getPage() == cur.getPage()
					&& overFlowPucket[id].get(i).getIndexInPage() > cur.getIndexInPage()) {
				cur = new Ref(overFlowPucket[id].get(i).getPage(), overFlowPucket[id].get(i).getIndexInPage());
			}
		}
		return cur;
	}

	public Ref getMinRecord(int id) {
		Ref cur = new Ref(Integer.MAX_VALUE, Integer.MAX_VALUE);
		if (overFlowPucket[id].size() == 0 || keys[id] == null)
			return null;
		for (int i = 0; i < overFlowPucket[id].size(); i++) {
			if (overFlowPucket[id].get(i).getPage() < cur.getPage()) {
				cur = new Ref(overFlowPucket[id].get(i).getPage(), overFlowPucket[id].get(i).getIndexInPage());
			} else if (overFlowPucket[id].get(i).getPage() == cur.getPage()
					&& overFlowPucket[id].get(i).getIndexInPage() < cur.getIndexInPage()) {
				cur = new Ref(overFlowPucket[id].get(i).getPage(), overFlowPucket[id].get(i).getIndexInPage());
			}
		}
		return cur;
	}

	/**
	 * finds the minimum number of keys the current node must hold
	 */
	public int minKeys() {
		if (this.isRoot())
			return 1;
		return (order + 1) / 2;
	}

	/**
	 * insert the specified key associated with a given record refernce in the B+
	 * tree
	 */
	public PushUpRT<T> insert(T key, Ref recordReference, RTreeInnerNode<T> parent, int ptr, TreeSet<point> inP) {
		if (this.isFull()) {
			RTreeNode<T> newNode = this.split(key, recordReference);
			Comparable<T> newKey = newNode.getFirstKey();
			return new PushUpRT<T>(newNode, newKey);
		} else {
			int index = 0;
			while (index < numberOfKeys && getKey(index).compareTo(key) <= 0)
				++index;
			this.insertAt(index, key, recordReference, null, null);
			return null;
		}
	}

	/**
	 * inserts the passed key associated with its record reference in the specified
	 * index
	 * 
	 * @param index           the index at which the key will be inserted
	 * @param key             the key to be inserted
	 * @param recordReference the pointer to the record associated with the key
	 */
	private void insertAt(int index, Comparable<T> key, Ref recordReference, Vector v, Vector vinP) {
		for (int i = numberOfKeys - 1; i >= index; --i) {
			this.setKey(i + 1, getKey(i));
			this.setRecord(i + 1, getRecord(i));
			overFlowPucket[i + 1] = (Vector) overFlowPucket[i].clone();
			points[i + 1] = (Vector) points[i].clone();
		}

		this.setKey(index, key);
		this.setRecord(index, recordReference);
		if (v == null)
			overFlowPucket[index] = new Vector<>();
		else
			overFlowPucket[index] = (Vector) v.clone();
		if (vinP == null)
			points[index] = new Vector<>();
		else
			points[index] = (Vector) vinP.clone();
		++numberOfKeys;
	}

	/**
	 * splits the current node
	 * 
	 * @param key             the new key that caused the split
	 * @param recordReference the reference of the new key
	 * @return the new node that results from the split
	 */
	public RTreeNode<T> split(T key, Ref recordReference) {
		int keyIndex = this.findIndex(key);
		int midIndex = numberOfKeys / 2;
		if ((numberOfKeys & 1) == 1 && keyIndex > midIndex) // split nodes evenly
			++midIndex;

		int totalKeys = numberOfKeys + 1;
		// move keys to a new node
		RTreeLeafNode<T> newNode = new RTreeLeafNode<T>(order);
		for (int i = midIndex; i < totalKeys - 1; ++i) {
			newNode.insertAt(i - midIndex, this.getKey(i), this.getRecord(i), overFlowPucket[i], points[i]);
			numberOfKeys--;
		}

		// insert the new key
		if (keyIndex < totalKeys / 2)
			this.insertAt(keyIndex, key, recordReference, null, null);
		else
			newNode.insertAt(keyIndex - midIndex, key, recordReference, null, null);

		// set next pointers
		newNode.setNext(this.getNext());
		this.setNext(newNode);

		return newNode;
	}

	/**
	 * finds the index at which the passed key must be located
	 * 
	 * @param key the key to be checked for its location
	 * @return the expected index of the key
	 */
	public int findIndex(T key) {
		for (int i = 0; i < numberOfKeys; ++i) {
			int cmp = getKey(i).compareTo(key);
			if (cmp > 0)
				return i;
		}
		return numberOfKeys;
	}

	public Vector lowerThan(T key) {
		Vector<Ref> v = new Vector<>();
//		System.out.println("child "+numberOfKeys+" "+this);
		for (int i = 0; i < numberOfKeys; ++i) {
			if (getKey(i).compareTo(key) < 0)
				v.addAll(overFlowPucket[i]);
		}
		return v;
	}

	public Vector lowerThanOrEqual(T key) {
		Vector<Ref> v = new Vector<>();
		for (int i = 0; i < numberOfKeys; ++i) {
			if (getKey(i).compareTo(key) <= 0)
				v.addAll(overFlowPucket[i]);
		}
		return v;
	}

	public Vector biggerThan(T key) {
		Vector<Ref> v = new Vector<>();
		for (int i = 0; i < numberOfKeys; ++i) {
			if (getKey(i).compareTo(key) > 0)
				v.addAll(overFlowPucket[i]);
		}
		return v;
	}

	public Vector biggerThanOrEqual(T key) {
		Vector<Ref> v = new Vector<>();
		for (int i = 0; i < numberOfKeys; ++i) {
			if (getKey(i).compareTo(key) >= 0)
				v.addAll(overFlowPucket[i]);
		}
		return v;
	}

	/**
	 * returns the record reference with the passed key and null if does not exist
	 */
	@Override
	public Ref search(T key) {
		for (int i = 0; i < numberOfKeys; ++i)
			if (this.getKey(i).compareTo(key) == 0)
				return this.getRecord(i);
		return null;
	}

	public Vector getBucket(T key) {
		for (int i = 0; i < numberOfKeys; ++i)
			if (this.getKey(i).compareTo(key) == 0)
				return overFlowPucket[i];
		return null;
	}

	@Override
	public Ref lowerBound(T key) {
		// TODO Auto-generated method stub
		for (int i = 0; i < numberOfKeys; ++i) {
			if (this.getKey(i).compareTo(key) == 0) {
				return this.getMinRecord(i);
			}
		}
		for (int i = numberOfKeys - 1; i >= 0; --i) {
			if (this.getKey(i).compareTo(key) < 0) {
				return this.getMaxRecord(i);
			}
		}
		return null;
	}

	/**
	 * delete the passed key from the B+ tree
	 */
	public boolean delete(T key, RTreeInnerNode<T> parent, int ptr, TreeSet<point> inP) {
		for (int i = 0; i < numberOfKeys; ++i)
			if (keys[i].compareTo(key) == 0) {
				this.deleteAt(i);
				if (i == 0 && ptr > 0) {
					// update key at parent
					parent.setKey(ptr - 1, this.getFirstKey());
				}
				// check that node has enough keys
				if (!this.isRoot() && numberOfKeys < this.minKeys()) {
					// 1.try to borrow
					if (borrow(parent, ptr)) {
						// System.out.println("A7A");
						return true;
					}
					// 2.merge
					merge(parent, ptr);
				}
				return true;
			}
		return false;
	}

	/**
	 * delete the passed key and record from the B+ tree
	 */
	public boolean deleteKeyAndRef(T key, Ref r, RTreeInnerNode<T> parent, int ptr) {
		for (int i = 0; i < numberOfKeys; ++i)
			if (keys[i].compareTo(key) == 0 && records[i].equals(r)) {
				this.deleteAt(i);
				if (i == 0 && ptr > 0) {
					// update key at parent
					parent.setKey(ptr - 1, this.getFirstKey());
				}
				// check that node has enough keys
				if (!this.isRoot() && numberOfKeys < this.minKeys()) {
					// 1.try to borrow
					if (borrow(parent, ptr))
						return true;
					// 2.merge
					merge(parent, ptr);
				}
				return true;
			}
//		if(numberOfKeys>0 && keys[numberOfKeys-1].compareTo(key) == 0 && next!=null) {
//			System.out.println(r+" "+next);
//			       return next.deleteKeyAndRef(key, r, parent, ptr);
//		}
		return false;
	}

	/**
	 * delete a key at the specified index of the node
	 * 
	 * @param index the index of the key to be deleted
	 */
	public void deleteAt(int index) {
		for (int i = index; i < numberOfKeys - 1; ++i) {
			keys[i] = keys[i + 1];
			records[i] = records[i + 1];
			overFlowPucket[i] = (Vector) overFlowPucket[i + 1].clone();
			points[i] = (Vector) points[i + 1].clone();
		}
		numberOfKeys--;
	}

	/**
	 * tries to borrow a key from the left or right sibling
	 * 
	 * @param parent the parent of the current node
	 * @param ptr    the index of the parent pointer that points to this node
	 * @return true if borrow is done successfully and false otherwise
	 */
	public boolean borrow(RTreeInnerNode<T> parent, int ptr) {
		// check left sibling
		if (ptr > 0) {
			RTreeLeafNode<T> leftSibling = (RTreeLeafNode<T>) parent.getChild(ptr - 1);
			if (leftSibling.numberOfKeys > leftSibling.minKeys()) {
				this.insertAt(0, leftSibling.getLastKey(), leftSibling.getLastRecord(),
						leftSibling.overFlowPucket[leftSibling.numberOfKeys - 1],
						leftSibling.points[leftSibling.numberOfKeys - 1]);
				leftSibling.deleteAt(leftSibling.numberOfKeys - 1);
				parent.setKey(ptr - 1, keys[0]);
				return true;
			}
		}

		// check right sibling
		if (ptr < parent.numberOfKeys) {
			RTreeLeafNode<T> rightSibling = (RTreeLeafNode<T>) parent.getChild(ptr + 1);
			if (rightSibling.numberOfKeys > rightSibling.minKeys()) {
				this.insertAt(numberOfKeys, rightSibling.getFirstKey(), rightSibling.getFirstRecord(),
						rightSibling.overFlowPucket[0], rightSibling.points[0]);
				rightSibling.deleteAt(0);
				parent.setKey(ptr, rightSibling.getFirstKey());
				// System.out.println("right");
				return true;
			}
		}
		return false;
	}

	/**
	 * merges the current node with its left or right sibling
	 * 
	 * @param parent the parent of the current node
	 * @param ptr    the index of the parent pointer that points to this node
	 */
	public void merge(RTreeInnerNode<T> parent, int ptr) {
		if (ptr > 0) {
			// merge with left
			RTreeLeafNode<T> leftSibling = (RTreeLeafNode<T>) parent.getChild(ptr - 1);
			leftSibling.merge(this);
			parent.deleteAt(ptr - 1);
		} else {
			// merge with right
			RTreeLeafNode<T> rightSibling = (RTreeLeafNode<T>) parent.getChild(ptr + 1);
			this.merge(rightSibling);
			parent.deleteAt(ptr);
		}
	}

	/**
	 * merge the current node with the specified node. The foreign node will be
	 * deleted
	 * 
	 * @param foreignNode the node to be merged with the current node
	 */
	public void merge(RTreeLeafNode<T> foreignNode) {
		for (int i = 0; i < foreignNode.numberOfKeys; ++i)
			this.insertAt(numberOfKeys, foreignNode.getKey(i), foreignNode.getRecord(i), foreignNode.overFlowPucket[i],
					foreignNode.points[i]);

		this.setNext(foreignNode.getNext());
	}

	@Override
	public boolean updateKeyAndRef(T key, Ref oldR, Ref newR, RTreeInnerNode<T> parent, int ptr, TreeSet<point> inP) {
		int p = -1;
		for (int i = 0; i < numberOfKeys; ++i)
			if (keys[i].compareTo(key) == 0) {
				records[i] = newR;
				p = i;
				break;
			}
		if (p == -1)
			return false;
		for (int i = 0; i < overFlowPucket[p].size(); i++) {
			if (overFlowPucket[p].get(i).equals(oldR)) {
				overFlowPucket[p].set(i, newR);
				return true;
			}
		}
		return false;
	}

	public int searchId(T key) {
		for (int i = 0; i < numberOfKeys; ++i)
			if (this.getKey(i).compareTo(key) == 0)
				return i;
		return -1;
	}

	@Override
	public void insertRef(T key, Ref recordReference, TreeSet<point> inP) {
		// TODO Auto-generated method stub
		int id = searchId(key);
		if (id == -1)
			return;
		overFlowPucket[id].add(new Ref(recordReference.getPage(), recordReference.getIndexInPage()));
		points[id].add(inP);
	}

	@Override
	public void deleteRef(T key, Ref recordReference, TreeSet<point> inP) {

		int id = searchId(key);
		if (id == -1)
			return;
		for (int i = 0; i < overFlowPucket[id].size(); i++) {
			if (overFlowPucket[id].get(i).equals(recordReference) && equal(inP, points[id].get(i))) {
				overFlowPucket[id].remove(i);
				points[id].remove(i);
				return;
			}
		}
	}

	@Override
	public boolean empty(T key) {
		// TODO Auto-generated method stub
		int id = searchId(key);
		if (id == -1)
			return false;
		if (overFlowPucket[id].size() == 0)
			return true;
		return false;
	}

	public boolean equal(TreeSet<point> a, TreeSet<point> b) {
		if (a.size() != b.size())
			return false;
		TreeSet<point> x = (TreeSet) b.clone();
		for (point p : a) {
			point now = x.pollFirst();
			if (p.x != now.x || p.y != now.y)
				return false;
		}
		return true;
	}
}