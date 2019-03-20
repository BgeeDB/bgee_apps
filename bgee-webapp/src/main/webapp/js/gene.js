/**
 * JavaScript for the gene page.
 * 
 * @author  Philippe Moret
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Mar. 2019
 * @since   Bgee 13, Dec. 2015
 */

$( document ).ready( function(){ 
    
	removeMaskedClass = function(text) {
        return text.replace('masked','');
    };

    changeUlToOl = function(text) {
        return text.replace('<ul','<ol').replace('</ul>','</ol>');
    };
    
    $('table.gene-search-result').DataTable( {
        //enable ordering but apply no ordering during initialization
        "order": [],
        responsive: {
            details: {
                display: $.fn.dataTable.Responsive.display.modal( {
                    header: function ( row ) {
                        var data = row.data();
                        return 'Details' + data[1];
                    }
                } ),
                renderer: function ( api, rowIdx, columns ) {
                    var data =
                        '<tr><td>' + columns[0].title + '</td><td>' + columns[0].data + '</td></tr>' +
                        '<tr><td>' + columns[1].title + '</td><td>' + columns[1].data + '</td></tr>' +
                        '<tr><td>' + columns[2].title + '</td><td>' + columns[2].data + '</td></tr>' +
                        '<tr><td>' + columns[4].title + '</td><td>' + columns[3].data + '</td></tr>';
                    return $('<table class="table"/>').append( data );
                }
            },
            breakpoints: [
                // We use wrap option. So, we don't want a responsive table for not mobile display.  
                //make the default datatable breakpoints to be the same as bootstrap
                { name: 'desktop',  width: Infinity },
                { name: 'tablet-l', width: Infinity },
                { name: 'tablet-p', width: Infinity },
                { name: 'mobile-l', width: 480 },
                { name: 'mobile-p', width: 320 },
                //(default datatable parameters: )
                //{ name: 'desktop',  width: Infinity },
                //{ name: 'tablet-l', width: 1024 },
                //{ name: 'tablet-p', width: 768 },
                //{ name: 'mobile-l', width: 480 },
                //{ name: 'mobile-p', width: 320 }

                //create breakpoints corresponding exactly to bootstrap
                { name: 'table_lg', width: Infinity },
                { name: 'table_md', width: Infinity },
                { name: 'table_sm', width: Infinity },
                { name: 'table_xs', width: Infinity }
            ]
        },
        columnDefs: [ // Higher responsivePriority are removed first, target define the order
            { responsivePriority: 2, targets: 0 }, // Ensembl ID
            { responsivePriority: 1, targets: 1 }, // Name
            { responsivePriority: 4, targets: 2 }, // Description
            { responsivePriority: 3, targets: 3 }, // Organism
            { responsivePriority: 5, targets: 4 }  // Match
        ],
        columns: [ // sorting definition
            { "orderable": true }, // Ensembl ID
            { "orderable": true }, // Name
            { "orderable": true},  // Description
            { "orderable": true },  // Organism
            { "orderable": true}  // Match
        ]
    });

    $('table.expression').DataTable( {
    	//enable ordering but apply no ordering during initialization
    	"order": [],
        responsive: {
            details: {
                display: $.fn.dataTable.Responsive.display.modal( {
                    header: function ( row ) {
                        var data = row.data();
                        return 'Details in ' + data[1];
                    }
                } ),
                renderer: function ( api, rowIdx, columns ) {
                	var data = 
                		'<tr><td>' + columns[0].title + '</td><td>' + columns[0].data + '</td></tr>' +
                		'<tr><td>' + columns[1].title + '</td><td>' + columns[1].data + '</td></tr>' +
                		'<tr><td>' + columns[2].title + '</td><td>' + changeUlToOl(removeMaskedClass(columns[2].data.replace('[+]',''))) + '</td></tr>' +
                		'<tr><td>' + columns[3].title + '</td><td>' + changeUlToOl(removeMaskedClass(columns[3].data)) + '</td></tr>' +
            			'<tr><td>' + columns[4].title + '</td><td>' + changeUlToOl(removeMaskedClass(columns[4].data)) + '</td></tr>';
                    return $('<table class="table"/>').append( data );
                }
            }, 
            breakpoints: [
                //make the default datatable breakpoints to be the same as bootstrap
                { name: 'desktop',  width: Infinity },
                { name: 'tablet-l', width: 992 },
                { name: 'tablet-p', width: 768 },
                { name: 'mobile-l', width: 480 },
                { name: 'mobile-p', width: 320 }, 
                //(default datatable parameters: )
                //{ name: 'desktop',  width: Infinity },
                //{ name: 'tablet-l', width: 1024 },
                //{ name: 'tablet-p', width: 768 },
                //{ name: 'mobile-l', width: 480 },
                //{ name: 'mobile-p', width: 320 }
                
                //create breakpoints corresponding exactly to bootstrap
                { name: 'table_lg', width: Infinity },
                { name: 'table_md', width: 1200 },
                { name: 'table_sm', width: 992 },
                { name: 'table_xs', width: 768 }
            ]
        },
        columnDefs: [ // Higher responsivePriority are removed first, target define the order
           { responsivePriority: 2, targets: 0 }, // Anat. entity ID
           { responsivePriority: 1, targets: 1 }, // Anatomical entity
           { responsivePriority: 5, targets: 2 }, // Developmental stage(s)
           { responsivePriority: 3, targets: 3 }, // Score
           { responsivePriority: 4, targets: 4 }  // Quality
        ],
        columns: [ // sorting definition
           { "orderable": false }, // Anatomical entity - null = default sorting
           { "orderable": false }, // Anat. entity ID - null = default sorting
           { "orderable": false }, // Developmental stage(s) - ordering disabled
           { "orderable": false},  //score - ordering disabled
           //score ordering disabled, otherwise, use: 
           //{ "orderDataType": "dom-text", "type": "score" }, // Score - custom function
           { "orderable": false } // Quality - ordering disabled
        ]
    });

    // expand the aggregated stages
    $('table.expression tbody').on('click', '.expandable', function () {
        var t = $(this).text();
        var t2 = t.replace('+','-');
        if (t != t2) {
            $(this).text(t2);
            $(this).parent().parent().find("ul").show(250);
        } else {
            $(this).text(t.replace('-','+'));
            $(this).parent().parent().find("ul").hide(250);
        }
    } );
    
    jQuery.fn.dataTableExt.oSort['score-asc'] = function(a, b) {
    	var x = parseHtmlScore(a);
    	var y = parseHtmlScore(b);
    	
    	return ((x < y) ? -1 : ((x > y) ? 1 : 0));
    };
     
    jQuery.fn.dataTableExt.oSort['score-desc'] = function(a, b) {
    	var x = parseHtmlScore(a);
    	var y = parseHtmlScore(b);
    	
        return ((x < y) ? 1 : ((x > y) ? -1 : 0));
    };

    loadAutocompleteGene();
} );

//XXX: certainly the parsing needs to be udpated now that rank scores are displayed 
//in scientific notation (only for those >= 1000).
function parseHtmlScore(htmlScore) {
	// Example: "<span class="low-qual-score">1,037.0</span>
	//           <ul class="masked score-list">
	//               <li class="score">1,037.0</li>
	//               <li class="score">21,200.0</li>
	//           </ul>"
	//substring: start including, end excluded
	//parseFloat: doesn't deal with US comma separator for thousands
	return parseFloat(htmlScore.substring(0, htmlScore.indexOf('<ul'))
			.replace(/<span class="low-qual-score">/g,'').replace(/<\/span>/g,'').replace(/,/g,''));
}
