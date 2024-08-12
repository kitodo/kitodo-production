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
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.ProcessorException;
import org.kitodo.production.dto.ProcessDTO;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ImportConfigurationService;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Order to create a new process. This contains all the necessary data.
 */
public class CreateNewProcessOrder {

    /* Catalog imports can be specified (none, one or more). An
     * "importconfiguration" and a search "value" must be specified. The search
     * is carried out in the default search field. If no hit is found, or more
     * than one, the search aborts with an error message. In the case of
     * multiple imports, a repeated import is carried out according to the
     * procedure specified in the rule set. */
    private static final String FIELD_IMPORT = "import";
    private static final String FIELD_IMPORT_CONFIG = "importconfiguration";
    private static final String FIELD_IMPORT_VALUE = "value";

    /* Additionally metadata can be passed. Passing multiple metadata or passing
     * grouped metadata is also possible. */
    private static final String FIELD_METADATA = "metadata";

    /* A parent process can optionally be specified. The process ID or the
     * process title can be specified. (If the value is all digits, it is
     * considered the process ID, else it is considered the process title.) The
     * process must be found in the client’s processes. If no parent process is
     * specified, but a metadata entry with a use="higherLevelIdentifier" is
     * included in the data from the catalog, the parent process is searched for
     * using the metadata entry with use="recordIdentifier". It must already
     * exist for the client. No parent process is implicitly created. The child
     * process is added at the last position in the parent process. */
    private static final String FIELD_PARENT = "parent";

    // Mandatory information is the project ID.
    private static final String FIELD_PROJECT = "project";

    // Mandatory information is the process template.
    private static final String FIELD_TEMPLATE = "template";

    /* A process title can optionally be specified. If it is specified
     * explicitly, exactly this process title is used, otherwise the system
     * creates the process title according to the configured rule. The process
     * title must still be unused for the client who owns the project. */
    private static final String FIELD_TITLE = "title";

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
     * @throws DAOException
     *             if the ImportConfiguartionDAO is unable to find an import
     *             configuration with the given ID
     * @throws DataException
     *             if there is an error accessing the search service
     * @throws IllegalArgumentException
     *             If a required field is missing in the Active MQ message
     *             message, or contains inappropriate values.
     * @throws JMSException
     *             Defined by the JMS API. I have not seen any cases where this
     *             would actually be thrown in the calls used here.
     * @throws ProcessorException
     *             if the process count for the title is not exactly one
     */
    public CreateNewProcessOrder(MapMessageObjectReader ticket) throws DAOException, DataException, JMSException,
            ProcessorException {
        this.projectId = ticket.getMandatoryInteger(FIELD_PROJECT);
        this.templateId = ticket.getMandatoryInteger(FIELD_TEMPLATE);
        this.imports = convertImports(ticket.getList(FIELD_IMPORT));
        this.title = Optional.ofNullable(ticket.getString(FIELD_TITLE));
        this.parentId = Optional.ofNullable(convertProcessId(ticket.getString(FIELD_PARENT)));
        this.metadata = convertMetadata(ticket.getMapOfString(FIELD_METADATA));
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
    private static final List<Pair<ImportConfiguration, String>> convertImports(@Nullable List<?> imports)
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
            ImportConfiguration importconfiguration = importConfigurationService.getById(MapMessageObjectReader
                    .getMandatoryInteger(map, FIELD_IMPORT_CONFIG));
            String value = MapMessageObjectReader.getMandatoryString(map, FIELD_IMPORT_VALUE);
            result.add(Pair.of(importconfiguration, value));
        }
        return result;
    }

    /**
     * Gets the process ID. If the string is an integer, it is used as the
     * process ID. Otherwise it is considered a title and searched for. If it is
     * a title, there must be exactly one process for it to be converted to an
     * ID.
     * 
     * @param processId
     *            parent process reference
     * @return ID of the parent process
     * @throws DataException
     *             if there is an error accessing the search service
     * @throws ProcessorException
     *             if the process count for the title is not exactly one
     */
    @CheckForNull
    private static final Integer convertProcessId(String processId) throws DataException, ProcessorException {
        if (Objects.isNull(processId)) {
            return null;
        }
        if (processId.matches("\\d+")) {
            return Integer.valueOf(processId);
        } else {
            List<ProcessDTO> parents = ServiceManager.getProcessService().findByTitle(processId);
            if (parents.size() == 0) {
                throw new ProcessorException("Parent process not found");
            } else if (parents.size() > 1) {
                throw new ProcessorException("Parent process exists more than one");
            } else {
                return parents.get(0).getId();
            }
        }
    }

    /**
     * Converts metadata details into safe data objects. For {@code null}, it
     * will return an empty collection, never {@code null}.
     */
    private static final HashSet<Metadata> convertMetadata(@Nullable Map<?, ?> metadata) {

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
     * Returns the production template.
     * 
     * @return the template
     * @throws DAOException
     *             if the template cannot be loaded
     */
    public Template getTemplate() throws DAOException {
        return ServiceManager.getTemplateService().getById(templateId);
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
     * Returns the parent process, if any.
     * 
     * @return the parent process, or {@code null}
     * @throws DAOException
     *             if the process cannot be loaded
     */
    @CheckForNull
    public Process getParent() throws DAOException {
        return parentId.isPresent() ? ServiceManager.getProcessService().getById(parentId.get()) : null;
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
