' This file is made available under the terms of the LGPL licence.
' This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
' The source remains the property of the YAWL Foundation.  The YAWL Foundation 
' is a collaboration of individuals and organisations who are commited to  
' improving workflow technology.

Public Class TreeViewer
    Inherits System.Windows.Forms.Form

    ' This very simple form allows a user to view a tree and the effective composite rule 
    ' of each node in full-screen mode.

    ' The Worklet Service Rules Editor
    ' Author: Michael Adams
    '         BPM Group, QUT Australia
    ' Version: 0.8, 25/08/2006

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
    Friend WithEvents tvRules As System.Windows.Forms.TreeView
    Friend WithEvents txtCompRule As System.Windows.Forms.TextBox
    Friend WithEvents Splitter As System.Windows.Forms.Splitter
    Friend WithEvents gbxRules As System.Windows.Forms.GroupBox
    Friend WithEvents gbxComposite As System.Windows.Forms.GroupBox
    <System.Diagnostics.DebuggerStepThrough()> Private Sub InitializeComponent()
        Dim resources As System.Resources.ResourceManager = New System.Resources.ResourceManager(GetType(TreeViewer))
        Me.gbxRules = New System.Windows.Forms.GroupBox
        Me.tvRules = New System.Windows.Forms.TreeView
        Me.Splitter = New System.Windows.Forms.Splitter
        Me.gbxComposite = New System.Windows.Forms.GroupBox
        Me.txtCompRule = New System.Windows.Forms.TextBox
        Me.gbxRules.SuspendLayout()
        Me.gbxComposite.SuspendLayout()
        Me.SuspendLayout()
        '
        'gbxRules
        '
        Me.gbxRules.Controls.Add(Me.tvRules)
        Me.gbxRules.Dock = System.Windows.Forms.DockStyle.Top
        Me.gbxRules.Location = New System.Drawing.Point(0, 0)
        Me.gbxRules.Name = "gbxRules"
        Me.gbxRules.Size = New System.Drawing.Size(600, 288)
        Me.gbxRules.TabIndex = 11
        Me.gbxRules.TabStop = False
        Me.gbxRules.Text = "RDR Tree"
        '
        'tvRules
        '
        Me.tvRules.BackColor = System.Drawing.SystemColors.Window
        Me.tvRules.Dock = System.Windows.Forms.DockStyle.Fill
        Me.tvRules.HideSelection = False
        Me.tvRules.ImageIndex = -1
        Me.tvRules.Location = New System.Drawing.Point(3, 16)
        Me.tvRules.Name = "tvRules"
        Me.tvRules.SelectedImageIndex = -1
        Me.tvRules.Size = New System.Drawing.Size(594, 269)
        Me.tvRules.TabIndex = 1
        '
        'Splitter
        '
        Me.Splitter.Dock = System.Windows.Forms.DockStyle.Top
        Me.Splitter.Location = New System.Drawing.Point(0, 288)
        Me.Splitter.MinExtra = 50
        Me.Splitter.MinSize = 50
        Me.Splitter.Name = "Splitter"
        Me.Splitter.Size = New System.Drawing.Size(600, 3)
        Me.Splitter.TabIndex = 12
        Me.Splitter.TabStop = False
        '
        'gbxComposite
        '
        Me.gbxComposite.Controls.Add(Me.txtCompRule)
        Me.gbxComposite.Dock = System.Windows.Forms.DockStyle.Fill
        Me.gbxComposite.Location = New System.Drawing.Point(0, 291)
        Me.gbxComposite.Name = "gbxComposite"
        Me.gbxComposite.Size = New System.Drawing.Size(600, 155)
        Me.gbxComposite.TabIndex = 13
        Me.gbxComposite.TabStop = False
        Me.gbxComposite.Text = "Effective Composite Rule"
        '
        'txtCompRule
        '
        Me.txtCompRule.BackColor = System.Drawing.SystemColors.ControlLight
        Me.txtCompRule.Dock = System.Windows.Forms.DockStyle.Fill
        Me.txtCompRule.Location = New System.Drawing.Point(3, 16)
        Me.txtCompRule.Multiline = True
        Me.txtCompRule.Name = "txtCompRule"
        Me.txtCompRule.ReadOnly = True
        Me.txtCompRule.ScrollBars = System.Windows.Forms.ScrollBars.Both
        Me.txtCompRule.Size = New System.Drawing.Size(594, 136)
        Me.txtCompRule.TabIndex = 0
        Me.txtCompRule.Text = ""
        Me.txtCompRule.WordWrap = False
        '
        'TreeViewer
        '
        Me.AutoScaleBaseSize = New System.Drawing.Size(5, 13)
        Me.ClientSize = New System.Drawing.Size(600, 446)
        Me.Controls.Add(Me.gbxComposite)
        Me.Controls.Add(Me.Splitter)
        Me.Controls.Add(Me.gbxRules)
        Me.Icon = CType(resources.GetObject("$this.Icon"), System.Drawing.Icon)
        Me.Name = "TreeViewer"
        Me.Text = "Worklet TreeViewer"
        Me.gbxRules.ResumeLayout(False)
        Me.gbxComposite.ResumeLayout(False)
        Me.ResumeLayout(False)

    End Sub

#End Region

    ' these two members are set by the calling form
    Public currentTree As RuleTree                       ' the rule tree to display
    Public imlNodes As ImageList                         ' the images for each node type

    Private ratio As Single = (2 / 3)                   ' the current size ratio between the two panes

    ' initialise the tree
    Private Sub TreeViewer_Load(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles MyBase.Load
        tvRules.ImageList = imlNodes
        tvMgr.LoadTreeIntoView(tvRules, currentTree, False)
    End Sub

    ' show the effective rule when a node is selected
    Private Sub tvRules_AfterSelect(ByVal sender As System.Object, ByVal e As System.Windows.Forms.TreeViewEventArgs) Handles tvRules.AfterSelect
        txtCompRule.Text = tvMgr.getEffectiveCondition(tvRules, currentTree)
    End Sub

    ' maintain the current ratio between the two panes when the user resizes the form
    Private Sub TreeViewer_Resize(ByVal sender As Object, ByVal e As System.EventArgs) Handles MyBase.Resize
        gbxRules.Height = Me.ClientSize.Height * ratio
    End Sub

    ' reset the relative size ratio between the two panes after the user moves the splitter
    Private Sub Splitter_SplitterMoved(ByVal sender As Object, ByVal e As System.Windows.Forms.SplitterEventArgs) Handles Splitter.SplitterMoved
        ratio = gbxRules.Height / Me.ClientSize.Height
    End Sub
End Class
