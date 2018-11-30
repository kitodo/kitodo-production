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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.api.dataformat.mets.AgentXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.FLocatXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.FileXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.MetsXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.UseXmlAttributeAccessInterface;
import org.kitodo.dataformat.metskitodo.DivType;
import org.kitodo.dataformat.metskitodo.FileType;
import org.kitodo.dataformat.metskitodo.Mets;
import org.kitodo.dataformat.metskitodo.MetsType.FileSec;
import org.kitodo.dataformat.metskitodo.MetsType.FileSec.FileGrp;
import org.kitodo.dataformat.metskitodo.MetsType.MetsHdr;
import org.kitodo.dataformat.metskitodo.MetsType.MetsHdr.Agent;
import org.kitodo.dataformat.metskitodo.MetsType.MetsHdr.MetsDocumentID;
import org.kitodo.dataformat.metskitodo.MetsType.StructLink;
import org.kitodo.dataformat.metskitodo.StructLinkType.SmLink;
import org.kitodo.dataformat.metskitodo.StructMapType;

/**
 * The administrative structure of the product of an element that passes through
 * a Production workflow. The file format for this management structure is METS
 * XML after the ZVDD DFG Viewer Application Profile.
 * 
 * <p>
 * A {@code Workpiece} has two essential characteristics: {@link MediaUnit}s and
 * an outline {@link Structure}. {@code MediaUnit}s are the types of every
 * single digital medium on a conceptual level, such as the individual pages of
 * a book. Each {@code MediaUnit} can be in different {@link MediaVariant}s (for
 * example, in different resolutions or file formats). Each {@code MediaVariant}
 * of a {@code MediaUnit} resides in a {@link MediaFile} in the data store.
 * 
 * <p>
 * The {@code Structure} is a tree structure that can be finely subdivided, e.g.
 * a book, in which the chapters, in it individual elements such as tables or
 * figures. Each outline level points to the {@code MediaUnit}s that belong to
 * it via {@link View}s. Currently, a {@code View} always contains exactly one
 * {@code MediaUnit} unit, here a simple expandability is provided, so that in a
 * future version excerpts from {@code MediaUnit}s can be described. Each
 * outline level can be described with any {@link Metadata}.
 * 
 * @see "https://www.zvdd.de/fileadmin/AGSDD-Redaktion/METS_Anwendungsprofil_2.0.pdf"
 */
public class Workpiece implements MetsXmlElementAccessInterface {
    /**
     * The time this file was first created.
     */
    private GregorianCalendar createdate = new GregorianCalendar();

    /**
     * The processing history.
     */
    private List<AgentXmlElementAccessInterface> editHistory = new ArrayList<>();

    /**
     * The identifier of the workpiece.
     */
    private String id;

    /**
     * The media units that belong to this workpiece.
     */
    private List<FileXmlElementAccessInterface> mediaUnits = new LinkedList<>();

    /**
     * The logical structure.
     */
    private Structure structure = new Structure();

    /**
     * Creates an empty workpiece. This is the default state when the editor
     * starts. You can either load a file or create a new one.
     */
    public Workpiece() {
    }

    /**
     * Creates a workpiece from a METS XML structure. Due to limitations of the
     * API, this can only be done by calling {@link #read(InputStream)} and then
     * replacing the content of the current editor, but at least the
     * implementation is clean.
     * 
     * @param mets
     *            METS XML structure to read
     */
    private Workpiece(Mets mets) {
        createdate = mets.getMetsHdr().getCREATEDATE().toGregorianCalendar();
        for (Agent agent : mets.getMetsHdr().getAgent()) {
            editHistory.add(new ProcessingNote(agent));
        }
        Map<String, MediaVariant> mediaVariants = mets.getFileSec().getFileGrp().parallelStream().map(MediaVariant::new)
                .collect(Collectors.toMap(MediaVariant::getUse, Function.identity()));
        List<DivType> physicalDivs = getStructMapsStreamByType(mets, "PHYSICAL").findFirst().get().getDiv().getDiv();
        Map<String, MediaUnit> divIDsToMediaUnits = new HashMap<>((int) Math.ceil(physicalDivs.size() / 0.75));
        for (DivType div : physicalDivs) {
            MediaUnit mediaUnit = new MediaUnit(div, mets, mediaVariants);
            mediaUnits.add(mediaUnit);
            divIDsToMediaUnits.put(div.getID(), mediaUnit);
        }
        Map<String, Set<MediaUnit>> mediaUnitsMap = mets.getStructLink().getSmLinkOrSmLinkGrp().parallelStream()
                .filter(SmLink.class::isInstance).map(SmLink.class::cast)
                .collect(new MultiMapCollector<>(SmLink::getFrom, smLink -> divIDsToMediaUnits.get(smLink.getTo())));
        structure = getStructMapsStreamByType(mets, "LOGICAL")
                .map(structMap -> new Structure(structMap.getDiv(), mediaUnitsMap)).collect(Collectors.toList())
                .iterator().next();
    }

