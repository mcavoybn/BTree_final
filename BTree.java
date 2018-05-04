import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.InvalidParameterException;

/**
 *  a B-tree is a self-balancing tree data structure that keeps data sorted and allows searches, 
 *  sequential access, insertions, and deletions in logarithmic time. In this case there is no
 *  delete method because we are only interested in building and searching the tree.
 *  
 * 	This BTree stores gene subsequences. Each sequence is stored in a TreeObject.
 * 
 *  @author Ben McAvoy, Ben Peterson
 */
public class BTree{
	
	private int t;
	private int seqLength;
	BTreeNode root;
	File BtreeFile;
	RandomAccessFile btreeRAF;
	Cache cache;
	
	/**
	 * Constructor for creating a new BTree. Creates the files to store the BTree and its
	 * meta data.
	 * 
	 * @param t degree of BTree
	 * @param k subsequence length 
	 * @param gbk filename for the gbk file
	 * @param cache cache to use to limit disk read write. Null if no cache present.
	 * @throws IOException if gbk file is not accessible
	 */
	public BTree(int t, int k, String gbk, Cache cache) throws IOException{
		this.t = t;
		this.seqLength = k;
		this.cache = cache;
		File metadata = new File(gbk + ".btree.metadata." + k + "." + t);
		
		btreeRAF = new RandomAccessFile(metadata, "rw");
		btreeRAF.writeInt(t); //write tree degree to metadata file
		btreeRAF.writeInt(k); //write sequence length to metadata file
		btreeRAF.close();

		root = new BTreeNode(t,0);
		BtreeFile = new File(gbk + ".btree.data." + k + "." + t);			
		diskWrite(root);
	}
	
	/**
	 * Constructor used for searching an exsisting BTree. Loads the BTree
	 * from the passed in files. 
	 * 
	 * @param BtreeFile file containing the BTree
	 * @param metadata file containing BTree meta data
	 * @param cache cache to use to limit disk read write. Null if no cache present.
	 * @throws IOException if BTree or meta data file are not acessible.
	 */
	public BTree(File BtreeFile, File metadata, Cache cache) throws IOException {	
		btreeRAF = new RandomAccessFile(metadata, "r");
		this.t = btreeRAF.readInt(); //read in degree in terms of t
		this.seqLength = btreeRAF.readInt(); //sequence length (k) 
		btreeRAF.close();
		
		this.BtreeFile = BtreeFile;
		btreeRAF = new RandomAccessFile(BtreeFile, "r");
		root = diskRead(0);		
		btreeRAF.close();
		
		this.cache = cache;
	}
	
	/**
	 * Inserts a new key into the tree. If the key 
	 * is already present it increments the frequency 
	 * counter for that key.
	 * 
	 * @param key key to be added to the BTree
	 */
	public void insert(long key)  {
		//check if key is already inserted
		BTreeNode duplicate = search(root, key);
		if(duplicate != null) {
			for(int i=0; i<duplicate.keys.length; i++) {
				if(duplicate.keys[i].key == key) {
					duplicate.keys[i].freq++;
					nodeWrite(duplicate);
					return;
				}
			}
		}
		
		//key does not exsist add it to the BTree
		BTreeNode r = this.root;
		if(r.n == 2*t-1) {
			BTreeNode newNode = new BTreeNode(t, getFileLength());	
			diskWrite(newNode);	
			this.root.filePos = getFileLength();
			diskWrite(root);
			this.root = newNode;
			newNode.isLeaf = false;
			newNode.n = 0;
			newNode.children[0] = r.filePos;
			newNode.filePos = 0;
			splitChild(newNode,0,r);
			insertNonFull(newNode,key);
		}else {
			insertNonFull(root,key);
		}
	}
		
	/**
	 * Inserts the key into a node. Will search the tree 
	 * for the proper node to place it in and create new 
	 * nodes as needed.
	 * 
	 * @param x node to start search from
	 * @param key key to be inserted into the BTree
	 */
	public void insertNonFull(BTreeNode x, long key) {
		int i = x.n - 1;
		//check for leaf since keys can only be inserted in leaves.
		if(x.isLeaf) {
			while( i >= 0 && key < x.keys[i].key ) {
				x.keys[i+1] = new TreeObject(x.keys[i].key, x.keys[i].freq);
				i--;			
			}
			x.keys[i+1] = new TreeObject(key, 1);
			x.n++;
			nodeWrite(x);	
		}else { //not a leaf, find the correct place to insert.
			while( i >= 0 && key < x.keys[i].key) {
				i--;
			}
			i++;
			BTreeNode c;
			if(x.children[i] != -1) {
				c = diskRead(x.children[i]);
				if( c.n == 2*t-1 ) {
					splitChild(x, i, c);
					if( key > x.keys[i].key){
						i++;
					}
				}
				insertNonFull(diskRead(x.children[i]), key);
			}			
		}
	}
	
