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

VF6CMDISPATCH_DRV        = $(PROJECT_DRV)$(PS)vf6cmdispatch
VF6CMDISPATCH_OBJ        = $(PROJECT_OBJ)$(PS)libvf6cmdispatch
VF6CMDISPATCH_CXXS       = $(VF6CMDISPATCH_DRV)$(PS)*.cxx
VF6CMDISPATCH_CXX_FLAGS  = $(CXX_FLAGS) -DIPLIB=vf6cmdispatch
VF6CMDISPATCH_LINK_FLAGS = $(LINK_FLAGS)
VF6CMDISPATCH_INCLUDES   = -I$(BASE_PATH)$(PS)src$(PS)server -I$(PROJECT_DRV) \
                                  -I$(TC_ROOT)$(PS)include -I$(TC_ROOT)$(PS)include_cpp -I$(TC_ROOT)$(PS)aw_include_cpp
VF6CMDISPATCH_LIB_PATH   = $(LP)$(PROJECT_BIN) $(LIB_PATH)
VF6CMDISPATCH_LIBS       = $(LIB_PREFIX)foundationdispatch$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)cmdispatch$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)Vf6_custom_lib$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)metaframework$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)tccore$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)tc$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)cxpom$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)ecm$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)cm$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)epm$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)pom$(LIB_SUFFIX) $(LIB_PREFIX)mld$(LIB_SUFFIX)  $(LIB_PREFIX)base_utils$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)extensionframework$(LIB_SUFFIX) $(SYSLIBS)
 
                                                                   
VF6CMDISPATCH_LD_FLAGS   = $(VF6CMDISPATCH_LINK_FLAGS) $(VF6CMDISPATCH_LIB_PATH)
VF6CMDISPATCH_TARGET     = $(PROJECT_BIN)$(PS)libvf6cmdispatch




vf6cmdispatch_all:   $(VF6CMDISPATCH_OBJ)  $(VF6CMDISPATCH_TARGET).$(LIB_EXT)


$(VF6CMDISPATCH_OBJ) :
	@echo Creating: $(VF6CMDISPATCH_OBJ)
	-@$(MAKE_DIR) $(VF6CMDISPATCH_OBJ)

$(VF6CMDISPATCH_CXXS) :


# ==================================================
# Windows Build Target
# ==================================================
$(VF6CMDISPATCH_TARGET).dll : $(VF6CMDISPATCH_CXXS)
	@echo ________________________________________________________________________________
	@echo Building C++ Dispatcher vf6cmdispatch
	@echo Compiling Source...
	@$(COMPILE)  $(VF6CMDISPATCH_CXX_FLAGS) $(VF6CMDISPATCH_INCLUDES) -c $(**)   $(CARG_OUT)$(VF6CMDISPATCH_OBJ)$(PS)
	@echo.
	@$(LINK) $(VF6CMDISPATCH_LD_FLAGS) $(VF6CMDISPATCH_OBJ)$(PS)*.$(OBJ_EXT)  $(VF6CMDISPATCH_LIBS)  $(LARG_OUT)$(VF6CMDISPATCH_TARGET).dll
	$(COPY_FILE)  $(PROJECT_BIN)$(PS)libvf6cmdispatch.dll $(PROJECT_TC)$(PS)bin
	$(COPY_FILE)  $(PROJECT_BIN)$(PS)libvf6cmdispatch.lib $(PROJECT_TC)$(PS)lib
	@echo ________________________________________________________________________________


	
# ==================================================
# UNIX Build Target
# ==================================================
$(VF6CMDISPATCH_TARGET).so : $(VF6CMDISPATCH_CXXS)
	@echo ________________________________________________________________________________
	@echo Building C++ Dispatcher vf6cmdispatch
	@# The compile output goes to the current directory (project root), so remove any .o files
	@# that may have been left here from previous failed builds. Then after the compile move all .o
	@# files to the the desired obj folder.
	@-rm *.$(OBJ_EXT)
	@echo Compiling $(VF6CMDISPATCH_CXXS)
	@$(COMPILE)  $(VF6CMDISPATCH_CXX_FLAGS) $(VF6CMDISPATCH_INCLUDES) -c $(VF6CMDISPATCH_CXXS) 
	@mv *.$(OBJ_EXT) $(VF6CMDISPATCH_OBJ)
	@echo Linking $(VF6CMDISPATCH_TARGET).so
	@$(LINK) $(VF6CMDISPATCH_LD_FLAGS) $(VF6CMDISPATCH_OBJ)$(PS)*.$(OBJ_EXT)  $(VF6CMDISPATCH_LIBS)  $(LARG_OUT) $(VF6CMDISPATCH_TARGET).so
	$(COPY_FILE)  $(PROJECT_BIN)$(PS)libvf6cmdispatch.so $(PROJECT_TC)$(PS)lib
	@echo ________________________________________________________________________________


vf6cmdispatch_clean:
	@echo ________________________________________________________________________________
	@echo Cleaning C++ Dispatcher vf6cmdispatch
	-$(DELETE_DIR)  $(VF6CMDISPATCH_OBJ)
	-$(DELETE_DIR)  $(VF6CMDISPATCH_DRV)
	-$(DELETE_FILE) $(PROJECT_BIN)$(PS)libvf6cmdispatch.*	
	-$(DELETE_FILE) $(PROJECT_TC)$(PS)bin$(PS)libvf6cmdispatch.dll
	-$(DELETE_FILE) $(PROJECT_TC)$(PS)lib$(PS)libvf6cmdispatch.lib	
	-$(DELETE_FILE) $(PROJECT_TC)$(PS)lib$(PS)libvf6cmdispatch.so	
	@echo ________________________________________________________________________________

