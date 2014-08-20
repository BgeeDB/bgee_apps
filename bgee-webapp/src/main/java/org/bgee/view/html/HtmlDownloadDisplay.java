package org.bgee.view.html;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.DownloadDisplay;

public class HtmlDownloadDisplay extends HtmlParentDisplay implements DownloadDisplay
{
 
    private final static Logger log = LogManager.getLogger(HtmlDownloadDisplay.class.getName());
    
    public HtmlDownloadDisplay(HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop) throws IOException
    {
        super(response,requestParameters, prop);
    }

    @Override
    public void displayDownloadPage()
    {
        log.entry();
        this.startDisplay("download", "Bgee release 13 download page");

        this.writeln("<div id='sib_body'>");
        
        this.writeln("<div id='bgee_more_results_up'> &uarr;&nbsp;&nbsp;&nbsp;More result(s)</div>");
        this.writeln("<div id='bgee_more_results_down'> &darr;&nbsp;&nbsp;&nbsp;More result(s)</div>");
        
        // Introduction
        this.writeln("<div id='bgee_introduction'>");
        this.writeln("<h1>Welcome to the Bgee release 13 download page</h1>");
        this.writeln("<div class='biggroup'>");
        this.writeln("<p>Bgee is a database to retrieve and compare gene expression patterns between animal species. ");
        this.writeln("It currently provides &quot;presence/absence of expression&quot; and &quot;over-/under-expression&quot; data files.</p>");
        this.writeln("<p>This page is a download page containing pre-computed files of Bgee release 13 (based on Ensembl 75). ");
        this.writeln("<a href='http://bgee.unil.ch/bgee/bgee'>Bgee release 12 (based on Ensembl 69) is always available here</a>.</p>");
        this.writeln("</div>");
        this.writeln("</div>");
        
        // Search box
        this.writeln("<div id='bgee_search_box'>");
        this.writeln("<form action='/' method='get'>");
        this.writeln("<label for='search_label'>Search species</label>&nbsp;&nbsp;");
        this.writeln("<input id='search_label' class='sib_text' type='text' name='search' "
                + "value='Scientific name, common name...'/>&nbsp;&nbsp;");
        this.writeln("<input type='image' alt='Submit' "
                + "src='"+prop.getImagesRootDirectory()+"submit_button.png'/>");
        this.writeln("<div id='results_nb'></div>");
        this.writeln("</form>");
        this.writeln("</div>");
        
        // Single species part
        this.writeln("<div id='bgee_uniq_species'> ");
        this.writeln("<h1>Species</h1>");
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

        this.writeln("<div id='bgee_data_selection_cross'>");
        this.writeln("<img src='"+this.prop.getImagesRootDirectory()+"cross.png' "
                + "title='Close banner' alt='Close banner' /> ");
        this.writeln("</div>");
        this.writeln("<div id='bgee_data_selection_img'>");
        this.writeln(generateSpeciesImg(9606, "Homo sapiens", "H. sapiens", "human", null, false));
        this.writeln("</div>");
        this.writeln("<div id='bgee_data_selection_text'>");
        this.writeln("<h1 class='scientificname'>Homo sapiens</h1>&nbsp;&nbsp;<h1 class='commonname'>(human)</h1>");
        this.writeln("<p class='groupdescription'>16. species: <i>H. sapiens</i>, <i>M. musculus</i>, <i>D. rerio</i>, "
                + "<i>D. melanogaster</i>, <i>C. elegans</i>, <i>P. paniscus</i>, <i>P. troglodytes</i>, "
                + "<i>G. gorilla</i>, <i>P. pygmaeus</i>, <i>M. mulatta</i>, <i>R. norvegicus</i>, "
                + "<i>B. taurus</i>, <i>O. anatinus</i>, <i>G. gallus</i>, <i>A. carolinensis</i>, "
                + "<i>T. nigroviridis</i></p>");
        this.writeln("<ul>");    
        this.writeln("<li><h2>Presence/absence of expression</h2>");
        this.writeln("<a id='expr_simple_csv' class='download_link' href='./data/fake-file.csv' download>Download simple file</a>");
        this.writeln("&nbsp;&nbsp;");
        this.writeln("<a id='expr_complete_csv' class='download_link' href='./data/fake-file.csv' download>Download complete file</a>");
        this.writeln("</li>");
        this.writeln("<li><h2 >Over-/Under-expression</h2>");
        this.writeln("<a id='overunder_simple_csv' class='download_link' href='./data/fake-file.csv' download>Download simple file</a>");
        this.writeln("&nbsp;&nbsp;");
        this.writeln("<a id='overunder_complete_csv' class='download_link' href='./data/fake-file.csv' download>Download complete file</a>");
        this.writeln("</li>");
        this.writeln("</ul>");
        this.writeln("</div>");
        this.writeln("</div>");
        this.writeln("</div>");
        this.writeln("</div>");
        
        // Multi-species part
        this.writeln("<div id='bgee_multi_species'>");
        this.writeln("<h1>Multi-species</h1> <span>(data of only orthologous genes)</span>");
        this.writeln("<div class='biggroup'>");
        this.writeln(generateSpeciesFigure(Arrays.asList(9606, 10090), "Group 1", true));
        this.writeln(generateSpeciesFigure(Arrays.asList(9606, 9823, 10116), "Group 2", true));
        this.writeln(generateSpeciesFigure(Arrays.asList(9606, 10090, 7955, 7227, 6239, 9597, 9598, 9593, 9600, 9544, 10116, 9913, 9258, 9031, 28377, 99883,9606, 10090, 7955, 7227, 6239, 9597, 9598, 9593, 9600, 9544, 10116, 9913, 9258, 9031, 28377, 99883),
                "Group 3",true));
        this.writeln(generateSpeciesFigure(Arrays.asList(9606, 9823, 10116, 28377, 10116), "Group 4", true));
        this.writeln("</div>");
        this.writeln("</div>");
        
        // Images source
        this.writeln("<p id='creativecommons_title'>Images from Wikimedia Commons. In most cases, pictures corresponds to the sequenced strains. <a>Show information about original images.</a></p>");
        this.writeln("<div id='creativecommons'>");
        this.writeln("<p><i>Homo sapiens</i> picture by Yves Picq http://veton.picq.fr (Own work) [<a target='_blank' href='http://www.gnu.org/copyleft/fdl.html'>GFDL</a> or <a target='_blank' href='http://creativecommons.org/licenses/by-sa/3.0'>CC-BY-SA-3.0-2.5-2.0-1.0</a>]. <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3ALaos-lenten0264a.jpg'>See <i>H. sapiens</i> picture via Wikimedia Commons</a></p>");
        this.writeln("<p><i>Mus musculus</i> picture by Rasbak [<a target='_blank' href='http://www.gnu.org/copyleft/fdl.html'>GFDL</a> or <a target='_blank' href='http://creativecommons.org/licenses/by-sa/3.0/'>CC-BY-SA-3.0</a>], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3AApodemus_sylvaticus_bosmuis.jpg'>See <i>M. musculus</i> picture via Wikimedia Commons</a></p>");
        this.writeln("<p><i>Danio rerio</i> picture by Azul (Own work) [see page for license], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3AZebrafisch.jpg'>See <i>D. rerio</i> picture via Wikimedia Commons</a></p>");
        this.writeln("<p><i>Drosophila melanogaster</i> picture by Andr&eacute; Karwath aka Aka (Own work) [<a target='_blank' href='http://creativecommons.org/licenses/by-sa/2.5'>CC-BY-SA-2.5</a>], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3ADrosophila_melanogaster_-_side_(aka).jpg'>See <i>D. melanogaster</i> picture via Wikimedia Commons</a></p>");
        this.writeln("<p><i>Caenorhabditis elegans</i> picture by Bob Goldstein, UNC Chapel Hill http://bio.unc.edu/people/faculty/goldstein/ (Own work) [<a target='_blank' href='http://creativecommons.org/licenses/by-sa/3.0'>CC-BY-SA-3.0</a>], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3ACelegansGoldsteinLabUNC.jpg'>See <i>C. elegans</i> picture via Wikimedia Commons</a></p>");
        this.writeln("<p><i>Pan paniscus</i> picture by Ltshears (Own work) [<a target='_blank' href='http://creativecommons.org/licenses/by-sa/3.0'>CC-BY-SA-3.0</a> or <a target='_blank' href='http://www.gnu.org/copyleft/fdl.html'>GFDL</a>], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3ABonobo1_CincinnatiZoo.jpg'>See <i>P. paniscus</i> picture via Wikimedia Commons</a></p>");
        this.writeln("<p><i>Pan troglodytes</i> picture by Thomas Lersch (Own work) [<a target='_blank' href='http://www.gnu.org/copyleft/fdl.html'>GFDL</a>, <a target='_blank' href='http://creativecommons.org/licenses/by-sa/3.0/'>CC-BY-SA-3.0</a> or <a target='_blank' href='http://creativecommons.org/licenses/by/2.5'>CC-BY-2.5</a>], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3ASchimpanse_Zoo_Leipzig.jpg'>See <i>P. troglodytes</i> picture via Wikimedia Commons</a></p>");
        this.writeln("<p><i>Gorilla gorilla</i> picture by Brocken Inaglory (Own work) [<a target='_blank' href='http://creativecommons.org/licenses/by-sa/3.0'>CC-BY-SA-3.0</a> or <a target='_blank' href='http://www.gnu.org/copyleft/fdl.html'>GFDL</a>], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3AMale_gorilla_in_SF_zoo.jpg'>See <i>G. gorilla</i> picture via Wikimedia Commons</a></p>");
        this.writeln("<p><i>Pongo pygmaeus</i> picture by Greg Hume (Own work) [<a target='_blank' href='http://creativecommons.org/licenses/by-sa/3.0'>CC-BY-SA-3.0</a>], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3ASUMATRAN_ORANGUTAN.jpg'>See <i>P. pygmaeus</i> picture via Wikimedia Commons</a></p>");
        this.writeln("<p><i>Macaca mulatta</i> picture by Aiwok (Own work) [<a target='_blank' href='http://www.gnu.org/copyleft/fdl.html'>GFDL</a> or <a target='_blank' href='http://creativecommons.org/licenses/by-sa/3.0'>CC-BY-SA-3.0-2.5-2.0-1.0</a>], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3AMacaca_mulatta_3.JPG'>See <i>M. mulatta</i> picture via Wikimedia Commons</a></p>");
        this.writeln("<p><i>Rattus norvegicus</i> picture by Reg Mckenna (originally posted to Flickr as Wild Rat) [<a target='_blank' href='http://creativecommons.org/licenses/by/2.0'>CC-BY-2.0</a>], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3AWildRat.jpg'>See <i>R. norvegicus</i> picture via Wikimedia Commons</a></p>");
        this.writeln("<p><i>Bos taurus</i> picture by User Robert Merkel on en.wikipedia (US Department of Agriculture) [Public domain], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3AHereford_bull_large.jpg'>See <i>B. taurus</i> picture via Wikimedia Commons</a></p>");
        this.writeln("<p><i>Sus scrofa</i> picture by Joshua Lutz (Own work) [Public domain], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3ASus_scrofa_scrofa.jpg'>See <i>S. scrofa</i> picture via Wikimedia Commons</a></p>");
        this.writeln("<p><i>Monodelphis domestica</i> picture by <i>Marsupial Genome Sheds Light on the Evolution of Immunity.</i> Hill E, PLoS Biology Vol. 4/3/2006, e75 <a rel='nofollow' href='http://dx.doi.org/10.1371/journal.pbio.0040075'>http://dx.doi.org/10.1371/journal.pbio.0040075</a> [<a target='_blank' href='http://creativecommons.org/licenses/by/2.5'>CC-BY-2.5</a>], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3AOpossum_with_young.png'>See <i>M. domestica</i> picture via Wikimedia Commons</a></p>");
        this.writeln("<p><i>Ornithorhynchus anatinus</i> picture by Dr. Philip Bethge (private) [<a target='_blank' href='http://www.gnu.org/copyleft/fdl.html'>GFDL</a> or <a target='_blank' href='http://creativecommons.org/licenses/by-sa/3.0'>CC-BY-SA-3.0-2.5-2.0-1.0</a>], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3AOrnithorhynchus.jpg'>See <i>O. anatinus</i> picture via Wikimedia Commons</a></p>");
        this.writeln("<p><i>Gallus gallus</i> picture by Subramanya C K (Own work) [<a target='_blank' href='http://creativecommons.org/licenses/by-sa/3.0'>CC-BY-SA-3.0</a>], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3ARed_jungle_fowl.png'>See <i>G. gallus</i> picture via Wikimedia Commons</a></p>");
        this.writeln("<p><i>Anolis carolinensis</i> picture by PiccoloNamek (Moved from Image:P1010027.jpg) [<a target='_blank' href='http://www.gnu.org/copyleft/fdl.html'>GFDL</a> or <a target='_blank' href='http://creativecommons.org/licenses/by-sa/3.0/'>CC-BY-SA-3.0</a>], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3AAnolis_carolinensis.jpg'>See <i>A. carolinensis</i> picture via Wikimedia Commons</a></p>");
        this.writeln("<p><i>Xenopus tropicalis</i> picture by V&aacute;clav Gvo&zcaron;d&iacute;k (http://calphotos.berkeley.edu) [<a target='_blank' href='http://creativecommons.org/licenses/by-sa/2.5'>CC-BY-SA-2.5</a>, <a target='_blank' href='http://creativecommons.org/licenses/by-sa/2.5'>CC-BY-SA-2.5</a> or <a target='_blank' href='http://creativecommons.org/licenses/by-sa/3.0'>CC-BY-SA-3.0</a>], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3AXenopus_tropicalis01.jpeg'>See <i>X. tropicalis</i> picture via Wikimedia Commons</a></p>");
        this.writeln("<p><i>Tetraodon nigroviridis</i> picture by Starseed (Own work) [<a target='_blank' href='http://creativecommons.org/licenses/by-sa/3.0/de/deed.en'>CC-BY-SA-3.0-de</a> or <a target='_blank' href='http://creativecommons.org/licenses/by-sa/3.0'>CC-BY-SA-3.0</a>], <a target='_blank' href='http://commons.wikimedia.org/wiki/File%3ATetraodon_nigroviridis_1.jpg'>See <i>T. nigroviridis</i> picture via Wikimedia Commons</a></p>");
        this.writeln("</div>");

        // 
        this.endDisplay();
        log.exit();
    }
    
