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

package org.kitodo.production.services.validation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.ComplexMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewWithValuesInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.mets.AreaXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.DivXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.FileXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.MetadataAccessInterface;
import org.kitodo.api.dataformat.mets.MetadataGroupXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.MetadataXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.MetsXmlElementAccessInterface;
import org.kitodo.api.filemanagement.LockResult;
import org.kitodo.api.filemanagement.LockingMode;
import org.kitodo.api.validation.State;
import org.kitodo.api.validation.ValidationResult;
import org.kitodo.api.validation.metadata.MetadataValidationInterface;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.dto.ProcessDTO;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyPrefsHelper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.production.services.data.RulesetService;
import org.kitodo.production.services.dataeditor.RulesetManagementService;
import org.kitodo.production.services.dataformat.MetsService;
import org.kitodo.production.services.file.FileService;

public class MetadataValidationService implements MetadataValidationInterface {
    private static final Logger logger = LogManager.getLogger(MetadataValidationService.class);

    /**
     * Message key if the identifier contains invalid characters.
     */
    private static final String MESSAGE_IDENTIFIER_INVALID = "invalidIdentifierCharacter";

    /**
     * Message key if the value for the identifier is missing.
     */
    private static final String MESSAGE_IDENTIFIER_MISSING = "metadataMissingIdentifier";

    /**
     * Message key if the value of the identifier contains the same value in
     * different places.
     */
    private static final String MESSAGE_IDENTIFIER_NOT_UNIQUE = "invalidIdentifierSame";

    /**
     * Message key if no media is assigned.
     */
    private static final String MESSAGE_MEDIA_MISSING = "metadataPaginationError";

    /**
     * Message key if media is present but not assigned to a structure.
     */
    private static final String MESSAGE_MEDIA_UNASSIGNED = "metadataPaginationPages";

    /**
     * Message key if a structure has no media assigned.
     */
    private static final String MESSAGE_STRUCTURE_WITHOUT_MEDIA = "metadataPaginationStructure";

    /**
     * Message key if the input is invalid.
     */
    private static final String MESSAGE_VALUE_INVALID = "metadataInvalidData";

    /**
     * Message key if the input is missing.
     */
    private static final String MESSAGE_VALUE_MISSING = "metadataMandatoryElement";

    /**
     * Message key if there are too many entries of a type.
     */
    private static final String MESSAGE_VALUE_TOO_OFTEN = "metadataNotOneElement";

    /**
     * Message key if there are too little entries of a type.
     */
    private static final String MESSAGE_VALUE_TOO_RARE = "metadataNotEnoughElements";

    private final FileService fileService = ServiceManager.getFileService();
    private final MetsService metsService = ServiceManager.getMetsService();
    private final ProcessService processService = ServiceManager.getProcessService();
    private final RulesetManagementService rulesetManagementService = ServiceManager.getRulesetManagementService();
    private final RulesetService rulesetService = ServiceManager.getRulesetService();

    /**
     * Validate.
     *
     * @param process
     *            object
     * @return boolean
     * @deprecated This validation is a work-around to keep legacy code
     *             functional. It should not be used anymore.
     */
    @Deprecated
    public boolean validate(Process process) {
        LegacyPrefsHelper prefs = rulesetService.getPreferences(process.getRuleset());
        LegacyMetsModsDigitalDocumentHelper gdzfile;
        try {
            gdzfile = processService.readMetadataFile(process);
        } catch (IOException | RuntimeException e) {
            Helper.setErrorMessage("metadataReadError", new Object[] {process.getTitle() }, logger, e);
            return false;
        }
        return validate(gdzfile, prefs, process);
    }

