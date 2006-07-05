#
# This ProGuard configuration file illustrates how to process the ProGuard GUI.
# Configuration files for typical applications will be very similar.
# Usage:
#     java -jar proguard.jar @proguardgui.pro
#

# Specify the input jars, output jars, and library jars.
# The input jars will be merged in a single output jar.
# We'll filter out the Ant and WTK classes.

-injars  ../lib/proguardgui.jar
-injars  ../lib/proguard.jar(!META-INF/MANIFEST.MF,
                             !proguard/ant/**,!proguard/wtk/**)
-outjars proguardgui_out.jar

-libraryjars <java.home>/lib/rt.jar

# If we wanted to reuse the previously obfuscated proguard_out.jar, we could
# perform incremental obfuscation based on its mapping file, and only keep the
# additional GUI files instead of all files.

#-applymapping proguard.map
#-outjars      proguardgui_out.jar(proguard/gui/**)

# Allow methods with the same signature, except for the return type,
# to get the same obfuscation name.

-overloadaggressively

# Put all obfuscated classes into the nameless root package.

-defaultpackage ''

# Allow classes and class members to be made public.

-allowaccessmodification

# The entry point: ProGuardGUI and its main method.

-keep public class proguard.gui.ProGuardGUI {
    public static void main(java.lang.String[]);
}

# In addition, the following classes load resource files, based on their class
# names or package names, so we don't want them to be obfuscated or moved to
# the default package.

-keep class proguard.gui.GUIResources
-keep class proguard.gui.ClassPathPanel$MyListCellRenderer
