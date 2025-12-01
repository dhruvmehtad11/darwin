package com.dream11.admin.rest;

import com.dream11.admin.service.ConsumerManagerService;
import com.dream11.admin.service.KafkaAdminService;
import com.dream11.core.dto.consumer.AddFeatureGroupConsumerMetadataRequest;
import com.dream11.core.dto.consumer.ConsumerGroupMetadata;
import com.dream11.core.dto.consumer.FeatureGroupConsumerMap;
import com.dream11.core.dto.consumer.UpdateConsumerCountRequest;
import com.dream11.core.dto.consumer.UpdateTenantTopicRequest;
import com.dream11.core.dto.consumer.UpdateTopicPartitionRequest;
import com.dream11.rest.io.Response;
import com.dream11.rest.io.RestResponse;
import com.google.inject.Inject;
import io.reactivex.Single;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import java.util.List;
import java.util.concurrent.CompletionStage;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@Path("/consumer")
public class Consumers {
  private final ConsumerManagerService consumerManagerService;

  @GET
  @Path("/metadata/all")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<List<ConsumerGroupMetadata>>> getAllConsumerMetadata() {
    return consumerManagerService.getAllConsumerMetadata().to(RestResponse.jaxrsRestHandler());
  }

  @POST
  @Path("/onboard")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<AddFeatureGroupConsumerMetadataRequest>> addConsumerMetadata(
      @RequestBody AddFeatureGroupConsumerMetadataRequest request) {
    return consumerManagerService
        .onboardFeatureGroupConsumer(request)
        .andThen(Single.just(request))
        .to(RestResponse.jaxrsRestHandler());
  }

  @POST
  @Path("/register")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<FeatureGroupConsumerMap>> addAdhocConsumerMetadata(
      @RequestBody @Valid FeatureGroupConsumerMap request) {
    return consumerManagerService
        .registerConsumer(request)
        .andThen(Single.just(request))
        .to(RestResponse.jaxrsRestHandler());
  }

  @PUT
  @Path("/topic")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<UpdateTenantTopicRequest>> updateTenantTopicName(
      @RequestBody UpdateTenantTopicRequest request) {
    return consumerManagerService
        .updateTopicName(request)
        .andThen(Single.just(request))
        .to(RestResponse.jaxrsRestHandler());
  }

  @PUT
  @Path("/partitions")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<UpdateTopicPartitionRequest>> updateTopicPartitions(@RequestBody UpdateTopicPartitionRequest request) {
    return consumerManagerService.updateTopicPartitions(request)
        .andThen(Single.just(request))
        .to(RestResponse.jaxrsRestHandler());
  }

  @PUT
  @Path("/count")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<UpdateConsumerCountRequest>> updateConsumerCount(@RequestBody UpdateConsumerCountRequest request) {
    return consumerManagerService.updateConsumerCount(request)
        .andThen(Single.just(request))
        .to(RestResponse.jaxrsRestHandler());
  }
}
