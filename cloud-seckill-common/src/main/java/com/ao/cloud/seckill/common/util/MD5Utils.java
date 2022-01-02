package com.ao.cloud.seckill.common.util;


import org.apache.tomcat.util.codec.binary.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 加MD5工具
 */

public class MD5Utils {
    public static  String getMDStr(String strValue) throws NoSuchAlgorithmException{
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        return Base64.encodeBase64String((md5.digest(strValue.getBytes())));
    }

}
