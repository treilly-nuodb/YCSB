package com.yahoo.ycsb.statusreporter;

import java.util.Properties;

public interface StatusReporter
{
    /** Set up output stream for outbound status messages */
    boolean configure(Properties props);
    
    /**
     * Report statistics for current interval.
     * 
     * @param interval - current time interval in seconds, from start of test
     * @param totalops - total operations performed by the client to that point
     * @param curthroughput - current throughput (transactions per second) for this interval
     * @param measurements - output from Measurements.getMeasurements().getSummary() method; includes latency for any YCSB operations 
     *   (read, update, insert, delete, scan) that occur in this interval
     */
    void report(long interval, int totalops, double curthroughput, String measurements);
    
    /**
     * Clean up any resources and connections used by the reporter.
     */
    void close();
}

