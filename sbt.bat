set SCRIPT_DIR=%~dp0
rem "C:\Program Files\Java\jdk1.7.0_01\bin\java" -server -Xss2M -Xmx512M -XX:MaxPermSize=256M -noverify -Dsbt.boot.directory="C:\cygwin\home\SG0212047\.sbt\boot" -Dsbt.global.base="C:\cygwin\home\SG0212047\.sbt" -Xmx512M -jar "%SCRIPT_DIR%sbt-launch.jar" %*

"C:\Program Files\Java\jdk1.7.0_01\bin\java" -server -Xmx512M -XX:MaxPermSize=256M -Dsbt.boot.directory="C:\cygwin\home\SG0212047\.sbt\boot" -Dsbt.global.base="C:\cygwin\home\SG0212047\.sbt" -Xmx512M -jar "%SCRIPT_DIR%sbt-launch.jar" %*