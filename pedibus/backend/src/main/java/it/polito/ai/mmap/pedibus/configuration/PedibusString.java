package it.polito.ai.mmap.pedibus.configuration;

import it.polito.ai.mmap.pedibus.entity.UserEntity;

public class PedibusString {

    public static final String TOKEN_NOT_FOUND = "Token non trovato";
    public static final String RECOVER_FAILED = "Processo di recupero fallito";
    public static final String TOKEN_PROCESS_FAILED = "Il processo token è fallito";
    public static final String INVALID_DATA = "Dati inseriti non validi";
    public static final String POST_CHILD_ERROR = "Problemi nella creazione del bambino, ricontrolla i dati";
    public static final String PUT_CHILD_ERROR = "Problemi nell'aggiornamento del bambino, ricontrolla i dati";
    public static final String RESERVATION_INVALID = "Prenotazione già presente o non valida";
    public static final String PERMISSION_DENIED = "Non hai i privilegi per eseguire questa operazione";
    public static final String UNAUTHORIZED_OPERATION = "Operazione non autorizzata";
    public static final String ALL_DELETED = "Bambini, utenti e prenotazioni sono state cancellati";
    public static final String NOTIFICATION_SENT = "Notifica salvata e inviata";
    public static String POST_USER_ERROR = "Problemi nella creazione dell'utente, ricontrolla i dati";
    public static String PUT_USER_ERROR = "Problemi nell'aggiornamento dell'utente, ricontrolla i dati";
    public static String USER_NOT_FOUND = "Utente Non Trovato";

    public static String NEW_USER_MAIL(String href, UserEntity userEntity) {
        return "<p>Il tuo account è stato appena creato con le seguenti credenziali:</p>" +
                "<p><ul>" +
                "<li><b>Username</b>: " + userEntity.getUsername() + "</li>" +
                "<li><b>Password</b>: " + userEntity.getSurname() + userEntity.getName() + userEntity.getUsername().length() + "</li>" +
                "</ul></p>" +
                "<p><a href='" + href + "'>Clicca qui per modificare la password e attivare il tuo account</a></p>";

    }
    public static String MAIL_SENT(String what, String who) {
        return "Email di " + what + " inviata correttamente a " + who;

    }

    public static String CHILD_DUPLICATE(String cf) {
        return "Bambino con codice fiscale " + cf + "già presente nel database";
    }
    public static String CHILD_NOT_FOUND(String cf) {
        return "Bambino con codice fiscale " + cf + "non trovato nel database";
    }

    public static String ENDPOINT_CALLED(String post, String s) {
        return post + " " + s + " è stato contattato";
    }
}
