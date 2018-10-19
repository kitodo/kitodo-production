<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:goobi="http://meta.goobi.org/v1.5.1/"
                xmlns:mets="http://www.loc.gov/METS/"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="http://meta.goobi.org/v1.5.1/ ">
    <xsl:output method="xml" indent="yes" encoding="utf-8"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="/mods:mods">
        <mets:mdWrap MDTYPE="MODS">
            <mets:xmlData>
                <mods:mods>
                    <mods:extension>
                        <goobi:goobi>
                            <xsl:apply-templates select="@*|node()"/>
                        </goobi:goobi>
                    </mods:extension>
                </mods:mods>
            </mets:xmlData>
        </mets:mdWrap>
    </xsl:template>

    <!-- ### TitleDocMain ### -->
    <xsl:template match="/mods:mods/mods:titleInfo[not(@type='abbreviated')]/mods:title">
        <goobi:metadata name="TitleDocMain">
            <xsl:value-of select="replace(normalize-space(), '\(Titel\)', '')"/>
        </goobi:metadata>
    </xsl:template>

    <!-- ### Author, slub_Recipient ### -->
    <xsl:template match="/mods:mods/mods:name/mods:namePart">
        <xsl:variable name="uri" select="../@valueURI"/>
        <xsl:variable name="role" select="../mods:role/mods:roleTerm[not(@type)]"/>
        <xsl:variable name="last_name" select="substring-before(normalize-space(), ',')"/>
        <xsl:variable name="first_name" select="substring-after(normalize-space(), ',')"/>
        <xsl:choose>
            <xsl:when test="$role='author'">
                <goobi:metadata name="Author" type="person">
                    <goobi:lastName>
                        <xsl:value-of select="$last_name"/>
                    </goobi:lastName>
                    <goobi:firstName>
                        <xsl:value-of select="$first_name"/>
                    </goobi:firstName>
                    <goobi:displayName><xsl:value-of select="$last_name"/>,<xsl:value-of select="$first_name"/>
                    </goobi:displayName>
                    <xsl:if test="$uri and contains($uri, 'gnd')">
                        <goobi:identifier>
                            <xsl:value-of select="tokenize($uri, '/')[last()]"/>
                        </goobi:identifier>
                        <goobi:identifierType>GND</goobi:identifierType>
                    </xsl:if>
                </goobi:metadata>
            </xsl:when>
            <xsl:when test="$role='addressee'">
                <goobi:metadata name="slub_Recipient" type="person">
                    <goobi:lastName>
                        <xsl:value-of select="$last_name"/>
                    </goobi:lastName>
                    <goobi:firstName>
                        <xsl:value-of select="$first_name"/>
                    </goobi:firstName>
                    <goobi:displayName><xsl:value-of select="$last_name"/>,<xsl:value-of select="$first_name"/>
                    </goobi:displayName>
                    <xsl:if test="$uri and contains($uri, 'gnd')">
                        <goobi:identifier>
                            <xsl:value-of select="tokenize($uri, '/')[last()]"/>
                        </goobi:identifier>
                        <goobi:identifierType>GND</goobi:identifierType>
                    </xsl:if>
                </goobi:metadata>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <!-- ### PlaceOfPublication ### -->
    <xsl:template match="/mods:mods/mods:originInfo/mods:place/mods:placeTerm[@type='text']">
        <goobi:metadata name="PlaceOfPublication">
            <xsl:value-of select="normalize-space()"/>
        </goobi:metadata>
    </xsl:template>

    <!-- ### PublicationYear ### -->
    <xsl:template match="/mods:mods/mods:originInfo/mods:dateCreated[@encoding='w3cdtf']">
        <goobi:metadata name="PublicationYear">
            <xsl:value-of select="normalize-space()"/>
        </goobi:metadata>
    </xsl:template>

    <!-- ### ShelfMarkSource ### -->
    <xsl:template match="/mods:mods/mods:location/mods:shelfLocator">
        <goobi:metadata name="shelfmarksource">
            <xsl:value-of select="normalize-space()"/>
        </goobi:metadata>
    </xsl:template>

    <!-- ### FormatSourcePrint ### -->
    <xsl:template match="/mods:mods/mods:physicalDescription/mods:extent">
        <goobi:metadata name="FormatSourcePrint">
            <xsl:value-of select="normalize-space()"/>
        </goobi:metadata>
    </xsl:template>

    <!-- ### CatalogIDDigital ### -->
    <xsl:template match="/mods:mods/mods:recordInfo/mods:recordIdentifier">
        <goobi:metadata name="CatalogIDDigital">
            <xsl:value-of select="normalize-space()"/>
        </goobi:metadata>
    </xsl:template>

    <!-- ### slub_link, slub_linktext ### -->
    <xsl:template match="/mods:mods/mods:location/mods:url">
        <goobi:metadata name="slub_link">
            <xsl:value-of select="normalize-space()"/>
        </goobi:metadata>
        <goobi:metadata name="slub_linktext">Katalognachweis</goobi:metadata>
    </xsl:template>

    <!-- pass-through rule -->
    <xsl:template match="@*|node()">
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
</xsl:stylesheet>