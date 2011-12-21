package uk.ac.ox.oucs.oxam.logic;


public class PaperFileImpl implements PaperFile {

	private String path;
	private Location location;
	
	public PaperFileImpl(String year, String term, String paperCode, String extension, Location location) {
		this.path = "/uploads/"+ year + "/"+ term+ "/"+ paperCode.toLowerCase()+ "."+ extension;
		this.location = location;
	}

	public String getURL() {
		return location.getPrefix()+ path;  
		
	}

	public String getPath() {
		return location.getPath(path);
	}

}
