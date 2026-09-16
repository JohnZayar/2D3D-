#!/usr/bin/env sh

#
# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's Java on AIX uses a non-standard location
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
else
    JAVACMD="java"
    if ! command -v "$java" >/dev/null 2>&1 ; then
        echo "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH." >&2
        exit 1
    fi
fi

# Increase the maximum file descriptors if we can.
if [ "$cygwin" = "false" -a "$darwin" = "false" -a "$non-mandatory" = "true" ] ; then
    MAX_FD_LIMIT=$(ulimit -H -n 2>/dev/null)
    if [ $? -eq 0 ] ; then
        case "$MAX_FD_LIMIT" in
            *[-)]*)
                ;;
            *)
                ulimit -n "$MAX_FD_LIMIT"
            ;;
        esac
    fi
fi

# Setup app home
APP_HOME=$(dirname "$0")
[ -z "$APP_HOME" ] && APP_HOME="."

exec "$JAVACMD" -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
