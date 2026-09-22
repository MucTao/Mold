@file:Suppress("unused")

package org.muc.mold.utils.util

import android.os.Build
import org.muc.mold.utils.util.ConvertUtils.md2
import org.muc.mold.utils.util.ConvertUtils.md5
import org.muc.mold.utils.util.ConvertUtils.sha1
import org.muc.mold.utils.util.ConvertUtils.sha224
import org.muc.mold.utils.util.ConvertUtils.sha256
import org.muc.mold.utils.util.ConvertUtils.sha384
import org.muc.mold.utils.util.ConvertUtils.sha512
import org.muc.mold.utils.util.ConvertUtils.toHex
import java.io.File
import java.io.FileInputStream
import java.security.DigestInputStream
import java.security.Key
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.spec.AlgorithmParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Locale.getDefault
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2016/08/02
 * desc  : utils about encrypt
 * </pre> *
 */
object EncryptUtils {
    /**/
    /////////////////////////////////////////////////////////////////////// */ // hash encryption
    /**
     * Return the hex string of MD2 encryption.
     *
     * @param data The data . @ return the hex string of MD2 encryption
     */
    fun encryptMD2ToString(data: String) = data.toByteArray().md2()

    /**
     * Return the hex string of MD2 encryption.
     *
     * @param data The data.
     * @return the hex string of MD2 encryption
     */
    fun encryptMD2ToString(data: ByteArray) = data.md2()


    /**
     * Return the hex string of MD5 encryption.
     *
     * @param data The data.
     * @return the hex string of MD5 encryption
     */
    fun encryptMD5ToString(data: String) = data.toByteArray().md5()

    /**
     * Return the hex string of MD5 encryption.
     *
     * @param data The data.
     * @param salt The salt.
     * @return the hex string of MD5 encryption
     */
    fun encryptMD5ToString(data: String?, salt: String?): Result<String> {
        if (salt == null && data != null)
            return data.toByteArray().md5()
        if (data == null && salt != null)
            return salt.toByteArray().md5()
        return (data + salt).toByteArray().md5()
    }


    /**
     * Return the hex string of MD5 encryption.
     *
     * @param data The data.
     * @param salt The salt.
     * @return the hex string of MD5 encryption
     */
    fun encryptMD5ToString(data: ByteArray?, salt: ByteArray?): Result<String> = if (data != null) {
        if (salt != null) {
            val dataSalt = ByteArray(data.size + salt.size)
            System.arraycopy(data, 0, dataSalt, 0, data.size)
            System.arraycopy(salt, 0, dataSalt, data.size, salt.size)
            dataSalt.md5()
        } else {
            data.md5()
        }
    } else salt?.md5() ?: Result.failure(NullPointerException())


    /**
     * Return the hex string of file's MD5 encryption.
     *
     * @param filePath The path of file.
     * @return the hex string of file's MD5 encryption
     */
    fun encryptMD5File2String(filePath: String?): String {
        val file: File? = if (filePath.isNullOrBlank()) null else File(filePath)
        return encryptMD5File2String(file) ?: ""
    }

    /**
     * Return the bytes of file's MD5 encryption.
     *
     * @param filePath The path of file.
     * @return the bytes of file's MD5 encryption
     */
    fun encryptMD5File(filePath: String?): ByteArray? {
        val file: File? = if (filePath.isNullOrBlank()) null else File(filePath)
        return encryptMD5File(file)
    }

    /**
     * Return the hex string of file's MD5 encryption.
     *
     * @param file The file.
     * @return the hex string of file's MD5 encryption
     */
    fun encryptMD5File2String(file: File?): String? = encryptMD5File(file)?.toHex()

    /**
     * Return the bytes of file's MD5 encryption.
     *
     * @param file The file.
     * @return the bytes of file's MD5 encryption
     */
    fun encryptMD5File(file: File?): ByteArray? {
        if (file == null) return null
        return runCatching {
            FileInputStream(file).use { fis ->
                val md: MessageDigest = MessageDigest.getInstance("MD5")
                DigestInputStream(fis, md).use { digestInputStream ->
                    val buffer = ByteArray(256 * 1024)
                    while (digestInputStream.read(buffer) != -1) {
                        // Reading updates the digest.
                    }
                    md.digest()
                }
            }
        }.onFailure { error ->
            error.printStackTrace()
        }.getOrDefault(null)
    }

