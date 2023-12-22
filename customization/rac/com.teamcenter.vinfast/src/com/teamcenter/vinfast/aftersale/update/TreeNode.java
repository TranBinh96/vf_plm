package com.teamcenter.vinfast.aftersale.update;

import java.util.ArrayList;
import com.teamcenter.rac.kernel.TCComponent;

public class TreeNode {

	private ArrayList<TreeNode> children = null;
    private String value = null;
    private DataMap moduleMap = null;
    private TCComponent nodeTag = null;
    
    public TreeNode(String keyValue)
    {
        children = new ArrayList<TreeNode>();
        value = keyValue;
        moduleMap =  new DataMap();
    }

    public void addChild(TreeNode child)
    {
        children.add(child);
    }
    
    public void setNodeTag(TCComponent tag) {
    	nodeTag = tag;
    }
    
    public void setNodeMap(DataMap map) {
    	moduleMap = map;
    }
    public String getValue() {
    	return value;
    }
    public TCComponent getNodeTag() {
    	return nodeTag;
    }
    public DataMap getNodeMap() {
    	return moduleMap;
    }
    public ArrayList<TreeNode> getChildren() {
    	return children;
    }
    public boolean hasChilds() {
    	if(children.isEmpty() == true) {
    		return false;
    	}else {
    		return true;
    	}
    }
}
