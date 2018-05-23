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

package org.kitodo.dataeditor;

import java.io.FilenameFilter;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import org.kitodo.dataformat.metskitodo.DivType;
import org.kitodo.dataformat.metskitodo.FileType;
import org.kitodo.dataformat.metskitodo.KitodoType;
import org.kitodo.dataformat.metskitodo.MdSecType;
import org.kitodo.dataformat.metskitodo.MetadataType;
import org.kitodo.dataformat.metskitodo.Mets;
import org.kitodo.dataformat.metskitodo.MetsType;
import org.kitodo.dataformat.metskitodo.StructLinkType;
import org.kitodo.dataformat.metskitodo.StructMapType;

/**
 * This is a wrapper class for holding and manipulating the content of a
 * serialized mets-kitodo format xml file.
 */
public class MetsKitodoWrapper {

    private Mets mets;
    private MetsKitodoObjectFactory objectFactory = new MetsKitodoObjectFactory();
    private FileService fileService = new FileService();

    /**
     * Gets the mets object.
     *
     * @return The mets object.
     */
    public Mets getMets() {
        return mets;
    }

    /**
     * Constructor which creates a Mets object with corresponding object factory and
     * also inserts the basic mets elements (FileSec, StructLink, MetsHdr).
     */
    public MetsKitodoWrapper() throws DatatypeConfigurationException, IOException {
        this.mets = createBasicMetsElements(objectFactory.createMets());
    }

    private Mets createBasicMetsElements(Mets mets) throws DatatypeConfigurationException, IOException {
        if (Objects.isNull(mets.getFileSec())) {
            mets.setFileSec(objectFactory.createMetsTypeFileSec());
            MetsType.FileSec.FileGrp fileGroup = objectFactory.createMetsTypeFileSecFileGrp();
            fileGroup.setUSE("LOCAL");
            mets.getFileSec().getFileGrp().add(fileGroup);
        }
        if (Objects.isNull(mets.getStructLink())) {
            mets.setStructLink(objectFactory.createMetsTypeStructLink());
        }
        if (Objects.isNull(mets.getMetsHdr())) {
            mets.setMetsHdr(objectFactory.createKitodoMetsHeader());
        }
        if (mets.getStructMap().size() == 0) {
            StructMapType logicalStructMapType = objectFactory.createStructMapType();
            logicalStructMapType.setTYPE("LOGICAL");
            mets.getStructMap().add(logicalStructMapType);

            StructMapType physicalStructMapType = objectFactory.createStructMapType();
            physicalStructMapType.setTYPE("PHYSICAL");
            mets.getStructMap().add(physicalStructMapType);
        }
        return mets;
    }

    /**
     * Constructor which creates Mets object by unmarshalling given xml file of
     * mets-kitodo format.
     * 
     * @param xmlFile
     *            The xml file in mets-kitodo format as URI.
     * @param xsltFile
     *            The URI to the xsl file for transformation of old format goobi
     *            metadata files.
     */
    public MetsKitodoWrapper(URI xmlFile, URI xsltFile)
            throws JAXBException, TransformerException, IOException, DatatypeConfigurationException {
        this.mets = createBasicMetsElements(MetsKitodoReader.readAndValidateUriToMets(xmlFile, xsltFile));
    }

    /**
     * Adds a smLink to the structLink section of mets file.
     * 
     * @param from
     *            The from value.
     * @param to
     *            The to value.
     */
    public void addSmLink(String from, String to) {
        StructLinkType.SmLink structLinkTypeSmLink = objectFactory.createStructLinkTypeSmLink();
        structLinkTypeSmLink.setFrom(from);
        structLinkTypeSmLink.setTo(to);
        mets.getStructLink().getSmLinkOrSmLinkGrp().add(structLinkTypeSmLink);
    }

    /**
     * Gets all dmdSec elements.
     *
     * @return All dmdSec elements as list of MdSecType objects.
     */
    public List<MdSecType> getDmdSecs() {
        return this.mets.getDmdSec();
    }

    /**
     * Gets KitodoType object of specified MdSec index.
     * 
     * @param index
     *            The index as int.
     * @return The KitodoType object.
     */
    public KitodoType getKitodoTypeByMdSecIndex(int index) {
        if (this.mets.getDmdSec().size() > index) {
            List<Object> xmlData = getXmlDataByMdSecIndex(index);
            try {
                return MetsKitodoHandler.getFirstGenericTypeFromJaxbObjectList(xmlData, KitodoType.class);
            } catch (NoSuchElementException e) {
                throw new NoSuchElementException(
                        "MdSec element with index: " + index + " does not have kitodo metadata");
            }
        }
        throw new NoSuchElementException("MdSec element with index: " + index + " does not exist");
    }

    /**
     * Gets xml data object of specified MdSec index.
     *
     * @param index
     *            The index as int.
     * @return The KitodoType object.
     */
    public List<Object> getXmlDataByMdSecIndex(int index) {
        return MetsKitodoHandler.getXmlDataOfMetsByMdSecIndex(this.mets, index);
    }

