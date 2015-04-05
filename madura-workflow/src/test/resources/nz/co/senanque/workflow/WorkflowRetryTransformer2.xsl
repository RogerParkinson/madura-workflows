<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:pizza="http://www.senanque.co.nz/pizzaorder"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:temp="http://www.w3schools.com/webservices/">
    <xsl:template match="/">
        <xsl:apply-templates select="temp:FahrenheitToCelsiusResponse " />
    </xsl:template>
    <xsl:template match="temp:FahrenheitToCelsiusResponse">
        <xsl:choose>
            <xsl:when test="temp:FahrenheitToCelsiusResult = 'Error'">
                <pizza:Order error="Temperature conversion failed">
                </pizza:Order>
            </xsl:when>
            <xsl:otherwise>
                <pizza:Order>
                    <pizza:celsius>
                        <xsl:value-of select="temp:FahrenheitToCelsiusResult" />
                    </pizza:celsius>
                </pizza:Order>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>

