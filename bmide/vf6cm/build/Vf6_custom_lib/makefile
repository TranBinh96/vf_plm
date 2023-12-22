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
# Define macros for C++ Server Library
# ==================================================
VF6_CUSTOM_LIB_SRC        = $(BASE_PATH)$(PS)src$(PS)server$(PS)Vf6_custom_lib
VF6_CUSTOM_LIB_DRV        = $(PROJECT_DRV)$(PS)Vf6_custom_lib
VF6_CUSTOM_LIB_OBJ        = $(PROJECT_OBJ)$(PS)libvf6_custom_lib
VF6_CUSTOM_LIB_CXXS       = $(VF6_CUSTOM_LIB_SRC)$(PS)*.c* $(VF6_CUSTOM_LIB_DRV)$(PS)*.cxx
VF6_CUSTOM_LIB_CXX_FLAGS  = $(CXX_FLAGS) -DIPLIB=vf6_custom_lib
VF6_CUSTOM_LIB_LINK_FLAGS = $(LINK_FLAGS)
VF6_CUSTOM_LIB_INCLUDES   = -I$(BASE_PATH)$(PS)src$(PS)server -I$(PROJECT_DRV) \
                                  -I$(TC_ROOT)$(PS)include -I$(TC_ROOT)$(PS)include_cpp -I$(TC_ROOT)$(PS)aw_include_cpp \
                                  
VF6_CUSTOM_LIB_LIB_PATH   = $(LP)$(PROJECT_BIN)  $(LIB_PATH) 
VF6_CUSTOM_LIB_LIBS       = $(LIB_PREFIX)metaframework$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)tccore$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)tc$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)cxpom$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)ecm$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)cm$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)epm$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)pom$(LIB_SUFFIX)  $(LIB_PREFIX)fclasses$(LIB_SUFFIX)  $(LIB_PREFIX)itk$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)extensionframework$(LIB_SUFFIX) $(LIB_PREFIX)mld$(LIB_SUFFIX)  $(LIB_PREFIX)base_utils$(LIB_SUFFIX) $(SYSLIBS)
                                                                   
VF6_CUSTOM_LIB_LD_FLAGS   = $(VF6_CUSTOM_LIB_LINK_FLAGS) $(VF6_CUSTOM_LIB_LIB_PATH)
VF6_CUSTOM_LIB_TARGET     = $(PROJECT_BIN)$(PS)libVf6_custom_lib
VF6_CUSTOM_LIB_AUTO_TARG  = $(VF6_CUSTOM_LIB_DRV)$(PS)libvf6_custom_libinitializemodule.cxx
VF6_CUSTOM_LIB_AUTO_SRC   = $(PROJECT_HOME)$(PS)extensions$(PS)*.xml

Vf6_custom_lib_all:   $(VF6_CUSTOM_LIB_AUTO_TARG) $(VF6_CUSTOM_LIB_OBJ)  $(VF6_CUSTOM_LIB_TARGET).$(LIB_EXT)

$(VF6_CUSTOM_LIB_AUTO_TARG) : $(VF6_CUSTOM_LIB_AUTO_SRC)
	@echo ________________________________________________________________________________
	@echo Auto Generation of business object classes for Vf6_custom_lib
	$(TC_ROOT)$(PS)bin$(PS)bmide_generatecode$(SCRIPT_EXT) -templateProj=$(PROJECT_HOME) -templateDeps=$(TEMPLATE_HOME) -gensrcDir=$(PROJECT_DRV) -srcDir=$(BASE_PATH)$(PS)src$(PS)server -log=$(BASE_PATH)$(PS)output$(PS)generate.log
	@echo ________________________________________________________________________________

$(VF6_CUSTOM_LIB_OBJ) :
	@echo Creating: $(VF6_CUSTOM_LIB_OBJ)
	-@$(MAKE_DIR) $(VF6_CUSTOM_LIB_OBJ)

$(VF6_CUSTOM_LIB_CXXS) :


# ==================================================
# Windows Build Target
# ==================================================
$(VF6_CUSTOM_LIB_TARGET).dll : $(VF6_CUSTOM_LIB_CXXS)
	@echo ________________________________________________________________________________
	@echo Building C++ Server Vf6_custom_lib
	@echo Compiling Source...
	@$(COMPILE)  $(VF6_CUSTOM_LIB_CXX_FLAGS) $(VF6_CUSTOM_LIB_INCLUDES) -c $(**)   $(CARG_OUT)$(VF6_CUSTOM_LIB_OBJ)$(PS)
	@echo.
	@$(LINK) $(VF6_CUSTOM_LIB_LD_FLAGS) $(VF6_CUSTOM_LIB_OBJ)$(PS)*.$(OBJ_EXT)  $(VF6_CUSTOM_LIB_LIBS)  $(LARG_OUT)$(VF6_CUSTOM_LIB_TARGET).dll
	$(COPY_FILE)  $(PROJECT_BIN)$(PS)libVf6_custom_lib.dll $(PROJECT_TC)$(PS)bin
	$(COPY_FILE)  $(PROJECT_BIN)$(PS)libVf6_custom_lib.lib $(PROJECT_TC)$(PS)lib
	@echo ________________________________________________________________________________


	
# ==================================================
# UNIX Build Target
# ==================================================
$(VF6_CUSTOM_LIB_TARGET).so : $(VF6_CUSTOM_LIB_CXXS)
	@echo ________________________________________________________________________________
	@echo Building C++ Server Vf6_custom_lib
	@# The compile output goes to the current directory (project root), so remove any .o files
	@# that may have been left here from previous failed builds. Then after the compile move all .o
	@# files to the the desired obj folder.
	@-rm *.$(OBJ_EXT)
	@echo Compiling $(VF6_CUSTOM_LIB_CXXS)
	@$(COMPILE)  $(VF6_CUSTOM_LIB_CXX_FLAGS) $(VF6_CUSTOM_LIB_INCLUDES) -c $(VF6_CUSTOM_LIB_CXXS) 
	@mv *.$(OBJ_EXT) $(VF6_CUSTOM_LIB_OBJ)
	@echo Linking $(VF6_CUSTOM_LIB_TARGET).so
	@$(LINK) $(VF6_CUSTOM_LIB_LD_FLAGS) $(VF6_CUSTOM_LIB_OBJ)$(PS)*.$(OBJ_EXT)  $(VF6_CUSTOM_LIB_LIBS)  $(LARG_OUT) $(VF6_CUSTOM_LIB_TARGET).so
	$(COPY_FILE)  $(PROJECT_BIN)$(PS)libVf6_custom_lib.so $(PROJECT_TC)$(PS)lib
	@echo ________________________________________________________________________________


Vf6_custom_lib_clean:
	@echo ________________________________________________________________________________
	@echo Cleaning C++ Server Vf6_custom_lib
	-$(DELETE_DIR)  $(VF6_CUSTOM_LIB_OBJ)
	-$(DELETE_DIR)  $(VF6_CUSTOM_LIB_DRV)
	-$(DELETE_FILE) $(PROJECT_BIN)$(PS)libVf6_custom_lib.*	
	-$(DELETE_FILE) $(PROJECT_TC)$(PS)bin$(PS)libVf6_custom_lib.dll
	-$(DELETE_FILE) $(PROJECT_TC)$(PS)lib$(PS)libVf6_custom_lib.lib	
	-$(DELETE_FILE) $(PROJECT_TC)$(PS)lib$(PS)libVf6_custom_lib.so	
	@echo ________________________________________________________________________________

