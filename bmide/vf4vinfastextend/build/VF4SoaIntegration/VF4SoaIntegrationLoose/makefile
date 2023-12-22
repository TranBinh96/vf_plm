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
VF4SOAINTEGRATIONLOOSE_CLS       = $(PROJECT_CLS)$(PS)VF4SoaIntegrationLoose
VF4SOAINTEGRATIONLOOSE_ANTARGS   = -Dsoa.client.home=$(SOA_CLIENT_KIT) -Dtoolbox.home=$(TOOLBOX_HOME) \
                                 -Dgen.src.home=$(PROJECT_DRV)       -Dclasses.home=$(VF4SOAINTEGRATIONLOOSE_CLS) \
                                 -Djars.home=$(PROJECT_JAR)          -Djavac.debug=$(JAVAC_DEBUG)
VF4SOAINTEGRATIONLOOSE_JAVA_SRC  = $(PROJECT_DRV)$(PS)VF4SoaIntegration$(PS)VF4SoaIntegrationMap.properties
VF4SOAINTEGRATIONLOOSE_JAVA_TARG = $(PROJECT_JAR)$(PS)VF4SoaIntegrationLoose_13000.3.0.jar


VF4SoaIntegrationLoose_all:   VF4SoaIntegrationLoose-build_all$(JAVA_LOOSE_ENABLED)
VF4SoaIntegrationLoose_clean: VF4SoaIntegrationLoose-build_clean$(JAVA_LOOSE_ENABLED)

# ==================================================
# Build targets for Java Loose Client Bindings
# ==================================================
VF4SoaIntegrationLoose-build_allNoOp:
VF4SoaIntegrationLoose-build_all: $(VF4SOAINTEGRATIONLOOSE_JAVA_TARG)


$(VF4SOAINTEGRATIONLOOSE_JAVA_SRC) :

$(VF4SOAINTEGRATIONLOOSE_JAVA_TARG) :  $(VF4SOAINTEGRATIONLOOSE_JAVA_SRC)  
	@echo ________________________________________________________________________________
	@echo Building Java Client VF4SoaIntegrationLoose
	@$(ANT) -file build/VF4SoaIntegration/VF4SoaIntegrationLoose/VF4SoaIntegrationLoose-build.xml $(VF4SOAINTEGRATIONLOOSE_ANTARGS) all
	$(COPY_FILE) $(VF4SOAINTEGRATIONLOOSE_JAVA_TARG) $(PROJECT_SOA_CLIENT)$(PS)java$(PS)libs
	@echo ________________________________________________________________________________


VF4SoaIntegrationLoose-build_cleanNoOp :	
VF4SoaIntegrationLoose-build_clean:
	@echo ________________________________________________________________________________
	@echo Cleaning Java Client VF4SoaIntegrationLoose
	@$(ANT) -file  build/VF4SoaIntegration/VF4SoaIntegrationLoose/VF4SoaIntegrationLoose-build.xml $(VF4SOAINTEGRATIONLOOSE_ANTARGS) clean
	-$(DELETE_FILE)  $(PROJECT_SOA_CLIENT)$(PS)java$(PS)libs$(PS)VF4SoaIntegrationLoose_13000.3.0.jar
	@echo ________________________________________________________________________________

