package it.polito.ai.mmap.pedibus.objectDTO;

import it.polito.ai.mmap.pedibus.entity.FermataEntity;
import it.polito.ai.mmap.pedibus.entity.LineaEntity;
import it.polito.ai.mmap.pedibus.repository.FermataRepository;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class LineaDTO {
    private String id;
    private String nome;
    private String adminMast;
    private ArrayList<String> adminList;
    private ArrayList<String> guideList;
    ArrayList<FermataDTO> andata;
    ArrayList<FermataDTO> ritorno;

    /**
     * Crea una lineaDTO inserendo tutte le info sulle fermate
     *
     * @param lineaEntity
     * @param fermataRepository
     */
    public LineaDTO(LineaEntity lineaEntity, FermataRepository fermataRepository) {
        this.id = lineaEntity.getId();
        this.nome = lineaEntity.getNome();
        this.adminMast=lineaEntity.getAdminMast();
        this.adminList = lineaEntity.getAdminList();
        this.guideList = lineaEntity.getGuideList();
        this.andata = ((List<FermataEntity>) fermataRepository.findAllById(lineaEntity.getAndata())).stream().map(FermataDTO::new).collect(Collectors.toCollection(ArrayList::new));
        this.ritorno = ((List<FermataEntity>) fermataRepository.findAllById(lineaEntity.getRitorno())).stream().map(FermataDTO::new).collect(Collectors.toCollection(ArrayList::new));
    }
}
