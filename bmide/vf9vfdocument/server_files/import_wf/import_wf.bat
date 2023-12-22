set UPASS=-u=infodba -p=Inf0db19 -g=dba

plmxml_import %UPASS% -xml_file=.\VF_Standard_Doc_Release_Process.xml  -transfermode=workflow_template_overwrite -import_mode=overwrite -log=.\import_wf.log
