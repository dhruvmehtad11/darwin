package com.dream11.populator.rest;

import com.dream11.common.util.ContextUtils;
import com.dream11.populator.service.ClusterManager;
import com.dream11.populator.service.ControllerService;
import com.dream11.rest.io.Response;
import com.dream11.rest.io.RestResponse;
import com.google.inject.Inject;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@Path("/")
public class ServerMetadata {

  private final ClusterManager clusterManager = ContextUtils.getInstance(ClusterManager.class);
  private final ControllerService controllerService = ContextUtils.getInstance(ControllerService.class);

  @GET
  @Path("/assignments")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<Map<String, Map<String, Integer>>>> getGlobalAssignments() {
    return clusterManager.rxGetAssignmentMap()
        .to(RestResponse.jaxrsRestHandler());
  }

  @GET
  @Path("/healthcheck")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<Map<String, String>>> healthcheck() {
    return controllerService.consumerHealthcheck()
        .to(RestResponse.jaxrsRestHandler());
  }
}
