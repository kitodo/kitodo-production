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
                xmlns:ead="urn:isbn:1-931666-22-9"
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
                        <xsl:when test="c/@level='file'">
                            <kitodo:metadata name="docType">
                                <xsl:text>verzeichnungseinheit</xsl:text>
                            </kitodo:metadata>
                            <kitodo:metadata name="id">
                                <xsl:value-of select="replace(c/@id, 'VE_', '')"/>
                            </kitodo:metadata>
                            <xsl:apply-templates select="@*|node()"/>
                        </xsl:when>
                        <xsl:when test="c/@level='item'">
                            <kitodo:metadata name="docType">
                                <xsl:text>vorgang</xsl:text>
                            </kitodo:metadata>
                            <kitodo:metadata name="id">
                                <xsl:value-of select="replace(c/@id, 'VE_', '')"/>
                            </kitodo:metadata>
                            <xsl:apply-templates select="@*|node()"/>
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

    <!-- ### Name BK ### -->
    <xsl:template match="c[@level='collection' and @id]/did/unittitle">
        <kitodo:metadata name="name">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
        <!-- ### Name (intern) ### -->
        <kitodo:metadata name="name_intern">
            <xsl:value-of select="replace(replace(normalize-space(), ' ', '_'), '\.', '_')"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Einheitstitel VE ### -->
    <xsl:template match="c[@level='file' and @id]/did/unittitle[@type='Einheitstitel']">
        <kitodo:metadata name="einheitstitel">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Signatur ### -->
    <xsl:template match="c[@level='file' and @id]/did/unitid[not(@type)]">
        <kitodo:metadata name="signatur">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
        <!-- ### Signatur (intern) ### -->
        <kitodo:metadata name="signatur_intern">
            <xsl:value-of select="replace(replace(normalize-space(), ' ', '_'), '\.', '_')"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Alte Signatur ### -->
    <xsl:template match="c[@level='file' and @id]/did/unitid[@type='Altsignatur']">
        <kitodo:metadata name="alte_signatur">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Inventarnummer ### -->
    <xsl:template match="//c[@level='file' and @id]/did/unitid[@type='Inventarnummer']">
        <kitodo:metadata name="inventarnummer">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Enthält ### -->
    <xsl:template match="//c[@level='file' and @id]/did/abstract[@type='Enth&#xE4;lt']">
        <kitodo:metadata name="enthaelt">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Inhaltliche Beschreibung ### -->
    <xsl:template match="//c[@level='file' and @id]/did/abstract[@type='Inhaltliche Beschreibung']">
        <kitodo:metadata name="inhaltliche_beschreibung">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Provenienz ### -->
    <xsl:template match="//c[@level='file' and @id]/did/origination">
        <kitodo:metadata name="provenienz">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Umfang ### -->
    <xsl:template match="//c[@level='file']/did/physdesc/extent">
        <kitodo:metadata name="umfang">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Objekttyp ### -->
    <xsl:template match="//c[@level='file']/did/physdesc/genreform[@normal]">
        <kitodo:metadata name="objekttyp_id">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Sprache ### -->
    <xsl:template match="//c[@level='file']/did/langmaterial/language">
        <kitodo:metadata name="sprache">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Material/Technik ### -->
    <xsl:template match="//c[@level='file']/did/materialspec">
        <kitodo:metadata name="material_technik">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Freigabe-Status (VE) ### -->
    <xsl:template match="//c[@level='file']/accessrestrict">
        <xsl:if test="./head/text()='Zugangsbeschränkung' and ./p">
            <kitodo:metadata name="freigabe_id">
                <xsl:value-of select="normalize-space(./p/text())"/>
            </kitodo:metadata>
        </xsl:if>
    </xsl:template>

    <!-- ### Rechte (VE) & Lizenz ### -->
    <xsl:template match="//c[@level='file']/userestrict">
        <xsl:choose>
            <xsl:when test="./head/text()='Lizenzen' and ./p">
                <!-- ### Lizenz ### -->
                <kitodo:metadata name="license">
                    <xsl:value-of select="normalize-space(./p/text())"/>
                </kitodo:metadata>
            </xsl:when>
            <xsl:otherwise>
                <!-- ### Rechte (VE) ### -->
                <kitodo:metadata name="rechte_id">
                    <xsl:value-of select="normalize-space(./p/text())"/>
                </kitodo:metadata>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ### Maße/Format ### -->
    <xsl:template match="//c[@level='file']/dimensions">
        <kitodo:metadata name="masse_format">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### General rule for "odd" tags, including exceptions ### -->
    <xsl:template match="//c[@level='file']/odd">
        <xsl:if test="./head and ./p">
            <xsl:variable name="metadataName" select="replace(replace(replace(replace(replace(replace(./head/text(), 'Technische Daten / ', ''), 'ä', 'ae'), 'ö', 'oe'), 'ü', 'ue'), ' ', '_'), 'ß', 'ss')"/>
            <xsl:choose>
                <xsl:when test="$metadataName='Projekt/Inszenierung'">
                    <kitodo:metadata name="projekt">
                        <xsl:value-of select="normalize-space(./p/text())"/>
                    </kitodo:metadata>
                </xsl:when>
                <xsl:otherwise>
                    <kitodo:metadata name="{concat(lower-case(substring($metadataName, 1, 1)), substring($metadataName, 2))}">
                        <xsl:value-of select="normalize-space(./p/text())"/>
                    </kitodo:metadata>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>


    <!-- ### Ort ### -->
    <xsl:template match="//c[@level='file' and @id]/index/indexentry/geogname">
        <kitodo:metadata name="ort">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Person ### -->
    <xsl:template match="//c[@level='file' and @id]/index/indexentry/persname[@source='GND']">
        <kitodo:metadataGroup name="person">

            <!-- Name -->
            <kitodo:metadata name="gnd_name">
                <xsl:value-of select="normalize-space()"/>
            </kitodo:metadata>

            <!-- Rolle -->
            <xsl:if test="@role!=''">
                <kitodo:metadata name="typ_id">
                    <xsl:value-of select="normalize-space(@role)"/>
                </kitodo:metadata>
            </xsl:if>

            <!-- GND-ID -->
            <xsl:if test="@authfilenumber!=''">
                <kitodo:metadata name="gnd_id">
                    <xsl:value-of select="normalize-space(@authfilenumber)"/>
                </kitodo:metadata>
            </xsl:if>

        </kitodo:metadataGroup>
    </xsl:template>

    <!-- ### Datierung-verbal, Datierung von, Datierung bis ### -->
    <xsl:template match="c[@level='file']/did/unitdate[@normal]">
        <kitodo:metadata name="datierung">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
        <kitodo:metadata name="datierung_date_von">
            <xsl:value-of select="substring-before(normalize-space(@normal), '/')"/>
        </kitodo:metadata>
        <kitodo:metadata name="datierung_date_bis">
            <xsl:value-of select="substring-after(normalize-space(@normal), '')"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- pass-through rule -->
    <xsl:template match="@*|node()">
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
</xsl:stylesheet>
