#!/bin/bash

err=1
until [ $err == 0 ];
do
	java -Dfile.encoding=UTF-8 -Xms512m -Xmx2g -p ./lib -cp 'config:./lib/*' -m org.l2j.gameserver/org.l2j.gameserver.GameServer --enable-preview
    err=$?
	sleep 10;
done
