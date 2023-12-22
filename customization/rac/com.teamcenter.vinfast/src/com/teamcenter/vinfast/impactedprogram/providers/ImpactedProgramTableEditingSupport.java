package com.teamcenter.vinfast.impactedprogram.providers;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

import com.teamcenter.vinfast.impactedprogram.bean.ImpactedPartBean;
import com.teamcenter.vinfast.impactedprogram.bean.ImpactedProgramBean;

public class ImpactedProgramTableEditingSupport extends EditingSupport {
	private TextCellEditor cellEditor = null;
	private int colIndex = 0;
	private int commandIndex = 0;
	private String command = null;

	public ImpactedProgramTableEditingSupport(final TableViewer tableViewer, String command, int colIndex) {
		super(tableViewer);

		// this.tableViewer = tableViewer;
		this.command = command;
		this.colIndex = colIndex;
		this.cellEditor = new TextCellEditor(tableViewer.getTable());

		if (this.command.equalsIgnoreCase("Impacted Programs")) {
			commandIndex = 0;
		} else if (this.command.equalsIgnoreCase("Part View")) {
			commandIndex = 1;
		}
	}

	@Override
	protected boolean canEdit(final Object element) {
		return true;
	}

	@Override
	protected CellEditor getCellEditor(final Object element) {
		return cellEditor;
	}

	@Override
	protected Object getValue(final Object element) {
		if (element instanceof ImpactedProgramBean || element instanceof ImpactedPartBean) {
			switch (commandIndex) {
				case 0: {
					final ImpactedProgramBean impactedProgramBean = (ImpactedProgramBean) element;
					switch (colIndex) {
					case 0:
						return impactedProgramBean.getStrProgramName();
					case 1:
						return impactedProgramBean.getStrPartList();
					}
					break;
				}
				case 1: {
					final ImpactedPartBean impactedPartBean = (ImpactedPartBean) element;
					switch (colIndex) {
					case 0:
						return impactedPartBean.getStrPartName();
					case 1:
						return impactedPartBean.getStrProgramList();
					}
					break;
				}
			}
		}

		return Boolean.FALSE;
	}

	@Override
	protected void setValue(final Object element, final Object value) {

	}
}
