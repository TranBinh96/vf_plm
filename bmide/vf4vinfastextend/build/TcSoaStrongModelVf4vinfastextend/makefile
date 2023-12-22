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
# Define macros for the Client Data Model library
# ==================================================
LIBTCSOAMODELVF4VINFASTEXTEND_CLS       = $(PROJECT_CLS)$(PS)TcSoaStrongModelVf4vinfastextend
LIBTCSOAMODELVF4VINFASTEXTEND_ANTARGS   = -Dsoa.client.home=$(SOA_CLIENT_KIT)        -Dtoolbox.home=$(TOOLBOX_HOME)   \
                                 -Dtemplate.home=$(TEMPLATE_HOME)           -Dgen.src.home=$(PROJECT_DRV)  \
                                 -Dclasses.home=$(LIBTCSOAMODELVF4VINFASTEXTEND_CLS) -Dplatform=$(PLAT) \
                                 -Djars.home=$(PROJECT_JAR)                 -Djavac.debug=$(JAVAC_DEBUG) \
                                 -Dproject.soa.client=$(PROJECT_SOA_CLIENT) -Dproject.tc=$(PROJECT_TC) \
                                 -Dteamcenter.home=$(TC_ROOT)               -Dscript.ext=$(SCRIPT_EXT)
LIBTCSOAMODELVF4VINFASTEXTEND_NETARGS   = /property:Configuration=$(CONFIG),CHSARP_HOME=$(CHSARP_HOME),PROJECT_DRV=$(PROJECT_DRV),PROJECT_NET=$(PROJECT_NET),PROJECT_IOD=$(PROJECT_IOD),SOA_CLIENT_KIT=$(SOA_CLIENT_KIT)
LIBTCSOAMODELVF4VINFASTEXTEND_AUTO_SRC  = $(PROJECT_HOME)$(PS)extensions$(PS)*.xml
LIBTCSOAMODELVF4VINFASTEXTEND_AUTO_TARG = $(PROJECT_DRV)$(PS)TcSoaStrongModelVf4vinfastextend$(PS)touch.txt
LIBTCSOAMODELVF4VINFASTEXTEND_JAVA_TARG = $(PROJECT_JAR)$(PS)TcSoaStrongModelVf4vinfastextend_13000.3.0.jar
LIBTCSOAMODELVF4VINFASTEXTEND_NET_TARG  = $(PROJECT_NET)$(PS)TcSoaStrongModelVf4vinfastextend.dll



# ==================================================
# Build targets for C++ Client Data Model
# ==================================================
include build/TcSoaStrongModelVf4vinfastextend/libtcsoamodelvf4vinfastextend.makeInc
include build/TcSoaStrongModelVf4vinfastextend/libtcsoamodelvf4vinfastextendmngd.makeInc


TcSoaStrongModelVf4vinfastextend_all:    TcSoaStrongModelVf4vinfastextendAutoGen-build_all   TcSoaStrongModelVf4vinfastextend-build_all$(JAVA_STRONG_ENABLED)   libtcsoamodelvf4vinfastextend_all$(CPP_STRONG_ENABLED)   libtcsoamodelvf4vinfastextendmngd_all$(CPP_STRONG_ENABLED)   TcSoaStrongModelVf4vinfastextend-cs_all$(NET_STRONG_ENABLED)  
TcSoaStrongModelVf4vinfastextend_clean:  TcSoaStrongModelVf4vinfastextendAutoGen-build_clean TcSoaStrongModelVf4vinfastextend-build_clean$(JAVA_STRONG_ENABLED) libtcsoamodelvf4vinfastextend_clean$(CPP_STRONG_ENABLED) libtcsoamodelvf4vinfastextendmngd_clean$(CPP_STRONG_ENABLED) TcSoaStrongModelVf4vinfastextend-cs_clean$(NET_STRONG_ENABLED)



# ==================================================
# Build targets for Auto-Generation of Client Data Model
# ==================================================
TcSoaStrongModelVf4vinfastextendAutoGen-build_all:  $(LIBTCSOAMODELVF4VINFASTEXTEND_AUTO_TARG)

$(LIBTCSOAMODELVF4VINFASTEXTEND_AUTO_TARG) : $(LIBTCSOAMODELVF4VINFASTEXTEND_AUTO_SRC)
	@echo ________________________________________________________________________________
	@echo Auto Generation of Source for Client Data Model Library TcSoaStrongModelVf4vinfastextend
	@SET CLASSPATH=$(TOOLBOX_HOME)/xerces_j/xercesImpl.jar$(PD)$(TOOLBOX_HOME)/xerces_j/xml-apis.jar
	@$(ANT) -file build/TcSoaStrongModelVf4vinfastextend/TcSoaStrongModelVf4vinfastextendAutoGen-build.xml $(LIBTCSOAMODELVF4VINFASTEXTEND_ANTARGS) all
	@echo ________________________________________________________________________________



	
