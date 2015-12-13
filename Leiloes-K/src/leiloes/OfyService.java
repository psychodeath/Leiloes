package leiloes;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by joaomota on 13/09/15.
 *
 * Ref.
 * https://github.com/objectify/objectify/wiki/BasicOperations
 */
public class OfyService implements ServletContextListener{
    static {
        System.out.println("registering class from objectify service - leilao");
        ObjectifyService.register(Leilao.class);
        ObjectifyService.register(ParserState.class);
        //factory().register(Leilao.class);
    }

    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }

    public void contextInitialized (ServletContextEvent e){
        // não é necessário implementar
    }
    public void contextDestroyed (ServletContextEvent e){
        // não é necessário implementar
    }
}
