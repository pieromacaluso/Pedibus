package it.polito.ai.mmap.esercitazione3.services;

import it.polito.ai.mmap.esercitazione3.entity.FermataEntity;
import it.polito.ai.mmap.esercitazione3.entity.LineaEntity;
import it.polito.ai.mmap.esercitazione3.entity.PrenotazioneEntity;
import it.polito.ai.mmap.esercitazione3.exception.FermataNotFoundException;
import it.polito.ai.mmap.esercitazione3.exception.LineaNotFoundException;
import it.polito.ai.mmap.esercitazione3.exception.PrenotazioneNotFoundException;
import it.polito.ai.mmap.esercitazione3.exception.PrenotazioneNotValidException;
import it.polito.ai.mmap.esercitazione3.objectDTO.FermataDTO;
import it.polito.ai.mmap.esercitazione3.objectDTO.LineaDTO;
import it.polito.ai.mmap.esercitazione3.objectDTO.PrenotazioneDTO;
import it.polito.ai.mmap.esercitazione3.repository.FermataRepository;
import it.polito.ai.mmap.esercitazione3.repository.LineaRepository;
import it.polito.ai.mmap.esercitazione3.repository.PrenotazioneRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LineeService {

    @Autowired
    LineaRepository lineaRepository;
    @Autowired
    FermataRepository fermataRepository;
    @Autowired
    PrenotazioneRepository prenotazioneRepository;
    @Autowired
    UserService userService;

    /**
     * Salva lista Fermate sul DB
     *
     * @param listaFermate lista fermate
     */
    void addFermate(List<FermataDTO> listaFermate) {

        fermataRepository.saveAll(listaFermate.stream().map(FermataEntity::new).collect(Collectors.toList()));
    }

    /**
     * Eliminazione di tutte le fermate dal DB
     */
    public void dropAllFermate() {
        fermataRepository.deleteAll();
    }

    /**
     * Eliminazione di tutte le linee dal DB
     */
    public void dropAllLinee() {
        lineaRepository.deleteAll();
    }

    /**
     * Eliminazione di tutte le prenotazioni dal DB
     */
    public void dropAllPrenotazioni() {
        prenotazioneRepository.deleteAll();
    }

    /**
     * Fermata da ID fermata
     *
     * @param idFermata ID fermata
     * @return FermataDTO
     */
     FermataDTO getFermataById(Integer idFermata) {
        Optional<FermataEntity> check = fermataRepository.findById(idFermata);
        if (check.isPresent()) {
            return new FermataDTO(check.get());
        } else {
            throw new FermataNotFoundException("Fermata con ID " + idFermata + " non trovata!");
        }
    }

    /**
     * Salva una linea sul DB
     *
     * @param lineaDTO Linea DTO
     */
    void addLinea(LineaDTO lineaDTO) {
        LineaEntity lineaEntity = new LineaEntity(lineaDTO);
        lineaRepository.save(lineaEntity);

    }

    /**
     * Restituisce una LineaDTO a partire dal suo nome
     *
     * @param lineName nome linea
     * @return LineaDTO
     */
    public LineaDTO getLineByName(String lineName) {
        Optional<LineaEntity> linea = lineaRepository.findByNome(lineName);
        if (linea.isPresent()) {
            return new LineaDTO(linea.get(), fermataRepository);
        } else {
            throw new LineaNotFoundException("Nessuna linea trovata con nome " + lineName);
        }
    }


    /**
     * Restituisce tutte le linee in DB
     *
     * @return Lista LineaEntity
     */
    public List<LineaEntity> getAllLines() {
        return lineaRepository.findAll();
    }

    /**
     * Restituisce tutti i nomi delle linee presenti in DB
     *
     * @return Lista di nomi linee
     */
    public List<String> getAllLinesNames() {
        return lineaRepository.findAll().stream().map(LineaEntity::getNome).collect(Collectors.toList());
    }




    /**
     * Aggiunge alla lista di admin di una linea l'user indicato
     * @param userID
     * @param nomeLinea
     */
    public void addAdminLine(String userID, String nomeLinea) {
        Optional<LineaEntity> check = lineaRepository.findByNome(nomeLinea);
        if (!check.isPresent()) {
            throw new LineaNotFoundException("Linea not found");
        }
        LineaEntity lineaEntity = check.get();
        ArrayList<String> adminList = lineaEntity.getAdminList();
        if (adminList == null)
            adminList = new ArrayList<>(Arrays.asList(userID));
        else if(!adminList.contains(userID))
            adminList.add(userID);

        lineaEntity.setAdminList(adminList);
        lineaRepository.save(lineaEntity);
    }

    public void delAdminLine(String userID, String nomeLinea) {
        Optional<LineaEntity> check = lineaRepository.findByNome(nomeLinea);
        if (!check.isPresent()) {
            throw new LineaNotFoundException("Linea not found");
        }
        LineaEntity lineaEntity = check.get();
        ArrayList<String> adminList = lineaEntity.getAdminList();
        if (adminList == null)
            adminList = new ArrayList<>(Arrays.asList(userID));
        else if(adminList.contains(userID))
            adminList.remove(userID);

        lineaEntity.setAdminList(adminList);
        lineaRepository.save(lineaEntity);
    }
}
