package leiloes;

import com.google.appengine.tools.cloudstorage.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.Channels;

public class GcsLeiloesWriter {

	public static final String BUCKETNAME = "scrapingbucket";
    public static final String FILENAME = "resultsVendas.csv";
    
    private PrintWriter writer;
    private GcsOutputChannel outputChannel;
    
	public GcsLeiloesWriter(){
		outputChannel = null;
		GcsFilename fileName = new GcsFilename(BUCKETNAME,FILENAME);

        final GcsService gcsService = GcsServiceFactory.createGcsService(
                new RetryParams.Builder()
                    .initialRetryDelayMillis(10)
                    .retryMaxAttempts(10)
                    .totalRetryPeriodMillis(15000)
                    .build()
        );
        
        //GcsFileOptions.Builder gcsOptionsBuilder = new GcsFileOptions.Builder();
        
        //gcsOptionsBuilder.contentEncoding("ISO-8859-1");

        try {
            // TODO Fix character encoding on generated file
            //GcsFileOptions gcsFileOptionsConfig = gcsOptionsBuilder.build();
            outputChannel = gcsService.createOrReplace(fileName, GcsFileOptions.getDefaultInstance());

        } catch (IOException e) {
            System.out.println("something went wrong opening an output channel to the GCS service");
            e.printStackTrace();
        }

        writer = new PrintWriter(Channels.newWriter(outputChannel, "ISO-8859-1"));
        
        writer.println("Tipologia;URL;Número;Preço base;Data limite;Serviço de Finanças;Estado;Modalidade");
	}
	
	public void write(String txt){
		this.writer.print(txt);
	}
	
	public void writeln(){
		this.writer.println();
	}
	
	public void flush(){
		this.writer.flush();
	}
	
	public void close(){
		try {
			this.writer.close();
		} catch (RetriesExhaustedException e) {
			System.out.println("Retry attempts exhausted");
			e.printStackTrace();
		}
	}
	
	public void waitForWrites() throws IOException{
		try{
			outputChannel.waitForOutstandingWrites();
		}
		catch(IOException e){
			throw e;
		}
	}
}
