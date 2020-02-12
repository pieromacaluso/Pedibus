package it.polito.ai.mmap.pedibus.services;

import it.polito.ai.mmap.pedibus.entity.DispEntity;
import it.polito.ai.mmap.pedibus.entity.NotificaEntity;
import it.polito.ai.mmap.pedibus.entity.TurnoEntity;
import it.polito.ai.mmap.pedibus.entity.UserEntity;
import it.polito.ai.mmap.pedibus.exception.*;
import it.polito.ai.mmap.pedibus.objectDTO.DispDTO;
import it.polito.ai.mmap.pedibus.objectDTO.FermataDTO;
import it.polito.ai.mmap.pedibus.objectDTO.LineaDTO;
import it.polito.ai.mmap.pedibus.objectDTO.TurnoDTO;
import it.polito.ai.mmap.pedibus.repository.DispRepository;
import it.polito.ai.mmap.pedibus.repository.TurnoRepository;
import it.polito.ai.mmap.pedibus.resources.*;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Restituisce un TurnoEntity a partire dalle 3 "chiavi": idLinea, data e verso
     * Se il turno non è presente sul db per questa terna lo crea
     * Se il turno ha oltrepassato la sua data di scadenza (pedibus partito) viene chiuso o non viene creato
     *
     * @param turnoDTO turnoDTO ricevuto
     */
    private TurnoEntity getTurnoEntity(TurnoDTO turnoDTO) {
        Optional<TurnoEntity> checkTurno = turnoRepository.findByIdLineaAndDataAndVerso(turnoDTO.getIdLinea(), turnoDTO.getData(), turnoDTO.getVerso());
        TurnoEntity turnoEntity;
        Boolean turnoExpired = isTurnoExpired(turnoDTO);

        if (checkTurno.isPresent()) {
            turnoEntity = checkTurno.get();
            turnoEntity.setIsExpired(turnoExpired);
            turnoRepository.save(turnoEntity);
        } else {
            turnoEntity = new TurnoEntity(turnoDTO);
            turnoEntity.setIsOpen(!turnoExpired);
            turnoEntity.setIsExpired(turnoExpired);
            if (!turnoExpired) turnoRepository.save(turnoEntity);
        }
        return turnoEntity;
    }

    /**
     * Restituisce una dispEntity a partire dal turno e dalla persona indicata
     *
     * @param turnoDTO turnoDTO indicato
     * @param guideUsername guida specificata
     */
    private DispEntity getDispEntity(TurnoDTO turnoDTO, String guideUsername) throws DispNotFoundException {
        Optional<DispEntity> checkDisp = dispRepository.findByGuideUsernameAndTurnoId(guideUsername, getTurnoEntity(turnoDTO).getTurnoId());
        if (checkDisp.isPresent())
            return checkDisp.get();
        else
            throw new DispNotFoundException("Disponibilità non trovata");
    }

    /**
     * Restituisce una dispEntity a partire dall' id della disponibilità
     *
     * @param idDisp id Disponibilità
     */
    private DispEntity getDispEntitybyId(String idDisp) throws DispNotFoundException {
        Optional<DispEntity> checkDisp = dispRepository.findByDispId(idDisp);
        return checkDisp.orElseThrow(DispNotFoundException::new);
    }

    /**
     * Controlla che una GUIDE non debba essere in due posti in contemporanea e che quindi abbia una sola prenotazione per verso/data
     *
     * @param turnoDTO turno selezionato
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
     * @param date data specificata
     * @param verso verso indicato
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
     * - ruolo GUIDE
     * - non si già presente una disp per lo stesso verso/data
     *
     * @param dispDTO disponibilità DTO
     */
    public DispTurnoResource addDisp(DispDTO dispDTO) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!idDispDuplicate(dispDTO.getTurnoDTO())) {
            TurnoEntity turnoEntity = getTurnoEntity(dispDTO.getTurnoDTO());

            if (turnoEntity.getIsExpired())
                throw new TurnoExpiredException();
            if (!turnoEntity.getIsOpen())
                throw new TurnoClosedException();
            if (!this.userService.isGuide())
                throw new PermissionDeniedException();

            DispEntity dispEntity = new DispEntity(principal.getUsername(), turnoEntity.getIdLinea(), dispDTO.getIdFermata(), turnoEntity.getTurnoId());
            dispRepository.save(dispEntity);
            DispAllResource d = new DispAllResource(dispEntity,
                    lineeService.getFermataEntityById(dispEntity.getIdFermata()),
                    lineeService.getLineaEntityById(dispEntity.getIdLinea()));
            TurnoResource t = new TurnoResource(turnoEntity);
            DispTurnoResource res = new DispTurnoResource(d, t);
            simpMessagingTemplate.convertAndSend("/dispws/add/" + MongoTimeService.dateToString(dispDTO.getTurnoDTO().getData()) + "/" + dispDTO.getTurnoDTO().getIdLinea() + "/" + ((dispDTO.getTurnoDTO().getVerso()) ? 1 : 0), res.getDisp());
            simpMessagingTemplate.convertAndSendToUser(res.getDisp().getGuideUsername(), "/dispws/" + "/" + dispDTO.getTurnoDTO().getData() + "/" + dispDTO.getTurnoDTO().getIdLinea() + "/" + ((dispDTO.getTurnoDTO().getVerso()) ? 1 : 0), res);
            return res;
        } else
            throw new DispAlreadyPresentException();
    }

    /**
     * Aggiorna la disponibilità cambiando l'id della fermata
     *
     * @param idDisp id della disponibilità in questione
     * @param disp nuovi dati
     */
    public DispTurnoResource updateDisp(String idDisp, DispAllResource disp) {
        DispEntity e = getDispEntitybyId(idDisp);
        Optional<TurnoEntity> turnoCheck = turnoRepository.findByTurnoId(e.getTurnoId());
        TurnoEntity t = turnoCheck.orElseThrow(TurnoNotFoundException::new);
        if (!t.getIsExpired()) {
            e.setIdFermata(disp.getIdFermata());
            dispRepository.save(e);

            TurnoResource res_t = new TurnoResource(t);
            DispAllResource res_d = new DispAllResource(e,
                    lineeService.getFermataEntityById(e.getIdFermata()),
                    lineeService.getLineaEntityById(e.getIdLinea()));
            DispTurnoResource res = new DispTurnoResource(res_d, res_t);
            simpMessagingTemplate.convertAndSend("/dispws/up/" + MongoTimeService.dateToString(res.getTurno().getData()) + "/" + res.getTurno().getIdLinea() + "/" + ((res.getTurno().getVerso()) ? 1 : 0), res.getDisp());
            simpMessagingTemplate.convertAndSendToUser(res.getDisp().getGuideUsername(), "/dispws/" + MongoTimeService.dateToString(res.getTurno().getData()) + "/" + res.getTurno().getIdLinea() + "/" + ((res.getTurno().getVerso()) ? 1 : 0), res);
            return res;
        } else
            throw new TurnoExpiredException();

    }

    /**
     * Cancella la disponibilità per la persona loggata, a patto che esista
     *
     * @param turnoDTO turno da cui cancellare disponibilità
     */
    public void deleteDisp(TurnoDTO turnoDTO) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        DispEntity dispEntity = getDispEntity(turnoDTO, principal.getUsername());
        DispAllResource d = new DispAllResource(dispEntity,
                lineeService.getFermataEntityById(dispEntity.getIdFermata()),
                lineeService.getLineaEntityById(dispEntity.getIdLinea()));
        if (getTurnoEntity(turnoDTO).getIsOpen() && !getTurnoEntity(turnoDTO).getIsExpired()) {
            dispRepository.delete(dispEntity);
            simpMessagingTemplate.convertAndSendToUser(d.getGuideUsername(), "/dispws/" + MongoTimeService.dateToString(turnoDTO.getData()) + "/" + turnoDTO.getIdLinea() + "/" + ((turnoDTO.getVerso()) ? 1 : 0), new DispTurnoResource());
            simpMessagingTemplate.convertAndSend("/dispws/del/" + MongoTimeService.dateToString(turnoDTO.getData()) + "/" + turnoDTO.getIdLinea() + "/" + ((turnoDTO.getVerso()) ? 1 : 0), d);
        } else
            throw new TurnoClosedException();
    }

    /**
     * Cancella la disponibilità. Utilizzabile solo da Admin di linea
     *
     * @param idDisp disp da cancellare
     */
    public void deleteAdminDisp(String idDisp) {
        DispEntity dispEntity = getDispEntitybyId(idDisp);
        DispAllResource d = new DispAllResource(dispEntity,
                lineeService.getFermataEntityById(dispEntity.getIdFermata()),
                lineeService.getLineaEntityById(dispEntity.getIdLinea()));
        TurnoEntity turnoEntity = this.turnoRepository.findByTurnoId(dispEntity.getTurnoId()).orElseThrow(TurnoNotFoundException::new);
        if (!turnoEntity.getIsOpen() && !turnoEntity.getIsExpired()) {
            dispRepository.delete(dispEntity);
            simpMessagingTemplate.convertAndSendToUser(d.getGuideUsername(), "/dispws/" + MongoTimeService.dateToString(turnoEntity.getData()) + "/" + turnoEntity.getIdLinea() + "/" + ((turnoEntity.getVerso()) ? 1 : 0), new DispTurnoResource());
            simpMessagingTemplate.convertAndSend("/dispws/del/" + MongoTimeService.dateToString(turnoEntity.getData()) + "/" + turnoEntity.getIdLinea() + "/" + ((turnoEntity.getVerso()) ? 1 : 0), d);
            this.notificheService.generateNotUsedDisp(d, turnoEntity);
        } else
            throw new IllegalStateException();
    }

    /**
     * Restituisce tutte le DispAllResource per un turno, sia quelle confirmed che quelle no
     *
     * @param turnoDTO turno in questione
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
     * @param turnoDTO turno in questione
     * @param dispAllResource dati della disponibilità necessari
     */
    public void setAllTurnoDisp(TurnoDTO turnoDTO, DispAllResource dispAllResource) {
        if (!getTurnoEntity(turnoDTO).getIsOpen()) {
            if (!getTurnoEntity(turnoDTO).getIsExpired()) {
                DispEntity dispEntity = getDispEntity(turnoDTO, dispAllResource.getGuideUsername());
                dispEntity.setIdFermata(dispAllResource.getIdFermata());
                dispEntity.setIsConfirmed(dispAllResource.getIsConfirmed());
                dispEntity = dispRepository.save(dispEntity);
                NotificaEntity notificaEntity = this.notificheService.generateDispNotification(dispEntity);
                notificheService.addNotifica(notificaEntity);      //salvataggio e invio notifica
                DispStateResource state = new DispStateResource(dispAllResource);
                simpMessagingTemplate.convertAndSendToUser(dispAllResource.getGuideUsername(), "/dispws/status/" + MongoTimeService.dateToString(turnoDTO.getData()) + "/" + turnoDTO.getIdLinea() + "/" + ((turnoDTO.getVerso()) ? 1 : 0), state);
                simpMessagingTemplate.convertAndSend("/dispws/status/" + MongoTimeService.dateToString(turnoDTO.getData()) + "/" + turnoDTO.getIdLinea() + "/" + ((turnoDTO.getVerso()) ? 1 : 0), dispAllResource);
            } else
                throw new TurnoExpiredException();
        } else
            throw new TurnoClosedException();
    }

    /**
     * Permette a un admin di una linea di modificare lo stato del turno
     *
     * @param turnoDTO turno in questione
     * @param isOpen stato turno
     */
    public void setTurnoState(TurnoDTO turnoDTO, Boolean isOpen) {
        if (lineeService.isAdminLine(turnoDTO.getIdLinea()) || userService.isSysAdmin()) {
            TurnoEntity turnoEntity = getTurnoEntity(turnoDTO);
            if (!turnoEntity.getIsExpired()) {
                turnoEntity.setIsOpen(isOpen);
                turnoRepository.save(turnoEntity);
                TurnoResource tr = new TurnoResource(turnoEntity);
                simpMessagingTemplate.convertAndSend("/turnows/" + MongoTimeService.dateToString(tr.getData()) + "/" + tr.getIdLinea() + "/" + ((tr.getVerso()) ? 1 : 0), tr);
            } else
                throw new TurnoExpiredException();
        } else
            throw new PermissionDeniedException();
    }

    /**
     * Aggiorniamo lo stato della conferma della guida
     *
     * @param turnoDTO turno in questione
     */
    public void ackDisp(TurnoDTO turnoDTO) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        DispEntity dispEntity = getDispEntity(turnoDTO, principal.getUsername());
        if (dispEntity.getIsConfirmed()) {
            dispEntity.setIsAck(true);
            dispRepository.save(dispEntity);
            this.notificheService.sendNotificheDisp(dispEntity);
            this.notificheService.deleteNotificaDisp(dispEntity.getDispId());
        } else {
            throw new IllegalArgumentException("La guida non ha la facoltà di confermare la ricezione.");
        }
    }

    /**
     * Veridica se il turno indicato è staduto o meno
     * @param turnoDTO turno in questione
     */
    private Boolean isTurnoExpired(TurnoDTO turnoDTO) {
        LineaDTO lineaDTO = lineeService.getLineaDTOById(turnoDTO.getIdLinea());
        String timeFermata = turnoDTO.getVerso() ? lineaDTO.getAndata().stream().min(FermataDTO::compareTo).get().getOrario() : lineeService.getPartenzaScuola();

        // CONSEGNA: Filtro per evitare che il turno odierno risulti scaduto
        return MongoTimeService.getMongoZonedDateTimeFromDateTime(turnoDTO.getData(), timeFermata).before(MongoTimeService.getStartOfToday());
//        return MongoTimeService.getMongoZonedDateTimeFromDateTime(turnoDTO.getData(), timeFermata).before(MongoTimeService.getNow());

    }

    /**
     * Restituisce lo stato di un turno a partire dalla terna contenuta nel dto
     *
     * @param turnoDTO turno in questione
     */
    public TurnoResource getTurnoState(TurnoDTO turnoDTO) {
        return new TurnoResource(getTurnoEntity(turnoDTO));
    }


    /**
     * Verifica se la guida che ha eseguito l'accesso ha precedentemente confermato la disponibilità per il turno in questione
     * @param idLinea id linea
     * @param date data in questione
     * @param verso verso specificato
     */
    public Boolean isGuideConfirmed(String idLinea, Date date, Boolean verso) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        DispEntity dispEntity;
        try {
            dispEntity = getDispEntity(new TurnoDTO(idLinea, date, verso), principal.getUsername());
        } catch (DispNotFoundException ignored) {
            return false;
        }
        return dispEntity.getIsAck();


    }

    /**
     * Dato un id disponibilità la segna come letta (isAck=true e data settata).
     * Usato quando un utente legge la notifica con la quale l'amministratore di linea conferma la disponibilità data.
     *
     * @param dispID id disponibilità
     */
    public void setAckDisp(ObjectId dispID) {
        DispEntity dispEntity = getDispEntitybyId(dispID.toString());
        if (dispEntity.getIsConfirmed()) {
            dispEntity.setIsAck(true);
            dispEntity.setDataAck(new Date());
            dispRepository.save(dispEntity);
            notificheService.sendNotificheDisp(dispEntity);
        }
    }

    /**
     * Restituisce un TurnoEntity tramite il suo id
     * @param turnoId id del turno in questione
     */
    public TurnoEntity getTurnoEntityById(ObjectId turnoId) {
        Optional<TurnoEntity> turnoCheck = this.turnoRepository.findByTurnoId(turnoId);
        if (turnoCheck.isPresent()) {
            return turnoCheck.get();
        } else {
            throw new TurnoNotFoundException();
        }
    }
}
