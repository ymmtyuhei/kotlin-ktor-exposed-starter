import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.jackson.jackson
import io.ktor.routing.Routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import kotlinx.coroutines.delay
import service.DatabaseFactory
import service.WidgetService
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.core.sync.ResponseTransformer
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import web.widget
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.file.Paths
import java.util.*
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest




fun Application.module() {
    install(DefaultHeaders)
    install(CallLogging)
    install(WebSockets)

    install(ContentNegotiation) {
        jackson {
            configure(SerializationFeature.INDENT_OUTPUT, true)
        }
    }

    DatabaseFactory.init()

    val widgetService = WidgetService()

    install(Routing) {
        widget(widgetService)
    }

    val s3 = S3Client.builder().region(Region.US_EAST_1).build()

    list(s3)
    multipartUpload(s3, "yy-trial-bucket", "testUploadKey")
    //getObject(s3, "yy-trial-bucket", "testUploadKey")
    deleteObject(s3, "yy-trial-bucket", "testUploadKey")

}

fun list(s3: S3Client){
    // List buckets
    val listBucketsRequest = ListBucketsRequest.builder().build()
    val listBucketsResponse = s3.listBuckets(listBucketsRequest)
    listBucketsResponse.buckets().stream().forEach {
        println("s3 bucket : ${it.name()}")
    }
}

fun multipartUpload(s3: S3Client, bucketName: String, key: String) {
    val MB = 1024 * 1024

    // First create a multipart upload and get upload id
    val createMultipartUploadRequest = CreateMultipartUploadRequest.builder()
            .bucket(bucketName).key(key)
            .build()
    val response = s3.createMultipartUpload(createMultipartUploadRequest)
    val uploadId = response.uploadId()
    println(uploadId)

    // Upload all the different parts of the object
    val uploadPartRequest1 = UploadPartRequest.builder().bucket(bucketName).key(key)
            .uploadId(uploadId)
            .partNumber(1).build()
    val etag1 = s3.uploadPart(uploadPartRequest1, RequestBody.fromByteBuffer(getRandomByteBuffer(5 * MB))).eTag()
    val part1 = CompletedPart.builder().partNumber(1).eTag(etag1).build()

    val uploadPartRequest2 = UploadPartRequest.builder().bucket(bucketName).key(key)
            .uploadId(uploadId)
            .partNumber(2).build()
    val etag2 = s3.uploadPart(uploadPartRequest2, RequestBody.fromByteBuffer(getRandomByteBuffer(3 * MB))).eTag()
    val part2 = CompletedPart.builder().partNumber(2).eTag(etag2).build()


    // Finally call completeMultipartUpload operation to tell S3 to merge all uploaded
    // parts and finish the multipart operation.
    val completedMultipartUpload = CompletedMultipartUpload.builder().parts(part1, part2).build()
    val completeMultipartUploadRequest = CompleteMultipartUploadRequest.builder().bucket(bucketName).key(key).uploadId(uploadId)
            .multipartUpload(completedMultipartUpload).build()
    s3.completeMultipartUpload(completeMultipartUploadRequest)
}

fun getRandomByteBuffer(size: Int): ByteBuffer {
    val b = ByteArray(size)
    Random().nextBytes(b)
    return ByteBuffer.wrap(b)
}

fun getObject(s3: S3Client, bucketName: String, key: String){
    s3.getObject(
            GetObjectRequest.builder().bucket(bucketName).key(key).build(),
            ResponseTransformer.toFile(Paths.get("multiPartKey"))
    )
}

fun deleteObject(s3: S3Client, bucketName: String, key: String){
    s3.deleteObject(
            DeleteObjectRequest.builder().bucket(bucketName).key(key).build()
    )
}

fun main() {
    embeddedServer(Netty, 8080, watchPaths = listOf("MainKt"), module = Application::module).start()
}