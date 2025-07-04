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
import edu.harvard.hul.ois.jhove.Rational;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Helper class to extract NisoImageMetadata into a simple String map.
 */
public class KitodoJhoveNisoImageMetadataHelper {

    /**
     * Getter functions that return a long.
     */
    public static final Map<String, Function<NisoImageMetadata, Long>> LONG_PROPERTIES_MAP = Map.ofEntries(
        Map.entry("ImageWidth", (metadata) -> metadata.getImageWidth()),
        Map.entry("ImageLength", (metadata) -> metadata.getImageLength()),
        Map.entry("FileSize", (metadata) -> metadata.getFileSize()),
        Map.entry("RowsPerStrip", (metadata) -> metadata.getRowsPerStrip()),
        Map.entry("TileLength", (metadata) -> metadata.getTileLength()),
        Map.entry("TileWidth", (metadata) -> metadata.getTileWidth()),
        Map.entry("XTargetedDisplayAR", (metadata) -> metadata.getXTargetedDisplayAR()),
        Map.entry("YTargetedDisplayAR", (metadata) -> metadata.getYTargetedDisplayAR())
    );

    /**
     * Getter functions that return a long array.
     */
    public static final Map<String, Function<NisoImageMetadata, long[]>> LONG_ARRAY_PROPERTIES_MAP = Map.ofEntries(
        Map.entry("StripByteCounts", (metadata) -> metadata.getStripByteCounts()),
        Map.entry("StripOffsets", (metadata) -> metadata.getStripOffsets()),
        Map.entry("TileByteCounts", (metadata) -> metadata.getTileByteCounts()),
        Map.entry("TileOffsets", (metadata) -> metadata.getTileOffsets())
    );           

    /**
     * Getter functions that return a double.
     */
    public static final Map<String, Function<NisoImageMetadata, Double>> DOUBLE_PROPERTIES_MAP = Map.ofEntries(
        Map.entry("ColorTemp", (metadata) -> metadata.getColorTemp()),
        Map.entry("ExposureIndex", (metadata) -> metadata.getExposureIndex()),
        Map.entry("ExposureTime", (metadata) -> metadata.getExposureTime()),
        Map.entry("FNumber", (metadata) -> metadata.getFNumber()),
        Map.entry("FocalLength", (metadata) -> metadata.getFocalLength()),
        Map.entry("PixelSize", (metadata) -> metadata.getPixelSize()),
        Map.entry("SourceXDimension", (metadata) -> metadata.getSourceXDimension()),
        Map.entry("SourceYDimension", (metadata) -> metadata.getSourceYDimension()),
        Map.entry("XPrintAspectRatio", (metadata) -> metadata.getXPrintAspectRatio()),
        Map.entry("XPhysScanResolution", (metadata) -> metadata.getXPhysScanResolution()),
        Map.entry("YPrintAspectRatio", (metadata) -> metadata.getYPrintAspectRatio()),
        Map.entry("YPhysScanResolution", (metadata) -> metadata.getYPhysScanResolution())
    );

    /**
     * Getter functions that return a double array.
     */
    public static final Map<String, Function<NisoImageMetadata, double[]>> DOUBLE_ARRAY_PROPERTIES_MAP = Map.ofEntries(
        Map.entry("SubjectDistance", (metadata) -> metadata.getSubjectDistance())
    );

    /**
     * Getter functions that return an integer.
     */
    public static final Map<String, Function<NisoImageMetadata, Integer>> INTEGER_PROPERTIES_MAP = Map.ofEntries(
        Map.entry("CompressionLevel", (metadata) -> metadata.getCompressionLevel()),
        Map.entry("SamplesPerPixel", (metadata) -> metadata.getSamplesPerPixel())
    );

    /**
     * Getter functions that return an integer array.
     */
    public static final Map<String, Function<NisoImageMetadata, int[]>> INTEGER_ARRAY_PROPERTIES_MAP = Map.ofEntries(
        Map.entry("BitsPerSample", (metadata) -> metadata.getBitsPerSample()),
        Map.entry("ColormapBitCodeValue", (metadata) -> metadata.getColormapBitCodeValue()),
        Map.entry("ColormapBlueValue", (metadata) -> metadata.getColormapBlueValue()),
        Map.entry("ColormapGreenValue", (metadata) -> metadata.getColormapGreenValue()),
        Map.entry("ColormapRedValue", (metadata) -> metadata.getColormapRedValue()),
        Map.entry("GrayResponseCurve", (metadata) -> metadata.getGrayResponseCurve()),
        Map.entry("YCbCrSubSampling", (metadata) -> metadata.getYCbCrSubSampling())
    );