    /**
     * Validate.
     *
     * @param gdzfile
     *            Fileformat object
     * @param prefs
     *            Prefs object
     * @param process
     *            object
     * @return boolean
     * @deprecated This validation is a work-around to keep legacy code
     *             functional. It should not be used anymore.
     */
    @Deprecated
    public boolean validate(LegacyMetsModsDigitalDocumentHelper gdzfile, LegacyPrefsHelper prefs, Process process) {
        try {
            return !State.ERROR.equals(validate(gdzfile.getWorkpiece(), prefs.getRuleset()).getState());
        } catch (DataException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public ValidationResult validate(URI metsFileUri, URI rulesetFileUri) {
        try {
            MetsXmlElementAccessInterface workpiece = metsService.createMetsXmlElementAccess();
            try (LockResult lockResult = fileService.tryLock(metsFileUri, LockingMode.IMMUTABLE_READ)) {
                if (lockResult.isSuccessful()) {
                    try (InputStream in = fileService.read(metsFileUri, lockResult)) {
                        workpiece.read(in);
                    }
                } else {
                    throw new IOException(createLockErrorMessage(metsFileUri, lockResult));
                }
            }

            RulesetManagementInterface ruleset = rulesetManagementService.getRulesetManagement();
            ruleset.load(new File(rulesetFileUri.getPath()));

            return validate(workpiece, ruleset);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (DataException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * Validates a workpiece based on a rule set.
     * 
     * @param workpiece
     *            METS file
     * @param ruleset
     *            Ruleset file
     * @return the validation result
     * @throws DataException
     *             if an error occurs while reading from the search engine
     */
    private ValidationResult validate(MetsXmlElementAccessInterface workpiece, RulesetManagementInterface ruleset)
            throws DataException {

        Collection<ValidationResult> results = new ArrayList<>();

        results.add(checkTheIdentifier(workpiece));
        results.add(checkForStructuresWithoutMedia(workpiece));
        results.add(checkForUnlinkedMedia(workpiece));

        for (DivXmlElementAccessInterface structure : treeStream(workpiece.getStructMap(),
            DivXmlElementAccessInterface::getChildren).collect(Collectors.toList())) {
            StructuralElementViewInterface divisionView = ruleset.getStructuralElementView(structure.getType(), null,
                null);
            results.add(checkForMandatoryQuantitiesOfTheMetadataRecursive(
                structure.getMetadata().parallelStream()
                        .collect(Collectors.toMap(Function.identity(), MetadataAccessInterface::getType)),
                divisionView, structure.toString().concat(": ")));
            results.add(checkForDetailsInTheMetadataRecursive(
                structure.getMetadata().parallelStream()
                        .collect(Collectors.toMap(Function.identity(), MetadataAccessInterface::getType)),
                divisionView, structure.toString().concat(": ")));
        }

        return merge(results);
    }

    /**
     * Verifies that the rules for the identifier are met.
     * 
     * @param workpiece
     *            METS file
     * @return the validation result
     * @throws DataException
     *             if an error occurs while reading from the search engine
     */
    private ValidationResult checkTheIdentifier(MetsXmlElementAccessInterface workpiece) throws DataException {
        boolean error = false;
        boolean warning = false;
        Collection<String> messages = new HashSet<>();

        String workpieceId = workpiece.getId();
        if (Objects.isNull(workpieceId)) {
            messages.add(Helper.getTranslation(MESSAGE_IDENTIFIER_MISSING));
            warning = true;
        } else {
            if (!Pattern.matches(ConfigCore.getParameterOrDefaultValue(ParameterCore.VALIDATE_IDENTIFIER_REGEX),
                workpieceId)) {
                messages.add(Helper.getTranslation(MESSAGE_IDENTIFIER_INVALID,
                    Arrays.asList(workpieceId, workpiece.toString())));
                error = true;
            }
            List<ProcessDTO> processDTOs = processService.findAll().parallelStream()
                    .filter(processDTO -> workpieceId.equals(String.valueOf(processDTO.getId())))
                    .collect(Collectors.toList());
            if (processDTOs.size() > 1) {
                messages.add(Helper.getTranslation(MESSAGE_IDENTIFIER_NOT_UNIQUE,
                    Arrays.asList(workpieceId, processDTOs.get(0).getTitle(), processDTOs.get(1).getTitle())));
                error = true;
            }
        }

        return new ValidationResult(error ? State.ERROR : warning ? State.WARNING : State.SUCCESS, messages);
    }

    /**
     * Reports structures that have no assigned media units. These structures
     * are undesirable because you cannot look at them. It is also checked if
     * the linked media are even referenced in the document.
     * 
     * @param workpiece
     *            workpiece to be examined
     * @return the validation result
     */
    private static ValidationResult checkForStructuresWithoutMedia(MetsXmlElementAccessInterface workpiece) {
        boolean error = false;
        boolean warning = false;
        Collection<String> messages = new HashSet<>();

        Collection<String> structuresWithoutMedia = treeStream(workpiece.getStructMap(),
            DivXmlElementAccessInterface::getChildren).filter(structure -> structure.getAreas().isEmpty())
                    .map(structure -> Helper.getTranslation(MESSAGE_STRUCTURE_WITHOUT_MEDIA) + ' ' + structure)
                    .collect(Collectors.toSet());
        if (!structuresWithoutMedia.isEmpty()) {
            messages.addAll(structuresWithoutMedia);
            warning = true;
        }

        if (treeStream(workpiece.getStructMap(), DivXmlElementAccessInterface::getChildren)
                .flatMap(structure -> structure.getAreas().stream()).map(AreaXmlElementAccessInterface::getFile)
                .filter(workpiece.getFileGrp()::contains).findAny().isPresent()) {
            messages.add(Helper.getTranslation(MESSAGE_MEDIA_MISSING));
            error = true;
        }

        return new ValidationResult(error ? State.ERROR : warning ? State.WARNING : State.SUCCESS, messages);
    }

    /**
     * Checks whether media are referenced in the document that are not assigned
     * to a structure. Maybe not a mistake but sloppy.
     * 
     * @param workpiece
     *            workpiece to be examined
     * @return the validation result
     */
    private static ValidationResult checkForUnlinkedMedia(MetsXmlElementAccessInterface workpiece) {
        boolean warning = false;
        Collection<String> messages = new HashSet<>();

        KeySetView<FileXmlElementAccessInterface, ?> unassignedMediaUnits = ConcurrentHashMap.newKeySet();
        unassignedMediaUnits.addAll(workpiece.getFileGrp());
        treeStream(workpiece.getStructMap(), DivXmlElementAccessInterface::getChildren)
                .flatMap(structure -> structure.getAreas().stream()).map(AreaXmlElementAccessInterface::getFile)
                .forEach(unassignedMediaUnits::remove);
        if (!unassignedMediaUnits.isEmpty()) {
            for (FileXmlElementAccessInterface mediaUnit : unassignedMediaUnits) {
                messages.add(Helper.getTranslation(MESSAGE_MEDIA_UNASSIGNED) + ' ' + mediaUnit);
            }
            warning = true;
        }

        return new ValidationResult(warning ? State.WARNING : State.SUCCESS, messages);
    }

    /**
     * Checks if all description data occur in the given frequency (minimum /
     * maximum).
     * 
     * @param containedMetadata
     *            metadata
     * @param containingMetadataView
     *            associated hierarchy node of the rule set
     * @param location
     *            specifies which structure element or which metadata group in
     *            it is checked
     * @return the validation result
     */
    private static ValidationResult checkForMandatoryQuantitiesOfTheMetadataRecursive(
            Map<MetadataAccessInterface, String> containedMetadata, ComplexMetadataViewInterface containingMetadataView,
            String location) {
        boolean warning = false;
        Collection<String> messages = new HashSet<>();

        for (Entry<MetadataViewInterface, Collection<MetadataAccessInterface>> metadataViewWithValues : squash(
            containingMetadataView.getSortedVisibleMetadata(containedMetadata, Collections.emptyList())).entrySet()) {

            MetadataViewInterface metadataView = metadataViewWithValues.getKey();
            int min = metadataView.getMinOccurs();
            int max = metadataView.getMaxOccurs();
            int count = metadataViewWithValues.getValue().size();

            if (count == 0 && (min == 1 && max == 1)) {
                messages.add(Helper.getTranslation(MESSAGE_VALUE_MISSING) + ' ' + location + metadataView.getLabel());
                warning = true;
            } else if (count < min) {
                /*
                 * Double quotes for single chars in string building prevent
                 * their addition with the ints.
                 */
                messages.add(Helper.getTranslation(MESSAGE_VALUE_TOO_RARE) + ' ' + location + metadataView.getLabel()
                        + " (" + count + "/" + min + ")");
                warning = true;
            } else if (count > max) {
                messages.add(Helper.getTranslation(MESSAGE_VALUE_TOO_OFTEN) + ' ' + location + metadataView.getLabel()
                        + " (" + count + "/" + max + ")");
                warning = true;
            }

            if (metadataView instanceof ComplexMetadataViewInterface) {
                for (MetadataAccessInterface metadata : metadataViewWithValues.getValue()) {
                    if (metadata instanceof MetadataGroupXmlElementAccessInterface) {
                        ValidationResult validationResult = checkForMandatoryQuantitiesOfTheMetadataRecursive(
                            ((MetadataGroupXmlElementAccessInterface) metadata).getMetadata().parallelStream()
                                    .collect(Collectors.toMap(Function.identity(), MetadataAccessInterface::getType)),
                            (ComplexMetadataViewInterface) metadataView, location + metadataView.getLabel() + " - ");
                        if (validationResult.getState().equals(State.WARNING)) {
                            warning = true;
                        }
                        messages.addAll(validationResult.getResultMessages());
                    } else {
                        throw new IllegalStateException("metadataView is a " + metadataView.getClass().getSimpleName()
                                + ", but metadata is a " + metadata.getClass().getSimpleName());
                    }
                }
            }
        }

        return new ValidationResult(warning ? State.WARNING : State.SUCCESS, messages);
    }

    /**
     * Checks if all description data meet their individually defined validity
     * criteria. This essentially checks the data type. Actually, this should
     * already happen at the input and not get here, but we check it anyway. For
     * example, corrupt data may have been imported.
     * 
     * @param containedMetadata
     *            metadata
     * @param containingMetadataView
     *            associated hierarchy node of the rule set
     * @param location
     *            specifies which structure element or which metadata group in
     *            it is checked
     * @return the validation result
     */
    private static ValidationResult checkForDetailsInTheMetadataRecursive(
            Map<MetadataAccessInterface, String> containedMetadata, ComplexMetadataViewInterface containingMetadataView,
            String location) {
        boolean error = false;
        Collection<String> messages = new HashSet<>();

        Collection<String> result = new ArrayList<>();
        List<MetadataViewWithValuesInterface<MetadataAccessInterface>> metadataViewsWithValues = containingMetadataView
                .getSortedVisibleMetadata(containedMetadata, Collections.emptyList());
        for (MetadataViewWithValuesInterface<MetadataAccessInterface> metadataViewWithValues : metadataViewsWithValues) {
            MetadataViewInterface metadataView = metadataViewWithValues.getMetadata()
                    .orElseThrow(IllegalStateException::new);
            for (MetadataAccessInterface metadata : metadataViewWithValues.getValues()) {
                if (metadata instanceof MetadataXmlElementAccessInterface
                        && metadataView instanceof SimpleMetadataViewInterface) {
                    String value = ((MetadataXmlElementAccessInterface) metadata).getValue();
                    if (!((SimpleMetadataViewInterface) metadataView).isValid(value)) {
                        result.add(Helper.getTranslation(MESSAGE_VALUE_INVALID) + ' ' + location
                                + metadataView.getLabel() + " \"" + value + '"');
                        error = true;
                    }
                } else if (metadata instanceof MetadataGroupXmlElementAccessInterface
                        && metadataView instanceof ComplexMetadataViewInterface) {
                    ValidationResult validationResult = checkForDetailsInTheMetadataRecursive(
                        ((MetadataGroupXmlElementAccessInterface) metadata).getMetadata().parallelStream()
                                .collect(Collectors.toMap(Function.identity(), MetadataAccessInterface::getType)),
                        (ComplexMetadataViewInterface) metadataView, location + metadataView.getLabel() + " - ");
                    if (validationResult.getState().equals(State.ERROR)) {
                        error = true;
                    }
                    messages.addAll(validationResult.getResultMessages());
                } else {
                    throw new IllegalStateException("metadataView is a " + metadataView.getClass().getSimpleName()
                            + ", but metadata is a " + metadata.getClass().getSimpleName());
                }
            }
        }

        return new ValidationResult(error ? State.ERROR : State.SUCCESS, messages);
    }

    // helper methods

    /**
     * Extracts the formation of the error message as it occurs during both
     * reading and writing. In addition, the error is logged.
     * 
     * @param uri
     *            URI to be read/written
     * @param lockResult
     *            Lock result that did not work
     * @return The error message for the exception.
     */
    private static String createLockErrorMessage(URI uri, LockResult lockResult) {
        Collection<String> conflictingUsers = lockResult.getConflicts().get(uri);
        StringBuilder buffer = new StringBuilder();
        buffer.append("Cannot lock ");
        buffer.append(uri);
        buffer.append(" because it is already locked by ");
        buffer.append(String.join(" & ", conflictingUsers));
        String message = buffer.toString();
        logger.info(message);
        return message;
    }

    /**
     * Merges several individual validation results into one validation result.
     * 
     * @param results
     *            individual validation results
     * @return merged validation result
     */
    private static ValidationResult merge(Collection<ValidationResult> results) {
        boolean error = false;
        boolean warning = false;
        Collection<String> messages = new HashSet<>();

        for (ValidationResult result : results) {
            if (result.getState().equals(State.ERROR)) {
                error = true;
            } else if (result.getState().equals(State.WARNING)) {
                warning = true;
            }
            messages.addAll(result.getResultMessages());
        }

        return new ValidationResult(error ? State.ERROR : warning ? State.WARNING : State.SUCCESS, messages);
    }

    /**
     * Merges multiple meta-data lines of identical type. The output format of
     * the rule set refers to the display form and therefore generates a
     * separate line for each metadata value. In the validation we need the
     * number. To get that, the lines are summarized here.
     * 
     * @param metadataViewsWithValues
     *            list of meta-data view objects, each with their value
     * @return merged lines of identical type
     */
    private static Map<MetadataViewInterface, Collection<MetadataAccessInterface>> squash(
            List<MetadataViewWithValuesInterface<MetadataAccessInterface>> metadataViewsWithValues) {
        Map<MetadataViewInterface, Collection<MetadataAccessInterface>> squashed = new HashMap<>();
        for (MetadataViewWithValuesInterface<MetadataAccessInterface> metadataViewWithValues : metadataViewsWithValues) {
            Optional<MetadataViewInterface> optionalMetadataView = metadataViewWithValues.getMetadata();
            if (!optionalMetadataView.isPresent()) {
                continue;
            }
            squashed.computeIfAbsent(optionalMetadataView.get(), each -> new ArrayList<>());
            squashed.get(optionalMetadataView.get()).addAll(metadataViewWithValues.getValues());
        }
        return squashed;
    }

    /**
     * Generates a stream of nodes from a tree-like structure.
     * 
     * @param tree
     *            starting node
     * @param childAccessor
     *            function to access the children of the node
     * @return all nodes as stream
     */
    private static <T> Stream<T> treeStream(T tree, Function<T, Collection<T>> childAccessor) {
        return Stream.concat(Stream.of(tree),
            childAccessor.apply(tree).stream().flatMap(child -> treeStream(child, childAccessor)));
    }
}
