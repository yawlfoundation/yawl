' This file is made available under the terms of the LGPL licence.
' This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
' The source remains the property of the YAWL Foundation.  The YAWL Foundation
' is a collaboration of individuals and organisations who are commited to  
' improving workflow technology.

Friend Class frmDrawConc
    Inherits System.Windows.Forms.Form

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
    Friend WithEvents pbCanvas As System.Windows.Forms.Panel
    Friend WithEvents btnOK As System.Windows.Forms.Button
    Friend WithEvents btnCancel As System.Windows.Forms.Button
    Friend WithEvents ToolTip1 As System.Windows.Forms.ToolTip
    Friend WithEvents ToolBarButton1 As System.Windows.Forms.ToolBarButton
    Friend WithEvents ToolBarButton2 As System.Windows.Forms.ToolBarButton
    Friend WithEvents ToolBarButton3 As System.Windows.Forms.ToolBarButton
    Friend WithEvents ToolBarButton4 As System.Windows.Forms.ToolBarButton
    Friend WithEvents ToolBarButton5 As System.Windows.Forms.ToolBarButton
    Friend WithEvents ToolBarButton6 As System.Windows.Forms.ToolBarButton
    Friend WithEvents ToolBarButton7 As System.Windows.Forms.ToolBarButton
    Friend WithEvents ToolBarButton8 As System.Windows.Forms.ToolBarButton
    Friend WithEvents ToolBarButton9 As System.Windows.Forms.ToolBarButton
    Friend WithEvents ToolBarButton10 As System.Windows.Forms.ToolBarButton
    Friend WithEvents ToolBarButton11 As System.Windows.Forms.ToolBarButton
    Friend WithEvents ToolBarButton12 As System.Windows.Forms.ToolBarButton
    Friend WithEvents imlTools As System.Windows.Forms.ImageList
    Friend WithEvents ToolBar As System.Windows.Forms.ToolBar
    Friend WithEvents ToolBarButton13 As System.Windows.Forms.ToolBarButton
    Friend WithEvents ToolBarButton14 As System.Windows.Forms.ToolBarButton
    Friend WithEvents ToolBarButton15 As System.Windows.Forms.ToolBarButton
    Friend WithEvents pbStart As System.Windows.Forms.PictureBox
    Friend WithEvents pbStop As System.Windows.Forms.PictureBox
    Friend WithEvents btnClear As System.Windows.Forms.Button
    Friend WithEvents btnAlign As System.Windows.Forms.Button
    <System.Diagnostics.DebuggerStepThrough()> Private Sub InitializeComponent()
        Me.components = New System.ComponentModel.Container
        Dim resources As System.Resources.ResourceManager = New System.Resources.ResourceManager(GetType(frmDrawConc))
        Me.pbCanvas = New System.Windows.Forms.Panel
        Me.pbStop = New System.Windows.Forms.PictureBox
        Me.pbStart = New System.Windows.Forms.PictureBox
        Me.btnOK = New System.Windows.Forms.Button
        Me.btnCancel = New System.Windows.Forms.Button
        Me.ToolTip1 = New System.Windows.Forms.ToolTip(Me.components)
        Me.ToolBar = New System.Windows.Forms.ToolBar
        Me.ToolBarButton1 = New System.Windows.Forms.ToolBarButton
        Me.ToolBarButton2 = New System.Windows.Forms.ToolBarButton
        Me.ToolBarButton3 = New System.Windows.Forms.ToolBarButton
        Me.ToolBarButton4 = New System.Windows.Forms.ToolBarButton
        Me.ToolBarButton5 = New System.Windows.Forms.ToolBarButton
        Me.ToolBarButton6 = New System.Windows.Forms.ToolBarButton
        Me.ToolBarButton7 = New System.Windows.Forms.ToolBarButton
        Me.ToolBarButton8 = New System.Windows.Forms.ToolBarButton
        Me.ToolBarButton9 = New System.Windows.Forms.ToolBarButton
        Me.ToolBarButton10 = New System.Windows.Forms.ToolBarButton
        Me.ToolBarButton11 = New System.Windows.Forms.ToolBarButton
        Me.ToolBarButton12 = New System.Windows.Forms.ToolBarButton
        Me.ToolBarButton13 = New System.Windows.Forms.ToolBarButton
        Me.ToolBarButton14 = New System.Windows.Forms.ToolBarButton
        Me.ToolBarButton15 = New System.Windows.Forms.ToolBarButton
        Me.imlTools = New System.Windows.Forms.ImageList(Me.components)
        Me.btnClear = New System.Windows.Forms.Button
        Me.btnAlign = New System.Windows.Forms.Button
        Me.pbCanvas.SuspendLayout()
        Me.SuspendLayout()
        '
        'pbCanvas
        '
        Me.pbCanvas.AllowDrop = True
        Me.pbCanvas.Anchor = CType((((System.Windows.Forms.AnchorStyles.Top Or System.Windows.Forms.AnchorStyles.Bottom) _
                    Or System.Windows.Forms.AnchorStyles.Left) _
                    Or System.Windows.Forms.AnchorStyles.Right), System.Windows.Forms.AnchorStyles)
        Me.pbCanvas.AutoScroll = True
        Me.pbCanvas.BackColor = System.Drawing.SystemColors.Window
        Me.pbCanvas.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle
        Me.pbCanvas.Controls.Add(Me.pbStop)
        Me.pbCanvas.Controls.Add(Me.pbStart)
        Me.pbCanvas.Location = New System.Drawing.Point(136, 8)
        Me.pbCanvas.Name = "pbCanvas"
        Me.pbCanvas.Size = New System.Drawing.Size(520, 184)
        Me.pbCanvas.TabIndex = 0
        '
        'pbStop
        '
        Me.pbStop.Anchor = System.Windows.Forms.AnchorStyles.Right
        Me.pbStop.Image = CType(resources.GetObject("pbStop.Image"), System.Drawing.Image)
        Me.pbStop.Location = New System.Drawing.Point(472, 64)
        Me.pbStop.Name = "pbStop"
        Me.pbStop.Size = New System.Drawing.Size(32, 32)
        Me.pbStop.SizeMode = System.Windows.Forms.PictureBoxSizeMode.StretchImage
        Me.pbStop.TabIndex = 1
        Me.pbStop.TabStop = False
        '
        'pbStart
        '
        Me.pbStart.Anchor = System.Windows.Forms.AnchorStyles.Left
        Me.pbStart.Image = CType(resources.GetObject("pbStart.Image"), System.Drawing.Image)
        Me.pbStart.Location = New System.Drawing.Point(16, 64)
        Me.pbStart.Name = "pbStart"
        Me.pbStart.Size = New System.Drawing.Size(32, 32)
        Me.pbStart.SizeMode = System.Windows.Forms.PictureBoxSizeMode.StretchImage
        Me.pbStart.TabIndex = 0
        Me.pbStart.TabStop = False
        '
        'btnOK
        '
        Me.btnOK.Anchor = System.Windows.Forms.AnchorStyles.Bottom
        Me.btnOK.DialogResult = System.Windows.Forms.DialogResult.OK
        Me.btnOK.Location = New System.Drawing.Point(176, 208)
        Me.btnOK.Name = "btnOK"
        Me.btnOK.TabIndex = 3
        Me.btnOK.Text = "Save"
        '
        'btnCancel
        '
        Me.btnCancel.Anchor = System.Windows.Forms.AnchorStyles.Bottom
        Me.btnCancel.DialogResult = System.Windows.Forms.DialogResult.Cancel
        Me.btnCancel.Location = New System.Drawing.Point(464, 208)
        Me.btnCancel.Name = "btnCancel"
        Me.btnCancel.TabIndex = 4
        Me.btnCancel.Text = "Cancel"
        '
        'ToolBar
        '
        Me.ToolBar.AutoSize = False
        Me.ToolBar.Buttons.AddRange(New System.Windows.Forms.ToolBarButton() {Me.ToolBarButton1, Me.ToolBarButton2, Me.ToolBarButton3, Me.ToolBarButton4, Me.ToolBarButton5, Me.ToolBarButton6, Me.ToolBarButton7, Me.ToolBarButton8, Me.ToolBarButton9, Me.ToolBarButton10, Me.ToolBarButton11, Me.ToolBarButton12, Me.ToolBarButton13, Me.ToolBarButton14, Me.ToolBarButton15})
        Me.ToolBar.ButtonSize = New System.Drawing.Size(40, 40)
        Me.ToolBar.Dock = System.Windows.Forms.DockStyle.None
        Me.ToolBar.DropDownArrows = True
        Me.ToolBar.ImageList = Me.imlTools
        Me.ToolBar.Location = New System.Drawing.Point(8, 8)
        Me.ToolBar.Name = "ToolBar"
        Me.ToolBar.ShowToolTips = True
        Me.ToolBar.Size = New System.Drawing.Size(120, 208)
        Me.ToolBar.TabIndex = 5
        '
        'ToolBarButton1
        '
        Me.ToolBarButton1.ImageIndex = 0
        Me.ToolBarButton1.Style = System.Windows.Forms.ToolBarButtonStyle.ToggleButton
        Me.ToolBarButton1.Tag = "0"
        Me.ToolBarButton1.ToolTipText = "Remove WorkItem"
        '
        'ToolBarButton2
        '
        Me.ToolBarButton2.ImageIndex = 1
        Me.ToolBarButton2.Style = System.Windows.Forms.ToolBarButtonStyle.ToggleButton
        Me.ToolBarButton2.Tag = "1"
        Me.ToolBarButton2.ToolTipText = "Remove Case"
        '
        'ToolBarButton3
        '
        Me.ToolBarButton3.ImageIndex = 2
        Me.ToolBarButton3.Style = System.Windows.Forms.ToolBarButtonStyle.ToggleButton
        Me.ToolBarButton3.Tag = "2"
        Me.ToolBarButton3.ToolTipText = "Remove All Cases"
        '
        'ToolBarButton4
        '
        Me.ToolBarButton4.ImageIndex = 3
        Me.ToolBarButton4.Style = System.Windows.Forms.ToolBarButtonStyle.ToggleButton
        Me.ToolBarButton4.Tag = "3"
        Me.ToolBarButton4.ToolTipText = "Suspend Workitem"
        '
        'ToolBarButton5
        '
        Me.ToolBarButton5.ImageIndex = 4
        Me.ToolBarButton5.Style = System.Windows.Forms.ToolBarButtonStyle.ToggleButton
        Me.ToolBarButton5.Tag = "4"
        Me.ToolBarButton5.ToolTipText = "Suspend Case"
        '
        'ToolBarButton6
        '
        Me.ToolBarButton6.ImageIndex = 5
        Me.ToolBarButton6.Style = System.Windows.Forms.ToolBarButtonStyle.ToggleButton
        Me.ToolBarButton6.Tag = "5"
        Me.ToolBarButton6.ToolTipText = "Suspend All Cases"
        '
        'ToolBarButton7
        '
        Me.ToolBarButton7.ImageIndex = 6
        Me.ToolBarButton7.Style = System.Windows.Forms.ToolBarButtonStyle.ToggleButton
        Me.ToolBarButton7.Tag = "6"
        Me.ToolBarButton7.ToolTipText = "Continue Workitem"
        '
        'ToolBarButton8
        '
        Me.ToolBarButton8.ImageIndex = 7
        Me.ToolBarButton8.Style = System.Windows.Forms.ToolBarButtonStyle.ToggleButton
        Me.ToolBarButton8.Tag = "7"
        Me.ToolBarButton8.ToolTipText = "Continue Case"
        '
        'ToolBarButton9
        '
        Me.ToolBarButton9.ImageIndex = 8
        Me.ToolBarButton9.Style = System.Windows.Forms.ToolBarButtonStyle.ToggleButton
        Me.ToolBarButton9.Tag = "8"
        Me.ToolBarButton9.ToolTipText = "Continue All Cases"
        '
        'ToolBarButton10
        '
        Me.ToolBarButton10.ImageIndex = 9
        Me.ToolBarButton10.Style = System.Windows.Forms.ToolBarButtonStyle.ToggleButton
        Me.ToolBarButton10.Tag = "9"
        Me.ToolBarButton10.ToolTipText = "Restart Workitem"
        '
        'ToolBarButton11
        '
        Me.ToolBarButton11.ImageIndex = 10
        Me.ToolBarButton11.Style = System.Windows.Forms.ToolBarButtonStyle.ToggleButton
        Me.ToolBarButton11.Tag = "10"
        Me.ToolBarButton11.ToolTipText = "Force Complete Workitem"
        '
        'ToolBarButton12
        '
        Me.ToolBarButton12.ImageIndex = 11
        Me.ToolBarButton12.Style = System.Windows.Forms.ToolBarButtonStyle.ToggleButton
        Me.ToolBarButton12.Tag = "11"
        Me.ToolBarButton12.ToolTipText = "Force Fail Workitem"
        '
        'ToolBarButton13
        '
        Me.ToolBarButton13.ImageIndex = 12
        Me.ToolBarButton13.Style = System.Windows.Forms.ToolBarButtonStyle.ToggleButton
        Me.ToolBarButton13.Tag = "12"
        Me.ToolBarButton13.ToolTipText = "Compensate (run Worklet)"
        '
        'ToolBarButton14
        '
        Me.ToolBarButton14.ImageIndex = 13
        Me.ToolBarButton14.Style = System.Windows.Forms.ToolBarButtonStyle.ToggleButton
        Me.ToolBarButton14.Tag = "13"
        Me.ToolBarButton14.ToolTipText = "Selection Tool"
        '
        'ToolBarButton15
        '
        Me.ToolBarButton15.ImageIndex = 14
        Me.ToolBarButton15.Style = System.Windows.Forms.ToolBarButtonStyle.ToggleButton
        Me.ToolBarButton15.Tag = "14"
        Me.ToolBarButton15.ToolTipText = "Arc Tool"
        '
        'imlTools
        '
        Me.imlTools.ColorDepth = System.Windows.Forms.ColorDepth.Depth16Bit
        Me.imlTools.ImageSize = New System.Drawing.Size(32, 32)
        Me.imlTools.ImageStream = CType(resources.GetObject("imlTools.ImageStream"), System.Windows.Forms.ImageListStreamer)
        Me.imlTools.TransparentColor = System.Drawing.Color.White
        '
        'btnClear
        '
        Me.btnClear.Anchor = System.Windows.Forms.AnchorStyles.Bottom
        Me.btnClear.Location = New System.Drawing.Point(368, 208)
        Me.btnClear.Name = "btnClear"
        Me.btnClear.TabIndex = 6
        Me.btnClear.Text = "&Clear"
        '
        'btnAlign
        '
        Me.btnAlign.Anchor = System.Windows.Forms.AnchorStyles.Bottom
        Me.btnAlign.Location = New System.Drawing.Point(272, 208)
        Me.btnAlign.Name = "btnAlign"
        Me.btnAlign.TabIndex = 7
        Me.btnAlign.Text = "&Align"
        '
        'frmDrawConc
        '
        Me.AutoScaleBaseSize = New System.Drawing.Size(5, 13)
        Me.ClientSize = New System.Drawing.Size(664, 246)
        Me.Controls.Add(Me.btnAlign)
        Me.Controls.Add(Me.btnClear)
        Me.Controls.Add(Me.ToolBar)
        Me.Controls.Add(Me.btnCancel)
        Me.Controls.Add(Me.btnOK)
        Me.Controls.Add(Me.pbCanvas)
        Me.Icon = CType(resources.GetObject("$this.Icon"), System.Drawing.Icon)
        Me.Name = "frmDrawConc"
        Me.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen
        Me.Text = "Worklet Rules Editor: <New Conclusion>"
        Me.pbCanvas.ResumeLayout(False)
        Me.ResumeLayout(False)

    End Sub

