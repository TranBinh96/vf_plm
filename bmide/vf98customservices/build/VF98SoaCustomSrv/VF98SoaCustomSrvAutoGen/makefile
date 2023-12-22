# ==================================================
# Copyright 2013.
# Siemens Product Lifecycle Management Software Inc.
# All Rights Reserved.
# ==================================================
# ==================================================
#             Auto-generated source
#                 DO NOT EDIT
# ==================================================
   


 
# ==================================================
# Define macros for the service library Auto-Generation
# ==================================================   
VF98SOACUSTOMSRVAUTOGEN_ANTARGS   = -Dtoolbox.home=$(TOOLBOX_HOME)  \
                                 -Dtemplate.home=$(TEMPLATE_HOME)           -Dgen.src.home=$(PROJECT_DRV) \
                                 -Dproject.soa.client=$(PROJECT_SOA_CLIENT) -Dproject.tc=$(PROJECT_TC) \
                                 -Dplatform=$(PLAT) -Dteamcenter.home=$(TC_ROOT) \
                                 -Dgenerated.stubs="$(GENERATED_STUBS)"

VF98SOACUSTOMSRVAUTOGEN_AUTO_SRC  = $(PROJECT_HOME)$(PS)extensions$(PS)*.xml
VF98SOACUSTOMSRVAUTOGEN_AUTO_TARG = $(PROJECT_DRV)$(PS)VF98SoaCustomSrv$(PS)VF98SoaCustomSrvMap.properties


# ==================================================
# Build targets for Auto-Generation of Service library
# ==================================================
VF98SoaCustomSrvAutoGen_all:    $(VF98SOACUSTOMSRVAUTOGEN_AUTO_TARG) 

$(VF98SOACUSTOMSRVAUTOGEN_AUTO_TARG) : $(VF98SOACUSTOMSRVAUTOGEN_AUTO_SRC)
	@echo ________________________________________________________________________________
	@echo Auto Generation of Source for Service Library VF98SoaCustomSrv
	@SET CLASSPATH=$(TOOLBOX_HOME)/xerces_j/xercesImpl.jar$(PD)$(TOOLBOX_HOME)/xerces_j/xml-apis.jar
	@$(ANT) -file build/VF98SoaCustomSrv/VF98SoaCustomSrvAutoGen/VF98SoaCustomSrvAutoGen-build.xml $(VF98SOACUSTOMSRVAUTOGEN_ANTARGS) all
	@echo ________________________________________________________________________________


	
VF98SoaCustomSrvAutoGen_clean: 
	@echo ________________________________________________________________________________
	@echo Cleaning Auto Generated Source of Service Library VF98SoaCustomSrv
	@$(ANT) -file build/VF98SoaCustomSrv/VF98SoaCustomSrvAutoGen/VF98SoaCustomSrvAutoGen-build.xml $(VF98SOACUSTOMSRVAUTOGEN_ANTARGS) clean  
	@echo ________________________________________________________________________________
