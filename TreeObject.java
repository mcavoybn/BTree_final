public class TreeObject {
	public long key;
	public int freq;
	
	public TreeObject(long key, int freq) {
		this.key = key;
		this.freq = freq;
	}
	
	public TreeObject(long key) {
		this.key = key;
		freq = 1;
	}
	
	public TreeObject() {
		this.key = -1L;
		freq = 0;
	}
}
