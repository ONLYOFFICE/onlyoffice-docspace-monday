/**
 * (c) Copyright Ascensio System SIA 2025
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.onlyoffice.common.service.encryption;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AesEncryptionService implements EncryptionService {
  @Value("${spring.security.oauth2.client.registration.monday.encryptionSecret}")
  private String secret;

  private static final String ALGORITHM = "AES/GCM/NoPadding";
  private static final String FACTORY_INSTANCE = "PBKDF2WithHmacSHA256";
  private static final int TAG_LENGTH_BIT = 128;
  private static final int IV_LENGTH_BYTE = 12;
  private static final int SALT_LENGTH_BYTE = 16;
  private static final String ALGORITHM_TYPE = "AES";
  private static final int KEY_LENGTH = 128;
  private static final int ITERATION_COUNT = 1200; // Min 1000
  private static final Charset UTF_8 = StandardCharsets.UTF_8;

  private byte[] getRandomNonce(int length) {
    var nonce = new byte[length];
    new SecureRandom().nextBytes(nonce);
    return nonce;
  }

  private SecretKey getSecretKey(String password, byte[] salt)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    var spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
    var factory = SecretKeyFactory.getInstance(FACTORY_INSTANCE);
    return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), ALGORITHM_TYPE);
  }

  private Cipher initCipher(int mode, SecretKey secretKey, byte[] iv)
      throws InvalidKeyException,
          InvalidAlgorithmParameterException,
          NoSuchPaddingException,
          NoSuchAlgorithmException {
    var cipher = Cipher.getInstance(ALGORITHM);
    cipher.init(mode, secretKey, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
    return cipher;
  }

  public String encrypt(String plainText) throws EncryptionException {
    try {
      var salt = getRandomNonce(SALT_LENGTH_BYTE);
      var secretKey = getSecretKey(secret, salt);
      var iv = getRandomNonce(IV_LENGTH_BYTE);
      var cipher = initCipher(Cipher.ENCRYPT_MODE, secretKey, iv);
      var encryptedMessageByte = cipher.doFinal(plainText.getBytes(UTF_8));
      var cipherByte =
          ByteBuffer.allocate(iv.length + salt.length + encryptedMessageByte.length)
              .put(iv)
              .put(salt)
              .put(encryptedMessageByte)
              .array();

      return Base64.getEncoder().encodeToString(cipherByte);
    } catch (Exception e) {
      throw new EncryptionException(e);
    }
  }

  public String decrypt(String cipherText) throws DecryptionException {
    try {
      var decodedCipherByte = Base64.getDecoder().decode(cipherText.getBytes(UTF_8));
      var byteBuffer = ByteBuffer.wrap(decodedCipherByte);

      var iv = new byte[IV_LENGTH_BYTE];
      byteBuffer.get(iv);

      var salt = new byte[SALT_LENGTH_BYTE];
      byteBuffer.get(salt);

      var encryptedByte = new byte[byteBuffer.remaining()];
      byteBuffer.get(encryptedByte);

      var secretKey = getSecretKey(secret, salt);
      var cipher = initCipher(Cipher.DECRYPT_MODE, secretKey, iv);

      var decryptedMessageByte = cipher.doFinal(encryptedByte);

      return new String(decryptedMessageByte, UTF_8);
    } catch (Exception e) {
      throw new DecryptionException(e);
    }
  }
}