	/**
	 * Finds a key in the nodes starting with the passed in node.
	 * This will continue to look at children until it has no more nodes.
	 * 
	 * @param x node to start search at
	 * @param key key to locate in BTree
	 * @return null if not found, otherwise returns node containing key.
	 */
	public BTreeNode search(BTreeNode x, long key) {
		int i = 0;
		BTreeNode retNode = null;
		while(i < x.n && key > x.keys[i].key) {
			i++;
		}
		if(i < x.n && key == x.keys[i].key) {
			return x;
		}
		if(x.isLeaf) {
			return null;
		}
		if(x.children[i]!=-1) {
			retNode = diskRead(x.children[i]);
		}				
		return search(retNode,key);
	}
	
	/**
	 * Splits a full node into two seperate nodes and modifies
	 * the parent to point to the two new nodes. 
	 * 
	 * @param x The parent node of y
	 * @param i location of y in parent node array
	 * @param y The node being split
	 */
	public void splitChild(BTreeNode x, int i, BTreeNode y) {
		//x is the parent to y
		//y is the node being split 
		//z is the new node which ~half of y's keys/children will go to
		BTreeNode z = new BTreeNode(t, getFileLength());
		z.isLeaf = y.isLeaf;
		z.n = t-1;
		diskWrite(z);
		
		//copy keys from y to z
		for(int j=0; j<t-1; j++) {
			//need to create new TreeObjects to avoid passing by reference 
			z.keys[j] = new TreeObject(y.keys[j+t].key, y.keys[j+t].freq);
			y.keys[j+t] = new TreeObject();
		}
		if(!y.isLeaf) {
			for(int j=0; j<t; j++) {
				z.children[j] = y.children[j+t];
				y.children[j+t] = -1L;
			}
		}
		
		//move children to correct location
		y.n = t-1;
		for(int j=x.n; j>i; j--) {
			x.children[j+1] = x.children[j];
			x.children[j] = -1L;
		}
		x.children[i+1] = z.filePos;
		for(int j=x.n-1; j>i-1; j--) {
			x.keys[j+1] = new TreeObject(x.keys[j].key, x.keys[j].freq);
		}
		x.keys[i] = new TreeObject(y.keys[t-1].key, y.keys[t-1].freq);
		y.keys[t-1] = new TreeObject();
		x.n = x.n + 1;
		//write changes to nodes
		nodeWrite(z);		
		nodeWrite(y);		
		nodeWrite(x);
	}
	
	/**
	 * Converts a gene sequence into a numerica reresentation. This 
	 * allows for easy storage of sequences in a small space.
	 * 
	 * @param s String to be converted
	 * @return numeric representation of String
	 */
	public long sequenceToLong(String s) {
		if( s.length() > 31 ) throw new InvalidParameterException("stringToLong() string param must be 31 chars long !");
		Long retVal = 0L;
		for( int i=0; i<s.length(); i++ ) {
			int cur = 0;
			switch( s.substring(i, i+1) ){
				case "A": cur = 0; break;
				case "C": cur = 1; break;
				case "T": cur = 3; break;
				case "G": cur = 2; break;
			};
			if(i==0) retVal += cur;
			else retVal += cur * (long)Math.pow(4,i);
		}
		return retVal;
	}
	
	/**
	 * Turns a numeric reresentation of a gene subsequence into
	 * its String representation.
	 * 
	 * @param key subsequence to be turned into a string
	 * @param subsequenceLength how long the String should be
	 * @return String version of the subsequence
	 */
	public String longToSequence(long key, int subsequenceLength) {
		String retString = "";
		for(int i=0; i < subsequenceLength; i++){
			String cur = "";
			switch((int)(key % 4)) {
				case 1: cur = "C"; break;
				case 2: cur = "G"; break;
				case 3: cur = "T"; break;
				case 0: cur = "A"; break;
			}
			retString += cur;
			key = key >> 2;
		}
		return retString;
	}
	
