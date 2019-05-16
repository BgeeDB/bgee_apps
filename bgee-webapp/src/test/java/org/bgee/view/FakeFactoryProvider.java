package org.bgee.view;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;

/**
 * This test class extends {@code ViewFactoryProvider} and returns a {@code FakeFactory} only 
 * if the XML display type is requested (Arbitrary chosen to be used in tests). Return null in
 * all other cases. This is useful to assess that the display type is correctly handled by
 * controllers.
 * 
 * @author Mathieu Seppey
 * @version Bgee 13 Aug 2014
 * @since   Bgee 13
 */
public class FakeFactoryProvider extends ViewFactoryProvider
{    
    /**
     * Constructor
     */
    public FakeFactoryProvider(BgeeProperties prop)
    {
        super(prop);
    }

    /**
     * Return the Test {@code ViewFactory} if the {@code displayType} is XML, else return null
     * 
     * @param response          the {@code HttpServletResponse} where the outputs of the view 
     *                          classes will be written
     * @param displayType       an {@code int} specifying the requested display type, 
     *                          corresponding to either 
     *                          {@code HTML}, {@code XML}, {@code TSV}, or {@code CSV}.
     * @param requestParameters the {@code RequestParameters} handling the parameters of the 
     *                          current request, for display purposes.
     * @return     A {@code FakeFactory} if the {@code displayType} is XML, else null
     */
    @Override
    public synchronized ViewFactory getFactory(HttpServletResponse response, 
            DisplayType displayType, 
            RequestParameters requestParameters)
    {
        if (displayType == DisplayType.XML) {
            return new FakeFactory(response, requestParameters, this.prop);
        }
        ViewFactory mockFactory = mock(ViewFactory.class);
        try {
            when(mockFactory.getAboutDisplay())
            .thenReturn(mock(AboutDisplay.class));
        } catch (IOException e) {
            // Do nothing, should not occur with a mock
        }
        return mockFactory;
    }

}
