<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:goobi="http://meta.goobi.org/v1.5.1/"
                xmlns:kitodo="http://meta.kitodo.org/v1/"
                xmlns:ext="http://exslt.org/common">
    <xsl:output method="xml" indent="yes" encoding="UTF-8"/>
    <xsl:strip-space elements="*"/>

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
        <kitodo:metadata>
            <xsl:attribute name="type">group</xsl:attribute>
            <xsl:attribute name="name">
                <xsl:value-of select="@name"/>
            </xsl:attribute>
            <xsl:for-each select="node()">
                <kitodo:metadata>
                    <xsl:attribute name="name">
                        <xsl:value-of select="local-name()"/>
                    </xsl:attribute>
                    <xsl:value-of select="current()"/>
                </kitodo:metadata>
            </xsl:for-each>
        </kitodo:metadata>
    </xsl:template>
</xsl:stylesheet>