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
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.MetadataGroup;
import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.api.dataformat.Structure;
import org.kitodo.api.dataformat.View;
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
public class DivXmlElementAccess implements DivXmlElementAccessInterface {
    /**
     * The qualified name of the Kitodo meta-data format, needed to assemble the
     * meta-data entries in METS using JAXB.
     */
    private static final QName KITODO_QNAME = new QName("http://meta.kitodo.org/v1/", "kitodo");

    /**
     * The data object of this div XML element access.
     */
    private final Structure structure;

    /**
     * Public constructor to create a new structure. This constructor can be
     * called via the service loader to get a new structure.
     */
    public DivXmlElementAccess() {
        structure = new Structure();
    }

    DivXmlElementAccess(Structure structure) {
        this.structure = structure;
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
    DivXmlElementAccess(DivType div, Mets mets, Map<String, Set<FileXmlElementAccess>> mediaUnitsMap) {
        this();
        structure.setLabel(div.getLABEL());
        for (Object mdSec : div.getDMDID()) {
            readMetadata((MdSecType) mdSec, MdSec.DMD_SEC);
        }
        for (Object mdSec : div.getADMID()) {
            readMetadata((MdSecType) mdSec, amdSecTypeOf(mets, (MdSecType) mdSec));
        }
        structure.setOrderlabel(div.getORDERLABEL());
        for (DivType child : div.getDiv()) {
            structure.getChildren().add(new DivXmlElementAccess(child, mets, mediaUnitsMap).structure);
        }
        structure.setType(div.getTYPE());
        Set<FileXmlElementAccess> fileXmlElementAccesses = mediaUnitsMap.get(div.getID());
        if (fileXmlElementAccesses != null) {
            for (FileXmlElementAccess fileXmlElementAccess : fileXmlElementAccesses) {
                structure.getViews().add(new AreaXmlElementAccess(fileXmlElementAccess).getView());
            }
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
        structure.getMetadata()
                .addAll(
                    mdSec.getMdWrap().getXmlData().getAny().parallelStream().filter(JAXBElement.class::isInstance)
                            .map(JAXBElement.class::cast).map(JAXBElement::getValue)
                            .filter(KitodoType.class::isInstance).map(KitodoType.class::cast)
                            .flatMap(kitodoType -> Stream.concat(
                                kitodoType.getMetadata().parallelStream()
                                        .map(metadataType -> new MetadataXmlElementAccess(mdSecType,
                                                metadataType).getMetadataEntry()),
                                kitodoType.getMetadataGroup().parallelStream()
                                        .map(metadataGroupType -> new MetadataGroupXmlElementAccess(mdSecType,
                                                metadataGroupType).getMetadataGroup())))
                            .collect(Collectors.toList()));
    }

    @Override
    public List<AreaXmlElementAccessInterface> getAreas() {
        throw new UnsupportedOperationException("discontinued interface method pending removal");
    }

    @Override
    public List<DivXmlElementAccessInterface> getChildren() {
        throw new UnsupportedOperationException("discontinued interface method pending removal");
    }

    @Override
    public String getLabel() {
        throw new UnsupportedOperationException("discontinued interface method pending removal");
    }

    @Override
    public Collection<MetadataAccessInterface> getMetadata() {
        throw new UnsupportedOperationException("discontinued interface method pending removal");
    }

    @Override
    public String getOrderlabel() {
        throw new UnsupportedOperationException("discontinued interface method pending removal");
    }

    Structure getStructure() {
        return structure;
    }

    @Override
    public String getType() {
        throw new UnsupportedOperationException("discontinued interface method pending removal");
    }

    @Override
    public void setLabel(String label) {
        throw new UnsupportedOperationException("discontinued interface method pending removal");
    }

    @Override
    public void setOrderlabel(String orderlabel) {
        throw new UnsupportedOperationException("discontinued interface method pending removal");
    }

    @Override
    public void setType(String type) {
        throw new UnsupportedOperationException("discontinued interface method pending removal");
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
    DivType toDiv(Map<MediaUnit, String> mediaUnitIDs, LinkedList<Pair<String, String>> smLinkData, Mets mets) {
        DivType div = new DivType();
        String divId = UUID.randomUUID().toString();
        div.setID(divId);
        div.setLABEL(structure.getLabel());
        div.setORDERLABEL(structure.getOrderlabel());
        div.setTYPE(structure.getType());
        smLinkData.addAll(structure.getViews().parallelStream().map(View::getMediaUnit).map(mediaUnitIDs::get)
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

        for (Structure substructure : structure.getChildren()) {
            div.getDiv().add(new DivXmlElementAccess(substructure).toDiv(mediaUnitIDs, smLinkData, mets));
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
        for (Metadata entry : structure.getMetadata()) {
            if (domain.equals(entry.getDomain())) {
                if (entry instanceof MetadataEntry) {
                    kitodoType.getMetadata().add(new MetadataXmlElementAccess((MetadataEntry) entry).toMetadata());
                } else if (entry instanceof MetadataGroup) {
                    kitodoType.getMetadataGroup()
                            .add(new MetadataGroupXmlElementAccess((MetadataGroup) entry).toXMLMetadataGroup());
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
                | addMdSec(createMdSec(MdSec.TECH_MD), AmdSecType::getTechMD, amdSec, div) ? Optional.of(amdSec)
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
            Function<AmdSecType, List<MdSecType>> mdSecTypeGetter, AmdSecType amdSec, DivType div) {

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
