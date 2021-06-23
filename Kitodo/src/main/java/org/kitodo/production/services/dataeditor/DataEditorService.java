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
import java.util.List;
import java.util.Objects;
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
        Metadata metadata;
        for (final String titleKey : getTitleKeys()) {
            metadata = null;
            if (titleKey.contains("@")) {
                String[] metadataPath = titleKey.split("@");
                int i = 0;
                while (i < metadataPath.length) {
                    final String pathPart = metadataPath[i];
                    if (i == 0) {
                        // _first_ metadata child elements must be taken from structure element
                        metadata = element.getMetadata().stream()
                                .filter(m -> m.getKey().equals(pathPart)).findFirst().orElse(null);
                    } else if (i == metadataPath.length - 1 && metadata instanceof MetadataGroup) {
                        // _last_ metadata element must be MetadataEntry (must not have children)
                        metadata = ((MetadataGroup)metadata).getGroup().stream()
                                .filter(m -> m.getKey().equals(pathPart) && m instanceof MetadataEntry)
                                .findFirst().orElse(null);
                    } else {
                        // all intermediate path parts must represent metadata groups
                        metadata = ((MetadataGroup)metadata).getGroup().stream()
                                .filter(m -> m.getKey().equals(pathPart)).findFirst().orElse(null);
                    }
                    // skip current title key if no matching metadata could be found for current title key path part
                    if (Objects.isNull(metadata)) {
                        break;
                    }
                    i++;
                }
            } else {
                // if title key did not contain an "@" sign, use the whole string as metadata key instead
                metadata = element.getMetadata().stream()
                        .filter(m -> m.getKey().equals(titleKey)).findFirst().orElse(null);
            }
            if (metadata instanceof MetadataEntry && !((MetadataEntry) metadata).getValue().isEmpty()) {
                return " - " + ((MetadataEntry) metadata).getValue();
            }
        }
        return "";
    }
}
