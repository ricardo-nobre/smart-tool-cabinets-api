package smarttoolcabinets.support;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Factory de dados de teste para acelerar criacao de fixtures.
 *
 * Relaciona-se com testes unitarios e de integracao.
 * Falta implementar factories de DTOs e entidades por dominio.
 */
public final class TestDataFactory {

    private TestDataFactory() {
    }

    /**
     * Objetivo: gerar UUID estavel para testes simples.
     * Inputs esperados: sufixo numerico para distinguir cenarios.
     * Output esperado: UUID no formato 000...XYZ.
     * Passos logicos a implementar:
     * 1) Definir padrao de serializacao do sufixo.
     * 2) Garantir que o UUID resultante e valido.
     * Notas: manter determinismo para testes reprodutiveis.
     */
    public static UUID deterministicUuid(int suffix) {
        String tail = String.format("%012d", Math.max(0, suffix));
        return UUID.fromString("00000000-0000-0000-0000-" + tail);
    }

    /**
     * Objetivo: fornecer timestamp base para cenarios de teste.
     * Inputs esperados: nenhum.
     * Output esperado: OffsetDateTime fixo.
     * Passos logicos a implementar:
     * 1) Ajustar data para cenarios de ordering temporal.
     * 2) Reutilizar em asserts de eventos/sessoes.
     * Notas: evitar dependencia de relogio real nos testes.
     */
    public static OffsetDateTime baseTime() {
        return OffsetDateTime.parse("2026-01-01T10:00:00Z");
    }
}

