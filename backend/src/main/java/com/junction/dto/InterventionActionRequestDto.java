package com.junction.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterventionActionRequestDto {
    private String rationale;
    private String reason;
}

