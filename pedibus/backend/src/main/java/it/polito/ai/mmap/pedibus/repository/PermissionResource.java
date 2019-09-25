package it.polito.ai.mmap.pedibus.repository;

import lombok.Data;

/**
 * Resource che l'admin usa nell'endpoint PUT /admin/users/{userID}
 *
 * aggiungere(true) o eliminare(false)
 */
@Data
public class PermissionResource {
    private String idLinea;
    private boolean addOrDel;
}
