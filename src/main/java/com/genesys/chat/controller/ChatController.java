package com.genesys.chat.controller;

import com.genesys.chat.model.Message;
import com.genesys.chat.model.Notify;
import com.genesys.chat.model.Notify.NotificationType;
import com.genesys.chat.model.StoredMessage;
import com.genesys.chat.service.ChatService;
import com.genesys.chat.utils.ChatRoomValidator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class ChatController {

  @Autowired
  private ChatService chatService;
  @Autowired
  private SimpMessagingTemplate simpMessagingTemplate;

  @MessageMapping("/chat/createRoom/{roomName}")
  @SendTo("/topic/public")
  public List<String> createRoom(@DestinationVariable("roomName") String roomName) {
    return chatService.createChatRoom(roomName);
  }

  @MessageMapping("/chat/allRoom")
  @SendTo("/topic/public")
  public List<String> allRoom() {
    return chatService.getAllChatRoom();
  }

  /**
   * Message send to : /chat/typing/{user}/{roomName}
   *
   * subscribe : /secured/chatroom/{roomName}/notify
   *
   * Notify(JOIN)
   */
  @MessageMapping("/chat/join/{user}/{roomName}")
  public void joinChatRoom(@DestinationVariable String user, @DestinationVariable String roomName) {
    Notify notify = chatService.joinChatRoom(user, roomName);
    if (notify != null) {
      simpMessagingTemplate.convertAndSendToUser(roomName, "/notify", notify);
    }
  }

  /**
   * Message send to : /chat/typing/{user}/{roomName}
   *
   * subscribe : /secured/chatroom/{roomName}/notify
   *
   * Notify(TYPING)
   */
  @MessageMapping("/chat/typing/{user}/{roomName}")
  public void userTyping(@DestinationVariable String user, @DestinationVariable String roomName)
      throws IllegalAccessException, InterruptedException {
    if (ChatRoomValidator.isExistChatRoom(roomName) && ChatRoomValidator.isExistUser(user, roomName)) {
      StoredMessage.typingUserList.add(user); // add a user when typing
      TimeUnit.SECONDS.sleep(2);
      log.info(String.format("Currently typing users : %s", StoredMessage.typingUserList.toString()));
      simpMessagingTemplate.convertAndSendToUser(roomName, "/notify",
          Notify.builder()
              .typingUserList(StoredMessage.typingUserList.toString())
              .type(NotificationType.TYPING)
              .build());
      StoredMessage.typingUserList.clear();
    } else {
      throw new IllegalAccessException("UnAuthorized user to access the chat room.");
    }
  }

  /**
   * broadcast message to specific room
   *
   * Message send to : /chat/broadcast/{roomName}
   *
   * subscribe : /secured/chatroom/{roomName}/message
   */
  @MessageMapping("/chat/broadcast/{roomName}")
  public void broadCastMessage(@Payload Message chatMessage, @DestinationVariable String roomName)
      throws IllegalAccessException {
    Message message = chatService.broadcastMessage(chatMessage, roomName);
    if (message != null) {
      StoredMessage.typingUserList.remove(
          chatMessage.getUser()); // remove the user if tying finsihed and broadcasted message
      simpMessagingTemplate.convertAndSendToUser(roomName, "/message", message);
    }
  }
}
