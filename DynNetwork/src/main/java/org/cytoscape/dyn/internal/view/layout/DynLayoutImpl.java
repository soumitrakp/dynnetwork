/*
 * DynNetwork plugin for Cytoscape 3.0 (http://www.cytoscape.org/).
 * Copyright (C) 2012 Sabina Sara Pfister
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.cytoscape.dyn.internal.view.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.dyn.internal.io.util.KeyPairs;
import org.cytoscape.dyn.internal.model.attribute.DynDoubleAttribute;
import org.cytoscape.dyn.internal.model.tree.DynInterval;
import org.cytoscape.dyn.internal.model.tree.DynIntervalTree;
import org.cytoscape.dyn.internal.model.tree.DynIntervalTreeImpl;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

/**
 * <code> DynLayoutImpl </code> implements the interface {@link DynLayout}
 * and provides method to store dynamic visualization information in the form of 
 * intervals {@link DynInterval} stored in the interval tree {@link DynIntervalTree}.
 * For each node we store a series of intervals corresponding to its x,y, and z
 * positions in time. The interval tree guarantees that the write and read operation
 * to update the visualization are minimal and asynchronous.
 * 
 * 
 * @author sabina
 *
 * @param <T>
 */
public final class DynLayoutImpl implements DynLayout
{
	private final CyNetworkView view;
	
	private double alpha = 1;
	private int n = 1;
	
	private List<DynInterval<Double>> currentNodes;
	private final DynIntervalTreeImpl<Double> nodePositionsTree;
	
	private final Map<KeyPairs,DynDoubleAttribute> node_X_Pos;
	private final Map<KeyPairs,DynDoubleAttribute> node_Y_Pos;
	private final Map<KeyPairs,DynDoubleAttribute> node_Z_Pos;

	public DynLayoutImpl(CyNetworkView view)
	{
		this.view = view;

		this.currentNodes = new ArrayList<DynInterval<Double>>();
		this.nodePositionsTree = new DynIntervalTreeImpl<Double>();

		this.node_X_Pos = new HashMap<KeyPairs,DynDoubleAttribute>();
		this.node_Y_Pos = new HashMap<KeyPairs,DynDoubleAttribute>();
		this.node_Z_Pos = new HashMap<KeyPairs,DynDoubleAttribute>();
	}
	
	@Override
	public synchronized void insertNodePositionX(CyNode node, DynInterval<Double> interval)
	{
		setDynAttributeX(node,interval);
		node_X_Pos.put(interval.getAttribute().getKey(), (DynDoubleAttribute) interval.getAttribute());
		nodePositionsTree.insert(interval, node.getSUID());
	}

	@Override
	public synchronized void insertNodePositionY(CyNode node, DynInterval<Double> interval)
	{
		setDynAttributeY(node, (DynInterval<Double>) interval);
		node_Y_Pos.put(interval.getAttribute().getKey(), (DynDoubleAttribute)interval.getAttribute());
		nodePositionsTree.insert(interval, node.getSUID());
	}
	
	@Override
	public synchronized void insertNodePositionZ(CyNode node, DynInterval<Double> interval)
	{
		setDynAttributeZ(node,(DynInterval<Double>) interval);
		node_Z_Pos.put(interval.getAttribute().getKey(), (DynDoubleAttribute)interval.getAttribute());
		nodePositionsTree.insert(interval, node.getSUID());
	}
	
	@Override
	public synchronized void removeNode(CyNode node)
	{
		KeyPairs key = new KeyPairs("node_X_Pos", node.getSUID());
		for (DynInterval<Double> interval : node_X_Pos.get(key).getIntervalList())
			nodePositionsTree.remove(interval, node.getSUID());
		node_X_Pos.remove(key);
		
		key = new KeyPairs("node_Y_Pos", node.getSUID());
		for (DynInterval<Double> interval : node_Y_Pos.get(key).getIntervalList())
			nodePositionsTree.remove(interval, node.getSUID());
		node_Y_Pos.remove(key);
		
		key = new KeyPairs("node_Z_Pos", node.getSUID());
		for (DynInterval<Double> interval : node_Z_Pos.get(key).getIntervalList())
			nodePositionsTree.remove(interval, node.getSUID());
		node_Z_Pos.remove(key);

	}
	
