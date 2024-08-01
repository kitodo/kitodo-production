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

package org.kitodo.production.interfaces.activemq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import javax.jms.JMSException;

import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.MetadataGroup;
import org.kitodo.data.database.beans.ImportConfiguration;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ImportConfigurationService;

import edu.umd.cs.findbugs.annotations.NonNull;
import net.bytebuddy.utility.nullability.MaybeNull;

/**
 * Order to create a new process. This contains all the necessary data.
 */
public class CreateNewProcessOrder {
    private final Integer projectId;
    private final Integer templateId;
    private final List<Pair<ImportConfiguration, String>> imports;
    private final Optional<String> title;
    private final Optional<Integer> parentId;
    private final Collection<Metadata> metadata;

    /**
     * Creates a new CreateNewProcessOrder from an Active MQ message.
     * 
     * @param ticket
     *            Active MQ message with (hopefully) all the data
     * @throws JMSException
     *             Defined by the JMS API. I have not seen any cases where this
     *             would actually be thrown in the calls used here.
     * @throws DAOException
     *             if the ImportConfiguartionDAO is unable to find an import
     *             configuration with the given ID
     * @throws IllegalArgumentException
     *             If a required field is missing in the Active MQ message
     *             message, or contains inappropriate values.
     */
    public CreateNewProcessOrder(MapMessageObjectReader ticket) throws JMSException, DAOException {
        this.projectId = ticket.getMandatoryInteger("project");
        this.templateId = ticket.getMandatoryInteger("template");
        this.imports = convertImports(ticket.getList("import"));
        this.title = Optional.ofNullable(ticket.getString("title"));
        this.parentId = Optional.ofNullable(ticket.getInteger("parent"));
        this.metadata = convertMetadata(ticket.getMapOfString("metadata"));
    }

    /**
     * Converts import details into safe data objects. For {@code null}, it will
     * return an empty list, never {@code null}.
     * 
     * @throws IllegalArgumentException
     *             if a list member is not a map, or one of the mandatory map
     *             entries is missing or of a wrong type
     * @throws DAOException
     *             if the ImportConfiguartionDAO is unable to find an import
     *             configuration with that ID
     */
    private static final List<Pair<ImportConfiguration, String>> convertImports(@MaybeNull List<?> imports)
            throws DAOException {

        if (Objects.isNull(imports) || imports.isEmpty()) {
            return Collections.emptyList();
        }

        final ImportConfigurationService importConfigurationService = ServiceManager.getImportConfigurationService();
        List<Pair<ImportConfiguration, String>> result = new ArrayList<>();
        for (Object dubious : imports) {
            if (!(dubious instanceof Map)) {
                throw new IllegalArgumentException("Entry of \"imports\" is not a map");
            }
            Map<?, ?> map = (Map<?, ?>) dubious;
            ImportConfiguration importconfiguration = importConfigurationService.getById(
                    MapMessageObjectReader.getMandatoryInteger(map, "importconfiguration"));
            String value = MapMessageObjectReader.getMandatoryString(map, "value");
            result.add(Pair.of(importconfiguration, value));
        }
        return result;
    }

    /**
     * Converts metadata details into safe data objects. For {@code null}, it
     * will return an empty collection, never {@code null}.
     */
    private static final HashSet<Metadata> convertMetadata(@MaybeNull Map<?, ?> metadata) {

        HashSet<Metadata> result = new HashSet<>();
        if (Objects.isNull(metadata)) {
            return result;
        }

        for (Entry<?, ?> entry : metadata.entrySet()) {
            Object dubiousKey = entry.getKey();
            if (!(dubiousKey instanceof String) || ((String) dubiousKey).isEmpty()) {
                throw new IllegalArgumentException("Invalid metadata key");
            }
            String key = (String) dubiousKey;

            Object dubiousValuesList = entry.getValue();
            if (!(dubiousValuesList instanceof List)) {
                dubiousValuesList = Collections.singletonList(dubiousValuesList);
            }
            for (Object dubiousValue : (List<?>) dubiousValuesList) {
                if (dubiousValue instanceof Map) {
                    MetadataGroup metadataGroup = new MetadataGroup();
                    metadataGroup.setKey(key);
                    metadataGroup.setMetadata(convertMetadata((Map<?, ?>) dubiousValue));
                    result.add(metadataGroup);
                } else {
                    MetadataEntry metadataEntry = new MetadataEntry();
                    metadataEntry.setKey(key);
                    metadataEntry.setValue(dubiousValue.toString());
                    result.add(metadataEntry);
                }
            }
        }
        return result;
    }

    /**
     * Returns the project ID. This is a mandatory field and can never be
     * {@code null}.
     * 
     * @return the project ID
     */
    @NonNull
    public Integer getProjectId() {
        return projectId;
    }

    /**
     * Returns the production template ID. This is a mandatory field and can
     * never be {@code null}.
     * 
     * @return the template ID
     */
    @NonNull
    public Integer getTemplateId() {
        return templateId;
    }

    /**
     * Returns import instructions. Each instruction consists of an import
     * configuration and a search value to be searched for in the default search
     * field. Subsequent search statements must be executed as additive imports.
     * Can be empty, but never {@code null}.
     * 
     * @return import instructions
     */
    @NonNull
    public List<Pair<ImportConfiguration, String>> getImports() {
        return imports;
    }

    /**
     * Returns an (optional) predefined title. If specified, this title must be
     * used. Otherwise, the title must be formed using the formation rule. Can
     * be {@code Optional.empty()}, but never {@code null}.
     * 
     * @return the title, if any
     */
    @NonNull
    public Optional<String> getTitle() {
        return title;
    }

    /**
     * Returns the (optional) parent record ID. If set, the process to be
     * created must be created as the new last child under this parent process.
     * Otherwise, a standalone process is created. Can be
     * {@code Optional.empty()}, but never {@code null}.
     * 
     * @return the title, if any
     */
    @NonNull
    public Optional<Integer> getParentId() {
        return parentId;
    }

    /**
     * Specifies the metadata for the logical structure root of the process to
     * be created. Can be empty, but never {@code null}.
     * 
     * @return
     */
    @NonNull
    public Collection<Metadata> getMetadata() {
        return metadata;
    }
}
