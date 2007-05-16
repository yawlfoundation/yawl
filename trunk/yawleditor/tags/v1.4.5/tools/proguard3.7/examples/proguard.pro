#
# This ProGuard configuration file illustrates how to process ProGuard itself.
# Configuration files for typical applications will be very similar.
# Usage:
#     java -jar proguard.jar @proguard.pro
#

# Specify the input jars, output jars, and library jars.
# We'll filter out the Ant and WTK classes, keeping everything else.

-injars  ../lib/proguard.jar(!proguard/ant/**,!proguard/wtk/**)
-outjars proguard_out.jar

-libraryjars <java.home>/lib/rt.jar

# Write out an obfuscation mapping file, for de-obfuscating any stack traces
# later on, or for incremental obfuscation of extensions.

-printmapping proguard.map

# Allow methods with the same signature, except for the return type,
# to get the same obfuscation name.

-overloadaggressively

# Put all obfuscated classes into the nameless root package.

-defaultpackage ''

# Allow classes and class members to be made public.

-allowaccessmodification

# The entry point: ProGuard and its main method.

-keep public class proguard.ProGuard {
    public static void main(java.lang.String[]);
}

# If you want to preserve the Ant task as well, you'll have to specify the
# main ant.jar.

#-libraryjars /usr/local/java/ant1.6.2/lib/ant.jar
#-keep public class proguard.ant.* {
#    public void set*(%);
#    public void set*(**);
#    public void add*(**);
#}


# If you want to preserve the WTK obfuscation plug-in, you'll have to specify
# the kenv.zip file.

#-libraryjars /usr/local/java/wtk2.1/wtklib/kenv.zip
#-keep public class proguard.wtk.ProGuardObfuscator
