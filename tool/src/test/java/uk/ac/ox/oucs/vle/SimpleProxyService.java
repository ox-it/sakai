package uk.ac.ox.oucs.vle;

public class SimpleProxyService implements ProxyService {

	public String getProxyURL(String url) {
		return "@@**!!";
	}

	public String getSignature(String url) {
		return "123456";
	}

}
