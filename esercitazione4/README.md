# Esercitazione 4
## Composizione Gruppo

Il gruppo MMAP è composto da:
- **Marco Florian** - s247030
- **Piero Macaluso** - s252894
- **Marco Nanci** - s255089
- **Angelo Turco** - s255270

## Avvio Applicazione
1. Dopo aver scompattato l'archivio `mmap-lab04-v1.zip`, posizionarsi da terminale nella cartella `mmap-lab04-v1`
2. Avviare il comando: `npm install`
3. Avviare il comando: `npm start`
4. Aprire il browser e dirigersi su `http://localhost:4200`

## Utilizzo dell'app
### Header
Nell'header è presente solo il logo principale, ma non abbiamo ancora funzionalità (non erano richiesta dall'esercitazione).
Abbiamo comunque provato a inserire un Menù laterale (Drawer) con qualche voce di prova
che svilupperemo più avanti.

### Selezione linea, verso e data
Nella parte alta della schermata principale sono presenti alcuni campi in cui è possibile
selezionare linea, verso e data per visualizzare le fermate e le prenotazioni.
Sono disponibili anche sue pulsanti con frecce per cambiare giorno velocemente.
![Toolbar](img/toolbar.png)

### Prenotazioni e Presenze
Nel corpo centrale dell'app verranno mostrate le fermate con l'elenco alfabetico degli alunni
prenotati. L'accompagnatore potrà contrassegnare gli alunni come presenti premendo sul
pulsante del loro nome dall'elenco degli alunni prenotati per quella fermata. Il pulsante bianco indica un bambino prenotato
di cui non è stata confermata la presenza, mentre il pulsante color verde acqua rappresenta un alunno
con presenza alla fermata confermata.
![Prenotazioni](img/prenotazioni.png)

### Esempio di utilizzo
In questa veloce GIF mostriamo l'utilizzo dell'applicazione.
![Esempio di Utilizzo](img/esempio.gif)
