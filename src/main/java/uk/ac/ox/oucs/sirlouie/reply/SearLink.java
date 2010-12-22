package uk.ac.ox.oucs.sirlouie.reply;

public class SearLink implements SearObject {

	private String href;
	
	public SearLink() {}
	
	public SearLink(String href) {
		this.href = href;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public String getURL() {
		return "";
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("href="+href+":");
		return sb.toString();
	}
	
}
