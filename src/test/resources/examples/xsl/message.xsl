<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" 
    xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
    xmlns:xpe="http://com.sitc.xml.processing.engine.org/config"
    xmlns:test1="testing"
    exclude-result-prefixes="#all"
    version="3.0">

    <xsl:param name="test1:toto"/>

    <xd:doc>
        <xd:desc/>
    </xd:doc>
    <xsl:template match="/">
        <xsl:message select="$test1:toto"/>
    </xsl:template>
    
</xsl:stylesheet>
