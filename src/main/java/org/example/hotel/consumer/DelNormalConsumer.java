package org.example.hotel.consumer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import javax.annotation.Resource;
import org.apache.rocketmq.client.annotation.RocketMQMessageListener;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.apache.rocketmq.client.core.RocketMQListener;
import org.example.hotel.service.impl.HotelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @Author: Rimecoxu@gmai.com
 * @CreateTime: 2025-09-04 18:14
 * @Description: 消费普通消息
 */
@Service
@RocketMQMessageListener(consumerGroup = "spring-boot-push-consumer-normal", topic = "spring-boot-normal-topic", tag = "del")
public class DelNormalConsumer implements RocketMQListener {

    Logger logger = LoggerFactory.getLogger(DelNormalConsumer.class);

    @Resource
    private HotelService hotelService;

    @Override
    public ConsumeResult consume(MessageView messageView) {
        logger.info("del received message:{}", messageView);
        // 获取消息体 ByteBuffer
        ByteBuffer byteBuffer = messageView.getBody();

        // 将 ByteBuffer 转换为字节数组
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);

        // 将字节数组转换为字符串（假设使用UTF-8编码）
        String messageBody = new String(bytes, StandardCharsets.UTF_8);

        logger.info("Received message - ID: {}, Body: {}", messageView.getMessageId(), messageBody);

        // 处理消息
        hotelService.del(Long.parseLong(messageBody));

        // 处理成功
        return ConsumeResult.SUCCESS;
    }
}
