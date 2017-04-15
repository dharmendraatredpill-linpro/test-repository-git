#!/usr/bin/python
# coding=utf-8
import re
import sys
from os.path import join,exists
from os import makedirs,symlink
from datetime import datetime
from datetime import timedelta
def parseDocTypeMapping(filename):
    mappingFile = open(filename, "r")
    lines = mappingFile.readlines()
    mapping={}
    for line in lines:
        tmp=line.strip().split(',')
        mapping[tmp[0]]=tmp[1]
    mappingFile.close()
    return mapping

def parseSecurityLog(filename):
    secF = open(filename, "r")
    lines = secF.readlines()
    sec={}
    for line in lines:
        tmp=line.strip().split(',')
	if tmp[0] in sec:
            sec[tmp[0]]='Sites/dm-export/documentLibrary'
	else:
            sec[tmp[0]]='User Homes'
    secF.close()
    return sec

def parseExportLog(filename):
    export = open(filename, "r")
    lines = export.readlines()
    docs=[]
    fields=[]
    fields=lines[0].strip().split(',')
    for i in range(len(fields)):
        fields[i]=fields[i][1:-1]
    for line in lines[1:]:
        tmp_l=[]
        tmp_t={}
        tmp_l=line.strip().replace('","','"¤"').split('¤')
        for i in range(len(fields)):
            tmp_t[fields[i]]=tmp_l[i][1:-1]
        docs.append(tmp_t)
    export.close()
    return docs

def parseSpecificMetadata(filename):
    metaf = open(filename, "r")
    lines = metaf.readlines()
    spec_meta={}
    fields=[]
    fields=lines[0].strip().split(';')
    for line in lines[1:]:
        tmp_l=[]
        tmp_t={}
        tmp_l=line.strip().split(';')
        if len(fields) != len(tmp_l):
            print 'Non conformant row: %s'%(tmp_l)
        for i in range(len(fields)):
            tmp_t[fields[i]]=tmp_l[i]
        spec_meta[tmp_t['docnumber']]=tmp_t
    metaf.close()
    return spec_meta

def parseDmDate(cd,ct,ld,lt):
    cdt = datetime.strptime(cd+'T'+ct, '%Y-%m-%dT%H:%M:%S')
    ldt = datetime.strptime(ld+'T'+lt, '%Y-%m-%dT%H:%M:%S')
    adt = datetime.now()+timedelta(days=365+365)
    return(cdt,ldt,adt)

