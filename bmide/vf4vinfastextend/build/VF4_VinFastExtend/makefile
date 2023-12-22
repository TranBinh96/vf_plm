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
VF4_VINFASTEXTEND_SRC        = $(BASE_PATH)$(PS)src$(PS)server$(PS)VF4_VinFastExtend
VF4_VINFASTEXTEND_DRV        = $(PROJECT_DRV)$(PS)VF4_VinFastExtend
VF4_VINFASTEXTEND_OBJ        = $(PROJECT_OBJ)$(PS)libvf4_vinfastextend
VF4_VINFASTEXTEND_CXXS       = $(VF4_VINFASTEXTEND_SRC)$(PS)*.c* $(VF4_VINFASTEXTEND_DRV)$(PS)*.cxx
VF4_VINFASTEXTEND_CXX_FLAGS  = $(CXX_FLAGS) -DIPLIB=vf4_vinfastextend
VF4_VINFASTEXTEND_LINK_FLAGS = $(LINK_FLAGS)
VF4_VINFASTEXTEND_INCLUDES   = -I$(BASE_PATH)$(PS)src$(PS)server -I$(PROJECT_DRV) \
                                  -I$(TC_ROOT)$(PS)include -I$(TC_ROOT)$(PS)include_cpp -I$(TC_ROOT)$(PS)aw_include_cpp \
                                  
VF4_VINFASTEXTEND_LIB_PATH   = $(LP)$(PROJECT_BIN)  $(LIB_PATH) 
VF4_VINFASTEXTEND_LIBS       = $(LIB_PREFIX)metaframework$(LIB_SUFFIX) \
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
                                  $(LIB_PREFIX)pom$(LIB_SUFFIX)  $(LIB_PREFIX)fclasses$(LIB_SUFFIX)  $(LIB_PREFIX)itk$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)extensionframework$(LIB_SUFFIX) $(LIB_PREFIX)mld$(LIB_SUFFIX)  $(LIB_PREFIX)base_utils$(LIB_SUFFIX) $(SYSLIBS)
                                                                   
VF4_VINFASTEXTEND_LD_FLAGS   = $(VF4_VINFASTEXTEND_LINK_FLAGS) $(VF4_VINFASTEXTEND_LIB_PATH)
VF4_VINFASTEXTEND_TARGET     = $(PROJECT_BIN)$(PS)libVF4_VinFastExtend
VF4_VINFASTEXTEND_AUTO_TARG  = $(VF4_VINFASTEXTEND_DRV)$(PS)libvf4_vinfastextendinitializemodule.cxx
VF4_VINFASTEXTEND_AUTO_SRC   = $(PROJECT_HOME)$(PS)extensions$(PS)*.xml

VF4_VinFastExtend_all:   $(VF4_VINFASTEXTEND_AUTO_TARG) $(VF4_VINFASTEXTEND_OBJ)  $(VF4_VINFASTEXTEND_TARGET).$(LIB_EXT)

$(VF4_VINFASTEXTEND_AUTO_TARG) : $(VF4_VINFASTEXTEND_AUTO_SRC)
	@echo ________________________________________________________________________________
	@echo Auto Generation of business object classes for VF4_VinFastExtend
	$(TC_ROOT)$(PS)bin$(PS)bmide_generatecode$(SCRIPT_EXT) -templateProj=$(PROJECT_HOME) -templateDeps=$(TEMPLATE_HOME) -gensrcDir=$(PROJECT_DRV) -srcDir=$(BASE_PATH)$(PS)src$(PS)server -log=$(BASE_PATH)$(PS)output$(PS)generate.log
	@echo ________________________________________________________________________________

$(VF4_VINFASTEXTEND_OBJ) :
	@echo Creating: $(VF4_VINFASTEXTEND_OBJ)
	-@$(MAKE_DIR) $(VF4_VINFASTEXTEND_OBJ)

$(VF4_VINFASTEXTEND_CXXS) :


# ==================================================
# Windows Build Target
# ==================================================
$(VF4_VINFASTEXTEND_TARGET).dll : $(VF4_VINFASTEXTEND_CXXS)
	@echo ________________________________________________________________________________
	@echo Building C++ Server VF4_VinFastExtend
	@echo Compiling Source...
	@$(COMPILE)  $(VF4_VINFASTEXTEND_CXX_FLAGS) $(VF4_VINFASTEXTEND_INCLUDES) -c $(**)   $(CARG_OUT)$(VF4_VINFASTEXTEND_OBJ)$(PS)
	@echo.
	@$(LINK) $(VF4_VINFASTEXTEND_LD_FLAGS) $(VF4_VINFASTEXTEND_OBJ)$(PS)*.$(OBJ_EXT)  $(VF4_VINFASTEXTEND_LIBS)  $(LARG_OUT)$(VF4_VINFASTEXTEND_TARGET).dll
	$(COPY_FILE)  $(PROJECT_BIN)$(PS)libVF4_VinFastExtend.dll $(PROJECT_TC)$(PS)bin
	$(COPY_FILE)  $(PROJECT_BIN)$(PS)libVF4_VinFastExtend.lib $(PROJECT_TC)$(PS)lib
	@echo ________________________________________________________________________________


	
# ==================================================
# UNIX Build Target
# ==================================================
$(VF4_VINFASTEXTEND_TARGET).so : $(VF4_VINFASTEXTEND_CXXS)
	@echo ________________________________________________________________________________
	@echo Building C++ Server VF4_VinFastExtend
	@# The compile output goes to the current directory (project root), so remove any .o files
	@# that may have been left here from previous failed builds. Then after the compile move all .o
	@# files to the the desired obj folder.
	@-rm *.$(OBJ_EXT)
	@echo Compiling $(VF4_VINFASTEXTEND_CXXS)
	@$(COMPILE)  $(VF4_VINFASTEXTEND_CXX_FLAGS) $(VF4_VINFASTEXTEND_INCLUDES) -c $(VF4_VINFASTEXTEND_CXXS) 
	@mv *.$(OBJ_EXT) $(VF4_VINFASTEXTEND_OBJ)
	@echo Linking $(VF4_VINFASTEXTEND_TARGET).so
	@$(LINK) $(VF4_VINFASTEXTEND_LD_FLAGS) $(VF4_VINFASTEXTEND_OBJ)$(PS)*.$(OBJ_EXT)  $(VF4_VINFASTEXTEND_LIBS)  $(LARG_OUT) $(VF4_VINFASTEXTEND_TARGET).so
	$(COPY_FILE)  $(PROJECT_BIN)$(PS)libVF4_VinFastExtend.so $(PROJECT_TC)$(PS)lib
	@echo ________________________________________________________________________________


VF4_VinFastExtend_clean:
	@echo ________________________________________________________________________________
	@echo Cleaning C++ Server VF4_VinFastExtend
	-$(DELETE_DIR)  $(VF4_VINFASTEXTEND_OBJ)
	-$(DELETE_DIR)  $(VF4_VINFASTEXTEND_DRV)
	-$(DELETE_FILE) $(PROJECT_BIN)$(PS)libVF4_VinFastExtend.*	
	-$(DELETE_FILE) $(PROJECT_TC)$(PS)bin$(PS)libVF4_VinFastExtend.dll
	-$(DELETE_FILE) $(PROJECT_TC)$(PS)lib$(PS)libVF4_VinFastExtend.lib	
	-$(DELETE_FILE) $(PROJECT_TC)$(PS)lib$(PS)libVF4_VinFastExtend.so	
	@echo ________________________________________________________________________________

