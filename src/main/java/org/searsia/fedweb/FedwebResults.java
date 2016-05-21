package org.searsia.fedweb;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.searsia.Hit;
import org.searsia.SearchResult;

/**
 * 
 * Converts the XML results to JSON results for later use in 'mockup' engines
 * Copies lots of code of FedwebSamples (but we don't care, see above)
 * 
 * @author hiemstra
 *
 */
public class FedwebResults extends DefaultHandler  {

	private String resultDirName;
	private Map<String, String> currentAtt;
	private Hit currentHit;
	private SearchResult currentResult;
	private String currentQuery;
	private String currentText;
	private String currentRID;


    public FedwebResults(String xmlFile, String resultDirName) throws SAXException, ParserConfigurationException, IOException {
    	this.resultDirName = resultDirName;
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
        }
    	if (qName.equals("snippet")) {
    		currentHit = new Hit();
            String id = currentAtt.get("id");
            currentHit.put("trecid", id);
            String part[] = id.split("-");
            String rid = part[1];
    		if (rid.compareTo("e112") > 0 && rid.compareTo("e125") < 0) { // These are the image search engines
        		currentHit.put("tags", "#image");
    		}
    		currentRID = rid;
    	}
        if (qName.equals("thumb")) { 
            String cache = currentAtt.get("cache");
            if (cache != null) {
                cache = cache.replace("FW14-topics-docs", "http://circus.ewi.utwente.nl/FW14-topics-thumbnails");
                currentHit.put("image", cache);            	
            }
        }

    }
    
    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
    	String line = new String(ch, start, length);
    	line = line.replaceAll("\\<[^>]+\\>", " ");
    	line = line.replaceAll("\\s+", " ");
        currentText = currentText + line;
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
    	if (qName.equals("query")) {
    		currentQuery = currentText.replace("/", " ");
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
        if (qName.equals("snippet")) { 
        	if (currentHit.getTitle() == null) {
        		currentHit.setTitle(currentHit.getDescription());
        	}
        	if (currentHit.getTitle() == null) {
        		currentHit.setTitle("Fedweb14 result");
        	}
    		if (currentHit.getDescription() == null && currentHit.get("image") == null) {
    			currentHit.put("tags", "#small");
    		} else if (currentHit.getDescription() != null && currentHit.getDescription().length() + currentHit.getTitle().length() < 140) {
    			currentHit.put("tags", "#small");
    		}
            currentResult.addHit(currentHit);
        }
        if (qName.equals("search_results")) {
        	if (currentResult.getHits().size() > 0) {
                try {
        			File engineDir = new File(resultDirName, currentRID);
        		    if (!engineDir.exists()) {
        		    	engineDir.mkdir();
        		    }
        		    String json  = currentResult.toJson().toString();
        		    String query = currentQuery.toLowerCase().trim();
        		    String outFile = resultDirName + "/" + currentRID +  "/" + query;
        		    System.out.println("ADD: " + outFile);
        		    Files.write(Paths.get(outFile), json.getBytes(), StandardOpenOption.CREATE);
                    //System.out.println(currentResult.toJson());;
                }
                catch (Exception e) {
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
    	  	   	
    	/* Special directory, to simulate search engine */
    	String resultDirName = path + "/" + file + "_results";
    	File resultDir = new File(resultDirName);
	    if (!resultDir.exists()) {
	    	resultDir.mkdir();
	    }

    	for (int t = 7011; t <= 7501; t += 1) {
            for (int r = 1; r <= 200; r += 1) {
                String tid = Integer.toString(t);
                String rid = "00" + Integer.toString(r); 
                rid = rid.substring(rid.length() -3, rid.length());
                try {
                    String xmlFile = data + "search_data/fedweb14/FW14-topics-search/e" + rid + "/" + tid + ".xml";
                    new FedwebResults(xmlFile, resultDirName);
                } catch (IOException e) {
                	System.out.println("Warning: No e" + rid + "/" + tid + ".xml");
                }
            }

    	}
	}


	
}
