****************
* BTree
* CS321
* 5/5/2018
* Ben Mcavoy, Nick Figura, Ben Peterson 
**************** 

OVERVIEW:

Creates a BTree based on gene sequences from a gbk (gene bank file) file. The BTree can then be searched for occurrences of 
specific sequences from the BTree. 
 
INCLUDED FILES:
Source Files:
* BTree.java - The main class for the BTRee. It can construct and search a BTree.
* BtreeNode.java - Node that is stored in the BTree. Each Node holds multiple sequences and children.
* Cache.java - A BTree specific cache that is used to reduce the number of disk reads and writes while
creating and searching the BTree.
* GeneBankCreateBTree.java - Creates a Btree based on a gbk file. See the compiling and running section of this document 
for more details on its operation.
* GeneBankSearch.java - Searches a BTree for certain sequences from a query file. See the compiling and running section 
of this document for more details on its operation.
* TreeObject.java - Stores the gene sequence and the frequence of the gene sequence. These are stored in the BTreeNode 
class. 
 Other Files:
 * README - this file


COMPILING AND RUNNING:

Place all of the above listed source files in the same directory. Run the following
command for the command line to compile:
 $ javac *.java

Execute the following command to run the GeneBankCreateBTree.java:
 $ java  GeneBankCreateBTree <cache 0/1> <degree> <gbk file> <sequence length> <cache size> [<debug level>]

cache: 0 for no cache or 1 to use a cache
degree: Degree of Btree, 0 will default to a block size of 4096
gbk file: file with sequences saved to it
sequence length: length of subsequences allowed values are 1-31
Cache Size: If cache is enabled this will be the size desired by the user
Debug Level (optional): 0 for helpful diagnostics, 1 to dump information to a file. Defaults to 0. 
 
The result of this program will be 2 files, a meta data file and a file containing the BTree. The files will be named:

BTree file: gbkFilename.btree.data.sequenceLength.degree (ex: test2.gbk.btree.data.5.5)
Meta data file:  gbkFilename.btree.metadata.sequenceLength.degree (ex: test2.gbk.btree.metadata.5.5)

 
Execute the following command to run the GeneBankSearch.java: 
$ java Java GeneBankSearch <0/1(no/with Cache)> <Btree File> <Query File> <Cache Size> [<Debug level>]
 
cache: 0 for no cache or 1 to use a cache
degree: Degree of Btree, 0 will default to a block size of 4096
BTree File: The file created by the GeneBankCreateBTree program. Tree metadata file created by GeneBankCreateBTree
must also be present.
Cache Size: If cache is enabled this will be the size desired by the user
Debug level (optional): 0 will enable debugging output. The default is zero.

This will print each query string that was found and how often it appears in the sequence.

 
PROGRAM DESIGN:

There were a few major design choices that we decided on while creating the program:

1. Division of work

We split up the work in the following manner: 

Ben Peterson: Sequence Spliting, Cache, Printing, and disk read and write
Ben Mcavoy: Btree, Btree Node, and TreeObject
Nick Figura: GeneBankSearch, GeneBankCreateTree, and sequence read from file

This division of work seemed to work pretty well. The parts were separate enough that we could do some 
independent work before bringing it all together. Each person was able to work out all of the runtime 
errors before we started working on the code as a whole. We then began to test for logical errors in 
the fully assembled program. This method worked well and allowed each of us to contribute to the program.

2. File Storage

We decided to create two files for each BTree. One file would contain all of the meta data and the other file
would contain the BTree. We did this to make it easier to read and write the BTree itself. This also allowed us
to read in the BTree attributes before opening up the BTree file. This did cuase a bit more work in the search 
program but it was not enough to stop us from creating the two files.  

3. Reading in the DNA strings

We decided to read in each sequence as one long string using a string builder. We also used a bufferInputReader to 
minimize the reads from the disk. We then divided up the subsequences from the string. We also just decided to throw 
away any sequences that contained an N. This did cause the program to waste a few cycles, but it was not enough 
overhead for us to change it. Overall this approach worked well and was easy to pass into the BTree.

4. Cache 

We decided to take the cache program from the cache assignment earlier in the year. When we first tried to use it,
it did not work well. So the program was simplified to just perform the operations needed for a BTReeNode cache. 
After this, it was much easier to use and we were able to add the cache option to reading and writing the nodes.


DESCRIPTION OF THE FILE LAYOUT ON DISK:

The BTree is written to the hard disk as a binary file which contains a series of nodes. Each node is inserted into the file in the order that it was created with one exception. The root node is always at file position 0. This is makes it convenient to locate the root node on the .btree.data file created by GeneBankCreateBTree. 

Each node is written as a series of the fields which it contains. For each field which is an array, its elements will be written sequentially as well. Here is an example with a simple node:

Node:
filePos = 0 (long; 8 bytes) 
isLeaf = false(boolean; 4 bytes)
n = 3 (int; 4 bytes)
keys[] = [{key:1, freq:1}, {key:2, freq:1}, {key:3, freq:1}] (4*long + 4*int; 48 bytes)
children[] = [ 1, 2, 3, 4] (4*long; 32 bytes)

Will be written as (in hexadecimal):
(00 00 00 00 00 00 00 00)
(00 00 00 00 00)
(00 00 00 00 03)
[(00 00 00 00 00 00 00 01) (00 00 00 01)
 (00 00 00 00 00 00 00 10) (00 00 00 01)
 (00 00 00 00 00 00 00 11) (00 00 00 01)]
[(00 00 00 00 00 00 00 01)
 (00 00 00 00 00 00 00 10)
 (00 00 00 00 00 00 00 11)
 (00 00 00 00 00 00 01 00)]
 
 ...

Next would follow another node with the same number of bytes but with a file position at the byte following the last byte of this node (and different key/children data appropriate for a BTree).

EFFECTS OF CACHE SIZE ON RUNTIME:

GeneBankCreateBTree (test run with test2.gbk):
Cache Size | Subsequence Length | Degree | Runtime
100				5					5		4.2 sec
500				5					5		1.5 sec
No Cache		5					5		27  sec
100				16					16	    16.7 sec
500				16					16	    2.2 sec
No Cache		16					16		1:16 min				
100             31					31		8.3 sec
500				31					31		2.1 sec
No Cache		31					31		2:04 min

As seen from the results above, turning on the cache makes the process run much
quicker. The size of the cache also improves the runtime. You need to be careful
with the size of cache as to large of a cache could cause an overflow. You gain more
benefit from turning on the cache than making it very large. A cache size of 500 works
very well for our needs.
 
GeneBankSearch:
This program ran very quickly (most of the time sub 1 second) no matter what we picked for
the size of the cache. Just turning on the cache was a good improvement for the rumtime.
