/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.miscellaneousPrograms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 /**
 * 
 * @author Lachlan Aldred
 * Date: 10/03/2004
 * Time: 13:56:23
 * 
 */
public class TestSMS {
/*
POST /services/soap/SMSService.asmx HTTP/1.1
Host: mobile.act.cmis.csiro.org
Content-Type: text/xml; charset=utf-8
Content-Length: length
SOAPAction: "http://niki-bt.act.cmis.csiro.org/SMSService/SendText"

<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <SendText xmlns="http://niki-bt.act.cmis.csiro.org/SMSService/">
      <PhoneNumber>string</PhoneNumber>
      <Message>string</Message>
      <MessageClass>int</MessageClass>
      <ValidityPeriod>int</ValidityPeriod>
    </SendText>
  </soap:Body>
</soap:Envelope>
*/


    public static void main(String[] args) {
        String uri = "http://ws.cdyne.com/ip2geo/ip2geo.asmx/ResolveIP?" +
                "IPaddress=131.181.105.71" +
                "&LicenseKey=0";
        System.out.println(executeGet(uri));
/*
        String uri = "http://mobile.act.cmis.csiro.org/services/soap/SMSService.asmx";
        String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<soap:Envelope " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
                "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<soap:Body>" +
                "<SendText xmlns=\"http://niki-bt.act.cmis.csiro.org/SMSService/\">" +
                "<PhoneNumber>0438330056</PhoneNumber>" +
                "<Message>Hello myself</Message>" +
                "<MessageClass>1</MessageClass>" +
                "<ValidityPeriod>1</ValidityPeriod>" +
                "</SendText>" +
                "</soap:Body>" +
                "</soap:Envelope>";
        System.out.println(executePost(uri, content));
*/
//      System.out.println(executeGet("http://www.google.com"));
    }

    public static String executePost(String urlStr, String content) {
        StringBuilder result = new StringBuilder();
        try {
            Authentication.doIt();
            URL url = new URL(urlStr);
System.out.println("Host: " + url.getHost());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
System.out.println("got connection ");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            connection.setRequestProperty("Content-Length", "" + content.length());
            connection.setRequestProperty("SOAPAction", "\"http://niki-bt.act.cmis.csiro.org/SMSService/SendText\"");
            connection.setRequestMethod("POST");
            //send query
            PrintWriter out = new PrintWriter(connection.getOutputStream());

            out.print(content);

            out.flush();
            //retrieve reply
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                result.append(inputLine);
            }
            //clean up
            in.close();
            out.close();
            connection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String msg = result.toString();
        if (msg != null) {
            int beginCut = msg.indexOf('>');
            int endCut = msg.lastIndexOf('<');
            if(beginCut != -1 && endCut != -1){
                return msg.substring(beginCut + 1, endCut);
            }
        }
        return null;
    }


    private static String executeGet(String urlStr) {
        StringBuilder result = new StringBuilder();
        try {
            Authentication.doIt();
            URL url = new URL(urlStr);
System.out.println("Host: " + url.getHost());
//System.out.println("InterfaceB_EnvironmentBasedClient::executeGet() url.getQuery() = " + url.getQuery());
//InstanceValidator code
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(url.openStream()));
//end InstanceValidator code
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
//            connection.setRequestMethod("GET");
            //retrieve reply
//            BufferedReader in = new BufferedReader(
//                    new InputStreamReader(connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                result.append(inputLine);
            }
            //clean up
            in.close();
            connection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}