    /**
     * Returns the media units of this workpiece. An ordered list of media units
     * is used to digitally represent a cultural work. The order is of minor
     * importance at this point. It rather describes the order in which the
     * media units are displayed on the workstation of the compiler, which
     * determines the presentation form intended for the consumer (and thus also
     * their presentation order). Mostly this is the order in which each digital
     * part was recorded. The order of this list is described by the order of
     * the {@code <div>} elements in the {@code <structMap TYPE="PHYSICAL">} in
     * the METS file and need not necessarily be the same as the order of the
     * media units when referenced from the structure, which is determined by
     * the ORDER attribute of the {@code <file>} elements.
     * 
     * @return the media units
     */
    @Override
    public List<FileXmlElementAccessInterface> getFileGrp() {
        return mediaUnits;
    }

    /**
     * Returns the edit history. The head of each METS file has space for
     * processing notes of the individual editors. In this way, the processing
     * process of the digital representation can be understood.
     * 
     * @return the edit history
     */
    @Override
    public List<AgentXmlElementAccessInterface> getMetsHdr() {
        return editHistory;
    }

    /**
     * Returns the root element of the structure.
     * 
     * @return root element of the structure
     */
    @Override
    public Structure getStructMap() {
        return structure;
    }

    /**
     * The method helps to read {@code <structMap>}s from METS.
     * 
     * @param mets
     *            METS that can be read from
     * @param type
     *            type of the {@code <structMap>} to read
     * @return a stream of {@code <structMap>}s
     */
    private static final Stream<StructMapType> getStructMapsStreamByType(Mets mets, String type) {
        return mets.getStructMap().parallelStream().filter(structMap -> structMap.getTYPE().equals(type));
    }

    /**
     * Reads METS from an InputStream. JAXB is used to parse the XML.
     * 
     * @param in
     *            InputStream to read from
     */
    @Override
    public void read(InputStream in) throws IOException {
        try {
            JAXBContext jc = JAXBContext.newInstance(Mets.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            Mets mets = (Mets) unmarshaller.unmarshal(in);
            Workpiece workpiece = new Workpiece(mets);
            this.replace(workpiece);
        } catch (JAXBException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw new IOException(e.getMessage(), e);
            }
        }
    }

    /**
     * Replaces the contents of this workpiece to the contents of another
     * workpiece. This function does not implement a clone but merely sets the
     * state variables of this instance to the state variables of the passed
     * instance.
     * 
     * @param workpiece
     *            content to be set
     */
    private void replace(Workpiece workpiece) {
        this.mediaUnits = workpiece.mediaUnits;
        this.structure = workpiece.structure;
    }

    /**
     * Writes the contents of this workpiece as a METS file into an output
     * stream.
     * 
     * @param out
     *            writable output stream
     * @throws IOException
     *             if the output device has an error
     */
    @Override
    public void save(OutputStream out) throws IOException {
        try {
            JAXBContext context = JAXBContext.newInstance(Mets.class);
            Marshaller marshal = context.createMarshaller();
            marshal.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshal.marshal(this.toMets(), out);
        } catch (JAXBException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw new IOException(e.getMessage(), e);
            }
        }
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Generates a METS XML structure from this workpiece in the form of Java
     * objects in the main memory.
     * 
     * @return a METS XML structure from this workpiece
     */
    private Mets toMets() {
        Mets mets = new Mets();
        mets.setMetsHdr(generateMetsHdr());

        IdentifierProvider identifierProvider = new IdentifierProvider();
        Map<MediaFile, FileType> mediaFilesToIDFiles = new HashMap<>();
        mets.setFileSec(generateFileSec(identifierProvider, mediaFilesToIDFiles));

        Map<MediaUnit, String> mediaUnitIDs = new HashMap<>();
        mets.getStructMap().add(generatePhysicalStructMap(identifierProvider, mediaFilesToIDFiles, mediaUnitIDs));

        Map<Structure, String> structuresWithIDs = new HashMap<>();
        LinkedList<Pair<String, String>> smLinkData = new LinkedList<>();
        StructMapType logical = new StructMapType();
        logical.setTYPE("LOGICAL");
        logical.setDiv(structure.toDiv(identifierProvider, mediaUnitIDs, structuresWithIDs, smLinkData, mets));
        mets.getStructMap().add(logical);

        mets.setStructLink(createStructLink(smLinkData));
        return mets;
    }

    /**
     * Creates the header of the METS file. The header area stores the
     * timestamp, the ID and the processing notes.
     * 
     * @return the header of the METS file
     */
    private MetsHdr generateMetsHdr() {
        MetsHdr metsHdr = new MetsHdr();
        metsHdr.setCREATEDATE(convertDate(createdate));
        metsHdr.setLASTMODDATE(convertDate(new GregorianCalendar()));
        if (this.id != null) {
            MetsDocumentID id = new MetsDocumentID();
            id.setValue(this.id);
            metsHdr.setMetsDocumentID(id);
        }
        for (AgentXmlElementAccessInterface processingNote : editHistory) {
            metsHdr.getAgent().add(((ProcessingNote) processingNote).toAgent());
        }
        return metsHdr;
    }

