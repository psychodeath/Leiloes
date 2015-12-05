package com.eco;

import java.util.Date;
import java.util.Timer;

/**
 * Created by joaomota on 05/11/15.
 *
 * Classe para gestão do temporizador de cada cliente
 *
 */
public class ClientTimer {

    private long tempoTotal;
    private long ultimoInicio;
    private boolean running;
    private Client reportSocket;

    public ClientTimer(){
        tempoTotal = 0L;
        running = false;
        reportSocket = null;
    }

    public void iniciarTemporizador(){
        ultimoInicio = System.currentTimeMillis();

        running = true;
    }

    public long pararTemporizador(){
        tempoTotal += ((System.currentTimeMillis() - ultimoInicio));
        running = false;
        return tempoTotal;
    }

    public long getTempo(){
        if (this.isRunning()) {
            long t = (((System.currentTimeMillis() - ultimoInicio)) + tempoTotal);
            return t;
        }
        return (tempoTotal);
    }

    public void setReportSocket(Client s){
        this.reportSocket = s;
    }

    public boolean isRunning(){
        return running;
    }
}
