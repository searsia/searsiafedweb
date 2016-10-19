Searsia TREC FedWeb14 Experiments
=================================

Step 1, Download from FedWeb Greatest Hits (https://fedwebgh.intec.ugent.be):

    wget -r -nH -nv --no-parent --reject "index.html*" --user="" --password="" \
    https://fedwebgh.intec.ugent.be/fedwebgh/meta_data/
    wget -r -nH -nv --no-parent --reject "index.html*" --user="" --password="" \
    https://fedwebgh.intec.ugent.be/fedwebgh/search_data/fedweb14/FW14-sample-search/

Step 2, Unpack the data:

    cd fedwebgh/search_data/fedweb14
    find . -name "*.tgz" -exec tar xvfz \{\} \;

Step 3, Compile Searsia Fedweb:

    mvn package

Step 4, Install `trec_eval` (http://trec.nist.gov/trec_eval/)

    wget http://trec.nist.gov/trec_eval/trec_eval_latest.tar.gz
    tar xvfz trec_eval_latest.tar.gz
    cd trec_eval.9.0/
    make

Step 5, Add the Fedweb14 resources (optionally add the following parameters):

    java -cp target/searsiafedweb.jar org.searsia.fedweb.FedwebEngines

* 1st: the directory of the downloaded data (default: `fedwebgh`);
* 2nd: the directory where you want the index (default: `index`);
* 3rd: the name of the index (default `fedbweb14`).

Step 6, Create the FedWeb14 sample index (same optional parameters):

    java -cp target/searsiafedweb.jar org.searsia.fedweb.FedwebSamples

* You see "Index 0XX" for each engine that has samples;
* Some engines do not exist, causing normal warnings ("No e006.xml");

Step 7, Start the Searsia server (also works with `searsiaserver.jar` v0.4.0):

    java -jar target/searsiafedweb.jar -mother=none -name=fedweb14 --path=index

Step 8, Run the experiment: (Required: https://pypi.python.org/pypi/requests)

    python src/main/python/trec-run.py \
    fedwebgh/meta_data/topics/FW-topics.xml >fw.run

Step 9, Evaluate the results: (Required: http://www.numpy.org)

    python fedwebgh/meta_data/TREC_evaluation/eval_scripts/FW-eval-RS.py \
    fw.run fedwebgh/meta_data/TREC_evaluation/qrels_files/FW14-QRELS-RS.txt \
    trec_eval.9.0/

Expected result:

    topic,nDCG@10,nDCG@20,nDCG@100,nP@1,nP@5
    (...)
    all,0.6097,0.6637,0.6829,0.4307,0.5139

More information: Thomas Demeester, Dolf Trieschnigg, Dong Nguyen, Ke Zhou, 
and Djoerd Hiemstra. [Overview of the TREC 2014 Federated Web Search Track][1], 
In: Proceedings of the 23rd Text REtrieval Conference Proceedings (TREC), 
2015. 

[1]: http://trec.nist.gov/pubs/trec23/papers/overview-federated.pdf "FedWeb14"
