package com.genesys.chat.model;


import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class StoredMessage {

  public static Set<String> typingUserList = new HashSet<>();
  public static Map<String, Map<String, LinkedList<Message>>> chatRoomData = new HashMap<>();
}
