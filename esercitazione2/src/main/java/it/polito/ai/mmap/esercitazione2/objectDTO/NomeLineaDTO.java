package it.polito.ai.mmap.esercitazione2.objectDTO;

import it.polito.ai.mmap.esercitazione2.controller.HomeController;
import it.polito.ai.mmap.esercitazione2.entity.LineaEntity;
import lombok.Data;
import org.springframework.hateoas.ResourceSupport;

@Data
public class NomeLineaDTO {
    private String nome;

    public NomeLineaDTO(String name){
        nome=name;
    }

    public NomeLineaDTO(LineaEntity linea){
        nome=linea.getNome();
    }

}
