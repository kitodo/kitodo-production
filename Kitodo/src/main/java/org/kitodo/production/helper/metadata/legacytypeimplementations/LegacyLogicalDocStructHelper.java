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

package org.kitodo.production.helper.metadata.legacytypeimplementations;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.Domain;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewWithValuesInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.mets.AreaXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.DivXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.FileXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.MdSec;
import org.kitodo.api.dataformat.mets.MetadataAccessInterface;
import org.kitodo.api.dataformat.mets.MetadataXmlElementAccessInterface;
import org.kitodo.api.ugh.MetadataGroupInterface;
import org.kitodo.api.ugh.MetadataGroupTypeInterface;
import org.kitodo.api.ugh.PersonInterface;
import org.kitodo.api.ugh.exceptions.ContentFileNotLinkedException;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.api.ugh.exceptions.TypeNotAllowedAsChildException;
import org.kitodo.api.ugh.exceptions.TypeNotAllowedForParentException;
import org.kitodo.helper.metadata.LegacyDocStructHelperInterface;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.dataformat.MetsService;

/**
 * Connects a legacy doc struct from the logical map to a structure. This is a
 * soldering class to keep legacy code operational which is about to be removed.
 * Do not use this class.
 */
public class LegacyLogicalDocStructHelper implements LegacyDocStructHelperInterface {
    private static final Logger logger = LogManager.getLogger(LegacyLogicalDocStructHelper.class);

    private static final MetsService metsService = ServiceManager.getMetsService();

    /**
     * The media file accessed via this soldering class.
     */
    private DivXmlElementAccessInterface structure;

    /**
     * The current ruleset.
     */
    private RulesetManagementInterface ruleset;

    /**
     * The view on this division.
     */
    private StructuralElementViewInterface divisionView;

    /**
     * The user’s meta-data language priority list.
     */
    private List<LanguageRange> priorityList;

    /**
     * The parent of this class—required by legacy code.
     */
    private LegacyLogicalDocStructHelper parent;

    LegacyLogicalDocStructHelper(DivXmlElementAccessInterface structure, LegacyLogicalDocStructHelper parent,
            RulesetManagementInterface ruleset, List<LanguageRange> priorityList) {
        this.structure = structure;
        this.ruleset = ruleset;
        this.priorityList = priorityList;
        this.parent = parent;
        this.divisionView = ruleset.getStructuralElementView(structure.getType(), "edit", priorityList);
    }

    @Override
    public void addChild(LegacyDocStructHelperInterface child) throws TypeNotAllowedAsChildException {
        LegacyLogicalDocStructHelper legacyLogicalDocStructHelperChild = (LegacyLogicalDocStructHelper) child;
        legacyLogicalDocStructHelperChild.parent = this;
        structure.getChildren().add(legacyLogicalDocStructHelperChild.structure);
    }

    @Override
    public void addChild(Integer index, LegacyDocStructHelperInterface child) throws TypeNotAllowedAsChildException {
        LegacyLogicalDocStructHelper legacyLogicalDocStructHelperChild = (LegacyLogicalDocStructHelper) child;
        legacyLogicalDocStructHelperChild.parent = this;
        structure.getChildren().add(index, legacyLogicalDocStructHelperChild.structure);
    }

