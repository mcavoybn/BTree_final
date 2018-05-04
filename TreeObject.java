/**
 * Object used to store gene subsequence and frequency counter.
 * 
 * @author Ben Mcavoy
 */
public class TreeObject {
	public long key;
	public int freq;
	
	/**
	 * Constructor to build object with known frequency and key.
	 * 
	 * @param key key to be stored
	 * @param freq number of occurrences of subsequence in gene file.
	 */
	public TreeObject(long key, int freq) {
		this.key = key;
		this.freq = freq;
	}
	
	/**
	 * Constructor to build object on first occurance.
	 * 
	 * @param key key to be stored
	 */
	public TreeObject(long key) {
		this.key = key;
		freq = 1;
	}
	
	/**
	 * Constructor to build empty object.
	 * Used to initialize arrays.
	 */
	public TreeObject() {
		this.key = -1L;
		freq = 0;
	}
}
