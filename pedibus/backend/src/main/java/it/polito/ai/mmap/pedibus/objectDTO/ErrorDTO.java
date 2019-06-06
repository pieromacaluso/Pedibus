package it.polito.ai.mmap.pedibus.objectDTO;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder
public class ErrorDTO {
    private Timestamp timestamp;
    private String errorMessage;
    private String exception;
    private Integer status;
    private String error;
    private String path;

    public String toString(){
        return exception + " [" + status + "] " + "(" + path + ") " + errorMessage ;
    }
}