#End Region

    ' This form provides a tool to graphically represent an exception handling process. 
    '
    ' The Worklet Service Rules Editor
    ' Author: Michael Adams
    '         BPM Group, QUT Australia
    ' Version: 0.8, 25/08/2006


    '************************************************************************************************'

    ' A simple sub-class that represents one 'primitive' in the defined exception process
    Class ProcNode
        Public item As PictureBox                    ' encapsulating box to display icon on canvas
        Public nextItem As ProcNode                  ' next node in the process
        Public prevItem As ProcNode                  ' previous node in the process
        Public isStartNode As Boolean                ' true if this is the first node
        Public isEndNode As Boolean                  ' true if this is the last node 
        Public toolType As tool                      ' the kind of primitive for this node
        Public worklet As String                     ' the name of the compenatory worklet (if required)
        Public ancestorChecked As Boolean = False    ' for 'allcases' tools, limits to ancestor cases only

        Public Function hasPrevItem() As Boolean     ' does this node have a previous item
            Return (Not prevItem Is Nothing)
        End Function

        Public Function hasNextItem() As Boolean     ' does this node have a next item
            Return (Not nextItem Is Nothing)
        End Function

        Public Function hasWorklet() As Boolean      ' is there a worklet defined for this node
            If toolType = tool.compensate Then
                Return (Not worklet Is Nothing)
            Else
                Return True
            End If
        End Function

        Public Function MayBeSelected() As Boolean    ' start and end nodes may not be user selected   
            Return Not (isStartNode OrElse isEndNode)
        End Function

        Public Function isTerminalNode() As Boolean   ' a terminal node is either the start or end node
            Return Not MayBeSelected()
        End Function

        Public Function isLooseNode() As Boolean      ' an unconected node
            Return Not (hasNextItem() OrElse hasPrevItem())
        End Function

    End Class  ' ProcNode

    '************************************************************************************************'

    ' defines an arc that is in the process of being drawn
    Structure sArc
        Public startPoint As Point                ' edge of start node where arc begins
        Public endPoint As Point                  ' edge of end node whre arc completes
        Public startNode As ProcNode              ' outgoing node 
        Public EndNode As ProcNode                ' incoming node
        Public hasStartPoint As Boolean
    End Structure

    ' an enumeration of the available tools
    Public Enum tool
        none = -1
        remove = 0
        removeCase = 1
        removeAll = 2
        suspend = 3
        suspendCase = 4
        suspendAll = 5
        continue = 6
        continueCase = 7
        continueAll = 8
        restart = 9
        forceComplete = 10
        forceFail = 11
        compensate = 12
        selector = 13                     ' pointer
        arrow = 14                        ' arc tool
    End Enum

    ' an enumeration of the edges of the icon, used to position arcs
    Private Enum edge
        top
        right
        bottom
        left
    End Enum

    Private nodeList As New ArrayList(10)                   ' list of drawn primitives 
    Private selectedTool As tool = tool.none                ' the currently selected tool
    Private selectedNode As ProcNode                        ' the currently selected node 
    Private currentArc As sArc                              ' the arc being drawn 
    Public finalConclusion() As ConclusionItem              ' the resulting process as text
    Public CancelClose As Boolean = False                   ' flag to block close on bad validation


    '***********************************************************************************************
    '****** MOUSE EVENTS ***************************************************************************
    '***********************************************************************************************

    ' Begins the drawing of an arc or the selection of a node (depending on the currently selected tool)
    ' whan the mouse is clicked and held over a picturebox 'icon'.
    ' Used by all picturebox icons on the drawing canvas.
    Private Sub PB_MouseDown(ByVal sender As System.Object, ByVal e As System.Windows.Forms.MouseEventArgs) _
            Handles pbStart.MouseDown

        Dim startBox As PictureBox = DirectCast(sender, PictureBox)      ' the box the mouse is down on
        Dim pNode As ProcNode = getProcNode(startBox)                    ' the node for this picturebox

        If selectedTool = tool.arrow Then                                ' if the arc tool is selected  
            If Not pNode.hasNextItem Then                                ' .. and there's no outgoing arc
                With currentArc                                          ' start drawing an arc
                    .startPoint = getLineAttachPoint(pNode, edge.right)
                    .startNode = pNode
                    .hasStartPoint = True
                End With
            End If
        ElseIf selectedTool = tool.selector Then                         ' if the pointer tool is selected
            If pNode.MayBeSelected Then                                  ' and its not a terminal node
                selectedNode = pNode                                     ' mark it as selected
                startBox.DoDragDrop(startBox, DragDropEffects.Move)      ' and begin a drag process (so that  
            End If                                                       '   user can move it)  
        End If
    End Sub

    ' When the arc tool is selected, draws an interim arc between the edge of the starting box and the cursor
    Private Sub PB_MouseMove(ByVal sender As Object, ByVal e As System.Windows.Forms.MouseEventArgs) Handles pbStart.MouseMove

        If selectedTool = tool.arrow Then
            If currentArc.hasStartPoint Then                             ' i.e. in a drag operation
                UnDrawInterimArc()                                       ' remove previous interim arc
                currentArc.endPoint = PictXYToCanvasXY(DirectCast(sender, PictureBox), e.X, e.Y)
                DrawInterimArc()                                         ' draw arc to cursor
            End If
        End If

    End Sub

    ' Completes a drag operation when the arc tool is selected and the mouse is released over a picturebox
    Private Sub PB_MouseUp(ByVal sender As Object, ByVal e As System.Windows.Forms.MouseEventArgs) Handles pbStart.MouseUp
        Dim upPoint As Point = PictXYToCanvasXY(DirectCast(sender, PictureBox), e.X, e.Y)
        Dim pbEnding As PictureBox
        Dim endNode As ProcNode

        If (selectedTool <> tool.none) Then                              ' if arc tool selected and in a drag op.
            If (selectedTool = tool.arrow) AndAlso currentArc.hasStartPoint Then
                UnDrawInterimArc()
                pbEnding = pbCanvas.GetChildAtPoint(upPoint)             ' if mouse-up occurred on icon, get it
                If Not pbEnding Is Nothing Then
                    If (Not pbEnding Is currentArc.startNode.item) Then  ' if the end pbox is not the start pbox 
                        endNode = getProcNode(pbEnding)                  ' get the node this pbox contains
                        If Not endNode.hasPrevItem Then                  ' if node doesn't have an incoming arc  
                            currentArc.EndNode = endNode
                            currentArc.endPoint = upPoint
                            DrawArc()                                    ' draw the arc between the icons
                            updatenodeList()                             ' set prev/next pointers
                        End If
                    End If
                End If
            End If

            currentArc = Nothing                                         ' done with this arc
            pbCanvas.Refresh()                                           ' show the arc
        End If
    End Sub

    ' start a drag operation when a tool is selected and the mouse moves to the canvas
    Private Sub pbCanvas_DragEnter(ByVal sender As System.Object, ByVal e As System.Windows.Forms.DragEventArgs) Handles pbCanvas.DragEnter
        e.Effect = DragDropEffects.Move
    End Sub

    ' move currently selected node to it's new location as specified by the mouse pointer
    Private Sub pbCanvas_DragDrop(ByVal sender As System.Object, ByVal e As System.Windows.Forms.DragEventArgs) Handles pbCanvas.DragDrop
        selectedNode.item.Left = e.X - Me.Left - pbCanvas.Left - 16
        selectedNode.item.Top = e.Y - Me.Top - pbCanvas.Top - 48
        selectedNode = Nothing
        pbCanvas.Refresh()
    End Sub

    ' erase an interim arc or place a new icon when mouse up occurs (not over picturebox)
    Private Sub pbCanvas_MouseUp(ByVal sender As Object, ByVal e As System.Windows.Forms.MouseEventArgs) Handles pbCanvas.MouseUp

        If selectedTool = tool.arrow Then            ' mouse-up when arc not concluding over a node
            UnDrawInterimArc()                       ' remove interim arc  
            currentArc = Nothing
        ElseIf (selectedTool <> tool.none) Then      ' otherwise drop a new node icon onto the canvas
            AddNode(e.X, e.Y, selectedTool)
        End If
    End Sub


    '***********************************************************************************************
    ' ***** BUTTON EVENTS **************************************************************************
    '***********************************************************************************************

    ' a 'tool' button has been clicked
    Private Sub ToolBar_ButtonClick(ByVal sender As System.Object, ByVal e As System.Windows.Forms.ToolBarButtonClickEventArgs) Handles ToolBar.ButtonClick
        Dim tempButton As ToolBarButton

        If e.Button.Pushed Then                            ' toggled on
            selectedTool = e.Button.Tag                    ' set the selected tool

            ' toggle off all other buttons
            For Each tempButton In ToolBar.Buttons
                If tempButton.Tag <> selectedTool Then tempButton.Pushed = False
            Next
        Else
            selectedTool = tool.none                       ' toggled off - no tool selected
        End If
    End Sub

    ' ok - if the graph is valid, textify the graph & close the form
    Private Sub btnOK_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles btnOK.Click
        If ValidGraph() Then
            finalConclusion = ComposeGraphXML()          ' write to the conclusion structure 
            Close()                                      ' and exit form
        Else
            CancelClose = True                           ' didn't validate - block closing
        End If
    End Sub

    ' removes all nodes from canvas and nodelist (besides start & stop)
    Private Sub btnClear_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles btnClear.Click
        Dim pNode As ProcNode
        Dim idx As Integer
        Dim removeList(nodeList.Count - 3) As ProcNode    ' -1 for zero-index, -2 for start & stop nodes

        ' get all nodes (except start & stop)
        For Each pNode In nodeList
            If pNode.MayBeSelected() Then
                removeList(idx) = pNode
                idx += 1
            End If
        Next

        ' remove those nodes from the list & canvas
        For Each pNode In removeList
            RemoveNode(pNode)
        Next

        'refresh vars
        selectedNode = Nothing
        currentArc = Nothing
        pbCanvas.Refresh()                                ' will remove arcs also
    End Sub

    ' align all icons and arcs 
    Private Sub btnAlign_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles btnAlign.Click
        ReAlignNodes()
    End Sub


    '***********************************************************************************************
    ' ***** CONTEXT MENU METHODS & EVENTS **********************************************************
    '***********************************************************************************************

    ' creates a context menu for all drawing icons
    Private Function getContextMenu(ByVal tooltype As tool) As ContextMenu
        Dim result As New ContextMenu
        Dim mnuAncestor As MenuItem

        ' add delete function to all icons
        result.MenuItems.Add(New MenuItem("&Delete", New EventHandler(AddressOf cmDelete_Click)))

        ' add additional items as required
        Select Case tooltype
            Case tool.compensate                                                    ' compensation icons only
                result.MenuItems.Add(New MenuItem("Define &Worklet", New EventHandler(AddressOf cmWorklet_Click)))
            Case tool.continueAll, tool.removeAll, tool.suspendAll                  ' "allCases" icons only
                result.MenuItems.Add(New MenuItem("&Ancestors Only", New EventHandler(AddressOf cmAncestors_Click)))
        End Select

        Return result
    End Function

    ' deletes an icon from the canvas (available to all icons)
    Private Sub cmDelete_Click(ByVal sender As System.Object, ByVal e As System.EventArgs)
        Dim sourceMenu As Menu = DirectCast(sender, MenuItem).Parent                  ' which context menu?
        Dim delBox As PictureBox = DirectCast(sourceMenu, ContextMenu).SourceControl  ' who owns it?
        RemoveNode(getProcNode(delBox))                                               ' delete the owner
        pbCanvas.Refresh()                                                            ' redraw
    End Sub

    ' adds a worklet to a compensation icon (only available to comp. icons)
    Private Sub cmWorklet_Click(ByVal sender As System.Object, ByVal e As System.EventArgs)
        Dim sourceMenu As Menu = DirectCast(sender, MenuItem).Parent                          ' which menu?  
        Dim node As ProcNode = getProcNode(DirectCast(sourceMenu, ContextMenu).SourceControl) ' which node?
        Dim workletForm As New frmChooseWorklet

        If workletForm.ShowDialog = DialogResult.OK Then                                  ' user selects worklet  
            node.worklet = workletForm.workletSelections                                  ' save the name(s)
        End If
    End Sub

    ' limits 'allCases' to 'ancestorsOnly' (available to suspendAll, removeAll, continueAll)
    Private Sub cmAncestors_Click(ByVal sender As System.Object, ByVal e As System.EventArgs)
        Dim item As MenuItem = DirectCast(sender, MenuItem)                               ' get menu item ref.
        Dim sourceMenu As Menu = DirectCast(sender, MenuItem).Parent                      ' which menu?  
        Dim node As ProcNode = getProcNode(DirectCast(sourceMenu, ContextMenu).SourceControl)  ' which node?

        item.Checked = Not item.Checked                                                   ' toggle item
        node.ancestorChecked = item.Checked                                               ' save setting
    End Sub


    '***********************************************************************************************
    ' ***** OTHER EVENTS ***************************************************************************
    '***********************************************************************************************

    ' inits the start and end nodes on load
    Private Sub frmDrawConc_Load(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles MyBase.Load
        If nodeList.Count = 0 Then InitTerminalNodes()
    End Sub

    ' redraws arcs after a resize
    Private Sub pbCanvas_Paint(ByVal sender As System.Object, ByVal e As System.Windows.Forms.PaintEventArgs) Handles pbCanvas.Paint
        RedrawArcs()
    End Sub

    ' allows or blocks form close based on value of cancelClose
    Private Sub frmDrawConc_Closing(ByVal sender As Object, ByVal e As System.ComponentModel.CancelEventArgs) Handles MyBase.Closing
        e.Cancel = CancelClose
        CancelClose = False                               ' reset for next time
    End Sub


    '***********************************************************************************************
    ' ***** NODE METHODS ***************************************************************************
    '***********************************************************************************************

    ' get the proc node that is 'contained' within the picturebox passed
    Private Function getProcNode(ByVal item As PictureBox) As ProcNode
        Dim tempStep As ProcNode

        For Each tempStep In nodeList                     ' check each node
            If tempStep.item Is item Then                 ' to see if it's item references the 
                Return tempStep                           ' pbox passed 
            End If
        Next
    End Function

    ' Draw a node icon on the canvas and create a new node in thelist of nodes for the process
    Private Function AddNode(ByVal left As Integer, ByVal top As Integer, ByVal toolType As tool) As ProcNode
        Dim pb As New PictureBox
        Dim pNode As New ProcNode

        ' draw a new node icon on the canvas
        With pb
            .Parent = pbCanvas
            .Size = New Size(32, 32)
            .Left = left
            .Top = top
            .SizeMode = PictureBoxSizeMode.StretchImage                ' make all same size
            .Tag = toolType                                            ' store the tooltype 
            .Image = imlTools.Images(toolType)
            .Anchor = AnchorStyles.None                                ' let it float when resizing
            .ContextMenu = getContextMenu(toolType)                    ' add the relevant right-click menu

            ' add event handlers for drawing arcs and moving icons
            AddHandler .MouseDown, AddressOf PB_MouseDown
            AddHandler .MouseUp, AddressOf PB_MouseUp
            AddHandler .MouseMove, AddressOf PB_MouseMove
        End With

        ' add new node to the list
        pNode.item = pb
        pNode.toolType = toolType
        nodeList.Add(pNode)

        Return pNode
    End Function

    ' sets the next and prev item fields of two nodes joined by an arc:   start.next <---> end.prev
    Private Sub updatenodeList()
        With currentArc
            .startNode.nextItem = .EndNode
            .EndNode.prevItem = .startNode
        End With
    End Sub

    ' deletes the node from the nodelist and the canvas
    Private Sub RemoveNode(ByVal node As ProcNode)

        ' remove links to other nodes (if any)
        If node.hasNextItem Then node.nextItem.prevItem = Nothing
        If node.hasPrevItem Then node.prevItem.nextItem = Nothing

        ' remove node from list & canvas
        nodeList.Remove(node)
        pbCanvas.Controls.Remove(node.item)
    End Sub

    ' setup the start and end nodes & add them to nodelist
    Private Sub InitTerminalNodes()
        Dim pbl As New ProcNode

        pbl.item = pbStart
        pbl.isStartNode = True
        pbl.toolType = tool.none
        nodeList.Add(pbl)

        pbl = New ProcNode
        pbl.item = pbStop
        pbl.isEndNode = True
        pbl.toolType = tool.none
        nodeList.Add(pbl)
    End Sub

    ' sorts nodes into sequence along arcs from startnode, and returns the filtered out loose nodes
    Private Function SortNodeList()
        Dim sortedList As New ArrayList
        Dim remaining As New ArrayList
        Dim nextNode As ProcNode = getProcNode(pbStart) ' get start node

        ' get sequence from start node
        While Not nextNode Is Nothing
            sortedList.Add(nextNode)
            nodeList.Remove(nextNode)
            nextNode = nextNode.nextItem
        End While

        remaining = nodeList.Clone                      ' copy leftover nodes to remaining
        nodeList = sortedList.Clone                     ' copy processed nodes to nodelist
        Return remaining
    End Function


    '***********************************************************************************************
    ' ***** DRAWING METHODS ************************************************************************
    '***********************************************************************************************

    ' uses the current arc structure to draw a temporary arc between two points
    Private Sub DrawInterimArc()
        Dim g As Graphics = pbCanvas.CreateGraphics

        RefreshLineAttachPoint(True, currentArc)             ' ie. the edge to draw the line from 

        With currentArc
            g.DrawLine(Pens.Black, .startPoint, .endPoint)
        End With

        g.Dispose()
    End Sub

    ' uses the current arc structure to draw a permanent arc between two points
    Private Sub DrawArc()
        Dim g As Graphics = pbCanvas.CreateGraphics

        RefreshLineAttachPoint(False, currentArc)           ' the edge to draw the line to

        With currentArc
            g.DrawLine(Pens.Black, .startPoint, .endPoint)
        End With

        g.Dispose()
    End Sub

    ' overwrites (effectively erases) a previously drawn arc
    Private Sub UnDrawInterimArc()
        Dim g As Graphics = pbCanvas.CreateGraphics
        Dim eraser As Pen = New Pen(pbCanvas.BackColor)          ' use pen same colour as canvas background

        With currentArc
            g.DrawLine(eraser, .startPoint, .endPoint)
        End With

        g.Dispose()
    End Sub

    ' converts the top-left co-ords of a picturebox to the equivalent co-ords of the canvas
    Private Function PictXYToCanvasXY(ByVal pict As PictureBox, ByVal x As Integer, ByVal y As Integer) As Point
        Dim px As Integer = x + pict.Left
        Dim py As Integer = y + pict.Top
        Return New Point(px, py)
    End Function

    ' returns the midway point on the picturebox edge passed to attach an arc to
    Private Function getLineAttachPoint(ByVal node As ProcNode, ByVal outEdge As edge) As Point
        Dim result As Point
        Dim item As PictureBox = node.item
        Dim midEdgeX As Integer = item.Left + (item.Width / 2)      ' x axis midpoint of pbox
        Dim midEdgeY As Integer = item.Top + (item.Height / 2)      ' y  "      "      "   "   

        Select Case outEdge
            Case edge.bottom
                result = New Point(midEdgeX, item.Top + item.Height + 1)
            Case edge.left
                result = New Point(item.Left - 1, midEdgeY)
            Case edge.right
                result = New Point(item.Left + item.Width + 1, midEdgeY)
            Case edge.top
                result = New Point(midEdgeX, item.Top - 1)
        End Select
        Return result
    End Function

    ' determines the most appropriate picturebox edge for an arc to attach to
    Private Sub RefreshLineAttachPoint(ByVal start As Boolean, ByRef line As sArc)
        ' todo: make decision by slope, not just edges
        Dim inEdge, outEdge As edge
        If start Then                                          ' starting point of arc
            With line
                If .endPoint.X > .startNode.item.Left + .startNode.item.Width Then
                    outEdge = edge.right
                ElseIf .endPoint.X < .startNode.item.Left Then
                    outEdge = edge.left
                Else
                    If .endPoint.Y > .startNode.item.Top + .startNode.item.Height Then
                        outEdge = edge.bottom
                    Else
                        outEdge = edge.top
                    End If
                End If
                .startPoint = getLineAttachPoint(.startNode, outEdge)  ' get edge midpoint
            End With
        Else                                                   ' endpoint of arc
            With line
                If .startPoint.X < .endPoint.X Then
                    inEdge = edge.left
                ElseIf .startPoint.X > .endPoint.X Then
                    inEdge = edge.right
                Else
                    If .startPoint.Y > .endPoint.Y Then
                        inEdge = edge.top
                    Else
                        inEdge = edge.bottom
                    End If
                End If
                .endPoint = getLineAttachPoint(.EndNode, inEdge)
            End With
        End If

    End Sub

    ' redraws all permanent arcs after a form resize
    Private Sub RedrawArcs()
        Dim line As sArc
        Dim g As Graphics = pbCanvas.CreateGraphics
        Dim pbl As ProcNode

        For Each pbl In nodeList                                            ' for each node in the list
            If pbl.hasNextItem Then
                line.startNode = pbl                                        ' set start & end nodes
                line.EndNode = pbl.nextItem
                line.startPoint = getLineAttachPoint(pbl, edge.right)
                RefreshLineAttachPoint(True, line)                          ' get start point
                line.endPoint = getLineAttachPoint(pbl.nextItem, edge.left)
                RefreshLineAttachPoint(False, line)                         ' get end point    
                g.DrawLine(Pens.Black, line.startPoint, line.endPoint)      ' draw the arc
            End If
        Next

        g.Dispose()
    End Sub

    ' realigns node icons and arcs both vertically and horizontally
    Private Sub ReAlignNodes()
        Dim alignedTop As Integer = pbStart.Top                                      ' align vert. with start node
        Dim eachGap As Integer = (pbStop.Left - pbStart.Left) \ (nodeList.Count - 1) ' equidistant horiz.
        Dim runningLeft = pbStart.Left + eachGap                                     ' accum. for icon posn.
        Dim leftOvers As New ArrayList                                               ' unattached nodes
        Dim sortedList As New ArrayList
        Dim temp As ProcNode

        ' align tops 
        For Each node As ProcNode In nodeList
            node.item.Top = alignedTop
        Next

        ' align horizontal gaps: 
        ' sort node list firstly on joined nodes; leftovers don't have arcs
        leftOvers = SortNodeList()

        ' sort leftovers on where they are on canvas
        sortedList = leftOvers.Clone()
        For i As Integer = 0 To sortedList.Count - 1
            sortedList(i) = getNextLeftNode(leftOvers)
        Next

        ' align arc'ed nodes
        For Each temp In nodeList
            If Not temp.isTerminalNode Then
                temp.item.Left = runningLeft
                runningLeft += eachGap
            End If
        Next

        'align loose nodes
        For Each temp In sortedList
            If Not temp.isTerminalNode Then
                temp.item.Left = runningLeft
                If temp.isLooseNode Then temp.item.Top += 50 ' place loose nodes below arc'ed ones
                runningLeft += eachGap
            End If
        Next

        ' re-join the two lists together
        nodeList.AddRange(sortedList)

        pbCanvas.Refresh()                        ' redraw 
    End Sub

    ' returns the left-most node in the list (and removes it from the list)
    Private Function getNextLeftNode(ByVal list As ArrayList) As ProcNode
        Dim temp, result As ProcNode
        Dim lowLeft As Integer = 10000

        For Each temp In list
            If temp.item.Left < lowLeft Then           ' compare lefts of each node
                result = temp
                lowLeft = temp.item.Left
            End If
        Next
        list.Remove(result)                            ' remove it from list
        Return result
    End Function


    '***********************************************************************************************
    ' ***** GRAPH VALIDATION METHODS ***************************************************************
    '***********************************************************************************************

    ' controls graph validation
    Private Function ValidGraph() As Boolean
        Dim msg As String
        'validation:
        '  - all nodes are linked - map from start to end
        If Not isCompleteGraph() Then
            ShowError("Incomplete Graph: The End node is not reachable from the Start node. " & _
                      "Please join all nodes before saving.")
            Return False
        End If

        '  - no loose nodes
        If Not hasNoLooseNodes() Then
            ShowError("Extra Nodes: A node or nodes are not attached to the graph. " & _
                      "Please remove those nodes before saving.")
            Return False
        End If

        '  - any compensations have a worklet defined
        If Not hasCompleteCompensations() Then
            ShowError("Missing Worklet: One or more compensation nodes don't have a worklet defined. Please " & _
                      "right-click on those nodes to define compensatory worklets for each before saving.")
            Return False
        End If
        Return True
    End Function

    ' true if there is a path from the startnode to theendnode
    Private Function isCompleteGraph() As Boolean
        Dim node As ProcNode = getProcNode(pbStart)
        While node.hasNextItem
            node = node.nextItem
        End While
        Return node.isEndNode                           ' run out of next nodes, see if its at the end
    End Function

    ' true if no losse nodes (i.e. a node with no incoming or outgoing arc)
    Private Function hasNoLooseNodes() As Boolean
        For Each node As ProcNode In nodeList           ' check each node
            If node.isLooseNode Then
                Return False
            End If
        Next
        Return True
    End Function

    ' true if all compensation nodes have a worklet defined
    Private Function hasCompleteCompensations() As Boolean
        For Each node As ProcNode In nodeList
            If Not node.hasWorklet Then                   ' non-compensation nodes default to true
                Return False
            End If
        Next
        Return True
    End Function


    '***********************************************************************************************
    ' ***** MISC.METHODS ***************************************************************************
    '***********************************************************************************************

    ' converts the graphically drawn process into an array of ConclusionItems ready to write to XML
    Private Function ComposeGraphXML() As ConclusionItem()
        Dim result() As ConclusionItem
        Dim tempConc As ConclusionItem
        Dim tagIndex As Integer = 1
        Dim node As ProcNode = getProcNode(pbStart).nextItem   ' first item after start node

        ' each node is one 'conclusion' element in the array (only the end node has no next item)
        While node.hasNextItem
            tempConc = New ConclusionItem("_" + tagIndex.ToString)
            tempConc.Target = "workitem"                            'default target

            ' set action/target pair based on the node's tool
            Select Case node.toolType
                Case tool.compensate
                    tempConc.Action = "compensate"
                    tempConc.Target = node.worklet
                Case tool.continue
                    tempConc.Action = "continue"
                Case tool.continueAll
                    tempConc.Action = "continue"
                    tempConc.Target = "allcases"
                Case tool.continueCase
                    tempConc.Action = "continue"
                    tempConc.Target = "case"
                Case tool.forceComplete
                    tempConc.Action = "complete"
                Case tool.forceFail
                    tempConc.Action = "fail"
                Case tool.remove
                    tempConc.Action = "remove"
                Case tool.removeAll
                    tempConc.Action = "remove"
                    tempConc.Target = "allcases"
                Case tool.removeCase
                    tempConc.Action = "remove"
                    tempConc.Target = "case"
                Case tool.restart
                    tempConc.Action = "restart"
                Case tool.suspend
                    tempConc.Action = "suspend"
                Case tool.suspendAll
                    tempConc.Action = "suspend"
                    tempConc.Target = "allcases"
                Case tool.suspendCase
                    tempConc.Action = "suspend"
                    tempConc.Target = "case"
            End Select

            ' adjust for ancestor cases
            If node.ancestorChecked Then
                tempConc.Target = "ancestorCases"
            End If

            'resize array
            If result Is Nothing Then
                ReDim result(0)
            Else
                ReDim Preserve result(result.Length)
            End If

            ' assign conclusion element to array
            result(result.Length - 1) = tempConc

            node = node.nextItem
            tagIndex += 1
        End While             ' next item 

        Return result
    End Function

    ' draws a previously defined graph on the canvas (i.e. translates a conclusion into a graph)
    Public Sub ShowGraph(ByVal conclusion As ConclusionItem())
        Dim toolType As tool
        Dim factor As Integer = 1
        Dim idx As Integer
        Dim pNode, prevNode, nextNode As ProcNode

        nodeList.Clear()
        InitTerminalNodes()    ' add start and end nodes

        ' convert conclusion items into procNodes and re-create nodeList 
        For Each conc As ConclusionItem In conclusion
            toolType = ConclusionItemToToolType(conc)
            pNode = AddNode(factor * 50, 80, toolType)            ' space nodes 50 pixels apart initially

            ' adjust certain node settings as required
            Select Case toolType
                Case tool.compensate
                    pNode.worklet = conc.Target
                Case tool.continueAll, tool.removeAll, tool.suspendAll
                    pNode.ancestorChecked = (conc.Target = "ancestorCases")
                    pNode.item.ContextMenu.MenuItems(1).Checked = pNode.ancestorChecked
            End Select

            factor += 1
        Next

        ' move end node to end of list
        pNode = nodeList(1)
        nodeList.RemoveAt(1)
        nodeList.Add(pNode)

        'link nodes
        For idx = 0 To nodeList.Count - 2
            nodeList(idx).nextItem = nodeList(idx + 1)
            nodeList(idx + 1).prevItem = nodeList(idx)
        Next

        RedrawArcs()             ' add arcs
        ReAlignNodes()           'align icons

    End Sub

    ' translates a conclusionitem into a tool icon type
    Private Function ConclusionItemToToolType(ByVal conc As ConclusionItem) As tool
        Dim result As tool
        Select Case conc.Action
            Case "compensate"
                result = tool.compensate
            Case "continue"
                Select Case conc.Target
                    Case "workitem"
                        result = tool.continue
                    Case "case"
                        result = tool.continueCase
                    Case "allcases", "ancestorCases"
                        result = tool.continueAll
                End Select
            Case "complete"
                result = tool.forceComplete
            Case "fail"
                result = tool.forceFail
            Case "remove"
                Select Case conc.Target
                    Case "workitem"
                        result = tool.remove
                    Case "case"
                        result = tool.removeCase
                    Case "allcases", "ancestorCases"
                        result = tool.removeAll
                End Select
            Case "restart"
                result = tool.restart
            Case "suspend"
                Select Case conc.Target
                    Case "workitem"
                        result = tool.suspend
                    Case "case"
                        result = tool.suspendCase
                    Case "allcases", "ancestorCases"
                        result = tool.suspendAll
                End Select
        End Select
        Return result
    End Function

End Class
