' This file is made available under the terms of the LGPL licence.
' This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
' The source remains the property of the YAWL Foundation.  The YAWL Foundation
' is a collaboration of individuals and organisations who are commited to  
' improving workflow technology.

Option Strict On

<Serializable()> _
Public Class CompositeItem

    ' This simple class stores tags and text values for composite node elements
    '
    ' The Worklet Service Rules Editor
    ' Author: Michael Adams
    '         BPM Group, QUT Australia
    ' Version: 0.8, 25/08/2006

    Private _tag As String
    Private _text As String

    '*********************************************************************************'
    ' PROPERTIES '
    '************'

    Public Property Tag() As String
        Get
            Return _tag
        End Get
        Set(ByVal Value As String)
            _tag = Value
        End Set
    End Property

    Public Property Text() As String
        Get
            Return _text
        End Get
        Set(ByVal Value As String)
            _text = Value
        End Set
    End Property

    '*********************************************************************************'

    ' constructor
    Public Sub New(ByVal newTag As String, ByVal newText As String)
        If newTag Is Nothing Then _tag = "selection" ' default for version one rules
        _tag = newTag
        _text = newText
    End Sub

End Class
