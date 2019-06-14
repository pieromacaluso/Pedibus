# Docker Guide

**ATTENZIONE: se i comandi qui sotto non dovessero funzionare al primo colpo, inserire un bel `sudo`**

## Build Backend

1. Posizionarsi da terminale all'interno del progetto del backend.
2. Avviare il comando ` ./mvnw clean install -Pprod -Dmaven.test.skip=true`

## Build Frontend

1. Posizionarsi da terminale all'interno del progetto del frontend.
2. Avviare il comando `npm install`
3. Avviare il comando `ng build --prod`

## Docker Compose'n'Start
1. Posizionarsi da terminale all'interno della cartella `pedibus` (dove si trova il `docker-compose.yml`)
2. Se è stato già avviato in precedenza, assicurarsi che il docker sia `down` con il comando: `docker-compose down --volumes`
3. Avviare il comando `docker-compose up --build`

Collegarsi a [http://localhost:4200/](http://localhost:4200/) per accedere al frontend. Per dialogare direttamente con il backend fare richieste a [http://localhost:8080](http://localhost:8080) 

**ATTENZIONE:** Se si sta utilizzando Windows Home con Docker Toolkit sostituire `localhost` con l'indirizzo della VM che si sta utilizzando.
