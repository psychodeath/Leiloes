package leiloes;



import com.google.appengine.tools.cloudstorage.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.channels.Channels;

/**
 * Created by joaomota on 19/08/15.
 */
public class DownloadResults extends HttpServlet {
    public static final String BUCKETNAME = "scrapingbucket";
    public static final String FILENAME = "resultsVendas.csv";
    private static final int BUFFER_SIZE = 2 * 1024 * 1024;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final GcsService gcsService = GcsServiceFactory.createGcsService(
                new RetryParams.Builder()
                        .initialRetryDelayMillis(10)
                        .retryMaxAttempts(10)
                        .totalRetryPeriodMillis(15000)
                        .build()
        );

        GcsFilename fileName = new GcsFilename(BUCKETNAME,FILENAME);

        int fileSize;
        try {
            fileSize = (int) gcsService.getMetadata(fileName).getLength();
            System.out.println("Size of file to read: " + fileSize);

            try (GcsInputChannel readChannel = gcsService.openReadChannel(fileName, 0)) {
                response.setContentType("application/force-download");
                response.setContentLength(fileSize);
                response.setHeader("Content-Transfer-Encoding", "binary");
                response.setHeader("Content-Disposition","attachment; filename=\"" + FILENAME + "\"");
                copy(Channels.newInputStream(readChannel), response.getOutputStream());
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            System.out.println("no file to read");
            PrintWriter writer = response.getWriter();
            writer.write("no file found");
            writer.flush();
            writer.close();
        }



    }

    private void copy(InputStream input, OutputStream output) throws IOException {
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = input.read(buffer);
            while (bytesRead != -1) {
                output.write(buffer, 0, bytesRead);
                bytesRead = input.read(buffer);
            }
        } finally {
            input.close();
            output.close();
        }
    }

}
