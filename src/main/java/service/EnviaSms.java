package service;

import dominio.Participante;
import dominio.Sms;

public class EnviaSms {
    public void enviarSmsParaVencedor(Sms sms, Participante participante) {
        sms.setMensagem("Parabens pela sua vitoria " + participante.getNome() + "!!!");
    }
}
