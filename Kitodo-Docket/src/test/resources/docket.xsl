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

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:kitodo="http://www.kitodo.org/logfile" exclude-result-prefixes="fo">
    <xsl:output method="xml" indent="yes"/>
    <xsl:template match="kitodo:process">
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
            <fo:layout-master-set>
                <fo:simple-page-master master-name="A5-landscape" page-width="14.8cm" page-height="21.0cm"
                                       margin-left="1cm" margin-top="1cm"
                                       margin-right="1cm">
                    <fo:region-body/>
                </fo:simple-page-master>
            </fo:layout-master-set>
            <fo:page-sequence master-reference="A5-landscape">
                <fo:flow flow-name="xsl-region-body">
                    <fo:block font-family="sans-serif" font-weight="bold" font-size="16pt" margin-top="20pt">
                        <xsl:value-of select="kitodo:title"/>
                    </fo:block>
                    <fo:block border-top-width="1pt" border-top-style="solid" border-top-color="black"
                              margin-top="20pt"/>
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
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>
</xsl:stylesheet>
