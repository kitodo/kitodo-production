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
import org.kitodo.api.MdSec;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataeditor.rulesetmanagement.Domain;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewWithValuesInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.ExistingOrLinkedStructure;
import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.api.dataformat.Structure;
import org.kitodo.api.dataformat.View;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.dataformat.MetsService;

/**
 * Connects a legacy doc struct from the logical map to a structure. This is a
 * soldering class to keep legacy code operational which is about to be removed.
 * Do not use this class.
 */
public class LegacyLogicalDocStructHelper implements LegacyDocStructHelperInterface, BindingSaveInterface {
    private static final Logger logger = LogManager.getLogger(LegacyLogicalDocStructHelper.class);

    private static final MetsService metsService = ServiceManager.getMetsService();

    /**
     * The media file accessed via this soldering class.
     */
    private ExistingOrLinkedStructure structure;

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

    LegacyLogicalDocStructHelper(ExistingOrLinkedStructure child, LegacyLogicalDocStructHelper parent,
            RulesetManagementInterface ruleset, List<LanguageRange> priorityList) {
        this.structure = child;
        this.ruleset = ruleset;
        this.priorityList = priorityList;
        this.parent = parent;
        this.divisionView = ruleset.getStructuralElementView(child.getType(), "edit", priorityList);
    }

    @Override
    @Deprecated
    public void addChild(LegacyDocStructHelperInterface child) {
        LegacyLogicalDocStructHelper legacyLogicalDocStructHelperChild = (LegacyLogicalDocStructHelper) child;
        legacyLogicalDocStructHelperChild.parent = this;
        if (structure instanceof Structure) {
            ((Structure) structure).getChildren().add(legacyLogicalDocStructHelperChild.structure);
        } else {
            throw new IllegalStateException("cannot add child to linked structure");
        }
    }

    @Override
    @Deprecated
    public void addChild(Integer index, LegacyDocStructHelperInterface child) {
        LegacyLogicalDocStructHelper legacyLogicalDocStructHelperChild = (LegacyLogicalDocStructHelper) child;
        legacyLogicalDocStructHelperChild.parent = this;
        if (structure instanceof Structure) {
            ((Structure) structure).getChildren().add(index, legacyLogicalDocStructHelperChild.structure);
        } else {
            throw new IllegalStateException("cannot add child to linked structure");
        }
    }

    @Override
    @Deprecated
    public void addMetadata(LegacyMetadataHelper metadata) {
        if (!(structure instanceof Structure)) {
            throw new IllegalStateException("cannot add metadata to linked structure");
        }
        Map<Metadata, String> metadataEntriesMappedToKeyNames = ((Structure) structure).getMetadata().parallelStream()
                .collect(Collectors.toMap(Function.identity(), Metadata::getKey));
        Optional<MetadataViewInterface> optionalKeyView = divisionView
                .getAddableMetadata(metadataEntriesMappedToKeyNames, Collections.emptyList()).parallelStream()
                .filter(keyView -> keyView.getId().equals(metadata.getMetadataType().getName())).findFirst();
        Optional<Domain> optionalDomain = optionalKeyView.isPresent() ? optionalKeyView.get().getDomain()
                : Optional.empty();
        if (!optionalDomain.isPresent() || !optionalDomain.get().equals(Domain.METS_DIV)) {
            MetadataEntry metadataEntry = new MetadataEntry();
            metadata.setBinding(this, metadataEntry, optionalDomain.orElse(Domain.DESCRIPTION));
            metadata.saveToBinding();
            ((Structure) structure).getMetadata().add(metadataEntry);
        } else {
            metadata.setBinding(this, null, Domain.METS_DIV);
            metadata.saveToBinding();
        }
    }

