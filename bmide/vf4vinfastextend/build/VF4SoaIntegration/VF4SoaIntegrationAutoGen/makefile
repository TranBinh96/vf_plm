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
VF4SOAINTEGRATIONAUTOGEN_ANTARGS   = -Dtoolbox.home=$(TOOLBOX_HOME)  \
                                 -Dtemplate.home=$(TEMPLATE_HOME)           -Dgen.src.home=$(PROJECT_DRV) \
                                 -Dproject.soa.client=$(PROJECT_SOA_CLIENT) -Dproject.tc=$(PROJECT_TC) \
                                 -Dplatform=$(PLAT) -Dteamcenter.home=$(TC_ROOT) \
                                 -Dgenerated.stubs="$(GENERATED_STUBS)"

VF4SOAINTEGRATIONAUTOGEN_AUTO_SRC  = $(PROJECT_HOME)$(PS)extensions$(PS)*.xml
VF4SOAINTEGRATIONAUTOGEN_AUTO_TARG = $(PROJECT_DRV)$(PS)VF4SoaIntegration$(PS)VF4SoaIntegrationMap.properties


# ==================================================
# Build targets for Auto-Generation of Service library
# ==================================================
VF4SoaIntegrationAutoGen_all:    $(VF4SOAINTEGRATIONAUTOGEN_AUTO_TARG) 

$(VF4SOAINTEGRATIONAUTOGEN_AUTO_TARG) : $(VF4SOAINTEGRATIONAUTOGEN_AUTO_SRC)
	@echo ________________________________________________________________________________
	@echo Auto Generation of Source for Service Library VF4SoaIntegration
	@$(SET_ENV) CLASSPATH=$(TOOLBOX_HOME)/xerces_j/xercesImpl.jar$(PD)$(TOOLBOX_HOME)/xerces_j/xml-apis.jar
	@$(ANT) -file build/VF4SoaIntegration/VF4SoaIntegrationAutoGen/VF4SoaIntegrationAutoGen-build.xml $(VF4SOAINTEGRATIONAUTOGEN_ANTARGS) all
	@echo ________________________________________________________________________________


	
VF4SoaIntegrationAutoGen_clean: 
	@echo ________________________________________________________________________________
	@echo Cleaning Auto Generated Source of Service Library VF4SoaIntegration
	@$(ANT) -file build/VF4SoaIntegration/VF4SoaIntegrationAutoGen/VF4SoaIntegrationAutoGen-build.xml $(VF4SOAINTEGRATIONAUTOGEN_ANTARGS) clean  
	@echo ________________________________________________________________________________
