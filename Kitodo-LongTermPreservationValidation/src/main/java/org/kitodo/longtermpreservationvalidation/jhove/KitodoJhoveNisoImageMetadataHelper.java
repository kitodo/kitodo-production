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

import edu.harvard.hul.ois.jhove.NisoImageMetadata;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.ArrayUtils;



/**
 * Helper class to extract NisoImageMetadatainto simple String map.
 */
public class KitodoJhoveNisoImageMetadataHelper {

    /**
     * Getter functions that return a long.
     */
    public static final Map<String, Function<NisoImageMetadata, Long>> LONG_PROPERTIES_MAP = Map.ofEntries(
        Map.entry("ImageWidth", (metadata) -> metadata.getImageWidth()),
        Map.entry("ImageLength", (metadata) -> metadata.getImageLength())
    );

    /**
     * Getter functions that return an integer.
     */
    public static final Map<String, Function<NisoImageMetadata, Integer>> INTEGER_PROPERTIES_MAP = Map.ofEntries(
        Map.entry("CompressionLevel", (metadata) -> metadata.getCompressionLevel()),
        Map.entry("SamplesPerPixel", (metadata) -> metadata.getSamplesPerPixel())
    );

    /**
     * Getter functions that return a string.
     */
    public static final Map<String, Function<NisoImageMetadata, String>> STRING_PROPERTIES_MAP = Map.ofEntries(
        Map.entry("ByteOrder", (metadata) -> metadata.getByteOrder()),
        Map.entry("ProfileName", (metadata) -> metadata.getProfileName()),
        Map.entry("ProfileURL", (metadata) -> metadata.getProfileURL()),
        Map.entry("MimeType", (metadata) -> metadata.getMimeType())
    );

    /**
     * Getter functions that return an index into a label array.
     */
    public static final Map<String, Function<NisoImageMetadata, Integer>> LABEL_PROPERTIES_GETTER_MAP = Map.ofEntries(
        Map.entry("AutoFocus", (metadata) -> metadata.getAutoFocus()),
        Map.entry("BackLight", (metadata) -> metadata.getBackLight()),
        Map.entry("ChecksumMethod", (metadata) -> metadata.getChecksumMethod()),
        Map.entry("DisplayOrientation", (metadata) -> metadata.getDisplayOrientation()),
        Map.entry("Orientation", (metadata) -> metadata.getOrientation())
    );

    /**
     * Label arrays for getter functions that return an index into this array.
     */
    public static final Map<String, String[]> LABEL_PROPERTIES_LABEL_ARRAY_MAP = Map.ofEntries(
        Map.entry("AutoFocus", NisoImageMetadata.AUTOFOCUS),
        Map.entry("BackLight", NisoImageMetadata.BACKLIGHT),
        Map.entry("ChecksumMethod", NisoImageMetadata.CHECKSUM_METHOD),
        Map.entry("DisplayOrientation", NisoImageMetadata.DISPLAY_ORIENTATION),
        Map.entry("Orientation", NisoImageMetadata.ORIENTATION)
    );

    /**
     * Getter functions that return an index into an indexed label array.
     */
    public static final Map<String, Function<NisoImageMetadata, Integer>> INDEXED_LABEL_PROPERTIES_GETTER_MAP = Map.ofEntries(
        Map.entry("ColorSpace", (metadata) -> metadata.getColorSpace()),
        Map.entry("CompressionScheme", (metadata) -> metadata.getCompressionScheme()),
        Map.entry("SceneIlluminant", (metadata) -> metadata.getSceneIlluminant())
    );

    /**
     * Label arrays for getter functions that return an index into an indexed label array.
     */
    public static final Map<String, String[]> INDEXED_LABEL_PROPERTIES_LABEL_ARRAY_MAP = Map.ofEntries(
        Map.entry("ColorSpace", NisoImageMetadata.COLORSPACE),
        Map.entry("CompressionScheme", NisoImageMetadata.COMPRESSION_SCHEME),
        Map.entry("SceneIlluminant", NisoImageMetadata.SCENE_ILLUMINANT)
    );

