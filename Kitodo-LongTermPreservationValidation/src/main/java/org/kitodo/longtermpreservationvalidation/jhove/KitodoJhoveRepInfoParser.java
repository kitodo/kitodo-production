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

package org.kitodo.longtermpreservationvalidation.jhove;

import edu.harvard.hul.ois.jhove.Message;
import edu.harvard.hul.ois.jhove.NisoImageMetadata;
import edu.harvard.hul.ois.jhove.Property;
import edu.harvard.hul.ois.jhove.PropertyArity;
import edu.harvard.hul.ois.jhove.RepInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Parser for JHove validation object "RepInfo".
 */
public class KitodoJhoveRepInfoParser {

    private static final Logger logger = LogManager.getLogger(KitodoJhoveRepInfoParser.class);

    /**
     * Parse property if it claims to be of type (arity) list.
     * 
     * @param propertyKey
     *            the property name
     * @param propertyValue
     *            the property value
     * @param propertyMap
     *            the simple map of property values
     * @param stack
     *            the stack of properties that need to be parsed
     */
    private static void parsePropertyOfTypeList(String propertyKey, Object propertyValue,
            Map<String, String> propertyMap, Stack<Pair<String, Property>> stack) {
        if (propertyValue instanceof List<?>) {
            List<?> valueList = (List<?>) propertyValue;
            if (valueList.size() > 0) {
                if (valueList.getFirst() instanceof Property) {
                    for (Object p : ((List<?>) propertyValue)) {
                        stack.add(Pair.of(propertyKey, (Property) p));
                    }
                } else if (valueList.getFirst() instanceof String) {
                    propertyMap.put(propertyKey, StringUtils.join(valueList, ","));
                } else {
                    logger.debug(
                        "JHove RepInfo contains property list with unknown item type: " + valueList.getFirst().toString());
                }
            }
        } else {
            logger.debug("JHove RepInfo contains property list of unknown type: " + propertyValue.toString());
        }
    }

    /**
     * Parse property if it claims to be of type (arity) array.
     * 
     * @param propertyKey
     *            the property name
     * @param propertyValue
     *            the property value
     * @param propertyMap
     *            the simple map of property values
     * @param stack
     *            the stack of properties that need to be parsed
     */
    private static void parsePropertyOfTypeArray(String propertyKey, Object propertyValue,
            Map<String, String> propertyMap, Stack<Pair<String, Property>> stack) {
        if (propertyValue instanceof Property[]) {
            Property[] propertyArray = (Property[]) propertyValue;
            stack.addAll(IntStream.range(0, propertyArray.length)
                    .mapToObj((i) -> Pair.of(propertyKey, propertyArray[i])).collect(Collectors.toList()));
        } else if (propertyValue instanceof String[]) {
            propertyMap.put(propertyKey, Arrays.toString((String[]) propertyValue));
        } else if (propertyValue instanceof int[]) {
            propertyMap.put(propertyKey, Arrays.toString((int[]) propertyValue));
        } else if (propertyValue instanceof long[]) {
            propertyMap.put(propertyKey, Arrays.toString((long[]) propertyValue));
        } else {
            logger.debug("JHove RepInfo contains property array of unknown type: " + propertyValue.toString());
        }
    }

    /**
     * Iterates through hierarchical properties and add them to a flat map. The
     * hierarchy information is lost. If two properties in different parts of
     * the hierarchy have the same name, the outcome is not clear and depends on
     * the iteration order.
     * 
     * @param properties
     *            the map of properties as provided by Jhov
     * @return a simple flat map of property names to values
     */
    private static Map<String, String> hierarchicalPropertiesToSimpleMap(Map<String, Property> properties) {
        Map<String, String> propertyMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Stack<Pair<String, Property>> stack = new Stack<>();

        for (Map.Entry<String, Property> entry : properties.entrySet()) {
            stack.add(Pair.of(entry.getKey(), entry.getValue()));
        }

        while (stack.size() > 0) {
            Pair<String, Property> entry = stack.pop();
            PropertyArity propertyArity = entry.getRight().getArity();
            String propertyKey = entry.getRight().getName();
            Object propertyValue = entry.getRight().getValue();

            if (propertyArity == PropertyArity.SCALAR) {
                if (propertyValue instanceof NisoImageMetadata) {
                    NisoImageMetadata metadata = (NisoImageMetadata) propertyValue;
                    for (Map.Entry<String, String> e : KitodoJhoveNisoImageMetadataHelper
                            .nisoImageMetadataToMap(metadata).entrySet()) {
                        propertyMap.put(e.getKey(), e.getValue());
                    }
                } else {
                    propertyMap.put(propertyKey, String.valueOf(propertyValue));
                }
            } else if (propertyArity == PropertyArity.ARRAY) {
                parsePropertyOfTypeArray(propertyKey, propertyValue, propertyMap, stack);
            } else if (propertyArity == PropertyArity.LIST) {
                parsePropertyOfTypeList(propertyKey, propertyValue, propertyMap, stack);
            }
        }
        return propertyMap;
    }

