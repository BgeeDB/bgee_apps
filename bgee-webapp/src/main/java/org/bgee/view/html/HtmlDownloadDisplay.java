package org.bgee.view.html;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.DownloadDisplay;

public class HtmlDownloadDisplay extends HtmlParentDisplay implements DownloadDisplay
{

	public HtmlDownloadDisplay(HttpServletResponse response, RequestParameters requestParameters) throws IOException
	{
		super(response,requestParameters);
	}

	@Override
	public void displayDownloadPage()
	{
		this.startDisplay("download", "Bgee - welcome on Bgee: a dataBase for "
				+ "Gene Expression Evolution");

		this.writeln("<div id='sib_body'>");
		// Search box
		this.writeln("<div id='bgee_search_box'>");
		this.writeln("<form action='/' method='get'>");
		this.writeln("<label>Search species</label>");
		this.writeln("<input class='sib_text' type='text' id='search_label2' name='search'/>");
		this.writeln("</form>");
		this.writeln("</div>");
		// Single species part
		this.writeln("<p>Species</p>");
		this.writeln("<div id='bgee_uniq_species'>");
		this.writeln("<div class='biggroup'>");
		this.writeln(generateSpeciesFigure(9606));
		this.writeln(generateSpeciesFigure(10090));
		this.writeln(generateSpeciesFigure(7955));
		this.writeln(generateSpeciesFigure(7227));
		this.writeln(generateSpeciesFigure(6239));
		this.writeln(generateSpeciesFigure(9597));
		this.writeln(generateSpeciesFigure(9598));
		this.writeln(generateSpeciesFigure(9593));
		this.writeln(generateSpeciesFigure(9600));
		this.writeln(generateSpeciesFigure(9544));
		this.writeln(generateSpeciesFigure(10116));
		this.writeln(generateSpeciesFigure(9913));
		this.writeln(generateSpeciesFigure(9823));
		this.writeln(generateSpeciesFigure(13616));
		this.writeln(generateSpeciesFigure(9258));
		this.writeln(generateSpeciesFigure(9031));
		this.writeln(generateSpeciesFigure(28377));
		this.writeln(generateSpeciesFigure(8364));
		this.writeln(generateSpeciesFigure(99883));
		this.writeln("<div id='bgee_data_selection'>");
        this.writeln("<div id='bgee_data_selection_img'>");
        this.writeln(generateSpeciesImg(9606, "Homo sapiens", "human", false));
        this.writeln("</div>");
        this.writeln("<div id='bgee_data_selection_text'>");
        this.writeln("<h1 class='scientificname'>Homo sapiens</h1>"
                + "&nbsp;&nbsp;"
                + "<h1 class='commonname'>(human)</h1>");
        this.writeln("<ul>");
        this.writeln("<li><h2>Presence/absence of expression</h2>");
        this.writeln("<a id='expr_simple_csv' class='download_link' href='./data/fake-file.csv' download='bgee_homo_sapiens_isExpressed_simple.csv'>Download simple file</a>");
        this.writeln("&nbsp;&nbsp;");
        this.writeln("<a id='expr_complete_csv' class='download_link' href='./data/fake-file.csv' download='bgee_homo_sapiens_isExpressed_complete.csv'>Download complete file</a>");
        this.writeln("</li>");
        this.writeln("<li><h2 >Over-/Under-expression</h2>");
        this.writeln("<a id='overunder_simple_csv' class='download_link' href='./data/fake-file.csv' download='bgee_homo_sapiens_overUnderExpr_simple.tsv'>Download simple file</a>");
        this.writeln("&nbsp;&nbsp;");
        this.writeln("<a id='overunder_complete_csv' class='download_link' href='./data/fake-file.csv' download='bgee_homo_sapiens_overUnderExpr_complete.tsv'>Download complete file</a>");
        this.writeln("</li>");
        this.writeln("</ul>");
        this.writeln("</div>");
        this.writeln("</div>");
        this.writeln("</div>");
        this.writeln("</div>");
        
        // Multi species part
        this.writeln("<p>Multi-species</p>");
        this.writeln("<div id='bgee_multi_species'>");
        this.writeln("<div class='biggroup'>");
        this.writeln(generateSpeciesFigure(Arrays.asList(9606, 10090), "Group 1", true));
        this.writeln(generateSpeciesFigure(Arrays.asList(9606, 9823, 10116), "Group 2", true));
        this.writeln(generateSpeciesFigure(Arrays.asList(9606, 10090, 7955, 7227, 6239, 9597, 9598, 9593, 9600, 9544, 10116, 9913, 9258, 9031, 28377, 99883),
                "Group 3 (16 sp.)",true));
        this.writeln(generateSpeciesFigure(Arrays.asList(9606, 9823, 10116), "Group 4", true));
        this.writeln("</div>");
		this.writeln("</div>");
		
		// 
		this.writeln("</div>");

		this.endDisplay();
	}
	
