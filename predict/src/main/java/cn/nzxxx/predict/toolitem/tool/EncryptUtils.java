package cn.nzxxx.predict.toolitem.tool;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import sun.misc.BASE64Decoder;

import java.awt.*;

/**
 * AES算法进行加密
 *
 **/
public class EncryptUtils {

    /**
     * 密钥
     */
    private static final String KEY = "1266567997661820";// AES加密要求key必须要128个比特位（这里需要长度为16，否则会报错）

    /**
     * 算法
     */
    private static final String ALGORITHMSTR = "AES/ECB/PKCS5Padding";

    /*public static void main(String[] args) throws Exception {
        String content = "abcdefg风格";
        System.out.println("加密前：" + content+"  加密密钥和解密密钥：" + KEY);

        String encrypt = aesEncrypt(content);
        System.out.println("加密后：" + encrypt);

        String decrypt = aesDecrypt(encrypt);
        System.out.println("解密后：" + decrypt);
    }*/

    /**
     * AES加密
     * @param content 待加密的内容
     * @return 加密后的byte[]
     */
    private static byte[] aesEncryptToBytes(String content) throws Exception {
        if(content==null){
            return null;
        }
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);
        Cipher cipher = Cipher.getInstance(ALGORITHMSTR);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(KEY.getBytes(), "AES"));

        return cipher.doFinal(content.getBytes("utf-8"));
    }


    /**
     * AES加密为base 64 code
     *
     * @param content 待加密的内容
     * @return 加密后的base 64 code
     */
    public static String aesEncrypt(String content) throws Exception {
        return Helper.byteToBase64(aesEncryptToBytes(content));
    }

    /**
     * AES解密
     *
     * @param encryptBytes 待解密的byte[]
     * @return 解密后的String
     */
    private static String aesDecryptByBytes(byte[] encryptBytes) throws Exception {
        if(encryptBytes==null){
            return null;
        }
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);

        Cipher cipher = Cipher.getInstance(ALGORITHMSTR);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(KEY.getBytes(), "AES"));
        byte[] decryptBytes = cipher.doFinal(encryptBytes);

        return new String(decryptBytes);
    }


    /**
     * 将base 64 code AES解密
     *
     * @param encryptStr 待解密的base 64 code
     * @return 解密后的string
     */
    public static String aesDecrypt(String encryptStr) throws Exception {
        return aesDecryptByBytes(Helper.base64ToByte(encryptStr));
    }

}