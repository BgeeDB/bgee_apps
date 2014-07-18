/**
 * This code is loaded and run along with the download page to handle
 * the user"s actions and update the display.
 * It is run when the document is fully loaded by using the method ready()
 * 
 * @author Mathieu Seppey
 * @version Bgee 13, Jul 2014
 */
$( document ).ready(function() {

	var $species = $( "figure" );

	$species.each(function() {
		var id = "";
		$( this ).find("img").each(function() {
			id = id + $( this ).data("bgeespeciesid") + "_";
		});
		id = id.slice(0, - 1);
		$( this ).attr('id', id);
	});

	var speciesId = window.location.hash.substr(3);

	if(speciesId){
		var $currentSpecies = $("#"+speciesId);
		loadDetails($currentSpecies);
	}

	// Add a click listener to every figures to load the corresponding species
	$species.click(function() {
		loadDetails($( this ));
	});

	var $bgeeSearchBox = $( "#bgee_search_box input" );

	$bgeeSearchBox.bind("keyup change paste cut focus", function() {
		search($(this).val());
	});

	function loadDetails($currentSpecies){
		
		var $species = $( "figure" );
		var id = $currentSpecies.attr( "id" );
		var bgeeGroupName = $currentSpecies.data( "bgeegroupname" );
		var $images = $currentSpecies.find("img");
		var bgeeSpeciesName = $images.data( "bgeespeciesname" );
		var bgeeSpeciesCommonNames = $images.data( "bgeespeciescommonname" );
		var $bgeeDataSelection = $("#bgee_data_selection");
		var $bgeeDataSelectionImg = $("#bgee_data_selection_img");
		var $bgeeDataSelectionText = $("#bgee_data_selection_text h1");
		var $exprSimpleCsv = $( "#expr_simple_csv" );
		var $exprSimpleTsv = $( "#expr_simple_tsv" );
		var $exprCompleteCsv = $( "#expr_complete_csv" );		
		var $exprCompleteTsv = $( "#expr_complete_tsv" );	
		var $overUnderSimpleCsv = $( "#overunder_simple_csv" );
		var $overUnderSimpleTsv = $( "#overunder_simple_tsv" );
		var $overUnderCompleteCsv = $( "#overunder_complete_csv" );		
		var $overUnderCompleteTsv = $( "#overunder_complete_tsv" );
		var urls = getUrls(id);
		

		$bgeeDataSelectionImg.empty();

		$images.each( function(){
			var quantity = $images.size();
			var $newElement = $( this ).clone();
			$bgeeDataSelectionImg.append($newElement);
			var newHeight = $newElement.height() / (Math.ceil(Math.sqrt(quantity)));
			if(quantity > 1){
				newHeight = newHeight * 0.8;
			}
			var newUrl = $newElement.attr("src").replace("_light","");
			$newElement.css("height",newHeight).css("width",newHeight);			
			$newElement.attr("src",newUrl);

		});

		if(bgeeGroupName){
			$bgeeDataSelectionText.text(bgeeGroupName);
		}
		else {
			$bgeeDataSelectionText.text(bgeeSpeciesName);
		}

		$exprSimpleCsv.attr("href",urls["expr_simple_csv"]);
		$exprSimpleTsv.attr("href",urls["expr_simple_tsv"]);
		$exprCompleteCsv.attr("href",urls["expr_complete_csv"]);		
		$exprCompleteTsv.attr("href",urls["expr_complete_tsv"]);	
		$overUnderSimpleCsv.attr("href",urls["overunder_simple_csv"]);
		$overUnderSimpleTsv.attr("href",urls["overunder_simple_tsv"]);
		$overUnderCompleteCsv.attr("href",urls["overunder_complete_csv"]);		
		$overUnderCompleteTsv.attr("href",urls["overunder_complete_tsv"]);	

		// Display the detail box with a visual effect
		
		$species.removeClass("highligth");
		$currentSpecies.addClass("highligth");

		var yPos = $currentSpecies.position().top;
		var $nextLineFirstElement = null;
		var $container = $("html, body");

		$currentSpecies.nextAll().each(function() {
			if($(this).position().top > yPos){
				$nextLineFirstElement = $(this);
				return false;
			}
		});

		if($nextLineFirstElement == null){
			$bgeeDataSelection.insertAfter( $currentSpecies.nextAll().andSelf().last() );

		}
		else{
			$bgeeDataSelection.insertBefore( $nextLineFirstElement );
		}

		$bgeeDataSelection.show( 120,function() {
			$container.animate({
				scrollTop: $bgeeDataSelection.offset().top - $container.offset().top 
				- (1.5 * $currentSpecies.height())
			},{duration:500}
			);			
		});

		window.location.hash = "#id"+$currentSpecies.attr( "id" );

	}

	function search(text) {
		var $species = $( "figure" );
		$species.removeClass("highligth");
		if(text){
			$( "figcaption:containsIN('"+text.toLowerCase()+"')" ).each(function (){ 
				$( this ).parent().addClass("highligth") 
			});
		}
	};

	function getUrls(id){

		var urls = { 
				"9606":
				{
					"expr_simple_csv" : "http://www.isb-sib.ch/?1",
					"expr_simple_tsv" : "http://www.isb-sib.ch/?2",
					"expr_complete_csv" : "http://www.isb-sib.ch/?3",
					"expr_complete_tsv" : "http://www.isb-sib.ch/?4",
					"overunder_simple_csv" : "http://www.isb-sib.ch/?5",
					"overunder_simple_tsv" : "http://www.isb-sib.ch/?6",
					"overunder_complete_csv" : "http://www.isb-sib.ch/?7",
					"overunder_complete_tsv" : "http://www.isb-sib.ch/?8"
				},
				"10090":
				{
					"expr_simple_csv" : "http://www.isb-sib.ch/?9",
					"expr_simple_tsv" : "http://www.isb-sib.ch/?10",
					"expr_complete_csv" : "http://www.isb-sib.ch/?11",
					"expr_complete_tsv" : "http://www.isb-sib.ch/?12",
					"overunder_simple_csv" : "http://www.isb-sib.ch/?13",
					"overunder_simple_tsv" : "http://www.isb-sib.ch/?14",
					"overunder_complete_csv" : "http://www.isb-sib.ch/?15",
					"overunder_complete_tsv" : "http://www.isb-sib.ch/?16",
				}
		}

		// temp, remove me
		if(id != "10090"){
			id = "9606"
		}

		return urls[id];
	}
	
	$.extend($.expr[":"], {
		"containsIN": function(elem, i, match, array) {
		return (elem.textContent || elem.innerText || "").toLowerCase().indexOf((match[3] || "").toLowerCase()) >= 0;
		}
		});

});