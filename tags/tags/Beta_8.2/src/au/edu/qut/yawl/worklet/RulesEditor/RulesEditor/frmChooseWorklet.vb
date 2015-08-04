' This file is made available under the terms of the LGPL licence.
' This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
' The source remains the property of the YAWL Foundation.  The YAWL Foundation
' is a collaboration of individuals and organisations who are commited to  
' improving workflow technology.

Option Strict On

Imports System.IO

Public Class frmChooseWorklet
    Inherits System.Windows.Forms.Form


    ' This form allows a user to choose a worklet for a rule from a list of
    ' all worklets in the repository.

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
    Friend WithEvents proEditor As System.Diagnostics.Process
    Friend WithEvents lbxWorklet As System.Windows.Forms.ListBox
    Friend WithEvents Label1 As System.Windows.Forms.Label
    Friend WithEvents btnOK As System.Windows.Forms.Button
    Friend WithEvents btnCancel As System.Windows.Forms.Button
    Friend WithEvents btnNew As System.Windows.Forms.Button
    <System.Diagnostics.DebuggerStepThrough()> Private Sub InitializeComponent()
        Dim resources As System.Resources.ResourceManager = New System.Resources.ResourceManager(GetType(frmChooseWorklet))
        Me.proEditor = New System.Diagnostics.Process
        Me.lbxWorklet = New System.Windows.Forms.ListBox
        Me.Label1 = New System.Windows.Forms.Label
        Me.btnOK = New System.Windows.Forms.Button
        Me.btnCancel = New System.Windows.Forms.Button
        Me.btnNew = New System.Windows.Forms.Button
        Me.SuspendLayout()
        '
        'proEditor
        '
        Me.proEditor.EnableRaisingEvents = True
        Me.proEditor.StartInfo.Arguments = "-jar YAWLEditor1.3.jar"
        Me.proEditor.StartInfo.CreateNoWindow = True
        Me.proEditor.StartInfo.FileName = "java"
        Me.proEditor.StartInfo.WindowStyle = System.Diagnostics.ProcessWindowStyle.Hidden
        Me.proEditor.SynchronizingObject = Me
        '
        'lbxWorklet
        '
        Me.lbxWorklet.Location = New System.Drawing.Point(16, 24)
        Me.lbxWorklet.Name = "lbxWorklet"
        Me.lbxWorklet.SelectionMode = System.Windows.Forms.SelectionMode.MultiExtended
        Me.lbxWorklet.Size = New System.Drawing.Size(248, 212)
        Me.lbxWorklet.Sorted = True
        Me.lbxWorklet.TabIndex = 0
        '
        'Label1
        '
        Me.Label1.Location = New System.Drawing.Point(16, 8)
        Me.Label1.Name = "Label1"
        Me.Label1.Size = New System.Drawing.Size(64, 16)
        Me.Label1.TabIndex = 1
        Me.Label1.Text = "Worklet:"
        '
        'btnOK
        '
        Me.btnOK.DialogResult = System.Windows.Forms.DialogResult.OK
        Me.btnOK.Enabled = False
        Me.btnOK.Location = New System.Drawing.Point(104, 256)
        Me.btnOK.Name = "btnOK"
        Me.btnOK.TabIndex = 2
        Me.btnOK.Text = "OK"
        '
        'btnCancel
        '
        Me.btnCancel.DialogResult = System.Windows.Forms.DialogResult.Cancel
        Me.btnCancel.Location = New System.Drawing.Point(192, 256)
        Me.btnCancel.Name = "btnCancel"
        Me.btnCancel.TabIndex = 3
        Me.btnCancel.Text = "Cancel"
        '
        'btnNew
        '
        Me.btnNew.Location = New System.Drawing.Point(16, 256)
        Me.btnNew.Name = "btnNew"
        Me.btnNew.TabIndex = 4
        Me.btnNew.Text = "&New..."
        '
        'frmChooseWorklet
        '
        Me.AutoScaleBaseSize = New System.Drawing.Size(5, 13)
        Me.ClientSize = New System.Drawing.Size(282, 296)
        Me.Controls.Add(Me.btnNew)
        Me.Controls.Add(Me.btnCancel)
        Me.Controls.Add(Me.btnOK)
        Me.Controls.Add(Me.Label1)
        Me.Controls.Add(Me.lbxWorklet)
        Me.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedSingle
        Me.Icon = CType(resources.GetObject("$this.Icon"), System.Drawing.Icon)
        Me.MaximizeBox = False
        Me.Name = "frmChooseWorklet"
        Me.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen
        Me.Text = "Choose Worklet"
        Me.ResumeLayout(False)

    End Sub

