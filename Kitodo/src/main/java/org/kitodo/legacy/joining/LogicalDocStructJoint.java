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

package org.kitodo.legacy.joining;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.mets.AreaXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.DivXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.FileXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.MdSec;
import org.kitodo.api.dataformat.mets.MetadataAccessInterface;
import org.kitodo.api.dataformat.mets.MetadataXmlElementAccessInterface;
import org.kitodo.api.ugh.ContentFileInterface;
import org.kitodo.api.ugh.DigitalDocumentInterface;
import org.kitodo.api.ugh.DocStructInterface;
import org.kitodo.api.ugh.DocStructTypeInterface;
import org.kitodo.api.ugh.MetadataGroupInterface;
import org.kitodo.api.ugh.MetadataGroupTypeInterface;
import org.kitodo.api.ugh.MetadataInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;
import org.kitodo.api.ugh.PersonInterface;
import org.kitodo.api.ugh.PrefsInterface;
import org.kitodo.api.ugh.ReferenceInterface;
import org.kitodo.api.ugh.exceptions.ContentFileNotLinkedException;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.api.ugh.exceptions.TypeNotAllowedAsChildException;
import org.kitodo.api.ugh.exceptions.TypeNotAllowedForParentException;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.dataformat.MetsService;

public class LogicalDocStructJoint implements DocStructInterface {
    private static final Logger logger = LogManager.getLogger(LogicalDocStructJoint.class);

    private final ServiceManager serviceLoader = new ServiceManager();
    private final MetsService metsService = serviceLoader.getMetsService();

    private DivXmlElementAccessInterface structure;
    private StructuralElementViewInterface divisionView;

    public LogicalDocStructJoint() {
        logger.log(Level.TRACE, "new LogicalDocStructJoint()");
        // TODO Auto-generated method stub
        this.structure = metsService.createDiv();
    }

    LogicalDocStructJoint(DivXmlElementAccessInterface structure, StructuralElementViewInterface divisionView) {
        this.structure = structure;
        this.divisionView = divisionView;
    }

    @Override
    public void addChild(DocStructInterface child) throws TypeNotAllowedAsChildException {
        logger.log(Level.TRACE, "addChild(child: {})", child);
        // TODO Auto-generated method stub
    }

    @Override
    public void addChild(Integer index, DocStructInterface child) throws TypeNotAllowedAsChildException {
        logger.log(Level.TRACE, "addChild(index: {}, child: {})", index, child);
        // TODO Auto-generated method stub
    }

    @Override
    public void addContentFile(ContentFileInterface contentFile) {
        logger.log(Level.TRACE, "addContentFile(contentFile: {})", contentFile);
        // TODO Auto-generated method stub
    }

