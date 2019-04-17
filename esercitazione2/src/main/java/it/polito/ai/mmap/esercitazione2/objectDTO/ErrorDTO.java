package it.polito.ai.mmap.esercitazione2.objectDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorDTO {
    private String errorMessage;
}
