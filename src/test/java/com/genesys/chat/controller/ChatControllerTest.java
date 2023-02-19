package com.genesys.chat.controller;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.genesys.chat.model.Message;
import com.genesys.chat.model.StoredMessage;
import com.genesys.chat.service.ChatService;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
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

import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ChatControllerTest {

  @LocalServerPort
  private Integer port;

  private WebSocketStompClient webSocketStompClient;
  private CompletableFuture<List<String>> completableFutureCreateRoom;
  @Autowired
  private ChatService chatService;

  @BeforeEach
  void setup() {
    StoredMessage.chatRoomData.clear();
    this.completableFutureCreateRoom = new CompletableFuture<>();
    this.webSocketStompClient = new WebSocketStompClient(new SockJsClient(
        List.of(new WebSocketTransport(new StandardWebSocketClient()))));
    this.webSocketStompClient
        .setMessageConverter(new CompositeMessageConverter(Arrays
            .asList(new MappingJackson2MessageConverter(), new StringMessageConverter())));
  }

  @Test
  public void verifyCreateChatRoom() throws ExecutionException, InterruptedException, TimeoutException {
    String roomName = "room1";
    StompSession stompSession = webSocketStompClient
        .connect(getWsPath(), new StompSessionHandlerAdapter() {
        })
        .get(1, SECONDS);

    stompSession.subscribe("/topic/public", new CreateChatStompFrameHandler());
    stompSession.send("/app/chat/createRoom/" + roomName, null);

    List<String> list = completableFutureCreateRoom.get(10, SECONDS);
    assertEquals(list.get(0), roomName);
  }

  @Test
  public void verifyBroadCastMessageToSpecificChatRoomUserUser()
      throws ExecutionException, InterruptedException, TimeoutException {
    String roomName1 = "room1";
    String roomName2 = "room2";
    String user1 = "user1";
    String user11 = "user11";
    String user2 = "user2";
    chatService.createChatRoom(roomName1);
    chatService.createChatRoom(roomName2);
    chatService.joinChatRoom(user1, roomName1);
    chatService.joinChatRoom(user11, roomName1);
    chatService.joinChatRoom(user2, roomName2);

    StompSession stompSession = webSocketStompClient
        .connect(getWsPath(), new StompSessionHandlerAdapter() {
        })
        .get(10, SECONDS);
    BlockingQueue<Message> room1MessageList = new ArrayBlockingQueue<>(3);
    BlockingQueue<Message> room2MessageList = new ArrayBlockingQueue<>(1);
    //listen room1 messages
    stompSession.subscribe("/secured/chatroom/" + roomName1 + "/message",
        new BroadcastMessageStompFrameHandler(room1MessageList));
    //listen room2 messages
    stompSession.subscribe("/secured/chatroom/" + roomName2 + "/message",
        new BroadcastMessageStompFrameHandler(room2MessageList));

    stompSession.send("/app/chat/broadcast/" + roomName1,
        Message.builder().user(user1).content("Test message from user1").build());
    stompSession.send("/app/chat/broadcast/" + roomName1,
        Message.builder().user(user11).content("Test message from user11").build());


    // user2 is sending correct message to "room2"
    stompSession.send("/app/chat/broadcast/" + roomName2,
        Message.builder().user(user2).content("Test message from user2").build());

    await()
        .atMost(20, SECONDS)
        .untilAsserted(() -> {
          assertEquals(2, room1MessageList.size()); // user2 not boradcasted because it is in room2
          assertEquals(user1, room1MessageList.poll().getUser());
          assertEquals(user11, room1MessageList.poll().getUser());

          assertEquals(1, room2MessageList.size());
          assertEquals(user2, room2MessageList.poll().getUser());
        });
  }

  private class CreateChatStompFrameHandler implements StompFrameHandler {

    @Override
    public Type getPayloadType(StompHeaders stompHeaders) {
      return List.class;
    }

    @Override
    public void handleFrame(StompHeaders stompHeaders, Object o) {
      completableFutureCreateRoom.complete((List<String>) o);
    }
  }

  private class BroadcastMessageStompFrameHandler implements StompFrameHandler {

    final private BlockingQueue<Message> messageList;

    public BroadcastMessageStompFrameHandler(BlockingQueue<Message> messageList) {
      this.messageList = messageList;
    }

    @Override
    public Type getPayloadType(StompHeaders stompHeaders) {
      return Message.class;
    }

    @Override
    public void handleFrame(StompHeaders stompHeaders, Object o) {
      this.messageList.add((Message) o);
    }

  }

  private String getWsPath() {
    return String.format("ws://localhost:%d/ws", port);
  }
}
