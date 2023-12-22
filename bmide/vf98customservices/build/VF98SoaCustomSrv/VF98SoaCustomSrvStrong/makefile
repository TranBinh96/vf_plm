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
VF98SOACUSTOMSRVSTRONG_CLS      = $(PROJECT_CLS)$(PS)VF98SoaCustomSrvStrong
VF98SOACUSTOMSRVSTRONG_ANTARGS  = -Dsoa.client.home=$(SOA_CLIENT_KIT) -Dtoolbox.home=$(TOOLBOX_HOME) \
                                -Dgen.src.home=$(PROJECT_DRV)       -Dclasses.home=$(VF98SOACUSTOMSRVSTRONG_CLS) \
                                -Djars.home=$(PROJECT_JAR)          -Djavac.debug=$(JAVAC_DEBUG)
VF98SOACUSTOMSRVSTRONG_NETARGS  = /property:Configuration=$(NET_CONFIG),CHSARP_HOME=$(CHSARP_HOME),PROJECT_DRV=$(PROJECT_DRV),PROJECT_NET=$(PROJECT_NET),PROJECT_IOD=$(PROJECT_IOD),SOA_CLIENT_KIT=$(SOA_CLIENT_KIT)
VF98SOACUSTOMSRVSTRONG_JAVA_SRC = $(PROJECT_DRV)$(PS)VF98SoaCustomSrv$(PS)VF98SoaCustomSrvMap.properties
VF98SOACUSTOMSRVSTRONG_JAVA_TARG= $(PROJECT_JAR)$(PS)VF98SoaCustomSrvStrong_11000.2.0.jar
VF98SOACUSTOMSRVSTRONG_NET_SRC  = $(PROJECT_DRV)$(PS)VF98SoaCustomSrvClient$(PS)strong$(PS)net$(PS)*.cs
VF98SOACUSTOMSRVSTRONG_NET_TARG = $(PROJECT_NET)$(PS)VF98SoaCustomSrvStrong.dll

# ==================================================
# Build targets for C++ Strong Client Bindings
# ==================================================



VF98SoaCustomSrvStrong_all:    VF98SoaCustomSrvStrong-build_all$(JAVA_STRONG_ENABLED)  
VF98SoaCustomSrvStrong_clean:  VF98SoaCustomSrvStrong-build_clean$(JAVA_STRONG_ENABLED)


# ==================================================
# Build targets for Java Strong Client Bindings
# ==================================================
VF98SoaCustomSrvStrong-build_allNoOp:
VF98SoaCustomSrvStrong-build_all:  $(VF98SOACUSTOMSRVSTRONG_JAVA_TARG)

$(VF98SOACUSTOMSRVSTRONG_JAVA_SRC) :

$(VF98SOACUSTOMSRVSTRONG_JAVA_TARG) :  $(VF98SOACUSTOMSRVSTRONG_JAVA_SRC)  
	@echo ________________________________________________________________________________
	@echo Building Java Client VF98SoaCustomSrvStrong
	@$(ANT) -file build/VF98SoaCustomSrv/VF98SoaCustomSrvStrong/VF98SoaCustomSrvStrong-build.xml $(VF98SOACUSTOMSRVSTRONG_ANTARGS) all
	$(COPY_FILE) $(VF98SOACUSTOMSRVSTRONG_JAVA_TARG) $(PORJECT_SOA_CLIENT)$(PS)java$(PS)libs
	@echo ________________________________________________________________________________

	
VF98SoaCustomSrvStrong-build_cleanNoOp:
VF98SoaCustomSrvStrong-build_clean:
	@echo ________________________________________________________________________________
	@echo Cleaning Java Client VF98SoaCustomSrvStrong
	@$(ANT) -file  build/VF98SoaCustomSrv/VF98SoaCustomSrvStrong/VF98SoaCustomSrvStrong-build.xml $(VF98SOACUSTOMSRVSTRONG_ANTARGS) clean
	-$(DELETE_FILE)  $(PORJECT_SOA_CLIENT)$(PS)java$(PS)libs$(PS)VF98SoaCustomSrvStrong_11000.2.0.jar
	@echo ________________________________________________________________________________





# ==================================================
# Build targets for .NET Strong Client Bindings
# ==================================================
VF98SoaCustomSrvStrong-cs_allNoOp:
VF98SoaCustomSrvStrong-cs_all: $(VF98SOACUSTOMSRVSTRONG_NET_TARG)

$(VF98SOACUSTOMSRVSTRONG_NET_SRC) : 

$(VF98SOACUSTOMSRVSTRONG_NET_TARG) : $(VF98SOACUSTOMSRVSTRONG_NET_SRC)   
	@echo ________________________________________________________________________________
	@echo Building .NET Client VF98SoaCustomSrvStrong
	@$(CSHARP_COMPILER) build/VF98SoaCustomSrv/VF98SoaCustomSrvStrong/VF98SoaCustomSrvStrong.csproj $(VF98SOACUSTOMSRVSTRONG_NETARGS) /target:Build
	$(COPY_FILE)  $(VF98SOACUSTOMSRVSTRONG_NET_TARG) $(PORJECT_SOA_CLIENT)$(PS)net$(PS)libs
	@echo ________________________________________________________________________________

	
VF98SoaCustomSrvStrong-cs_cleanNoOp: 
VF98SoaCustomSrvStrong-cs_clean: 
	@echo ________________________________________________________________________________
	@echo Cleaning .NET Client VF98SoaCustomSrvStrong
	@$(CSHARP_COMPILER) build/VF98SoaCustomSrv/VF98SoaCustomSrvStrong/VF98SoaCustomSrvStrong.csproj $(VF98SOACUSTOMSRVSTRONG_NETARGS) /target:Clean
	-$(DELETE_FILE)  $(PORJECT_SOA_CLIENT)$(PS)net$(PS)libs$(PS)VF98SoaCustomSrvStrong.dll
	@echo ________________________________________________________________________________


