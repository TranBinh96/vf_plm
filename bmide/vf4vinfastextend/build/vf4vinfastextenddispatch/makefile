# ==================================================
# Copyright 2015
# Siemens Product Lifecycle Management Software Inc.
# All Rights Reserved.
# ==================================================
# ==================================================
#             Auto-generated source
#                 DO NOT EDIT
# ==================================================



# ==================================================
# Define macros for C++ Dispatcher Library
# ==================================================

VF4VINFASTEXTENDDISPATCH_DRV        = $(PROJECT_DRV)$(PS)vf4vinfastextenddispatch
VF4VINFASTEXTENDDISPATCH_OBJ        = $(PROJECT_OBJ)$(PS)libvf4vinfastextenddispatch
VF4VINFASTEXTENDDISPATCH_CXXS       = $(VF4VINFASTEXTENDDISPATCH_DRV)$(PS)*.cxx
VF4VINFASTEXTENDDISPATCH_CXX_FLAGS  = $(CXX_FLAGS) -DIPLIB=vf4vinfastextenddispatch
VF4VINFASTEXTENDDISPATCH_LINK_FLAGS = $(LINK_FLAGS)
VF4VINFASTEXTENDDISPATCH_INCLUDES   = -I$(BASE_PATH)$(PS)src$(PS)server -I$(PROJECT_DRV) \
                                  -I$(TC_ROOT)$(PS)include -I$(TC_ROOT)$(PS)include_cpp -I$(TC_ROOT)$(PS)aw_include_cpp
VF4VINFASTEXTENDDISPATCH_LIB_PATH   = $(LP)$(PROJECT_BIN) $(LIB_PATH)
VF4VINFASTEXTENDDISPATCH_LIBS       = $(LIB_PREFIX)foundationdispatch$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)aps0apscoredispatch$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)aws2dispatch$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)cmdispatch$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)materialmgmtdispatch$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)vendormanagementdispatch$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)activeworkspacebomdispatch$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)issuemgmtdispatch$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)qam0qualityactionmgmtdispatch$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)sc0suppliercollaborationdispatch$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)subscmpldispatch$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)cm1cmawsdispatch$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)caw0capaonawcdispatch$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)cfg0configuratordispatch$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)cpa0capadatamodeldispatch$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)sci0imdsintegrationdispatch$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)smc0psmcfgsupportdispatch$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)VF4_VinFastExtend$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)metaframework$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)tccore$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)tc$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)cxpom$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)form$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)epm$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)qry$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)sub_mgr$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)bom$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)cfm$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)sa$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)ae$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)pom$(LIB_SUFFIX) $(LIB_PREFIX)mld$(LIB_SUFFIX)  $(LIB_PREFIX)base_utils$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)extensionframework$(LIB_SUFFIX) $(SYSLIBS)
 
                                                                   
VF4VINFASTEXTENDDISPATCH_LD_FLAGS   = $(VF4VINFASTEXTENDDISPATCH_LINK_FLAGS) $(VF4VINFASTEXTENDDISPATCH_LIB_PATH)
VF4VINFASTEXTENDDISPATCH_TARGET     = $(PROJECT_BIN)$(PS)libvf4vinfastextenddispatch




vf4vinfastextenddispatch_all:   $(VF4VINFASTEXTENDDISPATCH_OBJ)  $(VF4VINFASTEXTENDDISPATCH_TARGET).$(LIB_EXT)


$(VF4VINFASTEXTENDDISPATCH_OBJ) :
	@echo Creating: $(VF4VINFASTEXTENDDISPATCH_OBJ)
	-@$(MAKE_DIR) $(VF4VINFASTEXTENDDISPATCH_OBJ)

$(VF4VINFASTEXTENDDISPATCH_CXXS) :


