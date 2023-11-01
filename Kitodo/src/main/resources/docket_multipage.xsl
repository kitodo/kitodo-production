<?xml version="1.0" encoding="utf-8"?>
<!--
 *
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 *
-->

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:kitodo="http://www.kitodo.org/logfile" exclude-result-prefixes="fo">
    <xsl:output method="xml" indent="yes"/>
    <xsl:template match="kitodo:processes">
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
            <fo:layout-master-set>
                <fo:simple-page-master master-name="A5-landscape" page-width="14.8cm" page-height="21.0cm"
                                       margin-left="1cm" margin-top="1cm"
                                       margin-right="1cm">
                    <fo:region-body/>
                </fo:simple-page-master>
            </fo:layout-master-set>
            <xsl:apply-templates/>
        </fo:root>
    </xsl:template>
    <xsl:template match="kitodo:process">
        <fo:page-sequence master-reference="A5-landscape">
            <fo:flow flow-name="xsl-region-body">
                <fo:block font-family="sans-serif" font-weight="bold" font-size="16pt" margin-top="20pt">
                    <xsl:value-of select="kitodo:title"/>
                </fo:block>
                <fo:block border-top-width="1pt" border-top-style="solid" border-top-color="black" margin-top="20pt"/>
                <fo:block margin-top="20pt" font-size="10pt">
                    <fo:table line-height="14pt">
                        <fo:table-column column-width="4cm"/>
                        <fo:table-column column-width="9cm"/>
                        <fo:table-body>
                            <fo:table-row>
                                <fo:table-cell>
                                    <fo:block>Projekt:</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block>
                                        <xsl:value-of select="kitodo:project"/>
                                    </fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                                <fo:table-cell>
                                    <fo:block>PPN digital:</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block>
                                        <xsl:value-of select="kitodo:title"/>
                                    </fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                                <fo:table-cell>
                                    <fo:block>Kitodo Identifier:</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block>
                                        <xsl:value-of select="@processID"/>
                                    </fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                                <fo:table-cell>
                                    <fo:block>Anlegedatum:</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block>
                                        <xsl:value-of select="kitodo:time"/>
                                    </fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                                <fo:table-cell>
                                    <fo:block>Regelsatz:</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block>
                                        <xsl:value-of select="kitodo:ruleset"/>
                                    </fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <xsl:for-each select="kitodo:originals/kitodo:original">
                                <xsl:for-each select="kitodo:properties/kitodo:property">
                                    <xsl:if test="@propertyIdentifier ='Signatur'">
                                        <xsl:variable name="barcodemessage2" select="@value"/>
                                        <fo:table-row>
                                            <fo:table-cell>
                                                <fo:block>Signatur:</fo:block>
                                            </fo:table-cell>
                                            <fo:table-cell>
                                                <fo:block>
                                                    <xsl:value-of select="@value"/>
                                                </fo:block>
                                            </fo:table-cell>
                                        </fo:table-row>
                                    </xsl:if>
                                </xsl:for-each>
                            </xsl:for-each>
                        </fo:table-body>
                    </fo:table>
                </fo:block>
                <fo:block border-top-width="1pt" border-top-style="solid" border-top-color="black" margin-top="20pt"
                          margin-bottom="20pt"/>
                <xsl:variable name="barcodemessage1" select="kitodo:title"/>
                <fo:block text-align="center">
                    <fo:instream-foreign-object>
                        <barcode:barcode xmlns:barcode="http://barcode4j.krysalis.org/ns" message="{$barcodemessage1}">
                            <barcode:code128>
                                <barcode:module-width>0.21mm</barcode:module-width>
                                <barcode:height>20mm</barcode:height>
                            </barcode:code128>
                        </barcode:barcode>
                    </fo:instream-foreign-object>
                </fo:block>
                <fo:block border-top-width="1pt" border-top-style="solid" border-top-color="black" margin-top="10pt"
                          margin-bottom="20pt"/>
                <xsl:variable name="barcodemessage2">
                    <xsl:for-each select="kitodo:originals/kitodo:original">
                        <xsl:for-each select="kitodo:properties/kitodo:property">
                            <xsl:if test="@propertyIdentifier ='Signatur'">
                                <xsl:value-of select="@value"/>
                            </xsl:if>
                        </xsl:for-each>
                    </xsl:for-each>
                </xsl:variable>
                <xsl:if test="$barcodemessage2 !=''">
                    <fo:block text-align="center" margin-top="20pt">
                        <fo:instream-foreign-object>
                            <barcode:barcode xmlns:barcode="http://barcode4j.krysalis.org/ns"
                                             message="{$barcodemessage2}">
                                <barcode:code128>
                                    <barcode:module-width>0.21mm</barcode:module-width>
                                    <barcode:height>20mm</barcode:height>
                                </barcode:code128>
                            </barcode:barcode>
                        </fo:instream-foreign-object>
                    </fo:block>
                </xsl:if>

                <fo:block border-top-width="1pt" border-top-style="solid" border-top-color="black" margin-top="20pt"/>
                <fo:block margin-top="20pt" font-size="10pt">
                    <fo:table line-height="14pt">
                        <fo:table-column column-width="4cm"/>
                        <fo:table-column column-width="0.5cm"/>
                        <fo:table-column column-width="0.1cm"/>
                        <fo:table-column column-width="2cm"/>
                        <fo:table-column column-width="0.5cm"/>
                        <fo:table-column column-width="0.1cm"/>
                        <fo:table-column column-width="2cm"/>
                        <fo:table-column column-width="0.5cm"/>
                        <fo:table-column column-width="0.1cm"/>
                        <fo:table-column column-width="2cm"/>
                        <fo:table-body>

                            <xsl:for-each select="kitodo:digitalDocuments/kitodo:digitalDocument">
                                <xsl:for-each select="kitodo:properties/kitodo:property">

                                    <xsl:if test="@propertyIdentifier ='Öffnungswinkel'">
                                        <fo:table-row>
                                            <fo:table-cell>
                                                <fo:block>Öffnungswinkel:</fo:block>
                                            </fo:table-cell>
                                            <fo:table-cell border-width="1pt" border-style="solid">
                                                <fo:block>
                                                    <xsl:if test="@value='180°'">
                                                        <fo:block>X</fo:block>
                                                    </xsl:if>
                                                </fo:block>
                                            </fo:table-cell>
                                            <fo:table-cell>
                                                <fo:block></fo:block>
                                            </fo:table-cell>
                                            <fo:table-cell>
                                                <fo:block>180°</fo:block>
                                            </fo:table-cell>
                                            <fo:table-cell border-width="1pt" border-style="solid">
                                                <fo:block>
                                                    <xsl:if test="@value='90°'">
                                                        <fo:block>X</fo:block>
                                                    </xsl:if>
                                                </fo:block>
                                            </fo:table-cell>
                                            <fo:table-cell>
                                                <fo:block></fo:block>
                                            </fo:table-cell>
                                            <fo:table-cell>
                                                <fo:block>90°</fo:block>
                                            </fo:table-cell>
                                            <fo:table-cell>
                                                <fo:block></fo:block>
                                            </fo:table-cell>
                                            <fo:table-cell>
                                                <fo:block></fo:block>
                                            </fo:table-cell>
                                            <fo:table-cell>
                                                <fo:block></fo:block>
                                            </fo:table-cell>
                                        </fo:table-row>
                                    </xsl:if>
                                </xsl:for-each>
                            </xsl:for-each>
                            <fo:table-row height="0.2cm">
                                <fo:table-cell>
                                    <fo:block></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block></fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <xsl:for-each select="kitodo:digitalDocuments/kitodo:digitalDocument">
                                <xsl:for-each select="kitodo:properties/kitodo:property">
                                    <xsl:if test="@propertyIdentifier ='Farbtiefe'">
                                        <fo:table-row>
                                            <fo:table-cell>
                                                <fo:block>Farbtiefe:</fo:block>
                                            </fo:table-cell>
                                            <fo:table-cell border-width="1pt" border-style="solid">
                                                <fo:block>
                                                    <xsl:if test="@value='Bitonal'">
                                                        <fo:block>X</fo:block>
                                                    </xsl:if>
                                                </fo:block>
                                            </fo:table-cell>
                                            <fo:table-cell>
                                                <fo:block></fo:block>
                                            </fo:table-cell>
                                            <fo:table-cell>
                                                <fo:block>Bitonal</fo:block>
                                            </fo:table-cell>
                                            <fo:table-cell border-width="1pt" border-style="solid">
                                                <fo:block>
                                                    <xsl:if test="@value='Graustufen'">
                                                        <fo:block>X</fo:block>
                                                    </xsl:if>
                                                </fo:block>
                                            </fo:table-cell>
                                            <fo:table-cell>
                                                <fo:block></fo:block>
                                            </fo:table-cell>
                                            <fo:table-cell>
                                                <fo:block>Graustufen</fo:block>
                                            </fo:table-cell>
                                            <fo:table-cell border-width="1pt" border-style="solid">
                                                <fo:block>
                                                    <xsl:if test="@value='Farbe'">
                                                        <fo:block>X</fo:block>
                                                    </xsl:if>
                                                </fo:block>
                                            </fo:table-cell>
                                            <fo:table-cell>
                                                <fo:block></fo:block>
                                            </fo:table-cell>
                                            <fo:table-cell>
                                                <fo:block>Farbe</fo:block>
                                            </fo:table-cell>
                                        </fo:table-row>
                                    </xsl:if>
                                </xsl:for-each>

                            </xsl:for-each>


                            <fo:table-row height="0.2cm">
                                <fo:table-cell>
                                    <fo:block></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block></fo:block>
                                </fo:table-cell>
                            </fo:table-row>


                        </fo:table-body>
                    </fo:table>
                    <fo:table line-height="14pt">
                        <fo:table-column column-width="12.8cm"/>
                        <fo:table-body>
                            <xsl:for-each select="kitodo:comments/kitodo:comment">
                                <fo:table-row border-width="1pt" border-style="solid">
                                    <fo:table-cell>
                                        <fo:block>
                                            <xsl:value-of select="text()"/>
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                            </xsl:for-each>
                        </fo:table-body>
                    </fo:table>

                </fo:block>
            </fo:flow>
        </fo:page-sequence>
    </xsl:template>
</xsl:stylesheet>
