package it.polito.ai.mmap.esercitazione3.entity;

import it.polito.ai.mmap.esercitazione3.objectDTO.FermataDTO;
import it.polito.ai.mmap.esercitazione3.objectDTO.LineaDTO;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Data
@Document(collection = "lines")
public class LineaEntity {

    @Id
    private Integer id;
    private String nome;
    private String admin;
    private ArrayList<Integer> andata;
    private ArrayList<Integer> ritorno;
    private String ultimaModifica;



    public LineaEntity() {

    }

    /**
     * Conversione da Entity a DTO
     *
     * @param lineaDTO
     */
    public LineaEntity(LineaDTO lineaDTO) {
        this.id = lineaDTO.getId();
        this.nome = lineaDTO.getNome();
        this.admin = lineaDTO.getAdmin();
        this.andata = lineaDTO.getAndata().stream().mapToInt(FermataDTO::getId).boxed().collect(Collectors.toCollection(ArrayList::new));
        this.ritorno = lineaDTO.getRitorno().stream().mapToInt(FermataDTO::getId).boxed().collect(Collectors.toCollection(ArrayList::new));
        this.ultimaModifica = "" + LocalDate.now().getDayOfMonth() + "-" + LocalDate.now().getMonthValue() + "-" + LocalDate.now().getYear();
    }



}
