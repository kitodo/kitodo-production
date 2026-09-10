/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.production.security.encrypt;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EncryptionUtils {
    
    private static final String LEGACY_ENCRYPTION_STRATEGY_KEY = null;
    private static final String AES_GCM_ENCRYPTION_STRATEGY_KEY = "aes-gcm";
    private static final String DEFAULT_ENCRYPTION_STRATEGY_KEY = AES_GCM_ENCRYPTION_STRATEGY_KEY;

    private static final Map<String, KitodoEncryptionStrategy> STRATEGIES;

    static {
        STRATEGIES = new HashMap<>();
        STRATEGIES.put(LEGACY_ENCRYPTION_STRATEGY_KEY, new LegacyEncryptionStrategy());
        STRATEGIES.put(AES_GCM_ENCRYPTION_STRATEGY_KEY, new AesGcmEncryptionStrategy());
    }

    /**
     * Encrypt text using a secret based on the default encryption strategy.
     * 
     * @param value the text to be encrypted
     * @param secret the secret used for encryption
     * @return the encrypted text
     * @throws KitodoEncryptionException in case encryption fails
     */
    public static String encrypt(String value, String secret) throws KitodoEncryptionException {
        if (Objects.isNull(secret) || secret.isBlank()) {
            throw new KitodoEncryptionException("Cannot encrypt without secret");
        }
        if (Objects.isNull(value)) {
            throw new KitodoEncryptionException("Cannot encrypt null value");
        }
        String strategyKey = DEFAULT_ENCRYPTION_STRATEGY_KEY;
        return String.format("{%s}%s", strategyKey, STRATEGIES.get(strategyKey).encrypt(value, secret));
    }

    /**
     * Decrypt previously encrypted text using a secret.
     * 
     * <p>Automatically detects the encryption strategy (via a prefix) and applies the correct decryption algorithm.</p>
     * 
     * @param encryptedValue the encrypted text
     * @param secret the secret that was used for encryption
     * @return the decrypted text
     * @throws KitodoEncryptionException in case decryption fails
     */
    public static String decrypt(String encryptedValue, String secret) throws KitodoEncryptionException {
        if (Objects.isNull(secret) || secret.isBlank()) {
            throw new KitodoEncryptionException("Cannot decrypt without secret");
        }
        if (Objects.isNull(encryptedValue)) {
            throw new KitodoEncryptionException("Cannot decrypt null value");
        }
        String strategyKey = identifyStrategy(encryptedValue);
        String encryptedValueWithoutSuffix = encryptedValue.substring(Objects.isNull(strategyKey) ? 0 : strategyKey.length() + 2);
        return STRATEGIES.get(strategyKey).decrypt(encryptedValueWithoutSuffix, secret);
    }

    /**
     * Return true if an encrypted value needs upgrading to a better encryption strategy.
     * 
     * @param encryptedValue the encrypted value
     * @return true if upgrading is needed
     */
    public static boolean needsUpgrade(String encryptedValue) {
        String strategyKey = identifyStrategy(encryptedValue);
        if (DEFAULT_ENCRYPTION_STRATEGY_KEY.equals(strategyKey)) {
            return false;
        }
        return true;
    }

    /**
     * Upgrade an encrypted value to the current default encryption strategy.
     * 
     * @param encryptedValue the encrypted value
     * @param secret the secret key that was used for encryption
     * @return the value encrypted with the current default encryption strategy
     * @throws KitodoEncryptionException in case encryption or decryption fails
     */
    public static String upgradeEncryption(String encryptedValue, String secret) throws KitodoEncryptionException {
        return encrypt(decrypt(encryptedValue, secret), secret);
    }

    /**
     * Identifies the encryption strategy that was used to encrypt the provided value.
     * 
     * @param encryptedValue the encrypted value
     * @return the strategy key of the strategy that was used for encryption (or null if not known)
     */
    private static String identifyStrategy(String encryptedValue) {
        for (String key : STRATEGIES.keySet()) {
            if (encryptedValue.startsWith(String.format("{%s}", key))) {
                return key;
            }
        }
        return LEGACY_ENCRYPTION_STRATEGY_KEY;
    }

}
