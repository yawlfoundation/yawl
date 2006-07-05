#
# This ProGuard configuration file illustrates how to process ProGuard
# (including its main application, its GUI, its Ant task, and its WTK plugin),
# and the ReTrace tool, all in one go.
# Configuration files for typical applications will be very similar.
# Usage:
#     java -jar proguard.jar @proguardall.pro
#

# Specify the input jars, output jars, and library jars.
# We'll read all jars from the lib directory, process them, and write the
# processed jars to a new out directory.

-injars  lib
-outjars out

# You may have to adapt the paths below.

-libraryjars <java.home>/lib/rt.jar
-libraryjars /usr/local/java/ant1.6.2/lib/ant.jar
-libraryjars /usr/local/java/wtk2.1/wtklib/kenv.zip

# Allow methods with the same signature, except for the return type,
# to get the same obfuscation name.

-overloadaggressively

# Put all obfuscated classes into the nameless root package.

-defaultpackage ''

# Allow classes and class members to be made public.

-allowaccessmodification

# The main entry points.

-keep public class proguard.ProGuard {
    public static void main(java.lang.String[]);
}

-keep public class proguard.gui.ProGuardGUI {
    public static void main(java.lang.String[]);
}

-keep public class proguard.retrace.ReTrace {
    public static void main(java.lang.String[]);
}

# If we have ant.jar, we can properly process the Ant task.

-keep public class proguard.ant.* {
    public void set*(%);
    public void set*(**);
    public void add*(**);
}

# If we have kenv.zip, we can process the J2ME WTK plugin.

-keep public class proguard.wtk.ProGuardObfuscator

# In addition, the following classes load resource files, based on their class
# names or package names, so we don't want them to be obfuscated or moved to 
# the default package.

-keep class proguard.gui.GUIResources
-keep class proguard.gui.ClassPathPanel$MyListCellRenderer
