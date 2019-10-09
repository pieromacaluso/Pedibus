package it.polito.ai.mmap.pedibus.services;

import it.polito.ai.mmap.pedibus.entity.DispEntity;
import it.polito.ai.mmap.pedibus.entity.TurnoEntity;
import it.polito.ai.mmap.pedibus.entity.UserEntity;
import it.polito.ai.mmap.pedibus.exception.DispNotFoundException;
import it.polito.ai.mmap.pedibus.exception.DispNotValidException;
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

    private TurnoEntity getTurnoEntity(TurnoDTO turnoDTO) {
        Optional<TurnoEntity> checkTurno = turnoRepository.findByIdLineaAndDataAndVerso(turnoDTO.getIdLinea(), turnoDTO.getData(), turnoDTO.getVerso());
        //TODO controllo che non sia un turno da chiudere tramite MongoZonedDateTime -> funzione in lineeService che possono usare anche altri metodi (?)
        if (checkTurno.isPresent())
            return checkTurno.get();
        else {
            return turnoRepository.save(new TurnoEntity(turnoDTO));
        }
    }

    private DispEntity getDispEntity(TurnoDTO turnoDTO, String guideUsername) {
        Optional<DispEntity> checkDisp = dispRepository.findByGuideUsernameAndTurnoId(guideUsername, getTurnoEntity(turnoDTO).getTurnoId());
        if (checkDisp.isPresent())
            return checkDisp.get();
        else
            throw new DispNotFoundException("Disponibilità non trovata");
    }

    public void addDisp(DispDTO dispDTO) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            getDispEntity(dispDTO.getTurnoDTO(), principal.getUsername());
            return;
        } catch (DispNotFoundException e) {
            TurnoEntity turnoEntity = getTurnoEntity(dispDTO.getTurnoDTO());

            if (!turnoEntity.getIsOpen())
                throw new IllegalArgumentException("Il turno è chiuso"); //TODO eccezione custom (?)

            if (!lineeService.isAdminOrGuideLine(principal.getUsername(), dispDTO.getTurnoDTO().getIdLinea()))
                throw new PermissionDeniedException("La guide/admin non è relativa alla linea indicata");

            dispRepository.save(new DispEntity(principal.getUsername(), dispDTO.getIdFermata(), turnoEntity.getTurnoId()));

        }
    }

    public void deleteDisp(TurnoDTO turnoDTO) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        DispEntity dispEntity = getDispEntity(turnoDTO, principal.getUsername());
        if (getTurnoEntity(turnoDTO).getIsOpen())
            dispRepository.delete(dispEntity);
        else
            throw new IllegalArgumentException("Il turno è chiuso"); //TODO eccezione custom (?)
    }

    public List<DispAllResource> getAllTurnoDisp(TurnoDTO turnoDTO) {
        List<DispAllResource> dispResourceList = dispRepository.findAllByTurnoId(getTurnoEntity(turnoDTO).getTurnoId())
                .stream()
                .map(dispEntity -> new DispAllResource(dispEntity.getGuideUsername(), lineeService.getFermataEntityById(dispEntity.getIdFermata()).getName(), dispEntity.getIsConfirmed()))
                .collect(Collectors.toList());
        LineaDTO lineaDTO = lineeService.getLineaDTOById(turnoDTO.getIdLinea());
        String nomeFermata = turnoDTO.getVerso() ? lineaDTO.getAndata().stream().min(FermataDTO::compareTo).get().getNome() : lineaDTO.getRitorno().stream().max(FermataDTO::compareTo).get().getNome();
        dispResourceList.stream().filter(res -> res.getFermata() == null).forEach(res -> res.setFermata(nomeFermata));
        return dispResourceList;
    }

    public void setAllTurnoDisp(TurnoDTO turnoDTO, List<String> usernameList) {
        usernameList.forEach(username -> {
            DispEntity dispEntity = getDispEntity(turnoDTO, username);
            dispEntity.setIsConfirmed(true);
            dispRepository.save(dispEntity);
            //TODO notifica

        });
    }

    public void setTurnoState(TurnoDTO turnoDTO, Boolean isOpen) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (lineeService.getLineaEntityById(turnoDTO.getIdLinea()).getAdminList().contains(principal.getUsername())) {
            TurnoEntity turnoEntity = getTurnoEntity(turnoDTO);
            turnoEntity.setIsOpen(isOpen);
            turnoRepository.save(turnoEntity);
        }
        else
            throw new PermissionDeniedException("Non possiedi i privilegi necessari");
    }

}
