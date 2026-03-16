package com.liv.infra.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
	
	public static String toBrazilianFormat(Date date) {
		try {
			// Formatar a data no padrão DD/MM/AAAA
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        	String dateFormated = sdf.format(date);
        	return dateFormated;
		} catch(Exception e) {
			return "";
		}
	}

}
