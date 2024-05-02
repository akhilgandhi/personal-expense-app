package com.akhil.microservices.api.composite.dashboard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Tag(
        name = "Dashboard",
        description = "REST API for composite information on dashboard."
)
@SecurityRequirement(name = "security_auth")
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
    Mono<DashboardAggregate> getDashboardSummary(@PathVariable int accountId,
        @RequestParam(value = "delay", required = false, defaultValue = "0") int delay,
        @RequestParam(value = "faultPercent", required = false, defaultValue = "0") int faultPercent);

    @Operation(
            summary = "${api.dashboard.create-account-summary.description}",
            description = "${api.dashboard.create-account-summary.notes}"
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
            summary = "${api.dashboard.create-account.description}",
            description = "${api.dashboard.create-account.notes}"
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
                            responseCode = "422",
                            description = "${api.responseCodes.unprocessableEntity.description}"
                    )
    })
    @PostMapping(
            value = "/dashboard/account",
            consumes = "application/json"
    )
    Mono<Void> createAccount(@RequestBody AccountSummary body);

    @Operation(
            summary = "${api.dashboard.delete-account-summary.description}",
            description = "${api.dashboard.delete-account-summary.notes}"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "${api.responseCodes.ok.description}"
                    ),
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
            value = "/dashboard/account/{accountId}"
    )
    Mono<Void> deleteAccount(@PathVariable int accountId);

    @Operation(
            summary = "${api.dashboard.create-expense-summary.description}",
            description = "${api.dashboard.create-expense-summary.notes}"
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
            })
    @PostMapping(
            value = "/dashboard/account/{accountId}/expense"
    )
    Mono<Void> createExpense(@PathVariable int accountId, @RequestBody ExpenseSummary body);

    @Operation(
            summary = "${api.dashboard.delete-expense-summary.description}",
            description = "${api.dashboard.delete-expense-summary.notes}"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "${api.responseCodes.ok.description}"
                    ),
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
            value = "/dashboard/account/{accountId}/expense/{expenseId}"
    )
    Mono<Void> deleteExpense(@PathVariable int accountId, @PathVariable int expenseId);
}
