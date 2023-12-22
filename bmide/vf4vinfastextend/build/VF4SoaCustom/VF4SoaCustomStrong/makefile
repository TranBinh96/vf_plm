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
VF4SOACUSTOMSTRONG_CLS      = $(PROJECT_CLS)$(PS)VF4SoaCustomStrong
VF4SOACUSTOMSTRONG_ANTARGS  = -Dsoa.client.home=$(SOA_CLIENT_KIT) -Dtoolbox.home=$(TOOLBOX_HOME) \
                                -Dgen.src.home=$(PROJECT_DRV)       -Dclasses.home=$(VF4SOACUSTOMSTRONG_CLS) \
                                -Djars.home=$(PROJECT_JAR)          -Djavac.debug=$(JAVAC_DEBUG)
VF4SOACUSTOMSTRONG_NETARGS  = /property:Configuration=$(NET_CONFIG),CHSARP_HOME=$(CHSARP_HOME),PROJECT_DRV=$(PROJECT_DRV),PROJECT_NET=$(PROJECT_NET),PROJECT_IOD=$(PROJECT_IOD),SOA_CLIENT_KIT=$(SOA_CLIENT_KIT)
VF4SOACUSTOMSTRONG_JAVA_SRC = $(PROJECT_DRV)$(PS)VF4SoaCustom$(PS)VF4SoaCustomMap.properties
VF4SOACUSTOMSTRONG_JAVA_TARG= $(PROJECT_JAR)$(PS)VF4SoaCustomStrong_13000.3.0.jar
VF4SOACUSTOMSTRONG_NET_SRC  = $(PROJECT_DRV)$(PS)VF4SoaCustomClient$(PS)strong$(PS)net$(PS)*.cs
VF4SOACUSTOMSTRONG_NET_TARG = $(PROJECT_NET)$(PS)VF4SoaCustomStrong.dll

# ==================================================
# Build targets for C++ Strong Client Bindings
# ==================================================
include build/VF4SoaCustom/VF4SoaCustomStrong/libvf4soacustomstrong.makeInc
include build/VF4SoaCustom/VF4SoaCustomStrong/libvf4soacustomstrongmngd.makeInc



VF4SoaCustomStrong_all:    VF4SoaCustomStrong-build_all$(JAVA_STRONG_ENABLED)   libvf4soacustomstrong_all$(CPP_STRONG_ENABLED)   libvf4soacustomstrong_all$(CPP_STRONG_ENABLED)   libvf4soacustomstrongmngd_all$(CPP_STRONG_ENABLED)   libvf4soacustomstrongmngd_all$(CPP_STRONG_ENABLED)   VF4SoaCustomStrong-cs_all$(NET_STRONG_ENABLED)  
VF4SoaCustomStrong_clean:  VF4SoaCustomStrong-build_clean$(JAVA_STRONG_ENABLED) libvf4soacustomstrong_clean$(CPP_STRONG_ENABLED) libvf4soacustomstrong_clean$(CPP_STRONG_ENABLED) libvf4soacustomstrongmngd_clean$(CPP_STRONG_ENABLED) libvf4soacustomstrongmngd_clean$(CPP_STRONG_ENABLED) VF4SoaCustomStrong-cs_clean$(NET_STRONG_ENABLED)


# ==================================================
# Build targets for Java Strong Client Bindings
# ==================================================
VF4SoaCustomStrong-build_allNoOp:
VF4SoaCustomStrong-build_all:  $(VF4SOACUSTOMSTRONG_JAVA_TARG)

$(VF4SOACUSTOMSTRONG_JAVA_SRC) :

$(VF4SOACUSTOMSTRONG_JAVA_TARG) :  $(VF4SOACUSTOMSTRONG_JAVA_SRC)  
	@echo ________________________________________________________________________________
	@echo Building Java Client VF4SoaCustomStrong
	@$(ANT) -file build/VF4SoaCustom/VF4SoaCustomStrong/VF4SoaCustomStrong-build.xml $(VF4SOACUSTOMSTRONG_ANTARGS) all
	$(COPY_FILE) $(VF4SOACUSTOMSTRONG_JAVA_TARG) $(PROJECT_SOA_CLIENT)$(PS)java$(PS)libs
	@echo ________________________________________________________________________________

	
VF4SoaCustomStrong-build_cleanNoOp:
VF4SoaCustomStrong-build_clean:
	@echo ________________________________________________________________________________
	@echo Cleaning Java Client VF4SoaCustomStrong
	@$(ANT) -file  build/VF4SoaCustom/VF4SoaCustomStrong/VF4SoaCustomStrong-build.xml $(VF4SOACUSTOMSTRONG_ANTARGS) clean
	-$(DELETE_FILE)  $(PROJECT_SOA_CLIENT)$(PS)java$(PS)libs$(PS)VF4SoaCustomStrong_13000.3.0.jar
	@echo ________________________________________________________________________________





# ==================================================
# Build targets for .NET Strong Client Bindings
# ==================================================
VF4SoaCustomStrong-cs_allNoOp:
VF4SoaCustomStrong-cs_all: $(VF4SOACUSTOMSTRONG_NET_TARG)

$(VF4SOACUSTOMSTRONG_NET_SRC) : 

$(VF4SOACUSTOMSTRONG_NET_TARG) : $(VF4SOACUSTOMSTRONG_NET_SRC)   
	@echo ________________________________________________________________________________
	@echo Building .NET Client VF4SoaCustomStrong
	@$(CSHARP_COMPILER) build/VF4SoaCustom/VF4SoaCustomStrong/VF4SoaCustomStrong.csproj $(VF4SOACUSTOMSTRONG_NETARGS) /target:Build
	$(COPY_FILE)  $(VF4SOACUSTOMSTRONG_NET_TARG) $(PROJECT_SOA_CLIENT)$(PS)net$(PS)libs
	@echo ________________________________________________________________________________

	
VF4SoaCustomStrong-cs_cleanNoOp: 
VF4SoaCustomStrong-cs_clean: 
	@echo ________________________________________________________________________________
	@echo Cleaning .NET Client VF4SoaCustomStrong
	@$(CSHARP_COMPILER) build/VF4SoaCustom/VF4SoaCustomStrong/VF4SoaCustomStrong.csproj $(VF4SOACUSTOMSTRONG_NETARGS) /target:Clean
	-$(DELETE_FILE)  $(PROJECT_SOA_CLIENT)$(PS)net$(PS)libs$(PS)VF4SoaCustomStrong.dll
	@echo ________________________________________________________________________________


