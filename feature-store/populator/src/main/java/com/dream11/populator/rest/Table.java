package com.dream11.populator.rest;

import com.dream11.common.util.ContextUtils;
import com.dream11.core.dto.populator.AddTableRequest;
import com.dream11.core.dto.populator.AddTableSinceTimestampRequest;
import com.dream11.core.dto.populator.GetTableLatestVersionResponse;
import com.dream11.core.dto.populator.GetTableSchemaResponse;
import com.dream11.core.dto.populator.ResetTableResponse;
import com.dream11.populator.deltareader.TableReader;
import com.dream11.populator.model.TableReaderMetadata;
import com.dream11.populator.util.DataTypeUtils;
import com.dream11.rest.io.Response;
import com.dream11.rest.io.RestResponse;
import com.google.inject.Inject;
import io.delta.kernel.types.StructType;
import io.reactivex.Single;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.vertx.reactivex.core.Vertx;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicLong;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@Path("/table")
public class Table {
  private final Vertx vertx = Vertx.currentContext().owner();
  private final TableReader tableReader = ContextUtils.getInstance(TableReader.class);

  @GET
  @Path("/latest-version")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<GetTableLatestVersionResponse>> getTableLatestVersion(
      @QueryParam("path") String path) {
    return vertx
        .<GetTableLatestVersionResponse>rxExecuteBlocking(
            promise -> {
              try {
                long latestVersion = tableReader.getLatestVersion(path);
                GetTableLatestVersionResponse response =
                    GetTableLatestVersionResponse.builder().latestVersion(latestVersion).path(path).build();
                promise.complete(response);
              } catch (Exception e) {
                log.error("error onboarding table to populator", e);
                promise.fail(e);
              }
            })
        .toSingle()
        .to(RestResponse.jaxrsRestHandler());
  }

  @GET
  @Path("/schema")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<GetTableSchemaResponse>> getTableSchema(
      @QueryParam("path") String path) {
    return vertx
        .<GetTableSchemaResponse>rxExecuteBlocking(
            promise -> {
              try {
                StructType schema = tableReader.getTableSchema(path);
                GetTableSchemaResponse response =
                    GetTableSchemaResponse.builder().schema(DataTypeUtils.parseSchema(schema)).path(path).build();
                promise.complete(response);
              } catch (Exception e) {
                log.error("error onboarding table to populator", e);
                promise.fail(e);
              }
            })
        .toSingle()
        .to(RestResponse.jaxrsRestHandler());
  }

  @POST
  @Path("/reset")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<ResetTableResponse>> resetTable(
      @QueryParam("name") String name) {
    return vertx
        .<ResetTableResponse>rxExecuteBlocking(
            promise -> {
              try {
                tableReader.resetTable(name);
                ResetTableResponse response =
                    ResetTableResponse.builder().name(name).build();
                promise.complete(response);
              } catch (Exception e) {
                log.error("error onboarding table to populator", e);
                promise.fail(e);
              }
            })
        .toSingle()
        .to(RestResponse.jaxrsRestHandler());
  }

  @POST
  @Path("/read")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<AddTableRequest>> putTableMetadataForRead(
      @RequestBody AddTableRequest addTableRequest) {
    return vertx
        .rxExecuteBlocking(
            promise -> {
              try {
                long latestVersion = tableReader.getLatestVersion(addTableRequest.getPath());
                if (addTableRequest.getEndVersion() > latestVersion)
                  promise.fail(
                      new RuntimeException("end version cannot be greater than latest version"));
                tableReader.setTableReaderMetadata(
                    TableReaderMetadata.builder()
                        .name(addTableRequest.getTableName())
                        .path(addTableRequest.getPath())
                        .tenantName(addTableRequest.getTenantName())
                        .featureGroupName(addTableRequest.getFeatureGroupName())
                        .featureGroupVersion(addTableRequest.getFeatureGroupVersion())
                        .topicName(addTableRequest.getTopicName())
                        .runId(addTableRequest.getRunId())
                        .replicateWrites(addTableRequest.getReplicateWrites())
                        .startVersion(addTableRequest.getStartVersion())
                        .endVersion(addTableRequest.getEndVersion())
                        .build());
                promise.complete();
              } catch (Exception e) {
                log.error("error onboarding table to populator", e);
                promise.fail(e);
              }
            })
        .ignoreElement()
        .andThen(Single.just(addTableRequest))
        .to(RestResponse.jaxrsRestHandler());
  }

  @POST
  @Path("/read-since-timestamp")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<AddTableRequest>> putTableMetadataForRead(
      @RequestBody AddTableSinceTimestampRequest addTableRequest) {
    AtomicLong latestVersion = new AtomicLong();
    AtomicLong startVersion = new AtomicLong();
    return vertx
        .rxExecuteBlocking(
            promise -> {
              try {
                latestVersion.set(tableReader.getLatestVersion(addTableRequest.getPath()));
                startVersion.set(
                    tableReader.getVersionAsOfTimestamp(
                        addTableRequest.getPath(), addTableRequest.getTimestamp()));

                tableReader.setTableReaderMetadata(
                    TableReaderMetadata.builder()
                        .name(addTableRequest.getTableName())
                        .path(addTableRequest.getPath())
                        .tenantName(addTableRequest.getTenantName())
                        .featureGroupName(addTableRequest.getFeatureGroupName())
                        .featureGroupVersion(addTableRequest.getFeatureGroupVersion())
                        .topicName(addTableRequest.getTopicName())
                        .startVersion(startVersion.get())
                        .endVersion(latestVersion.get())
                        .build());
                promise.complete();
              } catch (Exception e) {
                log.error("error onboarding table to populator", e);
                promise.fail(e);
              }
            })
        .ignoreElement()
        .andThen(
            Single.just(
                AddTableSinceTimestampRequest.getRequest(
                    addTableRequest, startVersion.get(), latestVersion.get())))
        .to(RestResponse.jaxrsRestHandler());
  }
}
