package com.czy.message.handler.eventListener;

import com.czy.api.domain.entity.event.Message;
import com.czy.api.domain.entity.event.event.MessageRouteEvent;

import com.czy.message.component.MessageEventManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author 13225
 * @date 2025/4/2 14:57
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class MessageRouteListener implements ApplicationListener<MessageRouteEvent> {

//    @Lazy
    private final MessageEventManager<Message> messageMessageEventManager;


//    @EventListener // 继承了ApplicationListener就不需要@EventListener
    @Override
    public void onApplicationEvent(@NotNull MessageRouteEvent event) {
        Message message = event.getSource();
        // 路由处理消息
        messageMessageEventManager.process(message);
    }
}
