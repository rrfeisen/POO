package rrfeisen.transbr.model;

import java.io.Serializable;

/**
 * Representa um colaborador motorista da TransBR.
 */
public class Motorista implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String nome;
    private String cnh;
    private boolean moppAtivo;

    public Motorista(String id, String nome, String cnh, boolean moppAtivo) {
        this.id = id;
        this.nome = nome;
        this.cnh = cnh;
        this.moppAtivo = moppAtivo;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCnh() {
        return cnh;
    }

    public boolean isMoppAtivo() {
        return moppAtivo;
    }

    public void setMoppAtivo(boolean moppAtivo) {
        this.moppAtivo = moppAtivo;
    }

    @Override
    public String toString() {
        return String.format("Motorista [ID: %s, Nome: %s, CNH: %s, MOPP: %s]",
                id, nome, cnh, moppAtivo ? "ATIVO" : "INATIVO");
    }
}
