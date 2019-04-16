package it.polito.ai.mmap.esercitazione2.entity;

import it.polito.ai.mmap.esercitazione2.objectDTO.FermataDTO;
import it.polito.ai.mmap.esercitazione2.objectDTO.LineaDTO;
import it.polito.ai.mmap.esercitazione2.repository.FermataRepository;
import it.polito.ai.mmap.esercitazione2.services.MongoService;
import lombok.Data;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Document(collection = "lines")
public class LineaEntity {

    @Id
    private Integer id;
    private String nome;
    private String admin;
    private ArrayList<String> andata;
    private ArrayList<String> ritorno;
    private String ultimaModifica;



    public LineaEntity() {

    }

    /**
     * Conversione da Entity a DTO
     *
     * @param lineaDTO
     */
    public LineaEntity(LineaDTO lineaDTO, FermataRepository fermataRepository) {

        this.id = lineaDTO.getId();
        this.nome = lineaDTO.getNome();
        this.admin = lineaDTO.getAdmin();
//TODO il repository non dovrebbe servire
        this.andata = fermataRepository.findAllByNomeLineaAndVerso(nome,true).stream().map(fermataEntity -> fermataEntity.getId()).collect(Collectors.toCollection(ArrayList::new)); //TODO True è andata giusto ?
        this.ritorno = fermataRepository.findAllByNomeLineaAndVerso(nome,false).stream().map(fermataEntity -> fermataEntity.getId()).collect(Collectors.toCollection(ArrayList::new));
        this.ultimaModifica = "" + LocalDate.now().getDayOfMonth() + "-" + LocalDate.now().getMonthValue() + "-" + LocalDate.now().getYear();
    }



}
