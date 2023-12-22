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
# Define macros for C++ Service implementation
# ==================================================
LIBVF98SOACUSTOMSRV_SRC        = $(PROJECT_HOME)$(PS)src$(PS)server$(PS)VF98SoaCustomSrv
LIBVF98SOACUSTOMSRV_DRV        = $(PROJECT_DRV)$(PS)VF98SoaCustomSrv
LIBVF98SOACUSTOMSRV_OBJ        = $(PROJECT_OBJ)$(PS)libvf98soacustomsrv
LIBVF98SOACUSTOMSRV_CXXS       = $(LIBVF98SOACUSTOMSRV_SRC)$(PS)*.cxx $(LIBVF98SOACUSTOMSRV_DRV)$(PS)*.cxx
LIBVF98SOACUSTOMSRV_CXX_FLAGS  = $(CXX_FLAGS) -DIPLIB=libvf98soacustomsrv
LIBVF98SOACUSTOMSRV_LINK_FLAGS = $(LINK_FLAGS)
LIBVF98SOACUSTOMSRV_INCLUDES   = $(CPP_INCLUDES) -I$(LIBVF98SOACUSTOMSRV_SRC) -I$(LIBVF98SOACUSTOMSRV_DRV) -I$(PROJECT_DRV) \
                                  -I$(TC_ROOT)$(PS)include -I$(TC_ROOT)$(PS)include_cpp -I$(TC_ROOT)$(PS)aw_include_cpp \
                                  -I$(TOOLBOX_HOME)$(PS)TCGATEWAYSRC \
                                  
LIBVF98SOACUSTOMSRV_LIB_PATH   = $(LP)$(PROJECT_BIN)  $(LIB_PATH)
LIBVF98SOACUSTOMSRV_LIBS       = $(SYSLIBS)  \
                                  $(LIB_PREFIX)tcsoacommon$(LIB_SUFFIX)  $(LIB_PREFIX)tcsoaserver$(LIB_SUFFIX)   $(LIB_PREFIX)pom$(LIB_SUFFIX)  \
                                  $(LIB_PREFIX)itk$(LIB_SUFFIX)          $(LIB_PREFIX)fclasses$(LIB_SUFFIX)      $(LIB_PREFIX)metaframework$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)tccore$(LIB_SUFFIX)       $(LIB_PREFIX)tc$(LIB_SUFFIX)            $(LIB_PREFIX)cxpom$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)base_utils$(LIB_SUFFIX)   $(LIB_PREFIX)mld$(LIB_SUFFIX)
LIBVF98SOACUSTOMSRV_LD_FLAGS   = $(LIBVF98SOACUSTOMSRV_LINK_FLAGS) $(LIBVF98SOACUSTOMSRV_LIB_PATH)
LIBVF98SOACUSTOMSRV_TARGET     = $(PROJECT_BIN)$(PS)libvf98soacustomsrv



libvf98soacustomsrv_all:   $(LIBVF98SOACUSTOMSRV_OBJ)  $(LIBVF98SOACUSTOMSRV_TARGET).$(LIB_EXT)


$(LIBVF98SOACUSTOMSRV_OBJ) :
	@echo Creating: $(LIBVF98SOACUSTOMSRV_OBJ)
	-@$(MAKE_DIR) $(LIBVF98SOACUSTOMSRV_OBJ)

$(LIBVF98SOACUSTOMSRV_CXXS) :


# ==================================================
# Windows Build Target
# ==================================================
$(LIBVF98SOACUSTOMSRV_TARGET).dll : $(LIBVF98SOACUSTOMSRV_CXXS)
	@echo ________________________________________________________________________________
	@echo Building C++ Server libvf98soacustomsrv
	@echo Compiling Source...
	@$(COMPILE)  $(LIBVF98SOACUSTOMSRV_CXX_FLAGS) $(LIBVF98SOACUSTOMSRV_INCLUDES) -c $(**)   $(CARG_OUT)$(LIBVF98SOACUSTOMSRV_OBJ)$(PS)
	@echo.
	@$(LINK) $(LIBVF98SOACUSTOMSRV_LD_FLAGS) $(LIBVF98SOACUSTOMSRV_OBJ)$(PS)*.$(OBJ_EXT)  $(LIBVF98SOACUSTOMSRV_LIBS)  $(LARG_OUT)$(LIBVF98SOACUSTOMSRV_TARGET).dll
	$(COPY_FILE)  $(PROJECT_BIN)$(PS)libvf98soacustomsrv.dll $(PROJECT_TC)$(PS)bin
	$(COPY_FILE)  $(PROJECT_BIN)$(PS)libvf98soacustomsrv.lib $(PROJECT_TC)$(PS)lib
	@echo ________________________________________________________________________________


	
# ==================================================
# UNIX Build Target
# ==================================================
$(LIBVF98SOACUSTOMSRV_TARGET).so : $(LIBVF98SOACUSTOMSRV_CXXS)
	@echo ________________________________________________________________________________
	@echo Building C++ Server libvf98soacustomsrv
	@# The compile output goes to the current directory (project root), so remove any .o files
	@# that may have been left here from previous failed builds. Then after the compile move all .o
	@# files to the the desired obj folder.
	@-rm *.$(OBJ_EXT)
	@echo Compiling $(LIBVF98SOACUSTOMSRV_CXXS)
	@$(COMPILE)  $(LIBVF98SOACUSTOMSRV_CXX_FLAGS) $(LIBVF98SOACUSTOMSRV_INCLUDES) -c $(LIBVF98SOACUSTOMSRV_CXXS) 
	@mv *.$(OBJ_EXT) $(LIBVF98SOACUSTOMSRV_OBJ)
	@echo Linking $(LIBVF98SOACUSTOMSRV_TARGET).so
	@$(LINK) $(LIBVF98SOACUSTOMSRV_LD_FLAGS) $(LIBVF98SOACUSTOMSRV_OBJ)$(PS)*.$(OBJ_EXT)  $(LIBVF98SOACUSTOMSRV_LIBS)  $(LARG_OUT) $(LIBVF98SOACUSTOMSRV_TARGET).so
	$(COPY_FILE)  $(PROJECT_BIN)$(PS)libvf98soacustomsrv.so $(PROJECT_TC)$(PS)lib
	@echo ________________________________________________________________________________


libvf98soacustomsrv_clean:
	@echo ________________________________________________________________________________
	@echo Cleaning C++ Server libvf98soacustomsrv
	-$(DELETE_DIR)  $(LIBVF98SOACUSTOMSRV_OBJ)
	-$(DELETE_FILE) $(PROJECT_BIN)$(PS)libvf98soacustomsrv.*	
	-$(DELETE_FILE) $(PROJECT_TC)$(PS)bin$(PS)libvf98soacustomsrv.dll
	-$(DELETE_FILE) $(PROJECT_TC)$(PS)lib$(PS)libvf98soacustomsrv.lib	
	-$(DELETE_FILE) $(PROJECT_TC)$(PS)lib$(PS)libvf98soacustomsrv.so	
	@echo ________________________________________________________________________________

