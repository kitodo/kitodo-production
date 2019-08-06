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
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.naming.ConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.config.ConfigProject;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.model.bibliography.course.Course;
import org.kitodo.production.model.bibliography.course.IndividualIssue;
import org.kitodo.production.process.field.AdditionalField;
import org.kitodo.production.services.ServiceManager;

public class NewspaperProcessesGenerator extends ProcessGenerator {
    private static final Logger logger = LogManager.getLogger(NewspaperProcessesGenerator.class);

    /**
     * Language for the ruleset. Since we are headless here, always English is
     * used.
     */
    private static final List<LanguageRange> ENGLISH = LanguageRange.parse("en");
    private static final int NUMBER_OF_INIT_STEPS = 1;
    private static final int NUMBER_OF_COMPLETION_STEPS = 1;

    protected final Process overallProcess;

    /**
     * The appearance history for which operations are to be created.
     */
    protected final Course course;

    /**
     * The current step. This class operates step by step and the long running
     * task can always be paused between two steps in Task Manager.
     */
    protected int currentStep = 0;

    /**
     * Uniform resource identifier of the location of the serialization of the
     * overall media presentation description.
     */
    private URI overallMetadataFileUri;

    /**
     * Object model of the overall media presentation description.
     */
    private Workpiece overallWorkpiece;

    /**
     * List of processes to be created. A process is characterized here only by
     * the issues contained therein.
     */
    private List<List<IndividualIssue>> processesToCreate;

    /**
     * A build statement for the process title, which can be interpreted by the
     * title generator.
     */
    private String titleDefinition;

    /**
     * The title generator is used to create the process titles.
     */
    private TitleGenerator titleGenerator;

    /**
     * Creates a new newspaper process generator.
     *
     * @param overallProcess
     *            Process that represents the entirety of the newspaper
     * @param course
     *            object model of the course of the issue
     */
    public NewspaperProcessesGenerator(Process overallProcess, Course course) {
        this.overallProcess = overallProcess;
        this.course = course;
    }

    /**
     * Runs the newspaper process generator. To create newspaper processes, call
     * this method.
     *
     * @throws ConfigurationException
     *             if the configuration is wrong
     * @throws DAOException
     *             if an error occurs while saving in the database
     * @throws DataException
     *             if an error occurs while saving in the database
     * @throws IOException
     *             if something goes wrong when reading or writing one of the
     *             affected files
     * @throws ProcessGenerationException
     *             if there is an item “Volume number” or “Bandnummer” in the
     *             projects configuration, but its value cannot be evaluated to
     *             an integer
     */
    public void run()
            throws ConfigurationException, DAOException, DataException, IOException, ProcessGenerationException {

        while (getProgress() < getNumberOfSteps()) {
            nextStep();
        }
    }

    public int getProgress() {
        return currentStep;
    }

    public int getNumberOfSteps() {
        return NUMBER_OF_INIT_STEPS + processesToCreate.size() + NUMBER_OF_COMPLETION_STEPS;
    }

    /**
     * Works the next step of the long-running task.
     */
    public void nextStep()
            throws ConfigurationException, DAOException, DataException, IOException, ProcessGenerationException {

        if (currentStep == 0) {
            initialize();
        } else if (currentStep - NUMBER_OF_INIT_STEPS < processesToCreate.size()) {
            createProcess(currentStep - NUMBER_OF_INIT_STEPS);
        } else {
            finish();
        }
    }

    protected void initialize() throws ConfigurationException, IOException {
        final long begin = System.nanoTime();

        overallMetadataFileUri = ServiceManager.getProcessService().getMetadataFileUri(overallProcess);
        overallWorkpiece = ServiceManager.getMetsService().loadWorkpiece(overallMetadataFileUri);

        ConfigProject configProject = new ConfigProject(overallProcess.getProject().getTitle());
        titleGenerator = initializeTitleGenerator(configProject, overallWorkpiece);
        titleDefinition = configProject.getTitleDefinition();

        processesToCreate = course.getProcesses();

        if (logger.isTraceEnabled()) {
            logger.trace("Initialization took {} ms", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin));
        }
    }

    /**
     * Initializes the title generator.
     *
     * @param overallProcess
     *            the overall course process
     * @param configProject
     *            the config project
     * @return the initialized title generator
     */
    private static TitleGenerator initializeTitleGenerator(ConfigProject configProject, Workpiece workpiece)
            throws IOException, ConfigurationException {

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
     * list as a string, separated by a horizontal line ({@code |}, U+007C).
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

    protected void createProcess(int index)
            throws DAOException, DataException, IOException, ProcessGenerationException {

        // TODO

    }

    protected void finish() throws DataException, IOException {

        // TODO

    }
}
