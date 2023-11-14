package org.bgee.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.source.Source;

import java.util.Objects;
import java.util.function.Function;

/**
 * Class allowing to describe a cross-reference.
 *
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 14, May 2019
 * @since   Bgee 14, Apr. 2019
 */
public class XRef {
    private final static Logger log = LogManager.getLogger(XRef.class.getName());

    private final String xRefId;
    private final String xRefName;
    private final Source source;
    private final String xRefURLWithTags;

    public XRef(String xRefId, String xRefName, Source source) {
        //Source might be null, and we want to avoid throwing a null pointer exception.
        //So we repeat the same code as in the other constructor
        if (StringUtils.isBlank(xRefId)) {
            throw log.throwing(new IllegalArgumentException("The cross-reference ID must be provided."));
        }
        if (source == null) {
            throw log.throwing(new IllegalArgumentException("The source must be provided."));
        }
        this.xRefId = xRefId;
        this.xRefName = xRefName;
        this.source = source;
        this.xRefURLWithTags = source.getXRefUrl();
    }
    public XRef(String xRefId, String xRefName, Source source, String xRefURLWithTags) {
        if (StringUtils.isBlank(xRefId)) {
            throw log.throwing(new IllegalArgumentException("The cross-reference ID must be provided."));
        }
        if (source == null) {
            throw log.throwing(new IllegalArgumentException("The source must be provided."));
        }
        this.xRefId = xRefId;
        this.xRefName = xRefName;
        this.source = source;
        this.xRefURLWithTags = xRefURLWithTags;
    }

    /**
     * @return  A {@code String} that is the cross-reference ID.
     */
    public String getXRefId() {
        return xRefId;
    }
    /**
     * @return  A {@code String} that is the cross-reference name.
     */
    public String getXRefName() {
        return xRefName;
    }
    /**
     * @return  A {@code Source} that is the data source the cross-reference links to.
     */
    public Source getSource() {
        return source;
    }

    /**
     * Returns the cross-reference URL for this {@code XRef}.
     * <p>
     * <strong>Important:</strong> users need to URL encode the parameter values,
     * such as the XRef ID. Also, if the returned URL is meant to be displayed
     * in an HTML page, {@code convertAmpersandToHTMLEntity} should be set to {@code true},
     * in order to convert {@code &} characters in the XRef URL to the HTML entity {@code &amp;}
     * (the conversion is performed before setting parameter values in the URL, it will not mess up
     * the parameter values). A limitation of this method is that it only offers the possibility
     * to convert {@code &} characters: maybe some URLs might include other characters that should be
     * converted to HTML entities to be displayed in a HTML page (but it seems very unlikely);
     * in that case, users should retrieve themselves the XRef URL (see {@link Source#getXRefUrl()}),
     * convert applicable characters to HTML entities, and replace the appropriate tags
     * with correct values (see static attributes in {@link Source} class).
     * Again, this need seems very unlikely.
     *
     * @param convertAmpersandToHTMLEntity  A {@code boolean} specifying, if {@code true}, to replace
     *                                      {@code &} characters in the URL returned by {@link #getXRefUrl()}
     *                                      with {@code &amp;} entities.
     * @param urlEncode                     A {@code Function} accepting a {@code String} and returning
     *                                      the {@code String} URL encoded. Can be {@code null} if it is not
     *                                      necessary to URL encode parameter values.
     * @return                              The {@code String} corresponding to the cross-reference URL.
     *                                      Can be {@code null} if no cross-reference URL exists for this XRef.
     * @see Source
     * @see Source#getXRefUrl()
     * @implNote    This method does not URL encode the parameter values itself,
     *              because URL encoding is charset dependent. This method could accept
     *              the desired charset as argument, but it seems cleaner to simply ask the user
     *              to provide a way to URL encode the parameter values when needed.
     */
    public String getXRefUrl(boolean convertAmpersandToHTMLEntity, Function<String, String> urlEncode) {
        log.traceEntry("{}, {}", convertAmpersandToHTMLEntity, urlEncode);
        if (urlEncode == null) {
            throw log.throwing(new IllegalArgumentException("An URL encoding method must be provided"));
        }

        String xRefUrl = this.xRefURLWithTags;
        if (StringUtils.isBlank(xRefUrl)) {
            return log.traceExit((String) null);
        }

        if (convertAmpersandToHTMLEntity) {
            assert !Source.X_REF_TAG.contains("&");
            //replaceAll uses regex, we want literal replacement.
            //'replace' performs all literal replacements, the naming is just terrible.
            xRefUrl = xRefUrl.replace("&", "&amp;");
        }
        if (xRefUrl.contains(Source.X_REF_TAG)) {
            xRefUrl = xRefUrl.replace(Source.X_REF_TAG, urlEncode.apply(this.getXRefId()));
        }
        if (xRefUrl.contains(Source.EXPERIMENT_TAG)) {
            xRefUrl = xRefUrl.replace(Source.EXPERIMENT_TAG, urlEncode.apply(this.getXRefId()));
        }
        return log.traceExit(xRefUrl);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        XRef xRef = (XRef) o;
        return Objects.equals(xRefId, xRef.xRefId) &&
                Objects.equals(xRefName, xRef.xRefName) &&
                Objects.equals(source, xRef.source) &&
                Objects.equals(xRefURLWithTags, xRef.xRefURLWithTags);
    }
    @Override
    public int hashCode() {
        return Objects.hash(xRefId, xRefName, source, xRefURLWithTags);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("XRef [xRefId=").append(xRefId)
                .append(", xRefName=").append(xRefName)
                .append(", source=").append(source)
                .append(", xRefURLWithTags=").append(xRefURLWithTags)
                .append("]");
        return sb.toString();
    }
}