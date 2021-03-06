package it.polito.ai.mmap.pedibus.entity;

import it.polito.ai.mmap.pedibus.objectDTO.FermataDTO;
import it.polito.ai.mmap.pedibus.objectDTO.LineaDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@Document(collection = "lines")
public class LineaEntity {

    @Id
    private String id;
    private String nome;
    private String masterMail;
    private String masterName;
    private String masterSurname;
    private Set<String> adminList;
    private ArrayList<Integer> andata;
    private ArrayList<Integer> ritorno;

    /**
     * Conversione da Entity a DTO
     *
     * @param lineaDTO
     */
    public LineaEntity(LineaDTO lineaDTO) {
        this.id = lineaDTO.getId();
        this.nome = lineaDTO.getNome();
        this.masterMail = lineaDTO.getMasterMail();
        this.masterName = lineaDTO.getMasterName();
        this.masterSurname = lineaDTO.getMasterSurname();
        this.adminList = lineaDTO.getAdminList();
        this.andata = lineaDTO.getAndata().stream().mapToInt(FermataDTO::getId).boxed().collect(Collectors.toCollection(ArrayList::new));
        this.ritorno = lineaDTO.getRitorno().stream().mapToInt(FermataDTO::getId).boxed().collect(Collectors.toCollection(ArrayList::new));
    }


}
