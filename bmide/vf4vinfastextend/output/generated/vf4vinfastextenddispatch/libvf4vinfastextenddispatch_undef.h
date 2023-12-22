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

#if !defined(LIBVF4VINFASTEXTENDDISPATCH) && !defined(IPLIB)
#   error IPLIB or LIBVF4VINFASTEXTENDDISPATCH is not defined
#endif

#undef VF4VINFASTEXTENDDISPATCH_API
#undef VF4VINFASTEXTENDDISPATCHEXPORT
#undef VF4VINFASTEXTENDDISPATCHGLOBAL
#undef VF4VINFASTEXTENDDISPATCHPRIVATE
