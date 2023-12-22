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
LIBVF4SOACM_SERVER     = $(PROJECT_HOME)$(PS)src$(PS)server
LIBVF4SOACM_SRC        = $(LIBVF4SOACM_SERVER)$(PS)VF4SoaCM
LIBVF4SOACM_DRV        = $(PROJECT_DRV)$(PS)VF4SoaCM
LIBVF4SOACM_OBJ        = $(PROJECT_OBJ)$(PS)libvf4soacm
LIBVF4SOACM_CXXS       = $(LIBVF4SOACM_SRC)$(PS)*.cxx $(LIBVF4SOACM_DRV)$(PS)*.cxx
LIBVF4SOACM_CXX_FLAGS  = $(CXX_FLAGS) -DIPLIB=libvf4soacm
LIBVF4SOACM_LINK_FLAGS = $(LINK_FLAGS)
LIBVF4SOACM_INCLUDES   = $(CPP_INCLUDES) -I$(LIBVF4SOACM_SRC) -I$(LIBVF4SOACM_DRV) -I$(PROJECT_DRV) -I$(LIBVF4SOACM_SERVER) \
                                  -I$(TC_ROOT)$(PS)include -I$(TC_ROOT)$(PS)include_cpp -I$(TC_ROOT)$(PS)aw_include_cpp \
                                  -I$(TOOLBOX_HOME)$(PS)TCGATEWAYSRC \
                                  
LIBVF4SOACM_LIB_PATH   = $(LP)$(PROJECT_BIN)  $(LIB_PATH)
LIBVF4SOACM_LIBS       = $(LIB_PREFIX)tc$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)cm$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)tccore$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)sa$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)pom$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)meta$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)metaframework$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)ps$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)bom$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)cfm$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)qry$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)epm$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)me$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)user_exits$(LIB_SUFFIX) \
                                  $(SYSLIBS)  \
                                  $(LIB_PREFIX)tcsoacommon$(LIB_SUFFIX)        $(LIB_PREFIX)tcsoaserver$(LIB_SUFFIX) $(LIB_PREFIX)pom$(LIB_SUFFIX)  \
                                  $(LIB_PREFIX)itk$(LIB_SUFFIX)                $(LIB_PREFIX)fclasses$(LIB_SUFFIX)    $(LIB_PREFIX)metaframework$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)extensionframework$(LIB_SUFFIX) $(LIB_PREFIX)tccore$(LIB_SUFFIX)      $(LIB_PREFIX)tc$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)cxpom$(LIB_SUFFIX)              $(LIB_PREFIX)base_utils$(LIB_SUFFIX)  $(LIB_PREFIX)mld$(LIB_SUFFIX)
LIBVF4SOACM_LD_FLAGS   = $(LIBVF4SOACM_LINK_FLAGS) $(LIBVF4SOACM_LIB_PATH)
LIBVF4SOACM_TARGET     = $(PROJECT_BIN)$(PS)libvf4soacm



libvf4soacm_all:   $(LIBVF4SOACM_OBJ)  $(LIBVF4SOACM_TARGET).$(LIB_EXT)


$(LIBVF4SOACM_OBJ) :
	@echo Creating: $(LIBVF4SOACM_OBJ)
	-@$(MAKE_DIR) $(LIBVF4SOACM_OBJ)

$(LIBVF4SOACM_CXXS) :


# ==================================================
# Windows Build Target
# ==================================================
$(LIBVF4SOACM_TARGET).dll : $(LIBVF4SOACM_CXXS)
	@echo ________________________________________________________________________________
	@echo Building C++ Server libvf4soacm
	@echo Compiling Source...
	@$(COMPILE)  $(LIBVF4SOACM_CXX_FLAGS) $(LIBVF4SOACM_INCLUDES) -c $(**)   $(CARG_OUT)$(LIBVF4SOACM_OBJ)$(PS)
	@echo.
	@$(LINK) $(LIBVF4SOACM_LD_FLAGS) $(LIBVF4SOACM_OBJ)$(PS)*.$(OBJ_EXT)  $(LIBVF4SOACM_LIBS)  $(LARG_OUT)$(LIBVF4SOACM_TARGET).dll
	$(COPY_FILE)  $(PROJECT_BIN)$(PS)libvf4soacm.dll $(PROJECT_TC)$(PS)bin
	$(COPY_FILE)  $(PROJECT_BIN)$(PS)libvf4soacm.lib $(PROJECT_TC)$(PS)lib
	@echo ________________________________________________________________________________


	
# ==================================================
# UNIX Build Target
# ==================================================
$(LIBVF4SOACM_TARGET).so : $(LIBVF4SOACM_CXXS)
	@echo ________________________________________________________________________________
	@echo Building C++ Server libvf4soacm
	@# The compile output goes to the current directory (project root), so remove any .o files
	@# that may have been left here from previous failed builds. Then after the compile move all .o
	@# files to the the desired obj folder.
	@-rm *.$(OBJ_EXT)
	@echo Compiling $(LIBVF4SOACM_CXXS)
	@$(COMPILE)  $(LIBVF4SOACM_CXX_FLAGS) $(LIBVF4SOACM_INCLUDES) -c $(LIBVF4SOACM_CXXS) 
	@mv *.$(OBJ_EXT) $(LIBVF4SOACM_OBJ)
	@echo Linking $(LIBVF4SOACM_TARGET).so
	@$(LINK) $(LIBVF4SOACM_LD_FLAGS) $(LIBVF4SOACM_OBJ)$(PS)*.$(OBJ_EXT)  $(LIBVF4SOACM_LIBS)  $(LARG_OUT) $(LIBVF4SOACM_TARGET).so
	$(COPY_FILE)  $(PROJECT_BIN)$(PS)libvf4soacm.so $(PROJECT_TC)$(PS)lib
	@echo ________________________________________________________________________________


libvf4soacm_clean:
	@echo ________________________________________________________________________________
	@echo Cleaning C++ Server libvf4soacm
	-$(DELETE_DIR)  $(LIBVF4SOACM_OBJ)
	-$(DELETE_FILE) $(PROJECT_BIN)$(PS)libvf4soacm.*	
	-$(DELETE_FILE) $(PROJECT_TC)$(PS)bin$(PS)libvf4soacm.dll
	-$(DELETE_FILE) $(PROJECT_TC)$(PS)lib$(PS)libvf4soacm.lib	
	-$(DELETE_FILE) $(PROJECT_TC)$(PS)lib$(PS)libvf4soacm.so	
	@echo ________________________________________________________________________________