TcSoaStrongModelVf4vinfastextendAutoGen-build_clean:
	@echo ________________________________________________________________________________
	@echo Cleaning Auto Generated Source of Client Data Model Library TcSoaStrongModelVf4vinfastextend
	@$(ANT) -file  build/TcSoaStrongModelVf4vinfastextend/TcSoaStrongModelVf4vinfastextendAutoGen-build.xml $(LIBTCSOAMODELVF4VINFASTEXTEND_ANTARGS) clean
	@echo ________________________________________________________________________________


# ==================================================
# Build targets for Java Client Data Model
# ==================================================
TcSoaStrongModelVf4vinfastextend-build_allNoOp:
TcSoaStrongModelVf4vinfastextend-build_all: $(LIBTCSOAMODELVF4VINFASTEXTEND_JAVA_TARG)


 
$(LIBTCSOAMODELVF4VINFASTEXTEND_JAVA_TARG) :  $(LIBTCSOAMODELVF4VINFASTEXTEND_AUTO_TARG)  
	@echo ________________________________________________________________________________
	@echo Building Java Client Data Model TcSoaStrongModelVf4vinfastextend
	@$(ANT) -file build/TcSoaStrongModelVf4vinfastextend/TcSoaStrongModelVf4vinfastextend-build.xml $(LIBTCSOAMODELVF4VINFASTEXTEND_ANTARGS) all
	$(COPY_FILE) $(LIBTCSOAMODELVF4VINFASTEXTEND_JAVA_TARG) $(PROJECT_SOA_CLIENT)$(PS)java$(PS)libs
	@echo ________________________________________________________________________________



TcSoaStrongModelVf4vinfastextend-build_cleanNoOp:
TcSoaStrongModelVf4vinfastextend-build_clean:
	@echo ________________________________________________________________________________
	@echo Cleaning Java Client Data Model TcSoaStrongModelVf4vinfastextend
	@$(ANT) -file  build/TcSoaStrongModelVf4vinfastextend/TcSoaStrongModelVf4vinfastextend-build.xml $(LIBTCSOAMODELVF4VINFASTEXTEND_ANTARGS) clean
	-$(DELETE_FILE)  $(PROJECT_SOA_CLIENT)$(PS)java$(PS)libs$(PS)TcSoaStrongModelVf4vinfastextend_13000.3.0.jar
	@echo ________________________________________________________________________________



# ==================================================
# Build targets for .NET Client Data Model
# ==================================================
TcSoaStrongModelVf4vinfastextend-cs_allNoOp:
TcSoaStrongModelVf4vinfastextend-cs_all: $(LIBTCSOAMODELVF4VINFASTEXTEND_NET_TARG)


$(LIBTCSOAMODELVF4VINFASTEXTEND_NET_TARG) : $(LIBTCSOAMODELVF4VINFASTEXTEND_AUTO_TARG)   
	@echo ________________________________________________________________________________
	@echo Building .NET Client Data Model TcSoaStrongModelVf4vinfastextend
	@$(CSHARP_COMPILER) build/TcSoaStrongModelVf4vinfastextend/TcSoaStrongModelVf4vinfastextend.csproj $(LIBTCSOAMODELVF4VINFASTEXTEND_NETARGS) /target:Build
	$(COPY_FILE)  $(LIBTCSOAMODELVF4VINFASTEXTEND_NET_TARG) $(PROJECT_SOA_CLIENT)$(PS)net$(PS)libs
	@echo ________________________________________________________________________________

TcSoaStrongModelVf4vinfastextend-cs_cleanNoOp: 
TcSoaStrongModelVf4vinfastextend-cs_clean: 
	@echo ________________________________________________________________________________
	@echo Cleaning .NET Client Data Model TcSoaStrongModelVf4vinfastextend
	@$(CSHARP_COMPILER) build/TcSoaStrongModelVf4vinfastextend/TcSoaStrongModelVf4vinfastextend.csproj $(LIBTCSOAMODELVF4VINFASTEXTEND_NETARGS) /target:Clean
	-$(DELETE_FILE)  $(PROJECT_SOA_CLIENT)$(PS)net$(PS)libs$(PS)TcSoaStrongModelVf4vinfastextend.dll
	@echo ________________________________________________________________________________


