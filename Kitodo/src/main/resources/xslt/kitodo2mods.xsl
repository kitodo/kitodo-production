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
                xmlns:kitodo="http://meta.kitodo.org/v1/"
>
    <xsl:output method="xml" indent="yes" encoding="utf-8"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="/">
        <mods:mods>
            <xsl:apply-templates/>
        </mods:mods>
    </xsl:template>

    <!-- ### TitleDocMain ### -->
    <xsl:template match="kitodo:metadata[@name='TitleDocMain']">
        <mods:titleInfo>
            <mods:title>
                <xsl:value-of select="normalize-space()"/>
            </mods:title>
        </mods:titleInfo>
    </xsl:template>

    <!-- ### Author, slub_Recipient ### -->
    <xsl:template match="kitodo:metadataGroup[@name='Person']">
        <xsl:variable name="role" select="../kitodo:metadata[@name='Role']"/>
        <xsl:variable name="last_name" select="../kitodo:metadata[@name='lastName']"/>
        <xsl:variable name="first_name" select="../kitodo:metadata[@name='firstName']"/>
        <xsl:choose>
            <xsl:when test="$role='author'">
                <mods:name>
                    <mods:namePart type="family">
                        <xsl:value-of select="$last_name"/>
                    </mods:namePart>
                    <mods:namePart type="given">
                        <xsl:value-of select="$first_name"/>
                    </mods:namePart>
                    <mods:displayForm>
                        <xsl:value-of select="$last_name"/>,
                        <xsl:value-of select="$first_name"/>
                    </mods:displayForm>
                    <mods:role>
                        <mods:roleTerm type="code" authority="marcrelator">
                            aut
                        </mods:roleTerm>
                        <mods:roleTerm type="text" authority="marcrelator">
                            Author
                        </mods:roleTerm>
                    </mods:role>
                </mods:name>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <!-- ### PlaceOfPublication ### -->
    <xsl:template match="kitodo:metadata[@name='PlaceOfPublication']">
        <mods:originInfo eventType="publication">
            <mods:place>
                <mods:placeTerm type="text">
                    <xsl:value-of select="normalize-space()"/>
                </mods:placeTerm>
            </mods:place>
        </mods:originInfo>
    </xsl:template>

    <!-- ### PublisherName ### -->
    <xsl:template match="kitodo:metadata[@name='PublisherName']">
        <mods:originInfo eventType="publication">
            <mods:publisher>
                <xsl:value-of select="normalize-space()"/>
            </mods:publisher>
        </mods:originInfo>
    </xsl:template>

    <!-- ### PublicationYear ### -->
    <xsl:template match="kitodo:metadata[@name='PublicationYear']">
        <mods:originInfo eventType="publication">
            <mods:dateIssued keyDate="yes" encoding="iso8601">
                <xsl:value-of select="normalize-space()"/>
            </mods:dateIssued>
        </mods:originInfo>
    </xsl:template>

    <!-- ### ShelfMarkSource ### -->
    <xsl:template match="kitodo:metadata[@name='shelfmarksource']">
        <mods:location>
            <mods:shelfLocator>
                <xsl:value-of select="normalize-space()"/>
            </mods:shelfLocator>
        </mods:location>
    </xsl:template>

    <!-- ### FormatSourcePrint ### -->
    <xsl:template match="kitodo:metadata[@name='FormatSourcePrint']">
        <mods:physicalDescription>
            <mods:extent>
                <xsl:value-of select="normalize-space()"/>
            </mods:extent>
        </mods:physicalDescription>
    </xsl:template>

    <!-- ### CatalogIDDigital ### -->
    <xsl:template match="kitodo:metadata[@name='CatalogIDDigital']">
        <mods:recordInfo>
            <mods:recordIdentifier>
                <xsl:value-of select="normalize-space()"/>
            </mods:recordIdentifier>
        </mods:recordInfo>
    </xsl:template>

    <!-- ### slub_link, slub_linktext ### -->
    <xsl:template match="kitodo:metadata[@name='slub_link']">
        <mods:location>
            <mods:url>
                <xsl:value-of select="normalize-space()"/>
            </mods:url>
        </mods:location>
    </xsl:template>

    <!-- pass-through rule -->
    <xsl:template match="@*|node()">
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
</xsl:stylesheet>
