package rrfeisen.transbr.model;

/**
 * Contrato para cargas que podem (ou precisam) ter um seguro calculado.
 * CargaPerigosa assina obrigatoriamente; CargaRegular assina também, mas só
 * retorna um valor de seguro maior que zero quando o valor de mercado é alto.
 */
public interface Seguravel {
    double calcularSeguro();
}
