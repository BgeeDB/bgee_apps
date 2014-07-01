package org.bgee.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * This class extends {@link org.apache.commons.lang3.StringUtils} and provides methods to 
 * check and secure {@code String}s for their use within the webapp. 
 * 
 * @author Mathieu Seppey
 * @author Frederic Bastian
 */
public class BgeeStringUtils extends org.apache.commons.lang3.StringUtils {

	/**
	 * Default constructor
	 * StringUtils instances should NOT be constructed in standard programming.
	 */
	public BgeeStringUtils(){
		super();
	}

	/**
	 * Perform security controls and prepare the submitted <code>String</code> for use
	 * 
	 * @param  	stringToCheck a {@code String} to be checked 
	 * @return 	a secured and prepared <code>String</code>. Return an empty String if security checks 
	 * 			have failed.
	 * TODO 	log message if the String is discarded.
	 */
	public static String secureString(String stringToCheck)
	{
		return secureString(stringToCheck, 0, null);
	}

	/**
	 * Perform security controls and prepare the submitted <code>String</code> for use. It includes
	 * a check of the <code>String</code> length and the format of the <code>String</code>.
	 * 
	 * @param  	stringToCheck	stringToCheck a {@code String} to be checked 
	 * @param 	lengthToCheck 	an <code>int</code> defining the max allowed length of 
	 * 							<code>stringToCheck</code>.
	 * 							If <code>stringToCheck</code> is greater than 0, and if the length of
	 * 							 <code>stringToCheck</code> is greater than <code>lengthToCheck</code>, 
	 * 							this method returns an empty string. 
	 * 							If <code>stringToCheck</code> is equal to 0, no control are performed 
	 * 							on string length (but other modifications are still performed, 
	 * 							such as triming the <code>String</code>). 
	 * @param	format			A {@code String} that contains the regular expression the 
	 * 							{@code String} should match.
	 * @return 	a secured and prepared <code>String</code>. Return an empty String if security checks
	 * 			have failed, or if the stringToCheck was null, or of its length was greater than 
	 * 			<code>lengthToCheck</code>.
	 * TODO 	log message if the String is discarded.
	 */
	public static String secureString(String stringToCheck, int lengthToCheck, String format)
	{
		if (stringToCheck == null || (lengthToCheck != 0 && stringToCheck.length() > lengthToCheck)) {
			return "";
		}
		else if(format != null && stringToCheck.matches(format) == false){
			return null;
		}
		return stringToCheck.trim();
	}

	/**
	 * Perform security controls and prepare the submitted <code>String</code> for use, 
	 * without checking length of <code>stringToCheck</code> (<code>MAXSTRINGLENGTH</code>).
	 * 
	 * @param  	stringToCheck
	 * @return 	a secured and prepared <code>String</code>. Return an empty String if security checks
	 * 			 have failed, or if the stringToCheck was null.
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
	 * 			Return also <code>false</code> if <code>stringToCheck</code> was null, empty,
	 * 			or not secured.
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

	/**
	 * Encode String to be used in URLs. 
	 * <p>
	 * This method is different from the <code>encodeURL</code> method 
	 * of <code>HttpServletResponse</code>, as it does not include a logic 
	 * for session tracking. It just converts special chars to be used in URL.
	 *  
	 * @param string 	the <code>String</code> to be encoded.
	 * @return 			a <code>String</code> encoded
	 */
	public static String urlEncode(String string)
	{
		String encodeString = string;

		try {
			// warning, you need to add an attribut to the connector in server.xml  
			// in order to get the utf-8 encoding working : URIEncoding="UTF-8"
			encodeString = java.net.URLEncoder.encode(string, "ISO-8859-1");
		} catch (Exception e) {
			//			LOGGER.error("Error while URLencoding", e);
		}
		return encodeString;
	}


}