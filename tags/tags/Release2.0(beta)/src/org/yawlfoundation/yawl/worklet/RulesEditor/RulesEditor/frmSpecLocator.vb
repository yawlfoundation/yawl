' This file is made available under the terms of the LGPL licence.
' This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
' The source remains the property of the YAWL Foundation.  The YAWL Foundation 
' is a collaboration of individuals and organisations who are committed to  
' improving workflow technology.


Option Strict On

Imports System.IO

Public Class frmSpecLocator
    Inherits System.Windows.Forms.Form

    ' This form is a simple dialog that is used when a new rule set is created. Its 
    ' purpose is to get the path to the specification file that the ruleset will
    ' serve.
    '
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
    Friend WithEvents btnCancel As System.Windows.Forms.Button
    Friend WithEvents btnBrowse As System.Windows.Forms.Button
    Friend WithEvents txtSpecFile As System.Windows.Forms.TextBox
    Friend WithEvents Label2 As System.Windows.Forms.Label
    Friend WithEvents btnOK As System.Windows.Forms.Button
    Friend WithEvents Label1 As System.Windows.Forms.Label
    Friend WithEvents ofdSpec As System.Windows.Forms.OpenFileDialog
    <System.Diagnostics.DebuggerStepThrough()> Private Sub InitializeComponent()
        Dim resources As System.Resources.ResourceManager = New System.Resources.ResourceManager(GetType(frmSpecLocator))
        Me.btnCancel = New System.Windows.Forms.Button
        Me.btnBrowse = New System.Windows.Forms.Button
        Me.txtSpecFile = New System.Windows.Forms.TextBox
        Me.Label2 = New System.Windows.Forms.Label
        Me.btnOK = New System.Windows.Forms.Button
        Me.Label1 = New System.Windows.Forms.Label
        Me.ofdSpec = New System.Windows.Forms.OpenFileDialog
        Me.SuspendLayout()
        '
        'btnCancel
        '
        Me.btnCancel.DialogResult = System.Windows.Forms.DialogResult.Cancel
        Me.btnCancel.Location = New System.Drawing.Point(244, 88)
        Me.btnCancel.Name = "btnCancel"
        Me.btnCancel.TabIndex = 14
        Me.btnCancel.Text = "Cancel"
        '
        'btnBrowse
        '
        Me.btnBrowse.Font = New System.Drawing.Font("Microsoft Sans Serif", 8.25!, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, CType(0, Byte))
        Me.btnBrowse.Location = New System.Drawing.Point(424, 48)
        Me.btnBrowse.Name = "btnBrowse"
        Me.btnBrowse.Size = New System.Drawing.Size(24, 23)
        Me.btnBrowse.TabIndex = 12
        Me.btnBrowse.Text = "..."
        '
        'txtSpecFile
        '
        Me.txtSpecFile.Location = New System.Drawing.Point(112, 48)
        Me.txtSpecFile.Name = "txtSpecFile"
        Me.txtSpecFile.Size = New System.Drawing.Size(304, 20)
        Me.txtSpecFile.TabIndex = 11
        Me.txtSpecFile.Text = ""
        '
        'Label2
        '
        Me.Label2.Location = New System.Drawing.Point(16, 48)
        Me.Label2.Name = "Label2"
        Me.Label2.TabIndex = 10
        Me.Label2.Text = "Specification File:"
        '
        'btnOK
        '
        Me.btnOK.DialogResult = System.Windows.Forms.DialogResult.OK
        Me.btnOK.Location = New System.Drawing.Point(144, 88)
        Me.btnOK.Name = "btnOK"
        Me.btnOK.TabIndex = 13
        Me.btnOK.Text = "OK"
        '
        'Label1
        '
        Me.Label1.AutoSize = True
        Me.Label1.Location = New System.Drawing.Point(16, 16)
        Me.Label1.Name = "Label1"
        Me.Label1.Size = New System.Drawing.Size(444, 16)
        Me.Label1.TabIndex = 15
        Me.Label1.Text = "Please select or enter the path to the specification file that the new rule set w" & _
        "ill describe:"
        '
        'ofdSpec
        '
        Me.ofdSpec.Filter = "Spec Files|*.xml"
        '
        'frmSpecLocator
        '
        Me.AutoScaleBaseSize = New System.Drawing.Size(5, 13)
        Me.ClientSize = New System.Drawing.Size(456, 118)
        Me.Controls.Add(Me.Label1)
        Me.Controls.Add(Me.btnCancel)
        Me.Controls.Add(Me.btnBrowse)
        Me.Controls.Add(Me.txtSpecFile)
        Me.Controls.Add(Me.Label2)
        Me.Controls.Add(Me.btnOK)
        Me.FormBorderStyle = System.Windows.Forms.FormBorderStyle.Fixed3D
        Me.Icon = CType(resources.GetObject("$this.Icon"), System.Drawing.Icon)
        Me.MaximizeBox = False
        Me.Name = "frmSpecLocator"
        Me.StartPosition = System.Windows.Forms.FormStartPosition.CenterParent
        Me.Text = "Specification Location"
        Me.ResumeLayout(False)

    End Sub

