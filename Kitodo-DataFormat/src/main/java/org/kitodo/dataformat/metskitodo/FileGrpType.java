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
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
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


/**
 * fileGrpType: Complex Type for File Groups
 * 				The file group is used to cluster all of the digital files composing a digital library object in a hierarchical arrangement (fileGrp is recursively defined to enable the creation of the hierarchy).  Any file group may contain zero or more file elements.  File elements in turn can contain one or more FLocat elements (a pointer to a file containing content for this object) and/or a FContent element (the contents of the file, in either XML or  Base64 encoding).
 * 				
 * 
 * <p>Java-Klasse für fileGrpType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="fileGrpType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="fileGrp" type="{http://www.loc.gov/METS/}fileGrpType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="file" type="{http://www.loc.gov/METS/}fileType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *       &lt;attribute name="VERSDATE" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="ADMID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" /&gt;
 *       &lt;attribute name="USE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;anyAttribute processContents='lax' namespace='##other'/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fileGrpType", propOrder = {
    "fileGrp",
    "file"
})
@XmlSeeAlso({
    org.kitodo.dataformat.metskitodo.MetsType.FileSec.FileGrp.class
})
public class FileGrpType implements Equals2
{

    protected List<FileGrpType> fileGrp;
    protected List<FileType> file;
    @XmlAttribute(name = "ID")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "VERSDATE")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar versdate;
    @XmlAttribute(name = "ADMID")
    @XmlIDREF
    @XmlSchemaType(name = "IDREFS")
    protected List<Object> admid;
    @XmlAttribute(name = "USE")
    protected String use;
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
     * {@link FileGrpType }
     * 
     * 
     */
    public List<FileGrpType> getFileGrp() {
        if (fileGrp == null) {
            fileGrp = new ArrayList<FileGrpType>();
        }
        return this.fileGrp;
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
     * Ruft den Wert der versdate-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getVERSDATE() {
        return versdate;
    }

    /**
     * Legt den Wert der versdate-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setVERSDATE(XMLGregorianCalendar value) {
        this.versdate = value;
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
        final FileGrpType that = ((FileGrpType) object);
        {
            List<FileGrpType> lhsFileGrp;
            lhsFileGrp = (((this.fileGrp!= null)&&(!this.fileGrp.isEmpty()))?this.getFileGrp():null);
            List<FileGrpType> rhsFileGrp;
            rhsFileGrp = (((that.fileGrp!= null)&&(!that.fileGrp.isEmpty()))?that.getFileGrp():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "fileGrp", lhsFileGrp), LocatorUtils.property(thatLocator, "fileGrp", rhsFileGrp), lhsFileGrp, rhsFileGrp, ((this.fileGrp!= null)&&(!this.fileGrp.isEmpty())), ((that.fileGrp!= null)&&(!that.fileGrp.isEmpty())))) {
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
            XMLGregorianCalendar lhsVERSDATE;
            lhsVERSDATE = this.getVERSDATE();
            XMLGregorianCalendar rhsVERSDATE;
            rhsVERSDATE = that.getVERSDATE();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "versdate", lhsVERSDATE), LocatorUtils.property(thatLocator, "versdate", rhsVERSDATE), lhsVERSDATE, rhsVERSDATE, (this.versdate!= null), (that.versdate!= null))) {
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

}
