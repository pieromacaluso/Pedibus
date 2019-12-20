# Last Requirements Steps

## Indice dei contenuti

- [Last Requirements Steps](#last-requirements-steps)
  - [Indice dei contenuti](#indice-dei-contenuti)
  - [NOTE IMPORTANTI](#note-importanti)
  - [Amministrazione](#amministrazione)
    - [Temi diversi a seconda della pagine/ruolo](#temi-diversi-a-seconda-della-pagineruolo)
    - [Gestione degli Utenti](#gestione-degli-utenti)
    - [Interfaccia inserimento nuovo utente](#interfaccia-inserimento-nuovo-utente)
    - [Gestione Ruoli dell'utente](#gestione-ruoli-dellutente)
    - [Amministratore Master per la linea](#amministratore-master-per-la-linea)
    - [Cambio fermata Disponibilità [PIERO e ANGELO]](#cambio-fermata-disponibilit%c3%a0-piero-e-angelo)
  - [Comunicazione](#comunicazione)
    - [Notifiche Speciali [ANGELO]](#notifiche-speciali-angelo)
    - [Revisione notifiche generale](#revisione-notifiche-generale)
    - [Ottimizzazione WebSocket](#ottimizzazione-websocket)
  - [Genitore [MARCO]](#genitore-marco)
    - [Aggiunta Prenotazione Figlio](#aggiunta-prenotazione-figlio)
  - [Database delle linee](#database-delle-linee)
    - [Aggiunta Posizione GPS](#aggiunta-posizione-gps)
    - [Aggiunta Amministratore Master](#aggiunta-amministratore-master)

## NOTE IMPORTANTI

SYS_ADMIN => ANAGRAFICA / RUOLI

ADMIN LINEA => SOLO RUOLI


## Amministrazione

### Temi diversi a seconda della pagine/ruolo

[TRIVIAL] da vedere dopo. Bisogna verificare se è fattibile lato frontend in maniera easy.

### Gestione degli Utenti

L'amministratore di sistema deve essere in grado di poter avere accesso alla lista di tutti gli utenti, consultare le informazioni e modificarle. Alcune delle informazioni importanti sono anagrafica, bambini e ruolo dell'utente.

**Spunti implementativi**

**LINK UTILE**: [https://www.baeldung.com/rest-api-pagination-in-spring](https://www.baeldung.com/rest-api-pagination-in-spring)

Lato Backend si dovrebbe implementare la restituzione di un semplice elenco degli utenti. Secondo me qui ha senso pensare alla **paginazione**, perchè caricare più di cento utenti nella stessa view potrebbe essere provocare *lag*. C'era qualche modo per farlo easy in Spring, dovremmo indagare.

Lato Frontend la cosa è relativamente semplice: l'unica cosa forse *tricky* è la gestione di pulsanti per la paginazione.

### Interfaccia inserimento nuovo utente

L'amministratore di sistema deve poter inserire un nuovo utente (solo lui può registrarlo). L'utente riceverà una mail per gestire e finalizzare il primo accesso (password di default oppure link di registrazione identico a quello di reimpostazione della password che permette all'utente di inserire la password iniziale).

**Spunti implementativi**

Sfruttare endpoint o solo implementazione degli endpoint fatti per la registrazione. Secondo me conviene riciclare quell'endpoint visto che da specifica un utente normale potrà SOLO loggarsi dalla homepage, non registrarsi. Per quanto riguarda la mail che l'utente riceverà, questa potrà sfruttare il meccanismo del recupero password: la registrazione dell'admin termina con un triggeramento personalizzato del recupero password. Una mail viene inviata e l'utente la utilizza come primo accesso.

### Gestione Ruoli dell'utente

Sul documento del professore è indicato che non solo l'amministratore master, ma anche gli amministratori di una determinata linea possono promuovere o declassare tutti gli accompagnatori per la linea in oggetto.

**Spunti implementativi**

Aspetto legato a [Gestione degli Utenti](#gestione-degli-utenti). Se implementiamo una modifica dei dati anagrafici (oppure questa viene gestita da file di configurazione iniziale?) allora andiamo a mettere un campo di selezione dove è possibile gestire i permessi.


### Amministratore Master per la linea

Dai requisiti viene richiesto un amministratore master per la linea che viene definito nel file di configurazione delle linee e che può essere cambiato solo dall'amministratore di sistema. Non può perciò essere spodestato da altri amministratori di linea.

**Spunti implementativi**

Aggiunta da file di configurazione caricato in fase di startup del server.

### Cambio fermata Disponibilità [PIERO e ANGELO]

L'amministratore di una determinata linea deve essere in grado di modificare la fermata di una disponibilità comunicata. Nel file del professore non viene indicato esplicitamente che nel momento della comunicazione della disponibilità si debba indicare una fermata: io manterrei comunque questa possibilità facendola passare come fermata preferita inserita al momento della sottomissione della disponibilità. Sarà cura dell'amministratore di linea che si trova con una fermata senza accompagnatori a riempirla modificando le disponibilità.

Aggiungere la possibilità per l'amministratore di linea di rifiutare una disponibilità (Notifica e Timestamp). Aggiungiamo uno stato all'interno della disponibilità al posto dell'`isConfirmed`.

**Spunti implementativi**

Oltre al pulsante Approva, metterei un pulsante modifica: attenzione va posta sulla gestione di websocket per aggiornare le view degli altri utenti.

## Comunicazione

### Notifiche Speciali [ANGELO]

Quando una notifica speciale (e.g. disponibilità) viene cancellata, questo provoca alcune operazioni callaterali. Nel caso della disponibilità setta `isAck` a `true` e inserisce la data e ora in cui questo Ack è stato ricevuto (all'interno dell'Entity Disponibilità).

### Revisione notifiche generale

Sul documento del professore sono riportate le notifiche richieste. Prenderei quelle come requisiti minimi: altre notifiche possiamo aggiungerle *appiacere*.

**Spunti implementativi**

Cosa può essere utile comunicare/notificare?

### Ottimizzazione WebSocket

Sarebbe utile ottimizzare gli endpoint dei websocket di disponibilità e turni utilizzando un endpoint per ognuno in grado di comunicare aggiunte, cancellazioni e modifiche. 

**Spunti implementativi**

Per far questo sarebbe necessario *"implementare"* una sorta di nostro protocollo. Ad esempio si potrebbe usare 0 per aggiunta, 1 per modifica, 2 per eliminazione.

**Esempio Aggiunta Prima**:

```json
{
    "id_disp": ... ,
    "fermata_disp": ... ,
    ...
}
```

**Esempio Aggiunta Dopo**:

```json
{
    "op": 0,
    "dato": {
        "id_disp": ... ,
        "fermata_disp": ... ,
        ...
    }
}
```

## Genitore [MARCO]

Credo sia utile per il genitore avere accesso solo ed esclusivamente a aggiunta prenotazione e comunicazioni (notifiche).

### Aggiunta Prenotazione Figlio

Schermata molto simile a quella delle presenze, almento per quanto riguarda la parte in alto della selezione del giorno/linea/verso. Nel corpo centrale sarebbe utile avere l'elenco dei figli del genitore con le rispettive prenotazioni se presenti, o un pulsante per procedere alla prenotazione (Finestra di dialogo simile a quella utilizzata per alunno non prenotato).

**Spunti implementativi**

Niente di particolare da segnalare

## Database delle linee

### Aggiunta Posizione GPS

Da requisito bisogna aggiungere la posizione GPS della fermata (magari implementare una mappa con l'elenco delle fermate per ogni linea, non visualizzazione live, ma solo esplicativa per il genitore che deve prenotare).

**Spunti implementativi**

Aggiunta nel file di configurazione del sistema. 

Opzioni per visualizzazione percorso:

1. Potremmo ad esempio mostrare una finestra di dialogo cliccando sul nome della fermata che mostri le informazioni sulla fermata e la sua posizione GPS con una piccola mappetta (Open Street Maps).
2. Pulsante Informazioni Linee da qualche parte che mostra i percorsi delle linee nello spazio dove il genitore può verificare la fermata più comoda.

### Aggiunta Amministratore Master

Come detto in [Amministratore Master per la linea](#amministratore-master-per-la-linea).
