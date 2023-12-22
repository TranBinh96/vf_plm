package com.teamcenter.vinfast.impactedprogram.providers;

import java.util.List;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.vinfast.impactedprogram.bean.ImpactedProgramBean;

public class ImpactedProgramTableCLProvider extends AbstractTableContentLabelProvider
{
	public ImpactedProgramTableCLProvider(Shell shell)
	{
		@SuppressWarnings("unused")
		LocalResourceManager jfaceRsManager = new LocalResourceManager(JFaceResources.getResources(), shell);
	}

	@Override
	public String getColumnText(Object element, int columnIndex)
	{
		ImpactedProgramBean impactedProgramBean = (ImpactedProgramBean) element;
		switch (columnIndex)
		{
			case 0:
				return impactedProgramBean.getStrProgramName();
			case 1:
				return impactedProgramBean.getStrPartList();
			
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
			List<ImpactedProgramBean> list = (List<ImpactedProgramBean>) input;
			return list.toArray();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
}
