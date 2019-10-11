package com.scosyf.mqtt.integration.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.scosyf.mqtt.integration.constant.Constant;
import com.scosyf.mqtt.integration.constant.MsgTopicEnum;
import com.scosyf.mqtt.integration.constant.MsgTypeEnum;
import com.scosyf.mqtt.integration.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.mqtt.support.MqttHeaders;

import java.util.Map;

/**
 * @project: spring-integration
 * @author: kunbu
 * @create: 2019-10-09 17:38
 **/
public class MessageTransferUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageTransferUtil.class);

    /** ============== mqtt header ============= */

    public static final String QOS                      = "mqtt_qos";
    public static final String RECEIVED_QOS             = "mqtt_receivedQos";
    public static final String DUPLICATE                = "mqtt_duplicate";
    public static final String RETAINED                 = "mqtt_retained";
    public static final String RECEIVED_RETAINED        = "mqtt_receivedRetained";
    public static final String TOPIC                    = "mqtt_topic";
    public static final String RECEIVED_TOPIC           = "mqtt_receivedTopic";


    /**
     * 转换为系统消息模型
     *
     **/
    public static SysMessage mqttMessage2SysMessage(String payload, Map<String, Object> headers) {
        LOGGER.info("received sys message, header:{}, payload:{}", headers, payload);
        // scosyf/sys/+/client/+/connected
        String topicStr = headers.get(MqttHeaders.RECEIVED_TOPIC).toString();
        String[] topicSplit = topicStr.split(Constant.TOPIC_SPLITTER);

        SysMessage sysMessage = new SysMessage();
        sysMessage.setMessage(payload);
        sysMessage.setTopic(topicStr);
        sysMessage.setTopicTokens(topicSplit);
        return sysMessage;
    }

    /**
     * 原始数据转换为业务消息模型
     *
     **/
    public static BizMessage mqttMessage2BizMessage(String payload, Map<String, Object> headers) {
        LOGGER.info("received biz message, header:{}, payload:{}", headers, payload);
        BizMessage bizMessage;
        try {
            // scosyf/biz/+/base/
            String topicStr = headers.get(MqttHeaders.RECEIVED_TOPIC).toString();
            String[] topicArr = topicStr.split(Constant.TOPIC_SPLITTER);
            int topicLength = topicArr.length;
            // + 为通配符
            String topicId = topicArr[topicLength - 2];
            String topicType = topicArr[topicLength - 1];
            MsgTopicEnum msgTopic = MsgTopicEnum.valueOf(topicType.toUpperCase());

            bizMessage = payload2BizMessage(payload);
            bizMessage.setTopicId(topicId);
            bizMessage.setMsgTopicEnum(msgTopic);
        } catch (Exception e) {
            LOGGER.error(">>> mqttMessage2BizMessage 转业务数据失败.", e);
            bizMessage = new BizMessage();
            bizMessage.setMsgTypeEnum(MsgTypeEnum.NA);
        }
        return bizMessage;
    }

    private static BizMessage payload2BizMessage(String payload) {
        JSONObject payloadJson = JSON.parseObject(payload);
        String type = payloadJson.getString(Constant.PAYLOAD_MSG_TYPE).toUpperCase();
        MsgTypeEnum msgType = MsgTypeEnum.valueOf(type);
        BizMessage bizMessage;
        switch (msgType) {
            case BOOK:
                bizMessage = payloadJson.toJavaObject(BookBizMessage.class);
                break;
            case IMAGE:
                bizMessage = payloadJson.toJavaObject(ImageBizMessage.class);
                break;
            case MUSIC:
                bizMessage = payloadJson.toJavaObject(MusicBizMessage.class);
                break;
            case MOVIE:
                bizMessage = payloadJson.toJavaObject(MovieBizMessage.class);
                break;
            default:
                bizMessage = payloadJson.toJavaObject(BizMessage.class);
                break;
        }
        bizMessage.setMsgTypeEnum(msgType);
        return bizMessage;
    }



}