package com.dream11.admin.rest;

import com.dream11.admin.service.PopulatorManagerService;
import com.dream11.core.dto.populator.PopulatorGroupMetadata;
import com.dream11.rest.io.Response;
import com.dream11.rest.io.RestResponse;
import com.google.inject.Inject;
import io.reactivex.Single;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import java.util.List;
import java.util.concurrent.CompletionStage;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@Path("/populator")
public class Populator {
  private final PopulatorManagerService populatorManagerService;

  @GET
  @Path("/metadata/all")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<List<PopulatorGroupMetadata>>> getAllPopulatorMetadata() {
    return populatorManagerService.getAllPopulatorMetadata().to(RestResponse.jaxrsRestHandler());
  }

  @PUT
  @Path("/metadata")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<PopulatorGroupMetadata>> putPopulatorMetadata(
      @RequestBody PopulatorGroupMetadata request) {
    return populatorManagerService
        .putPopulatorGroupMetadata(request)
        .andThen(Single.just(request))
        .to(RestResponse.jaxrsRestHandler());
  }
}
