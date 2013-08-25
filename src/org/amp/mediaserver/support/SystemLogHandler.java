package org.amp.mediaserver.support;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/*
 * Based on SystemOutLoggingHandler from the seamless-util package.
 */
public class SystemLogHandler extends StreamHandler {

	public SystemLogHandler() {
		super(System.out, new SimpleFormatter());
		setLevel(Level.ALL);
	}
	
	public void close() {
        flush();
    }
 
    public void publish(LogRecord record) {
    	if(!isLoggable(record)) {
    		return;
    	}
        super.publish(record);
        flush();
    }
	
	public static class SimpleFormatter extends Formatter {
        public String format(LogRecord record) {
            StringBuffer buf = new StringBuffer(180);
            
            DateFormat dateFormat = new SimpleDateFormat("kk:mm:ss,SS");
            buf.append(String.format("%-9s", record.getLevel().toString()));            
            buf.append(" - ");
            buf.append(String.format("%-12s", dateFormat.format(new Date(record.getMillis()))));
            buf.append(" - ");           
            // buf.append(toClassString(record.getSourceClassName(), 48));
            // buf.append('$');
            // buf.append(record.getSourceMethodName());            
            buf.append(      
            String.format("%-54s", toClassString(record.getSourceClassName(), 48) + "#" + record.getSourceMethodName())
            		);
            buf.append(": ");
            buf.append(formatMessage(record));
            buf.append("\n");
            
            Throwable throwable = record.getThrown();
            if (throwable != null) {
                StringWriter sink = new StringWriter();
                throwable.printStackTrace(new PrintWriter(sink, true));
                buf.append(sink.toString());
            }
 
            
            return buf.toString();
        }
	}
	
	public static String toClassString(String name, int maxLength) {
        return name.length() > maxLength ? name.substring(name.length() - maxLength) : name;
    }
	
}
