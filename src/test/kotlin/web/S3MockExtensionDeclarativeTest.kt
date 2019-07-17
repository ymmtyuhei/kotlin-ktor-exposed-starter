package web


import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull

import com.adobe.testing.s3mock.junit5.S3MockExtension
import com.adobe.testing.s3mock.util.HashUtil
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import software.amazon.awssdk.services.s3.model.PutObjectRequest


/**
 *
 * 以下モックサーバを起動しておくこと
 * docker run -p 9090:9090 -p 9191:9191 -t adobe/s3mock
 *
 * https://github.com/adobe/S3Mock
 */
@ExtendWith(S3MockExtension::class)
class S3MockExtensionDeclarativeTest {

    /**
     * Creates a bucket, stores a file, downloads the file again and compares checksums.
     *
     * @param s3Client Client injected by the test framework
     * @throws Exception if FileStreams can not be read
     */
    @Test
    @Throws(Exception::class)
    fun shouldUploadAndDownloadObject(s3Client: S3Client) {
        val uploadFile = File(UPLOAD_FILE_NAME)

        s3Client.createBucket(CreateBucketRequest.builder().bucket(BUCKET_NAME).build())
        s3Client.putObject(
                PutObjectRequest.builder().bucket(BUCKET_NAME).key(uploadFile.name).build(),
                RequestBody.fromFile(uploadFile))

        val response = s3Client.getObject(
                GetObjectRequest.builder().bucket(BUCKET_NAME).key(uploadFile.name).build())

        val uploadFileIs = FileInputStream(uploadFile)
        val uploadHash = HashUtil.getDigest(uploadFileIs)
        val downloadedHash = HashUtil.getDigest(response)
        uploadFileIs.close()
        response.close()

        assertEquals(uploadHash, downloadedHash, "Up- and downloaded Files should have equal Hashes")
    }

    @Nested
    internal inner class NestedTest {

        @Test
        fun nestedTestShouldNotStartSecondInstanceOfMock(s3Client: S3Client) {
            assertNotNull(s3Client)
        }
    }

    companion object {

        private val BUCKET_NAME = "mydemotestbucket"
        private val UPLOAD_FILE_NAME = "/Users/yy/Downloads/xsnqGjp.jpg"
    }
}