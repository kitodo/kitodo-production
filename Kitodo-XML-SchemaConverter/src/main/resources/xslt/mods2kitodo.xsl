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
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:kitodo="http://meta.kitodo.org/v1/" >

    <xsl:output method="xml" indent="yes" encoding="utf-8"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="mods:mods">
        <mets:mdWrap MDTYPE="MODS">
            <mets:xmlData>
                <kitodo:kitodo>
                    <xsl:apply-templates select="@*|node()"/>
                </kitodo:kitodo>
            </mets:xmlData>
        </mets:mdWrap>
    </xsl:template>

    <!-- ### TitleDocMain ### -->
    <xsl:template match="mods:titleInfo[not(@type='alternative')]/mods:title">
        <kitodo:metadata name="TitleDocMain"><xsl:value-of select="normalize-space()" /></kitodo:metadata>
    </xsl:template>

    <!-- ### TitleDocMainShort ### -->
    <xsl:template match="mods:titleInfo[@type='alternative']/mods:title">
        <kitodo:metadata name="TitleDocMainShort"><xsl:value-of select="normalize-space()" /></kitodo:metadata>
    </xsl:template>

    <!-- ### PlaceOfPublication ### -->
    <xsl:template match="mods:originInfo/mods:place/mods:placeTerm">
        <kitodo:metadata name="PlaceOfPublication"><xsl:value-of select="normalize-space()" /></kitodo:metadata>
    </xsl:template>

    <!-- ### PublicationRun ### -->
    <xsl:template match="mods:originInfo/mods:dateIssued | mods:originInfo/mods:dateCreated">
        <kitodo:metadata name="PublicationRun"><xsl:value-of select="normalize-space()" /></kitodo:metadata>
    </xsl:template>


    <!-- ### DocType ### -->
    <xsl:template match="mods:originInfo/mods:issuance">
        <kitodo:metadata name="docType"><xsl:value-of select="normalize-space()" /></kitodo:metadata>
    </xsl:template>

    <!-- ### DocLanguage ### -->
    <xsl:template match="mods:language/mods:languageTerm[@type='text']">
        <kitodo:metadata name="DocLanguage"><xsl:value-of select="normalize-space()" /></kitodo:metadata>
    </xsl:template>

    <!-- ### ShelfMarkSource ### -->
    <xsl:template match="mods:location/mods:shelfLocator">
        <kitodo:metadata name="shelfmarksource"><xsl:value-of select="normalize-space()" /></kitodo:metadata>
    </xsl:template>

    <!-- ### FormatSourcePrint ### -->
    <xsl:template match="mods:physicalDescription/mods:extent">
        <kitodo:metadata name="FormatSourcePrint"><xsl:value-of select="normalize-space()" /></kitodo:metadata>
    </xsl:template>

    <!-- ### SizeSourcePrint ### -->
    <xsl:template match="mods:abstract[@type='content']">
        <kitodo:metadata name="SizeSourcePrint"><xsl:value-of select="normalize-space()" /></kitodo:metadata>
    </xsl:template>

    <!-- ### PublisherName ### -->
    <xsl:template match="mods:recordInfo/mods:recordContentSource">
        <kitodo:metadata name="PublisherName"><xsl:value-of select="normalize-space()" /></kitodo:metadata>
    </xsl:template>

    <!-- ### PublicationYear ### -->
    <xsl:template match="mods:originInfo/mods:dateCreated[@encoding='w3cdtf']">
        <kitodo:metadata name="PublicationYear"><xsl:value-of select="normalize-space()" /></kitodo:metadata>
    </xsl:template>

    <!-- ### PublicationStart ### -->
    <xsl:template match="mods:recordInfo/mods:recordCreationDate">
        <kitodo:metadata name="PublicationStart"><xsl:value-of select="normalize-space(substring(., 1, 4))" /></kitodo:metadata>
    </xsl:template>

    <!-- ### CatalogIDDigital ### -->
    <xsl:template match="mods:recordInfo/mods:recordIdentifier">
        <kitodo:metadata name="CatalogIDDigital"><xsl:value-of select="normalize-space()" /></kitodo:metadata>
        <kitodo:metadata name="CatalogIDSource"><xsl:value-of select="normalize-space(@source)" /></kitodo:metadata>
    </xsl:template>

    <!-- ### slub_link, slub_linktext ### -->
    <xsl:template match="mods:location/mods:url">
        <kitodo:metadata name="Link"><xsl:value-of select="normalize-space()" /></kitodo:metadata>
        <kitodo:metadata name="slub_linktext">Katalognachweis</kitodo:metadata>
    </xsl:template>

    <!-- ### relatedItemId ### -->
    <xsl:template match="mods:relatedItem/mods:identifier[@type='localparentid'] | mods:relatedItem[@type='host']/mods:identifier">
        <kitodo:metadata name="CatalogIDPredecessorPeriodical"><xsl:value-of select="normalize-space()" /></kitodo:metadata>
    </xsl:template>

    <!-- ### Author, slub_Recipient ### -->
    <xsl:template match="mods:name">
        <xsl:variable name="uri" select="mods:nameIdentifier" />
        <xsl:variable name="role" select="mods:role/mods:roleTerm[@type='text']" />
        <xsl:variable name="last_name" select="mods:namePart[@type='family']"/>
        <xsl:variable name="first_name" select="mods:namePart[@type='given']" />
        <xsl:variable name="display_name" select="mods:namePart" />
        <xsl:variable name="date_death"/>
        <kitodo:metadataGroup name="Person">
            <kitodo:metadata name="IdentifierURI"><xsl:value-of select="$uri" /></kitodo:metadata>
            <kitodo:metadata name="Role"><xsl:value-of select="$role" /></kitodo:metadata>
            <kitodo:metadata name="LastName">
                <xsl:choose>
                    <xsl:when test="$last_name != ''">
                        <xsl:value-of select="$last_name"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="substring-before($display_name, ', ')"/>
                    </xsl:otherwise>
                </xsl:choose>
            </kitodo:metadata>
            <kitodo:metadata name="FirstName">
                <xsl:choose>
                    <xsl:when test="$first_name != ''">
                        <xsl:value-of select="$first_name"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="substring-after($display_name, ', ')"/>
                    </xsl:otherwise>
                </xsl:choose>
            </kitodo:metadata>
            <kitodo:metadata name="DateOfDeath"><xsl:value-of select="$date_death"/></kitodo:metadata>
        </kitodo:metadataGroup>
    </xsl:template>

    <!-- pass-through rule -->
    <xsl:template match="@*|node()">
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
</xsl:stylesheet>
