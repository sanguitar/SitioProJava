package com.example.sitiopro.administracao.configuracao.service;

import com.example.sitiopro.administracao.configuracao.config.ConfiguracaoOperacionalInicialProperties;
import com.example.sitiopro.administracao.configuracao.dto.ConfiguracaoOperacionalForm;
import com.example.sitiopro.administracao.configuracao.dto.ConfiguracaoOperacionalLeitura;
import com.example.sitiopro.administracao.configuracao.entity.ConfiguracaoOperacional;
import com.example.sitiopro.administracao.configuracao.repository.ConfiguracaoOperacionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DateTimeException;
import java.time.ZoneId;

@Service
public class ConfiguracaoOperacionalService {

    private static final String TIMEZONE_SEGURO = "Etc/UTC";
    private final ConfiguracaoOperacionalRepository repository;
    private final ConfiguracaoOperacionalInicialProperties propriedadesIniciais;

    public ConfiguracaoOperacionalService(ConfiguracaoOperacionalRepository repository,
            ConfiguracaoOperacionalInicialProperties propriedadesIniciais) {
        this.repository = repository;
        this.propriedadesIniciais = propriedadesIniciais;
    }

    @Transactional
    public void inicializar() {
        if (repository.buscarParaInicializar().isEmpty()) criarConfiguracaoInicial();
    }

    @Transactional(readOnly = true)
    public ConfiguracaoOperacionalLeitura obter() {
        return leitura(buscar());
    }

    private ConfiguracaoOperacional buscar() {
        return repository.findById(ConfiguracaoOperacional.ID_UNICO)
                .orElseThrow(() -> new IllegalStateException("Configuração operacional não inicializada."));
    }

    @Transactional
    public ConfiguracaoOperacionalLeitura atualizar(ConfiguracaoOperacionalForm form) {
        Valores validados = validar(form);
        ConfiguracaoOperacional configuracao = buscar();
        configuracao.atualizar(validados.nomePropriedade(), validados.timezone(),
                validados.latitude(), validados.longitude(), validados.diasPadraoIncubacao(),
                validados.antecedenciaAlertaEclosaoDias());
        return leitura(repository.saveAndFlush(configuracao));
    }

    private ConfiguracaoOperacional criarConfiguracaoInicial() {
        Valores iniciais = valoresIniciaisSeguros();
        return repository.saveAndFlush(new ConfiguracaoOperacional(
                iniciais.nomePropriedade(), iniciais.timezone(), iniciais.latitude(), iniciais.longitude(),
                iniciais.diasPadraoIncubacao(), iniciais.antecedenciaAlertaEclosaoDias()));
    }

    private Valores validar(ConfiguracaoOperacionalForm form) {
        String nome = textoObrigatorio(form.getNomePropriedade(), "nomePropriedade",
                "Informe o nome da propriedade.");
        if (nome.length() > 120) throw new ConfiguracaoOperacionalInvalidaException(
                "nomePropriedade", "O nome deve ter no máximo 120 caracteres.");
        String timezone = timezoneValido(form.getTimezone(), "timezone");
        validarCoordenadas(form.getLatitude(), form.getLongitude());
        int dias = form.getDiasPadraoIncubacao() == null ? 0 : form.getDiasPadraoIncubacao();
        int antecedencia = form.getAntecedenciaAlertaEclosaoDias() == null
                ? -1 : form.getAntecedenciaAlertaEclosaoDias();
        if (dias < 1 || dias > 120) {
            throw new ConfiguracaoOperacionalInvalidaException("diasPadraoIncubacao",
                    "O período de incubação deve ficar entre 1 e 120 dias.");
        }
        if (antecedencia < 0 || antecedencia > 30 || antecedencia > dias) {
            throw new ConfiguracaoOperacionalInvalidaException("antecedenciaAlertaEclosaoDias",
                    "A antecedência deve ficar entre 0 e 30 dias e não pode superar o período de incubação.");
        }
        return new Valores(nome, timezone, form.getLatitude(), form.getLongitude(), dias, antecedencia);
    }

