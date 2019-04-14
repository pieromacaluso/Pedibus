package it.polito.ai.mmap.esercitazione2.entity;

import lombok.Data;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CompositeKeyPrenotazione implements Serializable {
    private String nomeAlunno;
    private String data;
    private boolean verso;

    //non cancellare
    public CompositeKeyPrenotazione()
    {

    }

    public CompositeKeyPrenotazione(String nomeAlunno, String data, boolean verso)
    {
        this.nomeAlunno = nomeAlunno;
        this.data = data;
        this.verso = verso;
    }

    public CompositeKeyPrenotazione(String idPrenotazione)
    {
        String[] key = idPrenotazione.split("_");
        nomeAlunno = key[0];
        data = key[1];
        verso = Boolean.parseBoolean(key[2]);

    }
    @Override
    public String toString()
    {
        return nomeAlunno + "_" + data + "_" + verso;
    }
}