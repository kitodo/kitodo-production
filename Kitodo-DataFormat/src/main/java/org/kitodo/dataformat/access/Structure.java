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
import java.util.Optional;
import java.util.Set;
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
 * {@link #substructures}.
 * 
 * It can be described by {@link #metadata}.
 */
public class Structure implements DivXmlElementAccessInterface {
    /**
     * The name of the Q of Kitodo. This parameter is needed to assemble the
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
    private final List<AreaXmlElementAccessInterface> views = new OrderAwareList<AreaXmlElementAccessInterface>(
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
     * @param mediaUnitsMap
     *            From this map, the media units are read, which must be
     *            referenced here by their ID.
     */
    Structure(DivType div, Map<String, Set<MediaUnit>> mediaUnitsMap) {
        label = div.getLABEL();
        metadata = div.getDMDID().parallelStream().filter(MdSecType.class::isInstance).map(MdSecType.class::cast)
                .map(MdSecType::getMdWrap).map(MdWrap::getXmlData).map(XmlData::getAny).flatMap(List::parallelStream)
                .filter(JAXBElement.class::isInstance).map(JAXBElement.class::cast).map(JAXBElement::getValue)
                .filter(KitodoType.class::isInstance).map(KitodoType.class::cast)
                .flatMap(kitodoType -> Stream.concat(
                    kitodoType.getMetadata().parallelStream()
                            .map(metadataType -> new MetadataEntry(MdSec.DMD_SEC, metadataType)),
                    kitodoType.getMetadataGroup().parallelStream()
                            .map(metadataGroupType -> new MetadataEntryGroup(MdSec.DMD_SEC, metadataGroupType))))
                .collect(Collectors.toCollection(HashSet::new));
        orderlabel = div.getORDERLABEL();
        substructures = div.getDiv().stream().map(child -> new Structure(child, mediaUnitsMap))
                .collect(Collectors.toCollection(LinkedList::new));
        type = div.getTYPE();
        mediaUnitsMap.get(div.getID()).stream().map(View::new).forEach(views::add);
    }

    @Override
    public List<AreaXmlElementAccessInterface> getAreas() {
        return views;
    }

    @Override
    public List<DivXmlElementAccessInterface> getChildren() {
        return substructures;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public Collection<MetadataAccessInterface> getMetadata() {
        return metadata;
    }

    @Override
    public String getOrderlabel() {
        return orderlabel;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public void setOrderlabel(String orderlabel) {
        this.orderlabel = orderlabel;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    DivType toDiv(IdentifierProvider idp, Map<MediaUnit, String> mediaUnitIDs, Map<Structure, String> structuresWithIDs,
            LinkedList<Pair<String, String>> smLinkData, Mets mets) {
        DivType div = new DivType();
        String divId = idp.next();
        div.setID(divId);
        div.setLABEL(label);
        div.setORDERLABEL(orderlabel);
        div.setTYPE(type);
        structuresWithIDs.put(this, divId);
        smLinkData.addAll(views.parallelStream().map(AreaXmlElementAccessInterface::getFile).map(mediaUnitIDs::get)
                .map(mediaUnitId -> Pair.of(divId, mediaUnitId)).collect(Collectors.toList()));

        Optional<MdSecType> optionalDmdSec = createMdSec(MdSec.DMD_SEC);
        if (optionalDmdSec.isPresent()) {
            MdSecType dmdSec = optionalDmdSec.get();
            dmdSec.setID(idp.next());
            mets.getDmdSec().add(dmdSec);
            div.getDMDID().add(dmdSec);
        }

        Optional<AmdSecType> optionalAmdSec = createAmdSec(idp, div);
        if (optionalAmdSec.isPresent()) {
            AmdSecType admSec = optionalAmdSec.get();
            mets.getAmdSec().add(admSec);
        }

        for (DivXmlElementAccessInterface substructure : substructures) {
            div.getDiv().add(((Structure) substructure).toDiv(idp, mediaUnitIDs, structuresWithIDs, smLinkData, mets));
        }
        return div;
    }

    private Optional<MdSecType> createMdSec(MdSec domain) {
        KitodoType kitodoType = new KitodoType();
        for (MetadataAccessInterface entry : metadata) {
            if (domain.equals(entry.getDomain())) {
                if (entry instanceof MetadataEntry) {
                    kitodoType.getMetadata().add(((MetadataEntry) entry).toMetadata());
                } else if (entry instanceof MetadataEntryGroup) {
                    kitodoType.getMetadataGroup().add(((MetadataEntryGroup) entry).toMetadataGroup());
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

    private Optional<AmdSecType> createAmdSec(IdentifierProvider idp, DivType div) {
        AmdSecType amdSec = new AmdSecType();
        boolean data = false;
        Optional<MdSecType> optionalSourceMd = createMdSec(MdSec.SOURCE_MD);
        if (optionalSourceMd.isPresent()) {
            optionalSourceMd.get().setID(idp.next());
            amdSec.getSourceMD().add(optionalSourceMd.get());
            div.getADMID().add(optionalSourceMd.get());
            data = true;
        }
        Optional<MdSecType> optionalDigiprovMd = createMdSec(MdSec.DIGIPROV_MD);
        if (optionalDigiprovMd.isPresent()) {
            optionalDigiprovMd.get().setID(idp.next());
            amdSec.getDigiprovMD().add(optionalDigiprovMd.get());
            div.getADMID().add(optionalDigiprovMd.get());
            data = true;
        }
        Optional<MdSecType> optionalRightsMd = createMdSec(MdSec.RIGHTS_MD);
        if (optionalRightsMd.isPresent()) {
            optionalRightsMd.get().setID(idp.next());
            amdSec.getRightsMD().add(optionalRightsMd.get());
            div.getADMID().add(optionalRightsMd.get());
            data = true;
        }
        Optional<MdSecType> optionalTechMd = createMdSec(MdSec.TECH_MD);
        if (optionalTechMd.isPresent()) {
            optionalTechMd.get().setID(idp.next());
            amdSec.getTechMD().add(optionalTechMd.get());
            div.getADMID().add(optionalTechMd.get());
            data = true;
        }
        return data ? Optional.of(amdSec) : Optional.empty();
    }

}
