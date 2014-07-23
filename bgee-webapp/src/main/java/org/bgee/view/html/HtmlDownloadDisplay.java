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
		
		this.writeln("<div id='bgee_more_results_up'> &uarr;&nbsp;&nbsp;&nbsp;More result(s)</div>");
		this.writeln("<div id='bgee_more_results_down'> &darr;&nbsp;&nbsp;&nbsp;More result(s)</div>");
		
		// Introduction
		this.writeln("<div id='bgee_introduction'>");
		this.writeln("<p>Bgee is a database to retrieve and compare gene expression patterns between animal species. ");
		this.writeln("It currently provides &quot;presence/absence of expression&quot; and &quot;over-/under-expression&quot; data files.</p>");
		this.writeln("<p>This page is a download page containing pre-computed files of Bgee release 13 (based on Ensembl 75). ");
		this.writeln("Bgee release 12 (based on Ensembl 69) is always available <a href='http://bgee.unil.ch/bgee/bgee'>here</a>.</p>");
		this.writeln("</div>");
		
		// Search box
		this.writeln("<div id='bgee_search_box'>");
		this.writeln("<form action='/' method='get'>");
		this.writeln("<label>Search species</label>");
		this.writeln("<input class='sib_text' type='text' id='search_label2' name='search'/>"
				+ "<div id='results_nb'></div>");
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
		
		// Black banner when a species or a group is selected
		this.writeln("<div id='bgee_data_selection'>");
        this.writeln("<div id='bgee_data_selection_img'>");
        this.writeln(generateSpeciesImg(9606, "Homo sapiens", "human", null, false));
        this.writeln("</div>");
        this.writeln("<div id='bgee_data_selection_text'>");
        this.writeln("<h1 class='scientificname'>Homo sapiens</h1>&nbsp;&nbsp;<h1 class='commonname'>(human)</h1>");
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
        
        // Multi-species part
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
		
        // Images source
        this.writeln("<div class='creativecommons'>");
        this.writeln("<p class='creativecommons_title'>Images from Wikimedia Commons. See author images.</p>");
        this.writeln("<p>Homo sapiens by Yves Picq http://veton.picq.fr (Own work) [<a href='http://www.gnu.org/copyleft/fdl.html'>GFDL</a> or <a href='http://creativecommons.org/licenses/by-sa/3.0'>CC-BY-SA-3.0-2.5-2.0-1.0</a>], <a href='http://commons.wikimedia.org/wiki/File%3ALaos-lenten0264a.jpg'>via Wikimedia Commons</a></p>");
        this.writeln("<p>Mus musculus by Rasbak [<a href='http://www.gnu.org/copyleft/fdl.html'>GFDL</a> or <a href='http://creativecommons.org/licenses/by-sa/3.0/'>CC-BY-SA-3.0</a>], <a href='http://commons.wikimedia.org/wiki/File%3AApodemus_sylvaticus_bosmuis.jpg'>via Wikimedia Commons</a></p>");
        this.writeln("<p>Danio rerio by Azul (Own work) [see page for license], <a href='http://commons.wikimedia.org/wiki/File%3AZebrafisch.jpg'>via Wikimedia Commons</a></p>");
        this.writeln("<p>Drosophila melanogaster by Andr&eacute; Karwath aka Aka (Own work) [<a href='http://creativecommons.org/licenses/by-sa/2.5'>CC-BY-SA-2.5</a>], <a href='http://commons.wikimedia.org/wiki/File%3ADrosophila_melanogaster_-_side_(aka).jpg'>via Wikimedia Commons</a></p>");
        this.writeln("<p>Caenorhabditis elegans by Bob Goldstein, UNC Chapel Hill http://bio.unc.edu/people/faculty/goldstein/ (Own work) [<a href='http://creativecommons.org/licenses/by-sa/3.0'>CC-BY-SA-3.0</a>], <a href='http://commons.wikimedia.org/wiki/File%3ACelegansGoldsteinLabUNC.jpg'>via Wikimedia Commons</a></p>");
        this.writeln("<p>Pan paniscus by Ltshears (Own work) [<a href='http://creativecommons.org/licenses/by-sa/3.0'>CC-BY-SA-3.0</a> or <a href='http://www.gnu.org/copyleft/fdl.html'>GFDL</a>], <a href='http://commons.wikimedia.org/wiki/File%3ABonobo1_CincinnatiZoo.jpg'>via Wikimedia Commons</a></p>");
        this.writeln("<p>Pan troglodytes by Thomas Lersch (Own work) [<a href='http://www.gnu.org/copyleft/fdl.html'>GFDL</a>, <a href='http://creativecommons.org/licenses/by-sa/3.0/'>CC-BY-SA-3.0</a> or <a href='http://creativecommons.org/licenses/by/2.5'>CC-BY-2.5</a>], <a href='http://commons.wikimedia.org/wiki/File%3ASchimpanse_Zoo_Leipzig.jpg'>via Wikimedia Commons</a></p>");
        this.writeln("<p>Gorilla gorilla by Brocken Inaglory (Own work) [<a href='http://creativecommons.org/licenses/by-sa/3.0'>CC-BY-SA-3.0</a> or <a href='http://www.gnu.org/copyleft/fdl.html'>GFDL</a>], <a href='http://commons.wikimedia.org/wiki/File%3AMale_gorilla_in_SF_zoo.jpg'>via Wikimedia Commons</a></p>");
        this.writeln("<p>Pongo pygmaeus by Greg Hume (Own work) [<a href='http://creativecommons.org/licenses/by-sa/3.0'>CC-BY-SA-3.0</a>], <a href='http://commons.wikimedia.org/wiki/File%3ASUMATRAN_ORANGUTAN.jpg'>via Wikimedia Commons</a></p>");
        this.writeln("<p>Macaca mulatta by Aiwok (Own work) [<a href='http://www.gnu.org/copyleft/fdl.html'>GFDL</a> or <a href='http://creativecommons.org/licenses/by-sa/3.0'>CC-BY-SA-3.0-2.5-2.0-1.0</a>], <a href='http://commons.wikimedia.org/wiki/File%3AMacaca_mulatta_3.JPG'>via Wikimedia Commons</a></p>");
        this.writeln("<p>Rattus norvegicus by Reg Mckenna (originally posted to Flickr as Wild Rat) [<a href='http://creativecommons.org/licenses/by/2.0'>CC-BY-2.0</a>], <a href='http://commons.wikimedia.org/wiki/File%3AWildRat.jpg'>via Wikimedia Commons</a></p>");
        this.writeln("<p>Bos taurus by User Robert Merkel on en.wikipedia (US Department of Agriculture) [Public domain], <a href='http://commons.wikimedia.org/wiki/File%3AHereford_bull_large.jpg'>via Wikimedia Commons</a></p>");
        this.writeln("<p>Sus scrofa by Joshua Lutz (Own work) [Public domain], <a href='http://commons.wikimedia.org/wiki/File%3ASus_scrofa_scrofa.jpg'>via Wikimedia Commons</a></p>");
        this.writeln("<p>Monodelphis domestica by <i>Marsupial Genome Sheds Light on the Evolution of Immunity.</i> Hill E, PLoS Biology Vol. 4/3/2006, e75 <a rel='nofollow' href='http://dx.doi.org/10.1371/journal.pbio.0040075'>http://dx.doi.org/10.1371/journal.pbio.0040075</a> [<a href='http://creativecommons.org/licenses/by/2.5'>CC-BY-2.5</a>], <a href='http://commons.wikimedia.org/wiki/File%3AOpossum_with_young.png'>via Wikimedia Commons</a></p>");
        this.writeln("<p>Ornithorhynchus anatinus by Dr. Philip Bethge (private) [<a href='http://www.gnu.org/copyleft/fdl.html'>GFDL</a> or <a href='http://creativecommons.org/licenses/by-sa/3.0'>CC-BY-SA-3.0-2.5-2.0-1.0</a>], <a href='http://commons.wikimedia.org/wiki/File%3AOrnithorhynchus.jpg'>via Wikimedia Commons</a></p>");
        this.writeln("<p>Gallus gallus by Subramanya C K (Own work) [<a href='http://creativecommons.org/licenses/by-sa/3.0'>CC-BY-SA-3.0</a>], <a href='http://commons.wikimedia.org/wiki/File%3ARed_jungle_fowl.png'>via Wikimedia Commons</a></p>");
        this.writeln("<p>Anolis carolinensis by PiccoloNamek (Moved from Image:P1010027.jpg) [<a href='http://www.gnu.org/copyleft/fdl.html'>GFDL</a> or <a href='http://creativecommons.org/licenses/by-sa/3.0/'>CC-BY-SA-3.0</a>], <a href='http://commons.wikimedia.org/wiki/File%3AAnolis_carolinensis.jpg'>via Wikimedia Commons</a></p>");
        this.writeln("<p>Xenopus tropicalis by V&aacute;clav Gvo&zcaron;d&iacute;k (http://calphotos.berkeley.edu) [<a href='http://creativecommons.org/licenses/by-sa/2.5'>CC-BY-SA-2.5</a>, <a href='http://creativecommons.org/licenses/by-sa/2.5'>CC-BY-SA-2.5</a> or <a href='http://creativecommons.org/licenses/by-sa/3.0'>CC-BY-SA-3.0</a>], <a href='http://commons.wikimedia.org/wiki/File%3AXenopus_tropicalis01.jpeg'>via Wikimedia Commons</a></p>");
        this.writeln("<p>Tetraodon nigriviridis by Starseed (Own work) [<a href='http://creativecommons.org/licenses/by-sa/3.0/de/deed.en'>CC-BY-SA-3.0-de</a> or <a href='http://creativecommons.org/licenses/by-sa/3.0'>CC-BY-SA-3.0</a>], <a href='http://commons.wikimedia.org/wiki/File%3ATetraodon_nigroviridis_1.jpg'>via Wikimedia Commons</a></p>");

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
	    String name=null, commonName=null, shortName=null, alternateNames=null;
	    // Hidden info, to improve the jQuery search, allow to look for any of the name, short,
	    // or common name, even if not displayed... for example droso.
	    String hiddenInfo="";
	    for (Integer speciesId: speciesIds) {
	            switch(speciesId) {
	                case 9606: 
	                    name="Homo sapiens";
	                    shortName="H. sapiens";
	                    commonName="human";
	                    alternateNames="";
	                    break;
	                case 10090: 
	                    name="Mus musculus";
	                    shortName="M. musculus";
                        commonName="mouse";
	                    alternateNames="";
	                    break;
	                case 7955: 
	                    name="Danio rerio";
	                    shortName="D. rerio";
                        commonName="zebrafish";
	                    alternateNames="";
	                    break;
	                case 8364: 
	                    name="Xenopus tropicalis";
	                    shortName="X. tropicalis";
                        commonName="xenopus";
	                    alternateNames="";
	                    break;
	                case 7227: 
	                    name="Drosophila melanogaster";
	                    shortName="D. melanogaster";
                        commonName="fruitfly";
	                    alternateNames="";
	                    break;
	                case 9031: 
	                    name="Gallus gallus";
	                    shortName="G. gallus";
                        commonName="chicken";
	                    alternateNames="";
	                    break;
	                case 9593: 
	                    name="Gorilla gorilla";
	                    shortName="G. gorilla";
                        commonName="gorilla";
	                    alternateNames="";
	                    break;
	                case 9544: 
	                    name="Macaca mulatta";
	                    shortName="M. mulatta";
                        commonName="macaque";
	                    alternateNames="rhesus";
	                    break;
	                case 13616: 
	                    name="Monodelphis domestica";
	                    shortName="M. domestica";
                        commonName="opossum";
	                    alternateNames="";
	                    break;
	                case 9258: 
	                    name="Ornithorhynchus anatinus";
	                    shortName="O. anatinus";
                        commonName="platypus";
	                    alternateNames="";
	                    break;
	                case 9598: 
	                    name="Pan troglodytes";
	                    shortName="P. troglodytes";
                        commonName="chimpanzee";
	                    alternateNames="";
	                    break;
	                case 9597: 
	                    name="Pan paniscus";
	                    shortName="P. paniscus";
                        commonName="bonobo";
	                    alternateNames="";
	                    break;
	                case 9600: 
	                    name="Pongo pygmaeus";
	                    shortName="P. pygmaeus";
                        commonName="orangutan";
	                    alternateNames="";
	                    break;
	                case 9913: 
	                    name="Bos taurus";
	                    shortName="B. taurus";
                        commonName="cow";
	                    alternateNames="";
	                    break;
	                case 10116: 
	                    name="Rattus norvegicus";
	                    shortName="R. norvegicus";
                        commonName="rat";
	                    alternateNames="";
	                    break;
	                case 28377: 
	                    name="Anolis carolinensis";
	                    shortName="A. carolinensis";
                        commonName="anolis";
	                    alternateNames="";
	                    break;
	                case 99883: 
	                    name="Tetraodon nigroviridis";
	                    shortName="T. nigroviridis";
                        commonName="tetraodon";
	                    alternateNames="";
	                    break;
	                case 9823: 
	                    name="Sus scrofa";
	                    shortName="S. scrofa";
                        commonName="pig";
	                    alternateNames="";
	                    break;
	                case 6239: 
	                    name="Caenorhabditis elegans";
	                    shortName="C. elegans";
                        commonName="worm";
                        alternateNames="nematode";
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
	            images.append(generateSpeciesImg(speciesId, name, commonName, alternateNames,true));
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
			String alternateNames, boolean lightImg) {
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
	    image.append("' data-bgeespeciesalternatenames='");
	    image.append(alternateNames);
	    image.append("' />");
	    return (image.toString());
	}

	@Override
	public void includeJs()
	{
		this.includeJs("jquery.js");
		this.includeJs("jquery.visible.js");
		this.includeJs("jquery-ui.js");
		this.includeJs("download.js");
	}

}