    @Override
    public void saveMetadata(LegacyMetadataHelper metadata) {
        if (Domain.METS_DIV.equals(metadata.getDomain())) {
            try {
                structure.getClass().getMethod("set".concat(metadata.getMetadataType().getName()), String.class)
                        .invoke(structure, metadata.getValue());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        } else if (metadata.getBinding() != null) {
            metadata.getBinding().setKey(metadata.getMetadataType().getName());
            metadata.getBinding().setDomain(domainToMdSec(metadata.getDomain()));
            metadata.getBinding().setValue(metadata.getValue());
        }
    }

    @Override
    @Deprecated
    public LegacyReferenceHelper addReferenceTo(LegacyDocStructHelperInterface docStruct, String type) {
        if (!(structure instanceof Structure)) {
            throw new IllegalStateException("cannot add reference to linked structure");
        }
        View view = new View();
        LegacyInnerPhysicalDocStructHelper target = (LegacyInnerPhysicalDocStructHelper) docStruct;
        view.setMediaUnit(target.getMediaUnit());
        ((Structure) structure).getViews().add(view);
        return new LegacyReferenceHelper(target);
    }

    @Override
    @Deprecated
    public void deleteUnusedPersonsAndMetadata() {
        if (!(structure instanceof Structure)) {
            return;
        }
        Iterator<Metadata> metadataIterator = ((Structure) structure).getMetadata().iterator();
        while (metadataIterator.hasNext()) {
            Metadata metadata = metadataIterator.next();
            if (((MetadataEntry) metadata).getValue().isEmpty()) {
                metadataIterator.remove();
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

    @Override
    @Deprecated
    public List<LegacyMetadataTypeHelper> getAddableMetadataTypes() {
        if (!(structure instanceof Structure)) {
            return Collections.emptyList();
        }
        Map<Metadata, String> metadataEntriesMappedToKeyNames = ((Structure) structure).getMetadata().parallelStream()
                .collect(Collectors.toMap(Function.identity(), Metadata::getKey));
        Collection<MetadataViewInterface> addableKeys = divisionView.getAddableMetadata(metadataEntriesMappedToKeyNames,
            Collections.emptyList());
        ArrayList<LegacyMetadataTypeHelper> result = new ArrayList<>(addableKeys.size());
        for (MetadataViewInterface key : addableKeys) {
            result.add(new LegacyMetadataTypeHelper(key));
        }
        return result;
    }

    @Override
    @Deprecated
    public List<LegacyDocStructHelperInterface> getAllChildren() {
        if (!(structure instanceof Structure)) {
            return Collections.emptyList();
        }
        List<LegacyDocStructHelperInterface> wrappedChildren = new ArrayList<>();
        for (ExistingOrLinkedStructure child : ((Structure) structure).getChildren()) {
            wrappedChildren.add(new LegacyLogicalDocStructHelper(child, this, ruleset, priorityList));
        }
        return wrappedChildren;
    }

    @Override
    @Deprecated
    public List<LegacyMetadataHelper> getAllMetadata() {
        if (!(structure instanceof Structure)) {
            return Collections.emptyList();
        }
        List<LegacyMetadataHelper> result = new LinkedList<>();
        Map<Metadata, String> metadataEntriesMappedToKeyNames = ((Structure) structure).getMetadata().parallelStream()
                .collect(Collectors.toMap(Function.identity(), Metadata::getKey));
        List<MetadataViewWithValuesInterface<Metadata>> entryViews = divisionView
                .getSortedVisibleMetadata(metadataEntriesMappedToKeyNames, Collections.emptyList());
        for (MetadataViewWithValuesInterface<Metadata> entryView : entryViews) {
            if (entryView.getMetadata().isPresent()) {
                MetadataViewInterface key = entryView.getMetadata().get();
                for (Metadata value : entryView.getValues()) {
                    if (value instanceof MetadataEntry) {
                        result.add(new LegacyMetadataHelper(null, new LegacyMetadataTypeHelper(key),
                                ((MetadataEntry) value).getValue()));
                    }
                }
            }
        }
        return result;
    }

    @Override
    @Deprecated
    public List<LegacyMetadataHelper> getAllMetadataByType(LegacyMetadataTypeHelper metadataType) {
        if (!(structure instanceof Structure)) {
            return Collections.emptyList();
        }
        List<LegacyMetadataHelper> result = new LinkedList<>();
        Map<Metadata, String> metadataEntriesMappedToKeyNames = ((Structure) structure).getMetadata().parallelStream()
                .collect(Collectors.toMap(Function.identity(), Metadata::getKey));
        List<MetadataViewWithValuesInterface<Metadata>> entryViews = divisionView
                .getSortedVisibleMetadata(metadataEntriesMappedToKeyNames, Collections.emptyList());
        for (MetadataViewWithValuesInterface<Metadata> entryView : entryViews) {
            if (entryView.getMetadata().isPresent()) {
                MetadataViewInterface key = entryView.getMetadata().get();
                if (key.getId().equals(metadataType.getName())) {
                    for (Metadata value : entryView.getValues()) {
                        if (value instanceof MetadataEntry) {
                            result.add(new LegacyMetadataHelper(null, new LegacyMetadataTypeHelper(key),
                                    ((MetadataEntry) value).getValue()));
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    @Deprecated
    public List<LegacyReferenceHelper> getAllReferences(String direction) {
        if (!(structure instanceof Structure)) {
            return Collections.emptyList();
        }
        switch (direction) {
            case "to":
                Collection<View> views = ((Structure) structure).getViews();
                ArrayList<LegacyReferenceHelper> allReferences = new ArrayList<>(views.size());
                for (View view : views) {
                    MediaUnit mediaUnit = view.getMediaUnit();
                    allReferences.add(new LegacyReferenceHelper(new LegacyInnerPhysicalDocStructHelper(mediaUnit)));
                }
                return allReferences;
            default:
                throw new IllegalArgumentException("Unknown reference direction: " + direction);
        }
    }

    @Override
    @Deprecated
    public Collection<LegacyReferenceHelper> getAllToReferences() {
        return getAllReferences("to");
    }

    @Override
    @Deprecated
    public Collection<LegacyReferenceHelper> getAllToReferences(String type) {
        if (!(structure instanceof Structure)) {
            return Collections.emptyList();
        }
        switch (type) {
            case "logical_physical":
                Collection<View> views = ((Structure) structure).getViews();
                ArrayList<LegacyReferenceHelper> allReferences = new ArrayList<>(views.size());
                for (View view : views) {
                    MediaUnit mediaUnit = view.getMediaUnit();
                    allReferences.add(new LegacyReferenceHelper(new LegacyInnerPhysicalDocStructHelper(mediaUnit)));
                }
                return allReferences;
            default:
                throw new IllegalArgumentException("Unknown reference type: " + type);
        }
    }

    @Override
    @Deprecated
    public String getAnchorClass() {
        // The replacement of the UGH library has no concept of anchor classes.
        return null;
    }

    @Override
    @Deprecated
    public List<LegacyMetadataTypeHelper> getDisplayMetadataTypes() {
        if (!(structure instanceof Structure)) {
            return Collections.emptyList();
        }
        List<LegacyMetadataTypeHelper> result = new LinkedList<>();
        Map<Metadata, String> metadataEntriesMappedToKeyNames = ((Structure) structure).getMetadata().parallelStream()
                .collect(Collectors.toMap(Function.identity(), Metadata::getKey));
        List<MetadataViewWithValuesInterface<Metadata>> entryViews = divisionView
                .getSortedVisibleMetadata(metadataEntriesMappedToKeyNames, Collections.emptyList());
        for (MetadataViewWithValuesInterface<Metadata> entryView : entryViews) {
            if (entryView.getMetadata().isPresent()) {
                MetadataViewInterface key = entryView.getMetadata().get();
                result.add(new LegacyMetadataTypeHelper(key));
            }
        }
        return result;
    }

    /**
     * Metadata eines Docstructs ermitteln.
     *
     * @param inStruct
     *            DocStruct object
     * @param inMetadataType
     *            MetadataType object
     * @return Metadata
     */
    @Deprecated
    public static LegacyMetadataHelper getMetadata(LegacyDocStructHelperInterface inStruct, LegacyMetadataTypeHelper inMetadataType) {
        if (inStruct != null && inMetadataType != null) {
            List<? extends LegacyMetadataHelper> all = inStruct.getAllMetadataByType(inMetadataType);
            if (all.isEmpty()) {
                LegacyMetadataHelper md = new LegacyMetadataHelper(inMetadataType);
                md.setDocStruct(inStruct);
                inStruct.addMetadata(md);
                return md;
            } else {
                return all.get(0);
            }
        }
        return null;
    }

    @Override
    @Deprecated
    public LegacyDocStructHelperInterface getParent() {
        return parent;
    }

    @Override
    @Deprecated
    public List<LegacyMetadataTypeHelper> getPossibleMetadataTypes() {
        /*
         * The method is a doublet (in the interface, as well as doubled code in
         * the legacy implementation)
         */
        return getAddableMetadataTypes();
    }

    @Override
    @Deprecated
    public LegacyLogicalDocStructTypeHelper getDocStructType() {
        return new LegacyLogicalDocStructTypeHelper(divisionView);
    }

    /**
     * This method is not part of the interface, but the JSF code digs in the
     * depths of the UGH and uses it on the guts.
     * 
     * @return Method delegated to {@link #getDocStructType()}
     */
    @Deprecated
    public LegacyLogicalDocStructTypeHelper getType() {
        if (!logger.isTraceEnabled()) {
            StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
            logger.log(Level.WARN, "Method {}.{}() invokes {}.{}(), bypassing the interface!",
                stackTrace[1].getClassName(), stackTrace[1].getMethodName(), stackTrace[0].getClassName(),
                stackTrace[0].getMethodName());
        }
        return getDocStructType();
    }

    @Deprecated
    public boolean isDocStructTypeAllowedAsChild(LegacyLogicalDocStructTypeHelper type) {
        return divisionView.getAllowedSubstructuralElements().containsKey(type.getName());
    }

    public boolean isLink() {
        return structure.isLinked();
    }

    @Override
    @Deprecated
    public void removeChild(LegacyDocStructHelperInterface docStruct) {
        if (!(structure instanceof Structure)) {
            return;
        }
        LegacyLogicalDocStructHelper legacyLogicalDocStructHelperChild = (LegacyLogicalDocStructHelper) docStruct;
        legacyLogicalDocStructHelperChild.parent = null;
        ((Structure) structure).getChildren().remove(legacyLogicalDocStructHelperChild.structure);
    }

    @Override
    @Deprecated
    public void removeMetadata(LegacyMetadataHelper metaDatum) {
        if (!(structure instanceof Structure)) {
            return;
        }
        Iterator<Metadata> entries = ((Structure) structure).getMetadata().iterator();
        String metadataTypeName = metaDatum.getMetadataType().getName();
        while (entries.hasNext()) {
            Metadata entry = entries.next();
            if (entry.getKey().equals(metadataTypeName)
                    && ((MetadataEntry) entry).getValue().equals(metaDatum.getValue())) {
                entries.remove();
                break;
            }
        }
    }

    @Override
    @Deprecated
    public void removeReferenceTo(LegacyDocStructHelperInterface target) {
        if (!(structure instanceof Structure)) {
            return;
        }
        MediaUnit mediaUnit = ((LegacyInnerPhysicalDocStructHelper) target).getMediaUnit();
        Iterator<View> viewIterator = ((Structure) structure).getViews().iterator();
        while (viewIterator.hasNext()) {
            MediaUnit containedMediaUnit = viewIterator.next().getMediaUnit();
            if (containedMediaUnit.equals(mediaUnit)) {
                viewIterator.remove();
            }
        }
    }
}
