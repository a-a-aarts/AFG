package AFG;

//used as keys during the making of the graph
public class Pair {
	public final double a;
	public final double x;
	
	public Pair(Double a, Double x){
		this.a = a;
		this.x = x;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Double.hashCode(a) | Double.hashCode(x);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() != this.getClass()) return false;
		Pair p2 = (Pair) obj;
		return a == p2.a && x == p2.x;
	}
	
	
}