    /**
     * Return the hex string of SHA1 encryption.
     *
     * @param data The data.
     * @return the hex string of SHA1 encryption
     */
    fun encryptSHA1ToString(data: String) = data.toByteArray().sha1()

    /**
     * Return the hex string of SHA224 encryption.
     *
     * @param data The data.
     * @return the hex string of SHA224 encryption
     */
    fun encryptSHA224ToString(data: String) = data.toByteArray().sha224()

    /**
     * Return the hex string of SHA256 encryption.
     *
     * @param data The data.
     * @return the hex string of SHA256 encryption
     */
    fun encryptSHA256ToString(data: String) = data.toByteArray().sha256()


    /**
     * Return the hex string of SHA384 encryption.
     *
     * @param data The data.
     * @return the hex string of SHA384 encryption
     */
    fun encryptSHA384ToString(data: String) = data.toByteArray().sha384()


    /**
     * Return the hex string of SHA512 encryption.
     *
     * @param data The data.
     * @return the hex string of SHA512 encryption
     */
    fun encryptSHA512ToString(data: String) = data.toByteArray().sha512()


    /**/
    /////////////////////////////////////////////////////////////////////// */ // hmac encryption
    /**
     * Return the hex string of HmacMD5 encryption.
     *
     * @param data The data .
     * @param key The key. @ return the hex string of HmacMD5 encryption
     */
    fun encryptHmacMD5ToString(data: String?, key: String?): String {
        if (data.isNullOrEmpty() || key.isNullOrEmpty()) return ""
        return encryptHmacMD5ToString(data.toByteArray(), key.toByteArray())
    }

    /**
     * Return the hex string of HmacMD5 encryption.
     *
     * @param data The data.
     * @param key The key.
     * @return the hex string of HmacMD5 encryption
     */
    fun encryptHmacMD5ToString(data: ByteArray?, key: ByteArray?) = encryptHmacMD5(data, key)?.toHex() ?: ""


    /**
     * Return the bytes of HmacMD5 encryption.
     *
     * @param data The data.
     * @param key The key.
     * @return the bytes of HmacMD5 encryption
     */
    fun encryptHmacMD5(data: ByteArray?, key: ByteArray?): ByteArray? {
        return hmacTemplate(data, key, "HmacMD5")
    }

    /**
     * Return the hex string of HmacSHA1 encryption.
     *
     * @param data The data.
     * @param key The key.
     * @return the hex string of HmacSHA1 encryption
     */
    fun encryptHmacSHA1ToString(data: String?, key: String?): String {
        if (data.isNullOrEmpty() || key.isNullOrEmpty()) return ""
        return encryptHmacSHA1ToString(data.toByteArray(), key.toByteArray())
    }

    /**
     * Return the hex string of HmacSHA1 encryption.
     *
     * @param data The data.
     * @param key The key.
     * @return the hex string of HmacSHA1 encryption
     */
    fun encryptHmacSHA1ToString(data: ByteArray?, key: ByteArray?) = encryptHmacSHA1(data, key)?.toHex() ?: ""

    /**
     * Return the bytes of HmacSHA1 encryption.
     *
     * @param data The data.
     * @param key The key.
     * @return the bytes of HmacSHA1 encryption
     */
    fun encryptHmacSHA1(data: ByteArray?, key: ByteArray?): ByteArray? = hmacTemplate(data, key, "HmacSHA1")


    /**
     * Return the hex string of HmacSHA224 encryption.
     *
     * @param data The data.
     * @param key The key.
     * @return the hex string of HmacSHA224 encryption
     */
    fun encryptHmacSHA224ToString(data: String?, key: String?): String {
        if (data.isNullOrEmpty() || key.isNullOrEmpty()) return ""
        return encryptHmacSHA224ToString(data.toByteArray(), key.toByteArray())
    }

    /**
     * Return the hex string of HmacSHA224 encryption.
     *
     * @param data The data.
     * @param key The key.
     * @return the hex string of HmacSHA224 encryption
     */
    fun encryptHmacSHA224ToString(data: ByteArray?, key: ByteArray?): String = encryptHmacSHA224(data, key)?.toHex() ?: ""


