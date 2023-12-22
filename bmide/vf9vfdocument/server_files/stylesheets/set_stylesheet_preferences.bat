set UPASS=-u=infodba -p=Inf0db19 -g=dba

REM RAC#
preferences_manager %UPASS%  -mode=import -scope=SITE -catagories=General   -action=OVERRIDE  -report_file=.\5.log  -preference=VF_DocStand_Create.CREATE_REGISTEREDTO -values=VF9_D_Standard

preferences_manager %UPASS%  -mode=import -scope=SITE -catagories=General   -action=OVERRIDE  -report_file=.\3.log  -preference=VF9_D_Standard.CREATERENDERING -values=VF_DocStand_Create

preferences_manager %UPASS%  -mode=import -scope=SITE -catagories=General   -action=OVERRIDE  -report_file=.\4.log  -preference=VF9_D_StandardRevision.SUMMARYRENDERING -values=VF_DocStand_RevSummary

preferences_manager %UPASS%  -mode=import -scope=SITE -catagories=General   -action=OVERRIDE  -report_file=.\6.log  -preference=VF_DocStand_RevSummary.SUMMARY_REGISTEREDTO -values=VF9_D_StandardRevision



REM AWC# CREATE
preferences_manager %UPASS%  -mode=import -scope=SITE -catagories=General   -action=OVERRIDE  -report_file=.\1.log  -preference=Awp0VF_DocStand_Create.CREATE_REGISTEREDTO -values=VF9_D_Standard

preferences_manager %UPASS%  -mode=import -scope=SITE -catagories=General   -action=OVERRIDE  -report_file=.\2.log  -preference=Awp0VF_DocStand_Create.REGISTEREDTO -values=VF9_D_Standard



