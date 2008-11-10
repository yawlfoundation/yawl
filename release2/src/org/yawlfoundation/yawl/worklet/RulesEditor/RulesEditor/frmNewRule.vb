' This file is made available under the terms of the LGPL licence.
' This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
' The source remains the property of the YAWL Foundation.  The YAWL Group is a 
' collaboration of individuals and organisations who are committed to improving 
' workflow technology.

Imports System.Xml
Imports System.IO

Friend Class frmNewRule
    Inherits System.Windows.Forms.Form

    ' This form provides the ability to create new rule sets for specifications.
    '
    ' The Worklet Service Rules Editor
    ' Author: Michael Adams
    '         BPM Group, QUT Australia
    ' Version: 0.8, 25/08/2006

    Public tempRSMgr As RuleSetMgr
    Public specName As String                 ' name of spec this ruleset is for
    Public currentTree As RuleTree            ' the tree we are currently building
    Public newNode As RuleNode                ' the current node being added
    Private ruleAdded As Boolean = False      ' rule added without being saved yet
    Private treeAdded As Boolean = False      ' tree added without being saved yet
    Private cancelClosing As Boolean = False  ' to allow a backout of a cancel 
    Public formCancelled As Boolean = False   ' override of dialog result
    Public nextNodeId As Integer = 1          ' node id of new node


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
    Friend WithEvents txtDesc As System.Windows.Forms.TextBox
    Friend WithEvents txtParent As System.Windows.Forms.TextBox
    Friend WithEvents txtCondition As System.Windows.Forms.TextBox
    Friend WithEvents txtID As System.Windows.Forms.TextBox
    Friend WithEvents Label6 As System.Windows.Forms.Label
    Friend WithEvents Label5 As System.Windows.Forms.Label
    Friend WithEvents Label4 As System.Windows.Forms.Label
    Friend WithEvents Label3 As System.Windows.Forms.Label
    Friend WithEvents Label2 As System.Windows.Forms.Label
    Friend WithEvents btnCancel As System.Windows.Forms.Button
    Friend WithEvents GroupBox4 As System.Windows.Forms.GroupBox
    Friend WithEvents txtSpecName As System.Windows.Forms.TextBox
    Friend WithEvents Label1 As System.Windows.Forms.Label
    Friend WithEvents btnAdd As System.Windows.Forms.Button
    Friend WithEvents Label10 As System.Windows.Forms.Label
    Friend WithEvents GroupBox5 As System.Windows.Forms.GroupBox
    Friend WithEvents tvRules As System.Windows.Forms.TreeView
    Friend WithEvents txtCSValue As System.Windows.Forms.TextBox
    Friend WithEvents txtCSLabel As System.Windows.Forms.TextBox
    Friend WithEvents Label9 As System.Windows.Forms.Label
    Friend WithEvents Label8 As System.Windows.Forms.Label
    Friend WithEvents lbxCornerstone As System.Windows.Forms.ListBox
    Friend WithEvents txtConclusion As System.Windows.Forms.TextBox
    Friend WithEvents btnNew As System.Windows.Forms.Button
    Friend WithEvents GroupBox1 As System.Windows.Forms.GroupBox
    Friend WithEvents btnAddRule As System.Windows.Forms.Button
    Friend WithEvents cbxRuleType As System.Windows.Forms.ComboBox
    Friend WithEvents cbxTaskName As System.Windows.Forms.ComboBox
    Friend WithEvents lblTaskName As System.Windows.Forms.Label
    Friend WithEvents imlNodes As System.Windows.Forms.ImageList
    Friend WithEvents grpRuleNode As System.Windows.Forms.GroupBox
    Friend WithEvents grpCornerstone As System.Windows.Forms.GroupBox
    Friend WithEvents btnDone As System.Windows.Forms.Button
    Friend WithEvents btnAddTree As System.Windows.Forms.Button
    Friend WithEvents btnZoom As System.Windows.Forms.Button
    Friend WithEvents tTip As System.Windows.Forms.ToolTip
    Friend WithEvents txtCompRule As System.Windows.Forms.TextBox
    <System.Diagnostics.DebuggerStepThrough()> Private Sub InitializeComponent()
        Me.components = New System.ComponentModel.Container
        Dim resources As System.Resources.ResourceManager = New System.Resources.ResourceManager(GetType(frmNewRule))
        Me.grpRuleNode = New System.Windows.Forms.GroupBox
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
        Me.btnDone = New System.Windows.Forms.Button
        Me.btnCancel = New System.Windows.Forms.Button
        Me.grpCornerstone = New System.Windows.Forms.GroupBox
        Me.lbxCornerstone = New System.Windows.Forms.ListBox
        Me.btnAdd = New System.Windows.Forms.Button
        Me.txtCSValue = New System.Windows.Forms.TextBox
        Me.txtCSLabel = New System.Windows.Forms.TextBox
        Me.Label9 = New System.Windows.Forms.Label
        Me.Label8 = New System.Windows.Forms.Label
        Me.GroupBox4 = New System.Windows.Forms.GroupBox
        Me.cbxTaskName = New System.Windows.Forms.ComboBox
        Me.cbxRuleType = New System.Windows.Forms.ComboBox
        Me.Label10 = New System.Windows.Forms.Label
        Me.txtSpecName = New System.Windows.Forms.TextBox
        Me.lblTaskName = New System.Windows.Forms.Label
        Me.Label1 = New System.Windows.Forms.Label
        Me.GroupBox5 = New System.Windows.Forms.GroupBox
        Me.tvRules = New System.Windows.Forms.TreeView
        Me.imlNodes = New System.Windows.Forms.ImageList(Me.components)
        Me.GroupBox1 = New System.Windows.Forms.GroupBox
        Me.btnAddRule = New System.Windows.Forms.Button
        Me.btnZoom = New System.Windows.Forms.Button
        Me.btnAddTree = New System.Windows.Forms.Button
        Me.tTip = New System.Windows.Forms.ToolTip(Me.components)
        Me.txtCompRule = New System.Windows.Forms.TextBox
        Me.grpRuleNode.SuspendLayout()
        Me.grpCornerstone.SuspendLayout()
        Me.GroupBox4.SuspendLayout()
        Me.GroupBox5.SuspendLayout()
        Me.GroupBox1.SuspendLayout()
        Me.SuspendLayout()
        '
        'grpRuleNode
        '
        Me.grpRuleNode.Anchor = CType((System.Windows.Forms.AnchorStyles.Top Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.grpRuleNode.Controls.Add(Me.txtConclusion)
        Me.grpRuleNode.Controls.Add(Me.btnNew)
        Me.grpRuleNode.Controls.Add(Me.txtDesc)
        Me.grpRuleNode.Controls.Add(Me.txtParent)
        Me.grpRuleNode.Controls.Add(Me.txtCondition)
        Me.grpRuleNode.Controls.Add(Me.txtID)
        Me.grpRuleNode.Controls.Add(Me.Label6)
        Me.grpRuleNode.Controls.Add(Me.Label5)
        Me.grpRuleNode.Controls.Add(Me.Label4)
        Me.grpRuleNode.Controls.Add(Me.Label3)
        Me.grpRuleNode.Controls.Add(Me.Label2)
        Me.grpRuleNode.Enabled = False
        Me.grpRuleNode.Location = New System.Drawing.Point(296, 8)
        Me.grpRuleNode.Name = "grpRuleNode"
        Me.grpRuleNode.Size = New System.Drawing.Size(424, 216)
        Me.grpRuleNode.TabIndex = 2
        Me.grpRuleNode.TabStop = False
        Me.grpRuleNode.Text = "New Rule Node"
        '
        'txtConclusion
        '
        Me.txtConclusion.BackColor = System.Drawing.SystemColors.Control
        Me.txtConclusion.Location = New System.Drawing.Point(80, 80)
        Me.txtConclusion.Multiline = True
        Me.txtConclusion.Name = "txtConclusion"
        Me.txtConclusion.ReadOnly = True
        Me.txtConclusion.Size = New System.Drawing.Size(240, 64)
        Me.txtConclusion.TabIndex = 11
        Me.txtConclusion.Text = ""
        '
        'btnNew
        '
        Me.btnNew.Location = New System.Drawing.Point(328, 120)
        Me.btnNew.Name = "btnNew"
        Me.btnNew.Size = New System.Drawing.Size(88, 23)
        Me.btnNew.TabIndex = 10
        Me.btnNew.Text = "&New..."
        '
        'txtDesc
        '
        Me.txtDesc.Location = New System.Drawing.Point(80, 152)
        Me.txtDesc.Multiline = True
        Me.txtDesc.Name = "txtDesc"
        Me.txtDesc.Size = New System.Drawing.Size(336, 56)
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
        Me.txtParent.Text = "0"
        '
        'txtCondition
        '
        Me.txtCondition.Location = New System.Drawing.Point(80, 48)
        Me.txtCondition.Name = "txtCondition"
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
        Me.txtID.Text = "1"
        '
        'Label6
        '
        Me.Label6.AutoSize = True
        Me.Label6.Location = New System.Drawing.Point(16, 160)
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
        'btnDone
        '
        Me.btnDone.Enabled = False
        Me.btnDone.Location = New System.Drawing.Point(336, 504)
        Me.btnDone.Name = "btnDone"
        Me.btnDone.Size = New System.Drawing.Size(88, 23)
        Me.btnDone.TabIndex = 3
        Me.btnDone.Text = "&Save && Close"
        Me.tTip.SetToolTip(Me.btnDone, "Complete and close ")
        '
        'btnCancel
        '
        Me.btnCancel.DialogResult = System.Windows.Forms.DialogResult.Cancel
        Me.btnCancel.Location = New System.Drawing.Point(624, 504)
        Me.btnCancel.Name = "btnCancel"
        Me.btnCancel.Size = New System.Drawing.Size(88, 23)
        Me.btnCancel.TabIndex = 4
        Me.btnCancel.Text = "Cancel"
        Me.tTip.SetToolTip(Me.btnCancel, "Discard all additions")
        '
        'grpCornerstone
        '
        Me.grpCornerstone.Anchor = CType((System.Windows.Forms.AnchorStyles.Top Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.grpCornerstone.Controls.Add(Me.lbxCornerstone)
        Me.grpCornerstone.Controls.Add(Me.btnAdd)
        Me.grpCornerstone.Controls.Add(Me.txtCSValue)
        Me.grpCornerstone.Controls.Add(Me.txtCSLabel)
        Me.grpCornerstone.Controls.Add(Me.Label9)
        Me.grpCornerstone.Controls.Add(Me.Label8)
        Me.grpCornerstone.Enabled = False
        Me.grpCornerstone.Location = New System.Drawing.Point(296, 232)
        Me.grpCornerstone.Name = "grpCornerstone"
        Me.grpCornerstone.Size = New System.Drawing.Size(424, 136)
        Me.grpCornerstone.TabIndex = 1
        Me.grpCornerstone.TabStop = False
        Me.grpCornerstone.Text = "Cornerstone Case Data"
        '
        'lbxCornerstone
        '
        Me.lbxCornerstone.BackColor = System.Drawing.SystemColors.Control
        Me.lbxCornerstone.Location = New System.Drawing.Point(232, 16)
        Me.lbxCornerstone.Name = "lbxCornerstone"
        Me.lbxCornerstone.Size = New System.Drawing.Size(184, 108)
        Me.lbxCornerstone.TabIndex = 10
        Me.lbxCornerstone.TabStop = False
        '
        'btnAdd
        '
        Me.btnAdd.Location = New System.Drawing.Point(144, 88)
        Me.btnAdd.Name = "btnAdd"
        Me.btnAdd.TabIndex = 4
        Me.btnAdd.Text = "&Add -->"
        '
        'txtCSValue
        '
        Me.txtCSValue.Location = New System.Drawing.Point(72, 56)
        Me.txtCSValue.Name = "txtCSValue"
        Me.txtCSValue.Size = New System.Drawing.Size(144, 20)
        Me.txtCSValue.TabIndex = 3
        Me.txtCSValue.Text = ""
        '
        'txtCSLabel
        '
        Me.txtCSLabel.Location = New System.Drawing.Point(72, 24)
        Me.txtCSLabel.Name = "txtCSLabel"
        Me.txtCSLabel.Size = New System.Drawing.Size(144, 20)
        Me.txtCSLabel.TabIndex = 1
        Me.txtCSLabel.Text = ""
        '
        'Label9
        '
        Me.Label9.AutoSize = True
        Me.Label9.Location = New System.Drawing.Point(16, 56)
        Me.Label9.Name = "Label9"
        Me.Label9.Size = New System.Drawing.Size(36, 16)
        Me.Label9.TabIndex = 2
        Me.Label9.Text = "Value:"
        '
        'Label8
        '
        Me.Label8.AutoSize = True
        Me.Label8.Location = New System.Drawing.Point(16, 24)
        Me.Label8.Name = "Label8"
        Me.Label8.Size = New System.Drawing.Size(50, 16)
        Me.Label8.TabIndex = 0
        Me.Label8.Text = "Attribute:"
        '
        'GroupBox4
        '
        Me.GroupBox4.Controls.Add(Me.cbxTaskName)
        Me.GroupBox4.Controls.Add(Me.cbxRuleType)
        Me.GroupBox4.Controls.Add(Me.Label10)
        Me.GroupBox4.Controls.Add(Me.txtSpecName)
        Me.GroupBox4.Controls.Add(Me.lblTaskName)
        Me.GroupBox4.Controls.Add(Me.Label1)
        Me.GroupBox4.Location = New System.Drawing.Point(8, 8)
        Me.GroupBox4.Name = "GroupBox4"
        Me.GroupBox4.Size = New System.Drawing.Size(280, 120)
        Me.GroupBox4.TabIndex = 0
        Me.GroupBox4.TabStop = False
        Me.GroupBox4.Text = "Process Identifiers"
        '
        'cbxTaskName
        '
        Me.cbxTaskName.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList
        Me.cbxTaskName.Location = New System.Drawing.Point(108, 88)
        Me.cbxTaskName.Name = "cbxTaskName"
        Me.cbxTaskName.Size = New System.Drawing.Size(164, 21)
        Me.cbxTaskName.TabIndex = 8
        '
        'cbxRuleType
        '
        Me.cbxRuleType.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList
        Me.cbxRuleType.Location = New System.Drawing.Point(108, 56)
        Me.cbxRuleType.Name = "cbxRuleType"
        Me.cbxRuleType.Size = New System.Drawing.Size(164, 21)
        Me.cbxRuleType.TabIndex = 7
        '
        'Label10
        '
        Me.Label10.Location = New System.Drawing.Point(4, 56)
        Me.Label10.Name = "Label10"
        Me.Label10.TabIndex = 6
        Me.Label10.Text = "Rule Type:"
        '
        'txtSpecName
        '
        Me.txtSpecName.CausesValidation = False
        Me.txtSpecName.Location = New System.Drawing.Point(108, 24)
        Me.txtSpecName.Name = "txtSpecName"
        Me.txtSpecName.ReadOnly = True
        Me.txtSpecName.Size = New System.Drawing.Size(164, 20)
        Me.txtSpecName.TabIndex = 1
        Me.txtSpecName.Text = ""
        '
        'lblTaskName
        '
        Me.lblTaskName.Location = New System.Drawing.Point(4, 88)
        Me.lblTaskName.Name = "lblTaskName"
        Me.lblTaskName.TabIndex = 2
        Me.lblTaskName.Text = "Task Name:"
        '
        'Label1
        '
        Me.Label1.Location = New System.Drawing.Point(4, 24)
        Me.Label1.Name = "Label1"
        Me.Label1.Size = New System.Drawing.Size(112, 23)
        Me.Label1.TabIndex = 0
        Me.Label1.Text = "Specification Name:"
        '
        'GroupBox5
        '
        Me.GroupBox5.Controls.Add(Me.tvRules)
        Me.GroupBox5.Location = New System.Drawing.Point(8, 136)
        Me.GroupBox5.Name = "GroupBox5"
        Me.GroupBox5.Size = New System.Drawing.Size(280, 392)
        Me.GroupBox5.TabIndex = 10
        Me.GroupBox5.TabStop = False
        Me.GroupBox5.Text = "RDR Tree"
        '
        'tvRules
        '
        Me.tvRules.Anchor = CType((((System.Windows.Forms.AnchorStyles.Top Or System.Windows.Forms.AnchorStyles.Bottom) _
                    Or System.Windows.Forms.AnchorStyles.Left) _
                    Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.tvRules.BackColor = System.Drawing.SystemColors.Window
        Me.tvRules.HideSelection = False
        Me.tvRules.ImageList = Me.imlNodes
        Me.tvRules.Location = New System.Drawing.Point(8, 16)
        Me.tvRules.Name = "tvRules"
        Me.tvRules.Size = New System.Drawing.Size(264, 368)
        Me.tvRules.TabIndex = 1
        '
        'imlNodes
        '
        Me.imlNodes.ImageSize = New System.Drawing.Size(16, 16)
        Me.imlNodes.ImageStream = CType(resources.GetObject("imlNodes.ImageStream"), System.Windows.Forms.ImageListStreamer)
        Me.imlNodes.TransparentColor = System.Drawing.Color.Transparent
        '
        'GroupBox1
        '
        Me.GroupBox1.Anchor = CType(((System.Windows.Forms.AnchorStyles.Bottom Or System.Windows.Forms.AnchorStyles.Left) _
                    Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.GroupBox1.Controls.Add(Me.txtCompRule)
        Me.GroupBox1.Location = New System.Drawing.Point(296, 376)
        Me.GroupBox1.Name = "GroupBox1"
        Me.GroupBox1.Size = New System.Drawing.Size(424, 120)
        Me.GroupBox1.TabIndex = 11
        Me.GroupBox1.TabStop = False
        Me.GroupBox1.Text = "Effective Composite Rule"
        '
        'btnAddRule
        '
        Me.btnAddRule.Enabled = False
        Me.btnAddRule.Location = New System.Drawing.Point(528, 504)
        Me.btnAddRule.Name = "btnAddRule"
        Me.btnAddRule.Size = New System.Drawing.Size(88, 23)
        Me.btnAddRule.TabIndex = 12
        Me.btnAddRule.Text = "Add &Rule"
        Me.tTip.SetToolTip(Me.btnAddRule, "Save New Rule")
        '
        'btnZoom
        '
        Me.btnZoom.Image = CType(resources.GetObject("btnZoom.Image"), System.Drawing.Image)
        Me.btnZoom.Location = New System.Drawing.Point(296, 504)
        Me.btnZoom.Name = "btnZoom"
        Me.btnZoom.Size = New System.Drawing.Size(24, 23)
        Me.btnZoom.TabIndex = 13
        Me.tTip.SetToolTip(Me.btnZoom, "Open Tree Viewer Screen")
        '
        'btnAddTree
        '
        Me.btnAddTree.Enabled = False
        Me.btnAddTree.Location = New System.Drawing.Point(432, 504)
        Me.btnAddTree.Name = "btnAddTree"
        Me.btnAddTree.Size = New System.Drawing.Size(88, 23)
        Me.btnAddTree.TabIndex = 14
        Me.btnAddTree.Text = "Add &Tree"
        Me.tTip.SetToolTip(Me.btnAddTree, "Save New Tree")
        '
        'txtCompRule
        '
        Me.txtCompRule.BackColor = System.Drawing.SystemColors.ControlLight
        Me.txtCompRule.Location = New System.Drawing.Point(8, 16)
        Me.txtCompRule.Multiline = True
        Me.txtCompRule.Name = "txtCompRule"
        Me.txtCompRule.ReadOnly = True
        Me.txtCompRule.ScrollBars = System.Windows.Forms.ScrollBars.Both
        Me.txtCompRule.Size = New System.Drawing.Size(408, 96)
        Me.txtCompRule.TabIndex = 2
        Me.txtCompRule.Text = ""
        Me.txtCompRule.WordWrap = False
        '
        'frmNewRule
        '
        Me.AutoScaleBaseSize = New System.Drawing.Size(5, 13)
        Me.ClientSize = New System.Drawing.Size(726, 532)
        Me.Controls.Add(Me.btnAddTree)
        Me.Controls.Add(Me.btnZoom)
        Me.Controls.Add(Me.btnAddRule)
        Me.Controls.Add(Me.GroupBox1)
        Me.Controls.Add(Me.GroupBox5)
        Me.Controls.Add(Me.GroupBox4)
        Me.Controls.Add(Me.grpCornerstone)
        Me.Controls.Add(Me.btnCancel)
        Me.Controls.Add(Me.btnDone)
        Me.Controls.Add(Me.grpRuleNode)
        Me.FormBorderStyle = System.Windows.Forms.FormBorderStyle.Fixed3D
        Me.Icon = CType(resources.GetObject("$this.Icon"), System.Drawing.Icon)
        Me.MaximizeBox = False
        Me.Name = "frmNewRule"
        Me.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen
        Me.Text = "Worklet Rules Editor: Create New Rule Set"
        Me.grpRuleNode.ResumeLayout(False)
        Me.grpCornerstone.ResumeLayout(False)
        Me.GroupBox4.ResumeLayout(False)
        Me.GroupBox5.ResumeLayout(False)
        Me.GroupBox1.ResumeLayout(False)
        Me.ResumeLayout(False)

    End Sub

#End Region

    '***********************************************************************************************
    '****** BUTTON EVENTS **************************************************************************
    '***********************************************************************************************

    ' save additions and return to main form
    Private Sub btnDone_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles btnDone.Click
        Dim response As DialogResult

        ' check to add unsaved additions
        If btnAddTree.Enabled Then
            response = MessageBox.Show("The current tree has not yet been committed to the Rule Set for this " & _
                                       "specification. Would you like to commit the tree to the Rule Set?", _
                                       "Commit Current Tree?", _
                                       MessageBoxButtons.YesNo, MessageBoxIcon.Question)
            If response = Windows.Forms.DialogResult.Yes Then btnAddTree.PerformClick()
            'Exit Sub
        End If

        If treeAdded Then                               ' at least one tree has been added
            rsMgr = DeepClone(tempRSMgr)                ' copy changes back to manager
            rsMgr.SaveRulesToFile()                     ' and save them to file
            Close()
        End If

    End Sub

    ' opens the drawing form to draw a conclusion
    Private Sub btnNew_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles btnNew.Click
        Dim frmWorklet As frmChooseWorklet
        Dim frmDraw As frmDrawConc
        Dim result As DialogResult

        ' if a selection rule, there's no graph to draw - show worklet choice form directly
        If cbxRuleType.SelectedItem = "Selection" Then
            frmWorklet = New frmChooseWorklet
            If frmWorklet.ShowDialog = Windows.Forms.DialogResult.OK Then
                ReDim newNode.Conclusion(0)                          ' selection only has one item
                newNode.Conclusion(0) = New ConclusionItem("_1", "select", frmWorklet.workletSelections)
                txtConclusion.Text = ConclusionTextify(newNode.Conclusion, ConclusionItem.TextFormat.pretty)
            End If
        Else
            frmDraw = New frmDrawConc

            ' if there's already a conclusion defined, show it graphically in the editor
            If Not newNode.Conclusion Is Nothing Then
                frmDraw.ShowGraph(newNode.Conclusion)
            End If

            ' show form and if it closes correctly, get defined conclusion
            Me.Hide()
            result = frmDraw.ShowDialog()
            Me.Show()
            If Not (result = Windows.Forms.DialogResult.Cancel) Then
                If Not frmDraw.finalConclusion Is Nothing Then
                    newNode.Conclusion = frmDraw.finalConclusion.Clone
                    txtConclusion.Text = ConclusionTextify(newNode.Conclusion, ConclusionItem.TextFormat.pretty)
                End If
            End If
        End If
    End Sub

    ' add the new tree to the (temp) ruleset
    Private Sub btnAddTree_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles btnAddTree.Click
        Dim result As DialogResult
        Dim ruleTypeStr As String = cbxRuleType.SelectedItem
        Dim availableTasks As String()
        Dim idx As Integer = cbxRuleType.SelectedIndex

        'check user wants to commit
        result = MessageBox.Show("Are you sure you would like to complete the creation of this rule tree " & _
                                 "and commit it to the Rule Set?", _
                                 "Commit New Rule Tree?", MessageBoxButtons.YesNo, MessageBoxIcon.Warning)

        If result = Windows.Forms.DialogResult.Yes Then
            currentTree.Name = cbxTaskName.Text                             ' add name of task to tree
            tempRSMgr.addTreeToRuleSet(currentTree, ruleTypeStr)            ' add tree to the ruleset

            'if item level and no more tasks, remove from rules combo
            availableTasks = tempRSMgr.getAvailableTaskNamesForTreeType(ruleTypeStr)
            If availableTasks Is Nothing Then
                cbxRuleType.Items.Remove(ruleTypeStr)
                cbxRuleType.SelectedIndex = adjustIndex(idx, cbxRuleType.Items.Count)
            Else
                cbxTaskName.Items.Clear()
                cbxTaskName.Items.AddRange(availableTasks)                  ' add tasks to combo
                cbxTaskName.Text = cbxTaskName.Items(0)
            End If

            ' show new tree
            InitGUI(False)
            treeAdded = True
            btnAddTree.Enabled = False
            btnDone.Enabled = True
            cbxRuleType.Enabled = True
            cbxTaskName.Enabled = Not rsMgr.isCaseLevelTree(cbxRuleType.SelectedItem)

        End If
    End Sub

    ' adds the currently defined rule to the tree
    Private Sub btnAddRule_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles btnAddRule.Click
        Dim parentNode As RuleNode
        Dim selectedNode As TreeNode = tvRules.SelectedNode
        Dim selIndex As Integer = newNode.id

        If NoErrors() AndAlso WarningsOK() Then                       ' validates OK
            FillNodeFromGUI()                                         ' add stuff to node
            currentTree.addNode(newNode)                              ' add node to tree
            ruleAdded = True                                          ' flag 

            ' update the parent node to reflect its new child 
            parentNode = currentTree.getNode(newNode.Parent)
            If tvRules.SelectedNode.Text.IndexOf("True") > -1 Then
                parentNode.TrueChild = newNode.id
            Else
                parentNode.FalseChild = newNode.id
            End If

            ' reload tree (with new node added)
            tvRules.Nodes.Clear()
            tvMgr.LoadTreeIntoView(tvRules, currentTree, False)

            ' show new add points
            tvMgr.ShowAddPoints(tvRules, currentTree)

            ' reselect added node after tree reload
            tvRules.SelectedNode = tvMgr.SelectNodeWithID(tvRules, selIndex)
            ClearFields()
            FillGUIFromSelectedNode()                                  ' repopulate fields

            nextNodeId += 1
            btnAddTree.Enabled = True
            btnAddRule.Enabled = False
        End If
    End Sub

    ' discards all additions and returns to main form
    Private Sub btnCancel_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles btnCancel.Click

        ' warn that additions will be lost by cancelling
        If ruleAdded Then
            If MessageBox.Show("Cancelling now will discard all the rules and/or trees added. Are you sure " & _
                               "you want to cancel?", _
                               "Cancel - Are You Sure?", MessageBoxButtons.YesNo, MessageBoxIcon.Question) = _
                               Windows.Forms.DialogResult.Yes Then
                cancelClosing = False
                formCancelled = True
            Else
                cancelClosing = True                        ' user indicates don't close
            End If
        Else
            formCancelled = True                            ' no additions, go ahead and close
        End If
    End Sub

    'add a data attribute/value to the cornerstone case data 
    Private Sub btnAdd_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles btnAdd.Click
        Dim newCSEntry As String

        ' make sure there's a (valid xml) label and value provided
        If txtCSLabel.Text.Length = 0 Then
            MessageBox.Show("Please provide a Data Label", "Add Case Data", _
                              MessageBoxButtons.OK, MessageBoxIcon.Error)
            Exit Sub
        ElseIf Not XMLTagValidator(txtCSLabel.Text) Then
            MessageBox.Show("Illegal character in Cornerstone Data attribute name", _
                            "Add Case Data", MessageBoxButtons.OK, MessageBoxIcon.Error)
            Exit Sub
        End If
        If txtCSValue.Text.Length = 0 Then
            MessageBox.Show("Please provide a Data Value", "Add Case Data", _
                              MessageBoxButtons.OK, MessageBoxIcon.Error)
            Exit Sub
        End If

        newCSEntry = txtCSLabel.Text & " = " & txtCSValue.Text

        ' add new entry to listbox if it's not already there
        If lbxCornerstone.Items.IndexOf(newCSEntry) = -1 Then
            lbxCornerstone.Items.Add(newCSEntry)
            txtCSLabel.Clear()
            txtCSValue.Clear()
            txtCSLabel.Focus()
        Else
            MessageBox.Show("That data attribute is already in the list", "Add Case Data", _
                              MessageBoxButtons.OK, MessageBoxIcon.Error)
        End If
    End Sub

    ' show tree on full screen tree viewer form
    Private Sub btnZoom_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles btnZoom.Click
        Dim frmTV As New TreeViewer
        With frmTV
            .currentTree = currentTree
            .imlNodes = imlNodes
            .txtCompRule.Text = txtCompRule.Text
            Me.Hide()
            .ShowDialog()
            Me.Show()
        End With
    End Sub


    '***********************************************************************************************
    '****** CONTROL EVENTS *************************************************************************
    '***********************************************************************************************

    ' user has made a selection change in the rule type combo
    Private Sub cbxRuleType_SelectedIndexChanged(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles cbxRuleType.SelectedIndexChanged
        Dim item As String = cbxRuleType.SelectedItem
        Dim treeType As RuleSetMgr.exType = rsMgr.StringToTreeType(item)

        If rsMgr.isCaseLevelTree(treeType) Then                       ' disable taskname combo
            lblTaskName.Enabled = False
            cbxTaskName.Text = ""
            cbxTaskName.Enabled = False
        Else

            'enable taskname combo and fill it with the available tasks for that exception type
            cbxTaskName.Items.Clear()
            cbxTaskName.Items.AddRange(tempRSMgr.getAvailableTaskNamesForTreeType(treeType)) ' add tasks to combo
            If cbxTaskName.Items.Count > 0 Then
                lblTaskName.Enabled = True
                cbxTaskName.Text = cbxTaskName.Items(0)
                cbxTaskName.Enabled = True
            End If
        End If
    End Sub

    ' user has selected a different node in the treeview
    Private Sub tvRules_AfterSelect(ByVal sender As System.Object, ByVal e As System.Windows.Forms.TreeViewEventArgs) Handles tvRules.AfterSelect
        newNode = New RuleNode                                                       ' reset new node

        If tvRules.SelectedNode.Text.StartsWith("New") Then                          ' if add node selected

            ' reset data entry fields
            txtID.Text = nextNodeId.ToString
            txtParent.Text = tvRules.SelectedNode.Tag
            txtCondition.Clear()
            txtConclusion.Clear()
            txtDesc.Clear()
            txtCSLabel.Clear()
            txtCSValue.Clear()
            lbxCornerstone.Items.Clear()

            ' enable gui for input
            grpRuleNode.Enabled = True
            grpCornerstone.Enabled = True
            txtConclusion.BackColor = SystemColors.ControlLight
            lbxCornerstone.BackColor = SystemColors.ControlLight
            btnAddRule.Enabled = True

            ' set readonly fields
            cbxRuleType.Enabled = False
            cbxTaskName.Enabled = False
            newNode.id = txtID.Text
            newNode.Parent = txtParent.Text
        Else

            ' previously added node - show contents & reset fields
            FillGUIFromSelectedNode()
            If Not ruleAdded Then
                btnAddRule.Enabled = False
                cbxRuleType.Enabled = True
                cbxTaskName.Enabled = Not rsMgr.isCaseLevelTree(cbxRuleType.SelectedItem)
            End If
        End If
    End Sub


    '***********************************************************************************************
    '****** FORM EVENTS ***************************************************************************
    '***********************************************************************************************

    ' init form
    Private Sub frmNewRule_Load(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles MyBase.Load
        specName = tempRSMgr.SpecName                               ' display current spec name
        txtSpecName.Text = specName

        InitGUI(True)
    End Sub

    ' block form closing as required after bad validation
    Private Sub frmNewRule_Closing(ByVal sender As Object, ByVal e As System.ComponentModel.CancelEventArgs) Handles MyBase.Closing
        e.Cancel = cancelClosing
    End Sub


    '***********************************************************************************************
    '****** GUI METHODS ****************************************************************************
    '***********************************************************************************************

    ' initialises the GUI on load or re-load
    Private Sub InitGUI(ByVal onLoad As Boolean)

        ' initialise tree
        newNode = New RuleNode
        currentTree = New RuleTree

        ' add root node to new tree
        newNode.MakeRootNode()
        currentTree.addNode(newNode)

        nextNodeId = 1                ' reset node id counter

        ' set combos
        If onLoad Then
            cbxRuleType.Text = cbxRuleType.Items(0)
            cbxRuleType.SelectedIndex = 0
        End If

        'load the tree
        tvRules.Nodes.Clear()
        tvMgr.LoadTreeIntoView(tvRules, currentTree, True)
    End Sub

    ' fills gui inputs with values from the node selected in the treeview
    Private Sub FillGUIFromSelectedNode()
        Dim selectedNode As RuleNode

        ' show the data of the selected node
        selectedNode = currentTree.Nodes(tvMgr.getSelectedNode(tvRules))         ' get selected node values   
        txtID.Text = selectedNode.id
        txtParent.Text = selectedNode.Parent
        txtCondition.Text = selectedNode.Condition
        txtConclusion.Text = ConclusionTextify(selectedNode.Conclusion, ConclusionItem.TextFormat.pretty)
        txtDesc.Text = selectedNode.Description

        ' and the cornerstone list
        lbxCornerstone.Items.Clear()
        If Not selectedNode.Cornerstone Is Nothing Then
            For Each item As CompositeItem In selectedNode.Cornerstone
                lbxCornerstone.Items.Add(item.Tag & " = " & item.Text)
            Next
        End If

        ' ensure inputs are disabled (readonly) 
        grpRuleNode.Enabled = False
        grpCornerstone.Enabled = False
        txtConclusion.BackColor = SystemColors.Control
        lbxCornerstone.BackColor = SystemColors.Control
        btnAddRule.Enabled = False
        txtCompRule.Text = tvMgr.getEffectiveCondition(tvRules, currentTree)
    End Sub

    ' copy values from inputs to the selected node's fields
    Private Sub FillNodeFromGUI()
        With newNode
            '.id is already assigned
            '.parent is already assigned
            .TrueChild = "-1"
            .FalseChild = "-1"
            .Condition = txtCondition.Text
            '.conclusion is already assigned
            .Description = txtDesc.Text
            .Cornerstone = buildCornerStone(lbxCornerstone)
        End With
    End Sub

    ' reset inputs
    Private Sub ClearFields()
        newNode = New RuleNode
        txtCondition.Clear()
        txtConclusion.Clear()
        txtDesc.Clear()
        lbxCornerstone.Items.Clear()
        txtID.Clear()
        txtParent.Clear()
    End Sub


    '***********************************************************************************************
    '****** MISC METHODS ***************************************************************************
    '***********************************************************************************************

    ' checks that (at least) the new rule node has a condition and a conclusion
    Private Function NoErrors() As Boolean
        Dim errMsg As String = ""

        ' validate inputs
        If txtCondition.Text.Length = 0 Then
            errMsg = "A condition must be added before the rule can be saved."
        ElseIf txtConclusion.Text.Length = 0 Then
            errMsg = "A conclusion must be provided before the rule can be saved."
        End If

        If (errMsg.Length > 0) Then
            ShowError(errMsg)
            Return False
        Else
            Return True
        End If
    End Function

    ' shows warnings to user for non-completed optional fields
    Private Function WarningsOK() As Boolean
        Dim errMsg As String = ""

        If lbxCornerstone.Items.Count = 0 Then
            errMsg = "No cornerstone case data has been provided. " & vbCrLf & vbCrLf
        End If
        If txtDesc.Text.Length = 0 Then
            errMsg &= "No description has been provided." & vbCrLf
        End If

        If (errMsg.Length > 0) Then
            errMsg &= vbCrLf & "Do you want to save this rule anyway?"
            Return MessageBox.Show(errMsg, "Warning on non-mandatory items", MessageBoxButtons.YesNo, _
                    MessageBoxIcon.Warning) = Windows.Forms.DialogResult.Yes
        Else
            Return True
        End If
    End Function

    ' returns the correct selected index when an item is removed from a combobox. 
    Private Function adjustIndex(ByVal index As Integer, ByVal count As Integer) As Integer
        Dim result As Integer
        If count = 0 Then
            result = -1
        ElseIf index >= count Then
            result = index - 1
        Else
            result = index
        End If
        Return result
    End Function

End Class