    /**
     * Return the bytes of HmacSHA224 encryption.
     *
     * @param data The data.
     * @param key The key.
     * @return the bytes of HmacSHA224 encryption
     */
    fun encryptHmacSHA224(data: ByteArray?, key: ByteArray?): ByteArray? = hmacTemplate(data, key, "HmacSHA224")

    /**
     * Return the hex string of HmacSHA256 encryption.
     *
     * @param data The data.
     * @param key The key.
     * @return the hex string of HmacSHA256 encryption
     */
    fun encryptHmacSHA256ToString(data: String?, key: String?): String {
        if (data.isNullOrEmpty() || key.isNullOrEmpty()) return ""
        return encryptHmacSHA256ToString(data.toByteArray(), key.toByteArray())
    }

    /**
     * Return the hex string of HmacSHA256 encryption.
     *
     * @param data The data.
     * @param key The key.
     * @return the hex string of HmacSHA256 encryption
     */
    fun encryptHmacSHA256ToString(data: ByteArray?, key: ByteArray?): String = encryptHmacSHA256(data, key)?.toHex() ?: ""

    /**
     * Return the bytes of HmacSHA256 encryption.
     *
     * @param data The data.
     * @param key The key.
     * @return the bytes of HmacSHA256 encryption
     */
    fun encryptHmacSHA256(data: ByteArray?, key: ByteArray?): ByteArray? = hmacTemplate(data, key, "HmacSHA256")


    /**
     * Return the hex string of HmacSHA384 encryption.
     *
     * @param data The data.
     * @param key The key.
     * @return the hex string of HmacSHA384 encryption
     */
    fun encryptHmacSHA384ToString(data: String?, key: String?): String {
        if (data.isNullOrEmpty() || key.isNullOrEmpty()) return ""
        return encryptHmacSHA384ToString(data.toByteArray(), key.toByteArray())
    }

    /**
     * Return the hex string of HmacSHA384 encryption.
     *
     * @param data The data.
     * @param key The key.
     * @return the hex string of HmacSHA384 encryption
     */
    fun encryptHmacSHA384ToString(data: ByteArray?, key: ByteArray?): String = encryptHmacSHA384(data, key)?.toHex() ?: ""

    /**
     * Return the bytes of HmacSHA384 encryption.
     *
     * @param data The data.
     * @param key The key.
     * @return the bytes of HmacSHA384 encryption
     */
    fun encryptHmacSHA384(data: ByteArray?, key: ByteArray?): ByteArray? = hmacTemplate(data, key, "HmacSHA384")

    /**
     * Return the hex string of HmacSHA512 encryption.
     *
     * @param data The data.
     * @param key The key.
     * @return the hex string of HmacSHA512 encryption
     */
    fun encryptHmacSHA512ToString(data: String?, key: String?): String {
        if (data.isNullOrEmpty() || key.isNullOrEmpty()) return ""
        return encryptHmacSHA512ToString(data.toByteArray(), key.toByteArray())
    }

    /**
     * Return the hex string of HmacSHA512 encryption.
     *
     * @param data The data.
     * @param key The key.
     * @return the hex string of HmacSHA512 encryption
     */
    fun encryptHmacSHA512ToString(data: ByteArray?, key: ByteArray?): String = encryptHmacSHA512(data, key)?.toHex() ?: ""

    /**
     * Return the bytes of HmacSHA512 encryption.
     *
     * @param data The data.
     * @param key The key.
     * @return the bytes of HmacSHA512 encryption
     */
    fun encryptHmacSHA512(data: ByteArray?, key: ByteArray?): ByteArray? = hmacTemplate(data, key, "HmacSHA512")

    /**
     * Return the bytes of hmac encryption.
     *
     * @param data The data.
     * @param key The key.
     * @param algorithm The name of hmac encryption.
     * @return the bytes of hmac encryption
     */
    private fun hmacTemplate(
        data: ByteArray?,
        key: ByteArray?,
        algorithm: String?,
    ): ByteArray? {
        if (data == null || data.isEmpty() || key == null || key.isEmpty()) return null
        return runCatching {
            val secretKey = SecretKeySpec(key, algorithm)
            val mac: Mac = Mac.getInstance(algorithm)
            mac.init(secretKey)
            mac.doFinal(data)
        }.getOrElse { failure ->
            failure.printStackTrace()
            null
        }
    }

