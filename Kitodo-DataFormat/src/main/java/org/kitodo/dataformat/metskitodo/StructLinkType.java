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
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
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
 * structLinkType: Complex Type for Structural Map Linking
 * 				The Structural Map Linking section allows for the specification of hyperlinks between different components of a METS structure delineated in a structural map.  structLink contains a single, repeatable element, smLink.  Each smLink element indicates a hyperlink between two nodes in the structMap.  The structMap nodes recorded in smLink are identified using their XML ID attribute	values.
 * 			
 * 
 * <p>Java-Klasse für structLinkType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="structLinkType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="smLink"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *                 &lt;attribute ref="{http://www.w3.org/1999/xlink}arcrole"/&gt;
 *                 &lt;attribute ref="{http://www.w3.org/1999/xlink}title"/&gt;
 *                 &lt;attribute ref="{http://www.w3.org/1999/xlink}show"/&gt;
 *                 &lt;attribute ref="{http://www.w3.org/1999/xlink}actuate"/&gt;
 *                 &lt;attribute ref="{http://www.w3.org/1999/xlink}to use="required""/&gt;
 *                 &lt;attribute ref="{http://www.w3.org/1999/xlink}from use="required""/&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="smLinkGrp"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="smLocatorLink" maxOccurs="unbounded" minOccurs="2"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;attGroup ref="{http://www.w3.org/1999/xlink}locatorLink"/&gt;
 *                           &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="smArcLink" maxOccurs="unbounded"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;attGroup ref="{http://www.w3.org/1999/xlink}arcLink"/&gt;
 *                           &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *                           &lt;attribute name="ARCTYPE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                           &lt;attribute name="ADMID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" /&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/sequence&gt;
 *                 &lt;attGroup ref="{http://www.w3.org/1999/xlink}extendedLink"/&gt;
 *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *                 &lt;attribute name="ARCLINKORDER" default="unordered"&gt;
 *                   &lt;simpleType&gt;
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                       &lt;enumeration value="ordered"/&gt;
 *                       &lt;enumeration value="unordered"/&gt;
 *                     &lt;/restriction&gt;
 *                   &lt;/simpleType&gt;
 *                 &lt;/attribute&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/choice&gt;
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
@XmlType(name = "structLinkType", propOrder = {
    "smLinkOrSmLinkGrp"
})
@XmlSeeAlso({
    org.kitodo.dataformat.metskitodo.MetsType.StructLink.class
})
public class StructLinkType implements Equals2
{

    @XmlElements({
        @XmlElement(name = "smLink", type = StructLinkType.SmLink.class),
        @XmlElement(name = "smLinkGrp", type = StructLinkType.SmLinkGrp.class)
    })
    protected List<Object> smLinkOrSmLinkGrp;
    @XmlAttribute(name = "ID")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the smLinkOrSmLinkGrp property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the smLinkOrSmLinkGrp property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSmLinkOrSmLinkGrp().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link StructLinkType.SmLink }
     * {@link StructLinkType.SmLinkGrp }
     * 
     * 
     */
    public List<Object> getSmLinkOrSmLinkGrp() {
        if (smLinkOrSmLinkGrp == null) {
            smLinkOrSmLinkGrp = new ArrayList<Object>();
        }
        return this.smLinkOrSmLinkGrp;
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
        final StructLinkType that = ((StructLinkType) object);
        {
            List<Object> lhsSmLinkOrSmLinkGrp;
            lhsSmLinkOrSmLinkGrp = (((this.smLinkOrSmLinkGrp!= null)&&(!this.smLinkOrSmLinkGrp.isEmpty()))?this.getSmLinkOrSmLinkGrp():null);
            List<Object> rhsSmLinkOrSmLinkGrp;
            rhsSmLinkOrSmLinkGrp = (((that.smLinkOrSmLinkGrp!= null)&&(!that.smLinkOrSmLinkGrp.isEmpty()))?that.getSmLinkOrSmLinkGrp():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "smLinkOrSmLinkGrp", lhsSmLinkOrSmLinkGrp), LocatorUtils.property(thatLocator, "smLinkOrSmLinkGrp", rhsSmLinkOrSmLinkGrp), lhsSmLinkOrSmLinkGrp, rhsSmLinkOrSmLinkGrp, ((this.smLinkOrSmLinkGrp!= null)&&(!this.smLinkOrSmLinkGrp.isEmpty())), ((that.smLinkOrSmLinkGrp!= null)&&(!that.smLinkOrSmLinkGrp.isEmpty())))) {
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
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
     *       &lt;attribute ref="{http://www.w3.org/1999/xlink}arcrole"/&gt;
     *       &lt;attribute ref="{http://www.w3.org/1999/xlink}title"/&gt;
     *       &lt;attribute ref="{http://www.w3.org/1999/xlink}show"/&gt;
     *       &lt;attribute ref="{http://www.w3.org/1999/xlink}actuate"/&gt;
     *       &lt;attribute ref="{http://www.w3.org/1999/xlink}to use="required""/&gt;
     *       &lt;attribute ref="{http://www.w3.org/1999/xlink}from use="required""/&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class SmLink implements Equals2
    {

        @XmlAttribute(name = "ID")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlID
        @XmlSchemaType(name = "ID")
        protected String id;
        @XmlAttribute(name = "arcrole", namespace = "http://www.w3.org/1999/xlink")
        protected String arcrole;
        @XmlAttribute(name = "title", namespace = "http://www.w3.org/1999/xlink")
        protected String title;
        @XmlAttribute(name = "show", namespace = "http://www.w3.org/1999/xlink")
        protected String show;
        @XmlAttribute(name = "actuate", namespace = "http://www.w3.org/1999/xlink")
        protected String actuate;
        @XmlAttribute(name = "to", namespace = "http://www.w3.org/1999/xlink", required = true)
        protected String to;
        @XmlAttribute(name = "from", namespace = "http://www.w3.org/1999/xlink", required = true)
        protected String from;

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
         * 
         * 								 xlink:arcrole - the role of the link, as per the xlink specification.  See http://www.w3.org/TR/xlink/
         * 							
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
         * 
         * 								xlink:title - a title for the link (if needed), as per the xlink specification.  See http://www.w3.org/TR/xlink/
         * 							
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
         * 
         * 								xlink:show - see the xlink specification at http://www.w3.org/TR/xlink/
         * 							
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
         * 
         * 								xlink:actuate - see the xlink specification at http://www.w3.org/TR/xlink/
         * 							
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
         * 
         * 								xlink:to - the value of the label for the element in the structMap you are linking to.
         * 							
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTo() {
            return to;
        }

        /**
         * Legt den Wert der to-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTo(String value) {
            this.to = value;
        }

        /**
         * 
         * 								xlink:from - the value of the label for the element in the structMap you are linking from.
         * 							
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getFrom() {
            return from;
        }

        /**
         * Legt den Wert der from-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setFrom(String value) {
            this.from = value;
        }

        public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy2 strategy) {
            if ((object == null)||(this.getClass()!= object.getClass())) {
                return false;
            }
            if (this == object) {
                return true;
            }
            final StructLinkType.SmLink that = ((StructLinkType.SmLink) object);
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
                String lhsTo;
                lhsTo = this.getTo();
                String rhsTo;
                rhsTo = that.getTo();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "to", lhsTo), LocatorUtils.property(thatLocator, "to", rhsTo), lhsTo, rhsTo, (this.to!= null), (that.to!= null))) {
                    return false;
                }
            }
            {
                String lhsFrom;
                lhsFrom = this.getFrom();
                String rhsFrom;
                rhsFrom = that.getFrom();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "from", lhsFrom), LocatorUtils.property(thatLocator, "from", rhsFrom), lhsFrom, rhsFrom, (this.from!= null), (that.from!= null))) {
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
     *       &lt;sequence&gt;
     *         &lt;element name="smLocatorLink" maxOccurs="unbounded" minOccurs="2"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;attGroup ref="{http://www.w3.org/1999/xlink}locatorLink"/&gt;
     *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="smArcLink" maxOccurs="unbounded"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;attGroup ref="{http://www.w3.org/1999/xlink}arcLink"/&gt;
     *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
     *                 &lt;attribute name="ARCTYPE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *                 &lt;attribute name="ADMID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" /&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *       &lt;/sequence&gt;
     *       &lt;attGroup ref="{http://www.w3.org/1999/xlink}extendedLink"/&gt;
     *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
     *       &lt;attribute name="ARCLINKORDER" default="unordered"&gt;
     *         &lt;simpleType&gt;
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *             &lt;enumeration value="ordered"/&gt;
     *             &lt;enumeration value="unordered"/&gt;
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
    @XmlType(name = "", propOrder = {
        "smLocatorLink",
        "smArcLink"
    })
    public static class SmLinkGrp implements Equals2
    {

        @XmlElement(required = true)
        protected List<StructLinkType.SmLinkGrp.SmLocatorLink> smLocatorLink;
        @XmlElement(required = true)
        protected List<StructLinkType.SmLinkGrp.SmArcLink> smArcLink;
        @XmlAttribute(name = "ID")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlID
        @XmlSchemaType(name = "ID")
        protected String id;
        @XmlAttribute(name = "ARCLINKORDER")
        protected String arclinkorder;
        @XmlAttribute(name = "type", namespace = "http://www.w3.org/1999/xlink")
        protected String type;
        @XmlAttribute(name = "role", namespace = "http://www.w3.org/1999/xlink")
        protected String role;
        @XmlAttribute(name = "title", namespace = "http://www.w3.org/1999/xlink")
        protected String title;

        /**
         * Gets the value of the smLocatorLink property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the smLocatorLink property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getSmLocatorLink().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link StructLinkType.SmLinkGrp.SmLocatorLink }
         * 
         * 
         */
        public List<StructLinkType.SmLinkGrp.SmLocatorLink> getSmLocatorLink() {
            if (smLocatorLink == null) {
                smLocatorLink = new ArrayList<StructLinkType.SmLinkGrp.SmLocatorLink>();
            }
            return this.smLocatorLink;
        }

        /**
         * Gets the value of the smArcLink property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the smArcLink property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getSmArcLink().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link StructLinkType.SmLinkGrp.SmArcLink }
         * 
         * 
         */
        public List<StructLinkType.SmLinkGrp.SmArcLink> getSmArcLink() {
            if (smArcLink == null) {
                smArcLink = new ArrayList<StructLinkType.SmLinkGrp.SmArcLink>();
            }
            return this.smArcLink;
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
         * Ruft den Wert der arclinkorder-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getARCLINKORDER() {
            if (arclinkorder == null) {
                return "unordered";
            } else {
                return arclinkorder;
            }
        }

        /**
         * Legt den Wert der arclinkorder-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setARCLINKORDER(String value) {
            this.arclinkorder = value;
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
                return "extended";
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

        public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy2 strategy) {
            if ((object == null)||(this.getClass()!= object.getClass())) {
                return false;
            }
            if (this == object) {
                return true;
            }
            final StructLinkType.SmLinkGrp that = ((StructLinkType.SmLinkGrp) object);
            {
                List<StructLinkType.SmLinkGrp.SmLocatorLink> lhsSmLocatorLink;
                lhsSmLocatorLink = (((this.smLocatorLink!= null)&&(!this.smLocatorLink.isEmpty()))?this.getSmLocatorLink():null);
                List<StructLinkType.SmLinkGrp.SmLocatorLink> rhsSmLocatorLink;
                rhsSmLocatorLink = (((that.smLocatorLink!= null)&&(!that.smLocatorLink.isEmpty()))?that.getSmLocatorLink():null);
                if (!strategy.equals(LocatorUtils.property(thisLocator, "smLocatorLink", lhsSmLocatorLink), LocatorUtils.property(thatLocator, "smLocatorLink", rhsSmLocatorLink), lhsSmLocatorLink, rhsSmLocatorLink, ((this.smLocatorLink!= null)&&(!this.smLocatorLink.isEmpty())), ((that.smLocatorLink!= null)&&(!that.smLocatorLink.isEmpty())))) {
                    return false;
                }
            }
            {
                List<StructLinkType.SmLinkGrp.SmArcLink> lhsSmArcLink;
                lhsSmArcLink = (((this.smArcLink!= null)&&(!this.smArcLink.isEmpty()))?this.getSmArcLink():null);
                List<StructLinkType.SmLinkGrp.SmArcLink> rhsSmArcLink;
                rhsSmArcLink = (((that.smArcLink!= null)&&(!that.smArcLink.isEmpty()))?that.getSmArcLink():null);
                if (!strategy.equals(LocatorUtils.property(thisLocator, "smArcLink", lhsSmArcLink), LocatorUtils.property(thatLocator, "smArcLink", rhsSmArcLink), lhsSmArcLink, rhsSmArcLink, ((this.smArcLink!= null)&&(!this.smArcLink.isEmpty())), ((that.smArcLink!= null)&&(!that.smArcLink.isEmpty())))) {
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
                String lhsARCLINKORDER;
                lhsARCLINKORDER = this.getARCLINKORDER();
                String rhsARCLINKORDER;
                rhsARCLINKORDER = that.getARCLINKORDER();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "arclinkorder", lhsARCLINKORDER), LocatorUtils.property(thatLocator, "arclinkorder", rhsARCLINKORDER), lhsARCLINKORDER, rhsARCLINKORDER, (this.arclinkorder!= null), (that.arclinkorder!= null))) {
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
                String lhsRole;
                lhsRole = this.getRole();
                String rhsRole;
                rhsRole = that.getRole();
                if (!strategy.equals(LocatorUtils.property(thisLocator, "role", lhsRole), LocatorUtils.property(thatLocator, "role", rhsRole), lhsRole, rhsRole, (this.role!= null), (that.role!= null))) {
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
            return true;
        }

        public boolean equals(Object object) {
            final EqualsStrategy2 strategy = JAXBEqualsStrategy.INSTANCE;
            return equals(null, null, object, strategy);
        }


        /**
         * 
         * 										The structMap arc link element <smArcLink> is of xlink:type "arc" It can be used to establish a traversal link between two <div> elements as identified by <smLocatorLink> elements within the same smLinkGrp element. The associated xlink:from and xlink:to attributes identify the from and to sides of the arc link by referencing the xlink:label attribute values on the participating smLocatorLink elements.
         * 									
         * 
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *       &lt;attGroup ref="{http://www.w3.org/1999/xlink}arcLink"/&gt;
         *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
         *       &lt;attribute name="ARCTYPE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *       &lt;attribute name="ADMID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" /&gt;
         *     &lt;/restriction&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class SmArcLink implements Equals2
        {

            @XmlAttribute(name = "ID")
            @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
            @XmlID
            @XmlSchemaType(name = "ID")
            protected String id;
            @XmlAttribute(name = "ARCTYPE")
            protected String arctype;
            @XmlAttribute(name = "ADMID")
            @XmlIDREF
            @XmlSchemaType(name = "IDREFS")
            protected List<Object> admid;
            @XmlAttribute(name = "type", namespace = "http://www.w3.org/1999/xlink")
            protected String type;
            @XmlAttribute(name = "arcrole", namespace = "http://www.w3.org/1999/xlink")
            protected String arcrole;
            @XmlAttribute(name = "title", namespace = "http://www.w3.org/1999/xlink")
            protected String title;
            @XmlAttribute(name = "show", namespace = "http://www.w3.org/1999/xlink")
            protected String show;
            @XmlAttribute(name = "actuate", namespace = "http://www.w3.org/1999/xlink")
            protected String actuate;
            @XmlAttribute(name = "from", namespace = "http://www.w3.org/1999/xlink")
            protected String from;
            @XmlAttribute(name = "to", namespace = "http://www.w3.org/1999/xlink")
            protected String to;

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
             * Ruft den Wert der arctype-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getARCTYPE() {
                return arctype;
            }

            /**
             * Legt den Wert der arctype-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setARCTYPE(String value) {
                this.arctype = value;
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
            public String getType() {
                if (type == null) {
                    return "arc";
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
             * Ruft den Wert der from-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getFrom() {
                return from;
            }

            /**
             * Legt den Wert der from-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setFrom(String value) {
                this.from = value;
            }

            /**
             * Ruft den Wert der to-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getTo() {
                return to;
            }

            /**
             * Legt den Wert der to-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setTo(String value) {
                this.to = value;
            }

            public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy2 strategy) {
                if ((object == null)||(this.getClass()!= object.getClass())) {
                    return false;
                }
                if (this == object) {
                    return true;
                }
                final StructLinkType.SmLinkGrp.SmArcLink that = ((StructLinkType.SmLinkGrp.SmArcLink) object);
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
                    String lhsARCTYPE;
                    lhsARCTYPE = this.getARCTYPE();
                    String rhsARCTYPE;
                    rhsARCTYPE = that.getARCTYPE();
                    if (!strategy.equals(LocatorUtils.property(thisLocator, "arctype", lhsARCTYPE), LocatorUtils.property(thatLocator, "arctype", rhsARCTYPE), lhsARCTYPE, rhsARCTYPE, (this.arctype!= null), (that.arctype!= null))) {
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
                    String lhsType;
                    lhsType = this.getType();
                    String rhsType;
                    rhsType = that.getType();
                    if (!strategy.equals(LocatorUtils.property(thisLocator, "type", lhsType), LocatorUtils.property(thatLocator, "type", rhsType), lhsType, rhsType, (this.type!= null), (that.type!= null))) {
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
                    String lhsFrom;
                    lhsFrom = this.getFrom();
                    String rhsFrom;
                    rhsFrom = that.getFrom();
                    if (!strategy.equals(LocatorUtils.property(thisLocator, "from", lhsFrom), LocatorUtils.property(thatLocator, "from", rhsFrom), lhsFrom, rhsFrom, (this.from!= null), (that.from!= null))) {
                        return false;
                    }
                }
                {
                    String lhsTo;
                    lhsTo = this.getTo();
                    String rhsTo;
                    rhsTo = that.getTo();
                    if (!strategy.equals(LocatorUtils.property(thisLocator, "to", lhsTo), LocatorUtils.property(thatLocator, "to", rhsTo), lhsTo, rhsTo, (this.to!= null), (that.to!= null))) {
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
         *       &lt;attGroup ref="{http://www.w3.org/1999/xlink}locatorLink"/&gt;
         *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
         *     &lt;/restriction&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class SmLocatorLink implements Equals2
        {

            @XmlAttribute(name = "ID")
            @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
            @XmlID
            @XmlSchemaType(name = "ID")
            protected String id;
            @XmlAttribute(name = "type", namespace = "http://www.w3.org/1999/xlink")
            protected String type;
            @XmlAttribute(name = "href", namespace = "http://www.w3.org/1999/xlink", required = true)
            @XmlSchemaType(name = "anyURI")
            protected String href;
            @XmlAttribute(name = "role", namespace = "http://www.w3.org/1999/xlink")
            protected String role;
            @XmlAttribute(name = "title", namespace = "http://www.w3.org/1999/xlink")
            protected String title;
            @XmlAttribute(name = "label", namespace = "http://www.w3.org/1999/xlink")
            protected String label;

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
            public String getType() {
                if (type == null) {
                    return "locator";
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
             * Ruft den Wert der label-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getLabel() {
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
            public void setLabel(String value) {
                this.label = value;
            }

            public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy2 strategy) {
                if ((object == null)||(this.getClass()!= object.getClass())) {
                    return false;
                }
                if (this == object) {
                    return true;
                }
                final StructLinkType.SmLinkGrp.SmLocatorLink that = ((StructLinkType.SmLinkGrp.SmLocatorLink) object);
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
                    String lhsTitle;
                    lhsTitle = this.getTitle();
                    String rhsTitle;
                    rhsTitle = that.getTitle();
                    if (!strategy.equals(LocatorUtils.property(thisLocator, "title", lhsTitle), LocatorUtils.property(thatLocator, "title", rhsTitle), lhsTitle, rhsTitle, (this.title!= null), (that.title!= null))) {
                        return false;
                    }
                }
                {
                    String lhsLabel;
                    lhsLabel = this.getLabel();
                    String rhsLabel;
                    rhsLabel = that.getLabel();
                    if (!strategy.equals(LocatorUtils.property(thisLocator, "label", lhsLabel), LocatorUtils.property(thatLocator, "label", rhsLabel), lhsLabel, rhsLabel, (this.label!= null), (that.label!= null))) {
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
