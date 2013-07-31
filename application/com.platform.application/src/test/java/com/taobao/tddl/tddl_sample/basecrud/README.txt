basecrud 

是用于基本的crud样例。
最为基本的样例

场景为

modDBTab表 分了2个库，每个库2表。 db1 db2 db3 db4 .表是从modDBTab_0000开始到modDBTab_0003
modTab 表 分了4个表，在db1数据库上，表名分别为modDB_0000~modDB_0003
gmtTab 分了7张表，在db1数据库上，表名分别为gmtTab_0000~gmtTab_0006


需要注意的是
因为主备实际上只是同一个数据库的不同schema。所以没有办法设置为自动复制。
如果没有制定DBIndex的场景下，有可能会出现有时候能查到数据，有时候不能的情况，这是因为TDDL group datasource
进行了自动的读分离（这种情况在主备场景中也会可能出现，因为主库到备库的数据复制是需要一定时间的，有一定的延迟).

在样例中也可以看到如何控制TDDL 进行实时一致性的读取。

appName = tddl_sample

用户名密码都是tddl

10.13.1.2
port=3306
dbName=tddl_sample_0
dbType=mysql
dbStatus=RW

10.13.1.2
port=3306
dbName=tddl_sample_0_bac
dbType=mysql
dbStatus=RW

10.13.1.2
port=3306
dbName=tddl_sample_1
dbType=mysql
dbStatus=RW

10.13.1.2
port=3306
dbName=tddl_sample_1_bac
dbType=mysql
dbStatus=RW
