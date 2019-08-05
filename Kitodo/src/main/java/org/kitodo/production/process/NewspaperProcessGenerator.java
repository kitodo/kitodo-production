package org.kitodo.production.process;

import de.unigoettingen.sub.search.opac.ConfigOpac;
import de.unigoettingen.sub.search.opac.ConfigOpacDoctype;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.naming.ConfigurationException;

import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.config.ConfigProject;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.model.bibliography.course.Granularity;
import org.kitodo.production.model.bibliography.course.IndividualIssue;
import org.kitodo.production.process.field.AdditionalField;
import org.kitodo.production.services.ServiceManager;

public class NewspaperProcessGenerator extends ProcessGenerator {

    /**
     * @param overallCourse
     *            the overall course of the newspaper
     * @param issuesByProcess
     *            issues in the processes to be created
     * @param batchGranualarity
     *            granularity for batches to be produced
     */
    public void generateNewspaperProcesses(Process overallCourse, List<List<IndividualIssue>> issuesByProcess,
            Granularity batchGranualarity)
            throws ConfigurationException, DataException, IOException, ProcessGenerationException {

        int templateId = overallCourse.getTemplate().getId();
        int projectId = overallCourse.getProject().getId();
        ConfigProject configProject = new ConfigProject(overallCourse.getProject().getTitle());
        TitleGenerator titleGenerator = initializeTitleGenerator(overallCourse, configProject);
        String titleDefinition = configProject.getTitleDefinition();

        for (List<IndividualIssue> issues : issuesByProcess) {
            if (issues.isEmpty()) {
                continue;
            }

            generateProcess(templateId, projectId);
            String title = titleGenerator.generateTitle(titleDefinition, issues.get(0).getGenericFields());
            getGeneratedProcess().setTitle(title);
            ServiceManager.getProcessService().save(getGeneratedProcess());

            Workpiece workpiece = new Workpiece();
            workpiece.setRootElement(buildRootElement(issues));
        }
    }

    /**
     * Creates the root element. The topmost element is the month, as the
     * process links to the year process.
     *
     * @param issues
     *            issues in this process
     * @return the root element
     */
    private IncludedStructuralElement buildRootElement(List<IndividualIssue> issues,
            StructuralElementViewInterface monthView) {

        List<IncludedStructuralElement> monthIncludedStructuralElements = new ArrayList<>();
        IncludedStructuralElement currentMonthElement = new IncludedStructuralElement();
        currentMonthElement.setType(monthView.getId());

        // Rückgabe
        if (monthIncludedStructuralElements.size() == 1) {
            return monthIncludedStructuralElements.get(0);
        } else {
            IncludedStructuralElement holder = new IncludedStructuralElement();
            holder.getChildren().addAll(monthIncludedStructuralElements);
            return holder;
        }
    }


    /**
     * Initializes a title generator.
     *
     * @param overallCourse
     *            the overall course process
     * @param configProject
     *            the config project
     * @return the initialized title generator
     */
    private static TitleGenerator initializeTitleGenerator(Process overallCourse, ConfigProject configProject)
            throws IOException, ConfigurationException {

        URI metadataFileUri = ServiceManager.getProcessService().getMetadataFileUri(overallCourse);
        Workpiece workpiece = ServiceManager.getMetsService().loadWorkpiece(metadataFileUri);

        IncludedStructuralElement rootElement = workpiece.getRootElement();
        Map<String, Map<String, String>> metadata = new HashMap<>(4);
        Map<String, String> topstruct = getMetadataEntries(rootElement.getMetadata());
        metadata.put("topstruct", topstruct);
        List<IncludedStructuralElement> children = rootElement.getChildren();
        metadata.put("firstchild",
            children.isEmpty() ? Collections.emptyMap() : getMetadataEntries(children.get(0).getMetadata()));
        metadata.put("physSequence", getMetadataEntries(workpiece.getMediaUnit().getMetadata()));

        String docType = null;
        for (ConfigOpacDoctype configOpacDoctype : ConfigOpac.getAllDoctypes()) {
            if (configOpacDoctype.getRulesetType().equals(rootElement.getType())) {
                docType = configOpacDoctype.getTitle();
                break;
            }
        }

        List<AdditionalField> projectAdditionalFields = configProject.getAdditionalFields();
        List<AdditionalField> additionalFields = new ArrayList<>(projectAdditionalFields.size());
        for (AdditionalField additionalField : projectAdditionalFields) {
            if (isDocTypeAndNotIsNotDoctype(additionalField, docType)) {
                String value = metadata.getOrDefault(additionalField.getDocStruct(), Collections.emptyMap())
                        .get(additionalField.getMetadata());
                additionalField.setValue(value);
                additionalFields.add(additionalField);
            }
        }

        return new TitleGenerator(topstruct.getOrDefault("TSL_ATS", ""), additionalFields);
    }

    /**
     * Returns whether an additional field is assigned to the doc type and not
     * excluded from it. The {@code isDocType}s and {@code isNotDoctype}s are a
     * list as a string, separated by a horizontal line.
     *
     * @param additionalField
     *            the field in question
     * @param docType
     *            the doc type used
     * @return whether the field is assigned and not excluded
     */
    private static boolean isDocTypeAndNotIsNotDoctype(AdditionalField additionalField, String docType) {
        boolean isDocType = false;
        boolean isNotDoctype = false;
        String isDocTypes = additionalField.getIsDocType();
        if (Objects.nonNull(isDocTypes)) {
            for (String isDocTypeOption : isDocTypes.split("\\|")) {
                if (isDocTypeOption.equals(docType)) {
                    isDocType = true;
                    break;
                }
            }
        }
        String isNotDoctypes = additionalField.getIsNotDoctype();
        if (Objects.nonNull(isNotDoctypes)) {
            for (String isNotDoctypeOption : isNotDoctypes.split("\\|")) {
                if (isNotDoctypeOption.equals(docType)) {
                    isNotDoctype = true;
                    break;
                }
            }
        }
        return isDocType ^ !isNotDoctype;
    }

    /**
     * Reduces the metadata to metadata entries and returns them as a map.
     *
     * @param metadata
     *            all the metadata
     * @return the metadata entries as map
     */
    private static Map<String, String> getMetadataEntries(Collection<Metadata> metadata) {
        Map<String, String> metadataEntries = metadata.parallelStream().filter(MetadataEntry.class::isInstance)
                .map(MetadataEntry.class::cast).collect(Collectors.toMap(Metadata::getKey, MetadataEntry::getValue));
        return metadataEntries;
    }
}
