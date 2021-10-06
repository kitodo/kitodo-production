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
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:mets="http://www.loc.gov/METS/"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:kitodo="http://meta.kitodo.org/v1/">

    <xsl:output method="xml" indent="yes" encoding="utf-8"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="mets:mets">
        <mets:mets>
            <xsl:apply-templates select="@* | node()"/>
        </mets:mets>
    </xsl:template>

    <!-- Copy existing METS structure! -->
    <!-- ### METS header -->
    <xsl:template match="mets:metsHdr">
        <mets:metsHdr>
            <xsl:copy-of select="@*"/>
            <xsl:copy-of select="node()"/>
        </mets:metsHdr>
    </xsl:template>

    <!-- ### METS fileSec -->
    <xsl:template match="mets:fileSec">
        <mets:fileSec>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="mets:fileGrp"/>
        </mets:fileSec>
    </xsl:template>

    <!-- ### METS fileGrp -->
    <xsl:template match="mets:fileGrp">
        <mets:fileGrp>
            <xsl:copy-of select="@*"/>
            <xsl:copy-of select="node()"/>
        </mets:fileGrp>
    </xsl:template>

    <!-- ### METS structMaps -->
    <xsl:template match="mets:structMap">
        <mets:structMap>
            <xsl:copy-of select="@*"/>
            <xsl:copy-of select="node()"/>
        </mets:structMap>
    </xsl:template>

    <!-- ### METS structLink -->
    <xsl:template match="mets:structLink">
        <mets:structLink>
            <xsl:copy-of select="@*"/>
            <xsl:copy-of select="node()"/>
        </mets:structLink>
    </xsl:template>

    <!-- ### METS dmdSec -->
    <xsl:template match="mets:dmdSec">
        <mets:dmdSec>
            <!-- copy original ID of DMDSection! -->
            <xsl:copy-of select="@*"/>
            <!-- create required METS nodes -->
            <mets:mdWrap>
                <mets:xmlData>
                    <!-- map existing MODS metadata to internal format -->
                    <xsl:apply-templates select="mets:mdWrap/mets:xmlData/mods:mods"/>
                </mets:xmlData>
            </mets:mdWrap>
        </mets:dmdSec>
    </xsl:template>

    <!-- ### map MODS metadata to Kitodo metadata -->
    <xsl:template match="mods:mods">
        <kitodo:kitodo>
            <xsl:apply-templates select="@*|node()"/>
            <!-- ### DocType ### -->
            <kitodo:metadata name="docType">
                <xsl:variable name="genre" select="mods:genre[@authority='gnd-content']"/>
                <xsl:choose>
                    <xsl:when test="(mods:originInfo/mods:issuance[.='continuing'])
                                or  (mods:originInfo/mods:issuance[.='serial'])">
                        <xsl:choose>
                            <xsl:when test="$genre = 'Zeitschrift'">
                                <xsl:text>periodical</xsl:text>
                            </xsl:when>
                            <xsl:when test="$genre = 'Zeitung'">
                                <xsl:text>newspaper</xsl:text>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>periodical</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:when
                            test="((mods:relatedItem/mods:identifier[@type='localparentid']) or (mods:relatedItem[@type='host']))">
                        <xsl:choose>
                            <xsl:when test="((mods:originInfo/mods:issuance[.='monographic'])
                                          or (mods:originInfo/mods:issuance[.='integrating resource'])
                                          or (mods:originInfo/mods:issuance[.='single unit'])
                                          or (mods:typeOfResource[@manuscript='yes'][.='text']))">
                                <xsl:text>volume</xsl:text>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>multivolume_work</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:choose>
                            <xsl:when
                                    test="(mods:originInfo/mods:issuance[.='monographic']) and (mods:genre[@authoriy='gnd-content'][.='Altkarte'])">
                                <xsl:text>map</xsl:text>
                            </xsl:when>
                            <xsl:when
                                    test="(mods:originInfo/mods:issuance[.='monographic']) or (mods:originInfo/mods:issuance[.='integrating resource'])">
                                <xsl:text>monograph</xsl:text>
                            </xsl:when>
                            <xsl:when test="mods:originInfo/mods:issuance[.='single unit']">
                                <xsl:text>volume</xsl:text>
                            </xsl:when>
                            <xsl:when test="(mods:originInfo/mods:issuance[.='multipart monograph'])">
                                <xsl:text>multivolume_work</xsl:text>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>monograph</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:otherwise>
                </xsl:choose>
            </kitodo:metadata>
        </kitodo:kitodo>
    </xsl:template>

    <!-- ### Map MODS fields to Kitodo metadata with MODS node name without MODS prefix ### -->

    <!-- Create "kitodo:metadata" elements for MODS leaves without attributes or attributes of MODS leaf nodes -->
    <!-- MODS leaf nodes without attributes -->
    <xsl:template match="//mods:mods//*[not(*) and not(@*) and not(local-name(.)='topic'
                                      or local-name(.)='note'
                                      or local-name(.)='part'
                                      or local-name(.)='name'
                                      or local-name(.)='namePart'
                                      or local-name(.)='dateCreated'
                                      or local-name(.)='dateIssued'
                                      or local-name(.)='placeTerm'
                                      or local-name(.)='scriptTerm'
                                      or local-name(.)='roleTerm'
                                      or local-name(.)='relatedItem'
                                      or local-name(.)='geographic'
                                      or local-name(.)='temporal'
                                      or local-name(.)='titleInfo'
                                      or local-name(.)='classification'
                                      or local-name(.)='recordIdentifier'
                                      or local-name(.)='identifier'
                                      or local-name(.)='physicalLocation'
                                      or local-name(.)='languageTerm'
                                      or local-name(.)='accessCondition'
                                      or local-name(.)='url')]">
        <kitodo:metadata name="{local-name(.)}">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- attributes of MODS leaf nodes -->
    <xsl:template match="//mods:mods//@*[not(*) and local-name(.)!='schemaLocation']">
        <kitodo:metadata name="{local-name(.)}">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- Create "kitodo:metadataGroup" elements for MODS non-leaf nodes or leaf nodes with attributes -->
    <!-- MODS non-leaf nodes-->
    <xsl:template match="//mods:mods//*[child::* and not(local-name()='recordInfo')]">
        <kitodo:metadataGroup name="{local-name(.)}">
            <xsl:apply-templates/>
        </kitodo:metadataGroup>
    </xsl:template>

    <!-- MODS nodes with attributes (leaves and non-leaves)-->
    <xsl:template match="//mods:mods//*[(local-name(.)='topic'
                                      or local-name(.)='note'
                                      or local-name(.)='part'
                                      or local-name(.)='name'
                                      or local-name(.)='namePart'
                                      or local-name(.)='dateCreated'
                                      or local-name(.)='dateIssued'
                                      or local-name(.)='placeTerm'
                                      or local-name(.)='scriptTerm'
                                      or local-name(.)='roleTerm'
                                      or local-name(.)='relatedItem'
                                      or local-name(.)='geographic'
                                      or local-name(.)='temporal'
                                      or local-name(.)='titleInfo'
                                      or local-name(.)='classification'
                                      or local-name(.)='identifier'
                                      or local-name(.)='physicalLocation'
                                      or local-name(.)='languageTerm'
                                      or local-name(.)='accessCondition'
                                      or local-name(.)='url')
                                      or (not(*) and(@*))]">

        <kitodo:metadataGroup name="{local-name(.)}">
            <xsl:if test="(local-name(.)='topic'
                                      or local-name(.)='note'
                                      or local-name(.)='part'
                                      or local-name(.)='name'
                                      or local-name(.)='namePart'
                                      or local-name(.)='dateCreated'
                                      or local-name(.)='dateIssued'
                                      or local-name(.)='placeTerm'
                                      or local-name(.)='scriptTerm'
                                      or local-name(.)='roleTerm'
                                      or local-name(.)='relatedItem'
                                      or local-name(.)='geographic'
                                      or local-name(.)='temporal'
                                      or local-name(.)='titleInfo'
                                      or local-name(.)='classification'
                                      or local-name(.)='identifier'
                                      or local-name(.)='physicalLocation'
                                      or local-name(.)='languageTerm'
                                      or local-name(.)='accessCondition'
                                      or local-name(.)='url')">
                <xsl:if test="normalize-space(./text()) != ''">
                    <kitodo:metadata name="value"><xsl:value-of select="normalize-space(./text())"/></kitodo:metadata>
                </xsl:if>
                <xsl:apply-templates select="@*"/>
                <xsl:apply-templates />
            </xsl:if>
            <xsl:if test="not(local-name(.)='topic'
                                      or local-name(.)='note'
                                      or local-name(.)='part'
                                      or local-name(.)='name'
                                      or local-name(.)='namePart'
                                      or local-name(.)='dateCreated'
                                      or local-name(.)='dateIssued'
                                      or local-name(.)='placeTerm'
                                      or local-name(.)='scriptTerm'
                                      or local-name(.)='roleTerm'
                                      or local-name(.)='relatedItem'
                                      or local-name(.)='geographic'
                                      or local-name(.)='temporal'
                                      or local-name(.)='titleInfo'
                                      or local-name(.)='classification'
                                      or local-name(.)='identifier'
                                      or local-name(.)='physicalLocation'
                                      or local-name(.)='accessCondition'
                                      or local-name(.)='languageTerm'
                                      or local-name(.)='url')">
                <xsl:apply-templates select="@*"/>
                <xsl:apply-templates />
            </xsl:if>
        </kitodo:metadataGroup>
    </xsl:template>

    <!-- ### Allegro ID used for import (equals the searched ID) - SRU-MODS -->
    <xsl:template match="//mods:mods//mods:recordIdentifier[@source='allegro']">
        <kitodo:metadata name="allegroId">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### MODS Access Condition mapped to Kitodo Access Restriction -->
    <xsl:template match="//mods:mods//mods:accessCondition[@type='restriction on access']">
        <xsl:if test="normalize-space(./text())='Metadata Only Access'">
          <kitodo:metadata name="accessRestriction">restricted</kitodo:metadata>
        </xsl:if>
    </xsl:template>

    <!-- ### Ignore unmapped text ### -->
    <xsl:template match="text()"/>

    <!-- ### Ignore MODS schema location (as it is not required in the internal format) ### -->
    <xsl:template match="@xsi:schemaLocation"/>

</xsl:stylesheet>
