package com.jm.leiloes;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.auth.AccessToken;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author joaomota
 */
public class FBConnector {
    // app id: 882469371824638
    
    private final String pageAccessToken;
    private final Facebook fbPage;
    
    public FBConnector(){
        this.pageAccessToken = "CAAMimeluyf4BAGFkJHT1AGukH7J7wtKPKZBYf6795f7mi6iKAJwHlCsPYObSJ0zn9XzKt9PIX8kyigPeuhjzKBK7isGsOCpJWGJxMjsQQEWRsIv4uYsVfL51tKJcpIeKw8pRLtndnsOKJWQzYa4CY9fdsjGKwpvBnOvamYiujy5bXsH3WlB8iJZClo46UjdZCl2b9muXoTOd4Yi7Lk7";
        AccessToken at = new AccessToken(this.pageAccessToken);
            
        this.fbPage = new FacebookFactory().getInstance();
        this.fbPage.setOAuthAppId("", "");
        this.fbPage.setOAuthAccessToken(at);
    }
    
    public void postUpdate(String msg){
        try {            
            this.fbPage.postStatusMessage(msg);
        } catch (FacebookException ex) {
            Logger.getLogger(FBConnector.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getErrorMessage());
        }        
    }
    
}