    /**
     * Gets KitodoType object of specified MdSec id.
     *
     * @param id
     *            The id as String.
     * @return The KitodoType object.
     */
    public KitodoType getKitodoTypeByMdSecId(String id) {
        int index = 0;
        for (MdSecType mdSecType : this.mets.getDmdSec()) {
            if (mdSecType.getID().equals(id)) {
                return getKitodoTypeByMdSecIndex(index);
            }
            index++;
        }
        throw new NoSuchElementException("MdSec element with id: " + id + " was not found");
    }

    public void insertFilesFromDirectory(URI directory, FilenameFilter fileFilter, String type) {
        if (fileService.isDirectory(directory)) {
            List<URI> subUris = fileService.getSubUris(fileFilter, directory);
            insertFilesToLocalFileGroup(subUris, type);
            MdSecType physicalMetadataByImagePath = createPhysicalMetadataByImagePath(directory);
            createPhysicalStructMapByFiles(physicalMetadataByImagePath);

        }
    }

    private void insertFilesToLocalFileGroup(List<URI> files, String type) {
        for (URI file : files) {
            insertFileToFileGroup(file, type);
        }
    }

    private void insertFileToFileGroup(URI file, String type) {
        FileType.FLocat fLocat = objectFactory.createFileTypeFLocat();
        fLocat.setLOCTYPE("URL");
        fLocat.setHref(file.getPath());

        FileType fileType = objectFactory.createFileType();
        fileType.setMIMETYPE(type);
        fileType.getFLocat().add(fLocat);

        this.mets.getFileSec().getFileGrp().get(0).getFile().add(fileType);
        writeFileIds();
    }

    private MdSecType.MdWrap wrapKitodoDataByMdWrap(KitodoType kitodoTypeData) {
        MdSecType.MdWrap mdWrap = objectFactory.createMdSecTypeMdWrap();
        mdWrap.setMDTYPE("OTHER");
        mdWrap.setOTHERMDTYPE("KITODO");

        QName qName = new QName(MetsKitodoPrefixMapper.getKitodoUri(), MetsKitodoPrefixMapper.getKitodoPrefix());
        JAXBElement<KitodoType> goobiRoot = new JAXBElement<>(qName, KitodoType.class, kitodoTypeData);

        MdSecType.MdWrap.XmlData xmlData = objectFactory.createMdSecTypeMdWrapXmlData();
        xmlData.getAny().add(goobiRoot);

        mdWrap.setXmlData(xmlData);
        return mdWrap;
    }

    private MdSecType createPhysicalMetadataByImagePath(URI path) {
        KitodoType kitodoType = objectFactory.createKitodoType();
        MetadataType metadata = objectFactory.createMetadataType();
        metadata.setName("pathimagefiles");
        metadata.setValue(path.getRawPath());
        kitodoType.getMetadata().add(metadata);
        MdSecType mdSec = objectFactory.createMdSecType();
        mdSec.setMdWrap(wrapKitodoDataByMdWrap(kitodoType));
        mdSec.setID("DMDPHYS_0000");
        return mdSec;
    }

    public void writeFileIds() {
        int counter = 1;
        for (FileType file : this.mets.getFileSec().getFileGrp().get(0).getFile()) {
            file.setID("FILE_" + String.format("%04d", counter));
            counter++;
        }
    }

    private StructMapType getStructmapByType(String type) {
        for (StructMapType structMap : this.mets.getStructMap()) {
            if (Objects.equals(type,structMap.getTYPE())) {
                return structMap;
            }
        }
        throw new NoSuchElementException("StructMap element of type " + type + " does not exist");
    }

    public void createPhysicalStructMapByFiles(MdSecType physicalMdSec) {
        StructMapType physicalStructMap = getStructmapByType("PHYSICAL");
        DivType rootDiv = objectFactory.createDivType();
        rootDiv.setID("PHYS_0000");
//        rootDiv.getDMDID().add("DMDPHYS_0000");
        rootDiv.setTYPE("Book");
        physicalStructMap.setDiv(rootDiv);

        int counter = 1;
        for (FileType file : this.mets.getFileSec().getFileGrp().get(0).getFile()) {
            DivType div = objectFactory.createDivType();
            div.setID("PHYS" + String.format("%04d", counter));
            div.setORDER(BigInteger.valueOf(counter));
            div.setORDERLABEL("uncounted");
            div.setTYPE("page");
            DivType.Fptr divTypeFptr = objectFactory.createDivTypeFptr();
            divTypeFptr.setFILEID(file);
            div.getFptr().add(divTypeFptr);
            rootDiv.getDiv().add(div);

            counter++;
        }

        int i = 1+1;
//        physicalStructMap.getDiv(.add(rootDiv);
    }
}
