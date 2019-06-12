# Docker Guide

**ATTENZIONE: se i comandi qui sotto non dovessero funzionare al primo colpo, inserire un bel `sudo`**

## Build Backend

1. Posizionarsi da terminale all'interno del progetto del backend (es. *esercitazione3*).
2. Avviare il comando ` mvn clean install -Pprod -Dmaven.test.skip=true`

## Build Frontend

1. Posizionarsi da terminale all'interno del progetto del frontend (es. *esercitazione4*).
2. Avviare il comando `npm install`
3. Avviare il comando `ng build --prod`

## Docker Compose'n'Start
1. Posizionarsi da terminale all'interno della root del repository (dove si trova il `docker-compose.yml`)
2. Se è stato già avviato in precedenza, assicurarsi che il docker sia `down` con il comando: `docker-compose down --volumes`
3. Avviare il comando `docker-compose up --build`

Collegarsi a [http://localhost/](http://localhost/) per accedere al frontend. Per dialogare direttamente con il backend fare richieste a [http://localhost:8080](http://localhost:8080) 
