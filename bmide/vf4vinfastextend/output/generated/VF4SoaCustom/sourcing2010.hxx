/* 
 @<COPYRIGHT>@
 ==================================================
 Copyright 2014
 Siemens Product Lifecycle Management Software Inc.
 All Rights Reserved.
 ==================================================
 @<COPYRIGHT>@

 ==================================================

   Auto-generated source from service interface.
                 DO NOT EDIT

 ==================================================
*/

#ifndef TEAMCENTER_SERVICES_CUSTOM_2020_10_SOURCING_HXX 
#define TEAMCENTER_SERVICES_CUSTOM_2020_10_SOURCING_HXX





#include <teamcenter/soa/server/ServiceException.hxx>
#include <metaframework/BusinessObjectRef.hxx>

#include <Custom_exports.h>

namespace Teamcenter { namespace Soa {  namespace Internal { namespace Server { class SdmStream; }}}}
namespace Teamcenter { namespace Soa {  namespace Internal { namespace Server { class SdmParser; }}}}


namespace VF4
{
    namespace Soa
    {
        namespace Custom
        {
            namespace _2020_10
            {
                class Sourcing;
            }
        }
    }
}


class SOACUSTOM_API VF4::Soa::Custom::_2020_10::Sourcing

{
public:

    static const std::string XSD_NAMESPACE;




    Sourcing();
    virtual ~Sourcing();
    

    /**
     * processing plan dates plan dates.
     *
     * @param sourceparts
     *        Input: list of source parts
     *
     * @return
     *         return boolean true and false
     *
     *
     * @version VF_2020_10
     */
    virtual bool planDateCalculation ( const std::vector< std::string >& sourceparts ) = 0;


};

#include <Custom_undef.h>
#endif

