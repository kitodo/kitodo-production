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

package org.kitodo.dataformat.service;

import java.util.ArrayList;
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

public class Structure implements DivXmlElementAccessInterface {

    private static final QName KITODO_QNAME = new QName("http://meta.kitodo.org/v1/", "kitodo");
    private String label;
    private Collection<Metadata> metadata = new ArrayList<>();
    private String orderlabel;
    private LinkedList<Structure> substructures = new LinkedList<>();
    private String type;
    private List<View> views = new ArrayList<>();

    public Structure() {
    }

    Structure(DivType div, Map<String, Set<MediaUnit>> mediaUnitsMap) {
        substructures = div.getDiv().stream().map(child -> new Structure(child, mediaUnitsMap))
                .collect(Collectors.toCollection(LinkedList::new));
        label = div.getLABEL();
        views = mediaUnitsMap.get(div.getID()).stream().map(View::new)
                .collect(Collectors.toCollection(LinkedList::new));

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
        type = div.getTYPE();
    }

    @Override
    public List<View> getAreas() {
        return views;
    }

    @Override
    public List<Structure> getChildren() {
        return substructures;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public Collection<? extends MetadataAccessInterface> getMetadata() {
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
        views.parallelStream().map(View::getFile).map(mediaUnitIDs::get).map(mediaUnitId -> Pair.of(divId, mediaUnitId))
                .forEach(smLinkData::add);

        Optional<MdSecType> optionalDmdSec = createMdSec(MdSec.DMD_SEC);
        if (optionalDmdSec.isPresent()) {
            MdSecType dmdSec = optionalDmdSec.get();
            dmdSec.setID(idp.next());
            mets.getDmdSec().add(dmdSec);
            div.getDMDID().add(dmdSec);
        }

        Optional<AmdSecType> optionalAmdSec = createAmdSec();
        if (optionalAmdSec.isPresent()) {
            AmdSecType amdSec = optionalAmdSec.get();
            amdSec.setID(idp.next());
            mets.getAmdSec().add(amdSec);
            div.getDMDID().add(amdSec);
        }

        for (Structure substructure : substructures) {
            div.getDiv().add(substructure.toDiv(idp, mediaUnitIDs, structuresWithIDs, smLinkData, mets));
        }
        return div;
    }

    private Optional<MdSecType> createMdSec(MdSec dd) {
        KitodoType kitodoType = new KitodoType();
        metadata.parallelStream().filter(entry -> dd.equals(entry.getDomain())).filter(MetadataEntry.class::isInstance)
                .map(MetadataEntry.class::cast).map(MetadataEntry::toMetadata).forEach(kitodoType.getMetadata()::add);
        metadata.parallelStream().filter(entry -> dd.equals(entry.getDomain()))
                .filter(MetadataEntryGroup.class::isInstance).map(MetadataEntryGroup.class::cast)
                .map(MetadataEntryGroup::toMetadataGroup).forEach(kitodoType.getMetadataGroup()::add);
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

    private Optional<AmdSecType> createAmdSec() {
        AmdSecType amdSec = new AmdSecType();
        boolean data = false;
        Optional<MdSecType> optionalSourceMd = createMdSec(MdSec.SOURCE_MD);
        if (optionalSourceMd.isPresent()) {
            amdSec.getSourceMD().add(optionalSourceMd.get());
            data = true;
        }
        Optional<MdSecType> optionalDigiprovMd = createMdSec(MdSec.DIGIPROV_MD);
        if (optionalDigiprovMd.isPresent()) {
            amdSec.getDigiprovMD().add(optionalDigiprovMd.get());
            data = true;
        }
        Optional<MdSecType> optionalRightsMd = createMdSec(MdSec.RIGHTS_MD);
        if (optionalRightsMd.isPresent()) {
            amdSec.getRightsMD().add(optionalRightsMd.get());
            data = true;
        }
        Optional<MdSecType> optionalTechMd = createMdSec(MdSec.TECH_MD);
        if (optionalTechMd.isPresent()) {
            amdSec.getTechMD().add(optionalTechMd.get());
            data = true;
        }
        return data ? Optional.of(amdSec) : Optional.empty();
    }

}
