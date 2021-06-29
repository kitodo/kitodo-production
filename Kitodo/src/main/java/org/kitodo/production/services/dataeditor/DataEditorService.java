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

package org.kitodo.production.services.dataeditor;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.MetadataGroup;
import org.kitodo.api.dataeditor.DataEditorInterface;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.serviceloader.KitodoServiceLoader;

public class DataEditorService {

    /**
     * Reads the data of a given file in xml format. The format of that file
     * needs to be the corresponding to the one which is referenced by the data
     * editor module as data format module.
     *
     * @param xmlFileUri
     *            The path to the metadata file as URI.
     */
    public void readData(URI xmlFileUri) throws IOException {
        DataEditorInterface dataEditor = loadDataEditorModule();
        URI xsltFile = getXsltFileFromConfig();
        dataEditor.readData(xmlFileUri, xsltFile);
    }

    private DataEditorInterface loadDataEditorModule() {
        KitodoServiceLoader<DataEditorInterface> serviceLoader = new KitodoServiceLoader<>(DataEditorInterface.class);
        return serviceLoader.loadModule();
    }

    private URI getXsltFileFromConfig() {
        String path = getXsltFolder();
        String file = ConfigCore.getParameter(ParameterCore.XSLT_FILENAME_METADATA_TRANSFORMATION);
        return Paths.get(path + file).toUri();
    }

    private String getXsltFolder() {
        return ConfigCore.getParameter(ParameterCore.DIR_XSLT);
    }

    /**
     * Retrieve and return list of metadata keys that are used for displaying title information in the metadata editors
     * structure and gallery panels from the Kitodo configuration file.
     *
     * @return list of title metadata keys
     */
    public static List<String> getTitleKeys() {
        return Arrays.stream(ConfigCore.getParameter(ParameterCore.TITLE_KEYS, "").split(","))
                .map(String::trim).collect(Collectors.toList());
    }

    /**
     * Retrieve and return title value from given IncludedStructuralElement.
     *
     * @param element IncludedStructuralElement for which the title value is returned.
     * @return title value of given element
     */
    public static String getTitleValue(IncludedStructuralElement element) {
        for (final String titleKey : getTitleKeys()) {
            String[] metadataPath = titleKey.split("@");
            int lastIndex = metadataPath.length - 1;
            Collection<Metadata> metadata = element.getMetadata();
            for (int i = 0; i < lastIndex; i++) {
                final String metadataKey = metadataPath[i];
                metadata = metadata.stream()
                        .filter(currentMetadata -> Objects.equals(currentMetadata.getKey(), metadataKey))
                        .filter(MetadataGroup.class::isInstance).map(MetadataGroup.class::cast)
                        .flatMap(metadataGroup -> metadataGroup.getGroup().stream())
                        .collect(Collectors.toList());
            }
            Optional<String> metadataTitle = metadata.stream()
                    .filter(currentMetadata -> Objects.equals(currentMetadata.getKey(), metadataPath[lastIndex]))
                    .filter(MetadataEntry.class::isInstance).map(MetadataEntry.class::cast)
                    .map(MetadataEntry::getValue)
                    .filter(value -> !value.isEmpty())
                    .findFirst();
            if (metadataTitle.isPresent()) {
                return " - ".concat(metadataTitle.get());
            }
        }
        return "";
    }
}
