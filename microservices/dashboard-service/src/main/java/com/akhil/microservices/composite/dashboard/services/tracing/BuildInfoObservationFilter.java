package com.akhil.microservices.composite.dashboard.services.tracing;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation.Context;
import io.micrometer.observation.ObservationFilter;
import org.springframework.boot.info.BuildProperties;

public class BuildInfoObservationFilter implements ObservationFilter {

  private final BuildProperties buildProperties;

  public BuildInfoObservationFilter(BuildProperties buildProperties) {
    this.buildProperties = buildProperties;
  }

  @Override
  public Context map(Context context) {
    KeyValue buildVersion = KeyValue.of("build.version", buildProperties.getVersion());
    return context.addLowCardinalityKeyValue(buildVersion);
  }
}
