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
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
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
import org.kitodo.api.dataformat.LinkedStructure;
import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.api.dataformat.MediaVariant;
import org.kitodo.api.dataformat.ProcessingNote;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.api.dataformat.mets.MetsXmlElementAccessInterface;
import org.kitodo.dataformat.metskitodo.DivType;
import org.kitodo.dataformat.metskitodo.DivType.Mptr;
import org.kitodo.dataformat.metskitodo.FileType;
import org.kitodo.dataformat.metskitodo.Mets;
import org.kitodo.dataformat.metskitodo.MetsType;
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
 * A {@code Workpiece} has two essential characteristics: {@link FileXmlElementAccess}s and
 * an outline {@link DivXmlElementAccess}. {@code MediaUnit}s are the types of every
 * single digital medium on a conceptual level, such as the individual pages of
 * a book. Each {@code MediaUnit} can be in different {@link UseXmlAttributeAccess}s (for
 * example, in different resolutions or file formats). Each {@code MediaVariant}
 * of a {@code MediaUnit} resides in a {@link FLocatXmlElementAccess} in the data store.
 *
 * <p>
 * The {@code Structure} is a tree structure that can be finely subdivided, e.g.
 * a book, in which the chapters, in it individual elements such as tables or
 * figures. Each outline level points to the {@code MediaUnit}s that belong to
 * it via {@link AreaXmlElementAccess}s. Currently, a {@code View} always contains exactly one
 * {@code MediaUnit} unit, here a simple expandability is provided, so that in a
 * future version excerpts from {@code MediaUnit}s can be described. Each
 * outline level can be described with any {@link MetadataXmlElementsAccess}.
 *
 * @see "https://www.zvdd.de/fileadmin/AGSDD-Redaktion/METS_Anwendungsprofil_2.0.pdf"
 */
public class MetsXmlElementAccess implements MetsXmlElementAccessInterface {
    /**
     * There must not be multiple references to the child, or they must be
     * identical.
     */
    private static final BinaryOperator<LinkedList<LinkedStructure>> CHILD_REFERENCED_ONCE = (one, another) -> {
        if (one.equals(another)) {
            return one;
        }
        throw new IllegalStateException("Child is referenced from parent multiple times");
    };

    /**
     * The data object of this mets XML element access.
     */
    private final Workpiece workpiece;

    /**
     * Creates an empty workpiece. This is the default state when the editor
     * starts. You can either load a file or create a new one.
     */
    public MetsXmlElementAccess() {
        workpiece = new Workpiece();
    }

    private MetsXmlElementAccess(Workpiece workpiece) {
        this.workpiece = workpiece;
    }

    static final Workpiece toWorkpiece(Mets mets,
            Function<Pair<URI, Boolean>, InputStream> getInputStreamFunction) {
        Workpiece workpiece = new Workpiece();
        MetsHdr metsHdr = mets.getMetsHdr();
        if (Objects.nonNull(metsHdr)) {
            workpiece.setCreationDate(metsHdr.getCREATEDATE().toGregorianCalendar());
            for (Agent agent : metsHdr.getAgent()) {
                workpiece.getEditHistory().add(new AgentXmlElementAccess(agent).getProcessingNote());
            }
            MetsDocumentID metsDocumentID = metsHdr.getMetsDocumentID();
            if (Objects.nonNull(metsDocumentID)) {
                workpiece.setId(metsDocumentID.getID());
            }
        }
        FileSec fileSec = mets.getFileSec();
        Map<String, MediaVariant> useXmlAttributeAccess = fileSec != null
                ? fileSec.getFileGrp().parallelStream().map(UseXmlAttributeAccess::new)
                        .collect(Collectors.toMap(
                            newUseXmlAttributeAccess -> newUseXmlAttributeAccess.getMediaVariant().getUse(),
                            UseXmlAttributeAccess::getMediaVariant))
                : new HashMap<>();
        Optional<StructMapType> optionalPhysicalStructMap = getStructMapsStreamByType(mets, "PHYSICAL").findFirst();
        Map<String, FileXmlElementAccess> divIDsToMediaUnits = new HashMap<>();
        if (optionalPhysicalStructMap.isPresent()) {
            DivType div = optionalPhysicalStructMap.get().getDiv();
            FileXmlElementAccess fileXmlElementAccess = new FileXmlElementAccess(div, mets, useXmlAttributeAccess);
            MediaUnit mediaUnit = fileXmlElementAccess.getMediaUnit();
            workpiece.setMediaUnit(mediaUnit);
            divIDsToMediaUnits.put(div.getID(), fileXmlElementAccess);
            readMeadiaUnitsTreeRecursive(div, mets, useXmlAttributeAccess, mediaUnit, divIDsToMediaUnits);
        }
        StructLink structLink = mets.getStructLink();
        if (structLink == null) {
            structLink = new StructLink();
        }
        Map<String, Set<FileXmlElementAccess>> mediaUnitsMap = new HashMap<>();
        for (Object smLinkOrSmLinkGrp : mets.getStructLink().getSmLinkOrSmLinkGrp()) {
            if (smLinkOrSmLinkGrp instanceof SmLink) {
                SmLink smLink = (SmLink) smLinkOrSmLinkGrp;
                mediaUnitsMap.computeIfAbsent(smLink.getFrom(), any -> new HashSet<>());
                mediaUnitsMap.get(smLink.getFrom()).add(divIDsToMediaUnits.get(smLink.getTo()));
            }
        }
        /*
         * If the topmost <mets:div> contains a <mets:mptr>, then it is a holder
         * <div> for that <mptr> and must be skipped.
         */
        workpiece.setStructure(
            getStructMapsStreamByType(mets, "LOGICAL").map(structMap -> structMap.getDiv())
                .map(div -> div.getMptr().isEmpty() ? div : div.getDiv().get(0))
                    .map(div -> new DivXmlElementAccess(div, mets, mediaUnitsMap, getInputStreamFunction))
                .collect(Collectors.toList()).iterator().next());
        workpiece.getUplinks().addAll(readUplinks(mets, getInputStreamFunction));
        return workpiece;
    }

