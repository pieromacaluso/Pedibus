package it.polito.ai.mmap.esercitazione2.entity;

import lombok.Value;

import java.io.Serializable;
import java.time.LocalDate;

//TODO capire se Serializable serve

public class CompositeKeyPrenotazione implements Serializable {
    private String nomeAlunno;
    private LocalDate data;
    private boolean verso;

    public CompositeKeyPrenotazione(String nomeAlunno, LocalDate data, boolean verso)
    {
        this.nomeAlunno = nomeAlunno;
        this.data = data;
        this.verso = verso;
    }

    public CompositeKeyPrenotazione(String idPrenotazione)
    {
        String[] key = idPrenotazione.split("_");
        nomeAlunno = key[0];
        data = LocalDate.parse(key[1]);
        verso = Boolean.parseBoolean(key[2]);

    }
    @Override
    public String toString()
    {
        return nomeAlunno + "_" + data.toString() + "_" + verso;
    }
}