def outputXMLandLink(doc,path,mapping,spec_meta,directory_path,strip_origin,origin_path):
    (created,modified,availableTo)=parseDmDate(doc['CREATION_DATE'],doc['CREATION_TIME'],doc['LASTEDITDATE'],doc['LASTEDITTIME'])
    out_path=join(path,directory_path[doc['DOCNUMBER']],doc['AUTHOR'],created.strftime('%Y/%m'))
    if not exists(out_path):
        makedirs(out_path)
    doc_filename=re.sub('.+\\\\','',doc['FULL_NAME'])
    print 'Doc filename %s'%(doc_filename)
    doc_filename_extension=re.sub('.+\.','',doc_filename)
    print 'Doc extension %s'%(doc_filename_extension)
    doc_filename_safe=doc['DOCNUMBER']+'.'+doc_filename_extension
    print 'Doc filename safe %s'%(doc_filename_safe)
    xml_filename=join(out_path,doc_filename_safe+'.metadata.properties.xml')
    print
    print 'Processing doc: %s'%(doc['DOCNUMBER'])
    print 'Created xml file: %s'%(xml_filename)
    doc_filename_in=join(origin_path,re.sub('\\\\','/',doc['FULL_NAME'].replace(strip_origin,'').replace('INFORMAT.','INFORMAT').replace('PRESENTAT.','PRESENTAT').replace('ORGANISAT.','ORGANISAT').replace('FÖRTECKN.','FÖRTECKN').replace('BESTÄLLN.','BESTÄLLN')))
    doc_filename_out=join(out_path,doc_filename_safe)
    print 'Create link: %s->%s'%(doc_filename_out,doc_filename_in)
    symlink(doc_filename_in,doc_filename_out)
    xml_file=open(xml_filename,'w')
    xml_file.write('<?xml version="1.0" encoding="UTF-8"?>\n')
    xml_file.write('<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">\n')
    xml_file.write('<properties>\n')
    xml_file.write('\t<entry key="type">smhi:document</entry>\n')
    xml_file.write('\t<entry key="smhi:documentId">%s</entry>\n'%(doc['DOCNUMBER']))
    xml_file.write('\t<entry key="cm:creator">%s</entry>\n'%(doc['AUTHOR']))
    xml_file.write('\t<entry key="cm:owner">%s</entry>\n'%(doc['AUTHOR']))
    xml_file.write('\t<entry key="cm:modifier">%s</entry>\n'%(doc['TYPIST']))
    xml_file.write('\t<entry key="cm:name">%s</entry>\n'%(doc_filename.replace('&','&amp;').replace('<','&lt;').replace('>','&gt;').replace('#','DM')))
    xml_file.write('\t<entry key="cm:title">%s</entry>\n'%(doc['DOCNAME'].replace('&','&amp;').replace('<','&lt;').replace('>','&gt;')))
    if len(mapping[doc['TYPE_ID']])>0:
      xml_file.write('\t<entry key="smhi:recordType">%s</entry>\n'%(mapping[doc['TYPE_ID']]))
    xml_file.write('\t<entry key="cm:description">%s</entry>\n'%(doc['ABSTRACT'].replace('&','&amp;').replace('<','&lt;').replace('>','&gt;')))
    xml_file.write('\t<entry key="cm:created">%s</entry>\n'%(created.strftime('%Y-%m-%dT%H:%M:%SZ')))
    xml_file.write('\t<entry key="cm:modified">%s</entry>\n'%(modified.strftime('%Y-%m-%dT%H:%M:%SZ')))
    xml_file.write('\t<entry key="smhi:availableTo">%s</entry>\n'%(availableTo.strftime('%Y-%m-%dT%H:%M:%SZ')))
    if doc['DOCNUMBER'] in spec_meta:
        if len(spec_meta[doc['DOCNUMBER']]['SMHI_DIARIENR'])>0:
            xml_file.write('\t<entry key="smhi:customDiarieNumber">%s</entry>\n'%(spec_meta[doc['DOCNUMBER']]['SMHI_DIARIENR']))
        if len(spec_meta[doc['DOCNUMBER']]['smhi_sekretess'])>0 and spec_meta[doc['DOCNUMBER']]['smhi_sekretess']!='Normal':
            xml_file.write('\t<entry key="smhi:informationManagement">LIMITED</entry>\n')
        else:
            xml_file.write('\t<entry key="smhi:informationManagement">NORMAL</entry>\n')
        if len(spec_meta[doc['DOCNUMBER']]['user_id'])>0:
            xml_file.write('\t<entry key="smhi:documentStatusPerformer">%s</entry>\n'%(spec_meta[doc['DOCNUMBER']]['user_id']))
    else:
        xml_file.write('\t<entry key="smhi:informationManagement">NORMAL</entry>\n')
    xml_file.write('</properties>\n')
    xml_file.close()

if len(sys.argv)!=5:
    print '%s <input path> <output path> <strip this prefix from export path> <type mapping file>'%(sys.argv[0])
    sys.exit(1)

in_path=sys.argv[1]
out_path=sys.argv[2]
strip_origin=sys.argv[3]
type_mapping_file=sys.argv[4]

docs=parseExportLog(join(in_path,'Export.log'))
mapping=parseDocTypeMapping(type_mapping_file)
spec_meta=parseSpecificMetadata(join(in_path,'DM_specifik_metadata2.csv'))
directory_path=parseSecurityLog(join(in_path,'Security.log'))
for doc in docs:
    outputXMLandLink(doc,out_path,mapping,spec_meta,directory_path,strip_origin,in_path)

