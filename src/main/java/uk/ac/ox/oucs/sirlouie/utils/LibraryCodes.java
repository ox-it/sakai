package uk.ac.ox.oucs.sirlouie.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.StringTokenizer;


public class LibraryCodes extends HashMap<String, String> {
	
	private static final long serialVersionUID = 1L;
	private static LibraryCodes ref;
	
	private LibraryCodes() {

		super();
		try {
			InputStream input = getClass().getResourceAsStream("/libnames.csv");
			Reader in = new InputStreamReader(input);
			BufferedReader bufRdr = new BufferedReader(in);
			String line = null;

			//read each line of text file
			while((line = bufRdr.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line,",");
				String[] data = new String[2];
				int col = 0;
				
				while (st.hasMoreTokens()) {
					//get next token and store it in the array
					data[col] = trimQuotes(st.nextToken());
					col++;
				}
				put(data[0], data[1]);
			}

			//close the file
			bufRdr.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static synchronized LibraryCodes getInstance()
	{
		if (ref == null)
			ref = new LibraryCodes();
		return ref;
	}
	
	public static String trimQuotes(String value) {
		
	    if (value == null) {
	    	return value;
	    }
	    value = value.trim();
	    if (value.startsWith("\"") && value.endsWith("\"")) {
	    	return value.substring(1, value.length() - 1);
	    }
	    return value;
	}
	
}
