#!/bin/sh
JMX_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=1203 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
JVM_OPTS="-Xms512m -Xmx512m -XX:PermSize=126m -XX:MaxPermSize=126m"
export MAVEN_OPTS="$JVM_OPTS"

mvn test -Dtest=NapoliNormalQueueTest -DforkMode=never -Dmaven.test.jvmargs='-Xms512m -Xmx512m -XX:PermSize=126m -XX:MaxPermSize=126m -DrequestCount=100000' 2>&1 > aa.txt &
tail -f aa.txt
#mvn -o  exec:java -Dexec.mainClass=NapoliNormalQueueTest -DrequestCount=100000

#-Denforcer.skip
                        