    private static void readMeadiaUnitsTreeRecursive(DivType div, Mets mets,
            Map<String, MediaVariant> useXmlAttributeAccess,
            MediaUnit mediaUnit, Map<String, FileXmlElementAccess> divIDsToMediaUnits) {

        for (DivType child : div.getDiv()) {
            FileXmlElementAccess fileXmlElementAccess = new FileXmlElementAccess(child, mets, useXmlAttributeAccess);
            MediaUnit childMediaUnit = fileXmlElementAccess.getMediaUnit();
            mediaUnit.getChildren().add(childMediaUnit);
            divIDsToMediaUnits.put(child.getID(), fileXmlElementAccess);
            readMeadiaUnitsTreeRecursive(child, mets, useXmlAttributeAccess, childMediaUnit, divIDsToMediaUnits);
        }
    }

    private static final LinkedList<LinkedStructure> findMyself(DivType div, Mets current, URI parentUri,
            Function<Pair<URI, Boolean>, InputStream> getInputStreamFunction) {

        if (!div.getMptr().isEmpty()) {
            boolean found = div.getMptr().stream().map(Mptr::getHref)
                    .map(href -> Objects.deepEquals(readMets(getInputStreamFunction, hrefToUri(href), false), current))
                    .reduce(Boolean::logicalOr).get();
            return found ? new LinkedList<>() : null;
        } else {
            Optional<LinkedList<LinkedStructure>> optionalResult = div.getDiv().stream()
                    .map(child -> findMyself(child, current, parentUri, getInputStreamFunction))
                    .filter(Objects::nonNull).reduce(CHILD_REFERENCED_ONCE);
            if (optionalResult.isPresent()) {
                LinkedStructure linkedStructure = new LinkedStructure();
                linkedStructure.setLabel(div.getLABEL());
                linkedStructure.setType(div.getTYPE());
                linkedStructure.setOrder(div.getORDER());
                linkedStructure.setUri(parentUri);
                LinkedList<LinkedStructure> result = optionalResult.get();
                result.addFirst(linkedStructure);
                return result;
            }
        }
        return null;
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

    static final URI hrefToUri(String href) {
        try {
            return new URI(href);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    /**
     * Reads METS from an InputStream. JAXB is used to parse the XML.
     *
     * @param in
     *            InputStream to read from
     * @param getInputStreamFunction
     *            A reference to a function
     *            {@code InputStream getInputStream(URI uri, Boolean couldHaveToBeWrittenInTheFuture)}.
     *            If invoked, the calling function is responsible of closing the
     *            stream.
     */
    @Override
    public Workpiece read(InputStream in, Function<Pair<URI, Boolean>, InputStream> getInputStreamFunction)
            throws IOException {

        return toWorkpiece(readMets(in), getInputStreamFunction);
    }

    /**
     * Reads METS from an InputStream. JAXB is used to parse the XML.
     *
     * @param in
     *            InputStream to read from
     * @param getInputStreamFunction
     *            A reference to a function
     *            {@code InputStream getInputStream(URI uri, Boolean couldHaveToBeWrittenInTheFuture)}.
     *            If invoked, the calling function is responsible of closing the
     *            stream.
     */
    static Mets readMets(InputStream in) throws IOException {
        try {
            JAXBContext jc = JAXBContext.newInstance(Mets.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            return (Mets) unmarshaller.unmarshal(in);
        } catch (JAXBException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw new IOException(e.getMessage(), e);
            }
        }
    }

    static final Mets readMets(Function<Pair<URI, Boolean>, InputStream> getInputStreamFunction, URI uri,
            boolean couldHaveToBeWrittenInTheFuture) {
        try (InputStream in = getInputStreamFunction.apply(Pair.of(uri, couldHaveToBeWrittenInTheFuture))) {
            return readMets(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static final List<LinkedStructure> readUplinks(Mets current,
            Function<Pair<URI, Boolean>, InputStream> getInputStreamFunction) {

        Optional<List<LinkedStructure>> result = getStructMapsStreamByType(current, "LOGICAL")
                .map(structMap -> structMap.getDiv()).filter(div -> !div.getMptr().isEmpty())
                .flatMap(div -> div.getMptr().parallelStream()).map(mptr -> mptr.getHref()).map(href -> {
                    URI parentUri = hrefToUri(href);
                    Mets parent = readMets(getInputStreamFunction, parentUri, false);

                    LinkedList<LinkedStructure> found = getStructMapsStreamByType(parent, "LOGICAL")
                            .map(structMap -> structMap.getDiv())
                            .map(div -> div.getMptr().isEmpty() ? div : div.getDiv().get(0))
                            .map(div -> findMyself(div, current, parentUri, getInputStreamFunction))
                            .filter(Objects::nonNull).reduce(CHILD_REFERENCED_ONCE)
                            .orElseThrow(() -> new IllegalStateException("Child not referenced from parent"));
                    found.addAll(0, readUplinks(parent, getInputStreamFunction));
                    return found;
                }).reduce((one, another) -> {
                    one.addAll(another);
                    return one;
                }).map(linkedList -> (List<LinkedStructure>) linkedList);
        return result.orElse(Collections.emptyList());
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
    public void save(Workpiece workpiece, OutputStream out) throws IOException {
        try {
            JAXBContext context = JAXBContext.newInstance(Mets.class);
            Marshaller marshal = context.createMarshaller();
            marshal.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshal.marshal(new MetsXmlElementAccess(workpiece).toMets(), out);
        } catch (JAXBException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw new IOException(e.getMessage(), e);
            }
        }
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

        Map<URI, FileType> mediaFilesToIDFiles = new HashMap<>();
        mets.setFileSec(generateFileSec(mediaFilesToIDFiles));

        Map<MediaUnit, String> mediaUnitIDs = new HashMap<>();
        mets.getStructMap().add(generatePhysicalStructMap(mediaFilesToIDFiles, mediaUnitIDs, mets));

        LinkedList<Pair<String, String>> smLinkData = new LinkedList<>();
        StructMapType logical = new StructMapType();
        logical.setTYPE("LOGICAL");
        logical.setDiv(new DivXmlElementAccess(workpiece.getStructure()).toDiv(mediaUnitIDs, smLinkData, mets));
        mets.getStructMap().add(logical);

        mets.setStructLink(createStructLink(smLinkData));
        return mets;
    }

    /**
     * Creates the header of the METS file. The header area stores the time
     * stamp, the ID and the processing notes.
     *
     * @return the header of the METS file
     */
    private MetsHdr generateMetsHdr() {
        MetsHdr metsHdr = new MetsHdr();
        metsHdr.setCREATEDATE(convertDate(workpiece.getCreationDate()));
        metsHdr.setLASTMODDATE(convertDate(new GregorianCalendar()));
        if (workpiece.getId() != null) {
            MetsDocumentID id = new MetsDocumentID();
            id.setValue(workpiece.getId());
            metsHdr.setMetsDocumentID(id);
        }
        for (ProcessingNote processingNote : workpiece.getEditHistory()) {
            metsHdr.getAgent().add(new AgentXmlElementAccess(processingNote).toAgent());
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
     * @param mediaFilesToIDFiles
     *            In this map, for each media unit, the corresponding XML file
     *            element is added, so that it can be used for linking later.
     * @return
     */
    private FileSec generateFileSec(Map<URI, FileType> mediaFilesToIDFiles) {
        FileSec fileSec = new FileSec();

        Map<UseXmlAttributeAccess, Set<URI>> useToMediaUnits = new HashMap<>();
        Map<Pair<UseXmlAttributeAccess, URI>, String> fileIds = new HashMap<>();
        generateFileSecRecursive(workpiece.getMediaUnit(), useToMediaUnits, fileIds);

        for (Entry<UseXmlAttributeAccess, Set<URI>> fileGrpData : useToMediaUnits.entrySet()) {
            FileGrp fileGrp = new FileGrp();
            UseXmlAttributeAccess useXmlAttributeAccess = fileGrpData.getKey();
            fileGrp.setUSE(useXmlAttributeAccess.getMediaVariant().getUse());
            String mimeType = useXmlAttributeAccess.getMediaVariant().getMimeType();
            Map<URI, FileType> files = fileGrpData.getValue().parallelStream()
                    .map(uri -> Pair.of(uri,
                        new FLocatXmlElementAccess(uri).toFile(mimeType,
                            fileIds.get(Pair.of(useXmlAttributeAccess, uri)))))
                    .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
            mediaFilesToIDFiles.putAll(files);
            fileGrp.getFile().addAll(files.values());
            fileSec.getFileGrp().add(fileGrp);
        }
        return fileSec;
    }

    private void generateFileSecRecursive(MediaUnit mediaUnit, Map<UseXmlAttributeAccess, Set<URI>> useToMediaUnits,
            Map<Pair<UseXmlAttributeAccess, URI>, String> fileIds) {

        for (Entry<MediaVariant, URI> variantEntry : mediaUnit.getMediaFiles().entrySet()) {
            UseXmlAttributeAccess use = new UseXmlAttributeAccess(variantEntry.getKey());
            useToMediaUnits.computeIfAbsent(use, any -> new HashSet<>());
            URI uri = variantEntry.getValue();
            useToMediaUnits.get(use).add(uri);
            if (mediaUnit instanceof MediaUnitMetsReferrerStorage) {
                fileIds.put(Pair.of(use, uri), ((MediaUnitMetsReferrerStorage) mediaUnit).getFileId(uri));
            }
        }
        for (MediaUnit child : mediaUnit.getChildren()) {
            generateFileSecRecursive(child, useToMediaUnits, fileIds);
        }
    }

    /**
     * Creates the physical struct map. In the physical struct map, the
     * individual files with their variants are enumerated and labeled.
     *
     * @param mediaFilesToIDFiles
     *            A map of the media files to the XML file elements used to
     *            declare them in the file section. To output a link to the ID,
     *            the XML element must be passed to JAXB.
     * @param mediaUnitIDs
     *            In this map, the function returns the assigned identifier for
     *            each media unit so that the link pairs of the struct link
     *            section can be formed later.
     * @param mets
     *            the METS structure in which the meta-data is added
     * @return the physical struct map
     */
    private StructMapType generatePhysicalStructMap(
            Map<URI, FileType> mediaFilesToIDFiles, Map<MediaUnit, String> mediaUnitIDs, MetsType mets) {
        StructMapType physical = new StructMapType();
        physical.setTYPE("PHYSICAL");
        physical.setDiv(
            generatePhysicalStructMapRecursive(workpiece.getMediaUnit(), mediaFilesToIDFiles, mediaUnitIDs, mets));
        return physical;
    }

    private DivType generatePhysicalStructMapRecursive(MediaUnit mediaUnit, Map<URI, FileType> mediaFilesToIDFiles,
            Map<MediaUnit, String> mediaUnitIDs, MetsType mets) {
        DivType div = new FileXmlElementAccess(mediaUnit).toDiv(mediaFilesToIDFiles, mediaUnitIDs, mets);
        for (MediaUnit child : mediaUnit.getChildren()) {
            div.getDiv().add(generatePhysicalStructMapRecursive(child, mediaFilesToIDFiles, mediaUnitIDs, mets));
        }
        return div;
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