    /**/
    /////////////////////////////////////////////////////////////////////// */ // DES encryption
    /**
     * Return the Base64-encode bytes of DES encryption.
     *
     * @param data The data .
     * @param key The key.
     * @param transformation The name of the transformation, e.g., *DES/CBC/PKCS5Padding*.
     * @param iv The buffer with the IV. The contents of the buffer are copied to protect against
     *   subsequent modification. @ return the Base64-encode bytes of DES encryption
     */
    fun encryptDES2Base64(
        data: ByteArray?,
        key: ByteArray?,
        transformation: String?,
        iv: ByteArray?,
    ): ByteArray = EncodeUtils.base64Encode(encryptDES(data, key, transformation, iv))


    /**
     * Return the hex string of DES encryption.
     *
     * @param data The data.
     * @param key The key.
     * @param transformation The name of the transformation, e.g., *DES/CBC/PKCS5Padding*.
     * @param iv The buffer with the IV. The contents of the buffer are copied to protect against
     *   subsequent modification.
     * @return the hex string of DES encryption
     */
    fun encryptDES2HexString(
        data: ByteArray?,
        key: ByteArray?,
        transformation: String?,
        iv: ByteArray?,
    ): String = encryptDES(data, key, transformation, iv)?.toHex() ?: ""

    /**
     * Return the bytes of DES encryption.
     *
     * @param data The data.
     * @param key The key.
     * @param transformation The name of the transformation, e.g., *DES/CBC/PKCS5Padding*.
     * @param iv The buffer with the IV. The contents of the buffer are copied to protect against
     *   subsequent modification.
     * @return the bytes of DES encryption
     */
    fun encryptDES(
        data: ByteArray?,
        key: ByteArray?,
        transformation: String?,
        iv: ByteArray?,
    ): ByteArray? = symmetricTemplate(
        data,
        key,
        "DES",
        transformation,
        iv,
        true,
    )

    /**
     * Return the bytes of DES decryption for Base64-encode bytes.
     *
     * @param data The data.
     * @param key The key.
     * @param transformation The name of the transformation, e.g., *DES/CBC/PKCS5Padding*.
     * @param iv The buffer with the IV. The contents of the buffer are copied to protect against
     *   subsequent modification.
     * @return the bytes of DES decryption for Base64-encode bytes
     */
    fun decryptBase64DES(
        data: ByteArray?,
        key: ByteArray?,
        transformation: String?,
        iv: ByteArray?,
    ): ByteArray? = decryptDES(
        EncodeUtils.base64Decode(data),
        key,
        transformation,
        iv,
    )

    /**
     * Return the bytes of DES decryption for hex string.
     *
     * @param data The data.
     * @param key The key.
     * @param transformation The name of the transformation, e.g., *DES/CBC/PKCS5Padding*.
     * @param iv The buffer with the IV. The contents of the buffer are copied to protect against
     *   subsequent modification.
     * @return the bytes of DES decryption for hex string
     */
    fun decryptHexStringDES(
        data: String?,
        key: ByteArray?,
        transformation: String?,
        iv: ByteArray?,
    ): ByteArray? = decryptDES(
        StringUtils.hexString2Bytes(data),
        key,
        transformation,
        iv,
    )

    /**
     * Return the bytes of DES decryption.
     *
     * @param data The data.
     * @param key The key.
     * @param transformation The name of the transformation, e.g., *DES/CBC/PKCS5Padding*.
     * @param iv The buffer with the IV. The contents of the buffer are copied to protect against
     *   subsequent modification.
     * @return the bytes of DES decryption
     */
    fun decryptDES(
        data: ByteArray?,
        key: ByteArray?,
        transformation: String?,
        iv: ByteArray?,
    ): ByteArray? {
        return symmetricTemplate(
            data,
            key,
            "DES",
            transformation,
            iv,
            false,
        )
    }

    /**/
    /////////////////////////////////////////////////////////////////////// */ // 3DES encryption
    /**
     * Return the Base64-encode bytes of 3DES encryption.
     *
     * @param data The data .
     * @param key The key.
     * @param transformation The name of the transformation, e.g., *DES/CBC/PKCS5Padding*.
     * @param iv The buffer with the IV. The contents of the buffer are copied to protect against
     *   subsequent modification. @ return the Base64-encode bytes of 3DES encryption
     */
    fun encrypt3DES2Base64(
        data: ByteArray?,
        key: ByteArray?,
        transformation: String?,
        iv: ByteArray?,
    ): ByteArray {
        return EncodeUtils.base64Encode(
            encrypt3DES(data, key, transformation, iv)
        )
    }

