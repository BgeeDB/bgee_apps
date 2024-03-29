#!/usr/bin/env perl

use strict;
use warnings;
use diagnostics;


my $email_address       = $ARGV[0]  // '';
my $alternative_display = $ARGV[1]  // '';
my $index               = $ARGV[2]  // '';
#Add $index to duplicate the javascript function if several different e-mails to hide per page
die "\n\tInvalid email address as argument\n\n"  if ( $email_address eq '' || $email_address !~ /^[\w\.-]+@[\w\.-]+\.[a-z]{2,5}$/i );


print transpose_eMails_delayed($email_address, $alternative_display, $index), "\n";
exit 0;


#cf. http://www.pgregg.com/projects/encode/htmlemail2.php & http://www.pgregg.com/projects/encode/htmlemail.php
sub transpose_eMails_delayed {
    my ($emails, $alternative, $index) = @_;

    my $href = "mailto:$emails"; #For mailto href here only
    #You can use a list of e-mails, comma separated, to send to several addresses from one click
    my $code = sprintf("function seb_transpose2$index(h) {var s='%s';var r='';for(var i=0;i<s.length;i++,i++){r=r+s.substring(i+1,i+2)+s.substring(i,i+1)}h.href=r;}document.write('<a href=\"#\" onMouseOver=\"javascript:seb_transpose2$index(this)\" onFocus=\"javascript:seb_transpose2$index(this)\">%s</a>');", &transpose($href), $alternative);

    $emails =~ s{@}{ [AT] }g;
    # Break e-mail addresses for noscript tag.
    my $userCode = sprintf('%s%s%s', "<script>eval(unescape('", &escapeencode($code), "'));</script><noscript>$emails</noscript>");

    return $userCode;
}

sub escapeencode {
    my ($strg) = @_;

    my $ret = '';
    my @arr = unpack('C*', $strg);
    for my $char (@arr){
        $ret .= sprintf('%%%X', $char);
    }

    return $ret;
}

sub transpose {
    my ($strg) = @_;

    # function takes the string and swaps the order of each group of 2 chars
    my $len = length($strg);
    my $ret = '';
    for (my $i=0; $i<$len; $i=$i+2){
        if ($i+1 == $len){
            $ret .= substr($strg, $i, 1);
        }
        else{
            $ret .= sprintf('%s%s', substr($strg, $i+1, 1), substr($strg, $i, 1));
        }
    }

    return $ret;
}

