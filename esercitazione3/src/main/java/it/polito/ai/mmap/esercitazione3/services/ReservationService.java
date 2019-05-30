package it.polito.ai.mmap.esercitazione3.services;

import it.polito.ai.mmap.esercitazione3.entity.PrenotazioneEntity;
import it.polito.ai.mmap.esercitazione3.exception.PrenotazioneNotFoundException;
import it.polito.ai.mmap.esercitazione3.exception.PrenotazioneNotValidException;
import it.polito.ai.mmap.esercitazione3.objectDTO.FermataDTO;
import it.polito.ai.mmap.esercitazione3.objectDTO.PrenotazioneDTO;
import it.polito.ai.mmap.esercitazione3.repository.PrenotazioneRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservationService {
    
    @Autowired
    PrenotazioneRepository prenotazioneRepository;
    @Autowired
    LineeService lineeService;
   

    /**
     * Aggiunge una nuova prenotazione al db. Controlla:
     * - se per quella Linea esiste una fermata con quel id
     * - se esiste già una prenotazione per lo stesso utente nello stesso giorno con lo stesso verso, in tal caso l'inserimento non viene eseguito
     *
     * @param prenotazioneDTO PrenotazioneDTO
     */
    public String addPrenotazione(PrenotazioneDTO prenotazioneDTO) {
        if (isValidPrenotation(prenotazioneDTO) && !isDuplicate(prenotazioneDTO)) {
            PrenotazioneEntity prenotazioneEntity = new PrenotazioneEntity(prenotazioneDTO);
            return prenotazioneRepository.save(prenotazioneEntity).getId().toString();
        } else
            throw new PrenotazioneNotValidException("Prenotazione già presente o non valida");
    }

    /**
     * Metodo utilizzato per aggiornare una vecchia prenotazione tramite il suo reservationId con uno dei
     * nuovi campi passati dall'utente
     *
     * @param prenotazioneDTO contiene i nuovi dati
     */
    public void updatePrenotazione(PrenotazioneDTO prenotazioneDTO, ObjectId reservationId) {
        Optional<PrenotazioneEntity> checkPrenotazione = prenotazioneRepository.findById(reservationId);
        if (isValidPrenotation(prenotazioneDTO) && checkPrenotazione.isPresent()) {
            PrenotazioneEntity prenotazioneEntity = checkPrenotazione.get();
            prenotazioneEntity.update(prenotazioneDTO);
            prenotazioneRepository.save(prenotazioneEntity);
        } else {
            throw new IllegalArgumentException("Aggiornamento prenotazione non valida");
        }
    }

    /**
     * Controlla che i dettagli della prenotazione siano consistenti.
     *
     * @param prenotazioneDTO: Oggetto PrenotazioneDTO
     * @return True se la prenotazione è valida, altrimenti False
     */
    private Boolean isValidPrenotation(PrenotazioneDTO prenotazioneDTO) {
        FermataDTO fermataDTO = lineeService.getFermataById(prenotazioneDTO.getIdFermata());
        return (lineeService.getLineByName(prenotazioneDTO.getNomeLinea()).getAndata().contains(fermataDTO) && prenotazioneDTO.getVerso()) ||
                (lineeService.getLineByName(prenotazioneDTO.getNomeLinea()).getRitorno().contains(fermataDTO) && !prenotazioneDTO.getVerso());
    }

    /**
     * Controlla che la prenotazione non esista già
     *
     * @param prenotazioneDTO: Oggetto PrenotazioneDTO
     * @return True se la prenotazione è duplicata, altrimenti False
     */
    private Boolean isDuplicate(PrenotazioneDTO prenotazioneDTO) {
        Optional<PrenotazioneEntity> check = prenotazioneRepository
                .findByNomeAlunnoAndDataAndVerso(
                        prenotazioneDTO.getNomeAlunno(),
                        prenotazioneDTO.getData(),
                        prenotazioneDTO.getVerso());
        return check.isPresent();
    }

    /**
     * PrenotazioneEntity da ReservationID
     *
     * @param reservationId ReservationID
     * @return PrenotazioneEntity
     */
    public PrenotazioneEntity getPrenotazione(ObjectId reservationId) {
        Optional<PrenotazioneEntity> prenotazione = prenotazioneRepository.findById(reservationId);
        if (prenotazione.isPresent()) {
            return prenotazione.get();
        } else {
            throw new PrenotazioneNotFoundException("Prenotazione " + reservationId + " non trovata");
        }

    }

    /**
     * Elimina la prenotazione indicata dall'objectId controllando che i dettagli siano consistenti
     *
     * @param nomeLinea:     Nome della Linea
     * @param data:          Data
     * @param reservationId: Id Prenotazione
     */
    public void deletePrenotazione(String nomeLinea, Date data, ObjectId reservationId) {
        PrenotazioneEntity prenotazione = getPrenotazione(reservationId);
        if (prenotazione.getData().equals(data) && lineeService.getLineByName(prenotazione.getNomeLinea()).getNome().equals(nomeLinea)) {
            prenotazioneRepository.delete(prenotazione);
        } else {
            throw new IllegalArgumentException("Errore in cancellazione");
        }
    }

    /**
     * Lista Alunni da Data, IdFermata e Verso
     *
     * @param data  Data
     * @param id    IdFermata
     * @param verso verso
     * @return Lista di nomi alunni
     */
    public List<String> findAlunniFermata(Date data, Integer id, boolean verso) {
        List<PrenotazioneEntity> prenotazioni = prenotazioneRepository.findAllByDataAndIdFermataAndVerso(data, id, verso);
        return prenotazioni.stream().map(PrenotazioneEntity::getNomeAlunno).collect(Collectors.toList());

    }
}
