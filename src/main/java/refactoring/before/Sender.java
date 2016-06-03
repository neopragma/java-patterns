package refactoring.before;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Properties;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;

public class Sender {
	
	private String emailHost = "localhost";
//    private String emailHost = "email1host";
//    private String emailHost = "email2host";
	
    /**
     * Send a message.
     * @param message
     * @return response
     * @throws UnknownHostException
     */
	public String send(Message message) throws UnknownHostException {
		String method = getMethod(message);
		String responseText = "";
		if (method.equals("soap")) {
		    try {
	            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
	            SOAPConnection soapConnection = soapConnectionFactory.createConnection();
	            MessageFactory messageFactory = MessageFactory.newInstance();
	            SOAPMessage soapMessage = messageFactory.createMessage();
	            SOAPPart soapPart = soapMessage.getSOAPPart();
	            String serverURI = message.getDestination();
	            SOAPEnvelope envelope = soapPart.getEnvelope();
	            envelope.addNamespaceDeclaration("tddcourse", serverURI);
	            /*
	            Constructed SOAP Request Message:
	            <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:example="http://ws.cdyne.com/">
	                <SOAP-ENV:Header/>
	                <SOAP-ENV:Body>
	                    <example:VerifyEmail>
	                        <example:email>mutantninja@gmail.com</example:email>
	                        <example:LicenseKey>123</example:LicenseKey>
	                    </example:VerifyEmail>
	                </SOAP-ENV:Body>
	            </SOAP-ENV:Envelope>
	             */
	            SOAPBody soapBody = envelope.getBody();
	            SOAPElement soapBodyElem = soapBody.addChildElement("Payload", "tddcourse");
	            SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("content", "tddcourse");
	            soapBodyElem1.addTextNode((String) message.getPayload());
	            MimeHeaders headers = soapMessage.getMimeHeaders();
	            headers.addHeader("SOAPAction", serverURI  + "Payload");
	            soapMessage.saveChanges();
// debug             
//	            System.out.print("Request SOAP Message = ");
//	            soapMessage.writeTo(System.out);
//	            System.out.println();
// end debug             
	            String url = "http://happy.soap.endpoint";
	            SOAPMessage soapResponse = soapConnection.call(soapMessage, url);
	            TransformerFactory transformerFactory = TransformerFactory.newInstance();
	            Transformer transformer = transformerFactory.newTransformer();
//	            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
//	            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	            Source sourceContent = soapResponse.getSOAPPart().getContent();
	            OutputStream out = new ByteArrayOutputStream();
	            StreamResult streamResult = new StreamResult();
	            streamResult.setOutputStream(out);
	            transformer.transform(sourceContent, streamResult);
	            responseText = streamResult.getOutputStream().toString();
// debug
//	            System.out.print("\nResponse SOAP Message = " + responseText);
// end debug   
	            soapConnection.close();
	        } catch (Exception e) {
	            System.err.println("What??? Impossible!!!");
	            e.printStackTrace();
	        }		
		} else {
			if (method.equals("rest")) {
				JsonObject jsonMessage = Json.createObjectBuilder()
						   .add("payload", (String) message.getPayload())
						   .build();
				try {
					URL url = new URL(message.getDestination());
					URLConnection connection = url.openConnection();
					connection.setDoOutput(true);
					connection.setRequestProperty("Content-Type", "application/json");
					connection.setConnectTimeout(5000);
					connection.setReadTimeout(5000);
					OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
// debug             
//		            System.out.println("Request JSON Message = " + jsonMessage.toString());
// end debug             				
					out.write(jsonMessage.toString());
					out.close();
                    JsonReader reader = Json.createReader(connection.getInputStream());
					JsonObject jsonResponse = reader.readObject();
					reader.close();
// debug
//		            System.out.print("\nResponse SOAP Message = " + responseText);
// end debug   
					responseText = jsonResponse.getString("payload");		
				} catch (Exception e) {
					// TODO: handle exception
				}				
				
			} else {
				if (method.equals("jms")) {
					Properties env = new Properties();
					env.put(Context.INITIAL_CONTEXT_FACTORY,
							"org.apache.activemq.jndi.ActiveMQInitialContextFactory");
					env.put(Context.PROVIDER_URL, "tcp://localhost:61616");
					env.put("queue.queueSampleQueue", "TDDCourseQueue");					
					try {
						InitialContext ctx = new InitialContext(env);
						Queue queue = (Queue) ctx.lookup("TDDCourseQueue");
						QueueConnectionFactory connFactory = (QueueConnectionFactory) ctx.lookup("QueueConnectionFactory");
						QueueConnection queueConn = connFactory.createQueueConnection();
						QueueSession queueSession = queueConn.createQueueSession(false,Session.DUPS_OK_ACKNOWLEDGE);
						QueueSender queueSender = queueSession.createSender(queue);
						queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
						TextMessage jmsMessage = queueSession.createTextMessage((String) message.getPayload());
						queueSender.send(jmsMessage);
// debug
// System.out.println("sent: " + jmsMessage.getText());
						QueueReceiver queueReceiver = queueSession.createReceiver(queue);
						queueConn.start();
						jmsMessage = (TextMessage) queueReceiver.receive();
                        responseText = jmsMessage.getText(); 
// debug
// System.out.println("received: " + message.getText());					
						queueConn.close();
					} catch (NamingException e) {
						e.printStackTrace();
					} catch (JMSException e) {
						e.printStackTrace();
					}				
				} else {
					if (method.equals("email")) 
					{
						Properties properties = System.getProperties();  
					      properties.setProperty("mail.smtp.host", emailHost);  
					      javax.mail.Session session = javax.mail.Session.getDefaultInstance(properties);  
					      try
					      {  
					         MimeMessage emailMessage = new MimeMessage(session);  
					         emailMessage.setFrom(new InternetAddress("us@ourdomain.com"));  
					         emailMessage.addRecipient(javax.mail.Message.RecipientType.TO,new InternetAddress(message.getDestination()));  
					         emailMessage.setSubject("A message from us to you");  
					         emailMessage.setText((String) message.getPayload());  
					         javax.mail.Transport.send(emailMessage);  
					         System.out.println("message sent successfully....");  					  
					      } 
					      catch (MessagingException mex)
					      {
					    	  System.err.println("oops!");
					      }  
					}    
				}
			}
		}
		return responseText;
	}
	
	private static String getMethod(Message message) throws UnknownHostException {
		MongoClient mongoClient = new MongoClient();
		DB db = mongoClient.getDB( "development" );
		BasicDBObject dbObject = new BasicDBObject();
		BasicDBObject fields = new BasicDBObject("method", 1).append("_id", 0);
		DBCollection coll = db.getCollection("activity");	
		DBObject doc = coll.findOne(dbObject, fields);
		String sendMethod = (String) doc.get("method");
//		System.out.println(doc);		
		return sendMethod;
		
	}

}
