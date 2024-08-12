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

package org.kitodo.production.interfaces.activemq;

import edu.umd.cs.findbugs.annotations.CheckForNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.utils.Guard;

public class MapMessageObjectReader {

    private MapMessage ticket;
    private static final Logger logger = LogManager.getLogger(MapMessageObjectReader.class);
    private static final String MANDATORY_ARGUMENT = "Mandatory argument ";
    private static final String MISSING_ARGUMENT = "Missing mandatory argument: \"";
    private static final String WRONG_TYPE = "\" was not found to be of type ";

    /**
     * This instantiates a new MapMessageObjectReader which is attached to a
     * given MapMessage.
     *
     * @param message
     *            MapMessage object
     */
    public MapMessageObjectReader(MapMessage message) {
        Guard.isNotNull("message", message);
        this.ticket = message;
    }

    /**
     * Fetches a {@code Set<String>} from a MapMessage. This is a strict
     * implementation that requires the collection not to be empty and not to
     * contain the null element.
     *
     * <p>
     * Please note that the Set is allowed to contain the empty String (“”).
     *
     * @param key
     *            the name of the set to return
     * @return the set requested
     * @throws IllegalArgumentException
     *             in case that getObject returns null, the returned object is
     *             not of type Collection, any of the content elements are not
     *             of type String or the collection is empty at all.
     * @throws JMSException
     *             can be thrown by MapMessage.getObject(String)
     */
    public Set<String> getMandatorySetOfString(String key) throws JMSException {
        Set<String> mandatorySetOfStrings = new HashSet<>();

        Object collectionObject = ticket.getObject(key);
        if (Objects.isNull(collectionObject)) {
            throw new IllegalArgumentException(MISSING_ARGUMENT + key + "\"");
        }
        if (!(collectionObject instanceof Collection<?>)) {
            throw new IllegalArgumentException(
                    "Incompatible types: \"" + key + WRONG_TYPE + "Collection<?>.");
        }
        for (Object contentObject : (Collection<?>) collectionObject) {
            if (!(contentObject instanceof String)) {
                if (Objects.isNull(contentObject)) {
                    throw new IllegalArgumentException(MISSING_ARGUMENT + key + "\" must not be empty.");
                } else {
                    throw new IllegalArgumentException(
                            "Incompatible types: An element of \"" + key + WRONG_TYPE + " String.");
                }
            }
            mandatorySetOfStrings.add((String) contentObject);
        }
        return mandatorySetOfStrings;
    }

    /**
     * Fetches a String from a MapMessage. This is a strict implementation that
     * requires the string not to be null and not to be empty.
     *
     * @param key
     *            the name of the string to return
     * @return the string requested
     * @throws IllegalArgumentException
     *             in case that getObject returns null or the returned string is
     *             of length “0”.
     * @throws JMSException
     *             thrown by MapMessage.getString(String) if the string is a
     *             byte[]
     */
    public String getMandatoryString(String key) throws JMSException {
        String mandatoryString = ticket.getString(key);
        if (Objects.isNull(mandatoryString) || mandatoryString.isEmpty()) {
            throw new IllegalArgumentException(MISSING_ARGUMENT + key + "\"");
        }
        return mandatoryString;
    }

    /**
     * Fetches a {@code Collection<Integer>} from a MapMessage. This is a loose
     * implementation for an optional object with optional content. The
     * collection content is filtered through {@code toString()} and split on
     * non-digits, dealing generously with list variants and separators. If not
     * found, returns an empty collection, never {@code null}.
     *
     * @param key
     *            the name of the set to return
     * @return the set requested
     * @throws JMSException
     *             can be thrown by MapMessage.getObject(String)
     */
    public Collection<Integer> getCollectionOfInteger(String key) throws JMSException {
        return getCollectionOfString(key).stream()
                .flatMap(string -> Arrays.stream(string.split("\\D+"))).filter(StringUtils::isNumeric)
                .map(Integer::valueOf).collect(Collectors.toList());
    }

    /**
     * Fetches a {@code Collection<String>} from a MapMessage. This is a loose
     * implementation for an optional object with optional content. The
     * collection content is filtered through {@code toString()}, {@code null}
     * objects will be skipped. If not found, returns an empty collection, never
     * {@code null}.
     *
     * @param key
     *            the name of the set to return
     * @return the set requested
     * @throws JMSException
     *             can be thrown by MapMessage.getObject(String)
     */
    public Collection<String> getCollectionOfString(String key) throws JMSException {

        Object collectionObject = ticket.getObject(key);
        if (Objects.isNull(collectionObject)) {
            return Collections.emptyList();
        }
        if (!(collectionObject instanceof Collection<?>)) {
            return Collections.singletonList(collectionObject.toString());
        }
        return ((Collection<?>) collectionObject).stream().filter(Objects::nonNull).map(Object::toString)
                .collect(Collectors.toList());
    }

    /**
     * Fetches a String from a map. This is a strict implementation that
     * requires the string not to be null and not to be empty.
     *
     * @param key
     *            the name of the string to return
     * @return the string requested
     * @throws IllegalArgumentException
     *             in case that get returns null, an inappropriate object, or
     *             the returned string is of length “0”.
     */
    public static String getMandatoryString(Map<?, ?> data, String key) {
        Object value = data.get(key);
        if (Objects.isNull(value)) {
            throw new IllegalArgumentException(MISSING_ARGUMENT + key + "\"");
        }
        if (!(value instanceof String)) {
            throw new IllegalArgumentException(MANDATORY_ARGUMENT + key + " is not a string");
        }
        String mandatoryString = (String) value;
        if (mandatoryString.isEmpty()) {
            throw new IllegalArgumentException(MISSING_ARGUMENT + key + "\"");
        }
        return mandatoryString;
    }

