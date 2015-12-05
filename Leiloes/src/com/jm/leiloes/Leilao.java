package com.jm.leiloes;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.io.Serializable;

//import com.sun.javafx.beans.IDProperty;

/**
 * Created by joaomota on 30/08/15.
 */
@Entity
public class Leilao implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -203462816962197819L;
	@Id public Long id;
    public String tipologia;
    public String url;
    public String numero;

    public Leilao() {
    }

    public Long getId() {
        return id;
    }

    public String getTipologia() {
        return tipologia;
    }

    public String getUrl() {
        return url;
    }

    public String getNumero() {
        return numero;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTipologia(String tipologia) {
        this.tipologia = tipologia;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }
}
