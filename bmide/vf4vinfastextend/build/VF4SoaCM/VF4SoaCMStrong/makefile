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
VF4SOACMSTRONG_CLS      = $(PROJECT_CLS)$(PS)VF4SoaCMStrong
VF4SOACMSTRONG_ANTARGS  = -Dsoa.client.home=$(SOA_CLIENT_KIT) -Dtoolbox.home=$(TOOLBOX_HOME) \
                                -Dgen.src.home=$(PROJECT_DRV)       -Dclasses.home=$(VF4SOACMSTRONG_CLS) \
                                -Djars.home=$(PROJECT_JAR)          -Djavac.debug=$(JAVAC_DEBUG)
VF4SOACMSTRONG_NETARGS  = /property:Configuration=$(NET_CONFIG),CHSARP_HOME=$(CHSARP_HOME),PROJECT_DRV=$(PROJECT_DRV),PROJECT_NET=$(PROJECT_NET),PROJECT_IOD=$(PROJECT_IOD),SOA_CLIENT_KIT=$(SOA_CLIENT_KIT)
VF4SOACMSTRONG_JAVA_SRC = $(PROJECT_DRV)$(PS)VF4SoaCM$(PS)VF4SoaCMMap.properties
VF4SOACMSTRONG_JAVA_TARG= $(PROJECT_JAR)$(PS)VF4SoaCMStrong_13000.3.0.jar
VF4SOACMSTRONG_NET_SRC  = $(PROJECT_DRV)$(PS)VF4SoaCMClient$(PS)strong$(PS)net$(PS)*.cs
VF4SOACMSTRONG_NET_TARG = $(PROJECT_NET)$(PS)VF4SoaCMStrong.dll

# ==================================================
# Build targets for C++ Strong Client Bindings
# ==================================================
include build/VF4SoaCM/VF4SoaCMStrong/libvf4soacmstrong.makeInc
include build/VF4SoaCM/VF4SoaCMStrong/libvf4soacmstrongmngd.makeInc



VF4SoaCMStrong_all:    VF4SoaCMStrong-build_all$(JAVA_STRONG_ENABLED)   libvf4soacmstrong_all$(CPP_STRONG_ENABLED)   libvf4soacmstrong_all$(CPP_STRONG_ENABLED)   libvf4soacmstrongmngd_all$(CPP_STRONG_ENABLED)   libvf4soacmstrongmngd_all$(CPP_STRONG_ENABLED)   VF4SoaCMStrong-cs_all$(NET_STRONG_ENABLED)  
VF4SoaCMStrong_clean:  VF4SoaCMStrong-build_clean$(JAVA_STRONG_ENABLED) libvf4soacmstrong_clean$(CPP_STRONG_ENABLED) libvf4soacmstrong_clean$(CPP_STRONG_ENABLED) libvf4soacmstrongmngd_clean$(CPP_STRONG_ENABLED) libvf4soacmstrongmngd_clean$(CPP_STRONG_ENABLED) VF4SoaCMStrong-cs_clean$(NET_STRONG_ENABLED)


# ==================================================
# Build targets for Java Strong Client Bindings
# ==================================================
VF4SoaCMStrong-build_allNoOp:
VF4SoaCMStrong-build_all:  $(VF4SOACMSTRONG_JAVA_TARG)

$(VF4SOACMSTRONG_JAVA_SRC) :

$(VF4SOACMSTRONG_JAVA_TARG) :  $(VF4SOACMSTRONG_JAVA_SRC)  
	@echo ________________________________________________________________________________
	@echo Building Java Client VF4SoaCMStrong
	@$(ANT) -file build/VF4SoaCM/VF4SoaCMStrong/VF4SoaCMStrong-build.xml $(VF4SOACMSTRONG_ANTARGS) all
	$(COPY_FILE) $(VF4SOACMSTRONG_JAVA_TARG) $(PROJECT_SOA_CLIENT)$(PS)java$(PS)libs
	@echo ________________________________________________________________________________

	
VF4SoaCMStrong-build_cleanNoOp:
VF4SoaCMStrong-build_clean:
	@echo ________________________________________________________________________________
	@echo Cleaning Java Client VF4SoaCMStrong
	@$(ANT) -file  build/VF4SoaCM/VF4SoaCMStrong/VF4SoaCMStrong-build.xml $(VF4SOACMSTRONG_ANTARGS) clean
	-$(DELETE_FILE)  $(PROJECT_SOA_CLIENT)$(PS)java$(PS)libs$(PS)VF4SoaCMStrong_13000.3.0.jar
	@echo ________________________________________________________________________________





# ==================================================
# Build targets for .NET Strong Client Bindings
# ==================================================
VF4SoaCMStrong-cs_allNoOp:
VF4SoaCMStrong-cs_all: $(VF4SOACMSTRONG_NET_TARG)

$(VF4SOACMSTRONG_NET_SRC) : 

$(VF4SOACMSTRONG_NET_TARG) : $(VF4SOACMSTRONG_NET_SRC)   
	@echo ________________________________________________________________________________
	@echo Building .NET Client VF4SoaCMStrong
	@$(CSHARP_COMPILER) build/VF4SoaCM/VF4SoaCMStrong/VF4SoaCMStrong.csproj $(VF4SOACMSTRONG_NETARGS) /target:Build
	$(COPY_FILE)  $(VF4SOACMSTRONG_NET_TARG) $(PROJECT_SOA_CLIENT)$(PS)net$(PS)libs
	@echo ________________________________________________________________________________

	
VF4SoaCMStrong-cs_cleanNoOp: 
VF4SoaCMStrong-cs_clean: 
	@echo ________________________________________________________________________________
	@echo Cleaning .NET Client VF4SoaCMStrong
	@$(CSHARP_COMPILER) build/VF4SoaCM/VF4SoaCMStrong/VF4SoaCMStrong.csproj $(VF4SOACMSTRONG_NETARGS) /target:Clean
	-$(DELETE_FILE)  $(PROJECT_SOA_CLIENT)$(PS)net$(PS)libs$(PS)VF4SoaCMStrong.dll
	@echo ________________________________________________________________________________


