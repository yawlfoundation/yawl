' This file is made available under the terms of the LGPL licence.
' This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
' The source remains the property of the YAWL Foundation.  The YAWL Foundation
' is a collaboration of individuals and organisations who are commited to  
' improving workflow technology.

Imports System.IO

Public Class frmConfig
    Inherits System.Windows.Forms.Form

    ' This form allows the user to specify the file paths to various resources needed
    ' by the editor.

    ' The Worklet Service Rules Editor
    ' Author: Michael Adams
    '         BPM Group, QUT Australia
    ' Version: 0.8, 25/08/2006

    Private myPath As String = Application.StartupPath      ' default path 
    Private OKToClose As Boolean = True                     ' flag to block close
    Public isValid As Boolean = False                       ' true if inputs validate

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
    Friend WithEvents Label1 As System.Windows.Forms.Label
    Friend WithEvents txtRepository As System.Windows.Forms.TextBox
    Friend WithEvents btnOK As System.Windows.Forms.Button
    Friend WithEvents txtYAWLEditor As System.Windows.Forms.TextBox
    Friend WithEvents Label2 As System.Windows.Forms.Label
    Friend WithEvents Label3 As System.Windows.Forms.Label
    Friend WithEvents btnRepository As System.Windows.Forms.Button
    Friend WithEvents btnYAWLEditor As System.Windows.Forms.Button
    Friend WithEvents btnCancel As System.Windows.Forms.Button
    Friend WithEvents BrowserDialog As System.Windows.Forms.FolderBrowserDialog
    Friend WithEvents txtServiceURI As System.Windows.Forms.TextBox
    Friend WithEvents txtSpecPaths As System.Windows.Forms.TextBox
    Friend WithEvents Label4 As System.Windows.Forms.Label
    Friend WithEvents btnSpecPaths As System.Windows.Forms.Button
    Friend WithEvents ofdYAWL As System.Windows.Forms.OpenFileDialog
    <System.Diagnostics.DebuggerStepThrough()> Private Sub InitializeComponent()
        Dim resources As System.Resources.ResourceManager = New System.Resources.ResourceManager(GetType(frmConfig))
        Me.Label1 = New System.Windows.Forms.Label
        Me.txtRepository = New System.Windows.Forms.TextBox
        Me.btnOK = New System.Windows.Forms.Button
        Me.txtYAWLEditor = New System.Windows.Forms.TextBox
        Me.Label2 = New System.Windows.Forms.Label
        Me.txtServiceURI = New System.Windows.Forms.TextBox
        Me.Label3 = New System.Windows.Forms.Label
        Me.btnRepository = New System.Windows.Forms.Button
        Me.btnYAWLEditor = New System.Windows.Forms.Button
        Me.btnCancel = New System.Windows.Forms.Button
        Me.BrowserDialog = New System.Windows.Forms.FolderBrowserDialog
        Me.txtSpecPaths = New System.Windows.Forms.TextBox
        Me.Label4 = New System.Windows.Forms.Label
        Me.btnSpecPaths = New System.Windows.Forms.Button
        Me.ofdYAWL = New System.Windows.Forms.OpenFileDialog
        Me.SuspendLayout()
        '
        'Label1
        '
        Me.Label1.Location = New System.Drawing.Point(16, 56)
        Me.Label1.Name = "Label1"
        Me.Label1.Size = New System.Drawing.Size(112, 23)
        Me.Label1.TabIndex = 0
        Me.Label1.Text = "Worklet Repository:"
        '
        'txtRepository
        '
        Me.txtRepository.Location = New System.Drawing.Point(136, 56)
        Me.txtRepository.Name = "txtRepository"
        Me.txtRepository.Size = New System.Drawing.Size(304, 20)
        Me.txtRepository.TabIndex = 1
        Me.txtRepository.Text = "TextBox1"
        '
        'btnOK
        '
        Me.btnOK.Location = New System.Drawing.Point(152, 176)
        Me.btnOK.Name = "btnOK"
        Me.btnOK.TabIndex = 8
        Me.btnOK.Text = "OK"
        '
        'txtYAWLEditor
        '
        Me.txtYAWLEditor.Location = New System.Drawing.Point(136, 136)
        Me.txtYAWLEditor.Name = "txtYAWLEditor"
        Me.txtYAWLEditor.Size = New System.Drawing.Size(304, 20)
        Me.txtYAWLEditor.TabIndex = 4
        Me.txtYAWLEditor.Text = "TextBox2"
        '
        'Label2
        '
        Me.Label2.Location = New System.Drawing.Point(16, 136)
        Me.Label2.Name = "Label2"
        Me.Label2.Size = New System.Drawing.Size(80, 23)
        Me.Label2.TabIndex = 3
        Me.Label2.Text = "YAWL Editor:"
        '
        'txtServiceURI
        '
        Me.txtServiceURI.Location = New System.Drawing.Point(136, 16)
        Me.txtServiceURI.Name = "txtServiceURI"
        Me.txtServiceURI.Size = New System.Drawing.Size(304, 20)
        Me.txtServiceURI.TabIndex = 7
        Me.txtServiceURI.Text = "TextBox3"
        '
        'Label3
        '
        Me.Label3.Location = New System.Drawing.Point(16, 16)
        Me.Label3.Name = "Label3"
        Me.Label3.Size = New System.Drawing.Size(112, 23)
        Me.Label3.TabIndex = 6
        Me.Label3.Text = "Worklet Service URI:"
        '
        'btnRepository
        '
        Me.btnRepository.Location = New System.Drawing.Point(456, 56)
        Me.btnRepository.Name = "btnRepository"
        Me.btnRepository.Size = New System.Drawing.Size(24, 23)
        Me.btnRepository.TabIndex = 2
        Me.btnRepository.Text = "..."
        '
        'btnYAWLEditor
        '
        Me.btnYAWLEditor.Font = New System.Drawing.Font("Microsoft Sans Serif", 8.25!, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, CType(0, Byte))
        Me.btnYAWLEditor.Location = New System.Drawing.Point(456, 136)
        Me.btnYAWLEditor.Name = "btnYAWLEditor"
        Me.btnYAWLEditor.Size = New System.Drawing.Size(24, 23)
        Me.btnYAWLEditor.TabIndex = 5
        Me.btnYAWLEditor.Text = "..."
        '
        'btnCancel
        '
        Me.btnCancel.Location = New System.Drawing.Point(248, 176)
        Me.btnCancel.Name = "btnCancel"
        Me.btnCancel.TabIndex = 9
        Me.btnCancel.Text = "Cancel"
        '
        'BrowserDialog
        '
        Me.BrowserDialog.RootFolder = System.Environment.SpecialFolder.MyComputer
        Me.BrowserDialog.ShowNewFolderButton = False
        '
        'txtSpecPaths
        '
        Me.txtSpecPaths.Location = New System.Drawing.Point(136, 96)
        Me.txtSpecPaths.Name = "txtSpecPaths"
        Me.txtSpecPaths.Size = New System.Drawing.Size(304, 20)
        Me.txtSpecPaths.TabIndex = 10
        Me.txtSpecPaths.Text = "TextBox3"
        '
        'Label4
        '
        Me.Label4.Location = New System.Drawing.Point(16, 96)
        Me.Label4.Name = "Label4"
        Me.Label4.Size = New System.Drawing.Size(112, 23)
        Me.Label4.TabIndex = 11
        Me.Label4.Text = "Specification Paths:"
        '
        'btnSpecPaths
        '
        Me.btnSpecPaths.Location = New System.Drawing.Point(456, 96)
        Me.btnSpecPaths.Name = "btnSpecPaths"
        Me.btnSpecPaths.Size = New System.Drawing.Size(24, 23)
        Me.btnSpecPaths.TabIndex = 12
        Me.btnSpecPaths.Text = "..."
        '
        'ofdYAWL
        '
        Me.ofdYAWL.Filter = "JAR Files|*.jar|All Files|*.*"
        Me.ofdYAWL.RestoreDirectory = True
        Me.ofdYAWL.Title = "Select the YAWL Editor"
        '
        'frmConfig
        '
        Me.AcceptButton = Me.btnOK
        Me.AutoScaleBaseSize = New System.Drawing.Size(5, 13)
        Me.ClientSize = New System.Drawing.Size(498, 216)
        Me.Controls.Add(Me.btnSpecPaths)
        Me.Controls.Add(Me.Label4)
        Me.Controls.Add(Me.txtSpecPaths)
        Me.Controls.Add(Me.btnCancel)
        Me.Controls.Add(Me.btnYAWLEditor)
        Me.Controls.Add(Me.btnRepository)
        Me.Controls.Add(Me.txtServiceURI)
        Me.Controls.Add(Me.Label3)
        Me.Controls.Add(Me.txtYAWLEditor)
        Me.Controls.Add(Me.Label2)
        Me.Controls.Add(Me.btnOK)
        Me.Controls.Add(Me.txtRepository)
        Me.Controls.Add(Me.Label1)
        Me.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedDialog
        Me.Icon = CType(resources.GetObject("$this.Icon"), System.Drawing.Icon)
        Me.MaximizeBox = False
        Me.MinimizeBox = False
        Me.Name = "frmConfig"
        Me.StartPosition = System.Windows.Forms.FormStartPosition.CenterParent
        Me.Text = "Configure Paths"
        Me.ResumeLayout(False)

    End Sub

