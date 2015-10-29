
package com.jm.Leiloes;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.LinkedList;

import java.util.logging.Level;
import java.util.logging.Logger;

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

	private String WriteLeilaoToGcs(LinkedList<Leilao> leiloes){
		GcsLeiloesWriter glw = new GcsLeiloesWriter();
		
		for(Leilao l : leiloes){
			System.out.println("datastore: leilão " + l.getId() + " com tipologia " + l.getTipologia());
			
			glw.write(l.getTipologia() + ";");
			glw.write(l.getUrl() + ";");
			glw.writeln();
            glw.flush();
            glw.close();
            
            OfyService.ofy().save().entity(l).now();
            System.out.println("saving to datastore complete");
		}
		
        try {            
            glw.waitForWrites();
            System.out.println("finished writing file to GCS");

            return "Tamanho da lista de leilões: " + leiloes.size();
        } catch (IOException e) {
            System.out.println("something went wrong finalizing writes to output channel");
            e.printStackTrace();
        }

        return "commit to gcs exited abnormally";
	}
	
    private String parse(){
        LinkedList<Leilao> leiloes = new LinkedList<>();

        try {
            Document doc;
            //StringWriter res = new StringWriter();


            // PrintWriter writer = new PrintWriter("file", "ISO-8859-1");


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
                            //glw.write(infoText.text() + ";");
                            resto.add(infoText.text());
                        }
                        // TODO acrescentar resto à bd, ajustar parsing morada (tamanho variável)

                    }

                    if (found){
                        try {
                            //OfyService.ofy().save().entity(leilao).now();
                            leiloes.add(leilao);
                        } catch (Exception e1) {
                            System.out.println("persistence to datastore failed");
                            e1.printStackTrace();
                        }

                    }
                }

                Element el = doc.getElementsByAttributeValueStarting("href","consultaVendasCurso").last();

                urlToOpen = el.attr("abs:href");

            } while((doc.hasText()) && (!beyondEnd));
        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
            Logger.getLogger(Scraper.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        this.WriteLeilaoToGcs(leiloes);
        
        return "parsing function failed";
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{

        // trigger events to update status on jsp (not working?)
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");

        PrintWriter writer = response.getWriter();

        writer.write(this.parse() + "\n");

        writer.flush();
        writer.close();

        System.out.println("write operation complete.");

    }


}
