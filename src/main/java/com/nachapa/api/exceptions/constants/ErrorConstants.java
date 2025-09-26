package com.nachapa.api.exceptions.constants;

import java.util.AbstractMap;
import java.util.Map;

public class ErrorConstants {

    public static final String INVALID_DESERIALIZATION_SNIPPET = "Cannot deserialize value of type";

    public static final String DUPLICATE_ENTRY_SNIPPET = "Duplicate entry";

    public static final String ERRO_DEFAULT_CODE = "0000";
    public static final String ERRO_DEFAULT_MESSAGE = "Erro Interno!";

    public static final String ERROR_VALUE_NOT_VALID_CODE = "0100";

    public static final String ERROR_VALUE_NOT_VALID_DESERIALIZE_CODE = "0101";
    public static final String ERROR_VALUE_NOT_VALID_DESERIALIZE_MESSAGE = "Erro de Deserializacao";

    public static final String INVALID_CREDENTIALS_CODE = "4001";
    public static final String INVALID_CREDENTIALS_MESSAGE = "Credenciais inválidas";

    public static final String JWT_KEY_MISSING_CODE = "4002";
    public static final String JWT_KEY_MISSING_MESSAGE = "A chave JWT é inválida ou está ausente";

    public static final String EMAIL_ALREADY_REGISTERED_CODE = "5001";
    public static final String EMAIL_ALREADY_REGISTERED_MESSAGE = "E-mail já cadastrado no sistema";

    public static final String USER_NOT_FOUND_CODE = "5002";
    public static final String USER_NOT_FOUND_MESSAGE = "Usuário não encontrado";

    public static final String ERROR_DEACTIVATE_USER_CODE = "5003";
    public static final String ERROR_DEACTIVATE_USER_MESSAGE = "Erro ao inativar Usuario do Sistema";

    public static final String CPF_ALREADY_REGISTERED_CODE = "5004";
    public static final String CPF_ALREADY_REGISTERED_MESSAGE = "CPF já cadastrado no sistema";

    public static final String CPF_CANNOT_BE_CHANGED_CODE = "5005";
    public static final String CPF_CANNOT_BE_CHANGED_CODE_MESSAGE = "CPF não pode ser alterado.";


    public static final String ERROR_LOG_ENTITY_CHANGES_CODE = "10001";
    public static final String ERROR_LOG_ENTITY_CHANGES_MESSAGE = "Erro ao registrar mudanças";

    private static final Map<String, String> ERROR_MAP;

    static {
        ERROR_MAP = Map.ofEntries(
                new AbstractMap.SimpleEntry<>(ERRO_DEFAULT_CODE, ERRO_DEFAULT_MESSAGE),
                new AbstractMap.SimpleEntry<>(INVALID_CREDENTIALS_CODE, INVALID_CREDENTIALS_MESSAGE),
                new AbstractMap.SimpleEntry<>(EMAIL_ALREADY_REGISTERED_CODE, EMAIL_ALREADY_REGISTERED_MESSAGE),
                new AbstractMap.SimpleEntry<>(USER_NOT_FOUND_CODE, USER_NOT_FOUND_MESSAGE),
                new AbstractMap.SimpleEntry<>(JWT_KEY_MISSING_CODE, JWT_KEY_MISSING_MESSAGE),
                new AbstractMap.SimpleEntry<>(ERROR_DEACTIVATE_USER_CODE, ERROR_DEACTIVATE_USER_MESSAGE),
                new AbstractMap.SimpleEntry<>(ERROR_VALUE_NOT_VALID_DESERIALIZE_CODE, ERROR_VALUE_NOT_VALID_DESERIALIZE_MESSAGE),
                new AbstractMap.SimpleEntry<>(CPF_ALREADY_REGISTERED_CODE, CPF_ALREADY_REGISTERED_MESSAGE),
                new AbstractMap.SimpleEntry<>(CPF_CANNOT_BE_CHANGED_CODE, CPF_CANNOT_BE_CHANGED_CODE_MESSAGE),
                new AbstractMap.SimpleEntry<>(ERROR_LOG_ENTITY_CHANGES_CODE, ERROR_LOG_ENTITY_CHANGES_MESSAGE)

        );
    }

    private ErrorConstants() {
    }

    public static Map.Entry<String, String> getError(String code) {
        return new AbstractMap.SimpleEntry<>(code, ERROR_MAP.getOrDefault(code, "Erro desconhecido"));
    }
}