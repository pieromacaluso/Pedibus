## Role breakdown

|  | USER | ACCOMPAGNATORE | ADMIN | SYSTEM-ADMIN |
| --- | --- | --- | --- | --- |
| Mamma | <ul><li> - [x] </li></ul> | <ul><li> - [ ] </li></ul> | <ul><li> - [ ] </li></ul> | <ul><li> - [ ] </li></ul> |
| Mamma casalinga | <ul><li> - [x] </li></ul> | <ul><li> - [x] </li></ul> | <ul><li> - [ ] </li></ul> | <ul><li> - [ ] </li></ul> |
| Mamma casalinga, boss | <ul><li> - [x] </li></ul> | <ul><li> - [ ] </li></ul> | <ul><li> - [x] </li></ul> | <ul><li> - [ ] </li></ul> |
| Nonno | <ul><li> - [ ] </li></ul> | <ul><li> - [x] </li></ul> | <ul><li> - [ ] </li></ul> | <ul><li> - [ ] </li></ul> |
| Nonno, boss | <ul><li> - [ ] </li></ul> | <ul><li> - [ ] </li></ul> | <ul><li> - [x] </li></ul> | <ul><li> - [ ] </li></ul> |
| Nonno che si occupa molto dei bimbi | <ul><li> - [x] </li></ul> | <ul><li> - [x] </li></ul> | <ul><li> - [ ] </li></ul> | <ul><li> - [ ] </li></ul> |
| Nonno che si occupa molto dei bimbi, boss | <ul><li> - [x] </li></ul> | <ul><li> - [ ] </li></ul> | <ul><li> - [x] </li></ul> | <ul><li> - [ ] </li></ul> |

Se ci viene più comodo possiamo usare drive https://docs.google.com/document/d/179EY5c0OUg_GRBTm9K-zO88aBWxs957rbkmt13e51RU/edit?usp=sharing

Ad oggi (pre consegna es5):  
- Un genitore ha solo il ruolo USER  
- Tutti i nonni hanno ruolo ADMIN per una linea
- Quando aggiungiamo attraverso endpoint il ruolo ADMIN ha un account lo sommiamo a quello di USER
- I vari ADMIN possono effettuare/modificare una prenotazione solo nel giorno corrente e per la linea di cui è amministratore
- Il system admin può effettuare/modificare una prenotazione quando vuole

## User breakdown
System admin:   
applicazioni.internet.mmap@gmail.com  
12345@Sys  

Altri utenti:    
La password in ogni caso è -> 1!qwerty1!

Abbiamo 50 genitori con ROLE_USER, ad es:  
(genitori.json)
- miles.reilly@test.it
- enid.crawford@test.it
- morales.holloway@test.it

25 nonni con ROLE_ADMIN, linea1:   
(nonni_linea1.json) 
- reed.snyder@test.it
- alexandra.winters@test.it
- briana.butler@test.it
  
25 nonni con ROLE_ADMIN, linea2:   
(nonni_linea2.json) 
- mai.berg@test.it
- cooley.bradshaw@test.it
- juliet.salazar@test.it
