import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.InvalidParameterException;

public class BTree{
	
	private int t;
	private int seqLength;
	BTreeNode root;
	File BtreeFile;
	RandomAccessFile btreeRAF;
	Cache cache;
	
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
		
	
	public void insertNonFull(BTreeNode x, long key) {
		int i = x.n - 1;
		if(x.isLeaf) {
			while( i >= 0 && key < x.keys[i].key ) {
				x.keys[i+1] = new TreeObject(x.keys[i].key, x.keys[i].freq);
				i--;			
			}
			x.keys[i+1] = new TreeObject(key, 1);
			x.n++;
			nodeWrite(x);	
		}else {
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
	
	public void splitChild(BTreeNode x, int i, BTreeNode y) {
		//x is the parent to y
		//y is the node being split 
		//z is the new node which ~half of y's keys/children will go to
		BTreeNode z = new BTreeNode(t, getFileLength());
		z.isLeaf = y.isLeaf;
		z.n = t-1;
		diskWrite(z);
		
		for(int j=0; j<t-1; j++) {
			z.keys[j] = new TreeObject(y.keys[j+t].key, y.keys[j+t].freq);
			y.keys[j+t] = new TreeObject();
		}
		if(!y.isLeaf) {
			for(int j=0; j<t; j++) {
				z.children[j] = y.children[j+t];
				y.children[j+t] = -1L;
			}
		}
		
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
		nodeWrite(z);		
		nodeWrite(y);		
		nodeWrite(x);
	}
	
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
	
	public void print(BTreeNode root_node, boolean debug) {	
		//in-order traversal of the btree nodes
		//print all keys of the node on each traverse step
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
				System.out.print(longToSequence(cur.key, seqLength) + " ");
				System.out.print(cur.freq + " ");
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
	
	public void writeCache() {
		for (int i = cache.cacheSize(); i > 0; i--) {
			diskWrite(cache.getLast());
		}
	}
}