#End Region

    Public workletSelections As String

    ' fill listbox with list of worklet spec files
    Private Function FillWorkletList() As Boolean
        Dim di As DirectoryInfo = New DirectoryInfo(pathOf.repository & "\worklets")
        Dim fi As FileInfo
        Dim fName As String

        ' if the repository path is bad, go no further
        If Not di.Exists Then
            MessageBox.Show("Can't load worklet names because the specified path " & _
                            "is invalid. Please specify a valid path to the " & _
                            "worklet repository folder in Options...Configure.", _
            "Invalid Repository Path", MessageBoxButtons.OK, MessageBoxIcon.Error)
            Return False
        End If

        ' add worklet names to file
        For Each fi In di.GetFiles("*.xml")
            fName = fi.Name.Remove(fi.Name.LastIndexOf(".xml"), 4)       ' cut extn
            lbxWorklet.Items.Add(fName)
        Next

        Return True
    End Function


    ' adds names  to combo of any new worklet(s) created by yawl editor
    Private Sub UpdateWorkletList()
        Dim di As DirectoryInfo = New DirectoryInfo(pathOf.repository & "\worklets")
        Dim fi As FileInfo
        Dim fName As String

        ' if the repository path is bad, go no further
        If Not di.Exists Then
            MessageBox.Show("Can't update worklet names because the specified path " & _
                            "is invalid. Please specify a valid path to the " & _
                            "worklet repository folder in Options...Configure.", _
            "Invalid Repository Path", MessageBoxButtons.OK, MessageBoxIcon.Error)
            Exit Sub
        End If

        ' add new worklet names to file
        For Each fi In di.GetFiles("*.xml")
            fName = fi.Name.Remove(fi.Name.LastIndexOf(".xml"), 4)

            ' only add new worklets (don't double up in list)
            With lbxWorklet
                If .Items.IndexOf(fName) = -1 Then
                    .Items.Add(fName)
                    .SelectedIndex = .Items.IndexOf(fName)    ' show new worklet as selected
                End If
            End With
        Next
    End Sub

    ' double click to select worklet closes form (except if 'define new worklet' selected)
    Private Sub lbxWorklet_DoubleClick(ByVal sender As Object, ByVal e As System.EventArgs) Handles lbxWorklet.DoubleClick
        btnOK.PerformClick()
    End Sub

    ' inits the form
    Private Sub frmChooseWorklet_Load(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles MyBase.Load

        If GoodYAWLEditorPath(pathOf.YAWLEditor) Then

            ' split the full path to editor into path / filename
            Dim splitIndex As Integer = pathOf.YAWLEditor.LastIndexOf("\"c)
            Dim editorDir As String = pathOf.YAWLEditor.Substring(0, splitIndex)
            Dim editorName As String = pathOf.YAWLEditor.Substring(splitIndex + 1)

            ' set start dir and arguments for process
            proEditor.StartInfo.WorkingDirectory = editorDir
            proEditor.StartInfo.Arguments = "-jar " & editorName

            ' add an Exited event handler for the YAWL Editor process
            AddHandler proEditor.Exited, AddressOf Me.ProcessExited
        Else
            Close()
        End If

        ' if bad repository path, can't continue
        If Not FillWorkletList() Then Close()
    End Sub

    ' the user has selected a worklet
    Private Sub lbxWorklet_SelectedIndexChanged(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles lbxWorklet.SelectedIndexChanged

        ' enable ok button if some item is selected (other than the first one)
        btnOK.Enabled = lbxWorklet.SelectedIndex > -1
    End Sub

    ' event handler that fires when YAWL Editor closes
    Friend Sub ProcessExited(ByVal sender As Object, ByVal e As System.EventArgs)
        UpdateWorkletList()
    End Sub

    Private Sub btnNew_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles btnNew.Click
        proEditor.Start()                ' start the Process to load the YAWL Editor
    End Sub

    Private Sub btnOK_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles btnOK.Click

        workletSelections = ""                                       ' clear the result string

        For i As Integer = 0 To lbxWorklet.Items.Count - 1
            If lbxWorklet.GetSelected(i) Then                        ' if the item is selected
                If workletSelections.Length > 0 Then
                    workletSelections &= ","                         ' multi-select - sep with commas
                End If
                workletSelections &= lbxWorklet.Items(i).ToString    ' add to list  
            End If
        Next

    End Sub
End Class
