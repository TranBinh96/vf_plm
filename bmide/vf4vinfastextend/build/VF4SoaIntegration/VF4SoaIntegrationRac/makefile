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
VF4SOAINTEGRATIONRAC_CLS      = $(PROJECT_CLS)$(PS)VF4SoaIntegrationRac
VF4SOAINTEGRATIONRAC_ANTARGS  = -Dsoa.client.home=$(SOA_CLIENT_KIT) -Dtoolbox.home=$(TOOLBOX_HOME) \
                                -Dgen.src.home=$(PROJECT_DRV)       -Dclasses.home=$(VF4SOAINTEGRATIONRAC_CLS) \
                                -Djars.home=$(PROJECT_JAR)          -Djavac.debug=$(JAVAC_DEBUG) \
                                -Drac.home=$(RAC_HOME) 
VF4SOAINTEGRATIONRAC_JAVA_SRC = $(PROJECT_DRV)$(PS)VF4SoaIntegration$(PS)VF4SoaIntegrationMap.properties
VF4SOAINTEGRATIONRAC_JAVA_TARG= $(PROJECT_JAR)$(PS)VF4SoaIntegrationRac_13000.3.0.jar



VF4SoaIntegrationRac_all:   VF4SoaIntegrationRac-build_all$(JAVA_RAC_ENABLED)
VF4SoaIntegrationRac_clean: VF4SoaIntegrationRac-build_clean$(JAVA_RAC_ENABLED)


# ==================================================
# Build targets for Java RAC Client Bindings
# ==================================================
VF4SoaIntegrationRac-build_allNoOp:
VF4SoaIntegrationRac-build_all:  $(VF4SOAINTEGRATIONRAC_JAVA_TARG)

$(VF4SOAINTEGRATIONRAC_JAVA_SRC) :

$(VF4SOAINTEGRATIONRAC_JAVA_TARG) :  $(VF4SOAINTEGRATIONRAC_JAVA_SRC)  
	@echo ________________________________________________________________________________
	@echo Building Java Client VF4SoaIntegrationRac
	@$(ANT) -file build/VF4SoaIntegration/VF4SoaIntegrationRac/VF4SoaIntegrationRac-build.xml $(VF4SOAINTEGRATIONRAC_ANTARGS) all
	$(COPY_FILE) $(VF4SOAINTEGRATIONRAC_JAVA_TARG) $(PROJECT_SOA_CLIENT)$(PS)java$(PS)libs
	@echo ________________________________________________________________________________



VF4SoaIntegrationRac-build_cleanNoOp:	
VF4SoaIntegrationRac-build_clean:
	@echo ________________________________________________________________________________
	@echo Cleaning Java Client VF4SoaIntegrationRac
	@$(ANT) -file  build/VF4SoaIntegration/VF4SoaIntegrationRac/VF4SoaIntegrationRac-build.xml $(VF4SOAINTEGRATIONRAC_ANTARGS) clean
	-$(DELETE_FILE)  $(PROJECT_SOA_CLIENT)$(PS)java$(PS)libs$(PS)VF4SoaIntegrationRac_13000.3.0.jar
	@echo ________________________________________________________________________________




