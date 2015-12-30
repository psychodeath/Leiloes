
package leiloes;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.repackaged.com.google.datastore.v1.Datastore;
import com.googlecode.objectify.Key;
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

    public static final String BASE_URL_TO_SCRAPE = "http://www.e-financas.gov.pt/vendas/consultaVendasCurso.action?tipoConsulta=02&modalidade=&distrito=&concelho=&minimo=++.+++.+++.+++%2C++&maximo=++.+++.+++.+++%2C++&dataMin=&dataMax=";

    // TODO code application logic here
    // ****** Google SQL: https://cloud.google.com/appengine/docs/java/cloud-sql/
    // http://www.e-financas.gov.pt/vendas/consultaVendasCurso.action?page=2&maximo=&concelho=&freguesia=&tipoBem=&dataMax=&dataMin=&distrito=&modalidade=&minimo=&tipoConsulta=XX
    // summary of changes/diff with previous version of csv
    // cron job (google has it) to scrape & parse (http://www.mkyong.com/google-app-engine/cron-job-on-google-app-engine-for-java/)
    // create twitter, instagram profiles
    // stream articles w/fotos in twitter
    // monitor deadline, post to social networks
    // Google fusion tables with results ...?
    // JavaScript to show dynamic scraping status? http://www.javaworld.com/article/2076181/web-app-frameworks/dynamic-webpages-with-json.html?page=2

    //TESTED:
    //FBConnector facebook = new FBConnector();

    // *** ALTERNATIVA: https://github.com/google/google-apps-script-samples/blob/master/bibstro/datastore.gs
    // ** https://script.google.com/d/1WPqlFxl5_UIATSTI3GRD9PaBulbVen8MOmFwcAkZ60A_V-HqiKToIX21/edit?usp=drive_web

	public static String WriteLeilaoToGcs(LinkedList<Leilao> leiloes){
		GcsLeiloesWriter glw = new GcsLeiloesWriter();
		
		for(Leilao l : leiloes){
			System.out.println("datastore: leilão " + l.getId() + " com tipologia " + l.getTipologia());
			
			glw.write(l.getTipologia() + ";");
			glw.write(l.getUrl() + ";");
			glw.writeln();
            glw.flush();
            //glw.close();
            
            OfyService.ofy().save().entity(l).now();
            System.out.println("saving to datastore complete");
		}
		
        try {            
            glw.waitForWrites();
            System.out.println("finished writing file to GCS : " + leiloes.size() + " records written.");
            glw.close();
            return "Tamanho da lista de leilões: " + leiloes.size();
        } catch (IOException e) {
            System.out.println("something went wrong finalizing writes to output channel");
            e.printStackTrace();
        }

        return "commit to gcs exited abnormally";
	}
	
    public static boolean parse(ParserState ps){

        String urlToOpen;
        LinkedList<Leilao> leiloes = ps.getLeiloes();
        //int previousPage = ps.page;
        Document doc = ps.getDoc();

        int pag;

        pag = Integer.parseInt(doc.baseUri().substring(doc.baseUri().indexOf("=") + 1, doc.baseUri().indexOf("&")));

        System.out.println("A processar página #" + pag);

        if (pag < ps.getPage()){
            // beyondEnd = true;
            ps.setPage(0); // reset state for next run
            return true;
        }
        ps.setPage(pag);

        for (Element e : doc.getElementsByClass("w95")){
            boolean found = false;

            Leilao leilao = new Leilao();

            Elements tipologia = e.getElementsByClass("info-table-title");
            if (tipologia.size() > 1){

                leilao.setTipologia(tipologia.get(1).text());
                String url = tipologia.get(2).getElementsByAttribute("href").first().attr("abs:href");

                leilao.setUrl(url);
            }
            else{
                leilao.setTipologia("n/a");
                leilao.setUrl("n/a");
            }

            for (Element imovel : e.getElementsByClass("info-element")){
                found = true;
                ArrayList<String> resto = new ArrayList<String>();
                for (Element infoText : imovel.getElementsByClass("info-element-text")){
                    resto.add(infoText.text());
                }
                // TODO acrescentar resto à bd, ajustar parsing morada (tamanho variável)

            }

            if (found){
                leiloes.add(leilao);
            }
        }

        Element el = doc.getElementsByAttributeValueStarting("href","consultaVendasCurso").last();

        ps.setUrl(el.attr("abs:href"));
        ps.setLeiloes(leiloes);

        OfyService.ofy().save().entity(ps); // persistir estado na datastore

        if (pag % 25 == 0){         // processar em blocos de 25
            return true;
        }

        return false;
    }

    public static void runScraper(){

        // TODO Obsolete after serializing parsing state!! better not use until refactoring

        ParserState pState = new ParserState();
        Document doc;

        LinkedList<Leilao> leiloes = new LinkedList<>();

        pState.setUrl("http://www.e-financas.gov.pt/vendas/consultaVendasCurso.action?tipoConsulta=02&modalidade=&distrito=&concelho=&minimo=++.+++.+++.+++%2C++&maximo=++.+++.+++.+++%2C++&dataMin=&dataMax=");
        do {
            try {
                doc = Jsoup.connect(pState.getUrl()).get();
                pState.setDoc(doc);
            } catch (IOException e) {
                System.out.println("Erro a abrir site das finanças");
                e.printStackTrace();
                break;
            }

        } while (!parse(pState));

        WriteLeilaoToGcs(leiloes);

        System.out.println("Parsed and stored up to page " + pState.getPage());

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{

        // trigger events to update status on jsp (not working?)
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");


        PrintWriter writer = response.getWriter();

        ParserState pState = new ParserState();
        Document doc;

        try {
            pState = OfyService.ofy().load().type(ParserState.class).id(666L).now();

            if((pState == null) || (pState.getPage() == 0)){
                System.out.println("nothing to retrieve");
                pState = new ParserState();
                pState.setUrl(BASE_URL_TO_SCRAPE);
            } else {
                System.out.println("retrieved state from datastore");
                System.out.println("retrieved URL: " + pState.getUrl());
                System.out.println("retrieved page #: " + pState.getPage());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        LinkedList<Leilao> leiloes = new LinkedList<>();

        do {
            try {
                doc = Jsoup.connect(pState.getUrl()).get();
                pState.setDoc(doc);
            } catch (IOException e) {
                System.out.println("Erro a abrir site das finanças");
                e.printStackTrace();
                break;
            }

        } while (!parse(pState));

        // TODO retirar upload para o GCS e substituir por commit no Google SQL - a criação do CSV deverá ser feita a partir daí
        // Hipótese 1: caso seja rápido (o suficiente para evitar o timeout) percorrer a DB, fazer o CSV imediatamente a seguir
        // Hipótese 2: caso contrário, criar helper app noutro sistema, tipo Heroku ou similar...

        WriteLeilaoToGcs(pState.getLeiloes());      // Problema: só vai escrever o último bloco processado...

        writer.write("Parsed and stored up to page " + pState.getPage());

        writer.flush();
        writer.close();

        System.out.println("write operation complete.");

    }


}
