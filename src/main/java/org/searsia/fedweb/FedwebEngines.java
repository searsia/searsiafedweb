package org.searsia.fedweb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

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
    	
    	String line;
		BufferedReader br = new BufferedReader(new FileReader(data + "/meta_data/engines/FW14-engines.txt"));
		while ((line = br.readLine()) != null) {
			String fields[] = line.split("\t");
			String rid  = fields[0];
			String name = fields[1];
			String url  = fields[2];
			Float prior = 0.02f;
			if (fields.length > 5) {
				prior = Float.parseFloat(fields[5]);
			}
			if (rid.startsWith("FW14-")) {
				rid = rid.substring(5);
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
    	me.setFavicon("http://wwwhome.cs.utwente.nl/~hiemstra/images/feddude128.png"); 
    	me.setBanner("http://wwwhome.ewi.utwente.nl/~hiemstra/images/fedweb320.png"); 
    	me.setTestQuery("test"); 
    	engines.putMyself(me);

	}

}
