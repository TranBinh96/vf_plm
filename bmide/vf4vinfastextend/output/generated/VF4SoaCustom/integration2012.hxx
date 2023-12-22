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

#ifndef TEAMCENTER_SERVICES_CUSTOM_2020_12_INTEGRATION_HXX 
#define TEAMCENTER_SERVICES_CUSTOM_2020_12_INTEGRATION_HXX


#include <bom/BOMLine.hxx>
#include <teamcenter/soa/server/ServiceData.hxx>



#include <teamcenter/soa/server/ServiceException.hxx>
#include <metaframework/BusinessObjectRef.hxx>

#include <Custom_exports.h>

namespace Teamcenter { namespace Soa {  namespace Internal { namespace Server { class SdmStream; }}}}
namespace Teamcenter { namespace Soa {  namespace Internal { namespace Server { class SdmParser; }}}}
namespace VF4 { namespace Services { namespace Custom { namespace _2020_12 { class IntegrationIiopSkeleton; }}}}


namespace VF4
{
    namespace Soa
    {
        namespace Custom
        {
            namespace _2020_12
            {
                class Integration;
            }
        }
    }
}


class SOACUSTOM_API VF4::Soa::Custom::_2020_12::Integration

{
public:

    static const std::string XSD_NAMESPACE;

    struct StructureSearchInMPPInput;
    struct StructureSearchInMPPOutput;

    struct  StructureSearchInMPPInput
    {
        /**
         * Search Scope for Structure Search
         */
        BusinessObjectRef<Teamcenter::BOMLine> searchScope;
        /**
         * Query for Structure Search
         */
        std::string queryName;
        /**
         * Query Criteria for Structure Search
         */
        std::vector< std::string > queryCriteria;
        /**
         * Query Criteria Values for Structure Search
         */
        std::vector< std::string > queryCriteriaValues;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmParser;
        friend class VF4::Services::Custom::_2020_12::IntegrationIiopSkeleton;

        void parse    ( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser );
    };

    struct  StructureSearchInMPPOutput
    {
        /**
         * Found lines from structure search
         */
        std::vector< BusinessObjectRef<Teamcenter::BOMLine> > foundLines;
        /**
         * Service Data
         */
        Teamcenter::Soa::Server::ServiceData serviceData;
        /**
         * custom error msg
         */
        std::string errorString;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::Custom::_2020_12::IntegrationIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="StructureSearchInMPPOutput" );
    };



    Integration();
    virtual ~Integration();
    

    /**
     * .
     *
     * @param input
     *        Input for service StructureSearchInMPP
     *
     * @return
     *
     *
     * @version ActiveWorkspace5.1
     */
    virtual StructureSearchInMPPOutput structureSearchInMPP ( const StructureSearchInMPPInput& input ) = 0;


};

#include <Custom_undef.h>
#endif

