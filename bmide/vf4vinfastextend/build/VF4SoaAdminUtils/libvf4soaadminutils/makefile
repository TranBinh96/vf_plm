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
LIBVF4SOAADMINUTILS_SERVER     = $(PROJECT_HOME)$(PS)src$(PS)server
LIBVF4SOAADMINUTILS_SRC        = $(LIBVF4SOAADMINUTILS_SERVER)$(PS)VF4SoaAdminUtils
LIBVF4SOAADMINUTILS_DRV        = $(PROJECT_DRV)$(PS)VF4SoaAdminUtils
LIBVF4SOAADMINUTILS_OBJ        = $(PROJECT_OBJ)$(PS)libvf4soaadminutils
LIBVF4SOAADMINUTILS_CXXS       = $(LIBVF4SOAADMINUTILS_SRC)$(PS)*.cxx $(LIBVF4SOAADMINUTILS_DRV)$(PS)*.cxx
LIBVF4SOAADMINUTILS_CXX_FLAGS  = $(CXX_FLAGS) -DIPLIB=libvf4soaadminutils
LIBVF4SOAADMINUTILS_LINK_FLAGS = $(LINK_FLAGS)
LIBVF4SOAADMINUTILS_INCLUDES   = $(CPP_INCLUDES) -I$(LIBVF4SOAADMINUTILS_SRC) -I$(LIBVF4SOAADMINUTILS_DRV) -I$(PROJECT_DRV) -I$(LIBVF4SOAADMINUTILS_SERVER) \
                                  -I$(TC_ROOT)$(PS)include -I$(TC_ROOT)$(PS)include_cpp -I$(TC_ROOT)$(PS)aw_include_cpp \
                                  -I$(TOOLBOX_HOME)$(PS)TCGATEWAYSRC \
                                  
LIBVF4SOAADMINUTILS_LIB_PATH   = $(LP)$(PROJECT_BIN)  $(LIB_PATH)
LIBVF4SOAADMINUTILS_LIBS       = $(LIB_PREFIX)tc$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)tccore$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)pom$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)sa$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)metaframework$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)meta$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)qry$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)bom$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)cfm$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)ps$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)epm$(LIB_SUFFIX) \
                                  $(SYSLIBS)  \
                                  $(LIB_PREFIX)tcsoacommon$(LIB_SUFFIX)        $(LIB_PREFIX)tcsoaserver$(LIB_SUFFIX) $(LIB_PREFIX)pom$(LIB_SUFFIX)  \
                                  $(LIB_PREFIX)itk$(LIB_SUFFIX)                $(LIB_PREFIX)fclasses$(LIB_SUFFIX)    $(LIB_PREFIX)metaframework$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)extensionframework$(LIB_SUFFIX) $(LIB_PREFIX)tccore$(LIB_SUFFIX)      $(LIB_PREFIX)tc$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)cxpom$(LIB_SUFFIX)              $(LIB_PREFIX)base_utils$(LIB_SUFFIX)  $(LIB_PREFIX)mld$(LIB_SUFFIX)
LIBVF4SOAADMINUTILS_LD_FLAGS   = $(LIBVF4SOAADMINUTILS_LINK_FLAGS) $(LIBVF4SOAADMINUTILS_LIB_PATH)
LIBVF4SOAADMINUTILS_TARGET     = $(PROJECT_BIN)$(PS)libvf4soaadminutils



libvf4soaadminutils_all:   $(LIBVF4SOAADMINUTILS_OBJ)  $(LIBVF4SOAADMINUTILS_TARGET).$(LIB_EXT)


$(LIBVF4SOAADMINUTILS_OBJ) :
	@echo Creating: $(LIBVF4SOAADMINUTILS_OBJ)
	-@$(MAKE_DIR) $(LIBVF4SOAADMINUTILS_OBJ)

$(LIBVF4SOAADMINUTILS_CXXS) :


# ==================================================
# Windows Build Target
# ==================================================
$(LIBVF4SOAADMINUTILS_TARGET).dll : $(LIBVF4SOAADMINUTILS_CXXS)
	@echo ________________________________________________________________________________
	@echo Building C++ Server libvf4soaadminutils
	@echo Compiling Source...
	@$(COMPILE)  $(LIBVF4SOAADMINUTILS_CXX_FLAGS) $(LIBVF4SOAADMINUTILS_INCLUDES) -c $(**)   $(CARG_OUT)$(LIBVF4SOAADMINUTILS_OBJ)$(PS)
	@echo.
	@$(LINK) $(LIBVF4SOAADMINUTILS_LD_FLAGS) $(LIBVF4SOAADMINUTILS_OBJ)$(PS)*.$(OBJ_EXT)  $(LIBVF4SOAADMINUTILS_LIBS)  $(LARG_OUT)$(LIBVF4SOAADMINUTILS_TARGET).dll
	$(COPY_FILE)  $(PROJECT_BIN)$(PS)libvf4soaadminutils.dll $(PROJECT_TC)$(PS)bin
	$(COPY_FILE)  $(PROJECT_BIN)$(PS)libvf4soaadminutils.lib $(PROJECT_TC)$(PS)lib
	@echo ________________________________________________________________________________


	
# ==================================================
# UNIX Build Target
# ==================================================
$(LIBVF4SOAADMINUTILS_TARGET).so : $(LIBVF4SOAADMINUTILS_CXXS)
	@echo ________________________________________________________________________________
	@echo Building C++ Server libvf4soaadminutils
	@# The compile output goes to the current directory (project root), so remove any .o files
	@# that may have been left here from previous failed builds. Then after the compile move all .o
	@# files to the the desired obj folder.
	@-rm *.$(OBJ_EXT)
	@echo Compiling $(LIBVF4SOAADMINUTILS_CXXS)
	@$(COMPILE)  $(LIBVF4SOAADMINUTILS_CXX_FLAGS) $(LIBVF4SOAADMINUTILS_INCLUDES) -c $(LIBVF4SOAADMINUTILS_CXXS) 
	@mv *.$(OBJ_EXT) $(LIBVF4SOAADMINUTILS_OBJ)
	@echo Linking $(LIBVF4SOAADMINUTILS_TARGET).so
	@$(LINK) $(LIBVF4SOAADMINUTILS_LD_FLAGS) $(LIBVF4SOAADMINUTILS_OBJ)$(PS)*.$(OBJ_EXT)  $(LIBVF4SOAADMINUTILS_LIBS)  $(LARG_OUT) $(LIBVF4SOAADMINUTILS_TARGET).so
	$(COPY_FILE)  $(PROJECT_BIN)$(PS)libvf4soaadminutils.so $(PROJECT_TC)$(PS)lib
	@echo ________________________________________________________________________________


libvf4soaadminutils_clean:
	@echo ________________________________________________________________________________
	@echo Cleaning C++ Server libvf4soaadminutils
	-$(DELETE_DIR)  $(LIBVF4SOAADMINUTILS_OBJ)
	-$(DELETE_FILE) $(PROJECT_BIN)$(PS)libvf4soaadminutils.*	
	-$(DELETE_FILE) $(PROJECT_TC)$(PS)bin$(PS)libvf4soaadminutils.dll
	-$(DELETE_FILE) $(PROJECT_TC)$(PS)lib$(PS)libvf4soaadminutils.lib	
	-$(DELETE_FILE) $(PROJECT_TC)$(PS)lib$(PS)libvf4soaadminutils.so	
	@echo ________________________________________________________________________________

