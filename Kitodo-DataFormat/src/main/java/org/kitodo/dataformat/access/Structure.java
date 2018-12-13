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

package org.kitodo.dataformat.access;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.api.dataformat.mets.AreaXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.DivXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.MdSec;
import org.kitodo.api.dataformat.mets.MetadataAccessInterface;
import org.kitodo.dataformat.metskitodo.AmdSecType;
import org.kitodo.dataformat.metskitodo.DivType;
import org.kitodo.dataformat.metskitodo.KitodoType;
import org.kitodo.dataformat.metskitodo.MdSecType;
import org.kitodo.dataformat.metskitodo.MdSecType.MdWrap;
import org.kitodo.dataformat.metskitodo.MdSecType.MdWrap.XmlData;
import org.kitodo.dataformat.metskitodo.Mets;

/**
 * The tree-like outline structure for digital representation. This structuring
 * structure can be subdivided into arbitrary finely granular
 * {@link #substructures}. It can be described by {@link #metadata}.
 */
public class Structure implements DivXmlElementAccessInterface {
    /**
     * The qualified name of the Kitodo meta-data format, needed to assemble the
     * meta-data entries in METS using JAXB.
     */
    private static final QName KITODO_QNAME = new QName("http://meta.kitodo.org/v1/", "kitodo");

    /**
     * The label for this structure. The label is displayed in the graphical
     * representation of the structure tree for this level.
     */
    private String label;

    /**
     * The meta-data for this structure. This structure level can be described
     * with any meta-data.
     */
    private Collection<MetadataAccessInterface> metadata = new HashSet<>();

    /**
     * The order label of this structure. This is needed very rarely. It is not
     * displayed, and unlike the name suggests, it does not specify the order of
     * this substructure along with other substructures within its parent
     * structure, but the order is determined by the order of references from
     * the parent tree to each substructure. The order label may be used to
     * store the machine-readable value if the label contains a human-readable
     * value that can be mapped to a machine-readable value. An example of this
     * are calendar dates. For example, a label of “the fifteenth year of the
     * reign of Tiberius Caesar” could be stored as “{@code -0006}”.
     */
    private String orderlabel;

    /**
     * The substructures of this structure, which form the structure tree. The
     * order of the substructures described by the order of the {@code <div>}
     * elements in the {@code <structMap TYPE="LOGICAL">} in the METS file.
     */
    private List<DivXmlElementAccessInterface> substructures = new LinkedList<>();

    /**
     * The type of structure, for example, book, chapter, page. Although the
     * data type of this variable is a string, it is recommended to use a
     * controlled vocabulary. If the generated METS files are to be used with
     * the DFG Viewer, the list of possible structure types is defined.
     * 
     * @see "https://dfg-viewer.de/en/structural-data-set/"
     */
    private String type;

    /**
     * The views on media units that this structure level comprises. Currently,
     * only {@link View}s on media units as a whole are possible with
     * Production, but here it has already been built for the future, that also
     * {@code View}s on parts of {@link MediaUnit}s are to be made possible. The
     * list of {@code View}s is aware of the order of the {@code MediaUnit}s
     * encoded by the {@code MediaUnit}’s {@code order} property. Although this
     * list implements the {@link List} interface, it always preserves the order
     * as dictated by the {@code order} property of the {@code MediaUnit}s.
     * Therefore, to reorder this list, you must change the {@code order}
     * property of the {@code MediaUnit}s instead. It is not possible to code
     * several sequences that are in conflict with each other.
     */
    private final List<AreaXmlElementAccessInterface> views = new SortedList<AreaXmlElementAccessInterface>(
        areaXmlElementAccessInterface -> areaXmlElementAccessInterface.getFile().getOrder());

    /**
     * Public constructor to create a new structure. This constructor can be
     * called via the service loader to get a new structure.
     */
    public Structure() {
    }

    /**
     * Constructor to read a structure from METS.
     * 
     * @param div
     *            METS {@code <div>} element from which the structure is to be
     *            built
     * @param mets
     *            METS data structure from which it is possible to determine
     *            what kind of metadata section is linked
     * @param mediaUnitsMap
     *            From this map, the media units are read, which must be
     *            referenced here by their ID.
     */
    Structure(DivType div, Mets mets, Map<String, Set<MediaUnit>> mediaUnitsMap) {
        label = div.getLABEL();
        for (Object mdSec : div.getDMDID()) {
            readMetadata((MdSecType) mdSec, MdSec.DMD_SEC);
        }
        for (Object mdSec : div.getADMID()) {
            readMetadata((MdSecType) mdSec, amdSecTypeOf(mets, (MdSecType) mdSec));
        }
        orderlabel = div.getORDERLABEL();
        substructures = div.getDiv().stream().map(child -> new Structure(child, mets, mediaUnitsMap))
                .collect(Collectors.toCollection(LinkedList::new));
        type = div.getTYPE();
        Set<MediaUnit> mediaUnits = mediaUnitsMap.get(div.getID());
        if (mediaUnits != null) {
            mediaUnits.stream().map(View::new).forEach(views::add);
        }
    }