    /**
     * Getter functions that return a Rational.
     */
    public static final Map<String, Function<NisoImageMetadata, Rational>> RATIONAL_PROPERTIES_MAP = Map.ofEntries(
        Map.entry("Brightness", (metadata) -> metadata.getBrightness()),
        Map.entry("ExposureBias", (metadata) -> metadata.getExposureBias()),
        Map.entry("FlashEnergy", (metadata) -> metadata.getFlashEnergy()),
        Map.entry("MaxApertureValue", (metadata) -> metadata.getMaxApertureValue()),
        Map.entry("PrimaryChromaticitiesBlueX", (metadata) -> metadata.getPrimaryChromaticitiesBlueX()),
        Map.entry("PrimaryChromaticitiesBlueY", (metadata) -> metadata.getPrimaryChromaticitiesBlueY()),
        Map.entry("PrimaryChromaticitiesGreenX", (metadata) -> metadata.getPrimaryChromaticitiesGreenX()),
        Map.entry("PrimaryChromaticitiesGreenY", (metadata) -> metadata.getPrimaryChromaticitiesGreenY()),
        Map.entry("PrimaryChromaticitiesRedX", (metadata) -> metadata.getPrimaryChromaticitiesRedX()),
        Map.entry("PrimaryChromaticitiesRedY", (metadata) -> metadata.getPrimaryChromaticitiesRedY()),
        Map.entry("WhitePointXValue", (metadata) -> metadata.getWhitePointXValue()),
        Map.entry("WhitePointYValue", (metadata) -> metadata.getWhitePointYValue()),
        Map.entry("XSamplingFrequency", (metadata) -> metadata.getXSamplingFrequency()),
        Map.entry("YSamplingFrequency", (metadata) -> metadata.getYSamplingFrequency())
    );

    /**
     * Getter functions that return a Rational array.
     */
    public static final Map<String, Function<NisoImageMetadata, Rational[]>> RATIONAL_ARRAY_PROPERTIES_MAP = Map.ofEntries(
        Map.entry("ReferenceBlackWhite", (metadata) -> metadata.getReferenceBlackWhite()),
        Map.entry("YCbCrCoefficients", (metadata) -> metadata.getYCbCrCoefficients())
    );