    /**
     * Return the hex string of 3DES encryption.
     *
     * @param data The data.
     * @param key The key.
     * @param transformation The name of the transformation, e.g., *DES/CBC/PKCS5Padding*.
     * @param iv The buffer with the IV. The contents of the buffer are copied to protect against
     *   subsequent modification.
     * @return the hex string of 3DES encryption
     */
    fun encrypt3DES2HexString(
        data: ByteArray?,
        key: ByteArray?,
        transformation: String?,
        iv: ByteArray?,
    ): String = encrypt3DES(data, key, transformation, iv)?.toHex() ?: ""


    /**
     * Return the bytes of 3DES encryption.
     *
     * @param data The data.
     * @param key The key.
     * @param transformation The name of the transformation, e.g., *DES/CBC/PKCS5Padding*.
     * @param iv The buffer with the IV. The contents of the buffer are copied to protect against
     *   subsequent modification.
     * @return the bytes of 3DES encryption
     */
    fun encrypt3DES(
        data: ByteArray?,
        key: ByteArray?,
        transformation: String?,
        iv: ByteArray?,
    ): ByteArray? = symmetricTemplate(
        data,
        key,
        "DESede",
        transformation,
        iv,
        true,
    )

    /**
     * Return the bytes of 3DES decryption for Base64-encode bytes.
     *
     * @param data The data.
     * @param key The key.
     * @param transformation The name of the transformation, e.g., *DES/CBC/PKCS5Padding*.
     * @param iv The buffer with the IV. The contents of the buffer are copied to protect against
     *   subsequent modification.
     * @return the bytes of 3DES decryption for Base64-encode bytes
     */
    fun decryptBase64_3DES(
        data: ByteArray?,
        key: ByteArray?,
        transformation: String?,
        iv: ByteArray?,
    ): ByteArray? = decrypt3DES(
        EncodeUtils.base64Decode(data),
        key,
        transformation,
        iv,
    )


    /**
     * Return the bytes of 3DES decryption for hex string.
     *
     * @param data The data.
     * @param key The key.
     * @param transformation The name of the transformation, e.g., *DES/CBC/PKCS5Padding*.
     * @param iv The buffer with the IV. The contents of the buffer are copied to protect against
     *   subsequent modification.
     * @return the bytes of 3DES decryption for hex string
     */
    fun decryptHexString3DES(
        data: String?,
        key: ByteArray?,
        transformation: String?,
        iv: ByteArray?,
    ): ByteArray? = decrypt3DES(
        StringUtils.hexString2Bytes(data),
        key,
        transformation,
        iv,
    )

    /**
     * Return the bytes of 3DES decryption.
     *
     * @param data The data.
     * @param key The key.
     * @param transformation The name of the transformation, e.g., *DES/CBC/PKCS5Padding*.
     * @param iv The buffer with the IV. The contents of the buffer are copied to protect against
     *   subsequent modification.
     * @return the bytes of 3DES decryption
     */
    fun decrypt3DES(
        data: ByteArray?,
        key: ByteArray?,
        transformation: String?,
        iv: ByteArray?,
    ): ByteArray? = symmetricTemplate(
        data,
        key,
        "DESede",
        transformation,
        iv,
        false,
    )


    /**/
    /////////////////////////////////////////////////////////////////////// */ // AES encryption
    /**
     * Return the Base64-encode bytes of AES encryption.
     *
     * @param data The data .
     * @param key The key.
     * @param transformation The name of the transformation, e.g., *DES/CBC/PKCS5Padding*.
     * @param iv The buffer with the IV. The contents of the buffer are copied to protect against
     *   subsequent modification. @ return the Base64-encode bytes of AES encryption
     */
    fun encryptAES2Base64(
        data: ByteArray?,
        key: ByteArray?,
        transformation: String?,
        iv: ByteArray?,
    ): ByteArray = EncodeUtils.base64Encode(encryptAES(data, key, transformation, iv))


