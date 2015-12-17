/**
 * JavaScript for the gene page.
 * 
 * @author Philippe Moret
 * 
 */

var extraHidden = true;
var maskedHidden = true;

$( document ).ready( function(){ 
		// expand the aggregated column
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
	    	if (maskedHidden) {
	    		$("tr.aggregate.extra").filter($(":not(.masked)")).toggle(250);
	    		$("tr.aggregate.extra.masked").hide(250);

	    	} else {
	    		$("tr.aggregate.extra").toggle(250);
	    	}
	        extraHidden = !extraHidden;
	        var t = $(this).text();
		    var t2 = t.replace('Show','Hide');
		    if (t != t2) {
		        $(this).text(t2);
		    } else {
		       $(this).text(t.replace('Hide','Show'));
		    }
	    });
	    
	    $(".show_masked").click(function() {
	    	if (extraHidden) { 
	    		$("tr.aggregate.masked").filter($(":not(.extra)")).toggle(250);
	    		$("tr.aggregate.masked.extra").hide(250);
	    	} else {
	    		$("tr.aggregate.masked").toggle(250);
	    	}
	    	maskedHidden = !maskedHidden;
    	    var t = $(this).text();
		    var t2 = t.replace('Show','Hide');
		    if (t != t2) {
		        $(this).text(t2);
		    } else {
		       $(this).text(t.replace('Hide','Show'));
		    }
	    });
} );
