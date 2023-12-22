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
VF4SOAADMINUTILSLOOSE_CLS       = $(PROJECT_CLS)$(PS)VF4SoaAdminUtilsLoose
VF4SOAADMINUTILSLOOSE_ANTARGS   = -Dsoa.client.home=$(SOA_CLIENT_KIT) -Dtoolbox.home=$(TOOLBOX_HOME) \
                                 -Dgen.src.home=$(PROJECT_DRV)       -Dclasses.home=$(VF4SOAADMINUTILSLOOSE_CLS) \
                                 -Djars.home=$(PROJECT_JAR)          -Djavac.debug=$(JAVAC_DEBUG)
VF4SOAADMINUTILSLOOSE_JAVA_SRC  = $(PROJECT_DRV)$(PS)VF4SoaAdminUtils$(PS)VF4SoaAdminUtilsMap.properties
VF4SOAADMINUTILSLOOSE_JAVA_TARG = $(PROJECT_JAR)$(PS)VF4SoaAdminUtilsLoose_13000.3.0.jar


VF4SoaAdminUtilsLoose_all:   VF4SoaAdminUtilsLoose-build_all$(JAVA_LOOSE_ENABLED)
VF4SoaAdminUtilsLoose_clean: VF4SoaAdminUtilsLoose-build_clean$(JAVA_LOOSE_ENABLED)

# ==================================================
# Build targets for Java Loose Client Bindings
# ==================================================
VF4SoaAdminUtilsLoose-build_allNoOp:
VF4SoaAdminUtilsLoose-build_all: $(VF4SOAADMINUTILSLOOSE_JAVA_TARG)


$(VF4SOAADMINUTILSLOOSE_JAVA_SRC) :

$(VF4SOAADMINUTILSLOOSE_JAVA_TARG) :  $(VF4SOAADMINUTILSLOOSE_JAVA_SRC)  
	@echo ________________________________________________________________________________
	@echo Building Java Client VF4SoaAdminUtilsLoose
	@$(ANT) -file build/VF4SoaAdminUtils/VF4SoaAdminUtilsLoose/VF4SoaAdminUtilsLoose-build.xml $(VF4SOAADMINUTILSLOOSE_ANTARGS) all
	$(COPY_FILE) $(VF4SOAADMINUTILSLOOSE_JAVA_TARG) $(PROJECT_SOA_CLIENT)$(PS)java$(PS)libs
	@echo ________________________________________________________________________________


VF4SoaAdminUtilsLoose-build_cleanNoOp :	
VF4SoaAdminUtilsLoose-build_clean:
	@echo ________________________________________________________________________________
	@echo Cleaning Java Client VF4SoaAdminUtilsLoose
	@$(ANT) -file  build/VF4SoaAdminUtils/VF4SoaAdminUtilsLoose/VF4SoaAdminUtilsLoose-build.xml $(VF4SOAADMINUTILSLOOSE_ANTARGS) clean
	-$(DELETE_FILE)  $(PROJECT_SOA_CLIENT)$(PS)java$(PS)libs$(PS)VF4SoaAdminUtilsLoose_13000.3.0.jar
	@echo ________________________________________________________________________________

