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
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
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
 * mdSecType: Complex Type for Metadata Sections
 * 			A generic framework for pointing to/including metadata within a METS document, a la Warwick Framework.
 * 			
 * 
 * <p>Java-Klasse für mdSecType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="mdSecType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;all&gt;
 *         &lt;element name="mdRef" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attGroup ref="{http://www.w3.org/1999/xlink}simpleLink"/&gt;
 *                 &lt;attGroup ref="{http://www.loc.gov/METS/}FILECORE"/&gt;
 *                 &lt;attGroup ref="{http://www.loc.gov/METS/}LOCATION"/&gt;
 *                 &lt;attGroup ref="{http://www.loc.gov/METS/}METADATA"/&gt;
 *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *                 &lt;attribute name="LABEL" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;attribute name="XPTR" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="mdWrap" minOccurs="0"&gt;
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
 *                 &lt;attGroup ref="{http://www.loc.gov/METS/}FILECORE"/&gt;
 *                 &lt;attGroup ref="{http://www.loc.gov/METS/}METADATA"/&gt;
 *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *                 &lt;attribute name="LABEL" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/all&gt;
 *       &lt;attribute name="ID" use="required" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *       &lt;attribute name="GROUPID" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="ADMID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" /&gt;
 *       &lt;attribute name="CREATED" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="STATUS" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;anyAttribute processContents='lax' namespace='##other'/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mdSecType", propOrder = {

})
public class MdSecType implements Equals2
{