    /**
     * Getter functions that return a string.
     */
    public static final Map<String, Function<NisoImageMetadata, String>> STRING_PROPERTIES_MAP = Map.ofEntries(
        Map.entry("ByteOrder", (metadata) -> metadata.getByteOrder()),
        Map.entry("ChecksumValue", (metadata) -> metadata.getChecksumValue()),
        Map.entry("ColormapReference", (metadata) -> metadata.getColormapReference()),
        Map.entry("DateTimeCreated", (metadata) -> metadata.getDateTimeCreated()),
        Map.entry("DateTimeProcessed", (metadata) -> metadata.getDateTimeProcessed()),
        Map.entry("DeviceSource", (metadata) -> metadata.getDeviceSource()),
        Map.entry("DigitalCameraManufacturer", (metadata) -> metadata.getDigitalCameraManufacturer()),
        Map.entry("DigitalCameraModelName", (metadata) -> metadata.getDigitalCameraModelName()),
        Map.entry("DigitalCameraModelNumber", (metadata) -> metadata.getDigitalCameraModelNumber()),
        Map.entry("DigitalCameraModelSerialNo", (metadata) -> metadata.getDigitalCameraModelSerialNo()),
        Map.entry("ExifVersion", (metadata) -> metadata.getExifVersion()),
        Map.entry("HostComputer", (metadata) -> metadata.getHostComputer()),
        Map.entry("ImageData", (metadata) -> metadata.getImageData()),
        Map.entry("ImageIdentifier", (metadata) -> metadata.getImageIdentifier()),
        Map.entry("ImageIdentifierLocation", (metadata) -> metadata.getImageIdentifierLocation()),
        Map.entry("ImageProducer", (metadata) -> metadata.getImageProducer()),
        Map.entry("Methodology", (metadata) -> metadata.getMethodology()),
        Map.entry("MimeType", (metadata) -> metadata.getMimeType()),
        Map.entry("OS", (metadata) -> metadata.getOS()),
        Map.entry("OSVersion", (metadata) -> metadata.getOSVersion()),
        Map.entry("PerformanceData", (metadata) -> metadata.getPerformanceData()),
        Map.entry("PreferredPresentation", (metadata) -> metadata.getPreferredPresentation()),
        Map.entry("ProcessingAgency", (metadata) -> metadata.getProcessingAgency()),
        Map.entry("ProcessingSoftwareName", (metadata) -> metadata.getProcessingSoftwareName()),
        Map.entry("ProcessingSoftwareVersion", (metadata) -> metadata.getProcessingSoftwareVersion()),
        Map.entry("ProfileName", (metadata) -> metadata.getProfileName()),
        Map.entry("ProfileURL", (metadata) -> metadata.getProfileURL()),
        Map.entry("ScannerManufacturer", (metadata) -> metadata.getScannerManufacturer()),
        Map.entry("ScannerModelName", (metadata) -> metadata.getScannerModelName()),
        Map.entry("ScannerModelNumber", (metadata) -> metadata.getScannerModelNumber()),
        Map.entry("ScannerModelSerialNo", (metadata) -> metadata.getScannerModelSerialNo()),
        Map.entry("ScanningSoftware", (metadata) -> metadata.getScanningSoftware()),
        Map.entry("ScanningSoftwareVersionNo", (metadata) -> metadata.getScanningSoftwareVersionNo()),
        Map.entry("SourceData", (metadata) -> metadata.getSourceData()),
        Map.entry("SourceID", (metadata) -> metadata.getSourceID()),
        Map.entry("SourceType", (metadata) -> metadata.getSourceType()),
        Map.entry("TargetIDManufacturer", (metadata) -> metadata.getTargetIDManufacturer()),
        Map.entry("TargetIDMedia", (metadata) -> metadata.getTargetIDMedia()),
        Map.entry("TargetIDName", (metadata) -> metadata.getTargetIDName()),
        Map.entry("TargetIDNo", (metadata) -> metadata.getTargetIDNo())
    );

    /**
     * Getter functions that return a string array.
     */
    public static final Map<String, Function<NisoImageMetadata, String[]>> STRING_ARRAY_PROPERTIES_MAP = Map.ofEntries(
        Map.entry("ProcessingActions", (metadata) -> metadata.getProcessingActions())
    );

    /**
     * Getter functions that return an index into a label array.
     */
    public static final Map<String, Function<NisoImageMetadata, Integer>> SINGLE_LABEL_PROPERTIES_GETTER_MAP = Map.ofEntries(
        Map.entry("AutoFocus", (metadata) -> metadata.getAutoFocus()),
        Map.entry("BackLight", (metadata) -> metadata.getBackLight()),
        Map.entry("ChecksumMethod", (metadata) -> metadata.getChecksumMethod()),
        Map.entry("DisplayOrientation", (metadata) -> metadata.getDisplayOrientation()),
        Map.entry("ExposureProgram", (metadata) -> metadata.getExposureProgram()),
        Map.entry("Flash", (metadata) -> metadata.getFlash()),
        Map.entry("FlashReturn", (metadata) -> metadata.getFlashReturn()),
        Map.entry("GrayResponseUnit_02", (metadata) -> metadata.getGrayResponseUnit()),
        Map.entry("GrayResponseUnit_20", (metadata) -> metadata.getGrayResponseUnit()),
        Map.entry("MeteringMode", (metadata) -> metadata.getMeteringMode()),
        Map.entry("Orientation", (metadata) -> metadata.getOrientation()),
        Map.entry("PlanarConfiguration", (metadata) -> metadata.getPlanarConfiguration()),
        Map.entry("SamplingFrequencyPlane", (metadata) -> metadata.getSamplingFrequencyPlane()),
        Map.entry("SamplingFrequencyUnit", (metadata) -> metadata.getSamplingFrequencyUnit()),
        Map.entry("SegmentType", (metadata) -> metadata.getSegmentType()),
        Map.entry("Sensor", (metadata) -> metadata.getSensor()),
        Map.entry("SourceXDimensionUnit", (metadata) -> metadata.getSourceXDimensionUnit()),
        Map.entry("SourceYDimensionUnit", (metadata) -> metadata.getSourceYDimensionUnit()),
        Map.entry("YCbCrPositioning", (metadata) -> metadata.getYCbCrPositioning()),
        Map.entry("TargetType", (metadata) -> metadata.getTargetType())
    );

