package uk.ac.ox.it.shoal.logic;

/**
 * Exception which attempts to convey help to user.
 */
public class UserMessageException extends RuntimeException implements UserMessage {

    private final String key;
    private final String[] parameters;

    public UserMessageException(String message, Throwable cause, String key, String[] parameters) {
        super(message, cause);
        this.key = key;
        this.parameters = parameters;
    }

    @Override
    public String getBundleKey() {
        return key;
    }

    @Override
    public String[] getParameters() {
        return parameters;
    }
}
