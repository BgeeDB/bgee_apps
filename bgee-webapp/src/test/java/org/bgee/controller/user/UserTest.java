package org.bgee.controller.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Objects;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.TestAncestor;
import org.bgee.controller.RequestParameters;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link User}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Nov 2016
 * @since Bgee 13 Nov 2016
 */
public class UserTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(UserTest.class.getName());
    
    private HttpServletRequest mockRequest;
    private RequestParameters mockRequestParams;
    
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Load mock objects before each test.
     */
    @Before
    public void loadMock() {
        this.mockRequest = mock(HttpServletRequest.class);
        //it is always needed to return an IP address
        when(this.mockRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        this.mockRequestParams = mock(RequestParameters.class);
    }
    
    /**
     * Test the extraction of the user IP address from the {@code HttpServletRequest}.
     */
    @Test
    public void shouldGetIPAddress() {
        //Test the parsing of IP address info
        User user = new User(this.mockRequest, this.mockRequestParams);
        assertEquals("Incorrect IP address retrieved", "127.0.0.1", user.getIPAddress());

        when(this.mockRequest.getRemoteAddr()).thenReturn("127.0.0.1 255.255.255.255");
        user = new User(this.mockRequest, this.mockRequestParams);
        assertEquals("Incorrect IP address retrieved", "127.0.0.1", user.getIPAddress());

        when(this.mockRequest.getRemoteAddr()).thenReturn("a1::e0/123%54-255.255.255.255");
        user = new User(this.mockRequest, this.mockRequestParams);
        assertEquals("Incorrect IP address retrieved", "a1::e0/123%54", user.getIPAddress());
        
        //test some different headers allowing to acquire the IP address
        when(this.mockRequest.getHeader("X-Forwarded-For")).thenReturn("1.1.1.1");
        when(this.mockRequest.getRemoteAddr()).thenReturn("a1::e0/123%54-255.255.255.255");
        user = new User(this.mockRequest, this.mockRequestParams);
        assertEquals("Incorrect IP address retrieved", "1.1.1.1", user.getIPAddress());

        when(this.mockRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(this.mockRequest.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn("2.2.2.2");
        when(this.mockRequest.getRemoteAddr()).thenReturn("a1::e0/123%54-255.255.255.255");
        user = new User(this.mockRequest, this.mockRequestParams);
        assertEquals("Incorrect IP address retrieved", "2.2.2.2", user.getIPAddress());
    }
    
    /**
     * Test the extraction of the most preferred user language from the {@code HttpServletRequest}.
     */
    @Test
    public void shouldGetPreferredLanguage() {
        when(this.mockRequest.getHeader("accept-language")).thenReturn("en-us;q=0.7,en;q=0.3,iw");
        User user = new User(this.mockRequest, this.mockRequestParams);
        //see http://docs.oracle.com/javase/8/docs/api/java/util/Locale.LanguageRange.html#parse-java.lang.String-, 
        //'he' can be added to the list with an equal weigth.
        assertTrue("Incorrect preferred language retrieved, expected 'iw' or 'he', but was: " 
        + user.getPreferredLanguage().get(), 
        "iw".equals(user.getPreferredLanguage().get()) || "he".equals(user.getPreferredLanguage().get()));
        
        //return an empty optional if no "accept-language"
        when(this.mockRequest.getHeader("accept-language")).thenReturn(null);
        user = new User(this.mockRequest, this.mockRequestParams);
        assertFalse("No preferred language should be present", user.getPreferredLanguage().isPresent());
    }
    
    /**
     * Test the retrieval from a {@code HttpServletRequest} of various info about the user 
     * for analytics purposes.
     */
    @Test
    public void shouldGetOtherInfo() {
        when(this.mockRequest.getHeader("user-agent")).thenReturn("myagent");
        when(this.mockRequest.getHeader("accept-language")).thenReturn("en-us");
        when(this.mockRequest.getHeader("referer")).thenReturn("myreferrer");
        
        User user = new User(this.mockRequest, this.mockRequestParams);
        assertEquals("Incorret IP address", "127.0.0.1", user.getIPAddress());
        assertEquals("Incorret user agent", "myagent", user.getUserAgent().orElse(null));
        assertEquals("Incorret preferred language", "en-us", user.getPreferredLanguage().orElse(null));
        assertEquals("Incorret referrer", "myreferrer", user.getReferrer().orElse(null));
    }
    
    /**
     * Test the identification of a user from a cookie.
     */
    @Test
    public void shouldCreateUserFromCookie() {
        String uuid = "db700a72-505b-3416-9136-a8bc31e22062";
        long timestamp = 500000;
        Cookie[] cookies = {new Cookie(User.BGEE_UUID_COOKIE_NAME, 
                uuid + User.UUID_TIMESTAMP_COOKIE_SEPARATOR + timestamp)};
        when(this.mockRequest.getCookies()).thenReturn(cookies);
        
        User user = new User(this.mockRequest, this.mockRequestParams);
        assertEquals("Incorrect UUID retrieved", uuid, user.getUUID().toString());
        assertEquals("Incorrect UUID source retrieved", UUIDSource.BGEE_COOKIE, user.getUUIDSource());
        
        //check that the cookie has precedence over other information
        when(this.mockRequest.getHeader("user-agent")).thenReturn("myagent");
        user = new User(this.mockRequest, this.mockRequestParams);
        assertEquals("Incorrect UUID retrieved", uuid, user.getUUID().toString());
        assertEquals("Incorrect UUID source retrieved", UUIDSource.BGEE_COOKIE, user.getUUIDSource());
        
        //And even over change of IP address
        when(this.mockRequest.getRemoteAddr()).thenReturn("1.1.1.1");
        user = new User(this.mockRequest, this.mockRequestParams);
        assertEquals("Incorrect UUID retrieved", uuid, user.getUUID().toString());
        assertEquals("Incorrect UUID source retrieved", UUIDSource.BGEE_COOKIE, user.getUUIDSource());
    }
    
    /**
     * Test the identification of a user from an API key parameter.
     */
    @Test
    public void shouldCreateUserFromAPIKey() {
        when(this.mockRequestParams.getApiKey()).thenReturn("mykey");
        User user = new User(this.mockRequest, this.mockRequestParams);
        assertEquals("Incorrect UUID retrieved", "ed22018e-2102-3b84-8b38-507b6fe2dd2f", user.getUUID().toString());
        assertEquals("Incorrect UUID source retrieved", UUIDSource.API_KEY, user.getUUIDSource());
        
        //check that the UUID doesn't change when other information are provided
        when(this.mockRequest.getHeader("user-agent")).thenReturn("myagent");
        User user2 = new User(this.mockRequest, this.mockRequestParams);
        assertEquals("Incorrect UUID retrieved", user.getUUID().toString(), user2.getUUID().toString());
        assertEquals("Incorrect UUID source retrieved", user.getUUIDSource(), user2.getUUIDSource());
        
        //Except that, for now, as we do not make users to officially request an API key, 
        //we take into account the IP address
        when(this.mockRequest.getRemoteAddr()).thenReturn("1.1.1.1");
        user2 = new User(this.mockRequest, this.mockRequestParams);
        assertNotEquals("Incorrect UUID retrieved", user.getUUID().toString(), user2.getUUID().toString());
        assertEquals("Incorrect UUID source retrieved", user.getUUIDSource(), user2.getUUIDSource());
        
        //Check that API key has precedence over cookie
        String uuid = "db700a72-505b-3416-9136-a8bc31e22062";
        long timestamp = 500000;
        Cookie[] cookies = {new Cookie(User.BGEE_UUID_COOKIE_NAME, 
                uuid + User.UUID_TIMESTAMP_COOKIE_SEPARATOR + timestamp)};
        when(this.mockRequest.getCookies()).thenReturn(cookies);
        when(this.mockRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        user2 = new User(this.mockRequest, this.mockRequestParams);
        assertEquals("Incorrect UUID retrieved", user.getUUID().toString(), user2.getUUID().toString());
        assertEquals("Incorrect UUID source retrieved", user.getUUIDSource(), user2.getUUIDSource());
    }
    
    /**
     * Test the identification of a user from information gathered from the request.
     */
    @Test
    public void shouldCreateUserFromRequestInfo() {
        when(this.mockRequest.getHeader("user-agent")).thenReturn("myagent");
        when(this.mockRequest.getHeader("accept")).thenReturn("text/html");
        when(this.mockRequest.getHeader("accept-language")).thenReturn("en-us;q=0.7,en;q=0.3,iw");
        User user = new User(this.mockRequest, this.mockRequestParams);
        
        //2 users created with the same request parameters should be equal
        User user2 = new User(this.mockRequest, this.mockRequestParams);
        assertEquals("Users from same parameters are different", user, user2);
        
        //Check that additional parameters are indeed taken into account
        when(this.mockRequest.getHeader("accept-charset")).thenReturn("utf-8");
        when(this.mockRequest.getHeader("accept-encoding")).thenReturn("gzip, deflate");
        User user3 = new User(this.mockRequest, this.mockRequestParams);
        assertNotEquals("Users from different parameters are equal", user, user3);

        when(this.mockRequest.getHeader("from")).thenReturn("test@example.com");
        when(this.mockRequest.getHeader("dnt")).thenReturn("1");
        User user4 = new User(this.mockRequest, this.mockRequestParams);
        assertNotEquals("Users from different parameters are equal", user, user4);
        assertNotEquals("Users from different parameters are equal", user3, user4);

        when(this.mockRequest.getHeader("accept-charset")).thenReturn("iso-whatever");
        User user5 = new User(this.mockRequest, this.mockRequestParams);
        assertNotEquals("Users from different parameters are equal", user, user5);
        assertNotEquals("Users from different parameters are equal", user3, user5);
        assertNotEquals("Users from different parameters are equal", user4, user5);
    }
    
    /**
     * Test appropriate creation of tracking cookie by the method 
     * {@link User#manageTrackingCookie(HttpServletRequest, String, long)}
     */
    @Test
    public void shouldCreateTrackingCookie() {
        //For a tracking cooking to be created, the UUIDSource of the user must be GENERATED
        User user = new User(this.mockRequest, this.mockRequestParams);
        
        Cookie expectedCookie = new Cookie(User.BGEE_UUID_COOKIE_NAME, user.getUUID().toString() 
                + User.UUID_TIMESTAMP_COOKIE_SEPARATOR + user.getBgeeUUID().getLastUpdateTimestamp());
        expectedCookie.setPath("/");
        expectedCookie.setMaxAge(User.BGEE_UUID_COOKIE_MAX_AGE);
        //Cookie does not override equals/hashCode :(
        this.compareCookies(expectedCookie, user.manageTrackingCookie(this.mockRequest, null).get(), false);
        //test with domain
        expectedCookie.setDomain(".bgee.org");
        this.compareCookies(expectedCookie, user.manageTrackingCookie(this.mockRequest, ".bgee.org").get(), false);
        
        //test that, even if the request includes a Bgee tracking cookie, it is not considered 
        //if the UUID source is GENERATED
        String uuid = "db700a72-505b-3416-9136-a8bc31e22062";
        long timestamp = 500000;
        Cookie requestCookie = new Cookie(User.BGEE_UUID_COOKIE_NAME, 
                uuid + User.UUID_TIMESTAMP_COOKIE_SEPARATOR + timestamp);
        Cookie[] cookies = {requestCookie};
        when(this.mockRequest.getCookies()).thenReturn(cookies);
        
        this.compareCookies(expectedCookie, user.manageTrackingCookie(this.mockRequest, ".bgee.org").get(), false);
        this.compareCookies(requestCookie, user.manageTrackingCookie(this.mockRequest, ".bgee.org").get(), true);
        
        //test that an invalid root domain is rejected (with only 1 dot)
        try {
            user.manageTrackingCookie(this.mockRequest, "bgee.org");
            //test failed
            throw new AssertionError("An IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {
            //test passed
        }
    }
    
    /**
     * Test appropriate update of tracking cookie by the method 
     * {@link User#manageTrackingCookie(HttpServletRequest, String, long)}
     */
    @Test
    public void shouldUpdateTrackingCookie() {
        //For a tracking cooking to be created, the UUIDSource of the user must be BGEE_COOKIE, 
        //so a cookie must be available from the request
        String uuid = "db700a72-505b-3416-9136-a8bc31e22062";
        long timestamp = 500000;
        Cookie requestCookie = new Cookie(User.BGEE_UUID_COOKIE_NAME, 
                uuid + User.UUID_TIMESTAMP_COOKIE_SEPARATOR + timestamp);
        requestCookie.setMaxAge(User.BGEE_UUID_COOKIE_MAX_AGE);
        requestCookie.setPath("/whatever");
        requestCookie.setDomain(".whatever.org");
        Cookie[] cookies = {requestCookie};
        when(this.mockRequest.getCookies()).thenReturn(cookies);
        User user = new User(this.mockRequest, this.mockRequestParams);
        
        //if half the expiration date of the cookie has been reached, it should be updated
        long fakeCurrentTimestamp = timestamp + 1000l * User.BGEE_UUID_COOKIE_MAX_AGE/2;
        Cookie expectedCookie = new Cookie(User.BGEE_UUID_COOKIE_NAME, 
                uuid + User.UUID_TIMESTAMP_COOKIE_SEPARATOR + fakeCurrentTimestamp);
        expectedCookie.setMaxAge(User.BGEE_UUID_COOKIE_MAX_AGE);
        expectedCookie.setPath("/whatever");
        expectedCookie.setDomain(".whatever.org");
        this.compareCookies(expectedCookie, 
                user.manageTrackingCookie(this.mockRequest, ".whatever.org", fakeCurrentTimestamp).get(), 
                false);
        
        //and if half the expiration date is not reached, the method does not update the cookie 
        //and returns nothing
        assertFalse("Incorrect cookie update", 
                user.manageTrackingCookie(this.mockRequest, ".whatever.org", fakeCurrentTimestamp - 1)
                .isPresent());
        
        //if the cookie is updated, only the expiration and creation date are changed, 
        //the domain is retrieved from the request Cookie
        this.compareCookies(expectedCookie, 
                user.manageTrackingCookie(this.mockRequest, ".another.domain.org", fakeCurrentTimestamp).get(), 
                false);
    }
    
    /**
     * Test that the method {@link User#manageTrackingCookie(HttpServletRequest, String, long)}
     * returns {@code null} when appropriate.
     */
    @Test
    public void shouldCreateNoTrackingCookie() {
        //if the user is formally identified (trough, e.g., an API key), we do not use tracking cookies
        when(this.mockRequestParams.getApiKey()).thenReturn("mykey");
        User user = new User(this.mockRequest, this.mockRequestParams);
        
        assertFalse("No cookie should be created when an API ky is provided", 
                user.manageTrackingCookie(this.mockRequest, null).isPresent());
        
        //Even if a cookie was present and half the expiration date of the cookie has been reached
        String uuid = "db700a72-505b-3416-9136-a8bc31e22062";
        long timestamp = 500000;
        Cookie[] cookies = {new Cookie(User.BGEE_UUID_COOKIE_NAME, 
                uuid + User.UUID_TIMESTAMP_COOKIE_SEPARATOR + timestamp)};
        when(this.mockRequest.getCookies()).thenReturn(cookies);
        long fakeCurrentTimestamp = timestamp + 1000l * User.BGEE_UUID_COOKIE_MAX_AGE/2;

        assertFalse("No cookie should be created when an API ky is provided", 
                user.manageTrackingCookie(this.mockRequest, null, fakeCurrentTimestamp).isPresent());
    }
    
    /**
     * {@code Cookie} class does not override equals/hashCode.
     * @param expected  The {@code Cookie} expected.
     * @param actual    The {@code Cookie} to test.
     * @param notEquals If we want to test that the two arguments are not equal.
     */
    private void compareCookies(Cookie expected, Cookie actual, boolean notEquals) {
        log.entry(expected, actual, notEquals);
        
        if (notEquals) {
            assertTrue("The cookies should be different. Cookie1: " + expected + " - Cookie2: " + actual, 
                    !Objects.equals(expected.getName(), actual.getName()) ||  
                    !Objects.equals(expected.getValue(), actual.getValue()) || 
                    !Objects.equals(expected.getPath(), actual.getPath()) || 
                    !Objects.equals(expected.getDomain(), actual.getDomain()) || 
                    !Objects.equals(expected.getMaxAge(), actual.getMaxAge()));
            log.traceExit(); return;
        }
        
        if (actual == null && expected != null || expected == null && actual != null ) {
            throw new AssertionError("Null and not-null cookies");
        }
        if (actual == null) {
            log.traceExit(); return;
        }
        
        assertEquals("Incorrect name of tracking cookie", expected.getName(), actual.getName());
        assertEquals("Incorrect value of tracking cookie", expected.getValue(), actual.getValue());
        assertEquals("Incorrect path of tracking cookie", expected.getPath(), actual.getPath());
        assertEquals("Incorrect domain of tracking cookie", expected.getDomain(), actual.getDomain());
        assertEquals("Incorrect max age of tracking cookie", expected.getMaxAge(), actual.getMaxAge());
        
        log.traceExit();
    }
}
