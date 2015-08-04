' This file is made available under the terms of the LGPL licence.
' This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
' The source remains the property of the YAWL Foundation.  The YAWL Foundation 
' is a collaboration of individuals and organisations who are committed to  
' improving workflow technology.

<Serializable()> _
Public Class RuleNode

    ' This class represents one rule node in an RDR tree.

    ' The Worklet Service Rules Editor
    ' Author: Michael Adams
    '         BPM Group, QUT Australia
    ' Version: 0.8, 25/08/2006

    ' the various fields of a rule node
    Private _id As String
    Private _parent As String
    Private _trueChild As String
    Private _falseChild As String
    Private _condition As String
    Private _conclusion() As ConclusionItem                   ' list of Conclusion primitives
    Private _description As String
    Private _cornerstone() As CompositeItem                   ' list of composite items

    '******************
    '*** PROPERTIES ***
    '******************

    Public Property id() As String
        Get
            Return _id
        End Get
        Set(ByVal Value As String)
            _id = Value
        End Set
    End Property

    Public Property Parent() As String
        Get
            Return _parent
        End Get
        Set(ByVal Value As String)
            _parent = Value
        End Set
    End Property

    Public Property TrueChild() As String
        Get
            Return _trueChild
        End Get
        Set(ByVal Value As String)
            _trueChild = Value
        End Set
    End Property

    Public Property FalseChild() As String
        Get
            Return _falseChild
        End Get
        Set(ByVal Value As String)
            _falseChild = Value
        End Set
    End Property

    Public Property Condition() As String
        Get
            Return _condition
        End Get
        Set(ByVal Value As String)
            _condition = Value
        End Set
    End Property

    Public Property Conclusion() As ConclusionItem()
        Get
            Return _conclusion
        End Get
        Set(ByVal Value As ConclusionItem())
            _conclusion = Value
        End Set
    End Property

    Public Property Description() As String
        Get
            Return _description
        End Get
        Set(ByVal Value As String)
            _description = Value
        End Set
    End Property

    Public Property Cornerstone() As CompositeItem()
        Get
            Return _cornerstone
        End Get
        Set(ByVal Value As CompositeItem())
            _cornerstone = Value
        End Set
    End Property

    '******************

    ' the constructor
    Public Sub New()
        _id = ""
        _parent = ""
        _trueChild = ""
        _falseChild = ""
        _condition = ""
        _description = ""
    End Sub

    ' sets the root node to default values
    Public Sub MakeRootNode()
        _id = "0"
        _parent = "-1"
        _trueChild = "-1"
        _falseChild = "-1"
        _condition = "True"
        _description = "default root node"
    End Sub

End Class
