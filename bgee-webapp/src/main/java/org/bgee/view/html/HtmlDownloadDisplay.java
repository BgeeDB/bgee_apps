package org.bgee.view.html;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

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


		this.writeln("<div id='bgee_body'>");
		this.writeln("<h2>Temporary interface to access to new data.</h2>");
		this.writeln("<div id='bgee_uniq_species'>");
		this.writeln("<fieldset id='bgee_fieldset'>");
		this.writeln("<legend>Species</legend>");
		this.writeln("<div>");
		this.writeln("<figure><img src='"+BgeeProperties.getImagesRootDirectory()+"species/human.jpg' data-bgee-species id='9606' data-bgee-name='human' data-bgee-latin='Homo sapiens' alt='Homo sapiens'><figcaption><i>H. sapiens</i><br>(human)</figcaption></figure>");
		this.writeln("<figure><img src='"+BgeeProperties.getImagesRootDirectory()+"species/human.jpg' data-bgee-species id='10090' data-bgee-name='mouse' data-bgee-latin='Mus musculus' alt='Mus musculus'><figcaption><i>M. musculus</i><br>(mouse)</figcaption></figure>");
		this.writeln("<figure><img src='"+BgeeProperties.getImagesRootDirectory()+"species/human.jpg' data-bgee-species id='7955' data-bgee-name='zebrafish' data-bgee-latin='Danio rerio' alt='Danio rerio'><figcaption><i>D. rerio</i><br>(zebrafish)</figcaption></figure>");
		this.writeln("<figure><img src='"+BgeeProperties.getImagesRootDirectory()+"species/human.jpg' data-bgee-species id='8364' data-bgee-name='xenopus' data-bgee-latin='Xenopus tropicalis' alt='Xenopus tropicalis'><figcaption><i>X. tropicalis</i><br>(xenopus)</figcaption></figure>");
		this.writeln("<figure><img src='"+BgeeProperties.getImagesRootDirectory()+"species/human.jpg' data-bgee-species id='7227' data-bgee-name='fruitfly' data-bgee-latin='Drosophila melanogaster' alt='Drosophila melanogaster'><figcaption><i>D. melanogaster</i><br>(fruitfly)</figcaption></figure>");
		this.writeln("<figure><img src='"+BgeeProperties.getImagesRootDirectory()+"species/human.jpg' data-bgee-species id='9031' data-bgee-name='chicken' data-bgee-latin='Gallus gallus' alt='Gallus gallus'><figcaption><i>G. gallus</i><br>(chicken)</figcaption></figure>");
		this.writeln("<figure><img src='"+BgeeProperties.getImagesRootDirectory()+"species/human.jpg' data-bgee-species id='9593' data-bgee-name='gorilla' data-bgee-latin='Gorilla gorilla' alt='Gorilla gorilla'><figcaption><i>G. gorilla</i><br>(gorilla)</figcaption></figure>");
		this.writeln("<figure><img src='"+BgeeProperties.getImagesRootDirectory()+"species/human.jpg' data-bgee-species id='9544' data-bgee-name='macaque' data-bgee-latin='Macaca mulatta' alt='Macaca mulatta'><figcaption><i>M. mulatta</i><br>(macaque)</figcaption></figure>");
		this.writeln("<figure><img src='"+BgeeProperties.getImagesRootDirectory()+"species/human.jpg' data-bgee-species id='13616' data-bgee-name='opossum' data-bgee-latin='Monodelphis domestica' alt='Monodelphis domestica'><figcaption><i>M. domestica</i><br>(opossum)</figcaption></figure>");
		this.writeln("<figure><img src='"+BgeeProperties.getImagesRootDirectory()+"species/human.jpg' data-bgee-species id='9258' data-bgee-name='platypus' data-bgee-latin='Ornithorhynchus anatinus' alt='Ornithorhynchus anatinus'><figcaption><i>O. anatinus</i><br>(platypus)</figcaption></figure>");
		this.writeln("<figure><img src='"+BgeeProperties.getImagesRootDirectory()+"species/human.jpg' data-bgee-species id='9598' data-bgee-name='chimpanzee' data-bgee-latin='Pan troglodytes' alt='Pan troglodytes'><figcaption><i>P. troglodytes</i><br>(chimpanzee)</figcaption></figure>");
		this.writeln("<figure><img src='"+BgeeProperties.getImagesRootDirectory()+"species/human.jpg' data-bgee-species id='9597' data-bgee-name='bonobo' data-bgee-latin='Pan paniscus' alt='Pan paniscus'><figcaption><i>P. paniscus</i><br>(bonobo)</figcaption></figure>");
		this.writeln("<figure><img src='"+BgeeProperties.getImagesRootDirectory()+"species/human.jpg' data-bgee-species id='9600' data-bgee-name='orangutan' data-bgee-latin='Pongo pygmaeus' alt='Pongo pygmaeus'><figcaption><i>P. pygmaeus</i><br>(orangutan)</figcaption></figure>");
		this.writeln("<figure><img src='"+BgeeProperties.getImagesRootDirectory()+"species/human.jpg' data-bgee-species id='9913' data-bgee-name='cow' data-bgee-latin='Bos taurus' alt='Bos taurus'><figcaption><i>B. taurus</i><br>(cow)</figcaption></figure>");
		this.writeln("<figure><img src='"+BgeeProperties.getImagesRootDirectory()+"species/human.jpg' data-bgee-species id='10116' data-bgee-name='rat' data-bgee-latin='Rattus norvegicus' alt='Rattus norvegicus'><figcaption><i>R. norvegicus</i><br>(rat)</figcaption></figure>");
		this.writeln("<figure><img src='"+BgeeProperties.getImagesRootDirectory()+"species/human.jpg' data-bgee-species id='28377' data-bgee-name='anolis' data-bgee-latin='Anolis carolinensis' alt='Anolis carolinensis'><figcaption><i>A. carolinensis</i><br>(anolis)</figcaption></figure>");
		this.writeln("<figure><img src='"+BgeeProperties.getImagesRootDirectory()+"species/human.jpg' data-bgee-species id='99883' data-bgee-name='tetraodon' data-bgee-latin='Tetraodon nigroviridis' alt='Tetraodon nigroviridis'><figcaption><i>T. nigroviridis</i><br>(tetraodon)</figcaption></figure>");
		this.writeln("<figure><img src='"+BgeeProperties.getImagesRootDirectory()+"species/human.jpg' data-bgee-species id='9823' data-bgee-name='pig' data-bgee-latin='Sus scrofa' alt='Sus scrofa'><figcaption><i>S. scrofa</i><br>(pig)</figcaption></figure>");
		this.writeln("<figure><img src='"+BgeeProperties.getImagesRootDirectory()+"species/human.jpg' data-bgee-species id='6239' data-bgee-name='c.elegans' data-bgee-latin='Caenorhabditis elegans' alt='Caenorhabditis elegans'><figcaption><i>C. elegans</i><br>(c.elegans)</figcaption></figure>");
		this.writeln("</div>");
		this.writeln("</fieldset>");
		this.writeln("</div>");
		this.writeln("<div id='bgee_multi_species'>");
		this.writeln("<fieldset id='bgee_fieldset'>");
		this.writeln("<legend>Multi species</legend>");
		this.writeln("<div>");
		this.writeln("<figure><img src='"+BgeeProperties.getImagesRootDirectory()+"species/human.jpg' alt='Homo sapiens'><figcaption><i>H. sapiens</i><br>(Human)</figcaption></figure>");
		this.writeln("<figure><img src='"+BgeeProperties.getImagesRootDirectory()+"species/human.jpg' alt='Homo sapiens'><figcaption><i>H. sapiens</i><br>(Human)</figcaption></figure>");
		this.writeln("<figure><img src='"+BgeeProperties.getImagesRootDirectory()+"species/human.jpg' alt='Homo sapiens'><figcaption><i>H. sapiens</i><br>(Human)</figcaption></figure>");
		this.writeln("<figure><img src='"+BgeeProperties.getImagesRootDirectory()+"species/human.jpg' alt='Homo sapiens'><figcaption><i>H. sapiens</i><br>(Human)</figcaption></figure>");
		this.writeln("<figure><img src='"+BgeeProperties.getImagesRootDirectory()+"species/human.jpg' alt='Homo sapiens'><figcaption><i>H. sapiens</i><br>(Human)</figcaption></figure>");
		this.writeln("<figure><img src='"+BgeeProperties.getImagesRootDirectory()+"species/human.jpg' alt='Homo sapiens'><figcaption><i>H. sapiens</i><br>(Human)</figcaption></figure>");
		this.writeln("</div>");
		this.writeln("</fieldset>");
		this.writeln("</div>");
		this.writeln("<div id='bgee_data_selection'>");
		this.writeln("<div id='bgee_data_selection_left'>");
		this.writeln("<img src='"+BgeeProperties.getImagesRootDirectory()+"species/human.jpg' alt='Homo sapiens'>");
		this.writeln("</div>");
		this.writeln("<div id='bgee_data_selection_right'>");
		this.writeln("<h1>Homo sapiens</h1>");
		this.writeln("<ul>");
		this.writeln("<li><h2>Presence and absence of expression</h2>");
		this.writeln("<ul>");
		this.writeln("<li><a id='simple_pres_abs' href='http://www.isb-sib.ch/' alt='Presence and absence of expression in a simple file'>Simple file</a></li>");
		this.writeln("<li><a id='complete_pres_abs' href='http://www.isb-sib.ch/' alt='Presence and absence of expression in a complete file'>Complete file</a></li>");
		this.writeln("</ul></li>");
		this.writeln("<li><h2>Over- and under-expression</h2>");
		this.writeln("<ul>");
		this.writeln("<li><a id='simple_over_under' href='http://www.isb-sib.ch/' alt='Over- and under-expression in a simple file'>Simple file</a></li>");
		this.writeln("<li><a id='complete_over_under' href='http://www.isb-sib.ch/' alt='Over- and under-expression in a complete file'>Complete file</a></li>");
		this.writeln("</ul>");
		this.writeln("</li>");
		this.writeln("</ul>");
		this.writeln("</div>");
		this.writeln("</div>");
		this.writeln("</div>");

		this.endDisplay();
	}

	@Override
	public void includeJs()
	{
		this.includeJs("jquery");
		this.includeJs("download");
	}

}

