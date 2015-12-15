/**
 * JavaScript for the gene page.
 * 
 * @author Philippe Moret
 * 
 */


$( document ).ready( function(){ 
		// expand the aggrageted column
	   $(".expandable").click(function() {
	        $(this).parent().parent().nextUntil("tr.aggregate").toggle(250);
	        var t = $(this).text();
	        var t2 = t.replace('+','-');
	        if (t != t2) {
	           $(this).text(t2);
	        } else {
	          $(this).text(t.replace('-','+'));
	        }
	    });
	    
	   // show extra element of the expression table
	    $(".show_extra").click(function() {
	        $("tr.aggregate.extra").toggle(250);
	        var t = $(this).text();
	        var t2 = t.replace('more','less');
	        if (t != t2) {
	           $(this).text(t2);
	        } else {
	          $(this).text(t.replace('less','more'));
	        }
	    });
} );
