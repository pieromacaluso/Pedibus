package it.polito.ai.mmap.pedibus.objectDTO;

import it.polito.ai.mmap.pedibus.entity.FermataEntity;
import it.polito.ai.mmap.pedibus.entity.LineaEntity;
import it.polito.ai.mmap.pedibus.repository.FermataRepository;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class LineaDTO {
    private String id;
    private String nome;
    private String masterMail;
    private String masterName;
    private String masterSurname;
    private Set<String> adminList;
    ArrayList<FermataDTO> andata;
    ArrayList<FermataDTO> ritorno;

    public LineaDTO(LineaEntity lineaEntity, FermataRepository fermataRepository) {
        this.id = lineaEntity.getId();
        this.nome = lineaEntity.getNome();
        this.masterMail = lineaEntity.getMasterMail();
        this.masterSurname = lineaEntity.getMasterSurname();
        this.adminList = lineaEntity.getAdminList();
        this.adminList = lineaEntity.getAdminList();
        this.andata = ((List<FermataEntity>) fermataRepository.findAllById(lineaEntity.getAndata())).stream().map(FermataDTO::new).collect(Collectors.toCollection(ArrayList::new));
        this.ritorno = ((List<FermataEntity>) fermataRepository.findAllById(lineaEntity.getRitorno())).stream().map(FermataDTO::new).collect(Collectors.toCollection(ArrayList::new));
    }
}
