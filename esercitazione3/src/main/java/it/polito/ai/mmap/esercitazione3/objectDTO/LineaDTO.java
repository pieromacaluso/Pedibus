package it.polito.ai.mmap.esercitazione3.objectDTO;

import it.polito.ai.mmap.esercitazione3.entity.FermataEntity;
import it.polito.ai.mmap.esercitazione3.entity.LineaEntity;
import it.polito.ai.mmap.esercitazione3.repository.FermataRepository;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
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
     * @param lineaEntity
     * @param fermataRepository
     */
    public LineaDTO(LineaEntity lineaEntity, FermataRepository fermataRepository) {
        this.id = lineaEntity.getId();
        this.nome = lineaEntity.getNome();
        this.admin = lineaEntity.getAdmin();

        this.andata = ((List<FermataEntity>) fermataRepository.findAllById(lineaEntity.getAndata())).stream().map(FermataDTO::new).collect(Collectors.toCollection(ArrayList::new));
        this.ritorno = ((List<FermataEntity>) fermataRepository.findAllById(lineaEntity.getRitorno())).stream().map(FermataDTO::new).collect(Collectors.toCollection(ArrayList::new));
    }
}
