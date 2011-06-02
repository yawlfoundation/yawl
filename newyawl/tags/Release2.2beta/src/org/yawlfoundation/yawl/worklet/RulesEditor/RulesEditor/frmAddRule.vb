' This file is made available under the terms of the LGPL licence.
' This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
' The source remains the property of the YAWL Foundation.  The YAWL Foundation
' is a collaboration of individuals and organisations who are committed to  
' improving workflow technology.

Imports System.Xml
Imports System.IO

Friend Class frmAddRule
    Inherits System.Windows.Forms.Form


    ' The Add Rule form allows the addition of a new rule to a rule tree in the event
    ' that a selected worklet is rejected by the user. 
    '
    ' The Worklet Service Rules Editor
    ' Author: Michael Adams
    '         BPM Group, QUT Australia
    ' Version: 0.8, 25/08/2006


    ' store of workitem descriptors 
    Public Structure sWorkItem
        Public caseid As String
        Public worklets As Hashtable
        Public id As String
        Public specid As String
        Public specversion As String
        Public specuri As String
        Public taskid As String
        Public ruleType As String
        Public casedata() As String
    End Structure

    ' store info about the selection for the workitem
    Public workItem As sWorkItem                        ' workitem info read from 'selected' file
    Public loadedRuleType As String                     ' what rule is the tree handling
    Public loadedTask As String                         ' and what task (if any) is the tree for
    Public nextNodeId As Integer                        ' the id of the added node
    Public newNode As New RuleNode                      ' the node we're going to add to the tree
    Public isTrueBranch As Boolean                      ' is this an exception rule?

    Private lastTested As Integer                       ' the last tested and last satisfied nodes
    Private lastSatisfied As Integer                    ' that returned the selected worklet
    Private conclusion() As ConclusionItem              ' the conclusion for the new rule 
    Private cornerstone() As CompositeItem              ' and its cornerstone data
    Private IsOKtoClose As Boolean = True               ' flag to block close after failed validation

