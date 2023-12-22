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
VF4SOACUSTOMAUTOGEN_ANTARGS   = -Dtoolbox.home=$(TOOLBOX_HOME)  \
                                 -Dtemplate.home=$(TEMPLATE_HOME)           -Dgen.src.home=$(PROJECT_DRV) \
                                 -Dproject.soa.client=$(PROJECT_SOA_CLIENT) -Dproject.tc=$(PROJECT_TC) \
                                 -Dplatform=$(PLAT) -Dteamcenter.home=$(TC_ROOT) \
                                 -Dgenerated.stubs="$(GENERATED_STUBS)"

VF4SOACUSTOMAUTOGEN_AUTO_SRC  = $(PROJECT_HOME)$(PS)extensions$(PS)*.xml
VF4SOACUSTOMAUTOGEN_AUTO_TARG = $(PROJECT_DRV)$(PS)VF4SoaCustom$(PS)VF4SoaCustomMap.properties


# ==================================================
# Build targets for Auto-Generation of Service library
# ==================================================
VF4SoaCustomAutoGen_all:    $(VF4SOACUSTOMAUTOGEN_AUTO_TARG) 

$(VF4SOACUSTOMAUTOGEN_AUTO_TARG) : $(VF4SOACUSTOMAUTOGEN_AUTO_SRC)
	@echo ________________________________________________________________________________
	@echo Auto Generation of Source for Service Library VF4SoaCustom
	@$(SET_ENV) CLASSPATH=$(TOOLBOX_HOME)/xerces_j/xercesImpl.jar$(PD)$(TOOLBOX_HOME)/xerces_j/xml-apis.jar
	@$(ANT) -file build/VF4SoaCustom/VF4SoaCustomAutoGen/VF4SoaCustomAutoGen-build.xml $(VF4SOACUSTOMAUTOGEN_ANTARGS) all
	@echo ________________________________________________________________________________


	
VF4SoaCustomAutoGen_clean: 
	@echo ________________________________________________________________________________
	@echo Cleaning Auto Generated Source of Service Library VF4SoaCustom
	@$(ANT) -file build/VF4SoaCustom/VF4SoaCustomAutoGen/VF4SoaCustomAutoGen-build.xml $(VF4SOACUSTOMAUTOGEN_ANTARGS) clean  
	@echo ________________________________________________________________________________
