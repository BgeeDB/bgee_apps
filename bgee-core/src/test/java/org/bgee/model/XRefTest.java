package org.bgee.model;

import static org.junit.Assert.assertEquals;

import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.source.Source;
import org.junit.Test;

/**
 * Unit tests for {@link XRef}.
 *
 * @author  Frederic Bastian
 * @version Bgee 14, May 2019
 * @since   Bgee 14, Apr. 2019
 */
public class XRefTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(XRefTest.class.getName());
    @Override
    protected Logger getLogger() {
        return log;
    }

    private final static XRef X_REF = new XRef("&myid&", "myname",
            new Source(1, "source", "source",
                    "https://myresource.org/?xrefid=" + Source.X_REF_TAG + "&whatever=yes",
                    null, null, null, null, null, null, null, null));
    private final static Function<String, String> FAKE_URL_ENCODE = s -> s.replace("&", "%26");

    /**
     * Test the method {@link XRef#getXRefUrl(boolean, Function)}
     * with HTML entity replacement.
     */
    @Test
    public void shouldGetXRefUrlWithAmpersandReplacement() {
        assertEquals("https://myresource.org/?xrefid=%26myid%26&amp;whatever=yes",
                X_REF.getXRefUrl(true, FAKE_URL_ENCODE));
    }
    /**
     * Test the method {@link XRef#getXRefUrl(boolean, Function)}
     * with no HTML entity replacement.
     */
    @Test
    public void shouldGetXRefUrlWithNoAmpersandReplacement() {
        assertEquals("https://myresource.org/?xrefid=%26myid%26&whatever=yes",
                X_REF.getXRefUrl(false, FAKE_URL_ENCODE));
    }
}
