package it.polito.ai.mmap.pedibus.services;

import it.polito.ai.mmap.pedibus.entity.DispEntity;
import it.polito.ai.mmap.pedibus.entity.NotificaEntity;
import it.polito.ai.mmap.pedibus.entity.TurnoEntity;
import it.polito.ai.mmap.pedibus.entity.UserEntity;
import it.polito.ai.mmap.pedibus.exception.DispNotFoundException;
import it.polito.ai.mmap.pedibus.exception.PermissionDeniedException;
import it.polito.ai.mmap.pedibus.objectDTO.*;
import it.polito.ai.mmap.pedibus.repository.DispRepository;
import it.polito.ai.mmap.pedibus.repository.TurnoRepository;
import it.polito.ai.mmap.pedibus.resources.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;


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
    @Autowired
    NotificheService notificheService;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Value("${notifiche.type.Base}")
    String NotBASE;
    @Value("${notifiche.type.Disponibilita}")
    String NotDISPONIBILITA;
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
            turnoRepository.delete(turnoEntity);
            turnoEntity.setIsExpired(turnoExpired);
            turnoRepository.save(turnoEntity);
        } else {
            turnoEntity = new TurnoEntity(turnoDTO);
            turnoEntity.setIsOpen(!turnoExpired);
            turnoEntity.setIsExpired(turnoExpired);
            if (!turnoExpired) turnoRepository.save(turnoEntity);
            //throw new IllegalArgumentException("Il turno è chiuso"); //TODO eccezione custom (?)
        }
        return turnoEntity;
    }

    /**
     * Restituisce una dispEntity a partire dal turno e dalla persona indicata
     *
     * @param turnoDTO
     * @param guideUsername
     * @return
     */
    private DispEntity getDispEntity(TurnoDTO turnoDTO, String guideUsername) throws DispNotFoundException {
        Optional<DispEntity> checkDisp = dispRepository.findByGuideUsernameAndTurnoId(guideUsername, getTurnoEntity(turnoDTO).getTurnoId());
        if (checkDisp.isPresent())
            return checkDisp.get();
        else
            throw new DispNotFoundException("Disponibilità non trovata");
    }

    /**
     * Controlla che una GUIDE non debba essere in due posti in contemporanea e che quindi abbia una sola prenotazione per verso/data
     *
     * @param turnoDTO
     * @return
     */
    private Boolean idDispDuplicate(TurnoDTO turnoDTO) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<String> listIdLinee = lineeService.getAllLinesIds();
        for (String idLinea : listIdLinee) {
            try {
                getDispEntity(new TurnoDTO(idLinea, turnoDTO.getData(), turnoDTO.getVerso()), principal.getUsername());
                return true;
            } catch (DispNotFoundException e) {
            }
        }
        return false;
    }

    /**
     * Restituisce una dispTurnoResource a partire dal turno e dalla persona loggata
     * Questa risorsa contiene lo stato di tutti i turni per quei parametri ed eventualmente la disponibilità
     *
     * @param date
     * @param verso
     * @return
     */
    public DispTurnoResource getDispTurnoResource(Date date, Boolean verso) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<String> listIdLinee = lineeService.getAllLinesIds();

        for (String idLinea : listIdLinee) {
            TurnoDTO turnoDTO = new TurnoDTO(idLinea, date, verso);
            try {
                DispEntity dispEntity = getDispEntity(turnoDTO, principal.getUsername());
                return new DispTurnoResource(new DispAllResource(dispEntity,
                        lineeService.getFermataEntityById(dispEntity.getIdFermata()),
                        lineeService.getLineaEntityById(dispEntity.getIdLinea())),
                        new TurnoResource(getTurnoEntity(turnoDTO)));
            } catch (DispNotFoundException e) {
            }
        }
        return null;

    }

    /**
     * Salva la disponibilità, a patto che:
     * - il turno sia aperto
     * - ruolo GUIDE //TODO
     * - non si già presente una disp per lo stesso verso/data
     *
     * @param dispDTO
     */
    public DispTurnoResource addDisp(DispDTO dispDTO) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!idDispDuplicate(dispDTO.getTurnoDTO())) {
            TurnoEntity turnoEntity = getTurnoEntity(dispDTO.getTurnoDTO());

            if (turnoEntity.getIsExpired())
                throw new IllegalArgumentException("Il turno è chiuso e scaduto"); //TODO eccezione custom (?)
            if (!turnoEntity.getIsOpen())
                throw new IllegalArgumentException("Il turno è chiuso"); //TODO eccezione custom (?)
            if (!this.userService.isGuide())
                throw new PermissionDeniedException("Accesso negato, l'utente non è guida");

            DispEntity dispEntity = new DispEntity(principal.getUsername(), turnoEntity.getIdLinea(), dispDTO.getIdFermata(), turnoEntity.getTurnoId());
            dispRepository.save(dispEntity);
            DispAllResource d = new DispAllResource(dispEntity,
                    lineeService.getFermataEntityById(dispEntity.getIdFermata()),
                    lineeService.getLineaEntityById(dispEntity.getIdLinea()));
            TurnoResource t = new TurnoResource(turnoEntity);
            return new DispTurnoResource(d, t);
        } else
            throw new IllegalArgumentException("Disponibilità già presente");
    }

    /**
     * Cancella la disponibilità per la persona loggata, a patto che esista
     *
     * @param turnoDTO
     */
    public DispAllResource deleteDisp(TurnoDTO turnoDTO) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        DispEntity dispEntity = getDispEntity(turnoDTO, principal.getUsername());
        DispAllResource d = new DispAllResource(dispEntity,
                lineeService.getFermataEntityById(dispEntity.getIdFermata()),
                lineeService.getLineaEntityById(dispEntity.getIdLinea()));
        if (getTurnoEntity(turnoDTO).getIsOpen() && !getTurnoEntity(turnoDTO).getIsExpired()) {
            dispRepository.delete(dispEntity);
            return d;
        } else
            throw new IllegalArgumentException("Il turno è chiuso"); //TODO eccezione custom (?)
    }

    /**
     * Restituisce tutte le DispAllResource per un turno, sia quelle confirmed che quelle no
     *
     * @param turnoDTO
     * @return
     */
    public TurnoDispResource getAllTurnoDisp(TurnoDTO turnoDTO) {
        List<DispEntity> dispEntities = dispRepository.findAllByTurnoId(getTurnoEntity(turnoDTO).getTurnoId());
        List<DispAllResource> dispRes = new ArrayList<>();
        for (DispEntity d : dispEntities) {
            DispAllResource dR = new DispAllResource(d,
                    lineeService.getFermataEntityById(d.getIdFermata()),
                    lineeService.getLineaEntityById(d.getIdLinea()));
            dispRes.add(dR);
        }

        Map<String, List<DispAllResource>> dispResourceMap = dispRes.stream()
                .collect(groupingBy(DispAllResource::getNomeFermata));

        return new TurnoDispResource(new TurnoResource(getTurnoEntity(turnoDTO)), dispResourceMap);
    }

    /**
     * Aggiorna lo stato isConfirmed per ogni disp
     * Controlla che prima il turno sia stato chiuso tramite PUT /turno/state/{idLinea}/{verso}/{data}
     *
     * @param turnoDTO
     * @param dispAllResource
     */
    public void setAllTurnoDisp(TurnoDTO turnoDTO, DispAllResource dispAllResource) {
        if (!getTurnoEntity(turnoDTO).getIsOpen()) {
            if (!getTurnoEntity(turnoDTO).getIsExpired()) {
                DispEntity dispEntity = getDispEntity(turnoDTO, dispAllResource.getGuideUsername());
                dispEntity.setIdFermata(dispAllResource.getIdFermata());
                dispEntity.setIsConfirmed(dispAllResource.getIsConfirmed());
                dispEntity = dispRepository.save(dispEntity);
                NotificaEntity notificaEntity = new NotificaEntity(NotDISPONIBILITA, dispAllResource.getGuideUsername(), "La tua disponibilità è stata confermata", dispEntity.getDispId());
                notificheService.addNotifica(notificaEntity);      //salvataggio notifica
                simpMessagingTemplate.convertAndSendToUser(dispAllResource.getGuideUsername(), "/notifiche", notificaEntity);
                //todo messaggio per aggiornare l'interfaccia admin, quando arrivato a scuola friz tutti gli utenti
            } else
                throw new IllegalArgumentException("Il turno è scaduto"); //TODO eccezione custom (?)
        } else
            throw new IllegalArgumentException("Il turno deve essere chiuso"); //TODO eccezione custom (?)
    }

    /**
     * Permette a un admin di una linea di modificare lo stato del turno
     *
     * @param turnoDTO
     * @param isOpen
     */
    public TurnoEntity setTurnoState(TurnoDTO turnoDTO, Boolean isOpen) {
        if (lineeService.isAdminLine(turnoDTO.getIdLinea()) || userService.isSysAdmin()) {
            TurnoEntity turnoEntity = getTurnoEntity(turnoDTO);
            if (!turnoEntity.getIsExpired()) {
                turnoEntity.setIsOpen(isOpen);
                turnoRepository.save(turnoEntity);
                return turnoEntity;
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
    public DispAllResource ackDisp(TurnoDTO turnoDTO) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        DispEntity dispEntity = getDispEntity(turnoDTO, principal.getUsername());
        if (dispEntity.getIsConfirmed()) {
            dispEntity.setIsAck(true);
            dispRepository.save(dispEntity);
            return new DispAllResource(dispEntity,
                    lineeService.getFermataEntityById(dispEntity.getIdFermata()),
                    lineeService.getLineaEntityById(dispEntity.getIdLinea()));
        } else {
            //todo else questa guida non era stata confermata per quel turno, quindi non dovrebbe mandare l'ack: ignoriamo o segnaliamo errore ?
            throw new IllegalArgumentException("La guida non ha la facoltà di confermare la ricezione.");
        }
    }

    private Boolean isTurnoExpired(TurnoDTO turnoDTO) {
        LineaDTO lineaDTO = lineeService.getLineaDTOById(turnoDTO.getIdLinea());
        String timeFermata = turnoDTO.getVerso() ? lineaDTO.getAndata().stream().min(FermataDTO::compareTo).get().getOrario() : lineeService.getPartenzaScuola();

        return MongoTimeService.getMongoZonedDateTimeFromDateTime(turnoDTO.getData(), timeFermata).before(MongoTimeService.getNow());

    }

    /**
     * Restituisce lo stato di un turno a partire dalla terna contenuta nel dto
     *
     * @param turnoDTO
     * @return
     */
    public TurnoResource getTurnoState(TurnoDTO turnoDTO) {
        return new TurnoResource(getTurnoEntity(turnoDTO));
    }


    public Boolean isGuideConfirmed(String idLinea, Date date, Boolean verso) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        //todo basta controllare isConfirmed ? oppure anche isAck ?
        return  getDispEntity(new TurnoDTO(idLinea, date, verso), principal.getUsername()).getIsConfirmed();

    }
}
