package uk.ac.ox.it.shoal.logic;

/**
 * This interface is implemented by objects wanting to provide better user messages.
 * We have this interface so that all the messages and i18n are kept outside the API.
 * This is typically implemented by an Exception that wants to have a nicer message returned.
 */
public interface UserMessage {

    /**
     * @return The key that should be looked up in an i18n bundle.
     */
    String getBundleKey();

    /**
     * @return Any parameters that should be used for the i18n string.
     */
    String[] getParameters();
}
