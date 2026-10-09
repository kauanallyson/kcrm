package br.com.kauanallyson.kcrm.auth;

import java.util.UUID;

// Quem o JWT diz ser: o id e o papel, conferidos no banco a cada requisição
public record Sujeito(UUID id, Papel papel) {
}
