package it.polito.ai.mmap.pedibus.services;

import it.polito.ai.mmap.pedibus.configuration.MongoZonedDateTime;
import it.polito.ai.mmap.pedibus.entity.DispEntity;
import it.polito.ai.mmap.pedibus.entity.TurnoEntity;
import it.polito.ai.mmap.pedibus.entity.UserEntity;
import it.polito.ai.mmap.pedibus.exception.DispNotFoundException;
import it.polito.ai.mmap.pedibus.exception.PermissionDeniedException;
import it.polito.ai.mmap.pedibus.objectDTO.DispDTO;
import it.polito.ai.mmap.pedibus.objectDTO.FermataDTO;
import it.polito.ai.mmap.pedibus.objectDTO.LineaDTO;
import it.polito.ai.mmap.pedibus.objectDTO.TurnoDTO;
import it.polito.ai.mmap.pedibus.repository.DispRepository;
import it.polito.ai.mmap.pedibus.repository.TurnoRepository;
import it.polito.ai.mmap.pedibus.resources.DispAllResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class GestioneCorseService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    DispRepository dispRepository;

    @Autowired
    TurnoRepository turnoRepository;

    @Autowired
    LineeService lineeService;

    /**
     * Restituisce un TurnoEntity a partire dalle 3 "chiavi": idLinea, data e verso
     * Se il turno non è presente sul db per questa terna lo crea
     * Se il turno ha oltrepassato la sua data di scadenza (pedibus partito) viene chiuso o non viene creato
     *
     * @param turnoDTO
     * @return
     */
    private TurnoEntity getTurnoEntity(TurnoDTO turnoDTO) {
        Optional<TurnoEntity> checkTurno = turnoRepository.findByIdLineaAndDataAndVerso(turnoDTO.getIdLinea(), turnoDTO.getData(), turnoDTO.getVerso());
        TurnoEntity turnoEntity;
        Boolean isTurnoOpen = isTurnoPassed(turnoDTO);

        if (checkTurno.isPresent()) {
            turnoEntity = checkTurno.get();
            if (!isTurnoOpen)
                turnoEntity.setIsOpen(false);
        } else if (isTurnoOpen) {
            turnoEntity = new TurnoEntity(turnoDTO);
            turnoEntity.setIsOpen(true);
        } else
            throw new IllegalArgumentException("Il turno è chiuso"); //TODO eccezione custom (?)

        return turnoRepository.save(turnoEntity);
    }

    /**
     * Restituisce una dispEntity a partire dal turno e dalla persona indicata
     * @param turnoDTO
     * @param guideUsername
     * @return
     */
    private DispEntity getDispEntity(TurnoDTO turnoDTO, String guideUsername) {
        Optional<DispEntity> checkDisp = dispRepository.findByGuideUsernameAndTurnoId(guideUsername, getTurnoEntity(turnoDTO).getTurnoId());
        if (checkDisp.isPresent())
            return checkDisp.get();
        else
            throw new DispNotFoundException("Disponibilità non trovata");
    }

    /**
     * Salva la disponibilità, a patto che il turno sia aperto e la persona loggata ne abbia diritto
     * @param dispDTO
     */
    public void addDisp(DispDTO dispDTO) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            getDispEntity(dispDTO.getTurnoDTO(), principal.getUsername());
            return;
        } catch (DispNotFoundException e) {
            TurnoEntity turnoEntity = getTurnoEntity(dispDTO.getTurnoDTO());

            if (!turnoEntity.getIsOpen())
                throw new IllegalArgumentException("Il turno è chiuso"); //TODO eccezione custom (?)

            if (!lineeService.isAdminOrGuideLine(dispDTO.getTurnoDTO().getIdLinea()))
                throw new PermissionDeniedException("La guide/admin non è relativa alla linea indicata");

            dispRepository.save(new DispEntity(principal.getUsername(), dispDTO.getIdFermata(), turnoEntity.getTurnoId()));

        }
    }

    /**
     * Cancella la disponibilità per la persona loggata, a patto che esista
     * @param turnoDTO
     */
    public void deleteDisp(TurnoDTO turnoDTO) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        DispEntity dispEntity = getDispEntity(turnoDTO, principal.getUsername());
        if (getTurnoEntity(turnoDTO).getIsOpen())
            dispRepository.delete(dispEntity);
        else
            throw new IllegalArgumentException("Il turno è chiuso"); //TODO eccezione custom (?)
    }

    /**
     * Restituisce tutte le DispAllResource per un turno, sia quelle confirmed che quelle no
     * @param turnoDTO
     * @return
     */
    public List<DispAllResource> getAllTurnoDisp(TurnoDTO turnoDTO) {
        List<DispAllResource> dispResourceList = dispRepository.findAllByTurnoId(getTurnoEntity(turnoDTO).getTurnoId())
                .stream()
                .map(dispEntity -> new DispAllResource(dispEntity.getGuideUsername(), dispEntity.getIdFermata(), lineeService.getFermataEntityById(dispEntity.getIdFermata()).getName(), dispEntity.getIsConfirmed()))
                .collect(Collectors.toList());
        FermataDTO fermataDTO = lineeService.getFermataPartenzaOrArrivo(turnoDTO.getIdLinea(), turnoDTO.getVerso());
        dispResourceList.stream().filter(res -> res.getNomeFermata() == null).forEach(res -> res.setNomeFermata(fermataDTO.getNome()));
        return dispResourceList;
    }

    /**
     * Aggiorna lo stato isConfirmed per ogni disp e chiude automaticamente il turno
     * @param turnoDTO
     * @param resList
     */
    public void setAllTurnoDisp(TurnoDTO turnoDTO, List<DispAllResource> resList) {
        resList.forEach(res -> {
            DispEntity dispEntity = getDispEntity(turnoDTO, res.getGuideUsername());
            dispEntity.setIdFermata(res.getIdFermata());
            dispEntity.setIsConfirmed(res.getIsConfirmed());
            dispRepository.save(dispEntity);
            //TODO notifica

        });
        setTurnoState(turnoDTO, true);
    }

    /**
     * Permette a un admin di una linea di modificare lo stato del turno
     * @param turnoDTO
     * @param isOpen
     */
    public void setTurnoState(TurnoDTO turnoDTO, Boolean isOpen) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (lineeService.getLineaEntityById(turnoDTO.getIdLinea()).getAdminList().contains(principal.getUsername())) {
            TurnoEntity turnoEntity = getTurnoEntity(turnoDTO);
            turnoEntity.setIsOpen(isOpen);
            turnoRepository.save(turnoEntity);
        } else
            throw new PermissionDeniedException("Non possiedi i privilegi necessari");
    }

    /**
     * Aggiorniamo lo stato della conferma della guida
     * @param turnoDTO
     */
    public void ackDisp(TurnoDTO turnoDTO) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        DispEntity dispEntity = getDispEntity(turnoDTO, principal.getUsername());
        if (dispEntity.getIsConfirmed()) {
            dispEntity.setIsAck(true);
            dispRepository.save(dispEntity);
        }
        //todo else questa guida non era stata confermata per quel turno, quindi non dovrebbe mandare l'ack: ignoriamo o segnaliamo errore ?
    }

    private Boolean isTurnoPassed(TurnoDTO turnoDTO) {
        LineaDTO lineaDTO = lineeService.getLineaDTOById(turnoDTO.getIdLinea());
        String timeFermata = turnoDTO.getVerso() ? lineaDTO.getAndata().stream().min(FermataDTO::compareTo).get().getOrario() : lineeService.getPartenzaScuola();

        return MongoZonedDateTime.getMongoZonedDateTimeFromDateTime(turnoDTO.getData(), timeFermata).after(MongoZonedDateTime.getNow());

    }


}