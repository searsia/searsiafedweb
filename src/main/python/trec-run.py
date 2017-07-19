
import requests
import sys
import xml.etree.ElementTree

URL = 'http://localhost:16842/searsia/index/fedweb14.json'

def trecout(qid, json): 
    tag = json['searsia'].replace('.', 'x')
    nr = 1
    done = dict()
    for hit in json['hits']:
        rid = hit['rid']
        if (not rid in done):
            print qid, 'Q0', 'FW14-' + hit['rid'], nr, hit['score'], tag
            done[rid] = True
            nr += 1

if (len(sys.argv) != 2):
    sys.stderr.write ("Usage: python trec-run.py fedwebgh/meta_data/topics/FW-topics.xml\n")
    sys.exit(1)

xmldoc = xml.etree.ElementTree.parse(sys.argv[1])
for topic in xmldoc.getroot().findall('./topic'):
    if (topic.attrib['official'] ==  'FedWeb14' and
        topic.attrib['evaluation'] == 'TREC'):
        qid = topic.attrib['id']
        query = topic.find('./query').text
        param = { 'q': query, 'resources': '1000' }
        response = requests.get(URL, params=param)
        trecout(qid, response.json())
