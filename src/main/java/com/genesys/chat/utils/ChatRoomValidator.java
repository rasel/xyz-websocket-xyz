package com.genesys.chat.utils;

import com.genesys.chat.model.StoredMessage;

public class ChatRoomValidator {

  public static boolean isExistChatRoom(String roomName) {
    return roomName != null && StoredMessage.chatRoomData.containsKey(roomName);
  }

  public static boolean isExistUser(String user, String roomName) {
    return isExistChatRoom(roomName) && StoredMessage.chatRoomData.get(roomName).containsKey(user);
  }

}
