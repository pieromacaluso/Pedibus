package it.polito.ai.mmap.esercitazione3.filter;

import it.polito.ai.mmap.esercitazione3.services.JwtTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class JwtTokenFilter extends GenericFilterBean {

    private JwtTokenService jwtTokenService;

    public JwtTokenFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * Filtro aggiunto per intercettare tutte le richieste http e verificare la presenza o meno del token.
     * Se il token è null e la risorsa richiesta è libera, sarà possibile accedervi, altrimenti sarà ritornato un 401-Unauthorized
     * Se il token è presente ed è valido, l'user è autenticato.
     * @param req
     * @param res
     * @param filterChain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
            throws IOException, ServletException {

        String token = jwtTokenService.resolveToken((HttpServletRequest) req);

        if (token != null && jwtTokenService.validateToken(token)) {
            Authentication auth = token != null ? jwtTokenService.getAuthentication(token) : null;
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(req, res);
    }
}
