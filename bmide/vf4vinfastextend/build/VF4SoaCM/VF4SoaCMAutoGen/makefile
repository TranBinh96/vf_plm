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
VF4SOACMAUTOGEN_ANTARGS   = -Dtoolbox.home=$(TOOLBOX_HOME)  \
                                 -Dtemplate.home=$(TEMPLATE_HOME)           -Dgen.src.home=$(PROJECT_DRV) \
                                 -Dproject.soa.client=$(PROJECT_SOA_CLIENT) -Dproject.tc=$(PROJECT_TC) \
                                 -Dplatform=$(PLAT) -Dteamcenter.home=$(TC_ROOT) \
                                 -Dgenerated.stubs="$(GENERATED_STUBS)"

VF4SOACMAUTOGEN_AUTO_SRC  = $(PROJECT_HOME)$(PS)extensions$(PS)*.xml
VF4SOACMAUTOGEN_AUTO_TARG = $(PROJECT_DRV)$(PS)VF4SoaCM$(PS)VF4SoaCMMap.properties


# ==================================================
# Build targets for Auto-Generation of Service library
# ==================================================
VF4SoaCMAutoGen_all:    $(VF4SOACMAUTOGEN_AUTO_TARG) 

$(VF4SOACMAUTOGEN_AUTO_TARG) : $(VF4SOACMAUTOGEN_AUTO_SRC)
	@echo ________________________________________________________________________________
	@echo Auto Generation of Source for Service Library VF4SoaCM
	@$(SET_ENV) CLASSPATH=$(TOOLBOX_HOME)/xerces_j/xercesImpl.jar$(PD)$(TOOLBOX_HOME)/xerces_j/xml-apis.jar
	@$(ANT) -file build/VF4SoaCM/VF4SoaCMAutoGen/VF4SoaCMAutoGen-build.xml $(VF4SOACMAUTOGEN_ANTARGS) all
	@echo ________________________________________________________________________________


	
VF4SoaCMAutoGen_clean: 
	@echo ________________________________________________________________________________
	@echo Cleaning Auto Generated Source of Service Library VF4SoaCM
	@$(ANT) -file build/VF4SoaCM/VF4SoaCMAutoGen/VF4SoaCMAutoGen-build.xml $(VF4SOACMAUTOGEN_ANTARGS) clean  
	@echo ________________________________________________________________________________
