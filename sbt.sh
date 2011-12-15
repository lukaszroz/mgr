#!/bin/bash --posix

dir=`dirname $0`
home=`cygpath -w ~`
JAVA=`cygpath 'C:\Program Files\Java\jdk1.7.0_01\bin\java'`

"$JAVA" \
  -Xmx512M \
  -XX:MaxPermSize=256M \
  -server \
  -Dsbt.global.base=$home/.sbt/ -Dsbt.boot.directory=$home/.sbt/boot/ \
  -jar `cygpath -w $dir`/sbt-launch.jar "$@"