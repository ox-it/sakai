package uk.ac.ox.oucs.sirlouie.reply;

public class SearLibrary implements SearObject {
	
	private String callNumber;
	private String collection;
	private String institution;
	private String library;
	private String status;
	private String url;
	
	public SearLibrary() {}
	
	public SearLibrary(String institution, String library, String status, String collection, String callNumber, String url) {
		this.callNumber = callNumber;
		this.collection = collection;
		this.institution = institution;
		this.library = library;
		this.status = status;
		this.url = url;
	}

	public String getCallNumber() {
		return callNumber;
	}

	public void setCallNumber(String callNumber) {
		this.callNumber = callNumber;
	}
	
	public String getCollection() {
		return collection;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}
	
	public String getInstitution() {
		return institution;
	}

	public void setInstitution(String institution) {
		this.institution = institution;
	}

	public String getLibrary() {
		return library;
	}

	public void setLibrary(String library) {
		this.library = library;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getURL() {
		return url;
	}

	public void setURL(String url) {
		this.url = url;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("institution="+institution+":");
		sb.append("library="+library+":");
		sb.append("status="+status+":");
		sb.append("collection="+collection+":");
		sb.append("callNumber="+callNumber+":");
		sb.append("url="+url);
		return sb.toString();
	}
	
}

