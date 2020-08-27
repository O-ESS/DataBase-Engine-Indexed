package StairwayToHeaven;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeSet;
import java.util.Vector;

public class RTree<T extends Comparable<T>> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int order;
	private RTreeNode<T> root;
	private String filename;

	/**
	 * Creates an empty B+ tree
	 * 
	 * @param order the maximum number of keys in the nodes of the tree
	 */
	public RTree(int order, String filename) throws Exception {
		this.order = order;
		root = new RTreeLeafNode<T>(this.order);
		root.setRoot(true);
		this.filename = filename;
		writeRTree();
	}

	/**
	 * Inserts the specified key associated with the given record in the B+ tree
	 * 
	 * @param key             the key to be inserted
	 * @param recordReference the reference of the record associated with the key
	 * @throws Exception
	 */
	public void insert(T key, Ref recordReference, TreeSet<point> inP) throws Exception {
		PushUpRT<T> pushUp = root.insert(key, recordReference, null, -1, inP);
		if (pushUp != null) {
			RTreeInnerNode<T> newRoot = new RTreeInnerNode<T>(order);
			newRoot.insertLeftAt(0, pushUp.key, root);
			newRoot.setChild(1, pushUp.newNode);
			root.setRoot(false);
			root = newRoot;
			root.setRoot(true);
			writeRTree();
		}
	}

	public String getFileName() {
		return this.filename;
	}

	public void insertO_E(T key, Ref recordReference, TreeSet<point> inP) throws Exception {
		Ref r = search(key);
		if (r == null) {
			insert(key, recordReference, inP);
		}
		insertRef(key, recordReference, inP);
		writeRTree();
	}

	public Vector getBucket(T key) {
		return root.getBucket(key);
	}

	public void insertRef(T key, Ref recordReference, TreeSet<point> inP) throws Exception {
		root.insertRef(key, recordReference, inP);
		writeRTree();
	}

	public Ref lowerBound(T key) {
		return root.lowerBound(key);
	}

	/**
	 * Looks up for the record that is associated with the specified key
	 * 
	 * @param key the key to find its record
	 * @return the reference of the record associated with this key
	 */
	public Ref search(T key) {
		return root.search(key);
	}

	/**
	 * Delete a key and its associated record from the tree.
	 * 
	 * @param key the key to be deleted
	 * @return a boolean to indicate whether the key is successfully deleted or it
	 *         was not in the tree
	 * @throws Exception
	 */
	public boolean delete(T key) throws Exception {
		boolean done = root.delete(key, null, -1, null);
		// go down and find the new root in case the old root is deleted
		while (root instanceof RTreeInnerNode && !root.isRoot())
			root = ((RTreeInnerNode<T>) root).getFirstChild();
		writeRTree();
		return done;
	}

	public void deleteO_E(T key, Ref recordReference, TreeSet<point> inP) throws Exception {
		Ref r = search(key);
		if (r == null) {
			return;
		}
		deleteRef(key, recordReference, inP);
		writeRTree();
	}

	public void deleteAll() throws Exception {
		root = new RTreeLeafNode<T>(this.order);
		root.setRoot(true);
		writeRTree();
	}

	public void deleteRef(T key, Ref recordReference, TreeSet<point> inP) throws Exception {
		root.deleteRef(key, recordReference, inP);
		if (empty(key))
			delete(key);
		writeRTree();
	}

	public boolean empty(T key) {
		return root.empty(key);
	}

	public boolean updateKey(T key, Ref oldR, Ref newR, TreeSet<point> inP) throws Exception {
		boolean done = root.updateKeyAndRef(key, oldR, newR, null, -1, inP);
		writeRTree();
		return done;
	}

	public boolean deleteKeyAndRef(T key, Ref r, TreeSet<point> inP) throws Exception {
		boolean done = root.deleteKeyAndRef(key, r, null, -1);
		// go down and find the new root in case the old root is deleted
		while (root instanceof RTreeInnerNode && !root.isRoot())
			root = ((RTreeInnerNode<T>) root).getFirstChild();
		writeRTree();
		return done;
	}

	public Vector lowerThan(T key) {
		return root.lowerThan(key);
	}

	public Vector lowerThanOrEqual(T key) {
		return root.lowerThanOrEqual(key);
	}

	public Vector biggerThan(T key) {
		return root.biggerThan(key);
	}

	public Vector biggerThanOrEqual(T key) {
		return root.biggerThanOrEqual(key);
	}

	/**
	 * Returns a string representation of the B+ tree.
	 */
	public String toString() {

		// <For Testing>
		// node : (id)[k1|k2|k3|k4]{P1,P2,P3,}
		String s = "";
		Queue<RTreeNode<T>> cur = new LinkedList<RTreeNode<T>>(), next;
		cur.add(root);
		while (!cur.isEmpty()) {
			next = new LinkedList<RTreeNode<T>>();
			while (!cur.isEmpty()) {
				RTreeNode<T> curNode = cur.remove();
				System.out.print(curNode);
				if (curNode instanceof RTreeLeafNode)
					System.out.print(Arrays.toString(((RTreeLeafNode) curNode).getPuckets())+" points  "+Arrays.toString(((RTreeLeafNode) curNode).getPoints()) + "->");
				else {
					System.out.print("{");
					RTreeInnerNode<T> parent = (RTreeInnerNode<T>) curNode;
					for (int i = 0; i <= parent.numberOfKeys; ++i) {
						System.out.print(parent.getChild(i).index + ",");
						next.add(parent.getChild(i));
					}
					System.out.print("} ");
				}

			}
			System.out.println();
			cur = next;
		}
		// </For Testing>
		return s;
	}

	public void writeRTree() throws Exception {
		File f = new File(filename);
		f.delete();
		f.createNewFile();
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
		oos.writeObject(this);
		oos.close();
	}

}