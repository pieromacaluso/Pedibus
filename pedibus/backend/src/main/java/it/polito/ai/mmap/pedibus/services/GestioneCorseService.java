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
import it.polito.ai.mmap.pedibus.resources.DispTurnoResource;
import it.polito.ai.mmap.pedibus.resources.TurnoResource;
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
    @Autowired
    DispRepository dispRepository;
    @Autowired
    TurnoRepository turnoRepository;
    @Autowired
    UserService userService;
    @Autowired
    LineeService lineeService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

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
        Boolean turnoExpired = isTurnoExpired(turnoDTO);

        if (checkTurno.isPresent()) {
            turnoEntity = checkTurno.get();
            turnoEntity.setIsExpired(turnoExpired);
        } else {
            turnoEntity = new TurnoEntity(turnoDTO);
            turnoEntity.setIsOpen(!turnoExpired);
            turnoEntity.setIsExpired(turnoExpired);
            //throw new IllegalArgumentException("Il turno è chiuso"); //TODO eccezione custom (?)
        }
        turnoRepository.save(turnoEntity);
        return turnoEntity;
    }

    /**
     * Restituisce una dispEntity a partire dal turno e dalla persona indicata
     *
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
     * Restituisce una dispEntity a partire dal turno e dalla persona indicata
     *
     * @param turnoDTO
     * @return
     */
    public DispTurnoResource getDisp(TurnoDTO turnoDTO) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        DispAllResource disp;
        TurnoResource turnoResource;
        try {
            TurnoEntity turnoEntity = getTurnoEntity(turnoDTO);
            turnoResource = new TurnoResource(turnoEntity);

        } catch (IllegalArgumentException e) {
            TurnoEntity turnoEntity = new TurnoEntity(turnoDTO);
            turnoResource = new TurnoResource(turnoEntity);
        }

        try {
            DispEntity dispEntity = getDispEntity(turnoDTO, principal.getUsername());
            disp = new DispAllResource(dispEntity, lineeService.getFermataEntityById(dispEntity.getIdFermata()).getName());
        } catch (DispNotFoundException e) {
            disp = null;
        }
        DispTurnoResource result = new DispTurnoResource(disp, turnoResource);
        return result;


    }

    /**
     * Salva la disponibilità, a patto che il turno sia aperto e la persona loggata ne abbia diritto
     *
     * @param dispDTO
     */
    public void addDisp(DispDTO dispDTO) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            getDispEntity(dispDTO.getTurnoDTO(), principal.getUsername());
            return;
        } catch (DispNotFoundException e) {
            TurnoEntity turnoEntity = getTurnoEntity(dispDTO.getTurnoDTO());

            if (turnoEntity.getIsExpired())
                throw new IllegalArgumentException("Il turno è chiuso e scaduto"); //TODO eccezione custom (?)
            if (!turnoEntity.getIsOpen())
                throw new IllegalArgumentException("Il turno è chiuso"); //TODO eccezione custom (?)
            if (!this.userService.isGuide())
                throw new PermissionDeniedException("Accesso negato, l'utente non è guida");

            dispRepository.save(new DispEntity(principal.getUsername(), dispDTO.getIdFermata(), turnoEntity.getTurnoId()));

        }
    }

    /**
     * Cancella la disponibilità per la persona loggata, a patto che esista
     *
     * @param turnoDTO
     */
    public void deleteDisp(TurnoDTO turnoDTO) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        DispEntity dispEntity = getDispEntity(turnoDTO, principal.getUsername());
        if (getTurnoEntity(turnoDTO).getIsOpen() && !getTurnoEntity(turnoDTO).getIsExpired())
            dispRepository.delete(dispEntity);
        else
            throw new IllegalArgumentException("Il turno è chiuso"); //TODO eccezione custom (?)
    }

    /**
     * Restituisce tutte le DispAllResource per un turno, sia quelle confirmed che quelle no
     *
     * @param turnoDTO
     * @return
     */
    public List<DispAllResource> getAllTurnoDisp(TurnoDTO turnoDTO) {
        List<DispAllResource> dispResourceList = dispRepository.findAllByTurnoId(getTurnoEntity(turnoDTO).getTurnoId())
                .stream()
                .map(dispEntity -> new DispAllResource(dispEntity, lineeService.getFermataEntityById(dispEntity.getIdFermata()).getName()))
                .collect(Collectors.toList());
        FermataDTO fermataDTO = lineeService.getFermataPartenzaOrArrivo(turnoDTO.getIdLinea(), turnoDTO.getVerso());
        dispResourceList.stream().filter(res -> res.getNomeFermata() == null).forEach(res -> res.setNomeFermata(fermataDTO.getNome()));
        return dispResourceList;
    }

    /**
     * Aggiorna lo stato isConfirmed per ogni disp e chiude automaticamente il turno
     * Controlla che prima il turno sia stato chiuso tramite PUT /turno/state/{idLinea}/{verso}/{data}
     *
     * @param turnoDTO
     * @param resList
     */
    public void setAllTurnoDisp(TurnoDTO turnoDTO, List<DispAllResource> resList) {
        if (!getTurnoEntity(turnoDTO).getIsOpen()) {
            if (!getTurnoEntity(turnoDTO).getIsExpired()) {
                resList.forEach(res -> {
                    DispEntity dispEntity = getDispEntity(turnoDTO, res.getGuideUsername());
                    dispEntity.setIdFermata(res.getIdFermata());
                    dispEntity.setIsConfirmed(res.getIsConfirmed());
                    dispRepository.save(dispEntity);
                    //TODO notifica
                });
            } else throw new IllegalArgumentException("Il turno è scaduto"); //TODO eccezione custom (?)
        } else
            throw new IllegalArgumentException("Il turno deve essere chiuso"); //TODO eccezione custom (?)
    }

    /**
     * Permette a un admin di una linea di modificare lo stato del turno
     *
     * @param turnoDTO
     * @param isOpen
     */
    public void setTurnoState(TurnoDTO turnoDTO, Boolean isOpen) {
        if (lineeService.isAdminLine(turnoDTO.getIdLinea()) || userService.isSysAdmin()) {
            TurnoEntity turnoEntity = getTurnoEntity(turnoDTO);
            if (!turnoEntity.getIsExpired()) {
                turnoEntity.setIsOpen(isOpen);
                turnoRepository.save(turnoEntity);
            } else
                throw new PermissionDeniedException("Il turno è scaduto");
        } else
            throw new PermissionDeniedException("Non possiedi i privilegi necessari");
    }

    /**
     * Aggiorniamo lo stato della conferma della guida
     *
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

    private Boolean isTurnoExpired(TurnoDTO turnoDTO) {
        LineaDTO lineaDTO = lineeService.getLineaDTOById(turnoDTO.getIdLinea());
        String timeFermata = turnoDTO.getVerso() ? lineaDTO.getAndata().stream().min(FermataDTO::compareTo).get().getOrario() : lineeService.getPartenzaScuola();

        return MongoZonedDateTime.getMongoZonedDateTimeFromDateTime(turnoDTO.getData(), timeFermata).before(MongoZonedDateTime.getNow());

    }


}