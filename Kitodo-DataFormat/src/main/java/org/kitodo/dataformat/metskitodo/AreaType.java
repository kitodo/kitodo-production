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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import org.jvnet.jaxb2_commons.lang.Equals2;
import org.jvnet.jaxb2_commons.lang.EqualsStrategy2;
import org.jvnet.jaxb2_commons.lang.JAXBEqualsStrategy;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;
import org.jvnet.jaxb2_commons.locator.util.LocatorUtils;


/**
 * areaType: Complex Type for Area Linking
 * 				The area element provides for more sophisticated linking between a div element and content files representing that div, be they text, image, audio, or video files.  An area element can link a div to a point within a file, to a one-dimension segment of a file (e.g., text segment, image line, audio/video clip), or a two-dimensional section of a file 	(e.g, subsection of an image, or a subsection of the  video display of a video file.  The area element has no content; all information is recorded within its various attributes.
 * 			
 * 
 * <p>Java-Klasse für areaType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="areaType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attGroup ref="{http://www.loc.gov/METS/}ORDERLABELS"/&gt;
 *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *       &lt;attribute name="FILEID" use="required" type="{http://www.w3.org/2001/XMLSchema}IDREF" /&gt;
 *       &lt;attribute name="SHAPE"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="RECT"/&gt;
 *             &lt;enumeration value="CIRCLE"/&gt;
 *             &lt;enumeration value="POLY"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="COORDS" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="BEGIN" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="END" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="BETYPE"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="BYTE"/&gt;
 *             &lt;enumeration value="IDREF"/&gt;
 *             &lt;enumeration value="SMIL"/&gt;
 *             &lt;enumeration value="MIDI"/&gt;
 *             &lt;enumeration value="SMPTE-25"/&gt;
 *             &lt;enumeration value="SMPTE-24"/&gt;
 *             &lt;enumeration value="SMPTE-DF30"/&gt;
 *             &lt;enumeration value="SMPTE-NDF30"/&gt;
 *             &lt;enumeration value="SMPTE-DF29.97"/&gt;
 *             &lt;enumeration value="SMPTE-NDF29.97"/&gt;
 *             &lt;enumeration value="TIME"/&gt;
 *             &lt;enumeration value="TCF"/&gt;
 *             &lt;enumeration value="XPTR"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="EXTENT" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="EXTTYPE"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="BYTE"/&gt;
 *             &lt;enumeration value="SMIL"/&gt;
 *             &lt;enumeration value="MIDI"/&gt;
 *             &lt;enumeration value="SMPTE-25"/&gt;
 *             &lt;enumeration value="SMPTE-24"/&gt;
 *             &lt;enumeration value="SMPTE-DF30"/&gt;
 *             &lt;enumeration value="SMPTE-NDF30"/&gt;
 *             &lt;enumeration value="SMPTE-DF29.97"/&gt;
 *             &lt;enumeration value="SMPTE-NDF29.97"/&gt;
 *             &lt;enumeration value="TIME"/&gt;
 *             &lt;enumeration value="TCF"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="ADMID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" /&gt;
 *       &lt;attribute name="CONTENTIDS" type="{http://www.loc.gov/METS/}URIs" /&gt;
 *       &lt;anyAttribute processContents='lax' namespace='##other'/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "areaType")
public class AreaType implements Equals2
{

    @XmlAttribute(name = "ID")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "FILEID", required = true)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected Object fileid;
    @XmlAttribute(name = "SHAPE")
    protected String shape;
    @XmlAttribute(name = "COORDS")
    protected String coords;
    @XmlAttribute(name = "BEGIN")
    protected String begin;
    @XmlAttribute(name = "END")
    protected String end;
    @XmlAttribute(name = "BETYPE")
    protected String betype;
    @XmlAttribute(name = "EXTENT")
    protected String extent;
    @XmlAttribute(name = "EXTTYPE")
    protected String exttype;
    @XmlAttribute(name = "ADMID")
    @XmlIDREF
    @XmlSchemaType(name = "IDREFS")
    protected List<Object> admid;
    @XmlAttribute(name = "CONTENTIDS")
    protected List<String> contentids;
    @XmlAttribute(name = "ORDER")
    protected BigInteger order;
    @XmlAttribute(name = "ORDERLABEL")
    protected String orderlabel;
    @XmlAttribute(name = "LABEL")
    protected String label;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

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
     * Ruft den Wert der fileid-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getFILEID() {
        return fileid;
    }

    /**
     * Legt den Wert der fileid-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setFILEID(Object value) {
        this.fileid = value;
    }

    /**
     * Ruft den Wert der shape-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSHAPE() {
        return shape;
    }

    /**
     * Legt den Wert der shape-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSHAPE(String value) {
        this.shape = value;
    }

    /**
     * Ruft den Wert der coords-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCOORDS() {
        return coords;
    }

    /**
     * Legt den Wert der coords-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCOORDS(String value) {
        this.coords = value;
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
     * Ruft den Wert der extent-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEXTENT() {
        return extent;
    }

    /**
     * Legt den Wert der extent-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEXTENT(String value) {
        this.extent = value;
    }

    /**
     * Ruft den Wert der exttype-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEXTTYPE() {
        return exttype;
    }

    /**
     * Legt den Wert der exttype-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEXTTYPE(String value) {
        this.exttype = value;
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
     * Gets the value of the contentids property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the contentids property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCONTENTIDS().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getCONTENTIDS() {
        if (contentids == null) {
            contentids = new ArrayList<String>();
        }
        return this.contentids;
    }

    /**
     * Ruft den Wert der order-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getORDER() {
        return order;
    }

    /**
     * Legt den Wert der order-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setORDER(BigInteger value) {
        this.order = value;
    }

    /**
     * Ruft den Wert der orderlabel-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getORDERLABEL() {
        return orderlabel;
    }

    /**
     * Legt den Wert der orderlabel-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setORDERLABEL(String value) {
        this.orderlabel = value;
    }

    /**
     * Ruft den Wert der label-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLABEL() {
        return label;
    }

    /**
     * Legt den Wert der label-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLABEL(String value) {
        this.label = value;
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
        final AreaType that = ((AreaType) object);
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
            Object lhsFILEID;
            lhsFILEID = this.getFILEID();
            Object rhsFILEID;
            rhsFILEID = that.getFILEID();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "fileid", lhsFILEID), LocatorUtils.property(thatLocator, "fileid", rhsFILEID), lhsFILEID, rhsFILEID, (this.fileid!= null), (that.fileid!= null))) {
                return false;
            }
        }
        {
            String lhsSHAPE;
            lhsSHAPE = this.getSHAPE();
            String rhsSHAPE;
            rhsSHAPE = that.getSHAPE();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "shape", lhsSHAPE), LocatorUtils.property(thatLocator, "shape", rhsSHAPE), lhsSHAPE, rhsSHAPE, (this.shape!= null), (that.shape!= null))) {
                return false;
            }
        }
        {
            String lhsCOORDS;
            lhsCOORDS = this.getCOORDS();
            String rhsCOORDS;
            rhsCOORDS = that.getCOORDS();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "coords", lhsCOORDS), LocatorUtils.property(thatLocator, "coords", rhsCOORDS), lhsCOORDS, rhsCOORDS, (this.coords!= null), (that.coords!= null))) {
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
            String lhsEXTENT;
            lhsEXTENT = this.getEXTENT();
            String rhsEXTENT;
            rhsEXTENT = that.getEXTENT();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "extent", lhsEXTENT), LocatorUtils.property(thatLocator, "extent", rhsEXTENT), lhsEXTENT, rhsEXTENT, (this.extent!= null), (that.extent!= null))) {
                return false;
            }
        }
        {
            String lhsEXTTYPE;
            lhsEXTTYPE = this.getEXTTYPE();
            String rhsEXTTYPE;
            rhsEXTTYPE = that.getEXTTYPE();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "exttype", lhsEXTTYPE), LocatorUtils.property(thatLocator, "exttype", rhsEXTTYPE), lhsEXTTYPE, rhsEXTTYPE, (this.exttype!= null), (that.exttype!= null))) {
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
            List<String> lhsCONTENTIDS;
            lhsCONTENTIDS = (((this.contentids!= null)&&(!this.contentids.isEmpty()))?this.getCONTENTIDS():null);
            List<String> rhsCONTENTIDS;
            rhsCONTENTIDS = (((that.contentids!= null)&&(!that.contentids.isEmpty()))?that.getCONTENTIDS():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "contentids", lhsCONTENTIDS), LocatorUtils.property(thatLocator, "contentids", rhsCONTENTIDS), lhsCONTENTIDS, rhsCONTENTIDS, ((this.contentids!= null)&&(!this.contentids.isEmpty())), ((that.contentids!= null)&&(!that.contentids.isEmpty())))) {
                return false;
            }
        }
        {
            BigInteger lhsORDER;
            lhsORDER = this.getORDER();
            BigInteger rhsORDER;
            rhsORDER = that.getORDER();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "order", lhsORDER), LocatorUtils.property(thatLocator, "order", rhsORDER), lhsORDER, rhsORDER, (this.order!= null), (that.order!= null))) {
                return false;
            }
        }
        {
            String lhsORDERLABEL;
            lhsORDERLABEL = this.getORDERLABEL();
            String rhsORDERLABEL;
            rhsORDERLABEL = that.getORDERLABEL();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "orderlabel", lhsORDERLABEL), LocatorUtils.property(thatLocator, "orderlabel", rhsORDERLABEL), lhsORDERLABEL, rhsORDERLABEL, (this.orderlabel!= null), (that.orderlabel!= null))) {
                return false;
            }
        }
        {
            String lhsLABEL;
            lhsLABEL = this.getLABEL();
            String rhsLABEL;
            rhsLABEL = that.getLABEL();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "label", lhsLABEL), LocatorUtils.property(thatLocator, "label", rhsLABEL), lhsLABEL, rhsLABEL, (this.label!= null), (that.label!= null))) {
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
