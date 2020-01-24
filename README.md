<h1 align="center">
  <a href="https://github.com/pieromacaluso/Pedibus" title="Pedibus Documentation">
    <img alt="RoomMonitor" src="pedibus/stuff/img/logo.png" height="100px" />
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
Repository delle esercitazioni e del progetto finale del corso "Applicazioni Internet" tenuto dal prof. Giovanni Malnati nell' a.a. 2018/2019

## Docker Guide

**ATTENZIONE: se i comandi qui sotto non dovessero funzionare al primo colpo, inserire un bel `sudo`**

### Build Backend

1. Posizionarsi da terminale all'interno del progetto del backend (es. *esercitazione3*).
2. Avviare il comando ` mvn clean install -Pprod -Dmaven.test.skip=true`

### Build Frontend

1. Posizionarsi da terminale all'interno del progetto del frontend (es. *esercitazione4*).
2. Avviare il comando `npm install`
3. Avviare il comando `ng build --prod`

### Docker Compose'n'Start
1. Posizionarsi da terminale all'interno della root del repository (dove si trova il `docker-compose.yml`)
2. Se è stato già avviato in precedenza, assicurarsi che il docker sia `down` con il comando: `docker-compose down --volumes`
3. Avviare il comando `docker-compose up --build`

Collegarsi a [http://localhost/](http://localhost/) per accedere al frontend. Per dialogare direttamente con il backend fare richieste a [http://localhost:8080](http://localhost:8080) 

## Team
- [Marco Florian](https://github.com/MarcoFlo) - s247030
- [Piero Macaluso](https://github.com/pieromacaluso) - s252894
- [Marco Nanci](https://github.com/GJGits) - s255089
- [Angelo Turco](https://github.com/angeloturco) - s255270

