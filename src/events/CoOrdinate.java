package events;

public class CoOrdinate {
	public int x;
	public int y;

	public CoOrdinate(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer("CoOrdinate: x: ");
		sb.append(x);
		sb.append(", y: ");
		sb.append(y);
		
		return sb.toString();
	}
}