    /**
     * Return the hex string of AES encryption.
     *
     * @param data The data.
     * @param key The key.
     * @param transformation The name of the transformation, e.g., *DES/CBC/PKCS5Padding*.
     * @param iv The buffer with the IV. The contents of the buffer are copied to protect against
     *   subsequent modification.
     * @return the hex string of AES encryption
     */
    fun encryptAES2HexString(
        data: ByteArray?,
        key: ByteArray?,
        transformation: String?,
        iv: ByteArray?,
    ): String = encryptAES(data, key, transformation, iv)?.toHex() ?: ""

    /**
     * Return the bytes of AES encryption.
     *
     * @param data The data.
     * @param key The key.
     * @param transformation The name of the transformation, e.g., *DES/CBC/PKCS5Padding*.
     * @param iv The buffer with the IV. The contents of the buffer are copied to protect against
     *   subsequent modification.
     * @return the bytes of AES encryption
     */
    fun encryptAES(
        data: ByteArray?,
        key: ByteArray?,
        transformation: String?,
        iv: ByteArray?,
    ): ByteArray? = symmetricTemplate(
        data,
        key,
        "AES",
        transformation,
        iv,
        true,
    )


    /**
     * Return the bytes of AES decryption for Base64-encode bytes.
     *
     * @param data The data.
     * @param key The key.
     * @param transformation The name of the transformation, e.g., *DES/CBC/PKCS5Padding*.
     * @param iv The buffer with the IV. The contents of the buffer are copied to protect against
     *   subsequent modification.
     * @return the bytes of AES decryption for Base64-encode bytes
     */
    fun decryptBase64AES(
        data: ByteArray?,
        key: ByteArray?,
        transformation: String?,
        iv: ByteArray?,
    ): ByteArray? = decryptAES(
        EncodeUtils.base64Decode(data),
        key,
        transformation,
        iv,
    )

    /**
     * Return the bytes of AES decryption for hex string.
     *
     * @param data The data.
     * @param key The key.
     * @param transformation The name of the transformation, e.g., *DES/CBC/PKCS5Padding*.
     * @param iv The buffer with the IV. The contents of the buffer are copied to protect against
     *   subsequent modification.
     * @return the bytes of AES decryption for hex string
     */
    fun decryptHexStringAES(
        data: String?,
        key: ByteArray?,
        transformation: String?,
        iv: ByteArray?,
    ): ByteArray? = decryptAES(
        StringUtils.hexString2Bytes(data),
        key,
        transformation,
        iv,
    )

    /**
     * Return the bytes of AES decryption.
     *
     * @param data The data.
     * @param key The key.
     * @param transformation The name of the transformation, e.g., *DES/CBC/PKCS5Padding*.
     * @param iv The buffer with the IV. The contents of the buffer are copied to protect against
     *   subsequent modification.
     * @return the bytes of AES decryption
     */
    fun decryptAES(
        data: ByteArray?,
        key: ByteArray?,
        transformation: String?,
        iv: ByteArray?,
    ): ByteArray? = symmetricTemplate(
        data,
        key,
        "AES",
        transformation,
        iv,
        false,
    )

    /**
     * Return the bytes of symmetric encryption or decryption.
     *
     * @param data The data.
     * @param key The key.
     * @param algorithm The name of algorithm.
     * @param transformation The name of the transformation, e.g., *DES/CBC/PKCS5Padding*.
     * @param isEncrypt True to encrypt, false otherwise.
     * @return the bytes of symmetric encryption or decryption
     */
    private fun symmetricTemplate(
        data: ByteArray?,
        key: ByteArray?,
        algorithm: String?,
        transformation: String?,
        iv: ByteArray?,
        isEncrypt: Boolean,
    ): ByteArray? {
        if (data == null || data.isEmpty() || key == null || key.isEmpty()) return null
        return runCatching {
            val secretKey: SecretKey?
            if ("DES" == algorithm) {
                val desKey = DESKeySpec(key)
                val keyFactory: SecretKeyFactory = SecretKeyFactory.getInstance(algorithm)
                secretKey = keyFactory.generateSecret(desKey)
            } else {
                secretKey = SecretKeySpec(key, algorithm)
            }
            val cipher: Cipher = Cipher.getInstance(transformation)
            if (iv == null || iv.isEmpty()) {
                cipher.init(
                    if (isEncrypt) Cipher.ENCRYPT_MODE else Cipher.DECRYPT_MODE,
                    secretKey,
                )
            } else {
                val params: AlgorithmParameterSpec = IvParameterSpec(iv)
                cipher.init(
                    if (isEncrypt) Cipher.ENCRYPT_MODE else Cipher.DECRYPT_MODE,
                    secretKey,
                    params,
                )
            }
            return@runCatching cipher.doFinal(data)
        }
            .onFailure { e ->
                e.printStackTrace()
            }
            .getOrDefault(null)
    }

