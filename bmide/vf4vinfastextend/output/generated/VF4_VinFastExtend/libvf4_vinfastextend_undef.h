//@<COPYRIGHT>@
//==================================================
//Copyright $2023.
//Siemens Product Lifecycle Management Software Inc.
//All Rights Reserved.
//==================================================
//@<COPYRIGHT>@


#include <common/library_indicators.h>

#if !defined(EXPORTLIBRARY)
#   error EXPORTLIBRARY is not defined
#endif

#undef EXPORTLIBRARY

#if !defined(LIBVF4_VINFASTEXTEND) && !defined(IPLIB)
#   error IPLIB or LIBVF4_VINFASTEXTEND is not defined
#endif

#undef VF4_VINFASTEXTEND_API
#undef VF4_VINFASTEXTENDEXPORT
#undef VF4_VINFASTEXTENDGLOBAL
#undef VF4_VINFASTEXTENDPRIVATE
