package it.polito.ai.mmap.esercitazione3.entity;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Data
@Document(collection = "children")
public class ChildEntity {

    private String name;
    private String surname;
}