#Region " Windows Form Designer generated code "

    Public Sub New()
        MyBase.New()

        'This call is required by the Windows Form Designer.
        InitializeComponent()

        'Add any initialization after the InitializeComponent() call

    End Sub

    'Form overrides dispose to clean up the component list.
    Protected Overloads Overrides Sub Dispose(ByVal disposing As Boolean)
        If disposing Then
            If Not (components Is Nothing) Then
                components.Dispose()
            End If
        End If
        MyBase.Dispose(disposing)
    End Sub

    'Required by the Windows Form Designer
    Private components As System.ComponentModel.IContainer

    'NOTE: The following procedure is required by the Windows Form Designer
    'It can be modified using the Windows Form Designer.  
    'Do not modify it using the code editor.
    Friend WithEvents GroupBox2 As System.Windows.Forms.GroupBox
    Friend WithEvents lbxCornerstone As System.Windows.Forms.ListBox
    Friend WithEvents GroupBox1 As System.Windows.Forms.GroupBox
    Friend WithEvents lbxCurrent As System.Windows.Forms.ListBox
    Friend WithEvents GroupBox3 As System.Windows.Forms.GroupBox
    Friend WithEvents txtDesc As System.Windows.Forms.TextBox
    Friend WithEvents txtParent As System.Windows.Forms.TextBox
    Friend WithEvents txtCondition As System.Windows.Forms.TextBox
    Friend WithEvents txtID As System.Windows.Forms.TextBox
    Friend WithEvents Label6 As System.Windows.Forms.Label
    Friend WithEvents Label5 As System.Windows.Forms.Label
    Friend WithEvents Label4 As System.Windows.Forms.Label
    Friend WithEvents Label3 As System.Windows.Forms.Label
    Friend WithEvents Label2 As System.Windows.Forms.Label
    Friend WithEvents btnSave As System.Windows.Forms.Button
    Friend WithEvents btnLoad As System.Windows.Forms.Button
    Friend WithEvents btnCancel As System.Windows.Forms.Button
    Friend WithEvents Label7 As System.Windows.Forms.Label
    Friend WithEvents lblTaskName As System.Windows.Forms.Label
    Friend WithEvents txtRuleType As System.Windows.Forms.TextBox
    Friend WithEvents txtTaskName As System.Windows.Forms.TextBox
    Friend WithEvents txtConclusion As System.Windows.Forms.TextBox
    Friend WithEvents btnNew As System.Windows.Forms.Button
    <System.Diagnostics.DebuggerStepThrough()> Private Sub InitializeComponent()
        Dim resources As System.Resources.ResourceManager = New System.Resources.ResourceManager(GetType(frmAddRule))
        Me.GroupBox2 = New System.Windows.Forms.GroupBox
        Me.lbxCornerstone = New System.Windows.Forms.ListBox
        Me.GroupBox1 = New System.Windows.Forms.GroupBox
        Me.lbxCurrent = New System.Windows.Forms.ListBox
        Me.GroupBox3 = New System.Windows.Forms.GroupBox
        Me.txtConclusion = New System.Windows.Forms.TextBox
        Me.btnNew = New System.Windows.Forms.Button
        Me.txtDesc = New System.Windows.Forms.TextBox
        Me.txtParent = New System.Windows.Forms.TextBox
        Me.txtCondition = New System.Windows.Forms.TextBox
        Me.txtID = New System.Windows.Forms.TextBox
        Me.Label6 = New System.Windows.Forms.Label
        Me.Label5 = New System.Windows.Forms.Label
        Me.Label4 = New System.Windows.Forms.Label
        Me.Label3 = New System.Windows.Forms.Label
        Me.Label2 = New System.Windows.Forms.Label
        Me.btnSave = New System.Windows.Forms.Button
        Me.btnLoad = New System.Windows.Forms.Button
        Me.btnCancel = New System.Windows.Forms.Button
        Me.Label7 = New System.Windows.Forms.Label
        Me.lblTaskName = New System.Windows.Forms.Label
        Me.txtRuleType = New System.Windows.Forms.TextBox
        Me.txtTaskName = New System.Windows.Forms.TextBox
        Me.GroupBox2.SuspendLayout()
        Me.GroupBox1.SuspendLayout()
        Me.GroupBox3.SuspendLayout()
        Me.SuspendLayout()
        '
        'GroupBox2
        '
        Me.GroupBox2.Controls.Add(Me.lbxCornerstone)
        Me.GroupBox2.Location = New System.Drawing.Point(8, 56)
        Me.GroupBox2.Name = "GroupBox2"
        Me.GroupBox2.Size = New System.Drawing.Size(152, 200)
        Me.GroupBox2.TabIndex = 8
        Me.GroupBox2.TabStop = False
        Me.GroupBox2.Text = "Cornerstone Case"
        '
        'lbxCornerstone
        '
        Me.lbxCornerstone.BackColor = System.Drawing.SystemColors.ControlLight
        Me.lbxCornerstone.Location = New System.Drawing.Point(3, 16)
        Me.lbxCornerstone.Name = "lbxCornerstone"
        Me.lbxCornerstone.SelectionMode = System.Windows.Forms.SelectionMode.None
        Me.lbxCornerstone.Size = New System.Drawing.Size(141, 173)
        Me.lbxCornerstone.TabIndex = 0
        Me.lbxCornerstone.TabStop = False
        '
        'GroupBox1
        '
        Me.GroupBox1.Controls.Add(Me.lbxCurrent)
        Me.GroupBox1.Location = New System.Drawing.Point(176, 56)
        Me.GroupBox1.Name = "GroupBox1"
        Me.GroupBox1.Size = New System.Drawing.Size(152, 200)
        Me.GroupBox1.TabIndex = 0
        Me.GroupBox1.TabStop = False
        Me.GroupBox1.Text = "Current Case"
        '
        'lbxCurrent
        '
        Me.lbxCurrent.BackColor = System.Drawing.SystemColors.Window
        Me.lbxCurrent.Location = New System.Drawing.Point(3, 16)
        Me.lbxCurrent.Name = "lbxCurrent"
        Me.lbxCurrent.Size = New System.Drawing.Size(141, 173)
        Me.lbxCurrent.TabIndex = 0
        '
        'GroupBox3
        '
        Me.GroupBox3.Controls.Add(Me.txtConclusion)
        Me.GroupBox3.Controls.Add(Me.btnNew)
        Me.GroupBox3.Controls.Add(Me.txtDesc)
        Me.GroupBox3.Controls.Add(Me.txtParent)
        Me.GroupBox3.Controls.Add(Me.txtCondition)
        Me.GroupBox3.Controls.Add(Me.txtID)
        Me.GroupBox3.Controls.Add(Me.Label6)
        Me.GroupBox3.Controls.Add(Me.Label5)
        Me.GroupBox3.Controls.Add(Me.Label4)
        Me.GroupBox3.Controls.Add(Me.Label3)
        Me.GroupBox3.Controls.Add(Me.Label2)
        Me.GroupBox3.Location = New System.Drawing.Point(8, 264)
        Me.GroupBox3.Name = "GroupBox3"
        Me.GroupBox3.Size = New System.Drawing.Size(432, 200)
        Me.GroupBox3.TabIndex = 1
        Me.GroupBox3.TabStop = False
        Me.GroupBox3.Text = "New Rule Node"
        '
        'txtConclusion
        '
        Me.txtConclusion.Location = New System.Drawing.Point(80, 80)
        Me.txtConclusion.Multiline = True
        Me.txtConclusion.Name = "txtConclusion"
        Me.txtConclusion.ReadOnly = True
        Me.txtConclusion.Size = New System.Drawing.Size(240, 56)
        Me.txtConclusion.TabIndex = 9
        Me.txtConclusion.Text = ""
        '
        'btnNew
        '
        Me.btnNew.Enabled = False
        Me.btnNew.Location = New System.Drawing.Point(328, 112)
        Me.btnNew.Name = "btnNew"
        Me.btnNew.Size = New System.Drawing.Size(88, 23)
        Me.btnNew.TabIndex = 4
        Me.btnNew.Text = "&New..."
        '
        'txtDesc
        '
        Me.txtDesc.Location = New System.Drawing.Point(80, 144)
        Me.txtDesc.Multiline = True
        Me.txtDesc.Name = "txtDesc"
        Me.txtDesc.ReadOnly = True
        Me.txtDesc.Size = New System.Drawing.Size(336, 48)
        Me.txtDesc.TabIndex = 6
        Me.txtDesc.Text = ""
        '
        'txtParent
        '
        Me.txtParent.Location = New System.Drawing.Point(216, 18)
        Me.txtParent.Name = "txtParent"
        Me.txtParent.ReadOnly = True
        Me.txtParent.Size = New System.Drawing.Size(32, 20)
        Me.txtParent.TabIndex = 8
        Me.txtParent.TabStop = False
        Me.txtParent.Text = ""
        '
        'txtCondition
        '
        Me.txtCondition.Location = New System.Drawing.Point(80, 48)
        Me.txtCondition.Name = "txtCondition"
        Me.txtCondition.ReadOnly = True
        Me.txtCondition.Size = New System.Drawing.Size(336, 20)
        Me.txtCondition.TabIndex = 1
        Me.txtCondition.Text = ""
        '
        'txtID
        '
        Me.txtID.Location = New System.Drawing.Point(80, 19)
        Me.txtID.Name = "txtID"
        Me.txtID.ReadOnly = True
        Me.txtID.Size = New System.Drawing.Size(32, 20)
        Me.txtID.TabIndex = 6
        Me.txtID.TabStop = False
        Me.txtID.Text = ""
        '
        'Label6
        '
        Me.Label6.AutoSize = True
        Me.Label6.Location = New System.Drawing.Point(16, 144)
        Me.Label6.Name = "Label6"
        Me.Label6.Size = New System.Drawing.Size(64, 16)
        Me.Label6.TabIndex = 5
        Me.Label6.Text = "Description:"
        '
        'Label5
        '
        Me.Label5.AutoSize = True
        Me.Label5.Location = New System.Drawing.Point(16, 78)
        Me.Label5.Name = "Label5"
        Me.Label5.Size = New System.Drawing.Size(63, 16)
        Me.Label5.TabIndex = 2
        Me.Label5.Text = "Conclusion:"
        '
        'Label4
        '
        Me.Label4.AutoSize = True
        Me.Label4.Location = New System.Drawing.Point(16, 50)
        Me.Label4.Name = "Label4"
        Me.Label4.Size = New System.Drawing.Size(55, 16)
        Me.Label4.TabIndex = 0
        Me.Label4.Text = "Condition:"
        '
        'Label3
        '
        Me.Label3.AutoSize = True
        Me.Label3.Location = New System.Drawing.Point(128, 21)
        Me.Label3.Name = "Label3"
        Me.Label3.Size = New System.Drawing.Size(85, 16)
        Me.Label3.TabIndex = 1
        Me.Label3.Text = "Parent Node ID:"
        '
        'Label2
        '
        Me.Label2.AutoSize = True
        Me.Label2.Location = New System.Drawing.Point(16, 21)
        Me.Label2.Name = "Label2"
        Me.Label2.Size = New System.Drawing.Size(49, 16)
        Me.Label2.TabIndex = 0
        Me.Label2.Text = "Node ID:"
        '
        'btnSave
        '
        Me.btnSave.DialogResult = System.Windows.Forms.DialogResult.OK
        Me.btnSave.Enabled = False
        Me.btnSave.Location = New System.Drawing.Point(344, 96)
        Me.btnSave.Name = "btnSave"
        Me.btnSave.Size = New System.Drawing.Size(88, 23)
        Me.btnSave.TabIndex = 3
        Me.btnSave.Text = "&Save"
        '
        'btnLoad
        '
        Me.btnLoad.Location = New System.Drawing.Point(344, 64)
        Me.btnLoad.Name = "btnLoad"
        Me.btnLoad.Size = New System.Drawing.Size(88, 23)
        Me.btnLoad.TabIndex = 2
        Me.btnLoad.Text = "&Open..."
        '
        'btnCancel
        '
        Me.btnCancel.DialogResult = System.Windows.Forms.DialogResult.Cancel
        Me.btnCancel.Location = New System.Drawing.Point(344, 128)
        Me.btnCancel.Name = "btnCancel"
        Me.btnCancel.Size = New System.Drawing.Size(88, 23)
        Me.btnCancel.TabIndex = 4
        Me.btnCancel.Text = "Cancel"
        '
        'Label7
        '
        Me.Label7.AutoSize = True
        Me.Label7.Location = New System.Drawing.Point(8, 16)
        Me.Label7.Name = "Label7"
        Me.Label7.Size = New System.Drawing.Size(59, 16)
        Me.Label7.TabIndex = 12
        Me.Label7.Text = "Rule Type:"
        '
        'lblTaskName
        '
        Me.lblTaskName.AutoSize = True
        Me.lblTaskName.Enabled = False
        Me.lblTaskName.Location = New System.Drawing.Point(224, 16)
        Me.lblTaskName.Name = "lblTaskName"
        Me.lblTaskName.Size = New System.Drawing.Size(68, 16)
        Me.lblTaskName.TabIndex = 10
        Me.lblTaskName.Text = "Task Name: "
        '
        'txtRuleType
        '
        Me.txtRuleType.BackColor = System.Drawing.SystemColors.ControlLight
        Me.txtRuleType.Location = New System.Drawing.Point(72, 16)
        Me.txtRuleType.Name = "txtRuleType"
        Me.txtRuleType.ReadOnly = True
        Me.txtRuleType.Size = New System.Drawing.Size(128, 20)
        Me.txtRuleType.TabIndex = 13
        Me.txtRuleType.Text = ""
        '
        'txtTaskName
        '
        Me.txtTaskName.BackColor = System.Drawing.SystemColors.ControlLight
        Me.txtTaskName.Location = New System.Drawing.Point(304, 16)
        Me.txtTaskName.Name = "txtTaskName"
        Me.txtTaskName.ReadOnly = True
        Me.txtTaskName.Size = New System.Drawing.Size(128, 20)
        Me.txtTaskName.TabIndex = 14
        Me.txtTaskName.Text = ""
        '
        'frmAddRule
        '
        Me.AutoScaleBaseSize = New System.Drawing.Size(5, 13)
        Me.ClientSize = New System.Drawing.Size(448, 468)
        Me.Controls.Add(Me.txtTaskName)
        Me.Controls.Add(Me.txtRuleType)
        Me.Controls.Add(Me.Label7)
        Me.Controls.Add(Me.lblTaskName)
        Me.Controls.Add(Me.btnCancel)
        Me.Controls.Add(Me.btnSave)
        Me.Controls.Add(Me.btnLoad)
        Me.Controls.Add(Me.GroupBox3)
        Me.Controls.Add(Me.GroupBox1)
        Me.Controls.Add(Me.GroupBox2)
        Me.FormBorderStyle = System.Windows.Forms.FormBorderStyle.Fixed3D
        Me.Icon = CType(resources.GetObject("$this.Icon"), System.Drawing.Icon)
        Me.MaximizeBox = False
        Me.Name = "frmAddRule"
        Me.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen
        Me.Text = "Worklet Rules Editor <Add Rule> : "
        Me.GroupBox2.ResumeLayout(False)
        Me.GroupBox1.ResumeLayout(False)
        Me.GroupBox3.ResumeLayout(False)
        Me.ResumeLayout(False)

    End Sub

