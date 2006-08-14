FCKEditor 2.3 patch information
-------------------------------

This directory contains a patched version of FCKEditor. Patches were made to support the XForms incremental
feature and attach the appropriate listeners as follows:

Patch 1
-------

in fckeditor\editor\js\fckeditorcode_gecko.js - the following lines had been added :

var oOnKeyDown = function(e) {
    FCK.Events.FireEvent("OnKeyDown");
};

this.EditorDocument.addEventListener('keydown', oOnKeyDown, true ) ;

Patch 2
-------

fckeditor\editor\js\fckeditorcode_ie.js - in the begining of the Doc_OnKeyDown() function, the following line had been added :

FCK.Events.FireEvent("OnKeyDown");
