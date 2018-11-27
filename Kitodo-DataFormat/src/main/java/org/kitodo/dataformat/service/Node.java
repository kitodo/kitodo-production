package org.kitodo.dataformat.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.JAXBElement;

import org.kitodo.api.dataformat.mets.AreaXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.DivXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.MdSec;
import org.kitodo.api.dataformat.mets.MetadataAccessInterface;
import org.kitodo.dataformat.metskitodo.DivType;
import org.kitodo.dataformat.metskitodo.KitodoType;
import org.kitodo.dataformat.metskitodo.MdSecType;
import org.kitodo.dataformat.metskitodo.MdSecType.MdWrap;
import org.kitodo.dataformat.metskitodo.MdSecType.MdWrap.XmlData;

public class Node implements DivXmlElementAccessInterface {

    private LinkedList<Node> children = new LinkedList<>();
    private Collection<CommonDescription> description = new ArrayList<>();
    private String label;
    private String orderlabel;
    private String type;
    private List<View> views = new ArrayList<>();

    public Node() {
    }

    Node(DivType div, Map<String, Set<MediaUnit>> mediaUnitsMap) {
        children = div.getDiv().stream().map(child -> new Node(child, mediaUnitsMap))
                .collect(Collectors.toCollection(LinkedList::new));
        label = div.getLABEL();
        views = mediaUnitsMap.get(div.getID()).stream().map(View::new)
                .collect(Collectors.toCollection(LinkedList::new));

        description = div.getDMDID().parallelStream().filter(MdSecType.class::isInstance).map(MdSecType.class::cast)
                .map(MdSecType::getMdWrap).map(MdWrap::getXmlData).map(XmlData::getAny).flatMap(List::parallelStream)
                .filter(JAXBElement.class::isInstance).map(JAXBElement.class::cast).map(JAXBElement::getValue)
                .filter(KitodoType.class::isInstance).map(KitodoType.class::cast)
                .flatMap(kitodoType -> Stream.concat(
                    kitodoType.getMetadata().parallelStream()
                            .map(metadataType -> new Description(MdSec.DMD_SEC, metadataType)),
                    kitodoType.getMetadataGroup().parallelStream()
                            .map(metadataGroupType -> new NestedDescription(MdSec.DMD_SEC, metadataGroupType))))
                .collect(Collectors.toCollection(HashSet::new));

        orderlabel = div.getORDERLABEL();
        type = div.getTYPE();
    }

    @Override
    public List<? extends AreaXmlElementAccessInterface> getAreas() {
        return views;
    }

    @Override
    public List<? extends DivXmlElementAccessInterface> getChildren() {
        return children;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public Collection<? extends MetadataAccessInterface> getMetadata() {
        return description;
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
}
