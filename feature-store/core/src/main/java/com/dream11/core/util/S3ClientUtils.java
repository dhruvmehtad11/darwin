package com.dream11.core.util;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
public class S3ClientUtils {
  public static S3AsyncClient createS3Client() {
    // for testing purposes because current library implementation for aws-sdk-v2 does not support
    // override using env variable AWS_ENDPOINT_URL
    if ((Objects.equals(System.getProperty("app.environment"), "test")
            || Objects.equals(System.getProperty("app.environment"), "local")
            || Objects.equals(System.getProperty("app.environment"), "darwin-local"))
        && System.getProperty("AWS_ENDPOINT_URL") != null)
      try {
        URI newAwsEndpoint = new URI(System.getProperty("AWS_ENDPOINT_URL"));
        return S3AsyncClient.crtBuilder()
            .region(Region.US_EAST_1)
            .endpointOverride(newAwsEndpoint)
            .build();
      } catch (Exception ignore) {
        return S3AsyncClient.crtBuilder().region(Region.US_EAST_1).build();
      }
    return S3AsyncClient.crtBuilder().region(Region.US_EAST_1).build();
  }

  public static Completable putObject(S3AsyncClient client, String bucket, String path, JsonObject jsonObject) {
   log.error("putting object: {} to path: {}", jsonObject, path);
    return Completable.create(
        emitter ->
            client
                .putObject(
                    PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(path)
                        .build(),
                    AsyncRequestBody.fromBytes(
                        jsonObject.toString().getBytes(StandardCharsets.UTF_8)))
                .whenComplete(
                    (r, e) -> {
                      if (e != null) {
                        log.error("error putting object: {} to path: {}", jsonObject, path, e);
                        emitter.onError(e);
                      } else emitter.onComplete();
                    }));
  }

  public static Single<JsonObject> getObject(S3AsyncClient client, String bucket, String path) {
    return Single.create(
        emitter ->
            client
                .getObject(
                    GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(path)
                        .build(),
                    AsyncResponseTransformer.toBytes())
                .whenComplete(
                    (r, e) -> {
                      if (e != null) {
                        log.error("error getting object from path: {}", path, e);
                        emitter.onError(e);
                      } else emitter.onSuccess(new JsonObject(r.asString(StandardCharsets.UTF_8)));
                    }));
  }

  public static Completable deleteObject(S3AsyncClient client, String bucket, String path) {
    return Completable.create(
        emitter ->
            client
                .deleteObject(
                    DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(path)
                        .build())
                .whenComplete(
                    (r, e) -> {
                      if (e != null) {
                        log.error("error deleting object from path: {}", path, e);
                        emitter.onError(e);
                      } else emitter.onComplete();
                    }));
  }
}
