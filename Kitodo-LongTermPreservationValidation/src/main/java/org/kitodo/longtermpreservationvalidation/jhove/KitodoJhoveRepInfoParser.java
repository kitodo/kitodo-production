package org.kitodo.longtermpreservationvalidation.jhove;

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

import edu.harvard.hul.ois.jhove.Message;
import edu.harvard.hul.ois.jhove.NisoImageMetadata;
import edu.harvard.hul.ois.jhove.Property;
import edu.harvard.hul.ois.jhove.PropertyArity;
import edu.harvard.hul.ois.jhove.RepInfo;

public class KitodoJhoveRepInfoParser {

    private static final Logger logger = LogManager.getLogger(KitodoJhoveRepInfoParser.class);

    private static Map<String, String> hierarchicalPropertiesToSimpleMap(Map<String, Property> properties) {
        Map<String, String> propertyMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Stack<Pair<String, Property>> stack = new Stack<>();

        for (Map.Entry<String, Property> entry : properties.entrySet()) {
            stack.add(Pair.of(entry.getKey(), entry.getValue()));
        }

        while (stack.size() > 0) {
            Pair<String, Property> entry = stack.pop();
            Property property = entry.getRight();
            String propertyKey = property.getName(); 
            Object propertyValue = entry.getRight().getValue();

            if (property.getArity() == PropertyArity.SCALAR) {
                if (propertyValue instanceof NisoImageMetadata) {
                    NisoImageMetadata metadata = (NisoImageMetadata)propertyValue;
                    for (Map.Entry<String, String> e : KitodoJhoveNisoImageMetadataHelper.nisoImageMetadataToMap(metadata).entrySet()) {
                        propertyMap.put(e.getKey(), e.getValue());
                    }
                } else {
                    propertyMap.put(propertyKey, String.valueOf(propertyValue));
                }
            } else if (property.getArity() == PropertyArity.ARRAY) {
                if (propertyValue instanceof Property[]) {
                    Property[] propertyArray = (Property[])propertyValue;
                    stack.addAll(
                        IntStream.range(0, propertyArray.length)
                            .mapToObj((i) -> Pair.of(propertyKey, propertyArray[i]))
                            .collect(Collectors.toList()
                        )
                    );
                } else if (propertyValue instanceof String[]) {
                    propertyMap.put(propertyKey, Arrays.toString((String[])propertyValue));
                } else if (propertyValue instanceof int[]) {
                    propertyMap.put(propertyKey, Arrays.toString((int[])propertyValue));
                } else if (propertyValue instanceof long[]) {
                    propertyMap.put(propertyKey, Arrays.toString((long[])propertyValue));
                } else {
                    logger.debug("JHove RepInfo contains property array of unknown type: " + propertyValue.toString());
                }
            } else if (property.getArity() == PropertyArity.LIST) {
                if (propertyValue instanceof List<?>) {
                    List<?> valueList = (List<?>)propertyValue;
                    if (valueList.size() > 0) {
                        if (valueList.get(0) instanceof Property) {
                            for(Object p : ((List<?>)property.getValue())) {
                                stack.add(Pair.of(propertyKey, (Property)p));
                            }
                        } else if (valueList.get(0) instanceof String) {
                            propertyMap.put(propertyKey, StringUtils.join(valueList, ","));
                        } else {
                            logger.debug("JHove RepInfo contains property list with unknown item type: " + valueList.get(0).toString());
                        }
                    }
                } else {
                    logger.debug("JHove RepInfo contains property list of unknown type: " + propertyValue.toString());
                }
            }
        }
        return propertyMap;
    }

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

    private static String propertiesToString(Map<String, Property> properties) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : hierarchicalPropertiesToSimpleMap(properties).entrySet()) {
            builder.append("- " + entry.getKey() + ": " + entry.getValue() + "\n");
        }
        return builder.toString();
    }

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

    public static List<String> repInfoMessagesToStringList(RepInfo info) {
        return info.getMessage().stream().map((m) -> repoInfoMessageToString(m)).collect(Collectors.toList());
    }

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
    
    public static String repInfoToString(RepInfo info) {
        StringBuilder builder = new StringBuilder();

        List<String> checksums = info.getChecksum().stream()
            .map((c) -> c.getType() + ":" + c.getValue())
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
            builder.append("- Message: " + message.getMessage()+ "\n");
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
