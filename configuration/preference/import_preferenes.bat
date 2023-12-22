set INFODBA_PF="%TC_ROOT%\security\VINFAST_infodba.pwf"
preferences_manager -u=infodba -pf=%INFODBA_PF%  -g=dba -mode=import -scope=SITE -file=preferences_site.xml -action=OVERRIDE