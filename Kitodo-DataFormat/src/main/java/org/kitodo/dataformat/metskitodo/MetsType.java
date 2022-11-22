//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2022.11.22 um 01:29:20 PM CET 
//


package org.kitodo.dataformat.metskitodo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import org.jvnet.jaxb2_commons.lang.Equals2;
import org.jvnet.jaxb2_commons.lang.EqualsStrategy2;
import org.jvnet.jaxb2_commons.lang.JAXBEqualsStrategy;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;
import org.jvnet.jaxb2_commons.locator.util.LocatorUtils;


/**
 * metsType: Complex Type for METS Sections
 * 			A METS document consists of seven possible subsidiary sections: metsHdr (METS document header), dmdSec (descriptive metadata section), amdSec (administrative metadata section), fileGrp (file inventory group), structLink (structural map linking), structMap (structural map) and behaviorSec (behaviors section).
 * 			
 * 
 * <p>Java-Klasse für metsType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="metsType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="metsHdr" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="agent" maxOccurs="unbounded" minOccurs="0"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                             &lt;element name="note" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                           &lt;/sequence&gt;
 *                           &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *                           &lt;attribute name="ROLE" use="required"&gt;
 *                             &lt;simpleType&gt;
 *                               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                                 &lt;enumeration value="CREATOR"/&gt;
 *                                 &lt;enumeration value="EDITOR"/&gt;
 *                                 &lt;enumeration value="ARCHIVIST"/&gt;
 *                                 &lt;enumeration value="PRESERVATION"/&gt;
 *                                 &lt;enumeration value="DISSEMINATOR"/&gt;
 *                                 &lt;enumeration value="CUSTODIAN"/&gt;
 *                                 &lt;enumeration value="IPOWNER"/&gt;
 *                                 &lt;enumeration value="OTHER"/&gt;
 *                               &lt;/restriction&gt;
 *                             &lt;/simpleType&gt;
 *                           &lt;/attribute&gt;
 *                           &lt;attribute name="OTHERROLE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                           &lt;attribute name="TYPE"&gt;
 *                             &lt;simpleType&gt;
 *                               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                                 &lt;enumeration value="INDIVIDUAL"/&gt;
 *                                 &lt;enumeration value="ORGANIZATION"/&gt;
 *                                 &lt;enumeration value="OTHER"/&gt;
 *                               &lt;/restriction&gt;
 *                             &lt;/simpleType&gt;
 *                           &lt;/attribute&gt;
 *                           &lt;attribute name="OTHERTYPE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="altRecordID" maxOccurs="unbounded" minOccurs="0"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;simpleContent&gt;
 *                         &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
 *                           &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *                           &lt;attribute name="TYPE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                         &lt;/extension&gt;
 *                       &lt;/simpleContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="metsDocumentID" minOccurs="0"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;simpleContent&gt;
 *                         &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
 *                           &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *                           &lt;attribute name="TYPE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                         &lt;/extension&gt;
 *                       &lt;/simpleContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/sequence&gt;
 *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *                 &lt;attribute name="ADMID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" /&gt;
 *                 &lt;attribute name="CREATEDATE" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *                 &lt;attribute name="LASTMODDATE" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *                 &lt;attribute name="RECORDSTATUS" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;anyAttribute processContents='lax' namespace='##other'/&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="dmdSec" type="{http://www.loc.gov/METS/}mdSecType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="amdSec" type="{http://www.loc.gov/METS/}amdSecType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="fileSec" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="fileGrp" maxOccurs="unbounded"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;extension base="{http://www.loc.gov/METS/}fileGrpType"&gt;
 *                           &lt;anyAttribute processContents='lax' namespace='##other'/&gt;
 *                         &lt;/extension&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/sequence&gt;
 *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *                 &lt;anyAttribute processContents='lax' namespace='##other'/&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="structMap" type="{http://www.loc.gov/METS/}structMapType" maxOccurs="unbounded"/&gt;
 *         &lt;element name="structLink" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;extension base="{http://www.loc.gov/METS/}structLinkType"&gt;
 *                 &lt;anyAttribute processContents='lax' namespace='##other'/&gt;
 *               &lt;/extension&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="behaviorSec" type="{http://www.loc.gov/METS/}behaviorSecType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *       &lt;attribute name="OBJID" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="LABEL" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="TYPE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="PROFILE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;anyAttribute processContents='lax' namespace='##other'/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "metsType", propOrder = {
    "metsHdr",
    "dmdSec",
    "amdSec",
    "fileSec",
    "structMap",
    "structLink",
    "behaviorSec"
})
@XmlSeeAlso({
    Mets.class
})
public class MetsType implements Equals2
{

    protected MetsType.MetsHdr metsHdr;
    protected List<MdSecType> dmdSec;
    protected List<AmdSecType> amdSec;
    protected MetsType.FileSec fileSec;
    @XmlElement(required = true)
    protected List<StructMapType> structMap;
    protected MetsType.StructLink structLink;
    protected List<BehaviorSecType> behaviorSec;
    @XmlAttribute(name = "ID")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "OBJID")
    protected String objid;
    @XmlAttribute(name = "LABEL")
    protected String label;
    @XmlAttribute(name = "TYPE")
    protected String type;
    @XmlAttribute(name = "PROFILE")
    protected String profile;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Ruft den Wert der metsHdr-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link MetsType.MetsHdr }
     *     
     */
    public MetsType.MetsHdr getMetsHdr() {
        return metsHdr;
    }

    /**
     * Legt den Wert der metsHdr-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link MetsType.MetsHdr }
     *     
     */
    public void setMetsHdr(MetsType.MetsHdr value) {
        this.metsHdr = value;
    }

    /**
     * Gets the value of the dmdSec property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dmdSec property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDmdSec().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MdSecType }
     * 
     * 
     */
    public List<MdSecType> getDmdSec() {
        if (dmdSec == null) {
            dmdSec = new ArrayList<MdSecType>();
        }
        return this.dmdSec;
    }

    /**
     * Gets the value of the amdSec property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the amdSec property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAmdSec().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AmdSecType }
     * 
     * 
     */
    public List<AmdSecType> getAmdSec() {
        if (amdSec == null) {
            amdSec = new ArrayList<AmdSecType>();
        }
        return this.amdSec;
    }

    /**
     * Ruft den Wert der fileSec-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link MetsType.FileSec }
     *     
     */
    public MetsType.FileSec getFileSec() {
        return fileSec;
    }

    /**
     * Legt den Wert der fileSec-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link MetsType.FileSec }
     *     
     */
    public void setFileSec(MetsType.FileSec value) {
        this.fileSec = value;
    }

    /**
     * Gets the value of the structMap property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the structMap property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStructMap().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link StructMapType }
     * 
     * 
     */
    public List<StructMapType> getStructMap() {
        if (structMap == null) {
            structMap = new ArrayList<StructMapType>();
        }
        return this.structMap;
    }

    /**
     * Ruft den Wert der structLink-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link MetsType.StructLink }
     *     
     */
    public MetsType.StructLink getStructLink() {
        return structLink;
    }

    /**
     * Legt den Wert der structLink-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link MetsType.StructLink }
     *     
     */
    public void setStructLink(MetsType.StructLink value) {
        this.structLink = value;
    }

    /**
     * Gets the value of the behaviorSec property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the behaviorSec property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBehaviorSec().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BehaviorSecType }
     * 
     * 
     */
    public List<BehaviorSecType> getBehaviorSec() {
        if (behaviorSec == null) {
            behaviorSec = new ArrayList<BehaviorSecType>();
        }
        return this.behaviorSec;
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
     * Ruft den Wert der objid-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOBJID() {
        return objid;
    }

    /**
     * Legt den Wert der objid-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOBJID(String value) {
        this.objid = value;
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
     * Ruft den Wert der type-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTYPE() {
        return type;
    }

    /**
     * Legt den Wert der type-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTYPE(String value) {
        this.type = value;
    }

    /**
     * Ruft den Wert der profile-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPROFILE() {
        return profile;
    }

    /**
     * Legt den Wert der profile-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPROFILE(String value) {
        this.profile = value;
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
        final MetsType that = ((MetsType) object);
        {
            MetsType.MetsHdr lhsMetsHdr;
            lhsMetsHdr = this.getMetsHdr();
            MetsType.MetsHdr rhsMetsHdr;
            rhsMetsHdr = that.getMetsHdr();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "metsHdr", lhsMetsHdr), LocatorUtils.property(thatLocator, "metsHdr", rhsMetsHdr), lhsMetsHdr, rhsMetsHdr, (this.metsHdr!= null), (that.metsHdr!= null))) {
                return false;
            }
        }
        {
            List<MdSecType> lhsDmdSec;
            lhsDmdSec = (((this.dmdSec!= null)&&(!this.dmdSec.isEmpty()))?this.getDmdSec():null);
            List<MdSecType> rhsDmdSec;
            rhsDmdSec = (((that.dmdSec!= null)&&(!that.dmdSec.isEmpty()))?that.getDmdSec():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "dmdSec", lhsDmdSec), LocatorUtils.property(thatLocator, "dmdSec", rhsDmdSec), lhsDmdSec, rhsDmdSec, ((this.dmdSec!= null)&&(!this.dmdSec.isEmpty())), ((that.dmdSec!= null)&&(!that.dmdSec.isEmpty())))) {
                return false;
            }
        }
        {
            List<AmdSecType> lhsAmdSec;
            lhsAmdSec = (((this.amdSec!= null)&&(!this.amdSec.isEmpty()))?this.getAmdSec():null);
            List<AmdSecType> rhsAmdSec;
            rhsAmdSec = (((that.amdSec!= null)&&(!that.amdSec.isEmpty()))?that.getAmdSec():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "amdSec", lhsAmdSec), LocatorUtils.property(thatLocator, "amdSec", rhsAmdSec), lhsAmdSec, rhsAmdSec, ((this.amdSec!= null)&&(!this.amdSec.isEmpty())), ((that.amdSec!= null)&&(!that.amdSec.isEmpty())))) {
                return false;
            }
        }
        {
            MetsType.FileSec lhsFileSec;
            lhsFileSec = this.getFileSec();
            MetsType.FileSec rhsFileSec;
            rhsFileSec = that.getFileSec();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "fileSec", lhsFileSec), LocatorUtils.property(thatLocator, "fileSec", rhsFileSec), lhsFileSec, rhsFileSec, (this.fileSec!= null), (that.fileSec!= null))) {
                return false;
            }
        }
        {
            List<StructMapType> lhsStructMap;
            lhsStructMap = (((this.structMap!= null)&&(!this.structMap.isEmpty()))?this.getStructMap():null);
            List<StructMapType> rhsStructMap;
            rhsStructMap = (((that.structMap!= null)&&(!that.structMap.isEmpty()))?that.getStructMap():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "structMap", lhsStructMap), LocatorUtils.property(thatLocator, "structMap", rhsStructMap), lhsStructMap, rhsStructMap, ((this.structMap!= null)&&(!this.structMap.isEmpty())), ((that.structMap!= null)&&(!that.structMap.isEmpty())))) {
                return false;
            }
        }
        {
            MetsType.StructLink lhsStructLink;
            lhsStructLink = this.getStructLink();
            MetsType.StructLink rhsStructLink;
            rhsStructLink = that.getStructLink();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "structLink", lhsStructLink), LocatorUtils.property(thatLocator, "structLink", rhsStructLink), lhsStructLink, rhsStructLink, (this.structLink!= null), (that.structLink!= null))) {
                return false;
            }
        }
        {
            List<BehaviorSecType> lhsBehaviorSec;
            lhsBehaviorSec = (((this.behaviorSec!= null)&&(!this.behaviorSec.isEmpty()))?this.getBehaviorSec():null);
            List<BehaviorSecType> rhsBehaviorSec;
            rhsBehaviorSec = (((that.behaviorSec!= null)&&(!that.behaviorSec.isEmpty()))?that.getBehaviorSec():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "behaviorSec", lhsBehaviorSec), LocatorUtils.property(thatLocator, "behaviorSec", rhsBehaviorSec), lhsBehaviorSec, rhsBehaviorSec, ((this.behaviorSec!= null)&&(!this.behaviorSec.isEmpty())), ((that.behaviorSec!= null)&&(!that.behaviorSec.isEmpty())))) {
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
            String lhsOBJID;
            lhsOBJID = this.getOBJID();
            String rhsOBJID;
            rhsOBJID = that.getOBJID();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "objid", lhsOBJID), LocatorUtils.property(thatLocator, "objid", rhsOBJID), lhsOBJID, rhsOBJID, (this.objid!= null), (that.objid!= null))) {
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
        {
            String lhsTYPE;
            lhsTYPE = this.getTYPE();
            String rhsTYPE;
            rhsTYPE = that.getTYPE();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "type", lhsTYPE), LocatorUtils.property(thatLocator, "type", rhsTYPE), lhsTYPE, rhsTYPE, (this.type!= null), (that.type!= null))) {
                return false;
            }
        }
        {
            String lhsPROFILE;
            lhsPROFILE = this.getPROFILE();
            String rhsPROFILE;
            rhsPROFILE = that.getPROFILE();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "profile", lhsPROFILE), LocatorUtils.property(thatLocator, "profile", rhsPROFILE), lhsPROFILE, rhsPROFILE, (this.profile!= null), (that.profile!= null))) {
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
     *         &lt;element name="fileGrp" maxOccurs="unbounded"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;extension base="{http://www.loc.gov/METS/}fileGrpType"&gt;
     *                 &lt;anyAttribute processContents='lax' namespace='##other'/&gt;
     *               &lt;/extension&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *       &lt;/sequence&gt;
     *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
     *       &lt;anyAttribute processContents='lax' namespace='##other'/&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "fileGrp"
    })
    public static class FileSec implements Equals2
    {

        @XmlElement(required = true)
        protected List<MetsType.FileSec.FileGrp> fileGrp;
        @XmlAttribute(name = "ID")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlID
        @XmlSchemaType(name = "ID")
        protected String id;
        @XmlAnyAttribute
        private Map<QName, String> otherAttributes = new HashMap<QName, String>();

        /**
         * Gets the value of the fileGrp property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the fileGrp property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getFileGrp().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link MetsType.FileSec.FileGrp }
         * 
         * 
         */
        public List<MetsType.FileSec.FileGrp> getFileGrp() {
            if (fileGrp == null) {
                fileGrp = new ArrayList<MetsType.FileSec.FileGrp>();
            }
            return this.fileGrp;
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
            final MetsType.FileSec that = ((MetsType.FileSec) object);
            {
                List<MetsType.FileSec.FileGrp> lhsFileGrp;
                lhsFileGrp = (((this.fileGrp!= null)&&(!this.fileGrp.isEmpty()))?this.getFileGrp():null);
                List<MetsType.FileSec.FileGrp> rhsFileGrp;
                rhsFileGrp = (((that.fileGrp!= null)&&(!that.fileGrp.isEmpty()))?that.getFileGrp():null);
                if (!strategy.equals(LocatorUtils.property(thisLocator, "fileGrp", lhsFileGrp), LocatorUtils.property(thatLocator, "fileGrp", rhsFileGrp), lhsFileGrp, rhsFileGrp, ((this.fileGrp!= null)&&(!this.fileGrp.isEmpty())), ((that.fileGrp!= null)&&(!that.fileGrp.isEmpty())))) {
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
         *     &lt;extension base="{http://www.loc.gov/METS/}fileGrpType"&gt;
         *       &lt;anyAttribute processContents='lax' namespace='##other'/&gt;
         *     &lt;/extension&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class FileGrp
            extends FileGrpType
            implements Equals2
        {


            public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy2 strategy) {
                if ((object == null)||(this.getClass()!= object.getClass())) {
                    return false;
                }
                if (this == object) {
                    return true;
                }
                if (!super.equals(thisLocator, thatLocator, object, strategy)) {
                    return false;
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
     *       &lt;sequence&gt;
     *         &lt;element name="agent" maxOccurs="unbounded" minOccurs="0"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *                   &lt;element name="note" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
     *                 &lt;/sequence&gt;
     *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
     *                 &lt;attribute name="ROLE" use="required"&gt;
     *                   &lt;simpleType&gt;
     *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *                       &lt;enumeration value="CREATOR"/&gt;
     *                       &lt;enumeration value="EDITOR"/&gt;
     *                       &lt;enumeration value="ARCHIVIST"/&gt;
     *                       &lt;enumeration value="PRESERVATION"/&gt;
     *                       &lt;enumeration value="DISSEMINATOR"/&gt;
     *                       &lt;enumeration value="CUSTODIAN"/&gt;
     *                       &lt;enumeration value="IPOWNER"/&gt;
     *                       &lt;enumeration value="OTHER"/&gt;
     *                     &lt;/restriction&gt;
     *                   &lt;/simpleType&gt;
     *                 &lt;/attribute&gt;
     *                 &lt;attribute name="OTHERROLE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *                 &lt;attribute name="TYPE"&gt;
     *                   &lt;simpleType&gt;
     *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *                       &lt;enumeration value="INDIVIDUAL"/&gt;
     *                       &lt;enumeration value="ORGANIZATION"/&gt;
     *                       &lt;enumeration value="OTHER"/&gt;
     *                     &lt;/restriction&gt;
     *                   &lt;/simpleType&gt;
     *                 &lt;/attribute&gt;
     *                 &lt;attribute name="OTHERTYPE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="altRecordID" maxOccurs="unbounded" minOccurs="0"&gt;
     *           &lt;complexType&gt;
     *             &lt;simpleContent&gt;
     *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
     *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
     *                 &lt;attribute name="TYPE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *               &lt;/extension&gt;
     *             &lt;/simpleContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="metsDocumentID" minOccurs="0"&gt;
     *           &lt;complexType&gt;
     *             &lt;simpleContent&gt;
     *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
     *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
     *                 &lt;attribute name="TYPE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *               &lt;/extension&gt;
     *             &lt;/simpleContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *       &lt;/sequence&gt;
     *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
     *       &lt;attribute name="ADMID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" /&gt;
     *       &lt;attribute name="CREATEDATE" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
     *       &lt;attribute name="LASTMODDATE" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
     *       &lt;attribute name="RECORDSTATUS" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *       &lt;anyAttribute processContents='lax' namespace='##other'/&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "agent",
        "altRecordID",
        "metsDocumentID"
    })
    public static class MetsHdr implements Equals2
    {

        protected List<MetsType.MetsHdr.Agent> agent;
        protected List<MetsType.MetsHdr.AltRecordID> altRecordID;
        protected MetsType.MetsHdr.MetsDocumentID metsDocumentID;
        @XmlAttribute(name = "ID")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlID
        @XmlSchemaType(name = "ID")
        protected String id;
        @XmlAttribute(name = "ADMID")
        @XmlIDREF
        @XmlSchemaType(name = "IDREFS")
        protected List<Object> admid;
        @XmlAttribute(name = "CREATEDATE")
        @XmlSchemaType(name = "dateTime")
        protected XMLGregorianCalendar createdate;
        @XmlAttribute(name = "LASTMODDATE")
        @XmlSchemaType(name = "dateTime")
        protected XMLGregorianCalendar lastmoddate;
        @XmlAttribute(name = "RECORDSTATUS")
        protected String recordstatus;
        @XmlAnyAttribute
        private Map<QName, String> otherAttributes = new HashMap<QName, String>();

        /**
         * Gets the value of the agent property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the agent property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAgent().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link MetsType.MetsHdr.Agent }
         * 
         * 
         */
        public List<MetsType.MetsHdr.Agent> getAgent() {
            if (agent == null) {
                agent = new ArrayList<MetsType.MetsHdr.Agent>();
            }
            return this.agent;
        }

        /**
         * Gets the value of the altRecordID property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the altRecordID property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAltRecordID().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link MetsType.MetsHdr.AltRecordID }
         * 
         * 
         */
        public List<MetsType.MetsHdr.AltRecordID> getAltRecordID() {
            if (altRecordID == null) {
                altRecordID = new ArrayList<MetsType.MetsHdr.AltRecordID>();
            }
            return this.altRecordID;
        }

        /**
         * Ruft den Wert der metsDocumentID-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link MetsType.MetsHdr.MetsDocumentID }
         *     
         */
        public MetsType.MetsHdr.MetsDocumentID getMetsDocumentID() {
            return metsDocumentID;
        }

        /**
         * Legt den Wert der metsDocumentID-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link MetsType.MetsHdr.MetsDocumentID }
         *     
         */
        public void setMetsDocumentID(MetsType.MetsHdr.MetsDocumentID value) {
            this.metsDocumentID = value;
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
         * Ruft den Wert der createdate-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getCREATEDATE() {
            return createdate;
        }

        /**
         * Legt den Wert der createdate-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setCREATEDATE(XMLGregorianCalendar value) {
            this.createdate = value;
        }

        /**
         * Ruft den Wert der lastmoddate-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getLASTMODDATE() {
            return lastmoddate;
        }

        /**
         * Legt den Wert der lastmoddate-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setLASTMODDATE(XMLGregorianCalendar value) {
            this.lastmoddate = value;
        }

        /**
         * Ruft den Wert der recordstatus-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getRECORDSTATUS() {
            return recordstatus;
        }

        /**
         * Legt den Wert der recordstatus-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setRECORDSTATUS(String value) {
            this.recordstatus = value;
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
            final MetsType.MetsHdr that = ((MetsType.MetsHdr) object);
            {
                List<MetsType.MetsHdr.Agent> lhsAgent;
                lhsAgent = (((this.agent!= null)&&(!this.agent.isEmpty()))?this.getAgent():null);
                List<MetsType.MetsHdr.Agent> rhsAgent;
                rhsAgent = (((that.agent!= null)&&(!that.agent.isEmpty()))?that.getAgent():null);
                if (!strategy.equals(LocatorUtils.property(thisLocator, "agent", lhsAgent), LocatorUtils.property(thatLocator, "agent", rhsAgent), lhsAgent, rhsAgent, ((this.agent!= null)&&(!this.agent.isEmpty())), ((that.agent!= null)&&(!that.agent.isEmpty())))) {
                    return false;
                }
            }
            {
                List<MetsType.MetsHdr.AltRecordID> lhsAltRecordID;
                lhsAltRecordID = (((this.altRecordID!= null)&&(!this.altRecordID.isEmpty()))?this.getAltRecordID():null);
                List<MetsType.MetsHdr.AltRecordID> rhsAltRecordID;
                rhsAltRecordID = (((that.altRecordID!= null)&&(!that.altRecordID.isEmpty()))?that.getAltRecordID():null);
                if (!strategy.equals(LocatorUtils.property(thisLocator, "altRecordID", lhsAltRecordID), LocatorUtils.property(thatLocator, "altRecordID", rhsAltRecordID), lhsAltRecordID, rhsAltRecordID, ((this.altRecordID!= null)&&(!this.altRecordID.isEmpty())), ((that.altRecordID!= null)&&(!that.altRecordID.isEmpty())))) {
                    return false;
                }
            }
            {
                MetsType.MetsHdr.MetsDocumentID lhsMetsDocumentID;
                lhsMetsDocumentID = this.getMetsDocumentID();
                MetsType.MetsHdr.MetsDocumentID rhsMetsDocumentID;
                rhsMetsDocumentID = that.getMetsDocumentID();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "metsDocumentID", lhsMetsDocumentID), LocatorUtils.property(thatLocator, "metsDocumentID", rhsMetsDocumentID), lhsMetsDocumentID, rhsMetsDocumentID, (this.metsDocumentID!= null), (that.metsDocumentID!= null))) {
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
                List<Object> lhsADMID;
                lhsADMID = (((this.admid!= null)&&(!this.admid.isEmpty()))?this.getADMID():null);
                List<Object> rhsADMID;
                rhsADMID = (((that.admid!= null)&&(!that.admid.isEmpty()))?that.getADMID():null);
                if (!strategy.equals(LocatorUtils.property(thisLocator, "admid", lhsADMID), LocatorUtils.property(thatLocator, "admid", rhsADMID), lhsADMID, rhsADMID, ((this.admid!= null)&&(!this.admid.isEmpty())), ((that.admid!= null)&&(!that.admid.isEmpty())))) {
                    return false;
                }
            }
            {
                XMLGregorianCalendar lhsCREATEDATE;
                lhsCREATEDATE = this.getCREATEDATE();
                XMLGregorianCalendar rhsCREATEDATE;
                rhsCREATEDATE = that.getCREATEDATE();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "createdate", lhsCREATEDATE), LocatorUtils.property(thatLocator, "createdate", rhsCREATEDATE), lhsCREATEDATE, rhsCREATEDATE, (this.createdate!= null), (that.createdate!= null))) {
                    return false;
                }
            }
            {
                XMLGregorianCalendar lhsLASTMODDATE;
                lhsLASTMODDATE = this.getLASTMODDATE();
                XMLGregorianCalendar rhsLASTMODDATE;
                rhsLASTMODDATE = that.getLASTMODDATE();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "lastmoddate", lhsLASTMODDATE), LocatorUtils.property(thatLocator, "lastmoddate", rhsLASTMODDATE), lhsLASTMODDATE, rhsLASTMODDATE, (this.lastmoddate!= null), (that.lastmoddate!= null))) {
                    return false;
                }
            }
            {
                String lhsRECORDSTATUS;
                lhsRECORDSTATUS = this.getRECORDSTATUS();
                String rhsRECORDSTATUS;
                rhsRECORDSTATUS = that.getRECORDSTATUS();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "recordstatus", lhsRECORDSTATUS), LocatorUtils.property(thatLocator, "recordstatus", rhsRECORDSTATUS), lhsRECORDSTATUS, rhsRECORDSTATUS, (this.recordstatus!= null), (that.recordstatus!= null))) {
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
         *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
         *         &lt;element name="note" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
         *       &lt;/sequence&gt;
         *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
         *       &lt;attribute name="ROLE" use="required"&gt;
         *         &lt;simpleType&gt;
         *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
         *             &lt;enumeration value="CREATOR"/&gt;
         *             &lt;enumeration value="EDITOR"/&gt;
         *             &lt;enumeration value="ARCHIVIST"/&gt;
         *             &lt;enumeration value="PRESERVATION"/&gt;
         *             &lt;enumeration value="DISSEMINATOR"/&gt;
         *             &lt;enumeration value="CUSTODIAN"/&gt;
         *             &lt;enumeration value="IPOWNER"/&gt;
         *             &lt;enumeration value="OTHER"/&gt;
         *           &lt;/restriction&gt;
         *         &lt;/simpleType&gt;
         *       &lt;/attribute&gt;
         *       &lt;attribute name="OTHERROLE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *       &lt;attribute name="TYPE"&gt;
         *         &lt;simpleType&gt;
         *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
         *             &lt;enumeration value="INDIVIDUAL"/&gt;
         *             &lt;enumeration value="ORGANIZATION"/&gt;
         *             &lt;enumeration value="OTHER"/&gt;
         *           &lt;/restriction&gt;
         *         &lt;/simpleType&gt;
         *       &lt;/attribute&gt;
         *       &lt;attribute name="OTHERTYPE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *     &lt;/restriction&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "name",
            "note"
        })
        public static class Agent implements Equals2
        {

            @XmlElement(required = true)
            protected String name;
            protected List<String> note;
            @XmlAttribute(name = "ID")
            @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
            @XmlID
            @XmlSchemaType(name = "ID")
            protected String id;
            @XmlAttribute(name = "ROLE", required = true)
            protected String role;
            @XmlAttribute(name = "OTHERROLE")
            protected String otherrole;
            @XmlAttribute(name = "TYPE")
            protected String type;
            @XmlAttribute(name = "OTHERTYPE")
            protected String othertype;

            /**
             * Ruft den Wert der name-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getName() {
                return name;
            }

            /**
             * Legt den Wert der name-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setName(String value) {
                this.name = value;
            }

            /**
             * Gets the value of the note property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the note property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getNote().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link String }
             * 
             * 
             */
            public List<String> getNote() {
                if (note == null) {
                    note = new ArrayList<String>();
                }
                return this.note;
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
             * Ruft den Wert der role-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getROLE() {
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
            public void setROLE(String value) {
                this.role = value;
            }

            /**
             * Ruft den Wert der otherrole-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getOTHERROLE() {
                return otherrole;
            }

            /**
             * Legt den Wert der otherrole-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setOTHERROLE(String value) {
                this.otherrole = value;
            }

            /**
             * Ruft den Wert der type-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getTYPE() {
                return type;
            }

            /**
             * Legt den Wert der type-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setTYPE(String value) {
                this.type = value;
            }

            /**
             * Ruft den Wert der othertype-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getOTHERTYPE() {
                return othertype;
            }

            /**
             * Legt den Wert der othertype-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setOTHERTYPE(String value) {
                this.othertype = value;
            }

            public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy2 strategy) {
                if ((object == null)||(this.getClass()!= object.getClass())) {
                    return false;
                }
                if (this == object) {
                    return true;
                }
                final MetsType.MetsHdr.Agent that = ((MetsType.MetsHdr.Agent) object);
                {
                    String lhsName;
                    lhsName = this.getName();
                    String rhsName;
                    rhsName = that.getName();
                    if (!strategy.equals(LocatorUtils.property(thisLocator, "name", lhsName), LocatorUtils.property(thatLocator, "name", rhsName), lhsName, rhsName, (this.name!= null), (that.name!= null))) {
                        return false;
                    }
                }
                {
                    List<String> lhsNote;
                    lhsNote = (((this.note!= null)&&(!this.note.isEmpty()))?this.getNote():null);
                    List<String> rhsNote;
                    rhsNote = (((that.note!= null)&&(!that.note.isEmpty()))?that.getNote():null);
                    if (!strategy.equals(LocatorUtils.property(thisLocator, "note", lhsNote), LocatorUtils.property(thatLocator, "note", rhsNote), lhsNote, rhsNote, ((this.note!= null)&&(!this.note.isEmpty())), ((that.note!= null)&&(!that.note.isEmpty())))) {
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
                    String lhsROLE;
                    lhsROLE = this.getROLE();
                    String rhsROLE;
                    rhsROLE = that.getROLE();
                    if (!strategy.equals(LocatorUtils.property(thisLocator, "role", lhsROLE), LocatorUtils.property(thatLocator, "role", rhsROLE), lhsROLE, rhsROLE, (this.role!= null), (that.role!= null))) {
                        return false;
                    }
                }
                {
                    String lhsOTHERROLE;
                    lhsOTHERROLE = this.getOTHERROLE();
                    String rhsOTHERROLE;
                    rhsOTHERROLE = that.getOTHERROLE();
                    if (!strategy.equals(LocatorUtils.property(thisLocator, "otherrole", lhsOTHERROLE), LocatorUtils.property(thatLocator, "otherrole", rhsOTHERROLE), lhsOTHERROLE, rhsOTHERROLE, (this.otherrole!= null), (that.otherrole!= null))) {
                        return false;
                    }
                }
                {
                    String lhsTYPE;
                    lhsTYPE = this.getTYPE();
                    String rhsTYPE;
                    rhsTYPE = that.getTYPE();
                    if (!strategy.equals(LocatorUtils.property(thisLocator, "type", lhsTYPE), LocatorUtils.property(thatLocator, "type", rhsTYPE), lhsTYPE, rhsTYPE, (this.type!= null), (that.type!= null))) {
                        return false;
                    }
                }
                {
                    String lhsOTHERTYPE;
                    lhsOTHERTYPE = this.getOTHERTYPE();
                    String rhsOTHERTYPE;
                    rhsOTHERTYPE = that.getOTHERTYPE();
                    if (!strategy.equals(LocatorUtils.property(thisLocator, "othertype", lhsOTHERTYPE), LocatorUtils.property(thatLocator, "othertype", rhsOTHERTYPE), lhsOTHERTYPE, rhsOTHERTYPE, (this.othertype!= null), (that.othertype!= null))) {
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
         *   &lt;simpleContent&gt;
         *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
         *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
         *       &lt;attribute name="TYPE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *     &lt;/extension&gt;
         *   &lt;/simpleContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "value"
        })
        public static class AltRecordID implements Equals2
        {

            @XmlValue
            protected String value;
            @XmlAttribute(name = "ID")
            @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
            @XmlID
            @XmlSchemaType(name = "ID")
            protected String id;
            @XmlAttribute(name = "TYPE")
            protected String type;

            /**
             * Ruft den Wert der value-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getValue() {
                return value;
            }

            /**
             * Legt den Wert der value-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setValue(String value) {
                this.value = value;
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
             * Ruft den Wert der type-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getTYPE() {
                return type;
            }

            /**
             * Legt den Wert der type-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setTYPE(String value) {
                this.type = value;
            }

            public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy2 strategy) {
                if ((object == null)||(this.getClass()!= object.getClass())) {
                    return false;
                }
                if (this == object) {
                    return true;
                }
                final MetsType.MetsHdr.AltRecordID that = ((MetsType.MetsHdr.AltRecordID) object);
                {
                    String lhsValue;
                    lhsValue = this.getValue();
                    String rhsValue;
                    rhsValue = that.getValue();
                    if (!strategy.equals(LocatorUtils.property(thisLocator, "value", lhsValue), LocatorUtils.property(thatLocator, "value", rhsValue), lhsValue, rhsValue, (this.value!= null), (that.value!= null))) {
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
                    String lhsTYPE;
                    lhsTYPE = this.getTYPE();
                    String rhsTYPE;
                    rhsTYPE = that.getTYPE();
                    if (!strategy.equals(LocatorUtils.property(thisLocator, "type", lhsTYPE), LocatorUtils.property(thatLocator, "type", rhsTYPE), lhsTYPE, rhsTYPE, (this.type!= null), (that.type!= null))) {
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
         *   &lt;simpleContent&gt;
         *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
         *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
         *       &lt;attribute name="TYPE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *     &lt;/extension&gt;
         *   &lt;/simpleContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "value"
        })
        public static class MetsDocumentID implements Equals2
        {

            @XmlValue
            protected String value;
            @XmlAttribute(name = "ID")
            @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
            @XmlID
            @XmlSchemaType(name = "ID")
            protected String id;
            @XmlAttribute(name = "TYPE")
            protected String type;

            /**
             * Ruft den Wert der value-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getValue() {
                return value;
            }

            /**
             * Legt den Wert der value-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setValue(String value) {
                this.value = value;
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
             * Ruft den Wert der type-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getTYPE() {
                return type;
            }

            /**
             * Legt den Wert der type-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setTYPE(String value) {
                this.type = value;
            }

            public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy2 strategy) {
                if ((object == null)||(this.getClass()!= object.getClass())) {
                    return false;
                }
                if (this == object) {
                    return true;
                }
                final MetsType.MetsHdr.MetsDocumentID that = ((MetsType.MetsHdr.MetsDocumentID) object);
                {
                    String lhsValue;
                    lhsValue = this.getValue();
                    String rhsValue;
                    rhsValue = that.getValue();
                    if (!strategy.equals(LocatorUtils.property(thisLocator, "value", lhsValue), LocatorUtils.property(thatLocator, "value", rhsValue), lhsValue, rhsValue, (this.value!= null), (that.value!= null))) {
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
                    String lhsTYPE;
                    lhsTYPE = this.getTYPE();
                    String rhsTYPE;
                    rhsTYPE = that.getTYPE();
                    if (!strategy.equals(LocatorUtils.property(thisLocator, "type", lhsTYPE), LocatorUtils.property(thatLocator, "type", rhsTYPE), lhsTYPE, rhsTYPE, (this.type!= null), (that.type!= null))) {
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
     *     &lt;extension base="{http://www.loc.gov/METS/}structLinkType"&gt;
     *       &lt;anyAttribute processContents='lax' namespace='##other'/&gt;
     *     &lt;/extension&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class StructLink
        extends StructLinkType
        implements Equals2
    {


        public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy2 strategy) {
            if ((object == null)||(this.getClass()!= object.getClass())) {
                return false;
            }
            if (this == object) {
                return true;
            }
            if (!super.equals(thisLocator, thatLocator, object, strategy)) {
                return false;
            }
            return true;
        }

        public boolean equals(Object object) {
            final EqualsStrategy2 strategy = JAXBEqualsStrategy.INSTANCE;
            return equals(null, null, object, strategy);
        }

    }

}
