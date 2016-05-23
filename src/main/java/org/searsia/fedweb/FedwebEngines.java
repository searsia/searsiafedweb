package org.searsia.fedweb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Math;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import org.searsia.index.ResourceIndex;
import org.searsia.engine.Resource;

/**
 * Inserts the information about the Fedweb engines in the index 'fedweb14'
 * Also writes the information to files, for later use in 'mockup' engines that
 * always give empty results (except for the official topic queries).
 * 
 * @author hiemstra
 *
 */
public class FedwebEngines {

	private static final String FW_URL = "http://circus.ewi.utwente.nl/fedweb14/?q={q}&r=";
	private static Map<String, Integer> resourceRel = new HashMap<String, Integer>();
    private static Map<String, Integer> resourceNonRel = new HashMap<String, Integer>();
    private static Integer totalRel = 0;
    private static Integer totalNonRel = 0;
	
    private static void incMap(Map<String, Integer> map, String key, Integer inc) {
        Integer value = map.get(key);
        if (value != null) {
            map.put(key, value + inc);
        } else {
            map.put(key, 1);
        }
    }
    
    private static double computePrior(String rid) {
        Integer nrRel = resourceRel.get(rid);
        if (nrRel == null) nrRel = 1;
        Integer nrNonRel = resourceNonRel.get(rid);
        if (nrNonRel == null) nrNonRel = 1;
        double probRel = ((double) nrRel) / (totalRel + 1);
        double probNonRel = ((double) nrNonRel) / (totalNonRel + 1);
        return Math.log(probRel) - Math.log(probNonRel);
    }
    
    /**
     * Use the data from 2013, to estimate the logit transform of the probability of relevance
     * given the resource.
     * @param data
     */
	private static void collect2013relevanceInfo(String data) {
	    String qrels13 = data + "meta_data/TREC_evaluation/qrels_files/FW13-QRELS-RM.txt";
        String line;
	    BufferedReader br = null;
	    try {
	        br = new BufferedReader(new FileReader(qrels13));
            while ((line = br.readLine()) != null) {
                String fields[] = line.split(" ");
                String document = fields[2];
                Integer relevant = new Integer(fields[3]);
                String moreFields[] = document.split("-");
                String resource = moreFields[1];
                if (relevant > 0) {
                    totalRel += relevant;
                    incMap(resourceRel, resource, relevant);                        
                } else {
                    totalNonRel += 1;
                    incMap(resourceNonRel, resource, relevant);
                }
            }
	    } catch (Exception e) {
	        System.err.println("Warning: Unable to estimate priors from 2013 data:" + e.getMessage());
	    } finally {
	        try {
	            if (br != null) br.close();
	        } catch(IOException e) { }
	    }
	}
	
    public static void main(String[] args) throws IOException {

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
    
    	ResourceIndex engines = new ResourceIndex(path, file);
       	
    	/* Special directory, to simulate search engine */
    	String resultDirName = file + "_results";
    	File resultDir = new File(path, resultDirName);
	    if (!resultDir.exists()) {
	    	resultDir.mkdir();
	    }
	    
	    collect2013relevanceInfo(data);
    	
    	String line;
		BufferedReader br = new BufferedReader(new FileReader(data + "meta_data/engines/FW14-engines.txt"));
		while ((line = br.readLine()) != null) {
			String fields[] = line.split("\t");
			String rid  = fields[0];
			String name = fields[1];
			String url  = fields[2];
			Float prior = 1.0f;
			if (rid.startsWith("FW14-")) {
				rid = rid.substring(5);
	            if (fields.length > 5) {
	                prior = Float.parseFloat(fields[5]);
	            } else {
	                prior = new Float(computePrior(rid));
	            }
				Resource engine = new Resource(FW_URL + rid, rid);
	    		if (url.startsWith("http"))
    	    		engine.setUrlUserTemplate(url);
			    engine.setName(name);
    			engine.setMimeType("application/searsia+json");
    			engine.setPrior(prior);
	    		engines.put(engine);    		
		    	
			    String engineString = engine.toJson().toString();			
    			File engineDir = new File(path, resultDirName + "/" + rid);
	    	    if (!engineDir.exists()) {
		    	    engineDir.mkdir();
    		    }
	    	    engineString = "\"resource\":" + engineString;
		    	System.out.println("Add:  " + engineString);
		        Files.write(Paths.get(path, resultDirName + "/" + rid +  "/resource.json"), engineString.getBytes(), StandardOpenOption.CREATE);
			}				
		}
		br.close();

	    Resource me = new Resource("http://localhost:16842/searsia/search?q={q?}&r={r?}"); 
    	me.setName("FedWeb 14 Search");  
    	me.setFavicon("http://wwwhome.ewi.utwente.nl/~hiemstra/images/feddude128.png"); 
    	me.setBanner("http://wwwhome.ewi.utwente.nl/~hiemstra/images/fedweb320.png"); 
    	me.setTestQuery("test"); 
    	engines.putMyself(me);

	}

}
