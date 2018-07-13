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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * A {@code kitodo_fileFormats.xml} config file. This class corresponds to the
 * {@code <kitodo_fileFormats>} tag in {@code kitodo_fileFormats.xml}.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"fileFormat" })
@XmlRootElement(name = "kitodo_fileFormats")
public class FileFormatsConfig {

    /**
     * Reads a kitodo_fileFormats config file from disk.
     *
     * @param file
     *            file to read
     * @return java object in memory
     * @throws JAXBException
     *             if reading fails
     */
    public static FileFormatsConfig read(File file) throws JAXBException {
        return (FileFormatsConfig) JAXBContext.newInstance(FileFormatsConfig.class).createUnmarshaller()
                .unmarshal(file);
    }

    @XmlElement(required = true)
    protected List<FileFormat> fileFormat;

    /**
     * Returns the list of configured file formats.
     *
     * @return the configured file formats
     */
    public List<FileFormat> getFileFormats() {
        if (fileFormat == null) {
            fileFormat = new ArrayList<FileFormat>();
        }
        return this.fileFormat;
    }

    /**
     * Returns a map of configured file formats.
     *
     * @return a map of the configured file formats
     */
    public <T> Map<T, FileFormat> getFileFormats(Function<? super FileFormat, T> keyMapper) {
        return getFileFormats().stream().collect(Collectors.toMap(keyMapper, Function.identity()));
    }

    /**
     * Returns a file format by its MIME type, if any.
     *
     * @param mimeType
     *            MIME type to look up
     * @return the file format
     */
    public Optional<FileFormat> getFileFormat(String mimeType) {
        return Optional.ofNullable(getFileFormats(FileFormat::getMimeType).get(mimeType));
    }
}