	/**
	 * Writes a node to cache or disk depending if cache is present.
	 * 
	 * @param node node to write to cache/disk
	 */
	public void nodeWrite(BTreeNode node) {
		if (cache != null) {
			//add node to the cache, if the cache is full addObject will return the
			//last element in the cache. When the last element is returned, write it to disk
			//so that its data is updated appropriately
			BTreeNode checkNode = cache.addObject(node);
			if (checkNode != null) {
				diskWrite(checkNode);
			}
		}
		else {
			diskWrite(node);
		}
		
	}
	
	/**
	 * Writes a node to the BTree file.
	 * 
	 * @param node node to be written to disk
	 */
	public void diskWrite(BTreeNode node) {
		try {
			btreeRAF = new RandomAccessFile(BtreeFile, "rw");
			btreeRAF.seek(node.filePos);
			for (int i = 0; i < node.keys.length; i++) {
				btreeRAF.writeLong(node.keys[i].key);
				btreeRAF.writeInt(node.keys[i].freq);
			}
			for (int i = 0; i < node.children.length; i++) {
				btreeRAF.writeLong(node.children[i]);
			}
			btreeRAF.writeInt(node.n);
			btreeRAF.writeBoolean(node.isLeaf);
			btreeRAF.writeLong(node.filePos);
			btreeRAF.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets a BTree node from the cache or disk. Gets from disk
	 * if cache is not enabled, or if the node is not found in the
	 * cache.
	 * 
	 * @param filePos location of BTreeNode on disk
	 * @return node at requested file position
	 */
	public BTreeNode diskRead(long filePos) {		
		//search the cache for the node with the given filePos
		//if it is found in the cache, return it instead of reading from disk
		BTreeNode checkCache = null;
		if (cache != null) {
			checkCache = cache.getObject(filePos);
		}
		if (checkCache != null)
		{
			return checkCache;
		}
		
		BTreeNode node = new BTreeNode(t,filePos);
		try {
			btreeRAF = new RandomAccessFile(BtreeFile, "r");
			btreeRAF.seek(filePos);
			for (int i = 0; i < node.keys.length; i++) {
				node.keys[i].key = btreeRAF.readLong();
				node.keys[i].freq = btreeRAF.readInt();
			}
			for (int i = 0; i < node.children.length; i++) {
				node.children[i] = btreeRAF.readLong();
			}
			node.n = btreeRAF.readInt();
			node.isLeaf = btreeRAF.readBoolean();
			node.filePos = btreeRAF.readLong();
			btreeRAF.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return node;
	}
	
	/**
	 * In-order traversal of the btree nodes.
	 * Print all keys of the node on each traverse step.
	 * 
	 * @param root_node node to start the traversal at.
	 * @param debug allows aditional print functions for troubleshooting
	 */
	public void print(BTreeNode root_node, boolean debug) {	
		int i;
		for(i=0; i < 2*t-1; i++) {
			if (!root_node.isLeaf) {
				if(root_node.children[i] != -1L) {
					BTreeNode n = diskRead(root_node.children[i]);
					print(n, debug);
				}
			}
			TreeObject cur = root_node.keys[i];
			if(cur.key != -1) {
				System.out.print(cur.freq + " ");
				System.out.print(longToSequence(cur.key, seqLength));
				System.out.println();
			}
		}
		
		if (!root_node.isLeaf) {
			if(root_node.children[i] != -1L) {
				BTreeNode n = diskRead(root_node.children[i]);
				print(n, debug);
			}
		}		
	}
	
	/**
	 * Gets the current length of the BTree file. This is
	 * needed when new nodes are added to insure they are written
	 * to end of file.
	 * 
	 * @return current file length
	 */
	private long getFileLength() {
		long fileLength = -1L;
		try {
			btreeRAF = new RandomAccessFile(BtreeFile, "r");
			fileLength = btreeRAF.length();
			btreeRAF.close();
		} catch (IOException e) {
			System.out.println("Error accessing file");
			e.printStackTrace();
		}	
		return fileLength;
	}
	
	/**
	 * Writes the cache to disk to save all node changes.
	 */
	public void writeCache() {
		for (int i = cache.cacheSize(); i > 0; i--) {
			diskWrite(cache.getLast());
		}
	}
}