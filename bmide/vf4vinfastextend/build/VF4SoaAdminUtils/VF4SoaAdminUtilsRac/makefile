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
VF4SOAADMINUTILSRAC_CLS      = $(PROJECT_CLS)$(PS)VF4SoaAdminUtilsRac
VF4SOAADMINUTILSRAC_ANTARGS  = -Dsoa.client.home=$(SOA_CLIENT_KIT) -Dtoolbox.home=$(TOOLBOX_HOME) \
                                -Dgen.src.home=$(PROJECT_DRV)       -Dclasses.home=$(VF4SOAADMINUTILSRAC_CLS) \
                                -Djars.home=$(PROJECT_JAR)          -Djavac.debug=$(JAVAC_DEBUG) \
                                -Drac.home=$(RAC_HOME) 
VF4SOAADMINUTILSRAC_JAVA_SRC = $(PROJECT_DRV)$(PS)VF4SoaAdminUtils$(PS)VF4SoaAdminUtilsMap.properties
VF4SOAADMINUTILSRAC_JAVA_TARG= $(PROJECT_JAR)$(PS)VF4SoaAdminUtilsRac_13000.3.0.jar



VF4SoaAdminUtilsRac_all:   VF4SoaAdminUtilsRac-build_all$(JAVA_RAC_ENABLED)
VF4SoaAdminUtilsRac_clean: VF4SoaAdminUtilsRac-build_clean$(JAVA_RAC_ENABLED)


# ==================================================
# Build targets for Java RAC Client Bindings
# ==================================================
VF4SoaAdminUtilsRac-build_allNoOp:
VF4SoaAdminUtilsRac-build_all:  $(VF4SOAADMINUTILSRAC_JAVA_TARG)

$(VF4SOAADMINUTILSRAC_JAVA_SRC) :

$(VF4SOAADMINUTILSRAC_JAVA_TARG) :  $(VF4SOAADMINUTILSRAC_JAVA_SRC)  
	@echo ________________________________________________________________________________
	@echo Building Java Client VF4SoaAdminUtilsRac
	@$(ANT) -file build/VF4SoaAdminUtils/VF4SoaAdminUtilsRac/VF4SoaAdminUtilsRac-build.xml $(VF4SOAADMINUTILSRAC_ANTARGS) all
	$(COPY_FILE) $(VF4SOAADMINUTILSRAC_JAVA_TARG) $(PROJECT_SOA_CLIENT)$(PS)java$(PS)libs
	@echo ________________________________________________________________________________



VF4SoaAdminUtilsRac-build_cleanNoOp:	
VF4SoaAdminUtilsRac-build_clean:
	@echo ________________________________________________________________________________
	@echo Cleaning Java Client VF4SoaAdminUtilsRac
	@$(ANT) -file  build/VF4SoaAdminUtils/VF4SoaAdminUtilsRac/VF4SoaAdminUtilsRac-build.xml $(VF4SOAADMINUTILSRAC_ANTARGS) clean
	-$(DELETE_FILE)  $(PROJECT_SOA_CLIENT)$(PS)java$(PS)libs$(PS)VF4SoaAdminUtilsRac_13000.3.0.jar
	@echo ________________________________________________________________________________




