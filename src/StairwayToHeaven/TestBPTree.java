package StairwayToHeaven;

import java.util.Scanner;

public class TestBPTree {

	public static void main(String[] args) throws Exception {
		BPTree<Integer> tree = new BPTree<Integer>(4, "test");
		Scanner sc = new Scanner(System.in);
		while (true) {
			int x = sc.nextInt();
			if (x == -1)
				break;
			tree.insertO_E(x, new Ref(sc.nextInt(), sc.nextInt()));
			System.out.println(tree.toString());
		}
		System.out.println("here");
		while (true) {
			int x = sc.nextInt();
			if (x == -1)
				break;
			System.out.println(tree.lowerThan(x));
			System.out.println(tree.lowerThanOrEqual(x));
			System.out.println(tree.biggerThan(x));
			System.out.println(tree.biggerThanOrEqual(x));
			System.out.println(tree.toString());
		}
		sc.close();
	}
}