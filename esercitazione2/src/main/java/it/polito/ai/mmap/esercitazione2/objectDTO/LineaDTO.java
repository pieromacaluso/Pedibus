package it.polito.ai.mmap.esercitazione2.objectDTO;

import it.polito.ai.mmap.esercitazione2.entity.FermataEntity;
import it.polito.ai.mmap.esercitazione2.entity.LineaEntity;
import it.polito.ai.mmap.esercitazione2.repository.FermataRepository;
import it.polito.ai.mmap.esercitazione2.services.MongoService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Data
public class LineaDTO {
    private Integer id;
    private String nome;
    private String admin;
    private ArrayList<FermataDTO> andata;
    private ArrayList<FermataDTO> ritorno;

    public LineaDTO() {
    }

    /**
     * Crea una lineaDTO inserendo tutte le info sulle fermate
     *
     * @param line
     * @param
     */
    public LineaDTO(LineaEntity line, FermataRepository fermataRepository) {
        this.id = line.getId();
        this.nome = line.getNome();
        this.admin = line.getAdmin();

        //TODO il repository non dovrebbe servire e manca il sort (?)
        //fermate.sort(Comparator.comparing(FermataEntity::getOrario));
        //this.andata = fermataRepository.findAllById(line.getAndata()).stream().map(fermataEntity -> new FermataDTO(fermataEntity)).collect(Collectors.toCollection(ArrayList::new));
        //this.ritorno = fermataRepository.findAllById(line.getRitorno()).stream().map(fermataEntity -> new FermataDTO(fermataEntity)).collect(Collectors.toCollection(ArrayList::new));
        this.andata = fermataRepository.findAllByNomeLineaAndVerso(nome,true).stream().map(fermataEntity -> new FermataDTO(fermataEntity)).collect(Collectors.toCollection(ArrayList::new)); //TODO True è andata giusto ?
        this.ritorno = fermataRepository.findAllByNomeLineaAndVerso(nome,false).stream().map(fermataEntity -> new FermataDTO(fermataEntity)).collect(Collectors.toCollection(ArrayList::new));

    }
}
