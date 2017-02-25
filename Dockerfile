FROM ubuntu:14.04
MAINTAINER Tarık Yılmaz

# Update Ubuntu repositories
RUN apt-get update -q

# Download and extract Apache Hadoop 2.7.3
RUN cd /tmp \
    && wget http://www-eu.apache.org/dist/hadoop/common/hadoop-2.7.3/hadoop-2.7.3.tar.gz \
    && tar -xf hadoop-2.7.3.tar.gz \
    && cd hadoop-2.7.3


# Download and extract Apache Spark 2.1.0
RUN cd /tmp \
    && wget http://d3kbcqa49mib13.cloudfront.net/spark-2.1.0-bin-hadoop2.7.tgz \
    && tar -xf spark-2.1.0-bin-hadoop2.7.tgz \
    && cd spark-2.1.0-bin-hadoop2.7

# Expose Hadoop, Spark and SSH ports
EXPOSE 22 4040 8088 50070