package com.czy.api.domain.dto.base;

import com.czy.api.constant.netty.NettyResponseStatuesEnum;
import com.czy.api.converter.base.BaseResponseConverter;
import com.czy.api.domain.entity.event.Message;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author 13225
 * @date 2025/2/11 23:33
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class BaseResponseData extends BaseRequestData implements ToDataMap{
    // 响应结果
    public String code = NettyResponseStatuesEnum.SUCCESS.getCode();
    public String message = NettyResponseStatuesEnum.SUCCESS.getMessage();

    // 提供给Json的无参构造器
    public BaseResponseData(){

    }

    public BaseResponseData(BaseResponseData baseResponseData){
        this.setBaseResponseData(
                baseResponseData.getCode(),
                baseResponseData.getMessage(),
                baseResponseData.getSenderId(),
                baseResponseData.getReceiverId(),
                baseResponseData.getType(),
                baseResponseData.getTimestamp()
        );
    }

    // 通过已有的请求体设置响应体
    public void setBaseResponseData(BaseResponseData baseResponseData){
        this.setBaseResponseData(
                baseResponseData.getCode(),
                baseResponseData.getMessage(),
                baseResponseData.getSenderId(),
                baseResponseData.getReceiverId(),
                baseResponseData.getType(),
                baseResponseData.getTimestamp()
        );
    }

    public void setBaseResponseData(String code, String message,
                                    Long senderId, Long receiverId, String type, String timestamp){
        this.code = Optional.ofNullable(code).orElse("");
        this.message = Optional.ofNullable(message).orElse("");
        this.setSenderId(senderId);
        this.setReceiverId(receiverId);
        this.setType(type);
        this.setTimestamp(timestamp);
    }

    public void initResponseByRequest(@NotNull BaseRequestData request){
        this.setSenderId(request.getSenderId());
        this.setReceiverId(request.getReceiverId());
        // 转化在Push中转化
        this.setType(request.getType());
        this.setTimestamp(request.getTimestamp());
    }

    // 抽象方法，继承者都需要实现将自己的字段设置为dataMap，用于组装Message
    @JsonIgnore
    public Map<String, String> toDataMap(){
        return new HashMap<>();
    }

    // 导出为Message的方法，需要JsonIgnore，否则会序列化错误
    @JsonIgnore
    public Message getMessageByResponse() {
        Message message = BaseResponseConverter.INSTANCE.getMessage(this);
        Map<String, String> data = toDataMap();
        // 设置非空的data，如果data中存在空，会造成rabbitMq反序列化失败，从而消息堆积
        if (data != null){
            Map<String, String> notNullData = data.entrySet().stream()
                    .filter(entry -> entry.getValue() != null) // 过滤掉值为 null 的条目
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            message.getData().putAll(notNullData);
        }
        return message;
    }
}