    /**
     * Generate the HTML figure tag with a figcaption tag from a {@code int} that is a 
     * species ID.
     * 
     * @param speciesId     An {@code int} that is the species ID of the species to be 
     *                      diplayed.
     * @return             A {@String} that is the  HTML figure tag generated from the 
     *                     provided {@code int} of a species ID.
     */
    private String generateSpeciesFigure(int speciesId) {
        return generateSpeciesFigure(Arrays.asList(speciesId), null, false);
    }
	
    /**
     * Generate the HTML figure tag from a {@code List} of species IDs.
     * 
     * @param speciesIds   A {@code List} of {@code Integer} containing the species IDs to
     *                     be diplayed.
     * @param figcaption   A {@String} that is the fieldcaption of the figure. If empty 
     *                     or {@code null}, it's generated with the last species of the 
     *                     {@code List}.
     * @return             A {@String} that is the  HTML figure tag generated from the 
     *                     provided {@code List} of species IDs.
     */
	private String generateSpeciesFigure(List<Integer> speciesIds, String figcaption) {
		return generateSpeciesFigure(speciesIds, figcaption, false);
	}
	
	/**
	 * Generate the HTML figure tag from a {@code List} of species IDs.
	 * 
	 * @param speciesIds   A {@code List} of {@code Integer} containing the species IDs to
	 *                     be diplayed.
	 * @param figcaption   A {@String} that is the fieldcaption of the figure. If empty 
	 *                     or {@code null}, it's generated with the last species of the 
	 *                     {@code List}.
	 * @param isGroup      A {@code boolean} that is {@code true} if the figure represents 
	 *                     a group of species.
	 * @return             A {@String} that is the  HTML figure tag generated from the 
	 *                     provided {@code List} of species IDs.
	 */
	private String generateSpeciesFigure(List<Integer> speciesIds, String figcaption, 
	        boolean isGroup) {
	    StringBuilder images = new StringBuilder();
	    if (speciesIds == null || speciesIds.size() == 0) {
	        return ("");
	    }
	    String name=null, commonName=null, shortName=null;
	    // Hidden info, to improve the jQuery search, allow to look for any of the name, short,
	    // or common name, even if not displayed... for example droso.
	    String hiddenInfo="";
	    for (Integer speciesId: speciesIds) {
	            switch(speciesId) {
	                case 9606: 
	                    name="Homo sapiens";
	                    shortName="H. sapiens";
	                    commonName="human";
	                    break;
	                case 10090: 
	                    name="Mus musculus";
	                    shortName="M. musculus";
                        commonName="mouse";
	                    break;
	                case 7955: 
	                    name="Danio rerio";
	                    shortName="D. rerio";
                        commonName="zebrafish";
	                    break;
	                case 8364: 
	                    name="Xenopus tropicalis";
	                    shortName="X. tropicalis";
                        commonName="xenopus";
	                    break;
	                case 7227: 
	                    name="Drosophila melanogaster";
	                    shortName="D. melanogaster";
                        commonName="fruitfly";
	                    break;
	                case 9031: 
	                    name="Gallus gallus";
	                    shortName="G. gallus";
                        commonName="chicken";
	                    break;
	                case 9593: 
	                    name="Gorilla gorilla";
	                    shortName="G. gorilla";
                        commonName="gorilla";
	                    break;
	                case 9544: 
	                    name="Macaca mulatta";
	                    shortName="M. mulatta";
                        commonName="macaque";
	                    break;
	                case 13616: 
	                    name="Monodelphis domestica";
	                    shortName="M. domestica";
                        commonName="opossum";
	                    break;
	                case 9258: 
	                    name="Ornithorhynchus anatinus";
	                    shortName="O. anatinus";
                        commonName="platypus";
	                    break;
	                case 9598: 
	                    name="Pan troglodytes";
	                    shortName="P. troglodytes";
                        commonName="chimpanzee";
	                    break;
	                case 9597: 
	                    name="Pan paniscus";
	                    shortName="P. paniscus";
                        commonName="bonobo";
	                    break;
	                case 9600: 
	                    name="Pongo pygmaeus";
	                    shortName="P. pygmaeus";
                        commonName="orangutan";
	                    break;
	                case 9913: 
	                    name="Bos taurus";
	                    shortName="B. taurus";
                        commonName="cow";
	                    break;
	                case 10116: 
	                    name="Rattus norvegicus";
	                    shortName="R. norvegicus";
                        commonName="rat";
	                    break;
	                case 28377: 
	                    name="Anolis carolinensis";
	                    shortName="A. carolinensis";
                        commonName="anolis";
	                    break;
	                case 99883: 
	                    name="Tetraodon nigroviridis";
	                    shortName="T. nigroviridis";
                        commonName="tetraodon";
	                    break;
	                case 9823: 
	                    name="Sus scrofa";
	                    shortName="S. scrofa";
                        commonName="pig";
	                    break;
	                case 6239: 
	                    name="Caenorhabditis elegans";
	                    shortName="C. elegans";
                        commonName="worm";
	                    break;
	                default:
	                    return ("");
	            }
	            if(isGroup){
	            	hiddenInfo = hiddenInfo.concat(name + ", " + commonName + ", ");
	            }
	            else{
	            	hiddenInfo = name;
	            }
	            images.append(generateSpeciesImg(speciesId, name, commonName, true));
        }
        if (StringUtils.isBlank(figcaption)) {
            StringBuilder newFigcaption = new StringBuilder();
            newFigcaption.append("<p><i>");
            newFigcaption.append(shortName);
            newFigcaption.append("</i></p><p>");
            newFigcaption.append(commonName);
            newFigcaption.append("</p>");
            figcaption=newFigcaption.toString();   
        }
	    StringBuilder figure = new StringBuilder();
	    if(isGroup){
	    	figure.append("<figure data-bgeegroupname='" + figcaption + "'>");
	    }
	    else{
	    	figure.append("<figure>");
	    }
	    figcaption = figcaption.concat(" <span class='invisible'>" + hiddenInfo + "</span>");
	    figure.append("<div>"+images+"</div>");
	    figure.append("<figcaption>");
        figure.append(figcaption);
        figure.append("</figcaption>");
        figure.append("</figure>");
	    return (figure.toString());
	}
	