    /**
     * Converts an integer value representing a ternary value (true, false,
     * undetermined) to a string of true, false, undetermined.
     * 
     * @param value
     *            the int value
     * @return the string value
     */
    private static String ternaryValueToString(int value) {
        if (value == RepInfo.TRUE) {
            return "true";
        } else if (value == RepInfo.FALSE) {
            return "false";
        } else if (value == RepInfo.UNDETERMINED) {
            return "undetermined";
        }
        return "invalid";
    }

    /**
     * Converts properties provided by JHove to a simple flat string
     * representation for debugging.
     * 
     * @param properties
     *            the properties provided by JHove
     * @return the flat string representation of these properties
     */
    private static String propertiesToString(Map<String, Property> properties) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : hierarchicalPropertiesToSimpleMap(properties).entrySet()) {
            builder.append("- " + entry.getKey() + ": " + entry.getValue() + "\n");
        }
        return builder.toString();
    }

    /**
     * Extracts property values from a RepInfo object provided by JHove by
     * parsing the hierarchical property structure and adding non-hierarchical
     * base properties provided directly by Jhove (e.g. valid, wellformed).
     * 
     * @param info
     *            the RepInfo object
     * @return the flat map of properties and values
     */
    public static Map<String, String> repInfoToPropertyMap(RepInfo info) {
        Map<String, String> propertyMap = hierarchicalPropertiesToSimpleMap(info.getProperty());
        propertyMap.put("mimetype", info.getMimeType());
        propertyMap.put("format", info.getFormat());
        propertyMap.put("version", info.getVersion());
        propertyMap.put("size", String.valueOf(info.getSize()));
        propertyMap.put("profile", StringUtils.join(info.getProfile(), ","));
        propertyMap.put("valid", ternaryValueToString(info.getValid()));
        propertyMap.put("wellformed", ternaryValueToString(info.getWellFormed()));
        return propertyMap;
    }

    /**
     * Converts info messages contained in the RepInfo object to a simple list
     * of strings.
     * 
     * @param info
     *            the RepInfo object
     * @return a simple list of message strings
     */
    public static List<String> repInfoMessagesToStringList(RepInfo info) {
        return info.getMessage().stream().map((m) -> repoInfoMessageToString(m)).collect(Collectors.toList());
    }

    /**
     * Converts a single message contained in the RepInfo object to a string.
     * 
     * @param message
     *            the message object
     * @return a string representing the message
     */
    public static String repoInfoMessageToString(Message message) {
        String offset = "";
        String subMessage = "";
        String prefix = "";
        if (message.getOffset() != Message.NULL) {
            offset = " (offset " + String.valueOf(message.getOffset()) + ")";
        }
        if (Objects.nonNull(message.getSubMessage()) && !message.getSubMessage().isEmpty()) {
            subMessage = " - " + message.getSubMessage();
        }
        if (Objects.nonNull(message.getPrefix()) && !message.getPrefix().isEmpty()) {
            prefix = message.getPrefix() + ": ";
        }
        return prefix + message.getMessage() + subMessage + offset;
    }

    /**
     * Converts a repInfo object to a simple string for debugging.
     * 
     * @param info
     *            the RepInfo object
     * @return a simple string representing the RepInfo object for debugging
     */
    public static String repInfoToString(RepInfo info) {
        StringBuilder builder = new StringBuilder();

        List<String> checksums = info.getChecksum().stream().map((c) -> c.getType() + ":" + c.getValue())
                .collect(Collectors.toList());

        builder.append("RepInfo object " + info.toString() + "\n");
        builder.append("URI: " + info.getUri() + "\n");
        builder.append("Format: " + info.getFormat() + "\n");
        builder.append("MimeType: " + info.getMimeType() + "\n");
        builder.append("Note: " + info.getNote() + "\n");
        builder.append("Size: " + info.getSize() + "\n");
        builder.append("Valid: " + info.getValid() + "\n");
        builder.append("Version: " + info.getVersion() + "\n");
        builder.append("Well-Formed: " + info.getWellFormed() + "\n");
        builder.append("CheckSums: " + StringUtils.join(checksums, ",") + "\n");
        builder.append("Created: " + info.getCreated() + "\n");
        builder.append("Last-Modified: " + info.getLastModified() + "\n");
        builder.append("Messages:\n");
        for (Message message : info.getMessage()) {
            builder.append("- Message: " + message.getMessage() + "\n");
            builder.append("  - Submessage: " + message.getSubMessage() + "\n");
        }
        builder.append("Profiles: " + StringUtils.join(info.getProfile(), ",") + "\n");
        builder.append("Properties:\n");
        builder.append(propertiesToString(info.getProperty()));
        builder.append("SigMatches: " + StringUtils.join(info.getSigMatch(), ",") + "\n");
        builder.append("URLFlag: " + info.getURLFlag() + "\n");
        builder.append("isConsistent: " + info.isConsistent() + "\n");
        return builder.toString();
    }

}
