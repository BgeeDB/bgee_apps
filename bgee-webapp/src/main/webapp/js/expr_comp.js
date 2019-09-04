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
    
    // If you change any option of this table, take care to also change it for the single-species table
    $('table.expr_comp.multi-sp').DataTable( {
        "order": [[ 1, 'desc'], [ 3, 'desc'], [ 2, 'asc']], //  score desc, expressed genes desc, rank asc
        responsive: {
            details: {
                display: $.fn.dataTable.Responsive.display.childRowImmediate,
                type: 'none',
                target: ''
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
           { responsivePriority: 1, targets: 0 }, // Anatomical entities
           { responsivePriority: 1, targets: 1 }, // Score
           { responsivePriority: 1, targets: 2 }, // Min rank
           { type: 'scientific', targets: 2 },    // sort using the scientific type
           { responsivePriority: 2, targets: 3 }, // Gene count with presence of expression
           { type: 'gene-number', targets: 3 },   // sort using the gene-number type
           { responsivePriority: 2, targets: 4 }, // Gene count with absence of expression
           { responsivePriority: 2, targets: 5 }, // Gene count with no data
           { responsivePriority: 3, targets: 6 }, // Species count with presence of expression
           { responsivePriority: 3, targets: 7 }, // Species count with absence of expression
           { responsivePriority: 2, targets: 8 }  // Details
        ],
        columns: [ // sorting definition
           { "orderable": true }, // Anatomical entities
           { "orderable": true }, // Score
           { "orderable": true }, // Min rank
           { "orderable": true }, // Gene count with presence of expression
           { "orderable": true }, // Gene count with absence of expression
           { "orderable": true }, // Gene count with no data
           { "orderable": true }, // Species count with presence of expression
           { "orderable": true }, // Species count with absence of expression
           { "orderable": false } // Details
        ]
    });

    // If you change any option of this table, take care to also change it for the multi-species table
    $('table.expr_comp.single-sp').DataTable( {
        "order": [[ 1, 'desc'], [ 3, 'desc'], [ 2, 'asc']], //  score desc, expressed genes desc, rank asc
        responsive: {
            details: {
                display: $.fn.dataTable.Responsive.display.childRowImmediate,
                type: 'none',
                target: ''
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
            { responsivePriority: 1, targets: 0 }, // Anatomical entities
            { responsivePriority: 1, targets: 1 }, // Score
            { responsivePriority: 1, targets: 2 }, // Min rank
            { type: 'scientific', targets: 2 },    // sort using the scientific type
            { responsivePriority: 2, targets: 3 }, // Gene count with presence of expression
            { type: 'gene-number', targets: 3 },   // sort using the gene-number type
            { responsivePriority: 2, targets: 4 }, // Gene count with absence of expression
            { responsivePriority: 2, targets: 5 }, // Gene count with no data
            { responsivePriority: 2, targets: 6 }  // Details
        ],
        columns: [ // sorting definition
            { "orderable": true }, // Anatomical entities
            { "orderable": true }, // Score
            { "orderable": true }, // Min rank
            { "orderable": true }, // Gene count with presence of expression
            { "orderable": true }, // Gene count with absence of expression
            { "orderable": true }, // Gene count with no data
            { "orderable": false } // Details
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

