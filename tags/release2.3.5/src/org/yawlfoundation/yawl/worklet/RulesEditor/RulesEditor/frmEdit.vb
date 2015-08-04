' This file is made available under the terms of the LGPL licence.
' This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
' The source remains the property of the YAWL Foundation.  The YAWL Foundation 
' is a collaboration of individuals and organisations who are committed to  
' improving workflow technology.

Imports System.IO
Imports System.Net
Imports System.Text


Friend Class frmEdit
    Inherits System.Windows.Forms.Form

    ' This is the main & startup form for the RulesEditor
    ' The Worklet Service Rules Editor
    ' Author: Michael Adams
    '         BPM Group, QUT Australia
    ' Version: 0.8, 25/08/2006


    Private loadingRules As Boolean = False                ' flag to prevent double load
    Private currentTree As RuleTree                        ' the rule tree loaded in GUI 

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
    Friend WithEvents cbxTasks As System.Windows.Forms.ComboBox
    Friend WithEvents GroupBox1 As System.Windows.Forms.GroupBox
    Friend WithEvents Label2 As System.Windows.Forms.Label
    Friend WithEvents Label3 As System.Windows.Forms.Label
    Friend WithEvents Label4 As System.Windows.Forms.Label
    Friend WithEvents Label5 As System.Windows.Forms.Label
    Friend WithEvents Label6 As System.Windows.Forms.Label
    Friend WithEvents txtID As System.Windows.Forms.TextBox
    Friend WithEvents txtCondition As System.Windows.Forms.TextBox
    Friend WithEvents txtParent As System.Windows.Forms.TextBox
    Friend WithEvents txtConclusion As System.Windows.Forms.TextBox
    Friend WithEvents txtDesc As System.Windows.Forms.TextBox
    Friend WithEvents GroupBox2 As System.Windows.Forms.GroupBox
    Friend WithEvents lbxCornerstone As System.Windows.Forms.ListBox
    Friend WithEvents GroupBox3 As System.Windows.Forms.GroupBox
    Friend WithEvents tvRules As System.Windows.Forms.TreeView
    Friend WithEvents MainMenu1 As System.Windows.Forms.MainMenu
    Friend WithEvents MenuItem6 As System.Windows.Forms.MenuItem
    Friend WithEvents mFile As System.Windows.Forms.MenuItem
    Friend WithEvents mNew As System.Windows.Forms.MenuItem
    Friend WithEvents mOpen As System.Windows.Forms.MenuItem
    Friend WithEvents mClose As System.Windows.Forms.MenuItem
    Friend WithEvents mExit As System.Windows.Forms.MenuItem
    Friend WithEvents mRule As System.Windows.Forms.MenuItem
    Friend WithEvents mAdd As System.Windows.Forms.MenuItem
    Friend WithEvents mOptions As System.Windows.Forms.MenuItem
    Friend WithEvents mConfigure As System.Windows.Forms.MenuItem
    Friend WithEvents mHelp As System.Windows.Forms.MenuItem
    Friend WithEvents mAbout As System.Windows.Forms.MenuItem
    Friend WithEvents imlNodes As System.Windows.Forms.ImageList
    Friend WithEvents cbxRuleType As System.Windows.Forms.ComboBox
    Friend WithEvents Label7 As System.Windows.Forms.Label
    Friend WithEvents lblTaskName As System.Windows.Forms.Label
    Friend WithEvents imlToolbar As System.Windows.Forms.ImageList
    Friend WithEvents tbbNew As System.Windows.Forms.ToolBarButton
    Friend WithEvents tbbOpen As System.Windows.Forms.ToolBarButton
    Friend WithEvents tbbClose As System.Windows.Forms.ToolBarButton
    Friend WithEvents tbbSep1 As System.Windows.Forms.ToolBarButton
    Friend WithEvents tbbAdd As System.Windows.Forms.ToolBarButton
    Friend WithEvents tbbConfig As System.Windows.Forms.ToolBarButton
    Friend WithEvents tbbSep2 As System.Windows.Forms.ToolBarButton
    Friend WithEvents tbbHelp As System.Windows.Forms.ToolBarButton
    Friend WithEvents Toolbar As System.Windows.Forms.ToolBar
    Friend WithEvents mTreeView As System.Windows.Forms.MenuItem
    Friend WithEvents tbbZoom As System.Windows.Forms.ToolBarButton
    <System.Diagnostics.DebuggerStepThrough()> Private Sub InitializeComponent()
        Me.components = New System.ComponentModel.Container
        Dim resources As System.Resources.ResourceManager = New System.Resources.ResourceManager(GetType(frmEdit))
        Me.lblTaskName = New System.Windows.Forms.Label
        Me.cbxTasks = New System.Windows.Forms.ComboBox
        Me.GroupBox1 = New System.Windows.Forms.GroupBox
        Me.txtDesc = New System.Windows.Forms.TextBox
        Me.txtConclusion = New System.Windows.Forms.TextBox
        Me.txtParent = New System.Windows.Forms.TextBox
        Me.txtCondition = New System.Windows.Forms.TextBox
        Me.txtID = New System.Windows.Forms.TextBox
        Me.Label6 = New System.Windows.Forms.Label
        Me.Label5 = New System.Windows.Forms.Label
        Me.Label4 = New System.Windows.Forms.Label
        Me.Label3 = New System.Windows.Forms.Label
        Me.Label2 = New System.Windows.Forms.Label
        Me.GroupBox2 = New System.Windows.Forms.GroupBox
        Me.lbxCornerstone = New System.Windows.Forms.ListBox
        Me.GroupBox3 = New System.Windows.Forms.GroupBox
        Me.tvRules = New System.Windows.Forms.TreeView
        Me.imlNodes = New System.Windows.Forms.ImageList(Me.components)
        Me.MainMenu1 = New System.Windows.Forms.MainMenu
        Me.mFile = New System.Windows.Forms.MenuItem
        Me.mNew = New System.Windows.Forms.MenuItem
        Me.mOpen = New System.Windows.Forms.MenuItem
        Me.mClose = New System.Windows.Forms.MenuItem
        Me.MenuItem6 = New System.Windows.Forms.MenuItem
        Me.mExit = New System.Windows.Forms.MenuItem
        Me.mRule = New System.Windows.Forms.MenuItem
        Me.mAdd = New System.Windows.Forms.MenuItem
        Me.mTreeView = New System.Windows.Forms.MenuItem
        Me.mOptions = New System.Windows.Forms.MenuItem
        Me.mConfigure = New System.Windows.Forms.MenuItem
        Me.mHelp = New System.Windows.Forms.MenuItem
        Me.mAbout = New System.Windows.Forms.MenuItem
        Me.cbxRuleType = New System.Windows.Forms.ComboBox
        Me.Label7 = New System.Windows.Forms.Label
        Me.Toolbar = New System.Windows.Forms.ToolBar
        Me.tbbNew = New System.Windows.Forms.ToolBarButton
        Me.tbbOpen = New System.Windows.Forms.ToolBarButton
        Me.tbbClose = New System.Windows.Forms.ToolBarButton
        Me.tbbSep1 = New System.Windows.Forms.ToolBarButton
        Me.tbbAdd = New System.Windows.Forms.ToolBarButton
        Me.tbbZoom = New System.Windows.Forms.ToolBarButton
        Me.tbbSep2 = New System.Windows.Forms.ToolBarButton
        Me.tbbConfig = New System.Windows.Forms.ToolBarButton
        Me.tbbHelp = New System.Windows.Forms.ToolBarButton
        Me.imlToolbar = New System.Windows.Forms.ImageList(Me.components)
        Me.GroupBox1.SuspendLayout()
        Me.GroupBox2.SuspendLayout()
        Me.GroupBox3.SuspendLayout()
        Me.SuspendLayout()
        '
        'lblTaskName
        '
        Me.lblTaskName.AutoSize = True
        Me.lblTaskName.Enabled = False
        Me.lblTaskName.Location = New System.Drawing.Point(272, 40)
        Me.lblTaskName.Name = "lblTaskName"
        Me.lblTaskName.Size = New System.Drawing.Size(68, 16)
        Me.lblTaskName.TabIndex = 0
        Me.lblTaskName.Text = "Task Name: "
        '
        'cbxTasks
        '
        Me.cbxTasks.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList
        Me.cbxTasks.Enabled = False
        Me.cbxTasks.Location = New System.Drawing.Point(344, 40)
        Me.cbxTasks.Name = "cbxTasks"
        Me.cbxTasks.Size = New System.Drawing.Size(136, 21)
        Me.cbxTasks.TabIndex = 1
        '
        'GroupBox1
        '
        Me.GroupBox1.Controls.Add(Me.txtDesc)
        Me.GroupBox1.Controls.Add(Me.txtConclusion)
        Me.GroupBox1.Controls.Add(Me.txtParent)
        Me.GroupBox1.Controls.Add(Me.txtCondition)
        Me.GroupBox1.Controls.Add(Me.txtID)
        Me.GroupBox1.Controls.Add(Me.Label6)
        Me.GroupBox1.Controls.Add(Me.Label5)
        Me.GroupBox1.Controls.Add(Me.Label4)
        Me.GroupBox1.Controls.Add(Me.Label3)
        Me.GroupBox1.Controls.Add(Me.Label2)
        Me.GroupBox1.Location = New System.Drawing.Point(8, 280)
        Me.GroupBox1.Name = "GroupBox1"
        Me.GroupBox1.Size = New System.Drawing.Size(480, 248)
        Me.GroupBox1.TabIndex = 5
        Me.GroupBox1.TabStop = False
        Me.GroupBox1.Text = "Selected Node"
        '
        'txtDesc
        '
        Me.txtDesc.BackColor = System.Drawing.SystemColors.ControlLight
        Me.txtDesc.Location = New System.Drawing.Point(80, 192)
        Me.txtDesc.Multiline = True
        Me.txtDesc.Name = "txtDesc"
        Me.txtDesc.ReadOnly = True
        Me.txtDesc.Size = New System.Drawing.Size(392, 48)
        Me.txtDesc.TabIndex = 10
        Me.txtDesc.TabStop = False
        Me.txtDesc.Text = ""
        '
        'txtConclusion
        '
        Me.txtConclusion.BackColor = System.Drawing.SystemColors.ControlLight
        Me.txtConclusion.Location = New System.Drawing.Point(80, 76)
        Me.txtConclusion.Multiline = True
        Me.txtConclusion.Name = "txtConclusion"
        Me.txtConclusion.ReadOnly = True
        Me.txtConclusion.Size = New System.Drawing.Size(392, 108)
        Me.txtConclusion.TabIndex = 9
        Me.txtConclusion.TabStop = False
        Me.txtConclusion.Text = ""
        '
        'txtParent
        '
        Me.txtParent.BackColor = System.Drawing.SystemColors.ControlLight
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
        Me.txtCondition.BackColor = System.Drawing.SystemColors.ControlLight
        Me.txtCondition.Location = New System.Drawing.Point(80, 48)
        Me.txtCondition.Name = "txtCondition"
        Me.txtCondition.ReadOnly = True
        Me.txtCondition.Size = New System.Drawing.Size(392, 20)
        Me.txtCondition.TabIndex = 7
        Me.txtCondition.TabStop = False
        Me.txtCondition.Text = ""
        '
        'txtID
        '
        Me.txtID.BackColor = System.Drawing.SystemColors.ControlLight
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
        Me.Label6.Location = New System.Drawing.Point(16, 192)
        Me.Label6.Name = "Label6"
        Me.Label6.Size = New System.Drawing.Size(64, 16)
        Me.Label6.TabIndex = 4
        Me.Label6.Text = "Description:"
        '
        'Label5
        '
        Me.Label5.AutoSize = True
        Me.Label5.Location = New System.Drawing.Point(16, 78)
        Me.Label5.Name = "Label5"
        Me.Label5.Size = New System.Drawing.Size(63, 16)
        Me.Label5.TabIndex = 3
        Me.Label5.Text = "Conclusion:"
        '
        'Label4
        '
        Me.Label4.AutoSize = True
        Me.Label4.Location = New System.Drawing.Point(16, 50)
        Me.Label4.Name = "Label4"
        Me.Label4.Size = New System.Drawing.Size(55, 16)
        Me.Label4.TabIndex = 2
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
        'GroupBox2
        '
        Me.GroupBox2.Controls.Add(Me.lbxCornerstone)
        Me.GroupBox2.Location = New System.Drawing.Point(288, 72)
        Me.GroupBox2.Name = "GroupBox2"
        Me.GroupBox2.Size = New System.Drawing.Size(200, 200)
        Me.GroupBox2.TabIndex = 7
        Me.GroupBox2.TabStop = False
        Me.GroupBox2.Text = "Cornerstone Case"
        '
        'lbxCornerstone
        '
        Me.lbxCornerstone.BackColor = System.Drawing.SystemColors.ControlLight
        Me.lbxCornerstone.Location = New System.Drawing.Point(3, 16)
        Me.lbxCornerstone.Name = "lbxCornerstone"
        Me.lbxCornerstone.SelectionMode = System.Windows.Forms.SelectionMode.None
        Me.lbxCornerstone.Size = New System.Drawing.Size(189, 173)
        Me.lbxCornerstone.TabIndex = 0
        Me.lbxCornerstone.TabStop = False
        '
        'GroupBox3
        '
        Me.GroupBox3.Controls.Add(Me.tvRules)
        Me.GroupBox3.Location = New System.Drawing.Point(8, 72)
        Me.GroupBox3.Name = "GroupBox3"
        Me.GroupBox3.Size = New System.Drawing.Size(272, 200)
        Me.GroupBox3.TabIndex = 2
        Me.GroupBox3.TabStop = False
        Me.GroupBox3.Text = "RDR Tree"
        '
        'tvRules
        '
        Me.tvRules.BackColor = System.Drawing.SystemColors.Window
        Me.tvRules.HideSelection = False
        Me.tvRules.ImageList = Me.imlNodes
        Me.tvRules.Location = New System.Drawing.Point(3, 16)
        Me.tvRules.Name = "tvRules"
        Me.tvRules.Size = New System.Drawing.Size(261, 176)
        Me.tvRules.TabIndex = 1
        '
        'imlNodes
        '
        Me.imlNodes.ImageSize = New System.Drawing.Size(16, 16)
        Me.imlNodes.ImageStream = CType(resources.GetObject("imlNodes.ImageStream"), System.Windows.Forms.ImageListStreamer)
        Me.imlNodes.TransparentColor = System.Drawing.Color.Transparent
        '
        'MainMenu1
        '
        Me.MainMenu1.MenuItems.AddRange(New System.Windows.Forms.MenuItem() {Me.mFile, Me.mRule, Me.mOptions, Me.mHelp})
        '
        'mFile
        '
        Me.mFile.Index = 0
        Me.mFile.MenuItems.AddRange(New System.Windows.Forms.MenuItem() {Me.mNew, Me.mOpen, Me.mClose, Me.MenuItem6, Me.mExit})
        Me.mFile.Text = "&File"
        '
        'mNew
        '
        Me.mNew.Index = 0
        Me.mNew.Text = "&New..."
        '
        'mOpen
        '
        Me.mOpen.Index = 1
        Me.mOpen.Text = "&Open..."
        '
        'mClose
        '
        Me.mClose.Enabled = False
        Me.mClose.Index = 2
        Me.mClose.Text = "&Close"
        '
        'MenuItem6
        '
        Me.MenuItem6.Index = 3
        Me.MenuItem6.Text = "-"
        '
        'mExit
        '
        Me.mExit.Index = 4
        Me.mExit.Text = "E&xit"
        '
        'mRule
        '
        Me.mRule.Index = 1
        Me.mRule.MenuItems.AddRange(New System.Windows.Forms.MenuItem() {Me.mAdd, Me.mTreeView})
        Me.mRule.Text = "&Rule"
        '
        'mAdd
        '
        Me.mAdd.Enabled = False
        Me.mAdd.Index = 0
        Me.mAdd.Text = "&Add..."
        '
        'mTreeView
        '
        Me.mTreeView.Enabled = False
        Me.mTreeView.Index = 1
        Me.mTreeView.Text = "Tree Viewer..."
        '
        'mOptions
        '
        Me.mOptions.Index = 2
        Me.mOptions.MenuItems.AddRange(New System.Windows.Forms.MenuItem() {Me.mConfigure})
        Me.mOptions.Text = "&Options"
        '
        'mConfigure
        '
        Me.mConfigure.Index = 0
        Me.mConfigure.Text = "Co&nfigure..."
        '
        'mHelp
        '
        Me.mHelp.Index = 3
        Me.mHelp.MenuItems.AddRange(New System.Windows.Forms.MenuItem() {Me.mAbout})
        Me.mHelp.Text = "&Help"
        '
        'mAbout
        '
        Me.mAbout.Index = 0
        Me.mAbout.Text = "A&bout..."
        '
        'cbxRuleType
        '
        Me.cbxRuleType.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList
        Me.cbxRuleType.Enabled = False
        Me.cbxRuleType.Location = New System.Drawing.Point(80, 40)
        Me.cbxRuleType.Name = "cbxRuleType"
        Me.cbxRuleType.Size = New System.Drawing.Size(160, 21)
        Me.cbxRuleType.TabIndex = 9
        '
        'Label7
        '
        Me.Label7.AutoSize = True
        Me.Label7.Location = New System.Drawing.Point(16, 40)
        Me.Label7.Name = "Label7"
        Me.Label7.Size = New System.Drawing.Size(59, 16)
        Me.Label7.TabIndex = 8
        Me.Label7.Text = "Rule Type:"
        '
        'Toolbar
        '
        Me.Toolbar.Buttons.AddRange(New System.Windows.Forms.ToolBarButton() {Me.tbbNew, Me.tbbOpen, Me.tbbClose, Me.tbbSep1, Me.tbbAdd, Me.tbbZoom, Me.tbbSep2, Me.tbbConfig, Me.tbbHelp})
        Me.Toolbar.ButtonSize = New System.Drawing.Size(24, 24)
        Me.Toolbar.Divider = False
        Me.Toolbar.DropDownArrows = True
        Me.Toolbar.ImageList = Me.imlToolbar
        Me.Toolbar.Location = New System.Drawing.Point(0, 0)
        Me.Toolbar.Name = "Toolbar"
        Me.Toolbar.ShowToolTips = True
        Me.Toolbar.Size = New System.Drawing.Size(494, 28)
        Me.Toolbar.TabIndex = 10
        '
        'tbbNew
        '
        Me.tbbNew.ImageIndex = 0
        Me.tbbNew.ToolTipText = "New"
        '
        'tbbOpen
        '
        Me.tbbOpen.ImageIndex = 1
        Me.tbbOpen.ToolTipText = "Open"
        '
        'tbbClose
        '
        Me.tbbClose.Enabled = False
        Me.tbbClose.ImageIndex = 2
        Me.tbbClose.ToolTipText = "Close"
        '
        'tbbSep1
        '
        Me.tbbSep1.Style = System.Windows.Forms.ToolBarButtonStyle.Separator
        '
        'tbbAdd
        '
        Me.tbbAdd.Enabled = False
        Me.tbbAdd.ImageIndex = 3
        Me.tbbAdd.ToolTipText = "Add Rule"
        '
        'tbbZoom
        '
        Me.tbbZoom.Enabled = False
        Me.tbbZoom.ImageIndex = 4
        Me.tbbZoom.ToolTipText = "Tree Viewer"
        '
        'tbbSep2
        '
        Me.tbbSep2.Style = System.Windows.Forms.ToolBarButtonStyle.Separator
        '
        'tbbConfig
        '
        Me.tbbConfig.ImageIndex = 5
        Me.tbbConfig.ToolTipText = "Settings"
        '
        'tbbHelp
        '
        Me.tbbHelp.ImageIndex = 6
        Me.tbbHelp.ToolTipText = "Help"
        '
        'imlToolbar
        '
        Me.imlToolbar.ColorDepth = System.Windows.Forms.ColorDepth.Depth16Bit
        Me.imlToolbar.ImageSize = New System.Drawing.Size(16, 16)
        Me.imlToolbar.ImageStream = CType(resources.GetObject("imlToolbar.ImageStream"), System.Windows.Forms.ImageListStreamer)
        Me.imlToolbar.TransparentColor = System.Drawing.Color.Transparent
        '
        'frmEdit
        '
        Me.AutoScaleBaseSize = New System.Drawing.Size(5, 13)
        Me.ClientSize = New System.Drawing.Size(494, 535)
        Me.Controls.Add(Me.Toolbar)
        Me.Controls.Add(Me.cbxRuleType)
        Me.Controls.Add(Me.Label7)
        Me.Controls.Add(Me.lblTaskName)
        Me.Controls.Add(Me.GroupBox3)
        Me.Controls.Add(Me.GroupBox2)
        Me.Controls.Add(Me.GroupBox1)
        Me.Controls.Add(Me.cbxTasks)
        Me.FormBorderStyle = System.Windows.Forms.FormBorderStyle.Fixed3D
        Me.Icon = CType(resources.GetObject("$this.Icon"), System.Drawing.Icon)
        Me.MaximizeBox = False
        Me.Menu = Me.MainMenu1
        Me.Name = "frmEdit"
        Me.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen
        Me.Text = "Worklet Rules Editor"
        Me.GroupBox1.ResumeLayout(False)
        Me.GroupBox2.ResumeLayout(False)
        Me.GroupBox3.ResumeLayout(False)
        Me.ResumeLayout(False)

    End Sub

