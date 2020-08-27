package StairwayToHeaven;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;

public class BPTree<T extends Comparable<T>> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int order;
	private BPTreeNode<T> root;
	private String filename;

	/**
	 * Creates an empty B+ tree
	 * 
	 * @param order the maximum number of keys in the nodes of the tree
	 */
	public BPTree(int order, String filename) throws Exception {
		this.order = order;
		root = new BPTreeLeafNode<T>(this.order);
		root.setRoot(true);
		this.filename = filename;
		writeBPTree();
	}

	/**
	 * Inserts the specified key associated with the given record in the B+ tree
	 * 
	 * @param key             the key to be inserted
	 * @param recordReference the reference of the record associated with the key
	 * @throws Exception
	 */
	public void insert(T key, Ref recordReference) throws Exception {
		PushUp<T> pushUp = root.insert(key, recordReference, null, -1);
		if (pushUp != null) {
			BPTreeInnerNode<T> newRoot = new BPTreeInnerNode<T>(order);
			newRoot.insertLeftAt(0, pushUp.key, root);
			newRoot.setChild(1, pushUp.newNode);
			root.setRoot(false);
			root = newRoot;
			root.setRoot(true);
			writeBPTree();
		}
	}

	public String getFileName() {
		return this.filename;
	}

	public void insertO_E(T key, Ref recordReference) throws Exception {
		Ref r = search(key);
		if (r == null) {
			insert(key, recordReference);
		}
		insertRef(key, recordReference);
		writeBPTree();
	}

	public Vector getBucket(T key) {
		return root.getBucket(key);
	}

	public void insertRef(T key, Ref recordReference) throws Exception {
		root.insertRef(key, recordReference);
		writeBPTree();
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
		boolean done = root.delete(key, null, -1);
		// go down and find the new root in case the old root is deleted
		while (root instanceof BPTreeInnerNode && !root.isRoot())
			root = ((BPTreeInnerNode<T>) root).getFirstChild();
		writeBPTree();
		return done;
	}

	public void deleteO_E(T key, Ref recordReference) throws Exception {
		Ref r = search(key);
		if (r == null) {
			return;
		}
		deleteRef(key, recordReference);
		writeBPTree();
	}

	public void deleteAll() throws Exception {
		root = new BPTreeLeafNode<T>(this.order);
		root.setRoot(true);
		writeBPTree();
	}

	public void deleteRef(T key, Ref recordReference) throws Exception {
		root.deleteRef(key, recordReference);
		if (empty(key))
			delete(key);
		writeBPTree();
	}

	public boolean empty(T key) {
		return root.empty(key);
	}

	public boolean updateKey(T key, Ref oldR, Ref newR) throws Exception {
		boolean done = root.updateKeyAndRef(key, oldR, newR, null, -1);
		writeBPTree();
		return done;
	}

	public boolean deleteKeyAndRef(T key, Ref r) throws Exception {
		boolean done = root.deleteKeyAndRef(key, r, null, -1);
		// go down and find the new root in case the old root is deleted
		while (root instanceof BPTreeInnerNode && !root.isRoot())
			root = ((BPTreeInnerNode<T>) root).getFirstChild();
		writeBPTree();
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
		Queue<BPTreeNode<T>> cur = new LinkedList<BPTreeNode<T>>(), next;
		cur.add(root);
		while (!cur.isEmpty()) {
			next = new LinkedList<BPTreeNode<T>>();
			while (!cur.isEmpty()) {
				BPTreeNode<T> curNode = cur.remove();
				System.out.print(curNode);
				if (curNode instanceof BPTreeLeafNode)
					System.out.print(Arrays.toString(((BPTreeLeafNode) curNode).getPuckets()) + "->");
				else {
					System.out.print("{");
					BPTreeInnerNode<T> parent = (BPTreeInnerNode<T>) curNode;
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

	public void writeBPTree() throws Exception {
		File f = new File(filename);
		f.delete();
		f.createNewFile();
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
		oos.writeObject(this);
		oos.close();
	}

}