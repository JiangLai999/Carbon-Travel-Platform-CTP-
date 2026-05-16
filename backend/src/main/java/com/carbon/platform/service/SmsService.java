package com.carbon.platform.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SmsService {

    // 存储验证码：phone -> {code, expireTime}
    private final Map<String, String[]> codeStore = new ConcurrentHashMap<>();
    private static final long EXPIRE_MS = 60_000; // 60秒有效

    public String sendCode(String phone) {
        String code = String.format("%06d", new Random().nextInt(1000000));
        long expireAt = System.currentTimeMillis() + EXPIRE_MS;
        codeStore.put(phone, new String[]{code, String.valueOf(expireAt)});
        System.out.println("[验证码] " + phone + " -> " + code);
        return code;
    }

    public boolean verifyCode(String phone, String code) {
        String[] entry = codeStore.get(phone);
        if (entry == null) return false;
        if (System.currentTimeMillis() > Long.parseLong(entry[1])) {
            codeStore.remove(phone);
            return false;
        }
        boolean match = entry[0].equals(code.trim());
        if (match) codeStore.remove(phone);
        return match;
    }
}
