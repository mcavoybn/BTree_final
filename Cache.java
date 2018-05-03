import java.util.LinkedList;

/**
 * Implements a 1 level cache. 
 * The cache stores btree nodes in a linked list. 
 * Each node represents one open space in the cache. 
 *  
 * @author Ben Peterson
 */
public class Cache { 
	//cache
	private LinkedList<BTreeNode> cache1;
	//cache size
	private final int CACHE_MAX_SIZE; 
	
	/**
	 * Constructor for one level cache.
	 * 
	 * @param cache1Size Size for 1st-level cache
	 */
	public Cache(int cache1Size) {
		//initialize class variables
		cache1 = new LinkedList<BTreeNode>();
		this.CACHE_MAX_SIZE = cache1Size;

	}//end of Cache
		
	/**
	 * Adds an object to the cache if it does not
	 * already exist. Moves object to front of the
	 * cache if it does exist.
	 * 
	 * @param toAdd object to be added to the cache
	 */
	public BTreeNode addObject(BTreeNode toAdd) {
		BTreeNode returnNode = null;
		if (isFull()) {
			 returnNode = cache1.removeLast();
		}
		BTreeNode moveToFront = getObject(toAdd);
		if (moveToFront == null){
			cache1.addFirst(toAdd);
		}
		else { //already in cache move to front
			cache1.addFirst(moveToFront);
		}
		return returnNode;
	}//end of addObject
	
	/**
	 * Looks for BTreeNode in cache and returns it if found. 
	 * 
	 * @param toGet object to check cache for
	 * @return  BTreeNode if found, null if not found
	 */
	public BTreeNode getObject(BTreeNode toGet) {
		//look for object in cache and return it if found
		for (int i = 0; i < cache1.size(); i++) {
			if (cache1.get(i).equals(toGet)) {

				return cache1.remove(i);
			}
		}
		//not found
		return null;
	}//end of getObject
	
	/**
	 * Looks for BTreeNode by file offset in cache and returns it if found. 
	 * 
	 * @param toGet object to check cache for
	 * @return  BTreeNode if found, null if not found
	 */
	public BTreeNode getObject(long fileOffset) {
		//look for object in cache and return it if found
		for (int i = 0; i < cache1.size(); i++) {
			if (cache1.get(i).filePos == fileOffset) {
				BTreeNode toReturn = cache1.remove(i);
				cache1.addFirst(toReturn);
				return toReturn;
			}
		}
		//not found
		return null;
	}//end of getObject
	
	/**
	 * Returns last node in the cache.
	 * 
	 * @return last node in cache
	 */
	public BTreeNode getLast () {
		return cache1.removeLast();
	}


	/**
	 * Checks if cache is full
	 * 
	 * @return true if full false otherwise
	 */
	public boolean isFull() {
		return cache1.size() == CACHE_MAX_SIZE;
	}
	
	/**
	 * Gets cache size
	 * 
	 * @return cacheSize
	 */
	public int cacheSize() {
		return cache1.size();
	}
}//end of class Cache