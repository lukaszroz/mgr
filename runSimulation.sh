#!/bin/bash --posix

JAVA=`cygpath 'C:\Program Files\Java\jdk1.7.0_01\bin\java'`

servers=( XX )

#SS SJ LS LJ CS CJ IM AS AJ

users=( 10 20 )
writers=( 0 1000 100 10 )

for server in ${servers[@]}
do
	for user in ${users[@]}
	do
		for writer in ${writers[@]}
		do
			command=`echo -server \
				-Dfile.encoding=UTF-8 \
				-jar target/simulation.jar "$@" -f -u $user -w $writer $server`
			echo '******************************************************************************'
			echo "$JAVA" $command
			"$JAVA" $command
			echo '------------------------------------------------------------------------------'
		done
	done
done

actors=( AS AJ )

for server in ${servers[@]}
do
	for user in ${actors[@]}
	do
		for writer in ${writers[@]}
		do
			command=`echo -server \
				-Dfile.encoding=UTF-8 \
				-jar target/simulation.jar "$@" -u $user -w $writer $server`
			echo '******************************************************************************'
			echo "$JAVA" $command
			"$JAVA" $command
			echo '------------------------------------------------------------------------------'
		done
	done
done