#End Region

    ' loads a 'selected' file into the editor. A 'selected' file contains information
    ' about the selection of the rejected worklet.
    Private Sub btnLoad_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles btnLoad.Click
        Dim x As XmlTextReader = Nothing
        Dim item As String = ""

        Try
            ' setup dialog to open a 'selected' file
            Dim ofd As New OpenFileDialog
            ofd.InitialDirectory = pathOf.repository & "\selected"
            ofd.Filter = "Worklet Selection Files (*.xws) | *.xws"
            If ofd.ShowDialog = Windows.Forms.DialogResult.OK Then

                ' open & read the file
                x = New XmlTextReader(ofd.FileName)
                x.WhitespaceHandling = WhitespaceHandling.None

                While x.Read
                    If x.NodeType = XmlNodeType.Element Then
                        item = x.Name
                        If item = "casedata" Then
                            workItem.casedata = ReadCaseData(x)               ' special handling required
                        ElseIf item = "worklets" Then
                            workItem.worklets = ReadWorklets(x)               ' running worklets for case
                        End If
                    ElseIf x.NodeType = XmlNodeType.Text Then

                        ' load data into structure
                        Select Case item
                            Case "id"
                                workItem.id = x.Value
                            Case "specid"
                                workItem.specid = x.Value
                            Case "specversion"
                                workItem.specversion = x.Value
                            Case "specuri"
                                workItem.specuri = x.Value
                            Case "caseid"
                                workItem.caseid = x.Value
                            Case "taskid"
                                workItem.taskid = x.Value
                            Case "extype"
                                workItem.ruleType = x.Value

                            Case "satisfied"
                                lastSatisfied = Convert.ToInt32(x.Value)
                            Case "tested"
                                lastTested = Convert.ToInt32(x.Value)

                                ' last tested node will be parent of new node
                                txtParent.Text = x.Value
                                isTrueBranch = (lastSatisfied = lastTested)
                        End Select
                    End If
                End While
                x.Close()

                ' check if this workitem relates to the ruletree loaded on main form
                If ValidFile(workItem, ofd.FileName) Then PostLoadGUI(ofd.FileName) 'setup gui
            End If
        Finally
            If Not x Is Nothing Then x.Close()
        End Try
    End Sub

    ' setup gui after selection file is loaded
    Private Sub PostLoadGUI(ByVal fName As String)
        txtID.Text = nextNodeId.ToString
        lbxCurrent.Items.AddRange(workItem.casedata)
        LoadCornerstone()
        Me.Text = "Add New Rule: " & extractFileName(fName)
        btnSave.Enabled = True
        btnNew.Enabled = True
        btnLoad.Enabled = False
        txtCondition.ReadOnly = False
        txtDesc.ReadOnly = False
    End Sub

    ' returns only the file name without path or extension
    Private Function extractFileName(ByVal fname As String) As String
        Dim s As String = fname.Substring(fname.LastIndexOf("\"c) + 1)    'remove path
        Return s.Substring(0, s.IndexOf(".xws"))
    End Function

    ' retrieve and display cornerstone data for the worklet originally selected
    Private Sub LoadCornerstone()
        Dim cornerstone() As CompositeItem = DirectCast(Owner, frmEdit).getCornerstone(lastSatisfied)

        If Not cornerstone Is Nothing Then
            For Each item As CompositeItem In cornerstone
                lbxCornerstone.Items.Add(item.Tag & " = " + item.Text)
            Next
        End If
    End Sub

    ' read each data item of the case from the file and add it into the listbox
    Private Function ReadCaseData(ByVal x As XmlTextReader) As String()
        Dim item As String = ""
        Dim list As New ArrayList

        While x.Read
            If x.NodeType = XmlNodeType.Element Then
                item = x.Name                                      ' name of item
            ElseIf x.NodeType = XmlNodeType.Text Then
                list.Add(item & " = " & x.Value)
            ElseIf x.NodeType = XmlNodeType.EndElement Then
                If x.Name = "casedata" Then                        ' done with casedata
                    Exit While
                End If
            End If
        End While

        Return list.ToArray(GetType(String))

    End Function

    ' read the set of pairs of caseid and worklet name for all worklets running for this case
    Private Function ReadWorklets(ByVal x As XmlTextReader) As Hashtable
        Dim item As String = ""
        Dim wName As String = ""
        Dim result As New Hashtable

        While x.Read
            If x.NodeType = XmlNodeType.Element Then
                item = x.Name                                      ' name of item
            ElseIf x.NodeType = XmlNodeType.Text Then
                If item = "workletName" Then
                    wName = x.Value
                ElseIf item = "runningcaseid" Then
                    result.Add(x.Value, wName)                     ' add pair (id, name)
                End If
            ElseIf x.NodeType = XmlNodeType.EndElement Then
                If x.Name = "worklets" Then                        ' done with worklets
                    Exit While
                End If
            End If
        End While

        Return result

    End Function


    ' returns true if the identifiers of the selected file loaded matches those of the rules file loaded
    Private Function ValidFile(ByVal wi As sWorkItem, ByVal fName As String) As Boolean
        Dim ruleType As RuleSetMgr.exType
        Dim rTypeStr As String
        Dim result As Boolean
        Dim specName As String

        ' check for matching specid and ruletype
        If wi.ruleType Is Nothing Then
            ruleType = RuleSetMgr.exType.Selection                       ' version 1 rules file
        Else
            ruleType = rsMgr.StringToTreeType(wi.ruleType)
        End If
        rTypeStr = rsMgr.TreeTypeToString(ruleType)

        specName = wi.specuri
        If wi.specuri Is Nothing Then
            specName = wi.specid
        End If

        ' check match on specid and rule type identifiers
        result = ((rsMgr.SpecName = specName) AndAlso (loadedRuleType = rTypeStr))

        ' if it's not a case level rule set then check the task id also
        If Not rsMgr.isCaseLevelTree(ruleType) Then
            result = result AndAlso (loadedTask = wi.taskid)
        End If

        ' bad match - tell the user
        If Not result Then
            MessageBox.Show("Selected file does not match the currently loaded rule set." _
            & vbCrLf & vbCrLf & _
            "   File: " & fName & vbCrLf & vbCrLf & _
            "   Current spec           : " & vbTab & rsMgr.SpecName & vbCrLf & _
            "   Selected file spec     : " & vbTab & wi.specuri & vbCrLf & vbCrLf & _
            "   Current rule type      : " & vbTab & loadedRuleType & vbCrLf & _
            "   Selected file rule type: " & vbTab & rTypeStr & vbCrLf & vbCrLf & _
            "   Current task           : " & vbTab & loadedTask & vbCrLf & _
            "   Selected file task     : " & vbTab & wi.taskid, "Load file error", _
            MessageBoxButtons.OK, MessageBoxIcon.Error)
        End If
        Return result
    End Function

    'copy selected case data item to condition textbox
    Private Sub lbxCurrent_SelectedIndexChanged(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles lbxCurrent.SelectedIndexChanged
        If txtCondition.Text.Length = 0 Then
            txtCondition.Text = lbxCurrent.SelectedItem.ToString
        Else
            txtCondition.Text &= " & " & lbxCurrent.SelectedItem.ToString          ' and 'em
        End If
    End Sub

    ' saves the new rule descriptors to the new node structure
    Private Sub btnSave_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles btnSave.Click
        Dim errMsg As String = ""

        ' validate inputs
        If txtCondition.Text.Length = 0 Then
            errMsg = "A condition must be added before the rule can be saved."
        ElseIf txtConclusion.Text.Length = 0 Then
            errMsg = "A conclusion must be provided before the rule can be saved."
        End If
        IsOKtoClose = (errMsg.Length = 0)
        If Not IsOKtoClose Then
            ShowError(errMsg)
            Exit Sub
        End If

        ' validates ok, so save the new rule to the node
        With newNode
            .id = txtID.Text
            .Parent = txtParent.Text
            .TrueChild = "-1"
            .FalseChild = "-1"
            .Condition = txtCondition.Text
            .Conclusion = conclusion
            .Description = txtDesc.Text
            .Cornerstone = buildCornerStone(lbxCurrent)
        End With
    End Sub

    ' close form and return to browse screen
    Private Sub btnCancel_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles btnCancel.Click
        Close()
    End Sub

    ' block close on bad validation
    Private Sub frmAddRule_Closing(ByVal sender As Object, ByVal e As System.ComponentModel.CancelEventArgs) Handles MyBase.Closing
        e.Cancel = Not IsOKtoClose                             ' inputs didn't validate
        IsOKtoClose = True                                     ' reset for next time
    End Sub

    ' update GUI as form loads
    Private Sub frmAddRule_Load(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles MyBase.Load
        Me.Text += rsMgr.SpecName
        txtRuleType.Text = loadedRuleType
        txtTaskName.Text = loadedTask
    End Sub

    ' allows the user to graphically define a new handling process
    Private Sub btnNew_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles btnNew.Click
        Dim frmWorklet As frmChooseWorklet
        Dim frmDraw As frmDrawConc

        ' if a selection rule, there's no graph to draw, so go directly to worklet list
        If loadedRuleType = "Selection" Then
            frmWorklet = New frmChooseWorklet
            If frmWorklet.ShowDialog = Windows.Forms.DialogResult.OK Then
                ReDim conclusion(0)
                conclusion(0) = New ConclusionItem("_1", "select", frmWorklet.workletSelections)
                txtConclusion.Text = ConclusionTextify(conclusion, ConclusionItem.TextFormat.pretty)
            End If
        Else
            ' let user draw the exception process
            frmDraw = New frmDrawConc
            If Not conclusion Is Nothing Then frmDraw.ShowGraph(conclusion) ' redisplay conclusion
            Me.Hide()
            frmDraw.ShowDialog()
            Me.Show()
            If Not frmDraw.finalConclusion Is Nothing Then
                conclusion = frmDraw.finalConclusion.Clone
                txtConclusion.Text = ConclusionTextify(conclusion, ConclusionItem.TextFormat.pretty)
            End If
        End If
    End Sub
End Class
