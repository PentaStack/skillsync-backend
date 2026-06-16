package com.pentastack.skillsync.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiErrorResponse {

    private String message;
    private int status;
    private Map<String, List<String>> errors;
}
