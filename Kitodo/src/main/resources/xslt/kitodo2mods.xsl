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

    <xsl:template match="kitodo:kitodo">
        <xsl:variable name="title" select="kitodo:metadata[@name='TitleDocMain']"/>

        <xsl:variable name="role" select="kitodo:metadataGroup[@name='Person']/kitodo:metadata[@name='Role']"/>
        <xsl:variable name="last_name" select="kitodo:metadataGroup[@name='Person']/kitodo:metadata[@name='lastName']"/>
        <xsl:variable name="first_name" select="kitodo:metadataGroup[@name='Person']/kitodo:metadata[@name='firstName']"/>

        <xsl:variable name="place" select="kitodo:metadata[@name='PlaceOfPublication']"/>
        <xsl:variable name="publisher" select="kitodo:metadata[@name='PublisherName']"/>
        <xsl:variable name="dateIssued" select="kitodo:metadata[@name='PublicationYear']"/>

        <xsl:variable name="physicalDescription" select="kitodo:metadata[@name='FormatSourcePrint']"/>

        <xsl:variable name="shelfLocator" select="kitodo:metadata[@name='shelfmarksource']"/>
        <xsl:variable name="url" select="kitodo:metadata[@name='slub_link']"/>

        <xsl:variable name="recordIdentifier" select="kitodo:metadata[@name='CatalogIDDigital']"/>

        <mods:titleInfo>
            <mods:title>
                <xsl:value-of select="$title"/>
            </mods:title>
        </mods:titleInfo>

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

        <mods:originInfo eventType="publication">
            <mods:place>
                <mods:placeTerm type="text">
                    <xsl:value-of select="$place"/>
                </mods:placeTerm>
                <mods:publisher>
                    <xsl:value-of select="$publisher"/>
                </mods:publisher>
                <mods:dateIssued keyDate="yes" encoding="iso8601">
                    <xsl:value-of select="$dateIssued"/>
                </mods:dateIssued>
            </mods:place>
        </mods:originInfo>

        <mods:physicalDescription>
            <mods:extent>
                <xsl:value-of select="$physicalDescription"/>
            </mods:extent>
        </mods:physicalDescription>

        <mods:location>
            <mods:shelfLocator>
                <xsl:value-of select="$shelfLocator"/>
            </mods:shelfLocator>
            <mods:url>
                <xsl:value-of select="$url"/>
            </mods:url>
        </mods:location>

        <mods:recordInfo>
            <mods:recordIdentifier>
                <xsl:value-of select="$recordIdentifier"/>
            </mods:recordIdentifier>
        </mods:recordInfo>
    </xsl:template>

    <!-- pass-through rule -->
    <xsl:template match="@*|node()">
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
</xsl:stylesheet>
