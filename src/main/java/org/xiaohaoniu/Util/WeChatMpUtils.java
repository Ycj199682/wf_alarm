package org.xiaohaoniu.Util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信公众平台接口工具类
 */
@Component
public class WeChatMpUtils {
    private static final Logger logger = LoggerFactory.getLogger(WeChatMpUtils.class);

    // 从配置文件中注入微信公众平台相关配置
    @Value("${wechat.mp.appId}")
    private String appId;

    @Value("${wechat.mp.appSecret}")
    private String appSecret;

    @Value("${wechat.mp.accessTokenUrl}")
    private String accessTokenUrl;

    @Value("${wechat.mp.templateMsgUrl}")
    private String templateMsgUrl;

    private final RestTemplate restTemplate;

    // 使用Spring的依赖注入
    public WeChatMpUtils(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 获取微信公众平台的access_token
     * @return access_token字符串，获取失败返回null
     */
    public String getAccessToken() {
        try {
            // 构建请求URL
            String url = String.format("%s?grant_type=client_credential&appid=%s&secret=%s", 
                    accessTokenUrl, appId, appSecret);

            // 发送GET请求
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String responseBody = response.getBody();

            // 解析响应结果
            JSONObject jsonObject = JSON.parseObject(responseBody);
            if (jsonObject.containsKey("access_token")) {
                return jsonObject.getString("access_token");
            } else {
                logger.error("获取access_token失败: {}", responseBody);
                return null;
            }
        } catch (Exception e) {
            logger.error("获取access_token异常", e);
            return null;
        }
    }

    /**
     * 推送订阅模板消息
     * @param accessToken 接口调用凭证
     * @param openId 接收者的openid
     * @param templateId 模板ID
     * @param page 点击模板消息跳转的页面
     * @param data 模板内容数据
     * @return 是否发送成功
     */
    public boolean sendSubscribeTemplateMsg(String accessToken, String openId, String templateId, 
                                          String page, Map<String, Object> data) {
        if (StringUtils.isAnyBlank(accessToken, openId, templateId)) {
            logger.error("推送模板消息参数缺失");
            return false;
        }

        try {
            // 构建请求URL
            String url = String.format("%s?access_token=%s", templateMsgUrl, accessToken);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("touser", openId);
            requestBody.put("template_id", templateId);
            if (StringUtils.isNotBlank(page)) {
                requestBody.put("page", page);
            }
            requestBody.put("data", data);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 发送POST请求
            HttpEntity<String> request = new HttpEntity<>(JSON.toJSONString(requestBody), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            String responseBody = response.getBody();

            // 解析响应结果
            JSONObject jsonObject = JSON.parseObject(responseBody);
            if (jsonObject.getInteger("errcode") == 0) {
                return true;
            } else {
                logger.error("推送模板消息失败: {}", responseBody);
                return false;
            }
        } catch (Exception e) {
            logger.error("推送模板消息异常", e);
            }
        return false;
    }

    /**
     * 推送订阅模板消息（简化版）
     * @param openId 接收者的openid
     * @param templateId 模板ID
     * @param data 模板内容数据
     * @return 是否发送成功
     */
    public boolean sendSubscribeTemplateMsg(String openId, String templateId, Map<String, Object> data) {
        String accessToken = getAccessToken();
        if (accessToken == null) {
            return false;
        }
        return sendSubscribeTemplateMsg(accessToken, openId, templateId, null, data);
    }
}