package org.bgee.model.gene;

import static org.junit.Assert.assertEquals;

import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.TestAncestor;
import org.bgee.model.source.Source;
import org.junit.Test;

/**
 * Unit tests for {@link GeneXRef}.
 *
 * @author  Frederic Bastian
 * @version Bgee 14, May 2019
 * @since   Bgee 14, Apr. 2019
 */
public class GeneXRefTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(GeneXRefTest.class.getName());
    @Override
    protected Logger getLogger() {
        return log;
    }

    private final static GeneXRef X_REF = new GeneXRef("&myid&", "myname",
            new Source(1, "source", "source",
                    "https://myresource.org/?xrefid=" + Source.X_REF_TAG + "&whatever=" + Source.GENE_TAG
                    + "&whatever2=" + Source.SPECIES_SCIENTIFIC_NAME_TAG,
                    null, null, null, null, null, null, null, null),
            "&geneId&", "My& Species");
    private final static Function<String, String> FAKE_URL_ENCODE = s -> s.replace("&", "%26");

    /**
     * Test the method {@link GeneXRef#getXRefUrl(boolean, Function)}
     * with HTML entity replacement.
     */
    @Test
    public void shouldGetXRefUrlWithAmpersandReplacement() {
        assertEquals("https://myresource.org/?xrefid=%26myid%26&amp;whatever=%26geneId%26"
                + "&amp;whatever2=My%26_Species",
                X_REF.getXRefUrl(true, FAKE_URL_ENCODE));
    }
    /**
     * Test the method {@link GeneXRef#getXRefUrl(boolean, Function)}
     * with no HTML entity replacement.
     */
    @Test
    public void shouldGetXRefUrlWithNoAmpersandReplacement() {
        assertEquals("https://myresource.org/?xrefid=%26myid%26&whatever=%26geneId%26"
                + "&whatever2=My%26_Species",
                X_REF.getXRefUrl(false, FAKE_URL_ENCODE));
    }
    /**
     * Test the method {@link GeneXRef#getXRefUrl(boolean, Function)}
     * with no tag to be replaced.
     */
    @Test
    public void shouldGetXRefUrlWithNoTag() {
        GeneXRef xref = new GeneXRef("&myid&", "myname",
                new Source(1, "source", "source",
                        "https://myresource.org/?xrefid=&whatever=&whatever2=",
                        null, null, null, null, null, null, null, null),
                "&geneId&", "My& Species");
        assertEquals("https://myresource.org/?xrefid=&whatever=&whatever2=",
                xref.getXRefUrl(false, FAKE_URL_ENCODE));
    }
}
