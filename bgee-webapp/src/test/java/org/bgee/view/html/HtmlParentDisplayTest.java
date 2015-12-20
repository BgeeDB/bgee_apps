package org.bgee.view.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bgee.controller.BgeeProperties;
import org.junit.Test;

/**
 * Test methods in {@link HtmlParentDisplay}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Mar. 2015
 * @since Bgee 13
 */
public class HtmlParentDisplayTest {
    
    @Test
    public void shouldGetVersionedJsFileName() throws IOException {
        BgeeProperties props = mock(BgeeProperties.class);
        when(props.isMinify()).thenReturn(true);
        HtmlFactory factory = mock(HtmlFactory.class);
        when(props.getJavascriptVersionExtension()).thenReturn("js13");
        HtmlParentDisplay display = new HtmlParentDisplay(null, null, props, factory);
        assertEquals("Incorrect versioned javascript file name generated", 
                "common.js13.js", display.getVersionedJsFileName("common.js"));
    }
    
    @Test
    public void shouldGetVersionedCssFileName() throws IOException {
        BgeeProperties props = mock(BgeeProperties.class);
        when(props.isMinify()).thenReturn(true);
        HtmlFactory factory = mock(HtmlFactory.class);
        when(props.getCssVersionExtension()).thenReturn("css13");
        HtmlParentDisplay display = new HtmlParentDisplay(null, null, props, factory);
        assertEquals("Incorrect versioned CSS file name generated", 
                "bgee.css13.css", display.getVersionedCssFileName("bgee.css"));
    }

    /**
     * Test {@link HtmlParentDisplay#getHTMLTag(String, String)}.
     */
    @Test
    public void shouldGetHTMLTagNoAttributes() {
        String tag = HtmlParentDisplay.getHTMLTag("div", "my content");
        assertEquals("Incorrect generated HTML tag, generated", "<div>my content</div>", tag);
    }
    /**
     * Test {@link HtmlParentDisplay#getHTMLTag(String, Map)}.
     */
    @Test
    public void shouldGetHTMLTagMapAttributes() {
        Map<String, String> attrs = new HashMap<>();
        attrs.put("id", "myid");
        attrs.put("class", "myclass");
        String tag = HtmlParentDisplay.getHTMLTag("div", attrs);
        assertTrue("Incorrect generated HTML tag, generated: " + tag, 
                "<div id='myid' class='myclass'></div>".equals(tag) || 
                "<div class='myclass' id='myid'></div>".equals(tag));
    }
    /**
     * Test {@link HtmlParentDisplay#getHTMLTag(String, Map, String)}.
     */
    @Test
    public void shouldGetHTMLTagAllArgs() {
        Map<String, String> attrs = new HashMap<>();
        attrs.put("id", "myid");
        attrs.put("class", "myclass");
        String tag = HtmlParentDisplay.getHTMLTag("div", attrs, "my content");
        assertTrue("Incorrect generated HTML tag, generated: " + tag, 
                "<div id='myid' class='myclass'>my content</div>".equals(tag) || 
                "<div class='myclass' id='myid'>my content</div>".equals(tag));
    }
}
