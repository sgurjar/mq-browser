command line mq browser
=======================

    $ mqb -help
    usage: mqb [options]
    where options are:
        -b,--browse <msgs>  browse <msgs> number of messages from queue
        -h,--host <mqhost>  mq host, host[:port]@channel[:qmgr]
        -help,--help        display this message
        -q,--queue <qname>  queue name, use multiple options to pass multiple queues
    If you do not specify any filename or qname, reads qnames from standard input.

jar files to compile
--------------------
    com.ibm.mq.jar
    com.ibm.mqjms.jar
    jms.jar
    commons-cli-1.2.jar

jar files to run
--------------------
    com.ibm.mq.jar
    com.ibm.mqjms.jar
    commons-cli-1.2.jar
    connector.jar
    dhbcore.jar
    jms.jar
    jta.jar
