package it.polito.ai.mmap.esercitazione3.entity;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Data
@Document(collection = "children")
public class ChildEntity {

    @Id
    private ObjectId id;

    private String name;
    private String surname;


    public ChildEntity(){}


    public ChildEntity(String nome,String cognome){
        name=nome;
        surname=cognome;
        //todo salvare
         }
}

