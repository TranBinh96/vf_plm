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
VF4SOAADMINUTILSAUTOGEN_ANTARGS   = -Dtoolbox.home=$(TOOLBOX_HOME)  \
                                 -Dtemplate.home=$(TEMPLATE_HOME)           -Dgen.src.home=$(PROJECT_DRV) \
                                 -Dproject.soa.client=$(PROJECT_SOA_CLIENT) -Dproject.tc=$(PROJECT_TC) \
                                 -Dplatform=$(PLAT) -Dteamcenter.home=$(TC_ROOT) \
                                 -Dgenerated.stubs="$(GENERATED_STUBS)"

VF4SOAADMINUTILSAUTOGEN_AUTO_SRC  = $(PROJECT_HOME)$(PS)extensions$(PS)*.xml
VF4SOAADMINUTILSAUTOGEN_AUTO_TARG = $(PROJECT_DRV)$(PS)VF4SoaAdminUtils$(PS)VF4SoaAdminUtilsMap.properties


# ==================================================
# Build targets for Auto-Generation of Service library
# ==================================================
VF4SoaAdminUtilsAutoGen_all:    $(VF4SOAADMINUTILSAUTOGEN_AUTO_TARG) 

$(VF4SOAADMINUTILSAUTOGEN_AUTO_TARG) : $(VF4SOAADMINUTILSAUTOGEN_AUTO_SRC)
	@echo ________________________________________________________________________________
	@echo Auto Generation of Source for Service Library VF4SoaAdminUtils
	@$(SET_ENV) CLASSPATH=$(TOOLBOX_HOME)/xerces_j/xercesImpl.jar$(PD)$(TOOLBOX_HOME)/xerces_j/xml-apis.jar
	@$(ANT) -file build/VF4SoaAdminUtils/VF4SoaAdminUtilsAutoGen/VF4SoaAdminUtilsAutoGen-build.xml $(VF4SOAADMINUTILSAUTOGEN_ANTARGS) all
	@echo ________________________________________________________________________________


	
VF4SoaAdminUtilsAutoGen_clean: 
	@echo ________________________________________________________________________________
	@echo Cleaning Auto Generated Source of Service Library VF4SoaAdminUtils
	@$(ANT) -file build/VF4SoaAdminUtils/VF4SoaAdminUtilsAutoGen/VF4SoaAdminUtilsAutoGen-build.xml $(VF4SOAADMINUTILSAUTOGEN_ANTARGS) clean  
	@echo ________________________________________________________________________________
