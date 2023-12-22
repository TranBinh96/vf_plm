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
VF98SOACUSTOMSRVRAC_CLS      = $(PROJECT_CLS)$(PS)VF98SoaCustomSrvRac
VF98SOACUSTOMSRVRAC_ANTARGS  = -Dsoa.client.home=$(SOA_CLIENT_KIT) -Dtoolbox.home=$(TOOLBOX_HOME) \
                                -Dgen.src.home=$(PROJECT_DRV)       -Dclasses.home=$(VF98SOACUSTOMSRVRAC_CLS) \
                                -Djars.home=$(PROJECT_JAR)          -Djavac.debug=$(JAVAC_DEBUG) \
                                -Drac.home=$(RAC_HOME) 
VF98SOACUSTOMSRVRAC_JAVA_SRC = $(PROJECT_DRV)$(PS)VF98SoaCustomSrv$(PS)VF98SoaCustomSrvMap.properties
VF98SOACUSTOMSRVRAC_JAVA_TARG= $(PROJECT_JAR)$(PS)VF98SoaCustomSrvRac_11000.2.0.jar



VF98SoaCustomSrvRac_all:   VF98SoaCustomSrvRac-build_all$(JAVA_RAC_ENABLED)
VF98SoaCustomSrvRac_clean: VF98SoaCustomSrvRac-build_clean$(JAVA_RAC_ENABLED)


# ==================================================
# Build targets for Java RAC Client Bindings
# ==================================================
VF98SoaCustomSrvRac-build_allNoOp:
VF98SoaCustomSrvRac-build_all:  $(VF98SOACUSTOMSRVRAC_JAVA_TARG)

$(VF98SOACUSTOMSRVRAC_JAVA_SRC) :

$(VF98SOACUSTOMSRVRAC_JAVA_TARG) :  $(VF98SOACUSTOMSRVRAC_JAVA_SRC)  
	@echo ________________________________________________________________________________
	@echo Building Java Client VF98SoaCustomSrvRac
	@$(ANT) -file build/VF98SoaCustomSrv/VF98SoaCustomSrvRac/VF98SoaCustomSrvRac-build.xml $(VF98SOACUSTOMSRVRAC_ANTARGS) all
	$(COPY_FILE) $(VF98SOACUSTOMSRVRAC_JAVA_TARG) $(PORJECT_SOA_CLIENT)$(PS)java$(PS)libs
	@echo ________________________________________________________________________________



VF98SoaCustomSrvRac-build_cleanNoOp:	
VF98SoaCustomSrvRac-build_clean:
	@echo ________________________________________________________________________________
	@echo Cleaning Java Client VF98SoaCustomSrvRac
	@$(ANT) -file  build/VF98SoaCustomSrv/VF98SoaCustomSrvRac/VF98SoaCustomSrvRac-build.xml $(VF98SOACUSTOMSRVRAC_ANTARGS) clean
	-$(DELETE_FILE)  $(PORJECT_SOA_CLIENT)$(PS)java$(PS)libs$(PS)VF98SoaCustomSrvRac_11000.2.0.jar
	@echo ________________________________________________________________________________




