package web

import common.ServerTest
import org.junit.jupiter.api.Test
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.Region.US_EAST_1
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3ClientBuilder
import software.amazon.awssdk.services.s3.model.ListBucketsRequest


class S3BucketTest{

    @Test
    fun loadCredential(){
        println("env : " + System.getenv())

        val accessKey = System.getenv("AWS_ACCESS_KEY_ID")
        println("accessKey : $accessKey ")

        val credentialsProvider = EnvironmentVariableCredentialsProvider.create()
        // val credentialsProvider = SystemPropertyCredentialsProvider.create() // 取得不可
        val credentials = credentialsProvider?.resolveCredentials()

        print("credential accessKeyId: ${credentials?.accessKeyId()}\n  secret: ${credentials?.secretAccessKey()}")
    }

    @Test
    fun testBucketList() {

        val credentialsProvider = EnvironmentVariableCredentialsProvider.create()
        val s3 = S3Client.builder().region(US_EAST_1).credentialsProvider(credentialsProvider).build()

        // List buckets
        val listBucketsRequest = ListBucketsRequest.builder().build()
        val listBucketsResponse = s3.listBuckets(listBucketsRequest)
        listBucketsResponse.buckets().stream().forEach {
            println("s3 bucket : ${it.name()}")
        }
    }

}