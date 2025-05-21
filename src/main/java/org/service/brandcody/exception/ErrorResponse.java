package org.service.brandcody.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "오류 응답 모델")
public class ErrorResponse {
    @Schema(description = "오류 발생 시간", example = "2023-01-01T12:00:00")
    private final LocalDateTime timestamp;
    
    @Schema(description = "HTTP 상태 코드", example = "400")
    private final int status;
    
    @Schema(description = "오류 유형", example = "Bad Request")
    private final String error;
    
    @Schema(description = "오류 상세 메시지", example = "Brand with name 'TestBrand' already exists")
    private final String message;
    
    public static ErrorResponse of(HttpStatus status, String message) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message
        );
    }
}