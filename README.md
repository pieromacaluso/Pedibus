<h1 align="center">
  <a href="https://github.com/pieromacaluso/Pedibus" title="Pedibus Documentation">
    <img alt="Pedibus" src="pedibus/stuff/img/logo.png" height="100px" />
  </a>
  <br/>
</h1>

<p align="center">
  Gestionale per sistema di accompagnamento sicuro per ragazzi con genitori troppo apprensivi.
</p>

<p align="center">
 <img alt="Languages" src="https://img.shields.io/badge/Languages-Java | Typescript | HTML,CSS,JS-orange"/>
 <img alt="Framework" src="https://img.shields.io/badge/Framework-Spring | Angular-green"/>
 <img alt="Status" src="https://github.com/pieromacaluso/pedibus/workflows/CI/badge.svg"/>
</p>

# Indice

- [Indice](#indice)
- [Introduzione](#introduzione)
- [Struttura del Repo](#struttura-del-repo)
- [Team](#team)
- [Pedibus](#pedibus)
  - [Indicazioni generali](#indicazioni-generali)
  - [Ruoli](#ruoli)
    - [Amministratore di Sistema](#amministratore-di-sistema)
    - [Amministratore di Linea](#amministratore-di-linea)
    - [Accompagnatore](#accompagnatore)
    - [Genitore](#genitore)
  - [Sezioni](#sezioni)
    - [Login](#login)
    - [Recupero Password](#recupero-password)
    - [Anagrafica](#anagrafica)
      - [Creazione Nuovo Utente](#creazione-nuovo-utente)
    - [Presenze](#presenze)
    - [Turni](#turni)
    - [Disponibilità](#disponibilit%c3%a0)
    - [Permessi](#permessi)
    - [Comunicazioni](#comunicazioni)
      - [Notifiche Principali](#notifiche-principali)
    - [Disponibilità e turni](#disponibilit%c3%a0-e-turni)
  - [Viste](#viste)
    - [Registrazione utente e login](#registrazione-utente-e-login)
    - [Interfaccia accompagnatore: disponibilità](#interfaccia-accompagnatore-disponibilit%c3%a0)

# Introduzione

Repository dei laboratori e del progetto finale del corso "Applicazioni Internet" tenuto dal prof. Giovanni Malnati a.a. 2018/2019.

L’applicazione dovrà realizzare l’interfaccia ed il backend di un’applicazione web per la gestione diuna forma di trasporto scolastico denominata **Pedibus** [1].
Per Pedibus si intende sostanzialmentel’accompagnamento a scuola a piedi dei bambini, generalmente delle elementari, da parte di accompagnatori adulti.
Gli accompagnatori “prelevano” e “consegnano” i bambini ai genitori presso apposite fermate e insieme ai bambini percorrono un determinato tragitto per andare e tornare da scuola.

Compito dell’applicazione è quello di fornire supporto alla gestione e organizzazione del pedibus in particolare per:

1. Gestire gli utenti e fornire loro informazioni
2. Tenere traccia dei bambini “caricati” e “scaricati” dal pedibus
3. Raccogliere le disponibilità degli accompagnatori e predisporre i turni di accompagnamento.

[1] https://it.wikipedia.org/wiki/Piedibus


# Struttura del Repo

- [Cartella Esercitazioni](esercitazioni)
- [Progetto Pedibus](pedibus)

# Team

- <img alt="avatar" src="https://github.com/MarcoFlo.png" width="20px" height="20px"> **Marco Florian** - [MarcoFlo](https://github.com/MarcoFlo) - s247030
- <img alt="avatar" src="https://github.com/pieromacaluso.png" width="20px" height="20px"> **Piero Macaluso** - [pieromacaluso](https://github.com/pieromacaluso) - s252894
- <img alt="avatar" src="https://github.com/GJGits.png" width="20px" height="20px"> **Marco Nanci** - [GJGits](https://github.com/GJGits) - s255089
- <img alt="avatar" src="https://github.com/angeloturco.png" width="20px" height="20px"> **Angelo Turco** - [angeloturco](https://github.com/angeloturco) - s255270

# Pedibus

## Indicazioni generali
**Tecnologie Utilizzate**: Java Spring, Angular 2+, MongoDB

## Ruoli

All'interno del sistema, gli utenti vengono suddivisi in base ai seguenti ruoli:

### Amministratore di Sistema

**Ruolo in Database**: *(ROLE_SYSTEM-ADMIN)*

L'amministratore di sistema è unico.
Può gestire [Anagrafica](#anagrafica) utenti, bambini e [amministratori di linea](#linee), [Presenze](#presenze), [Turni](#turni).
L'utente in questione viene creato in fase di avvio del sistema e non può possedere altri ruoli.

A differenza di una semplice guida o di un amministratore di linea, questo utente può andare a modificare in qualsiasi momento le presenze di qualsiasi bambino.

### Amministratore di Linea

**Ruolo in Database**: *(ROLE_ADMIN)*

Ogni linea può avere più amministratori della stessa. Tra questi, uno solo sarà il master: questo viene indicato nel JSON contenente i dettagli sulle linee. Il master non può essere eliminato.

Può gestire [Presenze](#presenze) odierne, [Turni](#turni) e [amministratori delle proprie linee](#linee).


### Accompagnatore

**Ruolo in Database**: *(ROLE_GUIDE)*

Può gestire [Presenze](#presenze) odierne solo se incaricato per il relativo turno che potrà gestire attraverso la schermata [Disponibilità](#disponibilita).

### Genitore

**Ruolo in Database**: *(ROLE_USER)*

L'utente genitore potrà accedere solamente alla schermata [Genitore](#genitore).

## Sezioni

### Login

La schermata di login permette all'utente di accedere al sistema attraverso email e password. Da questa interfaccia è possibile accedere al [recupero della password](#recupero-password).
Il token JWT viene gestito attraverso l'`AuthenticationService` che provvedere a salvare il token nello storage locale per consentire alle `Guard` di indirizzare l'utente verso le pagine per lui disponibili.

### Recupero Password

L'utente potrà eseguire il recupero password attraverso l'opportuna schermata raggiungibile dal login. Questa procedura genera un token che viene inviato all'utente tramite mail.
L'utente cliccherà sul link ricevuto e accederà alla schermata di recupero password: qui potrà inserire le nuove credenziali.

### Anagrafica

Questa sezione permette all'amministratore di sistema di gestire utenti e bambini. La visualizzazione è paginata e offre un meccanismo di ricerca.

L'utente potrà scrivere nella barra di ricerca un elenco di parole che verranno separate e utilizzate lato backend per costruire una regex per implementare una query personalizzata di Mongo.

L'amministratore potrà [aggiungere](#creazione-nuovo-utente), modificare e eliminare utenti e bambini.

I dati per le due tipologie sono:

- **Utente**
  - Nome e Cognome
  - Email (identificativo)
  - Ruolo
    - Se `ROLE_USER` sarà possibile aggiungere figli
    - Se `ROLE_ADMIN` sarà possibile indicare la linea di competenza.
- **Bambino**
  - Nome e Cognome
  - Codice Fiscale (identificativo)
  - Fermate di Default (Andata e Ritorno)

#### Creazione Nuovo Utente

Abbiamo deciso che la registazione degli utenti sarà affidata all'amministratore di sistema. La scuola raccoglierà le adesioni ad inizio anno e si provvederà a caricare bambini e utenti utilizzando le informazioni personali raccolte.

Il singolo utente riceverà una mail con un link attraverso cui attivare l'account specificando una password personale.

### Presenze

La schermata delle presenze è costituita da due componenti principali: la toolbar attraverso la quale è possibile andare a selezionare linea, data e verso del servizio, e la lista delle prenotazioni.

Quest'ultima è suddivisa in tante parti quante sono le fermate: ognuna di queste conterrà un pulsante per ogni bambino prenotato per la specifica fermata e un bottone per poter mostrare ulteriori dettagli quali sulla posizione della stessa. Attraverso questi pulsanti, l'accompagnatore potrà indicare:

- La presa in carico del bambino alla fermata / dalla scuola.
- L'arrivo del bambino a scuola / alla fermata.
- L'assenza di un bambino prenotato
- Annullare le decisioni prese

Tutte queste azione scatenano le rispettive notifiche a tutti i genitori del bambino selezionato. In caso di annullamento delle decisioni, le notifiche precedentemente inviate saranno automaticamente cancellate.

Nella parte finale della pagina è presente una sezione per poter inserire prenotazioni di bambini che si sono presentati alla fermata senza essere stati precedentemente prenotati.

Sarà possibile esportare un file `.json` contenente lo stato della prenotazioni dei bambini nel turno selezionato.

### Turni

L'interfaccia per la gestione dei turni viene gestita esclusivamente da amministratori di linea e di sistema.
Dopo aver scelto il turno da gestire attraverso la toolbar, viene mostrato lo stato del turno e l'elenco delle disponibilità ricevute dagli accompagnatori suddivise nelle rispettive fermate.

Un turno può essere:

- Aperto: gli accompagnatori possono inviare le proprie disponibilità o cancellarle.
- Chiuso: gli accompagnatori non possono più modificare le disponibilità comunicate. L'amministratore, invece potrà modificare e confermare le disponibilità ricevute. Una volta confermata, l'accompagnatore della disponibilità verrà notificato. Per concludere la procedura, l'accompagnatore dovrà mandare la ricevuta di lettura che lo abiliterà all'utilizzo della schermata presenza per il corrispettivo turno
- Scaduto: Il turno non può più essere modificato. Scade in automatico allo scoccare della precedente mezzanotte.

L'amministratore può usare il toggle apposito per modificare lo stato prima della scadenza.

### Disponibilità

Questa schermata permette all'utente di indicare le proprie disponibilità per un turno. E' possibile scegliere la linea e la fermata di partenza (o di arrivo) solo se il turno è aperto. Inoltre, è possibile visionare il percorso attraverso una piccola mappa riportante le fermate.

Una volta comunicata la disponibilità, la schermata restituirà un resoconto della stessa con un bottone che permetterà la sua cancellazione fino a quando l'amministratore non chiuderà il turno per procedere alle modifiche e conferme.

Una volta ricevuta la conferma, la schermata proporrà un pulsante per comunicare l'avvenuta ricezione.

### Permessi

In questa schermata è possibile visualizzare amministratori e guide semplici di ogni linea. Ogni amministratore di linea può operare sulla linea di sua competenza promuovendo ad amministratori o declassando a guide semplici gli accompagnatori.

### Comunicazioni

Questa interfaccia è attivata a priori per qualsiasi ruolo e contiene le notifiche paginate dell'utente.
Le comunicazioni vengono notificate in tempo reale grazie all'utilizzo di un WebSocket. Non appena l'utente effettua l'accesso, il sistema lo iscrive al corrispettivo endpoint per abilitare questa funzionalità. 

L'utente avrà la possibilità di cancellare le notifiche lette premendo sull'apposito pulsante.

Abbiamo immaginato due tipologie di notifiche:

- **Notifiche Base**: queste notifiche sono dei semplici avvisi che non richiedono interazione da parte dell'utente.
- **Notifiche Azione**: queste notifiche appaiono all'utente come le precedenti, ma permettono di svolgere comodamente il passo successivo nel flow applicativo (e.g. Disponibilità).

#### Notifiche Principali

Abbiamo deciso di implementare le seguenti notifiche:

- **Notifiche Genitore** *[BASE]*: al genitore vengono comunicate la presa in carico da fermata/scuola, l'arrivo a scuola/fermata e l'eventuale assenza del bambino.
- **Notifiche Guida** *[AZIONE]*: quando un amministratore di linea chiude il turno e conferma la disponibilità comunicata dalla guida, questa riceve una notifica tramite la quale potrà segnalare l'avvenuta lettura.
- **Notifiche Amministratore** *[BASE]*: quando un utente viene promosso (o declassato) ad amministratore di linea, viene avvertito tramite l'opportuna notifica.

### Disponibilità e turni

Per organizzare i turni degli accompagnatori è necessario definire e gestire due fasi distinte.
La prima in cui gli accompagnatori indicano la loro disponibilità per le corse del pedibus (giorno, andata o ritorno) e la seconda in cui l’amministratore (o gli amministratori) del servizio selezionano tra i disponibili gli accompagnatori che effettivamente verranno utilizzati, definendo quindi i turni di ciascun accompagnatore per ciascuna corsa del pedibus.
A fronte della definizione o della modifica di un turno, le persone coinvolte vengono avvisate tramite un messaggio interno
all’applicazione del loro impegno.

## Viste

L’applicazione gestirà le seguenti schermate.

### Registrazione utente e login

All’atto della “creazione” di una nuova linea il sistema manderà analoga mail all’indirizzo definito come amministratore della linea (se non già presente e amministratore della stessa).

### Interfaccia accompagnatore: disponibilità

In tale interfaccia, dopo la definizione dei turni per ciascuna corsa, l’accompagnatore potrà verificare se la propria disponibilità è stata utilizzata o no (cioè se è stato assegnato a quel turno), ed eventualmente confermare la presa visione.