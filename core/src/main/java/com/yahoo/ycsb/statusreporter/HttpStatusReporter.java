package com.yahoo.ycsb.statusreporter;

//import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.regex.Pattern;


/**
 * Direct status information to a remote HTTP listener.
 * Enable this plugin by defining YCSB parameter statusreporter=com.yahoo.ycsb.statusreporter.HttpStatusReporter
 * YCSB param statusreporter.httpurl defines the endpoint to which the status message will be posted; if that URL
 * includes the string "%c", that string gets replaced with the value of the "clientid" param.
 * The output HTTP message has no body, but includes a list of http parameters identifying the clientid, interval
 * and statistics for the current test interval.  
 * @author dhentchel
 */
public class HttpStatusReporter implements StatusReporter {
    String _clientid;
    String _httpUrl;
    
    public boolean configure(Properties props) {
        _clientid = props.getProperty("clientid");
        _httpUrl = props.getProperty("statusreporter.httpurl").replaceAll("%c", _clientid);
        return true;
    }

    /**
     * Post a list of HTTP parameters to the configured URL.
     * Passes statistics as URL parameters, i.e. <_httpUrl>?parm1=value&parm2=value...
     * Where parms always include client, interval, totalops and tps;
     * and may include latency measures for read, update, insert, delete or scan.
     */
    public void report(long interval, long totalops, double curthroughput, String measures) {
        String urlParameters = "client=" + _clientid + "&interval=" + interval + "&totalops=" + totalops + "&tps=" + curthroughput;
                                
       String[] parts = Pattern.compile("]|\\[").split(measures.trim());
        for (String m : parts)
        {
            if (m.contains("="))
            {
                String[] vals = Pattern.compile("\\s|=").split(m.trim());
                if (vals.length == 3)
                {
                    urlParameters = urlParameters + "&" + vals[0].toLowerCase() + "=" + vals[2];
                }
            }
        }
//        System.out.println("URL Params:"+urlParameters); 

        try {
            URL url = new URL(_httpUrl); 
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();           
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(true); 
            connection.setRequestMethod("POST"); 
            connection.setUseCaches (true);
            connection.setRequestProperty("Content-length", String.valueOf(urlParameters.length())); 
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded"); 
            // connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0;Windows98;DigExt)"); 

            DataOutputStream outstream = new DataOutputStream(connection.getOutputStream ());
            outstream.writeBytes(urlParameters);
            outstream.flush();
            outstream.close();
            
            int respCode = connection.getResponseCode();
            if (respCode != 200)
            {
                System.err.println("Response Code:" + respCode); 
                System.out.println("Response Message:" + connection.getResponseMessage()); 
//            DataInputStream input = new DataInputStream(connection.getInputStream() ); 
//            for( int c = input.read(); c != -1; c = input.read() ) 
//                System.out.print( (char)c ); 
//                input.close(); 
            }
            connection.disconnect();
        } catch (IOException e) {
            System.err.println("HttpStatusReporter: Failed to open connection for " + _httpUrl);
            e.printStackTrace();
        }
    }

    public void close() {
        return;
    }
}
