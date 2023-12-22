package com.vinfast.sap.util;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentEnvelope;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core.EnvelopeService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateOut;
import com.teamcenter.soa.client.model.ServiceData;

public class EnvelopeSend 
{
	private EnvelopeService envService;
	private DataManagementService dmService;
	private TCSession session;
	
	private TCComponentEnvelope envelope = null;
	
	public EnvelopeSend(TCSession isession) throws ServiceException
	{
		session = isession;
		dmService = DataManagementService.getService(session);
		envService = EnvelopeService.getService(session);
	}
	
	/**
	 * 
	 * @param toTcUser
	 * @param subject limited to 240 characters
	 * @param body240 limited to 240 characters
	 * @return
	 */
	public TCComponentEnvelope createEnvelope(TCComponentUser toTcUser[], String subject240c, String body240c, TCComponent[] attachments)
	{
	
		try
		{
			CreateIn input = new CreateIn();

			input.clientId = "ID:" + input.hashCode();
			input.data = new DataManagementService.CreateInput();
			input.data.boName = "Envelope"; 
			input.data.stringProps.put("object_name", subject240c); // Subject
			input.data.stringProps.put("object_desc", body240c); //body
			if((toTcUser!=null) && (toTcUser.length >0 ))
				input.data.tagArrayProps.put("listOfReceivers", toTcUser); // To: receivers (User objects)
			if((attachments!=null) && (attachments.length >0 ))
				input.data.tagArrayProps.put("contents", attachments); // attachments (BO)
			
			DataManagementService.CreateResponse resp = dmService.createObjects(new DataManagementService.CreateIn[]{ input });

			if(!ServiceDataError(resp.serviceData)) {
				CreateOut[] createOutResp = resp.output ;
				TCComponent[] compOnent = createOutResp[0].objects ;
				
				try
				{
					for (int i = 0; i < compOnent.length ; i ++)
					{
						if (compOnent[i] instanceof TCComponentEnvelope )
						{
							envelope = (TCComponentEnvelope)compOnent[i] ;
							break;
						}
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}

		
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return envelope;
	}
	
	public void sendEnvelope()
	{
		ServiceData sdata = envService.sendAndDeleteEnvelopes(new TCComponentEnvelope[]{ envelope });
		
		ServiceDataError(sdata);		
	}
	
	protected boolean ServiceDataError(final ServiceData data)
	{
		if(data.sizeOfPartialErrors() > 0)
		{
			for(int i = 0; i < data.sizeOfPartialErrors(); i++)
			{
				for(String msg : data.getPartialError(i).getMessages())
					System.out.println(msg);
			}

			return true;
		}

		return false;
	}
}