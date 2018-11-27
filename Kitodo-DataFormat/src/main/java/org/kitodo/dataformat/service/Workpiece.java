package org.kitodo.dataformat.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.kitodo.api.dataformat.mets.AgentXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.DivXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.FileXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.MetsXmlElementAccessInterface;
import org.kitodo.dataformat.metskitodo.DivType;
import org.kitodo.dataformat.metskitodo.Mets;
import org.kitodo.dataformat.metskitodo.MetsType.MetsHdr;
import org.kitodo.dataformat.metskitodo.MetsType.MetsHdr.MetsDocumentID;
import org.kitodo.dataformat.metskitodo.StructLinkType.SmLink;
import org.kitodo.dataformat.metskitodo.StructMapType;

/**
 * A workpiece is the administrative structure of the product of an element that
 * has passed through a Production workflow. The file format for this management
 * structure is METS XML after the ZVDD DFG Viewer Application Profile.
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
    private List<ProcessingNote> editHistory = new ArrayList<>();

    /**
     * The media units that belong to this workpiece.
     */
    private LinkedList<MediaUnit> mediaUnits = new LinkedList<>();

    /**
     * The root node of the outline tree.
     */
    private Node structure = new Node();

    /**
     * The identifier of the workpiece.
     */
    private String id;

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
        this.createdate = mets.getMetsHdr().getCREATEDATE().toGregorianCalendar();

        Map<String, MediaVariant> mediaVariants = mets.getFileSec().getFileGrp().parallelStream().map(MediaVariant::new)
                .collect(Collectors.toMap(MediaVariant::getUse, Function.identity()));

        List<DivType> divs = getStructMapsStreamByType(mets, "PHYSICAL").findFirst().get().getDiv().getDiv();
        Map<String, MediaUnit> mediaUnitsForDivIDs = new HashMap<>((int) Math.ceil(divs.size() / 0.75));
        mediaUnits = new LinkedList<>();
        for (DivType div : divs) {
            MediaUnit mediaUnit = new MediaUnit(div, mets, mediaVariants);
            mediaUnits.add(mediaUnit);
            mediaUnitsForDivIDs.put(div.getID(), mediaUnit);
        }

        Map<String, Set<MediaUnit>> mediaUnitsMap = mets.getStructLink().getSmLinkOrSmLinkGrp().parallelStream()
                .filter(SmLink.class::isInstance).map(SmLink.class::cast)
                .collect(new MultiMapCollector<>(SmLink::getFrom, smLink -> mediaUnitsForDivIDs.get(smLink.getTo())));
        structure = getStructMapsStreamByType(mets, "LOGICAL")
                .map(structMapType -> new Node(structMapType.getDiv(), mediaUnitsMap))
                .collect(Collectors.toList()).iterator().next();
    }

    @Override
    public List<? extends FileXmlElementAccessInterface> getFileGrp() {
        return mediaUnits;
    }

    @Override
    public List<? extends AgentXmlElementAccessInterface> getMetsHdr() {
        return editHistory;
    }

    @Override
    public DivXmlElementAccessInterface getStructMap() {
        return structure;
    }

    private static final Stream<StructMapType> getStructMapsStreamByType(Mets mets, String type) {
        return mets.getStructMap().parallelStream().filter(λ -> λ.getTYPE().equals(type));
    }

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
            marshal.marshal(this.toMets(), System.out);
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
        // TODO
        return mets;
    }

    private MetsHdr generateMetsHdr() {
        MetsHdr metsHdr = new MetsHdr();
        metsHdr.setCREATEDATE(MetsUtils.convertDate(createdate));
        metsHdr.setLASTMODDATE(MetsUtils.convertDate(new GregorianCalendar()));
        if (this.id != null) {
            MetsDocumentID id = new MetsDocumentID();
            id.setValue(this.id);
            metsHdr.setMetsDocumentID(id);
        }
        for (ProcessingNote processingNote : editHistory) {
            metsHdr.getAgent().add(processingNote.toAgent());
        }
        return metsHdr;
    }
}
