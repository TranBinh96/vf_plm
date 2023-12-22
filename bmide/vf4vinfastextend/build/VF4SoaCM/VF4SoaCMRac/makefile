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
# Define macros for RAC Client Binding libraries
# ==================================================
VF4SOACMRAC_CLS      = $(PROJECT_CLS)$(PS)VF4SoaCMRac
VF4SOACMRAC_ANTARGS  = -Dsoa.client.home=$(SOA_CLIENT_KIT) -Dtoolbox.home=$(TOOLBOX_HOME) \
                                -Dgen.src.home=$(PROJECT_DRV)       -Dclasses.home=$(VF4SOACMRAC_CLS) \
                                -Djars.home=$(PROJECT_JAR)          -Djavac.debug=$(JAVAC_DEBUG) \
                                -Drac.home=$(RAC_HOME) 
VF4SOACMRAC_JAVA_SRC = $(PROJECT_DRV)$(PS)VF4SoaCM$(PS)VF4SoaCMMap.properties
VF4SOACMRAC_JAVA_TARG= $(PROJECT_JAR)$(PS)VF4SoaCMRac_13000.3.0.jar



VF4SoaCMRac_all:   VF4SoaCMRac-build_all$(JAVA_RAC_ENABLED)
VF4SoaCMRac_clean: VF4SoaCMRac-build_clean$(JAVA_RAC_ENABLED)


# ==================================================
# Build targets for Java RAC Client Bindings
# ==================================================
VF4SoaCMRac-build_allNoOp:
VF4SoaCMRac-build_all:  $(VF4SOACMRAC_JAVA_TARG)

$(VF4SOACMRAC_JAVA_SRC) :

$(VF4SOACMRAC_JAVA_TARG) :  $(VF4SOACMRAC_JAVA_SRC)  
	@echo ________________________________________________________________________________
	@echo Building Java Client VF4SoaCMRac
	@$(ANT) -file build/VF4SoaCM/VF4SoaCMRac/VF4SoaCMRac-build.xml $(VF4SOACMRAC_ANTARGS) all
	$(COPY_FILE) $(VF4SOACMRAC_JAVA_TARG) $(PROJECT_SOA_CLIENT)$(PS)java$(PS)libs
	@echo ________________________________________________________________________________



VF4SoaCMRac-build_cleanNoOp:	
VF4SoaCMRac-build_clean:
	@echo ________________________________________________________________________________
	@echo Cleaning Java Client VF4SoaCMRac
	@$(ANT) -file  build/VF4SoaCM/VF4SoaCMRac/VF4SoaCMRac-build.xml $(VF4SOACMRAC_ANTARGS) clean
	-$(DELETE_FILE)  $(PROJECT_SOA_CLIENT)$(PS)java$(PS)libs$(PS)VF4SoaCMRac_13000.3.0.jar
	@echo ________________________________________________________________________________




