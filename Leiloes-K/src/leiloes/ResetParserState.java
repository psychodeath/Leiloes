package leiloes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by joaomota on 13/12/15.
 */
public class ResetParserState extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        ParserState pState = new ParserState();
        OfyService.ofy().save().entity(pState);

        PrintWriter writer = resp.getWriter();
        writer.write("no file found");
        writer.flush();
        writer.close();

        System.out.println("state reset");
    }
}
