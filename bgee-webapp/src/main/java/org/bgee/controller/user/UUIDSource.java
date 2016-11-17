package org.bgee.controller.user;

/**
 * An {@code Enum} representing the different sources for acquiring a user ID: 
 * <ul>
 * <li>{@code BGEE_COOKIE}: UUID retrieved from a Bgee cookie.
 * <li>{@code GENERATED}: UUID generated from information retrieved from the request to the server.
 * <li>{@code API_KEY}: user identified through a provided API key.
 * <li>{@code REMOTE_USER}: user identified through the remote user retrieved from tomcat 
 * (see http://stackoverflow.com/questions/7553967/getting-a-value-from-httpservletrequest-getremoteuser-in-tomcat-without-modify).
 * Not used for now.
 * <li>{@code AUTH}: user identified through HTTP authentication (hmm, isn't the same as {@code REMOTE_USER}?).
 * Not used for now.
 * </ul>
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Nov 2016
 * @since Bgee 13 Nov 2016
 */
public enum UUIDSource {
    BGEE_COOKIE, GENERATED, API_KEY, REMOTE_USER, AUTH;
}