# ==================================================
# Windows Build Target
# ==================================================
$(VF4VINFASTEXTENDDISPATCH_TARGET).dll : $(VF4VINFASTEXTENDDISPATCH_CXXS)
	@echo ________________________________________________________________________________
	@echo Building C++ Dispatcher vf4vinfastextenddispatch
	@echo Compiling Source...
	@$(COMPILE)  $(VF4VINFASTEXTENDDISPATCH_CXX_FLAGS) $(VF4VINFASTEXTENDDISPATCH_INCLUDES) -c $(**)   $(CARG_OUT)$(VF4VINFASTEXTENDDISPATCH_OBJ)$(PS)
	@echo.
	@$(LINK) $(VF4VINFASTEXTENDDISPATCH_LD_FLAGS) $(VF4VINFASTEXTENDDISPATCH_OBJ)$(PS)*.$(OBJ_EXT)  $(VF4VINFASTEXTENDDISPATCH_LIBS)  $(LARG_OUT)$(VF4VINFASTEXTENDDISPATCH_TARGET).dll
	$(COPY_FILE)  $(PROJECT_BIN)$(PS)libvf4vinfastextenddispatch.dll $(PROJECT_TC)$(PS)bin
	$(COPY_FILE)  $(PROJECT_BIN)$(PS)libvf4vinfastextenddispatch.lib $(PROJECT_TC)$(PS)lib
	@echo ________________________________________________________________________________


	
# ==================================================
# UNIX Build Target
# ==================================================
$(VF4VINFASTEXTENDDISPATCH_TARGET).so : $(VF4VINFASTEXTENDDISPATCH_CXXS)
	@echo ________________________________________________________________________________
	@echo Building C++ Dispatcher vf4vinfastextenddispatch
	@# The compile output goes to the current directory (project root), so remove any .o files
	@# that may have been left here from previous failed builds. Then after the compile move all .o
	@# files to the the desired obj folder.
	@-rm *.$(OBJ_EXT)
	@echo Compiling $(VF4VINFASTEXTENDDISPATCH_CXXS)
	@$(COMPILE)  $(VF4VINFASTEXTENDDISPATCH_CXX_FLAGS) $(VF4VINFASTEXTENDDISPATCH_INCLUDES) -c $(VF4VINFASTEXTENDDISPATCH_CXXS) 
	@mv *.$(OBJ_EXT) $(VF4VINFASTEXTENDDISPATCH_OBJ)
	@echo Linking $(VF4VINFASTEXTENDDISPATCH_TARGET).so
	@$(LINK) $(VF4VINFASTEXTENDDISPATCH_LD_FLAGS) $(VF4VINFASTEXTENDDISPATCH_OBJ)$(PS)*.$(OBJ_EXT)  $(VF4VINFASTEXTENDDISPATCH_LIBS)  $(LARG_OUT) $(VF4VINFASTEXTENDDISPATCH_TARGET).so
	$(COPY_FILE)  $(PROJECT_BIN)$(PS)libvf4vinfastextenddispatch.so $(PROJECT_TC)$(PS)lib
	@echo ________________________________________________________________________________


vf4vinfastextenddispatch_clean:
	@echo ________________________________________________________________________________
	@echo Cleaning C++ Dispatcher vf4vinfastextenddispatch
	-$(DELETE_DIR)  $(VF4VINFASTEXTENDDISPATCH_OBJ)
	-$(DELETE_DIR)  $(VF4VINFASTEXTENDDISPATCH_DRV)
	-$(DELETE_FILE) $(PROJECT_BIN)$(PS)libvf4vinfastextenddispatch.*	
	-$(DELETE_FILE) $(PROJECT_TC)$(PS)bin$(PS)libvf4vinfastextenddispatch.dll
	-$(DELETE_FILE) $(PROJECT_TC)$(PS)lib$(PS)libvf4vinfastextenddispatch.lib	
	-$(DELETE_FILE) $(PROJECT_TC)$(PS)lib$(PS)libvf4vinfastextenddispatch.so	
	@echo ________________________________________________________________________________