#End Region

    ' load paths to resources from file, or set to defaults
    Private Sub frmConfig_Load(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles MyBase.Load
        If File.Exists(myPath & "\RulesEditor.cfg") Then
            LoadPathsFromFile()
        Else
            LoadDefaultPaths()
        End If
    End Sub

    ' load the resource file paths from the config file
    Friend Sub LoadPathsFromFile()
        Dim cfgStream As StreamReader
        Dim cfgLine As String

        Try
            cfgStream = New StreamReader(myPath & "\RulesEditor.cfg")
            cfgLine = cfgStream.ReadLine
            txtRepository.Text = cfgLine.Substring(cfgLine.LastIndexOf("="c) + 1)
            cfgLine = cfgStream.ReadLine
            txtYAWLEditor.Text = cfgLine.Substring(cfgLine.LastIndexOf("="c) + 1)
            cfgLine = cfgStream.ReadLine
            txtServiceURI.Text = cfgLine.Substring(cfgLine.LastIndexOf("="c) + 1)
            cfgLine = cfgStream.ReadLine
            txtSpecPaths.Text = cfgLine.Substring(cfgLine.LastIndexOf("="c) + 1)
        Catch ex As Exception
            MessageBox.Show("Problem loading config - will use defaults", _
                            "Config File", MessageBoxButtons.OK, MessageBoxIcon.Error)
            LoadDefaultPaths()
        Finally
            If Not cfgStream Is Nothing Then cfgStream.Close()
        End Try

    End Sub

    ' save the resource file paths to the config file
    Friend Sub SavePathsToFile(ByVal validate As Boolean)
        Dim cfgStream As StreamWriter

        If validate Then
            If Not GoodPaths() Then         ' check paths in inputs are valid
                OKToClose = False
                isValid = False
                Exit Sub
            Else
                OKToClose = True
                isValid = True
            End If
        End If

        Try
            cfgStream = New StreamWriter(myPath & "\RulesEditor.cfg")
            cfgStream.WriteLine("Repository=" & txtRepository.Text)
            cfgStream.WriteLine("YAWLEditor=" & txtYAWLEditor.Text)
            cfgStream.WriteLine("ServiceURL=" & txtServiceURI.Text)
            cfgStream.WriteLine("SpecPaths=" & txtSpecPaths.Text)
        Catch ex As Exception
            MessageBox.Show("Problem saving config", _
                            "Config File", MessageBoxButtons.OK, MessageBoxIcon.Error)
        Finally
            If Not cfgStream Is Nothing Then cfgStream.Close()
        End Try

    End Sub

    'put some default paths in the inputs
    Friend Sub LoadDefaultPaths()
        txtRepository.Text = myPath.Substring(0, myPath.LastIndexOf("\"c))
        txtYAWLEditor.Text = myPath
        txtServiceURI.Text = "http://131.181.70.9:8080/workletService"
        txtSpecPaths.Clear()
    End Sub

    ' validate the inputs (calls validation methos in the global module)
    Friend Function GoodPaths() As Boolean
        Return GoodRepositoryPath(txtRepository.Text) AndAlso _
               GoodYAWLEditorPath(txtYAWLEditor.Text) AndAlso _
               ValidServiceURI(txtServiceURI.Text)
    End Function

    ' loads the dir browser dialog to locate the repository folder
    Private Sub btnRepository_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles btnRepository.Click
        With BrowserDialog
            .Description = "Select the worklet repository folder"
            .SelectedPath = txtRepository.Text
            If .ShowDialog() = DialogResult.OK Then
                txtRepository.Text = .SelectedPath
            End If
        End With
    End Sub

    ' loads the file open dialog to locate the yawl editor
    Private Sub btnYAWLEditor_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles btnYAWLEditor.Click
        With ofdYAWL
            If txtYAWLEditor.Text.Length = 0 Then
                .InitialDirectory = myPath
            Else
                .InitialDirectory = txtYAWLEditor.Text
            End If
            If .ShowDialog = DialogResult.OK Then
                txtYAWLEditor.Text = .FileName
            End If
        End With
    End Sub

    ' opens the browser dialog to allow user to specify path(s) to spec files
    Private Sub btnSpecPaths_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles btnSpecPaths.Click
        Dim currentPath As String = txtSpecPaths.Text
        With BrowserDialog
            .Description = "Select Specifications Folder(s)"
            If currentPath.Length = 0 Then
                .SelectedPath = myPath
            Else
                .SelectedPath = currentPath.Substring(currentPath.LastIndexOf(";"c))
            End If
            If .ShowDialog() = DialogResult.OK Then
                If currentPath.Length = 0 Then
                    txtSpecPaths.Text = .SelectedPath
                Else
                    txtSpecPaths.Text &= ";" & .SelectedPath
                End If
            End If
        End With
    End Sub

    Private Sub btnOK_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles btnOK.Click
        SavePathsToFile(True)                                                     ' validate and save
        Close()
    End Sub

    Private Sub frmConfig_Closing(ByVal sender As Object, ByVal e As System.ComponentModel.CancelEventArgs) Handles MyBase.Closing
        e.Cancel = Not OKToClose
    End Sub

    Private Sub btnCancel_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles btnCancel.Click

        'if Cancel, let it close
        OKToClose = True
        Close()
    End Sub
End Class
