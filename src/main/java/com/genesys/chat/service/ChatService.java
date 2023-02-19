package com.genesys.chat.service;

import com.genesys.chat.model.Message;
import com.genesys.chat.model.Notify;
import com.genesys.chat.model.Notify.NotificationType;
import com.genesys.chat.model.StoredMessage;
import com.genesys.chat.utils.ChatRoomValidator;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChatService {

  /**
   * create chat room
   */
  public List<String> createChatRoom(String chatRoom) {
    if (chatRoom != null && !ChatRoomValidator.isExistChatRoom(chatRoom)) {
      StoredMessage.chatRoomData.put(chatRoom, new HashMap<>());
      log.info(String.format("New chat room has created: %s", chatRoom));
      return new ArrayList<>(StoredMessage.chatRoomData.keySet());
    }
    throw new IllegalArgumentException("Invalid or duplicate chatname is not allowed.");
  }

  /**
   * return all chat room names
   */
  public List<String> getAllChatRoom() {
    return new ArrayList<>(StoredMessage.chatRoomData.keySet());
  }

  /**
   * join in chat room
   */
  public Notify joinChatRoom(String user, String roomName) {
    if (ChatRoomValidator.isExistChatRoom(roomName) && user != null) {
      if (ChatRoomValidator.isExistUser(user, roomName)) {
        return null;
      }
      Map<String, LinkedList<Message>> chatData = StoredMessage.chatRoomData.get(roomName);
      if (chatData == null) {
        chatData = new HashMap<>();
        StoredMessage.chatRoomData.put(roomName, chatData);
      }
      chatData.put(user, new LinkedList<>());
      log.info(String.format("User '%s' has joined to chat room '%s'", user, roomName));
      return Notify.builder().user(user).type(NotificationType.JOIN).build();
    }
    throw new IllegalArgumentException("Invalid chat room or user name.");
  }

  public Message broadcastMessage(Message chatMessage, String roomName) throws IllegalAccessException {
    if (ChatRoomValidator.isExistChatRoom(roomName) && ChatRoomValidator.isExistUser(chatMessage.getUser(), roomName)) {
      chatMessage.setDate(new Date());
      Map<String, LinkedList<Message>> chatData = StoredMessage.chatRoomData.get(roomName);
      LinkedList<Message> messages = chatData.get(chatMessage.getUser());
      messages.add(chatMessage);
      log.info(String.format("%s has sent message to chatRoom %s : %s", chatMessage.getUser(), roomName, chatMessage));
      return chatMessage;
    } else {
      throw new IllegalAccessException(
          String.format("Invalid chat room(%s) or user name(%s).", roomName, chatMessage.getUser()));
    }
  }
}
