package uk.ac.ox.oucs.sirlouie.reply;

public class SearError implements SearObject {
	
	private int code;
	private String message;
	
	public SearError() {}
	
	public SearError(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getURL() {
		return "";
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("code="+code+":");
		sb.append("message="+message+":");
		return sb.toString();
	}
}
