' This file is made available under the terms of the LGPL licence.
' This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
' The source remains the property of the YAWL Foundation.  The YAWL Foundation 
' is a collaboration of individuals and organisations who are commited to  
' improving workflow technology.

Imports System.Convert

Friend Class TreeViewMgr

    ' This class manages updates to the treeview controls on the various forms

    ' The Worklet Service Rules Editor
    ' Author: Michael Adams
    '         BPM Group, QUT Australia
    ' Version: 0.8, 25/08/2006


    ' ******* METHODS **********

    ' loads the rules into the treeview passed
    Public Sub LoadTreeIntoView(ByVal view As TreeView, ByVal tree As RuleTree, ByVal newTree As Boolean)
        Dim rootNode As TreeNode

        ' draw the root node
        rootNode = view.Nodes.Add("Rule 0")
        rootNode.ImageIndex = 7                                       ' the root has a blue icon
        rootNode.SelectedImageIndex = 3

        ' if we're building a new tree (via the 'new' form)
        If newTree Then
            ShowAddPoint(view.Nodes(0), tree)                         ' show where nodes can be added 
        Else
            AddChildTree(rootNode, tree, 0, True)                     ' else show pre-existing tree
        End If

        view.ExpandAll()                                              ' show all nodes
        view.SelectedNode = view.Nodes(0)                             ' fire selection
    End Sub

    ' The base procedure for the recursive method below using the top nodes of the treeview
    Public Sub ShowAddPoints(ByVal view As TreeView, ByVal tree As RuleTree)

        For Each node As TreeNode In view.Nodes
            ShowAddPoint(node, tree)                                   ' gets called recursively
        Next
        view.ExpandAll()
    End Sub

    ' Show potential places on the tree where a node can be added
    Public Sub ShowAddPoint(ByVal terminalNode As TreeNode, ByVal tree As RuleTree)
        Dim newTreeNode As TreeNode

        ' get rdrnode dataset for this treenode (node's text is "Rule XX" where XX is the node id)
        Dim RdrNode As RuleNode = tree.getNode(terminalNode.Text.Substring(5))

        If RdrNode Is Nothing Then Exit Sub ' no more nodes to process 

        If RdrNode.TrueChild = -1 Then                                 ' no true child - potential add point
            newTreeNode = terminalNode.Nodes.Add("New True Rule")
            newTreeNode.ImageIndex = 6                                 ' addpoint nodes are yellow 
            newTreeNode.Tag = RdrNode.id                               ' keep parent's id in 'tag' property
            newTreeNode.SelectedImageIndex = 2
        End If

        If (RdrNode.Parent <> -1) AndAlso (RdrNode.FalseChild = -1) Then   ' no false child, and not root node
            newTreeNode = terminalNode.Parent.Nodes.Add("New False Rule")
            newTreeNode.ImageIndex = 6
            newTreeNode.Tag = RdrNode.id
            newTreeNode.SelectedImageIndex = 2
        End If

        ' recurse
        For Each node As TreeNode In terminalNode.Nodes
            ShowAddPoint(node, tree)
        Next

    End Sub


    ' recursively add a (child) node to the true (exception) branch
    ' Notes: - a true branch child rule node is represented in the TreeView
    '          by adding a new NodeCollection to parentTreeNode.Nodes 
    '          and then adding a new TreeNode to it.
    '        - a false branch child rule node is represented in the TreeView
    '          by adding a new TreeNode to parentTreeNode.Nodes. 
    Public Sub AddChildTree(ByVal parentTreeNode As TreeNode, ByVal tree As RuleTree, _
                             ByVal parentNodeID As Integer, _
                             ByVal addAsTrueBranch As Boolean)

        ' the treenode to add that will contain the true child branch rdr node
        Dim newTreeNode As TreeNode
        Dim idxChildRule, iconIndex As Integer

        ' from the parent rdr node get the node id of the branch's child
        If addAsTrueBranch Then
            idxChildRule = Convert.ToInt32(tree.Nodes(parentNodeID).TrueChild)
            iconIndex = 1
        Else
            idxChildRule = Convert.ToInt32(tree.Nodes(parentNodeID).FalseChild)
            iconIndex = 0
        End If

        ' a node id of -1 means no child on that branch
        If idxChildRule > -1 Then

            ' add a new TreeNode for the rdr node as a child of the parentTreeNode
            newTreeNode = parentTreeNode.Nodes.Add("Rule " + idxChildRule.ToString)
            newTreeNode.ImageIndex = iconIndex + 4
            newTreeNode.SelectedImageIndex = iconIndex   ' don't change icon when sel.

            ' recurse for children of the new child node
            If NodeHasTrueChild(tree, idxChildRule) Then
                AddChildTree(newTreeNode, tree, idxChildRule, True)
            End If
            If NodeHasFalseChild(tree, idxChildRule) Then
                AddChildTree(parentTreeNode, tree, idxChildRule, False)
            End If
        End If
    End Sub

    'return true if the node id has a true child node
    Public Function NodeHasTrueChild(ByVal tree As RuleTree, ByVal nodeId As Integer) As Boolean
        Return (Convert.ToInt32(tree.Nodes(nodeId).TrueChild) > -1)
    End Function

    'return true if the node id has a false child node
    Public Function NodeHasFalseChild(ByVal tree As RuleTree, ByVal nodeId As Integer) As Boolean

        Return (Convert.ToInt32(tree.Nodes(nodeId).FalseChild) > -1)
    End Function

    ' returns the node id of the selected node (where the node's name is "Rule XX" and XX is the node id)
    Public Function getSelectedNode(ByVal view As TreeView) As Integer
        Dim selText As String = view.SelectedNode.Text
        Try
            Return Convert.ToInt32(selText.Substring(5))
        Catch ex As Exception
            Return -1
        End Try
    End Function

    ' The base procedure for the recursive method below using the top nodes of the treeview
    Public Function SelectNodeWithID(ByVal view As TreeView, ByVal id As String) As TreeNode
        Return FindNode(view.Nodes, "Rule " & id)
    End Function

    ' returns the treenode in the view with the specified rule id
    Private Function FindNode(ByVal nodes As TreeNodeCollection, ByVal id As String) As TreeNode
        Dim result As TreeNode

        For Each n As TreeNode In nodes
            If n.Text = id Then                                    ' either it's the top level Nodes
                result = n
            Else
                result = FindNode(n.Nodes, id)                     ' or it's in this node's Nodes
            End If
            If Not result Is Nothing Then Exit For ' we have a winner so we're done
        Next
        Return result
    End Function

    ' builds and returns the effective composite rule for the selected node
    Public Function getEffectiveCondition(ByVal view As TreeView, ByVal tree As RuleTree) As String
        Dim nodeID As Integer = getSelectedNode(view)
        Dim list As New ArrayList
        Dim node, prevNode As RuleNode
        Dim result As String = ""
        Dim tabs As Integer = 1
        Dim prevID As Integer
        Dim compact As ConclusionItem.TextFormat = ConclusionItem.TextFormat.compact

        ' create list of nodes on direct path from selected node back to root node
        While nodeID > 0                                             ' while not root node
            node = tree.getNode(nodeID)
            list.Add(node)                                           ' add node to list
            nodeID = ToInt32(node.Parent)                            ' get it's parent
        End While

        If list.Count > 0 Then                                       ' if there are some nodes in list
            list.Reverse()                                           ' reverse: now it's root -> selected
            For Each node In list
                If node.id = "1" Then                                ' if its the top rule node
                    result = "if "                                   ' it starts the comp. rule 
                ElseIf node.id = prevNode.TrueChild Then             ' or if its on a true branch 
                    result &= Indent(tabs) & "except if "
                    tabs += 1                                        ' inc. indents
                Else
                    result &= Indent(tabs - 1) & "else if "          ' or its on a false branch
                End If

                ' write a line describing this node's part of the composite rule
                result &= node.Condition & " then " & ConclusionTextify(node.Conclusion, compact) & vbCrLf
                prevNode = DeepClone(node)                           ' get a copy of the previous node
            Next
        End If

        Return result
    End Function

    ' returns a string of 3 spaces time the count passed
    Private Function Indent(ByVal count As Integer) As String
        Dim result As String = ""
        For i As Integer = 1 To count
            result &= "   "
        Next
        Return result
    End Function

End Class
