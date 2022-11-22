//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2022.11.22 um 01:29:20 PM CET 
//


package org.kitodo.dataformat.metskitodo;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import org.jvnet.jaxb2_commons.lang.Equals2;
import org.jvnet.jaxb2_commons.lang.EqualsStrategy2;
import org.jvnet.jaxb2_commons.lang.JAXBEqualsStrategy;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;
import org.jvnet.jaxb2_commons.locator.util.LocatorUtils;
import org.w3c.dom.Element;


/**
 * fileType: Complex Type for Files
 * 				The file element provides access to content files for a METS object.  A file element may contain one or more FLocat elements, which provide pointers to a content file, and/or an FContent element, which wraps an encoded version of the file. Note that ALL FLocat and FContent elements underneath a single file element should identify/contain identical copies of a single file.
 * 			
 * 
 * <p>Java-Klasse für fileType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="fileType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="FLocat" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attGroup ref="{http://www.w3.org/1999/xlink}simpleLink"/&gt;
 *                 &lt;attGroup ref="{http://www.loc.gov/METS/}LOCATION"/&gt;
 *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *                 &lt;attribute name="USE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="FContent" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;choice&gt;
 *                   &lt;element name="binData" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/&gt;
 *                   &lt;element name="xmlData" minOccurs="0"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;any processContents='lax' maxOccurs="unbounded"/&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/choice&gt;
 *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *                 &lt;attribute name="USE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="stream" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *                 &lt;attribute name="streamType" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;attribute name="OWNERID" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;attribute name="ADMID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" /&gt;
 *                 &lt;attribute name="DMDID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" /&gt;
 *                 &lt;attribute name="BEGIN" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;attribute name="END" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;attribute name="BETYPE"&gt;
 *                   &lt;simpleType&gt;
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                       &lt;enumeration value="BYTE"/&gt;
 *                     &lt;/restriction&gt;
 *                   &lt;/simpleType&gt;
 *                 &lt;/attribute&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="transformFile" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *                 &lt;attribute name="TRANSFORMTYPE" use="required"&gt;
 *                   &lt;simpleType&gt;
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                       &lt;enumeration value="decompression"/&gt;
 *                       &lt;enumeration value="decryption"/&gt;
 *                     &lt;/restriction&gt;
 *                   &lt;/simpleType&gt;
 *                 &lt;/attribute&gt;
 *                 &lt;attribute name="TRANSFORMALGORITHM" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;attribute name="TRANSFORMKEY" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;attribute name="TRANSFORMBEHAVIOR" type="{http://www.w3.org/2001/XMLSchema}IDREF" /&gt;
 *                 &lt;attribute name="TRANSFORMORDER" use="required" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="file" type="{http://www.loc.gov/METS/}fileType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attGroup ref="{http://www.loc.gov/METS/}FILECORE"/&gt;
 *       &lt;attribute name="ID" use="required" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *       &lt;attribute name="SEQ" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="OWNERID" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="ADMID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" /&gt;
 *       &lt;attribute name="DMDID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" /&gt;
 *       &lt;attribute name="GROUPID" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="USE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="BEGIN" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="END" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="BETYPE"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="BYTE"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;anyAttribute processContents='lax' namespace='##other'/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fileType", propOrder = {
    "fLocat",
    "fContent",
    "stream",
    "transformFile",
    "file"
})
public class FileType implements Equals2
{

    @XmlElement(name = "FLocat")
    protected List<FileType.FLocat> fLocat;
    @XmlElement(name = "FContent")
    protected FileType.FContent fContent;
    protected List<FileType.Stream> stream;
    protected List<FileType.TransformFile> transformFile;
    protected List<FileType> file;
    @XmlAttribute(name = "ID", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "SEQ")
    protected Integer seq;
    @XmlAttribute(name = "OWNERID")
    protected String ownerid;
    @XmlAttribute(name = "ADMID")
    @XmlIDREF
    @XmlSchemaType(name = "IDREFS")
    protected List<Object> admid;
    @XmlAttribute(name = "DMDID")
    @XmlIDREF
    @XmlSchemaType(name = "IDREFS")
    protected List<Object> dmdid;
    @XmlAttribute(name = "GROUPID")
    protected String groupid;
    @XmlAttribute(name = "USE")
    protected String use;
    @XmlAttribute(name = "BEGIN")
    protected String begin;
    @XmlAttribute(name = "END")
    protected String end;
    @XmlAttribute(name = "BETYPE")
    protected String betype;
    @XmlAttribute(name = "MIMETYPE")
    protected String mimetype;
    @XmlAttribute(name = "SIZE")
    protected Long size;
    @XmlAttribute(name = "CREATED")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar created;
    @XmlAttribute(name = "CHECKSUM")
    protected String checksum;
    @XmlAttribute(name = "CHECKSUMTYPE")
    protected String checksumtype;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the fLocat property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fLocat property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFLocat().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FileType.FLocat }
     * 
     * 
     */
    public List<FileType.FLocat> getFLocat() {
        if (fLocat == null) {
            fLocat = new ArrayList<FileType.FLocat>();
        }
        return this.fLocat;
    }

    /**
     * Ruft den Wert der fContent-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FileType.FContent }
     *     
     */
    public FileType.FContent getFContent() {
        return fContent;
    }

    /**
     * Legt den Wert der fContent-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FileType.FContent }
     *     
     */
    public void setFContent(FileType.FContent value) {
        this.fContent = value;
    }

    /**
     * Gets the value of the stream property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the stream property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStream().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FileType.Stream }
     * 
     * 
     */
    public List<FileType.Stream> getStream() {
        if (stream == null) {
            stream = new ArrayList<FileType.Stream>();
        }
        return this.stream;
    }

    /**
     * Gets the value of the transformFile property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the transformFile property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTransformFile().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FileType.TransformFile }
     * 
     * 
     */
    public List<FileType.TransformFile> getTransformFile() {
        if (transformFile == null) {
            transformFile = new ArrayList<FileType.TransformFile>();
        }
        return this.transformFile;
    }

    /**
     * Gets the value of the file property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the file property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFile().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FileType }
     * 
     * 
     */
    public List<FileType> getFile() {
        if (file == null) {
            file = new ArrayList<FileType>();
        }
        return this.file;
    }

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getID() {
        return id;
    }

    /**
     * Legt den Wert der id-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setID(String value) {
        this.id = value;
    }

    /**
     * Ruft den Wert der seq-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSEQ() {
        return seq;
    }

    /**
     * Legt den Wert der seq-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSEQ(Integer value) {
        this.seq = value;
    }

    /**
     * Ruft den Wert der ownerid-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOWNERID() {
        return ownerid;
    }

    /**
     * Legt den Wert der ownerid-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOWNERID(String value) {
        this.ownerid = value;
    }

    /**
     * Gets the value of the admid property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the admid property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getADMID().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getADMID() {
        if (admid == null) {
            admid = new ArrayList<Object>();
        }
        return this.admid;
    }

    /**
     * Gets the value of the dmdid property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dmdid property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDMDID().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getDMDID() {
        if (dmdid == null) {
            dmdid = new ArrayList<Object>();
        }
        return this.dmdid;
    }

    /**
     * Ruft den Wert der groupid-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGROUPID() {
        return groupid;
    }

    /**
     * Legt den Wert der groupid-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGROUPID(String value) {
        this.groupid = value;
    }

    /**
     * Ruft den Wert der use-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUSE() {
        return use;
    }

    /**
     * Legt den Wert der use-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUSE(String value) {
        this.use = value;
    }

    /**
     * Ruft den Wert der begin-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBEGIN() {
        return begin;
    }

    /**
     * Legt den Wert der begin-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBEGIN(String value) {
        this.begin = value;
    }

    /**
     * Ruft den Wert der end-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEND() {
        return end;
    }

    /**
     * Legt den Wert der end-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEND(String value) {
        this.end = value;
    }

    /**
     * Ruft den Wert der betype-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBETYPE() {
        return betype;
    }

    /**
     * Legt den Wert der betype-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBETYPE(String value) {
        this.betype = value;
    }

    /**
     * Ruft den Wert der mimetype-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMIMETYPE() {
        return mimetype;
    }

    /**
     * Legt den Wert der mimetype-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMIMETYPE(String value) {
        this.mimetype = value;
    }

    /**
     * Ruft den Wert der size-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getSIZE() {
        return size;
    }

    /**
     * Legt den Wert der size-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setSIZE(Long value) {
        this.size = value;
    }

    /**
     * Ruft den Wert der created-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCREATED() {
        return created;
    }

    /**
     * Legt den Wert der created-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCREATED(XMLGregorianCalendar value) {
        this.created = value;
    }

    /**
     * Ruft den Wert der checksum-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCHECKSUM() {
        return checksum;
    }

    /**
     * Legt den Wert der checksum-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCHECKSUM(String value) {
        this.checksum = value;
    }

    /**
     * Ruft den Wert der checksumtype-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCHECKSUMTYPE() {
        return checksumtype;
    }

    /**
     * Legt den Wert der checksumtype-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCHECKSUMTYPE(String value) {
        this.checksumtype = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

    public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy2 strategy) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final FileType that = ((FileType) object);
        {
            List<FileType.FLocat> lhsFLocat;
            lhsFLocat = (((this.fLocat!= null)&&(!this.fLocat.isEmpty()))?this.getFLocat():null);
            List<FileType.FLocat> rhsFLocat;
            rhsFLocat = (((that.fLocat!= null)&&(!that.fLocat.isEmpty()))?that.getFLocat():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "fLocat", lhsFLocat), LocatorUtils.property(thatLocator, "fLocat", rhsFLocat), lhsFLocat, rhsFLocat, ((this.fLocat!= null)&&(!this.fLocat.isEmpty())), ((that.fLocat!= null)&&(!that.fLocat.isEmpty())))) {
                return false;
            }
        }
        {
            FileType.FContent lhsFContent;
            lhsFContent = this.getFContent();
            FileType.FContent rhsFContent;
            rhsFContent = that.getFContent();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "fContent", lhsFContent), LocatorUtils.property(thatLocator, "fContent", rhsFContent), lhsFContent, rhsFContent, (this.fContent!= null), (that.fContent!= null))) {
                return false;
            }
        }
        {
            List<FileType.Stream> lhsStream;
            lhsStream = (((this.stream!= null)&&(!this.stream.isEmpty()))?this.getStream():null);
            List<FileType.Stream> rhsStream;
            rhsStream = (((that.stream!= null)&&(!that.stream.isEmpty()))?that.getStream():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "stream", lhsStream), LocatorUtils.property(thatLocator, "stream", rhsStream), lhsStream, rhsStream, ((this.stream!= null)&&(!this.stream.isEmpty())), ((that.stream!= null)&&(!that.stream.isEmpty())))) {
                return false;
            }
        }
        {
            List<FileType.TransformFile> lhsTransformFile;
            lhsTransformFile = (((this.transformFile!= null)&&(!this.transformFile.isEmpty()))?this.getTransformFile():null);
            List<FileType.TransformFile> rhsTransformFile;
            rhsTransformFile = (((that.transformFile!= null)&&(!that.transformFile.isEmpty()))?that.getTransformFile():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "transformFile", lhsTransformFile), LocatorUtils.property(thatLocator, "transformFile", rhsTransformFile), lhsTransformFile, rhsTransformFile, ((this.transformFile!= null)&&(!this.transformFile.isEmpty())), ((that.transformFile!= null)&&(!that.transformFile.isEmpty())))) {
                return false;
            }
        }
        {
            List<FileType> lhsFile;
            lhsFile = (((this.file!= null)&&(!this.file.isEmpty()))?this.getFile():null);
            List<FileType> rhsFile;
            rhsFile = (((that.file!= null)&&(!that.file.isEmpty()))?that.getFile():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "file", lhsFile), LocatorUtils.property(thatLocator, "file", rhsFile), lhsFile, rhsFile, ((this.file!= null)&&(!this.file.isEmpty())), ((that.file!= null)&&(!that.file.isEmpty())))) {
                return false;
            }
        }
        {
            String lhsID;
            lhsID = this.getID();
            String rhsID;
            rhsID = that.getID();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "id", lhsID), LocatorUtils.property(thatLocator, "id", rhsID), lhsID, rhsID, (this.id!= null), (that.id!= null))) {
                return false;
            }
        }
        {
            Integer lhsSEQ;
            lhsSEQ = this.getSEQ();
            Integer rhsSEQ;
            rhsSEQ = that.getSEQ();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "seq", lhsSEQ), LocatorUtils.property(thatLocator, "seq", rhsSEQ), lhsSEQ, rhsSEQ, (this.seq!= null), (that.seq!= null))) {
                return false;
            }
        }
        {
            String lhsOWNERID;
            lhsOWNERID = this.getOWNERID();
            String rhsOWNERID;
            rhsOWNERID = that.getOWNERID();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "ownerid", lhsOWNERID), LocatorUtils.property(thatLocator, "ownerid", rhsOWNERID), lhsOWNERID, rhsOWNERID, (this.ownerid!= null), (that.ownerid!= null))) {
                return false;
            }
        }
        {
            List<Object> lhsADMID;
            lhsADMID = (((this.admid!= null)&&(!this.admid.isEmpty()))?this.getADMID():null);
            List<Object> rhsADMID;
            rhsADMID = (((that.admid!= null)&&(!that.admid.isEmpty()))?that.getADMID():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "admid", lhsADMID), LocatorUtils.property(thatLocator, "admid", rhsADMID), lhsADMID, rhsADMID, ((this.admid!= null)&&(!this.admid.isEmpty())), ((that.admid!= null)&&(!that.admid.isEmpty())))) {
                return false;
            }
        }
        {
            List<Object> lhsDMDID;
            lhsDMDID = (((this.dmdid!= null)&&(!this.dmdid.isEmpty()))?this.getDMDID():null);
            List<Object> rhsDMDID;
            rhsDMDID = (((that.dmdid!= null)&&(!that.dmdid.isEmpty()))?that.getDMDID():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "dmdid", lhsDMDID), LocatorUtils.property(thatLocator, "dmdid", rhsDMDID), lhsDMDID, rhsDMDID, ((this.dmdid!= null)&&(!this.dmdid.isEmpty())), ((that.dmdid!= null)&&(!that.dmdid.isEmpty())))) {
                return false;
            }
        }
        {
            String lhsGROUPID;
            lhsGROUPID = this.getGROUPID();
            String rhsGROUPID;
            rhsGROUPID = that.getGROUPID();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "groupid", lhsGROUPID), LocatorUtils.property(thatLocator, "groupid", rhsGROUPID), lhsGROUPID, rhsGROUPID, (this.groupid!= null), (that.groupid!= null))) {
                return false;
            }
        }
        {
            String lhsUSE;
            lhsUSE = this.getUSE();
            String rhsUSE;
            rhsUSE = that.getUSE();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "use", lhsUSE), LocatorUtils.property(thatLocator, "use", rhsUSE), lhsUSE, rhsUSE, (this.use!= null), (that.use!= null))) {
                return false;
            }
        }
        {
            String lhsBEGIN;
            lhsBEGIN = this.getBEGIN();
            String rhsBEGIN;
            rhsBEGIN = that.getBEGIN();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "begin", lhsBEGIN), LocatorUtils.property(thatLocator, "begin", rhsBEGIN), lhsBEGIN, rhsBEGIN, (this.begin!= null), (that.begin!= null))) {
                return false;
            }
        }
        {
            String lhsEND;
            lhsEND = this.getEND();
            String rhsEND;
            rhsEND = that.getEND();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "end", lhsEND), LocatorUtils.property(thatLocator, "end", rhsEND), lhsEND, rhsEND, (this.end!= null), (that.end!= null))) {
                return false;
            }
        }
        {
            String lhsBETYPE;
            lhsBETYPE = this.getBETYPE();
            String rhsBETYPE;
            rhsBETYPE = that.getBETYPE();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "betype", lhsBETYPE), LocatorUtils.property(thatLocator, "betype", rhsBETYPE), lhsBETYPE, rhsBETYPE, (this.betype!= null), (that.betype!= null))) {
                return false;
            }
        }
        {
            String lhsMIMETYPE;
            lhsMIMETYPE = this.getMIMETYPE();
            String rhsMIMETYPE;
            rhsMIMETYPE = that.getMIMETYPE();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "mimetype", lhsMIMETYPE), LocatorUtils.property(thatLocator, "mimetype", rhsMIMETYPE), lhsMIMETYPE, rhsMIMETYPE, (this.mimetype!= null), (that.mimetype!= null))) {
                return false;
            }
        }
        {
            Long lhsSIZE;
            lhsSIZE = this.getSIZE();
            Long rhsSIZE;
            rhsSIZE = that.getSIZE();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "size", lhsSIZE), LocatorUtils.property(thatLocator, "size", rhsSIZE), lhsSIZE, rhsSIZE, (this.size!= null), (that.size!= null))) {
                return false;
            }
        }
        {
            XMLGregorianCalendar lhsCREATED;
            lhsCREATED = this.getCREATED();
            XMLGregorianCalendar rhsCREATED;
            rhsCREATED = that.getCREATED();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "created", lhsCREATED), LocatorUtils.property(thatLocator, "created", rhsCREATED), lhsCREATED, rhsCREATED, (this.created!= null), (that.created!= null))) {
                return false;
            }
        }
        {
            String lhsCHECKSUM;
            lhsCHECKSUM = this.getCHECKSUM();
            String rhsCHECKSUM;
            rhsCHECKSUM = that.getCHECKSUM();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "checksum", lhsCHECKSUM), LocatorUtils.property(thatLocator, "checksum", rhsCHECKSUM), lhsCHECKSUM, rhsCHECKSUM, (this.checksum!= null), (that.checksum!= null))) {
                return false;
            }
        }
        {
            String lhsCHECKSUMTYPE;
            lhsCHECKSUMTYPE = this.getCHECKSUMTYPE();
            String rhsCHECKSUMTYPE;
            rhsCHECKSUMTYPE = that.getCHECKSUMTYPE();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "checksumtype", lhsCHECKSUMTYPE), LocatorUtils.property(thatLocator, "checksumtype", rhsCHECKSUMTYPE), lhsCHECKSUMTYPE, rhsCHECKSUMTYPE, (this.checksumtype!= null), (that.checksumtype!= null))) {
                return false;
            }
        }
        return true;
    }

    public boolean equals(Object object) {
        final EqualsStrategy2 strategy = JAXBEqualsStrategy.INSTANCE;
        return equals(null, null, object, strategy);
    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;choice&gt;
     *         &lt;element name="binData" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/&gt;
     *         &lt;element name="xmlData" minOccurs="0"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;any processContents='lax' maxOccurs="unbounded"/&gt;
     *                 &lt;/sequence&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *       &lt;/choice&gt;
     *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
     *       &lt;attribute name="USE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "binData",
        "xmlData"
    })
    public static class FContent implements Equals2
    {

        protected byte[] binData;
        protected FileType.FContent.XmlData xmlData;
        @XmlAttribute(name = "ID")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlID
        @XmlSchemaType(name = "ID")
        protected String id;
        @XmlAttribute(name = "USE")
        protected String use;

        /**
         * Ruft den Wert der binData-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     byte[]
         */
        public byte[] getBinData() {
            return binData;
        }

        /**
         * Legt den Wert der binData-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     byte[]
         */
        public void setBinData(byte[] value) {
            this.binData = value;
        }

        /**
         * Ruft den Wert der xmlData-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link FileType.FContent.XmlData }
         *     
         */
        public FileType.FContent.XmlData getXmlData() {
            return xmlData;
        }

        /**
         * Legt den Wert der xmlData-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link FileType.FContent.XmlData }
         *     
         */
        public void setXmlData(FileType.FContent.XmlData value) {
            this.xmlData = value;
        }

        /**
         * Ruft den Wert der id-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getID() {
            return id;
        }

        /**
         * Legt den Wert der id-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setID(String value) {
            this.id = value;
        }

        /**
         * Ruft den Wert der use-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getUSE() {
            return use;
        }

        /**
         * Legt den Wert der use-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setUSE(String value) {
            this.use = value;
        }

        public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy2 strategy) {
            if ((object == null)||(this.getClass()!= object.getClass())) {
                return false;
            }
            if (this == object) {
                return true;
            }
            final FileType.FContent that = ((FileType.FContent) object);
            {
                byte[] lhsBinData;
                lhsBinData = this.getBinData();
                byte[] rhsBinData;
                rhsBinData = that.getBinData();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "binData", lhsBinData), LocatorUtils.property(thatLocator, "binData", rhsBinData), lhsBinData, rhsBinData, (this.binData!= null), (that.binData!= null))) {
                    return false;
                }
            }
            {
                FileType.FContent.XmlData lhsXmlData;
                lhsXmlData = this.getXmlData();
                FileType.FContent.XmlData rhsXmlData;
                rhsXmlData = that.getXmlData();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "xmlData", lhsXmlData), LocatorUtils.property(thatLocator, "xmlData", rhsXmlData), lhsXmlData, rhsXmlData, (this.xmlData!= null), (that.xmlData!= null))) {
                    return false;
                }
            }
            {
                String lhsID;
                lhsID = this.getID();
                String rhsID;
                rhsID = that.getID();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "id", lhsID), LocatorUtils.property(thatLocator, "id", rhsID), lhsID, rhsID, (this.id!= null), (that.id!= null))) {
                    return false;
                }
            }
            {
                String lhsUSE;
                lhsUSE = this.getUSE();
                String rhsUSE;
                rhsUSE = that.getUSE();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "use", lhsUSE), LocatorUtils.property(thatLocator, "use", rhsUSE), lhsUSE, rhsUSE, (this.use!= null), (that.use!= null))) {
                    return false;
                }
            }
            return true;
        }

        public boolean equals(Object object) {
            final EqualsStrategy2 strategy = JAXBEqualsStrategy.INSTANCE;
            return equals(null, null, object, strategy);
        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *       &lt;sequence&gt;
         *         &lt;any processContents='lax' maxOccurs="unbounded"/&gt;
         *       &lt;/sequence&gt;
         *     &lt;/restriction&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "any"
        })
        public static class XmlData implements Equals2
        {

            @XmlAnyElement(lax = true)
            protected List<Object> any;

            /**
             * Gets the value of the any property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the any property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getAny().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link Element }
             * {@link Object }
             * 
             * 
             */
            public List<Object> getAny() {
                if (any == null) {
                    any = new ArrayList<Object>();
                }
                return this.any;
            }

            public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy2 strategy) {
                if ((object == null)||(this.getClass()!= object.getClass())) {
                    return false;
                }
                if (this == object) {
                    return true;
                }
                final FileType.FContent.XmlData that = ((FileType.FContent.XmlData) object);
                {
                    List<Object> lhsAny;
                    lhsAny = (((this.any!= null)&&(!this.any.isEmpty()))?this.getAny():null);
                    List<Object> rhsAny;
                    rhsAny = (((that.any!= null)&&(!that.any.isEmpty()))?that.getAny():null);
                    if (!strategy.equals(LocatorUtils.property(thisLocator, "any", lhsAny), LocatorUtils.property(thatLocator, "any", rhsAny), lhsAny, rhsAny, ((this.any!= null)&&(!this.any.isEmpty())), ((that.any!= null)&&(!that.any.isEmpty())))) {
                        return false;
                    }
                }
                return true;
            }

            public boolean equals(Object object) {
                final EqualsStrategy2 strategy = JAXBEqualsStrategy.INSTANCE;
                return equals(null, null, object, strategy);
            }

        }

    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;attGroup ref="{http://www.w3.org/1999/xlink}simpleLink"/&gt;
     *       &lt;attGroup ref="{http://www.loc.gov/METS/}LOCATION"/&gt;
     *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
     *       &lt;attribute name="USE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class FLocat implements Equals2
    {

        @XmlAttribute(name = "ID")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlID
        @XmlSchemaType(name = "ID")
        protected String id;
        @XmlAttribute(name = "USE")
        protected String use;
        @XmlAttribute(name = "type", namespace = "http://www.w3.org/1999/xlink")
        protected String type;
        @XmlAttribute(name = "href", namespace = "http://www.w3.org/1999/xlink")
        @XmlSchemaType(name = "anyURI")
        protected String href;
        @XmlAttribute(name = "role", namespace = "http://www.w3.org/1999/xlink")
        protected String role;
        @XmlAttribute(name = "arcrole", namespace = "http://www.w3.org/1999/xlink")
        protected String arcrole;
        @XmlAttribute(name = "title", namespace = "http://www.w3.org/1999/xlink")
        protected String title;
        @XmlAttribute(name = "show", namespace = "http://www.w3.org/1999/xlink")
        protected String show;
        @XmlAttribute(name = "actuate", namespace = "http://www.w3.org/1999/xlink")
        protected String actuate;
        @XmlAttribute(name = "LOCTYPE", required = true)
        protected String loctype;
        @XmlAttribute(name = "OTHERLOCTYPE")
        protected String otherloctype;

        /**
         * Ruft den Wert der id-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getID() {
            return id;
        }

        /**
         * Legt den Wert der id-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setID(String value) {
            this.id = value;
        }

        /**
         * Ruft den Wert der use-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getUSE() {
            return use;
        }

        /**
         * Legt den Wert der use-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setUSE(String value) {
            this.use = value;
        }

        /**
         * Ruft den Wert der type-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getType() {
            if (type == null) {
                return "simple";
            } else {
                return type;
            }
        }

        /**
         * Legt den Wert der type-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setType(String value) {
            this.type = value;
        }

        /**
         * Ruft den Wert der href-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getHref() {
            return href;
        }

        /**
         * Legt den Wert der href-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setHref(String value) {
            this.href = value;
        }

        /**
         * Ruft den Wert der role-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getRole() {
            return role;
        }

        /**
         * Legt den Wert der role-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setRole(String value) {
            this.role = value;
        }

        /**
         * Ruft den Wert der arcrole-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getArcrole() {
            return arcrole;
        }

        /**
         * Legt den Wert der arcrole-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setArcrole(String value) {
            this.arcrole = value;
        }

        /**
         * Ruft den Wert der title-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTitle() {
            return title;
        }

        /**
         * Legt den Wert der title-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTitle(String value) {
            this.title = value;
        }

        /**
         * Ruft den Wert der show-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getShow() {
            return show;
        }

        /**
         * Legt den Wert der show-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setShow(String value) {
            this.show = value;
        }

        /**
         * Ruft den Wert der actuate-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getActuate() {
            return actuate;
        }

        /**
         * Legt den Wert der actuate-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setActuate(String value) {
            this.actuate = value;
        }

        /**
         * Ruft den Wert der loctype-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getLOCTYPE() {
            return loctype;
        }

        /**
         * Legt den Wert der loctype-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setLOCTYPE(String value) {
            this.loctype = value;
        }

        /**
         * Ruft den Wert der otherloctype-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getOTHERLOCTYPE() {
            return otherloctype;
        }

        /**
         * Legt den Wert der otherloctype-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOTHERLOCTYPE(String value) {
            this.otherloctype = value;
        }

        public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy2 strategy) {
            if ((object == null)||(this.getClass()!= object.getClass())) {
                return false;
            }
            if (this == object) {
                return true;
            }
            final FileType.FLocat that = ((FileType.FLocat) object);
            {
                String lhsID;
                lhsID = this.getID();
                String rhsID;
                rhsID = that.getID();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "id", lhsID), LocatorUtils.property(thatLocator, "id", rhsID), lhsID, rhsID, (this.id!= null), (that.id!= null))) {
                    return false;
                }
            }
            {
                String lhsUSE;
                lhsUSE = this.getUSE();
                String rhsUSE;
                rhsUSE = that.getUSE();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "use", lhsUSE), LocatorUtils.property(thatLocator, "use", rhsUSE), lhsUSE, rhsUSE, (this.use!= null), (that.use!= null))) {
                    return false;
                }
            }
            {
                String lhsType;
                lhsType = this.getType();
                String rhsType;
                rhsType = that.getType();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "type", lhsType), LocatorUtils.property(thatLocator, "type", rhsType), lhsType, rhsType, (this.type!= null), (that.type!= null))) {
                    return false;
                }
            }
            {
                String lhsHref;
                lhsHref = this.getHref();
                String rhsHref;
                rhsHref = that.getHref();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "href", lhsHref), LocatorUtils.property(thatLocator, "href", rhsHref), lhsHref, rhsHref, (this.href!= null), (that.href!= null))) {
                    return false;
                }
            }
            {
                String lhsRole;
                lhsRole = this.getRole();
                String rhsRole;
                rhsRole = that.getRole();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "role", lhsRole), LocatorUtils.property(thatLocator, "role", rhsRole), lhsRole, rhsRole, (this.role!= null), (that.role!= null))) {
                    return false;
                }
            }
            {
                String lhsArcrole;
                lhsArcrole = this.getArcrole();
                String rhsArcrole;
                rhsArcrole = that.getArcrole();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "arcrole", lhsArcrole), LocatorUtils.property(thatLocator, "arcrole", rhsArcrole), lhsArcrole, rhsArcrole, (this.arcrole!= null), (that.arcrole!= null))) {
                    return false;
                }
            }
            {
                String lhsTitle;
                lhsTitle = this.getTitle();
                String rhsTitle;
                rhsTitle = that.getTitle();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "title", lhsTitle), LocatorUtils.property(thatLocator, "title", rhsTitle), lhsTitle, rhsTitle, (this.title!= null), (that.title!= null))) {
                    return false;
                }
            }
            {
                String lhsShow;
                lhsShow = this.getShow();
                String rhsShow;
                rhsShow = that.getShow();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "show", lhsShow), LocatorUtils.property(thatLocator, "show", rhsShow), lhsShow, rhsShow, (this.show!= null), (that.show!= null))) {
                    return false;
                }
            }
            {
                String lhsActuate;
                lhsActuate = this.getActuate();
                String rhsActuate;
                rhsActuate = that.getActuate();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "actuate", lhsActuate), LocatorUtils.property(thatLocator, "actuate", rhsActuate), lhsActuate, rhsActuate, (this.actuate!= null), (that.actuate!= null))) {
                    return false;
                }
            }
            {
                String lhsLOCTYPE;
                lhsLOCTYPE = this.getLOCTYPE();
                String rhsLOCTYPE;
                rhsLOCTYPE = that.getLOCTYPE();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "loctype", lhsLOCTYPE), LocatorUtils.property(thatLocator, "loctype", rhsLOCTYPE), lhsLOCTYPE, rhsLOCTYPE, (this.loctype!= null), (that.loctype!= null))) {
                    return false;
                }
            }
            {
                String lhsOTHERLOCTYPE;
                lhsOTHERLOCTYPE = this.getOTHERLOCTYPE();
                String rhsOTHERLOCTYPE;
                rhsOTHERLOCTYPE = that.getOTHERLOCTYPE();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "otherloctype", lhsOTHERLOCTYPE), LocatorUtils.property(thatLocator, "otherloctype", rhsOTHERLOCTYPE), lhsOTHERLOCTYPE, rhsOTHERLOCTYPE, (this.otherloctype!= null), (that.otherloctype!= null))) {
                    return false;
                }
            }
            return true;
        }

        public boolean equals(Object object) {
            final EqualsStrategy2 strategy = JAXBEqualsStrategy.INSTANCE;
            return equals(null, null, object, strategy);
        }

    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
     *       &lt;attribute name="streamType" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *       &lt;attribute name="OWNERID" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *       &lt;attribute name="ADMID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" /&gt;
     *       &lt;attribute name="DMDID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" /&gt;
     *       &lt;attribute name="BEGIN" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *       &lt;attribute name="END" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *       &lt;attribute name="BETYPE"&gt;
     *         &lt;simpleType&gt;
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *             &lt;enumeration value="BYTE"/&gt;
     *           &lt;/restriction&gt;
     *         &lt;/simpleType&gt;
     *       &lt;/attribute&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Stream implements Equals2
    {

        @XmlAttribute(name = "ID")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlID
        @XmlSchemaType(name = "ID")
        protected String id;
        @XmlAttribute(name = "streamType")
        protected String streamType;
        @XmlAttribute(name = "OWNERID")
        protected String ownerid;
        @XmlAttribute(name = "ADMID")
        @XmlIDREF
        @XmlSchemaType(name = "IDREFS")
        protected List<Object> admid;
        @XmlAttribute(name = "DMDID")
        @XmlIDREF
        @XmlSchemaType(name = "IDREFS")
        protected List<Object> dmdid;
        @XmlAttribute(name = "BEGIN")
        protected String begin;
        @XmlAttribute(name = "END")
        protected String end;
        @XmlAttribute(name = "BETYPE")
        protected String betype;

        /**
         * Ruft den Wert der id-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getID() {
            return id;
        }

        /**
         * Legt den Wert der id-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setID(String value) {
            this.id = value;
        }

        /**
         * Ruft den Wert der streamType-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getStreamType() {
            return streamType;
        }

        /**
         * Legt den Wert der streamType-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setStreamType(String value) {
            this.streamType = value;
        }

        /**
         * Ruft den Wert der ownerid-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getOWNERID() {
            return ownerid;
        }

        /**
         * Legt den Wert der ownerid-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOWNERID(String value) {
            this.ownerid = value;
        }

        /**
         * Gets the value of the admid property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the admid property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getADMID().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Object }
         * 
         * 
         */
        public List<Object> getADMID() {
            if (admid == null) {
                admid = new ArrayList<Object>();
            }
            return this.admid;
        }

        /**
         * Gets the value of the dmdid property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the dmdid property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getDMDID().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Object }
         * 
         * 
         */
        public List<Object> getDMDID() {
            if (dmdid == null) {
                dmdid = new ArrayList<Object>();
            }
            return this.dmdid;
        }

        /**
         * Ruft den Wert der begin-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getBEGIN() {
            return begin;
        }

        /**
         * Legt den Wert der begin-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setBEGIN(String value) {
            this.begin = value;
        }

        /**
         * Ruft den Wert der end-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getEND() {
            return end;
        }

        /**
         * Legt den Wert der end-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setEND(String value) {
            this.end = value;
        }

        /**
         * Ruft den Wert der betype-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getBETYPE() {
            return betype;
        }

        /**
         * Legt den Wert der betype-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setBETYPE(String value) {
            this.betype = value;
        }

        public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy2 strategy) {
            if ((object == null)||(this.getClass()!= object.getClass())) {
                return false;
            }
            if (this == object) {
                return true;
            }
            final FileType.Stream that = ((FileType.Stream) object);
            {
                String lhsID;
                lhsID = this.getID();
                String rhsID;
                rhsID = that.getID();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "id", lhsID), LocatorUtils.property(thatLocator, "id", rhsID), lhsID, rhsID, (this.id!= null), (that.id!= null))) {
                    return false;
                }
            }
            {
                String lhsStreamType;
                lhsStreamType = this.getStreamType();
                String rhsStreamType;
                rhsStreamType = that.getStreamType();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "streamType", lhsStreamType), LocatorUtils.property(thatLocator, "streamType", rhsStreamType), lhsStreamType, rhsStreamType, (this.streamType!= null), (that.streamType!= null))) {
                    return false;
                }
            }
            {
                String lhsOWNERID;
                lhsOWNERID = this.getOWNERID();
                String rhsOWNERID;
                rhsOWNERID = that.getOWNERID();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "ownerid", lhsOWNERID), LocatorUtils.property(thatLocator, "ownerid", rhsOWNERID), lhsOWNERID, rhsOWNERID, (this.ownerid!= null), (that.ownerid!= null))) {
                    return false;
                }
            }
            {
                List<Object> lhsADMID;
                lhsADMID = (((this.admid!= null)&&(!this.admid.isEmpty()))?this.getADMID():null);
                List<Object> rhsADMID;
                rhsADMID = (((that.admid!= null)&&(!that.admid.isEmpty()))?that.getADMID():null);
                if (!strategy.equals(LocatorUtils.property(thisLocator, "admid", lhsADMID), LocatorUtils.property(thatLocator, "admid", rhsADMID), lhsADMID, rhsADMID, ((this.admid!= null)&&(!this.admid.isEmpty())), ((that.admid!= null)&&(!that.admid.isEmpty())))) {
                    return false;
                }
            }
            {
                List<Object> lhsDMDID;
                lhsDMDID = (((this.dmdid!= null)&&(!this.dmdid.isEmpty()))?this.getDMDID():null);
                List<Object> rhsDMDID;
                rhsDMDID = (((that.dmdid!= null)&&(!that.dmdid.isEmpty()))?that.getDMDID():null);
                if (!strategy.equals(LocatorUtils.property(thisLocator, "dmdid", lhsDMDID), LocatorUtils.property(thatLocator, "dmdid", rhsDMDID), lhsDMDID, rhsDMDID, ((this.dmdid!= null)&&(!this.dmdid.isEmpty())), ((that.dmdid!= null)&&(!that.dmdid.isEmpty())))) {
                    return false;
                }
            }
            {
                String lhsBEGIN;
                lhsBEGIN = this.getBEGIN();
                String rhsBEGIN;
                rhsBEGIN = that.getBEGIN();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "begin", lhsBEGIN), LocatorUtils.property(thatLocator, "begin", rhsBEGIN), lhsBEGIN, rhsBEGIN, (this.begin!= null), (that.begin!= null))) {
                    return false;
                }
            }
            {
                String lhsEND;
                lhsEND = this.getEND();
                String rhsEND;
                rhsEND = that.getEND();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "end", lhsEND), LocatorUtils.property(thatLocator, "end", rhsEND), lhsEND, rhsEND, (this.end!= null), (that.end!= null))) {
                    return false;
                }
            }
            {
                String lhsBETYPE;
                lhsBETYPE = this.getBETYPE();
                String rhsBETYPE;
                rhsBETYPE = that.getBETYPE();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "betype", lhsBETYPE), LocatorUtils.property(thatLocator, "betype", rhsBETYPE), lhsBETYPE, rhsBETYPE, (this.betype!= null), (that.betype!= null))) {
                    return false;
                }
            }
            return true;
        }

        public boolean equals(Object object) {
            final EqualsStrategy2 strategy = JAXBEqualsStrategy.INSTANCE;
            return equals(null, null, object, strategy);
        }

    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
     *       &lt;attribute name="TRANSFORMTYPE" use="required"&gt;
     *         &lt;simpleType&gt;
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *             &lt;enumeration value="decompression"/&gt;
     *             &lt;enumeration value="decryption"/&gt;
     *           &lt;/restriction&gt;
     *         &lt;/simpleType&gt;
     *       &lt;/attribute&gt;
     *       &lt;attribute name="TRANSFORMALGORITHM" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *       &lt;attribute name="TRANSFORMKEY" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *       &lt;attribute name="TRANSFORMBEHAVIOR" type="{http://www.w3.org/2001/XMLSchema}IDREF" /&gt;
     *       &lt;attribute name="TRANSFORMORDER" use="required" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class TransformFile implements Equals2
    {

        @XmlAttribute(name = "ID")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlID
        @XmlSchemaType(name = "ID")
        protected String id;
        @XmlAttribute(name = "TRANSFORMTYPE", required = true)
        protected String transformtype;
        @XmlAttribute(name = "TRANSFORMALGORITHM", required = true)
        protected String transformalgorithm;
        @XmlAttribute(name = "TRANSFORMKEY")
        protected String transformkey;
        @XmlAttribute(name = "TRANSFORMBEHAVIOR")
        @XmlIDREF
        @XmlSchemaType(name = "IDREF")
        protected Object transformbehavior;
        @XmlAttribute(name = "TRANSFORMORDER", required = true)
        @XmlSchemaType(name = "positiveInteger")
        protected BigInteger transformorder;

        /**
         * Ruft den Wert der id-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getID() {
            return id;
        }

        /**
         * Legt den Wert der id-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setID(String value) {
            this.id = value;
        }

        /**
         * Ruft den Wert der transformtype-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTRANSFORMTYPE() {
            return transformtype;
        }

        /**
         * Legt den Wert der transformtype-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTRANSFORMTYPE(String value) {
            this.transformtype = value;
        }

        /**
         * Ruft den Wert der transformalgorithm-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTRANSFORMALGORITHM() {
            return transformalgorithm;
        }

        /**
         * Legt den Wert der transformalgorithm-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTRANSFORMALGORITHM(String value) {
            this.transformalgorithm = value;
        }

        /**
         * Ruft den Wert der transformkey-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTRANSFORMKEY() {
            return transformkey;
        }

        /**
         * Legt den Wert der transformkey-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTRANSFORMKEY(String value) {
            this.transformkey = value;
        }

        /**
         * Ruft den Wert der transformbehavior-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Object }
         *     
         */
        public Object getTRANSFORMBEHAVIOR() {
            return transformbehavior;
        }

        /**
         * Legt den Wert der transformbehavior-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Object }
         *     
         */
        public void setTRANSFORMBEHAVIOR(Object value) {
            this.transformbehavior = value;
        }

        /**
         * Ruft den Wert der transformorder-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getTRANSFORMORDER() {
            return transformorder;
        }

        /**
         * Legt den Wert der transformorder-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setTRANSFORMORDER(BigInteger value) {
            this.transformorder = value;
        }

        public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy2 strategy) {
            if ((object == null)||(this.getClass()!= object.getClass())) {
                return false;
            }
            if (this == object) {
                return true;
            }
            final FileType.TransformFile that = ((FileType.TransformFile) object);
            {
                String lhsID;
                lhsID = this.getID();
                String rhsID;
                rhsID = that.getID();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "id", lhsID), LocatorUtils.property(thatLocator, "id", rhsID), lhsID, rhsID, (this.id!= null), (that.id!= null))) {
                    return false;
                }
            }
            {
                String lhsTRANSFORMTYPE;
                lhsTRANSFORMTYPE = this.getTRANSFORMTYPE();
                String rhsTRANSFORMTYPE;
                rhsTRANSFORMTYPE = that.getTRANSFORMTYPE();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "transformtype", lhsTRANSFORMTYPE), LocatorUtils.property(thatLocator, "transformtype", rhsTRANSFORMTYPE), lhsTRANSFORMTYPE, rhsTRANSFORMTYPE, (this.transformtype!= null), (that.transformtype!= null))) {
                    return false;
                }
            }
            {
                String lhsTRANSFORMALGORITHM;
                lhsTRANSFORMALGORITHM = this.getTRANSFORMALGORITHM();
                String rhsTRANSFORMALGORITHM;
                rhsTRANSFORMALGORITHM = that.getTRANSFORMALGORITHM();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "transformalgorithm", lhsTRANSFORMALGORITHM), LocatorUtils.property(thatLocator, "transformalgorithm", rhsTRANSFORMALGORITHM), lhsTRANSFORMALGORITHM, rhsTRANSFORMALGORITHM, (this.transformalgorithm!= null), (that.transformalgorithm!= null))) {
                    return false;
                }
            }
            {
                String lhsTRANSFORMKEY;
                lhsTRANSFORMKEY = this.getTRANSFORMKEY();
                String rhsTRANSFORMKEY;
                rhsTRANSFORMKEY = that.getTRANSFORMKEY();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "transformkey", lhsTRANSFORMKEY), LocatorUtils.property(thatLocator, "transformkey", rhsTRANSFORMKEY), lhsTRANSFORMKEY, rhsTRANSFORMKEY, (this.transformkey!= null), (that.transformkey!= null))) {
                    return false;
                }
            }
            {
                Object lhsTRANSFORMBEHAVIOR;
                lhsTRANSFORMBEHAVIOR = this.getTRANSFORMBEHAVIOR();
                Object rhsTRANSFORMBEHAVIOR;
                rhsTRANSFORMBEHAVIOR = that.getTRANSFORMBEHAVIOR();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "transformbehavior", lhsTRANSFORMBEHAVIOR), LocatorUtils.property(thatLocator, "transformbehavior", rhsTRANSFORMBEHAVIOR), lhsTRANSFORMBEHAVIOR, rhsTRANSFORMBEHAVIOR, (this.transformbehavior!= null), (that.transformbehavior!= null))) {
                    return false;
                }
            }
            {
                BigInteger lhsTRANSFORMORDER;
                lhsTRANSFORMORDER = this.getTRANSFORMORDER();
                BigInteger rhsTRANSFORMORDER;
                rhsTRANSFORMORDER = that.getTRANSFORMORDER();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "transformorder", lhsTRANSFORMORDER), LocatorUtils.property(thatLocator, "transformorder", rhsTRANSFORMORDER), lhsTRANSFORMORDER, rhsTRANSFORMORDER, (this.transformorder!= null), (that.transformorder!= null))) {
                    return false;
                }
            }
            return true;
        }

        public boolean equals(Object object) {
            final EqualsStrategy2 strategy = JAXBEqualsStrategy.INSTANCE;
            return equals(null, null, object, strategy);
        }

    }

}
