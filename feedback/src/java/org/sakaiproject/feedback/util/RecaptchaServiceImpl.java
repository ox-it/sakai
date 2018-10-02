package org.sakaiproject.feedback.util;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * This provides a simple implementation of the recaptcha service.
 */
public class RecaptchaServiceImpl implements RecaptchaService {

    public static final String PUBLIC_KEY = "user.recaptcha.public-key";
    public static final String PRIVATE_KEY = "user.recaptcha.private-key";

    private final Logger logger = LoggerFactory.getLogger(RecaptchaServiceImpl.class);
    private ServerConfigurationService serverConfigurationService;

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    @Override
    public boolean isEnabled() {
        return serverConfigurationService.getBoolean("user.recaptcha.enabled", false)
                && !getPublicKey().isEmpty()
                && !getSecretKey().isEmpty();
    }

    @Override
    public String getPublicKey() {
        return serverConfigurationService.getString(PUBLIC_KEY);
    }

    public String getSecretKey() {
        return serverConfigurationService.getString(PRIVATE_KEY);
    }

    @Override
    public boolean verify(String response) {
        return verify(response, null);
    }

    @Override
    public boolean verify(String response, String ip) {
        try {
            Unirest.setTimeouts(5000, 5000);
            HttpRequest httpRequest = Unirest.get("https://www.google.com/recaptcha/api/siteverify")
                    .queryString("secret", getSecretKey())
                    .queryString("response", response);
            if (ip != null && !ip.isEmpty()) {
                httpRequest.queryString("remoteip", ip);
            }
            HttpResponse<JsonNode> jsonResponse = httpRequest.asJson();
            int status = jsonResponse.getStatus();
            if (status >= 200 && status < 300) {
                JsonNode body = jsonResponse.getBody();
                JSONObject object = body.getObject();
                try {
                    if (object != null) {
                        boolean success = object.getBoolean("success");
                        if (!success) {
                            // Check if we're using a good secret.
                            JSONArray jsonArray = object.getJSONArray("error-codes");
                            for (Object aJsonArray : jsonArray) {
                                if ("invalid-input-secret".equals(aJsonArray)) {
                                    logger.warn("Secret currently used isn't valid.");
                                }
                            }
                        }
                        return success;
                    }
                } catch (JSONException e) {
                    // Logged outside catch.
                }
                logger.warn("Response JSON doesn't match expected: {}", body.toString());
            } else {
                logger.warn("Request failed status code: {}, message: {}", status, jsonResponse.getStatusText());
            }
        } catch (UnirestException e) {
            logger.warn("Problem with request: "+ e.getMessage());
        }
        return false;
    }

}
