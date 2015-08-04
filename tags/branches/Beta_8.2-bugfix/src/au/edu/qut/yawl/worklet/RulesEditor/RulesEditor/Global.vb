' This file is made available under the terms of the LGPL licence.
' This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
' The source remains the property of the YAWL Foundation.  The YAWL Foundation 
' is a collaboration of individuals and organisations who are commited to  
' improving workflow technology.

Imports System.Xml
Imports System.IO
Imports System.Runtime.Serialization.Formatters.Binary

Module Global

    ' This module contains some global variables and methods
    '
    ' The Worklet Service Rules Editor
    ' Author: Michael Adams
    '         BPM Group, QUT Australia
    ' Version: 0.8, 25/08/2006


    Public Structure sConfig                     ' contains the paths to various resources used or needed
        Public repository As String
        Public YAWLEditor As String
        Public ServiceURL As String
        Public SpecPaths As String
    End Structure

    Public rsMgr As New RuleSetMgr                      ' gloval rules file manager
    Public tvMgr As New TreeViewMgr                     ' manages the treeviews for various screens
    Public pathOf As sConfig                            ' resource paths
    Public lastPathInSpecLocator As String              ' remembers last path browsed

    ' returns a readable formatted version of a rule nodes conlusion
    Public Function ConclusionTextify(ByVal conclusion() As ConclusionItem, _
                                      ByVal format As ConclusionItem.TextFormat) As String
        Dim result As String = ""
        Dim concTemp As ConclusionItem

        If conclusion Is Nothing Then Return "null" ' special case for root node  

        ' textify each element of the conclusion
        For Each concTemp In conclusion
            result &= concTemp.toText(format)
        Next

        'if compact format, remove any delimiters from the end
        If format = ConclusionItem.TextFormat.compact Then
            result = result.Remove(result.LastIndexOf("; "), 2)
        End If

        Return result
    End Function

    ' grows by one an array of compositeitems and adds it to the array
    Public Function AddCompositeItem(ByVal itemSet As CompositeItem(), ByVal item As CompositeItem) As CompositeItem()
        Dim size As Integer                                        ' defaults to zero
        If Not itemSet Is Nothing Then size = itemSet.GetLength(0) ' length is one more than upperbound
        ReDim Preserve itemSet(size)                               ' so grow list by one
        itemSet(size) = item                                       ' and add item to it
        Return itemSet
    End Function

    ' grows by one an array of conclusionitems and adds it to the array
    Public Function AddConclusion(ByVal itemSet As ConclusionItem(), ByVal item As ConclusionItem) As ConclusionItem()
        Dim size As Integer                                        ' defaults to zero
        If Not itemSet Is Nothing Then size = itemSet.GetLength(0) ' length is one more than upperbound
        ReDim Preserve itemSet(size)                               ' so grow list by one
        itemSet(size) = item                                       ' and add item to it
        Return itemSet
    End Function

    ' converts the contents of a listbox of cornerstone data to a composite item array
    Public Function buildCornerStone(ByVal lbx As ListBox) As CompositeItem()
        Dim result As CompositeItem()
        Dim element As CompositeItem
        Dim pair() As String

        For Each item As String In lbx.Items
            pair = item.Split("="c)
            element = New CompositeItem(pair(0).Trim, pair(1).Trim)
            result = AddCompositeItem(result, element)
        Next

        Return result
    End Function

    ' displays a simple error message box
    Public Sub ShowError(ByVal msg As String)
        MessageBox.Show(msg, "Error", MessageBoxButtons.OK, MessageBoxIcon.Error)
    End Sub

    ' returns a deep cloned copy of the object passed
    ' source: http://www.code-magazine.com/Article.aspx?quickid=0601121
    Public Function DeepClone(ByVal obj As Object) As Object
        Dim ms As New MemoryStream
        Dim objResult As Object = Nothing

        Try
            ' serialise to memory the object passed
            Dim bf As New BinaryFormatter
            bf.Serialize(ms, obj)

            ' Rewind & Deserialise the data
            ms.Position = 0
            objResult = bf.Deserialize(ms)
        Finally
            ms.Close()
        End Try
        Return objResult   'returns nothing if exception thrown
    End Function

    ' returns true if tag string passed contians no invalid characters
    Public Function XMLTagValidator(ByVal tag As String) As Boolean
        Dim fragment As String = "<" & tag & ">some text</" & tag & ">"       ' create xml fragment with the tag
        Dim xtr As New XmlTextReader(fragment, XmlNodeType.Element, Nothing)  ' pass it to a reader
        Try
            xtr.Read()                                                        ' try to read the fragment
            Return True                                                       ' no exception = good tag
        Catch
            Return False                                                      ' exception = bad tag 
        End Try
    End Function

    ' converts an ArrayList to a 1-dim String array
    Public Function ListToStringArray(ByVal list As ArrayList) As String()
        Dim obj() As Array = list.ToArray
        Dim result(list.Count - 1) As String

        For i As Integer = 0 To obj.GetUpperBound(0)
            result(i) = obj(i).ToString
        Next

        Return result
    End Function

    ' checks for a valid repository path
    Public Function GoodRepositoryPath(ByVal path As String) As Boolean
        Dim errStr As String
        If Not (Directory.Exists(path & "\rules") OrElse _
                Directory.Exists(path & "\selected") OrElse _
                Directory.Exists(path & "\worklets")) Then
            MessageBox.Show("It appears the repository path specified is incorrect," & _
                            " or doesn't contain the required folders 'rules'," & _
                            " 'worklets' and/or 'selected'. Please correct the" & _
                            " path to the repository on the Configuration Form.", _
                            "Invalid Repository Path", MessageBoxButtons.OK, MessageBoxIcon.Error)
            Return False
        Else
            Return True
        End If
    End Function

    ' checks for a valid YAWL Editor path
    Public Function GoodYAWLEditorPath(ByVal path As String) As Boolean
        Dim errStr As String
        If Not File.Exists(path) Then
            MessageBox.Show("It appears the YAWLEditor path specified is incorrect," & _
                            " or doesn't contain the editor jar file." & _
                            " Please correct the path to the YAWL Editor on the Configuration Form.", _
                            "Invalid YAWLEditor Path", _
                             MessageBoxButtons.OK, MessageBoxIcon.Error)
            Return False
        Else
            Return True
        End If
    End Function

    ' checks if the supplied uri is well-formed
    Public Function ValidServiceURI(ByVal uriStr As String) As Boolean
        Dim uri As Uri
        Try
            uri = New Uri(uriStr)
            Return True
        Catch ex As Exception
            MessageBox.Show("It appears the Worklet Service URI specified is incorrect," & _
                " or is not a valid URI." & _
                " Please correct the URI specified.", _
                "Invalid Worklet Service URI", _
                 MessageBoxButtons.OK, MessageBoxIcon.Error)
            Return False
        End Try
    End Function

    'returns a string representation of the contents of the hashtable passed
    Public Function getWorkletCaseIdentifiers(ByVal list As Hashtable) As String
        Dim itr As IDictionaryEnumerator = list.GetEnumerator()
        Dim result As String

        While itr.MoveNext()
            result &= vbTab & "* " & itr.Key & ": " & itr.Value & vbCrLf
        End While
        Return result
    End Function

    '********************************************************************************************************

    ' the startup object
    Public Sub main()
        Dim startForm As New frmEdit
        startForm.ShowDialog()
    End Sub

End Module
