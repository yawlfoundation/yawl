#!/usr/bin/perl
# 
# @author Dean Mao
# @created Oct 27, 2005
#

###
### WARNING: You do not want to run this in any subversion directory.  This
### should only be run in a temp directory where you do not care what happens
### in it.  I suggest using /tmp or /temp as the place to run this script.
### 


# Check out the latest src/test directories from subversion
#  - alternatively, you can use them to check out a specific revision
#    if modifications only apply to that revision.  For example, this
#    script was designed for revision 312 of yawl.

system("svn co http://yawlsvn.fit.qut.edu.org/yawl/runtime/src");
system("svn co http://yawlsvn.fit.qut.edu.org/yawl/runtime/test");

#
# This is the license header adding tool.  It will remove license pieces from
# the code and add the main LGPL header to each file
# 

# This is the license header
$header = <<HEADER;
/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

HEADER

open(LIST, "find . -print | grep .java | grep -v .svn |");
while(<LIST>) {
  chop;
  $filename = $_;
  print "Processing file $filename\n";
  {
    open(FH, "$filename");
    local($/);
    $_ = <FH>;
   
    # This funny substitution statement is to replace new lines with a code. 
    # The reason this is done is because the .* operator does not work on newline
    # characters.
    s/\n/BRRRRRANK/g;

    # Perform the regular expression matching to remove older license/copyright
    # statements with nothing.
    s/This file remains the.*\.edu\.org\/yawl\.//;
    s/Copyright.*rights reserved\.//;

    # Reinstate the newline characters.
    s/BRRRRRANK/\n/g;
    close(FH);

    # Remove the file and create a new file containing the header followed by
    # the contents of the file.
    system("rm -f $filename");
    open(WRITEFH, "+>$filename");
    print WRITEFH "$header\n$_";
    close(WRITEFH);
  }
}
close(LIST);

# Remove the .svn directories so that we can perform a tar of the directory and
# untar it in a working copy.  This is done to prevent subversion from thinking 
# that the local copy is already the most recent one.
#
open(LIST, "du | grep .svn | awk '{print \$2}' |");
while(<LIST>) {
  chop;
  system "rm -rf $_";
}

# Tar up the two directories so that it can be untarred in the working copy
# directory
system("tar cvf output.tar *");
