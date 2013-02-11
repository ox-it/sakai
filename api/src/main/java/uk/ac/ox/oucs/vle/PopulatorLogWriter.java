package uk.ac.ox.oucs.vle;

import java.io.IOException;
import java.util.Date;

public interface PopulatorLogWriter {

	/**
	 * Called after the logWriter has been instantiated 
	 * 
	 * @param heading
	 * @throws IOException
	 */
	public void header(String heading) throws IOException;
	
	/**
	 * Called after a successfully reading the xcri xml file.
	 * The date parameter is the generated attribute of the xcri:catalog tag.
	 * 
	 * @param generated
	 * @throws IOException
	 */
	public void heading(Date generated) throws IOException;
	
	public void footer() throws IOException;
	
	public String getIdName();
	
	public String getDisplayName();
	
	public void write(String string) throws IOException;
	
	public void flush() throws IOException;
	
	public void close() throws IOException;

}