    /**
     * Fetches a String from a MapMessage. This is an access forward to the
     * native function of the MapMessage. You may consider to use
     * getMandatoryString() instead.
     *
     * @param key
     *            the name of the string to return
     * @return the string requested (may be null or empty)
     * @throws JMSException
     *             can be thrown by MapMessage.getString(String)
     */

    @CheckForNull
    public String getString(String key) throws JMSException {
        return ticket.getString(key);
    }

    /**
     * Fetches an Integer object from a MapMessage. This is a strict
     * implementation that requires the Integer not to be null.
     *
     * @param key
     *            the name of the integer to return
     * @return the string requested
     * @throws IllegalArgumentException
     *             in case that getObject returns null
     * @throws JMSException
     *             in case that getObject returns an unmatching object type
     */
    public Integer getMandatoryInteger(String key) throws JMSException {
        if (!ticket.itemExists(key)) {
            throw new IllegalArgumentException(MISSING_ARGUMENT + key + "\"");
        }
        return ticket.getInt(key);
    }

    /**
     * Fetches an Integer object from a map. This is a strict implementation
     * that requires the Integer not to be null.
     *
     * @param data
     *            the data map
     * @param key
     *            the name of the integer to return
     * @return the string requested
     * @throws IllegalArgumentException
     *             in case that there is no such key, or get returns an
     *             unmatching object type
     */
    public static Integer getMandatoryInteger(Map<?, ?> data, String key) {
        if (!data.containsKey(key)) {
            throw new IllegalArgumentException(MISSING_ARGUMENT + key + "\"");
        }
        Object value = data.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof String) {
            return Integer.valueOf((String) value);
        }
        throw new IllegalArgumentException(MANDATORY_ARGUMENT + key + " is not an integer");
    }

    /**
     * Fetches a {@code Map<String,String>} from a MapMessage. This is a partly
     * strict implementation that allows no null element neither as key, nor as
     * value. However, if no object was found for the given key, or the key
     * returned a null object, the function returns null. Also, the Map is
     * allowed to contain the empty String (“”) as key and as values.
     *
     * @param key
     *            the name of the map to return
     * @return the map requested
     * @throws IllegalArgumentException
     *             in case that the object returned by getObject returned object
     *             is not of type Map or any of the content elements are not of
     *             type String.
     */
    public Map<String, String> getMapOfStringToString(String key) {
        Object mapObject = null;
        try {
            mapObject = ticket.getObject(key);
        } catch (JMSException | RuntimeException e) {
            logger.error(e.getMessage(), e);
        }
        if (Objects.isNull(mapObject)) {
            return Collections.emptyMap();
        }

        if (!(mapObject instanceof Map<?, ?>)) {
            throw new IllegalArgumentException(
                    "Incompatible types: \"" + key + WRONG_TYPE + "Map<?, ?>.");
        }

        Map<String, String> mapOfStringToString = new HashMap<>();
        for (Object keyObject : ((Map<?, ?>) mapObject).keySet()) {
            Object valueObject = ((Map<?, ?>) mapObject).get(keyObject);
            if (!(keyObject instanceof String)) {
                throw new IllegalArgumentException(
                        "Incompatible types: A key element of \"" + key + WRONG_TYPE + "String.");
            }
            if (!(valueObject instanceof String)) {
                throw new IllegalArgumentException(
                        "Incompatible types: A value element of \"" + key + WRONG_TYPE + "String.");
            }
            mapOfStringToString.put((String) keyObject, (String) valueObject);
        }

        return mapOfStringToString;
    }

    @CheckForNull
    public List<?> getList(String key) throws JMSException {
        Object valueObject = ticket.getObject(key);
        if (Objects.isNull(valueObject)) {
            return null;
        }
        if (!(valueObject instanceof List)) {
            throw new IllegalArgumentException(key + " is not a List");
        }
        return (List<?>) valueObject;
    }

    @CheckForNull
    public Integer getInteger(String key) throws JMSException {
        Object valueObject = ticket.getObject(key);
        if (Objects.isNull(valueObject)) {
            return null;
        }
        if (!(valueObject instanceof Integer)) {
            throw new IllegalArgumentException(key + " is not an Integer");
        }
        return (Integer) valueObject;
    }

    @CheckForNull
    public Map<String, ?> getMapOfString(String key) throws JMSException {
        HashMap<String, Object> mapOfString = new HashMap<>();
        Object mapObject = ticket.getObject(key);
        if (Objects.isNull(mapObject)) {
            return null;
        }
        if (!(mapObject instanceof Map)) {
            throw new IllegalArgumentException("Incompatible types: \"" + key + WRONG_TYPE + "Map.");
        }
        for (Entry<?, ?> entry : ((Map<?, ?>) mapObject).entrySet()) {
            Object entryKey = entry.getKey();
            if (!(entryKey instanceof String)) {
                throw new IllegalArgumentException("Incompatible types: A key element of \"" + key + WRONG_TYPE
                        + "String.");
            }
            mapOfString.put(((String) entryKey), entry.getValue());
        }
        return mapOfString;
    }

    /**
     * Tests whether a field can be obtained from a MapMessage.
     *
     * @param fieldName
     *            name of the field
     * @return whether the field can be obtained
     * @throws IllegalArgumentException
     *             can be thrown by MapMessage
     * @throws JMSException
     *             can be thrown by MapMessage
     */
    public boolean hasField(String fieldName) throws JMSException {
        String valueOfField = ticket.getString(fieldName);
        return Objects.nonNull(valueOfField) && !valueOfField.isEmpty();
    }
}