    /**/
    /////////////////////////////////////////////////////////////////////// */ // RSA encryption
    /**
     * Return the Base64-encode bytes of RSA encryption.
     *
     * @param data The data .
     * @param publicKey The public key.
     * @param keySize The size of key, e.g. 1024, 2048...
     * @param transformation The name of the transformation, e.g., *RSA/CBC/PKCS1Padding*. @ return
     *   the Base64-encode bytes of RSA encryption
     */
    fun encryptRSA2Base64(
        data: ByteArray?,
        publicKey: ByteArray?,
        keySize: Int,
        transformation: String,
    ): ByteArray {
        return EncodeUtils.base64Encode(
            encryptRSA(
                data,
                publicKey,
                keySize,
                transformation,
            )
        )
    }

    /**
     * Return the hex string of RSA encryption.
     *
     * @param data The data.
     * @param publicKey The public key.
     * @param keySize The size of key, e.g. 1024, 2048...
     * @param transformation The name of the transformation, e.g., *RSA/CBC/PKCS1Padding*.
     * @return the hex string of RSA encryption
     */
    fun encryptRSA2HexString(
        data: ByteArray?,
        publicKey: ByteArray?,
        keySize: Int,
        transformation: String,
    ): String = encryptRSA(
        data,
        publicKey,
        keySize,
        transformation,
    )?.toHex() ?: ""

    /**
     * Return the bytes of RSA encryption.
     *
     * @param data The data.
     * @param publicKey The public key.
     * @param keySize The size of key, e.g. 1024, 2048...
     * @param transformation The name of the transformation, e.g., *RSA/CBC/PKCS1Padding*.
     * @return the bytes of RSA encryption
     */
    fun encryptRSA(
        data: ByteArray?,
        publicKey: ByteArray?,
        keySize: Int,
        transformation: String,
    ): ByteArray? = rsaTemplate(
        data,
        publicKey,
        keySize,
        transformation,
        true,
    )

    /**
     * Return the bytes of RSA decryption for Base64-encode bytes.
     *
     * @param data The data.
     * @param privateKey The private key.
     * @param keySize The size of key, e.g. 1024, 2048...
     * @param transformation The name of the transformation, e.g., *RSA/CBC/PKCS1Padding*.
     * @return the bytes of RSA decryption for Base64-encode bytes
     */
    fun decryptBase64RSA(
        data: ByteArray?,
        privateKey: ByteArray?,
        keySize: Int,
        transformation: String,
    ): ByteArray? = decryptRSA(
        EncodeUtils.base64Decode(data),
        privateKey,
        keySize,
        transformation,
    )

    /**
     * Return the bytes of RSA decryption for hex string.
     *
     * @param data The data.
     * @param privateKey The private key.
     * @param keySize The size of key, e.g. 1024, 2048...
     * @param transformation The name of the transformation, e.g., *RSA/CBC/PKCS1Padding*.
     * @return the bytes of RSA decryption for hex string
     */
    fun decryptHexStringRSA(
        data: String?,
        privateKey: ByteArray?,
        keySize: Int,
        transformation: String,
    ): ByteArray? = decryptRSA(
        StringUtils.hexString2Bytes(data),
        privateKey,
        keySize,
        transformation,
    )

    /**
     * Return the bytes of RSA decryption.
     *
     * @param data The data.
     * @param privateKey The private key.
     * @param keySize The size of key, e.g. 1024, 2048...
     * @param transformation The name of the transformation, e.g., *RSA/CBC/PKCS1Padding*.
     * @return the bytes of RSA decryption
     */
    fun decryptRSA(
        data: ByteArray?,
        privateKey: ByteArray?,
        keySize: Int,
        transformation: String,
    ): ByteArray? = rsaTemplate(
        data,
        privateKey,
        keySize,
        transformation,
        false,
    )


