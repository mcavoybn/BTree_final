import java.security.InvalidParameterException;

public class DNAParser {
	
	public DNAParser() {
		
	}
	
	public long sequenceToLong(String s) {
		if( s.length() > 31 ) 
			throw new InvalidParameterException("stringToLong() string param must be 31 chars long !");
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

}
