package com.genesys.chat.service;

import com.genesys.chat.model.Message;
import com.genesys.chat.model.Notify;
import com.genesys.chat.model.Notify.NotificationType;
import com.genesys.chat.model.StoredMessage;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChatServiceTest {

  private ChatService chatService = new ChatService();

  @BeforeEach
  public void before() {
    StoredMessage.chatRoomData.clear();
  }

  @Test
  public void testCreateChatRoomWhenUniqueRoomName() {
    final List<String> rooms = chatService.createChatRoom("room1");

    assertEquals(rooms.size(), 1);
    assertEquals(rooms.get(0), "room1");

  }

  @Test
  public void testCreateChatRoomWhenDuplicateRoomName() {
    final String expectedMessage = "Invalid or duplicate chatname is not allowed.";
    final String roomName = "room1";
    StoredMessage.chatRoomData.put("room1", new ConcurrentHashMap<>());

    final Exception exception = assertThrows(RuntimeException.class, () -> chatService.createChatRoom(roomName));

    assertTrue(exception instanceof IllegalArgumentException);
    assertTrue(exception.getMessage().contains(expectedMessage));
  }


  @Test
  public void testCreateChatRoomWhenRoomNameIsNull() {
    final String expectedMessage = "Invalid or duplicate chatname is not allowed.";
    final Exception exception = assertThrows(Exception.class, () -> chatService.createChatRoom(null));

    assertTrue(exception instanceof IllegalArgumentException);
    assertTrue(exception.getMessage().contains(expectedMessage));
  }

  @Test
  public void testJoinChatRoomWhenValidRoomAndUserName() {
    final String user = "user1";
    final String room = "room1";
    StoredMessage.chatRoomData.put("room1", new HashMap<>());

    final Notify notify = chatService.joinChatRoom(user, "room1");

    assertEquals(user, notify.getUser());
    assertEquals(NotificationType.JOIN, notify.getType());
  }


  @Test
  public void testJoinChatRoomWhenDuplicateUserName() {
    final String expectedMessage = "Invalid chat room or user name.";
    final String user = "user1";
    final String room = "room1";

    final Exception exception = assertThrows(Exception.class, () -> chatService.joinChatRoom(user, room));

    assertTrue(exception instanceof IllegalArgumentException);
    assertTrue(exception.getMessage().contains(expectedMessage));
  }

  @Test
  public void testJoinChatRoomWhenInValidRoomName() {
    final String user = "user1";
    final String room = "room1";
    addUser(user, room);

    final Notify notify = chatService.joinChatRoom(user, "room1");

    assertEquals(notify, null);
  }

  @Test
  public void testBroadcastMessageWhenValidRoomNameAndUserName() throws IllegalAccessException {
    final String user = "user1";
    final String room = "room1";
    final String content = "This is an assignment.";
    Message chatMessage = Message.builder().user(user).content(content).build();
    addUser(user, room);

    Message expectedMessage = chatService.broadcastMessage(chatMessage, room);

    assertEquals(expectedMessage.getUser(), user);
    assertEquals(expectedMessage.getContent(), content);
    assertTrue(expectedMessage.getDate() != null);
  }


  private void addUser(String user, String room) {
    Map<String, LinkedList<Message>> users = new HashMap<>();
    users.put(user, new LinkedList<>());
    StoredMessage.chatRoomData.put(room, users);
  }

}
