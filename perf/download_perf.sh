#! /bin/sh

# Define JMeter home and download test home
JMETER_HOME=$1
DOWNLOAD_TEST_HOME=$PWD

DOWNLOAD_JMETER_TEST=$DOWNLOAD_TEST_HOME/file_jmeter.jmx
DOWNLOAD_SERVER_HOST=$2
DOWNLOAD_SERVER_PORT=8080
FILE_LIST=$DOWNLOAD_TEST_HOME/file_list.csv
REPORT_RESPONSE_TIME_PATH=$DOWNLOAD_TEST_HOME/report/report_response_time.jtl
REPORT_AGGREGATE_PATH=$DOWNLOAD_TEST_HOME/report/report_aggregate.jtl
# Define download context: asyncFile, asyncByteArray, stream, byteArray, byteArrayVirtual
DOWNLOAD_CONTEXT=$3

echo "Starting JMeter test - download context: $DOWNLOAD_CONTEXT"

$JMETER_HOME/bin/jmeter.sh -n -t $DOWNLOAD_JMETER_TEST \
 -Jhost=$DOWNLOAD_SERVER_HOST \
 -Jport=$DOWNLOAD_SERVER_PORT \
 -Jfile_list=$FILE_LIST \
 -Jreport_response_time_path=$REPORT_RESPONSE_TIME_PATH \
 -Jreport_aggreage_path=$REPORT_AGGREGATE_PATH \
 -Jdownload_context=$DOWNLOAD_CONTEXT
