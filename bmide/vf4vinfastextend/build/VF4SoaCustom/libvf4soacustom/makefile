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
LIBVF4SOACUSTOM_SERVER     = $(PROJECT_HOME)$(PS)src$(PS)server
LIBVF4SOACUSTOM_SRC        = $(LIBVF4SOACUSTOM_SERVER)$(PS)VF4SoaCustom
LIBVF4SOACUSTOM_DRV        = $(PROJECT_DRV)$(PS)VF4SoaCustom
LIBVF4SOACUSTOM_OBJ        = $(PROJECT_OBJ)$(PS)libvf4soacustom
LIBVF4SOACUSTOM_CXXS       = $(LIBVF4SOACUSTOM_SRC)$(PS)*.cxx $(LIBVF4SOACUSTOM_DRV)$(PS)*.cxx
LIBVF4SOACUSTOM_CXX_FLAGS  = $(CXX_FLAGS) -DIPLIB=libvf4soacustom
LIBVF4SOACUSTOM_LINK_FLAGS = $(LINK_FLAGS)
LIBVF4SOACUSTOM_INCLUDES   = $(CPP_INCLUDES) -I$(LIBVF4SOACUSTOM_SRC) -I$(LIBVF4SOACUSTOM_DRV) -I$(PROJECT_DRV) -I$(LIBVF4SOACUSTOM_SERVER) \
                                  -I$(TC_ROOT)$(PS)include -I$(TC_ROOT)$(PS)include_cpp -I$(TC_ROOT)$(PS)aw_include_cpp \
                                  -I$(TOOLBOX_HOME)$(PS)TCGATEWAYSRC \
                                  
LIBVF4SOACUSTOM_LIB_PATH   = $(LP)$(PROJECT_BIN)  $(LIB_PATH)
LIBVF4SOACUSTOM_LIBS       = $(LIB_PREFIX)tc$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)tccore$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)pom$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)sa$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)metaframework$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)meta$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)qry$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)bom$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)cfm$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)epm$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)user_exits$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)me$(LIB_SUFFIX) \
                                  $(SYSLIBS)  \
                                  $(LIB_PREFIX)tcsoacommon$(LIB_SUFFIX)        $(LIB_PREFIX)tcsoaserver$(LIB_SUFFIX) $(LIB_PREFIX)pom$(LIB_SUFFIX)  \
                                  $(LIB_PREFIX)itk$(LIB_SUFFIX)                $(LIB_PREFIX)fclasses$(LIB_SUFFIX)    $(LIB_PREFIX)metaframework$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)extensionframework$(LIB_SUFFIX) $(LIB_PREFIX)tccore$(LIB_SUFFIX)      $(LIB_PREFIX)tc$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)cxpom$(LIB_SUFFIX)              $(LIB_PREFIX)base_utils$(LIB_SUFFIX)  $(LIB_PREFIX)mld$(LIB_SUFFIX)
LIBVF4SOACUSTOM_LD_FLAGS   = $(LIBVF4SOACUSTOM_LINK_FLAGS) $(LIBVF4SOACUSTOM_LIB_PATH)
LIBVF4SOACUSTOM_TARGET     = $(PROJECT_BIN)$(PS)libvf4soacustom



libvf4soacustom_all:   $(LIBVF4SOACUSTOM_OBJ)  $(LIBVF4SOACUSTOM_TARGET).$(LIB_EXT)


$(LIBVF4SOACUSTOM_OBJ) :
	@echo Creating: $(LIBVF4SOACUSTOM_OBJ)
	-@$(MAKE_DIR) $(LIBVF4SOACUSTOM_OBJ)

$(LIBVF4SOACUSTOM_CXXS) :


# ==================================================
# Windows Build Target
# ==================================================
$(LIBVF4SOACUSTOM_TARGET).dll : $(LIBVF4SOACUSTOM_CXXS)
	@echo ________________________________________________________________________________
	@echo Building C++ Server libvf4soacustom
	@echo Compiling Source...
	@$(COMPILE)  $(LIBVF4SOACUSTOM_CXX_FLAGS) $(LIBVF4SOACUSTOM_INCLUDES) -c $(**)   $(CARG_OUT)$(LIBVF4SOACUSTOM_OBJ)$(PS)
	@echo.
	@$(LINK) $(LIBVF4SOACUSTOM_LD_FLAGS) $(LIBVF4SOACUSTOM_OBJ)$(PS)*.$(OBJ_EXT)  $(LIBVF4SOACUSTOM_LIBS)  $(LARG_OUT)$(LIBVF4SOACUSTOM_TARGET).dll
	$(COPY_FILE)  $(PROJECT_BIN)$(PS)libvf4soacustom.dll $(PROJECT_TC)$(PS)bin
	$(COPY_FILE)  $(PROJECT_BIN)$(PS)libvf4soacustom.lib $(PROJECT_TC)$(PS)lib
	@echo ________________________________________________________________________________


	
# ==================================================
# UNIX Build Target
# ==================================================
$(LIBVF4SOACUSTOM_TARGET).so : $(LIBVF4SOACUSTOM_CXXS)
	@echo ________________________________________________________________________________
	@echo Building C++ Server libvf4soacustom
	@# The compile output goes to the current directory (project root), so remove any .o files
	@# that may have been left here from previous failed builds. Then after the compile move all .o
	@# files to the the desired obj folder.
	@-rm *.$(OBJ_EXT)
	@echo Compiling $(LIBVF4SOACUSTOM_CXXS)
	@$(COMPILE)  $(LIBVF4SOACUSTOM_CXX_FLAGS) $(LIBVF4SOACUSTOM_INCLUDES) -c $(LIBVF4SOACUSTOM_CXXS) 
	@mv *.$(OBJ_EXT) $(LIBVF4SOACUSTOM_OBJ)
	@echo Linking $(LIBVF4SOACUSTOM_TARGET).so
	@$(LINK) $(LIBVF4SOACUSTOM_LD_FLAGS) $(LIBVF4SOACUSTOM_OBJ)$(PS)*.$(OBJ_EXT)  $(LIBVF4SOACUSTOM_LIBS)  $(LARG_OUT) $(LIBVF4SOACUSTOM_TARGET).so
	$(COPY_FILE)  $(PROJECT_BIN)$(PS)libvf4soacustom.so $(PROJECT_TC)$(PS)lib
	@echo ________________________________________________________________________________


libvf4soacustom_clean:
	@echo ________________________________________________________________________________
	@echo Cleaning C++ Server libvf4soacustom
	-$(DELETE_DIR)  $(LIBVF4SOACUSTOM_OBJ)
	-$(DELETE_FILE) $(PROJECT_BIN)$(PS)libvf4soacustom.*	
	-$(DELETE_FILE) $(PROJECT_TC)$(PS)bin$(PS)libvf4soacustom.dll
	-$(DELETE_FILE) $(PROJECT_TC)$(PS)lib$(PS)libvf4soacustom.lib	
	-$(DELETE_FILE) $(PROJECT_TC)$(PS)lib$(PS)libvf4soacustom.so	
	@echo ________________________________________________________________________________

