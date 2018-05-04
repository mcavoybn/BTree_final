import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;

/**
 * Searches a provides gene sequence file for a certain set of sequences. 
 * The sequences are then prnted to the console if found.
 * 
 * @author Ben Mcavoy, Nick Figura
 */
public class GeneBankSearch {
	
	static int cacheCapacity, metaSeqLength, debugLevel, cacheSize;
	static String btreeFileName, queryFileName, metadataFileName;
	static long searchedKey;
	static Cache cache;
	static BTree bt;

	/**
	 * Main entery point for GeneBankSearch. Command line arguments are:
	 * GeneBankSearch <0/1 with/without Cache> <btree file> <query file> <Cache Size> [<debug level>]
	 *
	 * @param args command line arguements
	 */
	public static void main(String args[]){

		parseArgs(args);
		//setup BTree for search
		try {		
			BTree bt = new BTree(new File(btreeFileName), new File(metadataFileName), cache);

			if(debugLevel == 0){
				System.out.println("Btree File:" + btreeFileName);
				System.out.println("Metadata File:" + metadataFileName);
				System.out.println("Query File:" + queryFileName);
				if(debugLevel == 1) System.out.println("Debug Level : " + debugLevel);
				System.out.println();
			}
			//search for gene subsequences
			Scanner queryScanner = new Scanner(new File(queryFileName));
			String curLine = "";
			do{				
				curLine = queryScanner.nextLine();
				long k = bt.sequenceToLong(curLine);
				BTreeNode searchKey = bt.search(bt.root, k);
				
				if(searchKey == null) return;
				for(int i = 0; i < searchKey.keys.length; i++){
					if(searchKey.keys[i].key == k){
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
	/**
	 * Stores the arguments from the command line. Also verifies that 
	 * the corret arguments were passed.
	 * 
	 * @param args arguments from the cmmand line.
	 */
	public static void parseArgs(String args[]){		
                debugLevel = 0;

                try {
                        if(args.length > 5 || args.length < 4)
                                printUsage();

                        if(args.length == 5 && Integer.parseInt(args[4]) != 0)
                                printUsage();


                        if(args[0].equals("1")){
                                cacheSize = Integer.parseInt(args[3]);
                                cache = new Cache(cacheCapacity);
                        }else if(args[0].equals("0")){
                                cache = null;
                        }else{
                                printUsage();
                        }

                        btreeFileName = args[1];
                        queryFileName = args[2];
                        metadataFileName = btreeFileName.replace("data", "metadata");
                        metaSeqLength = Integer.parseInt(metadataFileName.split("\\.")[4]);
                 }catch(NumberFormatException e){
                        printUsage();
                }
        }

	}
	
	/**
	 * Usage message to be printed if correct arguements are not passed. 
	 */
	private static void printUsage(){
		System.err.println("Usage: Java GeneBankSearch "
				+ "<0/1(no/with Cache)> <Btree File>"
				+ " <Query File> <Cache Size> [<Debug level>]");
		System.exit(1);
	}

}
