function tag(s){ return "<"+s+">" }
function xf_help(text){
   var fenster = window.open("", "help", "dependent=yes,width=200,height=200,status=no,toolbar=no")
   if (fenster!=null){
      fenster.document.open()
      fenster.document.write(tag("HTML")+tag("HEAD")+tag("TITLE")+'help'+tag("/TITLE")+tag("/HEAD")+tag("BODY"))
      fenster.document.write(tag("DIV class=\'help\'")+text+tag("/DIV"))
      fenster.document.write(tag("/BODY")+tag("/HTML"))
      fenster.document.close()
   }
}
