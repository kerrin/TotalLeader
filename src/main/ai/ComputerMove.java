package main.ai;

import main.events.CoOrdinate;

public class ComputerMove {
	public CoOrdinate from;
	public CoOrdinate to;
	public short units;
	
	public ComputerMove(CoOrdinate from, CoOrdinate to, short units) {
		super();
		this.from = from;
		this.to = to;
		this.units = units;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("ComputerMove: From: ");
		sb.append(from);
		sb.append(", To:");
		sb.append(to);
		sb.append(", Units: ");
		sb.append(units);
		
		return sb.toString();
	}
}
