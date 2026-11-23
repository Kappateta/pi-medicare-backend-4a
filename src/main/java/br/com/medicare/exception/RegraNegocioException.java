package br.com.medicare.exception;

/** Lançada quando uma regra de negócio é violada. Vira HTTP 409 Conflict. */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