    /**
     * Creates an object of class XMLGregorianCalendar. Creating this
     * JAXB-specific class is quite complicated and has therefore been
     * outsourced to a separate method.
     * 
     * @param gregorianCalendar
     *            value of the calendar
     * @return an object of class XMLGregorianCalendar
     */
    private static XMLGregorianCalendar convertDate(GregorianCalendar gregorianCalendar) {
        DatatypeFactory datatypeFactory;
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            String message = e.getMessage();
            throw new NoClassDefFoundError(message != null ? message
                    : "Implementation of DatatypeFactory not available or cannot be instantiated.");
        }
        return datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
    }

    /**
     * Creates the file section. In the file section of a METS file after the
     * ZVDD DFG Viewer Application Profile, the files are declared in exactly
     * the opposite way as they are managed in Production. That is, there are
     * file groups, each file group accommodating the files of a media variant.
     * Therefore, the media units are first resolved according to their media
     * variants, then the corresponding XML elements are generated.
     * 
     * @param idp
     *            an object that generates a new, not yet assigned identifier
     *            each time it is called
     * @param mediaFilesToIDFiles
     *            In this map, for each media unit, the corresponding XML file
     *            element is added, so that it can be used for linking later.
     * @return
     */
    private FileSec generateFileSec(IdentifierProvider idp, Map<MediaFile, FileType> mediaFilesToIDFiles) {
        FileSec fileSec = new FileSec();

        Map<MediaVariant, Set<MediaFile>> useToMediaUnits = new HashMap<>();
        for (FileXmlElementAccessInterface mediaUnit : mediaUnits) {
            for (Entry<? extends UseXmlAttributeAccessInterface, ? extends FLocatXmlElementAccessInterface> variantEntry : mediaUnit
                    .getAllUsesWithFLocats()) {
                MediaVariant use = (MediaVariant) variantEntry.getKey();
                useToMediaUnits.computeIfAbsent(use, any -> new HashSet<>());
                useToMediaUnits.get(use).add((MediaFile) variantEntry.getValue());
            }
        }

        for (Entry<MediaVariant, Set<MediaFile>> fileGrpData : useToMediaUnits.entrySet()) {
            FileGrp fileGrp = new FileGrp();
            MediaVariant mediaVariant = fileGrpData.getKey();
            fileGrp.setUSE(mediaVariant.getUse());
            String mimeType = mediaVariant.getMimeType();
            Map<MediaFile, FileType> files = fileGrpData.getValue().parallelStream()
                    .map(mediaFile -> Pair.of(mediaFile, mediaFile.toFile(idp.next(), mimeType)))
                    .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
            mediaFilesToIDFiles.putAll(files);
            fileGrp.getFile().addAll(files.values());
            fileSec.getFileGrp().add(fileGrp);
        }
        return fileSec;
    }

    /**
     * Creates the physical struct map. In the physical struct map, the
     * individual files with their variants are enumerated and labeled.
     * 
     * @param identifierProvider
     *            an object that generates a new, not yet assigned identifier
     *            each time it is called
     * @param mediaFilesToIDFiles
     *            A map of the media files to the XML file elements used to
     *            declare them in the file section. To output a link to the ID,
     *            the XML element must be passed to JAXB.
     * @param mediaUnitIDs
     *            In this map, the function returns the assigned identifier for
     *            each media unit so that the link pairs of the struct link
     *            section can be formed later.
     * @return the physical struct map
     */
    private StructMapType generatePhysicalStructMap(IdentifierProvider identifierProvider,
            Map<MediaFile, FileType> mediaFilesToIDFiles, Map<MediaUnit, String> mediaUnitIDs) {
        StructMapType physical = new StructMapType();
        physical.setTYPE("PHYSICAL");
        DivType boundBook = new DivType();
        for (FileXmlElementAccessInterface mediaUnit : mediaUnits) {
            boundBook.getDiv()
                    .add(((MediaUnit) mediaUnit).toDiv(identifierProvider, mediaFilesToIDFiles, mediaUnitIDs));
        }
        physical.setDiv(boundBook);
        return physical;
    }

    /**
     * Creates the struct link section. The struct link section stores which
     * files are attached to which nodes and leaves of the description
     * structure.
     * 
     * @param smLinkData
     *            The list of related IDs
     * @return the struct link section
     */
    private StructLink createStructLink(LinkedList<Pair<String, String>> smLinkData) {
        StructLink structLink = new StructLink();
        structLink.getSmLinkOrSmLinkGrp().addAll(smLinkData.parallelStream().map(entry -> {
            SmLink smLink = new SmLink();
            smLink.setFrom(entry.getLeft());
            smLink.setTo(entry.getRight());
            return smLink;
        }).collect(Collectors.toList()));
        return structLink;
    }
}
