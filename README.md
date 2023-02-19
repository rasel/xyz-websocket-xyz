# WebSocket Coding challenge

Steps to run this project:

1. Clone this Git repository `https://github.com/rasel/xyz-websocket-xyz.git`
2. Navigate to the folder `xyz-websocket-xyz`
3. build `mvn clean install`
4. Run `mvn spring-boot:run` OR `java -jar target/genesys-chat-server-0.0.1-SNAPSHOT.jar`

Approach:

1. I have used he STOMP based Websocket using SpringBoot framework websocket broker

Done:
1. Message borker for 
a) Create Room
b) Join Room 
c.Typing Indicator broker 
d) Send message to specific chat-room
2. Unit test and Integration test
3. implemenation validation and exception handling

To Be done:
1. data saving Database
2. Code better documented
2. More integration test
3. Thread safety code (current implemention is not theard-safe)
4. Typing indicator currently (every 2 seconds it publish list of users name those are typing, this part not sure, best approch or not, can be more anlysed if there is better approach)
5. For high laod of message trafic Spring simple borker is not suitable for clustering. Better we should use external message broker like ActiveMQ, RabbitMQ..this
will be helpful high performance load balancing

