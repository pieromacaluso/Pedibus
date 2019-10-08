package it.polito.ai.mmap.pedibus.services;

import it.polito.ai.mmap.pedibus.entity.DispEntity;
import it.polito.ai.mmap.pedibus.entity.TurnoEntity;
import it.polito.ai.mmap.pedibus.exception.DispNotFoundException;
import it.polito.ai.mmap.pedibus.exception.DispNotValidException;
import it.polito.ai.mmap.pedibus.objectDTO.DispDTO;
import it.polito.ai.mmap.pedibus.objectDTO.TurnoDTO;
import it.polito.ai.mmap.pedibus.repository.DispRepository;
import it.polito.ai.mmap.pedibus.repository.TurnoRepository;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


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
        if (checkTurno.isPresent())
            return checkTurno.get();
        else {
            return turnoRepository.save(new TurnoEntity(turnoDTO));
        }
    }

    private DispEntity getDispEntity(DispDTO dispDTO) {
        Optional<DispEntity> checkDisp = dispRepository.findByGuideUsernameAndTurnoId(dispDTO.getGuideUsername(), getTurnoEntity(dispDTO.getTurnoDTO()).getTurnoId());
        if (checkDisp.isPresent())
            return checkDisp.get();
        else
            throw new DispNotFoundException("Disponibilità non trovata");
    }

    public void addDisp(DispDTO dispDTO) {
        try {
            getDispEntity(dispDTO);
            return;
        } catch (DispNotFoundException e) {
            TurnoEntity turnoEntity = getTurnoEntity(dispDTO.getTurnoDTO());
            if (lineeService.isAdminOrGuideLine(dispDTO.getGuideUsername(), dispDTO.getTurnoDTO().getIdLinea()) && turnoEntity.getIsAperto())
                dispRepository.save(new DispEntity(dispDTO.getGuideUsername(), turnoEntity.getTurnoId()));
            else
                throw new DispNotValidException("La guide/admin non è relativa alla linea indicata o il turno è chiuso");
        }
    }

    public void deleteDisp(DispDTO dispDTO) {
        DispEntity dispEntity = getDispEntity(dispDTO);
        if (getTurnoEntity(dispDTO.getTurnoDTO()).getIsAperto())
            dispRepository.delete(dispEntity);
    }
}