    @Override
    public void addContentFile(LegacyContentFileHelper contentFile) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public void addMetadata(LegacyMetadataHelper metadata) throws MetadataTypeNotAllowedException {
        Map<MetadataAccessInterface, String> metadataEntriesMappedToKeyNames = structure.getMetadata().parallelStream()
                .collect(Collectors.toMap(Function.identity(), MetadataAccessInterface::getType));
        Optional<MetadataViewInterface> optionalKeyView = divisionView
                .getAddableMetadata(metadataEntriesMappedToKeyNames, Collections.emptyList()).parallelStream()
                .filter(keyView -> keyView.getId().equals(metadata.getMetadataType().getName())).findFirst();
        Optional<Domain> optionalDomain = optionalKeyView.isPresent() ? optionalKeyView.get().getDomain()
                : Optional.empty();
        if (!optionalDomain.isPresent() || !optionalDomain.get().equals(Domain.METS_DIV)) {
            MetadataXmlElementAccessInterface metadataEntry = metsService.createMetadataXmlElementAccess();
            metadataEntry.setType(metadata.getMetadataType().getName());
            metadataEntry.setDomain(domainToMdSec(optionalDomain.orElse(Domain.DESCRIPTION)));
            metadataEntry.setValue(metadata.getValue());
            structure.getMetadata().add(metadataEntry);
        } else {
            try {
                structure.getClass().getMethod("set".concat(metadata.getMetadataType().getName()), String.class)
                        .invoke(structure, metadata.getValue());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        }
    }

    public LegacyDocStructHelperInterface addMetadata(String metadataType, String value)
            throws MetadataTypeNotAllowedException {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    public void addMetadataGroup(MetadataGroupInterface metadataGroup) throws MetadataTypeNotAllowedException {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    public void addPerson(PersonInterface person) throws MetadataTypeNotAllowedException {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public LegacyReferenceHelper addReferenceTo(LegacyDocStructHelperInterface docStruct, String type) {
        AreaXmlElementAccessInterface view = metsService.createAreaXmlElementAccess();
        LegacyInnerPhysicalDocStructHelper target = (LegacyInnerPhysicalDocStructHelper) docStruct;
        view.setFile(target.getMediaUnit());
        structure.getAreas().add(view);
        return new LegacyReferenceHelper(target);
    }

    public LegacyDocStructHelperInterface copy(boolean copyMetaData, Boolean recursive) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    public LegacyDocStructHelperInterface createChild(String docStructType, LegacyMetsModsDigitalDocumentHelper digitalDocument,
            LegacyPrefsHelper prefs) throws TypeNotAllowedAsChildException, TypeNotAllowedForParentException {

        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public void deleteUnusedPersonsAndMetadata() {
        Iterator<MetadataAccessInterface> metadataAccessInterfaceIterator = structure.getMetadata().iterator();
        while (metadataAccessInterfaceIterator.hasNext()) {
            MetadataAccessInterface metadataAccessInterface = metadataAccessInterfaceIterator.next();
            if (((MetadataXmlElementAccessInterface) metadataAccessInterface).getValue().isEmpty()) {
                metadataAccessInterfaceIterator.remove();
            }
        }
    }

    private MdSec domainToMdSec(Domain domain) {
        switch (domain) {
            case DESCRIPTION:
                return MdSec.DMD_SEC;
            case DIGITAL_PROVENANCE:
                return MdSec.DIGIPROV_MD;
            case RIGHTS:
                return MdSec.RIGHTS_MD;
            case SOURCE:
                return MdSec.SOURCE_MD;
            case TECHNICAL:
                return MdSec.TECH_MD;
            default:
                throw new IllegalArgumentException(domain.name());
        }
    }

    public List<MetadataGroupTypeInterface> getAddableMetadataGroupTypes() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public List<LegacyMetadataTypeHelper> getAddableMetadataTypes() {
        Map<MetadataAccessInterface, String> metadataEntriesMappedToKeyNames = structure.getMetadata().parallelStream()
                .collect(Collectors.toMap(Function.identity(), MetadataAccessInterface::getType));
        Collection<MetadataViewInterface> addableKeys = divisionView.getAddableMetadata(metadataEntriesMappedToKeyNames,
            Collections.emptyList());
        ArrayList<LegacyMetadataTypeHelper> result = new ArrayList<>(addableKeys.size());
        for (MetadataViewInterface key : addableKeys) {
            result.add(new LegacyMetadataTypeHelper(key));
        }
        return result;
    }

    @Override
    public List<LegacyDocStructHelperInterface> getAllChildren() {
        List<LegacyDocStructHelperInterface> wrappedChildren = new ArrayList<>();
        for (DivXmlElementAccessInterface child : structure.getChildren()) {
            wrappedChildren.add(new LegacyLogicalDocStructHelper(child, this, ruleset, priorityList));
        }
        return wrappedChildren;
    }

    @Override
    public List<LegacyDocStructHelperInterface> getAllChildrenByTypeAndMetadataType(String docStructType,
            String metaDataType) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    public List<LegacyContentFileHelper> getAllContentFiles() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    public List<LegacyReferenceHelper> getAllFromReferences() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    public List<LegacyMetadataHelper> getAllIdentifierMetadata() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public List<LegacyMetadataHelper> getAllMetadata() {
        List<LegacyMetadataHelper> result = new LinkedList<>();
        Map<MetadataAccessInterface, String> metadataEntriesMappedToKeyNames = structure.getMetadata().parallelStream()
                .collect(Collectors.toMap(Function.identity(), MetadataAccessInterface::getType));
        List<MetadataViewWithValuesInterface<MetadataAccessInterface>> entryViews = divisionView
                .getSortedVisibleMetadata(metadataEntriesMappedToKeyNames, Collections.emptyList());
        for (MetadataViewWithValuesInterface<MetadataAccessInterface> entryView : entryViews) {
            if (entryView.getMetadata().isPresent()) {
                MetadataViewInterface key = entryView.getMetadata().get();
                for (MetadataAccessInterface value : entryView.getValues()) {
                    if (value instanceof MetadataXmlElementAccessInterface) {
                        result.add(new LegacyMetadataHelper(null, new LegacyMetadataTypeHelper(key),
                                ((MetadataXmlElementAccessInterface) value).getValue()));
                    }
                }
            }
        }
        return result;
    }

    @Override
    public List<? extends LegacyMetadataHelper> getAllMetadataByType(LegacyMetadataTypeHelper metadataType) {
        List<LegacyMetadataHelper> result = new LinkedList<>();
        Map<MetadataAccessInterface, String> metadataEntriesMappedToKeyNames = structure.getMetadata().parallelStream()
                .collect(Collectors.toMap(Function.identity(), MetadataAccessInterface::getType));
        List<MetadataViewWithValuesInterface<MetadataAccessInterface>> entryViews = divisionView
                .getSortedVisibleMetadata(metadataEntriesMappedToKeyNames, Collections.emptyList());
        for (MetadataViewWithValuesInterface<MetadataAccessInterface> entryView : entryViews) {
            if (entryView.getMetadata().isPresent()) {
                MetadataViewInterface key = entryView.getMetadata().get();
                if (key.getId().equals(metadataType.getName())) {
                    for (MetadataAccessInterface value : entryView.getValues()) {
                        if (value instanceof MetadataXmlElementAccessInterface) {
                            result.add(new LegacyMetadataHelper(null, new LegacyMetadataTypeHelper(key),
                                    ((MetadataXmlElementAccessInterface) value).getValue()));
                        }
                    }
                }
            }
        }
        return result;
    }

    public List<MetadataGroupInterface> getAllMetadataGroups() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public List<PersonInterface> getAllPersons() {
        return Collections.emptyList();
    }

    public List<PersonInterface> getAllPersonsByType(LegacyMetadataTypeHelper metadataType) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public List<LegacyReferenceHelper> getAllReferences(String direction) {
        switch (direction) {
            case "to":
                List<AreaXmlElementAccessInterface> views = structure.getAreas();
                ArrayList<LegacyReferenceHelper> allReferences = new ArrayList<>(views.size());
                for (AreaXmlElementAccessInterface view : views) {
                    FileXmlElementAccessInterface mediaUnit = view.getFile();
                    allReferences.add(new LegacyReferenceHelper(new LegacyInnerPhysicalDocStructHelper(mediaUnit)));
                }
                return allReferences;
            default:
                throw new IllegalArgumentException("Unknown reference direction: " + direction);
        }
    }

    @Override
    public Collection<LegacyReferenceHelper> getAllToReferences() {
        return getAllReferences("to");
    }

    @Override
    public Collection<LegacyReferenceHelper> getAllToReferences(String type) {
        switch (type) {
            case "logical_physical":
                List<AreaXmlElementAccessInterface> views = structure.getAreas();
                ArrayList<LegacyReferenceHelper> allReferences = new ArrayList<>(views.size());
                for (AreaXmlElementAccessInterface view : views) {
                    FileXmlElementAccessInterface mediaUnit = view.getFile();
                    allReferences.add(new LegacyReferenceHelper(new LegacyInnerPhysicalDocStructHelper(mediaUnit)));
                }
                return allReferences;
            default:
                throw new IllegalArgumentException("Unknown reference type: " + type);
        }
    }

    public Object getAllVisibleMetadata() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public String getAnchorClass() {
        // The replacement of the UGH library has no concept of anchor classes.
        return null;
    }

    public LegacyDocStructHelperInterface getChild(String type, String identifierField, String identifier) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public List<LegacyMetadataTypeHelper> getDisplayMetadataTypes() {
        List<LegacyMetadataTypeHelper> result = new LinkedList<>();
        Map<MetadataAccessInterface, String> metadataEntriesMappedToKeyNames = structure.getMetadata().parallelStream()
                .collect(Collectors.toMap(Function.identity(), MetadataAccessInterface::getType));
        List<MetadataViewWithValuesInterface<MetadataAccessInterface>> entryViews = divisionView
                .getSortedVisibleMetadata(metadataEntriesMappedToKeyNames, Collections.emptyList());
        for (MetadataViewWithValuesInterface<MetadataAccessInterface> entryView : entryViews) {
            if (entryView.getMetadata().isPresent()) {
                MetadataViewInterface key = entryView.getMetadata().get();
                result.add(new LegacyMetadataTypeHelper(key));
            }
        }
        return result;
    }

    @Override
    public String getImageName() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    public LegacyDocStructHelperInterface getNextChild(LegacyDocStructHelperInterface predecessor) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public LegacyDocStructHelperInterface getParent() {
        return parent;
    }

    @Override
    public List<LegacyMetadataTypeHelper> getPossibleMetadataTypes() {
        /*
         * The method is a doublet (in the interface, as well as doubled code in
         * the legacy implementation)
         */
        return getAddableMetadataTypes();
    }

    @Override
    public LegacyLogicalDocStructTypeHelper getDocStructType() {
        return new LegacyLogicalDocStructTypeHelper(divisionView);
    }

    /**
     * This method is not part of the interface, but the JSF code digs in the
     * depths of the UGH and uses it on the guts.
     * 
     * @return Method delegated to {@link #getDocStructType()}
     */
    public LegacyLogicalDocStructTypeHelper getType() {
        if (!logger.isTraceEnabled()) {
            StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
            logger.log(Level.WARN, "Method {}.{}() invokes {}.{}(), bypassing the interface!",
                stackTrace[1].getClassName(), stackTrace[1].getMethodName(), stackTrace[0].getClassName(),
                stackTrace[0].getMethodName());
        }
        return getDocStructType();
    }

    public boolean isDocStructTypeAllowedAsChild(LegacyLogicalDocStructTypeHelper type) {
        return divisionView.getAllowedSubstructuralElements().containsKey(type.getName());
    }

    @Override
    public void removeChild(LegacyDocStructHelperInterface docStruct) {
        LegacyLogicalDocStructHelper legacyLogicalDocStructHelperChild = (LegacyLogicalDocStructHelper) docStruct;
        legacyLogicalDocStructHelperChild.parent = null;
        structure.getChildren().remove(legacyLogicalDocStructHelperChild.structure);
    }

    public void removeContentFile(LegacyContentFileHelper contentFile) throws ContentFileNotLinkedException {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public void removeMetadata(LegacyMetadataHelper metaDatum) {
        Iterator<MetadataAccessInterface> entries = structure.getMetadata().iterator();
        String metadataTypeName = metaDatum.getMetadataType().getName();
        while (entries.hasNext()) {
            MetadataAccessInterface entry = entries.next();
            if (entry.getType().equals(metadataTypeName)
                    && ((MetadataXmlElementAccessInterface) entry).getValue().equals(metaDatum.getValue())) {
                entries.remove();
                break;
            }
        }
    }

    public void removeMetadataGroup(MetadataGroupInterface metadataGroup) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    public void removePerson(PersonInterface person) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public void removeReferenceTo(LegacyDocStructHelperInterface target) {
        FileXmlElementAccessInterface mediaUnit = ((LegacyInnerPhysicalDocStructHelper) target).getMediaUnit();
        Iterator<AreaXmlElementAccessInterface> areaXmlElementAccessInterfaceIterator = structure.getAreas().iterator();
        while (areaXmlElementAccessInterfaceIterator.hasNext()) {
            FileXmlElementAccessInterface fileXmlElementAccessInterface = areaXmlElementAccessInterfaceIterator.next()
                    .getFile();
            if (fileXmlElementAccessInterface.equals(mediaUnit)) {
                areaXmlElementAccessInterfaceIterator.remove();
            }
        }
    }

    public void setImageName(String imageName) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    public void setType(LegacyLogicalDocStructTypeHelper docStructType) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    /**
     * This method generates a comprehensible log message in case something was
     * overlooked and one of the unimplemented methods should ever be called in
     * operation. The name was chosen deliberately short in order to keep the
     * calling code clear. This method must be implemented in every class
     * because it uses the logger tailored to the class.
     * 
     * @param exception
     *            created {@code UnsupportedOperationException}
     * @return the exception
     */
    private static RuntimeException andLog(UnsupportedOperationException exception) {
        StackTraceElement[] stackTrace = exception.getStackTrace();
        StringBuilder buffer = new StringBuilder(255);
        buffer.append(stackTrace[1].getClassName());
        buffer.append('.');
        buffer.append(stackTrace[1].getMethodName());
        buffer.append("()");
        if (stackTrace[1].getLineNumber() > -1) {
            buffer.append(" line ");
            buffer.append(stackTrace[1].getLineNumber());
        }
        buffer.append(" unexpectedly called unimplemented ");
        buffer.append(stackTrace[0].getMethodName());
        buffer.append("()");
        if (exception.getMessage() != null) {
            buffer.append(": ");
            buffer.append(exception.getMessage());
        }
        logger.error(buffer.toString());
        return exception;
    }
}