	@Override
	public synchronized void removeAllIntervals()
	{
		nodePositionsTree.clear();
		this.node_X_Pos.clear();
		this.node_Y_Pos.clear();
		this.node_Z_Pos.clear();
		currentNodes.clear();
	}
	
	@Override
	public List<DynInterval<Double>> getIntervals(CyNode node)
	{
		return nodePositionsTree.getIntervals(node.getSUID());
	}
	
	@Override
	public List<DynInterval<Double>> searchNodePositions(DynInterval<Double> interval)
	{
		return nodePositionsTree.search(interval);
	}
	
	@Override
	public List<DynInterval<Double>> searchChangedNodePositions(DynInterval<Double> interval)
	{
		List<DynInterval<Double>> tempList = nodePositionsTree.search(interval);
		List<DynInterval<Double>> changedList = nonOverlap(currentNodes, tempList);
		currentNodes = tempList;
		return changedList;
	}
	
	@Override
	public List<DynInterval<Double>> searchNodePositionsNot(DynInterval<Double> interval)
	{
		return nodePositionsTree.searchNot(interval);
	}
	
	@Override
	public void initNodePositions(double time) 
	{
		List<DynInterval<Double>> intervalList = this.searchChangedNodePositions(new DynInterval<Double>(time, time));
		for (DynInterval<Double> interval : intervalList)
		{
			CyNode node = view.getModel().getNode(interval.getAttribute().getKey().getRow());
			if (node!=null)
				if (interval.getAttribute().getColumn().equals("node_X_Pos"))
					view.getNodeView(node).setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, (Double) interval.getValue());
				else if (interval.getAttribute().getColumn().equals("node_Y_Pos"))
					view.getNodeView(node).setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, (Double) interval.getValue());
				else if (interval.getAttribute().getColumn().equals("node_Z_Pos"))
					view.getNodeView(node).setVisualProperty(BasicVisualLexicon.NODE_Z_LOCATION, (Double) interval.getValue());
		}
	}
	
	@Override
	public CyNetworkView getNetworkView() 
	{
		return this.view;
	}
	
	private List<DynInterval<Double>> nonOverlap(List<DynInterval<Double>> list1, List<DynInterval<Double>> list2) 
	{
		List<DynInterval<Double>> diff = new ArrayList<DynInterval<Double>>();
		for (DynInterval<Double> i : list2)
			if (!list1.contains(i))
				diff.add(i);
		return diff;
	}
	
	private synchronized void setDynAttributeX(CyNode node, DynInterval<Double> interval)
	{
		KeyPairs key = new KeyPairs("node_X_Pos", node.getSUID());
		if (this.node_X_Pos.containsKey(key))
			this.node_X_Pos.get(key).addInterval(interval);
		else
			this.node_X_Pos.put(key, new DynDoubleAttribute(interval, key));
	}
	
	private synchronized void setDynAttributeY(CyNode node, DynInterval<Double> interval)
	{
		KeyPairs key = new KeyPairs("node_Y_Pos", node.getSUID());
		if (this.node_Y_Pos.containsKey(key))
			this.node_Y_Pos.get(key).addInterval(interval);
		else
			this.node_Y_Pos.put(key, new DynDoubleAttribute(interval, key));
	}
	
	private synchronized void setDynAttributeZ(CyNode node, DynInterval<Double> interval)
	{
		KeyPairs key = new KeyPairs("node_Z_Pos", node.getSUID());
		if (this.node_Z_Pos.containsKey(key))
			this.node_Z_Pos.get(key).addInterval(interval);
		else
			this.node_Z_Pos.put(key, new DynDoubleAttribute(interval, key));
	}

	@Override
	public double getAlpha() 
	{
		return alpha;
	}

	@Override
	public void setAlpha(double alpha) 
	{
		this.alpha = alpha;
	}

	@Override
	public int getN() 
	{
		return n;
	}

	@Override
	public void setN(int n) 
	{
		this.n = n;
	}
	
}
