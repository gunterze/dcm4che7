<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="text"/>
  <xsl:template match="/uids">
    <xsl:text>
# This file is generated from Part 6 of the Standard Text Edition 2021a.
#
</xsl:text>
    <xsl:apply-templates select="uid" />
  </xsl:template>
  <xsl:template match="uid">
    <xsl:value-of select="@value" />
    <xsl:text>=</xsl:text>
    <xsl:value-of select="text()" />
    <xsl:text>
</xsl:text>
  </xsl:template>
</xsl:stylesheet>