# IDS-project-SMS
how to execute:
- sudo systemctl status rabbitmq-server
- sudo rabbitmqctl status

./run.sh to => compiles 

java -cp "class:lib/*" MasterAntennaNode => launch master node

java -cp "class:lib/*" Antenna AntA 0 0 AntB

java -cp "class:lib/*" Antenna AntB 200 0 AntA

java -cp "class:lib/*" User 1 10 10

java -cp "class:lib/*" User 2 200 10


sudo systemctl stop rabbitmq-server


## javac -d class -cp "lib\*" src\*.java
## java -cp "class;lib\*" DemoFenetre   