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
VF4SOACUSTOMRAC_CLS      = $(PROJECT_CLS)$(PS)VF4SoaCustomRac
VF4SOACUSTOMRAC_ANTARGS  = -Dsoa.client.home=$(SOA_CLIENT_KIT) -Dtoolbox.home=$(TOOLBOX_HOME) \
                                -Dgen.src.home=$(PROJECT_DRV)       -Dclasses.home=$(VF4SOACUSTOMRAC_CLS) \
                                -Djars.home=$(PROJECT_JAR)          -Djavac.debug=$(JAVAC_DEBUG) \
                                -Drac.home=$(RAC_HOME) 
VF4SOACUSTOMRAC_JAVA_SRC = $(PROJECT_DRV)$(PS)VF4SoaCustom$(PS)VF4SoaCustomMap.properties
VF4SOACUSTOMRAC_JAVA_TARG= $(PROJECT_JAR)$(PS)VF4SoaCustomRac_13000.3.0.jar



VF4SoaCustomRac_all:   VF4SoaCustomRac-build_all$(JAVA_RAC_ENABLED)
VF4SoaCustomRac_clean: VF4SoaCustomRac-build_clean$(JAVA_RAC_ENABLED)


# ==================================================
# Build targets for Java RAC Client Bindings
# ==================================================
VF4SoaCustomRac-build_allNoOp:
VF4SoaCustomRac-build_all:  $(VF4SOACUSTOMRAC_JAVA_TARG)

$(VF4SOACUSTOMRAC_JAVA_SRC) :

$(VF4SOACUSTOMRAC_JAVA_TARG) :  $(VF4SOACUSTOMRAC_JAVA_SRC)  
	@echo ________________________________________________________________________________
	@echo Building Java Client VF4SoaCustomRac
	@$(ANT) -file build/VF4SoaCustom/VF4SoaCustomRac/VF4SoaCustomRac-build.xml $(VF4SOACUSTOMRAC_ANTARGS) all
	$(COPY_FILE) $(VF4SOACUSTOMRAC_JAVA_TARG) $(PROJECT_SOA_CLIENT)$(PS)java$(PS)libs
	@echo ________________________________________________________________________________



VF4SoaCustomRac-build_cleanNoOp:	
VF4SoaCustomRac-build_clean:
	@echo ________________________________________________________________________________
	@echo Cleaning Java Client VF4SoaCustomRac
	@$(ANT) -file  build/VF4SoaCustom/VF4SoaCustomRac/VF4SoaCustomRac-build.xml $(VF4SOACUSTOMRAC_ANTARGS) clean
	-$(DELETE_FILE)  $(PROJECT_SOA_CLIENT)$(PS)java$(PS)libs$(PS)VF4SoaCustomRac_13000.3.0.jar
	@echo ________________________________________________________________________________




