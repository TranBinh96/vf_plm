set UPASS=-u=infodba -p=Inf0db19 -g=dba

plmxml_import %UPASS% -xml_file=.\MDS_VFDoc_0001.xml  -transfermode=ConfiguredDataImportDefault -import_mode=overwrite -log=.\import.log
