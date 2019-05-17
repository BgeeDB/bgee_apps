package org.bgee.view;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.controller.TestURLParameters;

/**
 * This class is a fake {@code ViewFactory} used for tests. It return a {@code FakeDownloadDisplay}
 * when a {@code DownloadDisplay} is requested. It returns a mock {@code GeneralDisplay} in other
 * cases.
 * @author  Mathieu Seppey
 * @author  Valentine Rech de Laval
 * @version Bgee 14, May 2019
 * @since   Bgee 13, Aug. 2014
 */
public class FakeFactory extends ViewFactory {
    
    public FakeFactory(HttpServletResponse response, RequestParameters requestParameters,
            BgeeProperties prop) {
        super(response, requestParameters, prop);
    }
    
    @Override
    /**
     * This method check that the injected {@code BgeeProperties} and {@code URLParameters}
     * are present with correct values.
     * 
     * @param prop  The injected {@code BgeeProperties}
     * @return  A new {@code TestURLParameters} if the parameters are correct, else {@code null}
     */
    public DownloadDisplay getDownloadDisplay()  throws IOException {
        if(prop.getUrlMaxLength() == 9999 && this.requestParameters.getFirstValue(
                ((TestURLParameters)this.requestParameters.getUrlParametersInstance())
                .getParamTestString()).equals("test")){
            return new FakeDownloadDisplay(this.response, this.requestParameters, this.prop, this);
        }
        return null;
    }

    @Override
    /**
     * This method should not be called if the code is error free.
     * @param prop  A {@code BgeeProperties}
     * @return  {@code null}
     */
    public GeneralDisplay getGeneralDisplay() {
        return null;
    }

    @Override
    /**
     * This method should not be called if the code is error free.
     * @param prop  A {@code BgeeProperties}
     * @return  {@code null}
     */
    public ErrorDisplay getErrorDisplay() {
        return null;
    }

    @Override
    public DocumentationDisplay getDocumentationDisplay() throws IOException {
        if(prop.getUrlMaxLength() == 9999 && this.requestParameters.getFirstValue(
                ((TestURLParameters)this.requestParameters.getUrlParametersInstance())
                .getParamTestString()).equals("test")){
            return new FakeDocumentationDisplay(this.response, this.requestParameters, prop, this);
        }
        return null;
    }

    @Override
    public AboutDisplay getAboutDisplay() throws IOException {
        if(prop.getUrlMaxLength() == 9999 && this.requestParameters.getFirstValue(
                ((TestURLParameters)this.requestParameters.getUrlParametersInstance())
                .getParamTestString()).equals("test")){
            return new FakeAboutDisplay(this.response, this.requestParameters, prop, this);
        }
        return null;
    }

    @Override
    public PrivacyPolicyDisplay getPrivacyPolicyDisplay() throws IOException {
        if(prop.getUrlMaxLength() == 9999 && this.requestParameters.getFirstValue(
                ((TestURLParameters)this.requestParameters.getUrlParametersInstance())
                        .getParamTestString()).equals("test")){
            return new FakePrivacyPolicyDisplay(this.response, this.requestParameters, prop, this);
        }
        return null;
    }

    @Override
    public CollaborationDisplay getCollaborationDisplay() throws IOException {
        if(prop.getUrlMaxLength() == 9999 && this.requestParameters.getFirstValue(
                ((TestURLParameters)this.requestParameters.getUrlParametersInstance())
                        .getParamTestString()).equals("test")){
            return new FakeCollaborationDisplay(this.response, this.requestParameters, prop, this);
        }
        return null;
    }

    @Override
    public TopAnatDisplay getTopAnatDisplay() throws IOException {
        if(prop.getUrlMaxLength() == 9999 && this.requestParameters.getFirstValue(
                ((TestURLParameters)this.requestParameters.getUrlParametersInstance())
                .getParamTestString()).equals("test")){
            return new FakeTopAnatDisplay(this.response, this.requestParameters, prop, this);
        }
        return null;
    }

	@Override
	public GeneDisplay getGeneDisplay() throws IOException {
		return null;
	}

    @Override
    public ExpressionComparisonDisplay getExpressionComparisonDisplay() throws IOException {
        return null;
    }

    @Override
    public RawDataDisplay getRawCallDisplay() throws IOException {
        return null;
    }

    @Override
    public SpeciesDisplay getSpeciesDisplay() throws IOException {
        if(prop.getUrlMaxLength() == 9999 && this.requestParameters.getFirstValue(
                ((TestURLParameters)this.requestParameters.getUrlParametersInstance())
                .getParamTestString()).equals("test")){
            return new FakeSpeciesDisplay(this.response, this.requestParameters, prop, this);
        }
        return null;
    }

	@Override
	public SearchDisplay getSearchDisplay() throws IOException {
        if(prop.getUrlMaxLength() == 9999 && this.requestParameters.getFirstValue(
                ((TestURLParameters)this.requestParameters.getUrlParametersInstance())
                .getParamTestString()).equals("test")){
            return new FakeSearchDisplay(this.response, this.requestParameters, prop, this);
        }
        return null;
	}

	@Override
	public SourceDisplay getSourceDisplay() throws IOException {
	    if(prop.getUrlMaxLength() == 9999 && this.requestParameters.getFirstValue(
	            ((TestURLParameters)this.requestParameters.getUrlParametersInstance())
	            .getParamTestString()).equals("test")){
	        return new FakeSourceDisplay(this.response, this.requestParameters, prop, this);
	    }
	    return null;
	}

    @Override
    public DAODisplay getDAODisplay() throws IOException {
        return null;
    }

    @Override
    public JobDisplay getJobDisplay() throws IOException {
        return null;
    }

    @Override
    public RPackageDisplay getRPackageDisplay() throws IOException {
        return null;
    }
    
    @Override
    public FaqDisplay getFaqDisplay() throws IOException {
        return null;
    }

    @Override
    public SparqlDisplay getSparqlDisplay() throws IOException {
        return null;
    }

    @Override
    public ResourcesDisplay getResourceDisplay() throws IOException {
        if(prop.getUrlMaxLength() == 9999 && this.requestParameters.getFirstValue(
                ((TestURLParameters)this.requestParameters.getUrlParametersInstance())
                .getParamTestString()).equals("test")){
            return new FakeResourcesDisplay(this.response, this.requestParameters, prop, this);
        }
        return null;
    }

    @Override
    public AnatomicalSimilarityDisplay getAnatomicalSimilarityDisplay() throws IOException {
        return null;
    }
}