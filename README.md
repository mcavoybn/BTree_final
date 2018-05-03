TODO: Describe the layout of the B-Tree file on disk
TODO: Report on how using cache sizes 100 and 500 affects the performance of the program

****************
* BTree
* CS321
* 5/5/2018
* Ben McAvoy, Nick Figura, Ben Peterson 
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

<cache>: 0 for no cache or 1 to use a cache
<degree>: Degree of Btree, 0 will default to a block size of 4096
<gbk file>: file with sequences saved to it
<sequence length>: length of subsequences allowed values are 1-31
<Cache Size>: If cache is enabled this will be the size desired by the user
[<Debug Level>] (optional): 0 for helpful diagnostics, 1 to dump information to a file. Defaults to 0. 
 
The result of this program will be 2 files, a meta data file and a file containing the BTree. The files will be named:

BTree file: gbkFilename.btree.data.sequenceLength.degree (ex: test2.gbk.btree.data.5.5)
Meta data file:  gbkFilename.btree.metadata.sequenceLength.degree (ex: test2.gbk.btree.metadata.5.5)

 
Execute the following command to run the GeneBankSearch.java: 
 $ java Java GeneBankSearch <0/1(no/with Cache)> <Btree File> <Query File> <Cache Size> [<Debug level>]
 
<cache>: 0 for no cache or 1 to use a cache
<degree>: Degree of Btree, 0 will default to a block size of 4096
<BTree File>: The file created by the GeneBankCreateBTree program. Tree metadata file created by GeneBankCreateBTree
must also be present.
<Cache Size>: If cache is enabled this will be the size desired by the user
[<Debug level>] (optional): 0 will enable debugging output. The default is zero.

This will 

 
PROGRAM DESIGN:


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




