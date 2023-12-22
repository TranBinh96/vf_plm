set INFODBA_PF="%TC_ROOT%\security\VINFAST_infodba.pwf"
preferences_manager -u=infodba -pf=%INFODBA_PF%  -g=dba -mode=export -scope=SITE -out_file=preferences_site.xml