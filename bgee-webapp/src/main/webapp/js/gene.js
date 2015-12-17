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
	    
	   $(".show_more").click(function() {
		   if (extraHidden) {
			   extraHidden = false;
		    	$("tr.aggregate.extra").filter($(":not(.masked)")).show(250);
		    	$(this).text("Show redundant elements");
		   } else {
			   if (maskedHidden) {
				   maskedHidden = false;
		    		$("tr.aggregate.masked.extra").show(250);
				   $(this).text("Hide redundant and supplementary elements");
			   } else {
				   $("tr.aggregate.extra").hide(250);
				   $("tr.aggregate.masked").hide(250);
				   maskedHidden = true;
				   extraHidden = true;
		    	   $(this).text("Show more elements");
		      }
		   }
		   
	   });
	   
	   // show extra element of the expression table
	    $(".show_extra").click(function() {
	    	
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
