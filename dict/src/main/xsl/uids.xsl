<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:doc="http://docbook.org/ns/docbook"
    exclude-result-prefixes="doc">
  <xsl:output method="xml" indent="yes"/>

  <xsl:variable name="LOWER">abcdefghijklmnopqrstuvwxyz</xsl:variable>
  <xsl:variable name="UPPER">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>

  <xsl:template match="/doc:book">
    <uids>
      <xsl:apply-templates select="doc:chapter[12]/doc:table[1]/doc:tbody/doc:tr"/>
    </uids>
  </xsl:template>

  <xsl:template match="doc:tr">
    <xsl:element name="uid">
      <xsl:variable name="uid">
        <xsl:call-template name="para2str">
          <xsl:with-param name="para" select="doc:td[1]/doc:para"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:attribute name="value">
        <xsl:value-of select="$uid"/>
      </xsl:attribute>
      <xsl:variable name="name">
        <xsl:choose>
          <xsl:when test="$uid='1.2.840.10008.5.1.4.1.1.40'">MR Image Storage Zero Padded (Retired)</xsl:when>
          <xsl:when test="$uid='1.2.840.10008.5.1.4.1.1.12.77'">Zeiss OPT File (Retired)</xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="skipAfterColon">
              <xsl:with-param name="name">
                <xsl:call-template name="para2str">
                  <xsl:with-param name="para" select="doc:td[2]/doc:para"/>
                </xsl:call-template>
              </xsl:with-param>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:attribute name="keyword">
        <xsl:choose>
          <xsl:when test="$uid='1.2.840.10008.5.1.4.1.1.40'">MRImageStorageZeroPadded</xsl:when>
          <xsl:when test="$uid='1.2.840.10008.5.1.4.1.1.12.77'">ZeissOPTFile</xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="para2str">
              <xsl:with-param name="para" select="doc:td[3]/doc:para" />
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <xsl:attribute name="type">
        <xsl:call-template name="removeSpaces">
          <xsl:with-param name="name">
            <xsl:call-template name="replaceNonAlpha">
              <xsl:with-param name="name">
                <xsl:call-template name="para2str">
                  <xsl:with-param name="para" select="doc:td[4]/doc:para"/>
                </xsl:call-template>
              </xsl:with-param>
            </xsl:call-template>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:attribute>
      <xsl:value-of select="$name"/>
    </xsl:element>
  </xsl:template>

  <xsl:template name="para2str">
    <xsl:param name="para"/>
    <xsl:variable name="str">
      <xsl:variable name="emphasis" select="$para/doc:emphasis"/>
      <xsl:choose>
        <xsl:when test="$emphasis">
          <xsl:value-of select="$emphasis/text()"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$para/text()"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:value-of select="translate($str,'&#x200b;&#xad;&#8203;','')"/>
  </xsl:template>

  <xsl:template name="skipAfterColon">
    <xsl:param name="name"/>
    <xsl:variable name="before" select="substring-before($name,':')" />
    <xsl:choose>
      <xsl:when test="$before">
        <xsl:value-of select="normalize-space($before)" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="normalize-space($name)" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="replaceNonAlpha">
    <xsl:param name="name"/>
    <xsl:value-of select="normalize-space(translate($name,'-,.@/()&amp;®','        '))"/>
  </xsl:template>

  <xsl:template name="removeSpaces">
    <xsl:param name="name"/>
    <xsl:variable name="after" select="substring-after($name, ' ')"/>
    <xsl:choose>
      <xsl:when test="$after">
        <xsl:value-of select="substring-before($name, ' ')"/>
        <xsl:value-of select="translate(substring($after,1,1),$LOWER,$UPPER)"/>
        <xsl:call-template name="removeSpaces">
          <xsl:with-param name="name" select="substring($after,2)"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$name"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>