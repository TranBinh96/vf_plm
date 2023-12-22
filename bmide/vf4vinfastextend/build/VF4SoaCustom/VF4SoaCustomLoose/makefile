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
# Define macros for Loose Client Binding libraries
# ==================================================
VF4SOACUSTOMLOOSE_CLS       = $(PROJECT_CLS)$(PS)VF4SoaCustomLoose
VF4SOACUSTOMLOOSE_ANTARGS   = -Dsoa.client.home=$(SOA_CLIENT_KIT) -Dtoolbox.home=$(TOOLBOX_HOME) \
                                 -Dgen.src.home=$(PROJECT_DRV)       -Dclasses.home=$(VF4SOACUSTOMLOOSE_CLS) \
                                 -Djars.home=$(PROJECT_JAR)          -Djavac.debug=$(JAVAC_DEBUG)
VF4SOACUSTOMLOOSE_JAVA_SRC  = $(PROJECT_DRV)$(PS)VF4SoaCustom$(PS)VF4SoaCustomMap.properties
VF4SOACUSTOMLOOSE_JAVA_TARG = $(PROJECT_JAR)$(PS)VF4SoaCustomLoose_13000.3.0.jar


VF4SoaCustomLoose_all:   VF4SoaCustomLoose-build_all$(JAVA_LOOSE_ENABLED)
VF4SoaCustomLoose_clean: VF4SoaCustomLoose-build_clean$(JAVA_LOOSE_ENABLED)

# ==================================================
# Build targets for Java Loose Client Bindings
# ==================================================
VF4SoaCustomLoose-build_allNoOp:
VF4SoaCustomLoose-build_all: $(VF4SOACUSTOMLOOSE_JAVA_TARG)


$(VF4SOACUSTOMLOOSE_JAVA_SRC) :

$(VF4SOACUSTOMLOOSE_JAVA_TARG) :  $(VF4SOACUSTOMLOOSE_JAVA_SRC)  
	@echo ________________________________________________________________________________
	@echo Building Java Client VF4SoaCustomLoose
	@$(ANT) -file build/VF4SoaCustom/VF4SoaCustomLoose/VF4SoaCustomLoose-build.xml $(VF4SOACUSTOMLOOSE_ANTARGS) all
	$(COPY_FILE) $(VF4SOACUSTOMLOOSE_JAVA_TARG) $(PROJECT_SOA_CLIENT)$(PS)java$(PS)libs
	@echo ________________________________________________________________________________


VF4SoaCustomLoose-build_cleanNoOp :	
VF4SoaCustomLoose-build_clean:
	@echo ________________________________________________________________________________
	@echo Cleaning Java Client VF4SoaCustomLoose
	@$(ANT) -file  build/VF4SoaCustom/VF4SoaCustomLoose/VF4SoaCustomLoose-build.xml $(VF4SOACUSTOMLOOSE_ANTARGS) clean
	-$(DELETE_FILE)  $(PROJECT_SOA_CLIENT)$(PS)java$(PS)libs$(PS)VF4SoaCustomLoose_13000.3.0.jar
	@echo ________________________________________________________________________________