    protected MdSecType.MdRef mdRef;
    protected MdSecType.MdWrap mdWrap;
    @XmlAttribute(name = "ID", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "GROUPID")
    protected String groupid;
    @XmlAttribute(name = "ADMID")
    @XmlIDREF
    @XmlSchemaType(name = "IDREFS")
    protected List<Object> admid;
    @XmlAttribute(name = "CREATED")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar created;
    @XmlAttribute(name = "STATUS")
    protected String status;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Ruft den Wert der mdRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link MdSecType.MdRef }
     *     
     */
    public MdSecType.MdRef getMdRef() {
        return mdRef;
    }

    /**
     * Legt den Wert der mdRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link MdSecType.MdRef }
     *     
     */
    public void setMdRef(MdSecType.MdRef value) {
        this.mdRef = value;
    }

    /**
     * Ruft den Wert der mdWrap-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link MdSecType.MdWrap }
     *     
     */
    public MdSecType.MdWrap getMdWrap() {
        return mdWrap;
    }

    /**
     * Legt den Wert der mdWrap-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link MdSecType.MdWrap }
     *     
     */
    public void setMdWrap(MdSecType.MdWrap value) {
        this.mdWrap = value;
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
     * Ruft den Wert der status-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSTATUS() {
        return status;
    }

    /**
     * Legt den Wert der status-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSTATUS(String value) {
        this.status = value;
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
        final MdSecType that = ((MdSecType) object);
        {
            MdSecType.MdRef lhsMdRef;
            lhsMdRef = this.getMdRef();
            MdSecType.MdRef rhsMdRef;
            rhsMdRef = that.getMdRef();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "mdRef", lhsMdRef), LocatorUtils.property(thatLocator, "mdRef", rhsMdRef), lhsMdRef, rhsMdRef, (this.mdRef!= null), (that.mdRef!= null))) {
                return false;
            }
        }
        {
            MdSecType.MdWrap lhsMdWrap;
            lhsMdWrap = this.getMdWrap();
            MdSecType.MdWrap rhsMdWrap;
            rhsMdWrap = that.getMdWrap();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "mdWrap", lhsMdWrap), LocatorUtils.property(thatLocator, "mdWrap", rhsMdWrap), lhsMdWrap, rhsMdWrap, (this.mdWrap!= null), (that.mdWrap!= null))) {
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
            String lhsGROUPID;
            lhsGROUPID = this.getGROUPID();
            String rhsGROUPID;
            rhsGROUPID = that.getGROUPID();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "groupid", lhsGROUPID), LocatorUtils.property(thatLocator, "groupid", rhsGROUPID), lhsGROUPID, rhsGROUPID, (this.groupid!= null), (that.groupid!= null))) {
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
            XMLGregorianCalendar lhsCREATED;
            lhsCREATED = this.getCREATED();
            XMLGregorianCalendar rhsCREATED;
            rhsCREATED = that.getCREATED();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "created", lhsCREATED), LocatorUtils.property(thatLocator, "created", rhsCREATED), lhsCREATED, rhsCREATED, (this.created!= null), (that.created!= null))) {
                return false;
            }
        }
        {
            String lhsSTATUS;
            lhsSTATUS = this.getSTATUS();
            String rhsSTATUS;
            rhsSTATUS = that.getSTATUS();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "status", lhsSTATUS), LocatorUtils.property(thatLocator, "status", rhsSTATUS), lhsSTATUS, rhsSTATUS, (this.status!= null), (that.status!= null))) {
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
     *       &lt;attGroup ref="{http://www.w3.org/1999/xlink}simpleLink"/&gt;
     *       &lt;attGroup ref="{http://www.loc.gov/METS/}FILECORE"/&gt;
     *       &lt;attGroup ref="{http://www.loc.gov/METS/}LOCATION"/&gt;
     *       &lt;attGroup ref="{http://www.loc.gov/METS/}METADATA"/&gt;
     *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
     *       &lt;attribute name="LABEL" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *       &lt;attribute name="XPTR" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class MdRef implements Equals2
    {

        @XmlAttribute(name = "ID")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlID
        @XmlSchemaType(name = "ID")
        protected String id;
        @XmlAttribute(name = "LABEL")
        protected String label;
        @XmlAttribute(name = "XPTR")
        protected String xptr;
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
        @XmlAttribute(name = "LOCTYPE", required = true)
        protected String loctype;
        @XmlAttribute(name = "OTHERLOCTYPE")
        protected String otherloctype;
        @XmlAttribute(name = "MDTYPE", required = true)
        protected String mdtype;
        @XmlAttribute(name = "OTHERMDTYPE")
        protected String othermdtype;
        @XmlAttribute(name = "MDTYPEVERSION")
        protected String mdtypeversion;

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
         * Ruft den Wert der xptr-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getXPTR() {
            return xptr;
        }

        /**
         * Legt den Wert der xptr-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setXPTR(String value) {
            this.xptr = value;
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

        /**
         * Ruft den Wert der mdtype-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMDTYPE() {
            return mdtype;
        }

        /**
         * Legt den Wert der mdtype-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMDTYPE(String value) {
            this.mdtype = value;
        }

        /**
         * Ruft den Wert der othermdtype-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getOTHERMDTYPE() {
            return othermdtype;
        }

        /**
         * Legt den Wert der othermdtype-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOTHERMDTYPE(String value) {
            this.othermdtype = value;
        }

        /**
         * Ruft den Wert der mdtypeversion-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMDTYPEVERSION() {
            return mdtypeversion;
        }

        /**
         * Legt den Wert der mdtypeversion-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMDTYPEVERSION(String value) {
            this.mdtypeversion = value;
        }

        public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy2 strategy) {
            if ((object == null)||(this.getClass()!= object.getClass())) {
                return false;
            }
            if (this == object) {
                return true;
            }
            final MdSecType.MdRef that = ((MdSecType.MdRef) object);
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
                String lhsLABEL;
                lhsLABEL = this.getLABEL();
                String rhsLABEL;
                rhsLABEL = that.getLABEL();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "label", lhsLABEL), LocatorUtils.property(thatLocator, "label", rhsLABEL), lhsLABEL, rhsLABEL, (this.label!= null), (that.label!= null))) {
                    return false;
                }
            }
            {
                String lhsXPTR;
                lhsXPTR = this.getXPTR();
                String rhsXPTR;
                rhsXPTR = that.getXPTR();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "xptr", lhsXPTR), LocatorUtils.property(thatLocator, "xptr", rhsXPTR), lhsXPTR, rhsXPTR, (this.xptr!= null), (that.xptr!= null))) {
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
            {
                String lhsMDTYPE;
                lhsMDTYPE = this.getMDTYPE();
                String rhsMDTYPE;
                rhsMDTYPE = that.getMDTYPE();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "mdtype", lhsMDTYPE), LocatorUtils.property(thatLocator, "mdtype", rhsMDTYPE), lhsMDTYPE, rhsMDTYPE, (this.mdtype!= null), (that.mdtype!= null))) {
                    return false;
                }
            }
            {
                String lhsOTHERMDTYPE;
                lhsOTHERMDTYPE = this.getOTHERMDTYPE();
                String rhsOTHERMDTYPE;
                rhsOTHERMDTYPE = that.getOTHERMDTYPE();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "othermdtype", lhsOTHERMDTYPE), LocatorUtils.property(thatLocator, "othermdtype", rhsOTHERMDTYPE), lhsOTHERMDTYPE, rhsOTHERMDTYPE, (this.othermdtype!= null), (that.othermdtype!= null))) {
                    return false;
                }
            }
            {
                String lhsMDTYPEVERSION;
                lhsMDTYPEVERSION = this.getMDTYPEVERSION();
                String rhsMDTYPEVERSION;
                rhsMDTYPEVERSION = that.getMDTYPEVERSION();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "mdtypeversion", lhsMDTYPEVERSION), LocatorUtils.property(thatLocator, "mdtypeversion", rhsMDTYPEVERSION), lhsMDTYPEVERSION, rhsMDTYPEVERSION, (this.mdtypeversion!= null), (that.mdtypeversion!= null))) {
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
     *       &lt;attGroup ref="{http://www.loc.gov/METS/}FILECORE"/&gt;
     *       &lt;attGroup ref="{http://www.loc.gov/METS/}METADATA"/&gt;
     *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
     *       &lt;attribute name="LABEL" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
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
    public static class MdWrap implements Equals2
    {

        protected byte[] binData;
        protected MdSecType.MdWrap.XmlData xmlData;
        @XmlAttribute(name = "ID")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlID
        @XmlSchemaType(name = "ID")
        protected String id;
        @XmlAttribute(name = "LABEL")
        protected String label;
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
        @XmlAttribute(name = "MDTYPE", required = true)
        protected String mdtype;
        @XmlAttribute(name = "OTHERMDTYPE")
        protected String othermdtype;
        @XmlAttribute(name = "MDTYPEVERSION")
        protected String mdtypeversion;

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
         *     {@link MdSecType.MdWrap.XmlData }
         *     
         */
        public MdSecType.MdWrap.XmlData getXmlData() {
            return xmlData;
        }

        /**
         * Legt den Wert der xmlData-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link MdSecType.MdWrap.XmlData }
         *     
         */
        public void setXmlData(MdSecType.MdWrap.XmlData value) {
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
         * Ruft den Wert der mdtype-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMDTYPE() {
            return mdtype;
        }

        /**
         * Legt den Wert der mdtype-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMDTYPE(String value) {
            this.mdtype = value;
        }

        /**
         * Ruft den Wert der othermdtype-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getOTHERMDTYPE() {
            return othermdtype;
        }

        /**
         * Legt den Wert der othermdtype-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOTHERMDTYPE(String value) {
            this.othermdtype = value;
        }

        /**
         * Ruft den Wert der mdtypeversion-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMDTYPEVERSION() {
            return mdtypeversion;
        }

        /**
         * Legt den Wert der mdtypeversion-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMDTYPEVERSION(String value) {
            this.mdtypeversion = value;
        }

        public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy2 strategy) {
            if ((object == null)||(this.getClass()!= object.getClass())) {
                return false;
            }
            if (this == object) {
                return true;
            }
            final MdSecType.MdWrap that = ((MdSecType.MdWrap) object);
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
                MdSecType.MdWrap.XmlData lhsXmlData;
                lhsXmlData = this.getXmlData();
                MdSecType.MdWrap.XmlData rhsXmlData;
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
                String lhsLABEL;
                lhsLABEL = this.getLABEL();
                String rhsLABEL;
                rhsLABEL = that.getLABEL();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "label", lhsLABEL), LocatorUtils.property(thatLocator, "label", rhsLABEL), lhsLABEL, rhsLABEL, (this.label!= null), (that.label!= null))) {
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
            {
                String lhsMDTYPE;
                lhsMDTYPE = this.getMDTYPE();
                String rhsMDTYPE;
                rhsMDTYPE = that.getMDTYPE();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "mdtype", lhsMDTYPE), LocatorUtils.property(thatLocator, "mdtype", rhsMDTYPE), lhsMDTYPE, rhsMDTYPE, (this.mdtype!= null), (that.mdtype!= null))) {
                    return false;
                }
            }
            {
                String lhsOTHERMDTYPE;
                lhsOTHERMDTYPE = this.getOTHERMDTYPE();
                String rhsOTHERMDTYPE;
                rhsOTHERMDTYPE = that.getOTHERMDTYPE();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "othermdtype", lhsOTHERMDTYPE), LocatorUtils.property(thatLocator, "othermdtype", rhsOTHERMDTYPE), lhsOTHERMDTYPE, rhsOTHERMDTYPE, (this.othermdtype!= null), (that.othermdtype!= null))) {
                    return false;
                }
            }
            {
                String lhsMDTYPEVERSION;
                lhsMDTYPEVERSION = this.getMDTYPEVERSION();
                String rhsMDTYPEVERSION;
                rhsMDTYPEVERSION = that.getMDTYPEVERSION();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "mdtypeversion", lhsMDTYPEVERSION), LocatorUtils.property(thatLocator, "mdtypeversion", rhsMDTYPEVERSION), lhsMDTYPEVERSION, rhsMDTYPEVERSION, (this.mdtypeversion!= null), (that.mdtypeversion!= null))) {
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
                final MdSecType.MdWrap.XmlData that = ((MdSecType.MdWrap.XmlData) object);
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

}
