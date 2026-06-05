#!/bin/sh
# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup script for Unix-like systems.
#
# Run chmod +x mvnw if this file loses its executable bit after cloning.
# ----------------------------------------------------------------------------

if [ -z "$MAVEN_SKIP_RC" ]; then
  if [ -f /usr/local/etc/mavenrc ]; then
    . /usr/local/etc/mavenrc
  fi
  if [ -f /etc/mavenrc ]; then
    . /etc/mavenrc
  fi
  if [ -f "$HOME/.mavenrc" ]; then
    . "$HOME/.mavenrc"
  fi
fi

cygwin=false
darwin=false
case "$(uname)" in
  CYGWIN*) cygwin=true ;;
  Darwin*)
    darwin=true
    if [ -z "$JAVA_HOME" ]; then
      if [ -x "/usr/libexec/java_home" ]; then
        JAVA_HOME="$(/usr/libexec/java_home)"
        export JAVA_HOME
      else
        JAVA_HOME="/Library/Java/Home"
        export JAVA_HOME
      fi
    fi
    ;;
esac

# Resolve script location, following symlinks
PRG="$0"
while [ -h "$PRG" ]; do
  ls=$(ls -ld "$PRG")
  link=$(expr "$ls" : '.*-> \(.*\)$')
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG="$(dirname "$PRG")/$link"
  fi
done

saveddir=$(pwd)
MAVEN_PROJECTBASEDIR=${MAVEN_BASEDIR:-"$(cd "$(dirname "$PRG")" && pwd)"}
cd "$saveddir"

WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_PROPERTIES="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"

if [ ! -f "$WRAPPER_JAR" ]; then
  WRAPPER_URL=$(grep '^wrapperUrl=' "$WRAPPER_PROPERTIES" | cut -d'=' -f2-)
  if command -v curl > /dev/null 2>&1; then
    curl -fsSL -o "$WRAPPER_JAR" "$WRAPPER_URL" || {
      echo "Failed to download Maven wrapper jar from $WRAPPER_URL" >&2
      exit 1
    }
  elif command -v wget > /dev/null 2>&1; then
    wget -q -O "$WRAPPER_JAR" "$WRAPPER_URL" || {
      echo "Failed to download Maven wrapper jar from $WRAPPER_URL" >&2
      exit 1
    }
  else
    echo "Neither curl nor wget is available; cannot download Maven wrapper jar." >&2
    exit 1
  fi
fi

if $cygwin; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME=$(cygpath --path --windows "$JAVA_HOME")
  MAVEN_PROJECTBASEDIR=$(cygpath --path --windows "$MAVEN_PROJECTBASEDIR")
  WRAPPER_JAR=$(cygpath --path --windows "$WRAPPER_JAR")
fi

if [ -z "$JAVACMD" ]; then
  if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
  else
    JAVACMD="$(command -v java)"
  fi
fi

if [ ! -x "$JAVACMD" ]; then
  echo "Error: JAVA_HOME is not defined correctly and no java was found on PATH." >&2
  exit 1
fi

exec "$JAVACMD" \
  $MAVEN_OPTS \
  -classpath "$WRAPPER_JAR" \
  "-Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"
