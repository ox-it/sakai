<?xml version="1.0" encoding="UTF-8"?>
  <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:media="http://search.yahoo.com/mrss/"    
  version="1.0"
   >
   
   <xsl:param name="proxy"></xsl:param>
   <xsl:variable name="width">80</xsl:variable>
   <xsl:variable name="height">80</xsl:variable>
   
    <xsl:template match="media:thumbnail">
      <media:thumbnail>
        <xsl:attribute name="src">
          <!-- TODO This doesn't work as the XSLT process doesn't like passed parameters as instance calls. -->
          <xsl:value-of select="proxy:getProxyUrl($proxy, string(.))"
            xmlns:proxy="java:uk.ac.ox.oucs.vle.SimpleProxyService" />
        </xsl:attribute>
        <xsl:attribute name="width">
          <xsl:value-of select="$width"/>
        </xsl:attribute>
        <xsl:attribute name="height">
          <xsl:value-of select="$height"/>
        </xsl:attribute>
      </media:thumbnail>
    </xsl:template>
 
  <xsl:template match="*|@*|node()">
    <xsl:copy >
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>