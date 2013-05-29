cd c:\yawl\editor

javac -classpath .;lib\yawl.jar;.\classFiles\YAWLEditor\ -d .\classFiles\YAWLEditor\ C:\yawl\editor\source\au\edu\qut\yawl\editor\analyser\*.java

javac -classpath .;lib\yawl.jar;.\classFiles\YAWLEditor\ -d .\classFiles\YAWLEditor\ C:\yawl\editor\source\au\edu\qut\yawl\editor\reductionrules\*.java


java -classpath .;lib\yawl.jar;.\classFiles\YAWLEditor\ au.edu.qut.yawl.editor.reductionrules.ReductionRuleTester C:\yawl\editor\RuleExs\FORtest.xml 1


java -classpath .;lib\yawl.jar;.\classFiles\YAWLEditor\ au.edu.qut.yawl.editor.reductionrules.ReductionRuleTester C:\yawl\editor\RuleExs\FORtest.xml 2

java -classpath .;lib\yawl.jar;.\classFiles\YAWLEditor\ au.edu.qut.yawl.editor.reductionrules.ResetReductionRuleTester C:\yawl\editor\RuleExs\FORtest.xml 1



java -classpath .;lib\yawl.jar;.\classFiles\YAWLEditor\ au.edu.qut.yawl.editor.reductionrules.ReductionRuleTester C:\yawl\editor\RuleExs\example.xml 3

java -classpath .;lib\yawl.jar;.\classFiles\YAWLEditor\ au.edu.qut.yawl.editor.reductionrules.ReductionRuleTester C:\yawl\editor\RuleExs\example_FSPYrule_Reduced.xml 11

java -classpath .;lib\yawl.jar;.\classFiles\YAWLEditor\ au.edu.qut.yawl.editor.reductionrules.ReductionRuleTester C:\yawl\editor\RuleExs\example_FSPYrule_Reduced_FXORrule_Reduced.xml 6

java -classpath .;lib\yawl.jar;.\classFiles\YAWLEditor\ au.edu.qut.yawl.editor.reductionrules.ReductionRuleTester C:\yawl\editor\RuleExs\Orexample.xml 3

java -classpath .;lib\yawl.jar;.\classFiles\YAWLEditor\ au.edu.qut.yawl.editor.reductionrules.ReductionRuleTester C:\yawl\editor\RuleExs\Orexample_FSPYrule_Reduced.xml 2

java -classpath .;lib\yawl.jar;.\classFiles\YAWLEditor\ au.edu.qut.yawl.editor.reductionrules.ReductionRuleTester C:\yawl\editor\RuleExs\Orexample_FSPYrule_Reduced_FIErule_Reduced.xml 3

java -classpath .;lib\yawl.jar;.\classFiles\YAWLEditor\ au.edu.qut.yawl.editor.reductionrules.ReductionRuleTester C:\yawl\editor\RuleExs\Orexample_FSPYrule_Reduced_FIErule_Reduced_FSPYrule_Reduced.xml 1



