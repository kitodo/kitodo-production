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

package org.kitodo.production.security.password;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.naming.NamingException;
import javax.xml.bind.DatatypeConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * A password encoder that generates PBKDF2 password hash.
 */
public class HashingPasswordEncoder implements PasswordEncoder {
    private static final Logger logger = LogManager.getLogger(HashingPasswordEncoder.class);

    // magic strings and numbers
    private static final String ALGORITHM_PBKDF_TWO = "PBKDF2WithHmacSHA1";
    private static final String ALGORITHM_SHA_ONE = "SHA1PRNG";
    private static final int BITS_PER_BYTE = 8;

    private static int LENGTH_SALT_BYTES = 4;
    private static int LENGTH_HASH_BYTES = 20;

    private static SecureRandom shaOneAlgorithm;
    private static SecretKeyFactory pbkdfTwoAlgorithm;

    static {
        try {
            pbkdfTwoAlgorithm = SecretKeyFactory.getInstance(ALGORITHM_PBKDF_TWO);
            shaOneAlgorithm = SecureRandom.getInstance(ALGORITHM_SHA_ONE);
        } catch (NoSuchAlgorithmException e) {
            // if Java no longer knows the algorithm, we are in serious trouble
            throw new Error(e.getMessage(), e);
        }
    }

    private User user;

    /**
     * <b>Constructor.</b> Creates an uninitialized hashing password encoder!
     * Note that this password encoder will not work before you did
     * {@link #setUser(User)} which stores and provides the algorithm parameters
     */
    public HashingPasswordEncoder() {
    }

    /**
     * <b>Constructor.</b> Creates a new hashing password encoder with a user
     * object.
     *
     * @param user
     *            user object which stores and provides the algorithm parameters
     *            (iteration count, salt)
     */
    public HashingPasswordEncoder(User user) {
        setUser(user);
    }

    /**
     * When encoding a password, the algorithm name and parameters (iteration
     * count, salt) are written to the user's algorithm field.
     *
     * @param password
     *            password in clear text to set
     * @return password hash
     */
    @Override
    public String encode(CharSequence password) {
        String salt = createHexedSalt(LENGTH_SALT_BYTES);
        int iterationCount = determineTodaysIterationCount();
        user.setAlgorithm(ALGORITHM_PBKDF_TWO + "," + iterationCount + "," + salt);
        return generateHash(password, salt, iterationCount, LENGTH_HASH_BYTES);
    }

    private static String createHexedSalt(int length) {
        byte[] salt = new byte[length];
        shaOneAlgorithm.nextBytes(salt);
        return DatatypeConverter.printHexBinary(salt);
    }

    /*
     * In 2000, the number of iterations to use for PBKDF2 was recommended to be
     * 1000. 15 years later, in 2015, to compensate the improvement in CPU
     * performance, 10000 was suggested. The e-function given below was roughly
     * approximated from these values, which defines the iteration number on a
     * daily basis. This automatically adjusts the number of iterations for
     * future use.
     */
    private static int determineTodaysIterationCount() {
        return (int) (11 * Math.exp(0.0004 * ChronoUnit.DAYS.between(LocalDate.ofEpochDay(0), LocalDate.now())));
    }

    private static String generateHash(CharSequence password, String salt, int iterations, int hashLength) {
        char[] passwordBytes = password.toString().toCharArray();
        byte[] saltBytes = DatatypeConverter.parseHexBinary(salt);
        int keyLengthInBits = hashLength * BITS_PER_BYTE;
        PBEKeySpec arguments = new PBEKeySpec(passwordBytes, saltBytes, iterations, keyLengthInBits);
        try {
            return DatatypeConverter.printHexBinary(pbkdfTwoAlgorithm.generateSecret(arguments).getEncoded());
        } catch (InvalidKeySpecException e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    /**
     * When checking a password, the algorithm parameters from the user are
     * taken into consideration.
     *
     * @param enteredPassword
     *            password in clear text to compare to
     * @param storedPasswordHash
     *            password hash
     * @return whether the password hash can be recreated from the clear text
     *         password
     */
    @Override
    public boolean matches(CharSequence enteredPassword, String storedPasswordHash) {
        List<String> algorithmArgs = Arrays.asList(user.getAlgorithm().split(","));
        if (!Objects.equals(algorithmArgs.get(0), ALGORITHM_PBKDF_TWO)) {
            throw new IllegalArgumentException(algorithmArgs.get(0));
        }
        int iterationCount = Integer.valueOf(algorithmArgs.get(1));
        String salt = algorithmArgs.get(2);
        int hashLength = storedPasswordHash.length() / 2;

        String hashOfInput = generateHash(enteredPassword, salt, iterationCount, hashLength);
        boolean correct = Objects.equals(storedPasswordHash, hashOfInput);

        if (correct) {
            ifDesiredAddUserToLdap(user, enteredPassword.toString());
        }

        return correct;
    }

    /**
     * Checks whether the conditions are met to write the user into the LDAP,
     * and if, does it. To write the user entry into the LDAP, the user password
     * is required in plain text (it will be coded, but different) so this
     * happens here in the password encoder, as it is one of the few places
     * where the password is available in plain text. At the next login, the
     * user is authenticated via LDAP. Therefore the password hash and the
     * algorithm parameters are reset in the database.
     *
     * @param user
     *            user who is currently logging in
     * @param password
     *            the password in plain text
     */
    static void ifDesiredAddUserToLdap(User user, String password) {
        if (ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.LDAP_USE) && Objects.nonNull(user.getLdapGroup())
                && Objects.nonNull(user.getLdapGroup().getLdapServer())
                && !user.getLdapGroup().getLdapServer().isReadOnly()) {
            try {
                ServiceManager.getLdapServerService().createNewUser(user, password);
                user.setPassword(null);
                user.setAlgorithm(null);
                ServiceManager.getUserService().saveToDatabase(user);
            } catch (NoSuchAlgorithmException | NamingException | IOException e) {
                Helper.setErrorMessage("Writing user to LDAP failed", logger, e);
                logger.error(e);
            } catch (DAOException e) {
                Helper.setErrorMessage("Couldn't delete database password", logger, e);
            }
        }
    }

    /**
     * Sets the user.
     *
     * @param user
     *            user object which stores and provides the algorithm parameters
     *            (iteration count, salt)
     */
    public void setUser(User user) {
        this.user = user;
    }
}
