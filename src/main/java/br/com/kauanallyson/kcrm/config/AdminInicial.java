package br.com.kauanallyson.kcrm.config;

import br.com.kauanallyson.kcrm.exception.UsuarioJaExisteException;
import br.com.kauanallyson.kcrm.model.common.Cpf;
import br.com.kauanallyson.kcrm.model.common.Email;
import br.com.kauanallyson.kcrm.model.common.Endereco;
import br.com.kauanallyson.kcrm.model.common.Telefone;
import br.com.kauanallyson.kcrm.model.usuario.Perfil;
import br.com.kauanallyson.kcrm.model.usuario.Usuario;
import br.com.kauanallyson.kcrm.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Cria o primeiro Admin a partir das variáveis ADMIN_*, já que o cadastro só é feito por um Admin
@Component
public class AdminInicial implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminInicial.class);
    private static final int SENHA_MINIMA = 8;

    private final UsuarioService usuarioService;
    private final Map<String, String> variaveis = new LinkedHashMap<>();
    private final String complemento;

    public AdminInicial(UsuarioService usuarioService,
                        @Value("${admin.nome}") String nome,
                        @Value("${admin.email}") String email,
                        @Value("${admin.senha}") String senha,
                        @Value("${admin.cpf}") String cpf,
                        @Value("${admin.telefone}") String telefone,
                        @Value("${admin.endereco.rua}") String rua,
                        @Value("${admin.endereco.numero}") String numero,
                        @Value("${admin.endereco.bairro}") String bairro,
                        @Value("${admin.endereco.cidade}") String cidade,
                        @Value("${admin.endereco.estado}") String estado,
                        @Value("${admin.endereco.cep}") String cep,
                        @Value("${admin.endereco.complemento}") String complemento) {
        this.usuarioService = usuarioService;
        variaveis.put("ADMIN_NOME", nome);
        variaveis.put("ADMIN_EMAIL", email);
        variaveis.put("ADMIN_SENHA", senha);
        variaveis.put("ADMIN_CPF", cpf);
        variaveis.put("ADMIN_TELEFONE", telefone);
        variaveis.put("ADMIN_ENDERECO_RUA", rua);
        variaveis.put("ADMIN_ENDERECO_NUMERO", numero);
        variaveis.put("ADMIN_ENDERECO_BAIRRO", bairro);
        variaveis.put("ADMIN_ENDERECO_CIDADE", cidade);
        variaveis.put("ADMIN_ENDERECO_ESTADO", estado);
        variaveis.put("ADMIN_ENDERECO_CEP", cep);
        // Opcional: fica fora de variaveis para não contar como ausente
        this.complemento = complemento;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (usuarioService.existeAdminAtivo()) {
            return;
        }
        List<String> ausentes = variaveis.entrySet().stream()
                .filter(e -> e.getValue() == null || e.getValue().isBlank())
                .map(Map.Entry::getKey)
                .toList();
        if (!ausentes.isEmpty()) {
            log.warn("No active Admin exists and the initial Admin was not created: missing {}. "
                    + "Nobody can create Usuarios until these are set and the app restarts.", ausentes);
            return;
        }
        criar();
    }

    // Variáveis presentes mas inválidas são erro de configuração: falha na subida com mensagem clara
    private void criar() {
        String senha = variaveis.get("ADMIN_SENHA");
        if (senha.length() < SENHA_MINIMA) {
            throw new IllegalStateException("ADMIN_SENHA must have at least " + SENHA_MINIMA + " characters");
        }
        Usuario.Dados dados;
        try {
            dados = new Usuario.Dados(variaveis.get("ADMIN_NOME").strip(), new Cpf(variaveis.get("ADMIN_CPF")),
                    new Email(variaveis.get("ADMIN_EMAIL")), new Telefone(variaveis.get("ADMIN_TELEFONE")),
                    Endereco.de(variaveis.get("ADMIN_ENDERECO_RUA"), variaveis.get("ADMIN_ENDERECO_NUMERO"), complemento,
                            variaveis.get("ADMIN_ENDERECO_BAIRRO"), variaveis.get("ADMIN_ENDERECO_CIDADE"),
                            variaveis.get("ADMIN_ENDERECO_ESTADO"), variaveis.get("ADMIN_ENDERECO_CEP")));
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid initial Admin variables: " + e.getMessage(), e);
        }
        try {
            Usuario admin = usuarioService.cadastrar(dados, Perfil.ADMIN, senha);
            log.info("Initial Admin created: {}", admin.getEmail().value());
        } catch (UsuarioJaExisteException e) {
            throw new IllegalStateException("ADMIN_EMAIL or ADMIN_CPF already belongs to a Usuario that is not an "
                    + "active Admin; fix the variables or promote that Usuario", e);
        }
    }
}
