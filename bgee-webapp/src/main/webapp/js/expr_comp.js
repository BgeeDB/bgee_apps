/**
 * JavaScript for the expression comparison page.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 14, May 2019
 * @since   Bgee 14, May 2019
 */

$( document ).ready( function(){

    $('#bgee_expr_comp_submit').on("click",  function(){
        var messageElmt = "#bgee_expr_comp_msg";

        var message = '';

        // Check that something is written in the gene list textarea.
        if ($("#bgee_gene_list").val().replace(' ', '').length === 0) {
            message += "At least two Ensembl IDs should be provided. ";
        }
        // Check if there is an error.
        if (message !== '') {
            $(messageElmt).empty();
            $(messageElmt).attr("class", 'errorMessage').text(message);
            return false;
        }

        // Set loading img or text
        $(messageElmt).empty();
        $(messageElmt).text('').append($("<img />").attr("class", 'ajax_waiting')
            .attr("src", "img/wait.gif")
            .attr("alt", 'Loading'));
    });
    
    changeUlToOl = function(text) {
        return text.replace('<ul','<ol').replace('</ul>','</ol>');
    };
    
    removeMaskedClass = function(text) {
        return text.replace('masked','');
    };

    var dom =  "<'row'<'col-sm-3'i><'col-sm-3'l><'col-sm-6'f>>" +
        "<'row'<'col-sm-12'tr>>" +
        "<'row'<'col-sm-4'B><'col-sm-8'p>>";

    var order = [[ 1, 'desc'], [ 4, 'asc'], [ 2, 'desc']]; //  conservation score desc, genes with abs of expr asc, max expression score desc

    var responsive = {
        details: {
        	display: $.fn.dataTable.Responsive.display.modal( {
                header: function ( row ) {
                    var data = row.data();
                    return 'Details in ' + data[0];
                }
            } ),
            renderer: function ( api, rowIdx, columns ) {
            	
                var data = 
                        '<tr><td>'+columns[1].title+':'+'</td><td>'+columns[1].data+'</td></tr>' +
                        '<tr><td>'+columns[2].title+':'+'</td><td>'+columns[2].data+'</td></tr>' +
                        '<tr><td>'+columns[3].title+':'+'</td><td>'+changeUlToOl(removeMaskedClass(columns[3].data))+'</td></tr>' +
                        '<tr><td>'+columns[4].title+':'+'</td><td>'+changeUlToOl(removeMaskedClass(columns[4].data))+'</td></tr>' +
                        '<tr><td>'+columns[5].title+':'+'</td><td>'+changeUlToOl(removeMaskedClass(columns[5].data))+'</td></tr>';
                // if multispecies expression comparison
                if(columns.length == 15) {
                	data = data +
                		'<tr><td>'+columns[6].title+':'+'</td><td>'+changeUlToOl(removeMaskedClass(columns[6].data))+'</td></tr>' +
                		'<tr><td>'+columns[7].title+':'+'</td><td>'+changeUlToOl(removeMaskedClass(columns[7].data))+'</td></tr>';
                }
 
                return $('<table/>').append( data );
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
    };
    
    function getButtons(columns) {
        return [
            {
                extend: 'copyHtml5',
                text: '<span><i class="glyphicon glyphicon-copy"></i></span><span class="buttonLabel">Copy to clipboard</span>',
                exportOptions: { orthogonal: 'export', columns: columns }
            },
            {
                extend: 'csvHtml5',
                fieldSeparator: '\t',
                extension: '.tsv',
                text: '<span><i class="glyphicon glyphicon-floppy-save"></i></span><span class="buttonLabel">TSV</span>',
                exportOptions: { orthogonal: 'export', columns: columns }
            }
        ];
    }

    function renderGeneList(data, type, htmlTag) {
        // If export, we keep text only
        if (type === 'export') {
            var elements = $(htmlTag, $.parseHTML(data));
            var output = [];
            $.each(elements, function(idx, value) {
                output.push($(value).text());
            });
            return output.join(", ");
        }
        return data;
    }

    function renderAEList(data, type) {
        if (type === 'export') {
            return $($.parseHTML(data)).text();
        }
        return data;
    }

    // If you change any option of this table, take care to also change it for the single-species table
    $('table.expr_comp.multi-sp').DataTable( {
        order: order,
        oLanguage: {
            sSearch: "Filter:"
        },
        dom: dom,
        buttons: getButtons([0, 1, 2, 3, 4, 5, 6, 7, 9, 10, 11, 12, 13, 14]),
        responsive: responsive,
        columnDefs: [ // Higher responsivePriority are removed first, target define the order
           { width: "16%", responsivePriority: 1, targets: 0 }, // Anatomical entities
           { width: "5%", responsivePriority: 3, targets: 1 }, // Conservation score
           { width: "5%", responsivePriority: 2, targets: 2 }, // Max expression score
           { type: 'scientific', targets: 2 },    // sort using the scientific type
           { width: "14%", responsivePriority: 4, targets: 3 }, // Gene count with presence of expression
           { type: 'gene-number', targets: 3 },   // sort using the gene-number type
           { width: "14%", responsivePriority: 6, targets: 4 }, // Gene count with absence of expression
           { type: 'gene-number', targets: 4 },   // sort using the gene-number type
           { width: "14%", responsivePriority: 7, targets: 5 }, // Gene count with no data
           { type: 'gene-number', targets: 5 },   // sort using the gene-number type
           { width: "14%", responsivePriority: 8, targets: 6 }, // Species count with presence of expression
           { type: 'species-number', targets: 6 },// sort using the species-number type
           { width: "14%", responsivePriority: 9, targets: 7 }, // Species count with absence of expression
           { type: 'species-number', targets: 7 },// sort using the species-number type
           { width: "4%", responsivePriority: 5, targets: 8 }, // Details
           { responsivePriority: 10, targets: 9, visible: false, searchable: false }, // Anatomical entity IDs
           { responsivePriority: 10, targets: 10, visible: false, searchable: false }, // Gene count with presence of expression
           { responsivePriority: 10, targets: 11, visible: false, searchable: false }, // Gene count with absence of expression
           { responsivePriority: 10, targets: 12, visible: false, searchable: false }, // Gene count with no data
           { responsivePriority: 10, targets: 13, visible: false, searchable: false }, // Species count with presence of expression
           { responsivePriority: 10, targets: 14, visible: false, searchable: false } // Species count with absence of expression
        ],
        columns: [ // sorting definition
            // Anatomical entities
            { orderable: true, render: function(data, type, row) { return renderAEList(data, type); } },
            // Conservation score
            { orderable: true },
            // Max expression score
            { orderable: true },
            // Genes with presence of expression
            { orderable: true, render: function(data, type, row) { return renderGeneList(data, type, 'a'); } },
            // Genes with absence of expression
            { orderable: true, render: function(data, type, row) { return renderGeneList(data, type, 'a'); } },
            // Genes with no data
            { orderable: true, render: function(data, type, row) { return renderGeneList(data, type, 'a'); } },
            // Species with presence of expression
            { orderable: true, render: function(data, type, row) { return renderGeneList(data, type, 'em'); } },
            // Species with absence of expression
            { orderable: true, render: function(data, type, row) { return renderGeneList(data, type, 'em'); } },
            // Details
            { orderable: false },
            // Anatomical entity ID
            { orderable: false },
            // Gene count with presence of expression
            { orderable: false },
            // Gene count with absence of expression
            { orderable: false },
            // Gene count with no data
            { orderable: false },
            // Species count with presence of expression
            { orderable: false },
            // Species count with absence of expression
            { orderable: false }
        ]
    });

    // If you change any option of this table, take care to also change it for the multi-species table
    $('table.expr_comp.single-sp').DataTable( {
        order: order, //  score desc, expressed genes desc, rank asc
        dom: dom,
        oLanguage: {
            sSearch: "Filter:"
        },
        buttons: getButtons([0, 1, 2, 3, 4, 5, 7, 8, 9, 10]),
        responsive: responsive,
        columnDefs: [ // Higher responsivePriority are removed first, target define the order
            { width: "20%", responsivePriority: 1, targets: 0 }, // Anatomical entities
            { width: "7%", responsivePriority: 3, targets: 1 }, // Conservation score
            { width: "7%", responsivePriority: 2, targets: 2 }, // Max expression score
            { type: 'scientific', targets: 2 },    // sort using the scientific type
            { width: "20%", responsivePriority: 4, targets: 3 }, // Gene count with presence of expression
            { type: 'gene-number', targets: 3 },   // sort using the gene-number type
            { width: "20%", responsivePriority: 6, targets: 4 }, // Gene count with absence of expression
            { type: 'gene-number', targets: 4 },   // sort using the gene-number type
            { width: "20%", responsivePriority: 7, targets: 5 }, // Gene count with no data
            { type: 'gene-number', targets: 5 },   // sort using the gene-number type
            { width: "6%", responsivePriority: 5, targets: 6 }, // Details
            { responsivePriority: 8, targets: 7, visible: false, searchable: false }, // Anatomical entity IDs
            { responsivePriority: 8, targets: 8, visible: false, searchable: false }, // Gene count with presence of expression
            { responsivePriority: 8, targets: 9, visible: false, searchable: false }, // Gene count with absence of expression
            { responsivePriority: 8, targets: 10, visible: false, searchable: false } // Gene count with no data

        ],
        columns: [ // sorting definition
            // Anatomical entities
            { orderable: true, render: function(data, type, row) { return renderAEList(data, type); } },
            // Conservation score
            { "orderable": true },
            // Max expression score
            { "orderable": true },
            // Genes with presence of expression
            { orderable: true, render: function(data, type, row) { return renderGeneList(data, type, 'a'); } },
            // Genes with absence of expression
            { orderable: true, render: function(data, type, row) { return renderGeneList(data, type, 'a'); } },
            // Genes with no data
            { orderable: true, render: function(data, type, row) { return renderGeneList(data, type, 'a'); } },
            { "orderable": true }, // Details
            { "orderable": false }, // Anatomical entity IDs
            { "orderable": false }, // Gene count with presence of expression
            { "orderable": false }, // Gene count with absence of expression
            { "orderable": false }  // Gene count with no data
        ]
    });

    // expand the aggregated cells
    $('table tbody').on('click', '.expandable', function () {
        var t = $(this).text();
        var t2 = t.replace('+','-');
        if (t !== t2) {
            $(this).text(t2);
            $(this).parent().parent().find("ul").show(250);
        } else {
            $(this).text(t.replace('-','+'));
            $(this).parent().parent().find("ul").hide(250);
        }
    } );
} );

