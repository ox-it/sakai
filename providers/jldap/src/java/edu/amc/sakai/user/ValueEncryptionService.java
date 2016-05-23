package edu.amc.sakai.user;

/**
 * This class provides encryption/decryption services.
 */
public class ValueEncryptionService {

    // The encryption key.
    private String key;

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * This salts and encrypts a value and returns a base64 encoded version of the encrypted value.
     * @param value The value to be encrypted.
     * @return A salted base64 encrypted version of the value.
     * @throws RuntimeException If encryption fails for any reason.
     */
    public String encrypt(String value) {
       return null;
    }

    /**
     * This extracts the salt and decrypts a value.
     * @param encrypted The salted and encrypted value which is base64 encoded.
     * @return The plain value;
     * @throws RuntimeException If decryption fails for any reason.
     */
    public String decrypt(String encrypted) {
        return null;
    }

}
