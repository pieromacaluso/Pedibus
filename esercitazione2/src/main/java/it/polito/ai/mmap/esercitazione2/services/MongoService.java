package it.polito.ai.mmap.esercitazione2.services;

import it.polito.ai.mmap.esercitazione2.entity.FermataEntity;
import it.polito.ai.mmap.esercitazione2.entity.LineaEntity;
import it.polito.ai.mmap.esercitazione2.entity.PrenotazioneEntity;
import it.polito.ai.mmap.esercitazione2.objectDTO.FermataDTO;
import it.polito.ai.mmap.esercitazione2.objectDTO.LineaDTO;
import it.polito.ai.mmap.esercitazione2.objectDTO.PrenotazioneDTO;
import it.polito.ai.mmap.esercitazione2.repository.FermataRepository;
import it.polito.ai.mmap.esercitazione2.repository.LineaRepository;
import it.polito.ai.mmap.esercitazione2.repository.PrenotazioneRepository;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MongoService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private LineaRepository lineaRepository;

    @Autowired
    private FermataRepository fermataRepository;

    @Autowired
    private PrenotazioneRepository prenotazioneRepository;


    /**
     * Salva fermate dul DB
     *
     * @param listaFermate
     */

    public void addFermate(List<FermataDTO> listaFermate) {

        fermataRepository.saveAll(listaFermate
                .stream()
                .map(FermataEntity::new)
                .collect(Collectors.toList()));
    }

    public void removeFermate()
    {
        fermataRepository.deleteAll();
    }

    /**
     * Mi da un fermataDTO partendo dal suo id
     *
     * @param idFermata
     * @return
     */
    public FermataDTO getFermataById(Integer idFermata) {
        return new FermataDTO(fermataRepository.findById(idFermata).get());
    }

    /**
     * Salva una linea sul DB
     *
     * @param lineaDTO
     */
    public void addLinea(LineaDTO lineaDTO) {
        LineaEntity lineaEntity = new LineaEntity(lineaDTO);
        lineaRepository.save(lineaEntity);

    }

    /**
     * Metodo che restituisce una LineaDTO a partire dal suo nome
     *
     * @param lineName
     * @return LineaDTO
     */
    public LineaDTO getLineByName(String lineName) {
        return new LineaDTO(lineaRepository.findByNome(lineName),fermataRepository); //ToDo check unica linea con tale nome, forse farlo in fase di caricamento db
    }

    public LineaDTO getLineById(Integer idLinea) {
        return new LineaDTO(lineaRepository.findById(idLinea).get(), fermataRepository);
    }


    public List<LineaEntity> getAllLines() {
        return lineaRepository.findAll();
    }

    public List<String> getAllLinesNames() {
        return lineaRepository.findAll().stream().map(f -> f.getNome()).collect(Collectors.toList());
    }


    /**
     * Aggiunge una nuova prenotazione al db. Controlla:
     * - se per quella Linea esiste una fermata con quel id
     * - se esiste già una prenotazione per lo stesso utente nello stesso giorno con lo stesso verso, in tal caso l'inserimento non viene eseguito
     *
     * @param prenotazioneDTO
     */
    public String addPrenotazione(PrenotazioneDTO prenotazioneDTO) {
        PrenotazioneEntity checkPren = prenotazioneRepository.findByNomeAlunnoAndDataAndVerso(prenotazioneDTO.getNomeAlunno(), prenotazioneDTO.getData(), prenotazioneDTO.getVerso());

        if (isValidPrenotation(prenotazioneDTO) && checkPren == null) {
            PrenotazioneEntity prenotazioneEntity = new PrenotazioneEntity(prenotazioneDTO);
            return prenotazioneRepository.save(prenotazioneEntity).getId().toString();
        } else
            throw new IllegalArgumentException();
    }

    /**
     * Metodo utilizzato per aggiornare una vecchia prenotazione tramite il suo reservationId con uno dei
     * nuovi campi passati dall'utente
     *
     * @param prenotazioneDTO contiene i nuovi dati
     */
    public void updatePrenotazione(PrenotazioneDTO prenotazioneDTO, ObjectId reservationId) {
        if (isValidPrenotation(prenotazioneDTO)) {
            PrenotazioneEntity prenotazioneEntity = prenotazioneRepository.findById(reservationId);
            prenotazioneEntity.update(prenotazioneDTO);
            prenotazioneRepository.save(prenotazioneEntity);
        }
    }

    /**
     * Controlla che i dettagli della prenotazione siano consistenti:
     * - l'idFermata deve appartenere alla linea indicata
     * *
     *
     * @param prenotazioneDTO
     * @return
     */
    private Boolean isValidPrenotation(PrenotazioneDTO prenotazioneDTO) {
        FermataDTO fermataDTO = this.getFermataById(prenotazioneDTO.getIdFermata());
        if (this.getLineById(prenotazioneDTO.getIdLinea()).getAndata().contains(fermataDTO) || this.getLineById(prenotazioneDTO.getIdLinea()).getRitorno().contains(fermataDTO)) {
            return true;
        } else
            throw new IllegalArgumentException();
    }

    public PrenotazioneEntity getPrenotazione(ObjectId reservationId) {
        return prenotazioneRepository.findById(reservationId);
    }

    /**
     * Elimina la prenotazione indicata dall'objectId controllando che i dettagli siano consistenti
     *
     * @param nomeLinea
     * @param data
     * @param reservationId
     */
    public void deletePrenotazione(String nomeLinea, Date data, ObjectId reservationId) {
        PrenotazioneEntity checkPren = prenotazioneRepository.findById(reservationId);
        if (checkPren.getData().equals(data) && getLineById(checkPren.getIdLinea()).getNome().equals(nomeLinea)) {
            prenotazioneRepository.delete(checkPren);
        } else {
            throw new IllegalArgumentException();
        }

    }

    public List<String> findAlunniFermata(Date data, Integer id, boolean verso) {
        List<PrenotazioneEntity> prenotazioni = prenotazioneRepository.findAllByDataAndIdFermataAndVerso(data, id, verso);
        return prenotazioni.stream().map(p -> p.getNomeAlunno()).collect(Collectors.toList());

    }

    public boolean isLineaUpdated(LineaDTO lineaDTO) {
        LineaEntity linea = new LineaEntity(lineaDTO);
        LineaEntity lineaEntity = lineaRepository.findByNome(linea.getNome());
        if (lineaEntity == null) //TODO da rivedere, messo alla buona se no crasha se le linee non sono già state caricate su db
        {
            return false;
        }
        String ultimaModifica = lineaEntity.getUltimaModifica();
        if (ultimaModifica != null)
            return linea.getUltimaModifica().compareToIgnoreCase(ultimaModifica) >= 0;
        else return false;
    }

}
