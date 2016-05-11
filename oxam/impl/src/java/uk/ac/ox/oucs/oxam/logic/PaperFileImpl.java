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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PaperFileImpl other = (PaperFileImpl) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

}
