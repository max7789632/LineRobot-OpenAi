package com.linerobot.handler;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import com.linerobot.model.UserProfile;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@Slf4j
@LineMessageHandler
public class Handler {

    @Value("${line.bot.channel-token}")
    private String token;

    @Value("${openai.token}")
    private String openAiToken;

    @Value("${openai.model}")
    private String completionModel;

    @EventMapping
    public Message handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws IOException, URISyntaxException {
        System.out.println("event: " + event);

        // 取得user資訊
        String userId = event.getSource().getUserId();
        UserProfile userProfile = new UserProfile();
        LineMessagingClient client = LineMessagingClient.builder(token).build();

        CompletableFuture<UserProfileResponse> future = client.getProfile(userId)
                .thenApply(profile -> {
                    userProfile.setUserId(profile.getUserId());
                    userProfile.setDisplayName(profile.getDisplayName());
                    userProfile.setPictureUrl(String.valueOf(profile.getPictureUrl()));
                    userProfile.setLanguage(profile.getLanguage());
                    return profile;
                })
                .exceptionally(err -> {
                    // error handling
                    return null;
                });

        future.join(); // 等待非同步操作完成

        System.out.println("使用者帳號: " + userProfile.getUserId());
        System.out.println("使用者暱稱: " + userProfile.getDisplayName());
        System.out.println("使用者照片: " + userProfile.getPictureUrl());
        System.out.println("使用者語言: " + userProfile.getLanguage());

        // 取得傳入訊息
        String userMessage = event.getMessage().getText();
        System.out.println("輸入文字: " + userMessage);

        // 設定OpenAi Token, timeout 60秒
        OpenAiService service = new OpenAiService(openAiToken, Duration.ofSeconds(60));

        List<ChatMessage> chatMessageList = new ArrayList<>();
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRole("user");
        chatMessage.setContent("請使用繁體中文回答我 " + userMessage);
        chatMessageList.add(chatMessage);

        // 設定 模組, 傳入訊息
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .model(completionModel)
                .messages(chatMessageList)
                .build();

        ChatCompletionResult completionResult = service.createChatCompletion(completionRequest);
        List<ChatCompletionChoice> choices = completionResult.getChoices();

        System.out.println("輸出文字: " + choices.get(0).getMessage().getContent());

        return new TextMessage(choices.get(0).getMessage().getContent());
    }
}