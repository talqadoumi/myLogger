package jo.aspire.automation.logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;

public class AspireApiLog4j 
{

    /**
     * method used to insert run_id into RUN_LOG table by API Call
     *
     * @return
     *
     * Inserted run id
     *
     *
     */
    public static String InsertIntoRunLogAPI() {
        EnvirommentManager propsUtil = EnvirommentManager.getInstance();

        String apiUrl = propsUtil.getProperty("db.runlog.api.url");

        HttpClient httpClient = HttpClientBuilder.create().build();
        StringBuffer result = new StringBuffer();
        try {

            HttpPost request = new HttpPost(apiUrl);

            request.addHeader("content-type", "application/json");
            HttpResponse response = httpClient.execute(request);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            //System.out.println("Result IS ----- " + result.toString());

        } catch (Exception ex) {

            AspireLog4j.setLoggerMessageLevel("Exception ", Log4jLevels.ERROR, ex);

        } finally {
            //Deprecated
            httpClient.getConnectionManager().shutdown();

        }
        return result.toString();
    }

    /**
     * Method used to insert log with information into logs table (API Call)
     *
     * @param logger logger class
     * @param level log4j level info , warn , error ....
     * @param message log4j message
     * @param exception
     *
     * exception object
     * @param runId
     *
     * run_id
     */
    public static void InsertIntoLogAPI(String logger, String level, String message, String exception, String runId) {
        EnvirommentManager propsUtil = EnvirommentManager.getInstance();

        String apiUrl = propsUtil.getProperty("db.log.api.url");

        JSONObject json = new JSONObject();
        json.put("LOGGER", logger);
        json.put("LEVEL", level);
        json.put("MESSAGE", message);
        json.put("STACKTRACE", exception);
        json.put("RUN_ID", runId);
        HttpClient httpClient = HttpClientBuilder.create().build(); //Use this instead 
        StringBuffer result = new StringBuffer();
        try {

            HttpPost request = new HttpPost(apiUrl);
            StringEntity params = new StringEntity(json.toString());
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);

            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

        } catch (Exception ex) {

            AspireLog4j.setLoggerMessageLevel("Exception ", Log4jLevels.ERROR, ex);

        } finally {

            httpClient.getConnectionManager().shutdown();

        }

    }

}
