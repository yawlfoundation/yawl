' This file is made available under the terms of the LGPL licence.
' This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
' The source remains the property of the YAWL Foundation.  The YAWL Foundation
' is a collaboration of individuals and organisations who are committed to  
' improving workflow technology.

Option Strict On

<Serializable()> _
Public Class ConclusionItem

    ' This class stores one element (or 'primitive') of a rulenode's conclusion set.
    '
    ' The Worklet Service Rules Editor
    ' Author: Michael Adams
    '         BPM Group, QUT Australia
    ' Version: 0.8, 25/08/2006


    Public Enum TextFormat                 ' formats for displaying conclusions as text
        pretty
        compact
    End Enum

    Private _tag As String                                ' sequence tag of primitive 
    Private _action As String                             ' suspend, fail, restart etc.
    Private _target As String                             ' item, case, all cases etc.

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

    Public Property Action() As String
        Get
            Return _action
        End Get
        Set(ByVal Value As String)
            _action = Value
        End Set
    End Property

    Public Property Target() As String
        Get
            Return _target
        End Get
        Set(ByVal Value As String)
            _target = Value
        End Set
    End Property

    '*********************************************************************************'

    ' constructors
    Public Sub New(ByVal newTag As String, ByVal newAction As String, ByVal newTarget As String)
        _tag = newTag
        _action = newAction
        _target = newTarget
    End Sub

    Public Sub New(ByVal newTag As String)
        _tag = newTag
    End Sub

    '*********************************************************************************'

    ' adds a conclusion primitive to the set
    ' for a conclusion, a composite item has two elements: 'action' and 'target' (both with values)
    Public Sub addItem(ByVal item() As CompositeItem)
        For Each subItem As CompositeItem In item
            If subItem.Tag = "action" Then
                _action = subItem.Text
            Else
                _target = subItem.Text
            End If
        Next
    End Sub

    Public Function toText(ByVal format As TextFormat) As String
        Dim result As String = ""

        ' add tag number to start of pretty output 
        If format = TextFormat.pretty Then result &= _tag.Substring(1, 1) & ". "

        If _action = "compensate" Then
            result &= "run worklet "                    ' reword compensate
        Else
            result &= _action & " "
        End If
        result &= _target

        If format = TextFormat.pretty Then
            result &= vbCrLf                            ' pretty: each on a newline
        Else
            result &= "; "                              ' compact: one line, each sep by ';' 
        End If

        Return result
    End Function

End Class
