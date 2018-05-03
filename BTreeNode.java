public class BTreeNode {
	
	public TreeObject[] keys;
	public long[] children;
	public boolean isLeaf;
	public int n; //current number of keys
	public long filePos; //position of node in file
	
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
	
	public boolean equals(BTreeNode t) {
		return this.filePos == t.filePos;
	}
	


}
