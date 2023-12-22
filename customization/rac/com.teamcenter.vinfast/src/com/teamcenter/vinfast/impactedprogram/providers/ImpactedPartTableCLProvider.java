package com.teamcenter.vinfast.impactedprogram.providers;

import java.util.List;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.vinfast.impactedprogram.bean.ImpactedPartBean;

public class ImpactedPartTableCLProvider extends AbstractTableContentLabelProvider
{
	public ImpactedPartTableCLProvider(Shell shell)
	{
		@SuppressWarnings("unused")
		LocalResourceManager jfaceRsManager = new LocalResourceManager(JFaceResources.getResources(), shell);
	}

	@Override
	public String getColumnText(Object element, int columnIndex)
	{
		ImpactedPartBean impactedPartBean = (ImpactedPartBean) element;
		switch (columnIndex)
		{
			case 0:
				return impactedPartBean.getStrPartName();
			case 1:
				return impactedPartBean.getStrProgramList();
			
			default:
				return "";
		}
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex)
	{
		switch (columnIndex)
		{
			case 0:
				return null;
			default:
				return null;
		}
	}

	// see: viewer.setInput(..)
	@Override
	public Object[] getElements(Object input)
	{
		try
		{
			@SuppressWarnings("unchecked")
			List<ImpactedPartBean> list = (List<ImpactedPartBean>) input;
			return list.toArray();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
}

