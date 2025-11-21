package com.yuehai.util.util;


import android.os.Build;
import android.text.TextUtils;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SignatureNewUtil {
    //sign签名算法
    //对参数按照key=value的格式, 并按照参数名ASCII字典序逆向排序, 再拼接&密钥, 最后再进行md5
    //md5(uid=9988469&token=1bd21ec13dd86f97cbe1dfb4fe4ad983&kes=1730252168673&密钥)




    // 对所有传入参数按照字段名的 ASCII 码从小到大排序（字典序）
    public static String getSign(Map<String, Object> map) {
        String result = "";
        try {
            List<Map.Entry<String, Object>> parameters= new ArrayList<Map.Entry<String, Object>>(map.entrySet());
            // 对所有传入参数按照字段名的 ASCII 码从小到大排序（字典序）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                parameters.sort(new Comparator<Map.Entry<String, Object>>() {
                    public int compare(Map.Entry<String, Object> o1, Map.Entry<String, Object> o2) {
                        return (o2.getKey()).compareTo(o1.getKey());
                    }
                });
            }
            // 构造签名键值对的格式
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Object> item : parameters) {
                if (item.getKey() != null || item.getKey() != "") {
                    String key = item.getKey();
                    Object val = item.getValue();
                    if (!(Objects.equals(val, "") || val == null)) {
                        sb.append(key).append("=").append(val).append("&");
                    }
                }
            }
            result = sb.toString();
        } catch (Exception e) {
            return null;
        }
        return result;

    }




    public static String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }



}
