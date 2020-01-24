# Installare MongoDB Community Edition su Ubuntu

Il seguente tutorial ripercorre i passaggi indicati sulla documentazione ufficiale, ne riporto soltabti una breve sintesi.
Bisogna precisare che il processo &egrave; stato testato su `Ubuntu 18.04`, che il package `mongodb-org` &egrave; ufficialmente
supportato da MongoDB Inc e che il tutorial &egrave; stato scritto ad **Aprile 2019**.

## 1. Importare la chiave pubblica utilizzata dal package manager

Il package manager di ubuntu `apt` si assicura della consistenza e autenticazione chiedendo ai distributori di package di
fornire tali pacchetti assieme ad una chiave GPG. Lanciare il seguente comando per ottenere una chiave GPG pubblica da MongoDB.

```sh
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 9DA31620334BD75D9DCB49F368818C72E52529D4
```

## 2. Creare un file .list per MongoDB

Creare un file `.list` lanciando il seguente comando, **NB. si consiglia di non modificare la directory di default**.

```sh
echo "deb [ arch=amd64 ] https://repo.mongodb.org/apt/ubuntu bionic/mongodb-org/4.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-4.0.list
```

## 3. Update del package manager

Effettuare un update del package manager.

```sh
sudo apt-get update
```

## 4. Installare il package di MongoDB

Installare l'ultima versione stabile di MongoDB che al momento in cui &egrave; stato scritto il file corrisponde alla `4.0.2`.

```sh
sudo apt-get install -y mongodb-org
```

Opzionalmente si pu&ograve; scegliere di rimanere aggiornati sulle nuove release lanciando i seguenti comandi da linea di comando.

```sh
echo "mongodb-org hold" | sudo dpkg --set-selections
echo "mongodb-org-server hold" | sudo dpkg --set-selections
echo "mongodb-org-shell hold" | sudo dpkg --set-selections
echo "mongodb-org-mongos hold" | sudo dpkg --set-selections
echo "mongodb-org-tools hold" | sudo dpkg --set-selections
```

### Note di installazione

- Molti sistemi operativi Unix-Like limitano le risorse associate ad una sessione, questo può inficiare sulle prestazioni delle stesse.
- Durante l'installazione vengono create due directory, una data directory `/var/lib/mongodb` e una per i log `/var/log/mongodb`.
- Prima di effettuare il deploy di MongoDB in un production environment &egrave; meglio consultare le [note di produzione](https://docs.mongodb.com/manual/administration/production-notes/).

## Interagire con il servizio mongod

Per interagire con i servizi offerti da MongoDB si utilizza un client che nella terminologia shell di mongo viene detto [mongod](https://docs.mongodb.com/manual/reference/program/mongod/#bin.mongod).

### Start MongoDB

Per iniziare ad utilizzare un mongod lanciare il seguente comando.

```sh
sudo service mongod start
```
### Verificare che il servizio sia partito

Per verificare che l'inizializzazione del servizio sia andata a buon fine bisogna controllare il file di log situato nel path `/var/log/mongodb/mongod.log`, tale file dovr&agrave; riportare una riga con scritto:

```sh
 [initandlisten] waiting for connections on port 27017
```
la porta 27017 &egrave; la porta di default di mongo.

### Stop MongoDB

Quando necessario per stoppare il servizio eseguire il comando

```sh
 sudo service mongod stop
```

### Restart MongoDB

Eventualmente &egrave; possibile effettuare il restart del servizio nel seguente modo

```sh
 sudo service mongod restart
```

## Utilizzare mongo

Per lanciare la shell di mongo, dopo aver lanciato il servizio eseguire il comando

```sh
 mongo
```

**Attenzione:** questo comando permette di poter lanciare la shell mongo in locale, per utilizzare il servizio ATLAS visualizzare il paragrafo relativo.

## Utilizzare mongo con ATLAS

Per utilizzare il servizio ATLAS dopo essersi registrati al servizio ed aver creato un cluster seguire i seguenti passaggi:

1. cliccare su `connect`

![](https://i1.wp.com/codeforgeek.com/wp-content/uploads/2018/03/MongoDB-Cluster-Dashboard.png?resize=640%2C283&ssl=1)

2. selezionare `connect with Mongo Shell`

![](https://www.dremio.com/img/tutorials/analyzing-mongodb-atlas/image_0.png)

3 selezionare l'opzione `I have the Mongo Shell installed` e fare copia incolla del codice generato direttamente nella linea di comando. A questo punto inserire la password.
