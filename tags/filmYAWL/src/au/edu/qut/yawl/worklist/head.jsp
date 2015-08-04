<%@ page import="au.edu.qut.yawl.worklist.model.WorklistController"%>
<meta http-equiv="pragma" content="no-cache"/>
<meta http-equiv="expires" content="0"/>
<meta http-equiv="cache-control" content="no-cache"/>
<meta http-equiv="content-type" content="text/html; charset=ISO-8859-1"/>
<link rel="stylesheet" href="./graphics/common.css"/>
<%!
    WorklistController _worklistController = null;
    String _ixURI ;

    public void jspInit(){
        ServletContext context = getServletContext();
        _worklistController = (WorklistController) context.getAttribute(
                "au.edu.qut.yawl.worklist.model.WorklistController");
        
        if(_worklistController == null){
            _worklistController = new WorklistController();
            _worklistController.setUpInterfaceBClient(context.getInitParameter("InterfaceB_BackEnd"));
            _worklistController.setUpInterfaceAClient(context.getInitParameter("InterfaceA_BackEnd"));
            context.setAttribute("au.edu.qut.yawl.worklist.model.WorklistController",
                    _worklistController);
        }

        // initialise the exception service add-ins to the worklist
        _ixURI = context.getInitParameter("InterfaceX_BackEnd");
    }
%>
    <script language="JavaScript">
        <!--
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
        -->
    </script>
    <style type="text/css"><!--
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
