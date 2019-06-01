package it.polito.ai.mmap.esercitazione3.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@Document(collection = "children")
public class ChildEntity {

    @Id
    private String codiceFiscale; //Se si cambia questo campo bisogna cambiare "$.alunniPerFermataAndata[0].alunni[0].codiceFiscale" in test2
    private String name;
    private String surname;


    public ChildEntity(){}


    public ChildEntity(String nome,String cognome){
        name=nome;
        surname=cognome;
        //todo salvare
         }
}