    /**
     * Determines from a METS data structure of which type is a meta-data
     * section.
     * 
     * <p>
     * Implementation note: This method would be a good candidate for
     * parallelization.
     * 
     * @param mets
     *            METS data structure that determines what type of meta-data
     *            section is
     * @param mdSec
     *            administrative meta-data section whose type is to be
     *            determined
     * @return the type of administrative meta-data section
     */
    private final MdSec amdSecTypeOf(Mets mets, MdSecType mdSec) {
        for (AmdSecType amdSec : mets.getAmdSec()) {
            if (amdSec.getSourceMD().contains(mdSec)) {
                return MdSec.SOURCE_MD;
            } else if (amdSec.getDigiprovMD().contains(mdSec)) {
                return MdSec.DIGIPROV_MD;
            } else if (amdSec.getRightsMD().contains(mdSec)) {
                return MdSec.RIGHTS_MD;
            } else if (amdSec.getTechMD().contains(mdSec)) {
                return MdSec.TECH_MD;
            }
        }
        throw new NoSuchElementException();
    }

    /**
     * Reads a meta-data section and adds the meta-data to the structure.
     * 
     * @param mdSec
     *            meta-data section to be read
     * @param mdSecType
     *            type of meta-data section
     */
    private final void readMetadata(MdSecType mdSec, MdSec mdSecType) {
        metadata.addAll(mdSec.getMdWrap().getXmlData().getAny().parallelStream().filter(JAXBElement.class::isInstance)
                .map(JAXBElement.class::cast).map(JAXBElement::getValue).filter(KitodoType.class::isInstance)
                .map(KitodoType.class::cast)
                .flatMap(kitodoType -> Stream.concat(
                    kitodoType.getMetadata().parallelStream()
                            .map(metadataType -> new MetadataEntry(mdSecType, metadataType)),
                    kitodoType.getMetadataGroup().parallelStream()
                            .map(metadataGroupType -> new MetadataEntriesGroup(mdSecType, metadataGroupType))))
                .collect(Collectors.toList()));
    }

    /**
     * Returns the views associated with this structure.
     * 
     * @return the views
     */
    @Override
    public List<AreaXmlElementAccessInterface> getAreas() {
        return views;
    }

    /**
     * Returns the substructures associated with this structure.
     * 
     * @return the substructures
     */
    @Override
    public List<DivXmlElementAccessInterface> getChildren() {
        return substructures;
    }

    /**
     * Returns the label of this structure.
     * 
     * @return the label
     */
    @Override
    public String getLabel() {
        return label;
    }

    /**
     * Returns the meta-data on this structure.
     * 
     * @return the meta-data
     */
    @Override
    public Collection<MetadataAccessInterface> getMetadata() {
        return metadata;
    }

    /**
     * Returns the order label of this structure.
     * 
     * @return the order label
     */
    @Override
    public String getOrderlabel() {
        return orderlabel;
    }

    /**
     * Returns the type of this structure.
     * 
     * @return the type
     */
    @Override
    public String getType() {
        return type;
    }

    /**
     * Sets the label of this structure.
     * 
     * @param label
     *            label to set
     */
    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Sets the order label of this structure.
     * 
     * @param orderlabel
     *            order label to set
     */
    @Override
    public void setOrderlabel(String orderlabel) {
        this.orderlabel = orderlabel;
    }

