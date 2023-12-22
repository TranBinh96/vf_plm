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
VF98SOACUSTOMSRVLOOSE_CLS       = $(PROJECT_CLS)$(PS)VF98SoaCustomSrvLoose
VF98SOACUSTOMSRVLOOSE_ANTARGS   = -Dsoa.client.home=$(SOA_CLIENT_KIT) -Dtoolbox.home=$(TOOLBOX_HOME) \
                                 -Dgen.src.home=$(PROJECT_DRV)       -Dclasses.home=$(VF98SOACUSTOMSRVLOOSE_CLS) \
                                 -Djars.home=$(PROJECT_JAR)          -Djavac.debug=$(JAVAC_DEBUG)
VF98SOACUSTOMSRVLOOSE_JAVA_SRC  = $(PROJECT_DRV)$(PS)VF98SoaCustomSrv$(PS)VF98SoaCustomSrvMap.properties
VF98SOACUSTOMSRVLOOSE_JAVA_TARG = $(PROJECT_JAR)$(PS)VF98SoaCustomSrvLoose_11000.2.0.jar


VF98SoaCustomSrvLoose_all:   VF98SoaCustomSrvLoose-build_all$(JAVA_LOOSE_ENABLED)
VF98SoaCustomSrvLoose_clean: VF98SoaCustomSrvLoose-build_clean$(JAVA_LOOSE_ENABLED)

# ==================================================
# Build targets for Java Loose Client Bindings
# ==================================================
VF98SoaCustomSrvLoose-build_allNoOp:
VF98SoaCustomSrvLoose-build_all: $(VF98SOACUSTOMSRVLOOSE_JAVA_TARG)


$(VF98SOACUSTOMSRVLOOSE_JAVA_SRC) :

$(VF98SOACUSTOMSRVLOOSE_JAVA_TARG) :  $(VF98SOACUSTOMSRVLOOSE_JAVA_SRC)  
	@echo ________________________________________________________________________________
	@echo Building Java Client VF98SoaCustomSrvLoose
	@$(ANT) -file build/VF98SoaCustomSrv/VF98SoaCustomSrvLoose/VF98SoaCustomSrvLoose-build.xml $(VF98SOACUSTOMSRVLOOSE_ANTARGS) all
	$(COPY_FILE) $(VF98SOACUSTOMSRVLOOSE_JAVA_TARG) $(PORJECT_SOA_CLIENT)$(PS)java$(PS)libs
	@echo ________________________________________________________________________________


VF98SoaCustomSrvLoose-build_cleanNoOp :	
VF98SoaCustomSrvLoose-build_clean:
	@echo ________________________________________________________________________________
	@echo Cleaning Java Client VF98SoaCustomSrvLoose
	@$(ANT) -file  build/VF98SoaCustomSrv/VF98SoaCustomSrvLoose/VF98SoaCustomSrvLoose-build.xml $(VF98SOACUSTOMSRVLOOSE_ANTARGS) clean
	-$(DELETE_FILE)  $(PORJECT_SOA_CLIENT)$(PS)java$(PS)libs$(PS)VF98SoaCustomSrvLoose_11000.2.0.jar
	@echo ________________________________________________________________________________

