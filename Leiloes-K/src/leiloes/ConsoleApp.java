package leiloes;

/**
 * Created by joaomota on 12/12/15.
 */
public class ConsoleApp {
    public static void main(String[] args) {
        System.out.println("Running scraper from console...");

        Scraper.runScraper();

        System.out.println("Scraping finished.");
    }
}
