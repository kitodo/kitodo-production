package org.kitodo.longtermpreservationvalidation.jhove;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.ArrayUtils;

import edu.harvard.hul.ois.jhove.NisoImageMetadata;

public class KitodoJhoveNisoImageMetadataHelper {

    public static final Map<String, Function<NisoImageMetadata, Long>> LONG_PROPERTIES_MAP = Map.ofEntries(
        Map.entry("ImageWidth", (metadata) -> metadata.getImageWidth()),
        Map.entry("ImageLength", (metadata) -> metadata.getImageLength())
    );

    public static final Map<String, Function<NisoImageMetadata, Integer>> INTEGER_PROPERTIES_MAP = Map.ofEntries(
        Map.entry("CompressionLevel", (metadata) -> metadata.getCompressionLevel())
    );

    public static final Map<String, Function<NisoImageMetadata, String>> STRING_PROPERTIES_MAP = Map.ofEntries(
        Map.entry("ByteOrder", (metadata) -> metadata.getByteOrder()),
        Map.entry("ProfileName", (metadata) -> metadata.getProfileName()),
        Map.entry("ProfileURL", (metadata) -> metadata.getProfileURL()),
        Map.entry("MimeType", (metadata) -> metadata.getMimeType())
    );

    public static final Map<String, Function<NisoImageMetadata, Integer>> LABEL_PROPERTIES_GETTER_MAP = Map.ofEntries(
        Map.entry("AutoFocus", (metadata) -> metadata.getAutoFocus()),
        Map.entry("BackLight", (metadata) -> metadata.getBackLight()),
        Map.entry("ChecksumMethod", (metadata) -> metadata.getChecksumMethod()),
        Map.entry("DisplayOrientation", (metadata) -> metadata.getDisplayOrientation()),
        Map.entry("Orientation", (metadata) -> metadata.getOrientation())
    );

    public static final Map<String, String[]> LABEL_PROPERTIES_LABEL_ARRAY_MAP = Map.ofEntries(
        Map.entry("AutoFocus", NisoImageMetadata.AUTOFOCUS),
        Map.entry("BackLight", NisoImageMetadata.BACKLIGHT),
        Map.entry("ChecksumMethod", NisoImageMetadata.CHECKSUM_METHOD),
        Map.entry("DisplayOrientation", NisoImageMetadata.DISPLAY_ORIENTATION),
        Map.entry("Orientation", NisoImageMetadata.ORIENTATION)
    );

    public static final Map<String, Function<NisoImageMetadata, Integer>> INDEXED_LABEL_PROPERTIES_GETTER_MAP = Map.ofEntries(
        Map.entry("ColorSpace", (metadata) -> metadata.getColorSpace()),
        Map.entry("CompressionScheme", (metadata) -> metadata.getCompressionScheme()),
        Map.entry("SceneIlluminant", (metadata) -> metadata.getSceneIlluminant())
    );

    public static final Map<String, String[]> INDEXED_LABEL_PROPERTIES_LABEL_ARRAY_MAP = Map.ofEntries(
        Map.entry("ColorSpace", NisoImageMetadata.COLORSPACE),
        Map.entry("CompressionScheme", NisoImageMetadata.COMPRESSION_SCHEME),
        Map.entry("SceneIlluminant", NisoImageMetadata.SCENE_ILLUMINANT)
    );

    public static final Map<String, int[]> INDEXED_LABEL_PROPERTIES_INDEX_ARRAY_MAP = Map.ofEntries(
        Map.entry("ColorSpace", NisoImageMetadata.COLORSPACE_INDEX),
        Map.entry("CompressionScheme", NisoImageMetadata.COMPRESSION_SCHEME_INDEX),
        Map.entry("SceneIlluminant", NisoImageMetadata.SCENE_ILLUMINANT_INDEX)
    );

    private static Optional<String> parseLabelFromInt(int value, String[] labelArray, int[] indexArray) {
        if (Objects.nonNull(value) && value != NisoImageMetadata.NULL) {
            int index = ArrayUtils.indexOf(indexArray, value);
            if (index != ArrayUtils.INDEX_NOT_FOUND) {
                return Optional.of(labelArray[index]);
            }
            return Optional.of(String.valueOf(value));
        }
        return Optional.empty();
    }
    
    public static Map<String, String> nisoImageMetadataToMap(NisoImageMetadata metadata) {
        Map<String, String> metadataMap = new HashMap<>();

        // long properties
        for (Map.Entry<String, Function<NisoImageMetadata, Long>> entry : LONG_PROPERTIES_MAP.entrySet()) {
            Long value = entry.getValue().apply(metadata);
            if (Objects.nonNull(value) && value != NisoImageMetadata.NULL) {
                metadataMap.put(entry.getKey(), String.valueOf(value));
            }            
        }

        // integer properties
        for (Map.Entry<String, Function<NisoImageMetadata, Integer>> entry : INTEGER_PROPERTIES_MAP.entrySet()) {
            Integer value = entry.getValue().apply(metadata);
            if (Objects.nonNull(value) && value != NisoImageMetadata.NULL) {
                metadataMap.put(entry.getKey(), String.valueOf(value));
            }            
        }        

        // string properties
        for (Map.Entry<String, Function<NisoImageMetadata, String>> entry : STRING_PROPERTIES_MAP.entrySet()) {
            String value = entry.getValue().apply(metadata);
            if (Objects.nonNull(value)) {
                metadataMap.put(entry.getKey(), value);
            }            
        }

        // label properties
        for (Map.Entry<String, Function<NisoImageMetadata, Integer>> entry : LABEL_PROPERTIES_GETTER_MAP.entrySet()) {
            Integer value = entry.getValue().apply(metadata);
            if (Objects.nonNull(value) && value != NisoImageMetadata.NULL) {
                String[] labelArray = LABEL_PROPERTIES_LABEL_ARRAY_MAP.get(entry.getKey());
                if (value >= 0 && value < labelArray.length) {
                    metadataMap.put(entry.getKey(), labelArray[value]);
                }
            }            
        }

        // indexed label properties
        for (Map.Entry<String, Function<NisoImageMetadata, Integer>> entry : INDEXED_LABEL_PROPERTIES_GETTER_MAP.entrySet()) {
            Integer value = entry.getValue().apply(metadata);
            if (Objects.nonNull(value) && value != NisoImageMetadata.NULL) {
                String[] labelArray = INDEXED_LABEL_PROPERTIES_LABEL_ARRAY_MAP.get(entry.getKey());
                int[] indexArray = INDEXED_LABEL_PROPERTIES_INDEX_ARRAY_MAP.get(entry.getKey());
                Optional<String> label = parseLabelFromInt(value, labelArray, indexArray);
                if (label.isPresent()) {
                    metadataMap.put(entry.getKey(), label.get());
                }
            }            
        }

        return metadataMap;
    }

}
