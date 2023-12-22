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
LIBVF4SOAINTEGRATION_SERVER     = $(PROJECT_HOME)$(PS)src$(PS)server
LIBVF4SOAINTEGRATION_SRC        = $(LIBVF4SOAINTEGRATION_SERVER)$(PS)VF4SoaIntegration
LIBVF4SOAINTEGRATION_DRV        = $(PROJECT_DRV)$(PS)VF4SoaIntegration
LIBVF4SOAINTEGRATION_OBJ        = $(PROJECT_OBJ)$(PS)libvf4soaintegration
LIBVF4SOAINTEGRATION_CXXS       = $(LIBVF4SOAINTEGRATION_SRC)$(PS)*.cxx $(LIBVF4SOAINTEGRATION_DRV)$(PS)*.cxx
LIBVF4SOAINTEGRATION_CXX_FLAGS  = $(CXX_FLAGS) -DIPLIB=libvf4soaintegration
LIBVF4SOAINTEGRATION_LINK_FLAGS = $(LINK_FLAGS)
LIBVF4SOAINTEGRATION_INCLUDES   = $(CPP_INCLUDES) -I$(LIBVF4SOAINTEGRATION_SRC) -I$(LIBVF4SOAINTEGRATION_DRV) -I$(PROJECT_DRV) -I$(LIBVF4SOAINTEGRATION_SERVER) \
                                  -I$(TC_ROOT)$(PS)include -I$(TC_ROOT)$(PS)include_cpp -I$(TC_ROOT)$(PS)aw_include_cpp \
                                  -I$(TOOLBOX_HOME)$(PS)TCGATEWAYSRC \
                                  
LIBVF4SOAINTEGRATION_LIB_PATH   = $(LP)$(PROJECT_BIN)  $(LIB_PATH)
LIBVF4SOAINTEGRATION_LIBS       = $(LIB_PREFIX)sa$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)epm$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)grm$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)tccore$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)tc$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)pom$(LIB_SUFFIX) \
                                  $(SYSLIBS)  \
                                  libVF4_curl.lib \
                                  $(LIB_PREFIX)tcsoacommon$(LIB_SUFFIX)        $(LIB_PREFIX)tcsoaserver$(LIB_SUFFIX) $(LIB_PREFIX)pom$(LIB_SUFFIX)  \
                                  $(LIB_PREFIX)itk$(LIB_SUFFIX)                $(LIB_PREFIX)fclasses$(LIB_SUFFIX)    $(LIB_PREFIX)metaframework$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)extensionframework$(LIB_SUFFIX) $(LIB_PREFIX)tccore$(LIB_SUFFIX)      $(LIB_PREFIX)tc$(LIB_SUFFIX) \
                                  $(LIB_PREFIX)cxpom$(LIB_SUFFIX)              $(LIB_PREFIX)base_utils$(LIB_SUFFIX)  $(LIB_PREFIX)mld$(LIB_SUFFIX)
LIBVF4SOAINTEGRATION_LD_FLAGS   = $(LIBVF4SOAINTEGRATION_LINK_FLAGS) $(LIBVF4SOAINTEGRATION_LIB_PATH)
LIBVF4SOAINTEGRATION_TARGET     = $(PROJECT_BIN)$(PS)libvf4soaintegration



libvf4soaintegration_all:   $(LIBVF4SOAINTEGRATION_OBJ)  $(LIBVF4SOAINTEGRATION_TARGET).$(LIB_EXT)


$(LIBVF4SOAINTEGRATION_OBJ) :
	@echo Creating: $(LIBVF4SOAINTEGRATION_OBJ)
	-@$(MAKE_DIR) $(LIBVF4SOAINTEGRATION_OBJ)

$(LIBVF4SOAINTEGRATION_CXXS) :


# ==================================================
# Windows Build Target
# ==================================================
$(LIBVF4SOAINTEGRATION_TARGET).dll : $(LIBVF4SOAINTEGRATION_CXXS)
	@echo ________________________________________________________________________________
	@echo Building C++ Server libvf4soaintegration
	@echo Compiling Source...
	@$(COMPILE)  $(LIBVF4SOAINTEGRATION_CXX_FLAGS) $(LIBVF4SOAINTEGRATION_INCLUDES) -c $(**)   $(CARG_OUT)$(LIBVF4SOAINTEGRATION_OBJ)$(PS)
	@echo.
	@$(LINK) $(LIBVF4SOAINTEGRATION_LD_FLAGS) $(LIBVF4SOAINTEGRATION_OBJ)$(PS)*.$(OBJ_EXT)  $(LIBVF4SOAINTEGRATION_LIBS)  $(LARG_OUT)$(LIBVF4SOAINTEGRATION_TARGET).dll
	$(COPY_FILE)  $(PROJECT_BIN)$(PS)libvf4soaintegration.dll $(PROJECT_TC)$(PS)bin
	$(COPY_FILE)  $(PROJECT_BIN)$(PS)libvf4soaintegration.lib $(PROJECT_TC)$(PS)lib
	@echo ________________________________________________________________________________


	
# ==================================================
# UNIX Build Target
# ==================================================
$(LIBVF4SOAINTEGRATION_TARGET).so : $(LIBVF4SOAINTEGRATION_CXXS)
	@echo ________________________________________________________________________________
	@echo Building C++ Server libvf4soaintegration
	@# The compile output goes to the current directory (project root), so remove any .o files
	@# that may have been left here from previous failed builds. Then after the compile move all .o
	@# files to the the desired obj folder.
	@-rm *.$(OBJ_EXT)
	@echo Compiling $(LIBVF4SOAINTEGRATION_CXXS)
	@$(COMPILE)  $(LIBVF4SOAINTEGRATION_CXX_FLAGS) $(LIBVF4SOAINTEGRATION_INCLUDES) -c $(LIBVF4SOAINTEGRATION_CXXS) 
	@mv *.$(OBJ_EXT) $(LIBVF4SOAINTEGRATION_OBJ)
	@echo Linking $(LIBVF4SOAINTEGRATION_TARGET).so
	@$(LINK) $(LIBVF4SOAINTEGRATION_LD_FLAGS) $(LIBVF4SOAINTEGRATION_OBJ)$(PS)*.$(OBJ_EXT)  $(LIBVF4SOAINTEGRATION_LIBS)  $(LARG_OUT) $(LIBVF4SOAINTEGRATION_TARGET).so
	$(COPY_FILE)  $(PROJECT_BIN)$(PS)libvf4soaintegration.so $(PROJECT_TC)$(PS)lib
	@echo ________________________________________________________________________________


libvf4soaintegration_clean:
	@echo ________________________________________________________________________________
	@echo Cleaning C++ Server libvf4soaintegration
	-$(DELETE_DIR)  $(LIBVF4SOAINTEGRATION_OBJ)
	-$(DELETE_FILE) $(PROJECT_BIN)$(PS)libvf4soaintegration.*	
	-$(DELETE_FILE) $(PROJECT_TC)$(PS)bin$(PS)libvf4soaintegration.dll
	-$(DELETE_FILE) $(PROJECT_TC)$(PS)lib$(PS)libvf4soaintegration.lib	
	-$(DELETE_FILE) $(PROJECT_TC)$(PS)lib$(PS)libvf4soaintegration.so	
	@echo ________________________________________________________________________________

