package org.searsia.fedweb;

import org.searsia.index.SearchResultIndex;

public class DumpIndex {

    public static void main(String[] args) throws Exception {
        
        SearchResultIndex index = null;
 
        String path = "index";
        String file = "fedweb14";
        
        if (args.length > 0)
            path = args[0];
        if (args.length > 1)
            file = args[1];
        	
        file = "local_" + file;
        
        index = new SearchResultIndex(path, file, 10);
        index.dump();
        index.close();
        
    }
	
}
  
