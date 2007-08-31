' This file is made available under the terms of the LGPL licence.
' This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
' The source remains the property of the YAWL Foundation.  The YAWL Foundation 
' is a collaboration of individuals and organisations who are commited to  
' improving workflow technology.

<Serializable()> _
Public Class RuleTree

    ' This class represents the binary tree of one set of RuleNodes.

    ' The Worklet Service Rules Editor
    ' Author: Michael Adams
    '         BPM Group, QUT Australia
    ' Version: 0.8, 25/08/2006

    Private _name As String                                                     ' task or type name
    Private _nodes() As RuleNode                                                ' set of nodes in this tree    

    ' ******* PROPERTIES *****************************************

    Public Property Name() As String
        Get
            Return _name
        End Get
        Set(ByVal Value As String)
            _name = Value
        End Set
    End Property

    Public Property Nodes() As RuleNode()
        Get
            Return _nodes
        End Get
        Set(ByVal Value As RuleNode())
            _nodes = Value
        End Set
    End Property


    ' ******* METHODS ****************************************

    ' adds a rule node to the set of nodes for this tree
    Public Sub addNode(ByVal newNode As RuleNode)
        Dim size As Integer
        If Not _nodes Is Nothing Then size = _nodes.GetLength(0) ' length is one more than upperbound
        ReDim Preserve _nodes(size)                              ' so grow list by one
        _nodes(size) = newNode                                   ' and add item to it
    End Sub

    ' returns the node for the id passed (that's the id property of the node, not the index of the node list)
    Public Function getNode(ByVal idx As String) As RuleNode
        For Each n As RuleNode In _nodes
            If n.id = idx Then Return n
        Next
        Return Nothing                                           ' not found
    End Function

    ' replace a node with the 'newNode' passed
    Public Sub updateNode(ByVal newNode As RuleNode)
        For i As Integer = 0 To _nodes.GetUpperBound(0)
            If _nodes(i).id = newNode.id Then
                _nodes(i) = newNode
                Exit For
            End If
        Next
    End Sub

    ' returns the set of terminal nodes for this tree (i.e. the nodes that have no child nodes)
    Public Function getTerminalNodes() As RuleNode()
        Dim result(0) As RuleNode                                ' always at least one term node
        Dim size As Integer

        For Each oneNode As RuleNode In _nodes
            If (oneNode.TrueChild <> -1) AndAlso (oneNode.FalseChild <> -1) Then       ' no kids
                ReDim Preserve result(size)                                            ' grow result by 1 
                result(size) = oneNode                                                 ' add node to it 
                size += 1                                                              ' inc size for next loop
            End If
        Next
        Return result
    End Function

    ' returns the next available node id for this tree
    Public Function getNextNodeID() As String
        Return Nodes.Length.ToString
    End Function

End Class
