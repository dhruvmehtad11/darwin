package com.dream11.app.rest;

import com.dream11.app.service.FeatureGroupService;
import com.dream11.common.util.CompletableFutureUtils;
import com.dream11.core.dto.helper.LegacyFeatureStoreResponse;
import com.dream11.core.dto.request.WriteCassandraFeaturesRequest;
import com.dream11.core.dto.request.legacystack.*;
import com.dream11.core.dto.response.LegacyBatchWriteResponse;
import com.dream11.core.dto.response.interfaces.WriteFeaturesResponse;
import com.dream11.rest.io.Response;
import com.dream11.rest.io.RestResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.vertx.core.json.JsonObject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import static com.dream11.core.constant.Constants.FeatureStoreType.cassandra;
import static com.dream11.core.constant.Constants.SUCCESS_HTTP_STATUS_CODE;
import static com.dream11.core.constant.Constants.WRITE_SUCCESSFUL_SUB_STRING;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@Path("/ofs")
public class LegacyStack {
  private final FeatureGroupService featureGroupService;
  private final ObjectMapper objectMapper;

  @POST
  @Path("/write-features")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<LegacyFeatureStoreResponse<WriteFeaturesResponse>> writeFeatures(
      @RequestBody @NotNull @Valid LegacyWriteFeaturesRequest legacyWriteFeaturesRequest)
      throws JsonProcessingException {
    WriteCassandraFeaturesRequest writeFeaturesRequest =
        LegacyWriteFeaturesRequest.getWriteFeaturesRequest(legacyWriteFeaturesRequest);
    JsonObject request = new JsonObject(objectMapper.writeValueAsString(writeFeaturesRequest));
    return featureGroupService
        .writeFeaturesV2(request, cassandra, true)
        .map(
            res ->
                LegacyFeatureStoreResponse.<WriteFeaturesResponse>builder()
                    .body(legacyWriteFeaturesRequest)
                    .message(WRITE_SUCCESSFUL_SUB_STRING + legacyWriteFeaturesRequest.getEntityName())
                    .code(SUCCESS_HTTP_STATUS_CODE)
                    .build())
        .to(CompletableFutureUtils::fromSingle);
  }

  @POST
  @Path("/async-batch-write")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<LegacyFeatureStoreResponse<LegacyBatchWriteResponse>> asyncBatchWrite(
      @RequestBody @NotNull @Valid LegacyBatchWriteFeaturesRequest legacyWriteFeaturesRequest) {
    // imp!!
    legacyWriteFeaturesRequest.patchRequest();
    return featureGroupService
        .legacyBatchWriteFeatures(legacyWriteFeaturesRequest)
        .to(CompletableFutureUtils::fromSingle);
  }

  @GET
  @Path("/get-data")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<LegacyFeatureStoreResponse<Object>> readData(
      @RequestBody @NotNull @Valid LegacyReadFeaturesRequest legacyReadFeaturesRequest) {
    // imp!!
    legacyReadFeaturesRequest.patchRequest();
    return featureGroupService
        .legacyReadFeatures(legacyReadFeaturesRequest)
        .to(CompletableFutureUtils::fromSingle);
  }

  @GET
  @Path("/bulk-read")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<LegacyFeatureStoreResponse<List<Map<String, Object>>>> bulkReadData(
      @RequestBody @NotNull @Valid LegacyBulkReadFeaturesRequest legacyBulkReadFeaturesRequest) {
    // imp!!
    legacyBulkReadFeaturesRequest.patchRequest();
    return featureGroupService
        .legacyBulkReadFeatures(legacyBulkReadFeaturesRequest)
        .to(CompletableFutureUtils::fromSingle);
  }

  @GET
  @Path("/multi-bulk-read")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<Map<String, List<LegacyFeatureStoreResponse<Object>>>>> bulkReadData(
      @RequestBody @NotNull @Valid LegacyMultiBulkReadFeaturesRequest legacyMultiBulkReadFeaturesRequest) {
    return featureGroupService.legacyMultiGetFeatures(legacyMultiBulkReadFeaturesRequest)
        .to(RestResponse.jaxrsRestHandler());
  }

  @POST
  @Path("/create-table")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<LegacyFeatureStoreResponse<Object>> createTable(
      @RequestBody @NotNull @Valid LegacyCreateTableRequest legacyCreateTableRequest) throws JsonProcessingException {
    return featureGroupService
        .legacyCreateTable(legacyCreateTableRequest)
        .to(CompletableFutureUtils::fromSingle);
  }

  @POST
  @Path("/alter-table")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<LegacyFeatureStoreResponse<Object>> createTable(
      @RequestBody @NotNull @Valid LegacyAlterTableRequest legacyAlterTableRequest) throws JsonProcessingException {
    return featureGroupService
        .legacyAlterTable(legacyAlterTableRequest)
        .to(CompletableFutureUtils::fromSingle);
  }
}
