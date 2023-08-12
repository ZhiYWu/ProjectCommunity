package com.nowcoder.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.Map;
import java.util.UUID;

public class CommunityUtil {

    // 生成随机字符串
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    // MD5加密
    public static String md5(String key) {
        if (StringUtils.isBlank(key)) {
            // 如果key为空就返回null
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    // 将JavaBean序列化成JSON
    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if (map != null) {
            for (String key : map.keySet()) {
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();
    }

    // 重载方法
    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    // 重载方法
    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }
}
