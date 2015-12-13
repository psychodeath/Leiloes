package leiloes;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Ignore;
import org.jsoup.nodes.Document;

import java.io.Serializable;
import java.util.LinkedList;
import com.googlecode.objectify.annotation.Id;

/**
 * Created by joaomota on 09/12/15.
 */
@Entity
public class ParserState implements Serializable{
    @Id
    public Long id = 666L;
    private int page;
    @Ignore
    public Document doc;
    @Ignore
    private LinkedList<Leilao> leiloes;
    private String url;

    public ParserState(){
        leiloes = new LinkedList<Leilao>();
        page = 0;
    }

    public LinkedList<Leilao> getLeiloes() {
        return leiloes;
    }

    public void setLeiloes(LinkedList<Leilao> leiloes) {
        this.leiloes = leiloes;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setDoc(Document d){
        this.doc = d;
    }

    public Document getDoc(){
        return this.doc;
    }
}
