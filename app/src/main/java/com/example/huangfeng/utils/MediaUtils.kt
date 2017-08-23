package com.example.huangfeng.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import java.io.File
import java.util.*
import java.util.logging.Logger

/**
 * 文 件 名: MediaUtils
 * 创 建 人: sineom
 * 创建日期: 2017/8/23 21:01
 * 邮   箱: h.sineom@gmail.com
 * 修改时间：
 * 修改备注：
 */

class MediaUtils {
    private val TAG = javaClass.simpleName!!

    /**
     *
     * 存储卡获取 指定文件
     *
     * @param extension 文件类型 null是表示获取所有的文件
     *
     * @param page 获取第几页 默认第一页
     *
     * *
     * @param limit 每页加载的数据 默认25
     *
     * @return 获取到的所有文件路径信息
     */
    fun getSpecificTypeFiles(extension: Array<String>?, page: Int = 1, limit: Int = 15): List<FileInfo> {

        val offset = (page - 1) * limit

        val fileInfoList = ArrayList<FileInfo>()

        //内存卡文件的Uri
        val fileUri = MediaStore.Files.getContentUri("external")
        //筛选列，这里只筛选了：文件路径和含后缀的文件名
        val projection = arrayOf<String>(MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.TITLE)

        //构造筛选条件语句
        var selection = ""
        if (null != extension) {
            for (i in extension!!.indices) {
                if (i != 0) {
                    selection += " OR "
                }
                selection = selection + MediaStore.Files.FileColumns.DATA + " LIKE '%" + extension[i] + "'"
            }
        }
        //按时间降序条件
        val sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC LIMIT " + limit + " OFFSET " + offset

        val cursor = Utils.getContext().contentResolver.query(fileUri, projection,
                if (selection == "") null else selection, null, sortOrder)
        if (cursor != null) {
            while (cursor!!.moveToNext()) {
                try {
                    val data = cursor!!.getString(0)
                    val fileInfo = FileInfo()
                    fileInfo.setFilePath(data)
                    var size: Long = 0
                    var name: String = ""
                    try {
                        val file = File(data)
                        //文件大小
                        size = file.length()
                        fileInfo.setSize(size)
                        //文件名
                        name = file.name
                        fileInfo.setName(name)
                        //文件缩略图
                        if (file.isDirectory) fileInfo.mFileType = FileInfo.FileType.DIR
                        else fileInfo.mFileType = FileTypeHelper.getFileType(file.name)
                        fileInfo.setFilePath(file.absolutePath)
                    } catch (e: Exception) {

                    }

                    fileInfoList.add(fileInfo)
                } catch (e: Exception) {
                    Logger.e("------>>>" + e.message)
                }

            }
        }
        Logger.i("getSize ===>>> " + fileInfoList.size)
        return fileInfoList
    }


    /**
     * 从数据库中读取图片
     */
    fun getMediaWithImageList(page: Int = 1, limit: Int = 15): List<FileInfo> {
        val offset = (page - 1) * limit
        val mediaBeanList = arrayListOf<FileInfo>()
        val contentResolver = Utils.getContext().contentResolver
        val projection = arrayListOf<String>()
        projection.add(MediaStore.Images.Media._ID)
        projection.add(MediaStore.Images.Media.TITLE)
        projection.add(MediaStore.Images.Media.DATA)
        projection.add(MediaStore.Images.Media.BUCKET_ID)
        projection.add(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        projection.add(MediaStore.Images.Media.MIME_TYPE)
        projection.add(MediaStore.Images.Media.DATE_ADDED)
        projection.add(MediaStore.Images.Media.DATE_MODIFIED)
        projection.add(MediaStore.Images.Media.SIZE)

        val cursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection.toTypedArray(), null,
                null, MediaStore.Images.Media.DATE_ADDED + " DESC LIMIT " + limit + " OFFSET " + offset)
        if (cursor != null) {
            val count = cursor.count
            if (count > 0) {
                cursor.moveToFirst()
                do {
                    val mediaBean = parseImageCursor(cursor)
                    mediaBeanList.add(mediaBean)
                } while (cursor.moveToNext())
            }
        }

        if (cursor != null && !cursor.isClosed) {
            cursor.close()
        }
        return mediaBeanList
    }


    /**
     * 从数据库中读取视屏
     */
    fun getMediaWithVideoList(page: Int = 1, limit: Int = 15): List<FileInfo> {
        val offset = (page - 1) * limit
        val mediaBeanList = arrayListOf<FileInfo>()
        val contentResolver = Utils.getContext().contentResolver
        val projection = arrayListOf<String>()
        projection.add(MediaStore.Video.Media._ID)
        projection.add(MediaStore.Video.Media.TITLE)
        projection.add(MediaStore.Video.Media.DATA)
        projection.add(MediaStore.Video.Media.BUCKET_ID)
        projection.add(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
        projection.add(MediaStore.Video.Media.MIME_TYPE)
        projection.add(MediaStore.Video.Media.DATE_ADDED)
        projection.add(MediaStore.Video.Media.DATE_MODIFIED)
        projection.add(MediaStore.Video.Media.SIZE)

        val cursor = contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection.toTypedArray(), null,
                null, MediaStore.Video.Media.DATE_ADDED + " DESC LIMIT " + limit + " OFFSET " + offset)
        if (cursor != null) {
            val count = cursor.count
            if (count > 0) {
                cursor.moveToFirst()
                do {
                    val mediaBean = parseVideoCursor(cursor)
                    mediaBeanList.add(mediaBean)
                } while (cursor.moveToNext())
            }
        }

        if (cursor != null && !cursor.isClosed) {
            cursor.close()
        }
        return mediaBeanList
    }


