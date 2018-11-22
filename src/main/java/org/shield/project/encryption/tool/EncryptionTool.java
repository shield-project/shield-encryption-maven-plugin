package org.shield.project.encryption.tool;

import org.shield.project.encryption.config.EncryptUtil;
import org.shield.project.encryption.config.maven.EncryptEnum;

public class EncryptionTool {
    public static String encryption(String password,String value, EncryptEnum encryptEnum){
        return EncryptUtil.encrytion(password,value,encryptEnum);
    }
}