#End Region

    Private canClose As Boolean = True                          ' block a close when spec file is invalid
    Public rulesFileName As String                              ' name for rules file derived from spec name

    ' shows an open file dialog to allow selection of the spec file
    Private Sub btnBrowse_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles btnBrowse.Click
        With ofdSpec
            If txtSpecFile.Text.Length > 0 Then
                .InitialDirectory = txtSpecFile.Text            ' use whats in the input as the start path   
            ElseIf Not pathOf.repository Is Nothing Then
                .InitialDirectory = pathOf.repository & "\worklets"   ' otherwise try worklets dir
            Else
                .InitialDirectory = Application.StartupPath           ' otherwise default to startup  
            End If
            If .ShowDialog = DialogResult.OK Then
                txtSpecFile.Text = .FileName                    ' copy filename to input
                lastPathInSpecLocator = .FileName               ' save thispath for next time
            End If
        End With
    End Sub

    ' ok - only close if spec file validates
    Private Sub btnOK_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles btnOK.Click
        Dim spec As String = rsMgr.ExtractSpecName(txtSpecFile.Text)   ' get spec name from path
        rulesFileName = pathOf.repository & "\rules\" & spec & ".xrs"  ' make rules filename

        If spec.Length > 0 Then                                        ' only validate if text has been entered
            If Not txtSpecFile.Text.EndsWith(".xml") Then              ' not an xml file
                MessageBox.Show("'" & txtSpecFile.Text & "' does not appear to be a valid specification " & _
                                "file name." & vbCrLf & "Please modify" & _
                                " the specification name and click OK again, or click" & _
                                " Cancel to cancel the addition of a new rule set.", _
                                "Invalid Specification File Name", MessageBoxButtons.OK, MessageBoxIcon.Error)
                canClose = False                                       ' block the close 

            End If
            If File.Exists(rulesFileName) Then                         ' rules file already exists
                MessageBox.Show("A rules file with the specification name supplied" & _
                                " already exists." & vbCrLf & "Please modify" & _
                                " the specification name and click OK again, or click" & _
                                " Cancel to cancel the addition of a new rule set.", _
                                "Rules File Already Exists", MessageBoxButtons.OK, MessageBoxIcon.Error)
                canClose = False                                       ' block the close
            End If
        End If
    End Sub

    ' if an ok click did not validate, don't allow a close
    Private Sub frmSpecLocator_Closing(ByVal sender As Object, ByVal e As System.ComponentModel.CancelEventArgs) Handles MyBase.Closing
        e.Cancel = Not canClose
        canClose = True                  ' reset for next time
    End Sub

    Private Sub frmSpecLocator_Load(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles MyBase.Load
        If Not lastPathInSpecLocator Is Nothing Then                   ' if there's a previous path saved
            txtSpecFile.Text = lastPathInSpecLocator                   ' load it into input
        End If
    End Sub

End Class
