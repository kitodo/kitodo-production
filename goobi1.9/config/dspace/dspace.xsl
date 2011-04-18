<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version='1.0'
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform' xmlns:mets="http://www.loc.gov/METS/"
	xmlns:mods="http://www.loc.gov/mods/v3" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:goobi="http://meta.goobi.org/v1.5.1/">

	<xsl:template match="/">
		<dublin_core>
			<xsl:apply-templates />
		</dublin_core>
	</xsl:template>

	<xsl:template match="//mets:dmdSec[@ID='DMDLOG_0000']//mods:extension/goobi:goobi">
		<xsl:for-each select="goobi:metadata">
			<xsl:if test="@name ='TitleDocMain'">
				<dcvalue qualifier="none" element="title">
					<xsl:value-of select="." />
				</dcvalue>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	</xsl:stylesheet> 