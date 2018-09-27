package org.sakaiproject.feedback.util;

/**
 * Simple interface for the Google reCAPTCHA service
 */
public interface RecaptchaService {

    public boolean isEnabled();

    public String getPublicKey();

    boolean verify(String response);
    boolean verify(String response, String ip);

}
