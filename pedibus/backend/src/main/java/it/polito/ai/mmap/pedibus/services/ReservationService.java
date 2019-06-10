package it.polito.ai.mmap.pedibus.services;

import it.polito.ai.mmap.pedibus.entity.ChildEntity;
import it.polito.ai.mmap.pedibus.entity.PrenotazioneEntity;
import it.polito.ai.mmap.pedibus.entity.RoleEntity;
import it.polito.ai.mmap.pedibus.entity.UserEntity;
import it.polito.ai.mmap.pedibus.exception.PrenotazioneNotFoundException;
import it.polito.ai.mmap.pedibus.exception.PrenotazioneNotValidException;
import it.polito.ai.mmap.pedibus.objectDTO.ChildDTO;
import it.polito.ai.mmap.pedibus.objectDTO.FermataDTO;
import it.polito.ai.mmap.pedibus.objectDTO.LineaDTO;
import it.polito.ai.mmap.pedibus.objectDTO.PrenotazioneDTO;
import it.polito.ai.mmap.pedibus.repository.ChildRepository;
import it.polito.ai.mmap.pedibus.repository.PrenotazioneRepository;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ReservationService {
    @Autowired
    PrenotazioneRepository prenotazioneRepository;

    @Autowired
    ChildRepository childRepository;
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
     * Controlla che i dettagli della prenotazione siano consistenti:
     * - La fermata è nel verso indicato
     * - Si sta cercando di prenotare per uno dei proprio children o system-admin o amministratore della linea
     *
     * @param prenotazioneDTO: Oggetto PrenotazioneDTO
     * @return True se la prenotazione è valida, altrimenti False
     */
    private Boolean isValidPrenotation(PrenotazioneDTO prenotazioneDTO) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        FermataDTO fermataDTO = lineeService.getFermataById(prenotazioneDTO.getIdFermata());
        LineaDTO lineaDTO = lineeService.getLineByName(prenotazioneDTO.getNomeLinea());

        return ((lineaDTO.getAndata().contains(fermataDTO) && prenotazioneDTO.getVerso()) ||
                (lineaDTO.getRitorno().contains(fermataDTO) && !prenotazioneDTO.getVerso())) &&
                (principal.getChildrenList().contains(prenotazioneDTO.getCfChild()) || principal.getRoleList().stream().map(RoleEntity::getRole).collect(Collectors.toList()).contains("ROLE_SYSTEM-ADMIN") || lineaDTO.getAdminList().contains(principal.getUsername()));
    }

    /**
     * Controlla che la prenotazione non esista già
     *
     * @param prenotazioneDTO: Oggetto PrenotazioneDTO
     * @return True se la prenotazione è duplicata, altrimenti False
     */
    private Boolean isDuplicate(PrenotazioneDTO prenotazioneDTO) {
        Optional<PrenotazioneEntity> check = prenotazioneRepository
                .findByCfChildAndDataAndVerso(
                        prenotazioneDTO.getCfChild(),
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
    public PrenotazioneEntity getReservationFromId(ObjectId reservationId) {
        Optional<PrenotazioneEntity> prenotazione = prenotazioneRepository.findById(reservationId);
        if (prenotazione.isPresent()) {
            return prenotazione.get();
        } else {
            throw new PrenotazioneNotFoundException("Prenotazione " + reservationId + " non trovata");
        }

    }

    /**
     * Prenotazione Entity da verso,data,idAlunno
     *
     * @param verso
     * @param data
     * @param cfChild
     * @return
     * @throws Exception
     */
    public PrenotazioneEntity getChildReservation(Boolean verso, String data, String cfChild) throws Exception {
        Date date = MongoZonedDateTime.getMongoZonedDateTimeFromDate(data);
        Optional<PrenotazioneEntity> check = prenotazioneRepository.findByCfChildAndDataAndVerso(cfChild, date, verso);
        if (check.isPresent()) {
            return check.get();
        } else {
            throw new PrenotazioneNotFoundException();
        }
    }

    /**
     * PrenotazioneDTO da ReservationId per controller
     *
     * @param reservationId
     * @return
     */
    public PrenotazioneDTO getPrenotazioneDTO(ObjectId reservationId) {
        return new PrenotazioneDTO(getReservationFromId(reservationId));
    }

    /**
     * Elimina la prenotazione indicata dall'objectId controllando:
     * - Dettagli siano consistenti
     * - Si sta cercando di cancellare uno dei proprio children o system-admin o amministratore della linea
     *
     * @param nomeLinea:     Nome della Linea
     * @param data:          Data
     * @param reservationId: Id Prenotazione
     */
    public void deletePrenotazione(String nomeLinea, Date data, ObjectId reservationId) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        PrenotazioneEntity prenotazione = getReservationFromId(reservationId);
        LineaDTO lineaDTO = lineeService.getLineByName(prenotazione.getNomeLinea());
        if (prenotazione.getData().equals(data) && lineeService.getLineByName(prenotazione.getNomeLinea()).getNome().equals(nomeLinea) && (principal.getChildrenList().contains(prenotazione.getCfChild()) || principal.getRoleList().stream().map(RoleEntity::getRole).collect(Collectors.toList()).contains("ROLE_SYSTEM-ADMIN") || lineaDTO.getAdminList().contains(principal.getUsername()))) {
            prenotazioneRepository.delete(prenotazione);
        } else {
            throw new IllegalArgumentException("Errore in cancellazione prenotazione");
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
    public List<ChildDTO> findAlunniFermata(Date data, Integer id, boolean verso) {
        List<PrenotazioneEntity> prenotazioni = prenotazioneRepository.findAllByDataAndIdFermataAndVerso(data, id, verso);
        List<String> cfList = prenotazioni.stream().map(PrenotazioneEntity::getCfChild).collect(Collectors.toList());
        return  ((List<ChildEntity>) childRepository.findAllById(cfList)).stream().map(ChildDTO::new).collect(Collectors.toList());
    }

    /**
     * Admin lina indica che ha preso l'alunno alla fermata
     *
     * @param verso
     * @param data
     * @param cfChild
     * @throws Exception
     */
    public void setHandled(Boolean verso, String data, String cfChild) throws Exception {
        PrenotazioneEntity prenotazioneEntity = getChildReservation(verso, data, cfChild);
        prenotazioneEntity.setPresoInCarico(true);
        prenotazioneRepository.save(prenotazioneEntity);
    }

    /**
     * Admin lina indica che ha lasciato l'alunno a scuola/fermata ritorno
     *
     * @param verso
     * @param data
     * @param cfChild
     * @throws Exception
     */
    public void setArrived(Boolean verso, String data, String cfChild) throws Exception {
        PrenotazioneEntity prenotazioneEntity = getChildReservation(verso, data, cfChild);
        prenotazioneEntity.setArrivatoScuola(true);
        prenotazioneRepository.save(prenotazioneEntity);
    }

    /**
     * ritorna tutti i bambini prenotati per una determinata giornata in una detrminata linea
     */
    public List<String> getAllChildrenForReservationDataVerso(Date data,boolean verso){
        List<PrenotazioneEntity> prenotazioniTotaliLineaDataVerso=prenotazioneRepository.findByDataAndVerso(data,verso);
        return  prenotazioniTotaliLineaDataVerso.stream().map(PrenotazioneEntity::getCfChild).collect(Collectors.toList());
    }
}
