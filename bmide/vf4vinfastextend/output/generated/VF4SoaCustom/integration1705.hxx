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

#ifndef TEAMCENTER_SERVICES_CUSTOM_2017_05_INTEGRATION_HXX 
#define TEAMCENTER_SERVICES_CUSTOM_2017_05_INTEGRATION_HXX


#include <tccore/ItemRevision.hxx>



#include <teamcenter/soa/server/ServiceException.hxx>
#include <metaframework/BusinessObjectRef.hxx>

#include <Custom_exports.h>

namespace Teamcenter { namespace Soa {  namespace Internal { namespace Server { class SdmStream; }}}}
namespace Teamcenter { namespace Soa {  namespace Internal { namespace Server { class SdmParser; }}}}
namespace VF4 { namespace Services { namespace Custom { namespace _2017_05 { class IntegrationIiopSkeleton; }}}}


namespace VF4
{
    namespace Soa
    {
        namespace Custom
        {
            namespace _2017_05
            {
                class Integration;
            }
        }
    }
}


class SOACUSTOM_API VF4::Soa::Custom::_2017_05::Integration

{
public:

    static const std::string XSD_NAMESPACE;

    struct NominateSupplierInput;

    struct  NominateSupplierInput
    {
        /**
         * List of VF Part Numbers
         */
        std::vector< std::string > vfPartNumbers;
        /**
         * Supplier Code
         */
        std::string supplierCode;
        /**
         * Manufacturing Location present by String
         */
        std::string manufacturingLocation;
        /**
         * If true, Karaf event will be triggerred. If false, it's not triggered.
         */
        bool isTriggerKarafEvent;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmParser;
        friend class VF4::Services::Custom::_2017_05::IntegrationIiopSkeleton;

        void parse    ( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser );
    };



    Integration();
    virtual ~Integration();
    

    /**
     * Nominate a list of parts to a supplier (SQ portal).
     *
     * @param nominateSupplierInput
     *        Parts list and nominated supplier code.
     *
     * @return
     *
     *
     * @version Teamcenter 11.3
     */
    virtual bool nominateSupplierToSQP ( const NominateSupplierInput& nominateSupplierInput ) = 0;

    /**
     * clean supplier info on source part.
     *
     * @param sourcePartNo
     *        Source Part Number
     *
     * @return
     *
     *
     * @version Teamcenter 11.3
     */
    virtual bool removeSupplierInfo ( const std::string sourcePartNo ) = 0;

    /**
     * update v4_q_checker without update last modified user.
     *
     * @param designRevision
     *        Vf4 DesignRevision
     *
     * @param result
     *        Qchecker result
     *
     * @return
     *
     *
     * @version Teamcenter 11.3
     */
    virtual bool updateQcheckerProperty ( const BusinessObjectRef<Teamcenter::ItemRevision>& designRevision,
        const std::string result ) = 0;


};

#include <Custom_undef.h>
#endif

