/**
 * Basic BTree node. Stores location on file,
 * keys and key counter, children pointers, and 
 * whether it is a leaf or not. 
 * 
 * @author Ben Mcavoy, Ben Peterson
 */
public class BTreeNode {
	
	public TreeObject[] keys;
	public long[] children;
	public boolean isLeaf;
	public int n; //current number of keys
	public long filePos; //position of node in file
	
	/**
	 * Constructor. Sets file position and creates 
	 * arrays large enough to hold keys and children.
	 * Intializes all arrays.
	 * 
	 * @param t degree of tree
	 * @param filePos location in the BTree file
	 */
	public BTreeNode(int t, long filePos) {
		this.keys = new TreeObject[(2*t-1)];		
		for(int i=0; i<keys.length; i++) {
			keys[i] = new TreeObject(-1L, 0);
		}	
		
		this.children = new long[(2*t)];
		for(int i=0; i<children.length; i++) {
			children[i] = -1L;
		}
		
		this.isLeaf = true;
		n = 0;
		this.filePos = filePos;
	}
	
	/**
	 * Prints node information. This is mostly
	 * for troubleshooting purposes. 
	 */
	public void printNode() {
		
		System.out.println("filePos: " + filePos);
		System.out.println("isLeaf: " + isLeaf);
		System.out.println("n: " + n);
		System.out.println("keys: ");
		for(int i = 0; i < keys.length; i++) {
			System.out.print(keys[i].key + ", ");
		}
		System.out.println();
		System.out.println("children: " );
		for(int j = 0; j < children.length; j++) {
			System.out.print(children[j] + ", ");
		}
		System.out.println();
	}
	
	/**
	 * Used for cache comparisions to see if nodes are equal.
	 * 
	 * @param t node to compare to
	 * @return true if the same, false otherwise
	 */
	public boolean equals(BTreeNode t) {
		return this.filePos == t.filePos;
	}
	


}