    /**
     * Generate the HTML figure tag with a figcaption tag from a {@code int} that is a 
     * species ID.
     * 
     * @param speciesId     An {@code int} that is the species ID of the species to be 
     *                      diplayed.
     * @return             A {@code String} that is the  HTML figure tag generated from the 
     *                     provided {@code int} of a species ID.
     */
    private String generateSpeciesFigure(int speciesId) {
        log.entry(speciesId);
        return log.exit(generateSpeciesFigure(Arrays.asList(speciesId), null, false));
    }
    
    /**
     * Generate the HTML figure tag from a {@code List} of species IDs.
     * 
     * @param speciesIds   A {@code List} of {@code Integer} containing the species IDs to
     *                     be diplayed.
     * @param figcaption   A {@code String} that is the fieldcaption of the figure. If empty 
     *                     or {@code null}, it's generated with the last species of the 
     *                     {@code List}.
     * @param isGroup      A {@code boolean} that is {@code true} if the figure represents 
     *                     a group of species.
     * @return             A {@code String} that is the  HTML figure tag generated from the 
     *                     provided {@code List} of species IDs.
     */
    private String generateSpeciesFigure(List<Integer> speciesIds, String figcaption, 
            boolean isGroup) {
        log.entry(speciesIds, figcaption, isGroup);
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
                        alternateNames="house mouse, mice";
                        break;
                    case 7955: 
                        name="Danio rerio";
                        shortName="D. rerio";
                        commonName="zebrafish";
                        alternateNames="leopard danio, zebra danio";
                        break;
                    case 7227: 
                        name="Drosophila melanogaster";
                        shortName="D. melanogaster";
                        commonName="fruitfly";
                        alternateNames="vinegar fly";
                        break;
                    case 6239: 
                        name="Caenorhabditis elegans";
                        shortName="C. elegans";
                        commonName="worm";
                        alternateNames="nematode, roundworm";
                        break;
                    case 9597: 
                        name="Pan paniscus";
                        shortName="P. paniscus";
                        commonName="bonobo";
                        alternateNames="pygmy chimpanzee";
                        break;
                    case 9598: 
                        name="Pan troglodytes";
                        shortName="P. troglodytes";
                        commonName="chimpanzee";
                        alternateNames="";
                        break;
                    case 9593: 
                        name="Gorilla gorilla";
                        shortName="G. gorilla";
                        commonName="gorilla";
                        alternateNames="western gorilla";
                        break;
                    case 9600: 
                        name="Pongo pygmaeus";
                        shortName="P. pygmaeus";
                        commonName="orangutan";
                        alternateNames="orang utan, orang-utan";
                        break;
                    case 9544: 
                        name="Macaca mulatta";
                        shortName="M. mulatta";
                        commonName="macaque";
                        alternateNames="rhesus monkey";
                        break;
                    case 10116: 
                        name="Rattus norvegicus";
                        shortName="R. norvegicus";
                        commonName="rat";
                        alternateNames="brown rat";
                        break;
                    case 9913: 
                        name="Bos taurus";
                        shortName="B. taurus";
                        commonName="cow";
                        alternateNames="domestic cow, domestic cattle, bovine cow";
                        break;
                    case 9823: 
                        name="Sus scrofa";
                        shortName="S. scrofa";
                        commonName="pig";
                        alternateNames="domestic pig, swine";
                        break;
                    case 13616: 
                        name="Monodelphis domestica";
                        shortName="M. domestica";
                        commonName="opossum";
                        alternateNames="gray short-tailed opossum, gray short tailed opossum";
                        break;
                    case 9258: 
                        name="Ornithorhynchus anatinus";
                        shortName="O. anatinus";
                        commonName="platypus";
                        alternateNames="duckbill platypus, duck-billed platypus";
                        break;
                    case 9031: 
                        name="Gallus gallus";
                        shortName="G. gallus";
                        commonName="chicken";
                        alternateNames="bantam, red junglefowl, red jungle fowl";
                        break;
                    case 28377: 
                        name="Anolis carolinensis";
                        shortName="A. carolinensis";
                        commonName="anolis";
                        alternateNames="green anole, carolina anole";
                        break;
                    case 8364: 
                        name="Xenopus tropicalis";
                        shortName="X. tropicalis";
                        commonName="xenopus";
                        alternateNames="western clawed frog";
                        break;
                    case 99883: 
                        name="Tetraodon nigroviridis";
                        shortName="T. nigroviridis";
                        commonName="tetraodon";
                        alternateNames="spotted green pufferfish";
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
                images.append(generateSpeciesImg(speciesId, name, shortName, commonName, alternateNames,true));
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
        return log.exit(figure.toString());
    }
    
