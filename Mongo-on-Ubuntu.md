# Installare MongoDB Community Edition su Ubuntu

Il seguente tutorial ripercorre i passaggi indicati sulla documentazione ufficiale, ne riporto soltabti una breve sintesi.
Bisogna precisare che il processo &egrave; stato testato su `Ubuntu 18.04` e che il package `mongodb-org` &egrave; ufficialmente
supportato da MongoDB Inc.

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

## 3. Update del package managere

Effettuare un update del package manager.

```sh
sudo apt-get update
```

## 4. Installare il package di MongoDB

Installare l'ultima versione stabile di MongoDB che al momento in cui &egrave; stata scritto file corrisponde alla `4.0.2`.

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
