package org.bgee.view.html;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

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
        when(props.getJavascriptVersionExtension()).thenReturn("-js13");
        HtmlParentDisplay display = new HtmlParentDisplay(null, null, props, null);
        assertEquals("Incorrect versioned javascript file name generated", 
                "common-js13.js", display.getVersionedJsFileName("common.js"));
    }
    
    @Test
    public void shouldGetVersionedCssFileName() throws IOException {
        BgeeProperties props = mock(BgeeProperties.class);
        when(props.getCssVersionExtension()).thenReturn("-css13");
        HtmlParentDisplay display = new HtmlParentDisplay(null, null, props, null);
        assertEquals("Incorrect versioned CSS file name generated", 
                "bgee-css13.css", display.getVersionedCssFileName("bgee.css"));
    }
}
