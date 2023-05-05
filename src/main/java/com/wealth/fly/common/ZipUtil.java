package com.wealth.fly.common;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author : lisong
 * @date : 2023/5/5
 */
public class ZipUtil {

    public static void zip(String sourceFolder, String destFile, String password) throws ZipException {
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setEncryptFiles(true);
        zipParameters.setEncryptionMethod(EncryptionMethod.AES);
        // Below line is optional. AES 256 is used by default. You can override it to use AES 128. AES 192 is supported only for extracting.
        zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);

        new ZipFile(destFile, password.toCharArray())
                .addFolder(new File(sourceFolder), zipParameters);
    }

    public static void main(String[] args) throws ZipException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        zip("/Users/lisong/code/fly", "/Users/lisong/Downloads/fly-"+sdf.format(new Date())+".zip", "lisong123");
    }
}
