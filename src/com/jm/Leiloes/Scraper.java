
package com.jm.Leiloes;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFileOptions.Builder;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.RetryParams;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



/**
 *
 * @author joaomota
 */
@SuppressWarnings("serial")
public class Scraper extends HttpServlet implements Serializable {

    public static final String BUCKETNAME = "scrapingbucket";
    public static final String FILENAME = "resultsVendas.csv";

    //private DatastoreService dbs;

    // TODO code application logic here
    // http://www.e-financas.gov.pt/vendas/consultaVendasCurso.action?page=2&maximo=&concelho=&freguesia=&tipoBem=&dataMax=&dataMin=&distrito=&modalidade=&minimo=&tipoConsulta=XX
    // summary of changes/diff with previous version of csv
    // cron job (google has it) to scrape & parse (http://www.mkyong.com/google-app-engine/cron-job-on-google-app-engine-for-java/)
    // create twitter, instagram profiles
    // stream articles w/fotos in twitter
    // monitor deadline, post to social networks
    // Google fusion tables with results ...?

    //TESTED:
    //FBConnector facebook = new FBConnector();

    public String parse(){

        LinkedList<Leilao> leiloes = new LinkedList<>();

        final GcsService gcsService = GcsServiceFactory.createGcsService(
                new RetryParams.Builder()
                    .initialRetryDelayMillis(10)
                    .retryMaxAttempts(10)
                    .totalRetryPeriodMillis(15000)
                    .build()
        );

        GcsFilename fileName = new GcsFilename(BUCKETNAME,FILENAME);
        
        //dbs = DatastoreServiceFactory.getDatastoreService();    // init objectify datastore service

        GcsOutputChannel outputChannel = null;
        GcsFileOptions.Builder gcsOptionsBuilder = new GcsFileOptions.Builder();
        
        gcsOptionsBuilder.contentEncoding("ISO-8859-1");


        try {
            // TODO Fix character encoding on generated file
            GcsFileOptions gcsFileOptionsConfig = gcsOptionsBuilder.build();
            outputChannel = gcsService.createOrReplace(fileName, gcsFileOptionsConfig);

        } catch (IOException e) {
            System.out.println("something went wrong opening an output channel to the GCS service");
            e.printStackTrace();
        }

        PrintWriter writer = new PrintWriter(Channels.newWriter(outputChannel, "ISO-8859-1"));

        try {
            Document doc;
            //StringWriter res = new StringWriter();


            // PrintWriter writer = new PrintWriter("file", "ISO-8859-1");

            writer.println("Tipologia;URL;Número;Preço base;Data limite;Serviço de Finanças;Estado;Modalidade");

            String urlToOpen = "http://www.e-financas.gov.pt/vendas/consultaVendasCurso.action?tipoConsulta=02&modalidade=&distrito=&concelho=&minimo=++.+++.+++.+++%2C++&maximo=++.+++.+++.+++%2C++&dataMin=&dataMax=";

            int previousPage = 0, pag;
            boolean beyondEnd = false;


            do{

                doc = Jsoup.connect(urlToOpen).get();

                // debugging
                pag = Integer.parseInt(doc.baseUri().substring(doc.baseUri().indexOf("=") + 1, doc.baseUri().indexOf("&")));

                System.out.println("A processar página #" + pag);

                //Entity e = new Entity("leilao");

                if (pag < previousPage){
                    beyondEnd = true;
                }
                previousPage = pag;

                for (Element e : doc.getElementsByClass("w95")){
                    boolean found = false;

                    Leilao leilao = new Leilao();

                    Elements tipologia = e.getElementsByClass("info-table-title");
                    if (tipologia.size() > 1){
                        writer.print(tipologia.get(1).text() + ";");

                        leilao.setTipologia(tipologia.get(1).text());
                        String url = tipologia.get(2).getElementsByAttribute("href").first().attr("abs:href");
                        writer.print(url + ";");
                        leilao.setUrl(url);
                    }

                    for (Element imovel : e.getElementsByClass("info-element")){
                        found = true;
                        ArrayList<String> resto = new ArrayList<String>();
                        for (Element infoText : imovel.getElementsByClass("info-element-text")){
                            writer.print(infoText.text() + ";");
                            resto.add(infoText.text());
                        }
                        // TODO acrescentar resto à DataStore, ajustar parsing morada (tamanho variável)

                    }

                    if (found){
                        writer.println();
                        //dbs.put(leilao);

                        try {
                            OfyService.ofy().save().entity(leilao).now();
                            leiloes.add(leilao);
                        } catch (Exception e1) {
                            System.out.println("persistence to datastore failed");
                            e1.printStackTrace();
                        }

                    }
                }

                Element el = doc.getElementsByAttributeValueStarting("href","consultaVendasCurso").last();

                //System.out.println("último elemento identificado com: " + el.attr("href"));



                urlToOpen = el.attr("abs:href");

            } while((doc.hasText()) && (!beyondEnd));
            writer.flush();
            writer.close();
            //dbs.put(leilao);

        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
            Logger.getLogger(Scraper.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                outputChannel.waitForOutstandingWrites();

                //outputChannel.close();

                System.out.println("finished writing to GCS");

                for(Leilao l : leiloes){
                    System.out.println("datastore: leilão " + l.getId() + " com tipologia " + l.getTipologia());
                    //OfyService.factory().begin();
                    OfyService.ofy().save().entity(l).now();
                }
                System.out.println("saving to datastore complete");

/*                ListIterator<Leilao> iterLeiloes = leiloes.listIterator();
                while(iterLeiloes.hasNext()){
                    System.out.print("*");
                    OfyService.ofy().save().entity(iterLeiloes.next()).now();
                }*/


                return "Tamanho da lista de leilões: " + leiloes.size();


            } catch (IOException e) {
                System.out.println("something went wrong finalizing writes to output channel");
                e.printStackTrace();
            }

        }
        return "parsing function failed";
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{

        // trigger events to update status on jsp (not working?)
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");

        PrintWriter writer = response.getWriter();
        writer.write("event:status_change\n");
        writer.write("data:parser initiated\n\n");
        writer.flush();

        writer.write(this.parse() + "\n");

        writer.write("event:status_change\n");
        writer.write("data:parser completed\n\n");
        writer.flush();
        writer.close();

        System.out.println("write operation complete.");

        //PrintWriter out = response.getWriter();

        /*int count = 0;

        ThreadFactory tf = ThreadManager.currentRequestThreadFactory();

        Thread t = tf.newThread(ScraperVendasFinancas());

        t.start();
        System.out.println("Parser thread running.");

        while(t.isAlive()){
            request.setAttribute("scrapeCounter", count++);
            request.setAttribute("testCount", count);

            this.getServletContext().getRequestDispatcher("/index.jsp").forward(request,response);
            System.out.println("Hit: " + count);
        }

        request.setAttribute("runningParser", t);
        this.getServletContext().getRequestDispatcher("/index.jsp").forward(request,response);
        System.out.println("pushed thread object back into JSP");

        System.out.println();
        System.out.println("Finished.");*/
        //return true;

    }


}
