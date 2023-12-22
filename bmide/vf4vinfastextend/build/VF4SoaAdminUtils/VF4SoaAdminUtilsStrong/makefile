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
# Define macros for Strong Client Binding libraries
# ==================================================
VF4SOAADMINUTILSSTRONG_CLS      = $(PROJECT_CLS)$(PS)VF4SoaAdminUtilsStrong
VF4SOAADMINUTILSSTRONG_ANTARGS  = -Dsoa.client.home=$(SOA_CLIENT_KIT) -Dtoolbox.home=$(TOOLBOX_HOME) \
                                -Dgen.src.home=$(PROJECT_DRV)       -Dclasses.home=$(VF4SOAADMINUTILSSTRONG_CLS) \
                                -Djars.home=$(PROJECT_JAR)          -Djavac.debug=$(JAVAC_DEBUG)
VF4SOAADMINUTILSSTRONG_NETARGS  = /property:Configuration=$(NET_CONFIG),CHSARP_HOME=$(CHSARP_HOME),PROJECT_DRV=$(PROJECT_DRV),PROJECT_NET=$(PROJECT_NET),PROJECT_IOD=$(PROJECT_IOD),SOA_CLIENT_KIT=$(SOA_CLIENT_KIT)
VF4SOAADMINUTILSSTRONG_JAVA_SRC = $(PROJECT_DRV)$(PS)VF4SoaAdminUtils$(PS)VF4SoaAdminUtilsMap.properties
VF4SOAADMINUTILSSTRONG_JAVA_TARG= $(PROJECT_JAR)$(PS)VF4SoaAdminUtilsStrong_13000.3.0.jar
VF4SOAADMINUTILSSTRONG_NET_SRC  = $(PROJECT_DRV)$(PS)VF4SoaAdminUtilsClient$(PS)strong$(PS)net$(PS)*.cs
VF4SOAADMINUTILSSTRONG_NET_TARG = $(PROJECT_NET)$(PS)VF4SoaAdminUtilsStrong.dll

# ==================================================
# Build targets for C++ Strong Client Bindings
# ==================================================
include build/VF4SoaAdminUtils/VF4SoaAdminUtilsStrong/libvf4soaadminutilsstrong.makeInc
include build/VF4SoaAdminUtils/VF4SoaAdminUtilsStrong/libvf4soaadminutilsstrongmngd.makeInc



VF4SoaAdminUtilsStrong_all:    VF4SoaAdminUtilsStrong-build_all$(JAVA_STRONG_ENABLED)   libvf4soaadminutilsstrong_all$(CPP_STRONG_ENABLED)   libvf4soaadminutilsstrong_all$(CPP_STRONG_ENABLED)   libvf4soaadminutilsstrongmngd_all$(CPP_STRONG_ENABLED)   libvf4soaadminutilsstrongmngd_all$(CPP_STRONG_ENABLED)   VF4SoaAdminUtilsStrong-cs_all$(NET_STRONG_ENABLED)  
VF4SoaAdminUtilsStrong_clean:  VF4SoaAdminUtilsStrong-build_clean$(JAVA_STRONG_ENABLED) libvf4soaadminutilsstrong_clean$(CPP_STRONG_ENABLED) libvf4soaadminutilsstrong_clean$(CPP_STRONG_ENABLED) libvf4soaadminutilsstrongmngd_clean$(CPP_STRONG_ENABLED) libvf4soaadminutilsstrongmngd_clean$(CPP_STRONG_ENABLED) VF4SoaAdminUtilsStrong-cs_clean$(NET_STRONG_ENABLED)


# ==================================================
# Build targets for Java Strong Client Bindings
# ==================================================
VF4SoaAdminUtilsStrong-build_allNoOp:
VF4SoaAdminUtilsStrong-build_all:  $(VF4SOAADMINUTILSSTRONG_JAVA_TARG)

$(VF4SOAADMINUTILSSTRONG_JAVA_SRC) :

$(VF4SOAADMINUTILSSTRONG_JAVA_TARG) :  $(VF4SOAADMINUTILSSTRONG_JAVA_SRC)  
	@echo ________________________________________________________________________________
	@echo Building Java Client VF4SoaAdminUtilsStrong
	@$(ANT) -file build/VF4SoaAdminUtils/VF4SoaAdminUtilsStrong/VF4SoaAdminUtilsStrong-build.xml $(VF4SOAADMINUTILSSTRONG_ANTARGS) all
	$(COPY_FILE) $(VF4SOAADMINUTILSSTRONG_JAVA_TARG) $(PROJECT_SOA_CLIENT)$(PS)java$(PS)libs
	@echo ________________________________________________________________________________

	
VF4SoaAdminUtilsStrong-build_cleanNoOp:
VF4SoaAdminUtilsStrong-build_clean:
	@echo ________________________________________________________________________________
	@echo Cleaning Java Client VF4SoaAdminUtilsStrong
	@$(ANT) -file  build/VF4SoaAdminUtils/VF4SoaAdminUtilsStrong/VF4SoaAdminUtilsStrong-build.xml $(VF4SOAADMINUTILSSTRONG_ANTARGS) clean
	-$(DELETE_FILE)  $(PROJECT_SOA_CLIENT)$(PS)java$(PS)libs$(PS)VF4SoaAdminUtilsStrong_13000.3.0.jar
	@echo ________________________________________________________________________________





# ==================================================
# Build targets for .NET Strong Client Bindings
# ==================================================
VF4SoaAdminUtilsStrong-cs_allNoOp:
VF4SoaAdminUtilsStrong-cs_all: $(VF4SOAADMINUTILSSTRONG_NET_TARG)

$(VF4SOAADMINUTILSSTRONG_NET_SRC) : 

$(VF4SOAADMINUTILSSTRONG_NET_TARG) : $(VF4SOAADMINUTILSSTRONG_NET_SRC)   
	@echo ________________________________________________________________________________
	@echo Building .NET Client VF4SoaAdminUtilsStrong
	@$(CSHARP_COMPILER) build/VF4SoaAdminUtils/VF4SoaAdminUtilsStrong/VF4SoaAdminUtilsStrong.csproj $(VF4SOAADMINUTILSSTRONG_NETARGS) /target:Build
	$(COPY_FILE)  $(VF4SOAADMINUTILSSTRONG_NET_TARG) $(PROJECT_SOA_CLIENT)$(PS)net$(PS)libs
	@echo ________________________________________________________________________________

	
VF4SoaAdminUtilsStrong-cs_cleanNoOp: 
VF4SoaAdminUtilsStrong-cs_clean: 
	@echo ________________________________________________________________________________
	@echo Cleaning .NET Client VF4SoaAdminUtilsStrong
	@$(CSHARP_COMPILER) build/VF4SoaAdminUtils/VF4SoaAdminUtilsStrong/VF4SoaAdminUtilsStrong.csproj $(VF4SOAADMINUTILSSTRONG_NETARGS) /target:Clean
	-$(DELETE_FILE)  $(PROJECT_SOA_CLIENT)$(PS)net$(PS)libs$(PS)VF4SoaAdminUtilsStrong.dll
	@echo ________________________________________________________________________________


