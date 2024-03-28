package com.akhil.microservices.api.composite.dashboard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Tag(
        name = "Dashboard",
        description = "REST API for composite information on dashboard."
)
public interface DashboardCompositeService  {

    @Operation(
            summary = "${api.dashboard.get-account-summary.description}",
            description = "${api.dashboard.get-account-summary.notes}"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "${api.responseCodes.ok.description}"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "${api.responseCodes.badRequest.description}"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "${api.responseCodes.notFound.description}"
                    ),
                    @ApiResponse(
                            responseCode = "422",
                            description = "${api.responseCodes.unprocessableEntity.description}"
                    )
            }
    )
    @GetMapping(
            value = "/dashboard/{accountId}",
            produces = "application/json"
    )
    Mono<DashboardAggregate> getAccountSummary(@PathVariable int accountId);

    @Operation(
            summary = "${api.dashboard.create-account-summary.description}",
            description = "{api.dashboard.create-account-summary.notes}"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "400",
                            description = "${api.responseCodes.badRequest.description}"
                    ),
                    @ApiResponse(
                            responseCode = "422",
                            description = "${api.responseCodes.unprocessableEntity.description}"
                    )
    })
    @PostMapping(
            value = "/dashboard",
            consumes = "application/json"
    )
    Mono<Void> createAccount(@RequestBody DashboardAggregate body);

    @Operation(
            summary = "${api.dashboard.delete-account-summary.description}",
            description = "${api.dashboard.delete-account-summary.notes}"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "400",
                            description = "${api.responseCodes.badRequest.description}"
                    ),
                    @ApiResponse(
                            responseCode = "422",
                            description = "${api.responseCodes.unprocessableEntity.description}"
                    )
            }
    )
    @DeleteMapping(
            value = "/dashboard/{accountId}"
    )
    Mono<Void> deleteAccount(@PathVariable int accountId);
}
