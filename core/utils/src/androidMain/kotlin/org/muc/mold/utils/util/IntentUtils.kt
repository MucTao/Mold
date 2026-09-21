@file:Suppress("unused")

package org.muc.mold.utils.util

import android.Manifest.permission.CALL_PHONE
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import androidx.annotation.RequiresPermission
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import java.io.File
import java.util.LinkedList

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2016/09/23
 * desc  : utils about intent
 * </pre> *
 */
object IntentUtils {
    /**
     * Return whether the intent is available.
     *
     * @param intent The intent.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isIntentAvailable(intent: Intent) =
        Utils.app.packageManager
            .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            .isNotEmpty()

    /**
     * Return the intent of install app.
     *
     * Target APIs greater than 25 must hold `<uses-permission
     * android:name="android.permission.REQUEST_INSTALL_PACKAGES" />`
     *
     * @param filePath The path of file.
     * @return the intent of install app
     */
    fun getInstallAppIntent(filePath: String?): Intent? {
        return getInstallAppIntent(FileUtils.getFileByPath(filePath))
    }

    /**
     * Return the intent of install app.
     *
     * Target APIs greater than 25 must hold `<uses-permission
     * android:name="android.permission.REQUEST_INSTALL_PACKAGES" />`
     *
     * @param file The file.
     * @return the intent of install app
     */
    fun getInstallAppIntent(file: File?): Intent? {
        if (file == null) return null
        if (!FileUtils.isFileExists(file)) return null
        val uri: Uri?
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            uri = Uri.fromFile(file)
        } else {
            val authority: String = Utils.app.packageName + ".utilcode.fileprovider"
            uri = FileProvider.getUriForFile(Utils.app, authority, file)
        }
        return getInstallAppIntent(uri)
    }

    /**
     * Return the intent of install app.
     *
     * Target APIs greater than 25 must hold `<uses-permission
     * android:name="android.permission.REQUEST_INSTALL_PACKAGES" />`
     *
     * @param uri The uri.
     * @return the intent of install app
     */
    fun getInstallAppIntent(uri: Uri?): Intent? {
        if (uri == null) return null
        val intent = Intent(Intent.ACTION_VIEW)
        val type = "application/vnd.android.package-archive"
        intent.setDataAndType(uri, type)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    /**
     * Return the intent of uninstall app.
     *
     * Target APIs greater than 25 must hold `<uses-permission
     * android:name="android.permission.REQUEST_DELETE_PACKAGES" />`
     *
     * @param pkgName The name of the package.
     * @return the intent of uninstall app
     */
    fun getUninstallAppIntent(pkgName: String?): Intent {
        val intent = Intent(Intent.ACTION_DELETE)
        intent.setData(("package:$pkgName").toUri())
        return intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    /**
     * Return the intent of launch app.
     *
     * @param pkgName The name of the package.
     * @return the intent of launch app
     */
    fun getLaunchAppIntent(pkgName: String = Utils.app.packageName): Intent? {
        val launcherActivity: String? = ActivityUtils.getLauncherActivity(pkgName)
        if (launcherActivity.isNullOrBlank()) return null
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.setClassName(pkgName, launcherActivity)
        return intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    /**
     * Return the intent of launch app details settings.
     *
     * @param pkgName The name of the package.
     * @return the intent of launch app details settings
     */
    fun getLaunchAppDetailsSettingsIntent(
        pkgName: String?,
        isNewTask: Boolean = false,
    ): Intent {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.setData(("package:$pkgName").toUri())
        return getIntent(intent, isNewTask)
    }

    /**
     * Return the intent of share text.
     *
     * @param content The content.
     * @return the intent of share text
     */
    fun getShareTextIntent(content: String?): Intent {
        var intent = Intent(Intent.ACTION_SEND)
        intent.setType("text/plain")
        intent.putExtra(Intent.EXTRA_TEXT, content)
        intent = Intent.createChooser(intent, "")
        return getIntent(intent, true)
    }

    /**
     * Return the intent of share image.
     *
     * @param imagePath The path of image.
     * @return the intent of share image
     */
    fun getShareImageIntent(imagePath: String?): Intent? {
        return getShareTextImageIntent("", imagePath)
    }

    /**
     * Return the intent of share image.
     *
     * @param imageFile The file of image.
     * @return the intent of share image
     */
    fun getShareImageIntent(imageFile: File): Intent {
        return getShareTextImageIntent("", imageFile)
    }

    /**
     * Return the intent of share image.
     *
     * @param imageUri The uri of image.
     * @return the intent of share image
     */
    fun getShareImageIntent(imageUri: Uri?): Intent {
        return getShareTextImageIntent("", imageUri)
    }

    /**
     * Return the intent of share image.
     *
     * @param content The content.
     * @param imagePath The path of image.
     * @return the intent of share image
     */
    fun getShareTextImageIntent(content: String?, imagePath: String?): Intent? {
        return FileUtils.getFileByPath(imagePath)?.let { getShareTextImageIntent(content, it) }
    }

    /**
     * Return the intent of share image.
     *
     * @param content The content.
     * @param imageFile The file of image.
     * @return the intent of share image
     */
    fun getShareTextImageIntent(content: String?, imageFile: File): Intent {
        return getShareTextImageIntent(content, UriUtils.file2Uri(imageFile))
    }

    /**
     * Return the intent of share image.
     *
     * @param content The content.
     * @param imageUri The uri of image.
     * @return the intent of share image
     */
    fun getShareTextImageIntent(content: String?, imageUri: Uri?): Intent {
        var intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_TEXT, content)
        intent.putExtra(Intent.EXTRA_STREAM, imageUri)
        intent.setType("image/*")
        intent = Intent.createChooser(intent, "")
        return getIntent(intent, true)
    }

    /**
     * Return the intent of share images.
     *
     * @param imagePaths The paths of images.
     * @return the intent of share images
     */
    fun getShareImageIntent(imagePaths: LinkedList<String?>?): Intent {
        return getShareTextImageIntent("", imagePaths)
    }

    /**
     * Return the intent of share images.
     *
     * @param images The files of images.
     * @return the intent of share images
     */
    fun getShareImageIntent(images: List<File>?): Intent {
        return getShareTextImageIntent("", images)
    }

    /**
     * Return the intent of share images.
     *
     * @param uris The Uris of image.
     * @return the intent of share image
     */
    fun getShareImageIntent(uris: ArrayList<Uri?>?): Intent {
        return getShareTextImageIntent("", uris)
    }

    /**
     * Return the intent of share images.
     *
     * @param content The content.
     * @param imagePaths The paths of images.
     * @return the intent of share images
     */
    fun getShareTextImageIntent(
        content: String?,
        imagePaths: LinkedList<String?>?,
    ): Intent {
        val files = ArrayList<File>()
        if (imagePaths != null) {
            for (imagePath in imagePaths) {
                val file: File? = FileUtils.getFileByPath(imagePath)
                if (file != null) {
                    files.add(file)
                }
            }
        }
        return getShareTextImageIntent(content, files)
    }

    /**
     * Return the intent of share images.
     *
     * @param content The content.
     * @param images The files of images.
     * @return the intent of share images
     */
    fun getShareTextImageIntent(content: String?, images: List<File>?): Intent {
        val uris: ArrayList<Uri?> = ArrayList()
        if (images != null) {
            for (image in images) {
                val uri: Uri = UriUtils.file2Uri(image)
                uris.add(uri)
            }
        }
        return getShareTextImageIntent(content, uris)
    }

    /**
     * Return the intent of share images.
     *
     * @param content The content.
     * @param uris The Uris of image.
     * @return the intent of share image
     */
    fun getShareTextImageIntent(content: String?, uris: ArrayList<Uri?>?): Intent {
        var intent = Intent(Intent.ACTION_SEND_MULTIPLE)
        intent.putExtra(Intent.EXTRA_TEXT, content)
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        intent.setType("image/*")
        intent = Intent.createChooser(intent, "")
        return getIntent(intent, true)
    }


    /**
     * Return the intent of component.
     *
     * @param pkgName The name of the package.
     * @param className The name of class.
     * @param bundle The Bundle of extras to add to this intent.
     * @param isNewTask True to add flag of new task, false otherwise.
     * @return the intent of component
     */
    fun getComponentIntent(
        className: String,
        pkgName: String = Utils.app.packageName,
        bundle: Bundle? = null,
        isNewTask: Boolean = false,
    ): Intent {
        val intent = Intent()
        if (bundle != null) intent.putExtras(bundle)
        val cn = ComponentName(pkgName, className)
        intent.setComponent(cn)
        return getIntent(intent, isNewTask)
    }

    val shutdownIntent: Intent
        /**
         * Return the intent of shutdown.
         *
         * Requires root permission or hold `android:sharedUserId="android.uid.system"`,
         * `<uses-permission android:name="android.permission.SHUTDOWN" />` in manifest.
         *
         * @return the intent of shutdown
         */
        get() {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent("com.android.internal.intent.action.REQUEST_SHUTDOWN")
            } else {
                Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN")
            }
            intent.putExtra("android.intent.extra.KEY_CONFIRM", false)
            return intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

    /**
     * Return the intent of dial.
     *
     * @param phoneNumber The phone number.
     * @return the intent of dial
     */
    fun getDialIntent(phoneNumber: String): Intent {
        val intent = Intent(Intent.ACTION_DIAL, ("tel:" + Uri.encode(phoneNumber)).toUri())
        return getIntent(intent, true)
    }

    /**
     * Return the intent of call.
     *
     * Must hold `<uses-permission android:name="android.permission.CALL_PHONE" />`
     *
     * @param phoneNumber The phone number.
     * @return the intent of call
     */
    @RequiresPermission(CALL_PHONE)
    fun getCallIntent(phoneNumber: String): Intent {
        val intent =
            Intent("android.intent.action.CALL", ("tel:" + Uri.encode(phoneNumber)).toUri())
        return getIntent(intent, true)
    }

    /**
     * Return the intent of send SMS.
     *
     * @param phoneNumber The phone number.
     * @param content The content of SMS.
     * @return the intent of send SMS
     */
    fun getSendSmsIntent(phoneNumber: String, content: String?): Intent {
        val uri: Uri = ("smsto:" + Uri.encode(phoneNumber)).toUri()
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        intent.putExtra("sms_body", content)
        return getIntent(intent, true)
    }

    /**
     * Return the intent of capture.
     *
     * @param outUri The uri of output.
     * @param isNewTask True to add flag of new task, false otherwise.
     * @return the intent of capture
     */
    fun getCaptureIntent(
        outUri: Uri?,
        isNewTask: Boolean = false,
    ): Intent {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        return getIntent(intent, isNewTask)
    }

    private fun getIntent(intent: Intent, isNewTask: Boolean) =
        if (isNewTask) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) else intent

    //    /**
    //     * 获取选择照片的 Intent
    //     *
    //     * @return
    //     */
    //    public static Intent getPickIntentWithGallery() {
    //        Intent intent = new Intent(Intent.ACTION_PICK);
    //        return intent.setType("image*//*");
    //    }
    //
    //    /**
    //     * 获取从文件中选择照片的 Intent
    //     *
    //     * @return
    //     */
    //    public static Intent getPickIntentWithDocuments() {
    //        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    //        return intent.setType("image*//*");
    //    }
    //
    //
    //    public static Intent buildImageGetIntent(final Uri saveTo, final int outputX, final int
    // outputY, final boolean returnData) {
    //        return buildImageGetIntent(saveTo, 1, 1, outputX, outputY, returnData);
    //    }
    //
    //    public static Intent buildImageGetIntent(Uri saveTo, int aspectX, int aspectY,
    //                                             int outputX, int outputY, boolean returnData) {
    //        Intent intent = new Intent();
    //        if (Build.VERSION.SDK_INT < 19) {
    //            intent.setAction(Intent.ACTION_GET_CONTENT);
    //        } else {
    //            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
    //            intent.addCategory(Intent.CATEGORY_OPENABLE);
    //        }
    //        intent.setType("image*//*");
    //        intent.putExtra("output", saveTo);
    //        intent.putExtra("aspectX", aspectX);
    //        intent.putExtra("aspectY", aspectY);
    //        intent.putExtra("outputX", outputX);
    //        intent.putExtra("outputY", outputY);
    //        intent.putExtra("scale", true);
    //        intent.putExtra("return-data", returnData);
    //        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
    //        return intent;
    //    }
    //
    //    public static Intent buildImageCropIntent(final Uri uriFrom, final Uri uriTo, final int
    // outputX, final int outputY, final boolean returnData) {
    //        return buildImageCropIntent(uriFrom, uriTo, 1, 1, outputX, outputY, returnData);
    //    }
    //
    //    public static Intent buildImageCropIntent(Uri uriFrom, Uri uriTo, int aspectX, int
    // aspectY,
    //                                              int outputX, int outputY, boolean returnData) {
    //        Intent intent = new Intent("com.android.camera.action.CROP");
    //        intent.setDataAndType(uriFrom, "image*//*");
    //        intent.putExtra("crop", "true");
    //        intent.putExtra("output", uriTo);
    //        intent.putExtra("aspectX", aspectX);
    //        intent.putExtra("aspectY", aspectY);
    //        intent.putExtra("outputX", outputX);
    //        intent.putExtra("outputY", outputY);
    //        intent.putExtra("scale", true);
    //        intent.putExtra("return-data", returnData);
    //        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
    //        return intent;
    //    }
    //
    //    public static Intent buildImageCaptureIntent(final Uri uri) {
    //        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    //        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
    //        return intent;
    //    }
}
