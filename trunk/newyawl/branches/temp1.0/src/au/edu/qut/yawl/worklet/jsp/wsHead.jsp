<%@ page import="au.edu.qut.yawl.worklet.exception.ExceptionService"%>
<%@ page import="au.edu.qut.yawl.worklet.WorkletService"%>
<meta name="Pragma" content="no-cache"/>
<meta name="Cache-Control" content="no-cache"/>
<meta name="Expires" content="0"/>
<link rel="stylesheet" href="./graphics/common.css"/>

<%!
    ExceptionService _exceptionService = null ;
    String _engineURI ;
    public void jspInit(){
                   ServletContext context = getServletContext();
        _exceptionService = (ExceptionService) context.getAttribute(
                "au.edu.qut.yawl.worklet.exception.ExceptionService");
        if(_exceptionService == null) {
            _exceptionService =  ExceptionService.getInst();
            context.setAttribute("au.edu.qut.yawl.workllet.exception.ExceptionService",
                    _exceptionService);
        }
    }
%>
  <script language="JavaScript">

        function isCompletedForm(formNme, radioGroupName){
            var oneChecked = false;
            var i = 0;
            //javascript or dom problem means one must access the properties of
            //a radio button different ways depending on whether one or more radios are in the group
            if(window.document[formNme].elements[radioGroupName].checked){
                oneChecked = true;
            }
            else{
                while( i < window.document[formNme].elements[radioGroupName].length){
                    if(window.document[formNme].elements[radioGroupName][i].checked == true){
                        oneChecked = true;
                    }
                    i++;
                }
            }
            if(! oneChecked){
                alert("You need to select one item.");
            }
            return oneChecked;
        }

    </script>
    <style type="TEXT/CSS"><!--
    .leftArea	{
        color:DarkGrey;
        background:#E8E8E8;
    }
    body{
        scrollbar-arrow-color:WHITE;
        scrollbar-track-color:#D6D6D6;
        scrollbar-shadow-color:#D6D6D6;
        scrollbar-face-color:#135184;
        scrollbar-highlight-color:#D6D6D6;
        scrollbar-darkshadow-color:#135184;
        scrollbar-3dlight-color:#135184;
    }
    --></style>