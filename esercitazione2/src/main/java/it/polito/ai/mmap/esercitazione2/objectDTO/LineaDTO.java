package it.polito.ai.mmap.esercitazione2.objectDTO;

import it.polito.ai.mmap.esercitazione2.entity.FermataEntity;
import it.polito.ai.mmap.esercitazione2.entity.LineaEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class LineaDTO {
    private Integer id;
    private String nome;
    private String admin;
    ArrayList<FermataDTO> andata;
    ArrayList<FermataDTO> ritorno;

    public LineaDTO(){}

    /**
     * TODO sarebbe bello passargli solo la LineaEntity e poi usare un mongoService per recuperarsi le altre informazioni, ma @Autowired non funziona su oggetti dichairati con new (?)
     * Crea una lineaDTO inserendo tutte le info sulle fermate
     * @param line
     * @param andata
     * @param ritorno
     */
    public LineaDTO(LineaEntity line, List<FermataEntity> andata,List<FermataEntity> ritorno) {
        this.id = line.getId();
        this.nome = line.getNome();
        this.admin = line.getAdmin();
        this.andata = andata.stream().map(FermataDTO::new).collect(Collectors.toCollection(ArrayList::new));
        this.ritorno = ritorno.stream().map(FermataDTO::new).collect(Collectors.toCollection(ArrayList::new));
    }
}
