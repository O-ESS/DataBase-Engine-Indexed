package StairwayToHeaven;

import java.io.Serializable;
import java.util.TreeSet;
import java.util.Vector;

public abstract class RTreeNode<T extends Comparable<T>> implements Serializable {

	/**
	 * Abstract class that collects the common functionalities of the inner and leaf
	 * nodes
	 */
	private static final long serialVersionUID = 1L;
	protected Comparable<T>[] keys;
	protected int numberOfKeys;
	protected int order;
	protected int index; // for printing the tree
	private boolean isRoot;
	private static int nextIdx = 0;

	public RTreeNode(int order) {
		index = nextIdx++;
		numberOfKeys = 0;
		this.order = order;
	}

	/**
	 * @return a boolean indicating whether this node is the root of the B+ tree
	 */
	public boolean isRoot() {
		return isRoot;
	}

	/**
	 * set this node to be a root or unset it if it is a root
	 * 
	 * @param isRoot the setting of the node
	 */
	public void setRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}

	/**
	 * find the key at the specified index
	 * 
	 * @param index the index at which the key is located
	 * @return the key which is located at the specified index
	 */
	public Comparable<T> getKey(int index) {
		return keys[index];
	}

	/**
	 * sets the value of the key at the specified index
	 * 
	 * @param index the index of the key to be set
	 * @param key   the new value for the key
	 */
	public void setKey(int index, Comparable<T> key) {
		keys[index] = key;
	}

	/**
	 * @return a boolean whether this node is full or not
	 */
	public boolean isFull() {
		return numberOfKeys == order;
	}

	/**
	 * @return the last key in this node
	 */
	public Comparable<T> getLastKey() {
		return keys[numberOfKeys - 1];
	}

	/**
	 * @return the first key in this node
	 */
	public Comparable<T> getFirstKey() {
		return keys[0];
	}

	/**
	 * @return the minimum number of keys this node can hold
	 */
	public abstract int minKeys();

	/**
	 * insert a key with the associated record reference in the B+ tree
	 * 
	 * @param key             the key to be inserted
	 * @param recordReference a pointer to the record on the hard disk
	 * @param parent          the parent of the current node
	 * @param ptr             the index of the parent pointer that points to this
	 *                        node
	 * @return a key and a new node in case of a node splitting and null otherwise
	 */
	public abstract PushUpRT<T> insert(T key, Ref recordReference, RTreeInnerNode<T> parent, int ptr,TreeSet<point> inP);

	public abstract Ref search(T key);

	public abstract Vector getBucket(T key);

	public abstract void insertRef(T key, Ref recordReference,TreeSet<point> inP);

	public abstract void deleteRef(T key, Ref recordReference,TreeSet<point> inP);

	public abstract boolean empty(T key);

	public abstract Ref lowerBound(T key);

	/**
	 * delete a key from the B+ tree recursively
	 * 
	 * @param key    the key to be deleted from the B+ tree
	 * @param parent the parent of the current node
	 * @param ptr    the index of the parent pointer that points to this node
	 * @return true if this node was successfully deleted and false otherwise
	 */
	public abstract boolean delete(T key, RTreeInnerNode<T> parent, int ptr,TreeSet<point> inP);

	public abstract boolean deleteKeyAndRef(T key, Ref r, RTreeInnerNode<T> parent, int ptr);

	public abstract boolean updateKeyAndRef(T key, Ref oldR, Ref newR, RTreeInnerNode<T> parent, int ptr,TreeSet<point> inP);

	public abstract Vector lowerThan(T key);

	public abstract Vector lowerThanOrEqual(T key);

	public abstract Vector biggerThan(T key);

	public abstract Vector biggerThanOrEqual(T key);

	/**
	 * A string represetation for the node
	 */
	public String toString() {
		String s = "(" + index + ")";

		s += "[";
		for (int i = 0; i < order; i++) {
			String key = " ";
			if (i < numberOfKeys)
				key = keys[i].toString();

			s += key;
			if (i < order - 1)
				s += "|";
		}
		s += "]";
		return s;
	}

}