package uk.ac.ox.oucs.vle;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class XcriLogWriter extends OutputStreamWriter implements PopulatorLogWriter {
		
	private String name;
	
	public XcriLogWriter(OutputStream arg0, String name) {
		super(arg0);
		this.name = name;
	}
	
	public void header(String heading, Date generated) throws IOException {
		Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    
		this.write("<html><head></head><body>"+"<h3>"+ heading+" ");
		this.write(sdf.format(cal.getTime()));
		this.write("</h3>");
		if (null != generated) {
			this.write("<h3>Using the XCRI file generated on ");
			this.write(sdf.format(generated));
			this.write("</h3>");
		}
		this.write("<pre>");
	}
	
	public void footer() throws IOException {
		Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		this.write("</pre>");
		this.write("<h3>Log completed at ");
		this.write(sdf.format(cal.getTime()));
		this.write("</h3>");
		this.write("</body></html>");
	}
	
	public String getIdName() {
		return name+"Log.html";
	}
	
	public String getDisplayName() {
		return getIdName();
	}

}