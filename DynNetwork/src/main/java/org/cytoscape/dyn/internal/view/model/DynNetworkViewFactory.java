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

package org.cytoscape.dyn.internal.view.model;

import org.cytoscape.dyn.internal.io.event.Sink;
import org.cytoscape.dyn.internal.model.DynNetwork;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;

/**
 * <code> DynNetworkViewFactory </code> is a the interface for the factory of
 * {@link DynNetworkView}s and is an event sink.
 * 
 * @author sabina
 *
 * @param <T>
 */
public interface DynNetworkViewFactory<T> extends Sink<T>
{
	/**
	 * Set node graphics attributes.
	 * @param dynNetwork
	 * @param currentNode
	 * @param type
	 * @param height
	 * @param width
	 * @param x
	 * @param y
	 * @param line width
	 * @param fill
	 * @param outline
	 */
	public void setNodeGraphics(DynNetwork<T> dynNetwork, CyNode currentNode, String type, String height, String width, String x, String y, String fill, String linew, String outline);
	
	/**
	 * Set edge graphics attributes.
	 * @param dynNetwork
	 * @param currentEdge
	 * @param width
	 * @param fill
	 */
	public void setEdgeGraphics(DynNetwork<T> dynNetwork, CyEdge currentEdge, String width, String fill);
	
}
