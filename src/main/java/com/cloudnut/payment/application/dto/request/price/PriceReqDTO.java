package com.cloudnut.payment.application.dto.request.price;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PriceReqDTO {
    @NotBlank
    private String name;

    @NotNull
    @NotEmpty
    private String accountType;

    private String description;

    @NotNull
    private Long price;

    @NotNull
    private Long amount;
}
