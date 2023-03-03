<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" 
    xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
    xmlns:xpe="http://com.sitc.xml.processing.engine.org/config"
    exclude-result-prefixes="#all"
    version="3.0">

    <xsl:param name="xpe:input-file-uri" as="xs:string"/>
  
  
  <xd:doc>
    <xd:desc/>
  </xd:doc>
  <xsl:template match="/">
      <xsl:message select="$xpe:input-file-uri"/>
      <xsl:next-match/>
    </xsl:template>
  
    <xd:doc>
        <xd:desc/>
    </xd:doc>
    <xsl:template match="node() | @*" mode="#all">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*" mode="#current"/>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
