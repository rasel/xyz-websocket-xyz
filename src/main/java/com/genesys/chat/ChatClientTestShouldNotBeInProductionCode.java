package com.genesys.chat;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

import com.genesys.chat.model.Message;
import com.genesys.chat.model.StoredMessage;
import com.genesys.chat.service.ChatService;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

//TODO write sample cleint to test the server (create room, join chat room, subscribe message)

/**
 * this class will not be in production code this is just test class to watch in log file..some checking
 */
public class ChatClientTestShouldNotBeInProductionCode {

  private static WebSocketStompClient webSocketStompClient;

  public static void runDummyChatClient() throws ExecutionException, InterruptedException, TimeoutException {

    System.out.println("Chat client test...........");
    ChatService chatService = new ChatService();
    StoredMessage.chatRoomData.clear();
    webSocketStompClient = new WebSocketStompClient(new SockJsClient(
        List.of(new WebSocketTransport(new StandardWebSocketClient()))));
    webSocketStompClient
        .setMessageConverter(new CompositeMessageConverter(Arrays
            .asList(new MappingJackson2MessageConverter(), new StringMessageConverter())));

    String roomName100 = "roomName100";
    String roomName200 = "roomName200";
    String user100 = "user100";
    String user110 = "user110";
    String user200 = "user200";
    chatService.createChatRoom(roomName100);
    chatService.createChatRoom(roomName200);
    chatService.joinChatRoom(user100, roomName100);
    chatService.joinChatRoom(user110, roomName100);
    chatService.joinChatRoom(user200, roomName200);

    StompSession stompSession = webSocketStompClient
        .connect("ws://localhost:8080/ws", new StompSessionHandlerAdapter() {
        })
        .get(1, SECONDS);

    susbcribeMessageBroker(stompSession, roomName100);
    susbcribeMessageBroker(stompSession, roomName200);

    //listen room2 messages

    stompSession.send("/app/chat/broadcast/" + roomName100,
        Message.builder().user(user100).content("Test message from user1").build());
    stompSession.send("/app/chat/broadcast/" + roomName100,
        Message.builder().user(user110).content("Test message from user11").build());

    //user2 try to broadcast message to room1, but it will not boradcast, user2 didn't join in "room1"
    stompSession.send("/app/chat/broadcast/" + roomName100,
        Message.builder().user(user200).content("Test message from user2").build());
    //now user to is sending correct message to "room2"
    stompSession.send("/app/chat/broadcast/" + roomName200,
        Message.builder().user(user200).content("Test message from user2").build());
  }

  private static void susbcribeMessageBroker(StompSession stompSession, String room) {
    stompSession.subscribe("/secured/chatroom/" + room + "/message",
        new StompFrameHandler() {
          @Override
          public Type getPayloadType(final StompHeaders headers) {
            return Message.class;
          }

          @Override
          public void handleFrame(final StompHeaders headers, final Object payload) {
            System.out.println(
                String.format("Message received in client '%s' and messages %s", room, (Message) payload));
          }
        });
  }
}