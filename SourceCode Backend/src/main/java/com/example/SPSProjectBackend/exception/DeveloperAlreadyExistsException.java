package com.example.SPSProjectBackend.exception;

public class DeveloperAlreadyExistsException extends RuntimeException {
    private final String accNbr;

    public DeveloperAlreadyExistsException(String accNbr) {
        super("Developer already exists with acc_nbr: " + accNbr);
        this.accNbr = accNbr;
    }

    public String getAccNbr() {
        return accNbr;
    }
}
