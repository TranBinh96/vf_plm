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

#ifndef TEAMCENTER_SERVICES_CUSTOM_2019_06_SOURCING_HXX 
#define TEAMCENTER_SERVICES_CUSTOM_2019_06_SOURCING_HXX


#include <teamcenter/soa/server/ServiceData.hxx>



#include <teamcenter/soa/server/ServiceException.hxx>
#include <metaframework/BusinessObjectRef.hxx>

#include <Custom_exports.h>

namespace Teamcenter { namespace Soa {  namespace Internal { namespace Server { class SdmStream; }}}}
namespace Teamcenter { namespace Soa {  namespace Internal { namespace Server { class SdmParser; }}}}
namespace VF4 { namespace Services { namespace Custom { namespace _2019_06 { class SourcingIiopSkeleton; }}}}


namespace VF4
{
    namespace Soa
    {
        namespace Custom
        {
            namespace _2019_06
            {
                class Sourcing;
            }
        }
    }
}


class SOACUSTOM_API VF4::Soa::Custom::_2019_06::Sourcing

{
public:

    static const std::string XSD_NAMESPACE;

    struct STResponse;

    typedef std::map< std::string, std::vector< std::string > > BodyAttributeValues;

    typedef std::map< int, std::string > HeaderDisplayNames;

    struct  STResponse
    {
        /**
         * First Row of the excel(Display Names)
         */
        HeaderDisplayNames attrDisplayNames;
        /**
         * Get the Attributes Values for the body of the excel
         */
        BodyAttributeValues attrActualValues;
        /**
         * Get the servicedata Object
         */
        Teamcenter::Soa::Server::ServiceData serviceData;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::Custom::_2019_06::SourcingIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="STResponse" );
    };



    Sourcing();
    virtual ~Sourcing();
    

    /**
     * .
     *
     * @param sourcingProgram
     *        Give the Sourcing Program as Input
     *
     * @param isDELRequiredInPurchaseLevel
     *        Is DEL attributes required, make it true
     *
     * @param isCostAttributesRequired
     *        If cost attributes required, make it true
     *
     * @param selecetdUIDs
     *        Selected UID
     *
     * @return
     *
     *
     * @version ActiveWorkspace4.2
     */
    virtual STResponse getSourceTrackerDetails ( const std::string sourcingProgram,
        bool isDELRequiredInPurchaseLevel,
        bool isCostAttributesRequired,
        const std::vector< std::string >& selecetdUIDs ) = 0;


};

#include <Custom_undef.h>
#endif