    @Override
    public void addMetadata(MetadataInterface metadata) throws MetadataTypeNotAllowedException {
        Map<MetadataAccessInterface, String> metadataEntriesMappedToKeyNames = structure.getMetadata().parallelStream()
                .collect(Collectors.toMap(Function.identity(), MetadataAccessInterface::getType));
        Optional<MetadataViewInterface> zz = divisionView
                .getAddableMetadata(metadataEntriesMappedToKeyNames, Collections.emptyList()).parallelStream()
                .filter(x -> x.getId().equals(metadata.getMetadataType().getName())).findFirst();
        Optional<Domain> optionalDomain = zz.isPresent() ? zz.get().getDomain() : Optional.empty();
        if (!optionalDomain.isPresent() || !optionalDomain.get().equals(Domain.METS_DIV)) {
            MetadataXmlElementAccessInterface metadataEntry = metsService.createMetadata();
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

    @Override
    public DocStructInterface addMetadata(String metadataType, String value) throws MetadataTypeNotAllowedException {
        logger.log(Level.TRACE, "addMetadata(metadataType: {}, value: {})", metadataType, value);
        // TODO Auto-generated method stub
        return this;
    }

    @Override
    public void addMetadataGroup(MetadataGroupInterface metadataGroup) throws MetadataTypeNotAllowedException {
        logger.log(Level.TRACE, "addMetadataGroup(metadataGroup: {})", metadataGroup);
        // TODO Auto-generated method stub
    }

    @Override
    public void addPerson(PersonInterface person) throws MetadataTypeNotAllowedException {
        logger.log(Level.TRACE, "addPerson(person: {})", person);
        // TODO Auto-generated method stub
    }

    @Override
    public ReferenceInterface addReferenceTo(DocStructInterface docStruct, String type) {
        logger.log(Level.TRACE, "()");
        // TODO Auto-generated method stub
        return new ReferenceJoint();
    }

    @Override
    public DocStructInterface copy(boolean copyMetaData, Boolean recursive) {
        logger.log(Level.TRACE, "()");
        // TODO Auto-generated method stub
        return new LogicalDocStructJoint();
    }

    @Override
    public DocStructInterface createChild(String docStructType, DigitalDocumentInterface digitalDocument,
            PrefsInterface prefs) throws TypeNotAllowedAsChildException, TypeNotAllowedForParentException {

        logger.log(Level.TRACE, "createChild(docStructType: \"{}\", digitalDocument: {}, prefs: {})", docStructType,
            digitalDocument, prefs);
        // TODO Auto-generated method stub
        return new LogicalDocStructJoint(); // returns the child
    }

    @Override
    public void deleteUnusedPersonsAndMetadata() {
        logger.log(Level.TRACE, "deleteUnusedPersonsAndMetadata()");
        // TODO Auto-generated method stub
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

    @Override
    public List<MetadataGroupTypeInterface> getAddableMetadataGroupTypes() {
        logger.log(Level.TRACE, "getAddableMetadataGroupTypes()");
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    public List<MetadataTypeInterface> getAddableMetadataTypes() {
        Map<MetadataAccessInterface, String> metadataEntriesMappedToKeyNames = structure.getMetadata().parallelStream()
                .collect(Collectors.toMap(Function.identity(), MetadataAccessInterface::getType));
        Collection<MetadataViewInterface> addableKeys = divisionView.getAddableMetadata(metadataEntriesMappedToKeyNames,
            Collections.emptyList());
        ArrayList<MetadataTypeInterface> result = new ArrayList<>(addableKeys.size());
        for (MetadataViewInterface key : addableKeys) {
            result.add(new MetadataTypeJoint(key));
        }
        return result;
    }

    @Override
    public List<DocStructInterface> getAllChildren() {
        List<DocStructInterface> wrappedChildren = new ArrayList<>();
        for (DivXmlElementAccessInterface child : structure.getChildren()) {
            wrappedChildren.add(new LogicalDocStructJoint(child, divisionView));
        }
        return wrappedChildren;
    }

    @Override
    public List<DocStructInterface> getAllChildrenByTypeAndMetadataType(String docStructType, String metaDataType) {
        logger.log(Level.TRACE, "getAllChildrenByTypeAndMetadataType(docStructType: \"{}\", metaDataType: \"{}\")",
            docStructType, metaDataType);
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    public List<ContentFileInterface> getAllContentFiles() {
        logger.log(Level.TRACE, "getAllContentFiles()");
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    public List<ReferenceInterface> getAllFromReferences() {
        logger.log(Level.TRACE, "getAllFromReferences()");
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    public List<MetadataInterface> getAllIdentifierMetadata() {
        logger.log(Level.TRACE, "getAllIdentifierMetadata()");
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    public List<MetadataInterface> getAllMetadata() {
        List<MetadataInterface> result = new LinkedList<>();
        // sortieren
        Map<MetadataAccessInterface, String> metadataEntriesMappedToKeyNames = structure.getMetadata().parallelStream()
                .collect(Collectors.toMap(Function.identity(), MetadataAccessInterface::getType));
        List<MetadataViewWithValuesInterface<MetadataAccessInterface>> a = divisionView
                .getSortedVisibleMetadata(metadataEntriesMappedToKeyNames, Collections.emptyList());

        // ausgabe

        for (MetadataViewWithValuesInterface<MetadataAccessInterface> x : a) {
            if (x.getMetadata().isPresent()) {
                MetadataViewInterface key = x.getMetadata().get();
                for (MetadataAccessInterface value : x.getValues()) {
                    if (value instanceof MetadataXmlElementAccessInterface) {
                        result.add(new MetadataJoint(null, new MetadataTypeJoint(key),
                                ((MetadataXmlElementAccessInterface) value).getValue()));
                    }
                }
            }
        }
        return result;
    }

    @Override
    public List<? extends MetadataInterface> getAllMetadataByType(MetadataTypeInterface metadataType) {
        List<MetadataInterface> result = new LinkedList<>();
        // sortieren
        Map<MetadataAccessInterface, String> metadataEntriesMappedToKeyNames = structure.getMetadata().parallelStream()
                .collect(Collectors.toMap(Function.identity(), MetadataAccessInterface::getType));
        List<MetadataViewWithValuesInterface<MetadataAccessInterface>> a = divisionView
                .getSortedVisibleMetadata(metadataEntriesMappedToKeyNames, Collections.emptyList());

        // ausgabe

        for (MetadataViewWithValuesInterface<MetadataAccessInterface> x : a) {
            if (x.getMetadata().isPresent()) {
                MetadataViewInterface key = x.getMetadata().get();
                if (key.getId().equals(metadataType.getName())) {
                    for (MetadataAccessInterface value : x.getValues()) {
                        if (value instanceof MetadataXmlElementAccessInterface) {
                            result.add(new MetadataJoint(null, new MetadataTypeJoint(key),
                                    ((MetadataXmlElementAccessInterface) value).getValue()));
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public List<MetadataGroupInterface> getAllMetadataGroups() {
        logger.log(Level.TRACE, "getAllMetadataGroups()");
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    public List<PersonInterface> getAllPersons() {
        // “persons” is no longer supported, uses groups instead
        return Collections.emptyList();
    }

    @Override
    public List<PersonInterface> getAllPersonsByType(MetadataTypeInterface metadataType) {
        logger.log(Level.TRACE, "getAllPersonsByType(metadataType: {})", metadataType);
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    public List<ReferenceInterface> getAllReferences(String direction) {
        switch (direction) {
            case "to":
                List<AreaXmlElementAccessInterface> views = structure.getAreas();
                ArrayList<ReferenceInterface> allReferences = new ArrayList<>(views.size());
                for (AreaXmlElementAccessInterface view : views) {
                    FileXmlElementAccessInterface mediaUnit = view.getFile();
                    allReferences.add(new ReferenceJoint(new InnerPhysicalDocStructJoint(mediaUnit)));
                }
                return allReferences;
            default:
                throw new IllegalArgumentException("Unknown reference direction: " + direction);
        }
    }

    @Override
    public Collection<ReferenceInterface> getAllToReferences() {
        logger.log(Level.TRACE, "getAllToReferences()");
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    public Collection<ReferenceInterface> getAllToReferences(String type) {
        logger.log(Level.TRACE, "getAllToReferences(type: \"{}\")", type);
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    public Object getAllVisibleMetadata() {
        logger.log(Level.TRACE, "getAllVisibleMetadata()");
        // TODO Auto-generated method stub
        return null; // null -> false, new Object() -> true
    }

    @Override
    public String getAnchorClass() {
        logger.log(Level.TRACE, "getAnchorClass()");
        // TODO Auto-generated method stub
        return "";
    }

    @Override
    public DocStructInterface getChild(String type, String identifierField, String identifier) {
        logger.log(Level.TRACE, "getChild(type: \"{}\", identifierField: \"{}\", identifier: \"{}\")");
        // TODO Auto-generated method stub
        return new LogicalDocStructJoint();
    }

    @Override
    public List<MetadataTypeInterface> getDisplayMetadataTypes() {
        List<MetadataTypeInterface> result = new LinkedList<>();
        // sortieren
        Map<MetadataAccessInterface, String> metadataEntriesMappedToKeyNames = structure.getMetadata().parallelStream()
                .collect(Collectors.toMap(Function.identity(), MetadataAccessInterface::getType));
        List<MetadataViewWithValuesInterface<MetadataAccessInterface>> a = divisionView
                .getSortedVisibleMetadata(metadataEntriesMappedToKeyNames, Collections.emptyList());

        // ausgabe

        for (MetadataViewWithValuesInterface<MetadataAccessInterface> x : a) {
            if (x.getMetadata().isPresent()) {
                MetadataViewInterface key = x.getMetadata().get();
                result.add(new MetadataTypeJoint(key));
            }
        }
        return result;
    }

    @Override
    public String getImageName() {
        logger.log(Level.TRACE, "getImageName()");
        // TODO Auto-generated method stub
        return "";
    }

    @Override
    public DocStructInterface getNextChild(DocStructInterface predecessor) {
        logger.log(Level.TRACE, "getNextChild(predecessor: {})", predecessor);
        // TODO Auto-generated method stub
        return new LogicalDocStructJoint();
    }

    @Override
    public DocStructInterface getParent() {
        logger.log(Level.TRACE, "getParent()");
        // TODO Auto-generated method stub
        return new LogicalDocStructJoint();
    }

    @Override
    public List<MetadataTypeInterface> getPossibleMetadataTypes() {
        // The method is a doublet (in the interface, as well as doubled code in
        // the legacy implementation)
        return getAddableMetadataTypes();
    }

    @Override
    public DocStructTypeInterface getDocStructType() {
        return new LogicalDocStructTypeJoint(divisionView);
    }

    /**
     * This method is not part of the interface, but the JSF code digs in the
     * depths of the UGH and uses it on the guts.
     * 
     * @return Method delegated to {@link #getDocStructType()}
     */
    public DocStructTypeInterface getType() {
        // StackTraceElement[] stackTrace = new
        // RuntimeException().getStackTrace();
        // logger.log(Level.WARN, "Method {}.{}() invokes {}.{}(), bypassing the
        // interface!", stackTrace[1].getClassName(),
        // stackTrace[1].getMethodName(), stackTrace[0].getClassName(),
        // stackTrace[0].getMethodName());
        return getDocStructType();
    }

    @Override
    public boolean isDocStructTypeAllowedAsChild(DocStructTypeInterface type) {
        logger.log(Level.TRACE, "isDocStructTypeAllowedAsChild(type: {})", type);
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void removeChild(DocStructInterface docStruct) {
        logger.log(Level.TRACE, "removeChild(docStruct: {})", docStruct);
        // TODO Auto-generated method stub
    }

    @Override
    public void removeContentFile(ContentFileInterface contentFile) throws ContentFileNotLinkedException {
        logger.log(Level.TRACE, "removeContentFile(contentFile: {})", contentFile);
        // TODO Auto-generated method stub
    }

    @Override
    public void removeMetadata(MetadataInterface metaDatum) {
        logger.log(Level.TRACE, "removeMetadata(metaDatum: {})", metaDatum);
        // TODO Auto-generated method stub
    }

    @Override
    public void removeMetadataGroup(MetadataGroupInterface metadataGroup) {
        logger.log(Level.TRACE, "removeMetadataGroup(metadataGroup: {})");
        // TODO Auto-generated method stub
    }

    @Override
    public void removePerson(PersonInterface person) {
        logger.log(Level.TRACE, "removePerson(person: {})", person);
        // TODO Auto-generated method stub
    }

    @Override
    public void removeReferenceTo(DocStructInterface target) {
        logger.log(Level.TRACE, "removeReferenceTo(target: {})", target);
        // TODO Auto-generated method stub

    }

    @Override
    public void setImageName(String imageName) {
        logger.log(Level.TRACE, "setImageName(imageName: {})", imageName);
        // TODO Auto-generated method stub
    }

    @Override
    public void setType(DocStructTypeInterface docStructType) {
        logger.log(Level.TRACE, "setType(docStructType: {})");
        // TODO Auto-generated method stub
    }
}
