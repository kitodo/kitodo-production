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
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mets="http://www.loc.gov/METS/"
                xmlns:srw="http://www.loc.gov/zing/srw/"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:kitodo="http://meta.kitodo.org/v1/"
>
    <xsl:output method="xml" indent="yes" encoding="utf-8"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="srw:records">
        <!-- <xsl:apply-templates select="@*|node()"/> -->
        <xsl:for-each select="srw:record/srw:recordData">
            <mets:mdWrap MDTYPE="MODS">
                <mets:xmlData>
                    <kitodo:kitodo>
                        <xsl:apply-templates select="@*|node()"/>
                    </kitodo:kitodo>
                </mets:xmlData>
            </mets:mdWrap>
        </xsl:for-each>
    </xsl:template>

    <!-- ### TitleDocMain ### -->
    <xsl:template match="mods:mods/mods:titleInfo/mods:title">
        <kitodo:metadata name="TitleDocMain"><xsl:value-of select="normalize-space()" /></kitodo:metadata>
    </xsl:template>

    <!-- ### Author, slub_Recipient ### -->
    <xsl:template match="mods:mods/mods:name/mods:namePart">
        <xsl:variable name="uri" select="../@valueURI" />
        <xsl:variable name="role" select="../mods:role/mods:roleTerm[not(@type)]" />
        <xsl:variable name="last_name" select="substring-before(normalize-space(), ',')" />
        <xsl:variable name="first_name" select="substring-after(normalize-space(), ',')" />
        <xsl:choose>
            <xsl:when test="$role='author'">
                <kitodo:metadataGroup name="person">
                    <kitodo:metadata name="role">aut</kitodo:metadata>
                    <kitodo:metadata name="lastName"><xsl:value-of select="$last_name" /></kitodo:metadata >
                    <kitodo:metadata name="firstName"><xsl:value-of select="$first_name" /></kitodo:metadata>
                    <kitodo:metadata name="displayName"><xsl:value-of select="$last_name" />,<xsl:value-of select="$first_name" /></kitodo:metadata>
                    <xsl:if test="$uri and contains($uri, 'gnd')">
                        <kitodo:metadata name="authorityValue"><xsl:value-of select="$uri" /></kitodo:metadata >
                    </xsl:if>
                </kitodo:metadataGroup>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <!-- ### PlaceOfPublication ### -->
    <xsl:template match="mods:mods/mods:originInfo/mods:place/mods:placeTerm[@type='text']">
        <kitodo:metadata name="PlaceOfPublication"><xsl:value-of select="normalize-space()" /></kitodo:metadata>
    </xsl:template>

    <!-- ### PublicationYear ### -->
    <xsl:template match="mods:mods/mods:originInfo/mods:dateCreated[@encoding='w3cdtf']">
        <kitodo:metadata name="PublicationYear"><xsl:value-of select="normalize-space()" /></kitodo:metadata>
    </xsl:template>

    <!-- ### ShelfMarkSource ### -->
    <xsl:template match="mods:mods/mods:location/mods:shelfLocator">
        <kitodo:metadata name="shelfmarksource"><xsl:value-of select="normalize-space()" /></kitodo:metadata>
    </xsl:template>

    <!-- ### FormatSourcePrint ### -->
    <xsl:template match="mods:mods/mods:physicalDescription/mods:extent">
        <kitodo:metadata name="FormatSourcePrint"><xsl:value-of select="normalize-space()" /></kitodo:metadata>
    </xsl:template>

    <!-- ### CatalogIDDigital ### -->
    <xsl:template match="mods:mods/mods:recordInfo/mods:recordIdentifier">
        <kitodo:metadata name="CatalogIDDigital"><xsl:value-of select="normalize-space()" /></kitodo:metadata>
    </xsl:template>

    <!-- ### slub_link, slub_linktext ### -->
    <xsl:template match="mods:mods/mods:location/mods:url">
        <kitodo:metadata name="slub_link"><xsl:value-of select="normalize-space()" /></kitodo:metadata>
        <kitodo:metadata name="slub_linktext">Katalognachweis</kitodo:metadata>
    </xsl:template>

    <!-- pass-through rule -->
    <xsl:template match="@*|node()">
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
</xsl:stylesheet>
