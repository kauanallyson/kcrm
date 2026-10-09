package br.com.kauanallyson.kcrm.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

// Vai no JWT e vira a authority do autenticado: Corretor só usa a Carteira, Administrador só a administração
public enum Papel {
    CORRETOR,
    ADMINISTRADOR;

    public GrantedAuthority authority() {
        return new SimpleGrantedAuthority("ROLE_" + name());
    }
}