    /**
     * Sets the type of this structure.
     * 
     * @param type
     *            type to set
     */
    @Override
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Creates a METS {@code <div>} element from this structure.
     * 
     * @param mediaUnitIDs
     *            the assigned identifier for each media unit so that the link
     *            pairs of the struct link section can be formed later
     * @param smLinkData
     *            the link pairs of the struct link section are added to this
     *            list
     * @param mets
     *            the METS structure in which the meta-data is added
     * @return a METS {@code <div>} element
     */
    DivType toDiv(Map<MediaUnit, String> mediaUnitIDs,
            LinkedList<Pair<String, String>> smLinkData, Mets mets) {
        DivType div = new DivType();
        String divId = UUID.randomUUID().toString();
        div.setID(divId);
        div.setLABEL(label);
        div.setORDERLABEL(orderlabel);
        div.setTYPE(type);
        smLinkData.addAll(views.parallelStream().map(AreaXmlElementAccessInterface::getFile).map(mediaUnitIDs::get)
                .map(mediaUnitId -> Pair.of(divId, mediaUnitId)).collect(Collectors.toList()));

        Optional<MdSecType> optionalDmdSec = createMdSec(MdSec.DMD_SEC);
        if (optionalDmdSec.isPresent()) {
            MdSecType dmdSec = optionalDmdSec.get();
            dmdSec.setID(UUID.randomUUID().toString());
            mets.getDmdSec().add(dmdSec);
            div.getDMDID().add(dmdSec);
        }
        Optional<AmdSecType> optionalAmdSec = createAmdSec(div);
        if (optionalAmdSec.isPresent()) {
            AmdSecType admSec = optionalAmdSec.get();
            mets.getAmdSec().add(admSec);
        }

        for (DivXmlElementAccessInterface substructure : substructures) {
            div.getDiv().add(((Structure) substructure).toDiv(mediaUnitIDs, smLinkData, mets));
        }
        return div;
    }

    /**
     * Creates a meta-data section of the specified domain of the Kitodo type
     * and returns it with its connection to the METS if there is data for it.
     * 
     * @param domain
     *            Domain for which a metadata section is to be generated
     * @return a metadata section, if there is data for it
     */
    private Optional<MdSecType> createMdSec(MdSec domain) {
        KitodoType kitodoType = new KitodoType();
        for (MetadataAccessInterface entry : metadata) {
            if (domain.equals(entry.getDomain())) {
                if (entry instanceof MetadataEntry) {
                    kitodoType.getMetadata().add(((MetadataEntry) entry).toMetadata());
                } else if (entry instanceof MetadataEntriesGroup) {
                    kitodoType.getMetadataGroup().add(((MetadataEntriesGroup) entry).toMetadataGroup());
                }
            }
        }
        if (kitodoType.getMetadata().isEmpty() && kitodoType.getMetadataGroup().isEmpty()) {
            return Optional.empty();
        } else {
            XmlData xmlData = new XmlData();
            xmlData.getAny().add(new JAXBElement<>(KITODO_QNAME, KitodoType.class, kitodoType));
            MdWrap mdWrap = new MdWrap();
            mdWrap.setXmlData(xmlData);
            MdSecType dmdSec = new MdSecType();
            dmdSec.setMdWrap(mdWrap);
            return Optional.of(dmdSec);
        }
    }

    /**
     * Generates an {@code <amdSec>} if administrative meta-data exists on this
     * structure. Remarkable in this function is the bitwise OR, so that in any
     * case all sections are generated, which would not be the case with logical
     * OR.
     * 
     * @param div
     *            div where ADMID references must be added to the generated
     *            meta-data sections
     * @return an {@code <amdSec>}, if necessary
     */
    private Optional<AmdSecType> createAmdSec(DivType div) {
        AmdSecType amdSec = new AmdSecType();
        return addMdSec(createMdSec(MdSec.SOURCE_MD), AmdSecType::getSourceMD, amdSec, div)
                | addMdSec(createMdSec(MdSec.DIGIPROV_MD), AmdSecType::getDigiprovMD, amdSec, div)
                | addMdSec(createMdSec(MdSec.RIGHTS_MD), AmdSecType::getRightsMD, amdSec, div)
                | addMdSec(createMdSec(MdSec.TECH_MD), AmdSecType::getTechMD, amdSec, div)
                        ? Optional.of(amdSec)
                        : Optional.empty();
    }

    /**
     * Adds a meta-data section to an administrative meta-data section, if there
     * is one. This function deduplicates fourfold existing function for four
     * different meta-data sections.
     * 
     * @param optionalMdSec
     *            perhaps existing meta-data section to be added if it exists
     * @param mdSecTypeGetter
     *            the getter via which the meta-data section can be added to the
     *            administrative meta-data section
     * @param amdSec
     *            administrative meta-data section to which the meta-data
     *            section should be added, if any
     * @param div
     *            div where ADMID references must be added to the generated
     *            meta-data sections
     * @return whether something has been added to the administrative meta-data
     *         section
     */
    private static boolean addMdSec(Optional<MdSecType> optionalMdSec,
            Function<AmdSecType, List<MdSecType>> mdSecTypeGetter,
            AmdSecType amdSec, DivType div) {

        if (!optionalMdSec.isPresent()) {
            return false;
        } else {
            MdSecType mdSec = optionalMdSec.get();
            mdSec.setID(UUID.randomUUID().toString());
            mdSecTypeGetter.apply(amdSec).add(mdSec);
            div.getADMID().add(mdSec);
            return true;
        }
    }
}
