package com.dream11.core.util;

import com.dream11.core.dto.helper.Data;
import com.dream11.core.error.ApiRestException;
import com.dream11.core.error.ServiceError;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Promise;
import io.vertx.reactivex.core.Vertx;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CompressionUtils {

  public static Single<String> compressNonBlocking(Vertx vertx, String data) {
    return vertx.rxExecuteBlocking((Promise<String> promise) -> {
      try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length())) {
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION, true);
        DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(outputStream, deflater);
        deflaterOutputStream.write(data.getBytes());
        deflaterOutputStream.finish();

        byte[] compressedData = outputStream.toByteArray();
        String compressedBase64 = Base64.getEncoder().encodeToString(compressedData);
        promise.complete(compressedBase64);
      } catch (Exception e) {
        e.printStackTrace();
        promise.fail(e);
      }
    }, false).toSingle();
  }

  public static Single<String> decompressNonBlocking(Vertx vertx, String compressedBase64) {
    return vertx.rxExecuteBlocking((Promise<String> promise) -> {
      try {
        byte[] compressedData = Base64.getDecoder().decode(compressedBase64);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedData);
        Inflater inflater = new Inflater(true);
        InflaterInputStream inflaterInputStream = new InflaterInputStream(inputStream, inflater);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int length;
        while ((length = inflaterInputStream.read(buffer)) != -1) {
          outputStream.write(buffer, 0, length);
        }

        inflaterInputStream.close();
        outputStream.close();

        String decompressedData = outputStream.toString();
        promise.complete(decompressedData);
      } catch (Exception e) {
        e.printStackTrace();
        promise.fail(e);
      }
    }, false).toSingle();
  }
}
