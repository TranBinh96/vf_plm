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
# Define macros for WSDLs
# ==================================================
VF4VINFASTEXTENDWSDL_CLS       = $(PROJECT_CLS)$(PS)Vf4vinfastextendWsdl
VF4VINFASTEXTENDWSDL_ANTARGS   = -Dsoa.client.home=$(SOA_CLIENT_KIT)        -Dtoolbox.home=$(TOOLBOX_HOME)   \
                                 -Dtemplate.home=$(TEMPLATE_HOME)           -Dgen.src.home=$(PROJECT_DRV)  \
                                 -Dclasses.home=$(VF4VINFASTEXTENDWSDL_CLS) -Dplatform=$(PLAT) \
                                 -Djars.home=$(PROJECT_JAR)                 -Djavac.debug=$(JAVAC_DEBUG) \
                                 -Dproject.soa.client=$(PROJECT_SOA_CLIENT) -Dproject.tc=$(PROJECT_TC) 
VF4VINFASTEXTENDWSDL_NETARGS   = /property:Configuration=$(CONFIG),CHSARP_HOME=$(CHSARP_HOME),PROJECT_DRV=$(PROJECT_DRV),PROJECT_NET=$(PROJECT_NET),PROJECT_IOD=$(PROJECT_IOD),SOA_CLIENT_KIT=$(SOA_CLIENT_KIT)
VF4VINFASTEXTENDWSDL_AUTO_SRC  = $(PROJECT_HOME)$(PS)extensions$(PS)*.xml
VF4VINFASTEXTENDWSDL_AUTO_TARG = $(PROJECT_DRV)$(PS)Vf4vinfastextendWsdl$(PS)touch.txt






Vf4vinfastextendWsdl_all:    Vf4vinfastextendWsdlAutoGen-build_all$(WSDLS_ENABLED)  
Vf4vinfastextendWsdl_clean:  Vf4vinfastextendWsdlAutoGen-build_clean$(WSDLS_ENABLED)

# ==================================================
# Build targets for WSDL Auto-Generation and
# Compile of Java Web Tier artifacts
# ==================================================
Vf4vinfastextendWsdlAutoGen-build_allNoOp:
Vf4vinfastextendWsdlAutoGen-build_all: $(VF4VINFASTEXTENDWSDL_AUTO_TARG)


$(VF4VINFASTEXTENDWSDL_AUTO_TARG) : $(VF4VINFASTEXTENDWSDL_AUTO_SRC)
	@echo ________________________________________________________________________________
	@echo Auto Generation of WSDLs and web-tier endpoints Vf4vinfastextendWsdl
	@$(ANT) -file build/Vf4vinfastextendWsdl/Vf4vinfastextendWsdlAutoGen-build.xml $(VF4VINFASTEXTENDWSDL_ANTARGS) all
	$(COPY_FILE) $(PROJECT_DRV)$(PS)Vf4vinfastextendWsdl$(PS)wsdls$(PS)*.wsdl $(PROJECT_SOA_CLIENT)$(PS)wsdls
	$(COPY_FILE) $(PROJECT_DRV)$(PS)Vf4vinfastextendWsdl$(PS)wsdls$(PS)*.xsd  $(PROJECT_SOA_CLIENT)$(PS)wsdls
	@echo ________________________________________________________________________________



Vf4vinfastextendWsdlAutoGen-build_cleanNoOp:	
Vf4vinfastextendWsdlAutoGen-build_clean:
	@echo ________________________________________________________________________________
	@echo Cleaning Auto Generated  WSDLs and web-tier endpoints Vf4vinfastextendWsdl
	@$(ANT) -file  build/Vf4vinfastextendWsdl/Vf4vinfastextendWsdlAutoGen-build.xml $(VF4VINFASTEXTENDWSDL_ANTARGS) clean
	@echo ________________________________________________________________________________



