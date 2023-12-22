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
VF4SOACMLOOSE_CLS       = $(PROJECT_CLS)$(PS)VF4SoaCMLoose
VF4SOACMLOOSE_ANTARGS   = -Dsoa.client.home=$(SOA_CLIENT_KIT) -Dtoolbox.home=$(TOOLBOX_HOME) \
                                 -Dgen.src.home=$(PROJECT_DRV)       -Dclasses.home=$(VF4SOACMLOOSE_CLS) \
                                 -Djars.home=$(PROJECT_JAR)          -Djavac.debug=$(JAVAC_DEBUG)
VF4SOACMLOOSE_JAVA_SRC  = $(PROJECT_DRV)$(PS)VF4SoaCM$(PS)VF4SoaCMMap.properties
VF4SOACMLOOSE_JAVA_TARG = $(PROJECT_JAR)$(PS)VF4SoaCMLoose_13000.3.0.jar


VF4SoaCMLoose_all:   VF4SoaCMLoose-build_all$(JAVA_LOOSE_ENABLED)
VF4SoaCMLoose_clean: VF4SoaCMLoose-build_clean$(JAVA_LOOSE_ENABLED)

# ==================================================
# Build targets for Java Loose Client Bindings
# ==================================================
VF4SoaCMLoose-build_allNoOp:
VF4SoaCMLoose-build_all: $(VF4SOACMLOOSE_JAVA_TARG)


$(VF4SOACMLOOSE_JAVA_SRC) :

$(VF4SOACMLOOSE_JAVA_TARG) :  $(VF4SOACMLOOSE_JAVA_SRC)  
	@echo ________________________________________________________________________________
	@echo Building Java Client VF4SoaCMLoose
	@$(ANT) -file build/VF4SoaCM/VF4SoaCMLoose/VF4SoaCMLoose-build.xml $(VF4SOACMLOOSE_ANTARGS) all
	$(COPY_FILE) $(VF4SOACMLOOSE_JAVA_TARG) $(PROJECT_SOA_CLIENT)$(PS)java$(PS)libs
	@echo ________________________________________________________________________________


VF4SoaCMLoose-build_cleanNoOp :	
VF4SoaCMLoose-build_clean:
	@echo ________________________________________________________________________________
	@echo Cleaning Java Client VF4SoaCMLoose
	@$(ANT) -file  build/VF4SoaCM/VF4SoaCMLoose/VF4SoaCMLoose-build.xml $(VF4SOACMLOOSE_ANTARGS) clean
	-$(DELETE_FILE)  $(PROJECT_SOA_CLIENT)$(PS)java$(PS)libs$(PS)VF4SoaCMLoose_13000.3.0.jar
	@echo ________________________________________________________________________________

