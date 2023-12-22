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
# Define macros for XSD Binding libraries
# ==================================================
VF98SOACUSTOMSRVTYPES_CLS      = $(PROJECT_CLS)$(PS)VF98SoaCustomSrvTypes
VF98SOACUSTOMSRVTYPES_IOD      = $(PROJECT_IOD)$(PS)vf98soacustomsrvtypes
VF98SOACUSTOMSRVTYPES_ANTARGS  = -Dsoa.client.home=$(SOA_CLIENT_KIT) -Dtoolbox.home=$(TOOLBOX_HOME) \
                                -Dgen.src.home=$(PROJECT_DRV)       -Dclasses.home=$(VF98SOACUSTOMSRVTYPES_CLS) \
                                -Djars.home=$(PROJECT_JAR)          -Djavac.debug=$(JAVAC_DEBUG)
VF98SOACUSTOMSRVTYPES_NETARGS  = /property:Configuration=$(NET_CONFIG),CHSARP_HOME=$(CHSARP_HOME),PROJECT_DRV=$(PROJECT_DRV),PROJECT_NET=$(PROJECT_NET),PROJECT_IOD=$(PROJECT_IOD),SOA_CLIENT_KIT=$(SOA_CLIENT_KIT)
VF98SOACUSTOMSRVTYPES_JAVA_SRC = $(PROJECT_DRV)$(PS)VF98SoaCustomSrv$(PS)VF98SoaCustomSrvMap.properties
VF98SOACUSTOMSRVTYPES_JAVA_TARG= $(PROJECT_JAR)$(PS)VF98SoaCustomSrvTypes_11000.2.0.jar
VF98SOACUSTOMSRVTYPES_NET_SRC  = $(PROJECT_DRV)$(PS)VF98SoaCustomSrvTypes$(PS)net$(PS)*.cs
VF98SOACUSTOMSRVTYPES_NET_TARG = $(PROJECT_NET)$(PS)VF98SoaCustomSrvTypes.dll

# ==================================================
# Build targets for C++ XSD Bindings
# ==================================================


# ==================================================
# All build targets for the XSD Binding libraries
# ==================================================
VF98SoaCustomSrvTypes_all:    VF98SoaCustomSrvTypes-build_all  
VF98SoaCustomSrvTypes_clean:  VF98SoaCustomSrvTypes-build_clean



# ==================================================
# Build targets for Java XSD Bindings
# ==================================================
VF98SoaCustomSrvTypes-build_all: $(VF98SOACUSTOMSRVTYPES_JAVA_TARG)

$(VF98SOACUSTOMSRVTYPES_JAVA_SRC) :

$(VF98SOACUSTOMSRVTYPES_JAVA_TARG) :  $(VF98SOACUSTOMSRVTYPES_JAVA_SRC)  
	@echo ________________________________________________________________________________
	@echo Building Java XSD Binding VF98SoaCustomSrvTypes
	@$(ANT) -file build/VF98SoaCustomSrv/VF98SoaCustomSrvTypes/VF98SoaCustomSrvTypes-build.xml $(VF98SOACUSTOMSRVTYPES_ANTARGS) all
	$(COPY_FILE) $(VF98SOACUSTOMSRVTYPES_JAVA_TARG) $(PORJECT_SOA_CLIENT)$(PS)java$(PS)libs
	@echo ________________________________________________________________________________

	
VF98SoaCustomSrvTypes-build_clean:
	@echo ________________________________________________________________________________
	@echo Cleaning Java XSD Binding VF98SoaCustomSrvTypes
	@$(ANT) -file  build/VF98SoaCustomSrv/VF98SoaCustomSrvTypes/VF98SoaCustomSrvTypes-build.xml $(VF98SOACUSTOMSRVTYPES_ANTARGS) clean
	-$(DELETE_FILE)  $(PORJECT_SOA_CLIENT)$(PS)java$(PS)libs$(PS)VF98SoaCustomSrvTypes_11000.2.0.jar
	@echo ________________________________________________________________________________



# ==================================================
# Build targets for .NET XSD Bindings
# ==================================================
VF98SoaCustomSrvTypes-cs_allNoOp:
VF98SoaCustomSrvTypes-cs_all:   $(VF98SOACUSTOMSRVTYPES_NET_TARG)


$(VF98SOACUSTOMSRVTYPES_NET_SRC) : 

$(VF98SOACUSTOMSRVTYPES_NET_TARG) : $(VF98SOACUSTOMSRVTYPES_NET_SRC)   
	@echo ________________________________________________________________________________
	@echo Building .NET XSD Binding VF98SoaCustomSrvTypes
	@$(CSHARP_COMPILER) build/VF98SoaCustomSrv/VF98SoaCustomSrvTypes/VF98SoaCustomSrvTypes.csproj $(VF98SOACUSTOMSRVTYPES_NETARGS) /target:Build
	$(COPY_FILE)  $(VF98SOACUSTOMSRVTYPES_NET_TARG) $(PORJECT_SOA_CLIENT)$(PS)net$(PS)libs
	@echo ________________________________________________________________________________

VF98SoaCustomSrvTypes-cs_cleanNoOp: 	
VF98SoaCustomSrvTypes-cs_clean: 
	@echo ________________________________________________________________________________
	@echo Cleaning .NET XSD Binding VF98SoaCustomSrvTypes
	@$(CSHARP_COMPILER) build/VF98SoaCustomSrv/VF98SoaCustomSrvTypes/VF98SoaCustomSrvTypes.csproj $(VF98SOACUSTOMSRVTYPES_NETARGS) /target:Clean
	-$(DELETE_FILE)  $(PORJECT_SOA_CLIENT)$(PS)net$(PS)libs$(PS)VF98SoaCustomSrvTypes.dll
	@echo ________________________________________________________________________________

