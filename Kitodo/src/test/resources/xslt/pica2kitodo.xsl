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
                xmlns:pica="info:srw/schema/5/picaXML-v1.0"
                xmlns:kitodo="http://meta.kitodo.org/v1/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="http://www.loc.gov/METS/ ">

    <xsl:output method="xml" indent="yes" encoding="utf-8"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="pica:record">
        <mets:mdWrap MDTYPE="PICAXML">
            <mets:xmlData>
                <kitodo:kitodo>
                    <xsl:apply-templates select="@*|node()"/>
                    <!-- ### DocType ### -->
                    <kitodo:metadata name="docType">
                        <xsl:variable name="status" select="pica:datafield[@tag='002@']/pica:subfield[@code='0']"/>
                        <xsl:variable name="genre" select="pica:datafield[@tag='013D']/pica:subfield[@code='a']"/>
                        <xsl:if test="matches($status,'^[AO]a[uv]')">
                            <xsl:choose>
                                <xsl:when test="($genre='Handschrift')">
                                    <xsl:text>Manuscript</xsl:text>
                                </xsl:when>
                                <xsl:when test="($genre='Musikhandschrift')">
                                    <xsl:text>Manuscript</xsl:text>
                                </xsl:when>
                                <xsl:when test="($genre='Bild')">
                                    <xsl:text>Graphics</xsl:text>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:text>Monograph</xsl:text>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:if>
                        <xsl:if test="matches($status,'^[AO][fF][uv]')">
                            <xsl:choose>
                                <xsl:when test="($genre='Bild')">
                                    <xsl:text>MultiPartGraphics</xsl:text>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:text>MultiVolumeWork</xsl:text>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:if>
                        <xsl:if test="matches($status,'^[AO]b[uv]')">
                            <xsl:choose>
                                <xsl:when test="($genre='Zeitung')">
                                    <xsl:text>Newspaper</xsl:text>
                                </xsl:when>
                                <xsl:when test="($genre='Programmheft')">
                                    <xsl:text>Ephemera</xsl:text>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:text>Periodical</xsl:text>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:if>
                    </kitodo:metadata>
                </kitodo:kitodo>
            </mets:xmlData>
        </mets:mdWrap>
    </xsl:template>

    <!-- ### VD16-Nummer ### -->
    <xsl:template match="datafield[@tag='006V']/subfield[@code='0']">
        <kitodo:metadata name="VD16">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### VD17-Nummer ### -->
    <xsl:template match="pica:datafield[@tag='006W']/pica:subfield[@code='0']">
        <kitodo:metadata name="VD17">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### VD18-Nummer ### -->
    <xsl:template match="pica:datafield[@tag='006M']/pica:subfield[@code='0']">
        <kitodo:metadata name="VD18">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### ZDB-Nummer ### -->
    <xsl:template match="pica:datafield[@tag='006M']/pica:subfield[@code='0']">
        <kitodo:metadata name="CatalogIDPeriodicalDB">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Besitznachweis des reproduzierten Exemplars / Signatur ### -->
    <xsl:template match="pica:datafield[@tag='009A']/pica:subfield[@code='a']">
        <kitodo:metadata name="shelfmarksource">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Sprachcodes ### -->
    <xsl:template match="pica:datafield[@tag='010@']/pica:subfield[@code='a']">
        <kitodo:metadata name="DocLanguage">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Entstehungsdatum (Sortierung) ### -->
    <xsl:template match="pica:datafield[@tag='011@']/pica:subfield[@code='a']">
        <kitodo:metadata name="PublicationYearSorting">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Entstehungsdatum ### -->
    <xsl:template match="pica:datafield[@tag='011@']/pica:subfield[@code='n']">
        <kitodo:metadata name="PublicationYear">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Musikalische Ausgabeform ### -->
    <xsl:template match="pica:datafield[@tag='013E']/pica:subfield[@code='a']">
        <kitodo:metadata name="slub_music_format">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### URL für sonstige Angaben zur Ressource / URL ### -->
    <xsl:template match="pica:datafield[@tag='017H']/pica:subfield[@code='u']">
        <kitodo:metadata name="slub_link">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### URL für sonstige Angaben zur Ressource / URL ### -->
    <xsl:template match="pica:datafield[@tag='017H']/pica:subfield[@code='y']">
        <kitodo:metadata name="slub_linktext">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Haupttitel, Titelzusatz, Haupttitel (Sortierung) ### -->
    <xsl:template match="pica:datafield[@tag='021A']">
        <xsl:variable name="TitleDocMain" select="pica:subfield[@code='a']"/>
        <xsl:variable name="TitleDocSub1" select="pica:subfield[@code='d']"/>
        <kitodo:metadata name="TitleDocMain">
            <xsl:value-of select="normalize-space (replace($TitleDocMain, ' @', ' '))"/>
        </kitodo:metadata>
        <kitodo:metadata name="TitleDocSub1">
            <xsl:value-of select="normalize-space($TitleDocSub1)"/>
        </kitodo:metadata>
        <kitodo:metadata name="TitleDocMainShort">
            <xsl:value-of select="normalize-space(substring-after($TitleDocMain, ' @'))"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Abweichender Titel (Sucheinstieg) ### -->
    <xsl:template match="pica:datafield[@tag='027A']">
        <xsl:variable name="TitleDocParallel" select="pica:subfield[@code='a']"/>
        <kitodo:metadata name="TitleDocParallel">
            <xsl:value-of select="normalize-space(replace($TitleDocParallel, ' @', ' '))"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Ausgabevermerk ### -->
    <xsl:template match="pica:datafield[@tag='032@']/pica:subfield[@code='a']">
        <kitodo:metadata name="slub_edition">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Verlagsname ### -->
    <xsl:template match="pica:datafield[@tag='033A']/pica:subfield[@code='n']">
        <kitodo:metadata name="PublisherName">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Erscheinungsort ### -->
    <xsl:template match="pica:datafield[@tag='033A']/pica:subfield[@code='p']">
        <kitodo:metadata name="PlaceOfPublication">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Erzeugername ### -->
    <xsl:template match="pica:datafield[@tag='033F']/pica:subfield[@code='n']">
        <kitodo:metadata name="ProducerName">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Entstehungsort ### -->
    <xsl:template match="pica:datafield[@tag='033F']/pica:subfield[@code='p']">
        <kitodo:metadata name="PlaceOfProduction">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Umfang ### -->
    <xsl:template match="pica:datafield[@tag='034D']/pica:subfield[@code='a']">
        <kitodo:metadata name="SizeSourcePrint">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Gesamttitel der fortlaufenden Ressource / Zählung ### -->
    <xsl:template match="pica:datafield[@tag='036E']">
        <xsl:variable name="slub_serialTitle" select="pica:subfield[@code='a']"/>
        <xsl:variable name="slub_serialCurrentNo" select="pica:subfield[@code='l']"/>
        <kitodo:metadataGroup name="slub_serialGroup">
            <kitodo:metadata name="slub_serialTitle">
                <xsl:value-of select="normalize-space(replace($slub_serialTitle, ' @', ' '))"/>
            </kitodo:metadata>
            <kitodo:metadata name="slub_serialCurrentNo">
                <xsl:value-of select="normalize-space($slub_serialCurrentNo)"/>
            </kitodo:metadata>
        </kitodo:metadataGroup>
    </xsl:template>

    <!-- ### CatalogIDDigital ### -->
    <xsl:template match="pica:datafield[@tag='003@']/pica:subfield[@code='0']">
        <kitodo:metadata name="CatalogIDDigital">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Reproduktion andere physische Form / PPN ### -->
    <xsl:template match="pica:datafield[@tag='039I']/pica:subfield[@code='9']">
        <kitodo:metadata name="CatalogIDSourceKXP">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Reproduktion andere physische Form / PPN ### -->
    <xsl:template match="pica:datafield[@tag='039D']/pica:subfield[@code='9']">
        <kitodo:metadata name="CatalogIDSourceKXP">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Person/Familie als 1. geistiger Schöpfer ### -->
    <xsl:template match="pica:datafield[@tag='028A']">
        <xsl:variable name="IdentifierPPN" select="pica:subfield[@code='9']"/>
        <xsl:variable name="IdentifierGND" select="pica:subfield[@code='7']"/>
        <xsl:variable name="IdentifierGNDURI" select="pica:subfield[@code='7']"/>
        <xsl:variable name="Authority" select="pica:subfield[@code='7']"/>
        <xsl:variable name="RoleCode" select="pica:subfield[@code='4']"/>
        <xsl:variable name="RoleText" select="pica:subfield[@code='B']"/>
        <xsl:variable name="LastName" select="pica:subfield[@code='A']"/>
        <xsl:variable name="LastName_2" select="pica:subfield[@code='a']"/>
        <xsl:variable name="FirstName" select="pica:subfield[@code='D']"/>
        <xsl:variable name="FirstName_2" select="pica:subfield[@code='d']"/>
        <xsl:variable name="PersonalName" select="pica:subfield[@code='P']"/>
        <xsl:variable name="AdditionalMetadataPerson" select="pica:subfield[@code='L']"/>
        <xsl:variable name="Count" select="pica:subfield[@code='N']"/>
        <kitodo:metadataGroup name="Person">
            <kitodo:metadata name="IdentifierPPN">
                <xsl:value-of select="normalize-space($IdentifierPPN)"/>
            </kitodo:metadata>
            <kitodo:metadata name="IdentifierGND">
                <xsl:choose>
                    <xsl:when test="$IdentifierGND != ''">
                        <xsl:value-of select="normalize-space(substring-after($IdentifierGND, 'gnd/'))"/>
                    </xsl:when>
                </xsl:choose>
            </kitodo:metadata>
            <kitodo:metadata name="IdentifierGNDURI">
                <xsl:choose>
                    <xsl:when test="$IdentifierGNDURI != ''">
                        <xsl:value-of select="normalize-space(concat('http://d-nb.info/',$IdentifierGND))"/>
                    </xsl:when>
                </xsl:choose>
            </kitodo:metadata>
            <kitodo:metadata name="Authority">
                <xsl:choose>
                    <xsl:when test="$Authority != ''">
                        <xsl:value-of select="normalize-space(substring-before($IdentifierGND, '/'))"/>
                    </xsl:when>
                </xsl:choose>
            </kitodo:metadata>
            <xsl:for-each select="$RoleCode">
                <kitodo:metadata name="RoleCode">
                    <xsl:value-of select="normalize-space(.)"/>
                </kitodo:metadata>
            </xsl:for-each>
            <xsl:for-each select="$RoleText">
                <kitodo:metadata name="RoleText">
                    <xsl:value-of select="normalize-space(.)"/>
                </kitodo:metadata>
            </xsl:for-each>
            <kitodo:metadata name="LastName">
                <xsl:choose>
                    <xsl:when test="($LastName != '')">
                        <xsl:value-of select="normalize-space($LastName)"/>
                    </xsl:when>
                    <xsl:when test="($LastName_2 != '')">
                        <xsl:value-of select="normalize-space($LastName_2)"/>
                    </xsl:when>
                </xsl:choose>
            </kitodo:metadata>
            <kitodo:metadata name="FirstName">
                <xsl:choose>
                    <xsl:when test="($FirstName != '')">
                        <xsl:value-of select="normalize-space($FirstName)"/>
                    </xsl:when>
                    <xsl:when test="($FirstName_2 != '')">
                        <xsl:value-of select="normalize-space($FirstName_2)"/>
                    </xsl:when>
                </xsl:choose>
            </kitodo:metadata>
            <kitodo:metadata name="DisplayForm">
                <xsl:choose>
                    <xsl:when test="(boolean($LastName) = boolean($FirstName)) and ($LastName != '')">
                        <xsl:value-of select="normalize-space(concat($LastName,', ',$FirstName))"/>
                    </xsl:when>
                    <xsl:when test="boolean($LastName) != boolean($FirstName) and ($LastName != '')">
                        <xsl:value-of select="normalize-space($LastName)"/>
                    </xsl:when>
                    <xsl:when test="boolean($FirstName) != boolean($LastName) and ($FirstName != '')">
                        <xsl:value-of select="normalize-space($FirstName)"/>
                    </xsl:when>
                    <xsl:when test="(boolean($PersonalName) = boolean($Count)) and ($PersonalName != '')">
                        <xsl:value-of select="concat($PersonalName, ' ',$Count,', ',$AdditionalMetadataPerson)"/>
                    </xsl:when>
                    <xsl:when
                            test="(boolean($PersonalName) != boolean($Count)) and (boolean($AdditionalMetadataPerson))">
                        <xsl:value-of select="concat($PersonalName,', ',$AdditionalMetadataPerson)"/>
                    </xsl:when>
                    <xsl:when
                            test="(boolean($PersonalName) != boolean($Count)) and (boolean($PersonalName) != boolean($AdditionalMetadataPerson))">
                        <xsl:value-of select="$PersonalName"/>
                    </xsl:when>
                </xsl:choose>
            </kitodo:metadata>
            <kitodo:metadata name="PersonalName">
                <xsl:value-of select="normalize-space($PersonalName)"/>
            </kitodo:metadata>
            <kitodo:metadata name="AdditionalMetadataPerson">
                <xsl:value-of select="normalize-space($AdditionalMetadataPerson)"/>
            </kitodo:metadata>
            <kitodo:metadata name="Count">
                <xsl:value-of select="normalize-space($Count)"/>
            </kitodo:metadata>
        </kitodo:metadataGroup>
    </xsl:template>

    <!-- ### Person/Familie als 2. und weiterer geistiger Schöpfer ### -->
    <xsl:template match="pica:datafield[@tag='028C']">
        <xsl:variable name="IdentifierPPN" select="pica:subfield[@code='9']"/>
        <xsl:variable name="IdentifierGND" select="pica:subfield[@code='7']"/>
        <xsl:variable name="IdentifierGNDURI" select="pica:subfield[@code='7']"/>
        <xsl:variable name="Authority" select="pica:subfield[@code='7']"/>
        <xsl:variable name="RoleCode" select="pica:subfield[@code='4']"/>
        <xsl:variable name="RoleText" select="pica:subfield[@code='B']"/>
        <xsl:variable name="LastName" select="pica:subfield[@code='A']"/>
        <xsl:variable name="LastName_2" select="pica:subfield[@code='a']"/>
        <xsl:variable name="FirstName" select="pica:subfield[@code='D']"/>
        <xsl:variable name="FirstName_2" select="pica:subfield[@code='d']"/>
        <xsl:variable name="PersonalName" select="pica:subfield[@code='P']"/>
        <xsl:variable name="AdditionalMetadataPerson" select="pica:subfield[@code='L']"/>
        <xsl:variable name="Count" select="pica:subfield[@code='N']"/>
        <kitodo:metadataGroup name="ContributorPerson">
            <kitodo:metadata name="IdentifierPPN">
                <xsl:value-of select="normalize-space($IdentifierPPN)"/>
            </kitodo:metadata>
            <kitodo:metadata name="IdentifierGND">
                <xsl:choose>
                    <xsl:when test="$IdentifierGND != ''">
                        <xsl:value-of select="normalize-space(substring-after($IdentifierGND, 'gnd/'))"/>
                    </xsl:when>
                </xsl:choose>
            </kitodo:metadata>
            <kitodo:metadata name="IdentifierGNDURI">
                <xsl:choose>
                    <xsl:when test="$IdentifierGNDURI != ''">
                        <xsl:value-of select="normalize-space(concat('http://d-nb.info/',$IdentifierGND))"/>
                    </xsl:when>
                </xsl:choose>
            </kitodo:metadata>
            <kitodo:metadata name="Authority">
                <xsl:choose>
                    <xsl:when test="$Authority != ''">
                        <xsl:value-of select="normalize-space(substring-before($IdentifierGND, '/'))"/>
                    </xsl:when>
                </xsl:choose>
            </kitodo:metadata>
            <xsl:for-each select="$RoleCode">
                <kitodo:metadata name="RoleCode">
                    <xsl:value-of select="normalize-space(.)"/>
                </kitodo:metadata>
            </xsl:for-each>
            <xsl:for-each select="$RoleText">
                <kitodo:metadata name="RoleText">
                    <xsl:value-of select="normalize-space(.)"/>
                </kitodo:metadata>
            </xsl:for-each>
            <kitodo:metadata name="LastName">
                <xsl:choose>
                    <xsl:when test="($LastName != '')">
                        <xsl:value-of select="normalize-space($LastName)"/>
                    </xsl:when>
                    <xsl:when test="($LastName_2 != '')">
                        <xsl:value-of select="normalize-space($LastName_2)"/>
                    </xsl:when>
                </xsl:choose>
            </kitodo:metadata>
            <kitodo:metadata name="FirstName">
                <xsl:choose>
                    <xsl:when test="($FirstName != '')">
                        <xsl:value-of select="normalize-space($FirstName)"/>
                    </xsl:when>
                    <xsl:when test="($FirstName_2 != '')">
                        <xsl:value-of select="normalize-space($FirstName_2)"/>
                    </xsl:when>
                </xsl:choose>
            </kitodo:metadata>
            <kitodo:metadata name="DisplayForm">
                <xsl:choose>
                    <xsl:when test="(boolean($LastName) = boolean($FirstName)) and ($LastName != '')">
                        <xsl:value-of select="normalize-space(concat($LastName,', ',$FirstName))"/>
                    </xsl:when>
                    <xsl:when test="boolean($LastName) != boolean($FirstName) and ($LastName != '')">
                        <xsl:value-of select="normalize-space($LastName)"/>
                    </xsl:when>
                    <xsl:when test="boolean($FirstName) != boolean($LastName) and ($FirstName != '')">
                        <xsl:value-of select="normalize-space($FirstName)"/>
                    </xsl:when>
                    <xsl:when test="(boolean($PersonalName) = boolean($Count)) and ($PersonalName != '')">
                        <xsl:value-of select="concat($PersonalName, ' ',$Count,', ',$AdditionalMetadataPerson)"/>
                    </xsl:when>
                    <xsl:when
                            test="(boolean($PersonalName) != boolean($Count)) and (boolean($AdditionalMetadataPerson))">
                        <xsl:value-of select="concat($PersonalName,', ',$AdditionalMetadataPerson)"/>
                    </xsl:when>
                    <xsl:when
                            test="(boolean($PersonalName) != boolean($Count)) and (boolean($PersonalName) != boolean($AdditionalMetadataPerson))">
                        <xsl:value-of select="$PersonalName"/>
                    </xsl:when>
                </xsl:choose>
            </kitodo:metadata>
            <kitodo:metadata name="PersonalName">
                <xsl:value-of select="normalize-space($PersonalName)"/>
            </kitodo:metadata>
            <kitodo:metadata name="AdditionalMetadataPerson">
                <xsl:value-of select="normalize-space($AdditionalMetadataPerson)"/>
            </kitodo:metadata>
            <kitodo:metadata name="Count">
                <xsl:value-of select="normalize-space($Count)"/>
            </kitodo:metadata>
        </kitodo:metadataGroup>
    </xsl:template>

    <!-- ### Sonstige Person/Familie (NICHT MIT BEISPIELEN GEPRÜFT!!!) ### -->
    <xsl:template match="pica:datafield[@tag='028G']">
        <xsl:variable name="IdentifierPPN" select="pica:subfield[@code='9']"/>
        <xsl:variable name="IdentifierGND" select="pica:subfield[@code='7']"/>
        <xsl:variable name="IdentifierGNDURI" select="pica:subfield[@code='7']"/>
        <xsl:variable name="Authority" select="pica:subfield[@code='7']"/>
        <xsl:variable name="RoleCode" select="pica:subfield[@code='4']"/>
        <xsl:variable name="RoleText" select="pica:subfield[@code='B']"/>
        <xsl:variable name="LastName" select="pica:subfield[@code='A']"/>
        <xsl:variable name="LastName_2" select="pica:subfield[@code='a']"/>
        <xsl:variable name="FirstName" select="pica:subfield[@code='D']"/>
        <xsl:variable name="FirstName_2" select="pica:subfield[@code='d']"/>
        <xsl:variable name="PersonalName" select="pica:subfield[@code='P']"/>
        <xsl:variable name="AdditionalMetadataPerson" select="pica:subfield[@code='L']"/>
        <xsl:variable name="Count" select="pica:subfield[@code='N']"/>
        <kitodo:metadataGroup name="ContributorPersonOther">
            <kitodo:metadata name="IdentifierPPN">
                <xsl:value-of select="normalize-space($IdentifierPPN)"/>
            </kitodo:metadata>
            <kitodo:metadata name="IdentifierGND">
                <xsl:choose>
                    <xsl:when test="$IdentifierGND != ''">
                        <xsl:value-of select="normalize-space(substring-after($IdentifierGND, 'gnd/'))"/>
                    </xsl:when>
                </xsl:choose>
            </kitodo:metadata>
            <kitodo:metadata name="IdentifierGNDURI">
                <xsl:choose>
                    <xsl:when test="$IdentifierGNDURI != ''">
                        <xsl:value-of select="normalize-space(concat('http://d-nb.info/',$IdentifierGND))"/>
                    </xsl:when>
                </xsl:choose>
            </kitodo:metadata>
            <kitodo:metadata name="Authority">
                <xsl:choose>
                    <xsl:when test="$Authority != ''">
                        <xsl:value-of select="normalize-space(substring-before($IdentifierGND, '/'))"/>
                    </xsl:when>
                </xsl:choose>
            </kitodo:metadata>
            <xsl:for-each select="$RoleCode">
                <kitodo:metadata name="RoleCode">
                    <xsl:value-of select="normalize-space(.)"/>
                </kitodo:metadata>
            </xsl:for-each>
            <xsl:for-each select="$RoleText">
                <kitodo:metadata name="RoleText">
                    <xsl:value-of select="normalize-space(.)"/>
                </kitodo:metadata>
            </xsl:for-each>
            <kitodo:metadata name="LastName">
                <xsl:choose>
                    <xsl:when test="($LastName != '')">
                        <xsl:value-of select="normalize-space($LastName)"/>
                    </xsl:when>
                    <xsl:when test="($LastName_2 != '')">
                        <xsl:value-of select="normalize-space($LastName_2)"/>
                    </xsl:when>
                </xsl:choose>
            </kitodo:metadata>
            <kitodo:metadata name="FirstName">
                <xsl:choose>
                    <xsl:when test="($FirstName != '')">
                        <xsl:value-of select="normalize-space($FirstName)"/>
                    </xsl:when>
                    <xsl:when test="($FirstName_2 != '')">
                        <xsl:value-of select="normalize-space($FirstName_2)"/>
                    </xsl:when>
                </xsl:choose>
            </kitodo:metadata>
            <kitodo:metadata name="DisplayForm">
                <xsl:choose>
                    <xsl:when test="(boolean($LastName) = boolean($FirstName)) and ($LastName != '')">
                        <xsl:value-of select="normalize-space(concat($LastName,', ',$FirstName))"/>
                    </xsl:when>
                    <xsl:when test="boolean($LastName) != boolean($FirstName) and ($LastName != '')">
                        <xsl:value-of select="normalize-space($LastName)"/>
                    </xsl:when>
                    <xsl:when test="(boolean($PersonalName) = boolean($Count)) and ($PersonalName != '')">
                        <xsl:value-of select="concat($PersonalName, ' ',$Count,', ',$AdditionalMetadataPerson)"/>
                    </xsl:when>
                    <xsl:when
                            test="(boolean($PersonalName) != boolean($Count)) and (boolean($AdditionalMetadataPerson))">
                        <xsl:value-of select="concat($PersonalName,', ',$AdditionalMetadataPerson)"/>
                    </xsl:when>
                    <xsl:when
                            test="(boolean($PersonalName) != boolean($Count)) and (boolean($PersonalName) != boolean($AdditionalMetadataPerson))">
                        <xsl:value-of select="$PersonalName"/>
                    </xsl:when>
                </xsl:choose>
            </kitodo:metadata>
            <kitodo:metadata name="PersonalName">
                <xsl:value-of select="normalize-space($PersonalName)"/>
            </kitodo:metadata>
            <kitodo:metadata name="AdditionalMetadataPerson">
                <xsl:value-of select="normalize-space($AdditionalMetadataPerson)"/>
            </kitodo:metadata>
            <kitodo:metadata name="Count">
                <xsl:value-of select="normalize-space($Count)"/>
            </kitodo:metadata>
        </kitodo:metadataGroup>
    </xsl:template>

    <!-- ### Körperschaft als 1. geistiger Schöpfer ### -->
    <xsl:template match="pica:datafield[@tag='029A']">
        <xsl:variable name="IdentifierPPN" select="pica:subfield[@code='9']"/>
        <xsl:variable name="IdentifierGND" select="pica:subfield[@code='7']"/>
        <xsl:variable name="IdentifierGNDURI" select="pica:subfield[@code='7']"/>
        <xsl:variable name="Authority" select="pica:subfield[@code='7']"/>
        <xsl:variable name="RoleCode" select="pica:subfield[@code='4']"/>
        <xsl:variable name="RoleText" select="pica:subfield[@code='B']"/>
        <xsl:variable name="MainUnit" select="pica:subfield[@code='A']"/>
        <xsl:variable name="SubordinateUnit" select="pica:subfield[@code='F']"/>
        <kitodo:metadataGroup name="Corporation">
            <kitodo:metadata name="IdentifierPPN">
                <xsl:value-of select="normalize-space($IdentifierPPN)"/>
            </kitodo:metadata>
            <kitodo:metadata name="IdentifierGND">
                <xsl:choose>
                    <xsl:when test="$IdentifierGND != ''">
                        <xsl:value-of select="normalize-space(substring-after($IdentifierGND, 'gnd/'))"/>
                    </xsl:when>
                </xsl:choose>
            </kitodo:metadata>
            <kitodo:metadata name="IdentifierGNDURI">
                <xsl:choose>
                    <xsl:when test="$IdentifierGNDURI != ''">
                        <xsl:value-of select="normalize-space(concat('http://d-nb.info/',$IdentifierGND))"/>
                    </xsl:when>
                </xsl:choose>
            </kitodo:metadata>
            <kitodo:metadata name="Authority">
                <xsl:choose>
                    <xsl:when test="$Authority != ''">
                        <xsl:value-of select="normalize-space(substring-before($IdentifierGND, '/'))"/>
                    </xsl:when>
                </xsl:choose>
            </kitodo:metadata>
            <xsl:for-each select="$RoleCode">
                <kitodo:metadata name="RoleCode">
                    <xsl:value-of select="normalize-space(.)"/>
                </kitodo:metadata>
            </xsl:for-each>
            <xsl:for-each select="$RoleText">
                <kitodo:metadata name="RoleText">
                    <xsl:value-of select="normalize-space(.)"/>
                </kitodo:metadata>
            </xsl:for-each>
            <kitodo:metadata name="MainUnit">
                <xsl:value-of select="normalize-space($MainUnit)"/>
            </kitodo:metadata>
            <xsl:for-each select="$SubordinateUnit">
                <kitodo:metadata name="SubordinateUnit">
                    <xsl:value-of select="normalize-space(.)"/>
                </kitodo:metadata>
            </xsl:for-each>
            <kitodo:metadata name="DisplayForm">
                <xsl:choose>
                    <xsl:when test="boolean($MainUnit) != boolean($SubordinateUnit)">
                        <xsl:value-of select="normalize-space($MainUnit)"/>
                    </xsl:when>
                    <xsl:when test="boolean($MainUnit) = boolean($SubordinateUnit)">
                        <xsl:value-of select="(normalize-space($MainUnit),'. ',($SubordinateUnit))"/>
                    </xsl:when>
                </xsl:choose>
            </kitodo:metadata>
        </kitodo:metadataGroup>
    </xsl:template>

    <!-- ### Körperschaft als 2. und weiterer geistiger Schöpfer ### -->
    <xsl:template match="pica:datafield[@tag='029F']">
        <xsl:variable name="IdentifierPPN" select="pica:subfield[@code='9']"/>
        <xsl:variable name="IdentifierGND" select="pica:subfield[@code='7']"/>
        <xsl:variable name="IdentifierGNDURI" select="pica:subfield[@code='7']"/>
        <xsl:variable name="Authority" select="pica:subfield[@code='7']"/>
        <xsl:variable name="RoleCode" select="pica:subfield[@code='4']"/>
        <xsl:variable name="RoleText" select="pica:subfield[@code='B']"/>
        <xsl:variable name="MainUnit" select="pica:subfield[@code='A']"/>
        <xsl:variable name="SubordinateUnit" select="pica:subfield[@code='F']"/>
        <kitodo:metadataGroup name="ContributorCorporation">
            <kitodo:metadata name="IdentifierPPN">
                <xsl:value-of select="normalize-space($IdentifierPPN)"/>
            </kitodo:metadata>
            <kitodo:metadata name="IdentifierGND">
                <xsl:choose>
                    <xsl:when test="$IdentifierGND != ''">
                        <xsl:value-of select="normalize-space(substring-after($IdentifierGND, 'gnd/'))"/>
                    </xsl:when>
                </xsl:choose>
            </kitodo:metadata>
            <kitodo:metadata name="IdentifierGNDURI">
                <xsl:choose>
                    <xsl:when test="$IdentifierGNDURI != ''">
                        <xsl:value-of select="normalize-space(concat('http://d-nb.info/',$IdentifierGND))"/>
                    </xsl:when>
                </xsl:choose>
            </kitodo:metadata>
            <kitodo:metadata name="Authority">
                <xsl:choose>
                    <xsl:when test="$Authority != ''">
                        <xsl:value-of select="normalize-space(substring-before($IdentifierGND, '/'))"/>
                    </xsl:when>
                </xsl:choose>
            </kitodo:metadata>
            <xsl:for-each select="$RoleCode">
                <kitodo:metadata name="RoleCode">
                    <xsl:value-of select="normalize-space(.)"/>
                </kitodo:metadata>
            </xsl:for-each>
            <xsl:for-each select="$RoleText">
                <kitodo:metadata name="RoleText">
                    <xsl:value-of select="normalize-space(.)"/>
                </kitodo:metadata>
            </xsl:for-each>
            <kitodo:metadata name="MainUnit">
                <xsl:value-of select="normalize-space($MainUnit)"/>
            </kitodo:metadata>
            <xsl:for-each select="$SubordinateUnit">
                <kitodo:metadata name="SubordinateUnit">
                    <xsl:value-of select="normalize-space(.)"/>
                </kitodo:metadata>
            </xsl:for-each>
            <kitodo:metadata name="DisplayForm">
                <xsl:choose>
                    <xsl:when test="boolean($MainUnit) != boolean($SubordinateUnit)">
                        <xsl:value-of select="normalize-space($MainUnit)"/>
                    </xsl:when>
                    <xsl:when test="boolean($MainUnit) = boolean($SubordinateUnit)">
                        <xsl:value-of select="normalize-space(concat($MainUnit,'. ',$SubordinateUnit))"/>
                    </xsl:when>
                </xsl:choose>
            </kitodo:metadata>
        </kitodo:metadataGroup>
    </xsl:template>

    <!-- pass-through rule -->
    <xsl:template match="@*|node()">
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
</xsl:stylesheet>
