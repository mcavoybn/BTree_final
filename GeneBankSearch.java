import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;

public class GeneBankSearch {
	
	static int cacheCapacity, metaSeqLength, debugLevel, cacheSize;
	static String btreeFileName, queryFileName, metadataFileName;
	static long searchedKey;
	static Cache cache;
	static BTree bt;

	public static void main(String args[]){

		parseArgs(args);
		
		try {		
			BTree bt = new BTree(new File(btreeFileName), new File(metadataFileName), cache);

			if(debugLevel == 0){
				System.out.println("Btree File:" + btreeFileName);
				System.out.println("Metadata File:" + metadataFileName);
				System.out.println("Query File:" + queryFileName);
				if(debugLevel == 1) System.out.println("Debug Level : " + debugLevel);
				System.out.println();
			}

			Scanner queryScanner = new Scanner(new File(queryFileName));
			String curLine = "";
			do{				
				curLine = queryScanner.nextLine();
				long k = bt.sequenceToLong(curLine);
				BTreeNode searchKey = bt.search(bt.root, k);
				
				if(searchKey == null) return;
				for(int i = 0; i < searchKey.keys.length; i++){
					if(searchKey.keys[i].key == k && debugLevel == 0){
						System.out.println(bt.longToSequence(k, metaSeqLength) + " " + searchKey.keys[i].freq);
					}
				}				
			}while(queryScanner.hasNextLine());	

		}catch(FileNotFoundException e){
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	//GeneBankSearch <0/1 with/without Cache> <btree file> <query file> <Cache Size> [<debug level>]
	public static void parseArgs(String args[]){		
		if(args.length == 5){
			if(debugLevel != 0) printUsage();
		}else{
			debugLevel = -1;
		}

		if(args[0].equals("1")){
			cacheSize = Integer.parseInt(args[3]);
			cache = new Cache(cacheCapacity);
		}else {
			cache = null;
		}
		btreeFileName = args[1];
		queryFileName = args[2];
		metadataFileName = btreeFileName.replace("data", "metadata");
		metaSeqLength = Integer.parseInt(metadataFileName.split("\\.")[4]);

		if(args.length > 5 || args.length < 3)	
			printUsage();
	}

	private static void printUsage(){
		System.err.println("Usage: Java GeneBankSearch "
				+ "<0/1(no/with Cache)> <Btree File>"
				+ " <Query File> <Cache Size> [<Debug level>]");
		System.exit(1);
	}

}