#End Region

    '*********************************************************************************************************
    '*********************************************************************************************************

    '*****************'
    ' FORM LOAD EVENT '
    '*****************'

    ' load paths from config file (or create one with defaults as required)
    Private Sub frmEdit_Load(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles MyBase.Load
        Dim fConfig As New frmConfig
        Dim pathsLoaded As Boolean = False

        'if user-defined paths exist, load them, otherwise assume its a first time
        If File.Exists(Application.StartupPath & "\RulesEditor.cfg") Then
            fConfig.LoadPathsFromFile()
            pathsLoaded = True
        Else
            ' first time use
            MessageBox.Show("Welcome to the Worklet Rules Editor. Please enter the " & _
                            "file paths to the resources listed in the next dialog.", _
                            "Rules Editor First Time Configure", MessageBoxButtons.OK, _
                            MessageBoxIcon.Information)
            fConfig.ShowDialog()
            If fConfig.isValid Then  ' paths entered in config form are OK
                pathsLoaded = True
            End If
        End If

        If Not pathsLoaded Then       ' create some defaults
            fConfig.LoadDefaultPaths()
            fConfig.SavePathsToFile(False)   ' save without validating
        End If

        ' we now have paths, so load them into the app
        pathOf.repository = fConfig.txtRepository.Text
        pathOf.YAWLEditor = fConfig.txtYAWLEditor.Text
        pathOf.ServiceURL = fConfig.txtServiceURI.Text
        pathOf.SpecPaths = fConfig.txtSpecPaths.Text
    End Sub

    '*********************************************************************************************************
    '*********************************************************************************************************

    '*************'
    ' MENU EVENTS '
    '*************'

    ' shows a form to allow the creation of a new rule set
    Private Sub mNew_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles mNew.Click

        Dim fNewRule As New frmNewRule
        Dim fSpecLocator As frmSpecLocator                            ' to locate the spec file if not yet opened

        fNewRule.cbxRuleType.Items.AddRange(rsMgr.getRuleTypeList)    ' init rules combo items

        ' if a ruleset is loaded, the new click will add content to it, rather than start afresh
        ' so those elements currently in the rulset must be filtered out
        If rsMgr.hasFileLoaded Then
            With fNewRule

                ' remove from list of rule types (i) any case level tree types already defined; and (ii)
                ' any item-level tree types that have rules already defined for all their items
                For Each ruleType As String In cbxRuleType.Items
                    If rsMgr.getAvailableTaskNamesForTreeType(ruleType) Is Nothing Then
                        .cbxRuleType.Items.Remove(ruleType)           ' remove rule trees already defined
                    End If
                Next

                .tempRSMgr = DeepClone(rsMgr)                         ' make a copy of current rule set manager
            End With
        Else
            ' no rules file loaded
            fSpecLocator = New frmSpecLocator
            If fSpecLocator.ShowDialog = Windows.Forms.DialogResult.OK Then

                ' create a temp ruleset manager for the new rules form
                fNewRule.tempRSMgr = New RuleSetMgr(fSpecLocator.txtSpecFile.Text, fSpecLocator.rulesFileName)
            Else
                Exit Sub                                              ' user cancelled
            End If
        End If

        ' show the form and update form on exit
        Me.Hide()
        fNewRule.ShowDialog()
        Me.Show()
        If Not fNewRule.formCancelled Then
            InitGUIAfterLoad()
            tvRules.Nodes.Clear()
            tvMgr.LoadTreeIntoView(tvRules, currentTree, False)     ' draw tree in editor
        End If

    End Sub

    ' loads a complete rule set, then loads the first tree into the GUI
    Private Sub mOpen_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles mOpen.Click

        loadingRules = True                                         ' prevent double load

        If LoadRules() Then                                         ' read rules from file (into rsMgr)
            tvMgr.LoadTreeIntoView(tvRules, currentTree, False)     ' draw tree in editor
            loadingRules = False                                    ' load has completed
        End If
    End Sub

    ' closes a rule set file, and resets the GUI
    Private Sub mClose_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles mClose.Click

        Clear()                                                     ' clear form controls' values
        rsMgr = New RuleSetMgr                                      ' reset the manager
        Me.Text = "Worklet Rules Editor"                            ' reset the form's title

        With cbxTasks                                               ' clear combos
            .Items.Clear()
            .Text = ""
            .Enabled = False
        End With
        With cbxRuleType
            .Items.Clear()
            .Text = ""
            .Enabled = False
        End With

        mOpen.Enabled = True                                        ' reset menus & toolbars
        mClose.Enabled = False
        mTreeView.Enabled = False
        mAdd.Enabled = False
        With Toolbar
            .Buttons(1).Enabled = True         ' Open 
            .Buttons(2).Enabled = False        ' Close
            .Buttons(4).Enabled = False        ' Add
            .Buttons(5).Enabled = False        ' Zoom
        End With
    End Sub

    ' exits the app
    Private Sub mExit_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles mExit.Click
        Close()
    End Sub

    ' show the 'Add' form to add a new rule node to the current tree 
    Private Sub mAdd_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles mAdd.Click
        Dim fAdd As New frmAddRule
        Dim newNode, parentNode As RuleNode                       ' node to be added and its parent 
        Dim result As DialogResult

        With fAdd                                                 ' initialise add form
            .Owner = Me
            .loadedRuleType = cbxRuleType.Text
            .loadedTask = cbxTasks.Text
            .nextNodeId = currentTree.getNextNodeID
        End With

        Me.Hide()
        result = fAdd.ShowDialog()
        Me.Show()
        If result = Windows.Forms.DialogResult.OK Then                ' show it & Ok pressed
            newNode = fAdd.newNode                                ' get new node from form 

            If Convert.ToInt32(newNode.id) > -1 Then              ' index of new node  

                currentTree.addNode(newNode)                      ' add the node to the tree

                ' update the parent node to reflect its new child
                parentNode = currentTree.getNode(newNode.Parent)  ' get node
                If Not parentNode Is Nothing Then
                    If fAdd.isTrueBranch Then
                        parentNode.TrueChild = newNode.id             ' add new node as child
                    Else
                        parentNode.FalseChild = newNode.id
                    End If

                    rsMgr.updateTreeInRuleSet(currentTree, cbxRuleType.Text)  ' update the tree

                    tvRules.Nodes.Clear()                                     ' reload tree in GUI
                    tvMgr.LoadTreeIntoView(tvRules, currentTree, False)

                    rsMgr.SaveRulesToFile()                            ' update the file  
                    ReplaceWorklet(fAdd.workItem)                      ' ask user if worklet replace wanted
                Else
                    MessageBox.Show("Invalid data in execution log, or log does not match current rule file.", _
                                    "Error Adding Rule", MessageBoxButtons.OK, MessageBoxIcon.Error)
                End If
            End If
        End If
    End Sub

    ' get the paths to various resources
    Private Sub mConfigure_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles mConfigure.Click
        Dim fConfig As New frmConfig

        fConfig.ShowDialog()
        If fConfig.isValid Then
            pathOf.repository = fConfig.txtRepository.Text
            pathOf.YAWLEditor = fConfig.txtYAWLEditor.Text
            pathOf.ServiceURL = fConfig.txtServiceURI.Text
            pathOf.SpecPaths = fConfig.txtSpecPaths.Text
        End If
    End Sub

    ' show the groovy about box
    Private Sub mAbout_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles mAbout.Click
        Dim fAbout As New frmAbout
        fAbout.ShowDialog()
    End Sub

    ' map tool button clicks to the corresponding menu event
    Private Sub ToolBar_ButtonClick(ByVal sender As System.Object, ByVal e As System.Windows.Forms.ToolBarButtonClickEventArgs) Handles Toolbar.ButtonClick

        ' buttons 3 & 6 are separators
        Select Case Toolbar.Buttons.IndexOf(e.Button)
            Case 0
                mNew.PerformClick()
            Case 1
                mOpen.PerformClick()
            Case 2
                mClose.PerformClick()
            Case 4
                mAdd.PerformClick()
            Case 5
                mTreeView.PerformClick()
            Case 7
                mConfigure.PerformClick()
            Case 8
                mAbout.PerformClick()
        End Select
    End Sub

    '*********************************************************************************************************
    '*********************************************************************************************************

    '****************'
    ' CONTROL EVENTS ' 
    '****************'

    ' fires when node is selected in the treeview - updates gui text fields
    Private Sub tvRules_AfterSelect(ByVal sender As System.Object, ByVal e As System.Windows.Forms.TreeViewEventArgs) Handles tvRules.AfterSelect
        Dim node As RuleNode = currentTree.Nodes(tvMgr.getSelectedNode(tvRules))         ' get selected node values   

        ' update textboxes
        txtID.Text = node.id
        txtParent.Text = node.Parent
        txtCondition.Text = node.Condition
        txtConclusion.Text = ConclusionTextify(node.Conclusion, ConclusionItem.TextFormat.pretty)
        txtDesc.Text = node.Description

        ' and the cornerstone list
        lbxCornerstone.Items.Clear()
        If Not node.Cornerstone Is Nothing Then
            For Each item As CompositeItem In node.Cornerstone
                lbxCornerstone.Items.Add(item.Tag & " = " & item.Text)
            Next
        End If
    End Sub

    ' a different task has been selected by the user
    Private Sub cbxTasks_SelectedIndexChanged(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles cbxTasks.SelectedIndexChanged
        If Not loadingRules Then                                         ' when triggered while loading, ignore it
            Clear()                                                      ' else clear all GUI fields 
            updateCurrentTree()                                          ' change the displayed tree
            tvMgr.LoadTreeIntoView(tvRules, currentTree, False)          ' load tree for this task into GUI
        End If
    End Sub

    ' a different rule type has been selected by the user
    Private Sub cbxRuleType_SelectedIndexChanged(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles cbxRuleType.SelectedIndexChanged
        If Not loadingRules Then                                         ' when triggered while loading, ignore it
            resetCombos()                                                ' else set combos for this rule 
            Clear()                                                      ' clear all GUI Ifields
            updateCurrentTree()                                          ' load tree for this task into GUI
            tvMgr.LoadTreeIntoView(tvRules, currentTree, False)          ' load tree for this task
        End If
    End Sub

    '*********************************************************************************************************
    '*********************************************************************************************************

    '*************************'
    ' WORKLET REPLACE METHODS '
    '*************************'

    ' asks if user wants to replace running worklet after a rule addition
    Private Sub ReplaceWorklet(ByVal workitem As frmAddRule.sWorkItem)

        Dim question As String = "Do you wish to immediately replace the running worklet case"
        Dim queryText As String = "action=replace&exType=" & workitem.ruleType & "&caseID=" & workitem.caseid
        Dim trigger As String = getTrigger(workitem.casedata)

        If workitem.worklets.Count > 1 Then                     ' pluralise if more than one worklet case 
            question &= "s"
        End If

        If workitem.id Is Nothing Then                          ' case-level worklet
            question &= " using the new rule?" & vbCrLf & _
                        vbCrLf & "Parent Spec ID: " & workitem.specuri & _
                        vbCrLf & "Parent Case ID: " & workitem.caseid
        Else
            question &= " for workitem '" & workitem.id & "' using the new rule?" & vbCrLf & _
                   vbCrLf & "Workitem Spec ID: " & workitem.specuri & _
                   vbCrLf & "Workitem Case ID: " & workitem.caseid & _
                   vbCrLf & "Workitem Task ID: " & workitem.taskid
            queryText &= "&itemID=" & workitem.id
        End If

        If Not trigger Is Nothing Then                         ' if external trigger, add it to query
            queryText &= "&trigger=" & trigger
        End If

        If workitem.worklets.Count > 1 Then                     'pluralise if more than one worklet case 
            question &= vbCrLf & "Running Worklet Cases: " & vbCrLf
        Else
            question &= vbCrLf & "Running Worklet Case:  "
        End If

        question &= getWorkletCaseIdentifiers(workitem.worklets)

        If MessageBox.Show(question, "Replace running worklet?", MessageBoxButtons.YesNo, _
                           MessageBoxIcon.Question) = Windows.Forms.DialogResult.Yes Then
            TriggerReplaceWorklet(queryText)         ' if 'yes', replace it
        End If
    End Sub

    ' tells worklet service to replace a worklet & waits for a response
    Private Function TriggerReplaceWorklet(ByVal params As String) As Stream
        Dim req As HttpWebRequest = Nothing
        Dim resp As HttpWebResponse = Nothing
        Dim url As String = pathOf.ServiceURL & "/gateway"     ' to wsGateway servlet
        Dim query As String = "?"
        Dim result As Stream = Nothing                         ' the response
        Dim strResp As String                                  ' converted to string

        If Not ValidServiceURI(url) Then                       ' make sure url is valid
            Return Nothing
        End If

        ' build the query here 
        query &= params

        Cursor = Cursors.WaitCursor

        Try
            ' build a request from the url and the query string 
            req = DirectCast(WebRequest.Create(url & query), HttpWebRequest)
            req.KeepAlive = False                                   ' ok to be async

            ' get the response from the service
            resp = DirectCast(req.GetResponse(), HttpWebResponse)
            result = resp.GetResponseStream
            If result.CanRead Then
                strResp = StreamToString(result)                    ' convert to string
            Else
                strResp = "The worklet service failed to respond to the request."
            End If
            MessageBox.Show(strResp, "Result of replace request", MessageBoxButtons.OK, _
                           MessageBoxIcon.Information)
        Catch we As Exception
            MessageBox.Show("The replace request failed on exception:" & vbCrLf & we.StackTrace(), _
                             "Replace Request Warning", MessageBoxButtons.OK, MessageBoxIcon.Warning)
        Finally
            resp.Close()
            Cursor = Cursors.Default
        End Try
        Return result   ' not used
    End Function

    ' converts a stream (response from wsGateway) to a String
    Private Function StreamToString(ByVal s As Stream) As String
        Dim encode As Encoding = System.Text.Encoding.GetEncoding("utf-8")
        Dim sr As New StreamReader(s, encode)
        Dim ch(256) As Char                          ' Reads 256 characters at a time
        Dim result As String = ""

        Dim count As Integer = sr.Read(ch, 0, 256)   ' fill the buffer
        While count > 0
            result &= New String(ch, 0, count)       ' add to result string 
            count = sr.Read(ch, 0, 256)
        End While

        sr.Close()
        Return result
    End Function

    '*********************************************************************************************************
    '*********************************************************************************************************

    '**********************'
    ' MISC SUPPORT METHODS '
    '**********************'

    ' fill the GUI controls with relevant values
    Private Sub InitGUIAfterLoad()

        Me.Text = "Worklet Rules Editor <Browse> : " & rsMgr.SpecName     ' add to titlebar

        With cbxRuleType
            .Items.Clear()
            .Items.AddRange(rsMgr.GetLoadedRuleTypesAsStrings)
            .Enabled = True
            .SelectedIndex = 0
            .Text = .Items(0)
        End With

        mClose.Enabled = True                                   ' enable menus   
        mAdd.Enabled = True
        mTreeView.Enabled = True
        mOpen.Enabled = False
        With Toolbar                                            ' ... and toolbar buttons
            .Buttons(1).Enabled = False                         ' Open 
            .Buttons(2).Enabled = True                          ' Close
            .Buttons(4).Enabled = True                          ' Add
            .Buttons(5).Enabled = True                          ' Zoom
        End With

        resetCombos()
        updateCurrentTree()

    End Sub

    ' enable or disable the tasks combo as required by rule level
    Private Sub resetCombos()

        cbxTasks.Items.Clear()

        'get selected tree set 
        If rsMgr.isCaseLevelTree(cbxRuleType.Text) Then
            With cbxTasks
                .Text = ""
                .Enabled = False
            End With
            lblTaskName.Enabled = False
        Else
            With cbxTasks
                .Enabled = True
                .Items.AddRange(rsMgr.GetLoadedTasksForRuleType(cbxRuleType.Text))
                .SelectedIndex = 0                       ' set combo to 1st task
                .Text = cbxTasks.Items(0)
            End With
            lblTaskName.Enabled = True
        End If
    End Sub

    ' set the 'current' tree to the tree selected by the user
    Private Sub updateCurrentTree()

        'get selected tree set 
        Dim treeSet As TreeSet = rsMgr(rsMgr.StringToTreeType(cbxRuleType.Text))

        ' set the selected tree within the set
        If rsMgr.isCaseLevelTree(treeSet.TreeType) Then
            currentTree = treeSet.Trees(0)
        Else
            currentTree = treeSet.getTreeForTask(cbxTasks.SelectedItem)
        End If
    End Sub

    ' reads an rdr ruleset file into the app
    Private Function LoadRules() As Boolean
        Dim ofd As New OpenFileDialog

        ' show an open dialog tailored for rules file selection
        ofd.Filter = "Worklet Ruleset Files (*.xrs) | *.xrs"
        ofd.InitialDirectory = pathOf.repository & "\rules"
        If ofd.ShowDialog = Windows.Forms.DialogResult.OK Then
            If rsMgr.LoadRulesFromFile(ofd.FileName) Then              ' successful load
                InitGUIAfterLoad()
                Return True
            End If
        End If
        Return False                                                   ' user cancel or unsuccessful load 
    End Function

    ' clear controls of data
    Private Sub Clear()
        tvRules.Nodes.Clear()
        lbxCornerstone.Items.Clear()
        txtID.Text = ""
        txtParent.Text = ""
        txtCondition.Text = ""
        txtConclusion.Text = ""
        txtDesc.Text = ""
    End Sub

    ' allows add form to get the cornerstone data for a rule
    Public Function getCornerstone(ByVal idx As Integer) As CompositeItem()
        Return currentTree.getNode(idx).Cornerstone
    End Function

    ' reads the trigger value from the data set
    Private Function getTrigger(ByVal items() As String) As String
        Dim result As String = ""
        For Each item As String In items
            If item.StartsWith("trigger") Then
                result = (item.Split("="c))(1).Trim
                Exit For
            End If
        Next
        Return result
    End Function

    '*********************************************************************************************************

    'opens the tree viewer 
    Private Sub mTreeView_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles mTreeView.Click
        Dim frmTV As New TreeViewer
        frmTV.currentTree = currentTree
        frmTV.imlNodes = imlNodes
        Me.Hide()
        frmTV.ShowDialog()
        Me.Show()
    End Sub

    Private Sub GroupBox1_Enter(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles GroupBox1.Enter

    End Sub
End Class