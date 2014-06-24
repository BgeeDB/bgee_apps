package org.bgee.utils;

import org.apache.commons.lang3.StringUtils;

public class BgeeStringUtils extends org.apache.commons.lang3.StringUtils {
	
	BgeeStringUtils(){
		super();
	}
	
	/**
	 * Perform security controls and prepare the submitted <code>String</code> for use, 
	 * including a check for String length.
	 * 
	 * @param  	stringToCheck
	 * @return 	a secured and prepared <code>String</code>. Return an empty String if security checks have failed, 
	 * 			or if the stringToCheck was null, or if the length of <code>stringToCheck</code>  
	 * 			is greater than <code>MAXSTRINGLENGTH</code>.
	 * TODO 	log message if the String is discarded.
	 */
	public static String secureString(String stringToCheck)
	{
		return secureString(stringToCheck, 0, null);
	}
		
	/**
	 * Perform security controls and prepare the submitted <code>String</code> for use.
	 * 
	 * @param  	stringToCheck
	 * @param 	lengthToCheck 	an <code>int</code> defining the max allowed length of <code>stringToCheck</code>.
	 * 							If <code>stringToCheck</code> is greater than 0, 
	 * 							and if the length of <code>stringToCheck</code> is greater than <code>lengthToCheck</code>, 
	 * 							this method returns an empty string. 
	 * 							If <code>stringToCheck</code> is equal to 0, no control are performed on string length 
	 * 							(but other modifications are still performed, such as triming the <code>String</code>). 
	 * @return 	a secured and prepared <code>String</code>. Return an empty String if security checks have failed, 
	 * 			or if the stringToCheck was null, or of its length was greater than <code>lengthToCheck</code>.
	 * TODO 	log message if the String is discarded.
	 */
	public static String secureString(String stringToCheck, int lengthToCheck, String format)
	{
		if (stringToCheck == null || (lengthToCheck != 0 && stringToCheck.length() > lengthToCheck)) {
			return "";
		}
		else if(format != null && stringToCheck.matches(format)){
			return "";
		}
		return stringToCheck.trim();
	}
	
	/**
	 * Perform security controls and prepare the submitted <code>String</code> for use, 
	 * without checking length of <code>stringToCheck</code> (<code>MAXSTRINGLENGTH</code>).
	 * 
	 * @param  	stringToCheck
	 * @return 	a secured and prepared <code>String</code>. Return an empty String if security checks have failed, 
	 * 			or if the stringToCheck was null.
	 * @see 	#secureString(String)
	 * TODO 	log message if the String is discarded.
	 */
	public static String secureStringWithoutLengthCheck(String stringToCheck)
	{
		return secureString(stringToCheck, 0, null);
	}

	/**
	 * Perform security controls on the submitted <code>String</code> and transform it to a boolean.
	 * 
	 * @param  	stringToCheck
	 * @return 	a <code>boolean</code> corresponding to the <code>stringToCheck</code>. 
	 * 			Return also <code>false</code> if <code>stringToCheck</code> was null, empty, or not secured.
	 * TODO 	log message if the String is discarded.
	 */
	public static boolean secureStringAndCastToBoolean(String stringToCheck)
	{
		String tempStringToCheck = secureString(stringToCheck);
		
		if (tempStringToCheck.equalsIgnoreCase("on") || 
				tempStringToCheck.equalsIgnoreCase("true")) {
			return true;
		}
		return false;
	}
	
	/**
	 * Perform security controls on the submitted <code>String</code> and transform it to an int.
	 * 
	 * @param  	stringToCheck
	 * @return 	an <code>int</code> corresponding to the <code>stringToCheck</code>. 
	 * 			Return also 0 if <code>stringToCheck</code> was null, empty, or not secured.
	 * TODO 	log message if the String is discarded.
	 */
	public static int secureStringAndCastToInt(String stringToCheck)
	{
		String tempStringToCheck = secureString(stringToCheck);
		
		int castInt = 0;
        if (StringUtils.isNotBlank(tempStringToCheck)) {
        	try {
        		castInt = Integer.parseInt(tempStringToCheck);
        	} catch(NumberFormatException e) {
        		castInt = 0;
        	}
    	}
        return castInt;
	}

}