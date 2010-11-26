package uk.ac.ox.oucs.vle;

import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;

public class ProxyServiceImpl implements ProxyService {

	private static final Log log = LogFactory.getLog(ProxyServiceImpl.class);

	private String secret;
	
	private byte[] secretBytes;

	private String hmac = "HmacSHA1";

	private String servletPrefix = "/proxy/";

	private ServerConfigurationService configService;

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public void setHmac(String hmac) {
		this.hmac = hmac;
	}

	public void setServletPrefix(String servletPrefix) {
		this.servletPrefix = servletPrefix;
	}

	public void setConfigService(ServerConfigurationService configService) {
		this.configService = configService;
	}
	
	public void init() {
		if (secret == null || secret.length() == 0) {
			log.warn("No secret supplied, autogenerating one, this may not work across a cluster.");
			Random rnd = new Random();
			secretBytes = new byte[32];
			rnd.nextBytes(secretBytes);
		} else {
			secretBytes = secret.getBytes();
		}
	}

	public String getProxyURL(String url) {
		if (url != null && url.length() > 0) {
			String signature = getSignature(url);
			if (signature != null) {
				try {
					StringBuilder proxyUrl = new StringBuilder(
							configService.getServerUrl());
					proxyUrl.append(servletPrefix);
					proxyUrl.append("?");
					proxyUrl.append("url=");

					// It shouldn't matter what encoding we use it should
					// already be encoded.
					proxyUrl.append(URLEncoder.encode(url, "UTF-8"));
					proxyUrl.append("&sig=");
					proxyUrl.append(signature); // This doesn't need
					// encoding
					return proxyUrl.toString();
				} catch (Exception e) {
					log.warn("Failed to generate proxy URL for:" + url, e);
				}
			}
		}
		return null;
	}

	public String getSignature(String url) {
		try {
			SecretKeySpec keySpec = new SecretKeySpec(secretBytes, hmac);

			Mac mac;
			mac = Mac.getInstance(hmac);
			mac.init(keySpec);

			byte[] result = mac.doFinal(url.getBytes());
			// Encode without any linebreaks and URL safe.
			return new String(Base64.encodeBase64(result, false, true));
		} catch (NoSuchAlgorithmException nsae) {
			log.warn("Failed to generate signature due to invalid algorithm: "+ hmac);
		} catch (InvalidKeyException e) {
			log.warn("Failed to generate signature due to invalid secret.");
		}
		return null;

	}

}
