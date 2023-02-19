package com.genesys.chat;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebSocketChatApplication {

  public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
    SpringApplication.run(WebSocketChatApplication.class, args);
    ChatClientTestShouldNotBeInProductionCode.runDummyChatClient();
  }
}
