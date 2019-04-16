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

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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
     * Salva una linea sul DB
     *
     * @param lineaDTO
     */
    public void addLinea(LineaDTO lineaDTO) {
        LineaEntity lineaEntity = new LineaEntity(lineaDTO);
        lineaRepository.save(lineaEntity);

    }

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


    public List<LineaEntity> getAllLines() {
        return lineaRepository.findAll();
    }

    public LineaEntity getLine(String lineName) {
        return lineaRepository.findByNome(lineName);       //ToDo check unica linea con tale nome, forse farlo in fase di caricamento db
    }
    public Optional<LineaEntity> getLine(Integer idLinea){
        return lineaRepository.findById(idLinea);       //ToDo perchè deve essere per forza Optional?
    }

    /**
     * Legge da db tutte le fermate presenti nella lista idFermata
     *
     * @param idFermate
     * @return ordinato per orario
     */
    public List<FermataEntity> getFermate(List<Integer> idFermate) {
        List<FermataEntity> fermate = (List<FermataEntity>) fermataRepository.findAllById(idFermate);
        fermate.sort(Comparator.comparing(FermataEntity::getOrario));                                  //ordinate per orario e non per id
        return fermate;
    }


    /**
     * Aggiunge una nuova prenotazione al db. Controlla se esiste già una prenotazione per lo stesso utente nello
     * stesso giorno con lo stesso verso, in tal caso l'inserimento non viene eseguito
     *
     * @param prenotazioneDTO
     */
    public String addPrenotazione(PrenotazioneDTO prenotazioneDTO) {
        PrenotazioneEntity prenotazioneEntity = new PrenotazioneEntity(prenotazioneDTO);
       /* PrenotazioneEntity checkPren=prenotazioneRepository.findByNomeAlunnoAndDataAndVerso(prenotazioneDTO.getNomeAlunno(),prenotazioneDTO.getData(),prenotazioneDTO.getVerso());
        String id;
        if(checkPren==null)
            id=prenotazioneRepository.save(prenotazioneEntity).getId().toString();
        else
            id="Prenotazione non valida";
        return id;*/
       return prenotazioneRepository.save(prenotazioneEntity).getId().toString();
    }



    /**
     *Metodo utilizzato per aggiornare una vecchia prenotazione tramite il suo reservationId con uno dei
     * nuovi campi passati dall'utente
     * @param prenotazioneDTO contiene i nuovi dati
     */
    public void updatePrenotazione(PrenotazioneDTO prenotazioneDTO, ObjectId reservationId)
    {
        PrenotazioneEntity prenotazioneEntity = prenotazioneRepository.findById(reservationId);
        prenotazioneEntity.update(prenotazioneDTO);
        prenotazioneRepository.save(prenotazioneEntity);
    }


    public PrenotazioneEntity getPrenotazione(ObjectId reservationId){
        return prenotazioneRepository.findById(reservationId);
    }

    /**
     * Elimina prenotazione selezionata
     *
     * @param prenotazioneDTO
     */
    public void deletePrenotazione(PrenotazioneDTO prenotazioneDTO,ObjectId reservationId) {
        PrenotazioneEntity prenotazioneEntity = new PrenotazioneEntity(prenotazioneDTO); //todo check prenotazione dto corrispondente a quel reservationId
        prenotazioneEntity.setId(reservationId);
        PrenotazioneEntity checkPren=prenotazioneRepository.findById(reservationId);
        if(prenotazioneEntity.equals(checkPren)){
            prenotazioneRepository.delete(prenotazioneEntity);
        }else{
            //todo vedere se ritornare qualcosa
        }
    }


    public void findPrenotazione(PrenotazioneDTO prenotazioneDTO) {
        PrenotazioneEntity prenotazioneEntity = new PrenotazioneEntity(prenotazioneDTO);
    }

    public List<String> findAlunniFermata(Date data,Integer id,boolean verso) {
        List<PrenotazioneEntity> prenotazioni=prenotazioneRepository.findAllByDataAndIdFermataAndVerso(data,id,verso);
        return prenotazioni.stream().map(p->p.getNomeAlunno()).collect(Collectors.toList());

    }
}
