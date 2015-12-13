<%@ page import="leiloes.ParserState" %>
<%@ page import="org.jsoup.nodes.Document" %>
<%@ page import="leiloes.Leilao" %>
<%@ page import="java.util.LinkedList" %>
<%@ page import="org.jsoup.Jsoup" %>
<%@ page import="java.io.IOException" %>
<%@ page import="leiloes.Scraper" %>
<%--
  Created by IntelliJ IDEA.
  User: joaomota
  Date: 01/11/15
  Time: 16:28
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
    <title>Scraper Vendas Finanças</title>
  </head>
  <body>
    <h1>Scraper Vendas Finanças (JSP)</h1>
    <h2>Rev.02</h2>
    <div>
      <%
        ParserState pState = new ParserState();
        Document doc;

        LinkedList<Leilao> leiloes = new LinkedList<>();
        pState.setUrl("http://www.e-financas.gov.pt/vendas/consultaVendasCurso.action?tipoConsulta=02&modalidade=&distrito=&concelho=&minimo=++.+++.+++.+++%2C++&maximo=++.+++.+++.+++%2C++&dataMin=&dataMax=");
        do {
          %>*<%

          try {
            doc = Jsoup.connect(pState.getUrl()).get();
            pState.setDoc(doc);
          } catch (IOException e) {
            System.out.println("Erro a abrir site das finanças");
            e.printStackTrace();
            break;
          }

        } while (!Scraper.parse(pState));

        Scraper.WriteLeilaoToGcs(leiloes);

      %>
      Parsed and stored...?
    </div>
  </body>
</html>
