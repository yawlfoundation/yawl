' This file is made available under the terms of the LGPL licence.
' This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
' The source remains the property of the YAWL Foundation.  The YAWL Foundation 
' is a collaboration of individuals and organisations who are committed to  
' improving workflow technology.

<Serializable()> _
Public Class TreeSet

    ' This class manages all the trees for one treeType (i.e. a particular set of rules for an exception type)

    ' The Worklet Service Rules Editor
    ' Author: Michael Adams
    '         BPM Group, QUT Australia
    ' Version: 0.8, 25/08/2006

    Private _treeType As RuleSetMgr.exType           ' the exception type for this set of rules
    Private _outerTag As String                      ' xml tag of outer 'parent' element
    Private _subTag As String                        ' xml tag of child - may be "case", "item" or null 
    Private _innerTag As String                      ' xml tag of grandchild - may be "pre" or "post" or null
    Private _trees() As RuleTree                     ' the set of trees for this ruleset type


    ' ******* PROPERTIES ************************

    Public Property TreeType() As RuleSetMgr.exType
        Get
            Return _treeType
        End Get
        Set(ByVal Value As RuleSetMgr.exType)
            _treeType = Value
        End Set
    End Property

    Public Property OuterTag() As String
        Get
            Return _outerTag
        End Get
        Set(ByVal Value As String)
            _outerTag = Value
        End Set
    End Property

    Public Property SubTag() As String
        Get
            Return _subTag
        End Get
        Set(ByVal Value As String)
            _subTag = Value
        End Set
    End Property

    Public Property InnerTag() As String
        Get
            Return _innerTag
        End Get
        Set(ByVal Value As String)
            _innerTag = Value
        End Set
    End Property

    Public Property Trees() As RuleTree()
        Get
            Return _trees
        End Get
        Set(ByVal Value As RuleTree())
            _trees = Value
        End Set
    End Property


    ' ******** METHODS **************

    ' adds the specified ruletree to the set of trees for this rule type
    Public Sub addTree(ByVal newtree As RuleTree)
        Dim size As Integer
        If Not _trees Is Nothing Then size = _trees.GetLength(0) ' length is one more than upperbound
        ReDim Preserve _trees(size)                              ' so grow list by one
        _trees(size) = newtree                                   ' and add item to it
    End Sub

    ' returns the rule tree for the specified task name
    Public Function getTreeForTask(ByVal task As String) As RuleTree
        Dim result As RuleTree = Nothing

        For Each tree As RuleTree In _trees
            If tree.Name = task Then
                result = tree
                Exit For
            End If
        Next
        Return result                                          ' returns nothing if no tree for that task exists
    End Function

    ' returns true if there is at least one tree in this treeset (i.e. this ruletype has some rules)
    Public Function hasTree() As Boolean
        If _trees Is Nothing Then Return False
        Return _trees.Length > 0
    End Function

    ' returns true if the ruletype of this treeset has a child or sub xml element
    Public Function hasSubTag() As Boolean
        Return Not ((_subTag Is Nothing) OrElse (_subTag.Length = 0))
    End Function

    ' returns true if the ruletype of this treeset has a grand-child xml element
    Public Function hasInnerTag() As Boolean
        Return Not ((_innerTag Is Nothing) OrElse (_innerTag.Length = 0))
    End Function

    ' replaces the tree for a certain task with the newTree passed
    Public Sub updateTreeByTaskName(ByVal newTree As RuleTree)
        For i As Integer = 0 To _trees.GetUpperBound(0)
            If _trees(i).Name = newTree.Name Then                 ' if the stored tree has the same name
                _trees(i) = newTree                               ' replace it
                Exit For
            End If
        Next
    End Sub

End Class
