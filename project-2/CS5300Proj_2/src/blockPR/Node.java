package blockPR;

public class Node {
	public int ID;
	public float PR;
	public int Degree;
	
	public Node(int id, float pr, int deg){
		this.ID = id;
		this.PR = pr;
		this.Degree = deg;
	}
	
	public Node(String line){
		line = line.trim();
		String[] parts = line.split(" ");
		this.ID = Integer.parseInt(parts[0]);
		this.PR = Float.parseFloat(parts[1]);
		this.Degree = Integer.parseInt(parts[2]);
	}
	
	@Override
	public String toString()
	{
		return ID + " " + PR + " " + Degree;
	}
}
