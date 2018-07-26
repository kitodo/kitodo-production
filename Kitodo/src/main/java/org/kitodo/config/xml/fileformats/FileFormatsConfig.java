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
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.myfaces.util.FilenameUtils;
import org.kitodo.config.Config;

/**
 * A {@code kitodo_fileFormats.xml} config file. This class corresponds to the
 * {@code <kitodo_fileFormats>} tag in {@code kitodo_fileFormats.xml}.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"fileFormat" })
@XmlRootElement(name = "kitodo_fileFormats")
public class FileFormatsConfig {

    private final static File CONFIG_FILE = new File(
            FilenameUtils.concat(Config.getKitodoConfigDirectory(), "kitodo_fileFormats.xml"));

    @XmlElement(required = true)
    protected List<FileFormat> fileFormat;

    /**
     * Returns the list of configured file formats.
     *
     * @return the configured file formats
     * @throws IOException
     *             if reading from the file fails
     * @throws JAXBException
     *             if the content of the file is syntactically or semantically
     *             incorrect
     */
    public static List<FileFormat> getFileFormats() throws IOException, JAXBException {
        Unmarshaller fileFormatsConfig = JAXBContext.newInstance(FileFormatsConfig.class).createUnmarshaller();
        FileFormatsConfig read = (FileFormatsConfig) fileFormatsConfig.unmarshal(CONFIG_FILE);
        return read.fileFormat;
    }

    /**
     * Returns a map of configured file formats.
     *
     * @param keyMapper
     *            a mapping function to produce the keys of the map
     * @return a map of the configured file formats
     * @throws IOException
     *             if reading from the file fails
     * @throws JAXBException
     *             if the content of the file is syntactically or semantically
     *             incorrect
     */
    public static <T> Map<T, FileFormat> getFileFormats(Function<? super FileFormat, T> keyMapper)
            throws IOException, JAXBException {
        return getFileFormats().stream().collect(Collectors.toMap(keyMapper, Function.identity()));
    }

    /**
     * Returns a file format by its MIME type, if any.
     *
     * @param mimeType
     *            MIME type to look up
     * @return the file format
     * @throws IOException
     *             if reading from the file fails
     * @throws JAXBException
     *             if the content of the file is syntactically or semantically
     *             incorrect
     */
    public static Optional<FileFormat> getFileFormat(String mimeType) throws IOException, JAXBException {
        return Optional.ofNullable(getFileFormats(FileFormat::getMimeType).get(mimeType));
    }
}