	/**
     * Generate the HTML img tag of one species.
     * 
     * @param isGroup      A {@code boolean} that is true if the figure represents a 
     *                     group of species.
     * @return             A {@String} that is the  HTML figure tag generated from the 
     *                     provided {@code List} of species IDs
	 * @param id           An {@code int} of the species IDs to be diplayed.
	 * @param name         A {@code String} that is the species name.
	 * @param commonName   A {@code String} that is the species common name.
	 * @param lightImg     A {@code boolean} that is {@code true} if the image to use is 
	 *                     the light one.
     * @return             A {@String} that is the  HTML img tag of the provided species 
     *                     data.
	 */
	private String generateSpeciesImg(int id, String name, String commonName, 
	        boolean lightImg) {
	    StringBuilder image = new StringBuilder();
	    image.append("<img src='");
	    image.append(BgeeProperties.getImagesRootDirectory());
	    image.append("species/");
	    image.append(commonName);
	    if (lightImg) {
	        image.append("_light");
	    }
	    image.append(".jpg' alt='");
	    image.append(name);
	    image.append("' data-bgeespeciesid='");
	    image.append(id);
	    image.append("'  data-bgeespeciesname='");
	    image.append(name);
	    image.append("' data-bgeespeciescommonname='");
	    image.append(commonName);
	    image.append("' />");
	    return (image.toString());
	}

	@Override
	public void includeJs()
	{
		this.includeJs("jquery.js");
		this.includeJs("download.js");
	}

}

