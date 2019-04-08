package it.polito.ai.mmap.esercitazione2.objectDTO;

import lombok.Data;

import java.util.ArrayList;

@Data
public class LineaDTO {
    private float id;
    private String nome;
    private String admin;
    ArrayList<FermataDTO> andata = new ArrayList<>();
    ArrayList<FermataDTO> ritorno = new ArrayList<>();
}