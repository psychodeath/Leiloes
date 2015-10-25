package com.jm.Leiloes;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.tools.cloudstorage.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;

/**
 * Created by joaomota on 16/08/15.
 */
public class ScraperTesting extends HttpServlet {

    public static final String BUCKETNAME = "scrapingbucket";
    public static final String FILENAME = "resultsVendas.csv";
    private static final int BUFFER_SIZE = 2 * 1024 * 1024;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Running tests");

        DatastoreService dbs = DatastoreServiceFactory.getDatastoreService();    // init objectify datastore service


    }
}
