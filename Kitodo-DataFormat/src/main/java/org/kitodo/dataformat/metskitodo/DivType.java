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
 * divType: Complex Type for Divisions
 * 					The METS standard represents a document structurally as a series of nested div elements, that is, as a hierarchy (e.g., a book, which is composed of chapters, which are composed of subchapters, which are composed of text).  Every div node in the structural map hierarchy may be connected (via subsidiary mptr or fptr elements) to content files which represent that div's portion of the whole document.
 * 
 * SPECIAL NOTE REGARDING DIV ATTRIBUTE VALUES:
 * to clarify the differences between the ORDER, ORDERLABEL, and LABEL attributes for the <div> element, imagine a text with 10 roman numbered pages followed by 10 arabic numbered pages. Page iii would have an ORDER of "3", an ORDERLABEL of "iii" and a LABEL of "Page iii", while page 3 would have an ORDER of "13", an ORDERLABEL of "3" and a LABEL of "Page 3".
 * 			
 * 
 * <p>Java-Klasse für divType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="divType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="mptr" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attGroup ref="{http://www.loc.gov/METS/}LOCATION"/&gt;
 *                 &lt;attGroup ref="{http://www.w3.org/1999/xlink}simpleLink"/&gt;
 *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *                 &lt;attribute name="CONTENTIDS" type="{http://www.loc.gov/METS/}URIs" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="fptr" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;choice&gt;
 *                   &lt;element name="par" type="{http://www.loc.gov/METS/}parType" minOccurs="0"/&gt;
 *                   &lt;element name="seq" type="{http://www.loc.gov/METS/}seqType" minOccurs="0"/&gt;
 *                   &lt;element name="area" type="{http://www.loc.gov/METS/}areaType" minOccurs="0"/&gt;
 *                 &lt;/choice&gt;
 *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *                 &lt;attribute name="FILEID" type="{http://www.w3.org/2001/XMLSchema}IDREF" /&gt;
 *                 &lt;attribute name="CONTENTIDS" type="{http://www.loc.gov/METS/}URIs" /&gt;
 *                 &lt;anyAttribute processContents='lax' namespace='##other'/&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="div" type="{http://www.loc.gov/METS/}divType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attGroup ref="{http://www.loc.gov/METS/}ORDERLABELS"/&gt;
 *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *       &lt;attribute name="DMDID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" /&gt;
 *       &lt;attribute name="ADMID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" /&gt;
 *       &lt;attribute name="TYPE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="CONTENTIDS" type="{http://www.loc.gov/METS/}URIs" /&gt;
 *       &lt;attribute ref="{http://www.w3.org/1999/xlink}label"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "divType", propOrder = {
    "mptr",
    "fptr",
    "div"
})
public class DivType implements Equals2
{

    protected List<DivType.Mptr> mptr;
    protected List<DivType.Fptr> fptr;
    protected List<DivType> div;
    @XmlAttribute(name = "ID")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "DMDID")
    @XmlIDREF
    @XmlSchemaType(name = "IDREFS")
    protected List<Object> dmdid;
    @XmlAttribute(name = "ADMID")
    @XmlIDREF
    @XmlSchemaType(name = "IDREFS")
    protected List<Object> admid;
    @XmlAttribute(name = "TYPE")
    protected String type;
    @XmlAttribute(name = "CONTENTIDS")
    protected List<String> contentids;
    @XmlAttribute(name = "label", namespace = "http://www.w3.org/1999/xlink")
    protected String xlinkLabel;
    @XmlAttribute(name = "ORDER")
    protected BigInteger order;
    @XmlAttribute(name = "ORDERLABEL")
    protected String orderlabel;
    @XmlAttribute(name = "LABEL")
    protected String label;

    /**
     * Gets the value of the mptr property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mptr property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMptr().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DivType.Mptr }
     * 
     * 
     */
    public List<DivType.Mptr> getMptr() {
        if (mptr == null) {
            mptr = new ArrayList<DivType.Mptr>();
        }
        return this.mptr;
    }

    /**
     * Gets the value of the fptr property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fptr property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFptr().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DivType.Fptr }
     * 
     * 
     */
    public List<DivType.Fptr> getFptr() {
        if (fptr == null) {
            fptr = new ArrayList<DivType.Fptr>();
        }
        return this.fptr;
    }

    /**
     * Gets the value of the div property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the div property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDiv().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DivType }
     * 
     * 
     */
    public List<DivType> getDiv() {
        if (div == null) {
            div = new ArrayList<DivType>();
        }
        return this.div;
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
     * xlink:label - an xlink label to be referred to by an smLink element
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXlinkLabel() {
        return xlinkLabel;
    }

    /**
     * Legt den Wert der xlinkLabel-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXlinkLabel(String value) {
        this.xlinkLabel = value;
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

    public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy2 strategy) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final DivType that = ((DivType) object);
        {
            List<DivType.Mptr> lhsMptr;
            lhsMptr = (((this.mptr!= null)&&(!this.mptr.isEmpty()))?this.getMptr():null);
            List<DivType.Mptr> rhsMptr;
            rhsMptr = (((that.mptr!= null)&&(!that.mptr.isEmpty()))?that.getMptr():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "mptr", lhsMptr), LocatorUtils.property(thatLocator, "mptr", rhsMptr), lhsMptr, rhsMptr, ((this.mptr!= null)&&(!this.mptr.isEmpty())), ((that.mptr!= null)&&(!that.mptr.isEmpty())))) {
                return false;
            }
        }
        {
            List<DivType.Fptr> lhsFptr;
            lhsFptr = (((this.fptr!= null)&&(!this.fptr.isEmpty()))?this.getFptr():null);
            List<DivType.Fptr> rhsFptr;
            rhsFptr = (((that.fptr!= null)&&(!that.fptr.isEmpty()))?that.getFptr():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "fptr", lhsFptr), LocatorUtils.property(thatLocator, "fptr", rhsFptr), lhsFptr, rhsFptr, ((this.fptr!= null)&&(!this.fptr.isEmpty())), ((that.fptr!= null)&&(!that.fptr.isEmpty())))) {
                return false;
            }
        }
        {
            List<DivType> lhsDiv;
            lhsDiv = (((this.div!= null)&&(!this.div.isEmpty()))?this.getDiv():null);
            List<DivType> rhsDiv;
            rhsDiv = (((that.div!= null)&&(!that.div.isEmpty()))?that.getDiv():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "div", lhsDiv), LocatorUtils.property(thatLocator, "div", rhsDiv), lhsDiv, rhsDiv, ((this.div!= null)&&(!this.div.isEmpty())), ((that.div!= null)&&(!that.div.isEmpty())))) {
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
            List<Object> lhsDMDID;
            lhsDMDID = (((this.dmdid!= null)&&(!this.dmdid.isEmpty()))?this.getDMDID():null);
            List<Object> rhsDMDID;
            rhsDMDID = (((that.dmdid!= null)&&(!that.dmdid.isEmpty()))?that.getDMDID():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "dmdid", lhsDMDID), LocatorUtils.property(thatLocator, "dmdid", rhsDMDID), lhsDMDID, rhsDMDID, ((this.dmdid!= null)&&(!this.dmdid.isEmpty())), ((that.dmdid!= null)&&(!that.dmdid.isEmpty())))) {
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
            String lhsTYPE;
            lhsTYPE = this.getTYPE();
            String rhsTYPE;
            rhsTYPE = that.getTYPE();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "type", lhsTYPE), LocatorUtils.property(thatLocator, "type", rhsTYPE), lhsTYPE, rhsTYPE, (this.type!= null), (that.type!= null))) {
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
            String lhsXlinkLabel;
            lhsXlinkLabel = this.getXlinkLabel();
            String rhsXlinkLabel;
            rhsXlinkLabel = that.getXlinkLabel();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "xlinkLabel", lhsXlinkLabel), LocatorUtils.property(thatLocator, "xlinkLabel", rhsXlinkLabel), lhsXlinkLabel, rhsXlinkLabel, (this.xlinkLabel!= null), (that.xlinkLabel!= null))) {
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
     *         &lt;element name="par" type="{http://www.loc.gov/METS/}parType" minOccurs="0"/&gt;
     *         &lt;element name="seq" type="{http://www.loc.gov/METS/}seqType" minOccurs="0"/&gt;
     *         &lt;element name="area" type="{http://www.loc.gov/METS/}areaType" minOccurs="0"/&gt;
     *       &lt;/choice&gt;
     *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
     *       &lt;attribute name="FILEID" type="{http://www.w3.org/2001/XMLSchema}IDREF" /&gt;
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
    @XmlType(name = "", propOrder = {
        "par",
        "seq",
        "area"
    })
    public static class Fptr implements Equals2
    {

        protected ParType par;
        protected SeqType seq;
        protected AreaType area;
        @XmlAttribute(name = "ID")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlID
        @XmlSchemaType(name = "ID")
        protected String id;
        @XmlAttribute(name = "FILEID")
        @XmlIDREF
        @XmlSchemaType(name = "IDREF")
        protected Object fileid;
        @XmlAttribute(name = "CONTENTIDS")
        protected List<String> contentids;
        @XmlAnyAttribute
        private Map<QName, String> otherAttributes = new HashMap<QName, String>();

        /**
         * Ruft den Wert der par-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link ParType }
         *     
         */
        public ParType getPar() {
            return par;
        }

        /**
         * Legt den Wert der par-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link ParType }
         *     
         */
        public void setPar(ParType value) {
            this.par = value;
        }

        /**
         * Ruft den Wert der seq-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link SeqType }
         *     
         */
        public SeqType getSeq() {
            return seq;
        }

        /**
         * Legt den Wert der seq-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link SeqType }
         *     
         */
        public void setSeq(SeqType value) {
            this.seq = value;
        }

        /**
         * Ruft den Wert der area-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link AreaType }
         *     
         */
        public AreaType getArea() {
            return area;
        }

        /**
         * Legt den Wert der area-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link AreaType }
         *     
         */
        public void setArea(AreaType value) {
            this.area = value;
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
            final DivType.Fptr that = ((DivType.Fptr) object);
            {
                ParType lhsPar;
                lhsPar = this.getPar();
                ParType rhsPar;
                rhsPar = that.getPar();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "par", lhsPar), LocatorUtils.property(thatLocator, "par", rhsPar), lhsPar, rhsPar, (this.par!= null), (that.par!= null))) {
                    return false;
                }
            }
            {
                SeqType lhsSeq;
                lhsSeq = this.getSeq();
                SeqType rhsSeq;
                rhsSeq = that.getSeq();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "seq", lhsSeq), LocatorUtils.property(thatLocator, "seq", rhsSeq), lhsSeq, rhsSeq, (this.seq!= null), (that.seq!= null))) {
                    return false;
                }
            }
            {
                AreaType lhsArea;
                lhsArea = this.getArea();
                AreaType rhsArea;
                rhsArea = that.getArea();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "area", lhsArea), LocatorUtils.property(thatLocator, "area", rhsArea), lhsArea, rhsArea, (this.area!= null), (that.area!= null))) {
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
                Object lhsFILEID;
                lhsFILEID = this.getFILEID();
                Object rhsFILEID;
                rhsFILEID = that.getFILEID();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "fileid", lhsFILEID), LocatorUtils.property(thatLocator, "fileid", rhsFILEID), lhsFILEID, rhsFILEID, (this.fileid!= null), (that.fileid!= null))) {
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
     *       &lt;attGroup ref="{http://www.loc.gov/METS/}LOCATION"/&gt;
     *       &lt;attGroup ref="{http://www.w3.org/1999/xlink}simpleLink"/&gt;
     *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
     *       &lt;attribute name="CONTENTIDS" type="{http://www.loc.gov/METS/}URIs" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Mptr implements Equals2
    {

        @XmlAttribute(name = "ID")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlID
        @XmlSchemaType(name = "ID")
        protected String id;
        @XmlAttribute(name = "CONTENTIDS")
        protected List<String> contentids;
        @XmlAttribute(name = "LOCTYPE", required = true)
        protected String loctype;
        @XmlAttribute(name = "OTHERLOCTYPE")
        protected String otherloctype;
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

        public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy2 strategy) {
            if ((object == null)||(this.getClass()!= object.getClass())) {
                return false;
            }
            if (this == object) {
                return true;
            }
            final DivType.Mptr that = ((DivType.Mptr) object);
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
                List<String> lhsCONTENTIDS;
                lhsCONTENTIDS = (((this.contentids!= null)&&(!this.contentids.isEmpty()))?this.getCONTENTIDS():null);
                List<String> rhsCONTENTIDS;
                rhsCONTENTIDS = (((that.contentids!= null)&&(!that.contentids.isEmpty()))?that.getCONTENTIDS():null);
                if (!strategy.equals(LocatorUtils.property(thisLocator, "contentids", lhsCONTENTIDS), LocatorUtils.property(thatLocator, "contentids", rhsCONTENTIDS), lhsCONTENTIDS, rhsCONTENTIDS, ((this.contentids!= null)&&(!this.contentids.isEmpty())), ((that.contentids!= null)&&(!that.contentids.isEmpty())))) {
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
            return true;
        }

        public boolean equals(Object object) {
            final EqualsStrategy2 strategy = JAXBEqualsStrategy.INSTANCE;
            return equals(null, null, object, strategy);
        }

    }

}
