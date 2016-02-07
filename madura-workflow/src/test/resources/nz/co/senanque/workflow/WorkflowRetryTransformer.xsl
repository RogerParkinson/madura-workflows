<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:pizza="http://www.senanque.co.nz/pizzaorder"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:temp="http://www.w3schools.com/xml/">
	<xsl:template  match="/" >
		<xsl:apply-templates select="pizza:Order"/>
	</xsl:template>
	<xsl:template  match="pizza:Order" >
		<temp:FahrenheitToCelsius>
	       	<temp:Fahrenheit><xsl:value-of select="pizza:fahrenheit"/></temp:Fahrenheit>
		</temp:FahrenheitToCelsius>
	</xsl:template>
</xsl:stylesheet>
