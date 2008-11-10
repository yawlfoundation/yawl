' This file is made available under the terms of the LGPL licence.
' This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
' The source remains the property of the YAWL Foundation.  The YAWL Foundation 
' is a collaboration of individuals and organisations who are committed to  
' improving workflow technology.

Imports System.Xml
Imports System.IO

<Serializable()> _
Public Class RuleSetMgr

    ' This class manages the loading, saving and modification of rulesets and their files.
    '
    ' The Worklet Service Rules Editor
    ' Author: Michael Adams
    '         BPM Group, QUT Australia
    ' Version: 0.8, 25/08/2006

    ' these are the types of exceptions - thus each has a set of rules (potentially)
    Public Enum exType
        CasePreConstraint = 0                         ' one tree
        CasePostConstraint = 1                        ' one tree
        ItemPreConstraint = 2                         ' many trees - one per (specified) task
        ItemPostConstraint = 3                        ' many trees - one per (specified) task
        ItemAbort = 4                                 ' many trees - one per (specified) task
        TimeOut = 5                                   ' many trees - one per (specified) task
        ResourceUnavailable = 6                       ' many trees - one per (specified) task
        ConstraintViolation = 7                       ' many trees - one per (specified) task
        CaseExternalTrigger = 8                       ' one tree
        ItemExternalTrigger = 9                       ' many trees - one per (specified) task
        Selection = 10                                ' many trees - one per (specified) task
    End Enum

    Private _spec As String = ""                      ' spec name
    Private _fName As String = ""                     ' the rules file
    Private _specFullPath As String                   ' full path to spec file (when new rules file created)
    Private _ruleSet(10) As TreeSet                   ' set of rule trees (there are 11 types) for this spec 
    Private _taskNames() As String                    ' list of task names in spec file 


    '***********************************************************************************************
    '****** PROPERTIES *****************************************************************************
    '***********************************************************************************************

    Public Property SpecName() As String
        Get
            Return _spec
        End Get
        Set(ByVal Value As String)
            _spec = Value
        End Set
    End Property

    Public Property SpecFullPath() As String
        Get
            Return _specFullPath
        End Get
        Set(ByVal Value As String)
            _specFullPath = Value
        End Set
    End Property

    Public Property LoadedFileName() As String
        Get
            Return _fName
        End Get
        Set(ByVal Value As String)
            _fName = Value
        End Set
    End Property

    ' allows access to individual trees in the ruleset
    Default Public Property item(ByVal ruleType As exType) As TreeSet
        Get
            Return _ruleSet(ruleType)
        End Get
        Set(ByVal Value As TreeSet)
            _ruleSet(ruleType) = Value
        End Set
    End Property

    Public ReadOnly Property hasFileLoaded() As Boolean
        Get
            Return (Not _fName Is Nothing) AndAlso (_fName.Length > 0)
        End Get
    End Property

    Public ReadOnly Property TaskNames() As String()
        Get
            If _taskNames Is Nothing Then
                _taskNames = getTaskListFromSpec()
            End If
            Return _taskNames
        End Get
    End Property

    '***********************************************************************************************
    '****** CONSTRUCTORS ***************************************************************************
    '***********************************************************************************************

    Public Sub New()
        For i As Integer = 0 To 10                     ' init each treeset in the ruleset
            _ruleSet(i) = New TreeSet
        Next
    End Sub

    Public Sub New(ByVal specFile As String, ByVal rulesFile As String)
        Me.New()
        _specFullPath = specFile
        _fName = rulesFile
        _taskNames = getTaskListFromSpec()
        ExtractSpecName()
    End Sub

    '***********************************************************************************************
    '****** METHODS FOR LOADING AND SAVING RULES FILES *********************************************
    '****** Note: at most one rules file may be open at any one time *******************************

    ' reads an rdr ruleset file into the editor
    Public Function LoadRulesFromFile(ByVal fName As String) As Boolean
        Dim x As XmlTextReader = Nothing
        Dim tsType As exType

        Try
            ' open and read file
            x = New XmlTextReader(fName)
            x.WhitespaceHandling = WhitespaceHandling.None

            While x.Read
                ' a rule file can contain up to 11 top level elements 
                ' there's one set of rules for each ruletype
                If x.NodeType = XmlNodeType.Element Then
                    Select Case x.Name
                        Case "constraints"
                            LoadConstraintRules(x)
                        Case "external"
                            LoadExternalRules(x)
                        Case "task"                       ' special case for version 1 rules files
                            _ruleSet(exType.Selection) = loadVersionOneRules(x)
                        Case "spec"
                            'ignore - root tag
                        Case Else
                            tsType = TreeNameToType(x.Name)              ' read rule tree directly
                            _ruleSet(tsType) = LoadItemRules(x, x.Name)
                            _ruleSet(tsType).TreeType = tsType
                    End Select
                End If
            End While
            _fName = fName                                      ' remember the file name
            ExtractSpecName()                                   ' get spec name from file name
            Return True
        Catch e As Exception
            MessageBox.Show("Exception when reading rules file: " & e.Message, "File Exception", _
                              MessageBoxButtons.OK, MessageBoxIcon.Error)
        Finally
            If Not x Is Nothing Then x.Close() ' close the file
        End Try

        Return False                                             ' if we get here we have a problem... 
    End Function

    ' reads in one or more of the four constraint types from file
    Private Sub LoadConstraintRules(ByVal x As XmlReader)
        Dim result As New TreeSet
        Dim tree As RuleTree
        Dim tempTag As String

        result.OuterTag = "constraints"

        ' inside the "constraints" tag-pair can be only the following tags: 'pre', 'post', 'case', 'item'
        ' so, outer = constraint; sub = case|item; inner = pre|post
        While x.Read
            If x.NodeType = XmlNodeType.Element Then
                If x.Name.StartsWith("p") Then
                    result.InnerTag = x.Name                                    ' must be "pre" or "post"

                    ' rules follow this tag 
                    If result.SubTag = "item" Then                              ' may have several trees - for each "task"
                        If result.InnerTag = "pre" Then
                            result.TreeType = exType.ItemPreConstraint          ' which rule set are we reading?
                        Else
                            result.TreeType = exType.ItemPostConstraint
                        End If
                        result.Trees = LoadItemRules(x, result.InnerTag).Trees  ' get set of loaded trees
                    Else                                                        ' "case", so one tree only
                        If result.InnerTag = "pre" Then
                            result.TreeType = exType.CasePreConstraint
                        Else
                            result.TreeType = exType.CasePostConstraint
                        End If

                        ' read the case level rules
                        result.addTree(LoadTree(x, result.InnerTag))
                    End If
                    _ruleSet(result.TreeType) = result                          ' save tree(s) to ruleset

                    ' reinitialise vars for next element
                    tree = New RuleTree
                    tempTag = result.SubTag
                    result = New TreeSet
                    result.OuterTag = "constraints"
                    result.SubTag = tempTag
                Else
                    result.SubTag = x.Name                                      ' must be "case" or "item"
                End If
            ElseIf x.NodeType = XmlNodeType.EndElement Then
                If x.Name = "constraints" Then                                  ' end of constraint rules
                    Exit While
                End If
            End If
        End While

    End Sub

    ' reads in one or two external rules trees (as specified in the file)
    Private Sub LoadExternalRules(ByVal x As XmlReader)
        Dim result As New TreeSet
        Dim tree As RuleTree

        result.OuterTag = "external"

        ' inside the 'external' tag-pair can be these tags: 'case' or 'item'
        While x.Read
            If x.NodeType = XmlNodeType.Element Then
                result.SubTag = x.Name
                If result.SubTag = "case" Then
                    result.TreeType = exType.CaseExternalTrigger                ' case type
                    result.addTree(LoadTree(x, "case"))
                Else
                    result.TreeType = exType.ItemExternalTrigger                ' item type
                    result.Trees = LoadItemRules(x, "item").Trees               ' get set of loaded trees
                End If
                _ruleSet(result.TreeType) = result                              ' save tree(s) to ruleset

                ' reinitialise vars
                tree = New RuleTree
                result = New TreeSet
                result.OuterTag = "external"

            ElseIf x.NodeType = XmlNodeType.EndElement Then
                If x.Name = "external" Then                                     ' end of external rules
                    Exit While
                End If
            End If
        End While
    End Sub

    ' reads in the tree of rule nodes for a certain (specified) task (ie. item)
    Private Function LoadItemRules(ByVal x As XmlTextReader, ByVal outerTag As String) As TreeSet
        Dim result As New TreeSet
        Dim tree As New RuleTree

        result.OuterTag = outerTag                                              ' tag denoting rule type

        While x.Read
            If x.NodeType = XmlNodeType.Element Then
                If x.Name = "task" Then                                         ' new task in ruleset
                    tree.Name = x.GetAttribute("name")                          ' init task structure
                ElseIf x.Name = "ruleNode" Then
                    tree.addNode(ReadRuleNode(x))                               ' read a node
                End If
            ElseIf x.NodeType = XmlNodeType.EndElement Then
                If x.Name = "task" Then                                         ' end of task block
                    result.addTree(tree)                                        ' add tree for this task to treeset
                    tree = New RuleTree                                         ' re-initialise
                ElseIf x.Name = outerTag Then                                   ' end of rules for this type
                    Exit While
                End If
            End If
        End While

        Return result
    End Function

    ' reads in one tree (for a case level set of rules)
    Private Function LoadTree(ByVal x As XmlTextReader, ByVal endTag As String) As RuleTree
        Dim tree As New RuleTree

        While x.Read
            If x.NodeType = XmlNodeType.Element Then
                If x.Name = "ruleNode" Then
                    tree.addNode(ReadRuleNode(x))                               ' read a node
                End If
            ElseIf x.NodeType = XmlNodeType.EndElement Then
                If x.Name = endTag Then                                         ' read closing tag
                    Exit While
                End If
            End If
        End While
        Return tree
    End Function

    ' reads a version one ruleset file into a 'selection' tree (version one only provided for selection rules)
    Private Function loadVersionOneRules(ByVal x As XmlTextReader) As TreeSet
        Dim result As New TreeSet
        Dim tree As New RuleTree

        result.OuterTag = "selection"                                           ' for later writeout
        result.TreeType = exType.Selection

        tree.Name = x.GetAttribute("name")                              ' this line has already been read by reader

        While x.Read
            If x.NodeType = XmlNodeType.Element Then
                If x.Name = "task" Then                                         ' new task in ruleset
                    tree.Name = x.GetAttribute("name")                          ' init task structure
                ElseIf x.Name = "ruleNode" Then
                    tree.addNode(ReadRuleNode(x))                               ' read a node
                End If
            ElseIf x.NodeType = XmlNodeType.EndElement Then
                If x.Name = "task" Then                                         ' end of task block
                    result.addTree(tree)                                        ' add tree for this task to treeset
                    tree = New RuleTree                                         ' re-initialise
                End If
            End If
        End While

        Return result
    End Function

    ' read a single rule node from the file
    Private Function ReadRuleNode(ByVal x As XmlTextReader) As RuleNode
        Dim item As String = ""                                                 ' element name
        Dim node As New RuleNode                                                ' this node
        Dim datalist As New ArrayList                                           ' for cornerstone data

        While x.Read
            If x.NodeType = XmlNodeType.Element Then
                item = x.Name
                If item = "conclusion" Then
                    node.Conclusion = ReadConclusion(x, item)                   ' conclusions are sets
                ElseIf item = "cornerstone" Then
                    node.Cornerstone = ReadCompositeItem(x, item)               ' cornerstones are sets 
                End If
            ElseIf x.NodeType = XmlNodeType.Text Then
                Select Case item                                                ' update node members
                    Case "id"
                        node.id = x.Value
                    Case "parent"
                        node.Parent = x.Value
                    Case "condition"
                        node.Condition = x.Value
                    Case "trueChild"
                        node.TrueChild = x.Value
                    Case "falseChild"
                        node.FalseChild = x.Value
                    Case "description"
                        node.Description = x.Value
                End Select
            ElseIf x.NodeType = XmlNodeType.EndElement Then
                If x.Name = "ruleNode" Then                                     ' end of rulenode def.
                    Exit While
                End If
            End If
        End While
        Return node
    End Function

    ' read conclusion data items & values
    Private Function ReadConclusion(ByVal x As XmlTextReader, ByVal outerTag As String) As ConclusionItem()
        Dim result() As ConclusionItem = Nothing                                ' to store conc data
        Dim conclusion As ConclusionItem

        While x.Read
            If x.NodeType = XmlNodeType.Element Then                            ' get element name  
                conclusion = New ConclusionItem(x.Name)
                conclusion.addItem(ReadCompositeItem(x, x.Name))                ' x.Name also denotes end tag
                result = AddConclusion(result, conclusion)
            ElseIf x.NodeType = XmlNodeType.EndElement Then
                If x.Name = outerTag Then                                       ' we're done when
                    Exit While                                                  ' at end of conc element
                End If
            End If
        End While
        Return result
    End Function

    ' read a set of attribute-value pairs (or <tag>value</tag> ) til closing tag found
    Private Function ReadCompositeItem(ByVal x As XmlTextReader, ByVal outerTag As String) As CompositeItem()
        Dim result() As CompositeItem = Nothing                                 ' to store comp data
        Dim element As CompositeItem
        Dim item As String = ""                                                 ' element name

        While x.Read
            If x.NodeType = XmlNodeType.Element Then                            ' get element name  
                item = x.Name
            ElseIf x.NodeType = XmlNodeType.Text Then
                element = New CompositeItem(item, x.Value)
                result = AddCompositeItem(result, element)                      ' ...then the data
            ElseIf x.NodeType = XmlNodeType.EndElement Then
                If x.Name = outerTag Then                                       ' we're done when
                    Exit While                                               ' at end of cs element
                End If
            End If
        End While
        Return result
    End Function

    '****** END OF READING METHODS ****************************************************************    

    'save ruleset back to file (with any additions)
    Public Sub SaveRulesToFile()
        Dim xw As XmlTextWriter = Nothing                     ' Write the XML file
        Dim tSet As TreeSet                                   ' for each treeset in ruleSet array
        Dim tree As RuleTree                                  ' ... for each tree in treeset 
        Dim node As RuleNode                                  ' ... ... for each node in tree
        Dim concItem As ConclusionItem                        ' ... ... ... for each conclusion element in node  
        Dim compItem As CompositeItem                         ' ... ... ... and each cornerstone element in node 
        Dim holdOuterTag As Boolean = False                   ' for sub-elements
        Dim holdSubTag As Boolean = False                     '  "       " 
        Dim heldOuterTag As String = "0"                      '  "       "
        Dim heldSubTag As String = "0"                        '  "       "

        Try
            ' open the file for writing
            xw = New XmlTextWriter(_fName, System.Text.Encoding.UTF8)
            xw.Formatting = Formatting.Indented

            xw.WriteStartDocument()
            xw.WriteStartElement("spec")                            ' top level element

            For Each tSet In _ruleSet                               ' for each set of rules in the ruleSet
                If tSet.hasTree Then                                ' if there are rules defined for this type

                    ' if there is more than one type of constraint or external rule, hold off writing 
                    ' out the end element until all the constraints or externals are done

                    ' sub tag is held if the rule type is 'constraint'
                    If holdSubTag Then

                        ' if this constraint sub-type is different from the previous one
                        If tSet.SubTag <> heldSubTag Then
                            xw.WriteEndElement()
                            holdSubTag = False
                        End If
                    End If

                    ' outer tag is held if previous rule type was 'constraints' or 'external'
                    If holdOuterTag Then

                        ' if this rule type has a different outer tag to the previous one
                        If tSet.OuterTag <> heldOuterTag Then
                            xw.WriteEndElement()
                            holdOuterTag = False
                        End If
                    End If

                    ' write the rule type tag (selection, constraints, etc)
                    If Not holdOuterTag Then
                        xw.WriteStartElement(tSet.OuterTag)
                        holdOuterTag = (tSet.OuterTag = "constraints") OrElse (tSet.OuterTag = "external")
                        If holdOuterTag Then heldOuterTag = tSet.OuterTag
                    End If

                    ' constraints and externals have sub tags (case or item)
                    If tSet.hasSubTag Then
                        If Not holdSubTag Then
                            xw.WriteStartElement(tSet.SubTag)
                            holdSubTag = (tSet.OuterTag = "constraints")
                            If holdSubTag Then heldSubTag = tSet.SubTag
                        End If
                    End If

                    ' constraints also have inner tags (pre or post)
                    If tSet.hasInnerTag Then xw.WriteStartElement(tSet.InnerTag)

                    For Each tree In tSet.Trees
                        If Not isCaseLevelTree(tSet.TreeType) Then         ' item level rules have a set of
                            xw.WriteStartElement("task")                   '    nodes for one or more tasks
                            xw.WriteAttributeString("name", tree.Name)     '    case level rules do not
                        End If

                        For Each node In tree.Nodes                        ' write rule node
                            xw.WriteStartElement("ruleNode")
                            WriteElement(xw, "id", node.id)
                            WriteElement(xw, "parent", node.Parent)
                            WriteElement(xw, "trueChild", node.TrueChild)
                            WriteElement(xw, "falseChild", node.FalseChild)
                            WriteElement(xw, "condition", node.Condition)

                            ' conclusion & cornerstone data are composite elements.
                            If node.Conclusion Is Nothing Then
                                WriteElement(xw, "conclusion", "null")          ' special case for root node 
                            Else
                                xw.WriteStartElement("conclusion")
                                For Each concItem In node.Conclusion
                                    xw.WriteStartElement(concItem.Tag)
                                    WriteElement(xw, "action", concItem.Action)
                                    WriteElement(xw, "target", concItem.Target)
                                    xw.WriteEndElement()                        ' /conclusion sub-element
                                Next
                                xw.WriteEndElement()                            ' /conclusion
                            End If

                            ' if cornerstone has no data, write fully qualified 
                            ' element(/endelement) otherwise there are problems reading 
                            ' the file in next time
                            xw.WriteStartElement("cornerstone")
                            If node.Cornerstone Is Nothing Then
                                xw.WriteWhitespace(" ")                         ' force full output 
                            Else
                                For Each compItem In node.Cornerstone
                                    WriteElement(xw, compItem.Tag, compItem.Text)
                                Next
                            End If
                            xw.WriteEndElement()                                ' /cornerstone

                            WriteElement(xw, "description", node.Description)

                            xw.WriteEndElement()                               ' /rulenode
                        Next node

                        ' write closing 'task' tag as required
                        If Not isCaseLevelTree(tSet.TreeType) Then xw.WriteEndElement() ' /task

                    Next tree

                    ' write closing sub and inner tags as required  
                    If tSet.hasInnerTag Then xw.WriteEndElement()

                    ' only write out the end element tag if it's not being held
                    If tSet.hasSubTag Then
                        If Not holdSubTag Then
                            xw.WriteEndElement()
                        End If
                    End If

                    If Not holdOuterTag Then xw.WriteEndElement() ' treeSet.outertag

                End If                            ' treeSet.hasTree
            Next tSet

            xw.WriteEndElement()                  ' spec
            xw.WriteEndDocument()                 ' End Document
            xw.Flush()
        Catch Ex As Exception
            ShowError("Unsuccessful update of rules file." & vbCrLf & vbCrLf & Ex.Message)
        Finally
            If Not xw Is Nothing Then xw.Close()
        End Try
    End Sub

    ' writes: <name>value</name>
    Private Sub WriteElement(ByVal xw As XmlTextWriter, ByVal name As String, _
                             ByVal value As String)
        xw.WriteStartElement(name)
        If Not value Is Nothing AndAlso (value.Length > 0) Then
            xw.WriteString(value)
        Else
            xw.WriteWhitespace(" ")      'forces "<name> </name>" instead of "<name/>"
        End If
        xw.WriteEndElement()
    End Sub

    'remove path and extension from filename to display in titlebar
    Private Sub ExtractSpecName()
        _spec = ExtractSpecName(_fName)
    End Sub

    ' returns the path and extension from the file name passed
    Public Function ExtractSpecName(ByVal fileName As String) As String
        Dim s As String = fileName.Substring(fileName.LastIndexOf("\"c) + 1)   ' remove path
        Return s.Substring(0, s.IndexOf("."c))                                 ' remove extn
    End Function


    '**************************************************************************************************
    '****** SUPPORT METHODS ***************************************************************************
    '**************************************************************************************************

    ' returns a string array of the rule type names of all rule types that have rules defined
    Public Function GetLoadedRuleTypesAsStrings() As String()
        Dim result As New ArrayList

        ' add tree set names to list for loaded treesets
        For Each tSet As TreeSet In _ruleSet
            If tSet.hasTree Then
                result.Add(TreeTypeToString(tSet.TreeType))
            End If
        Next

        Return result.ToArray(GetType(String))
    End Function

    ' returns a list of task names for all tasks having rules defined for the type of exception passed
    Public Function GetLoadedTasksForRuleType(ByVal ruleTypeStr As String) As String()
        Dim result As New ArrayList
        Dim treeSet As TreeSet = _ruleSet(StringToTreeType(ruleTypeStr))       ' get treeset for this ruletype

        For Each tree As RuleTree In treeSet.Trees
            result.Add(tree.Name)                                              ' get each task name in treeset
        Next

        Return result.ToArray(GetType(String))
    End Function

    ' returns a rule type for the string passed (non-composite rule types only)
    Private Function TreeNameToType(ByVal treeName As String) As exType
        Dim result As exType

        Select Case treeName
            Case "violation"
                result = exType.ConstraintViolation
            Case "timeout"
                result = exType.TimeOut
            Case "abort"
                result = exType.ItemAbort
            Case "selection"
                result = exType.Selection
            Case "resourceUnavailable"
                result = exType.ResourceUnavailable
        End Select
        Return result
    End Function

    ' replaces a tree for the rule type passed with the 'newtree' passed
    Public Sub updateTreeInRuleSet(ByVal newTree As RuleTree, ByVal ruleTypeStr As String)
        Dim treeSet As TreeSet

        'get selected tree set 
        treeSet = _ruleSet(StringToTreeType(ruleTypeStr))
        If isCaseLevelTree(treeSet.TreeType) Then
            treeSet.Trees(0) = newTree                                       ' case level have one tree only
        Else
            treeSet.updateTreeByTaskName(newTree)
        End If
    End Sub

    ' adds the 'newtree' passed for the ruletype passed to the treeset 
    Public Sub addTreeToRuleSet(ByVal newTree As RuleTree, ByVal ruleTypeStr As String)
        Dim ruleType As exType = StringToTreeType(ruleTypeStr)

        'add a tree to the selected tree set 
        _ruleSet(ruleType).addTree(newTree)
        setTreeSetTags(ruleType)                          ' set treeset's rule type (in case it's not already set)
    End Sub

    ' converts a specified rule type to its string equivalent
    Public Function TreeTypeToString(ByVal treeType As exType) As String
        Return [Enum].GetName(GetType(exType), treeType)
    End Function

    ' converts a specified string to its rule type equivalent
    Public Function StringToTreeType(ByVal text As String) As exType
        Return [Enum].Parse(GetType(exType), text)
    End Function

    ' converts a specified integer to its rule type equivalent
    Public Function IntToTreeType(ByVal idx As Integer) As exType
        Return [Enum].Parse(GetType(exType), idx)
    End Function

    ' returns true if the rule type passed (as a string) is a case-level rule type
    Public Function isCaseLevelTree(ByVal treeTypeString As String) As Boolean
        Return isCaseLevelTree(StringToTreeType(treeTypeString))
    End Function

    ' returns true if the rule type passed is a case-level rule type
    Public Function isCaseLevelTree(ByVal treeType As exType) As Boolean
        Select Case treeType
            Case exType.CasePreConstraint, exType.CasePostConstraint, exType.CaseExternalTrigger
                Return True
            Case Else
                Return False
        End Select
    End Function

    ' returns a string list containing the names of all rule types
    Public Function getRuleTypeList() As String()
        Dim result(10) As String

        For i As Integer = 0 To 10
            result(i) = TreeTypeToString(i)
        Next

        Return result
    End Function

    ' retrieves a list of task names from the specified spec file
    ' This method is also accessed through 'taskNames' property
    Private Function getTaskListFromSpec() As String()
        Dim result As New ArrayList
        Dim specFile As String = "\" & _spec & ".xml"                        ' name of spec file to find
        Dim paths() As String
        Dim fullPath As String
        Dim defResult() As String = {""}                    ' default empty array to return when spec not found

        ' don't try to find unspecified spec or if no spec path specified in config
        If _spec Is Nothing Then Return defResult
        If pathOf.SpecPaths Is Nothing Then Return defResult

        If Not _specFullPath Is Nothing Then                                 ' already got path to file
            result = ReadTaskNames(_specFullPath)
        Else
            paths = pathOf.SpecPaths.Split(";"c)                             ' work through each path to find file
            ReDim Preserve paths(paths.Length)
            paths(paths.GetUpperBound(0)) = pathOf.repository & "\worklets"  ' add repository to list of paths

            'search each specified path for the spec file in question
            For Each path As String In paths
                fullPath = path & specFile

                If File.Exists(fullPath) Then                                ' if found, open it
                    result = ReadTaskNames(fullPath)                         ' & get the task names 
                    _specFullPath = fullPath
                    Exit For
                End If
            Next
        End If

        If result Is Nothing Then                                            ' couldn't find spec file
            ShowError("Could not load task identifiers from specification")
            Return defResult
        Else
            Return result.ToArray(GetType(String))
        End If
    End Function

    ' get task names from spec file
    ' pre: specfile is a valid path to a spec xml file
    Private Function ReadTaskNames(ByVal specfile As String) As ArrayList
        Dim result As New ArrayList

        Try
            Dim xtr As XmlTextReader = New XmlTextReader(specfile)
            xtr.WhitespaceHandling = WhitespaceHandling.None

            ' look for "decomposesTo" tag to get name (id) of a task & add it to list
            While xtr.Read
                If xtr.NodeType = XmlNodeType.Element AndAlso xtr.Name = "decomposesTo" Then
                    result.Add(xtr.GetAttribute("id"))
                End If
            End While
        Catch ex As Exception
            ShowError("Exception reading specification " & _spec & vbCrLf & ex.Message)
            result = Nothing
        End Try

        Return result
    End Function

    ' returns a list of all task names in the loaded spec that 
    ' do NOT have rule trees defined for the specified rule type
    Public Function getAvailableTaskNamesForTreeType(ByVal treeType As String) As String()

        ' no tasks for case level trees
        If isCaseLevelTree(treeType) Then Return Nothing

        Dim tasks As String() = TaskNames                                       ' start with full list of tasks
        Dim result As New ArrayList(tasks)
        Dim trees As RuleTree() = _ruleSet(StringToTreeType(treeType)).Trees    ' get trees for the rule type

        ' if there is at least one tree defined for this rule type
        If Not trees Is Nothing Then

            ' remove any tasks that rules have been defined for
            For Each tree As RuleTree In trees
                result.Remove(tree.Name)
            Next

            If result.Count = 0 Then
                Return Nothing                                          ' item level tree, all items used
            Else
                Return result.ToArray(GetType(String))                  ' item level tree, some items not yet used
            End If
        Else
            Return TaskNames                                      ' no rules for this type, so all tasks available
        End If
    End Function

    ' Sets the appropriate element tags for the specified rule type
    ' Used when saving new trees to the treeset from the NewRule form
    Public Sub setTreeSetTags(ByVal treeType As exType)
        Dim tSet As TreeSet = _ruleSet(treeType)                                 ' get the appropriate treeSet

        tSet.TreeType = treeType                                                 ' set it's ruletype property

        ' set the treeset tags for the ruletype
        Select Case treeType
            Case exType.CasePreConstraint
                tSet.OuterTag = "constraints"
                tSet.SubTag = "case"
                tSet.InnerTag = "pre"
            Case exType.CasePostConstraint
                tSet.OuterTag = "constraints"
                tSet.SubTag = "case"
                tSet.InnerTag = "post"
            Case exType.ItemPreConstraint
                tSet.OuterTag = "constraints"
                tSet.SubTag = "item"
                tSet.InnerTag = "pre"
            Case exType.ItemPostConstraint
                tSet.OuterTag = "constraints"
                tSet.SubTag = "item"
                tSet.InnerTag = "post"
            Case exType.ItemAbort
                tSet.OuterTag = "abort"
            Case exType.TimeOut
                tSet.OuterTag = "timeout"
            Case exType.ResourceUnavailable
                tSet.OuterTag = "resourceUnavailable"
            Case exType.ConstraintViolation
                tSet.OuterTag = "violation"
            Case exType.CaseExternalTrigger
                tSet.OuterTag = "external"
                tSet.SubTag = "case"
            Case exType.ItemExternalTrigger
                tSet.OuterTag = "external"
                tSet.SubTag = "item"
            Case exType.Selection
                tSet.OuterTag = "selection"
        End Select
    End Sub

End Class
