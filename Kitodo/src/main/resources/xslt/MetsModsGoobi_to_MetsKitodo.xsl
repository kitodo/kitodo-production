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
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mets="http://www.loc.gov/METS/"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:goobi="http://meta.goobi.org/v1.5.1/"
                xmlns:kitodo="http://meta.kitodo.org/v1/"
                xmlns:ext="http://exslt.org/common"
                xmlns:xlink="http://www.w3.org/1999/xlink">
    <xsl:output method="xml" indent="yes" encoding="UTF-8"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="/*">
        <xsl:copy>
            <xsl:namespace name="mets">
                <xsl:value-of select="'http://www.loc.gov/METS/'"/>
            </xsl:namespace>
            <xsl:namespace name="mods">
                <xsl:value-of select="'http://www.loc.gov/mods/v3'"/>
            </xsl:namespace>
            <xsl:namespace name="goobi">
                <xsl:value-of select="'http://meta.goobi.org/v1.5.1/'"/>
            </xsl:namespace>
            <xsl:namespace name="kitodo">
                <xsl:value-of select="'http://meta.kitodo.org/v1/'"/>
            </xsl:namespace>
            <xsl:namespace name="ext">
                <xsl:value-of select="'http://exslt.org/common'"/>
            </xsl:namespace>
            <xsl:namespace name="xlink">
                <xsl:value-of select="'http://www.w3.org/1999/xlink'"/>
            </xsl:namespace>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <!--This is for storing the result of first transformation-->

    <xsl:template match="/">
        <xsl:variable name="pass1Result">
            <xsl:apply-templates/>
        </xsl:variable>
        <xsl:apply-templates mode="pass2" select="ext:node-set($pass1Result)/*"/>
    </xsl:template>


    <!--Transformation pass 1-->

    <!-- This is an identity template - it copies everything that doesn't match another template -->
    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- This replaces elements mods/extension/goobi with kitodo -->
    <xsl:template match="mods:mods">
        <kitodo:kitodo version="1.0">
            <xsl:copy-of select="mods:extension/goobi:goobi/node()"/>
        </kitodo:kitodo>
    </xsl:template>

    <!-- This replaces the mdWrap attribute mdtype -->
    <xsl:template match="mets:mdWrap">
        <mets:mdWrap MDTYPE="OTHER" OTHERMDTYPE="KITODO">
            <xsl:apply-templates select="node()"/>
        </mets:mdWrap>
    </xsl:template>

    <xsl:template match="/mets:mets/mets:fileSec/mets:fileGrp[@USE='LOCAL']/mets:file/mets:FLocat/@xlink:href">
        <xsl:attribute name="xlink:href">
            <xsl:value-of select="translate(., ' [!', '')"/>
        </xsl:attribute>
    </xsl:template>

    <xsl:template match="/mets:mets/mets:fileSec/mets:fileGrp[@USE='LOCAL']/mets:file/@MIMETYPE[.='']">
        <xsl:attribute name="MIMETYPE">image/tiff</xsl:attribute>
    </xsl:template>

    <!--Transformation pass 2-->

    <!-- This is an identity template - it copies everything that doesn't match another template -->
    <xsl:template match="@* | node()" mode="pass2">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()" mode="pass2"/>
        </xsl:copy>
    </xsl:template>

    <!-- This replaces namespace url of metadata elements -->
    <xsl:template match="goobi:metadata" mode="pass2">
        <kitodo:metadata>
            <xsl:apply-templates select="@* | node()" mode="pass2"/>
        </kitodo:metadata>
    </xsl:template>

    <!--This replaces the person metadata element by metadata group element-->
    <xsl:template match="goobi:metadata[@type='person']" mode="pass2">
        <kitodo:metadataGroup>
            <xsl:attribute name="name">Person</xsl:attribute>
            <xsl:if test="./goobi:firstName and string(./goobi:firstName)">
                <kitodo:metadata name="FirstName">
                    <xsl:value-of select="./goobi:firstName"/>
                </kitodo:metadata>
            </xsl:if>
            <xsl:if test="./goobi:lastName and string(./goobi:lastName)">
                <kitodo:metadata name="LastName">
                    <xsl:value-of select="./goobi:lastName"/>
                </kitodo:metadata>
            </xsl:if>
            <xsl:if test="./goobi:displayName and string(./goobi:displayName)">
                <kitodo:metadata name="DisplayForm">
                    <xsl:value-of select="./goobi:displayName"/>
                </kitodo:metadata>
            </xsl:if>
            <xsl:if test="./goobi:authorityID and string(./goobi:authorityID)">
                <kitodo:metadata name="Authority">
                    <xsl:value-of select="./goobi:authorityID"/>
                </kitodo:metadata>
            </xsl:if>
            <xsl:if test="./goobi:authorityValue and string(./goobi:authorityValue) and substring-after(./goobi:authorityValue, 'http://d-nb.info/gnd/') != ''">
                <kitodo:metadata name="IdentifierGNDURI">
                    <xsl:value-of select="./goobi:authorityValue"/>
                </kitodo:metadata>
                <kitodo:metadata name="IdentifierGND">
                    <xsl:value-of select="substring-after(./goobi:authorityValue, 'http://d-nb.info/gnd/')" />
                </kitodo:metadata>
            </xsl:if>
            <xsl:choose>
                <xsl:when test="@name = 'Creator'">
                    <kitodo:metadata name="RoleCode">cre</kitodo:metadata>
                </xsl:when>
                <xsl:when test="@name = 'slub_actor'">
                    <kitodo:metadata name="RoleCode">act</kitodo:metadata>
                </xsl:when>
                <xsl:when test="@name = 'slub_depicted'">
                    <kitodo:metadata name="RoleCode">dpc</kitodo:metadata>
                </xsl:when>
                <xsl:when test="@name = 'slub_etcher'">
                    <kitodo:metadata name="RoleCode">etr</kitodo:metadata>
                </xsl:when>
                <xsl:when test="@name = 'slub_woodcutter'">
                    <kitodo:metadata name="RoleCode">wdc</kitodo:metadata>
                </xsl:when>
                <xsl:when test="@name = 'slub_printmaker'">
                    <kitodo:metadata name="RoleCode">prm</kitodo:metadata>
                </xsl:when>
                <xsl:when test="@name = 'Photographer'">
                    <kitodo:metadata name="RoleCode">pht</kitodo:metadata>
                </xsl:when>
                <xsl:when test="@name = 'slub_DedicationRecipient'">
                    <kitodo:metadata name="RoleCode">dte</kitodo:metadata>
                </xsl:when>
                <xsl:when test="@name = 'slub_speaker'">
                    <kitodo:metadata name="RoleCode">spk</kitodo:metadata>
                </xsl:when>
                <xsl:when test="@name = 'Originator'">
                    <kitodo:metadata name="RoleCode">org</kitodo:metadata>
                </xsl:when>
                <xsl:when test="@name = 'slub_coverdesigner'">
                    <kitodo:metadata name="RoleCode">cov</kitodo:metadata>
                </xsl:when>
                <xsl:when test="@name = 'slub_engraver'">
                    <kitodo:metadata name="RoleCode">egr</kitodo:metadata>
                </xsl:when>
                <xsl:when test="@name = 'slub_Source'">
                    <kitodo:metadata name="RoleCode">ant</kitodo:metadata>
                </xsl:when>
                <xsl:when test="@name = 'slub_collector'">
                    <kitodo:metadata name="RoleCode">slub_collector</kitodo:metadata>
                </xsl:when>
                <xsl:when test="@name = 'IllustratorArtist'">
                    <kitodo:metadata name="RoleCode">ill</kitodo:metadata>
                </xsl:when>
                <xsl:when test="@name = 'slub_performer'">
                    <kitodo:metadata name="RoleCode">prf</kitodo:metadata>
                </xsl:when>
                <xsl:when test="@name = 'slub_bookdesigner'">
                    <kitodo:metadata name="RoleCode">bkd</kitodo:metadata>
                </xsl:when>
                <xsl:when test="@name = 'Translator'">
                    <kitodo:metadata name="RoleCode">trl</kitodo:metadata>
                </xsl:when>
                <xsl:when test="@name = 'Editor'">
                    <kitodo:metadata name="RoleCode">edt</kitodo:metadata>
                </xsl:when>
                <xsl:when test="@name = 'slub_client'">
                    <kitodo:metadata name="RoleCode">cli</kitodo:metadata>
                </xsl:when>
                <xsl:when test="@name = 'slub_designer'">
                    <kitodo:metadata name="RoleCode">dsr</kitodo:metadata>
                </xsl:when>
                <xsl:when test="@name = 'slub_Recipient'">
                    <kitodo:metadata name="RoleCode">rcp</kitodo:metadata>
                </xsl:when>
                <xsl:when test="@name = 'slub_architect'">
                    <kitodo:metadata name="RoleCode">arc</kitodo:metadata>
                </xsl:when>
                <xsl:when test="@name = 'Author'">
                    <kitodo:metadata name="RoleCode">aut</kitodo:metadata>
                </xsl:when>
                <xsl:when test="@name = 'slub_exhibitingArtist'">
                    <kitodo:metadata name="RoleCode">aqt</kitodo:metadata>
                </xsl:when>
                <xsl:when test="@name = 'slub_typedesigner'">
                    <kitodo:metadata name="RoleCode">tyd</kitodo:metadata>
                </xsl:when>
                <xsl:otherwise>
                    <kitodo:metadata name="RoleCode">oth</kitodo:metadata>
                </xsl:otherwise>
            </xsl:choose>
        </kitodo:metadataGroup>
    </xsl:template>

    <!--This replaces the metadata of type group element by metadata group element-->
    <xsl:template match="goobi:metadata[@type='group']" mode="pass2">
        <kitodo:metadataGroup>
            <xsl:attribute name="name">
                <xsl:value-of select="@name"/>
            </xsl:attribute>
            <xsl:apply-templates mode="pass2"/>
        </kitodo:metadataGroup>
    </xsl:template>

    <!-- Correct wrong pathimagefiles entries -->
    <xsl:template match="goobi:metadata[@name='pathimagefiles']" mode="pass2">
        <kitodo:metadata>
            <xsl:attribute name="name">
                <xsl:value-of select="@name"/>
            </xsl:attribute>
            <xsl:value-of select="translate(., ' [!', '')"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- Transform @name="CatalogIDDigital" @anchorId="true" by @name="CatalogIDDigitalAnchor" -->
    <xsl:template match="goobi:metadata[@name='CatalogIDDigital'][@anchorId='true']" mode="pass2">
        <kitodo:metadata>
            <xsl:attribute name="name">
                <xsl:text>CatalogIDDigitalAnchor</xsl:text>
            </xsl:attribute>
            <xsl:value-of select="." />
        </kitodo:metadata>
    </xsl:template>

    <!-- Transorm toplevel [@name='TitleDocMain']" to mets:div[@LABEL] -->
        <xsl:template match="mets:structMap[@TYPE='LOGICAL']/mets:div[@DMDID='DMDLOG_0000']/@TYPE" mode="pass2">
        <xsl:variable name="TitleDocMain" select="/mets:mets/mets:dmdSec[@ID='DMDLOG_0000']/mets:mdWrap/mets:xmlData/kitodo:kitodo/goobi:metadata[@name='TitleDocMain']"/>
        <xsl:variable name="TitleDocMainShort" select="/mets:mets/mets:dmdSec[@ID='DMDLOG_0000']/mets:mdWrap/mets:xmlData/kitodo:kitodo/goobi:metadata[@name='TitleDocMainShort']"/>
            <xsl:attribute name="LABEL">
                <xsl:value-of select="($TitleDocMain)"/>
            </xsl:attribute> 
            <xsl:attribute name="ORDERLABEL">
               <xsl:value-of select="($TitleDocMainShort)"/>
            </xsl:attribute>
            <xsl:copy>
                <xsl:apply-templates select="@* | node()"/>
            </xsl:copy>
    </xsl:template>

    <!-- Transorm year-level [@name='TitleDocMain']" to mets:div[@LABEL] -->
    <xsl:template match="mets:structMap[@TYPE='LOGICAL']/mets:div/mets:div[@TYPE='NewspaperYear']/@TYPE" mode="pass2">
        <xsl:variable name="TitleDocMainShort" select="/mets:mets/mets:dmdSec[@ID='DMDLOG_0003']/mets:mdWrap/mets:xmlData/kitodo:kitodo/goobi:metadata[@name='TitleDocMainShort']"/>
        <xsl:variable name="TitleDocMainShort2" select="/mets:mets/mets:dmdSec[@ID='DMDLOG_0001']/mets:mdWrap/mets:xmlData/kitodo:kitodo/goobi:metadata[@name='TitleDocMainShort']"/>
        <xsl:if test=". = 'NewspaperYear'">
            <xsl:attribute name="ORDERLABEL">
                <xsl:value-of select="concat($TitleDocMainShort, $TitleDocMainShort2)"/>
            </xsl:attribute>
        </xsl:if><!--
        <xsl:if test=". = 'NewspaperYear'">
            <xsl:attribute name="ORDERLABEL">
                <xsl:value-of select="($TitleDocMainShort)"/>
            </xsl:attribute>
        </xsl:if>-->
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="mets:structMap[@TYPE='LOGICAL']/mets:div/mets:div[@TYPE='PeriodicalVolume']/@TYPE" mode="pass2">
        <xsl:variable name="TitleDocMainShort" select="/mets:mets/mets:dmdSec[@ID='DMDLOG_0001']/mets:mdWrap/mets:xmlData/kitodo:kitodo/goobi:metadata[@name='CurrentNoSorting']"/>
        <xsl:if test=". = 'PeriodicalVolume'">
            <xsl:attribute name="ORDER">
                <xsl:value-of select="($TitleDocMainShort)"/>
            </xsl:attribute>
        </xsl:if>
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Transform month-level [@name='TitleDocMainShort']" to mets:div[@ORDERLABEL] -->
    <xsl:template match="mets:structMap[@TYPE='LOGICAL']/mets:div/mets:div[@TYPE='NewspaperYear']/mets:div[@TYPE='NewspaperMonth']/@TYPE" mode="pass2">
        <xsl:variable name="TitleDocMainShort" select="/mets:mets/mets:dmdSec[@ID='DMDLOG_0004']/mets:mdWrap/mets:xmlData/kitodo:kitodo/goobi:metadata[@name='TitleDocMainShort']"/>
        <xsl:attribute name="ORDERLABEL">
            <xsl:value-of select="($TitleDocMainShort)"/>
        </xsl:attribute>
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Transform day-level [@name='TitleDocMainShort']" to mets:div[@ORDERLABEL] -->
    <xsl:template match="mets:structMap[@TYPE='LOGICAL']/mets:div/mets:div[@TYPE='NewspaperYear']/mets:div[@DMDID='DMDLOG_0004']/mets:div[@TYPE='NewspaperDay']/@TYPE" mode="pass2">
        <xsl:variable name="TitleDocMainShort" select="/mets:mets/mets:dmdSec[@ID='DMDLOG_0005']/mets:mdWrap/mets:xmlData/kitodo:kitodo/goobi:metadata[@name='TitleDocMainShort']"/>
        <xsl:attribute name="ORDERLABEL">
             <xsl:value-of select="($TitleDocMainShort)"/>
        </xsl:attribute>
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

   <!-- Delete metadata "FormatSourcePrint" in <mets:dmdSec ID="DMDPHYS_0000"> -->
    <xsl:template match="mets:dmdSec[@ID='DMDPHYS_0000']/mets:mdWrap/mets:xmlData/kitodo:kitodo/goobi:metadata[@name='FormatSourcePrint']" mode="pass2">
    </xsl:template>

    <!-- Replace NewspaperYear, NewspaperMonth and NewspaperDay for Ephemera -->
    <xsl:template match="mets:div[@TYPE='Ephemera']/mets:div/@TYPE">
        <xsl:if test=". = 'NewspaperYear'">
            <xsl:attribute name="TYPE">
                <xsl:text>EphemeraYear</xsl:text>
            </xsl:attribute>
        </xsl:if>
    </xsl:template>

    <xsl:template match="mets:div[@TYPE='Ephemera']/mets:div/mets:div/@TYPE">
        <xsl:if test=". = 'NewspaperMonth'">
            <xsl:attribute name="TYPE">
                <xsl:text>EphemeraMonth</xsl:text>
            </xsl:attribute>
        </xsl:if>
    </xsl:template>

    <xsl:template match="mets:div[@TYPE='Ephemera']/mets:div/mets:div/mets:div/@TYPE">
        <xsl:if test=". = 'NewspaperDay'">
            <xsl:attribute name="TYPE">
                <xsl:text>EphemeraDay</xsl:text>
            </xsl:attribute>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
