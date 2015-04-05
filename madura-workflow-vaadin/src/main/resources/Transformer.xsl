<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- 
Copyright (c)2014 Prometheus Consulting

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->
<xsl:stylesheet version="1.0"
	xmlns:pizza="http://www.senanque.co.nz/pizzaorder"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:temp="http://www.w3schools.com/webservices/">
	<xsl:template  match="/" >
		<xsl:apply-templates select="pizza:Order"/>
	</xsl:template>
	<xsl:template  match="pizza:Order" >
		<temp:FahrenheitToCelsius>
	       	<temp:Fahrenheit><xsl:value-of select="pizza:fahrenheit"/></temp:Fahrenheit>
		</temp:FahrenheitToCelsius>
	</xsl:template>
</xsl:stylesheet>
