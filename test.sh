#GeneBankCreateBTree <cache 0/1> <degree> <gbk file> <sequence length> <cache size> [<debug level>]
#GeneBankSearch <0/1 with/without Cache> <btree file> <query file> <Cache Size> [<debug level>]

if [ "$1" = "clean" ]
then
echo "Removing all .btree and .class files..."
rm -f *.class
rm -f *.btree.*
rm -f debug

elif [ "$1" = "run" ]
then
echo "Running Tests..."
echo "" 

javac GeneBankCreateBTree.java
javac GeneBankSearch.java

#test1.gbk
java GeneBankCreateBTree 1 100 test1.gbk 1 500 1 
java GeneBankSearch 1 test1.gbk.btree.data.1.100 query1 500 0

java GeneBankCreateBTree 1 100 test1.gbk 2 500 0 
java GeneBankSearch 1 test1.gbk.btree.data.2.100 query2 500 0

java GeneBankCreateBTree 1 100 test1.gbk 3 500 
java GeneBankSearch 1 test1.gbk.btree.data.3.100 query3 1000 

java GeneBankCreateBTree 1 100 test1.gbk 3 500 
java GeneBankSearch 1 test1.gbk.btree.data.3.100 query3 1000 

java GeneBankCreateBTree 0 100 test1.gbk 3 100
java GeneBankSearch 1 test1.gbk.btree.data.3.100 query3 1000 0

java GeneBankCreateBTree 1 0 test1.gbk 16 100
java GeneBankSearch 1 test1.gbk.btree.data.16.145 query16 2 0

java GeneBankCreateBTree 1 0 test1.gbk 16 500
java GeneBankSearch 1 test1.gbk.btree.data.16.145 query16_2 40 

java GeneBankCreateBTree 1 0 test1.gbk 16 500 0
java GeneBankSearch 0 test1.gbk.btree.data.16.145 query16_2 40 

java GeneBankCreateBTree 1 100 test1.gbk 31 1000 1
java GeneBankSearch 1 test1.gbk.btree.data.31.100 query31 10000 

java GeneBankCreateBTree 1 100 test1.gbk 31 1000 1
java GeneBankSearch 1 test1.gbk.btree.data.31.100 query31 10 


# #test2.gbk
java GeneBankCreateBTree 1 100 test2.gbk 1 500
java GeneBankSearch 1 test2.gbk.btree.data.1.100 query1 500 0

java GeneBankCreateBTree 1 100 test2.gbk 2 1000
java GeneBankSearch 1 test2.gbk.btree.data.2.100 query2 500 0

java GeneBankCreateBTree 1 100 test2.gbk 3 500
java GeneBankSearch 1 test2.gbk.btree.data.3.100 query3 500 0

java GeneBankCreateBTree 1 100 test2.gbk 16 500
java GeneBankSearch 1 test2.gbk.btree.data.16.100 query16 100 

java GeneBankCreateBTree 1 100 test2.gbk 31 40
java GeneBankSearch 1 test2.gbk.btree.data.31.100 query31 2 0


# # #test3.gbk
# java GeneBankCreateBTree 1 100 test3.gbk 1 500
# java GeneBankSearch 1 test3.gbk.btree.data.1.100 query1 500 0

# java GeneBankCreateBTree 1 100 test3.gbk 2 500
# java GeneBankSearch 1 test3.gbk.btree.data.2.100 query2 500 0

# java GeneBankCreateBTree 1 100 test3.gbk 3 500
# java GeneBankSearch 1 test3.gbk.btree.data.3.100 query3 500 0

# java GeneBankCreateBTree 1 100 test3.gbk 16 500
# java GeneBankSearch 1 test3.gbk.btree.data.16.100 query16 500 0

# java GeneBankCreateBTree 1 100 test3.gbk 31 500
# java GeneBankSearch 1 test3.gbk.btree.data.31.100 query31 500 0

else
echo "Usage: ./test.sh [run | clean]"
fi