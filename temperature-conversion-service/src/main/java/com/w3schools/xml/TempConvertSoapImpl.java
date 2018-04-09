package com.w3schools.xml;

import java.text.DecimalFormat;

public class TempConvertSoapImpl implements TempConvertPortType {

	DecimalFormat df2 = new DecimalFormat( "#,###,###,##0.00" );
	
    public java.lang.String fahrenheitToCelsius(java.lang.String fahrenheit) {
    	try {
			double celsius = (Float.valueOf(fahrenheit) - 32) * .5556;
			return df2.format(celsius);
		} catch (NumberFormatException e) {
			throw new RuntimeException ("Invalid number");
		}
    }

    public java.lang.String celsiusToFahrenheit(java.lang.String celsius) {
    	try {
			double fahrenheit = (Float.valueOf(celsius) + 32) * 1.8;
			return df2.format(fahrenheit);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Invalid number");
		}
    }

}
