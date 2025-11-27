package com.dream11.app.providers;

import com.dream11.app.error.LegacyStackException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

@Provider
@Slf4j
public class LegacyResponseConverter implements ExceptionMapper<LegacyStackException> {
  public LegacyResponseConverter() {
  }

  public Response toResponse(LegacyStackException exception) {
    return Response.status(exception.getStatusCode()).entity(exception.getResponse()).build();
  }
}

