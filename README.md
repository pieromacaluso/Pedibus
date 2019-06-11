# Applicazioni Internet Lab Project
Repository dei laboratori e del progetto finale del corso "Applicazioni Internet" tenuto dal prof. Giovanni Malnati a.a. 2018/2019

## Sommario
|Source Code                        | Testo   |Pubblicato   | Deadline     | Consegnato                |
|-----------------------------------|-        |--------------|--------------|---------------------------|
| [Esercitazione 1](esercitazione1) |[Esercitazione1.pdf](testi_esercitazioni/Esercitazione1.pdf)| *21/03/2019* | *03/04/2019* | <ul><li> - [x] </li></ul> |
| [Esercitazione 2](esercitazione2) |[Esercitazione2.pdf](testi_esercitazioni/Esercitazione2.pdf)| *04/04/2019* | *01/05/2019* | <ul><li> - [x] </li></ul> |
| [Esercitazione 3](esercitazione3) |[Esercitazione3.pdf](testi_esercitazioni/Esercitazione3.pdf)| *02/05/2019* | *15/05/2019* | <ul><li> - [x] </li></ul> |
| [Esercitazione 4](esercitazione4) |[Esercitazione4.pdf](testi_esercitazioni/Esercitazione4.pdf)| *16/05/2019* | *29/05/2019* | <ul><li> - [x] </li></ul> |
| [Esercitazione 5](esercitazione4) |[Esercitazione5.pdf](testi_esercitazioni/Esercitazione5.pdf)| *30/05/2019* | *16/06/2019* | <ul><li> - [ ] </li></ul> |


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

