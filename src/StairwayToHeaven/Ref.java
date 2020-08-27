package StairwayToHeaven;

import java.io.Serializable;

public class Ref implements Serializable, Comparable<Ref> {

	/**
	 * This class represents a pointer to the record. It is used at the leaves of
	 * the B+ tree
	 */
	private static final long serialVersionUID = 1L;
	private int pageNo, indexInPage;

	public Ref(int pageNo, int indexInPage) {
		this.pageNo = pageNo;
		this.indexInPage = indexInPage;
	}

	/**
	 * @return the page at which the record is saved on the hard disk
	 */
	public int getPage() {
		return pageNo;
	}

	/**
	 * @return the index at which the record is saved in the page
	 */
	public int getIndexInPage() {
		return indexInPage;
	}

	public String toString() {
		return pageNo + " " + indexInPage;
	}

	public boolean equals(Ref o) {
		return pageNo == o.pageNo && o.indexInPage == indexInPage;
	}
	
	public int compareTo(Ref o) {
		if(pageNo == o.pageNo)
			return indexInPage - o.indexInPage;
		return pageNo - o.pageNo;
	}
}