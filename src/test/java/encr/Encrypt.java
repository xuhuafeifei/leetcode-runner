package encr;

import org.junit.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class Encrypt {
    @Test
    public void encryptAES() throws Exception {
        String plainText = "需要加密的内容";
        String secretKey = "this is password";
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        String encryptedText =  Base64.getEncoder().encodeToString(encryptedBytes);
        System.out.println("AES加密后的内容=" + encryptedText);

        System.out.println("-------------------------------------------");
        // 解密
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec("this ia password".getBytes(StandardCharsets.UTF_8), "AES"));
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        String decryptedText = new String(decryptedBytes, StandardCharsets.UTF_8);
        System.out.println("AES解密后的内容=" + decryptedText);
    }

}