    /**
     * Return the bytes of RSA encryption or decryption.
     *
     * @param data The data.
     * @param key The key.
     * @param keySize The size of key, e.g. 1024, 2048...
     * @param transformation The name of the transformation, e.g., *DES/CBC/PKCS1Padding*.
     * @param isEncrypt True to encrypt, false otherwise.
     * @return the bytes of RSA encryption or decryption
     */
    private fun rsaTemplate(
        data: ByteArray?,
        key: ByteArray?,
        keySize: Int,
        transformation: String,
        isEncrypt: Boolean,
    ): ByteArray? {
        if (data == null || data.isEmpty() || key == null || key.isEmpty()) {
            return null
        }
        runCatching {
            val rsaKey: Key?
            val keyFactory: KeyFactory
            if (Build.VERSION.SDK_INT < 28) {
                keyFactory = KeyFactory.getInstance("RSA", "BC")
            } else {
                keyFactory = KeyFactory.getInstance("RSA")
            }
            if (isEncrypt) {
                val keySpec = X509EncodedKeySpec(key)
                rsaKey = keyFactory.generatePublic(keySpec)
            } else {
                val keySpec = PKCS8EncodedKeySpec(key)
                rsaKey = keyFactory.generatePrivate(keySpec)
            }
            if (rsaKey == null) return null
            val cipher: Cipher = Cipher.getInstance(transformation)
            cipher.init(if (isEncrypt) Cipher.ENCRYPT_MODE else Cipher.DECRYPT_MODE, rsaKey)
            val len = data.size
            var maxLen = keySize / 8
            if (isEncrypt) {
                val lowerTrans = transformation.lowercase(getDefault())
                if (lowerTrans.endsWith("pkcs1padding")) {
                    maxLen -= 11
                }
            }
            val count = len / maxLen
            if (count > 0) {
                var ret = ByteArray(0)
                var buff = ByteArray(maxLen)
                var index = 0
                for (i in 0..<count) {
                    System.arraycopy(data, index, buff, 0, maxLen)
                    ret = EncryptUtils.joins(ret, cipher.doFinal(buff))
                    index += maxLen
                }
                if (index != len) {
                    val restLen = len - index
                    buff = ByteArray(restLen)
                    System.arraycopy(data, index, buff, 0, restLen)
                    ret = EncryptUtils.joins(ret, cipher.doFinal(buff))
                }
                return ret
            } else {
                return cipher.doFinal(data)
            }
        }
            .getOrElse { e ->
                e.printStackTrace()
            }
        return null
    }

    /**
     * Return the bytes of RC4 encryption/decryption.
     *
     * @param data The data.
     * @param key The key.
     */
    fun rc4(data: ByteArray?, key: ByteArray?): ByteArray? {
        if (data == null || data.isEmpty() || key == null) return null
        require(key.size in 1..256) { "key must be between 1 and 256 bytes" }
        val iS = ByteArray(256)
        val iK = ByteArray(256)
        val keyLen = key.size
        for (i in 0..255) {
            iS[i] = i.toByte()
            iK[i] = key[i % keyLen]
        }
        var j = 0
        var tmp: Byte
        for (i in 0..255) {
            j = (j + iS[i] + iK[i]) and 0xFF
            tmp = iS[j]
            iS[j] = iS[i]
            iS[i] = tmp
        }

        val ret = ByteArray(data.size)
        var i = 0
        var k: Int
        var t: Int
        for (counter in data.indices) {
            i = (i + 1) and 0xFF
            j = (j + iS[i]) and 0xFF
            tmp = iS[j]
            iS[j] = iS[i]
            iS[i] = tmp
            t = (iS[i] + iS[j]) and 0xFF
            k = iS[t].toInt()
            ret[counter] = (data[counter].toInt() xor k).toByte()
        }
        return ret
    }

    private fun joins(prefix: ByteArray, suffix: ByteArray): ByteArray {
        val ret = ByteArray(prefix.size + suffix.size)
        System.arraycopy(prefix, 0, ret, 0, prefix.size)
        System.arraycopy(suffix, 0, ret, prefix.size, suffix.size)
        return ret
    }
}