    /**
     * 从数据库中读取音频
     */
    fun getMediaWithAudioList(page: Int = 1, limit: Int = 15): List<FileInfo> {
        val offset = (page - 1) * limit
        val mediaBeanList = arrayListOf<FileInfo>()
        val contentResolver = Utils.getContext().contentResolver
        val projection = arrayListOf<String>()
        projection.add(MediaStore.Audio.Media._ID)
        projection.add(MediaStore.Audio.Media.DISPLAY_NAME)
        projection.add(MediaStore.Audio.Media.TITLE)
        projection.add(MediaStore.Audio.Media.MIME_TYPE)
        projection.add(MediaStore.Audio.Media.SIZE)
        projection.add(MediaStore.Audio.Media.DATA)

        val cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection.toTypedArray(), null,
                null, MediaStore.Audio.Media.DATE_ADDED + " DESC LIMIT " + limit + " OFFSET " + offset)
        if (cursor != null) {
            val count = cursor.count
            if (count > 0) {
                cursor.moveToFirst()
                do {
                    val mediaBean = parseAudioCursor(cursor)
                    mediaBeanList.add(mediaBean)
                } while (cursor.moveToNext())
            }
        }

        if (cursor != null && !cursor.isClosed) {
            cursor.close()
        }
        return mediaBeanList
    }

    /**
     * 说明
     *
     *@param context 上下文
     *
     * @param type 类型 image video audio 默认为image
     *
     *@return 所有的文件夹信息
     */
    fun getAllBucket(context: Context, type: FileInfo.FileType): List<BucketBean> {
        val bucketBeenList = arrayListOf<BucketBean>()
        val contentResolver = context.contentResolver
        val projection: Array<String> = when (type) {
            FileInfo.FileType.IMAGE -> {
                arrayOf(MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.ORIENTATION)
            }
            FileInfo.FileType.VIDEO -> {
                arrayOf(MediaStore.Video.Media.BUCKET_ID, MediaStore.Video.Media.DATA, MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
            }
            FileInfo.FileType.MUSIC -> {
                arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME)
            }
            else -> {
                arrayOf(MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.ORIENTATION)
            }
        }
        val allMediaBucket = BucketBean()
        allMediaBucket.bucketId = Integer.MIN_VALUE.toString()
        val uri: Uri

        when (type) {
            FileInfo.FileType.IMAGE -> {
                allMediaBucket.bucketName = context.getString(R.string.all_bucket, ResourceUtil.getStringFromRec(R.string.image))
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
            FileInfo.FileType.VIDEO -> {
                allMediaBucket.bucketName = context.getString(R.string.all_bucket, ResourceUtil.getStringFromRec(R.string.video))
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }
            FileInfo.FileType.MUSIC -> {
                allMediaBucket.bucketName = context.getString(R.string.all_bucket, ResourceUtil.getStringFromRec(R.string.video))
                uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            else -> {
                allMediaBucket.bucketName = context.getString(R.string.all_bucket, ResourceUtil.getStringFromRec(R.string.image))
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
        }
        bucketBeenList.add(allMediaBucket)
        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(uri, projection, null, null, MediaStore.Video.Media.DATE_ADDED + " DESC")
        } catch (e: Exception) {
            Logger.e(e.message)
        }

        if (cursor != null && cursor!!.count > 0) {
            cursor!!.moveToFirst()
            do {
                val bucketBean = BucketBean()
                val bucketId: String
                val bucketKey: String
                val cover: String

                when (type) {
                    FileInfo.FileType.IMAGE -> {
                        bucketId = cursor!!.getString(cursor!!.getColumnIndex(MediaStore.Images.Media.BUCKET_ID))
                        bucketBean.bucketId = bucketId
                        val bucketDisplayName = cursor!!.getString(cursor!!.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME))
                        bucketBean.bucketName = bucketDisplayName
                        bucketKey = MediaStore.Images.Media.BUCKET_ID
                        cover = cursor!!.getString(cursor!!.getColumnIndex(MediaStore.Images.Media.DATA))
                        val orientation = cursor!!.getInt(cursor!!.getColumnIndex(MediaStore.Images.Media.ORIENTATION))
                        bucketBean.orientation = orientation
                    }
                    FileInfo.FileType.VIDEO -> {
                        bucketId = cursor!!.getString(cursor!!.getColumnIndex(MediaStore.Video.Media.BUCKET_ID))
                        bucketBean.bucketId = bucketId
                        val bucketDisplayName = cursor!!.getString(cursor!!.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
                        bucketBean.bucketName = bucketDisplayName
                        bucketKey = MediaStore.Video.Media.BUCKET_ID
                        cover = cursor!!.getString(cursor!!.getColumnIndex(MediaStore.Video.Media.DATA))
                    }
                    FileInfo.FileType.MUSIC -> {
                        bucketId = cursor!!.getString(cursor!!.getColumnIndex(MediaStore.Audio.Media._ID))
                        bucketBean.bucketId = bucketId
                        val bucketDisplayName = cursor!!.getString(cursor!!.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))
                        bucketBean.bucketName = bucketDisplayName
                        bucketKey = MediaStore.Audio.Media._ID
                        cover = cursor!!.getString(cursor!!.getColumnIndex(MediaStore.Audio.Media.DATA))
                    }
                    else -> {
                        bucketId = cursor!!.getString(cursor!!.getColumnIndex(MediaStore.Images.Media.BUCKET_ID))
                        bucketBean.bucketId = bucketId
                        val bucketDisplayName = cursor!!.getString(cursor!!.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME))
                        bucketBean.bucketName = bucketDisplayName
                        bucketKey = MediaStore.Images.Media.BUCKET_ID
                        cover = cursor!!.getString(cursor!!.getColumnIndex(MediaStore.Images.Media.DATA))
                        val orientation = cursor!!.getInt(cursor!!.getColumnIndex(MediaStore.Images.Media.ORIENTATION))
                        bucketBean.orientation = orientation
                    }
                }


                if (TextUtils.isEmpty(allMediaBucket.cover)) {
                    allMediaBucket.cover = cover
                }
                if (bucketBeenList.contains(bucketBean)) {
                    continue
                }
                //获取数量
                val c = contentResolver.query(uri, projection, bucketKey + "=?", arrayOf(bucketId), null)
                if (c != null && c!!.count > 0) {
                    bucketBean.imageCount = c!!.count
                }
                bucketBean.cover = cover
                if (c != null && !c!!.isClosed) {
                    c!!.close()
                }
                bucketBeenList.add(bucketBean)
            } while (cursor!!.moveToNext())
        }

        if (cursor != null && !cursor!!.isClosed) {
            cursor!!.close()
        }
        return bucketBeenList
    }

    /**
     * 根据路径获取该目录的所有子元素
     * @param internalList 集合的容器
     *
     *@param inter 父目录
     *
     *@return
     */
    fun prepareFileListEntries(internalList: ArrayList<FileInfo>, inter: File): ArrayList<FileInfo> {
        var internalList = internalList
        try {
            //Check for each and every directory/file in 'inter' directory.
            //Filter by extension using 'filter' reference.

            for (name in inter.listFiles()) {
                //If file/directory can be read by the Application
                if (name.canRead()) {
                    //Create a row item for the directory list and define properties.
                    val item = FileInfo()
                    item.setName(name.name)
                    item.setFilePath(name.absolutePath)
                    item.setSize(name.length())
                    if (name.isDirectory) item.mFileType = FileInfo.FileType.DIR
                    else item.mFileType = FileTypeHelper.getFileType(name.name)
                    //Add row to the List of directories/files
                    internalList.add(item)
                }
            }
            //Sort the files and directories in alphabetical order.
            //See compareTo method in FileListItem class.
//            Collections.sort(internalList)
        } catch (e: NullPointerException) {   //Just dont worry, it rarely occurs.
            e.printStackTrace()
            internalList = ArrayList()
        }

        return internalList
    }

    /**
     * 根据路径获取该目录的所有子元素
     * @param internalList 集合的容器
     *
     *@param inter 父路径
     *
     *@return
     */
    fun prepareFileListEntries(internalList: ArrayList<FileInfo>, inter: String = Environment.getExternalStorageDirectory().absolutePath): ArrayList<FileInfo> {
        return prepareFileListEntries(internalList, File(inter))
    }


    /**
     * 解析视频cursor并且创建缩略图
     */
    private fun parseVideoCursor(cursor: Cursor): FileInfo {
        val mediaBean = FileInfo()
        val title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE))
        mediaBean.setName(title)
        val originalPath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
        mediaBean.setFilePath(originalPath)
        val length = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE))
        mediaBean.setSize(length)
        mediaBean.mFileType = FileInfo.FileType.VIDEO
        return mediaBean
    }

    /**
     * 解析图片cursor并且创建缩略图
     */
    private fun parseImageCursor(cursor: Cursor): FileInfo {
        val mediaBean = FileInfo()
        val title = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.TITLE))
        mediaBean.setName(title)
        val originalPath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
        mediaBean.setFilePath(originalPath)
        val length = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.SIZE))
        mediaBean.setSize(length)
        mediaBean.mFileType = FileInfo.FileType.IMAGE
        return mediaBean
    }

    /**
     * 解析音频cursor并且创建缩略图
     */
    private fun parseAudioCursor(cursor: Cursor): FileInfo {
        val mediaBean = FileInfo()
        val title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
        mediaBean.setName(title)
        val originalPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
        mediaBean.setFilePath(originalPath)
        val length = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE))
        mediaBean.setSize(length)
        mediaBean.mFileType = FileInfo.FileType.MUSIC
        return mediaBean
    }

}