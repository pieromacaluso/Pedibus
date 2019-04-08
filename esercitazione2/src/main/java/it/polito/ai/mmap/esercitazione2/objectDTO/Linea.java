package it.polito.ai.mmap.esercitazione2.objectDTO;

import lombok.Data;
import java.util.ArrayList;

@Data
public class Linea {
    private float id;
    private String nome;
    private String admin;
    ArrayList<Fermata> andata = new ArrayList<>();
    ArrayList<Fermata> ritorno = new ArrayList<>();
}