package com.akhil.microservices.api.composite.dashboard;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface DashboardCompositeService  {

    @GetMapping(
            value = "/dashboard/{accountId}",
            produces = "application/json"
    )
    DashboardAggregate getAccountSummary(@PathVariable int accountId);
}
