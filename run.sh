javac -cp "lib/amqp-client-5.16.0.jar:lib/slf4j-api-1.7.36.jar:lib/slf4j-simple-1.7.36.jar" -d class src/*.java

# Antennas (each in a separate terminal)
java -cp "class:lib/amqp-client-5.16.0.jar:lib/slf4j-api-1.7.36.jar:lib/slf4j-simple-1.7.36.jar" Antenna A 100 100 B
java -cp "class:lib/amqp-client-5.16.0.jar:lib/slf4j-api-1.7.36.jar:lib/slf4j-simple-1.7.36.jar" Antenna B 300 100 C
java -cp "class:lib/amqp-client-5.16.0.jar:lib/slf4j-api-1.7.36.jar:lib/slf4j-simple-1.7.36.jar" Antenna C 200 300 A

# Users (each in a separate terminal)
java -cp "class:lib/amqp-client-5.16.0.jar:lib/slf4j-api-1.7.36.jar:lib/slf4j-simple-1.7.36.jar" User alice 110 110
java -cp "class:lib/amqp-client-5.16.0.jar:lib/slf4j-api-1.7.36.jar:lib/slf4j-simple-1.7.36.jar" User bob 290 110