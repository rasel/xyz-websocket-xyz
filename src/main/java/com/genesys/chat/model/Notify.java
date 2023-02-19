package com.genesys.chat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notify {

  private String user;
  private String typingUserList;
  private NotificationType type;

  public enum NotificationType {
    JOIN,
    TYPING
  }
}
