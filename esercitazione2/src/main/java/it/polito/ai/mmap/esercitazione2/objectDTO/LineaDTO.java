package it.polito.ai.mmap.esercitazione2.objectDTO;

import it.polito.ai.mmap.esercitazione2.entity.FermataEntity;
import it.polito.ai.mmap.esercitazione2.entity.LineaEntity;
import it.polito.ai.mmap.esercitazione2.services.MongoService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Data
public class LineaDTO {
    private Integer id;
    private String nome;
    private String admin;
    ArrayList<FermataDTO> andata;
    ArrayList<FermataDTO> ritorno;

    public LineaDTO() {
    }

    /**
     * Crea una lineaDTO inserendo tutte le info sulle fermate
     *
     * @param line
     * @param mongoService
     */
    public LineaDTO(LineaEntity line, MongoService mongoService) {
        this.id = line.getId();
        this.nome = line.getNome();
        this.admin = line.getAdmin();
        this.andata = mongoService.getFermate(line.getAndata()).stream().map(FermataDTO::new).collect(Collectors.toCollection(ArrayList::new));
        this.ritorno = mongoService.getFermate(line.getAndata()).stream().map(FermataDTO::new).collect(Collectors.toCollection(ArrayList::new));
    }
}
