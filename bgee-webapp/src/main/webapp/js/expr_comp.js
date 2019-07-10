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
        // dom: 'lfBtipr',
        dom: "<'row'<'col-sm-3'l><'col-sm-6'f><'col-sm-3'B>>" +
            "<'row'<'col-sm-12'tr>>" +
            "<'row'<'col-sm-6'i><'col-sm-6'p>>",
        buttons: [
            { extend: 'copyHtml5', exportOptions: { orthogonal: 'export', columns: [0, 1, 2, 3, 4, 5, 6, 7, 9, 10, 11, 12, 13] } },
            { extend: 'csvHtml5', exportOptions: { orthogonal: 'export', columns: [0, 1, 2, 3, 4, 5, 6, 7, 9, 10, 11, 12, 13] } },
            { extend: 'excelHtml5', exportOptions: { orthogonal: 'export', columns: [0, 1, 2, 3, 4, 5, 6, 7, 9, 10, 11, 12, 13] } }
        ],
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
            { responsivePriority: 2, targets: 3 }, // Genes with presence of expression
            { responsivePriority: 2, targets: 4 }, // Genes with absence of expression
            { responsivePriority: 2, targets: 5 }, // Genes with no data
            { responsivePriority: 3, targets: 6 }, // Species with presence of expression
            { responsivePriority: 3, targets: 7 }, // Species with absence of expression
            { responsivePriority: 2, targets: 8 },  // Details
            { responsivePriority: 4, targets: 9, visible: false, searchable: false }, // Gene count with presence of expression
            { responsivePriority: 4, targets: 10, visible: false, searchable: false }, // Gene count with absence of expression
            { responsivePriority: 4, targets: 11, visible: false, searchable: false }, // Gene count with no data
            { responsivePriority: 4, targets: 12, visible: false, searchable: false }, // Species count with presence of expression
            { responsivePriority: 4, targets: 13, visible: false, searchable: false } // Species count with absence of expression
        ],
        columns: [ // sorting definition
            // Anatomical entities
            { orderable: true,
                render: function(data, type, row) {
                    if (type === 'export') {
                        return $($.parseHTML(data)).text();
                    }
                    return data;
                }
            },
            // Score
            { orderable: true },
            // Min rank
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
            { orderable: true, render: function(data, type, row) { return renderGeneList(data, type, 'a'); } },
            // Details
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
            { responsivePriority: 2, targets: 3 }, // Gene count with presence of expression
            { responsivePriority: 2, targets: 4 }, // Gene count with absence of expression
            { responsivePriority: 2, targets: 5 }, // Gene count with no data
            { responsivePriority: 2, targets: 6 }   // Details
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

