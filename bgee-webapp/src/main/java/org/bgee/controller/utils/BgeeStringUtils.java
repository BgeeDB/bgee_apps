package org.bgee.controller.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.WrongFormatException;

/**
 * This class extends {@link org.apache.commons.lang3.StringUtils} and provides methods to 
 * check and secure {@code String}s for their use within the webapp. 
 * 
 * @author Mathieu Seppey
 * @author Frederic Bastian
 * @version Bgee 13 Aug 2014
 * @since   Bgee 13
 * 
 */
public class BgeeStringUtils extends org.apache.commons.lang3.StringUtils {

    private final static Logger log = LogManager.getLogger(BgeeStringUtils.class.getName());

    /**
     * Default constructor
     * StringUtils instances should NOT be constructed in standard programming.
     */
    public BgeeStringUtils(){
        super();
    }

    /**
     * Perform security controls and prepare the submitted {@code String} for use
     * 
     * @param stringToCheck    a {@code String} to be checked 
     * @return a secured and prepared {@code String}. Return an empty String if security checks 
     *         have failed.
     * @throws WrongFormatException The {@code String} to secure does not fit the requirement
     */
    public static String secureString(String stringToCheck) throws WrongFormatException
    {
        return secureString(stringToCheck, 0, null);
    }

    /**
     * Perform security controls and prepare the submitted {@code String} for use. It includes
     * a check of the {@code String} length and the format of the {@code String}.
     * 
     * @param stringToCheck    a {@code String} to be checked 
     * @param lengthToCheck    an {@code int} defining the max allowed length of 
     *                         {@code stringToCheck}. If {@code stringToCheck} is greater 
     *                         than 0, and if the length of {@code stringToCheck} is greater
     *                         than {@code lengthToCheck}, this method returns an empty string. 
     * 					       If {@code stringToCheck} is equal to 0, no control are performed 
     * 					       on string length (but other modifications are still performed, 
     * 					       such as triming the {@code String}). 
     * @param format           A {@code String} that contains the regular expression the 
     *                         {@code String} should match.
     * @return a secured and prepared {@code String}. Return an empty String if security checks
     *         have failed, or if the stringToCheck was null, or of its length was greater than 
     *         {@code lengthToCheck}.
     * @throws WrongFormatException The {@code String} to secure does not fit the requirement
     *
     *              */
    public static String secureString(String stringToCheck, int lengthToCheck, String format)
            throws WrongFormatException
    {
        log.entry(stringToCheck, lengthToCheck, format);
        if (stringToCheck == null) {
            return "";
        }
        else if(lengthToCheck != 0 && stringToCheck.length() > lengthToCheck){
            log.info("The string {} cannot be validated because it is too long ({})", 
                    stringToCheck, stringToCheck.length());
            throw(new WrongFormatException());
        }
        else if(format != null && stringToCheck.matches(format) == false){
            log.info("The string {} cannot be validated because it does not match the format {}", 
                    stringToCheck, format);
            throw(new WrongFormatException());
        }
        return log.exit(stringToCheck.trim());
    }

    /**
     * Perform security controls and prepare the submitted {@code String} for use, 
     * without checking length of {@code stringToCheck} ({@code MAXSTRINGLENGTH}).
     * 
     * @param stringToCheck
     * @return  a secured and prepared {@code String}. Return an empty String if security checks
     * 	        have failed, or if the stringToCheck was null.
     * @throws WrongFormatException The {@code String} to secure does not fit the requirement
     * @see #secureString(String)
     */
    public static String secureStringWithoutLengthCheck(String stringToCheck) throws WrongFormatException
    {
        return secureString(stringToCheck, 0, null);
    }

    /**
     * Perform security controls on the submitted {@code String} and transform it to a boolean.
     * 
     * @param stringToCheck a {@code String} to be checked 
     * @return  a {@code boolean} corresponding to the {@code stringToCheck}. 
     * 			Return also {@code false} if {@code stringToCheck} was null, empty,
     * 			or not secured.
     * @throws WrongFormatException The {@code String} to secure does not fit the requirement
     */
    public static boolean secureStringAndCastToBoolean(String stringToCheck) throws WrongFormatException
    {
        log.entry(stringToCheck);
        String tempStringToCheck = secureString(stringToCheck);

        if (tempStringToCheck.equalsIgnoreCase("on") || 
                tempStringToCheck.equalsIgnoreCase("true")) {
            return log.exit(true);
        }
        return log.exit(false);
    }

    /**
     * Perform security controls on the submitted {@code String} and transform it to an int.
     * 
     * @param stringToCheck a {@code String} to be checked 
     * @return  an {@code int} corresponding to the {@code stringToCheck}. 
     * 			Return also 0 if {@code stringToCheck} was null, empty, or not secured.
     * @throws WrongFormatException The {@code String} to secure does not fit the requirement
     */
    public static int secureStringAndCastToInt(String stringToCheck) throws WrongFormatException
    {
        log.entry(stringToCheck);
        String tempStringToCheck = secureString(stringToCheck);

        int castInt = 0;
        if (StringUtils.isNotBlank(tempStringToCheck)) {
            try {
                castInt = Integer.parseInt(tempStringToCheck);
            } catch(NumberFormatException e) {
                castInt = 0;
            }
        }
        return log.exit(castInt);
    }

    /**
     * Encode String to be used in URLs. 
     * <p>
     * This method is different from the {@code encodeURL} method 
     * of {@code HttpServletResponse}, as it does not include a logic 
     * for session tracking. It just converts special chars to be used in URL.
     *  
     * @param string 	the {@code String} to be encoded.
     * @return  a {@code String} encoded
     */
    public static String urlEncode(String string)
    {
        log.entry(string);
        String encodeString = string;

        try {
            // warning, you need to add an attribut to the connector in server.xml  
            // in order to get the utf-8 encoding working : URIEncoding="UTF-8"
            encodeString = java.net.URLEncoder.encode(string, "ISO-8859-1");
        } catch (Exception e) {
            //			LOGGER.error("Error while URLencoding", e);
        }
        return log.exit(encodeString);
    }
}