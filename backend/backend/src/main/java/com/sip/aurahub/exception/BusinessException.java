package com.sip.aurahub.exception;

/** Violation d'une règle de gestion (RG1..RG7) : renvoyée en HTTP 409. */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
}
