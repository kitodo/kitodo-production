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

package org.kitodo.config.xml.fileformats;

import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.kitodo.api.imagemanagement.ImageFileFormat;
import org.kitodo.api.validation.longtermpreservation.FileType;

/**
 * A file format supported by Production. Files of this format can be uploaded
 * to, stored in, and exported from Production. It may be possible to generate
 * or validate them. This class corresponds to the {@code <fileFormat>} tag in
 * {@code kitodo_fileFormats.xml}.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"label" })
public class FileFormat {
    /**
     * The undefined language.
     */
    private static final String UNDEFINED_LANGUAGE = "und";

    /**
     * The undefined language range.
     */
    private static final List<LanguageRange> UNDEFINED_LANGUAGE_RANGE = LanguageRange.parse(UNDEFINED_LANGUAGE);

    /**
     * The undefined locale.
     */
    private static final Locale UNDEFINED_LOCALE = Locale.forLanguageTag(UNDEFINED_LANGUAGE);

    @XmlAttribute(name = "extension", required = true)
    protected String extension;

    @XmlAttribute(name = "fileType", required = true)
    protected String fileType;

    @XmlAttribute(name = "formatName")
    protected String formatName;

    @XmlAttribute(name = "imageFileFormat")
    protected String imageFileFormat;

    @XmlElement(required = true)
    protected List<Label> label;

    @XmlAttribute(name = "mimeType")
    protected String mimeType;

    /**
     * Returns the file extension.
     *
     * @param withDot
     *            whether to return the extension with leading dot
     * @return the extension
     */
    public String getExtension(boolean withDot) {
        return withDot ? ".".concat(extension) : extension;
    }

    /**
     * Returns the Internet MIME type.
     *
     * @return the MIME type
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Returns the long time preservation validation file type of this file
     * format, if any.
     *
     * @return the validation file type
     * @throws IllegalArgumentException
     *             if the configured value dose not match a member of the
     *             enumeration
     */
    public Optional<FileType> getFileType() {
        return fileType == null ? Optional.empty() : Optional.of(FileType.valueOf(fileType));
    }

    /**
     * Returns the image management file format of this file format, if any.
     *
     * @return the image management file format
     * @throws IllegalArgumentException
     *             if the configured value dose not match a member of the
     *             enumeration
     */
    public Optional<ImageFileFormat> getImageFileFormat() {
        return imageFileFormat == null ? Optional.empty() : Optional.of(ImageFileFormat.valueOf(imageFileFormat));
    }

    /**
     * Returns the Java ImageIO writer format name of this file format, if any.
     *
     * @return the writer format name
     */
    public Optional<String> getFormatName() {
        return Optional.ofNullable(formatName);
    }

    /**
     * Returns the label for an undefined language range. Returns the MIME type,
     * if no label is defined for no language.
     *
     * @return the label for an undefined language range
     */
    public String getLabel() {
        return getLabel(UNDEFINED_LANGUAGE_RANGE);
    }

    /**
     * Returns the label. Returns the MIME type, if no label is defined in the
     * given language.
     *
     * @param languageRanges
     *            user language preferences
     * @return the label
     */
    public String getLabel(List<LanguageRange> languageRanges) {
        Map<Locale, String> labels = label.stream()
                .collect(Collectors.toMap(locale -> locale.getLanguage().orElse(UNDEFINED_LOCALE), Label::getValue));
        Locale lookup = Locale.lookup(languageRanges, labels.keySet());
        if (Objects.isNull(lookup) && !languageRanges.equals(UNDEFINED_LANGUAGE_RANGE)) {
            lookup = Locale.lookup(UNDEFINED_LANGUAGE_RANGE, labels.keySet());
        }
        return Optional.ofNullable(labels.get(lookup)).orElse(mimeType);
    }
}
