Searsia FedWeb Experiments
==========================

Step 1, Download from FedWeb Greates Hits (https://fedwebgh.intec.ugent.be):

    fedwebgh/meta_data/
    fedwebgh/search_data/fedweb14/FW14-sample-search/

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

Step 6, Create the FedWeb14 sample index:

    java -cp searsiaserver.jar org.searsia.fedweb.FedwebSamples

* You see "Index 0XX" for each engine that has samples;
* Some engines do not exist, causing normal warnings ("No e006.xml");

Step 7, Start the Searsia server:

    java -jar target/searsiafedweb.jar -mother=none -name=fedweb14 --path=index`

Step 8, Run the experiment:

    python src/main/python/trec-run.py \
    fedwebgh/meta_data/topics/FW-topics.xml >fw.run

Step 9, Evaluate the results:

    python fedwebgh/meta_data/TREC_evaluation/eval_scripts/FW-eval-RS.py \
    fw.run fedwebgh/meta_data/TREC_evaluation/qrels_files/FW14-QRELS-RS.txt \
    trec_eval.9.0/
