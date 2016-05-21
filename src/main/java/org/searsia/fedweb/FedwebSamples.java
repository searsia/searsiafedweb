package org.searsia.fedweb;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.searsia.Hit;
import org.searsia.SearchResult;
import org.searsia.index.SearchResultIndex;

/**
 * Inserts the Fedweb Samples in a local index 'fedweb14'
 *
 * @author hiemstra
 *
 */
public class FedwebSamples extends DefaultHandler {

	
	private final static Pattern noXMLPat = Pattern.compile("(?s)\\<[^>]+>\\>"); // TODO: This does not work, why? whyyyyy??
	// Maybe: http://stackoverflow.com/questions/16008974/strange-java-unicode-regular-expression-stringindexoutofboundsexception
	
	private SearchResultIndex index;
	
	private Map<String, String> currentAtt;
	private Hit currentHit;
	private SearchResult currentResult;
	private String currentText;
	private String currentQuery;
	private int currentRank;


    public FedwebSamples(SearchResultIndex index, String xmlFile)
    		throws SAXException, ParserConfigurationException, IOException {
    	this.index = index; 
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();  
        SAXParser saxParser = saxParserFactory.newSAXParser();  
        saxParser.parse(xmlFile, this);
    }


    private Map<String, String> mapAttributes(Attributes attributes) {
        Map<String, String> resultMap = new HashMap<String, String>();
        int length = attributes.getLength();
        for (int i=0; i<length; i++) {
            resultMap.put(attributes.getQName(i), attributes.getValue(i));   
        }
        return resultMap;
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {  
    	currentText = "";
        Map<String, String> att = mapAttributes(attributes);
        currentAtt = att;
        if (qName.equals("search_results")) {
    		currentResult = new SearchResult();
    		currentRank = 0;
        }
    	if (qName.equals("snippet")) {
    		currentHit = new Hit();
            String id = currentAtt.get("id");
            currentHit.put("trecid", id);
            String part[] = id.split("-");
            String rid = part[1];
            currentHit.put("rid", rid);
            currentRank += 1;
            currentHit.put("rank", currentRank);
    		currentHit.put("query", currentQuery);
    		if (rid.compareTo("e112") > 0 && rid.compareTo("e125") < 0) { // These are the image search engines
        		currentHit.put("tags", "#image");
    		}
    	}
    	/*
        if (qName.equals("thumb")) { 
            String cache = currentAtt.get("cache");
            cache = cache.replace("FW14-sample-docs", "http://circus.ewi.utwente.nl/FW14-topics-thumbnails");
            currentHit.put("image", cache);
        }
        */

    }
    
    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
    	String line = new String(ch, start, length);
    	line = noXMLPat.matcher(line).replaceAll(" ");
    	line = line.replaceAll("\\s+", " ");
        currentText = currentText + line;
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
    	if (qName.equals("query")) {
    		currentQuery = currentText;
    	}    	
        if (qName.equals("title")) { 
            currentHit.setTitle(currentText);
        }
        if (qName.equals("description")) { 
            currentHit.setDescription(currentText); 
        }
        if (qName.equals("link")) { 
            currentHit.setUrl(currentText);
        }
        if (qName.equals("thumb")) { 
            currentHit.put("image", currentText);
        }
        if (qName.equals("snippet")) { 
        	if (currentHit.getTitle() == null) {
        		currentHit.setTitle(currentHit.getDescription());
        		currentHit.setDescription(null);
        	}
        	if (currentHit.getTitle() != null) {
        		if (currentHit.get("image") == null) {
        		     if (currentHit.getDescription() == null || currentHit.getDescription().length() + currentHit.getTitle().length() < 140) {
        			    currentHit.put("tags", "#small");
        		     }
        		}
                currentResult.addHit(currentHit);        		
        	}
        }
        if (qName.equals("search_results")) {
        	if (currentResult.getHits().size() > 0) {
                index.offer(currentResult);
                try {
                    index.check();
                }
                catch (IOException e) {
                	throw new RuntimeException(e);
                }
        	}
        }

    }


    public static void main(String[] args) throws SAXException, ParserConfigurationException, IOException {

        String data = "fedwebgh";
    	String path = "index";
    	String file = "fedweb14";
    	
    	if (args.length > 0)
    		data = args[0];
    	if (args.length > 1)
    		path = args[1];
    	if (args.length > 2)
    		file = args[2];
    	
    	if (!data.endsWith("/")) {
    		data +=  "/";
    	}
    	
    	file = "local_" + file;
    	
    	SearchResultIndex index = new SearchResultIndex(path, file, 10000);

        for (int i = 1; i <= 200; i += 1) {
            String rid = "00" + Integer.toString(i); 
            rid = rid.substring(rid.length() -3, rid.length());
            System.out.println("Insert " + rid);
            try {
                String xmlFile = data + "/search_data/fedweb14/FW14-sample-search/e" + rid + "/e" + rid + ".xml";
                new FedwebSamples(index, xmlFile);
            } catch (IOException e) {
            	System.out.println("Warning: No e" + rid + ".xml");
            }
        }
    	index.flush();
    	index.close();
	}


}
