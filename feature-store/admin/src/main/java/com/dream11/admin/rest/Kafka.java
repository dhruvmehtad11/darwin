package com.dream11.admin.rest;

import com.dream11.admin.service.ConsumerManagerService;
import com.dream11.admin.service.KafkaAdminService;
import com.dream11.core.dto.consumer.AddFeatureGroupConsumerMetadataRequest;
import com.dream11.core.dto.consumer.ConsumerGroupMetadata;
import com.dream11.core.dto.kafka.AllKafkaTopicConfigResponse;
import com.dream11.core.dto.kafka.CreateTopicRequest;
import com.dream11.core.service.S3MetastoreService;
import com.dream11.rest.io.Response;
import com.dream11.rest.io.RestResponse;
import com.google.inject.Inject;
import io.reactivex.Single;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.CompletionStage;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@Path("/kafka")
public class Kafka {
  private final KafkaAdminService kafkaAdminService;
  private final S3MetastoreService s3MetastoreService;

  @POST
  @Path("/topic")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<CreateTopicRequest>> createTopic(@RequestBody CreateTopicRequest request) {
    return kafkaAdminService.createTopic(request.getTopicName(), request.getNumPartitions())
        .andThen(Single.just(request))
        .to(RestResponse.jaxrsRestHandler());
  }

  @GET
  @Path("/topic-metadata")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<AllKafkaTopicConfigResponse>> getKafkaTopicConfigs() {
    return s3MetastoreService.getKafkaTopicConfig().to(RestResponse.jaxrsRestHandler());
  }

}