    /** 
     * Get functions that return an array of indexes into a label array.
     */
    public static final Map<String, Function<NisoImageMetadata, int[]>> MULTIPLE_LABEL_PROPERTIES_GETTER_MAP = Map.ofEntries(
        Map.entry("ExtraSamples", (metadata) -> metadata.getExtraSamples())
    );    

    /**
     * Label arrays for getter functions that return an index into this array.
     */
    public static final Map<String, String[]> LABEL_PROPERTIES_LABEL_ARRAY_MAP = Map.ofEntries(
        Map.entry("AutoFocus", NisoImageMetadata.AUTOFOCUS),
        Map.entry("BackLight", NisoImageMetadata.BACKLIGHT),
        Map.entry("ChecksumMethod", NisoImageMetadata.CHECKSUM_METHOD),
        Map.entry("DisplayOrientation", NisoImageMetadata.DISPLAY_ORIENTATION),
        Map.entry("ExtraSamples", NisoImageMetadata.EXTRA_SAMPLES),
        Map.entry("ExposureProgram", NisoImageMetadata.EXPOSURE_PROGRAM),
        Map.entry("Flash", NisoImageMetadata.FLASH),
        Map.entry("FlashReturn", NisoImageMetadata.FLASH_RETURN),
        Map.entry("GrayResponseUnit_02", NisoImageMetadata.GRAY_RESPONSE_UNIT_02),
        Map.entry("GrayResponseUnit_20", NisoImageMetadata.GRAY_RESPONSE_UNIT_20),
        Map.entry("MeteringMode", NisoImageMetadata.METERING_MODE),
        Map.entry("Orientation", NisoImageMetadata.ORIENTATION),
        Map.entry("PlanarConfiguration", NisoImageMetadata.PLANAR_CONFIGURATION),
        Map.entry("SamplingFrequencyPlane", NisoImageMetadata.SAMPLING_FREQUENCY_PLANE),
        Map.entry("SamplingFrequencyUnit", NisoImageMetadata.SAMPLING_FREQUENCY_UNIT),
        Map.entry("SegmentType", NisoImageMetadata.SEGMENT_TYPE),
        Map.entry("Sensor", NisoImageMetadata.SENSOR),
        Map.entry("SourceXDimensionUnit", NisoImageMetadata.SOURCE_DIMENSION_UNIT),
        Map.entry("SourceYDimensionUnit", NisoImageMetadata.SOURCE_DIMENSION_UNIT),
        Map.entry("YCbCrPositioning", NisoImageMetadata.YCBCR_POSITIONING),
        Map.entry("TargetType", NisoImageMetadata.TARGET_TYPE)
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
     * 
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
     * Extract single label properties from niso image metadata and add them to the metadata map.
     * 
     * @param metadata the niso image metadata
     * @param map the simple string map that is being filled
     */
    private static void extractSingleLabelProperties(NisoImageMetadata metadata, Map<String, String> map) {
        // label properties
        for (Map.Entry<String, Function<NisoImageMetadata, Integer>> entry : SINGLE_LABEL_PROPERTIES_GETTER_MAP.entrySet()) {
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
     * Extract multiple label properties from niso image metadata and add them to the metadata map.
     * 
     * @param metadata the niso image metadata
     * @param map the simple string map that is being filled
     */
    private static void extractMultipleLabelProperties(NisoImageMetadata metadata, Map<String, String> map) {
        // label properties
        for (Map.Entry<String, Function<NisoImageMetadata, int[]>> entry : MULTIPLE_LABEL_PROPERTIES_GETTER_MAP.entrySet()) {
            int[] values = entry.getValue().apply(metadata);
            if (Objects.nonNull(values)) {
                String[] labelArray = LABEL_PROPERTIES_LABEL_ARRAY_MAP.get(entry.getKey());
                String joined = Arrays.stream(values)
                    .filter((idx) -> idx >= 0 && idx < labelArray.length)
                    .mapToObj((idx) -> labelArray[idx])
                    .collect(Collectors.joining(","));
                if (!joined.isEmpty()) {
                    map.put(entry.getKey(), joined);
                }
            }
        }
    }

    /**
     * Extract number properties from niso image metadata and add them to the metadata map.
     * 
     * @param metadata the niso image metadata
     * @param map the simple string map that is being filled
     */
    private static void extractNumberProperties(NisoImageMetadata metadata, Map<String, String> map) {
        // long properties
        for (Map.Entry<String, Function<NisoImageMetadata, Long>> entry : LONG_PROPERTIES_MAP.entrySet()) {
            Long value = entry.getValue().apply(metadata);
            if (Objects.nonNull(value) && value != NisoImageMetadata.NULL) {
                map.put(entry.getKey(), String.valueOf(value));
            }
        }

        // double properties
        for (Map.Entry<String, Function<NisoImageMetadata, Double>> entry : DOUBLE_PROPERTIES_MAP.entrySet()) {
            Double value = entry.getValue().apply(metadata);
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

        // rational properties
        for (Map.Entry<String, Function<NisoImageMetadata, Rational>> entry : RATIONAL_PROPERTIES_MAP.entrySet()) {
            Rational value = entry.getValue().apply(metadata);
            if (Objects.nonNull(value)) {
                map.put(entry.getKey(), value.toString());
            }
        }
    }

    /**
     * Extract number array properties from niso image metadata and add them to the metadata map.
     * 
     * @param metadata the niso image metadata
     * @param map the simple string map that is being filled
     */
    private static void extractNumberArrayProperties(NisoImageMetadata metadata, Map<String, String> map) {
        // long array properties
        for (Map.Entry<String, Function<NisoImageMetadata, long[]>> entry: LONG_ARRAY_PROPERTIES_MAP.entrySet()) {
            long[] values = entry.getValue().apply(metadata);
            if (Objects.nonNull(values)) {
                map.put(entry.getKey(), Arrays.toString(values));
            }
        }

        // double array properties
        for (Map.Entry<String, Function<NisoImageMetadata, double[]>> entry: DOUBLE_ARRAY_PROPERTIES_MAP.entrySet()) {
            double[] values = entry.getValue().apply(metadata);
            if (Objects.nonNull(values)) {
                map.put(entry.getKey(), Arrays.toString(values));
            }
        }

        // integer array properties
        for (Map.Entry<String, Function<NisoImageMetadata, int[]>> entry: INTEGER_ARRAY_PROPERTIES_MAP.entrySet()) {
            int[] values = entry.getValue().apply(metadata);
            if (Objects.nonNull(values)) {
                map.put(entry.getKey(), Arrays.toString(values));
            }
        }

        // rationa array properties
        for (Map.Entry<String, Function<NisoImageMetadata, Rational[]>> entry: RATIONAL_ARRAY_PROPERTIES_MAP.entrySet()) {
            Rational[] values = entry.getValue().apply(metadata);
            if (Objects.nonNull(values)) {
                String joined = Arrays.stream(values)
                    .filter(Objects::nonNull)
                    .map((r) -> r.toString())
                    .collect(Collectors.joining(","));
                if (!joined.isEmpty()) {
                    map.put(entry.getKey(), joined);
                }
            }
        }
    }

    /**
     * Extract string properties from niso image metadata and add them to the metadata map.
     * 
     * @param metadata the niso image metadata
     * @param map the simple string map that is being filled
     */
    private static void extractStringProperties(NisoImageMetadata metadata, Map<String, String> map) {
        // string properties
        for (Map.Entry<String, Function<NisoImageMetadata, String>> entry : STRING_PROPERTIES_MAP.entrySet()) {
            String value = entry.getValue().apply(metadata);
            if (Objects.nonNull(value)) {
                map.put(entry.getKey(), value);
            }
        }

        // string array properties
        for (Map.Entry<String, Function<NisoImageMetadata, String[]>> entry : STRING_ARRAY_PROPERTIES_MAP.entrySet()) {
            String[] value = entry.getValue().apply(metadata);
            if (Objects.nonNull(value)) {
                map.put(entry.getKey(), Arrays.toString(value));
            }
        }
    }

    /**
     * Convert niso image metadata into a simple string map by calling all supported getter functions.
     * 
     * @param metadata the niso image metadata
     * @return a simple string map containing extracted properties and values
     */
    public static Map<String, String> nisoImageMetadataToMap(NisoImageMetadata metadata) {
        Map<String, String> map = new HashMap<>();

        extractStringProperties(metadata, map);
        extractNumberProperties(metadata, map);
        extractNumberArrayProperties(metadata, map);
        extractSingleLabelProperties(metadata, map);
        extractMultipleLabelProperties(metadata, map);
        extractIndexedLabelProperties(metadata, map);

        return map;
    }
}