    private Valores valoresIniciaisSeguros() {
        String nome = StringUtils.hasText(propriedadesIniciais.getNomePropriedade())
                ? propriedadesIniciais.getNomePropriedade().trim() : "Sítio Guaratinguetá";
        if (nome.length() > 120) nome = "Sítio Guaratinguetá";
        String timezone = timezoneOuPadrao(propriedadesIniciais.getTimezone());
        BigDecimal latitude = decimalOuNulo(propriedadesIniciais.getLatitude(), BigDecimal.valueOf(-90),
                BigDecimal.valueOf(90));
        BigDecimal longitude = decimalOuNulo(propriedadesIniciais.getLongitude(), BigDecimal.valueOf(-180),
                BigDecimal.valueOf(180));
        if (latitude == null || longitude == null) {
            latitude = null;
            longitude = null;
        }
        int dias = propriedadesIniciais.getDiasPadraoIncubacao();
        if (dias < 1 || dias > 120) dias = 21;
        int antecedencia = propriedadesIniciais.getAntecedenciaAlertaEclosaoDias();
        if (antecedencia < 0 || antecedencia > 30 || antecedencia > dias) antecedencia = Math.min(2, dias);
        return new Valores(nome, timezone, latitude, longitude, dias, antecedencia);
    }

    private void validarCoordenadas(BigDecimal latitude, BigDecimal longitude) {
        if (latitude != null && latitude.scale() > 6) throw new ConfiguracaoOperacionalInvalidaException(
                "latitude", "Use até 6 casas decimais na latitude.");
        if (longitude != null && longitude.scale() > 6) throw new ConfiguracaoOperacionalInvalidaException(
                "longitude", "Use até 6 casas decimais na longitude.");
        if ((latitude == null) != (longitude == null)) {
            throw new ConfiguracaoOperacionalInvalidaException(
                    latitude == null ? "latitude" : "longitude", "Informe latitude e longitude em conjunto.");
        }
        if (latitude != null && (latitude.compareTo(BigDecimal.valueOf(-90)) < 0
                || latitude.compareTo(BigDecimal.valueOf(90)) > 0)) {
            throw new ConfiguracaoOperacionalInvalidaException("latitude", "A latitude deve ficar entre -90 e 90.");
        }
        if (longitude != null && (longitude.compareTo(BigDecimal.valueOf(-180)) < 0
                || longitude.compareTo(BigDecimal.valueOf(180)) > 0)) {
            throw new ConfiguracaoOperacionalInvalidaException("longitude",
                    "A longitude deve ficar entre -180 e 180.");
        }
    }

    private String textoObrigatorio(String valor, String campo, String mensagem) {
        if (!StringUtils.hasText(valor)) throw new ConfiguracaoOperacionalInvalidaException(campo, mensagem);
        return valor.trim();
    }

    private String timezoneValido(String valor, String campo) {
        String timezone = textoObrigatorio(valor, campo, "Informe o timezone.");
        try {
            if (!ZoneId.getAvailableZoneIds().contains(timezone)) throw new DateTimeException("Timezone inválido");
            return ZoneId.of(timezone).getId();
        } catch (DateTimeException ex) {
            throw new ConfiguracaoOperacionalInvalidaException(campo, "Informe um timezone IANA válido.");
        }
    }

    private String timezoneOuPadrao(String valor) {
        try {
            return StringUtils.hasText(valor) && ZoneId.getAvailableZoneIds().contains(valor.trim())
                    ? ZoneId.of(valor.trim()).getId() : TIMEZONE_SEGURO;
        } catch (DateTimeException ex) {
            return TIMEZONE_SEGURO;
        }
    }

    private BigDecimal decimalOuNulo(String valor, BigDecimal minimo, BigDecimal maximo) {
        try {
            if (!StringUtils.hasText(valor)) return null;
            BigDecimal decimal = new BigDecimal(valor.trim());
            return decimal.compareTo(minimo) >= 0 && decimal.compareTo(maximo) <= 0
                    ? decimal.setScale(6, RoundingMode.HALF_UP) : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private ConfiguracaoOperacionalLeitura leitura(ConfiguracaoOperacional configuracao) {
        return new ConfiguracaoOperacionalLeitura(configuracao.getNomePropriedade(), configuracao.getTimezone(),
                configuracao.getLatitude(), configuracao.getLongitude(), configuracao.getDiasPadraoIncubacao(),
                configuracao.getAntecedenciaAlertaEclosaoDias(), configuracao.getRevisaoLocalizacao(), configuracao.getAlteradoEm(),
                configuracao.getAlteradoPor());
    }

    private record Valores(String nomePropriedade, String timezone, BigDecimal latitude, BigDecimal longitude,
                           int diasPadraoIncubacao, int antecedenciaAlertaEclosaoDias) {
    }
}
