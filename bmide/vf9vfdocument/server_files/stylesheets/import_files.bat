set UPASS=-u=dcproxy -p=dcproxy -g=dba
import_file %UPASS% -f=Awp0VF_DocStand_Create.xml  -d=Awp0VF_DocStand_Create -ref=XMLRendering -type=XMLRenderingStylesheet  -de=r 

import_file %UPASS% -f=Awp0VF_DocStand_RevSummary.xml  -d=Awp0VF_DocStand_RevSummary -ref=XMLRendering -type=XMLRenderingStylesheet  -de=r 

import_file %UPASS% -f=VF_DocStand_Create.xml  -d=VF_DocStand_Create -ref=XMLRendering -type=XMLRenderingStylesheet  -de=r 

import_file %UPASS% -f=VF_DocStand_RevSummary.xml  -d=VF_DocStand_RevSummary -ref=XMLRendering -type=XMLRenderingStylesheet  -de=r 

