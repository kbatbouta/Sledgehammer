@setlocal enableextensions
@cd /d "%~dp0"
java -javaagent:jamm-0.2.5.jar -Xms4g -Xmx4g -Djava.library.path=./ -Dzomboid.znetlog=0 -cp jamm-0.2.5.jar;jinput.jar;lwjgl.jar;lwjgl_util.jar;sqlite-jdbc-3.8.10.1.jar;trove-3.0.3.jar;uncommons-maths-1.2.3.jar;./ zombie.network.GameServer
PAUSE
