#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  JBoss Package Manager setup script                                      ##
##                                                                          ##
### ====================================================================== ###

DIRNAME=`dirname $0`
PROGNAME=`basename $0`
GREP="grep"


#
# Helper to complain.
#
warn() {
    echo "${PROGNAME}: $*"
}

#
# Helper to puke.
#
die() {
    warn $*
    exit 1
}

# OS specific support (must be 'true' or 'false').
cygwin=false;
darwin=false;
linux=false;
case "`uname`" in
    CYGWIN*)
        cygwin=true
        ;;

    Darwin*)
        darwin=true
        ;;
        
    Linux)
        linux=true
        ;;
esac

# Setup PACKAGE_MANAGER_HOME
if [ "x$PACKAGE_MANAGER_HOME" = "x" ]; then
    # get the full path (without any relative bits)
    PACKAGE_MANAGER_HOME=`cd $DIRNAME/..; pwd`
fi
export PACKAGE_MANAGER_HOME

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
	JAVA="$JAVA_HOME/bin/java"
    else
	JAVA="java"
    fi
fi

# Setup the classpath
# wildcards are supported in classpath, starting Java 6
PACKAGE_MANAGER_CLASSPATH="$PACKAGE_MANAGER_HOME/lib/*"

# Setup JBoss Package Manager specific properties
JAVA_OPTS="-Dprogram.name=$PROGNAME $JAVA_OPTS"

# Sample JPDA settings for remote socket debugging
#JAVA_OPTS="$JAVA_OPTS -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=y"


# Display our environment
echo "========================================================================="
echo ""
echo "  JBoss Package Manager Environment"
echo ""
echo "  PACKAGE_MANAGER_HOME: $PACKAGE_MANAGER_HOME"
echo ""
echo "  JAVA: $JAVA"
echo ""
echo "  JAVA_OPTS: $JAVA_OPTS"
echo ""
echo "  CLASSPATH: $PACKAGE_MANAGER_CLASSPATH"
echo ""
echo "========================================================================="
echo ""

"$JAVA" $JAVA_OPTS \
         -classpath "$PACKAGE_MANAGER_CLASSPATH" \
         org.jboss.ejb3.packagemanager.main.Main --setup "$PACKAGE_MANAGER_HOME/script/schema.sql" \
         "$@"
      PACKAGE_MANAGER_STATUS=$?

