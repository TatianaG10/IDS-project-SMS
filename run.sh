MY_VAT_TAT="/home/tatiana/School/M1-mosig/IDS/IDS-chat-app"
MY_VAR_AND="/home/andy/Documents/Workspace/Distributed_system/IDS-chat-app"
MY_VAR=$MY_VAT_TAT

export CLASSPATH="$MY_VAR/lib/Chat.jar:$MY_VAR/lib/Chat_itf.jar:$MY_VAR/lib/ChatDiscussion.jar:$MY_VAR/lib/ChatDiscussion_itf.jar:$MY_VAR/lib/ChatClientList.jar:$MY_VAR/lib/ChatClientList_itf.jar:$MY_VAR/lib/ClientInfo.jar:$MY_VAR/lib/ClientInfo_itf.jar:$MY_VAR/lib/ChatClientDisplay.jar:$MY_VAR/lib/ChatMessage.jar"

# Compiling 

javac -d classes -classpath .:classes src/ChatMessage.java
cd classes
jar cvf ../lib/ChatMessage.jar ChatMessage.class
cd ../

javac -d classes -classpath .:classes src/ClientInfo_itf.java
cd classes
jar cvf ../lib/ClientInfo_itf.jar ClientInfo_itf.class
cd ../

javac -d classes -classpath .:classes src/ClientInfo.java
cd classes
jar cvf ../lib/ClientInfo.jar ClientInfo.class
cd ../

javac -d classes -classpath .:classes src/ChatClientList_itf.java
cd classes
jar cvf ../lib/ChatClientList_itf.jar ChatClientList_itf.class
cd ../

javac -d classes -classpath .:classes src/ChatClientList.java
cd classes
jar cvf ../lib/ChatClientList.jar ChatClientList.class
cd ../

javac -d classes -classpath .:classes src/ChatDiscussion_itf.java
cd classes
jar cvf ../lib/ChatDiscussion_itf.jar ChatDiscussion_itf.class
cd ../

javac -d classes -classpath .:classes src/ChatDiscussion.java
cd classes
jar cvf ../lib/ChatDiscussion.jar ChatDiscussion.class
cd ../

javac -d classes -classpath .:classes src/Chat_itf.java
cd classes
jar cvf ../lib/Chat_itf.jar Chat_itf.class
cd ../

javac -d classes -classpath .:classes src/Chat.java
cd classes
jar cvf ../lib/Chat.jar Chat.class
cd ../

javac -d classes -classpath .:classes src/ChatServer.java
cd classes
jar cvf ../lib/ChatServer.jar ChatServer.class
cd ../

javac -d classes -classpath .:classes src/ChatClientDisplay.java
cd classes
jar cvf ../lib/ChatClientDisplay.jar ChatClientDisplay.class
cd ../

javac -d classes -classpath .:classes src/ChatClient.java
cd classes
jar cvf ../lib/ChatClient.jar ChatClient.class
cd ../

javac -d classes -cp .:classes:lib/Chat.jar:lib/Chat_itf.jar:lib/ChatDiscussion.jar:lib/ChatDiscussion_itf.jar:lib/ChatClientList.jar:lib/ChatClientList_itf.jar src/ChatServer.java

javac -d classes -cp .:classes:lib/ClientInfo.jar:lib/ClientInfo_itf.jar:lib/Chat_itf.jar:lib/ChatClientDisplay.jar:lib/ChatMessage src/ChatClient.java

# cd ../
# Lauching rmi, sever and client 

# rmiregistry &

# java -classpath .:classes:lib/Chat.jar:lib/Chat_itf.jar:lib/ChatDiscussion.jar:lib/ChatDiscussion_itf.jar:lib/ChatClientList.jar:lib/ChatClientList_itf.jar ChatServer

# java -classpath .:classes:lib/ClientInfo.jar:lib/ClientInfo_itf.jar:lib/Chat_itf.jar:lib/ChatClientDisplay.jar:lib/ChatMessage.jar ChatClient Andy localhost

