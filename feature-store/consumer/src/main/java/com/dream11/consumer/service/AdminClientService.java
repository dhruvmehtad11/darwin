package com.dream11.consumer.service;

import com.dream11.common.app.AppContext;
import com.dream11.core.config.ApplicationConfig;
import com.dream11.core.dto.consumer.AllConsumerGroupMetadataResponse;
import com.dream11.core.dto.consumer.ConsumerGroupMetadata;
import com.dream11.core.dto.helper.Data;
import com.dream11.core.error.ApiRestException;
import com.dream11.core.error.ServiceError;
import com.dream11.core.service.S3MetastoreService;
import com.dream11.webclient.reactivex.client.WebClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.Single;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class AdminClientService {
  private final WebClient webClient;
  private final S3MetastoreService s3MetastoreService;
  private final ObjectMapper objectMapper = AppContext.getInstance(ObjectMapper.class);
  private final ApplicationConfig applicationConfig =
      AppContext.getInstance(ApplicationConfig.class);

  public Single<List<ConsumerGroupMetadata>> getConsumerGroupsMetadataFromAdmin() {
    return webClient
        .getWebClient()
        .getAbs(
            applicationConfig.getOfsAdminHost()
                + applicationConfig.getOfsAdminConsumersMetadataEndpoint())
        .rxSend()
        .map(
            r ->
                objectMapper.readValue(
                    r.bodyAsJsonObject().toString(),
                    new TypeReference<Data<List<ConsumerGroupMetadata>>>() {}))
        .map(Data::getData)
        .onErrorResumeNext(
            e ->
                getConsumerGroupsMetadataFromS3()
                    .onErrorResumeNext(
                        Single.error(
                            new ApiRestException(
                                e.getMessage(),
                                ServiceError.CONSUMER_CONFIG_NOT_FOUND_EXCEPTION))));
  }

  public Single<List<ConsumerGroupMetadata>> getConsumerGroupsMetadataFromS3() {
    return s3MetastoreService
        .getConsumerGroupConfig()
        .map(AllConsumerGroupMetadataResponse::getConsumerGroupMetadata);
  }
}
