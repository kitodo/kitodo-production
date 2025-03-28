<?xml version="1.0" encoding="UTF-8" ?>
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

<xsl:stylesheet version="2.0"
                xmlns="urn:isbn:1-931666-22-9"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mets="http://www.loc.gov/METS/"
                xmlns:kitodo="http://meta.kitodo.org/v1/">

    <xsl:output method="xml" indent="yes" encoding="utf-8"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="/">
        <mets:mdWrap>
            <mets:xmlData>
                <kitodo:kitodo>
                    <!-- ### Classify document type ### -->
                    <xsl:choose>
                        <xsl:when test="c/@level='collection'">
                            <kitodo:metadata name="docType">
                                <xsl:text>bestand</xsl:text>
                            </kitodo:metadata>
                            <kitodo:metadata name="id">
                                <xsl:value-of select="replace(c/@id, 'BK_', '')"/>
                            </kitodo:metadata>
                            <xsl:apply-templates select="@*|./child::*[not(self::c[@level='file'])]"/>
                        </xsl:when>
                        <xsl:when test="c/@level='class'">
                            <kitodo:metadata name="docType">
                                <xsl:text>klassifikation</xsl:text>
                            </kitodo:metadata>
                            <kitodo:metadata name="id">
                                <xsl:value-of select="replace(c/@id, 'BK_', '')"/>
                            </kitodo:metadata>
                            <xsl:apply-templates select="@*|./child::*[not(self::c[@level='file'])]"/>
                        </xsl:when>
                        <xsl:when test="c/@level='series'">
                            <kitodo:metadata name="docType">
                                <xsl:text>series</xsl:text>
                            </kitodo:metadata>
                            <kitodo:metadata name="id">
                                <xsl:value-of select="replace(c/@id, 'BK_', '')"/>
                            </kitodo:metadata>
                            <xsl:apply-templates select="@*|./child::*[not(self::c[@level='file'])]"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <kitodo:metadata name="docType">
                                <xsl:text>UNKNOWN</xsl:text>
                            </kitodo:metadata>
                        </xsl:otherwise>
                    </xsl:choose>
                </kitodo:kitodo>
            </mets:xmlData>
        </mets:mdWrap>
    </xsl:template>

    <!-- ### Name ### -->
    <xsl:template match="c[@level='collection' and @id]/did/unittitle">
        <kitodo:metadata name="name">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
        <!-- ### Name (intern) ### -->
        <kitodo:metadata name="name_intern">
            <xsl:value-of select="replace(replace(normalize-space(), ' ', '_'), '\.', '_')"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Laufzeit (Bestand) ### -->
    <xsl:template match="//c[@level='collection' and @id]/did/unitdate">
        <kitodo:metadata name="info_laufzeit">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Rechtebedingungen (Bestand) ### -->
    <xsl:template match="//c[@level='collection']/userestrict">
        <xsl:if test="./head/text()='Nutzungsbedingungen' and ./p">
            <kitodo:metadata name="bestand_rechtebedingungen">
                <xsl:value-of select="normalize-space(./p/text())"/>
            </kitodo:metadata>
        </xsl:if>
    </xsl:template>

    <!-- ### Status (Bestand) ### -->
    <xsl:template match="//c[@level='collection']/accessrestrict">
        <xsl:if test="./head/text()='Zugangsbeschränkung' and ./p">
            <kitodo:metadata name="status_id">
                <xsl:value-of select="normalize-space(./p/text())"/>
            </kitodo:metadata>
        </xsl:if>
    </xsl:template>

    <!-- ### Umfang (Bestand) ### -->
    <xsl:template match="//c[@level='collection']/did/physdesc/extent">
        <kitodo:metadata name="info_umfang">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- pass-through rule -->
    <xsl:template match="@*|node()">
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
</xsl:stylesheet>
