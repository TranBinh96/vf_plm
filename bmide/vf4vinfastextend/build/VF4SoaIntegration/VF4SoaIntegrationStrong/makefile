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
VF4SOAINTEGRATIONSTRONG_CLS      = $(PROJECT_CLS)$(PS)VF4SoaIntegrationStrong
VF4SOAINTEGRATIONSTRONG_ANTARGS  = -Dsoa.client.home=$(SOA_CLIENT_KIT) -Dtoolbox.home=$(TOOLBOX_HOME) \
                                -Dgen.src.home=$(PROJECT_DRV)       -Dclasses.home=$(VF4SOAINTEGRATIONSTRONG_CLS) \
                                -Djars.home=$(PROJECT_JAR)          -Djavac.debug=$(JAVAC_DEBUG)
VF4SOAINTEGRATIONSTRONG_NETARGS  = /property:Configuration=$(NET_CONFIG),CHSARP_HOME=$(CHSARP_HOME),PROJECT_DRV=$(PROJECT_DRV),PROJECT_NET=$(PROJECT_NET),PROJECT_IOD=$(PROJECT_IOD),SOA_CLIENT_KIT=$(SOA_CLIENT_KIT)
VF4SOAINTEGRATIONSTRONG_JAVA_SRC = $(PROJECT_DRV)$(PS)VF4SoaIntegration$(PS)VF4SoaIntegrationMap.properties
VF4SOAINTEGRATIONSTRONG_JAVA_TARG= $(PROJECT_JAR)$(PS)VF4SoaIntegrationStrong_13000.3.0.jar
VF4SOAINTEGRATIONSTRONG_NET_SRC  = $(PROJECT_DRV)$(PS)VF4SoaIntegrationClient$(PS)strong$(PS)net$(PS)*.cs
VF4SOAINTEGRATIONSTRONG_NET_TARG = $(PROJECT_NET)$(PS)VF4SoaIntegrationStrong.dll

# ==================================================
# Build targets for C++ Strong Client Bindings
# ==================================================
include build/VF4SoaIntegration/VF4SoaIntegrationStrong/libvf4soaintegrationstrong.makeInc
include build/VF4SoaIntegration/VF4SoaIntegrationStrong/libvf4soaintegrationstrongmngd.makeInc



VF4SoaIntegrationStrong_all:    VF4SoaIntegrationStrong-build_all$(JAVA_STRONG_ENABLED)   libvf4soaintegrationstrong_all$(CPP_STRONG_ENABLED)   libvf4soaintegrationstrong_all$(CPP_STRONG_ENABLED)   libvf4soaintegrationstrongmngd_all$(CPP_STRONG_ENABLED)   libvf4soaintegrationstrongmngd_all$(CPP_STRONG_ENABLED)   VF4SoaIntegrationStrong-cs_all$(NET_STRONG_ENABLED)  
VF4SoaIntegrationStrong_clean:  VF4SoaIntegrationStrong-build_clean$(JAVA_STRONG_ENABLED) libvf4soaintegrationstrong_clean$(CPP_STRONG_ENABLED) libvf4soaintegrationstrong_clean$(CPP_STRONG_ENABLED) libvf4soaintegrationstrongmngd_clean$(CPP_STRONG_ENABLED) libvf4soaintegrationstrongmngd_clean$(CPP_STRONG_ENABLED) VF4SoaIntegrationStrong-cs_clean$(NET_STRONG_ENABLED)


# ==================================================
# Build targets for Java Strong Client Bindings
# ==================================================
VF4SoaIntegrationStrong-build_allNoOp:
VF4SoaIntegrationStrong-build_all:  $(VF4SOAINTEGRATIONSTRONG_JAVA_TARG)

$(VF4SOAINTEGRATIONSTRONG_JAVA_SRC) :

$(VF4SOAINTEGRATIONSTRONG_JAVA_TARG) :  $(VF4SOAINTEGRATIONSTRONG_JAVA_SRC)  
	@echo ________________________________________________________________________________
	@echo Building Java Client VF4SoaIntegrationStrong
	@$(ANT) -file build/VF4SoaIntegration/VF4SoaIntegrationStrong/VF4SoaIntegrationStrong-build.xml $(VF4SOAINTEGRATIONSTRONG_ANTARGS) all
	$(COPY_FILE) $(VF4SOAINTEGRATIONSTRONG_JAVA_TARG) $(PROJECT_SOA_CLIENT)$(PS)java$(PS)libs
	@echo ________________________________________________________________________________

	
VF4SoaIntegrationStrong-build_cleanNoOp:
VF4SoaIntegrationStrong-build_clean:
	@echo ________________________________________________________________________________
	@echo Cleaning Java Client VF4SoaIntegrationStrong
	@$(ANT) -file  build/VF4SoaIntegration/VF4SoaIntegrationStrong/VF4SoaIntegrationStrong-build.xml $(VF4SOAINTEGRATIONSTRONG_ANTARGS) clean
	-$(DELETE_FILE)  $(PROJECT_SOA_CLIENT)$(PS)java$(PS)libs$(PS)VF4SoaIntegrationStrong_13000.3.0.jar
	@echo ________________________________________________________________________________





# ==================================================
# Build targets for .NET Strong Client Bindings
# ==================================================
VF4SoaIntegrationStrong-cs_allNoOp:
VF4SoaIntegrationStrong-cs_all: $(VF4SOAINTEGRATIONSTRONG_NET_TARG)

$(VF4SOAINTEGRATIONSTRONG_NET_SRC) : 

$(VF4SOAINTEGRATIONSTRONG_NET_TARG) : $(VF4SOAINTEGRATIONSTRONG_NET_SRC)   
	@echo ________________________________________________________________________________
	@echo Building .NET Client VF4SoaIntegrationStrong
	@$(CSHARP_COMPILER) build/VF4SoaIntegration/VF4SoaIntegrationStrong/VF4SoaIntegrationStrong.csproj $(VF4SOAINTEGRATIONSTRONG_NETARGS) /target:Build
	$(COPY_FILE)  $(VF4SOAINTEGRATIONSTRONG_NET_TARG) $(PROJECT_SOA_CLIENT)$(PS)net$(PS)libs
	@echo ________________________________________________________________________________

	
VF4SoaIntegrationStrong-cs_cleanNoOp: 
VF4SoaIntegrationStrong-cs_clean: 
	@echo ________________________________________________________________________________
	@echo Cleaning .NET Client VF4SoaIntegrationStrong
	@$(CSHARP_COMPILER) build/VF4SoaIntegration/VF4SoaIntegrationStrong/VF4SoaIntegrationStrong.csproj $(VF4SOAINTEGRATIONSTRONG_NETARGS) /target:Clean
	-$(DELETE_FILE)  $(PROJECT_SOA_CLIENT)$(PS)net$(PS)libs$(PS)VF4SoaIntegrationStrong.dll
	@echo ________________________________________________________________________________


