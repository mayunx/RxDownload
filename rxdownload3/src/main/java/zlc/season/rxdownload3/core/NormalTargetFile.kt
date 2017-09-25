package zlc.season.rxdownload3.core

import io.reactivex.Maybe
import okhttp3.ResponseBody
import okio.Okio
import retrofit2.Response
import zlc.season.rxdownload3.core.DownloadConfig.ANY
import zlc.season.rxdownload3.core.DownloadConfig.DOWNLOADING_FILE_SUFFIX
import zlc.season.rxdownload3.helper.isChunked
import java.io.File
import java.io.File.separator


class NormalTargetFile(val mission: RealMission) {
    private val realFilePath = mission.actual.savePath + separator + mission.actual.saveName
    private val downloadFilePath = realFilePath + DOWNLOADING_FILE_SUFFIX

    private val realFile = File(realFilePath)
    private val downloadFile = File(downloadFilePath)

    init {
        val dir = File(mission.actual.savePath)
        if (!dir.exists() || !dir.isDirectory) {
            dir.mkdirs()
        }
    }

    fun ensureFinish(): Boolean {
        return realFile.exists()
    }

    fun realFile(): File {
        return realFile
    }

    fun getStatus(): Status {
        return if (ensureFinish()) {
            Status(realFile.length(), realFile.length())
        } else {
            Status()
        }
    }

    fun checkFile() {
        if (downloadFile.exists()) {
            downloadFile.delete()
        }
        downloadFile.createNewFile()
    }

    fun save(response: Response<ResponseBody>): Maybe<Any> {
        val respBody = response.body() ?: throw Throwable("Response body is NULL")

        var downloadSize = 0L
        val byteSize = 8192L
        val totalSize = respBody.contentLength()

        val downloading = Status(downloadSize, totalSize).toDownloading()

        if (isChunked(response)) {
            downloading.toChunked()
        }

        return Maybe.create {
            respBody.source().use { source ->
                Okio.buffer(Okio.sink(realFile)).use { sink ->
                    val buffer = sink.buffer()
                    var readLen = source.read(buffer, byteSize)

                    while (readLen != -1L && !it.isDisposed) {
                        downloadSize += readLen
                        downloading.downloadSize = downloadSize

                        mission.emitStatusWithNotification(downloading)
                        readLen = source.read(buffer, byteSize)
                    }

                    downloadFile.renameTo(realFile)

                    it.onSuccess(ANY)
                }
            }
        }
    }
}