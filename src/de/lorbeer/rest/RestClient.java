package de.lorbeer.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

/*
 * @author Luke Lowrey
 * http://lukencode.com/2010/04/27/calling-web-services-in-android
 * -using-httpclient/ modified version by a.augsburg
 */
public class RestClient
{
private ArrayList<NameValuePair> params;
private ArrayList<NameValuePair> headers;

private String                   url;
private Boolean                  s_entity = false;
private StringEntity             entity;

private int                      responseCode;
private String                   message;

private String                   response;
public Boolean                   timeout;

public String getResponse()
  {
   return response;
  }

public String getErrorMessage()
  {
   return message;
  }

public int getResponseCode()
  {
   return responseCode;
  }

/*
 * only for sending requests with xml-entity! setting via setEntity()
 * @param url
 */
public RestClient(String url)
  {
   this.url = url;
   timeout = false;
   params = new ArrayList<NameValuePair>();
   headers = new ArrayList<NameValuePair>();
  }

public void AddParam(String name,String value)
  {
   params.add(new BasicNameValuePair(name,value));
  }

public void AddHeader(String name,String value)
  {
   headers.add(new BasicNameValuePair(name,value));
  }

public void Execute(RequestMethod method) throws Exception
  {
   switch (method)
     {
      case GET:
        {
         // add parameters
         String combinedParams = "";
         if (!params.isEmpty())
           {
            combinedParams += "?";
            for (NameValuePair p:params)
              {
               String paramString = p.getName() + "=" + URLEncoder.encode(p.getValue(),"UTF-8");
               if (combinedParams.length() > 1)
                 {
                  combinedParams += "&" + paramString;
                 }
               else
                 {
                  combinedParams += paramString;
                 }
              }
           }

         HttpGet request = new HttpGet(url + combinedParams);

         // add headers
         for (NameValuePair h:headers)
           {
            request.addHeader(h.getName(),h.getValue());
           }

         executeRequest(request,url);
         break;
        }
      case POST:
        {
         HttpPost request = new HttpPost(url);

         if (s_entity)
           {
            request.setEntity(entity);
           }
         // add headers
         for (NameValuePair h:headers)
           {
            request.addHeader(h.getName(),h.getValue());
           }

         if (!params.isEmpty())
           {
            request.setEntity(new UrlEncodedFormEntity(params,HTTP.UTF_8));
           }

         executeRequest(request,url);
         break;
        }
     }
  }

private void executeRequest(HttpUriRequest request,String url) throws SocketTimeoutException
  {
   HttpClient client = new DefaultHttpClient();
   // set Timeout 2500ms
   client.getParams().setParameter("http.socket.timeout",new Integer(2500));

   HttpResponse httpResponse;

   try
     {
      httpResponse = client.execute(request);
      responseCode = httpResponse.getStatusLine().getStatusCode();
      message = httpResponse.getStatusLine().getReasonPhrase();
      System.out.println("debug: " + responseCode + " / " + message);

      HttpEntity entity = httpResponse.getEntity();

      if (entity != null)
        {

         InputStream instream = entity.getContent();
         response = convertStreamToString(instream);

         // Closing the input stream will trigger connection release
         instream.close();
        }

     }
   catch (ClientProtocolException e)
     {
      client.getConnectionManager().shutdown();
      e.printStackTrace();
     }
   catch (IOException e)
     {
      client.getConnectionManager().shutdown();
      e.printStackTrace();
      // might be a timeout
      timeout = true;
      System.out.println("timeout?");
     }
  }

private static String convertStreamToString(InputStream is)
  {

   BufferedReader reader = new BufferedReader(new InputStreamReader(is));
   StringBuilder sb = new StringBuilder();

   String line = null;
   try
     {
      while ((line = reader.readLine()) != null)
        {
         sb.append(line + "\n");
        }
     }
   catch (IOException e)
     {
      e.printStackTrace();
     }
   finally
     {
      try
        {
         is.close();
        }
      catch (IOException e)
        {
         e.printStackTrace();
        }
     }
   System.out.println(sb.toString());
   return sb.toString();
  }

public void setEntity(String entity) throws UnsupportedEncodingException
  {
   s_entity = true;
   this.entity = new StringEntity(entity,"UTF-8");
   this.entity.setContentType("application/xml");
  }
}