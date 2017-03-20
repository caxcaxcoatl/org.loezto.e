package org.loezto.e.dnd;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Widget;
import org.loezto.e.model.Task;

public class TaskTransfer extends ByteArrayTransfer {
	
	public enum SourceType { Structure, Plan }; 
	
	private static final String TYPE_NAME = "e-task-transfer";
	
    private static final int TYPEID = registerType(TYPE_NAME);

    private static final TaskTransfer INSTANCE = new TaskTransfer();

    private Task task;
    
    private SourceType sourceType;
    
    private Widget sourceWidget;
    
    public SourceType getSourceType() {
		return sourceType;
	}

	public void setSourceType(SourceType sourceType) {
		this.sourceType = sourceType;
	}

	private TaskTransfer ()
    {
    	// do nothing
    }

	@Override
	protected String[] getTypeNames() {
        return new String[] { TYPE_NAME };	}

	@Override
	protected int[] getTypeIds() {
        return new int[] { TYPEID };	}
	
    /**
     * Returns the singleton.
     *
     * @return the singleton
     */
    public static TaskTransfer getTransfer() {
        return INSTANCE;
    }
    
    public Task getTask ()
    {
    	return task;
    }
    
    public void setTask (Task task)
    {
    	 this.task = task;
    }

	public Widget getSourceWidget() {
		return sourceWidget;
	}

	public void setSourceWidget(Widget sourceWidget) {
		this.sourceWidget = sourceWidget;
	}

	@Override
	public boolean isSupportedType(TransferData transferData) {
		return true;
	}

	@Override
	protected Object nativeToJava(TransferData transferData) {
		return task;
	}

	@Override
	protected void javaToNative(Object object, TransferData transferData) {
		// Don't do anything, as this is strictly in-app move, and the data is on this.selection
	}
	
	
	
	
	


}