    /**
     * Generate the HTML img tag of one species.
     * 
     * @return             A {@code String} that is the  HTML figure tag generated from the 
     *                     provided {@code List} of species IDs
     * @param id           An {@code int} of the species IDs to be diplayed.
     * @param name         A {@code String} that is the species name.
     * @param commonName   A {@code String} that is the species common name.
     * @param lightImg     A {@code boolean} that is {@code true} if the image to use is 
     *                     the light one.
     * @return             A {@code String} that is the  HTML img tag of the provided species 
     *                     data.
     */
    private String generateSpeciesImg(int id, String name, String shortName, 
            String commonName, String alternateNames, boolean lightImg) {
        log.entry(id, name, shortName, commonName, alternateNames, lightImg);
        StringBuilder image = new StringBuilder();
        image.append("<img src='");
        image.append(this.prop.getImagesRootDirectory());
        image.append("species/");
        image.append(commonName);
        if (lightImg) {
            image.append("_light");
        }
        image.append(".jpg' alt='");
        image.append(name);
        image.append("' data-bgeespeciesid='");
        image.append(id);
        image.append("' data-bgeespeciesname='");
        image.append(name);
        image.append("' data-bgeespeciesshortname='");
        image.append(shortName);
        image.append("' data-bgeespeciescommonname='");
        image.append(commonName);
        image.append("' data-bgeespeciesalternatenames='");
        image.append(alternateNames);
        image.append("' />");
        return log.exit(image.toString());
    }

    @Override
    public void includeJs()
    {
        log.entry();
        this.includeJs("jquery.min.js");
        this.includeJs("jquery.visible.js");
        this.includeJs("jquery-ui.min.js");
        this.includeJs("download.js");
        log.exit();
    }
}