    /**
     * Index arrays for getter functions that return an index into an indexed label array.
     */
    public static final Map<String, int[]> INDEXED_LABEL_PROPERTIES_INDEX_ARRAY_MAP = Map.ofEntries(
        Map.entry("ColorSpace", NisoImageMetadata.COLORSPACE_INDEX),
        Map.entry("CompressionScheme", NisoImageMetadata.COMPRESSION_SCHEME_INDEX),
        Map.entry("SceneIlluminant", NisoImageMetadata.SCENE_ILLUMINANT_INDEX)
    );

    /**
     * Resolves a label by following the index through an index and label array.
     * 
     * @param value the index
     * @param labelArray the label array
     * @param indexArray the index array
     * @return the label
     */
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

    /**
     * Extract indexed label properties and add them to the metadata map.
     * @param metadata the niso image matadata 
     * @param map the simple string map that is being filled
     */
    private static void extractIndexedLabelProperties(NisoImageMetadata metadata, Map<String, String> map) {
        // indexed label properties
        for (Map.Entry<String, Function<NisoImageMetadata, Integer>> entry : INDEXED_LABEL_PROPERTIES_GETTER_MAP.entrySet()) {
            Integer value = entry.getValue().apply(metadata);
            if (Objects.nonNull(value) && value != NisoImageMetadata.NULL) {
                String[] labelArray = INDEXED_LABEL_PROPERTIES_LABEL_ARRAY_MAP.get(entry.getKey());
                int[] indexArray = INDEXED_LABEL_PROPERTIES_INDEX_ARRAY_MAP.get(entry.getKey());
                Optional<String> label = parseLabelFromInt(value, labelArray, indexArray);
                if (label.isPresent()) {
                    map.put(entry.getKey(), label.get());
                }
            }            
        }
    }

    /**
     * Extract label properties from niso image metadata and add them to the metadata map.
     * @param metadata the niso image metadata
     * @param map the simple string map that is being filled
     */
    private static void extractLabelProperties(NisoImageMetadata metadata, Map<String, String> map) {
        // label properties
        for (Map.Entry<String, Function<NisoImageMetadata, Integer>> entry : LABEL_PROPERTIES_GETTER_MAP.entrySet()) {
            Integer value = entry.getValue().apply(metadata);
            if (Objects.nonNull(value) && value != NisoImageMetadata.NULL) {
                String[] labelArray = LABEL_PROPERTIES_LABEL_ARRAY_MAP.get(entry.getKey());
                if (value >= 0 && value < labelArray.length) {
                    map.put(entry.getKey(), labelArray[value]);
                }
            }            
        }
    }

    /**
     * Convert niso image metadata into a simple string map by calling all supported getter functions.
     * @param metadata the niso image metadata
     * @return a simple string map containing extracted properties and values
     */
    public static Map<String, String> nisoImageMetadataToMap(NisoImageMetadata metadata) {
        Map<String, String> map = new HashMap<>();

        // long properties
        for (Map.Entry<String, Function<NisoImageMetadata, Long>> entry : LONG_PROPERTIES_MAP.entrySet()) {
            Long value = entry.getValue().apply(metadata);
            if (Objects.nonNull(value) && value != NisoImageMetadata.NULL) {
                map.put(entry.getKey(), String.valueOf(value));
            }
        }

        // integer properties
        for (Map.Entry<String, Function<NisoImageMetadata, Integer>> entry : INTEGER_PROPERTIES_MAP.entrySet()) {
            Integer value = entry.getValue().apply(metadata);
            if (Objects.nonNull(value) && value != NisoImageMetadata.NULL) {
                map.put(entry.getKey(), String.valueOf(value));
            }
        }

        // string properties
        for (Map.Entry<String, Function<NisoImageMetadata, String>> entry : STRING_PROPERTIES_MAP.entrySet()) {
            String value = entry.getValue().apply(metadata);
            if (Objects.nonNull(value)) {
                map.put(entry.getKey(), value);
            }
        }

        extractLabelProperties(metadata, map);
        extractIndexedLabelProperties(metadata, map);

        return map;
    }
}
